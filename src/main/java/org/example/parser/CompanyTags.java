package org.example.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class CompanyTags {

    public final static List<String> KNOWN_TAGS = new ArrayList<>(Arrays.asList("ALSO KNOWN AS",
            "FORMERLY", "FORMERLY KNOWN AS", "ALSO", "PLEASE REFER TO", "REFER TO"));

    public final static List<String> TERMINAL_TAGS = new ArrayList<>(Arrays.asList("INC", "AG", "CORP", "CO LTD", "BV",
            "SA", "LLC", "CO", "CO L", "LTD", "LIMITED", "COMPANY LIMITED", "HOLDINGS LTD", "HOLDINGS LIMITED", "LTDA", "A/S", "M/S",
            "Co JSC", "JSC", "OJSC", "CJSC", "FGUP", "GUP", "CMC", "PT", "HOLDINGS", "BVI LTD", "PTY LTD", "PVT LTD"));

    public final static HashSet<String> EXCLUDE_TAGS = new HashSet<>(Arrays.asList("THE", "AS", "NEWS", "M/S", "OAO", "OOO", "JSC",
            "OJSC", "CJSC", "FGUP", "GUP", "STICHTING", "PAO", "PAT", "TOO", "ZAO", "CMC", "IPC", "LLC", "MUP", "OLD", "CCI",
            "CPI", "GROUP", "PT", "HOLDINGS", "BVI", "INC", "LTD", "PTY", "PVT", "EL", "HOLDING"));


    public static void loadExcludeTags() {
        CompanyTagsReader companyTagsReader = new CompanyTagsReader();
        EXCLUDE_TAGS.addAll(companyTagsReader.readTags());
    }

}
