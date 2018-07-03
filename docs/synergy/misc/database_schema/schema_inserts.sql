SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `synergy` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `synergy` ;

-- -----------------------------------------------------
-- Table `synergy`.`specification`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`specification` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`specification` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `title` VARCHAR(512) NOT NULL ,
  `description` LONGTEXT NOT NULL ,
  `author_id` INT NOT NULL ,
  `version_id` INT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_specification_user1`
    FOREIGN KEY (`author_id` )
    REFERENCES `synergy`.`user` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_specification_version1`
    FOREIGN KEY (`version_id` )
    REFERENCES `synergy`.`version` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `synergy`.`user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`user` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`user` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `username` VARCHAR(256) NOT NULL ,
  `first_name` VARCHAR(256) NOT NULL ,
  `last_name` VARCHAR(256) NOT NULL ,
  `role` VARCHAR(45) NULL DEFAULT 'viewer' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `synergy`.`version`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`version` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`version` (
  `version` VARCHAR(52) NOT NULL ,
  `id` INT NOT NULL AUTO_INCREMENT ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;

CREATE UNIQUE INDEX `version_UNIQUE` ON `synergy`.`version` (`version` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`specification`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`specification` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`specification` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `title` VARCHAR(512) NOT NULL ,
  `description` LONGTEXT NOT NULL ,
  `author_id` INT NOT NULL ,
  `version_id` INT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_specification_user1`
    FOREIGN KEY (`author_id` )
    REFERENCES `synergy`.`user` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_specification_version1`
    FOREIGN KEY (`version_id` )
    REFERENCES `synergy`.`version` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_specification_user1_idx` ON `synergy`.`specification` (`author_id` ASC) ;

CREATE INDEX `fk_specification_version1_idx` ON `synergy`.`specification` (`version_id` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`suite`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`suite` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`suite` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `title` VARCHAR(512) NOT NULL ,
  `description` LONGTEXT NOT NULL ,
  `product` VARCHAR(512) NOT NULL ,
  `component` VARCHAR(512) NOT NULL ,
  `specification_id` INT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_suite_specification1`
    FOREIGN KEY (`specification_id` )
    REFERENCES `synergy`.`specification` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_suite_specification1_idx` ON `synergy`.`suite` (`specification_id` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`case`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`case` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`case` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `duration` INT NOT NULL DEFAULT 0 ,
  `title` VARCHAR(512) NOT NULL ,
  `steps` LONGTEXT NOT NULL ,
  `result` LONGTEXT NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `synergy`.`keyword`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`keyword` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`keyword` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `keyword` VARCHAR(128) NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;

CREATE UNIQUE INDEX `keyword_UNIQUE` ON `synergy`.`keyword` (`keyword` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`case_has_keyword`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`case_has_keyword` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`case_has_keyword` (
  `case_id` INT NOT NULL ,
  `keyword_id` INT NOT NULL ,
  PRIMARY KEY (`case_id`, `keyword_id`) ,
  CONSTRAINT `fk_case_has_keyword_case`
    FOREIGN KEY (`case_id` )
    REFERENCES `synergy`.`case` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_case_has_keyword_keyword1`
    FOREIGN KEY (`keyword_id` )
    REFERENCES `synergy`.`keyword` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_case_has_keyword_keyword1_idx` ON `synergy`.`case_has_keyword` (`keyword_id` ASC) ;

CREATE INDEX `fk_case_has_keyword_case_idx` ON `synergy`.`case_has_keyword` (`case_id` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`platform`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`platform` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`platform` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(256) NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;

CREATE UNIQUE INDEX `name_UNIQUE` ON `synergy`.`platform` (`name` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`specification_attachement`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`specification_attachement` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`specification_attachement` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `path` VARCHAR(1024) NOT NULL ,
  `specification_id` INT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_attachement_specification1`
    FOREIGN KEY (`specification_id` )
    REFERENCES `synergy`.`specification` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_attachement_specification1_idx` ON `synergy`.`specification_attachement` (`specification_id` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`test_run`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`test_run` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`test_run` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `title` VARCHAR(512) NOT NULL ,
  `description` LONGTEXT NULL ,
  `start` DATETIME NOT NULL ,
  `end` DATETIME NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `synergy`.`test_assignement`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`test_assignement` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`test_assignement` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `user_id` INT NOT NULL ,
  `platform_id` INT NOT NULL ,
  `specification_id` INT NOT NULL ,
  `state` VARCHAR(45) NULL ,
  `test_run_id` INT NOT NULL ,
  `number_of_cases` INT NOT NULL DEFAULT 0 ,
  `number_of_completed_cases` INT NOT NULL DEFAULT 0 ,
  `label` VARCHAR(255) NOT NULL DEFAULT 0 ,
  `keyword_id` INT NULL ,
  `passed_cases` INT NULL DEFAULT 0 ,
  `skipped_cases` INT NULL DEFAULT 0 ,
  `failed_cases` INT NULL DEFAULT 0 ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_test_assignement_user1`
    FOREIGN KEY (`user_id` )
    REFERENCES `synergy`.`user` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_test_assignement_platform1`
    FOREIGN KEY (`platform_id` )
    REFERENCES `synergy`.`platform` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_test_assignement_specification1`
    FOREIGN KEY (`specification_id` )
    REFERENCES `synergy`.`specification` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_test_assignement_test_run1`
    FOREIGN KEY (`test_run_id` )
    REFERENCES `synergy`.`test_run` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_test_assignement_keyword1`
    FOREIGN KEY (`keyword_id` )
    REFERENCES `synergy`.`keyword` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_test_assignement_user1_idx` ON `synergy`.`test_assignement` (`user_id` ASC) ;

CREATE INDEX `fk_test_assignement_platform1_idx` ON `synergy`.`test_assignement` (`platform_id` ASC) ;

CREATE INDEX `fk_test_assignement_specification1_idx` ON `synergy`.`test_assignement` (`specification_id` ASC) ;

CREATE INDEX `fk_test_assignement_test_run1_idx` ON `synergy`.`test_assignement` (`test_run_id` ASC) ;

CREATE INDEX `fk_test_assignement_keyword1_idx` ON `synergy`.`test_assignement` (`keyword_id` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`user_has_favorite`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`user_has_favorite` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`user_has_favorite` (
  `user_id` INT NOT NULL ,
  `specification_id` INT NOT NULL ,
  PRIMARY KEY (`user_id`, `specification_id`) ,
  CONSTRAINT `fk_user_has_specification_user1`
    FOREIGN KEY (`user_id` )
    REFERENCES `synergy`.`user` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_has_specification_specification1`
    FOREIGN KEY (`specification_id` )
    REFERENCES `synergy`.`specification` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_user_has_specification_specification1_idx` ON `synergy`.`user_has_favorite` (`specification_id` ASC) ;

CREATE INDEX `fk_user_has_specification_user1_idx` ON `synergy`.`user_has_favorite` (`user_id` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`tribe`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`tribe` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`tribe` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(512) NOT NULL ,
  `description` TEXT NULL ,
  `leader_id` INT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_tribe_user1`
    FOREIGN KEY (`leader_id` )
    REFERENCES `synergy`.`user` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_tribe_user1_idx` ON `synergy`.`tribe` (`leader_id` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`user_is_member_of`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`user_is_member_of` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`user_is_member_of` (
  `user_id` INT NOT NULL ,
  `tribe_id` INT NOT NULL ,
  PRIMARY KEY (`user_id`, `tribe_id`) ,
  CONSTRAINT `fk_user_has_tribe_user1`
    FOREIGN KEY (`user_id` )
    REFERENCES `synergy`.`user` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_has_tribe_tribe1`
    FOREIGN KEY (`tribe_id` )
    REFERENCES `synergy`.`tribe` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_user_has_tribe_tribe1_idx` ON `synergy`.`user_is_member_of` (`tribe_id` ASC) ;

CREATE INDEX `fk_user_has_tribe_user1_idx` ON `synergy`.`user_is_member_of` (`user_id` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`run_attachement`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`run_attachement` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`run_attachement` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `path` VARCHAR(1024) NOT NULL ,
  `test_run_id` INT NOT NULL ,
  PRIMARY KEY (`id`, `test_run_id`) ,
  CONSTRAINT `fk_run_attachement_test_run1`
    FOREIGN KEY (`test_run_id` )
    REFERENCES `synergy`.`test_run` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_run_attachement_test_run1_idx` ON `synergy`.`run_attachement` (`test_run_id` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`bug`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`bug` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`bug` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `bug_id` INT NOT NULL ,
  `case_id` INT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_bug_case1`
    FOREIGN KEY (`case_id` )
    REFERENCES `synergy`.`case` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_bug_case1_idx` ON `synergy`.`bug` (`case_id` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`suite_has_case`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`suite_has_case` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`suite_has_case` (
  `suite_id` INT NOT NULL ,
  `case_id` INT NOT NULL ,
  PRIMARY KEY (`suite_id`, `case_id`) ,
  CONSTRAINT `fk_suite_has_case_suite1`
    FOREIGN KEY (`suite_id` )
    REFERENCES `synergy`.`suite` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_suite_has_case_case1`
    FOREIGN KEY (`case_id` )
    REFERENCES `synergy`.`case` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_suite_has_case_case1_idx` ON `synergy`.`suite_has_case` (`case_id` ASC) ;

CREATE INDEX `fk_suite_has_case_suite1_idx` ON `synergy`.`suite_has_case` (`suite_id` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`assignment_progress`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`assignment_progress` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`assignment_progress` (
  `data` MEDIUMBLOB NOT NULL ,
  `test_assignement_id` INT NOT NULL ,
  CONSTRAINT `fk_assignment_progress_test_assignement1`
    FOREIGN KEY (`test_assignement_id` )
    REFERENCES `synergy`.`test_assignement` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_assignment_progress_test_assignement1_idx` ON `synergy`.`assignment_progress` (`test_assignement_id` ASC) ;


-- -----------------------------------------------------
-- Table `synergy`.`case_image`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synergy`.`case_image` ;

CREATE  TABLE IF NOT EXISTS `synergy`.`case_image` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `title` VARCHAR(256) NOT NULL ,
  `path` VARCHAR(1024) NOT NULL ,
  `case_id` INT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_case_image_case1`
    FOREIGN KEY (`case_id` )
    REFERENCES `synergy`.`case` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_case_image_case1_idx` ON `synergy`.`case_image` (`case_id` ASC) ;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `synergy`.`user`
-- -----------------------------------------------------
START TRANSACTION;
USE `synergy`;
INSERT INTO `synergy`.`user` (`id`, `username`, `first_name`, `last_name`, `role`) VALUES (1, 'tester', 'John', 'Smith', 'viewer');
INSERT INTO `synergy`.`user` (`id`, `username`, `first_name`, `last_name`, `role`) VALUES (2, 'admin', 'Admin', 'Root', 'admin');

COMMIT;

-- -----------------------------------------------------
-- Data for table `synergy`.`version`
-- -----------------------------------------------------
START TRANSACTION;
USE `synergy`;
INSERT INTO `synergy`.`version` (`version`, `id`) VALUES ('7.3', 1);

COMMIT;

-- -----------------------------------------------------
-- Data for table `synergy`.`specification`
-- -----------------------------------------------------
START TRANSACTION;
USE `synergy`;
INSERT INTO `synergy`.`specification` (`id`, `title`, `description`, `author_id`, `version_id`) VALUES (1, 'JavaScript Editor', 'Some Long Description here', 1, 1);
INSERT INTO `synergy`.`specification` (`id`, `title`, `description`, `author_id`, `version_id`) VALUES (2, 'Java Editor', 'Some Long Description here for Java Editor', 2, 1);

COMMIT;

-- -----------------------------------------------------
-- Data for table `synergy`.`suite`
-- -----------------------------------------------------
START TRANSACTION;
USE `synergy`;
INSERT INTO `synergy`.`suite` (`id`, `title`, `description`, `product`, `component`, `specification_id`) VALUES (1, 'Basic functionality', 'Setup and other useful info', '1', '2', 1);
INSERT INTO `synergy`.`suite` (`id`, `title`, `description`, `product`, `component`, `specification_id`) VALUES (2, 'Code Completion', 'No description', '2', '1111', 1);

COMMIT;

-- -----------------------------------------------------
-- Data for table `synergy`.`case`
-- -----------------------------------------------------
START TRANSACTION;
USE `synergy`;
INSERT INTO `synergy`.`case` (`id`, `duration`, `title`, `steps`, `result`) VALUES (1, 10, 'Prototype', '<ol><li>Step 1</li><li>Step 2</li></ol>', 'This should happen');
INSERT INTO `synergy`.`case` (`id`, `duration`, `title`, `steps`, `result`) VALUES (2, 6, 'Inheritance', '<ol><li>Step 1</li><li>Step 2</li></ol>', 'This should happen');
INSERT INTO `synergy`.`case` (`id`, `duration`, `title`, `steps`, `result`) VALUES (3, 10, 'Embedded', '<ol><li>Step 1</li><li>Step 2</li></ol>', 'This should happen');

COMMIT;

-- -----------------------------------------------------
-- Data for table `synergy`.`keyword`
-- -----------------------------------------------------
START TRANSACTION;
USE `synergy`;
INSERT INTO `synergy`.`keyword` (`id`, `keyword`) VALUES (1, 'sanity');
INSERT INTO `synergy`.`keyword` (`id`, `keyword`) VALUES (2, 'obsolete');
INSERT INTO `synergy`.`keyword` (`id`, `keyword`) VALUES (3, 'fails');

COMMIT;

-- -----------------------------------------------------
-- Data for table `synergy`.`case_has_keyword`
-- -----------------------------------------------------
START TRANSACTION;
USE `synergy`;
INSERT INTO `synergy`.`case_has_keyword` (`case_id`, `keyword_id`) VALUES (3, 1);
INSERT INTO `synergy`.`case_has_keyword` (`case_id`, `keyword_id`) VALUES (2, 3);

COMMIT;

-- -----------------------------------------------------
-- Data for table `synergy`.`platform`
-- -----------------------------------------------------
START TRANSACTION;
USE `synergy`;
INSERT INTO `synergy`.`platform` (`id`, `name`) VALUES (1, 'Windows 7 32b JDK7u7 32b');
INSERT INTO `synergy`.`platform` (`id`, `name`) VALUES (2, 'Ubuntu 12.04 JDK6u35 64b');

COMMIT;

-- -----------------------------------------------------
-- Data for table `synergy`.`user_has_favorite`
-- -----------------------------------------------------
START TRANSACTION;
USE `synergy`;
INSERT INTO `synergy`.`user_has_favorite` (`user_id`, `specification_id`) VALUES (1, 2);

COMMIT;

-- -----------------------------------------------------
-- Data for table `synergy`.`tribe`
-- -----------------------------------------------------
START TRANSACTION;
USE `synergy`;
INSERT INTO `synergy`.`tribe` (`id`, `name`, `description`, `leader_id`) VALUES (1, 'Web Client', 'description', 2);

COMMIT;

-- -----------------------------------------------------
-- Data for table `synergy`.`user_is_member_of`
-- -----------------------------------------------------
START TRANSACTION;
USE `synergy`;
INSERT INTO `synergy`.`user_is_member_of` (`user_id`, `tribe_id`) VALUES (1, 1);

COMMIT;
