
INSERT INTO REPORT(REPORT_ID,REPORT_CATEGORY_ID,REPORT_NAME,REPORT_IDENTIFIER) VALUES(28,6,'Detailed Aging of Portfolio at Risk','aging_portfolio_at_risk');

INSERT INTO report_jasper_map(REPORT_ID,REPORT_CATEGORY_ID,REPORT_NAME,REPORT_IDENTIFIER, REPORT_JASPER) VALUES 
(28,6,'Detailed Aging of Portfolio at Risk','aging_portfolio_at_risk',
'DetailedAgingPortfolioAtRisk.rptdesign');

INSERT INTO LOOKUP_VALUE VALUES(581,87,' ');
INSERT INTO LOOKUP_VALUE_LOCALE VALUES(926,1,581,'Can view Detailed Aging of Portfolio at Risk');
INSERT INTO ACTIVITY(ACTIVITY_ID,PARENT_ID,ACTIVITY_NAME_LOOKUP_ID,DESCRIPTION_LOOKUP_ID) VALUES(207,150,581,581);
INSERT INTO ROLES_ACTIVITY VALUES (207,1);

update DATABASE_VERSION set DATABASE_VERSION = 117 where DATABASE_VERSION = 116;
