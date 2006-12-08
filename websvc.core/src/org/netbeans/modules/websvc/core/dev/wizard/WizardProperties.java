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

package org.netbeans.modules.websvc.core.dev.wizard;

public class WizardProperties {
    public static final String WEB_SERVICE_TYPE = "webServiceType"; // NOI18N
    public static final String DELEGATE_TO_SESSION_BEAN = "delegateToSessionBean"; // NOI18N
    public static final String WSDL_FILE_PATH = "wsdlFilePath"; //NOI18N
    
    public static final int FROM_SCRATCH = 0;
    public static final int ENCAPSULATE_SESSION_BEAN = 1;
//convert Java class not implemented for 5.5 release
//    public static final int CONVERT_JAVA_CLASS = 2;

    public static final String WSDL_MODEL = "wsdlModel"; //NOI18N
    public static final String WSDL_MODELER = "wsdlModeler"; //NOI18N
    public static final String WSDL_SERVICE = "wsdlService"; //NOI18N
    public static final String WSDL_PORT = "wsdlPort"; //NOI18N
    public static final String WSDL_SERVICE_HANDLER = "wsdlServiceHandler"; //NOI18N
}
