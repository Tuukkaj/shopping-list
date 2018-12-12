package shoppingList.json;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import jsonParser.JSONComponent.JSONArray;
import jsonParser.JSONComponent.JSONFileData;
import jsonParser.JSONComponent.JSONItem;
import jsonParser.JSONParser;
import shoppingList.Product;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * Handles everything related to JSON. Uses JSONParser to read and write json files.
 *
 * @author Tuukka Juusela
 * @version 2018.0212
 * @since 1.8
 */
public class JSONHandler {
    /**
     * Reads json file and returns observableList of products. If exception is caused by something generates warning
     * dialog.
     * @param file to read.
     * @return ObservableList of products from JSON file.
     */
     public ObservableList<Product> readJsonFile(File file) {
        JSONFileData fileData = new JSONParser().read(file);
        ObservableList<Product> products = FXCollections.observableArrayList();

        JSONArray array;
        try {
            array = ((JSONArray) fileData.getComponent("shoppingList"));

            array.getData().forEach(linkedList -> products
                    .add(new Product(String.valueOf(linkedList.get("product")),
                            Integer.valueOf(String.valueOf(linkedList.get("quantity"))))));

        } catch (InvalidParameterException e) {
            generateNotProperJSONFileWarning();
        }

        return products;
    }


    /**
     * Saves tableView to JSONFile.
     * @param filename name of the file json file to be saved.
     * @param table TableView of products to save.
     * @return File where tableView was saved.
     */
     public File saveTableViewAsJson(String filename, TableView<Product> table) {
        JSONParser parser = new JSONParser();
        JSONFileData data = new JSONFileData();
        JSONArray array = new JSONArray("shoppingList");

        table.getItems().forEach(product -> {
            if(!product.getName().equalsIgnoreCase("-")) {
                ArrayList<JSONItem> itemList = new ArrayList<>();
                itemList.add(new JSONItem("product", product.getName()));
                itemList.add(new JSONItem("quantity", product.getQuantity()));
                array.add(itemList);
            }
        });
        File savedFile = new File(filename);
        data.add(array);
        parser.write(data,savedFile);
        return savedFile;
    }

    /**
     * Saves TableView to given JSONFile.
     * @param file to save TableView.
     * @param table TableView to save.
     */
    public void saveAsJSON(File file, TableView<Product> table) {
        JSONParser parser = new JSONParser();
        JSONFileData data = new JSONFileData();
        JSONArray array = new JSONArray("shoppingList");

        table.getItems().forEach(product -> {
            if(!product.getName().equalsIgnoreCase("-")) {
                ArrayList<JSONItem> itemList = new ArrayList<>();
                itemList.add(new JSONItem("product", product.getName()));
                itemList.add(new JSONItem("quantity", product.getQuantity()));
                array.add(itemList);
            }
        });

        data.add(array);
        parser.write(data,file);
    }

    /**
     * Generates Dialog window warning about error occouring in reading process.
     */
    private void generateNotProperJSONFileWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Problem occurred");
        alert.setHeaderText("JSON file you tried to read is\nnot supported by Tuukka Lister.");
        alert.setContentText("Ensure that file you tried to read is made with this program.");
        alert.showAndWait();
    }
}
