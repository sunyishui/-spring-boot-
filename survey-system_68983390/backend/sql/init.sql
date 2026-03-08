-- ============================================
-- 电子问卷管理系统 数据库初始化脚本
-- ============================================

-- 设置连接字符集，防止中文乱码/插入失败
SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;
SET CHARACTER_SET_CONNECTION = utf8mb4;

DROP DATABASE IF EXISTS survey_system;
CREATE DATABASE survey_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE survey_system;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS `answer`;
DROP TABLE IF EXISTS `answer_sheet`;
DROP TABLE IF EXISTS `question_option`;
DROP TABLE IF EXISTS `question`;
DROP TABLE IF EXISTS `survey`;
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(200) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN-管理员, USER-普通用户',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- 2. 问卷表
-- ----------------------------
CREATE TABLE `survey` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '问卷ID',
    `title` VARCHAR(200) NOT NULL COMMENT '问卷标题',
    `description` TEXT DEFAULT NULL COMMENT '问卷说明',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布, 2-已结束',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `create_by` BIGINT NOT NULL COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_create_by` (`create_by`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷表';

-- ----------------------------
-- 3. 题目表
-- ----------------------------
CREATE TABLE `question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    `survey_id` BIGINT NOT NULL COMMENT '所属问卷ID',
    `type` VARCHAR(20) NOT NULL COMMENT '题目类型: RADIO-单选, CHECKBOX-多选, INPUT-填空',
    `title` VARCHAR(500) NOT NULL COMMENT '题目标题',
    `required` TINYINT NOT NULL DEFAULT 1 COMMENT '是否必填: 0-否, 1-是',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_survey_id` (`survey_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

-- ----------------------------
-- 4. 题目选项表
-- ----------------------------
CREATE TABLE `question_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '选项ID',
    `question_id` BIGINT NOT NULL COMMENT '所属题目ID',
    `content` VARCHAR(500) NOT NULL COMMENT '选项内容',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    PRIMARY KEY (`id`),
    KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目选项表';

-- ----------------------------
-- 5. 答卷表
-- ----------------------------
CREATE TABLE `answer_sheet` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '答卷ID',
    `survey_id` BIGINT NOT NULL COMMENT '问卷ID',
    `user_id` BIGINT NOT NULL COMMENT '填写人ID',
    `submit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_survey_user` (`survey_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答卷表';

-- ----------------------------
-- 6. 答案表
-- ----------------------------
CREATE TABLE `answer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '答案ID',
    `answer_sheet_id` BIGINT NOT NULL COMMENT '答卷ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `option_ids` VARCHAR(500) DEFAULT NULL COMMENT '选择的选项ID，多选用逗号分隔',
    `content` TEXT DEFAULT NULL COMMENT '填空题内容',
    PRIMARY KEY (`id`),
    KEY `idx_answer_sheet_id` (`answer_sheet_id`),
    KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答案表';

-- ============================================
-- 初始数据
-- ============================================

-- ----------------------------
-- 用户数据 (admin密码: admin123, 其他用户密码: 123456)
-- ----------------------------
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `role`, `status`) VALUES
(1, 'admin', '$2a$10$YbmBZE4a4bmw7WunLMsBaewTtb.gU2EBF0hb7cjQaBdB3kXp0FsCu', '系统管理员', 'ADMIN', 1),
(2, 'zhangsan', '$2a$10$QagOHazpWz3I4ok/jO3XX.3c2.OrXm1fb6AEpHIFLBbNxnJH/cunC', '张三', 'USER', 1),
(3, 'lisi', '$2a$10$QagOHazpWz3I4ok/jO3XX.3c2.OrXm1fb6AEpHIFLBbNxnJH/cunC', '李四', 'USER', 1),
(4, 'wangwu', '$2a$10$QagOHazpWz3I4ok/jO3XX.3c2.OrXm1fb6AEpHIFLBbNxnJH/cunC', '王五', 'USER', 1),
(5, 'zhaoliu', '$2a$10$QagOHazpWz3I4ok/jO3XX.3c2.OrXm1fb6AEpHIFLBbNxnJH/cunC', '赵六', 'USER', 1),
(6, 'sunqi', '$2a$10$QagOHazpWz3I4ok/jO3XX.3c2.OrXm1fb6AEpHIFLBbNxnJH/cunC', '孙七', 'USER', 0);

-- ----------------------------
-- 问卷数据 (3份: 已发布、已结束、草稿各一份)
-- ----------------------------
INSERT INTO `survey` (`id`, `title`, `description`, `status`, `start_time`, `end_time`, `create_by`) VALUES
(1, '大学生网购消费习惯调查', '本问卷旨在了解大学生群体的网购消费行为、偏好及习惯，为相关研究提供数据支持。问卷匿名填写，感谢您的参与！', 1, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1),
(2, '员工工作满意度调研', '为了解公司员工的工作状态与满意度，持续改善工作环境，特开展本次匿名调研。请如实填写，感谢配合！', 2, '2024-06-01 00:00:00', '2024-12-31 23:59:59', 1),
(3, '校园食堂满意度调查', '为提升校园餐饮服务质量，诚邀您参与本次食堂满意度调查。您的反馈将帮助我们改进服务。', 0, NULL, NULL, 1);

-- ============================================
-- 问卷1: 大学生网购消费习惯调查 (已发布, 5道题)
-- ============================================

-- Q1: 单选题 - 网购频率
INSERT INTO `question` (`id`, `survey_id`, `type`, `title`, `required`, `sort_order`) VALUES
(1, 1, 'RADIO', '您平均每月网购的频次是？', 1, 0);
INSERT INTO `question_option` (`id`, `question_id`, `content`, `sort_order`) VALUES
(1, 1, '1-2次', 0),
(2, 1, '3-5次', 1),
(3, 1, '6-10次', 2),
(4, 1, '10次以上', 3);

-- Q2: 多选题 - 常购品类
INSERT INTO `question` (`id`, `survey_id`, `type`, `title`, `required`, `sort_order`) VALUES
(2, 1, 'CHECKBOX', '您经常网购的商品类别有哪些？（多选）', 1, 1);
INSERT INTO `question_option` (`id`, `question_id`, `content`, `sort_order`) VALUES
(5, 2, '服饰鞋帽', 0),
(6, 2, '数码电子', 1),
(7, 2, '食品饮料', 2),
(8, 2, '书籍文具', 3),
(9, 2, '美妆护肤', 4),
(10, 2, '日用百货', 5);

-- Q3: 单选题 - 月消费金额
INSERT INTO `question` (`id`, `survey_id`, `type`, `title`, `required`, `sort_order`) VALUES
(3, 1, 'RADIO', '您每月网购的平均消费金额是？', 1, 2);
INSERT INTO `question_option` (`id`, `question_id`, `content`, `sort_order`) VALUES
(11, 3, '200元以下', 0),
(12, 3, '200-500元', 1),
(13, 3, '500-1000元', 2),
(14, 3, '1000-2000元', 3),
(15, 3, '2000元以上', 4);

-- Q4: 单选题 - 影响因素
INSERT INTO `question` (`id`, `survey_id`, `type`, `title`, `required`, `sort_order`) VALUES
(4, 1, 'RADIO', '影响您网购决策的最主要因素是？', 1, 3);
INSERT INTO `question_option` (`id`, `question_id`, `content`, `sort_order`) VALUES
(16, 4, '价格优惠', 0),
(17, 4, '商品质量', 1),
(18, 4, '品牌信赖', 2),
(19, 4, '用户评价', 3),
(20, 4, '物流速度', 4);

-- Q5: 填空题 - 建议
INSERT INTO `question` (`id`, `survey_id`, `type`, `title`, `required`, `sort_order`) VALUES
(5, 1, 'INPUT', '您对目前网购体验有什么建议或期望？', 0, 4);

-- ============================================
-- 问卷2: 员工工作满意度调研 (已结束, 5道题)
-- ============================================

-- Q6: 单选题 - 总体满意度
INSERT INTO `question` (`id`, `survey_id`, `type`, `title`, `required`, `sort_order`) VALUES
(6, 2, 'RADIO', '您对当前工作的总体满意度如何？', 1, 0);
INSERT INTO `question_option` (`id`, `question_id`, `content`, `sort_order`) VALUES
(21, 6, '非常满意', 0),
(22, 6, '比较满意', 1),
(23, 6, '一般', 2),
(24, 6, '不太满意', 3),
(25, 6, '非常不满意', 4);

-- Q7: 多选题 - 满意方面
INSERT INTO `question` (`id`, `survey_id`, `type`, `title`, `required`, `sort_order`) VALUES
(7, 2, 'CHECKBOX', '您对以下哪些方面感到满意？（多选）', 1, 1);
INSERT INTO `question_option` (`id`, `question_id`, `content`, `sort_order`) VALUES
(26, 7, '薪酬福利', 0),
(27, 7, '工作环境', 1),
(28, 7, '团队氛围', 2),
(29, 7, '晋升机会', 3),
(30, 7, '培训发展', 4),
(31, 7, '管理制度', 5);

-- Q8: 单选题 - 工作压力
INSERT INTO `question` (`id`, `survey_id`, `type`, `title`, `required`, `sort_order`) VALUES
(8, 2, 'RADIO', '您认为目前的工作压力程度是？', 1, 2);
INSERT INTO `question_option` (`id`, `question_id`, `content`, `sort_order`) VALUES
(32, 8, '压力很小', 0),
(33, 8, '压力适中', 1),
(34, 8, '压力较大', 2),
(35, 8, '压力很大', 3);

-- Q9: 单选题 - 离职倾向
INSERT INTO `question` (`id`, `survey_id`, `type`, `title`, `required`, `sort_order`) VALUES
(9, 2, 'RADIO', '未来一年内您是否有换工作的打算？', 1, 3);
INSERT INTO `question_option` (`id`, `question_id`, `content`, `sort_order`) VALUES
(36, 9, '完全没有', 0),
(37, 9, '偶尔想过', 1),
(38, 9, '正在考虑', 2),
(39, 9, '已有计划', 3);

-- Q10: 填空题 - 改进建议
INSERT INTO `question` (`id`, `survey_id`, `type`, `title`, `required`, `sort_order`) VALUES
(10, 2, 'INPUT', '您对公司有哪些改进建议？', 0, 4);

-- ============================================
-- 问卷3: 校园食堂满意度调查 (草稿, 3道题)
-- ============================================

INSERT INTO `question` (`id`, `survey_id`, `type`, `title`, `required`, `sort_order`) VALUES
(11, 3, 'RADIO', '您最常去的食堂是？', 1, 0);
INSERT INTO `question_option` (`id`, `question_id`, `content`, `sort_order`) VALUES
(40, 11, '一食堂', 0),
(41, 11, '二食堂', 1),
(42, 11, '三食堂', 2);

INSERT INTO `question` (`id`, `survey_id`, `type`, `title`, `required`, `sort_order`) VALUES
(12, 3, 'CHECKBOX', '您对食堂哪些方面不满意？（多选）', 1, 1);
INSERT INTO `question_option` (`id`, `question_id`, `content`, `sort_order`) VALUES
(43, 12, '菜品种类少', 0),
(44, 12, '价格偏高', 1),
(45, 12, '卫生状况差', 2),
(46, 12, '就餐环境差', 3);

INSERT INTO `question` (`id`, `survey_id`, `type`, `title`, `required`, `sort_order`) VALUES
(13, 3, 'INPUT', '您希望食堂增加什么菜品？', 0, 2);

-- ============================================
-- 答卷数据: 问卷1 (4个用户填写)
-- ============================================

INSERT INTO `answer_sheet` (`id`, `survey_id`, `user_id`, `submit_time`) VALUES
(1, 1, 2, '2025-02-10 09:30:00'),
(2, 1, 3, '2025-02-11 14:20:00'),
(3, 1, 4, '2025-02-12 16:45:00'),
(4, 1, 5, '2025-02-13 10:10:00');

-- 张三的答卷 (sheet 1)
INSERT INTO `answer` (`answer_sheet_id`, `question_id`, `option_ids`, `content`) VALUES
(1, 1, '2', NULL),
(1, 2, '5,6,8', NULL),
(1, 3, '12', NULL),
(1, 4, '16', NULL),
(1, 5, NULL, '希望快递包装更环保，减少过度包装');

-- 李四的答卷 (sheet 2)
INSERT INTO `answer` (`answer_sheet_id`, `question_id`, `option_ids`, `content`) VALUES
(2, 1, '3', NULL),
(2, 2, '5,7,9,10', NULL),
(2, 3, '13', NULL),
(2, 4, '17', NULL),
(2, 5, NULL, '退换货流程太复杂，希望能简化');

-- 王五的答卷 (sheet 3)
INSERT INTO `answer` (`answer_sheet_id`, `question_id`, `option_ids`, `content`) VALUES
(3, 1, '1', NULL),
(3, 2, '6,8', NULL),
(3, 3, '11', NULL),
(3, 4, '19', NULL),
(3, 5, NULL, '商品实物与图片差距大，建议加强审核');

-- 赵六的答卷 (sheet 4)
INSERT INTO `answer` (`answer_sheet_id`, `question_id`, `option_ids`, `content`) VALUES
(4, 1, '4', NULL),
(4, 2, '5,6,7,9', NULL),
(4, 3, '14', NULL),
(4, 4, '20', NULL),
(4, 5, NULL, '双十一物流太慢了，希望提升配送速度');

-- ============================================
-- 答卷数据: 问卷2 (3个用户填写)
-- ============================================

INSERT INTO `answer_sheet` (`id`, `survey_id`, `user_id`, `submit_time`) VALUES
(5, 2, 2, '2024-07-15 11:00:00'),
(6, 2, 3, '2024-07-16 09:30:00'),
(7, 2, 4, '2024-07-17 15:20:00');

-- 张三的答卷 (sheet 5)
INSERT INTO `answer` (`answer_sheet_id`, `question_id`, `option_ids`, `content`) VALUES
(5, 6, '22', NULL),
(5, 7, '27,28', NULL),
(5, 8, '33', NULL),
(5, 9, '36', NULL),
(5, 10, NULL, '希望公司能提供更多远程办公的机会');

-- 李四的答卷 (sheet 6)
INSERT INTO `answer` (`answer_sheet_id`, `question_id`, `option_ids`, `content`) VALUES
(6, 6, '23', NULL),
(6, 7, '26,28,30', NULL),
(6, 8, '34', NULL),
(6, 9, '37', NULL),
(6, 10, NULL, '建议增加团建活动和员工关怀，加班太多了');

-- 王五的答卷 (sheet 7)
INSERT INTO `answer` (`answer_sheet_id`, `question_id`, `option_ids`, `content`) VALUES
(7, 6, '21', NULL),
(7, 7, '26,27,28,29', NULL),
(7, 8, '32', NULL),
(7, 9, '36', NULL),
(7, 10, NULL, '公司整体不错，希望能优化绩效考核制度');
