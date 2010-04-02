/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.javacard.project.customizer;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.javacard.classdetector.TypeFinder;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import org.netbeans.modules.javacard.common.GuiUtils;
import org.openide.util.Parameters;

/**
 * An explorer panel which is passed a fully qualified class name in its
 * constructor, and populates its root node with all classes of that type
 * in the classpath passed to setClassPath.
 * <p>
 * The search of the classpath is done in a background thread while a wait
 * node is displayed.  When the search is completed
 *
 * @author Tim Boudreau
 */
public class AllClassesOfTypeExplorerPanel extends JPanel implements ExplorerManager.Provider {

    protected final ExplorerManager mgr = new ExplorerManager();
    private final Children.Array children = new Children.Array();
    private final Node waitNode = GuiUtils.createWaitNode();
    private final CB cb = new CB();
    private final RequestProcessor.Task task = RequestProcessor.getDefault().create(cb);
    private TypeFinder finder;
    private ClassPath classpath;
    private final String className;
    private volatile boolean searchCompleted;

    public AllClassesOfTypeExplorerPanel(String className) {
        this (className, null);
    }

    protected AllClassesOfTypeExplorerPanel(String className, String waitMsg) {
        this.className = className;
        waitNode.setDisplayName(waitMsg == null ? NbBundle.getMessage(
                AllClassesOfTypeExplorerPanel.class, "WAIT") : waitMsg); //NOI18N
        children.add(new Node[]{waitNode});
        Node root = new AbstractNode(children);
        mgr.setRootContext(root);
    }

    public final ExplorerManager getExplorerManager() {
        return mgr;
    }

    public boolean shouldRunSearch() {
        return true;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        ClassPath path;
        synchronized (this) {
            path = classpath;
        }
        if (shouldRunSearch() && !searchCompleted && path != null) {
            task.schedule(20);
        }
    }

    @Override
    public void removeNotify() {
        TypeFinder f;
        synchronized (this) {
            f = finder;
        }
        if (!searchCompleted) {
            task.cancel();
            if (f != null) {
                f.cancel();
            }
        }
        super.removeNotify();
        if (!searchCompleted) {
            if (f != null) {
                f.reset();
            }
            children.remove(children.getNodes());
            children.add(new Node[] { waitNode });
        }
    }

    /**
     * Set the classpath
     * @param path
     */
    protected final void setClassPath(ClassPath path) {
        Parameters.notNull("path", path); //NOI18N
        synchronized (this) {
            this.classpath = path;
        }
        if (isDisplayable()) {
            searchCompleted = false;
            if (shouldRunSearch()) {
                task.schedule(20);
            }
        }
    }

    private void internalOnSearchCompleted() {
        Node[] nodes = mgr.getRootContext().getChildren().getNodes();
        if (nodes.length > 0) {
            if (nodes[0] != waitNode) {
                try {
                    mgr.setSelectedNodes(new Node[]{nodes[0]});

                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        try {
            onSearchCompleted();
        } finally {
            searchCompleted = true;
        }
    }

    /**
     * Called from the background thread before beginning to search
     * for classes - in some cases there is other background work to do.
     */
    protected void onSearchBegun() {

    }

    /**
     * Called when the child nodes have been fully populated;  useful
     * for enabling controls that should not be enabled until there is data.
     */
    protected void onSearchCompleted() {

    }

    private class CB implements TypeFinder.Callback, Runnable {

        public void run() {
            if (!EventQueue.isDispatchThread()) {
                onSearchBegun();
                if (!isDisplayable()) {
                    return;
                }
                TypeFinder f;
                synchronized (this) {
                    finder = f = new TypeFinder(className, classpath);
                }
                try {
                    f.findTypes(this);
                    children.remove(new Node[]{waitNode});
                } finally {
                    EventQueue.invokeLater(this);
                }
            } else {
                internalOnSearchCompleted();
            }
        }

        public void foundFileObject(FileObject fo, String className) {
            try {
                DataObject dob = DataObject.find(fo);
                FN fn = new FN(dob.getNodeDelegate(), className);
                children.add(new Node[]{fn});
                if (!isDisplayable()) {
                    synchronized (this) {
                        if (finder != null) {
                            finder.cancel();
                        }
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static final class FN extends FilterNode {

        private final String className;

        FN(Node orig, String className) {
            super(orig, Children.LEAF, new ProxyLookup(orig.getLookup(),
                    Lookups.singleton(className)));
            this.className = className;
            setDisplayName(orig.getLookup().lookup(DataObject.class).getName());
            disableDelegation(FilterNode.DELEGATE_SET_DISPLAY_NAME);
            disableDelegation(FilterNode.DELEGATE_GET_ACTIONS);
            disableDelegation(FilterNode.DELEGATE_GET_CONTEXT_ACTIONS);
            disableDelegation(FilterNode.DELEGATE_GET_VALUE);
            disableDelegation(FilterNode.DELEGATE_SET_VALUE);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }

        @Override
        public Action getPreferredAction() {
            return null;
        }

        @Override
        public String getHtmlDisplayName() {
            return getLookup().lookup(DataObject.class).getName() +
                    "<font color='!controlShadow'> (" + className + ")";
        }
    }
}
