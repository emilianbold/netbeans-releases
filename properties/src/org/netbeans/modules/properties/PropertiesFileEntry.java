
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


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.TreeSet;

import org.openide.cookies.CompilerCookie;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;


/** This entry represents one properties file which is part of bunlde of properties files with same basic name.
 * This entry has some dataobject attributes, has cookies, node delegate etc. */
public class PropertiesFileEntry extends PresentableFileEntry implements CookieSet.Factory {

    /** Basic name of bundle .properties file. */
    private String basicName;
    
    /** Structure handler for .properties file represented by this instance. */
    private transient StructHandler propStruct;
    
    /** Editor support for this entry. */
    private transient PropertiesEditorSupport editorSupport;

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
        
        getCookieSet().add(PropertiesEditorSupport.class, this);
    }

    
    /** Copies entry to folder. Overrides superclass method. 
     * @param folder folder where copy
     * @param suffix suffix to use
     * @exception IOException when error happens */
    public FileObject copy(FileObject folder, String suffix) throws IOException {
        String pasteSuffix = ((PropertiesDataObject)getDataObject()).getPasteSuffix();
        
        if(pasteSuffix == null)
            return super.copy(folder, suffix);
        
        FileObject fileObject = getFile();
        
        String basicName = getDataObject().getPrimaryFile().getName();
        String newName = basicName + pasteSuffix + Util.getLocalePartOfFileName(this);
        
        return fileObject.copy(folder, newName, fileObject.getExt());
    }
   
    /** Moves entry to folder. Overrides superclass method. 
     * @param folder folder where copy
     * @param suffix suffix to use 
     * @exception IOException when error happens */
    public FileObject move(FileObject folder, String suffix) throws IOException {
        String pasteSuffix = ((PropertiesDataObject)getDataObject()).getPasteSuffix();
        
        if(pasteSuffix == null)
            return super.move(folder, suffix);

        
        FileObject fileObject = getFile();
        FileLock lock = takeLock ();
        
        try {
            String basicName = getDataObject().getPrimaryFile().getName();
            String newName = basicName + pasteSuffix + Util.getLocalePartOfFileName(this);
            
            return fileObject.move (lock, folder, newName, fileObject.getExt());
        } finally {
            lock.releaseLock ();
        }
    }
    
    /** Implements <code>CookieSet.Factory</code> interface method. */
    public Node.Cookie createCookie(Class clazz) {
        if(clazz.isAssignableFrom(PropertiesEditorSupport.class)) {
            return getPropertiesEditor();
        } else
            return null;
    }
    
    /** Creates a node delegate for this entry. Implements superclass abstract method. */
    protected Node createNodeDelegate() {
        return new PropertiesLocaleNode(this);
    }

    /** Gets children for this file entry. */
    public Children getChildren() {
        return new PropKeysChildren();
    }

    /** Gets struct handler for this entry. 
     * @return <StructHanlder</code> for this entry */
    public StructHandler getHandler() {
        if (propStruct == null)
            propStruct = new StructHandler(this);
        return propStruct;
    }

    /** Deserialization. */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    /** Gets editor support for this entry.
     * @return <code>PropertiesEditorSupport</code> instance for this entry */
    protected PropertiesEditorSupport getPropertiesEditor() {
        // Hack to ensure open support is created.
        // PENDING has to be made finer.
        getDataObject().getCookie(PropertiesOpen.class);
        
        if(editorSupport == null) {
            synchronized(this) {
                if(editorSupport == null)
                    editorSupport = new PropertiesEditorSupport(this);
            }
        }
            
        return editorSupport;
    }

    /** Renames underlying fileobject. This implementation returns the same file.
     * Overrides superclass method.
     *
     * @param name new base name of the bundle
     * @return file object with renamed file
     */
    public FileObject rename (String name) throws IOException {
    
        if (!getFile().getName().startsWith(basicName))
            throw new IllegalStateException("Resource Bundles: error in Properties loader/rename."); // NOI18N

        FileObject fo = super.rename(name + getFile().getName().substring(basicName.length()));
        basicName = name;
        return fo;
    }

    /** Renames underlying fileobject. This implementation returns the same file.
     * Overrides superclass method.
     * 
     * @param name full name of the file represented by this entry
     * @return file object with renamed file
     */
    public FileObject renameEntry (String name) throws IOException {

        if (!getFile().getName().startsWith(basicName))
            throw new IllegalStateException("Resource Bundles: error in Properties loader / rename"); // NOI18N

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

    /** Creates from template. Overrides superclass method. */
    public FileObject createFromTemplate (FileObject folder, String name) throws IOException {
        ResourceBundle bundle = NbBundle.getBundle (PropertiesFileEntry.class);
        if (!getFile().getName().startsWith(basicName))
            throw new IllegalStateException("Resource Bundles: error in Properties createFromTemplate"); // NOI18N
        
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

    /** Whether the object may be deleted. Implemenst superclass abstract method.
     * @return <code>true</code> if it may (primary file can't be deleted)
     */
    public boolean isDeleteAllowed() {
        // PENDING - better implementation : don't allow deleting Bunlde_en when Bundle_en_US exists
        return (!getFile ().isReadOnly ()) && (!basicName.equals(getFile().getName()));
    }

    /** Whether the object may be copied. Implements superclass abstract method.
     * @return <code>true</code> if it may
     */
    public boolean isCopyAllowed () {
        return true;
    }
    // [PENDING] copy should be overridden because e.g. copy and then paste
    // to the same folder creates a new locale named "1"! (I.e. "foo_1.properties")

    /** Getter for move action. Implements superclass abstract method.
     * @return true if the object can be moved
     */
    public boolean isMoveAllowed() {
        return !getFile ().isReadOnly ();
    }

    /** Getter for rename action. Implements superclass abstract method.
     * @return true if the object can be renamed
     */
    public boolean isRenameAllowed () {
        return !getFile ().isReadOnly ();
    }

    /** Help context for this object. Implements superclass abstract method.
     * @return help context
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(Util.HELP_ID_CREATING);
    }

    
    /** Children of a node representing single properties file.
     * Contains nodes representing individual properties (key-value pairs with comments). */
    private class PropKeysChildren extends Children.Keys {

        /** Listens to changes on the property bundle structure. */
        private PropertyBundleListener bundleListener = null;

        
        /** Constructor. */
        PropKeysChildren() {
            super();
        }

        
        /** Sets all keys in the correct order. Calls <code>setKeys</code>. Helper method. 
         * @see org.openide.nodes.Children.Keys#setKeys(java.util.Collection) */
        private void mySetKeys() {
            // Use TreeSet because its iterator iterates in ascending order.
            TreeSet keys = new TreeSet(new KeyComparator());
            PropertiesStructure propStructure = getHandler().getStructure();
            if(propStructure != null) {
                for(Iterator iterator = propStructure.allItems(); iterator.hasNext(); ) {
                    Element.ItemElem item = (Element.ItemElem)iterator.next();
                    if(item != null && item.getKey() != null)
                        keys.add(item.getKey());
                }
            }
            
            setKeys(keys);
        }

        /** Called to notify that the children has been asked for children
         * after and that they should set its keys. Overrides superclass method.
         */
        protected void addNotify () {
            mySetKeys();

            bundleListener = new PropertyBundleListener () {
                public void bundleChanged(PropertyBundleEvent evt) {
                    int changeType = evt.getChangeType();
                    
                    if(changeType == PropertyBundleEvent.CHANGE_STRUCT 
                        || changeType == PropertyBundleEvent.CHANGE_ALL) {
                        mySetKeys();
                    } else if(changeType == PropertyBundleEvent.CHANGE_FILE 
                        && evt.getEntryName().equals(getFile().getName())) {
                            
                        // File underlying this entry changed.
                        mySetKeys();
                    } else if(changeType == PropertyBundleEvent.CHANGE_ITEM 
                        && evt.getItemName() != null
                        && evt.getEntryName().equals(getFile().getName()))
                        
                        refreshKey(evt.getItemName());
                }
            }; // End of annonymous class.

            ((PropertiesDataObject)PropertiesFileEntry.this.getDataObject()).getBundleStructure().addPropertyBundleListener(new WeakListenerPropertyBundle(bundleListener));
        }

        /** Called to notify that the children has lost all of its references to
         * its nodes associated to keys and that the keys could be cleared without
         * affecting any nodes (because nobody listens to that nodes). Overrides superclass method.
         */
        protected void removeNotify () {
            setKeys(new ArrayList());
        }

        /** Create nodes. Implements superclass abstract method. */
        protected Node[] createNodes (Object key) {
            String itemKey = (String)key;
            return new Node[] { new KeyNode(getHandler().getStructure(), itemKey) };
        }

    } // End of inner class PropKeysChildren.

}
