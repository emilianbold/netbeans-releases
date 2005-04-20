/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.viewmodel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyEditor;
import java.lang.IllegalAccessException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author   Jan Jancura
 */
public class TreeModelNode extends AbstractNode {

    
    // variables ...............................................................

    private Models.CompoundModel model;
    private TreeModelRoot       treeModelRoot;
    private Object              object;
    
    private String              htmlDisplayName;
    private String              shortDescription;
    private Map                 properties = new HashMap ();
    private Map                 oldProperties = new HashMap ();

    
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
            Lookups.singleton (object)
        );
        this.model = model;
        this.treeModelRoot = treeModelRoot;
        this.object = object;
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
            ps.put (new MyProperty (columns [i]));
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
                e.printStackTrace ();
                System.out.println (model);
                System.out.println ();
            }
            return Children.LEAF;
        }
    }
    
    public String getShortDescription () {
        if (shortDescription == null) {
            getRequestProcessor ().post (new Runnable () {
                public void run () {
                    try {
                        shortDescription = model.getShortDescription (object);
                        setShortDescription (shortDescription);
                    } catch (UnknownTypeException e) {
                        if (!(object instanceof String)) {
                            e.printStackTrace ();
                            System.out.println (model);
                            System.out.println ();
                        }
                    }
                }
            });
        }
        return shortDescription;
    }
    
    public String getHtmlDisplayName () {
        return htmlDisplayName;
    }
    
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
    
    public boolean canCopy () {
        return false;
    }
    
    public boolean canCut () {
        return false;
    }
    
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
            e.printStackTrace ();
            System.out.println (model);
            System.out.println ();
        }
    }

    
    // other methods ...........................................................
    
    void setObject (Object o) {
        object = o;
        Children ch = getChildren ();
        if (ch instanceof TreeModelChildren)
            ((TreeModelChildren) ch).object = o;
        refresh ();
    }
    
    public Object getObject () {
        return object;
    }

    private Task task;
    
    void refresh () {
        // 1) empty cache
        shortDescription = null;
        htmlDisplayName = null;
        oldProperties = properties;
        properties = new HashMap ();
        
        // 2) refresh name, displayName and iconBase
        if (task != null)
            task.cancel ();
        task = getRequestProcessor ().post (new Runnable () {
            public void run () {
                refreshNode ();
            }
        });
        
        // 3) refresh children
        Children ch = getChildren ();
        if (ch instanceof TreeModelChildren)
            ((TreeModelChildren) ch).refreshChildren ();
    }
    
    private static RequestProcessor requestProcessor;
    public static RequestProcessor getRequestProcessor () {
        if (requestProcessor == null)
            requestProcessor = new RequestProcessor ("TreeModel");
        return requestProcessor;
    }

    private void setName (String name, boolean italics) {
        if (name.startsWith ("<html>")) {
            if (italics && name.indexOf ("<i>") < 0) {
                name = "<html><i>" + name.substring (6, name.length () - 7) +
                    "</i></html>";
            }
            htmlDisplayName = name;
            setDisplayName (name.substring (6, name.length () - 7));
        } else {
            htmlDisplayName = null;
            setDisplayName (name);
        }
    }
    
    private void refreshNode () {
        try {
            String name = model.getDisplayName (object);
            if (name == null) 
                new NullPointerException (
                    "Model: " + model + ".getDisplayName (" + object + 
                    ") = null!"
                ).printStackTrace ();
            setName (name, false);
            String iconBase = model.getIconBase (object);
            if (iconBase != null)
                setIconBase (iconBase);
            else
                setIconBase ("org/openide/resources/actions/empty");
            firePropertyChange(null, null, null);
        } catch (UnknownTypeException e) {
            if (object instanceof String) {
                String name = (String) object;
                setName (name, false);
            } else {
                e.printStackTrace ();
                System.out.println (model);
                System.out.println ();
            }
        }
    }
    
    private static String i (String text) {
        if (text.startsWith ("<html>")) {
            if (text.indexOf ("<i>") > 0) return text;
            text = text.substring (6, text.length () - 7);
        }
        return "<html><font color=666666>" + text + "</font></html>";
    }
    
    private static String htmlValue (String name) {
        if (name.startsWith ("<html>")) return name;
        else return null;
    }
    
    private static String removeHTML (String text) {
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
        return text;
    }
    
    
    // innerclasses ............................................................
    
    /** Special locals subnodes (children) */
    private static final class TreeModelChildren extends Children.Keys {
            
        private boolean             initialezed = false;
        private Models.CompoundModel model;
        private TreeModelRoot       treeModelRoot;
        private Object              object;
        private WeakHashMap         objectToNode = new WeakHashMap ();
        
        
        TreeModelChildren (
            Models.CompoundModel model,
            TreeModelRoot   treeModelRoot,
            Object          object
        ) {
            this.model = model;
            this.treeModelRoot = treeModelRoot;
            this.object = object;
        }
        
        protected void addNotify () {
            initialezed = true;
            refreshChildren ();
        }
        
        protected void removeNotify () {
            initialezed = false;
            setKeys (Collections.EMPTY_SET);
        }
        
        private Task task;
        void refreshChildren () {
            if (!initialezed) return;

            if (task != null)
                task.cancel ();
            task = getRequestProcessor ().post (new Runnable () {
                public void run () {
                    try {
                        refreshChildren (model.getChildrenCount (object));
                    } catch (UnknownTypeException e) {
                        if (!(object instanceof String)) {
                            e.printStackTrace ();
                            System.out.println (model);
                            System.out.println ();
                        }
                        setKeys (new Object [0]);
                        return;
                    }
                }
            });
        }
        
        void refreshChildren (int count) {
            try {
                final Object[] ch = model.getChildren (
                    object, 
                    0, 
                    count
                );
                int i, k = ch.length; 
                WeakHashMap newObjectToNode = new WeakHashMap ();
                for (i = 0; i < k; i++) {
                    if (ch [i] == null) {
                        System.out.println("model: " + model);
                        System.out.println("parent: " + object);
                        throw new NullPointerException ();
                    }
                    WeakReference wr = (WeakReference) objectToNode.get 
                        (ch [i]);
                    if (wr == null) continue;
                    TreeModelNode tmn = (TreeModelNode) wr.get ();
                    if (tmn == null) continue;
                    tmn.setObject (ch [i]);
                    newObjectToNode.put (ch [i], wr);
                }
                objectToNode = newObjectToNode;
                setKeys (ch);
                
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        int i, k = ch.length;
                        for (i = 0; i < k; i++)
                            try {
                                if (model.isExpanded (ch [i]))
                                    treeModelRoot.getTreeTable ().expandNode (ch [i]);
                            } catch (UnknownTypeException ex) {
                            }
                    }
                });
            } catch (UnknownTypeException e) {
                setKeys (new Object [0]);
                if (!(object instanceof String)) {
                    e.printStackTrace ();
                    System.out.println (model);
                    System.out.println ();
                }
            }
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
            if (object instanceof Exception)
                return new Node[] {
                    new ExceptionNode ((Exception) object)
                };
            TreeModelNode tmn = new TreeModelNode (
                model, 
                treeModelRoot, 
                object
            );
            objectToNode.put (object, new WeakReference (tmn));
            return new Node[] {tmn};
        }
    } // ItemChildren
    
    private class MyProperty extends PropertySupport {
        
        private String      id;
        private ColumnModel columnModel;
        
        
        MyProperty (
            ColumnModel columnModel
        ) {
            super (
                columnModel.getID (),
                columnModel.getType (),
                columnModel.getDisplayName (),
                columnModel.getShortDescription (), 
                true,
                true
            );
            this.columnModel = columnModel;
            id = columnModel.getID ();
        }
        

        /* Can write the value of the property.
        * Returns the value passed into constructor.
        * @return <CODE>true</CODE> if the read of the value is supported
        */
        public boolean canWrite () {
            try {
                return !model.isReadOnly (object, columnModel.getID ());
            } catch (UnknownTypeException e) {
                if (!(object instanceof String)) {
                    e.printStackTrace ();
                    System.out.println("  Column id:" + columnModel.getID ());
                    System.out.println (model);
                    System.out.println ();
                }
                return false;
            }
        }
        
        public Object getValue () {
            // 1) return value from cache
            if (properties.containsKey (id))
                return properties.get (id);
            
            // 2) no value in cache => put there old value or null
            Object value = oldProperties.get (id);
            if (columnModel.getType ().equals (String.class)) {
                if (value == null)
                    value = "";
                else
                    properties.put (id + "#html", i ((String) value));
            }
            properties.put (id, value);
            
            // 3) get a new value
            getRequestProcessor ().post (new Runnable () {
                public void run () {
                    try {
                        Object value = model.getValueAt (object, id);
                        if (value instanceof String) {
                            properties.put (id, removeHTML ((String) value));
                            properties.put (
                                id + "#html", 
                                htmlValue ((String) value)
                            );
                        } else
                            properties.put (id, value);
                        firePropertyChange (id, null, value);
                    } catch (UnknownTypeException e) {
                        if (!(object instanceof String)) {
                            e.printStackTrace ();
                            System.out.println("  Column id:" + columnModel.getID ());
                            System.out.println (model);
                            System.out.println ();
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            });
            
            // 4) return value
            return value;
        }
        
        public Object getValue (String attributeName) {
            if (!canWrite ())
                return super.getValue (attributeName);
            if (attributeName.equals ("htmlDisplayValue"))
                return properties.get (id + "#html");
            return super.getValue (attributeName);
        }
        
        public void setValue (Object v) throws IllegalAccessException, 
        IllegalArgumentException, java.lang.reflect.InvocationTargetException {
            try {
                model.setValueAt (object, id, v);
                properties.put (id, v);
                firePropertyChange (id, null, null);
            } catch (UnknownTypeException e) {
                e.printStackTrace ();
                System.out.println("  Column id:" + columnModel.getID ());
                System.out.println (model);
                System.out.println ();
            }
        }
        
        public PropertyEditor getPropertyEditor () {
            return columnModel.getPropertyEditor ();
        }
    }
}

