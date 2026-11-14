package com.travelsync;

import com.travelsync.controller.NavigationController;
import com.travelsync.data.AppDataStore;
import com.travelsync.ui.AppShell;
import com.travelsync.ui.PageCatalog;
import com.travelsync.util.ThemeManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TravelSyncApplication extends Application {

    @Override
    public void start(Stage stage) {
        AppDataStore.getInstance(); // generate demo data up front
        NavigationController navigation = new NavigationController();
        navigation.setRoutes(PageCatalog.routes(navigation));
        AppShell shell = new AppShell(navigation);

        Scene scene = new Scene(shell, 1360, 860);
        ThemeManager.init(scene); // applies the saved (or default light) stylesheet

        stage.setTitle("TravelSync \u2014 Intelligent Travel Planning & Information Platform");
        stage.setScene(scene);
        stage.setMinWidth(1024);
        stage.setMinHeight(680);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
