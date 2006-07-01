/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2002-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings;

import java.beans.PropertyChangeEvent;
import java.lang.ref.SoftReference;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.InstanceCookie;

import org.netbeans.spi.settings.Convertor;
import org.openide.util.Exceptions;

/** Provides the Lookup content.
 *
 * @author  Jan Pokorsky
 */
final class InstanceProvider extends org.openide.filesystems.FileChangeAdapter
implements java.beans.PropertyChangeListener, FileSystem.AtomicAction {
    /** Logging for events in this class */
    private static final Logger LOG = Logger.getLogger(InstanceProvider.class.getName()); // NOI18N
    
    /** container handling objects provided by {@link #lookup} */
    private final org.openide.util.lookup.InstanceContent lkpContent;
    /** container exposing setting to the outside world */
    private final org.openide.util.Lookup lookup;
    private final org.openide.loaders.DataObject dobj;
    private final FileObject settingFO;
    private final FileObject providerFO;
    private final NodeConvertor node;
    //save support
    private SaveSupport saver;
    private SaveCookie scCache;
    private boolean wasReportedProblem = false;
    private java.util.Set instanceOfSet;
    private String instanceClassName;
    /** lock used to sync read/write operations for .settings file */
    final Object READWRITE_LOCK = new Object();
    
    /** Creates a new instance of InstanceCooikeProvider */
    public InstanceProvider(org.openide.loaders.DataObject dobj, FileObject providerFO) {
//        System.out.println("new IP: " + dobj);
        this.settingFO = dobj.getPrimaryFile();
        this.providerFO = providerFO;
        this.dobj = dobj;
        
        settingFO.addFileChangeListener(
            FileUtil.weakFileChangeListener(this, settingFO));
        
        lkpContent = new org.openide.util.lookup.InstanceContent();
        lkpContent.add(createInstance(null));
        node = new NodeConvertor();
        lkpContent.add(this, node);
        lookup = new org.openide.util.lookup.AbstractLookup(lkpContent);
    }
    
    /** provides content like InstanceCookie, SaveCokie */
    public Lookup getLookup() {
        return lookup;
    }
    /** file contanining various attributes related to setting like convertor
     * class, ...
     */
    FileObject getProvider() {
        return providerFO;
    }
    /** file containing a persisted setting object */
    FileObject getFile () {
        return settingFO;
    }
    
    org.openide.loaders.DataObject getDataObject() {
        return dobj;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) return;

        String name = evt.getPropertyName();
        if (name == null)
            return;
        else if (name == SaveSupport.PROP_SAVE)
            provideSaveCookie();
        else if (name == SaveSupport.PROP_FILE_CHANGED) {
            synchronized (this) {
                instanceOfSet = null;
            }
            instanceCookieChanged(null);
        }
    }
    
    /** process events coming from the file object*/
    public void fileChanged(org.openide.filesystems.FileEvent fe) {
        if (saver != null && fe.firedFrom((FileSystem.AtomicAction) saver.getSaveCookie())) return;
        propertyChange(new PropertyChangeEvent(this, SaveSupport.PROP_FILE_CHANGED, null, null));
    }
  
    public void fileDeleted(org.openide.filesystems.FileEvent fe) {
        if (saver != null && fe.firedFrom((FileSystem.AtomicAction) saver.getSaveCookie())) return;
        releaseInstance();
    }
    
    
    /** allow to listen on changes of the object inst; should be called when
     * new instance is created */
    private synchronized void attachToInstance(Object inst) {
        if (saver != null) {
            saver.removePropertyChangeListener(this);
            getScheduledRequest().forceToFinish();
        }
        saver = createSaveSupport(inst);
        saver.addPropertyChangeListener(this);
    }
    
    /** create own InstanceCookie implementation */
    private InstanceCookie.Of createInstance(Object inst) {
        return new InstanceCookieImpl(inst);
    }
    
    /** method provides a support storing the setting */
    private SaveSupport createSaveSupport(Object inst) {
        return new SaveSupport(this, inst);
    }
    
    private void provideSaveCookie() {
        SaveCookie scNew = saver.getSaveCookie();
        if (scCache != null) {
            if (!saver.isChanged()) {
                if (LOG.isLoggable(Level.FINE)) LOG.fine("remove save cookie: " + dobj); // NOI18N
                lkpContent.remove(scCache);
                scCache = null;
                return;
            }
        } else {
            if (saver.isChanged()) {
                scCache = scNew;
                if (LOG.isLoggable(Level.FINE)) LOG.fine("add save cookie: " + dobj + " cookie: " + scNew); // NOI18N
                lkpContent.add(scNew);
                return;
            }
        }
    }
    
    private void releaseInstance() {
        SaveSupport _saver = saver;
        if (_saver != null) {
            _saver.removePropertyChangeListener(this);
        }
        
        if (scCache != null) {
            if (LOG.isLoggable(Level.FINE)) LOG.fine("release instance and remove save cookie: " + dobj); // NOI18N
            lkpContent.remove(scCache);
            getScheduledRequest().cancel();
            scCache = null;
        }
        
        lkpContent.remove(this, node);
    }
    
    private void instanceCookieChanged(Object inst) {
        if (LOG.isLoggable(Level.FINE)) LOG.fine("instanceCookieChanged: " + dobj + " inst: " + inst); // NOI18N
        releaseInstance();
        
        lkpContent.add(this, node);
        
        Object ic = lookup.lookup(InstanceCookie.class);
        lkpContent.remove(ic);

        Object newCookie = createInstance(inst);
        lkpContent.add(newCookie);
        if (LOG.isLoggable(Level.FINE)) LOG.fine("cookie replaced: " + dobj + " old: " + ic + " new: " + newCookie); // NOI18N
    }
    
    private Convertor convertor;
    
    /** find out  proper convertor */
    Convertor getConvertor() throws IOException {
        if (convertor == null) {
            Object attrb = providerFO.getAttribute(Env.EA_CONVERTOR);
            if (attrb == null || !(attrb instanceof Convertor)) {
                throw new IOException("cannot create convertor: " + attrb + ", provider:" +providerFO); //NOI18N
            }
            convertor = (Convertor) attrb;
        }
        return convertor;
    }
    
    /** find out setting object class name */
    private synchronized String getInstanceClassName() {
        if (instanceClassName == null) {
            Object name = providerFO.getAttribute(Env.EA_INSTANCE_CLASS_NAME);
            if (name != null && name instanceof String) {
                instanceClassName = org.openide.util.Utilities.translate((String) name);
            } else {
                instanceClassName = null;
            }
        }
        return instanceClassName;
    }
    
    public String toString() {
        return this.getClass().getName() + '@' +
            Integer.toHexString(System.identityHashCode(this)) +
            '[' + getDataObject() + ", " + getProvider() + ']';
    }
    
    
    /** called by ScheduledRequest in order to perform the request */
    public void run() throws IOException {
        saver.writeDown();
    }
    
    /** scheduled request to store setting */
    private ScheduledRequest request;
    
    /** get the scheduled request to store setting */
    synchronized ScheduledRequest getScheduledRequest() {
        if (request == null) {
            request = new ScheduledRequest(settingFO, this);
        }
        return request;
    }
    
    /////////////////////////////////////////////////////////////////////////
    // InstanceCookieImpl
    /////////////////////////////////////////////////////////////////////////
    
    /** InstanceCookie implementation. */
    final class InstanceCookieImpl implements InstanceCookie.Of {
        private SoftReference cachedInstance;// = new SoftReference(null);
        
        public InstanceCookieImpl(Object inst) {
            setCachedInstance(inst);
        }
        
        public Class instanceClass() throws IOException, ClassNotFoundException {
            String name = getInstanceClassName();
            if (name == null) {
                return instanceCreate().getClass();
            } else {
                return ((ClassLoader)Lookup.getDefault().lookup(ClassLoader.class)).loadClass(name);
            }
        }

        public Object instanceCreate() throws java.io.IOException, ClassNotFoundException {
            Object inst;
            
            synchronized (this) {
                inst = getCachedInstance();
                if (inst != null) return inst;
            }
            
            try {
                synchronized (READWRITE_LOCK) {
                    java.io.Reader r = ContextProvider.createReaderContextProvider(
                        new java.io.InputStreamReader(settingFO.getInputStream(),"UTF-8"), //NOI18N
                        getFile()
                    );
                    inst = getConvertor().read(r);
                }
            } catch (IOException ex) {
                throw (IOException) Exceptions.attachLocalizedMessage(ex,
                                                  InstanceProvider.this.toString());
            } catch (ClassNotFoundException ex) {
                throw (ClassNotFoundException) Exceptions.attachLocalizedMessage(ex,
                                                  InstanceProvider.this.toString());
            }
            
            synchronized (this) {
                Object existing = getCachedInstance();
                if (existing != null) return existing;
                setCachedInstance(inst);
            }
            attachToInstance(inst);
            
            return inst;
        }

        public String instanceName() {
            String name = getInstanceClassName();
            if (name != null) return name;
            
            Exception e = null;
            try {
                return instanceClass().getName();
            } catch (IOException ex) {
                e = ex;
            } catch (ClassNotFoundException ex) {
                e = ex;
            }
            if (e != null && !wasReportedProblem) {
                wasReportedProblem = true;
                Exceptions.attachLocalizedMessage(e, dobj.toString());
                Logger.global.log(Level.WARNING, null, e);
            }
            return "Unknown"; // NOI18N
        }

        public boolean instanceOf(Class type) {
            synchronized (InstanceProvider.this) {
                if (instanceOfSet == null) {
                    instanceOfSet = Env.parseAttribute(providerFO.getAttribute(Env.EA_INSTANCE_OF));
                    java.util.Iterator it = instanceOfSet.iterator();
                    instanceOfSet = new java.util.HashSet(instanceOfSet.size() * 5 / 4);
                    while (it.hasNext()) {
                        instanceOfSet.add(org.openide.util.Utilities.translate((String) it.next()));
                    }
                }
            }
            if (instanceOfSet.isEmpty()) {
                Exception e = null;
                try {
                    return type.isAssignableFrom(instanceClass());
                } catch (IOException ex) {
                    e = ex;
                } catch (ClassNotFoundException ex) {
                    e = ex;
                }
                if (e != null && !wasReportedProblem) {
                    wasReportedProblem = true;
                    Exceptions.attachLocalizedMessage(e, dobj.toString());
                    Logger.global.log(Level.WARNING, null, e);
                }
                return false;
            } else {
                return instanceOfSet.contains(type.getName());
            }
        }
        
        // called by InstanceDataObject to set new object
        public void setInstance(Object inst, boolean save) throws IOException {
            instanceCookieChanged(inst);
            if (inst != null) {
                attachToInstance(inst);
                if (save) getScheduledRequest().runAndWait();
            }
        }
    
        private Object getCachedInstance() {
            return cachedInstance.get();
        }
        private void setCachedInstance(Object inst) {
            cachedInstance = new SoftReference(inst);
        }
    }
    
////////////////////////////////////////////////////////////////////////////
// NodeConvertor
////////////////////////////////////////////////////////////////////////////
    
    /** allow to postpone the node creation */
    private static final class NodeConvertor implements org.openide.util.lookup.InstanceContent.Convertor {
        NodeConvertor() {}
     
        public Object convert(Object o) {
            InstanceProvider ip = (InstanceProvider) o;
            return new org.netbeans.modules.settings.convertors.SerialDataNode(ip.getDataObject());
        }
     
        public Class type(Object o) {
            return org.openide.nodes.Node.class;
        }
     
        public String id(Object o) {
            // Generally irrelevant in this context.
            return o.toString();
        }
     
        public String displayName(Object o) {
            // Again, irrelevant here.
            return o.toString();
        }
     
    }
}
