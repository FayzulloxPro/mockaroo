package dev.jlkesh.java_telegram_bots.dataserver;


import com.github.javafaker.*;
import com.github.javafaker.service.RandomService;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import static dev.jlkesh.java_telegram_bots.dataserver.FieldType.*;

public class FakerApplicationService {

    public static final List<String> NUMERIC_FIELDS = List.of("ID", "AGE", "RANDOM_INT");
    private static final Scanner scanner = new Scanner(System.in);
    private static final AtomicLong id = new AtomicLong(1);
    private static final Faker faker = new Faker();
    private static final Country country = faker.country();
    private static final Address address = faker.address();
    private static final Book book = faker.book();
    private static final Name name = faker.name();
    private static final Lorem lorem = faker.lorem();
    private static final RandomService random = faker.random();
    private static final PhoneNumber phoneNumber = faker.phoneNumber();
    public static final Map<FieldType, BiFunction<Integer, Integer, Object>> functions = new HashMap<>() {{
        put(ID, (a, b) -> id.incrementAndGet());
        put(UUID, (a, b) -> java.util.UUID.randomUUID());
        put(BOOK_TITLE, (a, b) -> book.title());
        put(BOOT_AUTHOR, (a, b) -> book.author());
        put(POST_TITLE, (a, b) -> String.join(" ", lorem.words(random.nextInt(a, b))));
        put(POST_BODY, (a, b) -> String.join("", lorem.paragraphs(random.nextInt(a, b))));
        put(FIRSTNAME, (a, b) -> name.firstName());
        put(LASTNAME, (a, b) -> name.lastName());
        put(USERNAME, (a, b) -> name.username());
        put(FULLNAME, (a, b) -> name.fullName());
        put(BLOOD_GROUP, (a, b) -> name.bloodGroup());
        put(EMAIL, (a, b) -> name.username() + "@" + (random.nextBoolean() ? "gmail.com" : "mail.ru"));
        put(GENDER, (a, b) -> random.nextBoolean() ? "MALE" : "FEMALE");
        put(PHONE, (a, b) -> phoneNumber.cellPhone());
        put(LOCAlDATE, (a, b) -> {
            int year = random.nextInt(1900, Year.now().getValue() - 1);
            int month = random.nextInt(1, 12);
            YearMonth yearMonth = YearMonth.of(year, month);
            int day = random.nextInt(1, yearMonth.getMonth().length(yearMonth.isLeapYear()));
            return LocalDate.of(year, month, day);
        });
        put(COUNTRY_CODE, (a, b) -> country.countryCode3());
        put(COUNTRY_ZIP_CODE, (a, b) -> address.zipCode());
        put(CAPITAL, (a, b) -> country.capital());
        put(WORD, (a, b) -> lorem.word());
        put(WORDS, (a, b) -> lorem.words(random.nextInt(a, b)));
        put(PARAGRAPH, (a, b) -> lorem.paragraph());
        put(PARAGRAPHS, (a, b) -> lorem.paragraphs(random.nextInt(a, b)));
        put(AGE, random::nextInt);
        put(RANDOM_INT, random::nextInt);
        put(LETTERS, (a, b) -> lorem.characters(a, b, true));
    }};

    public static final List<FieldType> BLACK_LIST = List.of(
            AGE, WORDS, PARAGRAPHS, RANDOM_INT, POST_TITLE, POST_BODY, LETTERS
    );

    public File processRequest(FakerApplicationGenerateRequest request) {
        var fileType = request.getFileType();
        var fileName = request.getFileName() + "." + fileType.name().toLowerCase();
        var rowsCount = request.getCount();
        var fields = request.getFields();

        return switch (fileType) {
            case JSON -> generateDataAsJson(rowsCount, fileName, fields);
            case CSV -> generateDataAsCSV(rowsCount, fileName, fields, request.isFlag());
            case SQL -> generateDataAsSQL(rowsCount, fileName, fields);
        };
    }


    private File generateDataAsSQL(int rowsCount, String fileName, Set<Field> fields) {
        synchronized (FakerApplicationService.class) {
            var result = new StringJoiner(";\n");
            for (int i = 0; i < rowsCount; i++) {
                var rowInsert = new StringJoiner(", ", "(", ")");
                var valueInsert = new StringJoiner(", ", "(", ")");

                for (Field field : fields) {
                    SQLData sqlData = field.getPatternAsSQL();
                    rowInsert.add(sqlData.getFieldName());
                    valueInsert.add(sqlData.getValue());
                }
                result.add("insert into "+fileName.substring(0,fileName.length()-4)+rowInsert+" " + "values"+valueInsert);
            }
            Path path = Path.of(fileName);
            File file = new File(path.toUri());
            try (PrintWriter printWriter = new PrintWriter(file)) {

//                if ( Files.notExists(path) ) {
//                    Files.createFile(path);
//                    file=new File(path.toUri());
//                }
                printWriter.print(result+";");
//                Files.writeString(path, result.toString(), StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file;
        }
    }

    private File generateDataAsJson(int rowsCount, String fileName, Set<Field> fields) {
        synchronized (FakerApplicationService.class) {
            var result = new StringJoiner(",\n", "[", "]");
            for (int i = 0; i < rowsCount; i++) {
                var row = new StringJoiner(", ", "{", "}");
                for (Field field : fields)
                    row.add(field.getPatternAsJson());
                result.add(row.toString());
            }
            Path path = Path.of(fileName);
            File file = new File(path.toUri());
            try (PrintWriter printWriter = new PrintWriter(file)) {

//                if ( Files.notExists(path) ) {
//                    Files.createFile(path);
//                    file=new File(path.toUri());
//                }
                printWriter.print(result);
//                Files.writeString(path, result.toString(), StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file;
        }
    }

    private File generateDataAsCSV(int rowsCount, String fileName, Set<Field> fields, boolean includeHeader) {
        synchronized (FakerApplicationService.class) {
            var result = new StringJoiner(",\n", "", "");
            if (includeHeader) {
                var header = new StringJoiner(", ");
                for (Field field : fields) {
                    header.add(field.getFieldName());
                }
                result.add(header.toString());
            }
            for (int i = 0; i < rowsCount; i++) {
                var row = new StringJoiner(", ");
                for (Field field : fields)
                    row.add(field.getPatternAsCSV());
                result.add(row.toString());
            }
            Path path = Path.of(fileName);
            File file = new File(path.toUri());
            try (PrintWriter printWriter = new PrintWriter(file)) {

//                if ( Files.notExists(path) ) {
//                    Files.createFile(path);
//                    file=new File(path.toUri());
//                }
                printWriter.print(result);
//                Files.writeString(path, result.toString(), StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file;
        }
    }

}
