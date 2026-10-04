package issuetracker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(length = 255)
    private String description;

    @ManyToOne
    @JoinColumn(name = "default_department_id")
    private Department defaultDepartment;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected Category() {
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Department getDefaultDepartment() { return defaultDepartment; }
    public boolean isActive() { return active; }
}