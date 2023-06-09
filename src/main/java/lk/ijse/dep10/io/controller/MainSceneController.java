package lk.ijse.dep10.io.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class MainSceneController {

    @FXML
    private Button btnCopy;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnDestination;

    @FXML
    private Button btnMove;

    @FXML
    private Button btnSource;

    @FXML
    private Label lblProgress;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private TextField txtDestination;

    @FXML
    private TextField txtSource;

    private File source;
    private File destination;
    private long length = 0;

    public void initialize() {
        btnCopy.setDisable(true);
        btnMove.setDisable(true);
        btnDelete.setDisable(true);
//        progressBar.setVisible(false);
        lblProgress.setVisible(false);
//        progressBar.setProgress(1);
    }


    @FXML
    void btnSourceOnAction(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setDialogTitle("Select a file or directory");
        chooser.showOpenDialog(null);
        source = chooser.getSelectedFile();
        System.out.println(source);
        enableButtons();
        if (source==null) return;
        txtSource.setText(source.getAbsolutePath());
        if (source!=null)btnDelete.setDisable(false);
        findSize(source);
        length += 4096;
    }

    @FXML
    void btnDestinationOnAction(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a target directory");
        destination = directoryChooser.showDialog(null);
        enableButtons();
        if (destination==null) return;
        txtDestination.setText(destination.getAbsolutePath());
    }

    @FXML
    void btnCopyOnAction(ActionEvent event) {
        progressBar.setVisible(true);
        lblProgress.setVisible(true);
        if (!source.isDirectory()){
            copyFiles(source,new File(destination,source.getName()));
            new Alert(Alert.AlertType.INFORMATION, "Files successfully copied").show();
            return;
        }
        File newDestination = new File(destination, source.getName());
        if (newDestination.exists()){
            Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION, "File already exists. Do you want to replace it?", ButtonType.YES, ButtonType.NO).showAndWait();
            if (result.isEmpty() || result.get()==ButtonType.NO) return;
        }
        newDestination.mkdir();
        findFiles(source,newDestination);
        //new Alert(Alert.AlertType.INFORMATION, "Files successfully copied").show();
    }

    @FXML
    void btnMoveOnAction(ActionEvent event) {
        if (!source.isDirectory()){
            copyFiles(source,new File(destination,source.getName()));
            new Alert(Alert.AlertType.INFORMATION, "Files successfully moved").show();
            deleteSource(source);
            return;
        }
        File newDestination = new File(destination, source.getName());
        if (newDestination.exists()){
            Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION, "File already exists. Do you want to replace it?", ButtonType.YES, ButtonType.NO).showAndWait();
            if (result.isEmpty() || result.get()==ButtonType.NO) return;
        }
        newDestination.mkdir();
        findFiles(source,newDestination);
        deleteSource(source);
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        deleteSource(source);
        new Alert(Alert.AlertType.INFORMATION,"File Deleted!").show();
    }

    private void enableButtons() {
        if (source!=null && destination!=null){
            btnCopy.setDisable(false);
            btnMove.setDisable(false);
            btnDelete.setDisable(true);
        }
    }
    private double write = 4096;
    private void copyFiles(File srcFile, File targetFile) {
//        targetFile = new File(targetFile, srcFile.getName());
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                FileInputStream fis = new FileInputStream(srcFile);
                FileOutputStream fos = new FileOutputStream(targetFile);
                while(true){
                    byte[] buffer = new byte[1024];
                    int read = fis.read(buffer);
                    if (read==-1)break;
                    fos.write(buffer,0,read);
                    write += read;
//                    Thread.sleep(100);
//                    updateProgress(write, (double) length);
//                    System.out.printf("%s, %s \n", write, length);
                    String msg = String.format("%.1f",(write/length)*100)+ "% Completed";
//                    System.out.println(msg);
//                    updateMessage(msg);
                }
                fos.close();
                fis.close();
//                updateProgress(1.0, 1.0);
                String msg = String.format("%.1f",(write/length)*100)+ "% Completed";
//                System.out.println(msg);
//                updateMessage(msg);
                //System.out.println(getMessage());
                return null;
            }
        };
        new Thread(task).start();

        task.setOnFailed(event -> task.getException().printStackTrace());
        task.exceptionProperty().addListener((observableValue, throwable, t1) -> task.getException().printStackTrace());

//        progressBar.progressProperty().bind(task.progressProperty());
//        task.messageProperty().addListener((observableValue, s, t1) -> lblProgress.setText(t1));
       // lblProgress.textProperty().bind(task.messageProperty());
    }

    private void findFiles(File sourceFiles, File targetFile) {
        File[] listFiles = sourceFiles.listFiles();
        if (listFiles ==null) return;
        for (File eachFile : listFiles) {
            if (eachFile.isDirectory()){
                File destinationFile= new File(targetFile,eachFile.getName());
                destinationFile.mkdir();
                findFiles(eachFile,destinationFile);
            }else {
                copyFiles(eachFile,new File(targetFile,eachFile.getName()));
            }
        }
    }

    private void deleteSource(File file) {
        if (file.isFile()) file.delete();
        else {
            for (File listFile : file.listFiles()) {
                if (listFile.isFile()) listFile.delete();
                else {
                    deleteSource(listFile);
                    listFile.delete();
                }
            }
            file.delete();
        }
    }

    private void findSize(File file) {
        File[] listFiles = file.listFiles();
        if (listFiles == null) return;
        for (File each : listFiles) {
            length += each.length();
            if (each.isDirectory()) findSize(each);
        }
    }


}
