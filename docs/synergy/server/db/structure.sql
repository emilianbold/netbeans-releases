create database synergy;
use synergy;


SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;


CREATE TABLE IF NOT EXISTS `assignment_progress` (
  `data` mediumblob NOT NULL,
  `test_assignement_id` int(11) NOT NULL,
  KEY `fk_assignment_progress_test_assignement1_idx` (`test_assignement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS `bug` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bug_id` int(11) NOT NULL,
  `case_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_bug_case1_idx` (`case_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=4 ;

CREATE TABLE IF NOT EXISTS `case` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `duration` int(11) NOT NULL DEFAULT '0',
  `title` varchar(512) COLLATE utf8_bin NOT NULL,
  `steps` longtext COLLATE utf8_bin NOT NULL,
  `result` longtext COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=15 ;

CREATE TABLE IF NOT EXISTS `case_has_keyword` (
  `case_id` int(11) NOT NULL,
  `keyword_id` int(11) NOT NULL,
  PRIMARY KEY (`case_id`,`keyword_id`),
  KEY `fk_case_has_keyword_keyword1_idx` (`keyword_id`),
  KEY `fk_case_has_keyword_case_idx` (`case_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS `case_image` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(256) COLLATE utf8_bin NOT NULL,
  `path` varchar(1024) COLLATE utf8_bin NOT NULL,
  `case_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_case_image_case1_idx` (`case_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=6 ;

CREATE TABLE IF NOT EXISTS `keyword` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `keyword` varchar(128) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `keyword_UNIQUE` (`keyword`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=6 ;

CREATE TABLE IF NOT EXISTS `platform` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(256) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=3 ;

CREATE TABLE IF NOT EXISTS `run_attachement` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `path` varchar(1024) COLLATE utf8_bin NOT NULL,
  `test_run_id` int(11) NOT NULL,
  PRIMARY KEY (`id`,`test_run_id`),
  KEY `fk_run_attachement_test_run1_idx` (`test_run_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `specification` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(512) COLLATE utf8_bin NOT NULL,
  `description` longtext COLLATE utf8_bin NOT NULL,
  `author_id` int(11) NOT NULL,
  `version_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_specification_user1_idx` (`author_id`),
  KEY `fk_specification_version1_idx` (`version_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=4 ;

CREATE TABLE IF NOT EXISTS `specification_attachement` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `path` varchar(1024) COLLATE utf8_bin NOT NULL,
  `specification_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_attachement_specification1_idx` (`specification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `suite` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(512) COLLATE utf8_bin NOT NULL,
  `description` longtext COLLATE utf8_bin NOT NULL,
  `product` varchar(512) COLLATE utf8_bin NOT NULL,
  `component` varchar(512) COLLATE utf8_bin NOT NULL,
  `specification_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_suite_specification1_idx` (`specification_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=8 ;

CREATE TABLE IF NOT EXISTS `suite_has_case` (
  `suite_id` int(11) NOT NULL,
  `case_id` int(11) NOT NULL,
  PRIMARY KEY (`suite_id`,`case_id`),
  KEY `fk_suite_has_case_case1_idx` (`case_id`),
  KEY `fk_suite_has_case_suite1_idx` (`suite_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS `test_assignement` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `platform_id` int(11) NOT NULL,
  `specification_id` int(11) NOT NULL,
  `state` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `test_run_id` int(11) NOT NULL,
  `number_of_cases` int(11) NOT NULL DEFAULT '0',
  `number_of_completed_cases` int(11) NOT NULL DEFAULT '0',
  `label` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '0',
  `keyword_id` int(11) DEFAULT NULL,
  `passed_cases` int(11) DEFAULT '0',
  `skipped_cases` int(11) DEFAULT '0',
  `failed_cases` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_test_assignement_user1_idx` (`user_id`),
  KEY `fk_test_assignement_platform1_idx` (`platform_id`),
  KEY `fk_test_assignement_specification1_idx` (`specification_id`),
  KEY `fk_test_assignement_test_run1_idx` (`test_run_id`),
  KEY `fk_test_assignement_keyword1_idx` (`keyword_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=10 ;

CREATE TABLE IF NOT EXISTS `test_run` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(512) COLLATE utf8_bin NOT NULL,
  `description` longtext COLLATE utf8_bin,
  `start` datetime NOT NULL,
  `end` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=2 ;

CREATE TABLE IF NOT EXISTS `tribe` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(512) COLLATE utf8_bin NOT NULL,
  `description` text COLLATE utf8_bin,
  `leader_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_tribe_user1_idx` (`leader_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=3 ;

CREATE TABLE IF NOT EXISTS `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(256) COLLATE utf8_bin NOT NULL,
  `first_name` varchar(256) COLLATE utf8_bin NOT NULL,
  `last_name` varchar(256) COLLATE utf8_bin NOT NULL,
  `role` varchar(45) COLLATE utf8_bin DEFAULT 'viewer',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=6 ;

CREATE TABLE IF NOT EXISTS `user_has_favorite` (
  `user_id` int(11) NOT NULL,
  `specification_id` int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`specification_id`),
  KEY `fk_user_has_specification_specification1_idx` (`specification_id`),
  KEY `fk_user_has_specification_user1_idx` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS `user_is_member_of` (
  `user_id` int(11) NOT NULL,
  `tribe_id` int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`tribe_id`),
  KEY `fk_user_has_tribe_tribe1_idx` (`tribe_id`),
  KEY `fk_user_has_tribe_user1_idx` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS `version` (
  `version` varchar(52) COLLATE utf8_bin NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `version_UNIQUE` (`version`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=2 ;


ALTER TABLE `assignment_progress`
  ADD CONSTRAINT `fk_assignment_progress_test_assignement1` FOREIGN KEY (`test_assignement_id`) REFERENCES `test_assignement` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `bug`
  ADD CONSTRAINT `fk_bug_case1` FOREIGN KEY (`case_id`) REFERENCES `case` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `case_has_keyword`
  ADD CONSTRAINT `fk_case_has_keyword_case` FOREIGN KEY (`case_id`) REFERENCES `case` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_case_has_keyword_keyword1` FOREIGN KEY (`keyword_id`) REFERENCES `keyword` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `case_image`
  ADD CONSTRAINT `fk_case_image_case1` FOREIGN KEY (`case_id`) REFERENCES `case` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `run_attachement`
  ADD CONSTRAINT `fk_run_attachement_test_run1` FOREIGN KEY (`test_run_id`) REFERENCES `test_run` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `specification`
  ADD CONSTRAINT `fk_specification_user1` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_specification_version1` FOREIGN KEY (`version_id`) REFERENCES `version` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `specification_attachement`
  ADD CONSTRAINT `fk_attachement_specification1` FOREIGN KEY (`specification_id`) REFERENCES `specification` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `suite`
  ADD CONSTRAINT `fk_suite_specification1` FOREIGN KEY (`specification_id`) REFERENCES `specification` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `suite_has_case`
  ADD CONSTRAINT `fk_suite_has_case_case1` FOREIGN KEY (`case_id`) REFERENCES `case` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_suite_has_case_suite1` FOREIGN KEY (`suite_id`) REFERENCES `suite` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `test_assignement`
  ADD CONSTRAINT `fk_test_assignement_keyword1` FOREIGN KEY (`keyword_id`) REFERENCES `keyword` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_test_assignement_platform1` FOREIGN KEY (`platform_id`) REFERENCES `platform` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_test_assignement_specification1` FOREIGN KEY (`specification_id`) REFERENCES `specification` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_test_assignement_test_run1` FOREIGN KEY (`test_run_id`) REFERENCES `test_run` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_test_assignement_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `tribe`
  ADD CONSTRAINT `fk_tribe_user1` FOREIGN KEY (`leader_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `user_has_favorite`
  ADD CONSTRAINT `fk_user_has_specification_specification1` FOREIGN KEY (`specification_id`) REFERENCES `specification` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_user_has_specification_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `user_is_member_of`
  ADD CONSTRAINT `fk_user_has_tribe_tribe1` FOREIGN KEY (`tribe_id`) REFERENCES `tribe` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_user_has_tribe_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

CREATE TABLE IF NOT EXISTS `settings` (
  `key` varchar(255) COLLATE utf8_bin NOT NULL,
  `value` varchar(255) COLLATE utf8_bin NOT NULL,
  `label` text COLLATE utf8_bin,
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS `jobs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `specification_id` int(11) NOT NULL,
  `job_url` varchar(512) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

INSERT INTO  `user` (
`id` ,
`username` ,
`first_name` ,
`last_name` ,
`role`
)
VALUES (
NULL ,  'import',  'import',  'import',  'admin'
);

INSERT INTO  `settings` (
`key` ,
`value` ,
`label`
)
VALUES (
'anonym',  'import',  'anonymous username for import'
);

INSERT INTO `settings` (`key`, `value`, `label`) VALUES
('ATTACHMENT_PATH', '/var/www/att/', 'Absolute path where attachments are being saved to. Must end with /'),
('DOMAIN', 'localhost.com', 'Domain for sending emails'),
('IMAGE_BASE', 'http://localhost/media/', 'URL equivalent to IMAGE_PATH. Must end with /'),
('IMAGE_PATH', '/var/www/media/', 'Absolute path where images are being saved to. Must end with /'),
('LABEL_PAGE', '25', 'Number of cases per page to be shown on search by label page'),
('RUNS_PAGE', '25', 'Number of tests runs per page to be shown'),
('USERS_PAGE', '50', 'Number of users to be shown per page'),
('SEND_EMAIL', '1', '1 if Synergy should send emails, 0 if not'),
('SESSION_TIMEOUT', '2592000', '$_SESSION timeout'),
('SYNERGY_URL', 'http://localhost/synergy', 'base URL of synergy, must ends with /'),
('SALT', 'thisIsSynergy', 'random string to solt password before storing it in DB');

ALTER TABLE  `specification` ADD  `owner_id` INT NOT NULL;
ALTER TABLE  `specification` ADD  `last_updated` DATETIME NULL;
ALTER TABLE  `test_assignement` ADD  `started` DATETIME NULL ,ADD  `last_updated` DATETIME NULL;

CREATE TABLE IF NOT EXISTS `specification_revisions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` text COLLATE utf8_bin NOT NULL,
  `specification_id` int(11) NOT NULL,
  `date` datetime NOT NULL,
  `author` varchar(255) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=10 ;

ALTER TABLE  `case` ADD  `order` INT NULL DEFAULT  '1';
ALTER TABLE  `suite` ADD  `order` INT NULL DEFAULT  '1';
ALTER TABLE  `specification` ADD  `simpleName` VARCHAR( 255 ) NULL;
ALTER TABLE  `version` ADD  `isObsolete` INT NULL DEFAULT '0';
ALTER TABLE  `case` ADD  `duration_count` INT NOT NULL DEFAULT  '1';

CREATE TABLE IF NOT EXISTS `session` (
  `cookie` varchar(255) COLLATE utf8_bin NOT NULL,
  `username` varchar(255) COLLATE utf8_bin NOT NULL,
  `timestamp` int(11) NOT NULL,
  PRIMARY KEY (`cookie`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS `tribe_has_specification` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tribe_id` int(11) NOT NULL,
  `specification_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=18 ;

ALTER TABLE  `user` ADD  `email_notifications` INT NOT NULL DEFAULT  '1';

ALTER TABLE  `test_assignement` ADD  `issues` VARCHAR( 2048 ) NULL ,ADD  `time_taken` INT NULL DEFAULT  '0' COMMENT  'in minutes';
ALTER TABLE  `test_run` ADD  `is_active` INT NOT NULL DEFAULT  '1';

ALTER TABLE  `platform` ADD  `is_active` INT NOT NULL DEFAULT  '1';
ALTER TABLE  `specification` ADD  `is_active` INT NOT NULL DEFAULT  '1';

CREATE TABLE IF NOT EXISTS `assignment_comments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `assignment_id` int(11) NOT NULL,
  `case_id` int(11) NOT NULL,
  `suite_id` int(11) NOT NULL,
  `resolution` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT 'new',
  `comment_type_id` int(11) NOT NULL,
  `resolver_id` int(11) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `comment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(1024) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=1 ;

INSERT INTO  `synergy`.`comment` (
`id` ,
`name`
)
VALUES (
NULL ,  'Unclear instructions / Don''t understand'
), (
NULL ,  'Steps are missing or incorrect for my Operating System'
), (
NULL ,  'Steps are missing or incorrect in general'
), (
NULL ,  'Depends on previous case(s) which failed'
), (
NULL ,  'Missing sample file(s) required in test case'
), (
NULL ,  'Test case is obsolete'
), (
NULL ,  'No time to finish test case'
), (
NULL ,  'Duplicate test case'
),(
NULL ,  'Minor changes needed'
),(
NULL ,  'Dependency not met'
);
ALTER TABLE  `test_assignement` ADD  `created_by` INT NOT NULL DEFAULT  '1' COMMENT  '1 for admin/manager; 2 for tester, 3 for tribe leader';


CREATE TABLE IF NOT EXISTS `removal_request` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `specification_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=2 ;


CREATE TABLE IF NOT EXISTS `specification_lock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `specification_id` int(11) NOT NULL,
  `timestamp` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=13 ;
ALTER TABLE  `specification_lock` ADD  `test_assignment_id` INT NOT NULL;
ALTER TABLE  `test_run` ADD  `notifications_deadline` INT NOT NULL DEFAULT  '-1' COMMENT  'number of days before test run end when notification should be sent, < 0 means never';

CREATE TABLE IF NOT EXISTS `user_image` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `image_path` varchar(2048) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=17 ;

ALTER TABLE  `assignment_comments` ADD  `comment_free_text` VARCHAR( 512 ) NULL;
ALTER TABLE  `user` ADD  `passwd` VARCHAR( 256 ) NULL COMMENT  'hashed password based on SALT';

CREATE TABLE IF NOT EXISTS `review_assignment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `test_run_id` int(11) NOT NULL,
  `review_url` varchar(2048) COLLATE utf8_bin NOT NULL,
  `created_by` int(11) NOT NULL,
  `last_updated` datetime DEFAULT NULL,
  `started` datetime DEFAULT NULL,
  `notification_sent` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `review_comments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `text` varchar(2048) COLLATE utf8_bin NOT NULL,
  `elements` varchar(8192) COLLATE utf8_bin NOT NULL,
  `review_assignment_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=1 ;

ALTER TABLE  `review_assignment` ADD  `title` VARCHAR( 4096 ) NOT NULL , ADD  `owner` VARCHAR( 4096 ) NOT NULL;

CREATE TABLE IF NOT EXISTS `review_pages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `url` varchar(4096) COLLATE utf8_bin NOT NULL,
  `owner` varchar(1024) COLLATE utf8_bin NOT NULL,
  `title` varchar(4096) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=481 ;

ALTER TABLE  `review_assignment` ADD  `time_taken` INT NOT NULL COMMENT  'in minutes',
ADD  `is_finished` INT NOT NULL DEFAULT  '0',
ADD  `weight` INT NOT NULL;

CREATE TABLE IF NOT EXISTS `project` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=5 ;

CREATE TABLE IF NOT EXISTS `specification_has_project` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `specification_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=18 ;

ALTER TABLE  `user` ADD  `email` VARCHAR( 255 ) NULL ;

ALTER TABLE  `project` ADD  `report_link` TEXT CHARACTER SET utf8 COLLATE utf8_bin NULL ,
ADD  `display_link` TEXT CHARACTER SET utf8 COLLATE utf8_bin NULL,
ADD  `multi_display_link` TEXT CHARACTER SET utf8 COLLATE utf8_bin NULL ;
ALTER TABLE  `test_run` ADD  `project_id` INT NOT NULL DEFAULT  '-1';
ALTER TABLE  `project` ADD  `bug_tracking_system` VARCHAR( 256 ) NOT NULL DEFAULT 'other' ;
ALTER TABLE  `bug` CHANGE  `bug_id`  `bug_id` VARCHAR( 32 ) NOT NULL ;

CREATE TABLE IF NOT EXISTS `session_refresh` (
  `token` varchar(255) COLLATE utf8_bin NOT NULL,
  `created` datetime NOT NULL,
  PRIMARY KEY (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

INSERT INTO `comment`(`name`) VALUES ("Unclear instructions / Don't understand");INSERT INTO `comment`(`name`) VALUES ("Steps are missing or incorrect for my Operating System");INSERT INTO `comment`(`name`) VALUES ("Steps are missing or incorrect in general");INSERT INTO `comment`(`name`) VALUES ("Depends on previous case(s) which failed");INSERT INTO `comment`(`name`) VALUES ("Missing sample file(s) required in test case");INSERT INTO `comment`(`name`) VALUES ("Test case is obsolete");INSERT INTO `comment`(`name`) VALUES ("No time to finish test case");INSERT INTO `comment`(`name`) VALUES ("Duplicate test case");INSERT INTO `comment`(`name`) VALUES ("Minor changes need");INSERT INTO `comment`(`name`) VALUES ("Dependency not met");INSERT INTO `comment`(`name`) VALUES ("Dependency not met");


CREATE TABLE `synergy`.`assignment_blob` ( `assignment_id` INT NOT NULL ,  `test_run_id` INT NOT NULL ,  `data` LONGTEXT NOT NULL ,  `created` datetime NOT NULL DEFAULT NOW() ,    PRIMARY KEY  (`assignment_id`)) ENGINE = InnoDB;

INSERT INTO `project` (`id`, `name`, `report_link`, `display_link`, `multi_display_link`, `bug_tracking_system`) VALUES
(5, 'testproject', 'function(product, component, version, summary){\n  return "";\n}', 'function(bugNumber, returnString){\n  return returnString ? "" : {} \n}', 'function(bugNumbers, returnString){\n  return returnString ? "" : {} \n}', 'other');

INSERT INTO `version` (`version`, `id`, `isObsolete`) VALUES ('1', 2, 0);

INSERT INTO `platform` (`id`, `name`, `is_active`) VALUES (3, 'platform1', 1);