package com.projektestsiak.controllers.admin;

import java.text.NumberFormat;
import java.util.Locale;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

// OOP Class & Object
public class LaporanKeuanganController {

    @FXML private TableView<KeuanganDummy> table;
    @FXML private TableColumn<KeuanganDummy, String> colTanggal;
    @FXML private TableColumn<KeuanganDummy, String> colKeterangan;
    @FXML private TableColumn<KeuanganDummy, Integer> colJumlah;

    @FXML
    public void initialize() {
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colKeterangan.setCellValueFactory(new PropertyValueFactory<>("keterangan"));
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));

// Format rupiah + warna hijau/merah
// OOP Inheritance TableCell
        colJumlah.setCellFactory(column -> new TableCell<>() {

            private final NumberFormat rupiah =
                    NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("amount-positive", "amount-negative");
                } else {
                    setText(rupiah.format(item));
                    setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

                    getStyleClass().removeAll("amount-positive", "amount-negative");

                    if (item >= 0) {
                        getStyleClass().add("amount-positive"); // hijau
                    } else {
                        getStyleClass().add("amount-negative"); // merah
                    }
                }
            }
        });

        table.setItems(FXCollections.observableArrayList(
            new KeuanganDummy("01-01-2026", "SPP Januari", 500000),
            new KeuanganDummy("02-01-2026", "Uang Gedung", 2000000),
            new KeuanganDummy("03-01-2026", "Biaya Operasional", -750000)
        ));
    }

    @FXML
    private void handleBack() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/admin/laporan_menu.fxml"));
        Stage stage = (Stage) table.getScene().getWindow();
        stage.setScene(new Scene(root, 1200, 700));
    }

    @FXML
    private void handleRefresh() {
        table.refresh();
    }

// Encapsulation
// Dummy Data Class
    public static class KeuanganDummy {
        private String tanggal, keterangan;
        private int jumlah;

        public KeuanganDummy(String t, String k, int j) {
            tanggal = t;
            keterangan = k;
            jumlah = j;
        }

        public String getTanggal() { return tanggal; }
        public String getKeterangan() { return keterangan; }
        public int getJumlah() { return jumlah; }
    }
}
