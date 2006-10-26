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

package org.netbeans.modules.web.core;

//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.modules.j2ee.common.JMIUtils;
//import org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation;
//import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 *
 * @author Martin Adamek
 */
public class WebInjectionTargetQueryImplementation /*implements InjectionTargetQueryImplementation*/ {
    
    public WebInjectionTargetQueryImplementation() {
    }
    
//    public boolean isInjectionTarget(JavaClass jc) {
//        if (jc == null) {
//            throw new NullPointerException("Passed null to WebInjectionTargetQueryImplementation.isInjectionTarget(JavaClass)"); // NOI18N
//        }
//        WebModule webModule = WebModule.getWebModule(JavaModel.getFileObject(jc.getResource()));
//        if (webModule != null &&
//                !webModule.getJ2eePlatformVersion().equals("1.3") &&
//                !webModule.getJ2eePlatformVersion().equals("1.4")) {
//            return jc.isSubTypeOf(JMIUtils.findClass("javax.servlet.Servlet"));
//        }
//        return false;
//    }
//
//    public boolean isStaticReferenceRequired(JavaClass jc) {
//        return false;
//    }
    
}
