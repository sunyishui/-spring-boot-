package com.survey.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.survey.entity.Answer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {

    @Select("SELECT qo.content, COUNT(*) as count FROM answer a " +
            "JOIN question_option qo ON FIND_IN_SET(qo.id, a.option_ids) " +
            "WHERE a.question_id = #{questionId} " +
            "GROUP BY qo.id, qo.content ORDER BY count DESC")
    List<Map<String, Object>> countByOption(Long questionId);

    @Select("SELECT a.content FROM answer a WHERE a.question_id = #{questionId} AND a.content IS NOT NULL AND a.content != ''")
    List<String> getTextAnswers(Long questionId);
}
