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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * IConstants.java
 *
 * Created on January 10, 2006, 10:42 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.impl;

/**
 *
 * @author girix
 */
public interface IConstants {
    public static final String XPATH_SCHEMA_IMPORT_LOCATION = "//xsd:schema/xsd:import/@schemaLocation"; //NOI18N
    public static final String XPATH_SCHEMA_INCLUDE_LOCATION = "//xsd:schema/xsd:include/@schemaLocation"; //NOI18N
    public static final String XPATH_SCHEMA_REDEFINE_LOCATION = "//xsd:schema/xsd:redefine/@schemaLocation" ; //NOI18N
    public static final String XPATH_WSDL_IMPORT_LOCATION = "//wsdl:definitions/wsdl:import/@location"; //NOI18N
    
    public static final String XPATH_WSDL_TAG = "/wsdl:definitions"; //NOI18N
    public static final String XPATH_SCHEMA_TAG = "/xsd:schema"; //NOI18N     
}
