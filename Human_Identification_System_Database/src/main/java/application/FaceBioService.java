package application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FaceBioService {
    @Autowired
    private FaceBioRepository faceBioRepository;

    public FaceBio getFaceBioByCode(int code) {
        Optional<List<FaceBio>> optionalList = faceBioRepository.findByCode(code);
        FaceBio faceBio = new FaceBio();
        if (optionalList.isPresent()) {
            faceBio = optionalList.get().get(0);
        }
        return faceBio;
    }

    public FaceBio saveFaceBio(FaceBio faceBio) {
        return faceBioRepository.save(faceBio);
    }
}
