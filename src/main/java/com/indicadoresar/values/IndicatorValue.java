package com.indicadoresar.values;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import com.indicadoresar.indicators.Indicator;

@Entity
@Table(
        name = "indicator_value",
        uniqueConstraints = @UniqueConstraint(columnNames = {"indicator_id", "date"}))
public class IndicatorValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "indicator_id", nullable = false)
    private Indicator indicator;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "value", nullable = false, precision = 18, scale = 4)
    private BigDecimal value;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    protected IndicatorValue() {}

    public IndicatorValue(Indicator indicator, LocalDate date, BigDecimal value, Instant ingestedAt) {
        this.indicator = indicator;
        this.date = date;
        this.value = value;
        this.ingestedAt = ingestedAt;
    }

    public Long getId() {
        return id;
    }

    public Indicator getIndicator() {
        return indicator;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getValue() {
        return value;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }

    public void updateValue(BigDecimal newValue, Instant newIngestedAt) {
        this.value = newValue;
        this.ingestedAt = newIngestedAt;
    }
}
