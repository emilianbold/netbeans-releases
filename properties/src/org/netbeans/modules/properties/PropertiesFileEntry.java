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

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.AbstractCollection;
import java.util.ResourceBundle;
import java.util.Collections;
import java.util.Iterator;
import java.util.Comparator;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.openide.loaders.*;
import org.openide.*;
import org.openide.cookies.EditCookie;
import org.openide.util.datatransfer.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.*;
import org.openide.util.enum.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.nodes.*;


/** This entry defines utility methods for finding out locale-specific data about entries.
*/
public class PropertiesFileEntry extends PresentableFileEntry {

    protected String basicName;
    transient protected StructHandler propStruct;

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
        init();
    }

    /** Initializes the object after creation and deserialization */
    private void init() {
        // edit as a viewcookie
        getCookieSet().add (new PropertiesEditorSupport(this));
    }

    /** Creates a node delegate for this entry. */
    protected Node createNodeDelegate() {
        return new PropertiesLocaleNode(this);
    }

    /** Constructs children for this file entry */
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
        init();
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
            throw new InternalError("Never happens - error in Properties loader / rename");

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
            throw new InternalError("Never happens - error in Properties loader / rename");

        if (basicName.equals(getFile().getName())) {
            // primary entry - can not rename
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                               NbBundle.getBundle(PropertiesDataLoader.class).
                                               getString("MSG_AttemptToRenamePrimaryFile"),
                                               NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(msg);
            return getFile();
        }

        FileObject fo = super.rename(name);
        return fo;
    }

    public FileObject createFromTemplate (FileObject folder, String name) throws IOException {
        ResourceBundle bundle = NbBundle.getBundle (PropertiesFileEntry.class);
        if (! getFile ().getName ().startsWith (basicName))
            throw new InternalError("Never happens - error in Properties createFromTemplate");
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
    class PropKeysChildren extends Children.Keys {

        /** Listens to changes on the properties file entry */
        private PropertyChangeListener pcl = null;

        /** Listens to changes on the property bundle structure */
        private PropertyBundleListener pbl = null;

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
                                      }
                                      else
                                          ;
                                  }
                                  else
                                      ;
                                  // if it's me
                                  // in theory do nothing
                                  //PropKeysChildren.this.refreshKey(evt.getItemName());
                              }
                              break;
                          }
                      }
                  }; // end of inner class

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

        protected Node[] createNodes (Object key) {
            String itemKey = (String)key;
            return new Node[] { new KeyNode(getHandler().getStructure(), itemKey) };
        }

    } // end of class PropKeysChildren


}



/*
 * <<Log>>
 *  18   Gandalf-post-FCS1.16.1.0    3/28/00  Jesse Glick     Properties files used as
 *       templates can merge into one another.
 *  17   Gandalf   1.16        11/27/99 Patrik Knakal   
 *  16   Gandalf   1.15        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   Gandalf   1.14        10/12/99 Petr Jiricka    
 *  14   Gandalf   1.13        9/13/99  Petr Jiricka    Removed debug println
 *  13   Gandalf   1.12        9/10/99  Petr Jiricka    Comparator change
 *  12   Gandalf   1.11        8/18/99  Petr Jiricka    Some fix
 *  11   Gandalf   1.10        8/17/99  Petr Jiricka    Changes erlated to 
 *       saving
 *  10   Gandalf   1.9         8/9/99   Petr Jiricka    Removed debug prints
 *  9    Gandalf   1.8         6/24/99  Petr Jiricka    
 *  8    Gandalf   1.7         6/11/99  Petr Jiricka    
 *  7    Gandalf   1.6         6/10/99  Petr Jiricka    
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         6/8/99   Petr Jiricka    
 *  4    Gandalf   1.3         6/6/99   Petr Jiricka    
 *  3    Gandalf   1.2         5/14/99  Petr Jiricka    
 *  2    Gandalf   1.1         5/13/99  Petr Jiricka    
 *  1    Gandalf   1.0         5/12/99  Petr Jiricka    
 * $
 */
