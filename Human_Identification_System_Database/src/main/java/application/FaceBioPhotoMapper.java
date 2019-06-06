package application;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FaceBioPhotoMapper {

    private Blob mapStringToBlob(final String temp) throws Exception {
        byte[] tempInByte = Base64.decodeBase64(temp);
        return new SerialBlob(tempInByte);
    }

    private String mapBlobToString(final Blob blob) throws Exception {
        int blobLength = (int) blob.length();
        byte[] blobAsBytes = blob.getBytes(1, blobLength);
        return Base64.encodeBase64String(blobAsBytes);
    }

    private String handleException(final Blob blob) {
        String blobAsString = "";
        try {
            blobAsString = mapBlobToString(blob);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return blobAsString;
    }

    public List<FaceBioPhotoDto> mapToFaceBioPhotoDtoList(final List<FaceBioPhoto> faceBioPhotoList) {

        return faceBioPhotoList.stream()
                .map(t -> new FaceBioPhotoDto(t.getId(), t.getName(), handleException(t.getImage())))
                .collect(Collectors.toList());
    }

    public FaceBioPhoto mapToFaceBioPhoto(final FaceBioPhotoDto faceBioPhotoDto) {
        FaceBioPhoto faceBioPhoto = new FaceBioPhoto();
        try {
            faceBioPhoto = new FaceBioPhoto(
                    faceBioPhotoDto.getId(),
                    faceBioPhotoDto.getName(),
                    mapStringToBlob(faceBioPhotoDto.getImage())
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return faceBioPhoto;
    }

    public List<String> getPhotoNamesAsStringList(List<FaceBioPhoto> faceBioPhotoList) {
        return faceBioPhotoList.stream()
                .map(FaceBioPhoto::getName)
                .collect(Collectors.toList());
    }
}
