package com.example.assessment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseQuestionDto {

    private Integer setId;
    private Integer questionId;
    private String questionText;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ResponseAnswerDto> answers;
}
