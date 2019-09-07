CREATE TABLE `audit`.`aud_email_account` (
  `ID` BIGINT(20) NOT NULL,
  `REV` INT(11) NOT NULL,
  `REVTYPE` TINYINT(4) NULL,
  `NAME` VARCHAR(255) NULL,
  `PRIMARY_ACCOUNT` BIGINT(11) NULL,
  `PERSON_ID` BIGINT(11) NULL,
  `EMAIL_ACCOUNT_TYPE_ID` BIGINT(11) NULL,
  `EMAIL_STATUS_ID` BIGINT(11) NULL,
  PRIMARY KEY (`ID`, `REV`),
  INDEX `fk_aud_email_account_1_idx` (`REV` ASC),
  CONSTRAINT `fk_aud_email_account_1`
    FOREIGN KEY (`REV`)
    REFERENCES `audit`.`revinfo` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;
