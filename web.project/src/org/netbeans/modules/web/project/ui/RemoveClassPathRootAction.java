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

package org.netbeans.modules.web.project.ui;


import java.io.IOException;
import java.util.HashSet;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.EditableProperties;


import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.openide.ErrorManager;

/**
 * Action for removing an ClassPathRoot. The action looks up
 * the {@link RemoveClassPathRootAction.Removable} in the
 * activated node's Lookups and delegates to it.
 * @author Tomas Zezula
 */
final class RemoveClassPathRootAction extends NodeAction {

    /**
     * Implementation of this interfaces has to be placed
     * into the node's Lookup to allow {@link RemoveClassPathRootAction}
     * on the node.
     */
    static interface Removable {
        /**
         * Checks if the classpath root can be removed
         * @return returns true if the action should be enabled
         */
        public boolean canRemove ();

        /**
         * <p>Removes the classpath root. The caller has write access to
         * ProjectManager. The implementation should <strong>not</strong> save the changed project.
         * Instead, it should return the changed Project. The caller ensures
         * that all the changed projects are saved.
         * 
         * <p>The reason why the implementation shouldn't save the project is that
         * changed made to the project may cause the build-impl.xml file to be 
         * recreated upon saving, which is slow. There will be performance issues (see #54160) if
         * multiple references are removed and the project is saved after 
         * each removal.
         *
         * @return the changed project or null if no project has been changed.
         */
        public abstract Project remove ();
    }

    protected void performAction(final Node[] activatedNodes) {
        final Set changedProjectsSet = new HashSet();
        
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                for (int i = 0; i < activatedNodes.length; i++) {
                    Removable removable = (Removable) activatedNodes[i].getLookup().lookup(Removable.class);
                    if (removable == null)
                        continue;

                    Project p = removable.remove();
                    if (p != null)
                        changedProjectsSet.add(p);
                }

                for (Iterator i = changedProjectsSet.iterator(); i.hasNext();) {
                    Project p = (Project)i.next();
                    try {
                        ProjectManager.getDefault().saveProject(p);
                    }
                    catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }
        });
    }

    protected boolean enable(Node[] activatedNodes) {
        for (int i=0; i<activatedNodes.length; i++) {
            Removable removable = (Removable) activatedNodes[i].getLookup().lookup(Removable.class);
            if (removable==null) {
                return false;
            }
            if (!removable.canRemove()) {
                return false;
            }
        }
        return true;
    }

    public String getName() {
        return NbBundle.getMessage (RemoveClassPathRootAction.class,"CTL_RemoveProject");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (RemoveClassPathRootAction.class);
    }

    protected boolean asynchronous() {
        return false;
    }

    /**
     * Checks if the reference is still used in the project.
     * @param props the array of {@link EditableProperties} which
     * should be checked.
     * @param reference
     * @return true if the reference is used
     */
    public static boolean isReferenced (EditableProperties[] props, String reference) {
        for (int i=0; i< props.length; i++) {
            for (Iterator it = props[i].values().iterator(); it.hasNext();) {
                String value = (String) it.next ();
                if (value != null && value.indexOf(reference)>=0) {
                    return true;
                }
            }
        }
        return false;
    }
}
