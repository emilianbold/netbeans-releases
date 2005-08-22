/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview;

import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.windows.CloneableTopComponent;
import org.openide.ErrorManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Arrays;
import java.util.Date;
import java.lang.ref.WeakReference;

/**
 * XmlMultiviewDataObject.java
 *
 * Created on October 5, 2004, 10:49 AM
 * @author  mkuchtiak
 */
public abstract class XmlMultiViewDataObject extends MultiDataObject implements CookieSet.Factory {

    public static final String PROP_DOCUMENT_VALID = "document_valid"; //NOI18N
    public static final String PROP_SAX_ERROR = "sax_error"; //NOI18N
    protected static final String PROPERTY_DATA_MODIFIED = "data modified";  //NOI18N
    protected static final String PROPERTY_DATA_UPDATED = "data changed";  //NOI18N
    private XmlMultiViewEditorSupport editorSupport;
    private org.xml.sax.SAXException saxError;

    private final DataCache dataCache = new DataCache();
    private transient byte[] buffer = null;
    private transient long timeStamp = 0;
    private transient WeakReference lockReference;


    private AbstractMultiViewElement activeMVElement;

    private final SaveCookie saveCookie = new SaveCookie() {
        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws java.io.IOException {
            getEditorSupport().saveDocument();
        }
    };

    /** Creates a new instance of XmlMultiViewDataObject */
    public XmlMultiViewDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        getCookieSet().add(XmlMultiViewEditorSupport.class, this);
    }

    public org.openide.nodes.Node.Cookie createCookie(Class clazz) {
        if (clazz.isAssignableFrom(XmlMultiViewEditorSupport.class)) {
            return getEditorSupport();
        } else {
            return null;
        }
    }

    /** Gets editor support for this data object. */
    protected synchronized XmlMultiViewEditorSupport getEditorSupport() {
        if(editorSupport == null) {
            editorSupport = new XmlMultiViewEditorSupport(this);
        }
        return editorSupport;
    }

    /** enables to switch quickly to XML perspective in multi view editor
     */
    public void goToXmlView() {
        getEditorSupport().goToXmlPerspective();
    }

    protected void setSaxError(org.xml.sax.SAXException saxError) {
        org.xml.sax.SAXException oldError = this.saxError;
        this.saxError=saxError;
        if (oldError==null) {
            if (saxError != null) {
                firePropertyChange(PROP_DOCUMENT_VALID, Boolean.TRUE, Boolean.FALSE);
            }
        } else {
            if (saxError == null) {
                firePropertyChange(PROP_DOCUMENT_VALID, Boolean.FALSE, Boolean.TRUE);
            }
        }

        String oldErrorMessage = getErrorMessage(oldError);
        String newErrorMessage = getErrorMessage(saxError);
        if (oldErrorMessage==null) {
            if (newErrorMessage!=null) {
                firePropertyChange(PROP_SAX_ERROR, null, newErrorMessage);
            }
        } else if (!oldErrorMessage.equals(newErrorMessage)) {
            firePropertyChange(PROP_SAX_ERROR, oldErrorMessage, newErrorMessage);
        }
    }

    private static String getErrorMessage(Exception e) {
        return e == null ? null : e.getMessage();
    }

    public org.xml.sax.SAXException getSaxError() {
        return saxError;
    }

    /** Icon for XML View */
    protected java.awt.Image getXmlViewIcon() {
        return org.openide.util.Utilities.loadImage("org/netbeans/modules/xml/multiview/resources/xmlObject.gif"); //NOI18N
    }

    /** MultiViewDesc for MultiView editor
     */
    protected abstract DesignMultiViewDesc[] getMultiViewDesc();

    public void setLastOpenView(int index) {
        getEditorSupport().setLastOpenView(index);
    }

    /** provides renaming of super top component */
    protected FileObject handleRename(String name) throws IOException {
        FileObject retValue = super.handleRename(name);
        getEditorSupport().updateDisplayName();
        return retValue;
    }

    /**
     * Set whether the object is considered modified.
     * Also fires a change event.
     * If the new value is <code>true</code>, the data object is added into a {@link #getRegistry registry} of opened data objects.
     * If the new value is <code>false</code>,
     * the data object is removed from the registry.
     */
    public void setModified(boolean modif) {
        super.setModified(modif);
        //getEditorSupport().updateDisplayName();
        if (modif) {
            // Add save cookie
            if (getCookie(SaveCookie.class) == null) {
                getCookieSet().add(saveCookie);
            }
        } else {
            // Remove save cookie
            if(saveCookie.equals(getCookie(SaveCookie.class))) {
                getCookieSet().remove(saveCookie);
            }

        }
    }

    public boolean canClose() {
        final CloneableTopComponent topComponent = ((CloneableTopComponent) getEditorSupport().getMVTC());
        Enumeration enumeration = topComponent.getReference().getComponents();
        if (enumeration.hasMoreElements()) {
            enumeration.nextElement();
            if (enumeration.hasMoreElements()) {
                return true;
            }
        }
        FileLock lock;
        try {
            lock = waitForLock();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return !isModified();
        }
        try {
            return !isModified();
        } finally {
            lock.releaseLock();
        }
    }

    public FileLock waitForLock() throws IOException {
        return waitForLock(10000);
    }

    public FileLock waitForLock(long timeout) throws IOException {
        long t = new Date().getTime() + timeout;
        for (;;) {
            try {
                return dataCache.lock();
            } catch (IOException e) {
                if (new Date().getTime() > t) {
                    throw new IOException("Cannot take data lock for more than " + timeout + " ms");
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                }
            }
        }
    }

    public org.netbeans.core.api.multiview.MultiViewPerspective getSelectedPerspective() {
        return getEditorSupport().getSelectedPerspective();
    }

    /** Enable to focus specific object in Multiview Editor
     *  The default implementation opens the XML View.
     */
    public void showElement(Object element) {
        getEditorSupport().edit();
    }

    /** Enable to get active MultiViewElement object
     */
    protected AbstractMultiViewElement getActiveMultiViewElement() {
        return activeMVElement;
    }
    void setActiveMultiViewElement(AbstractMultiViewElement element) {
        activeMVElement = element;
    }
    /** Opens the specific view
     * @param index multi-view index
     */
    public void openView(int index) {
        getEditorSupport().openView(index);
    }

    protected abstract String getPrefixMark();

    public DataCache getDataCache() {
        return dataCache;
    }

    public class DataCache {

        private long fileTime = 0;

        public void loadData() {
            FileObject file = getPrimaryFile();
            if (fileTime == file.lastModified().getTime()) {
                return;
            }
            try {
                FileLock dataLock = lock();
                loadData(file, dataLock);
            } catch (IOException e) {
                if (buffer == null) {
                    buffer = new byte[0];
                }
            }
        }

        public void loadData(FileObject file, FileLock dataLock) throws IOException {
            try {
                InputStream inputStream = getEditorSupport().getXmlEnv().getFileInputStream();
                byte[] buffer;
                try {
                    fileTime = file.lastModified().getTime();
                    int size = (int) file.getSize();
                    buffer = new byte[size];
                    inputStream.read(buffer);
                } finally {
                    inputStream.close();
                }
                setData(dataLock, buffer, true);
            } finally {
                dataLock.releaseLock();
            }
        }

        public synchronized void saveData(FileLock dataLock) {
            try {
                getEditorSupport().saveDocument(dataLock);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }

        public FileLock lock() throws IOException {
            if (getLock() != null) {
                throw new FileAlreadyLockedException();
            }
            FileLock l = new FileLock();
            lockReference = new WeakReference(l);
            return l;
        }

        private FileLock getLock() {
            FileLock l = lockReference == null ? null : (FileLock) lockReference.get();
            if (l != null && !l.isValid()) {
                l = null;
            }
            return l;
        }

        public byte[] getData() {
            if (buffer == null) {
                loadData();
            }
            return buffer;
        }

        public void setData(FileLock lock, byte[] data, boolean modify) throws IOException {
            testLock(lock);
            boolean modified = isModified() || modify;
            long oldTimeStamp = timeStamp;
            if (setData(data)) {
                if (!modified) {
                    saveData(lock);
                    firePropertyChange(PROPERTY_DATA_UPDATED, new Long(oldTimeStamp), new Long(timeStamp));
                } else {
                    firePropertyChange(PROPERTY_DATA_MODIFIED, new Long(oldTimeStamp), new Long(timeStamp));
                }
            }
        }


        private boolean setData(byte[] data) {
            if (Arrays.equals(buffer, data)) {
                return false;
            }
            buffer = data;
            long newTimeStamp = new Date().getTime();
            if (newTimeStamp <= timeStamp) {
                newTimeStamp = timeStamp + 1;
            }
            timeStamp = newTimeStamp;
            fileTime = 0;
            return true;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public InputStream createInputStream() {
            return new ByteArrayInputStream(getData());
        }

        public OutputStream createOutputStream() throws IOException {
            final FileLock dataLock = lock();
            return new ByteArrayOutputStream() {
                public void close() throws IOException {
                    try {
                        super.close();
                        setData(dataLock, toByteArray(), true);
                    } finally {
                        dataLock.releaseLock();
                    }
                }
            };
        }

        public OutputStream createOutputStream(final FileLock dataLock, final boolean modify) throws IOException {
            testLock(dataLock);
            return new ByteArrayOutputStream() {
                public void close() throws IOException {
                    super.close();
                    setData(dataLock, toByteArray(), modify);
                    if (!modify) {
                        dataCache.saveData(dataLock);
                    }
                }
            };
        }

        public void testLock(FileLock lock) throws IOException {
            if (lock == null || lock != getLock()) {
                throw new IOException();
            }
        }

        public void resetFileTime() {
            fileTime = getPrimaryFile().lastModified().getTime();
        }
    }
}
