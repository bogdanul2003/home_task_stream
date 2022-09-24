package org.example;

import java.util.*;

// Trie that holds words as keys instead of chars
public class CompaniesTrieNode {
    private final Map<String, CompaniesTrieNode> otherNodes = new HashMap<>();

    Optional<CompanyDto> company = Optional.empty();

    private Optional<Boolean> foundCompany = Optional.empty();

    public Map<String, CompaniesTrieNode> getOtherNodes() {
        return otherNodes;
    }


    public Integer getCompanyId() {
        return company.isPresent() && company.get().getCompanyIds().size() == 0? null : company.get().getCompanyIds().stream().findFirst().get();
    }

    public List<Integer> getCompanyIds() {
        return company.isEmpty()? new ArrayList<>() : new ArrayList<>(company.get().getCompanyIds());
    }

    public void setCompanyId(Integer companyId) {
        company.ifPresentOrElse( c-> c.getCompanyIds().add(companyId), () -> {
            var c = new CompanyDto();
            c.getCompanyIds().add(companyId);
            company = Optional.of(c);
        });
    }

    public String getCurrentString() {
        return company.isEmpty() ? null : company.get().getCurrentString();
    }

    public void setCurrentString(String currentString) {
        company.ifPresentOrElse( c-> c.setCurrentString(currentString), () -> {
            var c = new CompanyDto();
            c.setCurrentString(currentString);
            company = Optional.of(c);
        });
    }

    public Optional<Boolean> getFoundCompany() {
        return foundCompany;
    }

    public void setFoundCompany(Boolean foundCompany) {
        this.foundCompany = Optional.of(foundCompany);
    }
}
