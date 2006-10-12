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

import java.io.IOException;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.ErrorManager;

/**
 *
 * @author jungi
 */
public class AppClientInjectionTargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    public AppClientInjectionTargetQueryImplementation() {
    }
    
    public boolean isInjectionTarget(JavaClass jc) {
        if (jc == null) {
            throw new NullPointerException("Passed null to EjbInjectionTargetQueryImplementation.isInjectionTarget(JavaClass)"); // NOI18N
        }
        Car apiCar = Car.getCar(JavaModel.getFileObject(jc.getResource()));
        if (apiCar != null && 
                !apiCar.getJ2eePlatformVersion().equals("1.3") && 
                !apiCar.getJ2eePlatformVersion().equals("1.4")) {
            JavaModel.getJavaRepository().beginTrans(false);
            try {
                return jc.getResource().getMain().contains(jc);
            } finally {
                JavaModel.getJavaRepository().endTrans();
            }
        }
        return false;
    }

    public boolean isStaticReferenceRequired(JavaClass jc) {
        // all injection references must be static in appclient
        return isInjectionTarget(jc);
    }
}
