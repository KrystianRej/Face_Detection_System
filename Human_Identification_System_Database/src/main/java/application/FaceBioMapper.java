package application;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FaceBioMapper {
    public FaceBio mapToFaceBio(final FaceBioDto faceBioDto) {
        return new FaceBio(
                faceBioDto.getId(),
                faceBioDto.getCode(),
                faceBioDto.getFirstName(),
                faceBioDto.getLastName(),
                //faceBioDto.getReg(),
                faceBioDto.getAge()
                //faceBioDto.getSection()
        );
    }

    public FaceBioDto mapToFaceBioDto(final FaceBio faceBio) {
        return new FaceBioDto(
                faceBio.getId(),
                faceBio.getCode(),
                faceBio.getFirstName(),
                faceBio.getLastName(),
                //faceBio.getReg(),
                faceBio.getAge()
                //faceBio.getSection()
        );
    }

    public List<String> mapToStringList(final FaceBio faceBio) {
        List<String> user = new ArrayList<>();

        user.add(0, Integer.toString(faceBio.getCode()));
        user.add(1, faceBio.getFirstName());
        user.add(2, faceBio.getLastName());
        //user.add(3, Integer.toString((int)faceBio.getReg()));
        user.add(3, Integer.toString((int) faceBio.getAge()));
        //user.add(5, faceBio.getSection());

        return user;
    }
}
