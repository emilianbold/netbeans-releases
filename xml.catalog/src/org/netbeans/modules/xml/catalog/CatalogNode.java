/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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
final class CatalogNode extends BeanNode implements Refreshable, PropertyChangeListener, Node.Cookie {
    
    /** Creates new CatalogNode */
    public CatalogNode(CatalogReader catalog) throws IntrospectionException {        
        super(catalog, new CatalogChildren(catalog));

        getCookieSet().add(this);
        
        if (catalog instanceof CatalogDescriptor) {
            
            // set node properties acording to descriptor
            
            CatalogDescriptor desc = (CatalogDescriptor) catalog;            
            setSynchronizeName(false);
            setName(desc.getDisplayName());
            setDisplayName(desc.getDisplayName());
            setShortDescription(desc.getShortDescription());
            fireIconChange();  

            // listen on it
            
            desc.addPropertyChangeListener(WeakListeners.propertyChange(this, desc));
        }
    }

    /** Lazy action initialization. */
    protected SystemAction[] createActions() {
        return new SystemAction[] {
            SystemAction.get(RefreshAction.class),
            SystemAction.get(CatalogNode.UnmountAction.class),
            null,
            //??? #24349 CustimizeAction sometimes added by BeanNode here
            SystemAction.get(PropertiesAction.class)
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
    
    public HelpCtx getHelpCtx() {
        //return new HelpCtx(CatalogNode.class);
        return HelpCtx.DEFAULT_HELP;
    }
    
    /**
     * Refresh catalog provider and then refresh children.
     */
    public void refresh() {
        ((CatalogReader)getBean()).refresh();
        ((CatalogChildren)getChildren()).reload();  // may be double reload
    }

    /** This node cannot be destroyed, just unmount.
     * @return always <CODE>false</CODE>
     */
    public boolean canDestroy () {
        return false;
    }
    
    public boolean canRename() {
        return false;
    }

    /**
     * Remove itseld from CatalogSettings,
     */
    public void destroy() throws IOException {
        CatalogSettings mounted = CatalogSettings.getDefault();
        mounted.removeCatalog((CatalogReader)getBean());
        super.destroy();
    }

    /**
     * The node listens on some changes
     */
    public void propertyChange(PropertyChangeEvent e) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug(e.toString());
        
        if (CatalogDescriptor.PROP_CATALOG_NAME.equals(e.getPropertyName())) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug(" Setting name: " + (String) e.getNewValue()); // NOI18N

            setName((String) e.getNewValue());
            setDisplayName((String) e.getNewValue());
        } else if (CatalogDescriptor.PROP_CATALOG_DESC.equals(e.getPropertyName())) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug(" Setting desc: " + (String) e.getNewValue()); // NOI18N

            setShortDescription((String) e.getNewValue());
        } else if (CatalogDescriptor.PROP_CATALOG_ICON.equals(e.getPropertyName())) { 
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug(" Updating icon"); // NOI18N

            fireIconChange();
        }
    }
    
    
    // ~~~~~~~~~~~~~~~~~~~~~~ Serialization stuff ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Reading Catalog node " + this); // NOI18N

        in.defaultReadObject();        
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Writing " + this); // NOI18N

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
                CatalogEntry catalogEntry = new CatalogEntry((String) key, peer);
                return new Node[] { 
                    new CatalogEntryNode(catalogEntry)
                };
            } catch (IntrospectionException ex) {
                return null;
            }
        }

        /**
          * Reloads catalog content
          */
        public void reload() {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug(" Reloading kids of " + peer + "..."); // NOI18N

            Set previous = new HashSet(keys);
            keys.clear();
            Iterator it = peer.getPublicIDs();
            if (it != null) {
                while (it.hasNext()) {
                    String publicID = (String) it.next();
                    keys.add(publicID);
                    if (previous.contains(publicID)) {
                        refreshKey(publicID);  // recreate node, the systemId may have changed
                    }
                }
            }    
            setKeys(keys);
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

    /**
     * Give to the action your own name
     */
    private static final class UnmountAction extends NodeAction {
        /** Serial Version UID */
        private static final long serialVersionUID = 3556006276357785484L;
        
        public UnmountAction() {
        }
        
        public String getName() {
            return Util.THIS.getString("LBL_unmount");
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(UnmountAction.class);
        }
        
        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes.length > 0) {
                for (int i = 0; i<activatedNodes.length; i++) {
                    Node me = activatedNodes[i];
                    CatalogNode self = (CatalogNode) me.getCookie(CatalogNode.class);
                    CatalogReader reader = (CatalogReader) self.getBean();
                    if (CatalogSettings.getDefault().isRemovable(reader)) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        protected void performAction(Node[] activatedNodes) {
            if (enable(activatedNodes) == false) return;
            for (int i = 0; i<activatedNodes.length; i++) {
                try {
                    Node me = activatedNodes[i];
                    CatalogNode self = (CatalogNode) me.getCookie(CatalogNode.class);
                    self.destroy();
                } catch (IOException ex) {
                    Util.THIS.debug("Cannot unmount XML entity catalog!", ex);
                }
            }
        }
        
        protected boolean asynchronous() {
            return false;
        }
        
    }
    
}
