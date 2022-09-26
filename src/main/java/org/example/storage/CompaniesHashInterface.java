package org.example.storage;

import org.example.model.CompanyDto;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

public interface CompaniesHashInterface {
    void loadCompanies(String fileName) throws FileNotFoundException;

    void insertCompany(String rawName, Integer companyId);

    Optional<Integer> getCompanyId(String threadId);

    Boolean isPartOfCompany(String stringFragment, String threadId);

    List<CompanyDto> getStoredCompanies(boolean onlyFound);
}
