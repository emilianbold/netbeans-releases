/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    public void handle(ClassFileLoader cl, ClassFile theClass);
}
