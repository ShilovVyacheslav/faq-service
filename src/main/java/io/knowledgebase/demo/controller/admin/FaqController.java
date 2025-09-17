package io.knowledgebase.demo.controller.admin;

import io.knowledgebase.demo.dto.faq.FaqCreateDto;
import io.knowledgebase.demo.dto.faq.FaqPreviewDto;
import io.knowledgebase.demo.dto.faq.FaqResponseDto;
import io.knowledgebase.demo.dto.faq.FaqUpdateDto;
import io.knowledgebase.demo.service.FaqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/faq")
public class FaqController {

    private final FaqService faqService;

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    @PostMapping("/add")
    public ResponseEntity<FaqResponseDto> createQuestion(
            @RequestBody @Valid FaqCreateDto faqCreateDto) {
        FaqResponseDto response = faqService.createFaq(faqCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    @PutMapping("/{id}")
    public ResponseEntity<FaqResponseDto> updateQuestion(@PathVariable Long id,
                                                         @RequestBody @Valid FaqUpdateDto faqUpdateDto) {
        FaqResponseDto updated = faqService.updateFaq(id, faqUpdateDto);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        faqService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    @GetMapping("/all")
    public ResponseEntity<Page<FaqResponseDto>> getAllFaqs(
            @PageableDefault(size = 50, sort = {"counter", "updatedAt"}, direction = Sort.Direction.DESC
            ) Pageable pageable) {
        return ResponseEntity.ok(faqService.readAllFaqs(pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @GetMapping("/{id}")
    public ResponseEntity<FaqResponseDto> getFaqDetailsById(@PathVariable Long id) {
        FaqResponseDto faqResponseDto = faqService.readFaqById(id);
        return ResponseEntity.ok(faqResponseDto);
    }

    // @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @GetMapping("/pg-search")
    public ResponseEntity<List<FaqResponseDto>> searchFaqByKeyWord(@RequestParam(name = "key") String key) {
        return ResponseEntity.ok(faqService.searchFaqByKeyWord(key));
    }

    // @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @GetMapping("/search")
    public ResponseEntity<List<FaqPreviewDto>> searchFaqDocs(@RequestParam(name = "query") String query) {
        return ResponseEntity.ok(faqService.searchFaqDocs(query));
    }

}
