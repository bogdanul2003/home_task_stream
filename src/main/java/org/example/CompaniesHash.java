package org.example;

import org.javatuples.Pair;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class CompaniesHash implements CompaniesHashInterface{
    private class ThreadDetails {
        ThreadDetails(CompaniesTrieNode start)
        {
            currentStream = start;
        }

        public CompaniesTrieNode currentStream;

        public int depth = 0;

        public boolean matchedRaw = false;
    }

    private CompaniesTrieNode root = new CompaniesTrieNode();

    private final static List<String> KnownTags = new ArrayList<>(Arrays.asList("ALSO KNOWN AS" ,
            "FORMERLY", "FORMERLY KNOWN AS", "ALSO", "PLEASE REFER TO", "REFER TO"));

    private final static List<String> TerminalTags = new ArrayList<>(Arrays.asList("INC", "AG", "CORP", "CO LTD", "BV",
            "SA", "LLC", "CO", "CO L", "LTD", "LIMITED", "COMPANY LIMITED", "HOLDINGS LTD", "HOLDINGS LIMITED", "LTDA", "A/S", "M/S",
            "Co JSC", "JSC", "OJSC", "CJSC", "FGUP", "GUP", "CMC", "PT", "HOLDINGS", "BVI LTD", "PTY LTD", "PVT LTD"));

    private final static HashSet<String> ExcludeTags = new HashSet<>(Arrays.asList("THE", "AS", "NEWS", "M/S", "OAO", "OOO", "JSC",
            "OJSC", "CJSC", "FGUP", "GUP", "STICHTING", "PAO", "PAT", "TOO", "ZAO", "CMC", "IPC", "LLC", "MUP", "OLD", "CCI",
            "CPI", "GROUP", "PT", "HOLDINGS", "BVI", "INC", "LTD", "PTY", "PVT", "EL", "HOLDING"));
    private String fileName;

    private HashMap<String, ThreadDetails> registeredThreads = new HashMap<>();

    public CompaniesHash() throws FileNotFoundException, URISyntaxException {
        URL resource = getClass().getClassLoader().getResource("countries.csv");
        try (Scanner scanner = new Scanner(new File(resource.toURI()));) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                var countries = line.split(";");
                ExcludeTags.add(countries[0]);
                ExcludeTags.add(countries[1]);
            }
        }
    }

    @Override
    public void insertCompanies(String fileName) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(new File(fileName));) {
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Integer id = getCompanyIdFromLine(line);
                insertCompany(getCompanyName(line), id);
            }
        }

        this.fileName = fileName.substring(0, fileName.indexOf('.')) + "out.csv";
    }

    @Override
    public void insertCompany(String rawName, Integer companyId) {
        addToTrie(getCompanyNames(rawName, 0), companyId);
    }

    @Override
    public Boolean isPartOfCompany(String stringFragment, String threadId) {
        if(stringFragment.isEmpty() || !registeredThreads.containsKey(threadId)) {
            return false;
        }


            var thread = registeredThreads.get(threadId);
            String upper = stringFragment.toUpperCase();
            String matcher = upper;
            if (thread.currentStream == root) {
                if (thread.currentStream.getOtherNodes().containsKey(stringFragment))
                    thread.matchedRaw = true;
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
                return Optional.empty();

            var thread = registeredThreads.get(threadId);
            if (thread.currentStream.getCurrentString() == null
                    || (thread.depth == 1 && !thread.matchedRaw)) {
                return Optional.empty();
            } else {
                thread.currentStream.setFoundCompany(true);
                return Optional.of(thread.currentStream.getCompanyId());
            }
    }

    public void registerThread(String threadId)
    {
        registeredThreads.put(threadId, new ThreadDetails(root));
    }

    public void resetReading(String threadId)
    {
            if (!registeredThreads.containsKey(threadId))
                return;

            var thread = registeredThreads.get(threadId);
            thread.depth = 0;
            thread.matchedRaw = false;
            thread.currentStream = root;
    }

    public void printContentToConsole() throws IOException {
        PrintWriter pw = new PrintWriter(System.out);
        printPrivate(root, pw);
        pw.flush();
    }

    public void printContentToFile() throws IOException {
        PrintWriter pw = null;
        if(fileName != null)
            pw = new PrintWriter(new FileWriter(fileName));
        printPrivate(root, pw);
        if(fileName!=null)
            pw.close();
    }

    public Set<Integer> getFoundCompanies()
    {
        var result = new HashSet<Integer>();
        getFoundCompanies(root, result);
        return result;
    }

    @Override
    public List<CompanyDto> getStoredCompanies(boolean onlyFound) {
        var result = new ArrayList<CompanyDto>();

        printPrivate(root, c -> result.add(new CompanyDto(c.getCurrentString(), c.getCompanyId())), onlyFound);

        return result;
    }

    private void getFoundCompanies(CompaniesTrieNode root, Set<Integer> result)
    {
        root.getFoundCompany().ifPresent( f -> result.add(root.getCompanyId()));

        if(root.getOtherNodes().isEmpty())
            return;

        for(var chld : root.getOtherNodes().entrySet()) {
            getFoundCompanies(chld.getValue(), result);
        }
    }

    private Integer getCompanyIdFromLine(String line) {
        return Integer.valueOf(line.substring(0, line.indexOf(";")));
    }

    private String getCompanyName(String line) {
        return line.substring(line.indexOf(";") + 1);
    }
    private List<String> getCompanyNames(String rawCompanyName, int index)
    {
        List<String> result = new ArrayList<>();

        if(index > 10) {
            System.out.println("Company name has too many parenthesis ! " + rawCompanyName);
            return result;
        }

        if(rawCompanyName.toUpperCase().contains("DUPLICATE OF") || rawCompanyName.length() == 1)
            return result;

        for(int i=0;i<rawCompanyName.length();i++)
        {
            if(rawCompanyName.charAt(i) == '(') {
                int j=i+1;
                int counter = 1;
                int closingIndex = -1;
                while(j < rawCompanyName.length() && counter > 0) {
                    if(rawCompanyName.charAt(j) == ')') {
                        counter--;
                        if(counter == 0) {
                            closingIndex = j;
                            break;
                        }
                    } else if(rawCompanyName.charAt(j) == '(') {
                        counter++;
                    }
                    j++;
                }

                closingIndex = closingIndex == -1 ? rawCompanyName.length() - 1: closingIndex;
                int closingIndexStart = closingIndex;
                if(closingIndex < rawCompanyName.length()-1 && rawCompanyName.charAt(closingIndex+1)!=';') {
                    closingIndex++;
                }

                result.addAll(getCompanyNames(rawCompanyName.substring(0,i) +
                        rawCompanyName.substring(closingIndex + 1), index + 1));

                result.addAll(getCompanyNames(rawCompanyName.substring(i+1, closingIndexStart), index +1));
                return result;
            } else if(rawCompanyName.charAt(i) == ';' || rawCompanyName.charAt(i) == ',')
            {
                result.addAll(getCompanyNames(rawCompanyName.substring(0, i), index + 1));
                result.addAll(getCompanyNames(rawCompanyName.substring(i+1), index + 1));

                return result;
            }
        }

        result.add(cleanStringBefore(rawCompanyName));
        return result;
    }

    private void addToTrie(List<String> companyNames, Integer companyId) {
        CompaniesTrieNode current = root;

        for(String s : companyNames) {
            if(ExcludeTags.contains(s.toUpperCase().trim()) || s.length()==0)
                continue;

            var tagPair = TerminalTags.stream().reduce(new Pair<Integer, Integer>(-1,0),
                    (pair, tag) -> bifunc(pair,tag, s),
                    (pair1, pair2) -> pair1.getValue1() > pair2.getValue1() ? pair1 :pair2);
            int tagPos = -1;
            if(tagPair.getValue1() + tagPair.getValue0() == s.length()) {
                if(tagPair.getValue0() > 0 && s.charAt(tagPair.getValue0() - 1) != ' ')
                    tagPos = -1;
                else
                    tagPos = tagPair.getValue0() - s.substring(0, tagPair.getValue0()).replace(" ", "").length();
            }


            var arr = s.split(" ");
            for (int i=0; i < arr.length; i++) {
                if(arr[i].isEmpty())
                    continue;
                current = current.getOtherNodes().computeIfAbsent(current == root ? arr[i] : arr[i].toUpperCase(),
                        c -> new CompaniesTrieNode());
                if(i==tagPos-1)
                {
                    var tag = s.substring(0, tagPair.getValue0()).trim();
                    if(!ExcludeTags.contains(tag.toUpperCase())) {
                        current.setCompanyId(companyId);
                        current.setCurrentString(tag);
                    }
                }
            }

            current.setCompanyId(companyId);
            current.setCurrentString(s);
            current = root;
        }
    }

    private static String cleanStringBefore(String string) {
        string = string.trim();
        int index =  string.lastIndexOf('/');
        if((index != -1 && string.substring(index).contains(" ")) || (string.contains("A/S") || string.contains("M/S")))
            index = -1;
        string = string.substring(0, index == -1 ? string.length() :  index);
        String upper = string.toUpperCase();
        int tagLength = KnownTags.stream().reduce( 0, (prev, s) -> upper.contains(s)
                && s.length() > prev? s.length() + 1 : prev, (a,b) -> a>b? a:b);

        return string.substring(tagLength > string.length() ? string.length() : tagLength).replaceAll("[^a-zA-Z0-9  '/-]", "");
    }

    private static Pair<Integer, Integer> bifunc(Pair<Integer, Integer> pair, String tag, String string)
    {
        int index = string.toUpperCase().lastIndexOf(tag);
        if(index != -1 && tag.length() > pair.getValue1()) {
            return new Pair<>(index, tag.length());
        }

        return pair;
    }

    private void printPrivate(CompaniesTrieNode root, PrintWriter pw) {
        printPrivate(root,
                c -> pw.println(c.getCurrentString() + ";" + c.getCompanyIds() + ";" + c.getFoundCompany()),
                true);
    }

    private void printPrivate(CompaniesTrieNode root, Consumer<CompaniesTrieNode> consume, boolean foundOnly) {
        if(root.getCurrentString() != null)
        {
            if(foundOnly)
            {
                root.getFoundCompany().ifPresent(f -> consume.accept(root));
            }
            else
                consume.accept(root);
        }

        if(root.getOtherNodes().isEmpty())
            return;

        for(var chld : root.getOtherNodes().entrySet()) {
            printPrivate(chld.getValue(), consume, foundOnly);
        }
    }
}
