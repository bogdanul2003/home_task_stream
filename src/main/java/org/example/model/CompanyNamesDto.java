package org.example.model;

import java.util.List;

public class CompanyNamesDto {

    private List<String> companyNames;
    private Integer companyId;

    public CompanyNamesDto(List<String> companyNames, Integer companyId) {
        this.companyNames = companyNames;
        this.companyId = companyId;
    }

    public List<String> getCompanyNames() {
        return companyNames;
    }

    public void setCompanyNames(List<String> companyNames) {
        this.companyNames = companyNames;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }
}
