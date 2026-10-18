package com.project.backend.catalog;

import jakarta.persistence.*;

@Entity
@Table(name = "shows")
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String detail;

    private String imageUrl;
    private String startDate;
    private String endDate;

    protected Show() {}

    public Show(String name, String detail, String imageUrl, String startDate, String endDate) {
        this.name = name;
        this.detail = detail;
        this.imageUrl = imageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDetail() { return detail; }
    public String getImageUrl() { return imageUrl; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
}
