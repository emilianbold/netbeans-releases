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

package org.netbeans.modules.viewmodel;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyEditor;
import java.lang.ref.WeakReference;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.TreeFeatures;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.awt.Actions;
import org.openide.explorer.view.CheckableNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
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
    private TreeModelRoot       treeModelRoot;
    private Object              object;
    
    private String              htmlDisplayName;
    private String              shortDescription;
    private final Object        shortDescriptionLock = new Object();
    private final Map<String, Object> properties = new HashMap<String, Object>();

    
    // init ....................................................................

    /**
    * Creates root of call stack for given producer.
    */
    public TreeModelNode ( 
        final Models.CompoundModel model, 
        final TreeModelRoot treeModelRoot,
        final Object object
    ) {
        super (
            createChildren (model, treeModelRoot, object),
            Lookups.fixed(object, new CheckNodeCookieImpl(model, object))
        );
        this.model = model;
        this.treeModelRoot = treeModelRoot;
        this.object = object;
        
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
        initProperties ();
    }

    
    // Node implementation .....................................................
    
    private void initProperties () {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet ();
        ColumnModel[] columns = model.getColumns ();
        int i, k = columns.length;
        for (i = 0; i < k; i++)
            ps.put (new MyProperty (columns [i], treeModelRoot));
        sheet.put (ps);
        setSheet (sheet);
    }
    
    private static Children createChildren (
        Models.CompoundModel model, 
        TreeModelRoot treeModelRoot,
        Object object
    ) {
        if (object == null) 
            throw new NullPointerException ();
        try {
            return model.isLeaf (object) ? 
                Children.LEAF : 
                new TreeModelChildren (model, treeModelRoot, object);
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
            return model.getActions (object);
        } catch (UnknownTypeException e) {
            // NodeActionsProvider is voluntary
            return new Action [0];
        }
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
        setObjectNoRefresh (o);
        refresh ();
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

    private Task task;
    
    void refresh () {
        // 1) empty cache
        synchronized (properties) {
            properties.clear();
        }
        
        
        // 2) refresh name, displayName and iconBase
        if (task == null) {
            task = getRequestProcessor ().create (new Runnable () {
                public void run () {
                    refreshNode ();
                    doFireShortDescriptionChange();
                    
                    // 3) refresh children
                    refreshTheChildren(true);
                }
            });
        }
        task.schedule(0);
    }
    
    void refresh (int changeMask) {
        if (changeMask == 0xFFFFFFFF) {
            refresh();
            return ;
        }
        boolean refreshed = false;
        if ((ModelEvent.NodeChanged.DISPLAY_NAME_MASK & changeMask) != 0) {
            try {
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
            getRequestProcessor ().post (new Runnable () {
                public void run () {
                    refreshTheChildren(false);
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
            refresh();
        }
    }
    
    private static RequestProcessor requestProcessor;
    // Accessed from test
    RequestProcessor getRequestProcessor () {
        RequestProcessor rp = treeModelRoot.getRequestProcessor();
        if (rp != null) {
            return rp;
        }
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
    
    private void refreshNode () {
        try {
            String name = model.getDisplayName (object);
            if (name == null) {
                Throwable t = 
                    new NullPointerException (
                        "Model: " + model + ".getDisplayName (" + object + 
                        ") = null!"
                    );
                Exceptions.printStackTrace(t);
            }
            setName (name, false);
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
    
    private void refreshTheChildren(boolean refreshSubNodes) {
        Children ch = getChildren();
        try {
            if (ch instanceof TreeModelChildren) {
                if (model.isLeaf(object)) {
                    setChildren(Children.LEAF);
                } else {
                    ((TreeModelChildren) ch).refreshChildren(refreshSubNodes);
                }
            } else if (!model.isLeaf (object)) {
                setChildren(new TreeModelChildren (model, treeModelRoot, object));
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
    
    /*
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
     */
    
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
    
    /*
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
     */
    
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
    static final class TreeModelChildren extends Children.Keys<Object>
                                                 implements LazyEvaluator.Evaluable {
            
        private boolean             initialezed = false;
        private Models.CompoundModel model;
        private TreeModelRoot       treeModelRoot;
        private Object              object;
        private WeakHashMap<Object, WeakReference<TreeModelNode>> objectToNode = new WeakHashMap<Object, WeakReference<TreeModelNode>>();
        private final int[]         evaluated = { 0 }; // 0 - not yet, 1 - evaluated, -1 - timeouted
        private Object[]            children_evaluated;
        private boolean refreshingSubNodes = true;
        private boolean refreshingStarted = true;
        
        private static final Object WAIT_KEY = new Object();
        
        
        TreeModelChildren (
            Models.CompoundModel model,
            TreeModelRoot   treeModelRoot,
            Object          object
        ) {
            this.model = model;
            this.treeModelRoot = treeModelRoot;
            this.object = object;
        }
        
        @Override
        protected void addNotify () {
            initialezed = true;
            refreshChildren (true);
        }
        
        @Override
        protected void removeNotify () {
            initialezed = false;
            setKeys (Collections.emptySet());
        }
        
        void refreshChildren (boolean refreshSubNodes) {
            if (!initialezed) return;

            refreshLazyChildren(refreshSubNodes);
        }
        
        public void evaluateLazily(Runnable evaluatedNotify) {
            synchronized (evaluated) {
                refreshingStarted = false;
            }
            Object[] ch;
            try {
                int count = model.getChildrenCount (object);
                ch = model.getChildren (
                    object, 
                    0, 
                    count
                );
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
            evaluatedNotify.run();
            boolean fire;
            synchronized (evaluated) {
                int eval = evaluated[0];
                if (refreshingStarted) {
                    fire = false;
                } else {
                    fire = evaluated[0] == -1;
                    if (!fire) {
                        children_evaluated = ch;
                    }
                    evaluated[0] = 1;
                    evaluated.notifyAll();
                }
                //System.err.println(this.hashCode()+" evaluateLazily() ready, evaluated[0] = "+eval+" => fire = "+fire+", refreshingStarted = "+refreshingStarted+", children_evaluated = "+(children_evaluated != null));
            }
            if (fire) {
                applyChildren(ch, refreshingSubNodes);
            }
        }
        
        private void refreshLazyChildren (boolean refreshSubNodes) {
            synchronized (evaluated) {
                evaluated[0] = 0;
                refreshingStarted = true;
                this.refreshingSubNodes = refreshSubNodes;
                //System.err.println(this.hashCode()+" refreshLazyChildren() started = true, evaluated = 0");
            }
            // It's refresh => do not check for this children already being evaluated
            treeModelRoot.getChildrenEvaluator().evaluate(this, false);
            Object[] ch;
            synchronized (evaluated) {
                if (evaluated[0] != 1) {
                    try {
                        evaluated.wait(200);
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
            }
            if (ch == null) {
                applyWaitChildren();
            } else {
                applyChildren(ch, refreshSubNodes);
            }
        }
        
        private void applyChildren(final Object[] ch, boolean refreshSubNodes) {
            //System.err.println(this.hashCode()+" applyChildren("+refreshSubNodes+")");
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
                if (refreshSubNodes) {
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
        
        private final void expandIfSetToExpanded(Object child) {
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
        
        protected Node[] createNodes (Object object) {
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
                treeModelRoot, 
                object
            );
            objectToNode.put (object, new WeakReference<TreeModelNode>(tmn));
            return new Node[] {tmn};
        }
    } // ItemChildren
    
    private class MyProperty extends PropertySupport implements LazyEvaluator.Evaluable {
        
        private final String EVALUATING_STR = NbBundle.getMessage(TreeModelNode.class, "EvaluatingProp");
        private String      id;
        private ColumnModel columnModel;
        private boolean nodeColumn;
        private TreeModelRoot treeModelRoot;
        private final int[] evaluated = { 0 }; // 0 - not yet, 1 - evaluated, -1 - timeouted
        
        
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
            this.columnModel = columnModel;
            this.nodeColumn = columnModel.getType() == null;
            this.treeModelRoot = treeModelRoot;
            id = columnModel.getID ();
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
        
        public void evaluateLazily(Runnable evaluatedNotify) {
            Object value = "";
            String htmlValue = null;
            String nonHtmlValue = null;
            try {
                value = model.getValueAt (object, id);
                //System.out.println("  evaluateLazily("+TreeModelNode.this.getDisplayName()+", "+id+"): have value = "+value);
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
                t.printStackTrace();
            } finally {
                evaluatedNotify.run();
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
                    firePropertyChange (id, null, value);
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
            
            synchronized (evaluated) {
                evaluated[0] = 0;
            }
            treeModelRoot.getValuesEvaluator().evaluate(this);
            
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
            }
            try {
                javax.swing.JToolTip tooltip = new javax.swing.JToolTip();
                try {
                    tooltip.putClientProperty("getShortDescription", object); // NOI18N
                    Object tooltipObj = model.getValueAt(tooltip, id);
                    if (tooltipObj == null) {
                        return null;
                    } else {
                        return adjustHTML(tooltipObj.toString());
                    }
                } finally {
                    // We MUST clear the client property, Swing holds this in a static reference!
                    tooltip.putClientProperty("getShortDescription", null); // NOI18N
                }
            } catch (UnknownTypeException e) {
                // Ignore models that do not define tooltips for values.
                return null;
            }
        }
        
        public void setValue (final Object value) throws IllegalAccessException,
        IllegalArgumentException, java.lang.reflect.InvocationTargetException {
            RequestProcessor prefferedRequestProcessor = treeModelRoot.getRequestProcessor();
            if (prefferedRequestProcessor == null) {
                prefferedRequestProcessor = new RequestProcessor("Debugger Values Setter", 1); // NOI18N
            }
            prefferedRequestProcessor.post(new Runnable() {
                public void run() {
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
                        firePropertyChange (id, null, null);
                    } catch (UnknownTypeException e) {
                        Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Column id:" + columnModel.getID ()+"\nModel: "+model, e);
                    }
                }
            });
        }
        
        @Override
        public PropertyEditor getPropertyEditor () {
            return columnModel.getPropertyEditor ();
        }
    }
    
    /** The single-threaded evaluator of lazy models. */
    static class LazyEvaluator implements Runnable {
        
        /** Release the evaluator task after this time. */
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

    }

}

