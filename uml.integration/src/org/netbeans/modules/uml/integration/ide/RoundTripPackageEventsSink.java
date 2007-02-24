/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.integration.ide;

import java.util.Vector;

import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripPackageEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class RoundTripPackageEventsSink
    extends RoundTripClassEventsSink
    implements IRoundTripPackageEventsSink {

    IPackage rootPkg = null;

    public void onPrePackageChangeRequest(
            IChangeRequest newVal,
            IResultCell cell) {
        Log.out("onPrePackageChangeRequest called");
    }
    
    public void onPackageChangeRequest(
            IChangeRequest newVal,
            IResultCell cell) {
        Log.out("---------------------------onPackageChangeRequest called");
        ChangeUtils.say(newVal);
        firePackageChangeEvent(newVal, false);
    }

    protected void firePackageChangeEvent(
            IChangeRequest newVal,
            boolean beforeChange) {
        int changeType = newVal.getState();
        if (changeType == ChangeUtils.CT_DELETE) {
            IElement ielem = newVal.getBefore();
            rootPkg = (IPackage)  ielem;
            
            Vector filesToBeDeleted = new Vector();
            handlePackageDeleteEvent(ielem, filesToBeDeleted);

            // Specifically for WSAD - We need the qualified name of the
            // project that was deleted so that it can removed
            // from the WSAD project tree.
            fireBulkDeleteEvent(filesToBeDeleted, rootPkg.getQualifiedName(),
                                rootPkg.getProject());
        }
    }

    protected void handlePackageDeleteEvent(
        IElement ielem,
        Vector filesToBeDeleted) {
        IPackage pkg = (IPackage)  ielem;
        if (pkg != null) {
            ETList<INamedElement> elems = pkg.getOwnedElements();
            if (elems != null) {
                for (int i = 0; i < elems.getCount(); i++) {
                    INamedElement elem = elems.item(i);
                    if (elem instanceof IClass
                        || elem instanceof IInterface) {
                        IClassifier before =
                            (IClassifier)  elem;
                        filesToBeDeleted.add(
                            ClassInfo.getSymbolFilename(before));
                    }
                    if (elem instanceof IPackage) {
                        handlePackageDeleteEvent(elem, filesToBeDeleted);
                    }
                }

            }
        }
    }
}