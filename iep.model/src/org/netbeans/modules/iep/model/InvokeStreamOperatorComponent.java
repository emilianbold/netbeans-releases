package org.netbeans.modules.iep.model;

public interface InvokeStreamOperatorComponent extends OperatorComponent {

    public static String PROP_EXTERNAL_IEP_PROCESS_QUALIFIED_NAME = "externalIepProcessQualifiedName";
    
    public static String PROP_EXTERNAL_OPERATOR_NAME = "externalOperatorName";
    
    public void setExternalIEPProcessQualifiedName(String name);
        
    public String getExternalIEPProcessQualifiedName();
    
    public void setExternalOperatorName(String name);
    
    public String getExternalOperatorName();
    
    public void setExternalOperator(IEPReference operator);
}
