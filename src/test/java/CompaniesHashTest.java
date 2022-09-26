import org.example.storage.CompaniesHash;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompaniesHashTest {
    private CompaniesHash hashTest;

    public CompaniesHashTest() throws FileNotFoundException, URISyntaxException {
        hashTest = new CompaniesHash();
    }

    @BeforeEach
    public void setup() throws FileNotFoundException, URISyntaxException {
        hashTest = new CompaniesHash();
    }

    @Test
    public void testCompanyInsert() {

        var expected = ExpectedHashCompanyNames();
        var expectedIds = ExpectedHashIds();

        AddCompanies(hashTest);

        var result = hashTest.getStoredCompanies(false);


        for(var c : result) {
            int index = expected.indexOf(c.getCurrentString());
            Assert.assertNotEquals(index, -1);
            Assert.assertEquals(c.getCompanyIds().contains(expectedIds.get(index)), true);

            expected.remove(index);
            expectedIds.remove(index);
        }

        Assert.assertEquals(expected.size(), 0);
    }

    @Test
    public void testCompanySearch() {
        var expected = ExpectedHashCompanyNames();
        var expectedIds = ExpectedHashIds();

        AddCompanies(hashTest);
        hashTest.registerThread("thread");

        var company = expected.stream().filter(c -> c.contains(" ")).findFirst().get();
        var index = expected.indexOf(company);
        for(var word : company.split(" "))
            Assert.assertTrue(hashTest.isPartOfCompany(word, "thread"));

        var id = hashTest.getCompanyId("thread");
        Assert.assertEquals(id.get(), expectedIds.get(index));

        //Assert.assertArrayEquals(hashTest.getFoundCompanies().toArray(), Arrays.asList(id.get()).toArray());
        var result = hashTest.getStoredCompanies(true).stream().findFirst();
        Assert.assertEquals(result.get().getCompanyIds().stream().findFirst(), id);
    }

    @Test
    public void TooManyParant() {
        hashTest.insertCompany("aaa(bbb ( ccc (ddd(eee(fff(ggg(hhh( gff( iff( jgg()))))))))))", 1);

        var result = hashTest.getStoredCompanies(false);

        Assert.assertEquals(result.size(), 10);
    }

    void AddCompanies(CompaniesHash hash) {
        hash.insertCompany("American President Lines (US) Ltd (APL)", 4);
        hash.insertCompany("Puerto Rico Electric Power Authority (PREPA) (Autoridad de Energia Electrica, AEE))", 5);
        hash.insertCompany("\"China National Electric Engineering Co Ltd (CNEEC; formerly known as China National Electric Equipment Corp\"", 1);
        hash.insertCompany("\"Hutan Hijau Mas; PT \"", 2);
        hash.insertCompany("DaimlerChrysler Rail Systems (Brasil) Ltda", 3);
    }

    List<Integer> ExpectedHashIds() {
        return new ArrayList(Arrays.asList(3, 3, 2, 1, 1, 1, 1, 1, 4, 4, 4, 5, 5, 5, 5));
    }

    List<String> ExpectedHashCompanyNames() {
        return new ArrayList(Arrays.asList("DaimlerChrysler Rail Systems", "DaimlerChrysler Rail Systems Ltda",
                "Hutan Hijau Mas", "China National Electric Engineering", "China National Electric Engineering Co Ltd",
                "CNEEC", "China National Electric Equipment Corp", "China National Electric Equipment", "American President Lines Ltd",
                "American President Lines", "APL", "Puerto Rico Electric Power Authority", "PREPA", "Autoridad de Energia Electrica", "AEE"));
    }
}
