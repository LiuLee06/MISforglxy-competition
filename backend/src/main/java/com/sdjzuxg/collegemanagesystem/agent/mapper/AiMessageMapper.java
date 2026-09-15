package com.sdjzuxg.collegemanagesystem.agent.mapper;

import com.sdjzuxg.collegemanagesystem.agent.entity.AiMessage;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AiMessageMapper {
    @Insert("INSERT INTO ai_message(conversation_id,role,content,tool_name,tool_call_id,tool_arguments,tool_result) VALUES(#{conversationId},#{role},#{content},#{toolName},#{toolCallId},#{toolArguments},#{toolResult})")
    @Options(useGeneratedKeys = true, keyProperty = "messageId")
    int insert(AiMessage message);

    @Select("SELECT message_id,conversation_id,role,content,tool_name,tool_call_id,tool_arguments,tool_result,created_at FROM ai_message WHERE conversation_id=#{conversationId} ORDER BY message_id DESC LIMIT #{limit}")
    List<AiMessage> selectRecent(@Param("conversationId") Long conversationId, @Param("limit") int limit);

    @Delete("DELETE FROM ai_message WHERE conversation_id=#{conversationId}")
    int deleteByConversationId(@Param("conversationId") Long conversationId);
}
