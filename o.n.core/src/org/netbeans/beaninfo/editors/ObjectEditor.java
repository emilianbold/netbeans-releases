/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.beans.*;
import java.awt.Component;
import java.util.*;

import org.openide.TopManager;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;

/**
 * Defines editor for choosing of any object using lookup.
 *
 * @author Jaroslav Tulach
 */
public final class ObjectEditor extends PropertyEditorSupport 
implements ExPropertyEditor, PropertyChangeListener {
    /** Name of the custom property that can be passed in PropertyEnv. 
     * Should contain superclass that is allowed to be 
     */
    private static final String PROP_SUPERCLASS = "superClass"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. 
     * Either Boolean.TRUE or a String, in such case the string represents
     * human readable name of the value.
     */
    private static final String PROP_NULL = "nullValue"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. 
     * A node to display as root of the custom editor
     */
    private static final String PROP_NODE = "node"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. 
     * A lookup to use to query for results.
     */
    private static final String PROP_LOOKUP = "lookup"; // NOI18N
    
    /** folder with all services */
    private static final String DEFAULT_FOLDER ="Services";

    /** custom editor */
    private ExplorerPanel customEditor;
    
    /** super class to search for */
    private Lookup.Template template;
    
    /** root node to use */
    private Node root;
    
    /** null or name to use for null value */
    private String nullValue;
    
    /** a special lookup to use or null */
    private Lookup lookup;
    
    /** Creates new ObjectEditor  */
    public ObjectEditor() {
    }

    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     */
    public synchronized void attachEnv(PropertyEnv env) {
        Object obj = env.getFeatureDescriptor ().getValue (PROP_SUPERCLASS);
        if (obj instanceof Class) {
            template = new Lookup.Template ((Class)obj);
        } else {
            template = null;
        }
        
        obj = env.getFeatureDescriptor ().getValue (PROP_NULL);
        if (Boolean.TRUE.equals (obj)) {
            nullValue = NbBundle.getMessage (ObjectEditor.class, "CTL_NullValue");
        } else {
            if (obj instanceof String) {
                nullValue = (String)obj;
            } else {
                nullValue = null;
            }
        }
        
        obj = env.getFeatureDescriptor ().getValue (PROP_LOOKUP);
        lookup = obj instanceof Lookup ? (Lookup)obj : null;
        
        obj = env.getFeatureDescriptor ().getValue (PROP_NODE);
        root = obj instanceof Node ? (Node)obj : null;
    }
    
    /** A lookup to work on.
     * @return a lookup.
     */
    protected Lookup lookup () {
        Lookup l = lookup;
        return l == null ? Lookup.getDefault () : l;
    }
    
    /** A root node to start search from.
     */
    protected synchronized Node root () {
        if (root == null) {
            root = new org.netbeans.core.ui.LookupNode ();
        }
        return root;
    }

    /** A template to use.
     */
    protected Lookup.Template template () {
        if (template == null) {
            template = new Lookup.Template ();
        }
         
        return template;
    }
    
    /** Notification of changes in custom property editor.
     */
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        Node[] arr = customEditor.getExplorerManager ().getSelectedNodes ();
        if (arr.length != 1) {
            setValue (null);
        }
        
        try {
            InstanceCookie ic = (InstanceCookie)arr[0].getCookie (InstanceCookie.class);
            boolean accept;
            if (ic instanceof InstanceCookie.Of) {
                InstanceCookie.Of of = (InstanceCookie.Of)ic;
                accept = of.instanceOf (template ().getType ());
            } else {
                accept = ic != null && template ().getType ().isAssignableFrom (ic.instanceClass ());
            }

            if (accept) {
                setValue (ic.instanceCreate ());
                return;
            }
        } catch (ClassNotFoundException ex) {
            TopManager.getDefault ().getErrorManager ().notify (
                ErrorManager.INFORMATIONAL, ex
            );
        } catch (java.io.IOException ex) {
            TopManager.getDefault ().getErrorManager ().notify (
                ErrorManager.INFORMATIONAL, ex
            );
        }
        
        setValue (null);
    }
    
    /** For a given Item tries to find appropriate node.
     * @param item item we are looking for
     * @param node root node to search from
     * @return node that represents the 
     */
    public String getAsText() {
        Object value = getValue ();
        if (value == null) {
            return nullValue == null ? 
                NbBundle.getMessage (ObjectEditor.class, "CTL_NullValue")
            :
                nullValue;
        }
        
        Lookup.Template t = new Lookup.Template (
            template ().getType (),
            template ().getId (),
            value // instance to search for
        );
        Lookup.Item item = lookup ().lookupItem (t);
        
        if (item == null) {
            return NbBundle.getMessage (ObjectEditor.class, "CTL_NullItem");
        }
        
        return itemDisplayName (item);
    }
    
    /** Searches between items whether there is one with the same display name.
     * @param str item name
     */
    public void setAsText(java.lang.String str) throws java.lang.IllegalArgumentException {
        if (nullValue != null && nullValue.equals (str)) {
            setValue (null);
            return;
        }
        
        
        Collection allItems = lookup ().lookup (template ()).allItems ();
        
        Iterator it = allItems.iterator ();
        while (it.hasNext ()) {
            Lookup.Item item = (Lookup.Item)it.next ();
            
            if (itemDisplayName (item).equals (str)) {
                setValue (item.getInstance ());
                return;
            }
        }
        
        throw new IllegalArgumentException ();
    }
    
    /** List of all display names for items.
     * @return array of strings
     */
    public java.lang.String[] getTags() {
        Collection allItems = lookup ().lookup (template ()).allItems ();
   
        ArrayList list = new ArrayList (allItems.size () + 1);
        if (nullValue != null) {
            list.add (nullValue);
        }
        
        Iterator it = allItems.iterator ();
        while (it.hasNext ()) {
            Lookup.Item item = (Lookup.Item)it.next ();
            list.add (itemDisplayName (item));
        }
        
        String[] retValue = new String[list.size()];
        list.toArray(retValue);
        return retValue;
    }

    /** Yes we have custom editor.
     */
    public boolean supportsCustomEditor() {
        return true;
    }
    
    /** Creates custom property editor.
     */
    public synchronized Component getCustomEditor () {
        if (customEditor != null) {
            ExplorerManager em = customEditor.getExplorerManager ();
            em.removePropertyChangeListener (this);
            selectNode (em, getValue ());
            em.addPropertyChangeListener (this);
            
            return customEditor;
        }
        
        ExplorerPanel panel = new ExplorerPanel ();
        ExplorerManager em = panel.getExplorerManager ();
        panel.add (new BeanTreeView ());
        em.setRootContext (root ());
        
        selectNode (em, getValue ());
        
        em.addPropertyChangeListener (this);
        
        return customEditor = panel;
    }
    
    /** Updates selected node in explorer based on the provided value.
     * @param value value to select
     * @param em manager to select the value in
     */
    private void selectNode (ExplorerManager em, Object value) {
        Node node = root ();
        if (value != null) {
            Lookup.Template t = new Lookup.Template (
                template ().getType (),
                template ().getId (),
                value // instance to search for
            );
            Lookup.Item item = lookup ().lookupItem (t);
            
            if (item != null) {
                node = itemToNode (item);
            }
        }             

        try {
            em.setSelectedNodes (new Node[] { node });
        } catch (PropertyVetoException ex) {
            TopManager.getDefault ().getErrorManager ().notify (ex);
        }
    }
    
    
    /** Finds a display name of an item.
     * @param item
     * @return the human readable string
     */
    private String itemDisplayName (Lookup.Item item) {
        return item.getDisplayName ();
    }

    /** Locates a node for given lookup item.
     * @param item item to search for
     * @return node
     */
    private Node itemToNode (Lookup.Item item) {
        String id = item.getId ();
        if (id.startsWith ("FL[")) { // NOI18N
            id = id.substring (3);
            
            // try to find the node from root node
            StringTokenizer tok = new StringTokenizer (id, "/"); // NOI18N
            Node root = root ();
            while (tok.hasMoreElements () && root != null) {
                String next = tok.nextToken ();
                Node n = root.getChildren ().findChild (next);
                if (n == null) {
                    // try to search via names of data objects
                    DataFolder df = (DataFolder)root.getCookie (DataFolder.class);
                    if (df != null) {
                        DataObject[] arr = df.getChildren ();
                        for (int i = 0; i < arr.length; i++) {
                            if (arr[i].getName ().equals (next)) {
                                n = arr[i].getNodeDelegate ();
                                break;
                            }
                        }
                    }
                }
                root = n;
            }
            
            if (root != null) {
                return root;
            }
        }
        
        // default fallback for not recognized items
        try {
            return new org.openide.nodes.BeanNode (item.getInstance ());
        } catch (java.beans.IntrospectionException ex) {
            return org.openide.nodes.Node.EMPTY;
        }
    }
    
}

