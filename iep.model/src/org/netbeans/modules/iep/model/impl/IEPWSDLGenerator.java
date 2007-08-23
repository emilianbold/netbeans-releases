package org.netbeans.modules.iep.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.InputOperatorComponent;
import org.netbeans.modules.iep.model.NameUtil;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.OperatorType;
import org.netbeans.modules.iep.model.OutputOperatorComponent;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.openide.util.NbBundle;

public class IEPWSDLGenerator {

    
    private static final String INPUT_PORT_TYPE = "InputPt";
    private static final String INPUT_PARTNER_LINK_TYPE = "InputPlt";
    private static final String INPUT_ROLE_NAME = "InputRn";
    private static final String INPUT_PARTNER_LINK = "InputPl";
    
   
//  ==========================================================================
    private static Map SQL_2_XSD_MAP = new HashMap();
    static {
        SQL_2_XSD_MAP.put("BIT", "xsd:boolean");
        SQL_2_XSD_MAP.put("TINYINT", "xsd:byte");
        SQL_2_XSD_MAP.put("SMALLINT", "xsd:short");
        SQL_2_XSD_MAP.put("INTEGER", "xsd:int");
        SQL_2_XSD_MAP.put("BIGINT", "xsd:long");
        SQL_2_XSD_MAP.put("REAL", "xsd:float");
        SQL_2_XSD_MAP.put("FLOAT", "xsd:double");
        SQL_2_XSD_MAP.put("DOUBLE", "xsd:double");
        SQL_2_XSD_MAP.put("DECIMAL", "xsd:decimal");
        SQL_2_XSD_MAP.put("NUMERIC", "xsd:decimal");
        SQL_2_XSD_MAP.put("CHAR", "xsd:string");
        SQL_2_XSD_MAP.put("VARCHAR", "xsd:string");
        SQL_2_XSD_MAP.put("LONGVARCHAR", "xsd:string");
        SQL_2_XSD_MAP.put("DATE", "xsd:date");
        SQL_2_XSD_MAP.put("TIME", "xsd:time");
        SQL_2_XSD_MAP.put("TIMESTAMP", "xsd:dateTime");
        SQL_2_XSD_MAP.put("BINARY", "xsd:base64Binary");
        SQL_2_XSD_MAP.put("VARBINARY", "xsd:base64Binary");
        SQL_2_XSD_MAP.put("LONGVARBINARY", "xsd:base64Binary");
        SQL_2_XSD_MAP.put("BLOB", "xsd:base64Binary");
        SQL_2_XSD_MAP.put("CLOB", "xsd:string");
//        "ARRAY",
//        "REF",
//        "STRUCT",
    };
    
	private IEPModel mModel;
	private OperatorComponentContainer mocContainer;
	
	
	public IEPWSDLGenerator(IEPModel model) {
		this.mModel = model;
		this.mocContainer = this.mModel.getPlanComponent().getOperatorComponentContainer();
		
	}
	
	private List getInputList() {
        List<InputOperatorComponent> list = new ArrayList<InputOperatorComponent>();
        	List<OperatorComponent> operators = mocContainer.getAllOperatorComponent();
        	Iterator<OperatorComponent> it = operators.iterator();
        	while(it.hasNext()) {
        		OperatorComponent oc = it.next();
        		if(oc instanceof InputOperatorComponent) {
        			InputOperatorComponent inOC = (InputOperatorComponent) oc;
        			if(inOC.isWebServiceInput()) {
        				list.add(inOC);
        			}
        		}
        		
        	}
        	
        return list;
    }
    
    private List getOutputList() {
    	List<OutputOperatorComponent> list = new ArrayList<OutputOperatorComponent>();
    	List<OperatorComponent> operators = mocContainer.getAllOperatorComponent();
    	Iterator<OperatorComponent> it = operators.iterator();
    	while(it.hasNext()) {
    		OperatorComponent oc = it.next();
    		if(oc instanceof OutputOperatorComponent) {
    			OutputOperatorComponent inOC = (OutputOperatorComponent) oc;
    			if(inOC.isWebServiceOutput()) {
    				list.add(inOC);
    			}
    		}
    		
    	}
    	
    	return list;
    }
    
	public String getWSDL(String tns) throws Exception {
        List inList = getInputList();
        List outList = getOutputList();
        
        StringBuffer sb = new StringBuffer();
        sb.append("<definitions targetNamespace=\"" + tns + "\"\n");
        sb.append("             xmlns:tns=\"" + tns + "\"\n");
        sb.append("             xmlns:typens=\"" + tns + "\"\n");
        sb.append("             xmlns:defns=\"" + tns + "\"\n");
        sb.append("             xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n");
        sb.append("             xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"\n");
        sb.append("             xmlns:file=\"http://schemas.sun.com/jbi/wsdl-extensions/file/\"\n");
        sb.append("             xmlns=\"http://schemas.xmlsoap.org/wsdl/\">\n");
        // types
        sb.append("<types>\n");
        sb.append("    <xsd:schema targetNamespace=\"" + tns + "\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n");
        // input types
        for (int i = 0, I = inList.size(); i < I; i++) {
            InputOperatorComponent op = (InputOperatorComponent)inList.get(i);
            String name = op.getDisplayName();
            name = NameUtil.makeJavaId(name);
            SchemaComponent outputSchema = op.getOutputSchemaId();
            
            if (outputSchema == null) {
                throw new Exception(NbBundle.getMessage(IEPWSDLGenerator.class,"PlanImpl.Input") + " " 
                        + name 
                        + " " + NbBundle.getMessage(IEPWSDLGenerator.class,"PlanImpl_schema_is_not_defined"));
            }
            
            // input_MsgObj
            sb.append("        <xsd:element name=\"" + name + "_MsgObj\">\n");
            sb.append("            <xsd:complexType>\n");
            sb.append("                <xsd:sequence>\n");
            if (outputSchema != null) {
            	List<SchemaAttribute> attrs =  outputSchema.getSchemaAttributes();
            	Iterator<SchemaAttribute> it = attrs.iterator();
            	while(it.hasNext()) {
            		SchemaAttribute sa = it.next();
            		String cName = sa.getAttributeName();
                    String cType = sa.getAttributeType();
                    String xsdType = (String)SQL_2_XSD_MAP.get(cType);
                    sb.append("                    <xsd:element name=\"" + cName + "\" type=\"" + xsdType + "\"/>\n");
            	}
                
            }
            sb.append("                </xsd:sequence>\n");
            sb.append("            </xsd:complexType>\n");
            sb.append("        </xsd:element>\n");
            
            // inputBatch_MsgObj
            sb.append("        <xsd:element name=\"" + name + "Batch_MsgObj\">\n");
            sb.append("            <xsd:complexType>\n");
            sb.append("                <xsd:sequence>\n");
            sb.append("                    <xsd:element name=\"" + name + "_MsgObj\" minOccurs=\"0\" maxOccurs=\"unbounded\">\n");
            sb.append("                        <xsd:complexType>\n");
            sb.append("                            <xsd:sequence>\n");
            
            if (outputSchema != null) {
            	List<SchemaAttribute> attrs =  outputSchema.getSchemaAttributes();
            	Iterator<SchemaAttribute> it = attrs.iterator();
            	while(it.hasNext()) {
            		SchemaAttribute sa = it.next();
            		String cName = sa.getAttributeName();
                    String cType = sa.getAttributeType();
                    String xsdType = (String)SQL_2_XSD_MAP.get(cType);
                    sb.append("                    <xsd:element name=\"" + cName + "\" type=\"" + xsdType + "\"/>\n");
            	}
                
            }
            sb.append("                            </xsd:sequence>\n");
            sb.append("                        </xsd:complexType>\n");
            sb.append("                    </xsd:element>\n");
            sb.append("                </xsd:sequence>\n");
            sb.append("            </xsd:complexType>\n");
            sb.append("        </xsd:element>\n");
        }
        // output_MsgObj
        for (int i = 0, I = outList.size(); i < I; i++) {
            OutputOperatorComponent op = (OutputOperatorComponent)outList.get(i);
            String name = op.getDisplayName();
            name = NameUtil.makeJavaId(name);
            boolean isRelation = op.getProperty(OperatorComponent.PROP_INPUTTYPE).getValue().equals(OperatorType.OPERATOR_RELATION.toString());
            SchemaComponent outputSchema = op.getOutputSchemaId();
            
            if (outputSchema == null) {
                throw new Exception(NbBundle.getMessage(IEPWSDLGenerator.class,"PlanImpl.Output")  
                        + " " + name + " " +  NbBundle.getMessage(IEPWSDLGenerator.class,"PlanImpl_schema_is_not_defined"));
            }
            
            boolean includeTimestamp = op.isIncludeTimestamp();
            boolean batchMode = op.isBatchMode();
            String indent = batchMode? "            " : "";
            if (batchMode) {
                sb.append("        <xsd:element name=\"" + name + "Batch_MsgObj\">\n");
                sb.append("            <xsd:complexType>\n");
                sb.append("                <xsd:sequence>\n");
                sb.append("                    <xsd:element name=\"" + name + "_MsgObj\" minOccurs=\"0\" maxOccurs=\"unbounded\">\n");
            } else {
                sb.append("        <xsd:element name=\"" + name + "_MsgObj\">\n");
            }
            sb.append(indent + "            <xsd:complexType>\n");
            sb.append(indent + "                <xsd:sequence>\n");
            if (outputSchema != null) {
            	
            	List<SchemaAttribute> attrs =  outputSchema.getSchemaAttributes();
            	Iterator<SchemaAttribute> it = attrs.iterator();
            	while(it.hasNext()) {
            		SchemaAttribute sa = it.next();
            		String cName = sa.getAttributeName();
                    String cType = sa.getAttributeType();
                    String xsdType = (String)SQL_2_XSD_MAP.get(cType);
                    sb.append(indent + "                   <xsd:element name=\"" + cName + "\" type=\"" + xsdType + "\"/>\n");
                    
            	}
                
            }
            if (isRelation) {
                sb.append(indent + "                   <xsd:element name=\"SeqId\" type=\"xsd:string\"/>\n");
            }
            if (includeTimestamp) {
                sb.append(indent + "                   <xsd:element name=\"Timestamp\" type=\"xsd:dateTime\"/>\n");
            }
            if (isRelation) {
                sb.append(indent + "                   <xsd:element name=\"Tag\" type=\"xsd:string\"/>\n");
            }
            sb.append(indent + "                </xsd:sequence>\n");
            sb.append(indent + "            </xsd:complexType>\n");
            sb.append(indent + "        </xsd:element>\n");
            if (batchMode) {
                sb.append("                </xsd:sequence>\n");
                sb.append("            </xsd:complexType>\n");
                sb.append("        </xsd:element>\n");
            }
        }
        
        sb.append("    </xsd:schema>\n");
        sb.append("</types>\n");
        sb.append("\n");
        // message
        for (int i = 0, I = inList.size(); i < I; i++) {
            InputOperatorComponent op = (InputOperatorComponent)inList.get(i);
            String name = op.getDisplayName();
            name = NameUtil.makeJavaId(name);
            sb.append("<message name=\"" + name + "_Msg\">\n");
            sb.append("    <part name=\"input\" element=\"typens:" + name + "_MsgObj\"/>\n");
            sb.append("</message>\n");
            
            sb.append("<message name=\"" + name + "Batch_Msg\">\n");
            sb.append("    <part name=\"input\" element=\"typens:" + name + "Batch_MsgObj\"/>\n");
            sb.append("</message>\n");
        }
        
        for (int i = 0, I = outList.size(); i < I; i++) {
            OutputOperatorComponent op = (OutputOperatorComponent)outList.get(i);
            boolean batchMode = op.isBatchMode();
            String name = op.getDisplayName();
            name = NameUtil.makeJavaId(name);
            if (batchMode) {
                sb.append("<message name=\"" + name + "Batch_Msg\">\n");
                sb.append("    <part name=\"output\" element=\"typens:" + name + "Batch_MsgObj\"/>\n");
                sb.append("</message>\n");
            } else {
                sb.append("<message name=\"" + name + "_Msg\">\n");
                sb.append("    <part name=\"output\" element=\"typens:" + name + "_MsgObj\"/>\n");
                sb.append("</message>\n");
            }
        }
        
        sb.append("\n");
        //portType
        sb.append("<portType name=\"" + INPUT_PORT_TYPE + "\">\n");
        for (int i = 0, I = inList.size(); i < I; i++) {
            InputOperatorComponent op = (InputOperatorComponent)inList.get(i);
            String name = op.getDisplayName();
            name = NameUtil.makeJavaId(name);
            sb.append("    <operation name=\"" + name + "\">\n");
            sb.append("        <input message=\"tns:" + name + "_Msg\"/>\n");
            sb.append("    </operation>\n");
            sb.append("    <operation name=\"" + name + "Batch\">\n");
            sb.append("        <input message=\"tns:" + name + "Batch_Msg\"/>\n");
            sb.append("    </operation>\n");
        }
        sb.append("</portType>\n");
        
        for (int i = 0, I = outList.size(); i < I; i++) {
            OutputOperatorComponent op = (OutputOperatorComponent)outList.get(i);
            boolean batchMode = op.isBatchMode();
            String name = op.getDisplayName();
            name = NameUtil.makeJavaId(name);
            sb.append("<portType name=\"" + getOutputPortType(name) + "\">\n");
            sb.append("    <operation name=\"" + name + "\">\n");
            if (batchMode) {
                sb.append("        <input message=\"tns:" + name + "Batch_Msg\"/>\n");
            } else {
                sb.append("        <input message=\"tns:" + name + "_Msg\"/>\n");
            }
            sb.append("    </operation>\n");
            sb.append("</portType>\n");
        }
        
        sb.append("\n");
        
        // partnerLinkType
        sb.append("<plnk:partnerLinkType name=\"" + INPUT_PARTNER_LINK_TYPE + "\" xmlns:plnk=\"http://docs.oasis-open.org/wsbpel/2.0/plnktype\">\n");
        sb.append("    <plnk:role name = \"" + INPUT_ROLE_NAME + "\" portType=\"tns:" + INPUT_PORT_TYPE + "\"/>\n");
        sb.append("</plnk:partnerLinkType>\n");
        for (int i = 0, I = outList.size(); i < I; i++) {
            OutputOperatorComponent op = (OutputOperatorComponent)outList.get(i);
            String name = op.getDisplayName();
            name = NameUtil.makeJavaId(name);
            sb.append("<plnk:partnerLinkType name=\"" + getOutputPartnerLinkType(name) + "\" xmlns:plnk=\"http://docs.oasis-open.org/wsbpel/2.0/plnktype\">\n");
            sb.append("    <plnk:role name = \"" + getOutputRoleName(name) + "\" portType=\"tns:" + getOutputPortType(name) + "\"/>\n");
            sb.append("</plnk:partnerLinkType>\n");
        }
        // input binding
        sb.append("<!-- input binding -->\n");
        sb.append("<binding name=\"InputBinding\" type=\"defns:InputPt\">\n");
        sb.append("    <soap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>\n");
        for (int i = 0, I = inList.size(); i < I; i++) {
            InputOperatorComponent op = (InputOperatorComponent)inList.get(i);
            String name = op.getDisplayName();
            name = NameUtil.makeJavaId(name);
            sb.append("    <operation name=\"" + name + "\">\n");
            sb.append("        <soap:operation soapAction=\"" + name + "\"/>\n");
            sb.append("        <input>\n");
            sb.append("            <soap:body use=\"literal\"/>\n");
            sb.append("        </input>\n");
            sb.append("    </operation>\n");
            
            sb.append("    <operation name=\"" + name + "Batch\">\n");
            sb.append("        <soap:operation soapAction=\"" + name + "Batch\"/>\n");
            sb.append("        <input>\n");
            sb.append("            <soap:body use=\"literal\"/>\n");
            sb.append("        </input>\n");
            sb.append("    </operation>\n");
        }
        sb.append("</binding>\n");
        
        // input service
        sb.append("<!-- input service -->\n");
        sb.append("<service name=\"InputService\">\n");
        sb.append("    <port name=\"InputPort\" binding=\"tns:InputBinding\">\n");
        sb.append("        <soap:address location=\"http://localhost:12100/service/" + tns + "\"/>\n");
        sb.append("    </port>\n");
        sb.append("</service>\n");

        // http output binding and service
        if (outList.size() > 0) {
            sb.append("\n<!-- http output binding and service\n");
        }
        for (int i = 0, I = outList.size(); i < I; i++) {
            OutputOperatorComponent op = (OutputOperatorComponent)outList.get(i);
            String name = op.getDisplayName();
            name = NameUtil.makeJavaId(name);
            sb.append("<binding name=\"OutputBinding_" + name + "\" type=\"defns:OutputPt_" + name + "\">\n");
            sb.append("    <operation name=\"" + name + "\">\n");
            sb.append("        <soap:operation soapAction=\"" + name + "\"/>\n");
            sb.append("        <input>\n");
            sb.append("            <soap:body use=\"literal\"/>\n");
            sb.append("        </input>\n");
            sb.append("    </operation>\n");
            sb.append("</binding>\n");
        }
        for (int i = 0, I = outList.size(); i < I; i++) {
        	OutputOperatorComponent op = (OutputOperatorComponent)outList.get(i);
            String name = op.getDisplayName();
            name = NameUtil.makeJavaId(name);
            String servceName = tns + "_" + name + "_callee";
            sb.append("<service name=\"OutputService_" + name + "\">\n");
            sb.append("    <port name=\"OutputPort_" + name + "\" binding=\"tns:OutputBinding_" + name + "\">\n");
            sb.append("        <soap:address location=\"http://localhost:12100/service/" + servceName + "\"/>\n");
            sb.append("    </port>\n");
            sb.append("</service>\n");
        }
        if (outList.size() > 0) {
            sb.append(" end of http output binding and service -->\n\n");
        }

        // file output binding and service
        if (outList.size() > 0) {
            sb.append("<!-- file output binding and service -->\n");
        }
        for (int i = 0, I = outList.size(); i < I; i++) {
        	OutputOperatorComponent op = (OutputOperatorComponent)outList.get(i);
            String name = op.getDisplayName();
            name = NameUtil.makeJavaId(name);
            sb.append("<binding name=\"OutputBinding_" + name + "\" type=\"defns:OutputPt_" + name + "\">\n");
            sb.append("    <file:binding/>\n");
            sb.append("    <operation name=\"" + name + "\">\n");
            sb.append("        <file:operation/>\n");
            sb.append("        <input>\n");
            sb.append("            <file:message fileName=\"" + name + ".txt\"\n");
            sb.append("                    fileNameIsPattern=\"false\"\n");
            sb.append("                    addEOL=\"false\"\n");
            sb.append("                    multipleRecordsPerFile=\"true\"\n");
            sb.append("                    use=\"literal\"/>\n");
            sb.append("        </input>\n");
            sb.append("    </operation>\n");
            sb.append("</binding>\n");
        }
        for (int i = 0, I = outList.size(); i < I; i++) {
        	OutputOperatorComponent op = (OutputOperatorComponent)outList.get(i);
            String name = op.getDisplayName();
            name = NameUtil.makeJavaId(name);
            String servceName = tns + "_" + name + "_callee";
            sb.append("<service name=\"OutputService_" + name + "\">\n");
            sb.append("    <port name=\"OutputPort_" + name + "\" binding=\"tns:OutputBinding_" + name + "\">\n");
            sb.append("        <file:address fileDirectory=\"C:/temp/" + tns + "\"/>\n");
            sb.append("    </port>\n");
            sb.append("</service>\n");
        }
        if (outList.size() > 0) {
            sb.append("<!-- end of file output binding and service -->\n");
        }
        sb.append("</definitions>\n");
        return sb.toString();
    }


    private static String getOutputPortType(String opJName) {
        return "OutputPt_" + opJName;
    }
    private static String getOutputPartnerLinkType(String opJName) {
        return "OutputPlt_" + opJName;
    }
    private static String getOutputRoleName(String opJName) {
        return "OutputRn_" + opJName;
    }
    private static String getOutputPartnerLink(String opJName) {
        return "OutputPl_" + opJName;
    }
    
    
}
