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

import java.awt.event.*;
import java.awt.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.net.*;

import javax.swing.event.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.actions.*;
import org.openide.util.datatransfer.*;
import org.openide.actions.*;

import org.netbeans.modules.xml.catalog.spi.*;
import org.netbeans.modules.xml.catalog.impl.*;
import org.netbeans.modules.xml.catalog.settings.CatalogSettings;

/**
 * Node representing catalog root in runtime tab.
 * To be placed in manifest file as:
 * <pre>
 * Name: org.netbeans.modules.xml.catalog.CatalogNode.class
 * OpenIDE-Module-Class: Node
 * </pre>
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class CatalogRootNode extends AbstractNode {

    private static final boolean DEBUG = false;
    
    /** Creates new CatalogNode */
    public CatalogRootNode() {
        super(new RootChildren());
        setName("XML-CATALOG"); // NOI18N
        setDisplayName (Util.getString("TEXT_catalog_root")); // NOI18N
        setIconBase("org/netbeans/modules/xml/catalog/resources/catalog-root");  // NOI18N
    }
    
    protected SystemAction[] createActions() {
        return new SystemAction[] {
            SystemAction.get(NewAction.class),
            null,
            SystemAction.get(PropertiesAction.class)            
        };
    }

    /** We can mount entity catalogs. */
    public NewType[] getNewTypes() {
        return new NewType[] {new CatalogMounter()};
    }
    
    /** 
     * Mounts new catalalog as specified by user. 
     */
    class CatalogMounter extends NewType implements ActionListener {

        CatalogMounterModel model = null;
        Dialog myDialog = null;
        
        public void create() throws IOException {
            
            Iterator it = ProvidersRegistry.getProviderClasses(new Class[] {CatalogReader.class});
            
            model = new CatalogMounterModel(it);
            Object rpanel = new CatalogMounterPanel(model);
            DialogDescriptor dd = new DialogDescriptor(rpanel,
                                  Util.getString ("PROP_Mount_Catalog"), false, this); // NOI18N
            myDialog = TopManager.getDefault().createDialog(dd);
            myDialog.show();
        }

        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == DialogDescriptor.OK_OPTION) {
                
                Object catalog = model.getCatalog();
                CatalogSettings.getDefault().addCatalog((CatalogReader)catalog);
                
            }
            if (myDialog != null) {
                myDialog.dispose();
                myDialog = null;
            }
        }
        
        public String getName() {
            return Util.getString ("PROP_Mount_Catalog"); // NOI18N
        }
    }
    

    // ~~~~~~~~~~~~~~~~~~~~~~ Serialization stuff ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        if (DEBUG) Util.trace("Reading CatalogRoot node " + this); // NOI18N
        in.defaultReadObject();        
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        if (DEBUG) Util.trace("Writing " + this); // NOI18N
        out.defaultWriteObject();        
    }

    
    // ~~~~~~~~~~~~~~~~~~~~~~ NODE KIDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    

    /**
     * Kids driven by CatalogSettings. Only one instance may be used
     * since redefined equals() method.
     */
    public static class RootChildren extends Children.Keys implements Comparator, PropertyChangeListener {
        
        /** Contains CatalogReader instances. */
        private final TreeSet keys = new TreeSet(this);

        /**
          * Create new keys, register itself as listener.
          */
        public void addNotify() {
            createKeys();                        
            CatalogSettings.getDefault().addPropertyChangeListener(this);
        }

        /**
          * Remove listener and keys.
          */
        public void removeNotify() {
            CatalogSettings.getDefault().removePropertyChangeListener(this);
            keys.clear();
            setKeys(keys);
        }

        /**
          * Create CatalogNodes initialized by provider instance.
          */
        public Node[] createNodes(Object key) {        
            try {
                return new Node[] { new CatalogNode((CatalogReader)key) };
            } catch (IntrospectionException ex) {
                return new Node[] {};
            }
        }

        /**
          * The only instance (see equals) listens on ProvidersRegistry
          * for its state changes.
          */
        public void propertyChange(PropertyChangeEvent e) {
            if (CatalogSettings.PROP_MOUNTED_CATALOGS.equals(e.getPropertyName())) {
                createKeys();
            }
        }
        
        /** 
          * Creates new keys according to CatalogSettings.
          */
        private void createKeys() {
            keys.clear();
            Iterator it = CatalogSettings.getDefault().getCatalogs(new Class[] {CatalogReader.class});
            while (it.hasNext()) {
                keys.add(it.next());
            }                
            setKeys(keys);
        }
        
        /** 
          * We are also comparators. Use class based equality.
          */
        public boolean equals(java.lang.Object peer) {
            return peer.getClass().equals(getClass());
        }        
        
        /**
         * Compare keys giving highest priority to system catalog.
         * Other catalogs sort by display name if available.
         */
        public int compare(java.lang.Object one,java.lang.Object two) {
            if (one instanceof SystemCatalogReader) return -1;
            if (two instanceof SystemCatalogReader) return 1;
            if (one instanceof CatalogDescriptor && two instanceof CatalogDescriptor) {
                return (((CatalogDescriptor)one).getDisplayName()).compareTo(
                    ((CatalogDescriptor)two).getDisplayName()
                );
            }
            if (one instanceof CatalogDescriptor) return -1;
            if (two instanceof CatalogDescriptor) return 1;            
            return 0;
        }
        
    }
 
}
