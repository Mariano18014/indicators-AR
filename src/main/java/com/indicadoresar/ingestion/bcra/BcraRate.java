package com.indicadoresar.ingestion.bcra;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BcraRate(LocalDate date, BigDecimal value) {}
