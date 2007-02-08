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

package org.netbeans.modules.refactoring.java.ui;

import org.netbeans.modules.refactoring.java.spi.ui.JavaActionsImplementationProvider;
import org.openide.util.Lookup;

/**
 * @author Jan Becicka
 */
public final class JavaActionsImplementationFactory {
    
    private JavaActionsImplementationFactory(){}
    
    private static final Lookup.Result<JavaActionsImplementationProvider> implementations =
        Lookup.getDefault().lookup(new Lookup.Template(JavaActionsImplementationProvider.class));

    public static boolean canChangeParameters(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canChangeParameters(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static Runnable changeParametersImpl(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canChangeParameters(lookup)) {
                return rafi.changeParametersImpl(lookup);
            }
        }
        return null;
    }

    public static boolean canEncapsulateFields(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canEncapsulateFields(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static Runnable encapsulateFieldsImpl(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canEncapsulateFields(lookup)) {
                return rafi.encapsulateFieldsImpl(lookup);
            }
        }
        return null;
    }
    public static boolean canExtractInterface(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canExtractInterface(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static Runnable extractInterfaceImpl(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canExtractInterface(lookup)) {
                return rafi.extractInterfaceImpl(lookup);
            }
        }
        return null;
    }
    
    public static Runnable extractSuperClassImpl(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canExtractSuperClass(lookup)) {
                return rafi.extractSuperClassImpl(lookup);
            }
        }
        return null;
    }
    
    public static boolean canExtractSuperClass(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canExtractSuperClass(lookup)) {
                return true;
            }
        }
        return false;
    }

    public static Runnable innerToOuterImpl(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canInnerToOuter(lookup)) {
                return rafi.innerToOuterImpl(lookup);
            }
        }
        return null;
    }
    
    public static boolean canInnerToOuter(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canInnerToOuter(lookup)) {
                return true;
            }
        }
        return false;
    }

    public static Runnable pullUpImpl(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canPullUp(lookup)) {
                return rafi.pullUpImpl(lookup);
            }
        }
        return null;
    }
    
    public static boolean canPullUp(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canPullUp(lookup)) {
                return true;
            }
        }
        return false;
    }

    public static Runnable pushDownImpl(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canPushDown(lookup)) {
                return rafi.pushDownImpl(lookup);
            }
        }
        return null;
    }
    
    public static boolean canPushDown(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canPushDown(lookup)) {
                return true;
            }
        }
        return false;
    }

    public static Runnable useSuperTypeImpl(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canUseSuperType(lookup)) {
                return rafi.useSuperTypeImpl(lookup);
            }
        }
        return null;
    }
    
    public static boolean canUseSuperType(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canUseSuperType(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    
}
