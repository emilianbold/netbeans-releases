package org.netbeans.modules.sql.project.anttasks;

import javax.xml.namespace.QName;

public class SQLMapEntry {

	public static final String REQUEST_REPLY_SERVICE = "requestReplyService";

	public static final String OPERATION_TAG = "operation";

	public static final String PORTTYPE_TAG = "portType";

	public static final String PARTNERLINK_TAG = "partnerLink";

	public static final String WSDL_FILE_TAG = "wsdlfile";
    
    public static final String SQL_FILE_TAG = "sqlfile";
    
	public static final String TYPE_TAG = "type";

	public static final String SQLMAP_TAG = "sql";

	private String partnerLink;

	private String portType;

	private String operation;

	private String sqlfileName;
    
    private String wsdlfileName;

	private String type;

	private String roleName;

	public SQLMapEntry(String partnerLink, String portType, 
                       String operation, String sqlfileName,
                       String wsdlFileName,
			String type) {
		super();
		this.partnerLink = partnerLink;
		this.portType = portType;
		this.operation = operation;
		this.sqlfileName = sqlfileName;
        this.wsdlfileName = wsdlFileName;
		this.type = type;
		this.roleName = wsdlFileName.substring(0, wsdlFileName.indexOf(".wsdl")) + "_myrole";
	}

	/**
	 * @return the fileName
	 */
	public String getSQLFileName() {
		return sqlfileName;
	}

    /**
     * @return the fileName
     */
    public String getWSDLFileName() {
        return wsdlfileName;
    }
    
	/**
	 * @return the operation
	 */
	public String getOperation() {
		return operation;
	}

	/**
	 * @return the partnerLink
	 */
	public QName getPartnerLink() {
		return QName.valueOf(partnerLink);

	}

	/**
	 * @return the portType
	 */
	public QName getPortType() {
		return QName.valueOf(portType);
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the roleName
	 */
	public String getRoleName() {
		return roleName;
	}

}
