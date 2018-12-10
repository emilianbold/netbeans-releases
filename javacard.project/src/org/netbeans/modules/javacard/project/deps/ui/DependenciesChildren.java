/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.javacard.project.deps.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.javacard.common.GuiUtils;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.deps.ArtifactKind;
import org.netbeans.modules.javacard.project.deps.DependenciesProvider;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.netbeans.modules.javacard.project.deps.ResolvedDependency;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Dependencies node children factory
 *
 */
final class DependenciesChildren extends ChildFactory.Detachable<DependencyDescriptor> implements ChangeListener {
    private final JCProject project;
    private Cancellable c;
    private final Object LOCK = new Object();
    private final ResolvedDependencies resolvedDependencies;

    DependenciesChildren (JCProject project, ResolvedDependencies resolvedDependencies) {
        this.project = project;
        this.resolvedDependencies = resolvedDependencies;
    }

    DependenciesChildren (JCProject project) {
        this (project, null);
    }

    public static Children createChildren(JCProject project) {
        return Children.create(new DependenciesChildren(project), true);
    }

    public static Children createChildren(JCProject project, ResolvedDependencies rd) {
        return Children.create(new DependenciesChildren(project, rd), true);
    }

    public void update (boolean now) {
        refresh (now);
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        project.addDependencyChangeListener(this);
        if (resolvedDependencies != null) {
            resolvedDependencies.addChangeListener(this);
        }
    }

    @Override
    protected void removeNotify() {
        super.removeNotify();
        project.removeDependencyChangeListener(this);
        if (resolvedDependencies != null) {
            resolvedDependencies.removeChangeListener(this);
        }
        synchronized(LOCK) {
            if (this.c != null) {
                c.cancel();
            }
        }
    }

    private ResolvedDependencies startRetrieval (DependenciesProvider p) {
        if (resolvedDependencies != null) {
            return resolvedDependencies;
        }
        //Okay, here it gets complicated.  We are running on one background
        //thread;  another background thread will actually resolve the
        //dependencies and pass them into us.  So, we wait on the Cancellable
        //returned by requestDependencies().  When the dependencies have
        //finished resolving, we wake up and return them.
        assert !EventQueue.isDispatchThread();
        if (p == null) {
            return null;
        }
        R fetcher = new R();
        Cancellable cancellable = p.requestDependencies(fetcher);
        if (!fetcher.done) {
            synchronized (LOCK) {
                this.c = cancellable;
            }
            while (!fetcher.done && cancellable != null) {
                if (Thread.interrupted()) {
                    return null;
                }
                synchronized (cancellable) {
                    try {
                        cancellable.wait();
                    } catch (InterruptedException ex) {
                        return null;
                    }
                }
            }
        }
        return fetcher.resolvedDependencies;
    }

    @Override
    protected boolean createKeys(List<DependencyDescriptor> toPopulate) {
        DependenciesProvider prov = project.getLookup().lookup(DependenciesProvider.class);
        ResolvedDependencies resolved = startRetrieval(prov);
        if (resolved == null) {
            return true;
        }
        if (resolved != null) {
            for (ResolvedDependency d : resolved.all()) {
                toPopulate.add (createDescriptor(d));
            }
        }
        synchronized(LOCK) {
            //Dispose references so we don't leak the ResolvedDependencies object,
            //and through it, project objects for everything we depend on
            this.c = null;
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(DependencyDescriptor key) {
        return new DDNode (project, key, key.rd);
    }

    public void stateChanged(ChangeEvent e) {
        refresh (false);
    }

    private static final class DDNode extends AbstractNode implements Runnable {
        private volatile boolean removing;
        DDNode (JCProject project, DependencyDescriptor desc, ResolvedDependency rd) {
            super (Children.LEAF, rd == null ? Lookups.fixed(project, desc) : Lookups.fixed(project, desc, rd));
            setDisplayName (desc.getName());
            setShortDescription(desc.getPath());
        }

        @Override
        public Image getIcon(int type) {
            ResolvedDependency rd = getLookup().lookup(ResolvedDependency.class);
            DependencyDescriptor d = getLookup().lookup(DependencyDescriptor.class);
            Image result = d.getIcon();
            result = result == null ? super.getIcon(type) : result;
            boolean valid = rd == null ? d.isValid() : rd.isValid();
            if (!valid) {
                Image badge = ImageUtilities.loadImage("org/netbeans/modules/javacard/resources/brokenProjectBadge.png"); //NOI18N
                result = ImageUtilities.mergeImages(result, badge, 8, 8);
            }
            return result;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public Action[] getActions(boolean ignored) {
            RA ra = new RA();
            ra.putValue (Action.NAME, NbBundle.getMessage(DDNode.class, 
                    "ACTION_REMOVE_DEPENDENCY")); //NOI18N
            return new Action[] { ra };
        }

        public void run() {
            removing = true;
            try {
                DependenciesProvider prov = getLookup().lookup(Project.class).getLookup().lookup(DependenciesProvider.class);
                if (prov != null) {
                    ResolvedDependencies rd = null;
                    R r = new R();
                    Cancellable c = prov.requestDependencies(r);
                    while (!r.done) {
                        synchronized (c) {
                            try {
                                c.wait();
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                    rd = r.resolvedDependencies;
                    if (rd != null) {
                        String id = getLookup().lookup(DependencyDescriptor.class).getId();
                        ResolvedDependency toRemove = null;
                        for (ResolvedDependency test : rd.all()) {
                            if (id.equals(test.getDependency().getID())) {
                                toRemove = test;
                                break;
                            }
                        }
                        if (toRemove != null) {
                            rd.remove(toRemove);
                            try {
                                rd.save();
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }

                }
            } finally {
                removing = false;
            }
        }

        private class RA extends AbstractAction {
            //XXX use ContextAction to support multiple selection
            public void actionPerformed(ActionEvent e) {
                DDNode.this.remove();
            }
            @Override
            public boolean isEnabled() {
                return !removing;
            }
        }

        private void remove() {
            ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RA.class, "MSG_REMOVING_DEPENDENCY",
                    getLookup().lookup(DependencyDescriptor.class).getName()));
            GuiUtils.showProgressDialogAndRun(handle, this, false);
        }
    }

    private DependencyDescriptor createDescriptor (ResolvedDependency d) {
        String path = d.getPath(ArtifactKind.ORIGIN);
        boolean valid = d.isValid();
        String id = d.getDependency().getID();
        Image icon = null;
        String name = d.getPath(ArtifactKind.ORIGIN);
        File f = name == null ? null : new File (name);
        if (f != null && f.exists()) {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
            switch (d.getKind()) {
                case CLASSIC_LIB :
                case EXTENSION_LIB:
                case JAVA_PROJECT :
                    Project p = FileOwnerQuery.getOwner(fo);
                    if (p != null) {
                        ProjectInformation info = p.getLookup().lookup(ProjectInformation.class);
                        if (info != null) {
                            icon = ImageUtilities.icon2Image(info.getIcon());
                            name = info.getDisplayName();
                        } else {
                            name = p.getProjectDirectory().getPath();
                        }
                    }
                    break;
                case CLASSIC_LIB_JAR :
                case EXTENSION_LIB_JAR :
                case JAR_WITH_EXP_FILE :
                case RAW_JAR :
                    try {
                        DataObject dob = DataObject.find(fo);
                        Node n = dob.getNodeDelegate();
                        name = n.getDisplayName();
                        icon = n.getIcon(BeanInfo.ICON_COLOR_16x16);
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    break;
                default : throw new AssertionError();
            }
        } else {
            icon = null;
        }
        if (name == null) {
            name = "[unknown]";
        }
        DependencyDescriptor result = new DependencyDescriptor(icon, path, name, id, valid);
        if (resolvedDependencies != null) {
            //We are in the dialog - we need to
            result.rd = d;
        }
        return result;
    }

    private static class R implements DependenciesProvider.Receiver {
        private volatile ResolvedDependencies resolvedDependencies;
        volatile boolean done;

        public void receive(ResolvedDependencies deps) {
            this.resolvedDependencies = deps;
            done = true;
        }

        public boolean failed(Throwable failure) {
            return true;
        }
    }
}
