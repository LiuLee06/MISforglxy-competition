package com.sdjzuxg.collegemanagesystem.agent.mapper;

import com.sdjzuxg.collegemanagesystem.agent.entity.AiActionLog;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AiActionLogMapper {
    @Insert("INSERT INTO ai_action_log(conversation_id,user_id,user_type,tool_name,risk_level,arguments_json,result_json,status,duration_ms) VALUES(#{conversationId},#{userId},#{userType},#{toolName},#{riskLevel},#{argumentsJson},#{resultJson},#{status},#{durationMs})")
    @Options(useGeneratedKeys = true, keyProperty = "logId")
    int insert(AiActionLog log);

    @Delete("DELETE FROM ai_action_log WHERE conversation_id=#{conversationId}")
    int deleteByConversationId(@Param("conversationId") Long conversationId);
}
