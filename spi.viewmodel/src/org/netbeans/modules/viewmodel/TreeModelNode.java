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

package org.netbeans.modules.viewmodel;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.lang.ref.WeakReference;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter.CALL;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.TreeFeatures;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.swing.etable.ETableColumn;

import org.openide.awt.Actions;
import org.openide.explorer.view.CheckableNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author   Jan Jancura
 */
public class TreeModelNode extends AbstractNode {

    /**
     * The maximum length of text that is interpreted as HTML.
     * This is documented at openide/explorer/src/org/openide/explorer/doc-files/propertyViewCustomization.html
     */
    private static final int MAX_HTML_LENGTH = 511;
    
    // variables ...............................................................

    private Models.CompoundModel model;
    private ColumnModel[]        columns;
    protected TreeModelRoot      treeModelRoot;
    protected Object             object;
    
    private String              htmlDisplayName;
    private String              shortDescription;
    private final Object        shortDescriptionLock = new Object();
    private final Map<String, Object> properties = new HashMap<String, Object>();
    private static final String EVALUATING_STR = NbBundle.getMessage(TreeModelNode.class, "EvaluatingProp");

    
    // init ....................................................................

    /**
    * Creates root of call stack for given producer.
    */
    public TreeModelNode (
        final Models.CompoundModel model, 
        final TreeModelRoot treeModelRoot,
        final Object object
    ) {
        this(
            model,
            model.getColumns (),
            treeModelRoot,
            object
        );
    }

    /**
    * Creates root of call stack for given producer.
    */
    public TreeModelNode (
        final Models.CompoundModel model,
        final ColumnModel[] columns,
        final TreeModelRoot treeModelRoot,
        final Object object
    ) {
        this(
            model,
            columns,
            createChildren (model, columns, treeModelRoot, object),
            treeModelRoot,
            object
        );
    }

    /**
    * Creates root of call stack for given producer.
    */
    protected TreeModelNode (
        final Models.CompoundModel model,
        final ColumnModel[] columns,
        final Children children,
        final TreeModelRoot treeModelRoot,
        final Object object
    ) {
        this(
            model,
            columns,
            children,
            treeModelRoot,
            object,
            new Index[] { null });
    }

    private TreeModelNode (
        final Models.CompoundModel model,
        final ColumnModel[] columns,
        final Children children,
        final TreeModelRoot treeModelRoot,
        final Object object,
        final Index[] indexPtr  // Hack, because we can not declare variables before call to super() :-(
    ) {
        super (
            children,
            createLookup(object, model, children, indexPtr)
        );
        this.model = model;
        this.treeModelRoot = treeModelRoot;
        this.object = object;
        if (indexPtr[0] != null) {
            ((IndexImpl) indexPtr[0]).setNode(this);
            setIndexWatcher(indexPtr[0]);
        }
        
        // <RAVE>
        // Use the modified CompoundModel class's field to set the 
        // propertiesHelpID for properties sheets if the model's helpID
        // has been set
        if (model.getHelpId() != null) {
            this.setValue("propertiesHelpID", model.getHelpId()); // NOI18N
        }
        // </RAVE>
        
        treeModelRoot.registerNode (object, this); 
        refreshNode ();
        initProperties (columns);
    }

    private static Lookup createLookup(Object object, Models.CompoundModel model,
                                       Children ch, Index[] indexPtr) {
        CheckNodeCookieImpl cnc = new CheckNodeCookieImpl(model, object);
        boolean canReorder;
        try {
            canReorder = model.canReorder(object);
        } catch (UnknownTypeException ex) {
            if (!(object instanceof String)) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, ex);
            }
            canReorder = false;
        }
        if (canReorder) {
            Index i = new IndexImpl(model, object);
            indexPtr[0] = i;
            return Lookups.fixed(object, cnc, i);
        } else {
            return Lookups.fixed(object, cnc);
        }
    }

    private void setIndexWatcher(Index childrenIndex) {
        childrenIndex.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Children ch = getChildren();
                if (ch instanceof TreeModelChildren) {
                    ((TreeModelChildren) ch).refreshChildren(new TreeModelChildren.RefreshingInfo(false));
                }
            }
        });
    }

    private static Executor asynchronous(Models.CompoundModel model, CALL asynchCall, Object object) {
        Executor exec;
        try {
            exec = model.asynchronous(asynchCall, object);
            //System.err.println("Asynchronous("+asynchCall+", "+object+") = "+exec);
            if (exec == null) {
                Exceptions.printStackTrace(Exceptions.attachMessage(new NullPointerException("Provided executor is null."), "model = "+model+", object = "+object));
                exec = AsynchronousModelFilter.CURRENT_THREAD;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(Exceptions.attachMessage(ex, "model = "+model+", object = "+object));
            exec = AsynchronousModelFilter.CURRENT_THREAD;
        }
        return exec;
    }


    // Node implementation .....................................................
    
    private void initProperties (ColumnModel[] columns) {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet ();
        this.columns = columns;
        int i, k = columns.length;
        for (i = 0; i < k; i++)
            ps.put (new MyProperty (columns [i], treeModelRoot));
        sheet.put (ps);
        setSheet (sheet);
    }
    
    private static Children createChildren (
        Models.CompoundModel model,
        ColumnModel[] columns,
        TreeModelRoot treeModelRoot,
        Object object
    ) {
        if (object == null) 
            throw new NullPointerException ();
        try {
            return model.isLeaf (object) ? 
                Children.LEAF :
                new TreeModelChildren (model, columns, treeModelRoot, object);
        } catch (UnknownTypeException e) {
            if (!(object instanceof String)) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            }
            return Children.LEAF;
        }
    }

    @Override
    public String getShortDescription () {
        synchronized (shortDescriptionLock) {
            if (shortDescription != null) {
                return shortDescription;
            }
        }
        Executor exec = asynchronous(model, CALL.SHORT_DESCRIPTION, object);
        if (exec == AsynchronousModelFilter.CURRENT_THREAD) {
            return updateShortDescription();
        } else {
            exec.execute(new Runnable() {
                public void run() {
                    updateShortDescription();
                    fireShortDescriptionChange(null, null);
                }
            });
            return EVALUATING_STR;
        }
    }

    private String updateShortDescription() {
        try {
            String sd = model.getShortDescription (object);
            if (sd != null) {
                sd = adjustHTML(sd);
            }
            synchronized (shortDescriptionLock) {
                shortDescription = sd;
            }
            return sd;
        } catch (UnknownTypeException e) {
            if (!(object instanceof String)) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            }
            return null;
        }
    }

    private void doFireShortDescriptionChange() {
        synchronized (shortDescriptionLock) {
            shortDescription = null;
        }
        fireShortDescriptionChange(null, null);
    }
    
    @Override
    public String getHtmlDisplayName () {
        return htmlDisplayName;
    }
    
    @Override
    public Action[] getActions (boolean context) {
        if (context) 
            return treeModelRoot.getRootNode ().getActions (false);
        try {
            return filterActionsWhenSorted(model.getActions (object));
        } catch (UnknownTypeException e) {
            // NodeActionsProvider is voluntary
            return new Action [0];
        }
    }

    private boolean isTableSorted() {
        TableColumnModel tcm = treeModelRoot.getOutlineView().getOutline().getColumnModel();
        Enumeration<TableColumn> cen = tcm.getColumns();
        while (cen.hasMoreElements()) {
            ETableColumn etc = (ETableColumn) cen.nextElement();
            if (etc.isSorted()) {
                return true;
            }
        }
        return false;
    }

    private Action[] filterActionsWhenSorted(Action[] actions) {
        if (actions == null || actions.length == 0) {
            return actions;
        }
        for (int i = 0; i < actions.length; i++) {
            Action a = actions[i];
            if (a == null) continue;
            boolean disabled = Boolean.TRUE.equals(a.getValue("DisabledWhenInSortedTable"));    // NOI18N
            if (disabled) {
                actions[i] = new DisabledWhenSortedAction(a);
            }
        }
        return actions;
    }

    @Override
    public Action getPreferredAction () {
        return new AbstractAction () {
            public void actionPerformed (ActionEvent e) {
                try {
                    model.performDefaultAction (object);
                } catch (UnknownTypeException ex) {
                    // NodeActionsProvider is voluntary
                }
            }
        };
    }
    
    @Override
    public boolean canDestroy () {
        try {
            Action[] as = model.getActions (object);
            int i, k = as.length;
            for (i = 0; i < k; i++) {
                if (as [i] == null) continue;
                Object key = as [i].getValue (Action.ACCELERATOR_KEY);
                if ( (key != null) &&
                     (key.equals (KeyStroke.getKeyStroke ("DELETE")))
                ) return as [i].isEnabled ();
            }
            return false;
        } catch (UnknownTypeException e) {
            // NodeActionsProvider is voluntary
            return false;
        }
    }
    
    @Override
    public boolean canCopy () {
        try {
            return model.canCopy(object);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            return false;
        }
    }
    
    @Override
    public boolean canCut () {
        try {
            return model.canCut(object);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            return false;
        }
    }
    
    @Override
    public void destroy () {
        try {
            Action[] as = model.getActions (object);
            int i, k = as.length;
            for (i = 0; i < k; i++) {
                if (as [i] == null) continue;
                Object key = as [i].getValue (Action.ACCELERATOR_KEY);
                if ( (key != null) &&
                     (key.equals (KeyStroke.getKeyStroke ("DELETE")))
                ) {
                    as [i].actionPerformed (null);
                    return;
                }
            }
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
        }
        if (model.getRoot() == object) {
            treeModelRoot.destroy();
        }
    }

    
    // other methods ...........................................................
    
    void setObject (Object o) {
        setObject(model, o);
    }
    
    void setObject (Models.CompoundModel model, Object o) {
        setObjectNoRefresh (o);
        refresh (model);
    }

    private void setObjectNoRefresh (Object o) {
        object = o;
        Children ch = getChildren ();
        if (ch instanceof TreeModelChildren)
            ((TreeModelChildren) ch).object = o;
    }
    
    public Object getObject () {
        return object;
    }

    private final Map<Models.CompoundModel, Task> tasksByModels = new HashMap<Models.CompoundModel, Task>();
    
    void refresh (final Models.CompoundModel model) {
        //System.err.println("TreeModelNode.refresh("+model+") on "+object);
        //Thread.dumpStack();
        // 1) empty cache
        synchronized (properties) {
            properties.clear();
        }
        
        
        // 2) refresh name, displayName and iconBase
        synchronized (tasksByModels) {
            Task task = tasksByModels.get(model);
            if (task == null) {
                task = getRequestProcessor ().create (new Runnable () {
                    public void run () {
                        if (!SwingUtilities.isEventDispatchThread()) {
                            try {
                                SwingUtilities.invokeAndWait(this);
                            } catch (InterruptedException ex) {
                            } catch (InvocationTargetException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            return ;
                        }
                        refreshNode ();
                        doFireShortDescriptionChange();

                        // 3) refresh children
                        refreshTheChildren(model, new TreeModelChildren.RefreshingInfo(true));
                    }
                });
                tasksByModels.put(model, task);
            }
            task.schedule(0);
        }
    }
    
    void refresh (final Models.CompoundModel model, int changeMask) {
        if (changeMask == 0xFFFFFFFF) {
            refresh(model);
            return ;
        }
        boolean refreshed = false;
        if ((ModelEvent.NodeChanged.DISPLAY_NAME_MASK & changeMask) != 0) {
            try {
                setModelDisplayName();
            } catch (UnknownTypeException e) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            }
            refreshed = true;
        }
        if ((ModelEvent.NodeChanged.ICON_MASK & changeMask) != 0) {
            try {
                String iconBase = model.getIconBaseWithExtension (object);
                if (iconBase != null)
                    setIconBaseWithExtension (iconBase);
                else
                    setIconBaseWithExtension ("org/openide/resources/actions/empty.gif");
            } catch (UnknownTypeException e) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            }
            refreshed = true;
        }
        if ((ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK & changeMask) != 0) {
            doFireShortDescriptionChange();
            refreshed = true;
        }
        if ((ModelEvent.NodeChanged.CHILDREN_MASK & changeMask) != 0) {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    refreshTheChildren(model, new TreeModelChildren.RefreshingInfo(false));
                }
            });
            refreshed = true;
        }
        if ((ModelEvent.NodeChanged.EXPANSION_MASK & changeMask) != 0) {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    expandIfSetToExpanded();
                }
            });
        }
        if (!refreshed) {
            refresh(model);
        }
    }
    
    private static RequestProcessor requestProcessor;
    // Accessed from test
    RequestProcessor getRequestProcessor () {
        /*RequestProcessor rp = treeModelRoot.getRequestProcessor();
        if (rp != null) {
            return rp;
        }*/
        synchronized (TreeModelNode.class) {
            if (requestProcessor == null)
                requestProcessor = new RequestProcessor ("TreeModel", 1);
            return requestProcessor;
        }
    }

    private void setName (String name, boolean italics) {
        // XXX HACK: HTMLDisplayName is missing in the models!
        String oldHtmlDisplayName = htmlDisplayName;
        String oldDisplayName = getDisplayName();
        
        String newDisplayName;
        if (name.startsWith ("<html>")) {
            htmlDisplayName = name;
            newDisplayName = removeHTML(name);
        } else if (name.startsWith ("<_html>")) { //[TODO] use empty string as name in the case of <_html> tag
            htmlDisplayName = '<' + name.substring(2);
            newDisplayName = "";
        } else {
            htmlDisplayName = null;
            newDisplayName = name;
        }
        if ((oldDisplayName == null) || !oldDisplayName.equals(newDisplayName)) {
            setDisplayName(newDisplayName);
        } else {
            if (oldHtmlDisplayName != null && !oldHtmlDisplayName.equals(htmlDisplayName) ||
                htmlDisplayName != null && !htmlDisplayName.equals(oldHtmlDisplayName)) {
                
                // Display names are equal, but HTML display names differ!
                // We hope that this is sufficient to refresh the HTML display name:
                fireDisplayNameChange(oldDisplayName + "_HACK", getDisplayName());
            }
        }
    }

    private void setModelDisplayName() throws UnknownTypeException {
        Executor exec = asynchronous(model, CALL.DISPLAY_NAME, object);
        if (exec == AsynchronousModelFilter.CURRENT_THREAD) {
            String name = model.getDisplayName (object);
            if (name == null) {
                Throwable t =
                    new NullPointerException (
                        "Model: " + model + ".getDisplayName (" + object +
                        ") = null!"
                    );
                Exceptions.printStackTrace(t);
            } else {
                setName (name, false);
            }
        } else {
            final String originalDisplayName = getDisplayName();
            setName(EVALUATING_STR, false);
            exec.execute(new Runnable() {
                public void run() {
                    String name;
                    try {
                        name = model.getDisplayName(object);
                    } catch (UnknownTypeException ex) {
                        Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, ex);
                        setName(originalDisplayName, false);
                        return ;
                    }
                    if (name == null) {
                        Throwable t =
                            new NullPointerException (
                                "Model: " + model + ".getDisplayName (" + object +
                                ") = null!"
                            );
                        Exceptions.printStackTrace(t);
                        setName(originalDisplayName, false);
                    } else {
                        setName (name, false);
                    }
                }
            });
        }
    }
    
    private void refreshNode () {
        try {
            setModelDisplayName();
            String iconBase = null;
            if (model.getRoot() != object) {
                iconBase = model.getIconBaseWithExtension (object);
            }
            if (iconBase != null)
                setIconBaseWithExtension (iconBase);
            else
                setIconBaseWithExtension ("org/openide/resources/actions/empty.gif");
            firePropertyChange(null, null, null);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
        }
    }
    
    void refreshColumn(String column) {
        synchronized (properties) {
            properties.remove(column);
            properties.remove(column + "#html");
        }
        firePropertyChange(column, null, null);
    }

    /**
     * @param model The associated model - necessary for hyper node.
     * @param refreshSubNodes If recursively refresh subnodes.
     */
    protected void refreshTheChildren(Models.CompoundModel model, TreeModelChildren.RefreshingInfo refreshInfo) {
        Children ch = getChildren();
        try {
            if (ch instanceof TreeModelChildren) {
                if (model.isLeaf(object)) {
                    setChildren(Children.LEAF);
                } else {
                    ((TreeModelChildren) ch).refreshChildren(refreshInfo);
                }
            } else if (!model.isLeaf (object)) {
                setChildren(new TreeModelChildren (model, columns, treeModelRoot, object));
            }
        } catch (UnknownTypeException utex) {
            // not known - do not change children
            if (!(object instanceof String)) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, utex);
            }
            setChildren(Children.LEAF);
        }
    }
    
    private static String htmlValue (String name) {
        if (!(name.length() > 6 && name.substring(0, 6).equalsIgnoreCase("<html>"))) return null;
        if (name.length() > MAX_HTML_LENGTH) {
            int endTagsPos = findEndTagsPos(name);
            String ending = name.substring(endTagsPos + 1);
            name = name.substring(0, MAX_HTML_LENGTH - 3 - ending.length());
            // Check whether we haven't cut "&...;" in between:
            int n = name.length();
            for (int i = n - 1; i > n - 6; i--) {
                if (name.charAt(i) == ';') {
                    break; // We have an end of the group
                }
                if (name.charAt(i) == '&') {
                    name = name.substring(0, i);
                    break;
                }
            }
            name += "..." + ending;
        }
        return adjustHTML(name);
    }
    
    private static int findEndTagsPos(String s) {
        int openings = 0;
        int i;
        for (i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '>') openings++;
            else if (s.charAt(i) == '<') openings--;
            else if (openings == 0) break;
        }
        return i;
    }
    
    private static String removeHTML (String text) {
        if (!(text.length() > 6 && text.substring(0, 6).equalsIgnoreCase("<html>"))) {
            return text;
        }
        text = text.replaceAll ("<i>", "");
        text = text.replaceAll ("</i>", "");
        text = text.replaceAll ("<b>", "");
        text = text.replaceAll ("</b>", "");
        text = text.replaceAll ("<html>", "");
        text = text.replaceAll ("</html>", "");
        text = text.replaceAll ("</font>", "");
        int i = text.indexOf ("<font");
        while (i >= 0) {
            int j = text.indexOf (">", i);
            text = text.substring (0, i) + text.substring (j + 1);
            i = text.indexOf ("<font");
        }
        text = text.replaceAll ("&lt;", "<");
        text = text.replaceAll ("&gt;", ">");
        text = text.replaceAll ("&amp;", "&");
        return text;
    }
    
    /** Adjusts HTML text so that it's rendered correctly.
     * In particular, this assures that white characters are visible.
     */
    private static String adjustHTML(String text) {
        text = text.replaceAll(java.util.regex.Matcher.quoteReplacement("\\"), "\\\\\\\\");
        StringBuffer sb = null;
        int j = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String replacement = null;
            if (c == '\n') {
                replacement = "\\n";
            } else if (c == '\r') {
                replacement = "\\r";
            } else if (c == '\f') {
                replacement = "\\f";
            } else if (c == '\b') {
                replacement = "\\b";
            }
            if (replacement != null) {
                if (sb == null) {
                    sb = new StringBuffer(text.substring(0, i));
                } else {
                    sb.append(text.substring(j, i));
                }
                sb.append(replacement);
                j = i+1;
            }
        }
        if (sb == null) {
            return text;
        } else {
            sb.append(text.substring(j));
            return sb.toString();
        }
    }
    
    
    @Override
    public boolean canRename() {
        try {
            return model.canRename(object);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            return false;
        }
    }

    @Override
    public void setName(String s) {
        try {
            model.setName(object, s);
            super.setName(s);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
        }
    }
    
    @Override
    public Transferable clipboardCopy() throws IOException {
        Transferable t;
        try {
            t = model.clipboardCopy(object);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            t = null;
        }
        if (t == null) {
            return super.clipboardCopy();
        } else {
            return t;
        }
    }
    
    @Override
    public Transferable clipboardCut() throws IOException {
        Transferable t;
        try {
            t = model.clipboardCut(object);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            t = null;
        }
        if (t == null) {
            return super.clipboardCut();
        } else {
            return t;
        }
    }
    
    @Override
    public Transferable drag() throws IOException {
        Transferable t;
        try {
            t = model.drag(object);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            t = null;
        }
        if (t == null) {
            return super.drag();
        } else {
            return t;
        }
    }
    
    @Override
    public void createPasteTypes(Transferable t, List<PasteType> l) {
        PasteType[] p;
        try {
            p = model.getPasteTypes(object, t);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            p = null;
        }
        if (p == null) {
            super.createPasteTypes(t, l);
        } else {
            l.addAll(Arrays.asList(p));
        }
    }
    
    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        PasteType p;
        try {
            p = model.getDropType(object, t, action, index);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            p = null;
        }
        if (p == null) {
            return super.getDropType(t, action, index);
        } else {
            return p;
        }
    }
    
    private final void expandIfSetToExpanded() {
        try {
            DefaultTreeExpansionManager.get(model).setChildrenToActOn(getTreeDepth());
            if (model.isExpanded (object)) {
                TreeFeatures treeTable = treeModelRoot.getTreeFeatures ();
                if (treeTable != null) {
                    treeTable.expandNode (object);
                }
            }
        } catch (UnknownTypeException ex) {
        }
    }

    private Integer depth;

    private Integer getTreeDepth() {
        Node p = getParentNode();
        if (p == null) {
            return 0;
        } else if (depth != null) {
            return depth;
        } else {
            int d = 1;
            while ((p = p.getParentNode()) != null) d++;
            depth = new Integer(d);
            return depth;
        }
    }

    // innerclasses ............................................................

    private class DisabledWhenSortedAction implements Action {

        private Action a;

        public DisabledWhenSortedAction(Action a) {
            this.a = a;
        }

        @Override
        public Object getValue(String key) {
            return a.getValue(key);
        }

        @Override
        public void putValue(String key, Object value) {
            a.putValue(key, value);
        }

        @Override
        public void setEnabled(boolean b) {
            a.setEnabled(b);
        }

        @Override
        public boolean isEnabled() {
            if (isTableSorted()) {
                return false;
            } else {
                return a.isEnabled();
            }
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            a.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            a.removePropertyChangeListener(listener);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            a.actionPerformed(e);
        }
    }

    private static final class CheckNodeCookieImpl implements CheckableNode {

        private final Models.CompoundModel model;
        private final Object object;

        public CheckNodeCookieImpl(Models.CompoundModel model, Object object) {
            this.model = model;
            this.object = object;
        }

        public boolean isCheckable() {
            try {
                return model.isCheckable(object);
            } catch (UnknownTypeException ex) {
                Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Model = "+model));
                return false;
            }
        }

        public boolean isCheckEnabled() {
            try {
                return model.isCheckEnabled(object);
            } catch (UnknownTypeException ex) {
                Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Model = "+model));
                return false;
            }
        }

        public Boolean isSelected() {
            try {
                return model.isSelected(object);
            } catch (UnknownTypeException ex) {
                Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Model = "+model));
                return false;
            }
        }

        public void setSelected(Boolean selected) {
            try {
                model.setSelected(object, selected);
            } catch (UnknownTypeException ex) {
                Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Model = "+model));
            }
        }

    }
    
    /** Special locals subnodes (children) */
    static class TreeModelChildren extends Children.Keys<Object>
                                   implements Runnable {// LazyEvaluator.Evaluable {
            
        private boolean             initialezed = false;
        private final Models.CompoundModel model;
        private final ColumnModel[]   columns;
        protected final TreeModelRoot      treeModelRoot;
        protected Object            object;
        protected WeakHashMap<Object, WeakReference<TreeModelNode>> objectToNode = new WeakHashMap<Object, WeakReference<TreeModelNode>>();
        private final int[]         evaluated = { 0 }; // 0 - not yet, 1 - evaluated, -1 - timeouted
        private RefreshingInfo      evaluatingRefreshingInfo;
        private Object[]            children_evaluated;
        private RefreshingInfo      refreshInfo = null;
        private boolean             refreshingStarted = true;

        private RequestProcessor.Task   task;
        private RequestProcessor        lastRp;
        
        protected static final Object WAIT_KEY = new Object();
        
        
        TreeModelChildren (
            Models.CompoundModel model,
            ColumnModel[]   columns,
            TreeModelRoot   treeModelRoot,
            Object          object
        ) {
            this.model = model;
            this.columns = columns;
            this.treeModelRoot = treeModelRoot;
            this.object = object;
        }
        
        @Override
        protected void addNotify () {
            initialezed = true;
            refreshChildren (new RefreshingInfo(true));
        }
        
        @Override
        protected void removeNotify () {
            initialezed = false;
            setKeys (Collections.emptySet());
        }
        
        void refreshChildren (RefreshingInfo refreshSubNodes) {
            if (!initialezed) return;

            refreshLazyChildren(refreshSubNodes);
        }
        
        public void run() {
            RefreshingInfo rinfo;
            synchronized (evaluated) {
                refreshingStarted = false;
                rinfo = refreshInfo;
                if (evaluatingRefreshingInfo == null) {
                    evaluatingRefreshingInfo = refreshInfo;
                } else {
                    if (refreshInfo != null) {
                        evaluatingRefreshingInfo = evaluatingRefreshingInfo.mergeWith(refreshInfo);
                    }
                }
                refreshInfo = null; // reset after use
            }
            Object[] ch;
            try {
                ch = getModelChildren(rinfo);
            } catch (UnknownTypeException e) {
                ch = new Object [0];
                if (!(object instanceof String)) {
                    Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
                }
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                // recover from defect in getChildren()
                // Otherwise there would remain "Please wait..." node.
                Exceptions.printStackTrace(t);
                ch = new Object[0];
            }
            //evaluatedNotify.run();
            boolean fire;
            synchronized (evaluated) {
                int eval = evaluated[0];
                if (refreshingStarted) {
                    fire = false;
                } else {
                    fire = evaluated[0] == -1;
                    if (!fire) {
                        children_evaluated = ch;
                    } else {
                        evaluatingRefreshingInfo = null;
                    }
                    evaluated[0] = 1;
                    evaluated.notifyAll();
                }
                //System.err.println(this.hashCode()+" evaluateLazily() ready, evaluated[0] = "+eval+" => fire = "+fire+", refreshingStarted = "+refreshingStarted+", children_evaluated = "+(children_evaluated != null));
            }
            if (fire) {
                applyChildren(ch, rinfo);
            }
        }

        protected Object[] getModelChildren(RefreshingInfo refreshInfo) throws UnknownTypeException {
            int count = model.getChildrenCount (object);
            return model.getChildren (
                object,
                0,
                count
            );
        }

        protected Executor getModelAsynchronous() {
            return asynchronous(model, CALL.CHILDREN, object);
        }
        
        private void refreshLazyChildren (RefreshingInfo refreshInfo) {
            Executor exec = getModelAsynchronous();
            if (exec == AsynchronousModelFilter.CURRENT_THREAD) {
                Object[] ch;
                try {
                    ch = getModelChildren(refreshInfo);
                } catch (UnknownTypeException ex) {
                    ch = new Object [0];
                    if (!(object instanceof String)) {
                        Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, ex);
                    }
                }
                applyChildren(ch, refreshInfo);
                return ;
            }
            synchronized (evaluated) {
                evaluated[0] = 0;
                refreshingStarted = true;
                if (this.refreshInfo == null) {
                    this.refreshInfo = refreshInfo;
                } else {
                    this.refreshInfo = this.refreshInfo.mergeWith(refreshInfo);
                }
                //System.err.println(this.hashCode()+" refreshLazyChildren() started = true, evaluated = 0");
            }
            /*if (exec instanceof RequestProcessor) {
                // Have a single task for RP
                RequestProcessor rp = (RequestProcessor) exec;
                if (rp != lastRp) {
                    task = rp.create(this);
                    lastRp = rp;
                }
                task.schedule(0);
            } else {*/
                exec.execute(this);
            //}
            // It's refresh => do not check for this children already being evaluated
            //treeModelRoot.getChildrenEvaluator().evaluate(this, false);
            Object[] ch;
            synchronized (evaluated) {
                if (evaluated[0] != 1) {
                    try {
                        evaluated.wait(getChildrenRefreshWaitTime());
                    } catch (InterruptedException iex) {}
                    if (evaluated[0] != 1) {
                        evaluated[0] = -1; // timeout
                        ch = null;
                    } else {
                        ch = children_evaluated;
                    }
                } else {
                    ch = children_evaluated;
                }
                //System.err.println(this.hashCode()+" refreshLazyChildren() ending, evaluated[0] = "+evaluated[0]+", refreshingStarted = "+refreshingStarted+", children_evaluated = "+(children_evaluated != null)+", ch = "+(ch != null));
                // Do nothing when it's evaluated, but already unset.
                if (children_evaluated == null && evaluated[0] == 1) return;
                children_evaluated = null;
                if (ch != null) {
                    refreshInfo = evaluatingRefreshingInfo;
                    evaluatingRefreshingInfo = null;
                    //refreshInfo = this.refreshInfo;
                    //this.refreshInfo = null;
                }
            }
            if (ch == null) {
                applyWaitChildren();
            } else {
                applyChildren(ch, refreshInfo);
            }
        }

        private static AtomicLong lastChildrenRefresh = new AtomicLong(0);
        
        private static long getChildrenRefreshWaitTime() {
            long now = System.currentTimeMillis();
            long last = lastChildrenRefresh.getAndSet(now);
            if ((now - last) < 1000) {
                // Refreshes in less than a second - the system needs to respond fast
                return 1;
            } else {
                return 200;
            }
        }
        
        private void applyChildren(final Object[] ch, RefreshingInfo refreshInfo) {
            //System.err.println(this.hashCode()+" applyChildren("+refreshSubNodes+")");
            //System.err.println("applyChildren("+Arrays.toString(ch)+")");
            int i, k = ch.length; 
            WeakHashMap<Object, WeakReference<TreeModelNode>> newObjectToNode = new WeakHashMap<Object, WeakReference<TreeModelNode>>();
            for (i = 0; i < k; i++) {
                if (ch [i] == null) {
                    throw new NullPointerException("Null child at index "+i+", parent: "+object+", model: "+model);
                }
                WeakReference<TreeModelNode> wr = objectToNode.get(ch [i]);
                if (wr == null) continue;
                TreeModelNode tmn = wr.get ();
                if (tmn == null) continue;
                if (refreshInfo == null || refreshInfo.isRefreshSubNodes(ch[i])) {
                    tmn.setObject (ch [i]);
                } else {
                    tmn.setObjectNoRefresh(ch[i]);
                }
                newObjectToNode.put (ch [i], wr);
            }
            objectToNode = newObjectToNode;
            setKeys (ch);

            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    int i, k = ch.length;
                    for (i = 0; i < k; i++)
                        expandIfSetToExpanded(ch[i]);
                }
            });
        }
        
        protected void expandIfSetToExpanded(Object child) {
            try {
                DefaultTreeExpansionManager.get(model).setChildrenToActOn(getTreeDepth());
                if (model.isExpanded (child)) {
                    TreeFeatures treeTable = treeModelRoot.getTreeFeatures ();
                    if (treeTable != null && treeTable.isExpanded(object)) {
                        // Expand the child only if the parent is expanded
                        treeTable.expandNode (child);
                    }
                }
            } catch (UnknownTypeException ex) {
            }
        }
        
        private Integer depth;
        
        Integer getTreeDepth() {
            Node p = getNode();
            if (p == null) {
                return 0;
            } else if (depth != null) {
                return depth;
            } else {
                int d = 1;
                while ((p = p.getParentNode()) != null) d++;
                depth = new Integer(d);
                return depth;
            }
        }
        
        private void applyWaitChildren() {
            //System.err.println(this.hashCode()+" applyWaitChildren()");
            setKeys(new Object[] { WAIT_KEY });
        }
        
//        protected void destroyNodes (Node[] nodes) {
//            int i, k = nodes.length;
//            for (i = 0; i < k; i++) {
//                TreeModelNode tmn = (TreeModelNode) nodes [i];
//                String name = null;
//                try {
//                    name = model.getDisplayName (tmn.object);
//                } catch (UnknownTypeException e) {
//                }
//                if (name != null)
//                    nameToChild.remove (name);
//            }
//        }
        
        public Node[] createNodes (Object object) {
            if (object == WAIT_KEY) {
                AbstractNode n = new AbstractNode(Children.LEAF);
                n.setName(NbBundle.getMessage(TreeModelNode.class, "WaitNode"));
                n.setIconBaseWithExtension("org/netbeans/modules/viewmodel/wait.gif");
                return new Node[] { n };
            }
            if (object instanceof Exception)
                return new Node[] {
                    new ExceptionNode ((Exception) object)
                };
            TreeModelNode tmn = new TreeModelNode (
                model,
                columns,
                treeModelRoot, 
                object
            );
            objectToNode.put (object, new WeakReference<TreeModelNode>(tmn));
            return new Node[] {tmn};
        }

        public static class RefreshingInfo {

            protected boolean refreshSubNodes;

            public RefreshingInfo(boolean refreshSubNodes) {
                this.refreshSubNodes = refreshSubNodes;
            }

            public RefreshingInfo mergeWith(RefreshingInfo rinfo) {
                this.refreshSubNodes = this.refreshSubNodes || rinfo.refreshSubNodes;
                return this;
            }

            public boolean isRefreshSubNodes(Object child) {
                return refreshSubNodes;
            }
        }
    } // TreeModelChildren

    private static final class IndexImpl extends Index.Support {

        private Models.CompoundModel model;
        private Object object;
        private Node node;

        IndexImpl(Models.CompoundModel model, Object object) {
            this.model = model;
            this.object = object;
        }

        void setNode(Node node) {
            this.node = node;
        }

        @Override
        public Node[] getNodes() {
            return node.getChildren().getNodes();
        }

        @Override
        public int getNodesCount() {
            return node.getChildren().getNodesCount();
        }

        @Override
        public void reorder(int[] perm) {
            try {
                model.reorder(object, perm);
                fireChangeEvent(new ChangeEvent(this));
            } catch (UnknownTypeException ex) {
                if (!(object instanceof String)) {
                    Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, ex);
                }
            }
        }

        void fireChange() {
            fireChangeEvent(new ChangeEvent(this));
        }

    }

    private class MyProperty extends PropertySupport implements Runnable { //LazyEvaluator.Evaluable {
        
        private String      id;
        private String      propertyId;
        private ColumnModel columnModel;
        private boolean nodeColumn;
        private TreeModelRoot treeModelRoot;
        private final int[] evaluated = { 0 }; // 0 - not yet, 1 - evaluated, -1 - timeouted
        
        private RequestProcessor.Task   task;
        private RequestProcessor        lastRp;
        
        
        MyProperty (
            ColumnModel columnModel, TreeModelRoot treeModelRoot
        ) {
            super (
                columnModel.getID (),
                (columnModel.getType() == null) ? String.class : columnModel.getType (),
                Actions.cutAmpersand(columnModel.getDisplayName ()),
                columnModel.getShortDescription (), 
                true,
                true
            );
            this.nodeColumn = columnModel.getType() == null;
            this.treeModelRoot = treeModelRoot;
            if (columnModel instanceof HyperColumnModel) {
                propertyId = columnModel.getID(); // main column ID
                this.columnModel = ((HyperColumnModel) columnModel).getSpecific();
                id = this.columnModel.getID ();   // specific column ID
            } else {
                id = propertyId = columnModel.getID ();
                this.columnModel = columnModel;
            }
        }
        

        /* Can write the value of the property.
        * Returns the value passed into constructor.
        * @return <CODE>true</CODE> if the read of the value is supported
        */
        @Override
        public boolean canWrite () {
            if (nodeColumn) return false;
            try {
                return !model.isReadOnly (object, columnModel.getID ());
            } catch (UnknownTypeException e) {
                if (!(object instanceof String)) {
                    Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Column id:" + columnModel.getID ()+"\nModel: "+model, e);
                }
                return false;
            }
        }
        
        public void run() {
            Object value = "";
            String htmlValue = null;
            String nonHtmlValue = null;
            try {
                value = model.getValueAt (object, id);
                //System.err.println("  Value of ("+object+") executed in "+Thread.currentThread()+" is "+value);
                //System.out.println("  evaluateLazily("+TreeModelNode.this.getDisplayName()+", "+id+"): have value = "+value);
                //System.out.println("      object = "+object+" class = "+((object != null) ? object.getClass().toString() : "null"));
                if (value instanceof String) {
                    htmlValue = htmlValue ((String) value);
                    nonHtmlValue = removeHTML ((String) value);
                }
            } catch (UnknownTypeException e) {
                if (!(object instanceof String)) {
                    e.printStackTrace ();
                    System.out.println("  Column id:" + columnModel.getID ());
                    System.out.println (model);
                    System.out.println ();
                }
            } catch (Throwable t) {
                Exceptions.printStackTrace(t);
            } finally {
                //evaluatedNotify.run();
                boolean fire;
                synchronized (properties) {
                    if (value instanceof String) {
                        properties.put (id, nonHtmlValue);
                        properties.put (id + "#html", htmlValue);
                    } else {
                        properties.put (id, value);
                    }
                    synchronized (evaluated) {
                        fire = evaluated[0] == -1;
                        evaluated[0] = 1;
                        evaluated.notifyAll();
                    }
                }
                //System.out.println("\nTreeModelNode.evaluateLazily("+TreeModelNode.this.getDisplayName()+", "+id+"): value = "+value+", fire = "+fire);
                if (fire) {
                    firePropertyChange (propertyId, null, value);
                }
                
            }
        }
        
        public synchronized Object getValue () { // Sync the calls
            if (nodeColumn) {
                return TreeModelNode.this.getDisplayName();
            }
            // 1) return value from cache
            synchronized (properties) {
                //System.out.println("getValue("+TreeModelNode.this.getDisplayName()+", "+id+"): contains = "+properties.containsKey (id)+", value = "+properties.get (id));
                if (properties.containsKey (id))
                    return properties.get (id);
            }
            
            Executor exec = asynchronous(model, CALL.VALUE, object);

            if (exec == AsynchronousModelFilter.CURRENT_THREAD) {
                return getTheValue();
            }

            synchronized (evaluated) {
                evaluated[0] = 0;
            }
            /*if (exec instanceof RequestProcessor) {
                RequestProcessor rp = (RequestProcessor) exec;
                if (rp != lastRp) {
                    task = rp.create(this);
                    lastRp = rp;
                }
                task.schedule(0);
            } else {*/
            //System.err.println("getTheValue of ("+object+") executed in "+exec);
                exec.execute(this);
            //}
            //treeModelRoot.getValuesEvaluator().evaluate(this);
            
            Object ret = null;
            
            synchronized (evaluated) {
                if (evaluated[0] != 1) {
                    try {
                        evaluated.wait(25);
                    } catch (InterruptedException iex) {}
                    if (evaluated[0] != 1) {
                        evaluated[0] = -1; // timeout
                        ret = EVALUATING_STR;
                    }
                }
            }
            if (ret == null) {
                synchronized (properties) {
                    ret = properties.get(id);
                }
            }
            
            if (ret == EVALUATING_STR &&
                    getValueType() != null && getValueType() != String.class) {
                ret = null; // Must not provide String when the property type is different.
                            // htmlDisplayValue attr will assure that the Evaluating str is there.
            }
            return ret;
        }

        private Object getTheValue() {
            Object value = "";
            String htmlValue = null;
            String nonHtmlValue = null;
            try {
                value = model.getValueAt (object, id);
                //System.err.println("  Value of ("+object+") executed in "+Thread.currentThread()+" is "+value);
                //System.out.println("  evaluateLazily("+TreeModelNode.this.getDisplayName()+", "+id+"): have value = "+value);
                //System.out.println("      object = "+object+" class = "+((object != null) ? object.getClass().toString() : "null"));
                if (value instanceof String) {
                    htmlValue = htmlValue ((String) value);
                    nonHtmlValue = removeHTML ((String) value);
                }
            } catch (UnknownTypeException e) {
                if (!(object instanceof String)) {
                    Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model+"\n,Column id:" + columnModel.getID (), e);
                }
            } finally {
                synchronized (properties) {
                    if (value instanceof String) {
                        properties.put (id, nonHtmlValue);
                        properties.put (id + "#html", htmlValue);
                    } else {
                        properties.put (id, value);
                    }
                }
            }
            return value;
        }
        
        @Override
        public Object getValue (String attributeName) {
            if (attributeName.equals ("htmlDisplayValue")) {
                if (nodeColumn) {
                    return TreeModelNode.this.getHtmlDisplayName();
                }
                synchronized (evaluated) {
                    if (evaluated[0] != 1) {
                        return "<html><font color=\"0000CC\">"+EVALUATING_STR+"</font></html>";
                    }
                }
                synchronized (properties) {
                    return properties.get (id + "#html");
                }
            }
            return super.getValue (attributeName);
        }

        @Override
        public String getShortDescription() {
            if (nodeColumn) {
                return TreeModelNode.this.getShortDescription();
            }
            synchronized (properties) {
                if (!properties.containsKey(id)) {
                    return null; // The same as value => EVALUATING_STR
                }
                String shortDescription = (String) properties.get (id + "#shortDescription");
                if (shortDescription != null) {
                    return shortDescription;
                }
            }
            Executor exec = asynchronous(model, CALL.SHORT_DESCRIPTION, object);
            
            if (exec == AsynchronousModelFilter.CURRENT_THREAD) {
                return updateShortDescription();
            } else {
                exec.execute(new Runnable() {
                    public void run() {
                        updateShortDescription();
                        firePropertyChange(propertyId, null, null);
                    }
                });
                return null;
            }
        }

        private String updateShortDescription() {
            try {
                javax.swing.JToolTip tooltip = new javax.swing.JToolTip();
                String sd = null;
                try {
                    tooltip.putClientProperty("getShortDescription", object); // NOI18N
                    Object tooltipObj = model.getValueAt(tooltip, id);
                    if (tooltipObj != null) {
                        sd = adjustHTML(tooltipObj.toString());
                    }
                    return sd;
                } finally {
                    // We MUST clear the client property, Swing holds this in a static reference!
                    tooltip.putClientProperty("getShortDescription", null); // NOI18N
                    synchronized (properties) {
                        properties.put (id + "#shortDescription", sd);
                    }
                }
            } catch (UnknownTypeException e) {
                // Ignore models that do not define tooltips for values.
                return null;
            }
        }
        
        public void setValue (final Object value) throws IllegalAccessException,
        IllegalArgumentException, java.lang.reflect.InvocationTargetException {
            Executor exec = asynchronous(model, CALL.VALUE, object);
            if (exec == AsynchronousModelFilter.CURRENT_THREAD) {
                setTheValue(value);
            } else {
                exec.execute(new Runnable() {
                    public void run() {
                        setTheValue(value);
                    }
                });
            }
        }

        private void setTheValue(final Object value) {
            try {
                Object v = value;
                model.setValueAt (object, id, v);
                v = model.getValueAt(object, id); // Store the new value
                synchronized (properties) {
                    if (v instanceof String) {
                        properties.put (id, removeHTML ((String) v));
                        properties.put (id + "#html", htmlValue ((String) v));
                    } else {
                        properties.put (id, v);
                    }
                }
                firePropertyChange (propertyId, null, null);
            } catch (UnknownTypeException e) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Column id:" + columnModel.getID ()+"\nModel: "+model, e);
            }
        }
        
        @Override
        public PropertyEditor getPropertyEditor () {
            PropertyEditor pe = columnModel.getPropertyEditor ();
            if (pe == null) {
                return super.getPropertyEditor();
            } else {
                return pe;
            }
        }
    }
    
    /** The single-threaded evaluator of lazy models. *//*
    static class LazyEvaluator implements Runnable {
        
        /** Release the evaluator task after this time. *//*
        private static final long EXPIRE_TIME = 1000L;

        private final List<Object> objectsToEvaluate = new LinkedList<Object>();
        private Evaluable currentlyEvaluating;
        private Task evalTask;
        
        public LazyEvaluator(RequestProcessor prefferedRequestProcessor) {
            if (prefferedRequestProcessor == null) {
                prefferedRequestProcessor = new RequestProcessor("Debugger Values Evaluator", 1); // NOI18N
            }
            evalTask = prefferedRequestProcessor.create(this, true);
        }
        
        public void evaluate(Evaluable eval) {
            evaluate(eval, true);
        }
        
        public void evaluate(Evaluable eval, boolean checkForEvaluating) {
            synchronized (objectsToEvaluate) {
                for (Iterator it = objectsToEvaluate.iterator(); it.hasNext(); ) {
                    if (eval == it.next()) return ; // Already scheduled
                }
                if (checkForEvaluating && currentlyEvaluating == eval) return ; // Is being evaluated
                objectsToEvaluate.add(eval);
                objectsToEvaluate.notify();
                if (evalTask.isFinished()) {
                    evalTask.schedule(0);
                }
            }
        }

        public void run() {
            while(true) {
                Evaluable eval;
                synchronized (objectsToEvaluate) {
                    if (objectsToEvaluate.size() == 0) {
                        try {
                            objectsToEvaluate.wait(EXPIRE_TIME);
                        } catch (InterruptedException iex) {
                            return ;
                        }
                        if (objectsToEvaluate.size() == 0) { // Expired
                            return ;
                        }
                    }
                    eval = (Evaluable) objectsToEvaluate.remove(0);
                    currentlyEvaluating = eval;
                }
                Runnable evaluatedNotify = new Runnable() {
                    public void run() {
                        synchronized (objectsToEvaluate) {
                            currentlyEvaluating = null;
                        }
                    }
                };
                eval.evaluateLazily(evaluatedNotify);
            }
        }

        public interface Evaluable {

            public void evaluateLazily(Runnable evaluatedNotify);

        }

    }*/

}

