package codingdojo.service.sync;

import codingdojo.domain.data.access.CustomerDataAccess;
import codingdojo.domain.data.model.Customer;
import codingdojo.domain.data.model.CustomerType;
import codingdojo.external.ExternalCustomer;
import codingdojo.service.loader.CustomerMatches;
import codingdojo.service.loader.PersonCustomerLoader;

import java.util.Objects;

class PersonCustomerSync implements TypeCustomerSync {

    private final PersonCustomerLoader personCustomerLoader;

    PersonCustomerSync(CustomerDataAccess customerDataAccess) {
        personCustomerLoader = new PersonCustomerLoader(customerDataAccess);
    }

    public CustomerMatches loadThenSyncCustomData(ExternalCustomer externalCustomer) {
        CustomerMatches customerMatches = personCustomerLoader.loadPerson(externalCustomer);
        Customer customer = customerMatches.getCustomer();
        if (customer == null) {
            customer = new Customer();
            customer.setExternalId(externalCustomer.getExternalId());
            customer.setMasterExternalId(externalCustomer.getExternalId());
            customerMatches.setCustomer(customer);
        }
        if (!Objects.equals(externalCustomer.getBonusPointsBalance(), customer.getBonusPointsBalance())) {
            customer.setBonusPointsBalance(externalCustomer.getBonusPointsBalance());
        }
        customer.setCustomerType(CustomerType.PERSON);
        return customerMatches;
    }
}
