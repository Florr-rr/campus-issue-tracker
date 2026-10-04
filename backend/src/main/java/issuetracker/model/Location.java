package issuetracker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Integer id;

    @Column(nullable = false, length = 80)
    private String building;

    @Column(length = 10)
    private String floor;

    @Column(length = 30)
    private String room;

    @Column(length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected Location() {
    }

    public Integer getId() { return id; }
    public String getBuilding() { return building; }
    public String getFloor() { return floor; }
    public String getRoom() { return room; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }

    public String displayName() {
        StringBuilder sb = new StringBuilder(building);
        if (floor != null) sb.append(", floor ").append(floor);
        if (room != null) sb.append(", room ").append(room);
        return sb.toString();
    }
}