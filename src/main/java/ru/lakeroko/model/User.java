package ru.lakeroko.model;

import lombok.Data;
import ru.lakeroko.BotState;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
//@Transactional(isolation = Isolation.SERIALIZABLE)
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private BigInteger userId;

    @Column(name = "username")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "utm")
    private String utm;

    @Column(name = "birthdate")
    private LocalDate birthDate;

    @Column(name = "gender")
    private String gender;

//    @Lob
//    @Column(name = "photo", columnDefinition = "bytea")
//    private byte[] photo;

    @Enumerated(EnumType.STRING)
    private BotState state;
}
