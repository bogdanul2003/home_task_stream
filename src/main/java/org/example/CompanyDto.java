package org.example;

import java.util.HashSet;
import java.util.Set;

public class CompanyDto {
    private String currentString;
    private Set<Integer> companyIds = new HashSet<>();

    public CompanyDto(String name, Integer id) {
        currentString = name;
        companyIds.add(id);
    }

    public CompanyDto() {}

    public String getCurrentString() {
        return currentString;
    }

    public void setCurrentString(String currentString) {
        this.currentString = currentString;
    }

    public Set<Integer> getCompanyIds() {
        return companyIds;
    }

    public void setCompanyIds(Set<Integer> companyIds) {
        this.companyIds = companyIds;
    }
}
