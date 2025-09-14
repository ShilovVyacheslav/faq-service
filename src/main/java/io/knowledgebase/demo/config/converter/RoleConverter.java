package io.knowledgebase.demo.config.converter;

import io.knowledgebase.demo.enums.Role;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RoleConverter implements Converter<String, Role> {

    @Override
    public Role convert(@NonNull String source) {
        return Role.fromString(source);
    }
}
