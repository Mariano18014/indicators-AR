package com.indicadoresar.indicators;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "indicator")
public class Indicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "unit", nullable = false, length = 50)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private IndicatorSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 20)
    private IndicatorFrequency frequency;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    protected Indicator() {}

    public Indicator(
            String code,
            String name,
            String unit,
            IndicatorSource source,
            IndicatorFrequency frequency,
            String description) {
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.source = source;
        this.frequency = frequency;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public IndicatorSource getSource() {
        return source;
    }

    public IndicatorFrequency getFrequency() {
        return frequency;
    }

    public String getDescription() {
        return description;
    }
}
