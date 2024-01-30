package codingdojo.service.loader;

import codingdojo.domain.data.access.CustomerDataAccess;
import codingdojo.domain.data.model.Customer;
import codingdojo.domain.data.model.CustomerType;
import codingdojo.external.ExternalCustomer;
import codingdojo.service.exception.ConflictException;

public class PersonCustomerLoader {

    private final CustomerDataAccess customerDataAccess;

    public PersonCustomerLoader(CustomerDataAccess customerDataAccess) {
        this.customerDataAccess = customerDataAccess;
    }

    public CustomerMatches loadPerson(ExternalCustomer externalCustomer) {
        CustomerMatches matches = new CustomerMatches();
        Customer matchByPersonalNumber = customerDataAccess.loadByExternalId(externalCustomer.getExternalId());
        if (matchByPersonalNumber == null) {
            return matches;
        }
        validateType(externalCustomer, matchByPersonalNumber);
        matches.setCustomer(matchByPersonalNumber);
        return matches;
    }

    private void validateType(ExternalCustomer externalCustomer, Customer matchByPersonalNumber) {
        if (!CustomerType.PERSON.equals(matchByPersonalNumber.getCustomerType())) {
            throw new ConflictException("Existing customer for externalCustomer " + externalCustomer.getExternalId()
                    + " already exists and is not a person");
        }
    }
}
