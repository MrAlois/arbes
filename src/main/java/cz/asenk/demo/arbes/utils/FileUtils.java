package cz.asenk.demo.arbes.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.core.io.Resource;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alois Šenkyřík asenkyrik@monetplus.cz
 * Created: 07.09.2023
 */

@Slf4j
@UtilityClass
public class FileUtils {
    public String parseCsvFile(Resource path) {
        try {
            return parseCsvFile(path.getFile().getAbsolutePath());
        } catch (IOException e) {
            return "";
        }
    }

    public String parseCsvFile(String path) {
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;

            while ((line = br.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            log.error("File {} doesn't exist. {}", path, e.getMessage());
            return "";
        } catch (IOException e) {
            log.error("File {} couldn't be opened. {}", path, e.getMessage());
            return "";
        }

        return stringBuilder.toString();
    }
}
