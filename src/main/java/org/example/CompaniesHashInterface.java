package org.example;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

public interface CompaniesHashInterface {
    void insertCompanies(String fileName) throws FileNotFoundException;
    void insertCompany(String rawName, Integer companyId);
    Boolean isPartOfCompany(String stringFragment, String threadId);
    Optional<Integer> getCompanyId(String threadId);

    List<CompanyDto> getStoredCompanies(boolean onlyFound);
}
