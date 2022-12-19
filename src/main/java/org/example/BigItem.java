package org.example;

import lombok.Data;
import org.hibernate.annotations.OptimisticLock;

import javax.persistence.*;

@Data
@Entity
@Table(name = "big_items")
public class BigItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "val")
    int val;

    @Column(name = "junkField")
    @OptimisticLock(excluded = true)
    int junkField;

    @Version
    long version;
}