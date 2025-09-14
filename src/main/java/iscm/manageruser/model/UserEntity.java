package iscm.manageruser.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users",uniqueConstraints = {@UniqueConstraint(columnNames = {"username"} )})
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Size(max=30)

    private String username;
    @NotBlank
    private String password;

    @Email
    @NotBlank
    @Size(max=80)
    private String email;

    @NotBlank
    @Size(max=25)
    private String primer_nombre;
    @Size(max=25)
    private String segundo_nombre;


    @Size(max=25)
    private String apellido_paterno;

    @Size(max=25)
    private String apellido_materno;
    @NotBlank
    @Size(max=20)
    private String punto;
    @NotBlank
    @Size(max=20)
    private String ciudad;
    @NotBlank
    @Size(max=45)
    private String cargo;

    private LocalDate fecha_caducidad_password;


    private int intentos_ingreso;

    private boolean bloqueado;


    @ManyToMany(fetch=FetchType.EAGER, targetEntity = RoleEntity.class, cascade= CascadeType.ALL)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name= "user_id"), inverseJoinColumns = @JoinColumn(name="role_id"))
    private Set<RoleEntity> roles;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name="user_id")
    private Set<OldPassword> old_passwords;
}