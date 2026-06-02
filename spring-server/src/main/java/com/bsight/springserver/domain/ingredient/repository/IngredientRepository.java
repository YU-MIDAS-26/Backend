package com.bsight.springserver.domain.ingredient.repository;

import com.bsight.springserver.domain.ingredient.entity.Ingredient;
import com.bsight.springserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findAllByUserOrderByCreatedAtDesc(User user);

    Optional<Ingredient> findByUserAndName(User user, String name);

    Optional<Ingredient> findByIdAndUser(Long id, User user);
}
