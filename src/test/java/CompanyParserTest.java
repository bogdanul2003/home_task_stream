import org.example.parser.CompanyParser;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class CompanyParserTest {

    @Test
    public void extractCompanyNames() {
        CompanyParser parser = new CompanyParser();

        var result = parser.extractCompanyNames(5 + ";Puerto Rico Electric Power Authority (PREPA) (Autoridad de Energia Electrica, AEE))");
        var expected = new ArrayList<>(Arrays.asList("Puerto Rico Electric Power Authority", "PREPA", "Autoridad de Energia Electrica", "AEE"));

        Assert.assertEquals(result.getCompanyId(), Integer.valueOf(5));

        for(var c : result.getCompanyNames()) {
            int index = expected.indexOf(c);
            Assert.assertNotEquals(index, -1);
            expected.remove(index);
        }

        Assert.assertEquals(expected.size(), 0);
    }
}
