package com.quickbite.orderservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long userId;

    @NotBlank(message = "Описание не должно быть пустым")
    @Column(nullable = false)
    private String description;

    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть больше 0")
    @Column(nullable = false)
    private BigDecimal price;
}