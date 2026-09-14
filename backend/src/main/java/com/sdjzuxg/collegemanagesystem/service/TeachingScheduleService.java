package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.mapper.TeachingScheduleMapper;
import com.sdjzuxg.collegemanagesystem.dto.TeachingScheduleUpdateDTO;
import com.sdjzuxg.collegemanagesystem.entity.TeachingSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TeachingScheduleService {

    @Autowired
    private TeachingScheduleMapper teachingScheduleMapper;

    public List<TeachingSchedule> findByConditions(String teacherName, String courseName, String major,
                                                   Integer semesterId, Integer confirmStatus) {
        return teachingScheduleMapper.findByConditions(teacherName, courseName, major, semesterId, confirmStatus);
    }

    /*
    public List<TeachingScheduleUpdateDTO> findEditableFieldsByTeacher(String teacherName) {
        return teachingScheduleMapper.findEditableFieldsByTeacher(teacherName);
    }
    */

    @Transactional
    public boolean updateEditableFields(TeachingScheduleUpdateDTO updateDTO) {
        int rows = teachingScheduleMapper.updateEditableFields(updateDTO);
        return rows > 0;
    }

    @Transactional
    public boolean confirmByNoticeId(String noticeId) {
        int rows = teachingScheduleMapper.confirmByNoticeId(noticeId);
        return rows > 0;
    }

    /*
    public List<TeachingSchedule> findAll() {
        return teachingScheduleMapper.findAll();
    }
    */

    public TeachingSchedule findByNoticeId(String noticeId) {
        return teachingScheduleMapper.findByNoticeId(noticeId);
    }

    @Transactional
    public boolean save(TeachingSchedule teachingSchedule) {
        int rows = teachingScheduleMapper.insert(teachingSchedule);
        return rows > 0;
    }

    @Transactional
    public boolean update(TeachingSchedule teachingSchedule) {
        int rows = teachingScheduleMapper.update(teachingSchedule);
        return rows > 0;
    }

    @Transactional
    public boolean batchUpsert(List<TeachingSchedule> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (TeachingSchedule teachingSchedule : list) {
            teachingSchedule.setConfirmStatus(0);
        }
        teachingScheduleMapper.batchUpsert(list);
        return true;
    }

    @Transactional
    public boolean deleteByNoticeId(String noticeId) {
        int rows = teachingScheduleMapper.deleteByNoticeId(noticeId);
        return rows > 0;
    }

    @Transactional
    public int deleteBySemesterId(Integer semesterId) {
        if (semesterId == null || semesterId <= 0) {
            throw new IllegalArgumentException("学期参数无效");
        }
        return teachingScheduleMapper.deleteBySemesterId(semesterId);
    }
}
