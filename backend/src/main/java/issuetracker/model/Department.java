package issuetracker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected Department() {
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getContactEmail() { return contactEmail; }
    public boolean isActive() { return active; }
}