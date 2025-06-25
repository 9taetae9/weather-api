package com.fourthread.ozang.module.domain.weather.entity;

import com.fourthread.ozang.module.domain.weather.dto.TemperatureDto;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class TemperatureInfo {
    private Double current = 0.0;
    private Double comparedToDayBefore = 0.0;
    private Double min = 0.0;
    private Double max = 0.0;

    public TemperatureDto toDto() {
        return new TemperatureDto(current, comparedToDayBefore, min, max);
    }
}