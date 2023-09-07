package cz.asenk.demo.arbes.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import lombok.val;

/**
 * @author Alois Šenkyřík asenkyrik@monetplus.cz
 * Created: 07.09.2023
 */
class FileUtilsTest {

    @Test
    void parseCsvFile() throws URISyntaxException {
        val resource = Path.of(ClassLoader.getSystemResource("testing_data.csv").toURI());

        assertNotNull(resource);
        val result = FileUtils.parseCsvFile(resource.toAbsolutePath().toString());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}