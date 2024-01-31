package codingdojo.service.sync;

import codingdojo.domain.data.access.CustomerDataAccess;
import codingdojo.domain.data.model.Customer;
import codingdojo.domain.data.model.CustomerType;
import codingdojo.external.ExternalCustomer;
import codingdojo.service.loader.CompanyCustomerLoader;
import codingdojo.service.loader.CustomerMatches;

class CompanyCustomerSync implements TypeCustomerSync {

    private final CompanyCustomerLoader companyCustomerLoader;

    CompanyCustomerSync(CustomerDataAccess customerDataAccess) {
        companyCustomerLoader = new CompanyCustomerLoader(customerDataAccess);
    }

    public CustomerMatches loadThenSyncCustomData(ExternalCustomer externalCustomer) {
        CustomerMatches customerMatches = companyCustomerLoader.loadCompanyCustomer(externalCustomer);
        Customer customer = customerMatches.getCustomer();

        if (customerMatches.isNewCustomer()) {
            customer = new Customer();
            customer.setExternalId(externalCustomer.getExternalId());
            customer.setMasterExternalId(externalCustomer.getExternalId());
            customerMatches.setCustomer(customer);
        } else if (customerMatches.isMatchByCompany()) {
            customer.setExternalId(externalCustomer.getExternalId());
            customer.setMasterExternalId(externalCustomer.getExternalId());
        }

        customer.setCompanyNumber(externalCustomer.getCompanyNumber());
        customer.setCustomerType(CustomerType.COMPANY);
        return customerMatches;
    }
}
