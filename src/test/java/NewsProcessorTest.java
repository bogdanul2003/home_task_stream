import org.example.storage.CompaniesHash;
import org.example.model.NewsDto;
import org.example.processor.NewsProcessor;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;

public class NewsProcessorTest {

    @Mock
    BlockingQueue<Optional<NewsDto>> queue;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFoundCompanySingleThreaded() throws FileNotFoundException, URISyntaxException, InterruptedException {
        CompaniesHash hash = new CompaniesHash();


        when(queue.poll(1, TimeUnit.SECONDS)).thenReturn(Optional.of(new NewsDto("title", "this is a test " +
                "news containing China National Electric Equipment or APL and many more")), Optional.empty());

        AddCompanies(hash);
        NewsProcessor newsProcessor = new NewsProcessor(queue, hash, true);
        var result = newsProcessor.startNewsProcessing();

        Assert.assertTrue(result.contains(4));
        Assert.assertTrue(result.contains(1));
        Assert.assertEquals(result.size(), 2);
    }

    @Test
    public void testFoundCompanySingleMultiThreaded() throws FileNotFoundException, URISyntaxException, InterruptedException {
        CompaniesHash hash = new CompaniesHash();


        when(queue.poll(1, TimeUnit.SECONDS)).thenReturn(Optional.of(new NewsDto("title", "this is a test " +
                "news containing China National Electric Equipment or APL and many more")), Optional.empty());

        AddCompanies(hash);
        NewsProcessor newsProcessor = new NewsProcessor(queue, hash, false);
        var result = newsProcessor.startNewsProcessing();

        Assert.assertTrue(result.contains(4));
        Assert.assertTrue(result.contains(1));
        Assert.assertEquals(result.size(), 2);
    }

    @Test
    public void testNoCompanyFound() throws FileNotFoundException, URISyntaxException, InterruptedException {
        CompaniesHash hash = new CompaniesHash();


        when(queue.poll(1, TimeUnit.SECONDS)).thenReturn(Optional.of(new NewsDto("title", "this is a test " +
                "news containing nothing or this and many more")), Optional.empty());

        AddCompanies(hash);
        NewsProcessor newsProcessor = new NewsProcessor(queue, hash, false);
        var result = newsProcessor.startNewsProcessing();

        Assert.assertEquals(result.size(), 0);
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
