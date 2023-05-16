package com.byes.paap.scheduledtasks.queries;

import com.planonsoftware.platform.backend.querybuilder.v3.IQueryBuilder;
import com.planonsoftware.platform.backend.querybuilder.v3.IQueryDefinition;
import com.planonsoftware.platform.backend.querybuilder.v3.IQueryDefinitionContext;

public class ConditionRecordQuery implements IQueryDefinition
{
    @Override
	public void create(IQueryBuilder aBuilder, IQueryDefinitionContext aContext) {
        aBuilder.addSelectField("Syscode");
        aBuilder.addSearchField("Name");
        aBuilder.addSearchField("Syscode");
	}

	@Override
	public String getBOName() {
		return "UsrConditionRecord";
	}

}