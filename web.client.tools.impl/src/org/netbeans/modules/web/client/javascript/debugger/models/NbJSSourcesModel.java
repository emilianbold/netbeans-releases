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
package org.netbeans.modules.web.client.javascript.debugger.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.JToolTip;

import org.netbeans.modules.web.client.javascript.debugger.api.NbJSContextProviderWrapper;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.modules.web.client.javascript.debugger.filesystem.URLFileObject;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSCallStackFrame;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEventListener;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerState;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSSource;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSEditorUtil;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * @author Joelle Lam ( copied from JPDA source )
 */
public class NbJSSourcesModel implements TreeModel, NodeModel, TableModel,
        NodeActionsProvider, JSDebuggerEventListener {

    private final Action GO_TO_ACTION;
    private final Action GO_TO_CLIENT_SOURCE_ACTION;

    protected enum COLUMN_ID {

        USE_COLUMN, DEFAULT_SOURCES_COLUMN;
    }
//    private Listener listener;
    private final NbJSDebugger debugger;
    private JSSource[] sources;
    private List<ModelListener> listeners;

    private class PropertyChangeListenerImpl implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(NbJSDebugger.PROPERTY_SOURCES)) {
                setSources((JSSource[]) evt.getNewValue());
            } else if (evt.getPropertyName().equals(NbJSDebugger.PROPERTY_RELOADSOURCES)) {
                fireTreeChanges();
            }
        }
    }
    private PropertyChangeListener propertyChangeListener;

    public NbJSSourcesModel(ContextProvider contextProvider) {
        debugger = NbJSContextProviderWrapper.getContextProviderWrapper(contextProvider).getNbJSDebugger();
        debugger.addJSDebuggerEventListener(WeakListeners.create(JSDebuggerEventListener.class, this, debugger));

        propertyChangeListener = new PropertyChangeListenerImpl();
        this.debugger.addPropertyChangeListener(WeakListeners.propertyChange(propertyChangeListener, debugger));

        listeners = new CopyOnWriteArrayList<ModelListener>();

        if (debugger.isIgnoringQueryStrings()) {
            this.sources = filterEquivalentSources(debugger.getSources());
        } else {
            sources = debugger.getSources();
        }
        
        GO_TO_ACTION = NbJSEditorUtil.createDebuggerGoToAction(debugger);
        GO_TO_CLIENT_SOURCE_ACTION = NbJSEditorUtil.createDebuggerGoToClientSourceAction(debugger);
    }

    private void setSources(JSSource[] newSources) {
        if (debugger.isIgnoringQueryStrings()) {
            newSources = filterEquivalentSources(newSources);
        }
        this.sources = newSources;
        fireTreeChanges();
    }

    private JSSource[] filterEquivalentSources(JSSource[] sources) {
        if (sources == null || sources.length < 2) {
            return sources;
        }

        Map<URI, JSSource> uniqueMap = new LinkedHashMap<URI, JSSource>();
        for (JSSource source : sources) {
            URI uriWithoutQuery = getURIWithoutQuery(source.getLocation().getURI());
            if (!uniqueMap.containsKey(uriWithoutQuery)) {
                uniqueMap.put(uriWithoutQuery, source);
            }
        }

        return uniqueMap.values().toArray(new JSSource[0]);
    }

    private URI getURIWithoutQuery(URI originalURI) {
            try {
                URI uriWithoutQuery = new URI(
                        originalURI.getScheme(),
                        originalURI.getUserInfo(),
                        originalURI.getHost(),
                        originalURI.getPort(),
                        originalURI.getPath(),
                        null,
                        originalURI.getFragment());
                return uriWithoutQuery;
            } catch (URISyntaxException ex) {
                Log.getLogger().log(Level.INFO, "Could not remove query string from URI: " + originalURI.toASCIIString());
                return originalURI;
            }
    }
    // TreeModel ...............................................................
    /**
     * Returns the root node of the tree or null, if the tree is empty.
     * @return the root node of the tree or null
     */
    public Object getRoot() {
        return ROOT;
    }

    /**
     *
     * @return threads contained in this group of threads
     */
    public Object[] getChildren(Object parent, int from, int to)
            throws UnknownTypeException {
        if (parent == ROOT) {
            if (sources == null) {
                return JSSource.EMPTY_ARRAY;
            } 
            return sources;
        } else {
            throw new UnknownTypeException(parent);
        }
    }

    /**
     * Returns number of children for given node.
     *
     * @param node
     *            the parent node
     * @throws UnknownTypeException
     *             if this TreeModel implementation is not able to resolve
     *             children for given node type
     *
     * @return true if node is leaf
     */
    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node == ROOT) {
//            if (listener == null) {
//                listener = new Listener(this);
//            }
            return (sources == null ? 0 : sources.length);
        // Performance, see issue #59058.
        // return Integer.MAX_VALUE;
        // return sourcePath.getOriginalSourceRoots ().length +
        // filters.size ();
        } else {
            throw new UnknownTypeException(node);
        }
    }

    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        } else if (node instanceof JSSource) {
            return true;
        }
        throw new UnknownTypeException(node);
    }

    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }

    public void fireTableValueChanges(Object source, Object node, String columnID) {
        for ( ModelListener modelListener : listeners){
            modelListener.modelChanged(new ModelEvent.TableValueChanged(source, node, columnID));
       }
    }
    
    public void fireTreeChanges() {
        for (ModelListener listener : listeners) {
            listener.modelChanged(new ModelEvent.TreeChanged(this));
        }
    }
    // TableModel ..............................................................
    public Object getValueAt(Object node, String columnID)
            throws UnknownTypeException {
        if (node instanceof JSSource) {
            if (columnID.equals(COLUMN_ID.USE_COLUMN.name())) {
                return ((JSSource) node).isEnabled();
            } else if (columnID.equals(COLUMN_ID.DEFAULT_SOURCES_COLUMN.name())) {
                return ((JSSource) node).getLocation().getURI().toString();
            }
        } else if (node instanceof JToolTip) {
            return "";
        }
        throw new UnknownTypeException(node);
    }

    public boolean isReadOnly(Object node, String columnID)
            throws UnknownTypeException {
//        if (columnID.equals(COLUMN_ID.USE_COLUMN.name())) {
//            return false;
//        }
//        else if (node instanceof JSSource) {
//            return true;
//        }
        return true; /* All other lines should be read only */
    }

    public void setValueAt(Object node, String columnID, Object value)
            throws UnknownTypeException {
        if (columnID.equals(COLUMN_ID.USE_COLUMN.name())) {
            if (node instanceof JSSource) {
                JSSource source = ((JSSource) node);
                source.setEnabled(((Boolean) value).booleanValue());
                return;
            }
        }
    }

    // NodeActionsProvider .....................................................
    public Action[] getActions(Object node) throws UnknownTypeException {
        if (node instanceof JSSource) {            
            return new Action[]{GO_TO_ACTION, GO_TO_CLIENT_SOURCE_ACTION};
        } 
        return new Action[]{};
    }

    public void performDefaultAction(Object node) throws UnknownTypeException {
        if (node instanceof JSSource) {
            FileObject fo = debugger.getFileObjectForSource((JSSource)node);
            NbJSEditorUtil.openFileObject(fo);
        } 
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view
     * representation.
     */
    public static class DefaultSourcesColumn extends AbstractColumnModel {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID() {
            return COLUMN_ID.DEFAULT_SOURCES_COLUMN.name();
        }

        /**
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName() {
            return NbBundle.getBundle(DefaultSourcesColumn.class).getString(
                    "CTL_SourcesModel_Column_Name_Name");
        }

        public Character getDisplayedMnemonic() {
            return new Character(NbBundle.getBundle(NbJSSourcesModel.class).getString("CTL_SourcesModel_Column_Name_Name_Mnc").charAt(
                    0));
        }

        /**
         * Returns tooltip for given column.
         *
         * @return tooltip for given node
         */
        @Override
        public String getShortDescription() {
            return NbBundle.getBundle(DefaultSourcesColumn.class).getString(
                    "CTL_SourcesModel_Column_Name_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType() {
            return null;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view
     * representation.
     */
    public static class SourcesUsedColumn extends AbstractColumnModel {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID() {
            return COLUMN_ID.USE_COLUMN.name();
        }

        /**
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName() {
            return NbBundle.getBundle(NbJSSourcesModel.class).getString(
                    "CTL_SourcesModel_Column_Debugging_Name");
        }

        public Character getDisplayedMnemonic() {
            return new Character(NbBundle.getBundle(NbJSSourcesModel.class).getString("CTL_SourcesModel_Column_Debugging_Name_Mnc").charAt(0));
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType() {
            return Boolean.TYPE;
        }

        /**
         * Returns tooltip for given column. Default implementation returns
         * <code>null</code> - do not use tooltip.
         *
         * @return tooltip for given node or <code>null</code>
         */
        public String getShortDescription() {
            return NbBundle.getBundle(NbJSSourcesModel.class).getString(
                    "CTL_SourcesModel_Column_Debugging_Desc");
        }

        /**
         * True if column should be visible by default. Default implementation
         * returns <code>true</code>.
         *
         * @return <code>true</code> if column should be visible by default
         */
        public boolean initiallyVisible() {
            return true;
        }
    }

    public void onDebuggerEvent(JSDebuggerEvent debuggerEvent) {
        JSDebuggerState jsDebuggerState = debuggerEvent.getDebuggerState();
        NbJSDebugger debugger = (NbJSDebugger) debuggerEvent.getSource();

        switch (jsDebuggerState.getState()) {
            case SUSPENDED:
                break;
            case RUNNING:
            case STARTING:
                break;
            case DISCONNECTED:
                debugger.removeJSDebuggerEventListener(this);
                break;
            default:
        }
        fireTreeChanges();
    }


    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return "";
        } else if (node instanceof JSSource) {
            JSSource source = (JSSource) node;
            final FileObject fileObjectForSource = debugger.getFileObjectForSource(source);
            if ( fileObjectForSource == null ){
                String uriString = source.getLocation().getURI().toString();
                Log.getLogger().warning("The File Object for the following source is null:" + uriString);
                return uriString;
            }
            return (fileObjectForSource instanceof URLFileObject ? 
                ((URLFileObject)fileObjectForSource).getDisplayName() : FileUtil.getFileDisplayName(fileObjectForSource));
        }
        throw new UnknownTypeException(node);
    }

    public String getIconBase(Object node) throws UnknownTypeException {
        return "org/openide/nodes/defaultNode";

    }

    public String getShortDescription(Object node) throws UnknownTypeException {
        return getDisplayName(node);
    }
}
