package application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/facebio")
public class MainController {
    @Autowired
    private FaceBioService faceBioService;

    @Autowired
    private FaceBioMapper faceBioMapper;

    @Autowired
    private FaceBioPhotoMapper faceBioPhotoMapper;

    @Autowired
    private FaceBioPhotoService faceBioPhotoService;

    private NamesWrapper namesWrapper;


    @RequestMapping(method = RequestMethod.GET, value = "getFaceBioByCode")
    public List<String> getFaceBioElements(@RequestParam int code) {
        return faceBioMapper.mapToStringList(faceBioService.getFaceBioByCode(code));
    }

    @RequestMapping(method = RequestMethod.GET, value = "getPhotos")
    public List<FaceBioPhotoDto> getPhotos() {
        return faceBioPhotoMapper.mapToFaceBioPhotoDtoList(faceBioPhotoService.getFaceBioPhoto(
                this.namesWrapper.getNamesList()));
    }

    @RequestMapping(method = RequestMethod.GET, value = "getSize")
    public long getSize() {
        return faceBioPhotoService.getCount();
    }

    @RequestMapping(method = RequestMethod.GET, value = "getNames")
    public List<String> getNames() {
        return faceBioPhotoMapper.getPhotoNamesAsStringList(faceBioPhotoService.getAllFaceBioPhotos());
    }

    @RequestMapping(method = RequestMethod.PUT, value = "savePhoto", consumes = APPLICATION_JSON_VALUE)
    public FaceBioPhoto savePhoto(@RequestBody FaceBioPhotoDto faceBioPhotoDto) {
        return faceBioPhotoService.savePhoto(faceBioPhotoMapper.mapToFaceBioPhoto(faceBioPhotoDto));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "setNamesList")
    public void setNamesList(@RequestBody NamesWrapper names) {
        this.namesWrapper = names;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "saveFaceBio", consumes = APPLICATION_JSON_VALUE)
    public FaceBioDto saveFaceBio(@RequestBody FaceBioDto faceBioDto) {
        return faceBioMapper.mapToFaceBioDto(faceBioService.saveFaceBio(faceBioMapper.mapToFaceBio(faceBioDto)));
    }
}


/*
 @RequestMapping(method = RequestMethod.GET, value = "getAllPhotos")
    public List<FaceBioPhoto> getAllPhotos() {
        return faceBioPhotoService.getAllFaceBioPhotos();
    }
 */