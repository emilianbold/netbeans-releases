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
package org.netbeans.modules.xml.catalog;

import java.awt.*;
import java.beans.*;
import java.util.*;
import java.io.*;

import org.openide.nodes.*;
import org.openide.util.actions.*;
import org.openide.util.*;
import org.openide.actions.*;

import org.netbeans.modules.xml.catalog.spi.*;
import org.netbeans.modules.xml.catalog.impl.*;
import org.netbeans.modules.xml.catalog.settings.CatalogSettings;

/**
 * Node representing a catalog.
 * Every catalog reader is considered to be a bean.
 * Information about catalog instance are obtained using CatalogDescriptor interface
 * if passed instance implements it.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class CatalogNode extends BeanNode implements Refreshable, PropertyChangeListener {

    //class debug switch
    private static final boolean DEBUG = false;
    
    /** Creates new CatalogNode */
    public CatalogNode(CatalogReader catalog) throws IntrospectionException {        
        super(catalog, new CatalogChildren(catalog));
        
        if (catalog instanceof CatalogDescriptor) {
            
            // set node properties acording to descriptor
            
            CatalogDescriptor desc = (CatalogDescriptor) catalog;            
            setSynchronizeName(false);
            setName(desc.getDisplayName());
            setDisplayName(desc.getDisplayName());
            setShortDescription(desc.getShortDescription());
            fireIconChange();

            // listen on it
            
            desc.addPropertyChangeListener(WeakListener.propertyChange(this, desc));
        }
    }

    /** Lazy action initialization. */
    protected SystemAction[] createActions() {
        return new SystemAction[] {
            SystemAction.get(RefreshAction.class),
            SystemAction.get(DeleteAction.class),
            
            //!!! sometimes added by BeanNode
            SystemAction.get(CustomizeAction.class)
        };
    }

    /**
     * @return icon regurned by CatalogDescriptor if instance of it
     */
    public Image getIcon(int type) {
        if (getBean() instanceof CatalogDescriptor) {
            Image icon = ((CatalogDescriptor)getBean()).getIcon(type);
            if (icon != null) return icon;
        }
        
        return super.getIcon(type);        
    }
    
    /**
     * Refresh catalog provider and then refresh children.
     */
    public void refresh() {
        ((CatalogReader)getBean()).refresh();
        ((CatalogChildren)getChildren()).reload();  // may be double reload
    }

    /**
     * Remove itseld from CatalogSettings,
     */
    public void destroy() throws IOException {
        CatalogSettings.getDefault().removeCatalog((CatalogReader)getBean());
        super.destroy();
    }

    /**
     * The node listens on some changes
     */
    public void propertyChange(PropertyChangeEvent e) {
        if (DEBUG) Util.trace(e.toString());
        
        if (CatalogDescriptor.PROP_CATALOG_NAME.equals(e.getPropertyName())) {
            if (DEBUG) Util.trace(" Setting name: " + (String) e.getNewValue()); // NOI18N
            setName((String) e.getNewValue());
            setDisplayName((String) e.getNewValue());
        } else if (CatalogDescriptor.PROP_CATALOG_DESC.equals(e.getPropertyName())) {
            if (DEBUG) Util.trace(" Setting desc: " + (String) e.getNewValue()); // NOI18N
            setShortDescription((String) e.getNewValue());
        } else if (CatalogDescriptor.PROP_CATALOG_ICON.equals(e.getPropertyName())) { 
            if (DEBUG) Util.trace(" Updating icon"); // NOI18N
            fireIconChange();
        }
    }
    
    
    // ~~~~~~~~~~~~~~~~~~~~~~ Serialization stuff ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        if (DEBUG) Util.trace("Reading Catalog node " + this); // NOI18N
        in.defaultReadObject();        
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        if (DEBUG) Util.trace("Writing " + this); // NOI18N
        out.defaultWriteObject();        
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
     * Kids have to listen at Catalog
     */
    public static class CatalogChildren extends Children.Keys {
        
        private CatalogReader peer;
        private CatalogListener catalogListener;
        
        public CatalogChildren(CatalogReader catalog) {
            peer = catalog;
            
        }
                
        /** Contains public ID (String) instances. */
        private final TreeSet keys = new TreeSet();
        
        public void addNotify() {            
            catalogListener = new Lis();
            try {
                peer.addCatalogListener(catalogListener);
            } catch (UnsupportedOperationException ex) {
                // User must use explicit refresh
            }            
            reload();
        }

        public void removeNotify() {
            try {
                peer.removeCatalogListener(catalogListener);
            } catch (UnsupportedOperationException ex) {
                // does not matter
            }
            keys.clear();
            setKeys(keys);
        }
        
        public Node[] createNodes(Object key) {        
            try {
                return new Node[] { 
                    new CatalogEntryNode((String)key, peer.getSystemID((String)key)) 
                }; 
            } catch (IntrospectionException ex) {
                return new Node[] {};
            }
        }

        /**
          * Reloads catalog content
          */
        public void reload() {
            if (DEBUG) Util.trace(" Reloading kids of " + peer + "..."); // NOI18N
            keys.clear();
            Iterator it = peer.getPublicIDs();
            if (it != null) {
                while (it.hasNext()) {
                    keys.add(it.next());
                }
                setKeys(keys);
            }            
        }
        
        private class Lis implements CatalogListener {
            
            /** Given public ID has changed - created.  */
            public void notifyNew(String publicID) {
                keys.add(publicID);
                setKeys(keys);
            }
            
            /** Given public ID has changed - disappeared.  */
            public void notifyRemoved(String publicID) {
                keys.remove(publicID);
                setKeys(keys);
            }
            
            /** Given public ID has changed.  */
            public void notifyUpdate(String publicID) {
                refreshKey(publicID);
            }
            
            /** All entries are invalidated.  */
            public void notifyInvalidate() {
                reload();
            }
            
        }
        
    }
    
}
