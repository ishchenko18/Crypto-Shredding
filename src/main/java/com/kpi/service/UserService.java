package com.kpi.service;

import com.kpi.dto.UserDTO;
import com.kpi.utils.AESUtils;
import com.kpi.utils.CsvUtils;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.validation.constraints.NotBlank;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Value("${com.kpi.users.path}")
    private String usersPath;

    @Value("${com.kpi.keys.path}")
    private String keysPath;

    public boolean createUser(UserDTO userDTO) throws Exception {

        Set<UserDTO> users = readUsers();

        if (users.stream().anyMatch(u -> StringUtils.equals(u.getUsername(), userDTO.getUsername()))) {
            LOGGER.warn("User already exist.");
            return false;
        }

        SecretKey secretKey = AESUtils.generateSecretKey();

        try (Writer writer = Files.newBufferedWriter(Paths.get(usersPath), StandardOpenOption.APPEND)) {
            CsvUtils.writeDataToCsv(createUserRecord(userDTO, secretKey), writer);
        } catch (IOException ex) {
            LOGGER.error("Error occurred during creation of user.");
            throw ex;
        }


        try (Writer writer = new FileWriter(String.format(keysPath, userDTO.getUsername()))) {
            writer.write(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        } catch (IOException ex) {
            LOGGER.error("Error occurred during writing of key.");
            throw ex;
        }

        LOGGER.info("User successfully created.");
        return true;
    }

    private Set<UserDTO> readUsers() throws IOException {

        Set<UserDTO> users = new HashSet<>();
        Reader reader = Files.newBufferedReader(Paths.get(usersPath));

        CSVParser csvParser = CsvUtils.createCsvParser(reader, "Username", "Email", "Password");

        for (CSVRecord record : csvParser) {
            if (record.size() == 3) {
                users.add(new UserDTO(record.get("Username"), record.get("Password"), record.get("Email")));
            }
        }

        return users;
    }

    public String readKeyForUser(@NotBlank String username) throws IOException {

        Reader reader = Files.newBufferedReader(Paths.get(keysPath));

        CSVParser csvParser = CsvUtils.createCsvParser(reader, "Username", "Key");

        for (CSVRecord record : csvParser) {
            if (record.size() == 2 && record.get("Username").equals(username)) {
                return record.get("Key");
            }
        }

        String message = String.format("Key for user %s isn't generated.", username);
        LOGGER.error(message);
        throw new IllegalStateException(message);
    }

    private List<Object> createUserRecord(UserDTO user, SecretKey secretKey) throws Exception {
        List<Object> record = new ArrayList<>();

        record.add(user.getUsername());
        record.add(user.getEmail());
        record.add(AESUtils.encrypt(user.getPassword(), secretKey));

        return record;
    }
}
