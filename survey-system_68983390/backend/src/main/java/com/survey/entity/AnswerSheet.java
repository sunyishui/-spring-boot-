package com.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("answer_sheet")
public class AnswerSheet {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long surveyId;

    private Long userId;

    private LocalDateTime submitTime;

    @TableLogic
    private Integer deleted;
}
