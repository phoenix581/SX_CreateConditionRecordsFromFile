package com.byes.paap.scheduledtasks.commons;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import com.planonsoftware.platform.backend.data.v1.BusinessException;
import com.planonsoftware.platform.backend.data.v1.FieldNotFoundException;
import com.planonsoftware.platform.backend.data.v1.IBusinessObject;
import com.planonsoftware.platform.backend.data.v1.IDatabaseQuery;
import com.planonsoftware.platform.backend.data.v1.IDatabaseQueryBuilder;
import com.planonsoftware.platform.backend.data.v1.IFieldDefinition;
import com.planonsoftware.platform.backend.data.v1.IResultSet;
import com.planonsoftware.platform.backend.data.v1.Operator;
import com.planonsoftware.platform.backend.scheduledtask.v3.IScheduledTaskContext;

public class Commons
{
    public transient static IDatabaseQuery dbQuery = null;
    public transient static IResultSet queryResults = null;

    public transient static IDatabaseQuery tradeQuery = null;
    public transient static IResultSet tradeQueryResults = null;

    public static String getPeetLocation(IScheduledTaskContext context) {
        IDatabaseQueryBuilder queryBuilder = context.getDataService().getBODatabaseQueryBuilder("SystemSettingFileLocation");
        queryBuilder.addSelectField("PathDataImportExport", "peetLocation");
        IDatabaseQuery query = queryBuilder.build();
        IResultSet resultSet = query.execute();
        if (resultSet.next())
            return resultSet.getString("peetLocation"); 
        return null;
    }
    
    public static IBusinessObject createUsrConditionRecord(IScheduledTaskContext aContext, int templatePK, int customerPrimaryKey, String buildingName) throws BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject conditionRecordBO = aContext.getDataService().create("UsrConditionRecord");
        IBusinessObject template = aContext.getDataService().getByPrimaryKey("UsrConditionRecordTemplate", templatePK);
        
        Iterator<IFieldDefinition> fieldIterator = template.getBODefinition().getFieldDefIterator();

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        while (fieldIterator.hasNext()) {
            IFieldDefinition fieldDefinition = fieldIterator.next();
            
            if (fieldDefinition.isInUse() && !fieldDefinition.isReadOnly()) {
                String systemName = fieldDefinition.getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    conditionRecordBO.getStringFieldByName(systemName).setValue(template.getStringFieldByName("Code").getValue() + "_" + buildingName);
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    conditionRecordBO.getDateNeutralFieldByName(systemName).setValue(date);
                    continue;
                }
                if ("CustomerRef".equals(systemName)) {
                    conditionRecordBO.getReferenceFieldByName(systemName).setValueAsInteger(customerPrimaryKey);
                    continue;
                }
                if ("FreeInteger6".equals(systemName)) {
                    conditionRecordBO.getReferenceFieldByName(systemName).setValueAsInteger(customerPrimaryKey);
                    continue;
                }

                conditionRecordBO.getFieldByName(systemName).setValueAsObject(template.getFieldByName(systemName).getValueAsObject());
            }
        }

        try {
            conditionRecordBO.save();
            return conditionRecordBO;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
     }

     public static IBusinessObject createUsrSLAAmount(IScheduledTaskContext aContext, int templatePK, int primaryKey, int buildingPrimaryKey) throws BusinessException, FieldNotFoundException, ParseException {        

        IBusinessObject newConditionRecordLineBO = aContext.getDataService().create("UsrSLAAmount");
        newConditionRecordLineBO.getReferenceFieldByName("ContractRef").setValueAsInteger(primaryKey);
        
        IBusinessObject template = aContext.getDataService().getByPrimaryKey("UsrSLAAmount", templatePK);

        Iterator<IFieldDefinition> fieldIterator = template.getBODefinition().getFieldDefIterator();
       
        String dateInString = "01.01.2010";
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Date date = formatter.parse(dateInString);

        while (fieldIterator.hasNext()) {
            IFieldDefinition fieldDefinition = fieldIterator.next();

            if (fieldDefinition.isInUse() && !fieldDefinition.isReadOnly()) {
                String systemName = fieldDefinition.getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newConditionRecordLineBO.getStringFieldByName(systemName).setValue(template.getStringFieldByName("Code").getValue() + "_" + template.getStringFieldByName("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName)) {
                    newConditionRecordLineBO.getDateNeutralFieldByName(systemName).setValue(date);
                    continue;
                }
                if ("PropertyRef".equals(systemName)) {
                    newConditionRecordLineBO.getReferenceFieldByName(systemName).setValueAsInteger(buildingPrimaryKey);
                    continue;
                }

                if ("ContractRef".equals(systemName)) {
                    continue;
                }
                newConditionRecordLineBO.getFieldByName(systemName).setValueAsObject(template.getFieldByName(systemName).getValueAsObject());
            }
        }

        newConditionRecordLineBO.save();

        return newConditionRecordLineBO;
     }

     public static IBusinessObject createBaseServiceAgreement(IScheduledTaskContext aContext, int templatePK, int primaryKey) throws BusinessException, FieldNotFoundException, ParseException {

        IBusinessObject newConditionRecordLineBO = aContext.getDataService().create("ContractLineServiceAgreement");
        IBusinessObject template = aContext.getDataService().getByPrimaryKey("ContractLineServiceAgreement", templatePK);

        Iterator<IFieldDefinition> fieldIterator = template.getBODefinition().getFieldDefIterator();
       
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        while (fieldIterator.hasNext()) {
            IFieldDefinition fieldDefinition = fieldIterator.next();

            if (fieldDefinition.isInUse() && !fieldDefinition.isReadOnly()) {
                String systemName = fieldDefinition.getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newConditionRecordLineBO.getStringFieldByName(systemName).setValue(template.getStringFieldByName("Code").getValue() + "_" + template.getStringFieldByName("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName)) {
                    newConditionRecordLineBO.getDateNeutralFieldByName(systemName).setValue(date);
                    continue;
                }
                if ("ContractLineRef".equals(systemName)) {
                    newConditionRecordLineBO.getReferenceFieldByName(systemName).setValueAsInteger(primaryKey);
                    continue;
                }
                newConditionRecordLineBO.getFieldByName(systemName).setValueAsObject(template.getFieldByName(systemName).getValueAsObject());
            }
        }

        newConditionRecordLineBO.save();

        return newConditionRecordLineBO;
     }

     public static IBusinessObject createGeneralTerms(IScheduledTaskContext aContext, int templatePK, int primaryKey) throws BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject newGeneralTermsBO = aContext.getDataService().create("GeneralTerms");
        IBusinessObject template = aContext.getDataService().getByPrimaryKey("GeneralTerms", templatePK);
        
        Iterator<IFieldDefinition> fieldIterator = template.getBODefinition().getFieldDefIterator();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
         while (fieldIterator.hasNext()) {
            IFieldDefinition fieldDefinition = fieldIterator.next();

            if (fieldDefinition.isInUse() && !fieldDefinition.isReadOnly()) {
                String systemName = fieldDefinition.getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newGeneralTermsBO.getStringFieldByName(systemName).setValue(template.getStringFieldByName("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    newGeneralTermsBO.getDateNeutralFieldByName(systemName).setValue(date);
                    continue;
                }
                if ("ContractlineRef".equals(systemName)) {
                    newGeneralTermsBO.getReferenceFieldByName(systemName).setValueAsInteger(primaryKey);
                    continue;
                }
                newGeneralTermsBO.getFieldByName(systemName).setValueAsObject(template.getFieldByName(systemName).getValueAsObject());
            }
        }

        newGeneralTermsBO.save();

        return newGeneralTermsBO;
     }

     public static IBusinessObject createTimeTerms(IScheduledTaskContext aContext, int templatePK, int primaryKey) throws BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject newTimeTermsBO = aContext.getDataService().create("TimeTerms");
        IBusinessObject template = aContext.getDataService().getByPrimaryKey("TimeTerms", templatePK);
        
        Iterator<IFieldDefinition> fieldIterator = template.getBODefinition().getFieldDefIterator();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        while (fieldIterator.hasNext()) {
            IFieldDefinition fieldDefinition = fieldIterator.next();

            if (fieldDefinition.isInUse() && !fieldDefinition.isReadOnly()) {
                String systemName = fieldDefinition.getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newTimeTermsBO.getStringFieldByName(systemName).setValue(template.getStringFieldByName("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    newTimeTermsBO.getDateNeutralFieldByName(systemName).setValue(date);
                    continue;
                }
                if ("ServiceAgreementRef".equals(systemName)) {
                    newTimeTermsBO.getReferenceFieldByName(systemName).setValueAsInteger(primaryKey);
                    continue;
                }
                newTimeTermsBO.getFieldByName(systemName).setValueAsObject(template.getFieldByName(systemName).getValueAsObject());
            }
        }

        newTimeTermsBO.save();

        return newTimeTermsBO;
     }

     public static IBusinessObject createManHourTerms(IScheduledTaskContext aContext, int templatePK, int primaryKey) throws BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject newManHourTerms = aContext.getDataService().create("ManHourTerms");
        IBusinessObject template = aContext.getDataService().getByPrimaryKey("ManHourTerms", templatePK);

        Iterator<IFieldDefinition> fieldIterator = template.getBODefinition().getFieldDefIterator();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        while (fieldIterator.hasNext()) {
            IFieldDefinition fieldDefinition = fieldIterator.next();

            if (fieldDefinition.isInUse() && !fieldDefinition.isReadOnly()) {
                String systemName = fieldDefinition.getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newManHourTerms.getStringFieldByName(systemName).setValue(template.getStringFieldByName("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    newManHourTerms.getDateNeutralFieldByName(systemName).setValue(date);
                    continue;
                }
                if ("ServiceAgreementRef".equals(systemName)) {
                    newManHourTerms.getReferenceFieldByName(systemName).setValueAsInteger(primaryKey);
                    continue;
                }
                newManHourTerms.getFieldByName(systemName).setValueAsObject(template.getFieldByName(systemName).getValueAsObject());
            }
        }

        newManHourTerms.save();

        tradeQuery = aContext.getDataService().getPVDatabaseQuery("TradeQuery");
        tradeQuery.getSearchExpression("ManHourTermRef", Operator.EQUAL).setValue(template.getPrimaryKey());
        tradeQueryResults = tradeQuery.execute();
        while (tradeQueryResults.next()) {
            IBusinessObject manHourTermMToNTrade = aContext.getDataService().create("ManHourTermMToNTrade");
            manHourTermMToNTrade.getReferenceFieldByName("ManHourTermRef").setValueAsInteger(newManHourTerms.getPrimaryKey()-1);
            manHourTermMToNTrade.getReferenceFieldByName("TradeRef").setValueAsInteger(tradeQueryResults.getReference("TradeRef"));
            manHourTermMToNTrade.getDateNeutralFieldByName("BeginDate").setValue(date);
            try {
                manHourTermMToNTrade.save();
            } catch (Exception e) {
                System.out.println("Error in ManHourTermMToNTrade!");
            }
        }
        
        return newManHourTerms;
     }

     public static IBusinessObject createSubContractorTerms(IScheduledTaskContext aContext, int templatePK, int primaryKey) throws BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject newSubContractorTerms = aContext.getDataService().create("SubContractorTerms");
        IBusinessObject template = aContext.getDataService().getByPrimaryKey("SubContractorTerms", templatePK);

        Iterator<IFieldDefinition> fieldIterator = template.getBODefinition().getFieldDefIterator();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        while (fieldIterator.hasNext()) {
            IFieldDefinition fieldDefinition = fieldIterator.next();

            if (fieldDefinition.isInUse() && !fieldDefinition.isReadOnly()) {
                String systemName = fieldDefinition.getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newSubContractorTerms.getStringFieldByName(systemName).setValue(template.getStringFieldByName("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    newSubContractorTerms.getDateNeutralFieldByName(systemName).setValue(date);
                    continue;
                }
                if ("ServiceAgreementRef".equals(systemName)) {
                    newSubContractorTerms.getReferenceFieldByName(systemName).setValueAsInteger(primaryKey);
                    continue;
                }
                newSubContractorTerms.getFieldByName(systemName).setValueAsObject(template.getFieldByName(systemName).getValueAsObject());
            }
        }

        newSubContractorTerms.save();

        return newSubContractorTerms;
     }

     public static IBusinessObject createMaterialTerms(IScheduledTaskContext aContext, int templatePK, int primaryKey) throws BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject newMaterialTerms = aContext.getDataService().create("MaterialTerms");
        IBusinessObject template = aContext.getDataService().getByPrimaryKey("MaterialTerms", templatePK);

        Iterator<IFieldDefinition> fieldIterator = template.getBODefinition().getFieldDefIterator();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        while (fieldIterator.hasNext()) {
            IFieldDefinition fieldDefinition = fieldIterator.next();

            if (fieldDefinition.isInUse() && !fieldDefinition.isReadOnly()) {
                String systemName = fieldDefinition.getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newMaterialTerms.getStringFieldByName(systemName).setValue(template.getStringFieldByName("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    newMaterialTerms.getDateNeutralFieldByName(systemName).setValue(date);
                    continue;
                }
                if ("ServiceAgreementRef".equals(systemName)) {
                    newMaterialTerms.getReferenceFieldByName(systemName).setValueAsInteger(primaryKey);
                    continue;
                }
                newMaterialTerms.getFieldByName(systemName).setValueAsObject(template.getFieldByName(systemName).getValueAsObject());
            }
        }

        newMaterialTerms.save();

        return newMaterialTerms;
     }

     public static IBusinessObject createTravelTerms(IScheduledTaskContext aContext, int templatePK, int primaryKey) throws BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject newTravelTerms = aContext.getDataService().create("TravelTerms");
        IBusinessObject template = aContext.getDataService().getByPrimaryKey("TravelTerms", templatePK);

        Iterator<IFieldDefinition> fieldIterator = template.getBODefinition().getFieldDefIterator();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        while (fieldIterator.hasNext()) {
            IFieldDefinition fieldDefinition = fieldIterator.next();

            if (fieldDefinition.isInUse() && !fieldDefinition.isReadOnly()) {
                String systemName = fieldDefinition.getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newTravelTerms.getStringFieldByName(systemName).setValue(template.getStringFieldByName("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    newTravelTerms.getDateNeutralFieldByName(systemName).setValue(date);
                    continue;
                }
                if ("ServiceAgreementRef".equals(systemName)) {
                    newTravelTerms.getReferenceFieldByName(systemName).setValueAsInteger(primaryKey);
                    continue;
                }
                newTravelTerms.getFieldByName(systemName).setValueAsObject(template.getFieldByName(systemName).getValueAsObject());
            }
        }

        newTravelTerms.save();

        return newTravelTerms;
     }

     public static IBusinessObject createTravelTermMethod(IScheduledTaskContext aContext, int templatePK, int primaryKey) throws BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject newTravelTermMethod = aContext.getDataService().create("TravelTermMethod");
        IBusinessObject template = aContext.getDataService().getByPrimaryKey("TravelTermMethod", templatePK);

        Iterator<IFieldDefinition> fieldIterator = template.getBODefinition().getFieldDefIterator();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        while (fieldIterator.hasNext()) {
            IFieldDefinition fieldDefinition = fieldIterator.next();

            if (fieldDefinition.isInUse() && !fieldDefinition.isReadOnly()) {
                String systemName = fieldDefinition.getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newTravelTermMethod.getStringFieldByName(systemName).setValue(template.getStringFieldByName("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    newTravelTermMethod.getDateNeutralFieldByName(systemName).setValue(date);
                    continue;
                }
                if ("ServiceAgreementRef".equals(systemName)) {
                    newTravelTermMethod.getReferenceFieldByName(systemName).setValueAsInteger(primaryKey);
                    continue;
                }
                newTravelTermMethod.getFieldByName(systemName).setValueAsObject(template.getFieldByName(systemName).getValueAsObject());
            }
        }

        newTravelTermMethod.save();

        return newTravelTermMethod;
	 }
}