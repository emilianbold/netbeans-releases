/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.debugger.jpda.projects;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * @author   Jan Jancura
 */
public class SourcesCurrentModel implements TreeModel, TableModel,
NodeActionsProvider {

    private Vector<ModelListener>   listeners = new Vector<ModelListener>();
    // set of filters
    //private Set<String>             enabledSourceRoots = new HashSet<String>();
    private Set<String>             disabledSourceRoots = new HashSet<String>();
    private List<String>            additionalSourceRoots = Collections.emptyList();
    private Properties              sourcesProperties = Properties.
        getDefault ().getProperties ("debugger").getProperties ("sources");
    //private final Set<String>       sourceRootsSet = new HashSet<String>();
    private String                  projectRoot;
    private PropertyChangeListener  mainProjectListener;
    private DebuggerManagerListener debuggerListener;
    private SourcePathProviderImpl  currentSourcePathProvider;


    public SourcesCurrentModel () {
        bindWithSourcePathProvider();
        DELETE_ACTION.putValue (
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke ("DELETE")
        );
    }

    private void bindWithSourcePathProvider() {
        debuggerListener = new DebuggerManagerAdapter() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if (DebuggerManager.PROP_CURRENT_SESSION.equals(propertyName)) {
                    Session s = DebuggerManager.getDebuggerManager().getCurrentSession();
                    SourcePathProviderImpl spImpl = null;
                    if (s != null) {
                        List<? extends SourcePathProvider> sourcePathProviders = s.lookup(null, SourcePathProvider.class);
                        for (SourcePathProvider sp : sourcePathProviders) {
                            if (sp instanceof SourcePathProviderImpl) {
                                spImpl = (SourcePathProviderImpl) sp;
                                setSources(spImpl);
                                break;
                            }
                        }
                    }
                    synchronized (this) {
                        currentSourcePathProvider = spImpl;
                    }
                    fireTreeChanged();
                }
            }
        };
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                WeakListeners.create(DebuggerManagerListener.class, debuggerListener,
                                             DebuggerManager.getDebuggerManager()));
    }

    private synchronized void setSources(SourcePathProviderImpl sp) {
        //Find the correct project root and take the disabled and additional sources for that project root.
        /*
        File projectBase = sp.baseDir;
        String rootPath = null;
        if (projectBase != null) {
            try {
                rootPath = projectBase.toURI().toURL().toExternalForm();
                System.err.println("\n\nrootPath = '"+rootPath+"'\n\n");
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
         */
        currentSourcePathProvider = sp;
        //Unset disabled source roots from sp.
        //Add additional source roots to sp.
    }


    // TreeModel ...............................................................

    /**
     *
     * @return threads contained in this group of threads
     */
    public Object getRoot () {
        return ROOT;
    }

    /**
     *
     * @return threads contained in this group of threads
     */
    public Object[] getChildren (Object parent, int from, int to)
    throws UnknownTypeException {
        if (parent == ROOT) {

            //Display source roots from currentSourcePathProvider, if set.
            synchronized (this) {
                if (currentSourcePathProvider != null) {
                    return currentSourcePathProvider.getSourceRoots();
                }
            }

            // 1) get source roots
            if (mainProjectListener == null) {
                mainProjectListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        fireTreeChanged();
                    }
                };
                MainProjectManager.getDefault().addPropertyChangeListener(
                        WeakListeners.propertyChange(mainProjectListener, MainProjectManager.getDefault()));
            }
            Project p = MainProjectManager.getDefault().getMainProject();
            String[] sourceRoots;
            String root = null;
            if (p != null) {
                List<FileObject> projectSources = getProjectSources(p);
                //System.err.println("\nProject sources = "+projectSources);
                sourceRoots = new String[projectSources.size()];
                for (int i = 0; i < sourceRoots.length; i++) {
                    sourceRoots[i] = SourcePathProviderImpl.getRoot(projectSources.get(i));
                }
                try {
                    root = p.getProjectDirectory().getURL().toExternalForm();
                } catch (FileStateInvalidException ex) {
                }
            } else {
                sourceRoots = new String[] {};
            }
            //String[] sourceRoots = sourcePath.getOriginalSourceRoots ();

            // 3) find additional and disabled source roots
            List<String> addSrcRoots = null;
            synchronized (this) {
                if (root != null) {
                    addSrcRoots = loadAdditionalSourceRoots(root);
                }
                if (addSrcRoots == null) {
                    addSrcRoots = new ArrayList<String>();
                } else {
                    addSrcRoots = new ArrayList<String>(addSrcRoots);
                }
                additionalSourceRoots = addSrcRoots;
                disabledSourceRoots = loadDisabledSourceRoots(root);
                if (disabledSourceRoots == null) {
                    disabledSourceRoots = new HashSet<String>();
                } else {
                    disabledSourceRoots = new HashSet<String>(disabledSourceRoots);
                }
                projectRoot = root;
            }

            // 3) join them
            Object[] os = new Object [sourceRoots.length + addSrcRoots.size()];
            System.arraycopy (sourceRoots, 0, os, 0, sourceRoots.length);
            System.arraycopy (addSrcRoots.toArray(), 0, os, sourceRoots.length, addSrcRoots.size());
            to = Math.min(os.length, to);
            from = Math.min(os.length, from);
            Object[] fos = new Object [to - from];
            System.arraycopy (os, from, fos, 0, to - from);
            return fos;
        } else
        throw new UnknownTypeException (parent);
    }

    private List<String> loadAdditionalSourceRoots(String projectRoot) {
        if (projectRoot == null) return null;
        return (List<String>) sourcesProperties.getProperties("additional_source_roots").
                getMap("project", Collections.emptyMap()).
                get(projectRoot);
    }

    private Set<String> loadDisabledSourceRoots(String projectRoot) {
        if (projectRoot == null) return null;
        return (Set<String>) sourcesProperties.getProperties("source_roots").
                getMap("project_disabled", Collections.emptyMap()).
                get(projectRoot);
    }

    private static List<FileObject> getProjectSources(Project p) {
        List<FileObject> allSourceRoots = new ArrayList<FileObject>();
        Set<FileObject> addedBinaryRoots = new HashSet<FileObject>();
        Set<FileObject> preferredRoots = new HashSet<FileObject>();
        Sources s = ProjectUtils.getSources(p);
        SourceGroup[] sgs = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sg : sgs) {
            ClassPath ecp = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.SOURCE);
            if (ecp != null) {
                addSourceRoots(ecp, allSourceRoots, preferredRoots);
            }
        }
        for (SourceGroup sg : sgs) {
            ClassPath ecp = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.EXECUTE);
            if (ecp != null) {
                addSourceRoots(ecp, allSourceRoots, addedBinaryRoots, preferredRoots);
            }
        }
        for (SourceGroup sg : sgs) {
            ClassPath ecp = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.BOOT);
            if (ecp != null) {
                addSourceRoots(ecp, allSourceRoots, addedBinaryRoots, preferredRoots);
            }
        }
        
        return allSourceRoots;
    }

    private static void addSourceRoots(ClassPath ecp,
                                       List<FileObject> allSourceRoots,
                                       Set<FileObject> addedBinaryRoots,
                                       Set<FileObject> preferredRoots) {
        FileObject[] binaryRoots = ecp.getRoots();
        for (FileObject fo : binaryRoots) {
            if (addedBinaryRoots.contains(fo)) {
                continue;
            }
            addedBinaryRoots.add(fo);
            try {
                FileObject[] roots = SourceForBinaryQuery.findSourceRoots(fo.getURL()).getRoots();
                for (FileObject fr : roots) {
                    if (!preferredRoots.contains(fr)) {
                        allSourceRoots.add(fr);
                        preferredRoots.add(fr);
                    }
                }
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static void addSourceRoots(ClassPath ecp,
                                       List<FileObject> allSourceRoots,
                                       Set<FileObject> preferredRoots) {
        FileObject[] sourceRoots = ecp.getRoots();
        for (FileObject fr : sourceRoots) {
            if (!preferredRoots.contains(fr) && !fr.isVirtual()) {
                allSourceRoots.add(fr);
                preferredRoots.add(fr);
            }
        }
    }

    /**
     * Returns number of children for given node.
     *
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    public int getChildrenCount (Object node) throws UnknownTypeException {
        if (node == ROOT) {
            // Performance, see issue #59058.
            return Integer.MAX_VALUE;
            //return sourcePath.getOriginalSourceRoots ().length +
            //    filters.size ();
        } else
        throw new UnknownTypeException (node);
    }

    public boolean isLeaf (Object node) throws UnknownTypeException {
        if (node == ROOT) return false;
        if (node instanceof String) return true;
        throw new UnknownTypeException (node);
    }

    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }

    public void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (null);
    }


    // TableModel ..............................................................

    public Object getValueAt (Object node, String columnID) throws
    UnknownTypeException {
        if ("use".equals (columnID)) {
            if (node instanceof String)
                return Boolean.valueOf (
                    isEnabled ((String) node)
                );
        }
        throw new UnknownTypeException (node);
    }

    public boolean isReadOnly (Object node, String columnID) throws
    UnknownTypeException {
        if ( "use".equals (columnID) &&
             (node instanceof String))
            return false;
        throw new UnknownTypeException (node);
    }

    public void setValueAt (Object node, String columnID, Object value)
    throws UnknownTypeException {
        if ("use".equals (columnID)) {
            if (node instanceof String) {
                setEnabled ((String) node, ((Boolean) value).booleanValue ());
                return;
            }
        }
        throw new UnknownTypeException (node);
    }


    // NodeActionsProvider .....................................................

    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node instanceof String) {
            if (additionalSourceRoots.contains(node)) {
                return new Action[] {
                    NEW_SOURCE_ROOT_ACTION,
                    DELETE_ACTION
                };
            } else {
                return new Action[] {
                    NEW_SOURCE_ROOT_ACTION
                };
            }
        } else
        throw new UnknownTypeException (node);
    }

    public void performDefaultAction (Object node)
    throws UnknownTypeException {
        if (node instanceof String) {
            return;
        } else
        throw new UnknownTypeException (node);
    }

    // other methods ...........................................................

    private boolean isEnabled (String root) {
        synchronized(this) {
            return !disabledSourceRoots.contains(root);
        }
    }

    private void setEnabled (String root, boolean enabled) {
        synchronized (this) {
            if (enabled) {
                disabledSourceRoots.remove (root);
            } else {
                disabledSourceRoots.add (root);
            }
            saveDisabledSourceRoots ();
        }
    }

    /*
    private void loadFilters () {
        enabledSourceRoots = new HashSet (
            sourcesProperties.getProperties ("source_roots").getCollection (
                "enabled",
                Collections.EMPTY_SET
            )
        );
        disabledSourceRoots = new HashSet (
            sourcesProperties.getProperties ("source_roots").getCollection (
                "disabled",
                Collections.EMPTY_SET
            )
        );
        additionalSourceRoots = new ArrayList(
            sourcesProperties.getProperties("additional_source_roots").getCollection(
                "src_roots",
                Collections.EMPTY_LIST)
        );
    }
     */

    private synchronized void saveDisabledSourceRoots () {
        Map map = sourcesProperties.getProperties("source_roots").
                getMap("project_disabled", new HashMap());
        map.put(projectRoot, disabledSourceRoots);
        sourcesProperties.getProperties("source_roots").
                setMap("project_disabled", map);
    }

    private synchronized void saveAdditionalSourceRoots () {
        Map map = sourcesProperties.getProperties("additional_source_roots").
                getMap("project", new HashMap());
        map.put(projectRoot, additionalSourceRoots);
        sourcesProperties.getProperties("additional_source_roots").
                setMap("project", map);
    }

    // innerclasses ............................................................

    private JFileChooser newSourceFileChooser;

    private final Action NEW_SOURCE_ROOT_ACTION = new AbstractAction(
            NbBundle.getMessage(SourcesCurrentModel.class, "CTL_SourcesModel_Action_AddSrc")) {
        public void actionPerformed (ActionEvent e) {
            if (newSourceFileChooser == null) {
                newSourceFileChooser = new JFileChooser();
                newSourceFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                newSourceFileChooser.setFileFilter(new FileFilter() {

                    public String getDescription() {
                        return NbBundle.getMessage(SourcesCurrentModel.class, "CTL_SourcesModel_AddSrc_Chooser_Filter_Description");
                    }

                    public boolean accept(File file) {
                        if (file.isDirectory()) {
                            return true;
                        }
                        try {
                            return FileUtil.isArchiveFile(file.toURI().toURL());
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                            return false;
                        }
                    }

                });
            }
            int state = newSourceFileChooser.showDialog(org.openide.windows.WindowManager.getDefault().getMainWindow(),
                                      NbBundle.getMessage(SourcesCurrentModel.class, "CTL_SourcesModel_AddSrc_Btn"));
            if (state == JFileChooser.APPROVE_OPTION) {
                File zipOrDir = newSourceFileChooser.getSelectedFile();
                try {
                    if (!zipOrDir.isDirectory() && !FileUtil.isArchiveFile(zipOrDir.toURI().toURL())) {
                        return ;
                    }
                    String d = zipOrDir.getCanonicalPath();
                    synchronized (SourcesCurrentModel.this) {
                        additionalSourceRoots.add(d);
                        //enabledSourceRoots.add(d);
                    }
                    // Set the new source roots:
                    /*
                    String[] sourceRoots = sourcePath.getSourceRoots();
                    int l = sourceRoots.length;
                    String[] newSourceRoots = new String[l + 1];
                    System.arraycopy(sourceRoots, 0, newSourceRoots, 0, l);
                    newSourceRoots[l] = d;
                    sourcePath.setSourceRoots(newSourceRoots);
                    */
                    saveAdditionalSourceRoots();
                    fireTreeChanged ();
                } catch (java.io.IOException ioex) {
                    ErrorManager.getDefault().notify(ioex);
                }
            }
        }
    };

    private final Action DELETE_ACTION = Models.createAction (
        NbBundle.getBundle (SourcesCurrentModel.class).getString
            ("CTL_SourcesModel_Action_Delete"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                synchronized (SourcesCurrentModel.this) {
                    for (i = 0; i < k; i++) {
                        String node = (String) nodes [i];
                        additionalSourceRoots.remove(node);
                        //enabledSourceRoots.remove(node);
                        disabledSourceRoots.remove(node);
                    }
                    saveAdditionalSourceRoots();
                    saveDisabledSourceRoots();
                    // Set the new source roots:
                    /*
                    String[] sourceRoots = sourcePath.getSourceRoots();
                    int l = sourceRoots.length;
                    String[] newSourceRoots = new String[l - 1];
                    int index = -1;
                    for (int ii = 0; ii < l; ii++) {
                        if (node.equals(sourceRoots[ii])) {
                            index = ii;
                            break;
                        }
                    }
                    if (index >= 0) {
                        System.arraycopy(sourceRoots, 0, newSourceRoots, 0, index);
                        System.arraycopy(sourceRoots, index + 1, newSourceRoots, index, l - (index + 1));
                        sourcePath.setSourceRoots(newSourceRoots);
                    }
                     */
                }
                fireTreeChanged ();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );

}
