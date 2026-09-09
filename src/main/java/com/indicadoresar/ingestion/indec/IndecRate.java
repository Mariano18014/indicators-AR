package com.indicadoresar.ingestion.indec;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndecRate(LocalDate date, BigDecimal value) {}
