package codingdojo.service.loader;

import codingdojo.domain.data.access.CustomerDataAccess;
import codingdojo.domain.data.model.Customer;
import codingdojo.domain.data.model.CustomerType;
import codingdojo.external.ExternalCustomer;
import codingdojo.service.exception.ConflictException;
import codingdojo.service.loader.CustomerMatches.CustomerDuplicate;

import static codingdojo.service.loader.CustomerMatches.CustomerDuplicate.EXISTING_CUSTOMER;
import static codingdojo.service.loader.CustomerMatches.CustomerDuplicate.NEW_CUSTOMER;

public class CompanyCustomerLoader {

    private final CustomerDataAccess customerDataAccess;

    public CompanyCustomerLoader(CustomerDataAccess customerDataAccess) {
        this.customerDataAccess = customerDataAccess;
    }

    public CustomerMatches loadCompanyCustomer(ExternalCustomer externalCustomer) {
        CustomerMatches matches = new CustomerMatches();
        Customer matchByExternalId = customerDataAccess.loadByExternalId(externalCustomer.getExternalId());
        if (matchByExternalId != null) {
            validateThenAppendMatchesByExternalId(matchByExternalId, matches, externalCustomer);
        } else {
            Customer matchByCompanyNumber = customerDataAccess.loadByCompanyNumber(externalCustomer.getCompanyNumber());
            validateThenAppendMatchesByCompanyNumber(matches, matchByCompanyNumber, externalCustomer);
        }
        return matches;
    }

    private void validateThenAppendMatchesByExternalId(Customer matchByExternalId, CustomerMatches matches,
                                                       ExternalCustomer externalCustomer) {
        validateType(matchByExternalId, externalCustomer);
        matches.setCustomer(matchByExternalId);
        appendNoMatchByCompanyNumber(matchByExternalId, matches, externalCustomer.getCompanyNumber());
        appendMatchesByMasterExternalId(matches, externalCustomer.getExternalId());
    }

    private void validateType(Customer matchByExternalId, ExternalCustomer externalCustomer) {
        if (!CustomerType.COMPANY.equals(matchByExternalId.getCustomerType())) {
            throw new ConflictException("Existing customer for externalCustomer " + externalCustomer.getExternalId() +
                    " already exists and is not a company");
        }
    }

    private void appendNoMatchByCompanyNumber(Customer matchByExternalId, CustomerMatches matches, String companyNumber) {
        if (companyNumber.equals(matchByExternalId.getCompanyNumber())) {
            return;
        }
        matches.getCustomer().setMasterExternalId(null);
        matches.addDuplicate(new CustomerDuplicate(EXISTING_CUSTOMER, matchByExternalId));
        matches.setCustomer(null);
    }

    private void appendMatchesByMasterExternalId(CustomerMatches matches, String externalId) {
        Customer matchByMasterId = customerDataAccess.loadByMasterExternalId(externalId);
        if (matchByMasterId != null) {
            matches.addDuplicate(new CustomerDuplicate(EXISTING_CUSTOMER, matchByMasterId));
        }
    }

    private void validateThenAppendMatchesByCompanyNumber(CustomerMatches matches, Customer matchByCompanyNumber,
                                                          ExternalCustomer externalCustomer) {
        if (matchByCompanyNumber == null) {
            return;
        }
        validateCustomerExternalId(matchByCompanyNumber.getExternalId(),
                externalCustomer.getExternalId(), externalCustomer.getCompanyNumber());

        matches.setMatchByCompany();
        matches.setCustomer(matchByCompanyNumber);
        matches.addDuplicate(new CustomerDuplicate(NEW_CUSTOMER, new Customer()));
    }

    private void validateCustomerExternalId(String customerExternalId, String externalId, String companyNumber) {
        if (customerExternalId != null && !externalId.equals(customerExternalId)) {
            throw new ConflictException("Existing customer for externalCustomer " + companyNumber +
                    " doesn't match external id " + externalId + " instead found " + customerExternalId);
        }
    }
}
