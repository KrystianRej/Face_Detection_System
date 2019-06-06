package application;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class SampleController {

    //------------------------------------
    //Location for photos to be saved

    public String pathToFile = "./faces";
    private static final String ULRADDRESS = "http://localhost:8000/v1/facebio/";
    //"https://stark-beyond-17099.herokuapp.com/v1/facebio/";

    //------------------------------------
    @FXML
    private Button startCameraButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button recogniseFaceButton;
    @FXML
    private Button stopRecognitionButton;
    @FXML
    private ImageView imageView;
    @FXML
    private TitledPane dataPane;
    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;
    @FXML
    private TextField code;
    @FXML
    private TextField age;
    @FXML
    public ListView<String> listWithLogs;
    @FXML
    public ListView<String> dataOutputList;
    @FXML
    public Label informationAboutSavingLabel;
    @FXML
    public Label missingDataError;
    @FXML
    public Label badFacesNumber;
    @FXML
    public Label title;
    @FXML
    public TilePane tilePane;
    @FXML
    public AnchorPane personalDataPanel;
    //-----------------------------------------------------
    @Autowired
    private FaceDetector newFaceDetector;    //Creating Face detector object
    private List<String> newPerson = new ArrayList<>();
    @Autowired
    private RestTemplate restTemplate;
    private FaceBio faceBio;
    private Gson gson;
    private String gsonString;
    public static ObservableList<String> eventList = FXCollections.observableArrayList();
    public static ObservableList<String> outEventList = FXCollections.observableArrayList();

    //-----------------------------------------------------
    public void showOnLog(String data) {

        Instant present = Instant.now();

        String informativeLogs = present.toString() + ":\n" + data;

        eventList.add(informativeLogs);

        listWithLogs.setItems(eventList);

    }

    @FXML
    protected void startCamera() {

        //--------------------------------------------------
        // Initialization, setting Image view

        newFaceDetector.init();

        newFaceDetector.setFrames(imageView);

        newFaceDetector.start();

        //--------------------------------------------------
        //Active buttons

        startCameraButton.setVisible(false);
        stopButton.setVisible(true);
        saveButton.setDisable(false);
        recogniseFaceButton.setDisable(false);
        recogniseFaceButton.setText("Recognise Face");
        dataPane.setDisable(false);
        stopRecognitionButton.setDisable(true);

        //--------------------------------------------------
        // Show photos on screen if possible
        tilePane.setPadding(new Insets(15, 15, 55, 15));
        tilePane.setHgap(30);
        File mainRoot = new File(pathToFile);
        if (mainRoot.listFiles() == null || Objects.requireNonNull(mainRoot.listFiles()).length == 0) {
            recogniseFaceButton.setDisable(true);
            recogniseFaceButton.setText("Rec not available");
        } else {
            this.displayPhotos();
        }
        showOnLog(" Stream from the camera has started!");
        //**********************************************************************************************
    }

    private int counter = 0;

    @FXML
    protected void faceRecognise() {

        newFaceDetector.setFaceRecognized(true);

        recogniseFaceButton.setText("Get Face Data");

        //Getting detected person data
        newPerson = newFaceDetector.getOutput();

        if (counter > 0) {

            String title = "------ Face Data: " + " --- " + newPerson.get(1) + " --- " + newPerson.get(2) + " ------";

            outEventList.add(title);

            String firstPersonName = "First Name : " + newPerson.get(1);

            outEventList.add(firstPersonName);

            dataOutputList.setItems(outEventList);

            String lastPersonName = "Last Name : " + newPerson.get(2);

            outEventList.add(lastPersonName);

            dataOutputList.setItems(outEventList);

            String uniqueCode = "Unique code : " + newPerson.get(0);

            outEventList.add(uniqueCode);

            dataOutputList.setItems(outEventList);

            String personAge = "Age      : " + newPerson.get(3);

            outEventList.add(personAge);

            dataOutputList.setItems(outEventList);

            showOnLog("Data has been displayed!");
        }

        counter++;

        stopRecognitionButton.setDisable(false);

    }

    @FXML
    protected void stopRecognise() {

        newFaceDetector.setFaceRecognized(false);
        newFaceDetector.clearOutput();

        this.newPerson.clear();

        recogniseFaceButton.setText("Recognise Face");

        stopRecognitionButton.setDisable(true);

        showOnLog("Face Recognition Deactivated !");
        counter = 0;
    }

    @FXML
    protected void saveFace() {

        if (firstName.getText().trim().isEmpty() || lastName.getText().trim().isEmpty() ||
                code.getText().trim().isEmpty() || age.getText().trim().isEmpty()) {

            new Thread(() -> {

                try {
                    missingDataError.setVisible(true);

                    Thread.sleep(2000);

                    missingDataError.setVisible(false);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }).start();
        } else if (newFaceDetector.isSavingDataValidator()) {
            new Thread(() -> {
                try {
                    badFacesNumber.setVisible(true);
                    Thread.sleep(1500);
                    badFacesNumber.setVisible(false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            new Thread(() -> {
                try {

                    newFaceDetector.setFirstName(firstName.getText());
                    newFaceDetector.setLastName(lastName.getText());
                    newFaceDetector.setCode(Integer.parseInt(code.getText()));

                    faceBio = new FaceBio();
                    faceBio.setFirstName(firstName.getText());
                    faceBio.setLastName(lastName.getText());
                    faceBio.setAge(Integer.parseInt(age.getText()));
                    faceBio.setCode(Integer.parseInt(code.getText()));

                    gson = new Gson();
                    gsonString = gson.toJson(this.faceBio);
                    System.out.println(gsonString);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<String> entity = new HttpEntity<>(gsonString, headers);
                    restTemplate.put(ULRADDRESS + "saveFaceBio", entity);
                    informationAboutSavingLabel.setVisible(true);
                    Thread.sleep(2000);
                    Platform.runLater(() -> informationAboutSavingLabel.setVisible(false));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            newFaceDetector.setSavingPossible(true);
            tilePane.getChildren().clear();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.displayPhotos();
            recogniseFaceButton.setDisable(false);
            recogniseFaceButton.setText("Recognise Face");
        }
    }

    @FXML
    protected void stopCam() {

        this.stopRecognise();
        this.counter = 0;
        newFaceDetector.stop();
        startCameraButton.setVisible(true);
        stopButton.setVisible(false);
        showOnLog("Stream is over!");
        recogniseFaceButton.setText("Data of the recognized person is shown");
        recogniseFaceButton.setDisable(true);
        saveButton.setDisable(true);
        dataPane.setDisable(true);
        stopRecognitionButton.setDisable(true);
        dataOutputList.getItems().clear();
        listWithLogs.getItems().clear();
        tilePane.getChildren().clear();
    }

    private void displayPhotos() {

        try {
            File folder = new File(pathToFile);
            File[] listOfFiles = folder.listFiles();

            for (final File file : listOfFiles) {

                Image image = new Image(new FileInputStream(file), 120, 0, true, true);
                ImageView newImageRepresentation = new ImageView(image);
                tilePane.getChildren().addAll(newImageRepresentation);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
