package com.sdjzuxg.collegemanagesystem.agent.service.impl;

import com.sdjzuxg.collegemanagesystem.agent.entity.*;
import com.sdjzuxg.collegemanagesystem.agent.mapper.*;
import com.sdjzuxg.collegemanagesystem.agent.service.AgentConversationService;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AgentConversationServiceImpl implements AgentConversationService {
    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final AiPendingActionMapper pendingActionMapper;
    public AgentConversationServiceImpl(AiConversationMapper conversationMapper, AiMessageMapper messageMapper,
            AiPendingActionMapper pendingActionMapper, AiActionLogMapper actionLogMapper) {
        this.conversationMapper=conversationMapper; this.messageMapper=messageMapper;
        this.pendingActionMapper=pendingActionMapper;
    }
    public AiConversation getOrCreate(Long id, LoginUser user, String firstMessage) {
        if(id!=null) {
            AiConversation found=conversationMapper.selectOwned(id,user.getUserId(),user.getUserType());
            if(found==null) throw new SecurityException("会话不存在或无权访问");
            return found;
        }
        AiConversation c=new AiConversation(); c.setUserId(user.getUserId()); c.setUserType(user.getUserType());
        c.setTitle(firstMessage==null?"学院行政助手":firstMessage.substring(0,Math.min(80,firstMessage.length())));
        c.setStatus("ACTIVE"); conversationMapper.insert(c); return c;
    }
    public void saveMessage(AiMessage m){if(m == null)return; messageMapper.insert(m); if(m.getConversationId() != null) conversationMapper.touch(m.getConversationId());}
    public List<AiMessage> recentMessages(Long id,int limit){List<AiMessage> l=messageMapper.selectRecent(id,limit); Collections.reverse(l); return l;}
    public List<AiConversation> list(LoginUser user){return conversationMapper.selectByUser(user.getUserId(),user.getUserType());}
    public List<AiMessage> listMessages(Long id,LoginUser user){
        if(conversationMapper.selectOwned(id,user.getUserId(),user.getUserType())==null)throw new SecurityException("会话不存在或无权访问");
        return recentMessages(id,100);
    }
    @Transactional
    public boolean delete(Long id, LoginUser user) {
        if (id == null || conversationMapper.selectOwned(id, user.getUserId(), user.getUserType()) == null) {
            return false;
        }
        pendingActionMapper.deleteUnfinishedByConversationId(id);
        messageMapper.deleteByConversationId(id);
        return conversationMapper.deleteOwned(id, user.getUserId(), user.getUserType()) > 0;
    }
}
