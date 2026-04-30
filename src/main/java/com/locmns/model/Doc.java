package com.locmns.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.view.DocView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Doc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(DocView.class)
    protected Integer id;

    @Column(length = 100, nullable = false)
    @NotBlank
    @Size(min = 3, max = 100)
    @JsonView(DocView.class)
    protected String title;

    @Column(nullable = false)
    @JsonView(DocView.class)
    protected String url;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @JsonView(DocView.class)
    private LocalDateTime addedDate;

    @ManyToMany
    @JoinTable(
            name = "fait_reference",
            joinColumns = @JoinColumn(name = "doc_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    protected List<Equipment> equipments;
}
