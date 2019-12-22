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
import javax.crypto.spec.SecretKeySpec;
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

        if (checkExistenceOfUSer(userDTO.getUsername())) {
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


        try (Writer writer = new FileWriter(buildKeyPath(userDTO.getUsername()))) {
            writer.write(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        } catch (IOException ex) {
            LOGGER.error("Error occurred during writing of key.");
            throw ex;
        }

        LOGGER.info("User successfully created.");
        return true;
    }

    public boolean deleteUser(String username) throws Exception {

        if (!checkExistenceOfUSer(username)) {
            LOGGER.warn("User with this username doesn't exist.");
            return false;
        }

        return Files.deleteIfExists(Paths.get(buildKeyPath(username)));
    }

    public UserDTO getUserByUsername(String username) throws Exception {

        UserDTO user = readUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("User doesn't exist."));

        String key = new String(Files.readAllBytes(Paths.get(buildKeyPath(username))));
        byte[] decodedKey = Base64.getDecoder().decode(key);
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        user.setPassword(AESUtils.decrypt(user.getPassword(), secretKey));

        return user;
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

    private List<Object> createUserRecord(UserDTO user, SecretKey secretKey) throws Exception {
        List<Object> record = new ArrayList<>();

        record.add(user.getUsername());
        record.add(user.getEmail());
        record.add(AESUtils.encrypt(user.getPassword(), secretKey));

        return record;
    }

    private boolean checkExistenceOfUSer(String username) throws IOException {
        return readUsers().stream().anyMatch(u -> StringUtils.equals(u.getUsername(), username));
    }

    private String buildKeyPath(String username) {
        return String.format(keysPath, username);
    }
}
