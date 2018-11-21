package shoppingList;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ShoppingListMain extends Application {

    public static void main(String... args) {
        System.out.println("Author: Tuukka Juusela <tuukka.juusela@cs.tamk.fi>");
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        CreateComponents createComponents = new CreateComponents();


        Scene content = new Scene(createComponents.generateBorderPanel(), 640,420);
        stage.setScene(content);
        stage.initStyle(StageStyle.UNIFIED);
        stage.centerOnScreen();
        stage.setTitle("Shopping List");
        stage.show();
    }
}
