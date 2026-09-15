package com.sdjzuxg.collegemanagesystem.agent.mapper;

import com.sdjzuxg.collegemanagesystem.agent.entity.AiConversation;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AiConversationMapper {
    @Insert("INSERT INTO ai_conversation(user_id,user_type,title,status) VALUES(#{userId},#{userType},#{title},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "conversationId")
    int insert(AiConversation conversation);

    @Select("SELECT conversation_id,user_id,user_type,title,status,created_at,updated_at FROM ai_conversation WHERE conversation_id=#{id} AND user_id=#{userId} AND user_type=#{userType}")
    AiConversation selectOwned(@Param("id") Long id, @Param("userId") Integer userId, @Param("userType") String userType);

    @Select("SELECT conversation_id,user_id,user_type,title,status,created_at,updated_at FROM ai_conversation WHERE user_id=#{userId} AND user_type=#{userType} ORDER BY updated_at DESC")
    List<AiConversation> selectByUser(@Param("userId") Integer userId, @Param("userType") String userType);

    @Update("UPDATE ai_conversation SET updated_at = CURRENT_TIMESTAMP WHERE conversation_id=#{id}")
    int touch(@Param("id") Long id);

    @Delete("DELETE FROM ai_conversation WHERE conversation_id=#{id} AND user_id=#{userId} AND user_type=#{userType}")
    int deleteOwned(@Param("id") Long id, @Param("userId") Integer userId, @Param("userType") String userType);
}
