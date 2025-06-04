-- 该 SQL 文件用于创建和初始化学生管理系统数据库 StuManager
-- 包含了管理员、学生、教师、课程、考勤、成绩等相关表的创建和数据插入操作
-- 表创建顺序已调整为符合外键依赖关系

-- 设置字符集为 UTF-8
SET NAMES utf8;

-- 创建数据库 StuManager，如果不存在则创建，默认字符集为 UTF-8
CREATE DATABASE IF NOT EXISTS StuManager DEFAULT CHARACTER SET utf8;

-- 使用 StuManager 数据库
USE StuManager;


### 一、无依赖基础表（无外键引用） ###
-- 创建班级表 s_clazz（无依赖，其他表依赖它）
DROP TABLE IF EXISTS s_clazz;
CREATE TABLE s_clazz (
                         id    INT(5)      NOT NULL AUTO_INCREMENT,
                         name  VARCHAR(32) NOT NULL,
                         info  VARCHAR(128) DEFAULT NULL,
                         PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- 插入班级表初始数据
INSERT INTO s_clazz (id, name, info)
VALUES
    (1, '软件一班', '软件工程专业。'),
    (4, '数学一班', '大学数学专业'),
    (5, '计算机科学与技术一班', '计算机专业');


### 二、依赖班级表的基础表 ###
-- 创建教师表 s_teacher（依赖 s_clazz.clazz_id）
DROP TABLE IF EXISTS s_teacher;
CREATE TABLE s_teacher (
                           id         INT(5)      NOT NULL AUTO_INCREMENT,
                           sn         VARCHAR(32) NOT NULL,
                           username   VARCHAR(32) NOT NULL,
                           password   VARCHAR(32) NOT NULL,
                           clazz_id   INT(5)      NOT NULL,  -- 外键关联 s_clazz.id
                           sex        VARCHAR(5)  NOT NULL DEFAULT '男',
                           mobile     VARCHAR(12) DEFAULT NULL,
                           qq         VARCHAR(18) DEFAULT NULL,
                           photo      VARCHAR(255) DEFAULT NULL,
                           PRIMARY KEY (id),
                           UNIQUE KEY uk_teacher_sn (sn),
                           KEY teacher_clazz_idx (clazz_id),
                           CONSTRAINT teacher_clazz_fk FOREIGN KEY (clazz_id) REFERENCES s_clazz (id)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;

-- 插入教师表初始数据
INSERT INTO s_teacher (id, sn, username, password, clazz_id, sex, mobile, qq, photo)
VALUES
    (9, 'T11528608730648', '张三', '111', 4, '男', '13918655656', '1193284480', NULL),
    (10, 'T11528609224588', '李四老师', '111', 4, '男', '13656565656', '123456', NULL),
    (11, 'T51528617262403', '李老师', '123456', 5, '男', '18989898989', '1456655565', NULL),
    (18, 'T11561727746515', '夏青松', '123456', 1, '女', '15174857845', '1745854125', '5d447b8b-ec54-4a8e-919a-453aa7b6d33b.jpg');

-- 创建学生表 s_student（依赖 s_clazz.clazz_id）
DROP TABLE IF EXISTS s_student;
CREATE TABLE s_student (
                           id         INT(5)      NOT NULL AUTO_INCREMENT,
                           sn         VARCHAR(32) NOT NULL,
                           username   VARCHAR(32) NOT NULL,
                           password   VARCHAR(32) NOT NULL,
                           clazz_id   INT(5)      NOT NULL,  -- 外键关联 s_clazz.id
                           sex        VARCHAR(5)  NOT NULL DEFAULT '男',
                           mobile     VARCHAR(12) DEFAULT NULL,
                           qq         VARCHAR(18) DEFAULT NULL,
                           photo      VARCHAR(255) DEFAULT NULL,
                           PRIMARY KEY (id),
                           UNIQUE KEY uk_student_sn (sn),
                           KEY student_clazz_idx (clazz_id),
                           CONSTRAINT student_clazz_fk FOREIGN KEY (clazz_id) REFERENCES s_clazz (id)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

-- 插入学生表初始数据
INSERT INTO s_student (id, sn, username, password, clazz_id, sex, mobile, qq, photo)
VALUES
    (2, 'S51528202992845', '张三纷', '123456', 1, '女', '13545454548', '1332365656', NULL),
    (4, 'S51528379586807', '王麻子', '111111', 5, '男', '13356565656', '123456', NULL),
    (9, 'S41528633634989', '马冬梅', '1', 5, '男', '13333332133', '131313132323', 'bb12326f-ef6c-4d3d-a2ae-f9eb30a15ad4.jpg');


### 三、依赖教师/学生表的课程表 ###
-- 创建课程表 s_course（依赖 s_teacher.teacher_id）
DROP TABLE IF EXISTS s_course;
CREATE TABLE s_course (
                          id            INT(5)      NOT NULL AUTO_INCREMENT,
                          name          VARCHAR(32) NOT NULL,
                          teacher_id    INT(5)      NOT NULL,  -- 外键关联 s_teacher.id
                          course_date   VARCHAR(32) DEFAULT NULL,
                          selected_num  INT(5)      NOT NULL DEFAULT 0,
                          max_num       INT(5)      NOT NULL DEFAULT 50,
                          info          VARCHAR(128) DEFAULT NULL,
                          PRIMARY KEY (id),
                          KEY course_teacher_idx (teacher_id),
                          CONSTRAINT course_teacher_fk FOREIGN KEY (teacher_id) REFERENCES s_teacher (id)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- 插入课程表初始数据
INSERT INTO s_course (id, name, teacher_id, course_date, selected_num, max_num, info)
VALUES
    (1, '大学英语', 9, '周三上午8点', 49, 50, '英语。'),
    (2, '大学数学', 10, '周三上午10点', 4, 50, '数学。'),
    (3, '计算机基础', 11, '周三上午', 3, 50, '计算机课程。');


### 四、依赖课程/学生表的关联表 ###
-- 创建管理员表 s_admin（无外键依赖，可放在此处或开头）
DROP TABLE IF EXISTS s_admin;
CREATE TABLE s_admin (
                         id          INT(5)      NOT NULL AUTO_INCREMENT,
                         username    VARCHAR(32) NOT NULL,
                         password    VARCHAR(32) NOT NULL,
                         status      TINYINT(1)  NOT NULL DEFAULT 1,
                         PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- 插入管理员表初始数据
INSERT INTO s_admin (id, username, password, status)
VALUES (1, 'admin', '123456', 1);

-- 创建考勤表 s_attendance（依赖 s_course.id 和 s_student.id）
DROP TABLE IF EXISTS s_attendance;
CREATE TABLE s_attendance (
                              id           INT(5)      NOT NULL AUTO_INCREMENT,
                              course_id    INT(5)      NOT NULL,  -- 外键关联 s_course.id
                              student_id   INT(5)      NOT NULL,  -- 外键关联 s_student.id
                              type         VARCHAR(11) NOT NULL,
                              date         VARCHAR(11) NOT NULL,
                              PRIMARY KEY (id),
                              KEY attendance_course_idx (course_id),
                              KEY attendance_student_idx (student_id),
                              CONSTRAINT attendance_course_fk FOREIGN KEY (course_id) REFERENCES s_course (id),
                              CONSTRAINT attendance_student_fk FOREIGN KEY (student_id) REFERENCES s_student (id)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;

-- 插入考勤表初始数据
INSERT INTO s_attendance (id, course_id, student_id, type, date)
VALUES
    (13, 1, 2, '上午', '2018-09-04'),
    (14, 1, 4, '上午', '2018-09-04'),
    (15, 2, 2, '上午', '2019-07-02');

-- 创建请假表 s_leave（依赖 s_student.id）
DROP TABLE IF EXISTS s_leave;
CREATE TABLE s_leave (
                         id          INT(5)      NOT NULL AUTO_INCREMENT,
                         student_id  INT(5)      NOT NULL,  -- 外键关联 s_student.id
                         info        VARCHAR(512) DEFAULT NULL,
                         status      TINYINT(1)  NOT NULL DEFAULT 0,
                         remark      VARCHAR(512) DEFAULT NULL,
                         PRIMARY KEY (id),
                         KEY leave_student_idx (student_id),
                         CONSTRAINT leave_student_fk FOREIGN KEY (student_id) REFERENCES s_student (id)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

-- 插入请假表初始数据
INSERT INTO s_leave (id, student_id, info, status, remark)
VALUES (13, 2, '世界这么大，想去看看', 1, '同意，你很6');

-- 创建成绩表 s_score（依赖 s_course.id 和 s_student.id）
DROP TABLE IF EXISTS s_score;
CREATE TABLE s_score (
                         id          INT(5)      NOT NULL AUTO_INCREMENT,
                         student_id  INT(5)      NOT NULL,  -- 外键关联 s_student.id
                         course_id   INT(5)      NOT NULL,  -- 外键关联 s_course.id
                         score       DOUBLE(5,2) NOT NULL,
                         remark      VARCHAR(128) DEFAULT NULL,
                         PRIMARY KEY (id),
                         KEY score_student_idx (student_id),
                         KEY score_course_idx (course_id),
                         CONSTRAINT score_course_fk FOREIGN KEY (course_id) REFERENCES s_course (id),
                         CONSTRAINT score_student_fk FOREIGN KEY (student_id) REFERENCES s_student (id)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8;

-- 插入成绩表初始数据
INSERT INTO s_score (id, student_id, course_id, score, remark)
VALUES
    (67, 4, 2, 78.00, '中等'),
    (68, 9, 1, 50.00, '不及格');

-- 创建选课表 s_selected_course（依赖 s_course.id 和 s_student.id）
DROP TABLE IF EXISTS s_selected_course;
CREATE TABLE s_selected_course (
                                   id           INT(5) NOT NULL AUTO_INCREMENT,
                                   student_id   INT(5) NOT NULL,  -- 外键关联 s_student.id
                                   course_id    INT(5) NOT NULL,  -- 外键关联 s_course.id
                                   PRIMARY KEY (id),
                                   KEY selected_course_student_idx (student_id),
                                   KEY selected_course_course_idx (course_id),
                                   CONSTRAINT selected_course_course_fk FOREIGN KEY (course_id) REFERENCES s_course (id),
                                   CONSTRAINT selected_course_student_fk FOREIGN KEY (student_id) REFERENCES s_student (id)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;

-- 插入选课表初始数据
INSERT INTO s_selected_course (id, student_id, course_id)
VALUES
    (18, 2, 1),
    (19, 2, 2),
    (20, 2, 3),
    (21, 4, 3),
    (22, 4, 2),
    (24, 9, 1);
