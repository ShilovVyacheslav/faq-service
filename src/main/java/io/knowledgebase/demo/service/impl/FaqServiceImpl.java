package io.knowledgebase.demo.service.impl;

import io.knowledgebase.demo.common.validation.SortValidator;
import io.knowledgebase.demo.dto.faq.FaqCreateDto;
import io.knowledgebase.demo.dto.faq.FaqPreviewDto;
import io.knowledgebase.demo.dto.faq.FaqResponseDto;
import io.knowledgebase.demo.dto.faq.FaqUpdateDto;
import io.knowledgebase.demo.entity.Faq;
import io.knowledgebase.demo.entity.User;
import io.knowledgebase.demo.exception.FaqException;
import io.knowledgebase.demo.mapper.FaqMapper;
import io.knowledgebase.demo.repository.FaqRepository;
import io.knowledgebase.demo.service.FaqDocService;
import io.knowledgebase.demo.service.FaqService;
import io.knowledgebase.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {

    private final FaqRepository faqRepository;
    private final FaqMapper faqMapper;
    private final SortValidator sortValidator;
    private final FaqDocService faqDocService;
    private final UserService userService;

    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of("question", "updatedAt", "counter");

    @Override
    public FaqResponseDto createFaq(FaqCreateDto faqCreateDto) {
        User user = userService.getUserByJwt();
        Faq faq = faqMapper.toEntity(faqCreateDto, user);
        getFaqByQuestionAndAnswer(faq.getQuestion(), faq.getAnswer());

        Faq savedFaq = faqRepository.save(faq);

        return faqMapper.toResponseDto(faqDocService.moveFaqToMongo(savedFaq));
    }

    @Override
    @Transactional
    public FaqResponseDto updateFaq(Long id, FaqUpdateDto faqUpdateDto) {
        Faq existingFaq = getFaqById(id);

        faqMapper.updateFromDto(faqUpdateDto, existingFaq);
        faqDocService.updateFaqDoc(faqUpdateDto, id);

        return faqMapper.toResponseDto(existingFaq);
    }

    @Override
    public void deleteFaq(Long id) {
        if (!faqRepository.existsById(id)) {
            throw FaqException.faqNotFound(id);
        }
        faqRepository.deleteById(id);
        faqDocService.deleteFaqDoc(id);
    }

    @Override
    public Page<FaqResponseDto> readAllFaqs(Pageable pageable) {
        sortValidator.validate(pageable, ALLOWED_SORT_FIELDS);
        Pageable sorted = ensureDefaultSort(pageable);

        return faqRepository.findAll(sorted).map(faqMapper::toResponseDto);
    }

    @Override
    @Transactional
    public FaqResponseDto readFaqById(Long id) {
        Faq faq = getFaqById(id);
        return faqMapper.toResponseDto(faq);
    }

    @Override
    public List<FaqPreviewDto> searchFaqDocs(String query) {
        return faqDocService.searchFaqDocs(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaqResponseDto> searchFaqByKeyWord(String key) {
        if (key == null || key.isBlank()) {
            return Collections.emptyList();
        }
        String tsQuery = buildTsQuery(key);
        List<Faq> faqs = faqRepository.searchByTsQuery(tsQuery);
        return faqs.stream().map(faqMapper::toResponseDto).collect(Collectors.toList());
    }

    public String buildTsQuery(String input) {
        return Arrays.stream(input.trim().split("\\s+"))
                .map(word -> word + ":*")
                .collect(Collectors.joining(" & "));
    }

    private Pageable ensureDefaultSort(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(
                            Sort.Order.desc("counter"),
                            Sort.Order.desc("updatedAt"),
                            Sort.Order.asc("question")
                    ));
        }
        return pageable;
    }

    private Faq getFaqById(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> FaqException.faqNotFound(id));
    }

    private void getFaqByQuestionAndAnswer(String question, String answer) {
        Faq faq = faqRepository.getFaqByQuestionIgnoreCaseAndAnswerIgnoreCase(question, answer);
        if (faq != null) {
            throw FaqException.faqAlreadyExists(faq.getQuestion(), faq.getAnswer());
        }
    }
}
