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
package org.netbeans.modules.xml.catalog.impl;

import java.awt.*;
import java.io.*;
import java.beans.*;
import java.util.*;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.openide.util.*;

import org.netbeans.modules.xml.catalog.spi.*;
import org.netbeans.modules.xml.catalog.lib.*;

/**
 * An abstract catalog implementation featuring firing support and
 * properties describing common catalog content.
 * <p>
 * It implements but does not declare that implements varios methods
 * from EntityResolver, CatalogDescriptor and CatalogReader interfaces.
 *
 * @author  Petr Kuzel
 */
public abstract class AbstractCatalog {


    private static final boolean DEBUG = false;

    /** Public identifier mappings. */
    private Map publicMap = new HashMap();

    /** System identifier mappings (aliases). */
    private Map systemMap = new HashMap();

    private String location;
    
    private CatalogListener catalogListener = null;
    
    // catalog delegation and chaining
    
    /** Delegates. */
    private Map delegate = new HashMap();
    
    /** Delegates ordering. */
    private Vector delegateOrder = new Vector();

    /** Contains patch catalogs. */
    protected Vector extenders = new Vector();
    
    //
    // Public methods
    //

    /**
     * Set catalog location URI.
     */
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getLocation() {
        return location;
    }
    
    
    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void addCatalogListener(CatalogListener l) {
        catalogListener = l;
    }
    
    /**
     * Optional operation couled with addCatalogListener.
     * @see addCatalogListener
     */
    public void removeCatalogListener(CatalogListener l) {
        catalogListener = null;
    }
    

    protected void notifyInvalidate() {
        if (catalogListener != null) {                
            catalogListener.notifyInvalidate();                
        }            
    }
    
    /**
     * Adds a public to system identifier mapping.
     *
     * @param publicId The public identifier, or "key".
     * @param systemId The system identifier, or "value".
     */
    public void addPublicMapping(String publicId, String systemId) {
        publicMap.put(publicId, systemId);
    }

    /**
     * Removes a public identifier mapping.
     *
     * @param publicId The public identifier to remove.
     */
    public void removePublicMapping(System publicId) {
        publicMap.remove(publicId);
    }

    /** Returns an enumeration of public identifier mapping keys. */
    public Iterator getPublicMappingKeys() {
        return publicMap.keySet().iterator();
    }

    /**
     * Returns a public identifier mapping.
     *
     * @param publicId The public identifier, or "key".
     *
     * @return Returns the system identifier value or null if there
     *         is no mapping defined.
     */
    public String getPublicMapping(String publicId) {
        return (String)publicMap.get(publicId);
    }

    /**
     * Adds a system identifier alias.
     *
     * @param publicId The system identifier "key".
     * @param systemId The system identifier "value".
     */
    public void addSystemMapping(String systemId1, String systemId2) {
        systemMap.put(systemId1, systemId2);
    }

    /**
     * Removes a system identifier alias.
     *
     * @param systemId The system identifier to remove.
     */
    public void removeSystemMapping(String systemId) {
        systemMap.remove(systemId);
    }

    /** Returns an enumeration of system identifier mapping keys. */
    public Iterator getSystemMappingKeys() {
        return systemMap.keySet().iterator();
    }

    /**
     * Clean content of all internal structures
     */
    protected void clearAll() {
        if (DEBUG) Util.trace("AbstractCatalog: clearing maps"); // NOI18N
        publicMap.clear();
        systemMap.clear();
        delegate.clear();
        delegateOrder.clear();
        extenders.clear();
    }
    
    /**
     * Returns a system identifier alias.
     *
     * @param systemId The system identifier "key".
     *
     * @return Returns the system identifier alias value or null if there
     *         is no alias defined.
     */
    public String getSystemMapping(String systemId) {
        return (String)systemMap.get(systemId);
    }

    
    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public Iterator getPublicIDs() {
        return getPublicIDs(""); // NOI18N
    }

    /** 
     * Obtain public IDs that starts with given prefix.
     */
    private Iterator getPublicIDs(String prefix) {        

        if (prefix == null) throw new IllegalArgumentException();

        IteratorIterator set = new IteratorIterator();
        set.add(getPublicMappingKeys());
        
        Iterator it = extenders.iterator();
        while (it.hasNext()) {
            set.add(((AbstractCatalog) it.next()).getPublicIDs());
        }

        Enumeration en = getDelegateCatalogKeys();
        while (en.hasMoreElements()) {
            String _prefix = (String) en.nextElement();
            AbstractCatalog delegee = (AbstractCatalog) delegate.get(_prefix);
            set.add(delegee.getPublicIDs(_prefix));
        } 

        return new FilterIterator(set, new PrefixFilter(prefix));
    }
    
    private class PrefixFilter implements FilterIterator.Filter {
        
        private final String prefix;
        
        PrefixFilter(String prefix) {
            this.prefix = prefix;
        }
        
        public boolean accept(Object obj) {
            return ((String)obj).startsWith(prefix);            
        }
    }
    

    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId) {
        return getPublicMapping(publicId);
    }
    
    /**
     * Resolves external entities.
     *
     * @param publicId The public identifier used for entity resolution.
     * @param systemId If the publicId is not null, this systemId is
     *                 to be considered the default system identifier;
     *                 else a system identifier alias mapping is
     *                 requested.
     *
     * @return Returns the input source of the resolved entity or null
     *         if no resolution is possible.
     *
     * @exception org.xml.sax.SAXException Exception thrown on SAX error.
     * @exception java.io.IOException Exception thrown on i/o error. 
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException 
    {
        
        // public id -> system id
        InputSource ret = resolvePublicId(publicId);
        if (ret != null) return ret;

        // system id(1) -> system id(2)
        return resolveSystemId(systemId);
    }
    
    protected InputSource resolvePublicId(String publicId) {

        // public id -> system id
        if (publicId != null) {
            String value = getPublicMapping(publicId);
            if (value != null) {                
                InputSource input = new InputSource(value);
                input.setPublicId(publicId);
                return input;
            }
        }
        
        return null;
    }
    
    protected InputSource resolveSystemId(String systemId) {

        if (systemId != null) {
            String value = getSystemMapping(systemId);
            if (value == null) {
                value = systemId;  //??? is it good
            }

            return new InputSource(value);
        }
        
        return null;
    }
    
    // Catalog delegation stuff ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /**
     * Adds a delegate mapping. If the prefix of a public identifier
     * matches a delegate prefix, then the delegate catalog is
     * searched in order to resolve the identifier.
     * <p>
     * This method makes sure that prefixes that match each other
     * are inserted into the delegate list in order of longest prefix
     * length first.
     *
     * @param prefix  The delegate prefix.
     * @param catalog The delegate catalog.
     */
    public void addDelegateCatalog(String prefix, AbstractCatalog catalog) {
        
        synchronized (delegate) {
            // insert prefix in proper order
            if (!delegate.containsKey(prefix)) {
                int size = delegateOrder.size();
                boolean found = false;
                for (int i = 0; i < size; i++) {
                    String element = (String)delegateOrder.elementAt(i);
                    if (prefix.startsWith(element) || prefix.compareTo(element) < 0) {
                        delegateOrder.insertElementAt(prefix, i);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    delegateOrder.addElement(prefix);
                }
            }
            
            // replace (or add new) prefix mapping
            delegate.put(prefix, catalog);
        }
        
    }
    
    /**
     * Removes a delegate.
     *
     * @param prefix The delegate prefix to remove.
     */
    public void removeDelegateCatalog(String prefix) {
        
        synchronized (delegate) {
            delegate.remove(prefix);
            delegateOrder.removeElement(prefix);
        }
        
    } // removeDelegateCatalog(String)
    
    /** Returns an enumeration of delegate prefixes. */
    public Enumeration getDelegateCatalogKeys() {
        return delegateOrder.elements();
    }
    
    /** Returns the catalog for the given delegate prefix. */
    public AbstractCatalog getDelegateCatalog(String prefix) {
        return (AbstractCatalog)delegate.get(prefix);
    }
    
    
    
    // Listeners ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
    
    
    
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * Should provide callbacks on PROP_CATALOG_ICON, PROP_CATALOG_DESC 
     * and PROP_CATALOG_NAME changes as defined in CatalogDescriptor.
     */    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String prop, Object val1, Object val2) {
        support.firePropertyChange(prop, val1, val2);
    }
    
    /** Get icon from bean info or null. */
    protected Image getDefaultIcon(int type) {
        try {
            BeanInfo info = Utilities.getBeanInfo(getClass());
            return info.getIcon(type);
        } catch (IntrospectionException ex) {
            return null;
        }                
    }
    
    /** 
     * Badge catalog icon with error sign. 
     * @return null
     */
    protected Image getDefaultErrorIcon(int type) {
        if (getDefaultIcon(type) == null) return null;
        
        return null;
    }
                
    private void trace(String msg) {
        Util.trace(msg);
    }
    
}
