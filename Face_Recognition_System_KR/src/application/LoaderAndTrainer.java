package application;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.*;

//import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.LBPHFaceRecognizer;

import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;

import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;

@Component
@Getter
@Setter
public class LoaderAndTrainer {

    private static final String ULRADDRESS = "http://localhost:8000/v1/facebio/";
    //"https://stark-beyond-17099.herokuapp.com/v1/facebio/";

    LBPHFaceRecognizer lbphfFaceRecognizer;
    File mainRoot;
    MatVector matVectorImages;
    Mat recognitionLabels;

    private List<FaceBioPhoto> faceBioPhotoList;
    private byte[] photoAsByte;
    private IplImage temp;

    @Autowired
    private RestTemplate restTemplate;

    private NamesWrapperDto namesWrapperDto;

    private String trainingDir = "./faces";


    public void init() {
        // mention the directory the facesSequence has been saved
        try {
            Long size = restTemplate.getForObject(ULRADDRESS + "getSize",
                    Long.class);
            ResponseEntity<List<String>> namesResponse = restTemplate.exchange(
                    ULRADDRESS + "getNames",
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
                    }
            );
            List<String> photosNames = namesResponse.getBody();
            namesWrapperDto = new NamesWrapperDto();
            mainRoot = new File(trainingDir);
            int len = mainRoot.listFiles().length;
            if (size != 0 && photosNames.size() != 0) {

                if ((len != 0) && (size > len)) {

                    for (int i = 0; i <= size - len; i = i + 10) {

                        if (compareNames(trainingDir, photosNames).size()
                                - namesWrapperDto.getNamesList().size() >= 10) {
                            namesWrapperDto.setNamesList(compareNames(trainingDir, photosNames).subList(i, i + 10));
                        } else {
                            namesWrapperDto.setNamesList(compareNames(trainingDir, photosNames));
                        }
                        convertAndSave(namesWrapperDto);
                    }
                } else if (compareNames(trainingDir, photosNames).size() != 0) {
                    for (int i = 0; i <= size; i = i + 10) {

                        if ((compareNames(trainingDir, photosNames).size()
                                - namesWrapperDto.getNamesList().size()) >= 10) {
                            namesWrapperDto.setNamesList(compareNames(trainingDir, photosNames).subList(i, i + 10));
                        } else {
                            namesWrapperDto.setNamesList(compareNames(trainingDir, photosNames));
                        }
                        convertAndSave(namesWrapperDto);
                    }
                }
            }
            if (mainRoot.listFiles() == null || len == 0) {
                // disable recognition process
            } else {
                this.trainAlgorithm();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Confidence value less than 60 means face is known
    //Confidence value greater than 60 means face is unknown

    public int recognize(IplImage imageData) {

        Mat imageDataAsMat = cvarrToMat(imageData);

        cvtColor(imageDataAsMat, imageDataAsMat, CV_BGR2GRAY);
        equalizeHist(imageDataAsMat, imageDataAsMat);

        DoublePointer confidenceOfRecognition = new DoublePointer(0);
        IntPointer labelOfCode = new IntPointer(1);
        this.lbphfFaceRecognizer.predict(imageDataAsMat, labelOfCode, confidenceOfRecognition);

        System.out.println(confidenceOfRecognition.get(0));
        int predictedCodeLabel = labelOfCode.get(0);
        //System.out.println(predictedCodeLabel);

        if (confidenceOfRecognition.get(0) > 60) {
            return -1;
        }
        return predictedCodeLabel;
    }


    public List<String> compareNames(String trainingDir, List<String> names) {
        List<String> results = new ArrayList<>();
        List<String> differentNames = new ArrayList<>();
        File[] files = new File(trainingDir).listFiles();
        for (File file : files) {
            if (file.isFile()) {
                results.add(file.getName());
            }
        }
        for (int i = 0; i < names.size(); i++) {
            if (!results.contains(names.get(i))) {
                differentNames.add(names.get(i));
            }
        }
        return differentNames;
    }

    public void convertAndSave(NamesWrapperDto namesWrapperDto) {
        String gsonString;
        Gson gson = new Gson();
        gsonString = gson.toJson(namesWrapperDto);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(gsonString, headers);
        restTemplate.put(ULRADDRESS + "setNamesList", entity);
        ResponseEntity<List<FaceBioPhoto>> response = restTemplate.exchange(
                ULRADDRESS + "getPhotos",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<FaceBioPhoto>>() {
                });
        List<FaceBioPhoto> listOfPhotos = response.getBody();
        try {
            for (FaceBioPhoto photo : listOfPhotos) {
                photoAsByte = Base64.decodeBase64(photo.getImage());
                InputStream in = new ByteArrayInputStream(photoAsByte);
                BufferedImage bImageFromConvert = ImageIO.read(in);
                OpenCVFrameConverter.ToIplImage iplConverter = new OpenCVFrameConverter.ToIplImage();
                Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
                this.temp = iplConverter.convert(java2dConverter.convert(bImageFromConvert));
                Mat matImage = new Mat(temp);
                imwrite("faces/" + photo.getName(), matImage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void trainAlgorithm() {
        mainRoot = new File(trainingDir);

        FilenameFilter imgFilter = (File dir, String name) -> {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
        };
        File[] imageFiles = mainRoot.listFiles(imgFilter);

        this.matVectorImages = new MatVector(imageFiles.length);

        this.recognitionLabels = new Mat(imageFiles.length, 1, CV_32SC1);
        IntBuffer labelsBuf = recognitionLabels.createBuffer();

        int counter = 0;

        for (File image : imageFiles) {
            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

            int label = Integer.parseInt(image.getName().split("-")[0]);

            matVectorImages.put(counter, img);

            labelsBuf.put(counter, label);

            counter++;
        }
        // face training
        this.lbphfFaceRecognizer = LBPHFaceRecognizer.create();
        this.lbphfFaceRecognizer.train(matVectorImages, recognitionLabels);
    }
}
