package com.example.libaryfx;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;

public class PerpustakaanApp extends Application {
    private static final String FILE_NAME = "buku_data.txt";
    TableView<Buku> bukuTable;
    ObservableList<Buku> data = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Aplikasi Perpustakaan");

        bukuTable = new TableView<>();
        bukuTable.setEditable(true);

        TableColumn<Buku, String> judulCol = new TableColumn<>("Judul");
        judulCol.setCellValueFactory(cellData -> cellData.getValue().judulProperty());
        TableColumn<Buku, String> pengarangCol = new TableColumn<>("Pengarang");
        pengarangCol.setCellValueFactory(cellData -> cellData.getValue().pengarangProperty());
        TableColumn<Buku, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        bukuTable.setItems(data);

        bukuTable.getColumns().addAll(judulCol, pengarangCol, statusCol);

        // Menu Tambah Buku
        Button tambahButton = new Button("Tambah Buku");
        tambahButton.setOnAction(e -> showTambahBukuDialog());

        // Menu Peminjaman Buku
        Button pinjamButton = new Button("Peminjaman Buku");
        pinjamButton.setOnAction(e -> showPeminjamanDialog());

        // Menu Update Buku
        Button updateButton = new Button("Update Buku");
        updateButton.setOnAction(e -> showUpdateBukuDialog());

        Button hapusButton = new Button("Hapus Buku");
        hapusButton.setOnAction(e -> hapusBuku());

        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.getChildren().addAll(bukuTable, tambahButton, pinjamButton, updateButton, hapusButton);

        Scene scene = new Scene(vbox, 400, 400);
        stage.setScene(scene);
        loadDataFromFile();

        stage.setOnCloseRequest(e -> saveDataToFile());
        stage.show();
    }

    void showTambahBukuDialog() {

        Dialog<Buku> dialog = new Dialog<>();
        dialog.setTitle("Tambah Buku");
        dialog.setHeaderText("Masukkan informasi buku");

        ButtonType tambahButtonType = new ButtonType("Tambah", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(tambahButtonType, ButtonType.CANCEL);

        TextField judulField = new TextField();
        TextField pengarangField = new TextField();

        VBox content = new VBox();
        content.getChildren().addAll(new Label("Judul:"), judulField, new Label("Pengarang:"), pengarangField);
        dialog.getDialogPane().setContent(content);




            dialog.showAndWait().ifPresent(buku -> {
                data.add(buku);
                saveDataToFile(); // Save data to file after adding a new book
            });







    }

    private void showPeminjamanDialog() {
        Buku selectedBuku = bukuTable.getSelectionModel().getSelectedItem();
        if (selectedBuku != null) {
            if(selectedBuku.status.equalsIgnoreCase("Dipinjam")){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Peringatan");
                alert.setHeaderText("Buku Sudah Dipinjamkan!!");
                alert.showAndWait();
            } else {
                selectedBuku.setStatus("Dipinjam");
                bukuTable.refresh();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Peringatan");
            alert.setHeaderText("Pilih buku terlebih dahulu");
            alert.showAndWait();
        }
    }

    public void showUpdateBukuDialog() {
        Buku selectedBuku = bukuTable.getSelectionModel().getSelectedItem();
        if (selectedBuku != null) {
            Dialog<Buku> dialog = new Dialog<>();
            dialog.setTitle("Update Buku");
            dialog.setHeaderText("Update informasi buku");

            ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

            TextField judulField = new TextField(selectedBuku.getJudul());
            TextField pengarangField = new TextField(selectedBuku.getPengarang());

            VBox content = new VBox();
            content.getChildren().addAll(new Label("Judul:"), judulField, new Label("Pengarang:"), pengarangField);
            dialog.getDialogPane().setContent(content);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == updateButtonType) {
                    selectedBuku.setJudul(judulField.getText());
                    selectedBuku.setPengarang(pengarangField.getText());
                    saveDataToFile(); // Save data to file after updating a book
                }
                return null;
            });

            dialog.showAndWait();
            bukuTable.refresh();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Peringatan");
            alert.setHeaderText("Pilih buku terlebih dahulu");
            alert.showAndWait();
        }
    }

    void hapusBuku() {
        Buku selectedBuku = bukuTable.getSelectionModel().getSelectedItem();
        if (selectedBuku != null) {
            data.remove(selectedBuku);
            saveDataToFile(); // Save data to file after deleting a book
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Peringatan");
            alert.setHeaderText("Pilih buku terlebih dahulu");
            alert.showAndWait();
        }
    }

    private void loadDataFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    data.add(new Buku(parts[0], parts[1], parts[2]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDataToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Buku buku : data) {
                writer.write(buku.getJudul() + "," + buku.getPengarang() + "," + buku.getStatus());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Buku {
        private String judul;
        private String pengarang;
        private String status;

        public Buku(String judul, String pengarang, String status) {
            this.judul = judul;
            this.pengarang = pengarang;
            this.status = status;
        }

        public String getJudul() {
            return judul;
        }

        public void setJudul(String judul) {
            this.judul = judul;
        }

        public String getPengarang() {
            return pengarang;
        }

        public void setPengarang(String pengarang) {
            this.pengarang = pengarang;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public StringProperty judulProperty() {
            return new SimpleStringProperty(judul);
        }

        public StringProperty pengarangProperty() {
            return new SimpleStringProperty(pengarang);
        }

        public StringProperty statusProperty() {
            return new SimpleStringProperty(status);
        }
    }
}



