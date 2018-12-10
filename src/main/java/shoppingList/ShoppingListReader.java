package shoppingList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ShoppingListReader {
    public ObservableList<Product> read(File file) {
        ObservableList<Product> list  = FXCollections.observableArrayList();
        String content = fileToString(file);
        System.out.println(content);
        return list;
    }

    String fileToString(File file) {
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
