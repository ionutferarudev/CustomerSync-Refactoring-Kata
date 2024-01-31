package codingdojo.domain.data.access;

import codingdojo.domain.data.model.Customer;
import codingdojo.domain.data.model.ShoppingList;

import static codingdojo.domain.data.access.CustomerDataAccess.CreateOrUpdateRecordResult.CREATED;
import static codingdojo.domain.data.access.CustomerDataAccess.CreateOrUpdateRecordResult.UPDATED;

public class CustomerDataAccess {

    private final CustomerDataLayer customerDataLayer;

    public CustomerDataAccess(CustomerDataLayer customerDataLayer) {
        this.customerDataLayer = customerDataLayer;
    }

    public Customer loadByCompanyNumber(String companyNumber) {
        return this.customerDataLayer.findByCompanyNumber(companyNumber);
    }

    public Customer loadByMasterExternalId(String masterExternalId) {
        return this.customerDataLayer.findByMasterExternalId(masterExternalId);
    }

    public Customer loadByExternalId(String externalId) {
        return this.customerDataLayer.findByExternalId(externalId);
    }

    public Customer updateCustomerRecord(Customer customer) {
        return customerDataLayer.updateCustomerRecord(customer);
    }

    public Customer createCustomerRecord(Customer customer) {
        return customerDataLayer.createCustomerRecord(customer);
    }

    public CreateOrUpdateRecordResult createOrUpdateRecord(Customer customer) {
        if (customer.getInternalId() == null) {
            return new CreateOrUpdateRecordResult(CREATED, createCustomerRecord(customer));
        }
        return new CreateOrUpdateRecordResult(UPDATED, updateCustomerRecord(customer));
    }

    public void updateShoppingList(Customer customer, ShoppingList consumerShoppingList) {
        customer.addShoppingList(consumerShoppingList);
        customerDataLayer.updateShoppingList(consumerShoppingList);
        customerDataLayer.updateCustomerRecord(customer);
    }

    public static class CreateOrUpdateRecordResult {
        public static final boolean CREATED = true;
        public static final boolean UPDATED = false;

        private final boolean created;
        private final Customer customer;

        public CreateOrUpdateRecordResult(boolean created, Customer customer) {
            this.created = created;
            this.customer = customer;
        }

        public Customer getCustomer() {
            return customer;
        }

        public boolean isCreated() {
            return created;
        }
    }
}
