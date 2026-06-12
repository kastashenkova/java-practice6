package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PipelineCheckerTest {

    @Test
    public void testAppLogic() {
        PipelineChecker app = new PipelineChecker();
        assertTrue(app.isWorking());
    }
}
