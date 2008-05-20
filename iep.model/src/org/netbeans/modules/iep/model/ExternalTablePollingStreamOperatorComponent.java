package org.netbeans.modules.iep.model;

public interface ExternalTablePollingStreamOperatorComponent extends OperatorComponent {

    public static String PROP_POLLING_INTERVAL = "pollingInterval";
    
    public static String PROP_POLLING_INTERVAL_TIME_UNIT = "pollingIntervalTimeUnit";
    
    public static String PROP_POLLING_RECORD_SIZE = "pollingRecordSize";
    
    public static String PROP_DATABASE_JNDI_NAME = "databaseJndiName";
    
//        public static String PROP_RECORD_IDENTIFIER_COLUMNS = "recordIdentifierColumns";
        
        public static String PROP_RECORD_IDENTIFIER_COLUMNS_SCHEMA = "recordIdentifierColumnsSchema";
        
                
        public static String PROP_IS_DELETE_RECORDS = "isDeleteRecords";
        
    public void setPollingInterval(String pollingInterval);
        
    public String getPollingInterval();
    
    public void setPollingIntervalTimeUnit(String pollingIntervalTimeUnit);
    
    public String getPollingIntervalTimeUnit();
    
    public void setPolllingIntervalRecordSize(String recordSize);
    
    public void getPolllingIntervalRecordSize();
    
    public void setDatabaseJndiName(String databaseJndiName);
    
    public String getDatabaseJndiName();
    
    
    
}
