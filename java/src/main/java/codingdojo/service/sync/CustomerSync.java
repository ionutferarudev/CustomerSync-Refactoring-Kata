package codingdojo.service.sync;

import codingdojo.domain.data.access.CustomerDataAccess;
import codingdojo.domain.data.access.CustomerDataAccess.CreateOrUpdateRecordResult;
import codingdojo.domain.data.access.CustomerDataLayer;
import codingdojo.domain.data.model.Customer;
import codingdojo.domain.data.model.CustomerType;
import codingdojo.domain.data.model.ShoppingList;
import codingdojo.external.ExternalCustomer;
import codingdojo.service.loader.CustomerMatches;
import codingdojo.service.loader.CustomerMatches.CustomerDuplicate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerSync {

    private final CustomerDataAccess customerDataAccess;

    private final Map<CustomerType, TypeCustomerSync> typeCustomerSyncMap = new HashMap<>();

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this(new CustomerDataAccess(customerDataLayer));
    }

    private CustomerSync(CustomerDataAccess customerDataAccess) {
        this.customerDataAccess = customerDataAccess;
        typeCustomerSyncMap.put(CustomerType.COMPANY, new CompanyCustomerSync(this.customerDataAccess));
        typeCustomerSyncMap.put(CustomerType.PERSON, new PersonCustomerSync(this.customerDataAccess));
    }

    public boolean syncWithDataLayer(ExternalCustomer externalCustomer) {
        CustomerType customerType = externalCustomer.isCompany() ? CustomerType.COMPANY : CustomerType.PERSON;
        CustomerMatches customerMatches = typeCustomerSyncMap.get(customerType).loadThenSyncCustomData(externalCustomer);

        CreateOrUpdateRecordResult createOrUpdateRecordResult = createOrUpdateCustomerData(externalCustomer, customerMatches);
        updateOrCreateDuplicates(externalCustomer, customerMatches);
        updateShoppingList(externalCustomer, createOrUpdateRecordResult.getCustomer());
        return createOrUpdateRecordResult.isCreated();
    }

    private CreateOrUpdateRecordResult createOrUpdateCustomerData(ExternalCustomer externalCustomer, CustomerMatches customerMatches) {
        Customer customer = customerMatches.getCustomer();
        customer.setName(externalCustomer.getName());
        customer.setPreferredStore(externalCustomer.getPreferredStore());
        customer.setAddress(externalCustomer.getPostalAddress());
        return customerDataAccess.createOrUpdateRecord(customer);
    }

    private void updateOrCreateDuplicates(ExternalCustomer externalCustomer, CustomerMatches customerMatches) {
        if (customerMatches.hasDuplicates()) {
            customerMatches.getDuplicates()
                    .forEach(duplicate -> updateOrCreateDuplicate(duplicate, externalCustomer));
        }
    }

    private void updateOrCreateDuplicate(CustomerDuplicate duplicate, ExternalCustomer externalCustomer) {
        Customer customer = duplicate.getCustomer();
        if (duplicate.isNew()) {
            customer.setExternalId(externalCustomer.getExternalId());
            customer.setMasterExternalId(externalCustomer.getExternalId());
        }
        customer.setName(externalCustomer.getName());
        customerDataAccess.createOrUpdateRecord(customer);
    }

    private void updateShoppingList(ExternalCustomer externalCustomer, Customer customer) {
        List<ShoppingList> consumerShoppingLists = externalCustomer.getShoppingLists();
        consumerShoppingLists.forEach(consumerShoppingList ->
                customerDataAccess.updateShoppingList(customer, consumerShoppingList));
    }
}
