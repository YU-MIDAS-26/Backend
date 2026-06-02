package com.bsight.springserver.domain.ingredient.entity;

import com.bsight.springserver.common.entity.BaseEntity;
import com.bsight.springserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "ingredients",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_ingredient_user_name",
                columnNames = {"user_id", "name"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ingredient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30)
    private String unit;

    @Builder
    public Ingredient(User user, String name, String unit) {
        this.user = user;
        this.name = name;
        this.unit = unit;
    }
}
