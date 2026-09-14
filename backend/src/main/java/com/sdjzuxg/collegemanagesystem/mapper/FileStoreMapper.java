package com.sdjzuxg.collegemanagesystem.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 附件文件存储 Mapper:文件二进制内容存入共享数据库 FILE_STORE 表,
 * 使本地部署与服务器部署的附件通过共用数据库保持一致
 */
@Mapper
public interface FileStoreMapper {

    /** 新增文件内容(file_name 为主键,重复插入会抛异常) */
    int insert(@Param("fileName") String fileName, @Param("content") byte[] content);

    /**
     * 按存储文件名读取文件行,不存在返回 null。
     * 注意:不能声明为返回 byte[]——MyBatis 会把数组返回类型当作多行查询转数组,导致类型不匹配;
     * 因此返回单行 Map,由调用方取出 content 字段(LONGBLOB 对应 byte[])
     */
    Map<String, Object> selectContentByName(@Param("fileName") String fileName);
}
