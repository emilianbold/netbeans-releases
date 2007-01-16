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

package org.netbeans.modules.refactoring.plugins;

import java.util.Collection;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.refactoring.spi.impl.SafeDeleteUI;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Becicka
 */
public class DefaultActionsProvider extends ActionsImplementationProvider {
    
    /** Creates a new instance of DefaultActionsProvider */
    public DefaultActionsProvider() {
    }
    @Override
    public boolean canDelete(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        boolean result = false;
        for (Node node:nodes) {
            DataObject dob = (DataObject) node.getCookie(DataObject.class);
            if (dob==null) {
                return false;
            } else {
                if (dob.getPrimaryFile().isFolder()) {
                    return false;
                }
            }
            result=true;
        }
        return result;
    }

    @Override
    public Runnable deleteImpl(final Lookup lookup) {
        return new Runnable() {
            public void run() {
                Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
                FileObject[] fobs = new FileObject[nodes.size()];
                int i = 0;
                for (Node node:nodes) {
                    DataObject dob = (DataObject) node.getCookie(DataObject.class);
                    if (dob!=null) {
                        fobs[i++] = dob.getPrimaryFile();
                    }
                }
                UI.openRefactoringUI(new SafeDeleteUI(fobs));
            }
        };
    }
}
