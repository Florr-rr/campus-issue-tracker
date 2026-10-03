package issuetracker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer id;

    @Column(name = "role_name", nullable = false, unique = true, length = 30)
    private String name;

    @Column(length = 255)
    private String description;

    protected Role() {
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}