package codingdojo.domain.data.access;

import codingdojo.domain.data.model.Customer;
import codingdojo.domain.data.model.ShoppingList;

public interface CustomerDataLayer {

    Customer updateCustomerRecord(Customer customer);

    Customer createCustomerRecord(Customer customer);

    void updateShoppingList(ShoppingList consumerShoppingList);

    Customer findByExternalId(String externalId);

    Customer findByMasterExternalId(String externalId);

    Customer findByCompanyNumber(String companyNumber);
}
