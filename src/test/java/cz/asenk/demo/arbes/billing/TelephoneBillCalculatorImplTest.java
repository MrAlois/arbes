package cz.asenk.demo.arbes.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import lombok.val;

import cz.asenk.demo.arbes.utils.FileUtils;

/**
 * @author Alois Šenkyřík asenkyrik@monetplus.cz
 * Created: 07.09.2023
 */
class TelephoneBillCalculatorImplTest {

    @Test
    void applyPromo() throws URISyntaxException {
        val resource = Path.of(ClassLoader.getSystemResource("same_number.csv").toURI());

        assertNotNull(resource);
        val logData = FileUtils.parseCsvFile(resource.toAbsolutePath().toString());

        val calculator = new TelephoneBillCalculatorImpl();
        val result = calculator.calculate(logData);

        assertEquals(0.0, result.doubleValue());
    }

    @Test
    void calculatePriceSimple() throws URISyntaxException {
        val resource = Path.of(ClassLoader.getSystemResource("multiple_numbers.csv").toURI());

        assertNotNull(resource);
        val logData = FileUtils.parseCsvFile(resource.toAbsolutePath().toString());

        val calculator = new TelephoneBillCalculatorImpl();
        val result = calculator.calculate(logData);

        assertEquals(9.5, result.doubleValue());
    }
}