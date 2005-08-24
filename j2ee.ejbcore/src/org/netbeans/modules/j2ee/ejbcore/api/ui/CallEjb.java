/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.api.ui;

import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddCmpFieldAction;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddFinderMethodStrategy;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddSelectMethodStrategy;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres.CallEjbDialog;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Pavel Buzek
 */
public final class CallEjb {
    
    public static boolean showCallEjbDialog (JavaClass beanClass, String title) {
        return new CallEjbDialog().open(beanClass, title);
    }
    
    public static boolean addCmpField(JavaClass beanClass, FileObject ddFile) {
        return AddCmpFieldAction.addCmpField(beanClass, ddFile);
    }
    
    public static void addFinderMethod (JavaClass beanClass) {
        new AddFinderMethodStrategy().addMethod(beanClass);
    }
    
    public static void addSelectMethod (JavaClass beanClass) {
        new AddSelectMethodStrategy().addMethod(beanClass);
    }
}
