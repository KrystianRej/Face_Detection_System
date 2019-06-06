package application;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


@Entity
@Table(name = "face_bio")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FaceBio {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "code")
    @NotNull
    private int code;

    @Column(name = "first_name")
    @NotNull
    private String firstName;

    @Column(name = "last_name")
    @NotNull
    private String lastName;

        /*@Column(name = "reg")
        @NotNull
        private long reg;*/

    @Column(name = "age")
    @NotNull
    private long age;

       /* @Column(name = "section")
        @NotNull
        private String section;*/
}
