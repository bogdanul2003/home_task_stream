package org.example.storage;

import org.example.model.CompanyDto;
import org.example.model.CompanyNamesDto;
import org.example.parser.CompanyParser;
import org.example.parser.CompanyTags;
import org.javatuples.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class CompaniesHash implements CompaniesHashInterface {
    private class ThreadDetails {
        ThreadDetails(CompaniesTrieNode start) {
            currentStream = start;
        }

        public CompaniesTrieNode currentStream;

        public int depth = 0;

        public boolean matchedRaw = false;
    }

    private CompaniesTrieNode root = new CompaniesTrieNode();

    private HashMap<String, ThreadDetails> registeredThreads = new HashMap<>();

    private CompanyParser companyParser = new CompanyParser();

    @Override
    public void loadCompanies(String fileName) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                addToTrie(companyParser.extractCompanyNames(line));
            }
        }
    }

    @Override
    public void insertCompany(String rawName, Integer companyId) {
        addToTrie(companyParser.extractCompanyNames(companyId + ";" + rawName));
    }

    @Override
    public List<CompanyDto> getStoredCompanies(boolean onlyFoundInNews) {
        List<CompanyDto> result = new ArrayList<>();
        consumeCompanies(root, companyNode -> result.add(nodeToDto(companyNode)), onlyFoundInNews);

        return result;
    }

    @Override
    public Boolean isPartOfCompany(String stringFragment, String threadId) {
        if (stringFragment.isEmpty() || !registeredThreads.containsKey(threadId)) {
            return false;
        }

        var thread = registeredThreads.get(threadId);
        String matcher = stringFragment.toUpperCase();
        if (thread.currentStream == root) {
            if (thread.currentStream.getOtherNodes().containsKey(stringFragment)) {
                thread.matchedRaw = true;
            }
            matcher = stringFragment;
        }

        if (thread.currentStream.getOtherNodes().containsKey(matcher)) {
            thread.currentStream = thread.currentStream.getOtherNodes().get(matcher);
            thread.depth++;
            return true;
        }

        return false;
    }

    @Override
    public Optional<Integer> getCompanyId(String threadId) {
        if (!registeredThreads.containsKey(threadId))
            return empty();

        var thread = registeredThreads.get(threadId);
        if (thread.currentStream.getCurrentString() == null || (thread.depth == 1 && !thread.matchedRaw)) {
            return empty();
        } else {
            thread.currentStream.setFoundCompany(true);
            return of(thread.currentStream.getCompanyId());
        }
    }

    public void registerThread(String threadId) {
        registeredThreads.put(threadId, new ThreadDetails(root));
    }

    public void resetReading(String threadId) {
        if (!registeredThreads.containsKey(threadId)) {
            return;
        }

        var thread = registeredThreads.get(threadId);
        thread.depth = 0;
        thread.matchedRaw = false;
        thread.currentStream = root;
    }

    private CompanyDto nodeToDto(CompaniesTrieNode companyNode) {
        return new CompanyDto(companyNode.getCurrentString(), companyNode.getCompanyIds(), companyNode.getFoundCompany().get());
    }

    private void addToTrie(CompanyNamesDto companyNames) {
        CompaniesTrieNode current = root;

        for (String companyName : companyNames.getCompanyNames()) {
            var arr = companyName.split(" ");
            Pair<Integer, Integer> terminalTag = companyParser.findTerminalTag(companyName);
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].isEmpty()) {
                    continue;
                }
                current = current.getOtherNodes()
                        .computeIfAbsent(current == root ? arr[i] : arr[i].toUpperCase(), c -> new CompaniesTrieNode());

                if (i == terminalTag.getValue0() - 1) {
                    var tag = companyName.substring(0, terminalTag.getValue1()).trim();
                    if (!CompanyTags.EXCLUDE_TAGS.contains(tag.toUpperCase())) {
                        current.setCompanyId(companyNames.getCompanyId());
                        current.setCurrentString(tag);
                    }
                }
            }

            current.setCompanyId(companyNames.getCompanyId());
            current.setCurrentString(companyName);
            current = root;
        }
    }

    private void consumeCompanies(CompaniesTrieNode root, Consumer<CompaniesTrieNode> consume, boolean foundOnly) {
        if (root.getCurrentString() != null) {
            if (foundOnly) {
                root.getFoundCompany().ifPresent(f -> {
                    if(f == true) {
                        consume.accept(root);
                    }
                });
            } else {
                consume.accept(root);
            }
        }

        if (root.getOtherNodes().isEmpty())
            return;

        root.getOtherNodes().forEach((key, value) -> consumeCompanies(value, consume, foundOnly));
    }

}
