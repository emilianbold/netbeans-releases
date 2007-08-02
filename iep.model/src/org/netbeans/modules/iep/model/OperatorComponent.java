package org.netbeans.modules.iep.model;

import java.util.List;

public interface OperatorComponent extends Component {

	public static final String PROP_X = "x";
	
	public static final String PROP_Y = "y";
	
	public static final String PROP_Z = "z";
	
	public static final String PROP_ID = "id";
	
	public static final String PROP_DISPLAYNAME = "name";
	
	public static final String PROP_INPUT_SCHEMA_ID_LIST = "inputSchemaIdList";

	public static final String PROP_OUTPUT_SCHEMA_ID = "outputSchemaId";
	
	public static final String PROP_DESCRIPTION = "description";
	
	public static final String PROP_TOPOSCORE = "topoScore";

	public static final String PROP_INPUTTYPE = "inputType";
	
	public static final String PROP_INPUT_ID_LIST = "inputIdList";

	public static final String PROP_STATIC_INPUT_ID_LIST = "staticInputIdList";
	
	public static final String PROP_OUTPUTTYPE = "outputType";
	
	public static final String PROP_ISGLOBAL = "isGlobal";

	public static final String PROP_GLOBALID = "globalId";
	
	public static final String PROP_BATCHMODE = "batchMode";
	
	public static final String PROP_WHERECLAUSE = "whereClause";
	
	int getX();
	
	int getY();
	
	int getZ();
	
	String getId();
	
	String getDisplayName();
	
	List<SchemaComponent> getInputSchemaIdList();

	SchemaComponent getOutputSchemaId();
	
	String getDescription();
	
	String getTopoScore();
	
	OperatorType getInputType();
	
	OperatorType getOutputType();
	
	List<OperatorComponent> getInputOperatorList();
	
	List<OperatorComponent> getStaticInputTableList();
	
	public boolean isGlobal();
	
	public String getGlobalId();
	
	public boolean isBatchMode();
	
	public List<OperatorComponent> getFromColumnList();
	
	public List<Property> getToColumnList();
	
	public List<Property> getGroupByColumnList();
	
	public String getWhereClause();
	
}
