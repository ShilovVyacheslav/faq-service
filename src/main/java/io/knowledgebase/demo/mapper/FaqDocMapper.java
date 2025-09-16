package io.knowledgebase.demo.mapper;

import io.knowledgebase.demo.document.FaqDoc;
import io.knowledgebase.demo.dto.faq.FaqCreateDto;
import io.knowledgebase.demo.dto.faq.FaqPreviewDto;
import io.knowledgebase.demo.dto.faq.FaqUpdateDto;
import io.knowledgebase.demo.entity.Faq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FaqDocMapper {

    @Mapping(target = "id", ignore = true)
    FaqDoc toEntity(FaqCreateDto faqCreateDto);

    FaqPreviewDto toResponseDto(FaqDoc faqDoc);

    @Mapping(target = "id", ignore = true)
    void updateFromDto(FaqUpdateDto faqUpdateDto, @MappingTarget FaqDoc faqDoc);

    @Mapping(target = "active", source = "active")
    FaqDoc fromFaqToFaqDoc(Faq faq);

}
