/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings.convertors;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.lang.ref.SoftReference;

import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.Environment;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.AbstractLookup;

import org.netbeans.spi.settings.Convertor;
import org.netbeans.modules.settings.Env;
import org.netbeans.modules.settings.ScheduledRequest;

/** Convertor handles serialdata format described in
 * http://www.netbeans.org/dtds/sessionsettings-1_0.dtd. The convertor overrides
 * implementation from org.netbeans.core.projects.SerialDataConvertor and adds
 * the upgrade possibility.
 * !!! KEEP CODE SYNCHRONOUS WITH org.netbeans.core.projects.SerialDataConvertor !!!
 *
 * @author  Jan Pokorsky
 */
public final class SerialDataConvertor implements PropertyChangeListener, FileSystem.AtomicAction {
    /** data object name cached in the attribute to prevent instance creation when
     * its node is displayed.
     * @see org.openide.loaders.InstanceDataObject#EA_NAME
     */
    static final String EA_NAME = "name"; // NOI18N
    /** lock used to sync read/write operations for .settings file */
    final Object READWRITE_LOCK = new Object();
    private final InstanceContent lkpContent;
    private final Lookup lookup;
    private final DataObject dobj;
    private final FileObject provider;
    private final SerialDataConvertor.NodeConvertor node;
    private SerialDataConvertor.SettingsInstance instance;
    private SaveSupport saver;
    private ErrorManager err;
    
    /** Creates a new instance of SDConvertor */
    public SerialDataConvertor(DataObject dobj, FileObject provider) {
        err = ErrorManager.getDefault().getInstance(this.getClass().getName());// +
            //dobj.getPrimaryFile().toString().replace('/', '.');
        this.dobj = dobj;
        this.provider = provider;
        lkpContent = new InstanceContent();
        
        if (isModuleEnabled()) {
            instance =  createInstance(null);
            lkpContent.add(instance);
        }
        lkpContent.add(this);
        node = new SerialDataConvertor.NodeConvertor();
        lkpContent.add(this, node);
        lookup = new AbstractLookup(lkpContent);
    }
    
    /** use just for write method purposes */
    private SerialDataConvertor() {
        lkpContent = null;
        lookup = null;
        dobj = null;
        provider = null;
        node = null;
    }
    
    /** create a writer able to store objects in the serialdata format. Used in
     * module layer for creating .settings file by
     * {@link org.openide.loaders.InstanceDataObject} InstanceDataObject
     * @see #write
     */
    public static Object createWriter() {
        return new SerialDataConvertor();
    }
    
    /** can store an object inst in the serialdata format
     * @param w stream into which inst is written
     * @param inst the setting object to be written
     * @exception IOException if the object cannot be written
     */
    public void write (java.io.Writer w, Object inst) throws java.io.IOException {
        XMLSettingsSupport.storeToXML10(inst, w, ModuleInfoManager.getDefault().getModuleInfo(inst.getClass()));
    }
    
    /** delegate to SaveSupport to handle an unfired setting object change
     * @see SerialDataNode#resolvePropertyChange
     */
    void handleUnfiredChange() {
        saver.propertyChange(null);
    }
    
    DataObject getDataObject() {
        return dobj;
    }
    
    FileObject getProvider() {
        return provider;
    }
    
    /** provides content like InstanceCookie, SaveCokie */
    public final Lookup getLookup() {
        return lookup;
    }
    
    /** create own InstanceCookie implementation */
    private SettingsInstance createInstance(Object inst) {
        return new SettingsInstance(inst);
    }
    
    /** method provides a support storing the setting */
    private SaveSupport createSaveSupport(Object inst) {
        return new SaveSupport(inst);
    }
    
    /** allow to listen on changes of the object inst; should be called when
     * new instance is created */
    private synchronized void attachToInstance(Object inst) {
        if (saver != null) {
            saver.removePropertyChangeListener(this);
            saver.flush();
        }
        saver = createSaveSupport(inst);
        saver.addPropertyChangeListener(this);
    }
    
    private void provideSaveCookie() {
        if (saver.isChanged()) {
            lkpContent.add(saver);
        } else {
            lkpContent.remove(saver);
        }
    }
    
    private void instanceCookieChanged(Object inst) {
        SaveSupport _saver = saver;
        if (_saver != null) {
            _saver.removePropertyChangeListener(this);
            lkpContent.remove(_saver);
            getScheduledRequest().cancel();
            saver = null;
        }
        
        lkpContent.remove(this, node);
        lkpContent.add(this, node);
        
        if (instance != null) {
            lkpContent.remove(instance);
            instance = null;
        }
        
        if (isModuleEnabled()) {
            instance = createInstance(inst);
            lkpContent.add(instance);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) return;

        String name = evt.getPropertyName();
        if (name == null)
            return;
        // setting was changed
        else if (name == SaveSupport.PROP_SAVE)
            provideSaveCookie();
        // .settings file was changed
        else if (name == SaveSupport.PROP_FILE_CHANGED) {
            miUnInitialized = true;
            if (mi != null) {
                mi.removePropertyChangeListener(this);
            }
            instanceCookieChanged(null);
        } else if(ModuleInfo.PROP_ENABLED.equals(evt.getPropertyName())) {

            boolean change;

            if (!Boolean.TRUE.equals (evt.getNewValue ())) {
                // a module has been disabled, use full checks
                //aModuleHasBeenChanged = true;

                // if wasModuleEnabled was true, we changed state
                change = wasModuleEnabled;
            } else {
                // a module was enabled, if wasModuleEnabled was false
                // we changed state
                change = !wasModuleEnabled;
            }

            // update wasModuleEnabled to current state of the module
            wasModuleEnabled = isModuleEnabled();
            
            if (change) {
                instanceCookieChanged(null);
            }
        }
    }
    
    private ModuleInfo mi;
    private boolean miUnInitialized = true;
    private boolean wasModuleEnabled;
    private boolean isModuleEnabled() {
        if (miUnInitialized) {
            mi = getModuleInfo();
            miUnInitialized = false;
            if (mi != null) {
                wasModuleEnabled = mi.isEnabled();
                mi.addPropertyChangeListener(this);
            }
        }
        return mi == null || mi.isEnabled();
    }
    
    private ModuleInfo getModuleInfo() {
        try {
            String module = new SettingsInstance(null).
                getSettings(true).getCodeNameBase();
            return module == null? null: ModuleInfoManager.getDefault().getModule(module);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }
    
    private static final ErrorManager _err = ErrorManager.getDefault();
    /** Little utility method for posting an exception
     *  to the default <CODE>ErrorManager</CODE> with severity
     *  <CODE>ErrorManager.INFORMATIONAL</CODE>
     */
    static void inform(Throwable t) {
	_err.notify(ErrorManager.INFORMATIONAL, t);
    }
    
    /** called by ScheduledRequest in order to perform the request */
    public void run() throws IOException {
        saver.writeDown();
    }
    
    /** scheduled request to store setting */
    private ScheduledRequest request;
    
    /** get the scheduled request to store setting */
    private synchronized ScheduledRequest getScheduledRequest() {
        if (request == null) {
            request = new ScheduledRequest(this.getDataObject().getPrimaryFile(), this);
        }
        return request;
    }
    
    //////////////////////////////////////////////////////////////////////////
    // SettingsInstance
    //////////////////////////////////////////////////////////////////////////
    
    /** InstanceCookie implementation */
    private final class SettingsInstance implements InstanceCookie.Of, InstanceCookie.Origin {
        
        /** created instance   */
        private SoftReference inst;
        
        /** holder of parsed settings  */
        private XMLSettingsSupport.SettingsRecognizer settings = null;
        
        private boolean doNotCheckContent = false;
        
        /** Creates new SettingsInstance   */
        public SettingsInstance(Object instance) {
            setCachedInstance(instance);
        }
        
        /** Getter for parsed settings
         * @param header if <code>true</code> parse just header(instanceof, module, classname)
         */
        private XMLSettingsSupport.SettingsRecognizer getSettings(boolean header) throws IOException {
            synchronized (this) {
                if (settings == null) {
                    synchronized (READWRITE_LOCK) {
                        settings = new XMLSettingsSupport.SettingsRecognizer(
                            header, getDataObject().getPrimaryFile());
                        settings.parse();
                    }
                    return settings;
                }
                if (!header) {
                    if (!settings.isAllRead()) {
                        settings.setAllRead(false);
                        settings.parse();
                    }
                }

                return settings;
            }
        }
        
        public Object instanceCreate() throws java.io.IOException, ClassNotFoundException {
            Object inst;
            XMLSettingsSupport.SettingsRecognizer recog;
            
            synchronized (this) {
                inst = getCachedInstance();
                if (inst != null) return inst;
            }
            
            recog = getSettings(false);
            inst = recog.instanceCreate();
            
            synchronized (this) {
                Object existing = getCachedInstance();
                if (existing != null) return existing;
                setCachedInstance(inst);
            }
            attachToInstance(inst);
            
            return inst;
        }
        
        public Class instanceClass() throws java.io.IOException, ClassNotFoundException {
            // cached
            Object inst = getCachedInstance();
            if (inst != null) {
                return inst.getClass();
            }
            
            XMLSettingsSupport.SettingsRecognizer recog = getSettings(false);
            return recog.instanceClass();
        }
        
        public boolean instanceOf(Class type) {
            try {
                return getSettings(true).getInstanceOf().contains(type.getName());
            } catch (IOException ex) {
                err.annotate(ex, getDataObject().getPrimaryFile().toString());
                inform(ex);
            }
            return false;
        }
        
        public String instanceName() {
            // try cached instance
            Object inst = getCachedInstance();
            if (inst != null) {
                return inst.getClass().getName();
            }
            
            try {
                return getSettings(true).instanceName();
            } catch (IOException ex) {
                err.annotate(ex, getDataObject().getPrimaryFile().toString());
                inform(ex);
                return ""; // NOI18N
            }
        }
        
        /** Returns the origin of the instance.
         * @return the original file
         */
        public FileObject instanceOrigin() {
            return getDataObject().getPrimaryFile();
        }
        
        private Object getCachedInstance() {
            return inst.get();
        }
        
        private void setCachedInstance(Object o) {
            inst = new SoftReference(o);
        }
        // called by InstanceDataObject to set new object
        public void setInstance(Object inst) throws IOException {
            instanceCookieChanged(inst);
            if (inst != null) {
                attachToInstance(inst);
                getScheduledRequest().runAndWait();
            }
        }
        
    }
    
    /** Support handles automatic setting objects storing and allows to identify
     * the origin of file events fired as a consequence of this storing
     */
    private final class SaveSupport extends org.openide.filesystems.FileChangeAdapter implements
    FileSystem.AtomicAction, SaveCookie, java.beans.PropertyChangeListener, org.netbeans.spi.settings.Saver {
        /** property means setting is changed and should be changed */
        public static final String PROP_SAVE = "savecookie"; //NOI18N
        /** property means setting file content is changed */
        public static final String PROP_FILE_CHANGED = "fileChanged"; //NOI18N
        
        /** Utility field holding list of PropertyChangeListeners.  */
        private java.util.ArrayList propertyChangeListenerList;
        
        /** setting is already changed */
        private boolean isChanged = false;
        /** file containing persisted setting */
        private final FileObject file;
        /** weak reference to setting object */
        private final java.lang.ref.WeakReference instance;
        /** remember whether the DataObject is a template or not; calling isTemplate() is slow  */
        private Boolean knownToBeTemplate = null;
        /** convertor for possible format upgrade */
        private Convertor convertor;
        
        /** Creates a new instance of SaveSupport  */
        public SaveSupport(Object inst) {
            this.instance = new java.lang.ref.WeakReference(inst);
            file = getDataObject().getPrimaryFile();
        }
        
        /** is setting object changed? */
        public final boolean isChanged() {
            return isChanged;
        }
        
        /** store setting or provide just SaveCookie? */
        private boolean acceptSave() {
            Object inst = instance.get();
            if (inst == null || !(inst instanceof java.io.Serializable) ||
                inst instanceof org.openide.windows.TopComponent) return false;
            
            return true;
        }
        
        /** place where to filter events comming from setting object */
        private boolean ignoreChange(PropertyChangeEvent pce) {
            if (knownToBeTemplate == null) knownToBeTemplate = getDataObject().isTemplate() ? Boolean.TRUE : Boolean.FALSE;
            return knownToBeTemplate.booleanValue();
        }
        
        /** get convertor for possible upgrade; can be null */
        private Convertor getConvertor() {
            return convertor;
        }

        /** try to find out convertor for possible upgrade and cache it; can be null */
        private Convertor initConvertor() {
            Object inst = instance.get();
            if (inst == null) {
                throw new IllegalStateException(
                    "setting object cannot be null: " + getDataObject());// NOI18N
            }

            try {
                FileObject newProviderFO = Env.findProvider(inst.getClass());
                if (newProviderFO != null) {
                    Object attrb = newProviderFO.getAttribute(Env.EA_PUBLICID);
                    if (attrb == null || !(attrb instanceof String)) {
                        throw new IOException("wrong attribute: " + //NOI18N
                            Env.EA_PUBLICID + ", provider: " + newProviderFO); //NOI18N
                    }
                    if (XMLSettingsSupport.INSTANCE_DTD_ID.equals(attrb)) {
                        convertor = null;
                        return convertor;
                    }
                    
                    attrb = newProviderFO.getAttribute(Env.EA_CONVERTOR);
                    if (attrb == null || !(attrb instanceof Convertor)) {
                        throw new IOException("cannot create convertor: " + //NOI18N
                            attrb + ", provider: " + newProviderFO); //NOI18N
                    } else {
                        convertor = (Convertor) attrb;
                        return convertor;
                    }
                }
            } catch (IOException ex) {
                inform(ex);
            }
            return convertor;
        }
        
        /** Registers PropertyChangeListener to receive events and initialize
         * listening to events comming from the setting object and file object.
         * @param listener The listener to register.
         */
        public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
            if (propertyChangeListenerList == null ) {
                propertyChangeListenerList = new java.util.ArrayList();
                Object inst = instance.get();
                if (inst == null) return;
                Convertor conv = initConvertor();
                if (conv != null) {
                    conv.registerSaver(inst, this);
                } else {
                    registerPropertyChangeListener(inst);
                }
                file.addFileChangeListener(this);
            }
            propertyChangeListenerList.add(listener);
        }
        
        /** Removes PropertyChangeListener from the list of listeners.
         * @param listener The listener to remove.
         */
        public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
            if (propertyChangeListenerList != null && !propertyChangeListenerList.isEmpty()) {
                propertyChangeListenerList.remove(listener);
                Object inst = instance.get();
                if (inst == null) return;
                
                Convertor conv = getConvertor();
                if (conv != null) {
                    conv.unregisterSaver(inst, this);
                } else {
                    unregisterPropertyChangeListener(inst);
                }
                file.removeFileChangeListener(this);
            }
        }
        
        /** try to register PropertyChangeListener to the setting object
         * to be notified about its changes.
         */
        private void registerPropertyChangeListener(Object inst) {
            // add propertyChangeListener
            try {
                java.lang.reflect.Method method = inst.getClass().getMethod(
                    "addPropertyChangeListener", // NOI18N
                    new Class[] {PropertyChangeListener.class});
                method.invoke(inst, new Object[] {this});
            } catch (NoSuchMethodException ex) {
                err.log(ErrorManager.INFORMATIONAL,
                "NoSuchMethodException: " + // NOI18N
                inst.getClass().getName() + ".addPropertyChangeListener"); // NOI18N
            } catch (IllegalAccessException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        /** @see #registerPropertyChangeListener
         */
        private void unregisterPropertyChangeListener(Object inst) {
            try {
                java.lang.reflect.Method method = inst.getClass().getMethod(
                    "removePropertyChangeListener", // NOI18N
                    new Class[] {PropertyChangeListener.class});
                method.invoke(inst, new Object[] {this});
            } catch (NoSuchMethodException ex) {
                err.log(ErrorManager.INFORMATIONAL,
                "NoSuchMethodException: " + // NOI18N
                inst.getClass().getName() + ".removePropertyChangeListener"); // NOI18N
                // just changes done through gui will be saved
            } catch (IllegalAccessException ex) {
                ErrorManager.getDefault().notify(ex);
                // just changes done through gui will be saved
            } catch (java.lang.reflect.InvocationTargetException ex) {
                ErrorManager.getDefault().notify(ex);
                // just changes done through gui will be saved
            }
        }
        
        /** Notifies all registered listeners about the event.
         * @param event The event to be fired
         * @see #PROP_FILE_CHANGED
         * @see #PROP_SAVE
         */
        private void firePropertyChange(String name) {
            java.util.ArrayList list;
            synchronized (this) {
                if (propertyChangeListenerList == null) return;
                list = (java.util.ArrayList)propertyChangeListenerList.clone();
            }
            java.beans.PropertyChangeEvent event =
            new java.beans.PropertyChangeEvent(this, name, null, null);
            for (int i = 0; i < list.size(); i++) {
                ((java.beans.PropertyChangeListener)list.get(i)).propertyChange(event);
            }
        }

        /** force to finish scheduled request */
        public void flush() {
            getScheduledRequest().forceToFinish();
        }
        
        private java.io.ByteArrayOutputStream buf;

        /** process events coming from a setting object */
        public final void propertyChange(java.beans.PropertyChangeEvent pce) {
            if (isChanged) {
                return;
            }
            if (ignoreChange(pce)) return ;
            isChanged = true;
            firePropertyChange(PROP_SAVE);
            if (acceptSave()) {
                getScheduledRequest().schedule(instance.get());
            }
        }
        
        public void markDirty() {
            if (isChanged) return;
            if (ignoreChange(null)) return;
            isChanged = true;
            firePropertyChange(PROP_SAVE);
        }
        
        public void requestSave() throws java.io.IOException {
            if (isChanged) return;
            if (ignoreChange(null)) return;
            isChanged = true;
            firePropertyChange(PROP_SAVE);
            getScheduledRequest().schedule(instance.get());
        }

        /** store buffer to the file. */
        public void run() throws IOException {
            if (!getDataObject().isValid()) {
                //invalid data object cannot be used for storing
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log("invalid data object cannot be used for storing " + getDataObject()); // NOI18N
                }
                return;
            }
            org.openide.filesystems.FileLock lock;
            java.io.OutputStream los;
            synchronized (READWRITE_LOCK) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log("saving " + getDataObject()); // NOI18N
                }
                lock = getScheduledRequest().getFileLock();
                if (lock == null) return;
                los = file.getOutputStream(lock);

                java.io.OutputStream os = new java.io.BufferedOutputStream(los, 1024);
                try {
                    buf.writeTo(os);
                    if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                        err.log("saved " + dobj); // NOI18N
                    }
                } finally {
                    os.close();
                }
            }
        }

        /** Implementation of SaveCookie.  */
        public void save() throws IOException {
            if (!isChanged) return;
            getScheduledRequest().runAndWait();
        }

        /** store the setting object even if was not changed */
        private void writeDown() throws IOException {
            Object inst = instance.get();
            if (inst == null) return ;
            
            java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream(1024);
            java.io.Writer w = new java.io.OutputStreamWriter(b, "UTF-8"); // NOI18N
            isChanged = false;
            try {
                Convertor conv = getConvertor();
                if (conv != null) {
                    conv.write(w, inst);
                } else {
                    write(w, inst);
                }
            } finally {
                w.close();
            }

            buf = b;
            file.getFileSystem().runAtomicAction(this);
            buf = null;
            synchronizeName(inst);
            if (!isChanged) firePropertyChange(PROP_SAVE);
        }
        
        /** try to synchronize file name with instance name */
        private void synchronizeName(Object inst) {
            java.lang.reflect.Method getter;
            try {
                try {
                    getter = inst.getClass().getMethod("getDisplayName", null); // NOI18N
                } catch (NoSuchMethodException me) {
                    getter = inst.getClass().getMethod("getName", null); // NOI18N
                }
            } catch (Exception ex) { // do nothing
                return;
            }
            
            try {
                String name = (String) getter.invoke(inst, null);
                String oldName = (String) dobj.getPrimaryFile().getAttribute(EA_NAME);
                if (name == null || !name.equals(oldName)) {
                    dobj.getPrimaryFile().setAttribute(EA_NAME, name);
                }
            } catch (Exception ex) {
                err.annotate(ex, dobj.getPrimaryFile().toString());
        	inform(ex);
            }
        }

        /** process events coming from the file object*/
        public void fileChanged(org.openide.filesystems.FileEvent fe) {
            if (fe.firedFrom(this)) return;
            firePropertyChange(PROP_FILE_CHANGED);
        }
        
    }

////////////////////////////////////////////////////////////////////////////
// Provider
////////////////////////////////////////////////////////////////////////////

    /** A provider for .settings files  containing serial data format
     * (hexa stream)
     */
    public final static class Provider implements Environment.Provider {
        private final FileObject providerFO;
        
        public static Environment.Provider create(FileObject fo) {
            return new Provider(fo);
        }

        private Provider(FileObject fo) {
            providerFO = fo;
        }

        public Lookup getEnvironment(DataObject dobj) {
            if (!(dobj instanceof org.openide.loaders.InstanceDataObject)) return Lookup.EMPTY;
            return new SerialDataConvertor(dobj, providerFO).getLookup();
        }

    }
    
////////////////////////////////////////////////////////////////////////////
// NodeConvertor
////////////////////////////////////////////////////////////////////////////
    
    /** allow to postpone the node creation */
    private static final class NodeConvertor implements InstanceContent.Convertor {
     
        public Object convert(Object o) {
            SerialDataConvertor convertor = (SerialDataConvertor) o;
            return new SerialDataNode(convertor);
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
