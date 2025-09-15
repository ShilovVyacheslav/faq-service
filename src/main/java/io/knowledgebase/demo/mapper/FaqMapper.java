package io.knowledgebase.demo.mapper;

import io.knowledgebase.demo.dto.faq.FaqCreateDto;
import io.knowledgebase.demo.dto.faq.FaqResponseDto;
import io.knowledgebase.demo.dto.faq.FaqUpdateDto;
import io.knowledgebase.demo.entity.Faq;
import io.knowledgebase.demo.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FaqMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "question", expression = "java(trim(faqCreateDto.getQuestion()))")
    @Mapping(target = "answer", expression = "java(trim(faqCreateDto.getAnswer()))")
    @Mapping(target = "description", expression = "java(trim(faqCreateDto.getDescription()))")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "counter", constant = "0")
    @Mapping(target = "showInTg", expression = "java(faqCreateDto.getShowInTg() != null ? faqCreateDto.getShowInTg() : Boolean.FALSE)")
    Faq toEntity(FaqCreateDto faqCreateDto, User createdBy);

    @Mapping(source = "createdBy.fullname", target = "createdBy")
    @Mapping(source = "updatedBy.fullname", target = "updatedBy")
    FaqResponseDto toResponseDto(Faq faq);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "active", source = "faqUpdateDto.active")
    void updateFromDto(FaqUpdateDto faqUpdateDto, @MappingTarget Faq entity, User updatedBy);

    default String trim(String text) {
        return text == null ? null : text.trim();
    }

}
