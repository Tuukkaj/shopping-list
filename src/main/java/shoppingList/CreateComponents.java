package shoppingList;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

class CreateComponents {
     BorderPane generateBorderPanel() {
        BorderPane borderPane = new BorderPane();

        borderPane.setTop(generateVBox());/*
        borderPane.setBottom(iFeelLuckyButton);
        borderPane.setRight(generateRightBorder());
        borderPane.setCenter(gPane);
        borderPane.setAlignment(iFeelLuckyButton, Pos.BOTTOM_CENTER);
        borderPane.setMargin(iFeelLuckyButton, new Insets(12,12,12,12));
*/
        return borderPane;
    }

    public VBox generateVBox() {
        VBox v = new VBox();
        Menu file = new Menu("File");
        //FILE---
        //BACKGROUNDMUSIC
        RadioMenuItem backgroundMusic = new RadioMenuItem ("Background music");
        backgroundMusic.setSelected(true);
        backgroundMusic.setOnAction((e) -> System.out.println("Background music!!"));

        //DIFFICULTY
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioMenuItem item5 = new RadioMenuItem("5");
        RadioMenuItem item6 = new RadioMenuItem("6");
        RadioMenuItem item7 = new RadioMenuItem("7");

        Menu toggleMenu = new Menu("skill");
        toggleMenu.getItems().addAll(item5,item6,item7);
        item5.setToggleGroup(toggleGroup);
        item6.setToggleGroup(toggleGroup);
        item7.setToggleGroup(toggleGroup);
        item7.setSelected(true);

        //HIGHSCORE
        MenuItem highScoreItem = new MenuItem("Show high scores");
        highScoreItem.setOnAction((event -> System.out.println("High score clicked")));

        //Exit
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction((e) -> Platform.exit());

        file.getItems().addAll(backgroundMusic, toggleMenu, highScoreItem, exitItem);

        //EDIT----
        Menu edit = new Menu("Edit");

        MenuItem cutItem = new MenuItem("Cut (ctrl+x)");
        MenuItem copyItem = new MenuItem("Copy (ctrl+c)");
        MenuItem pasteItem = new MenuItem("Paste (ctrl+v)");
        cutItem.setDisable(true);
        copyItem.setDisable(true);
        pasteItem.setDisable(true);
        edit.getItems().addAll(cutItem,copyItem,pasteItem);

        //ABOUT---
        Menu about = new Menu("About");
        MenuItem aboutItem = new MenuItem("About Lotto App");


        about.getItems().addAll(aboutItem);
        MenuBar menubar = new MenuBar();
        menubar.getMenus().addAll(file,edit,about);

        v.getChildren().add(menubar);
        v.setAlignment(Pos.CENTER);
        v.setSpacing(20);
        return v;
    }
}
