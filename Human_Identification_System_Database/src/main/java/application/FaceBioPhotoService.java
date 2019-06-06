package application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FaceBioPhotoService {
    @Autowired
    private FaceBioPhotoRepository faceBioPhotoRepository;

    public List<FaceBioPhoto> getFaceBioPhoto(List<String> names) {
        return names.stream()
                .map(this::handleException)
                .collect(Collectors.toList());
    }

    private FaceBioPhoto handleException(String name) {
        FaceBioPhoto faceBioPhoto = new FaceBioPhoto();
        try {
            faceBioPhoto = faceBioPhotoRepository.findByName(name).orElseThrow(Exception::new);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return faceBioPhoto;
    }

    public FaceBioPhoto savePhoto(FaceBioPhoto faceBioPhoto) {
        return faceBioPhotoRepository.save(faceBioPhoto);
    }

    public List<FaceBioPhoto> getAllFaceBioPhotos() {
        return faceBioPhotoRepository.findAll();
    }

    public long getCount() {
        return faceBioPhotoRepository.count();
    }
}
