package com.indicadoresar.ingestion.bcra;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BcraExchangeRate(LocalDate date, BigDecimal value) {}
