CREATE SCHEMA if not exists mambo_code_platform;
use mambo_code_platform;
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
    ) comment '用户' collate = utf8mb4_unicode_ci;
-- 拓展
ALTER TABLE user
ADD COLUMN vipExpireTime datetime NULL COMMENT '会员过期时间',
ADD COLUMN vipCode varchar(128) NULL COMMENT '会员兑换码',
ADD COLUMN vipNumber bigint NULL COMMENT '会员编号',
ADD COLUMN shareCode varchar(20) DEFAULT NULL COMMENT '分享码',
ADD COLUMN inviteUser bigint DEFAULT NULL COMMENT '邀请用户 id';