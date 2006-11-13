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
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation;
import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 *
 * @author Martin Adamek
 */
public class WebInjectionTargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    public WebInjectionTargetQueryImplementation() {
    }

    public boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        if (controller == null || typeElement==null) {
            throw new NullPointerException("Passed null to WebInjectionTargetQueryImplementation.isInjectionTarget(CompilationController, TypeElement)"); // NOI18N
        }
        
        boolean ret = false;
        WebModule webModule = WebModule.getWebModule(controller.getFileObject());
        if (webModule != null &&
                !webModule.getJ2eePlatformVersion().equals("1.3") && // NOI18N
                !webModule.getJ2eePlatformVersion().equals("1.4")) { // NOI18N
            
            Elements elements = controller.getElements();
            TypeElement servletElement = elements.getTypeElement("javax.servlet.Servlet"); //NOI18N
            if (servletElement!=null) {
                ret = controller.getTypes().isSubtype(typeElement.asType(),servletElement.asType());
            }
         }
        return ret;
    }
    
    public boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        return false;
    }

}
