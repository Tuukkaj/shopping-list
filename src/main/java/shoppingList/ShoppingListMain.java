package shoppingList;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ShoppingListMain extends Application {

    public static void main(String... args) {
        System.out.println("Author: Tuukka Juusela <tuukka.juusela@cs.tamk.fi>");
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Components components = new Components();
        Scene content = new Scene(components.generateBorderPanel(), 320,480);
        stage.setScene(content);
        stage.initStyle(StageStyle.UNIFIED);
        stage.centerOnScreen();
        stage.getIcons().add(new Image("file:resources/shoppingListIcon.png"));
        stage.setTitle("Shopping List");
        stage.show();
    }
}
