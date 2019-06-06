package application;

import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface FaceBioRepository extends CrudRepository<FaceBio, Long> {

    Optional<List<FaceBio>> findByCode(int code);

    @Override
    FaceBio save(FaceBio faceBio);

}
