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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.ui.SourcePath;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


/**
 * @author   Jan Jancura
 */
public class SourcesModel implements TreeModel, TableModel,
NodeActionsProvider {

    private static final String     FILTER_PREFIX = "Do not stop in: ";
    private static final String     DISP_FILTER_PREFIX = NbBundle.getBundle
        (SourcesModel.class).getString ("CTL_SourcesModel_Name_DoNotStopIn");


    private Listener                listener;
    private SourcePath              sourcePath;
    private JPDADebugger            debugger;
    private Vector<ModelListener>   listeners = new Vector<ModelListener>();
    // set of filters
    private Set<String>             filters = new HashSet<String>();
    private Set<String>             enabledFilters = new HashSet<String>();
    private Set<String>             enabledSourceRoots = new HashSet<String>();
    private Set<String>             disabledSourceRoots = new HashSet<String>();
    private List<String>            additionalSourceRoots = new ArrayList<String>();
    private Properties              filterProperties = Properties.
        getDefault ().getProperties ("debugger").getProperties ("sources");
    private final Set<String>       sourceRootsSet = new HashSet<String>();


    public SourcesModel (ContextProvider lookupProvider) {
        sourcePath = lookupProvider.lookupFirst(null, SourcePath.class);
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        loadFilters ();
        updateCachedRoots();
        DELETE_ACTION.putValue (
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke ("DELETE")
        );
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
            // 1) get source roots
            String[] sourceRoots = sourcePath.getOriginalSourceRoots ();

            // 2) get filters
            String[] ep = new String [filters.size ()];
            ep = filters.toArray (ep);
            int i, k = ep.length;
            for (i = 0; i < k; i++) {
                ep [i] = DISP_FILTER_PREFIX + ep [i];
            }

            // 3) find additional disabled source roots (enabled are in sourceRoots)
            List<String> addSrcRoots;
            synchronized (this) {
                if (additionalSourceRoots.size() > 0) {
                    addSrcRoots = new ArrayList<String>(additionalSourceRoots.size());
                    for (String addSrcRoot : additionalSourceRoots) {
                        if (!enabledSourceRoots.contains(addSrcRoot)) {
                            addSrcRoots.add(addSrcRoot);
                        }
                    }
                } else {
                    addSrcRoots = Collections.emptyList();
                }
            }

            // 3) join them
            Object[] os = new Object [sourceRoots.length + addSrcRoots.size() + ep.length];
            System.arraycopy (sourceRoots, 0, os, 0, sourceRoots.length);
            System.arraycopy (addSrcRoots.toArray(), 0, os, sourceRoots.length, addSrcRoots.size());
            System.arraycopy (ep, 0, os, sourceRoots.length + addSrcRoots.size(), ep.length);
            to = Math.min(os.length, to);
            from = Math.min(os.length, from);
            Object[] fos = new Object [to - from];
            System.arraycopy (os, from, fos, 0, to - from);
            if (listener == null)
                listener = new Listener (this);
            return fos;
        } else
        throw new UnknownTypeException (parent);
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
            if (listener == null)
                listener = new Listener (this);
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
            if (((String) node).startsWith (DISP_FILTER_PREFIX) || additionalSourceRoots.contains(node)) {
                return new Action[] {
                    NEW_FILTER_ACTION,
                    NEW_SOURCE_ROOT_ACTION,
                    DELETE_ACTION
                };
            } else {
                return new Action[] {
                    NEW_FILTER_ACTION,
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
        if (root.startsWith (DISP_FILTER_PREFIX)) {
            return enabledFilters.contains (root.substring (
                DISP_FILTER_PREFIX.length ()
            ));
        }
        synchronized(this) {
            return sourceRootsSet.contains(root);
        }
    }

    private void setEnabled (String root, boolean enabled) {
        if (root.startsWith (DISP_FILTER_PREFIX)) {
            String filter = root.substring (DISP_FILTER_PREFIX.length ());
            if (enabled) {
                synchronized (this) {
                    enabledFilters.add (filter);
                }
                debugger.getSmartSteppingFilter ().addExclusionPatterns (
                        Collections.singleton (filter)
                );
            } else {
                synchronized (this) {
                    enabledFilters.remove (filter);
                }
                debugger.getSmartSteppingFilter ().removeExclusionPatterns (
                        Collections.singleton (filter)
                );
            }
        } else {
            List<String> sourceRoots = new ArrayList<String>(sourceRootsSet);
            synchronized (this) {
                if (enabled) {
                    enabledSourceRoots.add (root);
                    disabledSourceRoots.remove (root);
                    sourceRoots.add (root);
                } else {
                    disabledSourceRoots.add (root);
                    enabledSourceRoots.remove (root);
                    sourceRoots.remove (root);
                }
            }
            String[] ss = new String [sourceRoots.size ()];
            sourcePath.setSourceRoots (sourceRoots.toArray (ss));

        }
        saveFilters ();
    }

    private void loadFilters () {
        filters = new HashSet (
            filterProperties.getProperties ("class_filters").getCollection (
                "all",
                Collections.EMPTY_SET
            )
        );
        enabledFilters = new HashSet (
            filterProperties.getProperties ("class_filters").getCollection (
                "enabled",
                Collections.EMPTY_SET
            )
        );
        enabledSourceRoots = new HashSet (
            filterProperties.getProperties ("source_roots").getCollection (
                "enabled",
                Collections.EMPTY_SET
            )
        );
        disabledSourceRoots = new HashSet (
            filterProperties.getProperties ("source_roots").getCollection (
                "disabled",
                Collections.EMPTY_SET
            )
        );
        additionalSourceRoots = new ArrayList(
            filterProperties.getProperties("additional_source_roots").getCollection(
                "src_roots",
                Collections.EMPTY_LIST)
        );
    }

    private synchronized void saveFilters () {
        filterProperties.getProperties ("class_filters").
            setCollection ("all", filters);
        filterProperties.getProperties ("class_filters").
            setCollection ("enabled", enabledFilters);
        filterProperties.getProperties ("source_roots").setCollection
            ("enabled", enabledSourceRoots);
        filterProperties.getProperties ("source_roots").setCollection
            ("disabled", disabledSourceRoots);
        filterProperties.getProperties("additional_source_roots").
            setCollection("src_roots", additionalSourceRoots);
    }

    private synchronized void updateCachedRoots() {
        String[] roots = sourcePath.getSourceRoots();
        sourceRootsSet.clear();
        for (int x = 0; x < roots.length; x++) {
            sourceRootsSet.add(roots[x]);
        }
    }

    // innerclasses ............................................................

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view representation.
     */
    public static class DefaultSourcesColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "DefaultSourcesColumn";
        }

        /**
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DefaultSourcesColumn.class).
                getString ("CTL_SourcesModel_Column_Name_Name");
        }

        public Character getDisplayedMnemonic() {
            return new Character(NbBundle.getBundle(SourcesModel.class).getString
                ("CTL_SourcesModel_Column_Name_Name_Mnc").charAt(0));
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (DefaultSourcesColumn.class).getString
                ("CTL_SourcesModel_Column_Name_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return null;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view representation.
     */
    public static class SourcesUsedColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "use";
        }

        /**
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (SourcesModel.class).getString
                ("CTL_SourcesModel_Column_Debugging_Name");
        }

        public Character getDisplayedMnemonic() {
            return new Character(NbBundle.getBundle(SourcesModel.class).getString
                ("CTL_SourcesModel_Column_Debugging_Name_Mnc").charAt(0));
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return Boolean.TYPE;
        }

        /**
         * Returns tooltip for given column. Default implementation returns
         * <code>null</code> - do not use tooltip.
         *
         * @return  tooltip for given node or <code>null</code>
         */
        public String getShortDescription () {
            return NbBundle.getBundle (SourcesModel.class).getString
                ("CTL_SourcesModel_Column_Debugging_Desc");
        }

        /**
         * True if column should be visible by default. Default implementation
         * returns <code>true</code>.
         *
         * @return <code>true</code> if column should be visible by default
         */
        public boolean initiallyVisible () {
            return true;
        }
    }

    private JFileChooser newSourceFileChooser;

    private final Action NEW_SOURCE_ROOT_ACTION = new AbstractAction(
            NbBundle.getMessage(SourcesModel.class, "CTL_SourcesModel_Action_AddSrc")) {
        public void actionPerformed (ActionEvent e) {
            if (newSourceFileChooser == null) {
                newSourceFileChooser = new JFileChooser();
                newSourceFileChooser.setFileFilter(new FileFilter() {

                    public String getDescription() {
                        return NbBundle.getMessage(SourcesModel.class, "CTL_SourcesModel_AddSrc_Chooser_Filter_Description");
                    }

                    public boolean accept(File file) {
                        if (file.isDirectory()) {
                            return true;
                        }
                        String name = file.getName();
                        int dotIndex = name.lastIndexOf('.');
                        if (dotIndex > 0) {
                            String ext = name.substring(dotIndex + 1);
                            if ("zip".equalsIgnoreCase(ext) || "jar".equalsIgnoreCase(ext)) { // NOI18N
                                return true;
                            }
                        }
                        return false;
                    }

                });
                newSourceFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            }
            int state = newSourceFileChooser.showDialog(org.openide.windows.WindowManager.getDefault().getMainWindow(),
                                      NbBundle.getMessage(SourcesModel.class, "CTL_SourcesModel_AddSrc_Btn"));
            if (state == JFileChooser.APPROVE_OPTION) {
                File zipOrDir = newSourceFileChooser.getSelectedFile();
                try {
                    String d = zipOrDir.getCanonicalPath();
                    synchronized (SourcesModel.this) {
                        additionalSourceRoots.add(d);
                        enabledSourceRoots.add(d);
                    }
                    // Set the new source roots:
                    String[] sourceRoots = sourcePath.getSourceRoots();
                    int l = sourceRoots.length;
                    String[] newSourceRoots = new String[l + 1];
                    System.arraycopy(sourceRoots, 0, newSourceRoots, 0, l);
                    newSourceRoots[l] = d;
                    sourcePath.setSourceRoots(newSourceRoots);

                    saveFilters();
                    fireTreeChanged ();
                } catch (java.io.IOException ioex) {
                    ErrorManager.getDefault().notify(ioex);
                }
            }
        }
    };

    private final Action NEW_FILTER_ACTION = new AbstractAction
        (NbBundle.getBundle (SourcesModel.class).getString
            ("CTL_SourcesModel_Action_AddFilter")) {
            public void actionPerformed (ActionEvent e) {
                NotifyDescriptor.InputLine descriptor = new
                    NotifyDescriptor.InputLine (
                        NbBundle.getBundle (SourcesModel.class).getString
                            ("CTL_SourcesModel_NewFilter_Filter_Label"),
                        NbBundle.getBundle (SourcesModel.class).getString
                            ("CTL_SourcesModel_NewFilter_Title")
                    );
                if (DialogDisplayer.getDefault ().notify (descriptor) ==
                    NotifyDescriptor.OK_OPTION
                ) {
                    String filter = descriptor.getInputText ();
                    synchronized (SourcesModel.this) {
                        filters.add (filter);
                        enabledFilters.add (filter);
                    }
                    debugger.getSmartSteppingFilter ().addExclusionPatterns (
                        Collections.singleton (filter)
                    );
                    saveFilters();
                    fireTreeChanged ();
                }
            }
    };
    private final Action DELETE_ACTION = Models.createAction (
        NbBundle.getBundle (SourcesModel.class).getString
            ("CTL_SourcesModel_Action_Delete"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++) {
                    String node = (String) nodes [i];
                    if (node.startsWith(DISP_FILTER_PREFIX)) {
                        node = node.substring(DISP_FILTER_PREFIX.length());
                        synchronized (SourcesModel.this) {
                            filters.remove (node);
                            enabledFilters.remove (node);
                        }
                        debugger.getSmartSteppingFilter ().removeExclusionPatterns (
                            Collections.singleton (node)
                        );
                    } else {
                        synchronized (SourcesModel.this) {
                            additionalSourceRoots.remove(node);
                            enabledSourceRoots.remove(node);
                            disabledSourceRoots.remove(node);
                        }
                        // Set the new source roots:
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
                    }
                }
                saveFilters ();
                fireTreeChanged ();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );

    private static class Listener implements PropertyChangeListener {

        private WeakReference<SourcesModel> model;

        private Listener (
            SourcesModel tm
        ) {
            model = new WeakReference<SourcesModel>(tm);
            tm.sourcePath.addPropertyChangeListener (this);
            tm.debugger.getSmartSteppingFilter ().
                addPropertyChangeListener (this);
        }

        private SourcesModel getModel () {
            SourcesModel tm = model.get ();
            if (tm == null) {
                tm.sourcePath.removePropertyChangeListener (this);
                tm.debugger.getSmartSteppingFilter ().
                    removePropertyChangeListener (this);
            }
            return tm;
        }

        public void propertyChange (PropertyChangeEvent evt) {
            SourcesModel m = getModel ();
            if (m == null) return;
            m.updateCachedRoots();
            m.fireTreeChanged ();
        }
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view representation.
     */
    public abstract static class AbstractColumn extends ColumnModel {

        Properties properties = Properties.getDefault ().
            getProperties ("debugger").getProperties ("views");


        /**
         * Set true if column is visible.
         *
         * @param visible set true if column is visible
         */
        public void setVisible (boolean visible) {
            properties.setBoolean (getID () + ".visible", visible);
        }

        /**
         * Set true if column should be sorted by default.
         *
         * @param sorted set true if column should be sorted by default
         */
        public void setSorted (boolean sorted) {
            properties.setBoolean (getID () + ".sorted", sorted);
        }

        /**
         * Set true if column should be sorted by default in descending order.
         *
         * @param sortedDescending set true if column should be sorted by default
         *        in descending order
         */
        public void setSortedDescending (boolean sortedDescending) {
            properties.setBoolean (getID () + ".sortedDescending", sortedDescending);
        }

        /**
         * Should return current order number of this column.
         *
         * @return current order number of this column
         */
        public int getCurrentOrderNumber () {
            return properties.getInt (getID () + ".currentOrderNumber", -1);
        }

        /**
         * Is called when current order number of this column is changed.
         *
         * @param newOrderNumber new order number
         */
        public void setCurrentOrderNumber (int newOrderNumber) {
            properties.setInt (getID () + ".currentOrderNumber", newOrderNumber);
        }

        /**
         * Return column width of this column.
         *
         * @return column width of this column
         */
        public int getColumnWidth () {
            return properties.getInt (getID () + ".columnWidth", 150);
        }

        /**
         * Is called when column width of this column is changed.
         *
         * @param newColumnWidth a new column width
         */
        public void setColumnWidth (int newColumnWidth) {
            properties.setInt (getID () + ".columnWidth", newColumnWidth);
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", true);
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean isSorted () {
            return properties.getBoolean (getID () + ".sorted", false);
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean isSortedDescending () {
            return properties.getBoolean (getID () + ".sortedDescending", false);
        }
    }

}
