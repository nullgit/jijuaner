package com.yunzen.jijuaner.user.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

@Configuration
public class UserUtils {
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static <T> String listToString(List<T> list, String separator) {
        var sb = new StringBuilder();
        for (T element : list) {
            if (element != null) {
                sb.append(element.toString() + separator);
            }
        }
        return sb.toString();
    }

    // public static List<String> stringToList(String str, String separator) {
    //     return Arrays.asList(str.split(separator));
    // }

    public static List<String> stringToStringList(String str, String separator) {
        return Arrays.asList(str.split(separator)).stream().filter(StringUtils::hasText).toList();
    }

    public static List<Integer> stringToIntegerList(String str, String separator) {
        return Arrays.asList(str.split(separator)).stream()
                .map(Integer::parseInt).toList();
    }

    public static <T> void swapListElement(List<T> list, int idx1, int idx2) {
        T tmp = list.get(idx1);
        list.set(idx1, list.get(idx2));
        list.set(idx2, tmp);
    }
}
