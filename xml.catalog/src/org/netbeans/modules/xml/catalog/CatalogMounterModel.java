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

import java.beans.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.openide.util.Utilities;

import org.netbeans.modules.xml.catalog.spi.*;

/**
 * Data holder driving CatalogMounterPanel.
 * Selected getCatalog() points to a catalog that have been choosen and customized 
 * by a user.
 *
 * @author  Petr Kuzel
 * @version 
 */
public class CatalogMounterModel extends Object {
    
    private Object catalog = null;  // selected & customized catalog instance
    
    private ComboBoxModel cxModel = null;  // model containig CatalogMounterModel.Entries
    
    private ChangeListener changeListener = null;
    
    /** Creates new CatalogMounterModel */
    public CatalogMounterModel(Iterator providers) {        
        
        Vector providersList = new Vector();
        while (providers.hasNext()) {
            providersList.add(new Entry((Class)providers.next()));
        }
        
        cxModel = new DefaultComboBoxModel(providersList);
        cxModel.addListDataListener(new Lis());
        initCatalog();
    }
                    
    /**
     * Currently selected & customized catalog instance.
     * (may return null if no provider available)
     */
    public Object getCatalog() {
        return catalog;
    }

    /**
     * Customizer class of current catalog.
     * @return Customizer instance it needs to be initialized by
     * setObject(getCatalog()); (may return null if no provider available)
     */
    public Customizer getCatalogCustomizer() {
        if (catalog == null) return null;
        return Util.getProviderCustomizer(catalog.getClass());
    }
 
    /**
     * Form visualizing this model have to use this method for 
     * obtaining model for Comboboxes or lists.
     */
    public ComboBoxModel getCatalogComboBoxModel() {
        return cxModel;
    }

    /**
     * A view can listen on our state.
     */
    public void setChangeListener(ChangeListener l) {
        changeListener = l;
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~ IMPL ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
        
    private Entry getSelectedEntry() {
        return (Entry) cxModel.getSelectedItem();
    }

    /** Set selected calatog instance to new uncustomized catalog. */
    private void initCatalog() {

        Entry entry = getSelectedEntry();                
        if (entry == null) {
            catalog = null;
        } else {
            catalog = Util.createProvider(entry.src);
        }
        
        if (changeListener != null) changeListener.stateChanged(new ChangeEvent(this));
    }
    
    /**
     * Wrapper class for ComboModel members redefinig toSting() method.
     */
    private class Entry {
        
        String name = null;
        Class src;
        
        public Entry(Class src) {
            this.src = src;
            try {
                name = Utilities.getBeanInfo(src).getBeanDescriptor().getDisplayName();
            } catch (IntrospectionException ex) {
                name = src.toString();
            }
        }
        
        public String toString() {
            return name;
        }
    }

    
    /**
     * Listen on combo model and update selected catalog instance.
     * Implementation calls CatalogMounterModel initCatalog() on selection
     * change.
     */
    private class Lis implements ListDataListener {
        
        public void contentsChanged(ListDataEvent e) {
            initCatalog();
        }
                
        public void intervalAdded(ListDataEvent e) {
            initCatalog();
        }
                
        public void intervalRemoved(ListDataEvent e) {
            initCatalog();
        }
    }
}
