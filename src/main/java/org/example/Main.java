package org.example;

import org.example.storage.CompaniesHash;
import org.example.writer.OutputWriter;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException {
        if (args.length != 2) {
            System.out.println("Usage: app [companies_file] [news_folder]");
            return;
        }

        CompaniesHash hash = new CompaniesHash();
        hash.loadCompanies(args[0]);

        CompaniesProcessorController controller = new CompaniesProcessorController(hash);
        Set<Integer> result = controller.processNews(args[1]);
        System.out.println("Parallel found " + result.size() + " companies in " + controller.getLastDuration());

        OutputWriter.printContentToFile(args[0], hash.getStoredCompanies(true));
    }

}