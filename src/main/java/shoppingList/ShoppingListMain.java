package shoppingList;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Main class of the program. Holds methods for reading and writing JSON files. Also holds methods for creation of
 * JSONComponents that are part of the JSONFileData.
 *
 * @author Tuukka Juusela
 * @version 2018.0212
 * @since 1.8
 */
public class ShoppingListMain extends Application {
    /**
     * Main method of the program. Prints authors name and launches program.
     * @param args command line argument. Not in use.
     */
    public static void main(String... args) {
        System.out.println("Author: Tuukka Juusela <tuukka.juusela@cs.tamk.fi>");
        launch(args);
    }

    /**
     * Starts the JavaFX. Creates new Components and components create borderPane for Scene.
     * @param stage to show.
     */
    @Override
    public void start(Stage stage) {
        Components components = new Components(stage, this);
        Scene content = new Scene(components.generateBorderPanel(), 320,480);
        stage.setScene(content);
        //stage.initStyle(StageStyle.UNIFIED);
        stage.centerOnScreen();
        stage.getIcons().add(new Image("file:icons/shoppingListIcon.png"));
        stage.setTitle("Tuukka Lister");
        stage.show();
    }
}
