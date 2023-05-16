package com.byes.paap.scheduledtasks;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import com.byes.paap.scheduledtasks.commons.Commons;
import com.planonsoftware.platform.backend.data.v1.IBusinessObject;
import com.planonsoftware.platform.backend.data.v1.IDatabaseQuery;
import com.planonsoftware.platform.backend.data.v1.IDatabaseQueryBuilder;
import com.planonsoftware.platform.backend.data.v1.IFieldDefinition;
import com.planonsoftware.platform.backend.data.v1.IResultSet;
import com.planonsoftware.platform.backend.data.v1.Operator;
import com.planonsoftware.platform.backend.scheduledtask.v3.IScheduledTask;
import com.planonsoftware.platform.backend.scheduledtask.v3.IScheduledTaskContext;
import com.planonsoftware.platform.data.v1.ActionNotFoundException;
import com.planonsoftware.platform.data.v1.BusinessException;
import com.planonsoftware.platform.data.v1.FieldNotFoundException;

public class CreateConditionRecordsFromFile implements IScheduledTask {
    public transient IDatabaseQuery dbQuery = null;
    public transient IResultSet queryResults = null;
    
    public transient IDatabaseQuery baseServiceAgreementQuery = null;
	public transient IResultSet baseServiceAgreementQueryResults = null;
	
	public transient IDatabaseQuery generalTermsQuery = null;
	public transient IResultSet generalTermsQueryResults = null;
	
	public transient IDatabaseQuery timeTermsQuery = null;
	public transient IResultSet timeTermsQueryResults = null;
	
	public transient IDatabaseQuery manHourTermsQuery = null;
	public transient IResultSet manHourTermsQueryResults = null;

	public transient IDatabaseQuery subContractorTermsQuery = null;
	public transient IResultSet subContractorTermsQueryResults = null;

	public transient IDatabaseQuery materialTermsQuery = null;
	public transient IResultSet materialTermsQueryResults = null;

	public transient IDatabaseQuery travelTermsQuery = null;
	public transient IResultSet travelTermsQueryResults = null;

	public transient IDatabaseQuery travelTermMethodQuery = null;
	public transient IResultSet travelTermMethodQueryResults = null;

    public void execute(IScheduledTaskContext aContext) {
        String peetLocation = Commons.getPeetLocation(aContext);
        InputStream ips;
        try {
            ips = new FileInputStream(peetLocation + "data/peet/inbound/conditionRecords/ConditionRecords.csv");
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String line;
            while ((line = br.readLine()) != null) {
                String propertyCode = line.split(";")[0];
                String templateCode = line.split(";")[1];

                this.dbQuery = aContext.getDataService().getPVDatabaseQuery("PropertyQuery");
                this.dbQuery.getStringSearchExpression("Code", Operator.EQUAL).addValue(propertyCode);
                this.queryResults = this.dbQuery.execute();

                String propertyName = "";
                int propertySyscode = 0;
                int customerPrimaryKey = 0;

                IBusinessObject propertyBO;
                while (this.queryResults.next()) {
                    propertyBO = aContext.getDataService().getByPrimaryKey("Property", queryResults.getPrimaryKey());
                    propertySyscode = queryResults.getPrimaryKey();
                    propertyName = queryResults.getString("Name");
                    customerPrimaryKey = propertyBO.getReferenceFieldByName("FreeInteger1").getValue().getPrimaryKey();
                }
                this.dbQuery = aContext.getDataService().getPVDatabaseQuery("ConditionRecordQuery");
                this.dbQuery.getStringSearchExpression("Name", Operator.CONTAINS).addValue(propertyName);
                int count = this.dbQuery.executeCount();

                if (count > 0) {
                    System.out.println("Has condition Record!");
                    continue;
                }
                System.out.println(propertyCode);
                System.out.println(templateCode);
                IBusinessObject template = null;
                this.dbQuery = aContext.getDataService().getPVDatabaseQuery("ConditionRecordTemplateQuery");
                this.dbQuery.getSearchExpression("Code", Operator.EQUAL).setValue(templateCode);
                this.queryResults = this.dbQuery.execute();

                if (this.queryResults.next()) {
                    template = aContext.getDataService().getByPrimaryKey("UsrConditionRecordTemplate", queryResults.getPrimaryKey());
                }
                
                IBusinessObject newCR = Commons.createUsrConditionRecord(aContext, template.getPrimaryKey(), customerPrimaryKey, propertyName);
                if (newCR == null) {
                    System.out.println("Error: No client linked");
                    continue;
                }
                int newCRKey = newCR.getPrimaryKey();
                newCRKey = newCR.getReferenceFieldByName("PivotLifecycleRef").getValue().getPrimaryKey();
                this.dbQuery = aContext.getDataService().getPVDatabaseQuery("SLAAmountQuery");
                this.dbQuery.getSearchExpression("ContractRef", Operator.EQUAL).setValue(template.getPrimaryKey());
                this.queryResults = this.dbQuery.execute();

                while (this.queryResults.next()) {
                    IBusinessObject conditionRecordLine = aContext.getDataService().getByPrimaryKey("UsrSLAAmount", this.queryResults.getPrimaryKey());
                    IBusinessObject newConditionRecordLine = Commons.createUsrSLAAmount(aContext, conditionRecordLine.getPrimaryKey(), newCRKey, propertySyscode);
                    this.baseServiceAgreementQuery = aContext.getDataService().getPVDatabaseQuery("ContractLineServiceAgreementQuery");
                    this.baseServiceAgreementQuery.setPageSize(1000);
                    this.baseServiceAgreementQuery.getSearchExpression("ContractLineRef", Operator.EQUAL).setValue(conditionRecordLine.getPrimaryKey());
                    this.baseServiceAgreementQueryResults = this.baseServiceAgreementQuery.execute();

                    while (this.baseServiceAgreementQueryResults.next()) {
                        IBusinessObject baseServiceAgreement = aContext.getDataService().getByPrimaryKey("ContractLineServiceAgreement", this.baseServiceAgreementQueryResults.getPrimaryKey());
                        IBusinessObject newBaseServiceAgreement = Commons.createBaseServiceAgreement(aContext, baseServiceAgreement.getPrimaryKey(), newConditionRecordLine.getReferenceFieldByName("PivotLifecycleRef").getValue().getPrimaryKey());
                        this.generalTermsQuery = aContext.getDataService().getPVDatabaseQuery("GeneralTermsQuery");
                        this.generalTermsQuery.setPageSize(1000);
                        this.generalTermsQuery.getSearchExpression("ContractlineRef", Operator.EQUAL).setValue(conditionRecordLine.getPrimaryKey());
                        this.generalTermsQueryResults = this.generalTermsQuery.execute();
                        while (this.generalTermsQueryResults.next()) {
                            IBusinessObject generalTerms = aContext.getDataService().getByPrimaryKey("GeneralTerms", this.generalTermsQueryResults.getPrimaryKey());
                            IBusinessObject newGeneralTerms = Commons.createGeneralTerms(aContext, generalTerms.getPrimaryKey(), newConditionRecordLine.getReferenceFieldByName("PivotLifecycleRef").getValue().getPrimaryKey());
                            this.generalTermsQueryResults.next();
                        }
                        this.timeTermsQuery = aContext.getDataService().getPVDatabaseQuery("TimeTermsQuery");
                        this.timeTermsQuery.setPageSize(1000);
                        this.timeTermsQuery.getSearchExpression("ServiceAgreementRef", Operator.EQUAL).setValue(baseServiceAgreement.getPrimaryKey());
                        this.timeTermsQueryResults = this.timeTermsQuery.execute();
                        while (this.timeTermsQueryResults.next()) {
                            IBusinessObject timeTerms = aContext.getDataService().getByPrimaryKey("TimeTerms", this.timeTermsQueryResults.getPrimaryKey());
                            IBusinessObject newTimeTerms = Commons.createTimeTerms(aContext, timeTerms.getPrimaryKey(), newBaseServiceAgreement.getReferenceFieldByName("PivotLifecycleRef").getValue().getPrimaryKey());
                            this.timeTermsQueryResults.next();
                        }
                        this.manHourTermsQuery = aContext.getDataService().getPVDatabaseQuery("ManHourTermsQuery");
                        this.manHourTermsQuery.setPageSize(1000);
                        this.manHourTermsQuery.getSearchExpression("ServiceAgreementRef", Operator.EQUAL).setValue(baseServiceAgreement.getPrimaryKey());
                        this.manHourTermsQueryResults = this.manHourTermsQuery.execute();
                        while (this.manHourTermsQueryResults.next()) {
                            IBusinessObject manHourTerms = aContext.getDataService().getByPrimaryKey("ManHourTerms", this.manHourTermsQueryResults.getPrimaryKey());
                            IBusinessObject newManHourTerms = Commons.createManHourTerms(aContext, manHourTerms.getPrimaryKey(), newBaseServiceAgreement.getReferenceFieldByName("PivotLifecycleRef").getValue().getPrimaryKey());
                            this.manHourTermsQueryResults.next();
                        }
                        this.subContractorTermsQuery = aContext.getDataService().getPVDatabaseQuery("SubContractorTermsQuery");
                        this.subContractorTermsQuery.setPageSize(1000);
                        this.subContractorTermsQuery.getSearchExpression("ServiceAgreementRef", Operator.EQUAL).setValue(baseServiceAgreement.getPrimaryKey());
                        this.subContractorTermsQueryResults = this.subContractorTermsQuery.execute();
                        while (this.subContractorTermsQueryResults.next()) {
                            IBusinessObject subContractorTerms = aContext.getDataService().getByPrimaryKey("SubContractorTerms", this.subContractorTermsQueryResults.getPrimaryKey());
                            IBusinessObject newSubContractorTerms = Commons.createSubContractorTerms(aContext, subContractorTerms.getPrimaryKey(), newBaseServiceAgreement.getReferenceFieldByName("PivotLifecycleRef").getValue().getPrimaryKey());
                            this.subContractorTermsQueryResults.next();
                        }
                        this.materialTermsQuery = aContext.getDataService().getPVDatabaseQuery("MaterialTermsQuery");
                        this.materialTermsQuery.setPageSize(1000);
                        this.materialTermsQuery.getSearchExpression("ServiceAgreementRef", Operator.EQUAL).setValue(baseServiceAgreement.getPrimaryKey());
                        this.materialTermsQueryResults = this.materialTermsQuery.execute();
                        while (this.materialTermsQueryResults.next()) {
                            IBusinessObject materialTerms = aContext.getDataService().getByPrimaryKey("MaterialTerms", this.materialTermsQueryResults.getPrimaryKey());
                            IBusinessObject newMaterialTerms = Commons.createMaterialTerms(aContext, materialTerms.getPrimaryKey(), newBaseServiceAgreement.getReferenceFieldByName("PivotLifecycleRef").getValue().getPrimaryKey());
                            this.materialTermsQueryResults.next();
                        }
                        this.travelTermsQuery = aContext.getDataService().getPVDatabaseQuery("TravelTermsQuery");
                        this.travelTermsQuery.setPageSize(1000);
                        this.travelTermsQuery.getSearchExpression("ServiceAgreementRef", Operator.EQUAL).setValue(baseServiceAgreement.getPrimaryKey());
                        this.travelTermsQueryResults = this.travelTermsQuery.execute();
                        while (this.travelTermsQueryResults.next()) {
                            IBusinessObject travelTerms = aContext.getDataService().getByPrimaryKey("TravelTerms", this.travelTermsQueryResults.getPrimaryKey());
                            IBusinessObject newTravelTerms = Commons.createTravelTerms(aContext, travelTerms.getPrimaryKey(), newBaseServiceAgreement.getReferenceFieldByName("PivotLifecycleRef").getValue().getPrimaryKey());
                            this.travelTermsQueryResults.next();
                        }
                        this.travelTermMethodQuery = aContext.getDataService().getPVDatabaseQuery("TravelTermMethodQuery");
                        this.travelTermMethodQuery.setPageSize(1000);
                        this.travelTermMethodQuery.getSearchExpression("ServiceAgreementRef", Operator.EQUAL).setValue(baseServiceAgreement.getPrimaryKey());
                        this.travelTermMethodQueryResults = this.travelTermMethodQuery.execute();
                        while (this.travelTermMethodQueryResults.next()) {
                            IBusinessObject travelTermMethod = aContext.getDataService().getByPrimaryKey("TravelTermMethod", this.travelTermMethodQueryResults.getPrimaryKey());
                            IBusinessObject newTravelTermMethod = Commons.createTravelTermMethod(aContext, travelTermMethod.getPrimaryKey(), newBaseServiceAgreement.getReferenceFieldByName("PivotLifecycleRef").getValue().getPrimaryKey());
                            this.travelTermMethodQueryResults.next();
                        }
                        this.baseServiceAgreementQueryResults.next();
                    }
                    this.queryResults.next();
                }

            }
            br.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
     }
}