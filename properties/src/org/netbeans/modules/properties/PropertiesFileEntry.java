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

package com.netbeans.developer.modules.loaders.properties;
 
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
  transient protected PropertiesEditorSupport editorSupport;
  transient protected StructHandler propStruct;
           
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
    if (editorSupport == null) {
System.out.println("creating new editorsupport");  
      editorSupport = new PropertiesEditorSupport(this);
    }  
    return editorSupport;
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


