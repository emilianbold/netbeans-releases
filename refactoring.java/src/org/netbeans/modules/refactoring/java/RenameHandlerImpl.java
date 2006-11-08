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

package org.netbeans.modules.refactoring.java;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.swing.Action;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.spi.java.loaders.RenameHandler;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jan Becicka
 */
public class RenameHandlerImpl implements RenameHandler {

    public void handleRename(Node node, String newName) {
        InstanceContent ic = new InstanceContent();
        ic.add(node);
        Dictionary d = new Hashtable();
        d.put("name", newName);
        ic.add(d);
        Lookup l = new AbstractLookup(ic);
        DataObject dob = node.getCookie(DataObject.class);
        Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(l);
        if (a.isEnabled()) {
            a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
        } else {
            try {
                dob.rename(newName);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
    }
}
