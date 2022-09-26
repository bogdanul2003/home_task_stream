package org.example.parser;

import org.example.model.CompanyNamesDto;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.example.parser.CompanyTags.KNOWN_TAGS;
import static org.example.parser.CompanyTags.loadExcludeTags;

public class CompanyParser {

    public CompanyParser() {
        loadExcludeTags();
    }

    public CompanyNamesDto extractCompanyNames(String line) {
        List<String> companyNames = getCompanyNames(getCompanyNameFromLine(line), 0);

        List<String> collect = companyNames.stream()
                .filter(companyName -> (!CompanyTags.EXCLUDE_TAGS.contains(companyName.toUpperCase().trim())
                        && companyName.length() > 0))
                .collect(toList());

        return new CompanyNamesDto(collect, getCompanyIdFromLine(line));
    }

    public Pair<Integer, Integer> findTerminalTag(String companyName) {
        Pair<Integer, Integer> tagPair = findLongestTerminalTag(companyName);
        int tagPos = getTerminalTagWordPosition(companyName, tagPair);

        return new Pair<>(tagPos, tagPair.getValue0());
    }


    private Integer getCompanyIdFromLine(String line) {
        return Integer.valueOf(line.substring(0, line.indexOf(";")));
    }

    private String getCompanyNameFromLine(String line) {
        return line.substring(line.indexOf(";") + 1);
    }

    private List<String> getCompanyNames(String rawCompanyName, int index) {
        List<String> result = new ArrayList<>();

        if (index > 10) {
            System.out.println("Company name has too many parenthesis ! " + rawCompanyName);
            return result;
        }

        if (rawCompanyName.toUpperCase().contains("DUPLICATE OF") || rawCompanyName.length() == 1) {
            return result;
        }

        for (int i = 0; i < rawCompanyName.length(); i++) {
            if (rawCompanyName.charAt(i) == '(') {
                int j = i + 1;
                int counter = 1;
                int closingIndex = -1;
                while (j < rawCompanyName.length() && counter > 0) {
                    if (rawCompanyName.charAt(j) == ')') {
                        counter--;
                        if (counter == 0) {
                            closingIndex = j;
                            break;
                        }
                    } else if (rawCompanyName.charAt(j) == '(') {
                        counter++;
                    }
                    j++;
                }

                closingIndex = closingIndex == -1 ? rawCompanyName.length() - 1 : closingIndex;
                int closingIndexStart = closingIndex;
                if (closingIndex < rawCompanyName.length() - 1 && rawCompanyName.charAt(closingIndex + 1) != ';') {
                    closingIndex++;
                }

                result.addAll(getCompanyNames(rawCompanyName.substring(0, i) +
                        rawCompanyName.substring(closingIndex + 1), index + 1));

                result.addAll(getCompanyNames(rawCompanyName.substring(i + 1, closingIndexStart), index + 1));
                return result;
            } else if (rawCompanyName.charAt(i) == ';' || rawCompanyName.charAt(i) == ',') {
                result.addAll(getCompanyNames(rawCompanyName.substring(0, i), index + 1));
                result.addAll(getCompanyNames(rawCompanyName.substring(i + 1), index + 1));

                return result;
            }
        }

        result.add(cleanStringBefore(rawCompanyName));
        return result;
    }


    private String cleanStringBefore(String string) {
        string = string.trim();
        int index = string.lastIndexOf('/');
        if ((index != -1 && string.substring(index).contains(" ")) || (string.contains("A/S") || string.contains("M/S"))) {
            index = -1;
        }
        string = string.substring(0, index == -1 ? string.length() : index);
        String upper = string.toUpperCase();
        int tagLength = KNOWN_TAGS.stream().reduce(0,
                (prev, s) -> upper.contains(s) && s.length() > prev ? s.length() + 1 : prev,
                (a, b) -> a > b ? a : b);

        return string.substring(Math.min(tagLength, string.length())).replaceAll("[^a-zA-Z0-9  '/-]", "");
    }

    private int getTerminalTagWordPosition(String companyName, Pair<Integer, Integer> tagPair) {
        int tagPos = -1;
        if (tagPair.getValue1() + tagPair.getValue0() == companyName.length()) {
            if (tagPair.getValue0() > 0 && companyName.charAt(tagPair.getValue0() - 1) != ' ') {
                tagPos = -1;
            } else {
                tagPos = tagPair.getValue0() - companyName.substring(0, tagPair.getValue0()).replace(" ", "").length();
            }
        }
        return tagPos;
    }

    private Pair<Integer, Integer> findLongestTerminalTag(String companyName) {
        return CompanyTags.TERMINAL_TAGS.stream().reduce(new Pair<>(-1, 0),
                (pair, tag) -> findLongestTag(pair, tag, companyName),
                (pair1, pair2) -> pair1.getValue1() > pair2.getValue1() ? pair1 : pair2);
    }

    private Pair<Integer, Integer> findLongestTag(Pair<Integer, Integer> pair, String tag, String string) {
        int index = string.toUpperCase().lastIndexOf(tag);
        if (index != -1 && tag.length() > pair.getValue1()) {
            return new Pair<>(index, tag.length());
        }

        return pair;
    }

}
