package org.netbeans.modules.xml.wsdl.ui.view;

import javax.swing.JTextField;

public interface OperationConfiguration {

    public static final java.lang.String PROP_ERROR_MESSAGE = "PROP_ERROR_MESSAGE";

    public java.lang.String getOperationName();

    public void setOperationName(java.lang.String operationName);

    public org.netbeans.modules.xml.wsdl.ui.view.OperationType getOperationType();

    public java.util.List<org.netbeans.modules.xml.wsdl.ui.view.PartAndElementOrTypeTableModel.PartAndElementOrType> getInputMessageParts();

    public java.util.List<org.netbeans.modules.xml.wsdl.ui.view.PartAndElementOrTypeTableModel.PartAndElementOrType> getOutputMessageParts();

    public java.util.List<org.netbeans.modules.xml.wsdl.ui.view.PartAndElementOrTypeTableModel.PartAndElementOrType> getFaultMessageParts();

    public javax.swing.JTextField getOperationNameTextField();

    public javax.swing.JComboBox getOperationTypeComboBox();

    public boolean isNewOutputMessage();

    public boolean isNewInputMessage();

    public boolean isNewFaultMessage();

    public java.lang.String getOutputMessageName();

    public java.lang.String getInputMessageName();

    public java.lang.String getFaultMessageName();

    public String getPortTypeName();

    public void setPortTypeName(String portTypeName);

    public JTextField getPortTypeNameTextField();
    
    public boolean isAutoGeneratePartnerLinkType();
    
    public void setAutoGeneratePartnerLinkType(boolean autoGenPartnerLinkType);
}
