package com.nhom12.enggo_backend.service.exam;

import com.nhom12.enggo_backend.dto.response.exam.FillBlankResultResponse;
import com.nhom12.enggo_backend.dto.response.exam.MatchingResultResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class ScoreCheck {
    private BigDecimal score;
    private boolean correct;
    private String userInput;
    private List<FillBlankResultResponse> blankResults;
    private List<MatchingResultResponse> matchingResults;

    public static ScoreCheck isCorrect(BigDecimal score, boolean isCorrect){
        return new ScoreCheck(score, isCorrect, null, List.of(), List.of());
    }

    public static ScoreCheck partial (BigDecimal score, boolean isCorrect){
        return new ScoreCheck(score, isCorrect, null, null, null);
    }

    public static ScoreCheck ofFillBlank(BigDecimal score, boolean correct, String userInput,
                                         List<FillBlankResultResponse> blankResults) {
        return new ScoreCheck(score, correct, userInput, blankResults, null);
    }

    public static ScoreCheck ofMatching(BigDecimal score, boolean correct, String userInput,
                                        List<MatchingResultResponse> matchingResults) {
        return new ScoreCheck(score, correct, userInput, null, matchingResults);
    }
}
