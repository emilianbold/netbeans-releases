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
package org.netbeans.modules.websvc.editor.hints.rules;

/**
 *
 * @author Ajit.Bhate@Sun.com
 */
public interface WebServiceAnnotations {

    public static final String ANNOTATION_WEBSERVICE = "javax.jws.WebService";
    public static final String ANNOTATION_WEBMETHOD = "javax.jws.WebMethod";
    public static final String ANNOTATION_WEBPARAM = "javax.jws.WebParam";
    public static final String ANNOTATION_WEBRESULT = "javax.jws.WebResult";
    public static final String ANNOTATION_ONEWAY = "javax.jws.Oneway";
    public static final String ANNOTATION_HANDLERCHAIN = "javax.jws.HandlerChain";
    public static final String ANNOTATION_SOAPMESSAGEHANDLERS = "javax.jws.soap.SOAPMessageHandlers";
    public static final String ANNOTATION_INITPARAM = "javax.jws.soap.InitParam";
    public static final String ANNOTATION_SOAPBINDING = "javax.jws.soap.SOAPBinding";
    public static final String ANNOTATION_SOAPMESSAGEHANDLER = "javax.jws.soap.SOAPMessageHandler";

    public static final String ANNOTATION_ATTRIBUTE_SERVICE_NAME = "serviceName";
    public static final String ANNOTATION_ATTRIBUTE_SEI = "endpointInterface";
    public static final String ANNOTATION_ATTRIBUTE_PORTNAME = "portName";
    public static final String ANNOTATION_ATTRIBUTE_WSDLLOCATION = "wsdlLocation";
    public static final String ANNOTATION_ATTRIBUTE_MODE = "mode";
    public static final String ANNOTATION_ATTRIBUTE_NAME = "name";
    public static final String ANNOTATION_ATTRIBUTE_TARGETNAMESPACE = "targetNamespace";
    public static final String ANNOTATION_ATTRIBUTE_EXCLUDE = "exclude";
    public static final String ANNOTATION_ATTRIBUTE_STYLE = "style";
    public static final String ANNOTATION_ATTRIBUTE_USE = "use";
    public static final String ANNOTATION_ATTRIBUTE_PARAMETERSTYLE = "parameterStyle";
}
