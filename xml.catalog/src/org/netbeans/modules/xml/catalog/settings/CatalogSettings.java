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
package org.netbeans.modules.xml.catalog.settings;

import java.io.*;
import java.util.*;
import java.beans.*;

import org.openide.*;
import org.openide.util.HelpCtx;
import org.openide.util.io.NbMarshalledObject;

import org.netbeans.modules.xml.catalog.spi.*;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;


/** 
 * An externalizable pool holding mounted catalogs at per project basics.
 * <p>
 * @thread implementation is thread safe
 *
 * @author  Petr Kuzel
 */
public final class CatalogSettings implements Externalizable {

    /** Serial Version UID */
    private static final long serialVersionUID = 7895789034L;

    public static final int VERSION_1 = 1;  //my externalization protocol version

    /** Identifies property holding mounted catalogs */
    public static final String PROP_MOUNTED_CATALOGS = "catalogs"; // NOI18N
    
    /** 
     * Project has changed. You MUST switch to new settings instance. 
     * It is fired at the old instance.
     * @deprecated It is hack for NetBeans 3.3 lacking project system
     */
    public static final String PROP_PRJ_INSTANCE = "cat-prj-in";
    
    private Set mountedCatalogs = new HashSet();

    private PropertyChangeSupport listeners = null;; 
        
    private static ErrorManager err = null;

    // active result of lookup for this setting
    private static Lookup.Result result = null;
    
    // the only active instance in current project
    private static CatalogSettings instance = null;
    
    /** 
     * Just for externalization purposes.
     * It MUST NOT be called directly by a user code.
     */
    public CatalogSettings() {
        init();
    }
    

    /**
     * Initialized the instance from externalization.
     */
    private void init() {
        listeners = new PropertyChangeSupport(this);
    }
    
    
    /**
     * Return active settings <b>instance</b> in the only one active project.
     * @deprecated does not allow multiple opened projects
     */
    public static synchronized CatalogSettings getDefault() {
        if (result == null) {
            result = Lookup.getDefault().lookup(new Lookup.Template(CatalogSettings.class));
            
            // listen at project "switch", we are project setting
            result.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent e) {
                    CatalogSettings oldSettings = instance;
                    synchronized (CatalogSettings.class) {
                        instance = (CatalogSettings) Lookup.getDefault().lookup(CatalogSettings.class);
                    }
                    if (oldSettings != null) {
                        oldSettings.firePropertyChange(PROP_PRJ_INSTANCE, oldSettings, instance);
                    }
                }
            });
            
            // start result listening
            instance = (CatalogSettings) result.allInstances().iterator().next();
        }
        return instance;
    }
    
    /** 
     * Register a mounted catalog.
     * @param provider to be registered. Must not be null.
     */
    public final void addCatalog(CatalogReader provider) {
        synchronized (this) {
            if (provider == null)
                throw new IllegalArgumentException("null provider not permited"); // NOI18N
            mountedCatalogs.add(provider);
        }
        firePropertyChange(PROP_MOUNTED_CATALOGS, null, null);
    }
    
    /** 
     * Deregister provider represented by given class. 
     */
    public final void removeCatalog(CatalogReader provider) {
        synchronized (this) {
            mountedCatalogs.remove(provider);
        }
        firePropertyChange(PROP_MOUNTED_CATALOGS, null, null);
    }

        
    /**
     * Return iterator of providers of given class.
     * 
     * @param providerClasses returned providers will be assignable to it
     *                        e.g. <code>CatalogReader</code> class or <code>null</code>
     *                        as a wildcard.
     * @return providers of given class or all if passed <code>null/code> argument.
     *         It never returns null.
     */
    public final synchronized Iterator getCatalogs(Class[] providerClasses) {

        Iterator it = mountedCatalogs.iterator();
        if (providerClasses == null)
            return it;
        
        ArrayList list = new ArrayList();
        
try_next_provider:
        while (it.hasNext()) {
            Object next = it.next();
            
            // provider test
            
            for (int i=0; i<providerClasses.length; i++) {
                
                if (providerClasses[i].isAssignableFrom(next.getClass()) == false)
                    break try_next_provider;
            }
            
            // add passed
            
            list.add(next);
        }
        
        return list.iterator();
    }    

    // ~~~~~~~~~~~~~~~~~~~~~~ listeners ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    public void addPropertyChangeListener(PropertyChangeListener l){
        listeners.addPropertyChangeListener(l);
        
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.removePropertyChangeListener(l);
    }
    
    private void firePropertyChange(String name, Object oldValue, Object newValue) {
        listeners.firePropertyChange(name, oldValue, newValue);
    }
    
    // ~~~~~~~~~~~~~~~~~~ Persistent state ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /**
     * Read persistent catalog settings logging diagnostics information if needed.
     */
    public synchronized void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        //super.readExternal(in);        

        Util.debug("CatalogSettings.readExternal()"); // NOI18N

        int version = in.readInt();  //IN version
        
        // version switch
        
        if (version != VERSION_1) throw new StreamCorruptedException("Unsupported catalog externalization protocol version (" + version + ").");  // NOI18N
        
        int persistentCount = in.readInt();  //IN count
        
        for (int i = 0; i<persistentCount; i++) {

            String catalogClass = (String) in.readObject();  //IN class name
            NbMarshalledObject marshaled = (NbMarshalledObject) in.readObject(); //IN marshalled object
            try {                
                mountedCatalogs.add(marshaled.get());
            } catch (ClassNotFoundException ex) {
                //ignore probably missing provider class
                emgr().annotate(ex, Util.getString("EXC_deserialization_failed", catalogClass));
                emgr().notify(ErrorManager.INFORMATIONAL, ex);                
            } catch (IOException ex) {
                //ignore incompatible classes
                emgr().annotate(ex, Util.getString("EXC_deserialization_failed", catalogClass));
                emgr().notify(ErrorManager.INFORMATIONAL, ex);                                
            } catch (RuntimeException ex) {
                //ignore catalog that can not deserialize itself without NPE etc.
                emgr().annotate(ex, Util.getString("EXC_deserialization_failed", catalogClass));
                emgr().notify(ErrorManager.INFORMATIONAL, ex);                                
            }
        }
    }
    
    /**
     * Write persistent catalog settings as NbMarshalledObjects with some diagnostics information.
     */
    public synchronized void writeExternal(ObjectOutput out) throws IOException  {
        //super.writeExternal(out);

        Util.debug("CatalogSettings.writeExternal()"); // NOI18N

        out.writeInt(VERSION_1);  //OUT version
        
        int persistentCount = 0;

        Iterator it = mountedCatalogs.iterator();
        
        while (it.hasNext()) {
            Object next = it.next();
            if (next instanceof Serializable) {
                persistentCount++;
            }            
        }
        
        it = mountedCatalogs.iterator();
                
        out.writeInt(persistentCount);  //OUT count
        
        while (it.hasNext()) {
            Object next = it.next();
            if (next instanceof Serializable) {
                try {
                    NbMarshalledObject marshaled = new NbMarshalledObject(next);
                    out.writeObject(next.getClass().getName());  //OUT class name
                    out.writeObject(marshaled);  //OUT marshalled object
                } catch (IOException ex) {
                    // catalog can not be serialized
                    emgr().annotate(ex, Util.getString("EXC_serialization_failed", next.getClass()));
                    emgr().notify(ErrorManager.INFORMATIONAL, ex);
                } catch (RuntimeException ex) {
                    //skip this odd catalog
                    emgr().annotate(ex, Util.getString("EXC_serialization_failed", next.getClass()));
                    emgr().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }        
    }    
        
    /** Lazy initialized error manager. */
    private ErrorManager emgr() {
        if (err == null) {
            err = TopManager.getDefault().getErrorManager();
        }
        return err;
    }
    

    /**
     * For debugging purposes only.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("CatalogSettings:");
        Iterator it = mountedCatalogs.iterator();
        while (it.hasNext()) {
            buf.append(", " + it.next());
        }
        return buf.toString();
    }
    
    
}
