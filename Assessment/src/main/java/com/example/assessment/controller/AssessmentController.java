package com.example.assessment.controller;

import com.example.assessment.dto.*;
import com.example.assessment.model.OptionModel;
import com.example.assessment.model.SetInfo;
import com.example.assessment.service.AssessmentService;
import com.example.assessment.model.Question;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assessment")
public class AssessmentController {


    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }



    @GetMapping
    public ResponseEntity<List<SetDto>> getAllAssessments() {
        return ResponseEntity.ok(assessmentService.getAllSet());
    }
    @PostMapping
    public ResponseEntity<ResponseSetDto> createAssessment(@RequestBody RequestSetDto fullResponse) {
        return new ResponseEntity<>(assessmentService.saveSetInfo(fullResponse), HttpStatus.CREATED);
    }
    @GetMapping("setId/{setId}")
    public ResponseEntity<List<Question>> getAssessmentBySetName(@PathVariable Integer setId) {
        return ResponseEntity.ok(assessmentService.getSetBySetName(setId));
    }

    @PutMapping("/{setId}/{questionId}")
    public ResponseEntity<ResponseQuestionDto> updateQuestionInAssessment(@PathVariable Integer setId, @PathVariable Integer questionId, @RequestBody List<RequestAnswerDto> optionModels) {

        return ResponseEntity.ok(assessmentService.modifySetQuestionInfo(setId, questionId,optionModels));
    }
    @DeleteMapping("/{setId}/{questionId}")
    public ResponseEntity<Void> deleteQuestionFromAssessment(@PathVariable Integer setId, @PathVariable Integer questionId) {
        assessmentService.deleteQuestionFromAssessment(setId, questionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{setName}")
    public ResponseEntity<ResponseSetDto> getAssessmentBySetId(@PathVariable String setName) {
        return ResponseEntity.ok(assessmentService.getSetBySetId(setName));
    }

}