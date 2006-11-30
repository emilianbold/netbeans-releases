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
import java.text.MessageFormat;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.java.plugins.RenameRefactoringPlugin;
import org.netbeans.spi.java.project.support.ui.PackageRenameHandler;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jan Becicka
 */
public class PackageRenameHandlerImpl implements PackageRenameHandler {

    public void handleRename(Node node, String newName) {
        DataFolder dob = (DataFolder) node.getCookie(DataObject.class);
        FileObject fo = dob.getPrimaryFile();
        if (node.isLeaf()) {
            //rename empty package and don't try to do any refactoring
            try {
                if (!RetoucheUtils.isValidPackageName(newName)) {
                    String msg = new MessageFormat(NbBundle.getMessage(RenameRefactoringPlugin.class,"ERR_InvalidPackage")).format(
                            new Object[] {newName}
                    );
                    
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        msg, NotifyDescriptor.INFORMATION_MESSAGE));
                    return;
                }
                FileUtil.createFolder(ClassPath.getClassPath(fo, ClassPath.SOURCE).findOwnerRoot(fo), newName.replace('.','/'));
                while (dob.getChildren().length == 0 && dob.isDeleteAllowed()) {
                    DataFolder parent = dob.getFolder();
                    dob.delete();
                    dob = parent;
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
            return;
        }

        InstanceContent ic = new InstanceContent();
        ic.add(node);
        Dictionary d = new Hashtable();
        d.put("name", newName);
        ic.add(d);
        Lookup l = new AbstractLookup(ic);
        Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(l);
        if (a.isEnabled()) {
            a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
        }
    }
}
