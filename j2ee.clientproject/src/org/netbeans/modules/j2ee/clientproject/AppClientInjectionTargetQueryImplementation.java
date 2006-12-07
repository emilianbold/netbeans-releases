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

package org.netbeans.modules.j2ee.clientproject;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation;

/**
 *
 * @author jungi
 */
public class AppClientInjectionTargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    public AppClientInjectionTargetQueryImplementation() {
    }
    
    public boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        Car apiCar = Car.getCar(controller.getFileObject());
        if (apiCar != null && 
                !apiCar.getJ2eePlatformVersion().equals("1.3") && 
                !apiCar.getJ2eePlatformVersion().equals("1.4")) {
            return SourceUtils.isMainClass(typeElement.getQualifiedName().toString(), controller.getClasspathInfo());
        }
        return false;
    }

    public boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        // all injection references must be static in appclient
        return isInjectionTarget(controller, typeElement);
    }
}
