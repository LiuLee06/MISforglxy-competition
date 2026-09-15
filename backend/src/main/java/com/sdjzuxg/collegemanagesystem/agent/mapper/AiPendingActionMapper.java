package com.sdjzuxg.collegemanagesystem.agent.mapper;

import com.sdjzuxg.collegemanagesystem.agent.entity.AiPendingAction;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AiPendingActionMapper {
    @Insert("INSERT INTO ai_pending_action(conversation_id,user_id,user_type,tool_name,arguments_json,action_summary,status,expires_at) VALUES(#{conversationId},#{userId},#{userType},#{toolName},#{argumentsJson},#{actionSummary},'PENDING',#{expiresAt})")
    @Options(useGeneratedKeys = true, keyProperty = "actionId")
    int insert(AiPendingAction action);

    @Select("SELECT action_id,conversation_id,user_id,user_type,tool_name,arguments_json,action_summary,status,expires_at,created_at,confirmed_at,executed_at FROM ai_pending_action WHERE action_id=#{id} AND user_id=#{userId} AND user_type=#{userType}")
    AiPendingAction selectOwned(@Param("id") Long id, @Param("userId") Integer userId, @Param("userType") String userType);

    @Update("UPDATE ai_pending_action SET status='EXECUTING',confirmed_at=NOW() WHERE action_id=#{id} AND user_id=#{userId} AND user_type=#{userType} AND status='PENDING' AND expires_at > NOW()")
    int claim(@Param("id") Long id, @Param("userId") Integer userId, @Param("userType") String userType);

    @Update("UPDATE ai_pending_action SET status='CANCELLED' WHERE action_id=#{id} AND user_id=#{userId} AND user_type=#{userType} AND status='PENDING'")
    int cancel(@Param("id") Long id, @Param("userId") Integer userId, @Param("userType") String userType);

    @Update("UPDATE ai_pending_action SET status='EXECUTED',executed_at=NOW() WHERE action_id=#{id} AND status='EXECUTING'")
    int markExecuted(@Param("id") Long id);

    @Update("UPDATE ai_pending_action SET status='FAILED' WHERE action_id=#{id} AND status='EXECUTING'")
    int markFailed(@Param("id") Long id);

    @Delete("DELETE FROM ai_pending_action WHERE conversation_id=#{conversationId} AND status IN ('PENDING','EXECUTING')")
    int deleteUnfinishedByConversationId(@Param("conversationId") Long conversationId);
}
