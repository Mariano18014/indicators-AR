package com.indicadoresar.ingestion.bcra;

import java.math.BigDecimal;
import java.time.LocalDate;

@Deprecated(since = "HU-02", forRemoval = true)
public record BcraExchangeRate(LocalDate date, BigDecimal value) {}
