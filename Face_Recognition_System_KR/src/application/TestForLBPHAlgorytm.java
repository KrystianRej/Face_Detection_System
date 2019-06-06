package application;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_core.*;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.*;

import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;

import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.DoublePointer;

public class TestForLBPHAlgorytm {

    private LBPHFaceRecognizer faceRecognizer;

    public File root;
    private MatVector images;
    private Mat labels;

    public static void main(String[] args) {
        String testDir = "./facesToTest";
        //Testing recognition for photos
        TestForLBPHAlgorytm testForLBPHAlgorytm = new TestForLBPHAlgorytm();
        IplImage testImage = cvLoadImage(testDir + "/1-test.pgm");
        testForLBPHAlgorytm.init();
        System.out.println("Docelowy kod = 1" + " wykryty kod :" + testForLBPHAlgorytm.recognize(testImage));

        testImage = cvLoadImage(testDir + "/2-test.pgm");
        testForLBPHAlgorytm.init();
        System.out.println("Docelowy kod = 2" + " wykryty kod :" + testForLBPHAlgorytm.recognize(testImage));

        testImage = cvLoadImage(testDir + "/3-test.pgm");
        testForLBPHAlgorytm.init();
        System.out.println("Docelowy kod = 3" + " wykryty kod :" + testForLBPHAlgorytm.recognize(testImage));

        testImage = cvLoadImage(testDir + "/4-test.pgm");
        testForLBPHAlgorytm.init();
        System.out.println("Docelowy kod = 4" + " wykryty kod :" + testForLBPHAlgorytm.recognize(testImage));

        testImage = cvLoadImage(testDir + "/5-test.pgm");
        testForLBPHAlgorytm.init();
        System.out.println("Docelowy kod = 5" + " wykryty kod :" + testForLBPHAlgorytm.recognize(testImage));

        testImage = cvLoadImage(testDir + "/6-test.pgm");
        testForLBPHAlgorytm.init();
        System.out.println("Docelowy kod = 6" + " wykryty kod :" + testForLBPHAlgorytm.recognize(testImage));

        testImage = cvLoadImage(testDir + "/7-test.pgm");
        testForLBPHAlgorytm.init();
        System.out.println("Docelowy kod = 7" + " wykryty kod :" + testForLBPHAlgorytm.recognize(testImage));

        testImage = cvLoadImage(testDir + "/8-test.pgm");
        testForLBPHAlgorytm.init();
        System.out.println("Docelowy kod = 8" + " wykryty kod :" + testForLBPHAlgorytm.recognize(testImage));

        testImage = cvLoadImage(testDir + "/9-test.pgm");
        testForLBPHAlgorytm.init();
        System.out.println("Docelowy kod = 9" + " wykryty kod :" + testForLBPHAlgorytm.recognize(testImage));

        testImage = cvLoadImage(testDir + "/10-test.pgm");
        testForLBPHAlgorytm.init();
        System.out.println("Docelowy kod = 10" + " wykryty kod :" + testForLBPHAlgorytm.recognize(testImage));
    }


    public void init() {
        // mention the directory the faces has been saved
        String trainingDir = "./TestFaces";

        root = new File(trainingDir);

        FilenameFilter imgFilter = (dir, name) -> {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
        };

        File[] imageFiles = root.listFiles(imgFilter);

        this.images = new MatVector(imageFiles.length);

        this.labels = new Mat(imageFiles.length, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();

        int counter = 0;

        for (File image : imageFiles) {
            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

            int label = Integer.parseInt(image.getName().split("\\-")[0]);

            images.put(counter, img);

            labelsBuf.put(counter, label);

            counter++;
        }
        this.faceRecognizer = LBPHFaceRecognizer.create();
        this.faceRecognizer.train(images, labels);

    }

    public int recognize(IplImage faceData) {

        Mat faces = cvarrToMat(faceData);

        cvtColor(faces, faces, CV_BGR2GRAY);

        IntPointer label = new IntPointer(1);
        DoublePointer confidence = new DoublePointer(0);


        this.faceRecognizer.predict(faces, label, confidence);


        int predictedLabel = label.get(0);

        System.out.println("Test : Confidence :" + confidence.get(0));

        if (confidence.get(0) > 100) {
            return -1;
        }

        return predictedLabel;

    }
}