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

package org.netbeans.modules.j2ee.ejbcore.api.ui;

import java.io.IOException;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddCmpFieldAction;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddFinderMethodStrategy;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddSelectMethodStrategy;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres.CallEjbDialog;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Pavel Buzek
 */
public final class CallEjb {
    
    public static boolean showCallEjbDialog(WorkingCopy workingCopy, TypeElement beanClass, String title) {
        try {
            return new CallEjbDialog().open(workingCopy, beanClass, title);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }
    }
    
    public static boolean addCmpField(TypeElement beanClass, FileObject ddFile) {
        return AddCmpFieldAction.addCmpField(beanClass, ddFile);
    }
    
    public static void addFinderMethod(FileObject fileObject, ElementHandle<TypeElement> beanClass) throws IOException {
        new AddFinderMethodStrategy().addMethod(fileObject, beanClass);
    }
    
    public static void addSelectMethod(FileObject fileObject, ElementHandle<TypeElement> beanClass) throws IOException {
        new AddSelectMethodStrategy().addMethod(fileObject, beanClass);
    }
}
