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

/*
 * AnnotationHandler.java
 *
 * Created on October 2, 2006, 12:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.annotation.handler;

import java.util.List;
import java.util.jar.JarFile;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.compapp.javaee.codegen.model.Endpoint;

/**
 *
 * @author gpatil
 */
public interface AnnotationHandler {
    // Annotation class names
    public static String ANNO_WEB_SERVICE = "Ljavax/jws/WebService;" ;  // No I18N    
    public static String ANNO_WEBSERVICE_CLIENT = "Ljavax/xml/ws/WebServiceClient;" ;  // No I18N
    public static String ANNO_WEB_ENDPOINT = "Ljavax/xml/ws/WebEndpoint;" ;  // No I18N
    public static String ANNO_WEB_SERVICE_REF = "Ljavax/xml/ws/WebServiceRef;" ;  // No I18N
    
    // Annotation property names
    public static String PROP_TNS = "targetNamespace" ;  // No I18N
    public static String PROP_NAME = "name" ;  // No I18N   
    public static String PROP_SVC_NAME = "serviceName" ; // No I18N
    public static String PROP_PORT_NAME = "portName" ; // No I18N
    
    public String getAnnotationClassConstant();
    public List<Endpoint> getEndPoints();
    public void resetEndPoints();
    public void handle(JarClassFileLoader cl, ClassFile theClass);
}
