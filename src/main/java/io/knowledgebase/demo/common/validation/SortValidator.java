package io.knowledgebase.demo.common.validation;

import io.knowledgebase.demo.exception.InvalidSortFieldException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SortValidator {
    public void validate(Pageable pageable, Set<String> allowedFields) {
        for (Sort.Order order : pageable.getSort()) {
            if (!allowedFields.contains(order.getProperty())) {
                throw InvalidSortFieldException.invalidSortFieldException(order.getProperty());
            }
        }
    }
}
