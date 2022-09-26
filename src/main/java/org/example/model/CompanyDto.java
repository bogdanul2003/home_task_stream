package org.example.model;

import java.util.HashSet;
import java.util.Set;

public class CompanyDto {

    private String currentString;
    private Set<Integer> companyIds = new HashSet<>();

    private Boolean found = Boolean.FALSE;

    public CompanyDto() {
    }

    public CompanyDto(String name, Integer id, Boolean found) {
        currentString = name;
        companyIds.add(id);
        this.found = found;
    }

    public CompanyDto(String currentString, Set<Integer> companyIds, Boolean found) {
        this.currentString = currentString;
        this.companyIds = companyIds;
        this.found = found;
    }

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

    public Boolean getFound() {
        return found;
    }

    public void setFound(Boolean found) {
        this.found = found;
    }
}
