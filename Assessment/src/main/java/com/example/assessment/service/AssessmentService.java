package com.example.assessment.service;

import com.example.assessment.dto.*;
import com.example.assessment.exception.SetNameExist;
import com.example.assessment.repository.AnswerRepository;
import com.example.assessment.repository.QuestionRepository;
import com.example.assessment.repository.SetInfoRepository;
import com.example.assessment.exception.QuestionIdNotFoundException;
import com.example.assessment.exception.SetIdNotFoundException;
import com.example.assessment.exception.SetNameNotFoundException;
import com.example.assessment.model.OptionModel;
import com.example.assessment.model.Question;
import com.example.assessment.model.SetInfo;
import com.example.assessment.model.SetStatus;
import org.hibernate.mapping.Set;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class AssessmentService {

    private final SetInfoRepository setInfoRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public AssessmentService(SetInfoRepository setInfoRepository, QuestionRepository questionRepository, AnswerRepository answerRepository) {
        this.setInfoRepository = setInfoRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    private ResponseAnswerDto mapAnswerToDto(OptionModel answer, Integer questionId) {
        ResponseAnswerDto responseAnswerDto = new ResponseAnswerDto();
        responseAnswerDto.setQuestionId(questionId);
        responseAnswerDto.setAnswerId((answer.getAnswerId()));
        responseAnswerDto.setAnswer(answer.getAnswer());
        responseAnswerDto.setSuggestion(answer.getSuggestion());
        return responseAnswerDto;
    }

    private ResponseQuestionDto mapQuestionToDto(Question question, Integer setId) {
        ResponseQuestionDto responseQuestionDto = new ResponseQuestionDto();
        responseQuestionDto.setSetId(setId);
        responseQuestionDto.setQuestionId(question.getQuestionId());
        responseQuestionDto.setQuestionText(question.getQuestionText());
        if (question.getAnswers()==null){
            responseQuestionDto.setAnswers(null);
        }else {
            List<ResponseAnswerDto> responseAnswers = question.getAnswers().stream()
                    .map(answer -> mapAnswerToDto(answer, question.getQuestionId()))
                    .toList();

            responseQuestionDto.setAnswers(responseAnswers);
        }
        return responseQuestionDto;
    }

    private ResponseSetDto mapSetInfoToDto(SetInfo setInfo) {
        ResponseSetDto responseSetDto = new ResponseSetDto();
        responseSetDto.setSetId(setInfo.getSetId());
        responseSetDto.setSetName(setInfo.getSetName());
        responseSetDto.setCreatedBy(setInfo.getCreatedBy());
        responseSetDto.setCreatedAt(setInfo.getCreatedAt());
        responseSetDto.setModifiedAt(setInfo.getModifiedAt());
        responseSetDto.setDomain(setInfo.getDomain());
        responseSetDto.setStatus(String.valueOf(setInfo.getStatus()));

        List<ResponseQuestionDto> responseQuestions = setInfo.getQuestions().stream()
                .map(question -> mapQuestionToDto(question, setInfo.getSetId()))
                .toList();

        responseSetDto.setQuestions(responseQuestions);
        return responseSetDto;
    }

    public List<Question> getSetBySetName(Integer setId)  {
        Optional<SetInfo> setInfos = setInfoRepository.findById(setId);

        if (setInfos.isPresent()) {
            SetInfo setInfo = setInfos.get();

            for (Question question : setInfo.getQuestions()) {
                question.setAnswers(null);

            }

            return setInfo.getQuestions();

        }
        else {
            throw new SetIdNotFoundException(setId);
        }
    }
    public ResponseSetDto saveSetInfo(RequestSetDto setDto) {

        Optional<SetInfo> seti = setInfoRepository.findBySetName(setDto.getSetName());
        if (seti.isPresent()) {
            throw new SetNameExist(setDto.getSetName());
        }
        SetInfo setInfo =new SetInfo();
        setInfo.setSetName(setDto.getSetName());
        setInfo.setCreatedBy("Admin");
        setInfo.setStatus(SetStatus.PENDING);
        setInfo.setDomain(setDto.getDomain());
        List<Question> questions = new ArrayList<>();
        for (RequestQuestionDto question : setDto.getQuestions()) {
            Question questionQuestion = new Question();
            questionQuestion.setQuestionText(question.getQuestionText());
            List<OptionModel> optionModels = new ArrayList<>();
            for (RequestAnswerDto answer : question.getAnswers()){
                OptionModel optionModel = new OptionModel();
                optionModel.setAnswer(answer.getAnswer());
                optionModel.setSuggestion(answer.getSuggestion());
                optionModels.add(optionModel);
            }
            questionQuestion.setAnswers(optionModels);
            questions.add(questionQuestion);

        }
        setInfo.setQuestions(questions);
        for (Question question : setInfo.getQuestions()) {
            for (OptionModel answer : question.getAnswers()) {
                answerRepository.save(answer);
            }
            questionRepository.save(question);
        }
        setInfoRepository.save(setInfo);

        return mapSetInfoToDto(setInfo);
    }



    public List<SetDto> getAllSet() {

        return mapSetInfoListToSetDtoList(setInfoRepository.findAll());
    }

    public List<SetDto> mapSetInfoListToSetDtoList(List<SetInfo> setInfoList) {
        return setInfoList.stream()
                .map(this::mapSetInfoToSetDto)
                .toList();
    }

    private SetDto mapSetInfoToSetDto(SetInfo setInfo) {
        return new SetDto(
                setInfo.getSetId(),
                setInfo.getSetName(),
                setInfo.getCreatedBy(),
                setInfo.getDomain(),
                setInfo.getStatus()
        );
    }

    public ResponseQuestionDto modifySetQuestionInfo(Integer setId, Integer questionId, List<RequestAnswerDto> options) {


        Optional<SetInfo> setInfo = setInfoRepository.findById(setId);

        Optional<Question> currentQuestion = questionRepository.findById(questionId);
        if (currentQuestion.isPresent() && setInfo.isPresent()){

            setInfo.get().setModifiedAt(new Timestamp(System.currentTimeMillis()));
            List<OptionModel> optionsList = new ArrayList<>();
            for (RequestAnswerDto answer:options){
                OptionModel optionModel = new OptionModel();
                optionModel.setAnswer(answer.getAnswer());
                optionModel.setSuggestion(answer.getSuggestion());
                optionsList.add(optionModel);
            }
            currentQuestion.get().setAnswers(optionsList);
            ResponseQuestionDto question = mapQuestionToDto(questionRepository.save(currentQuestion.get()),setId);
            return question;

        }
        else if (setInfo.isEmpty()) {
            throw new SetIdNotFoundException(setId);
        } else {
            throw new QuestionIdNotFoundException(questionId);
        }
    }

    public void deleteQuestionFromAssessment(Integer setId, Integer questionId) {
        Optional<SetInfo> setInfo = setInfoRepository.findById(setId);
        Optional<Question> question = questionRepository.findById(questionId);
        if (question.isPresent() && setInfo.isPresent()){
            questionRepository.deleteById(questionId);
        } else if (setInfo.isEmpty()) {
            throw new SetIdNotFoundException(setId);
        } else {
            throw new QuestionIdNotFoundException(questionId);
        }

    }

    public ResponseSetDto getSetBySetId(String setName) {
        Optional<SetInfo> setInfos = setInfoRepository.findBySetName(setName);

        if (setInfos.isPresent()) {
            SetInfo setInfo = setInfos.get();
            ResponseSetDto responseSetDto = mapSetInfoToDto(setInfo);

            return responseSetDto;

        }
        else {
            throw new SetNameNotFoundException(setName);
        }

    }
}