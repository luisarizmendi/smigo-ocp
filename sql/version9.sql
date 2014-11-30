CREATE TABLE rules_x_tags (
  rule_id INT NOT NULL,
  tag_id  INT NOT NULL
);

CREATE TABLE tags (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(64),
  PRIMARY KEY (id)
);

INSERT INTO tags (name)VALUES ('fight_disease'),('repel_pest'),('improve_flavor');
INSERT INTO rules_x_tags (rule_id, tag_id) SELECT
                                             rules.rule_id,
                                             d.type
                                           FROM rules
                                             JOIN (SELECT
                                                     *
                                                   FROM rules
                                                   WHERE rule_id IN (19, 10, 74, 88, 105, 117)) d
                                               ON d.host = rules.host AND d.causer = rules.causer
                                           WHERE rules.rule_id NOT IN (19, 10, 74, 88, 105, 117);

DELETE FROM rules WHERE rule_id IN (19, 10, 63, 74, 88, 105, 117);
INSERT INTO rules_x_tags (rule_id, tag_id) SELECT rule_id,1 FROM rules WHERE rules.type = 1;
INSERT INTO rules_x_tags (rule_id, tag_id) SELECT rule_id,2 FROM rules WHERE rules.type = 2;
INSERT INTO rules_x_tags (rule_id, tag_id) SELECT rule_id,3 FROM rules WHERE rules.type = 3;
UPDATE rules SET type = 0 WHERE type IN (1,2,3);

