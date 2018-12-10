package shoppingList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles reading of JSON files written by this program.
 *
 * @author Tuukka Juusela
 * @version 2018.0212
 * @since 1.8
 */
class ShoppingListReader {
    /**
     * Reads JSON file and creates ObservableList<Product> from it.
     * @param file to read.
     * @return ObservableList<Product> created from given file.
     */
    public ObservableList<Product> read(File file) {
        String content = fileToString(file);
        return stringToObservableList(content);
    }

    /**
     * Parses through given parameter String and creates ObservableList<Product> from it.
     * @param content File read as a String.
     * @return ObservableList<Product> created from given parameter.
     */
    private ObservableList<Product> stringToObservableList(String content) {
        ObservableList<Product> list  = FXCollections.observableArrayList();

        if(content.contains("shoppingList")) {
            Pattern squarePattern = Pattern.compile("(\\[.*\\])");
            Matcher squareMatcher = squarePattern.matcher(content);
            if(squareMatcher.find()) {
                String shoppingListContent = squareMatcher.group().substring(1);
                shoppingListContent = shoppingListContent.substring(0, shoppingListContent.length()-1);
                shoppingListContent = shoppingListContent.replaceAll("\\s+", " ").trim();
                Matcher itemMatcher = Pattern.compile("(\\{.*?})").matcher(shoppingListContent);

                ArrayList<String> items = new ArrayList<>();
                while (itemMatcher.find()) {
                    items.add(itemMatcher.group());
                }

                for(String item: items) {
                    Matcher productMatcher = Pattern.compile("\"product\":\\s(.*?), \"quantity\":\\s(\\d+)\\s}")
                            .matcher(item);

                    while (productMatcher.find()) {
                        try {
                            list.add(new Product(productMatcher.group(1).substring(1, productMatcher.group(1).length()-1), Integer.parseInt(productMatcher.group(2))));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return list;
    }

    /**
     * Reads file and creates String from it.
     * @param file File to read.
     * @return String created from file.
     */
    private String fileToString(File file) {
        BufferedReader bufferedReader;
        StringBuilder builder = new StringBuilder();
        try (FileReader fileReader = new FileReader(file)){
            bufferedReader =  new BufferedReader(fileReader);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }
}
