package application;

import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface FaceBioPhotoRepository extends CrudRepository<FaceBioPhoto, Long> {

    Optional<FaceBioPhoto> findByName(String name);

    @Override
    FaceBioPhoto save(FaceBioPhoto faceBioPhoto);

    @Override
    List<FaceBioPhoto> findAll();

    @Override
    long count();
}
