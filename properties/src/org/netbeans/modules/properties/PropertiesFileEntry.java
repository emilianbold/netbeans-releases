/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.properties;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.text.MessageFormat;
import java.util.TreeSet;
import java.util.ResourceBundle;
import java.util.Iterator;
import java.util.ArrayList;

import org.openide.cookies.CompilerCookie;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;


/** This entry defines utility methods for finding out locale-specific data about entries. */
public class PropertiesFileEntry extends PresentableFileEntry {

    /** Basic name of bundle .properties file. */
    private String basicName;
    
    /** Structure handler for .properties file represented by this instance. */
    private transient StructHandler propStruct;

    /** Helper variable. Flag if cookies were initialized. */
    private transient boolean cookiesInitialized = false;
    
    /** Generated serial version UID. */    
    static final long serialVersionUID =-3882240297814143015L;
    
    
    /** Creates new PropertiesFileEntry */
    PropertiesFileEntry(MultiDataObject obj, FileObject file) {
        super(obj, file);
        FileObject fo = getDataObject().getPrimaryFile();
        if (fo == null)
            // primary file not init'ed yet => I'm the primary entry
            basicName = getFile().getName();
        else
            basicName = fo.getName();
    }

    
    /** Overrides superclass method. */
    public CookieSet getCookieSet() {
        synchronized(this) {
            if (!cookiesInitialized) {
                initCookieSet();
            }
        }
        
        return super.getCookieSet();
    }
    
    /** 
     * Overrides superclass method.
     * Look for a cookie in the current cookie set matching the requested class.
     *
     * @param type the class to look for
     * @return an instance of that class, or <code>null</code> if this class of cookie
     *    is not supported
     */
    public Node.Cookie getCookie(Class clazz) {
        if(CompilerCookie.class.isAssignableFrom(clazz)) {
            return null;
        }
        
        synchronized(this) {
            if(!cookiesInitialized) {
                initCookieSet();
            }
        }
        
        return super.getCookie(clazz);
    }

    /** Helper method. Actually lazilly creating cookie when first asked.*/
    private synchronized void initCookieSet() {
        // Necessary to set flag before add cookieSet method, cause
        // it fires property event change and some Cookie action in its
        // enable method could call initCookieSet again. 
        cookiesInitialized = true;
        
        super.getCookieSet().add(new PropertiesEditorSupport(this));
    }
    
    /** Creates a node delegate for this entry. */
    protected Node createNodeDelegate() {
        return new PropertiesLocaleNode(this);
    }

    /** Constructs children for this file entry. */
    public Children getChildren() {
        return new PropKeysChildren();
    }

    /** Returns a properties structure corresponding to this entry. Constructs itif necessary. */
    public StructHandler getHandler() {
        if (propStruct == null)
            propStruct = new StructHandler(this);
        return propStruct;
    }

    /** Deserialization */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    /** Returns editor support for properties */
    protected PropertiesEditorSupport getPropertiesEditor() {
        return (PropertiesEditorSupport)getCookieSet().getCookie(EditCookie.class);
    }

    /* Renames underlying fileobject. This implementation returns the
    * same file.
    *
    * @param name new base name of the bundle
    * @return file object with renamed file
    */
    public FileObject rename (String name) throws IOException {
    
        if (!getFile().getName().startsWith(basicName))
            throw new InternalError("Never happens - error in Properties loader / rename"); // NOI18N

        FileObject fo = super.rename(name + getFile().getName().substring(basicName.length()));
        basicName = name;
        return fo;
    }

    /* Renames underlying fileobject. This implementation returns the
    * same file.
    *
    * @param name full name of the file
    * @return file object with renamed file
    */
    public FileObject renameEntry (String name) throws IOException {

        if (!getFile().getName().startsWith(basicName))
            throw new InternalError("Never happens - error in Properties loader / rename"); // NOI18N

        if (basicName.equals(getFile().getName())) {
            // primary entry - can not rename
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                NbBundle.getBundle(PropertiesDataLoader.class).getString("MSG_AttemptToRenamePrimaryFile"),
                NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(msg);
            return getFile();
        }

        FileObject fo = super.rename(name);

        // to notify the bundle structure that name of one file was changed
        ((PropertiesDataObject)getDataObject()).getBundleStructure().oneFileChanged(getHandler());
        
        return fo;
    }

    public FileObject createFromTemplate (FileObject folder, String name) throws IOException {
        ResourceBundle bundle = NbBundle.getBundle (PropertiesFileEntry.class);
        if (! getFile ().getName ().startsWith (basicName))
            throw new InternalError("Never happens - error in Properties createFromTemplate"); // NOI18N
        String suffix = getFile ().getName ().substring (basicName.length ());
        String nuename = name + suffix;
        String ext = getFile ().getExt ();
        FileObject existing = folder.getFileObject (nuename, ext);
        if (existing == null) {
            return super.createFromTemplate (folder, nuename);
        } else {
            Object leaveAloneOpt = bundle.getString ("OPT_leave_alone");
            Object concatOpt = bundle.getString ("OPT_concatenate");
            Object overwriteOpt = bundle.getString ("OPT_overwrite");
            String title = bundle.getString ("LBL_ask_how_to_template");
            String message = MessageFormat.format (bundle.getString ("MSG_ask_how_to_template"),
                                                   new Object[] { nuename });
            NotifyDescriptor desc = new NotifyDescriptor
                                    (message, title, NotifyDescriptor.DEFAULT_OPTION,
                                     NotifyDescriptor.QUESTION_MESSAGE,
                                     new Object[] { concatOpt, leaveAloneOpt, overwriteOpt },
                                     concatOpt); // [PENDING] default option does not seem to work--so make it 1st
            Object result = TopManager.getDefault ().notify (desc);
            if (leaveAloneOpt.equals (result) ||
                    NotifyDescriptor.CLOSED_OPTION.equals (result)) {
                return existing;
            } else if (concatOpt.equals (result)) {
                byte[] originalData;
                byte[] buf = new byte[4096];
                int count;
                FileLock lock = existing.lock ();
                try {
                    InputStream is = existing.getInputStream ();
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream ((int) existing.getSize ());
                        try {
                            while ((count = is.read (buf)) != -1) {
                                baos.write (buf, 0, count);
                            }
                        } finally {
                            originalData = baos.toByteArray ();
                            baos.close ();
                        }
                    } finally {
                        is.close ();
                    }
                    existing.delete (lock);
                } finally {
                    lock.releaseLock ();
                }
                FileObject nue = folder.createData (nuename, ext);
                lock = nue.lock ();
                try {
                    OutputStream os = nue.getOutputStream (lock);
                    try {
                        os.write (originalData);
                        InputStream is = getFile ().getInputStream ();
                        try {
                            while ((count = is.read (buf)) != -1) {
                                os.write (buf, 0, count);
                            }
                        } finally {
                            is.close ();
                        }
                    } finally {
                        os.close ();
                    }
                } finally {
                    lock.releaseLock ();
                }
                // Does not appear to have any effect:
                // ((PropertiesDataObject) getDataObject ()).getBundleStructure ().
                //   oneFileChanged (getHandler ());
                return nue;
            } else if (overwriteOpt.equals (result)) {
                FileLock lock = existing.lock ();
                try {
                    existing.delete (lock);
                } finally {
                    lock.releaseLock ();
                }
                return super.createFromTemplate (folder, nuename);
            } else {
                throw new IOException ("unrecognized result option: " + result); // NOI18N
            }
        }
    }

    /** Test whether the object may be deleted.
    * @return <code>true</code> if it may (primary file can't be deleted)
    */
    public boolean isDeleteAllowed() {
        // PENDING - better implementation : don't allow deleting Bunlde_en when Bundle_en_US exists
        return (!getFile ().isReadOnly ()) && (!basicName.equals(getFile().getName()));
    }

    /** Test whether the object may be copied.
    * @return <code>true</code> if it may
    */
    public boolean isCopyAllowed () {
        return true;
    }
    // [PENDING] copy should be overridden because e.g. copy and then paste
    // to the same folder creates a new locale named "1"! (I.e. "foo_1.properties")

    /* Getter for move action.
    * @return true if the object can be moved
    */
    public boolean isMoveAllowed() {
        return !getFile ().isReadOnly ();
    }

    /* Getter for rename action.
    * @return true if the object can be renamed
    */
    public boolean isRenameAllowed () {
        return !getFile ().isReadOnly ();
    }

    /* Help context for this object.
    * @return help context
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (PropertiesFileEntry.class);
    }

    
    /** Children of a node representing s single properties file.
    * Contains nodes representing individual properties. */
    private class PropKeysChildren extends Children.Keys {

        /** Listens to changes on the properties file entry */
        private PropertyChangeListener pcl = null;

        /** Listens to changes on the property bundle structure */
        private PropertyBundleListener pbl = null;

        
        /** Constructor. */
        PropKeysChildren() {
            super();
        }

        
        /** Sets all keys in the correct order */
        protected void mySetKeys() {
            // use TreeSet because its iterator iterates in ascending order
            TreeSet ts = new TreeSet(new KeyComparator());
            PropertiesStructure ps = getHandler().getStructure();
            if (ps != null) {
                for (Iterator it = ps.nonEmptyItems();it.hasNext();) {
                    Element.ItemElem el = (Element.ItemElem)it.next();
                    ts.add(el.getKey());
                }
            }
            setKeys(ts);
        }

        /** Called to notify that the children has been asked for children
        * after and that they should set its keys.
        */
        protected void addNotify () {
            mySetKeys();
            // listener
            pcl = new PropertyChangeListener () {
                      public void propertyChange(PropertyChangeEvent evt) {
                          mySetKeys();
                      }

                  }; // end of inner class

            PropertiesFileEntry.this.addPropertyChangeListener (new WeakListener.PropertyChange(pcl));
            PropertiesFileEntry.this.getHandler().addPropertyChangeListener (new WeakListener.PropertyChange(pcl));

            pbl = new PropertyBundleListener () {
                public void bundleChanged(PropertyBundleEvent evt) {
                    switch (evt.getChangeType()) {
                        case PropertyBundleEvent.CHANGE_STRUCT:
                        case PropertyBundleEvent.CHANGE_ALL:
                            mySetKeys();
                            break;
                        case PropertyBundleEvent.CHANGE_FILE:
                            if (evt.getEntryName().equals(getFile().getName()))
                                // if it's me
                                mySetKeys();
                            break;
                        case PropertyBundleEvent.CHANGE_ITEM:
                            if (evt.getEntryName().equals(getFile().getName())) {
                                // the node should fire the change (to its property sheet, for example
                                KeyNode kn = (KeyNode)findChild(evt.getItemName());
                                if (kn != null) {                                  
                                    PropertiesStructure ps = getHandler().getStructure();
                                    if (ps != null) {
                                        Element.ItemElem it = ps.getItem(evt.getItemName());
                                        kn.fireChange(new PropertyChangeEvent(kn, Element.ItemElem.PROP_ITEM_VALUE, null, it.getValue()));
                                        kn.fireChange(new PropertyChangeEvent(kn, Element.ItemElem.PROP_ITEM_COMMENT, null, it.getComment()));
                                    } else
                                        ;
                                } else
                                    ;
                                // if it's me
                                // in theory do nothing
                                //PropKeysChildren.this.refreshKey(evt.getItemName());
                            }
                            break;
                    }
                }
            }; // End of annonymous class.

            ((PropertiesDataObject)PropertiesFileEntry.this.getDataObject()).getBundleStructure().
            addPropertyBundleListener (new WeakListenerPropertyBundle(pbl));
        }

        /** Called to notify that the children has lost all of its references to
        * its nodes associated to keys and that the keys could be cleared without
        * affecting any nodes (because nobody listens to that nodes).
        */
        protected void removeNotify () {
            setKeys(new ArrayList());
        }

        /** Create nodes. */
        protected Node[] createNodes (Object key) {
            String itemKey = (String)key;
            return new Node[] { new KeyNode(getHandler().getStructure(), itemKey) };
        }

    } // End of inner class PropKeysChildren.


}
