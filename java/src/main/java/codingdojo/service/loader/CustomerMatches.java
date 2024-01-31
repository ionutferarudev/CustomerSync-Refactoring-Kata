package codingdojo.service.loader;

import codingdojo.domain.data.model.Customer;

import java.util.ArrayList;
import java.util.Collection;

public class CustomerMatches {
    private final Collection<CustomerDuplicate> duplicates = new ArrayList<>();
    private Customer customer;

    private boolean matchByCompany = false;

    public Customer getCustomer() {
        return customer;
    }

    public boolean isNewCustomer() {
        return customer == null;
    }

    public boolean isMatchByCompany() {
        return matchByCompany;
    }

    public void setMatchByCompany() {
        matchByCompany = true;
    }

    public boolean hasDuplicates() {
        return !duplicates.isEmpty();
    }

    public void addDuplicate(CustomerDuplicate duplicate) {
        duplicates.add(duplicate);
    }

    public Collection<CustomerDuplicate> getDuplicates() {
        return duplicates;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public static class CustomerDuplicate {
        public static final boolean EXISTING_CUSTOMER = true;
        public static final boolean NEW_CUSTOMER = false;

        private final boolean created;
        private final Customer customer;

        public CustomerDuplicate(boolean created, Customer customer) {
            this.created = created;
            this.customer = customer;
        }

        public Customer getCustomer() {
            return customer;
        }

        public boolean isNew() {
            return !created;
        }
    }
}
