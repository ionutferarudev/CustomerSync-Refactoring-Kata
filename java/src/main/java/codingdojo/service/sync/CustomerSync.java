package codingdojo.service.sync;

import codingdojo.domain.data.access.CustomerDataAccess;
import codingdojo.domain.data.access.CustomerDataLayer;
import codingdojo.domain.data.model.Customer;
import codingdojo.domain.data.model.CustomerType;
import codingdojo.domain.data.model.ShoppingList;
import codingdojo.external.ExternalCustomer;
import codingdojo.service.loader.CustomerMatches;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerSync {

    protected final CustomerDataAccess customerDataAccess;

    private final Map<CustomerType, TypeCustomerSync> typeCustomerSyncMap = new HashMap<>();

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this(new CustomerDataAccess(customerDataLayer));
    }

    CustomerSync(CustomerDataAccess customerDataAccess) {
        this.customerDataAccess = customerDataAccess;
        typeCustomerSyncMap.put(CustomerType.COMPANY, new CompanyCustomerSync(this.customerDataAccess));
        typeCustomerSyncMap.put(CustomerType.PERSON, new PersonCustomerSync(this.customerDataAccess));
    }

    public boolean syncWithDataLayer(ExternalCustomer externalCustomer) {
        CustomerType customerType = externalCustomer.isCompany() ? CustomerType.COMPANY : CustomerType.PERSON;
        CustomerMatches customerMatches = typeCustomerSyncMap.get(customerType).loadThenSyncCustomData(externalCustomer);
        Customer customer = customerMatches.getCustomer();
        customer.setName(externalCustomer.getName());
        customer.setPreferredStore(externalCustomer.getPreferredStore());
        customer.setAddress(externalCustomer.getPostalAddress());
        CustomerDataAccess.CreateOrUpdateRecordResult createOrUpdateRecordResult =
                customerDataAccess.createOrUpdateRecord(customer);
        updateOrCreateDuplicates(externalCustomer, customerMatches);
        updateShoppingList(externalCustomer, createOrUpdateRecordResult.getCustomer());
        return createOrUpdateRecordResult.isCreated();
    }

    private void updateOrCreateDuplicates(ExternalCustomer externalCustomer, CustomerMatches customerMatches) {
        if (!customerMatches.hasDuplicates()) {
            return;
        }
        customerMatches.getDuplicates().forEach(duplicate -> updateOrCreateDuplicate(duplicate, externalCustomer));
    }

    private void updateOrCreateDuplicate(Customer duplicate, ExternalCustomer externalCustomer) {
        if (duplicate == null) {
            duplicate = new Customer();
            duplicate.setExternalId(externalCustomer.getExternalId());
            duplicate.setMasterExternalId(externalCustomer.getExternalId());
        }
        duplicate.setName(externalCustomer.getName());
        customerDataAccess.createOrUpdateRecord(duplicate);
    }

    private void updateShoppingList(ExternalCustomer externalCustomer, Customer customer) {
        List<ShoppingList> consumerShoppingLists = externalCustomer.getShoppingLists();
        consumerShoppingLists.forEach(consumerShoppingList ->
                customerDataAccess.updateShoppingList(customer, consumerShoppingList));
    }
}
