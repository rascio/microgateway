package it.r.ports.rest;

import lombok.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UriSupport {

    private static final Pattern TOKEN = Pattern.compile("\\{(\\w+)}");

    public static Set<String> tokens(String path) {
        final Matcher matcher = TOKEN.matcher(path);
        final Set<String> tokens = new HashSet<>();
        while (matcher.find()) {
            tokens.add(matcher.group(1));
        }
        return tokens;
    }

    @Value
    private static class PathToken {
        private String name;
        private int start;
        private int end;
    }
}
