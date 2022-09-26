import org.example.model.NewsDto;
import org.example.processor.XmlFilesProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class XmlNewsExtractTest {

    @Mock
    BlockingQueue<Optional<NewsDto>> queue;// = new LinkedBlockingDeque<>(ForkJoinPool.commonPool().getParallelism() * 4);

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testWithPills() throws IOException, InterruptedException {
        when(queue.offer(any(), any(Long.class), any())).thenReturn(true);
        XmlFilesProcessor xmlNewsExtract = new XmlFilesProcessor(queue, 3);

        URL resource = getClass().getClassLoader().getResource("testNews");
        xmlNewsExtract.startNewsStreamFromFolder(resource.getFile());

        xmlNewsExtract.waitOnCurrentTask();

        verify(queue, times(7)).offer(any(), any(Long.class), any());
        verify(queue, times(4)).offer(Optional.empty(), 1, TimeUnit.SECONDS);
    }
}
