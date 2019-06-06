package application;

import java.awt.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;

@Component
@Getter
@Setter
public class FaceDetector implements Runnable {

    //private List<String> user;
    private static final String ULRADDRESS = "http://localhost:8000/v1/facebio/";
    //"https://stark-beyond-17099.herokuapp.com/v1/facebio/";

    @Autowired
    private LoaderAndTrainer newLoaderAndTrainer;
    private Java2DFrameConverter frameConverter = new Java2DFrameConverter();
    private List<String> output = new ArrayList<>();
    private OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
    private BufferedImage bufferedImage;

    private int count = 0;
    public String classifierName;
    public File classFile;

    private boolean savingDataValidator = false;
    public boolean isSavingPossible = false;
    public boolean isFaceRecognized = false;
    private boolean isStopped = false;

    private CvHaarClassifierCascade mainHaarClassifier = null;
    private CvHaarClassifierCascade sideFaceHaarClassifier = null;


    @Autowired
    private RestTemplate restTemplate;
    private FaceBioPhoto faceBioPhoto;

    private Gson gson;
    private String gsonString;

    public CvMemStorage cvMemStorage = null;
    private FrameGrabber newCameraGrabber = null;
    private IplImage imageGrabbedFromCamera = null;
    private IplImage temporaryImage;
    private IplImage cuttedImage;
    private IplImage imageInGrayColors = null;
    private IplImage reducedImage = null;
    //public ImageView frames2;
    public ImageView frames;


    private CvSeq facesSequence = null;
    //private CvSeq eyes = null;


    int codeForAPerson;
    public int code;
    //public int reg;
    public int age;

    public String firstName; //first name
    public String lastName; //last name
    //public String section; //section
    //public String name;

    public void init() {
        newLoaderAndTrainer.init();
        setMainHaarClassifier();
        setSideFaceHaarClassifier();
    }

    public void start() {
        try {
            new Thread(this).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            try {
                // 0 for default camera
                newCameraGrabber = OpenCVFrameGrabber.createDefault(0);
                newCameraGrabber.start();

                imageGrabbedFromCamera = grabberConverter.convert(newCameraGrabber.grab());

                cvMemStorage = CvMemStorage.create();
            } catch (Exception e) {
                e.printStackTrace();
                if (newCameraGrabber != null)
                    newCameraGrabber.release();
                newCameraGrabber = new OpenCVFrameGrabber(0);
                newCameraGrabber.start();
                imageGrabbedFromCamera = grabberConverter.convert(newCameraGrabber.grab());
            }

            //converting bufferedImage to grayscale
            imageInGrayColors = cvCreateImage(cvGetSize(imageGrabbedFromCamera), 8, 1);

            //reduce the Image size
            reducedImage = cvCreateImage(cvSize(imageGrabbedFromCamera.width() / 4,
                    imageGrabbedFromCamera.height() / 4), 8, 1);

            int photoNameCounter = 10;

            isStopped = false;
            while (!isStopped && (imageGrabbedFromCamera = grabberConverter.convert(newCameraGrabber.grab())) != null) {

                Frame frame = grabberConverter.convert(imageGrabbedFromCamera);
                bufferedImage = frameConverter.getBufferedImage(frame, 2.2 / newCameraGrabber.getGamma());
                Graphics2D grafic2DForSquare = bufferedImage.createGraphics();

                if (facesSequence == null) {
                    cvClearMemStorage(cvMemStorage);

                    temporaryImage = cvCreateImage(cvGetSize(imageGrabbedFromCamera),
                            imageGrabbedFromCamera.depth(), imageGrabbedFromCamera.nChannels());

                    cvCopy(imageGrabbedFromCamera, temporaryImage);
                    cvCvtColor(imageGrabbedFromCamera, imageInGrayColors, CV_BGR2GRAY);
                    cvResize(imageInGrayColors, reducedImage, CV_INTER_AREA);

                    facesSequence = cvHaarDetectObjects(reducedImage, mainHaarClassifier, cvMemStorage,
                            1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
                    this.savingDataValidator = false;

                    if (imageGrabbedFromCamera != null) {

                        if (facesSequence.total() == 0) {
                            facesSequence = cvHaarDetectObjects(reducedImage, sideFaceHaarClassifier,
                                    cvMemStorage, 1.1, 3,
                                    CV_HAAR_DO_CANNY_PRUNING);
                            this.savingDataValidator = false;
                        }

                        if (facesSequence != null) {
                            grafic2DForSquare.setColor(Color.green);
                            grafic2DForSquare.setStroke(new BasicStroke(2));
                            int totalFacesFound = facesSequence.total();
                            if (totalFacesFound != 1) {
                                this.savingDataValidator = true;
                            }

                            for (int i = 0; i < totalFacesFound; i++) {

                                CvRect rectangle = new CvRect(cvGetSeqElem(facesSequence, i));
                                grafic2DForSquare.drawRect((rectangle.x() * 4), (rectangle.y() * 4),
                                        (rectangle.width() * 4), (rectangle.height() * 4));
                                CvRect secondRect = new CvRect((rectangle.x() * 4), rectangle.y() * 4,
                                        (rectangle.width() * 4), rectangle.height() * 4);
                                cvSetImageROI(temporaryImage, secondRect);

                                if (isFaceRecognized) {
                                    this.codeForAPerson = newLoaderAndTrainer.recognize(temporaryImage);
                                    System.out.println(this.codeForAPerson);

                                    List<String> recognizedUser = new ArrayList<>();
                                    String recognizedName;
                                    if (this.codeForAPerson != -1) {
                                        String temporaryUrl = ULRADDRESS + "getFaceBioByCode?code="
                                                + this.codeForAPerson;
                                        ResponseEntity<List<String>> entity = restTemplate.exchange(
                                                temporaryUrl,
                                                HttpMethod.GET,
                                                null,
                                                new ParameterizedTypeReference<List<String>>() {
                                                }
                                        );
                                        recognizedUser = entity.getBody();
                                        grafic2DForSquare.setColor(Color.WHITE);
                                        grafic2DForSquare.setFont(new Font("Arial Black", Font.BOLD, 24));
                                        recognizedName = recognizedUser.get(1) + " " + recognizedUser.get(2);
                                        grafic2DForSquare.drawString(recognizedName, (rectangle.x() * 4),
                                                rectangle.y() * 4);
                                    } else {
                                        grafic2DForSquare.setColor(Color.WHITE);
                                        grafic2DForSquare.setFont(new Font("Arial Black", Font.BOLD, 24));
                                        recognizedName = "Unknown person";
                                        grafic2DForSquare.drawString(recognizedName, (rectangle.x() * 4),
                                                rectangle.y() * 4);
                                        recognizedUser.add(0, "-1");
                                        recognizedUser.add(1, "Unknown");
                                        recognizedUser.add(2, "Unknown");
                                        recognizedUser.add(3, "-");
                                    }
                                    this.output = recognizedUser;

                                }
                                if (facesSequence.total() == 1) {

                                    if (isSavingPossible) {

                                        String fName = code + "-" + firstName + "_" + lastName + "_" + photoNameCounter + ".jpg";

                                        //Converting IplImage to BufferedImage, then to Byte[] and at the end to Base64 String.
                                        try {
                                            Frame nextFrame = grabberConverter.convert(temporaryImage);
                                            BufferedImage bufferedTemp = frameConverter.getBufferedImage(nextFrame, 1);
                                            bufferedTemp = bufferedTemp.getSubimage(secondRect.x(), secondRect.y(),
                                                    secondRect.width(), secondRect.height());

                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            ImageIO.write(bufferedTemp, "jpg", baos);
                                            baos.flush();
                                            byte[] tempInByte = baos.toByteArray();
                                            String tempToString = Base64.encodeBase64String(tempInByte);
                                            baos.close();
                                            int k = 0;
                                            ResponseEntity<List<String>> namesResponse = restTemplate.exchange(
                                                    ULRADDRESS + "getNames",
                                                    HttpMethod.GET,
                                                    null,
                                                    new ParameterizedTypeReference<List<String>>() {
                                                    });
                                            List<String> namesOfPhotos = namesResponse.getBody();
                                            if (namesOfPhotos.size() != 0) {
                                                while (k == 0) {
                                                    if (namesOfPhotos.contains(fName)) {
                                                        photoNameCounter++;
                                                        fName = code + "-" + firstName + "_" + lastName + "_"
                                                                + photoNameCounter + ".jpg";
                                                    } else {
                                                        k = 1;
                                                        Mat matImage = new Mat(temporaryImage);
                                                        imwrite("faces/" + fName, matImage);
                                                    }
                                                }
                                            }
                                            faceBioPhoto = new FaceBioPhoto();
                                            faceBioPhoto.setName(fName);
                                            faceBioPhoto.setImage(tempToString);
                                            gson = new Gson();
                                            gsonString = gson.toJson(this.faceBioPhoto);
                                            HttpHeaders headers = new HttpHeaders();
                                            headers.setContentType(MediaType.APPLICATION_JSON);
                                            HttpEntity<String> entity = new HttpEntity<>(gsonString, headers);
                                            restTemplate.put(ULRADDRESS + "savePhoto", entity);
                                            //Download new photos from database, and keep program working properly
                                            newLoaderAndTrainer.init();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }
                            this.isSavingPossible = false;
                            facesSequence = null;
                        }

                        WritableImage showFrame = SwingFXUtils.toFXImage(bufferedImage, null);

                        javafx.application.Platform.runLater(() -> frames.setImage(showFrame));

                    }
                    cvReleaseImage(temporaryImage);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {

        isStopped = true;
        imageGrabbedFromCamera = null;
        imageInGrayColors = null;
        reducedImage = null;

        try {
            newCameraGrabber.stop();
        } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {

            e.printStackTrace();
        }
        try {
            newCameraGrabber.release();
        } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {

            e.printStackTrace();
        }
        newCameraGrabber = null;
    }

    private void setMainHaarClassifier() {

        try {

            setClassifierName("haar/haarcascade_frontalface_alt.xml");
            classFile = Loader.extractResource(classifierName, null,
                    "mainHaarClassifier", ".xml");

            if (classFile == null || classFile.length() <= 0) {
                throw new IOException("Error while trying to extract " + classifierName + " from Java source files.");
            }

            Loader.load(opencv_objdetect.class);
            mainHaarClassifier = new CvHaarClassifierCascade(cvLoad(classFile.getAbsolutePath()));
            classFile.delete();
            if (mainHaarClassifier.isNull()) {
                throw new IOException("Error while loading the file for mainHaarClassifier.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setSideFaceHaarClassifier() {

        try {

            classifierName = "haar/haarcascade_profileface.xml";
            classFile = Loader.extractResource(classifierName, null,
                    "sideFaceHaarClassifier", ".xml");

            if (classFile == null || classFile.length() <= 0) {
                throw new IOException("Error while trying to extract " + classifierName + " from Java source files.");
            }

            Loader.load(opencv_objdetect.class);
            sideFaceHaarClassifier = new CvHaarClassifierCascade(cvLoad(classFile.getAbsolutePath()));
            classFile.delete();
            if (sideFaceHaarClassifier.isNull()) {
                throw new IOException("Error while loading the file for sideFaceHaarClassifier.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearOutput() {
        this.output.clear();
    }

}
