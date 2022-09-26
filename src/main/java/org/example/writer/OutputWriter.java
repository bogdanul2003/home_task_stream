package org.example.writer;

import org.example.model.CompanyDto;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class OutputWriter {

    public static void printContentToFile(String fileName, List<CompanyDto> companyDtos) throws IOException {
        if (fileName == null) {
            return;
        }

        String outputFileName = getOutputFileName(fileName);
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(outputFileName))) {
            companyDtos.forEach(companyDto ->
                    printWriter.println(companyDto.getCurrentString() + ";" + companyDto.getCompanyIds() + ";" + companyDto.getFound()));
        }
    }

    private static String getOutputFileName(String fileName) {
        return fileName.substring(0, fileName.indexOf('.')) + "out.csv";
    }

}
