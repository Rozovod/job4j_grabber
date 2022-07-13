package ru.job4j.grabber.utils;

import java.time.LocalDateTime;

public class HabrCareerDateTimeParser implements DateTimeParser {
    @Override
    public LocalDateTime parse(String parse) {
        String[] str = parse.split("\\+", 2);
        return LocalDateTime.parse(str[0]);
    }
}
