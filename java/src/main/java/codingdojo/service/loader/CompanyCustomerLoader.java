package codingdojo.service.loader;

import codingdojo.domain.data.access.CustomerDataAccess;
import codingdojo.domain.data.model.Customer;
import codingdojo.domain.data.model.CustomerType;
import codingdojo.external.ExternalCustomer;
import codingdojo.service.exception.ConflictException;

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

        String externalId = externalCustomer.getExternalId();
        String companyNumber = externalCustomer.getCompanyNumber();

        matches.setCustomer(matchByExternalId);
        if (!companyNumber.equals(matchByExternalId.getCompanyNumber())) {
            matches.getCustomer().setMasterExternalId(null);
            matches.addDuplicate(matchByExternalId);
            matches.setCustomer(null);
        }
        appendMatchesByMasterExternalId(matches, externalId);
    }

    private void validateType(Customer matchByExternalId, ExternalCustomer externalCustomer) {
        if (!CustomerType.COMPANY.equals(matchByExternalId.getCustomerType())) {
            throw new ConflictException("Existing customer for externalCustomer " + externalCustomer.getExternalId() +
                    " already exists and is not a company");
        }
    }

    private void appendMatchesByMasterExternalId(CustomerMatches matches, String externalId) {
        Customer matchByMasterId = customerDataAccess.loadByMasterExternalId(externalId);
        if (matchByMasterId != null) {
            matches.addDuplicate(matchByMasterId);
        }
    }

    private void validateThenAppendMatchesByCompanyNumber(CustomerMatches matches, Customer matchByCompanyNumber,
                                                          ExternalCustomer externalCustomer) {
        if (matchByCompanyNumber == null) {
            return;
        }
        String externalId = externalCustomer.getExternalId();
        String companyNumber = externalCustomer.getCompanyNumber();

        validateCustomerExternalId(matchByCompanyNumber.getExternalId(), externalId, companyNumber);

        matchByCompanyNumber.setExternalId(externalId);
        matchByCompanyNumber.setMasterExternalId(externalId);
        matches.setCustomer(matchByCompanyNumber);
        matches.addDuplicate(null);
    }

    private void validateCustomerExternalId(String customerExternalId, String externalId, String companyNumber) {
        if (customerExternalId != null && !externalId.equals(customerExternalId)) {
            throw new ConflictException("Existing customer for externalCustomer " + companyNumber +
                    " doesn't match external id " + externalId + " instead found " + customerExternalId);
        }
    }
}
