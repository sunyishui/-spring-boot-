package com.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("answer")
public class Answer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long answerSheetId;

    private Long questionId;

    private String optionIds;

    private String content;
}
