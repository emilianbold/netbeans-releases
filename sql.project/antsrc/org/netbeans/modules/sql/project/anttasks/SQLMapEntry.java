/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

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
