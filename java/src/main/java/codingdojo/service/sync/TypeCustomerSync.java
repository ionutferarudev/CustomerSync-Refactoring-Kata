package codingdojo.service.sync;

import codingdojo.external.ExternalCustomer;
import codingdojo.service.loader.CustomerMatches;

public interface TypeCustomerSync {
    CustomerMatches loadThenSyncCustomData(ExternalCustomer externalCustomer);
}
