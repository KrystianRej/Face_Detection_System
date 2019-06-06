package application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonIgnoreProperties
public class FaceBioDto {

    @JsonProperty("id")
    private long id;

    @JsonProperty("code")
    private int code;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    /*@JsonProperty("reg")
    private long reg;*/

    @JsonProperty("age")
    private long age;

    /*@JsonProperty("section")
    private String section;*/
}
