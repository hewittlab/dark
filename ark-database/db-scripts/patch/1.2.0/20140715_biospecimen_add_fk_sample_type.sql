ALTER TABLE `lims`.`biospecimen` 
  ADD CONSTRAINT `fk_biospecimen_sampletype`
  FOREIGN KEY (`SAMPLETYPE_ID` )
  REFERENCES `lims`.`bio_sampletype` (`ID` )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
, ADD INDEX `fk_biospecimen_sampletype_idx` (`SAMPLETYPE_ID` ASC) ;


-- select * from lims.biospecimen
-- where sampletype_id not in (select id from lims.bio_sampletype);

