package it.r.ports.utils.beans;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Value;

import java.util.List;

@Getter
public class MappingException extends RuntimeException {

    private final List<FailedMapping> failedMappings;
    private Class<?> type;

    public static void checkNoErrors(List<FailedMapping> failedMappings, Class<?> type) throws MappingException {
        if (failedMappings != null && !failedMappings.isEmpty()) {
            throw new MappingException(ImmutableList.copyOf(failedMappings), type);
        }
    }


    public MappingException(List<FailedMapping> failedMappings, Class<?> type) {
        super(String.format("Error during conversion of [%s.%s] for value: %s", type.getName(), failedMappings));
        this.failedMappings = failedMappings;
    }

    @Value
    public static class FailedMapping {
        private String property;
        private Object invalidValue;
        private transient Exception exception;
    }
}
