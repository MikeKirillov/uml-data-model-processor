package com.github.mikekirillov;

import java.util.List;

@FunctionalInterface
public interface PlantUmlParser<T> {
    List<T> parseLinesFrom(List<String> lines);
}
