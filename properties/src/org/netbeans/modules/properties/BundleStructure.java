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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.table.*;

import com.netbeans.ide.filesystems.FileStateInvalidException;
import com.netbeans.ide.filesystems.FileObject;
import com.netbeans.ide.filesystems.FileUtil;
import com.netbeans.ide.util.datatransfer.*;
import com.netbeans.ide.actions.InstantiateAction;
import com.netbeans.ide.util.HelpCtx;
import com.netbeans.ide.util.RequestProcessor;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.util.WeakListener;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.loaders.*;
import com.netbeans.ide.*;

/** General abstract structure for properties files, in its use similar to source hierarchy.
*   Interoperates with Document, PropertiesTableModel, Nodes and other display-specific models
*   Implementations of the model interfaces generally reference this structure.
*   <br>This structure provides support for sorting entries and fast mapping of integers to entries.
*
* @author Petr Jiricka
*/
public class BundleStructure extends PropertyChangeSupport {

  /** generated Serialized Version UID */
//  static final long serialVersionUID = -7882925922830244768L;
                    
  /** Main dataobject */                  
  PropertiesDataObject obj;
                             
  /** Array of PropertiesFileEntry */
  private Object entries[];
  
  private SortedArrayList keyList;
                     
  /** Listens to changes on the underlying dataobject */
  private PropertyChangeListener pcl;
  
  /** Create a data node for a given data object.
  * The provided children object will be used to hold all child nodes.
  * @param obj object to work with
  * @param ch children container for the node
  */
  public BundleStructure (PropertiesDataObject obj) {
    super (obj);                 
    this.obj = obj;                 
    updateEntries();
    
    // listener for the DataObject
    pcl = new PropertyChangeListener () {                
    
      public void propertyChange(PropertyChangeEvent evt) { 
        if (evt.getPropertyName().equals(PropertiesDataObject.PROP_FILES)) {
          updateEntries();
          // PENDING
          firePropertyChange (evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
          System.out.println("PropertyChange in BundleStructure"); 
        }
      }
      
    };
    obj.addPropertyChangeListener(new WeakListener.PropertyChange(pcl));
    
    //PENDING move the column corresponding to curNode to the beginning
  }
                        
  /** Retrieves n-th entry from the list, indexed from 0 */
  public PropertiesFileEntry getNthEntry(int i) {
    if (entries != null)
      return (PropertiesFileEntry)entries[i];
    else throw new InternalError(getClass().getName() +" - Entries not initialized");
  }
                                           
  /** Retrieves number of all entries */                      
  public int getEntryCount() {
    if (entries != null)
      return Array.getLength(entries);
    else 
      throw new InternalError(getClass().getName() +" - Entries not initialized");
  }       
  
  /** Retrieves n-th key from the list, indexed from 0 */
  public String getNthKey(int keyIndex) {
    if (keyList == null)
      throw new InternalError(getClass().getName() +" - KeyList not initialized");
    return (String)keyList.get(keyIndex);
  }
                                           
  /** Retrieves keyIndex-th key in the entryIndex-th entry from the list, indexed from 0 
  * @return item for keyIndex-th key in the entryIndex-th entry 
  *  or null if the entry does not contain the key
  */
  public Element.ItemElem getItem(int entryIndex, int keyIndex) {
    PropertiesFileEntry pfe = getNthEntry(entryIndex);
    String key = getNthKey(keyIndex);
    PropertiesStructure ps = pfe.getHandler().getStructure();
    if (ps != null)
      return ps.getItem(key);
    else
      return null;
  } 
                                                     
  /** Retrieves number of all keys */
  public int getKeyCount() {
    if (keyList != null)
      return keyList.size();
    else 
      throw new InternalError(getClass().getName() +" - KeyList not initialized");
  }                                                   

  /** Updates internal entries from the underlying dataobject */
  protected synchronized void updateEntries() {               
    TreeMap tm = new TreeMap(PropertiesDataObject.getSecondaryFilesComparator());
    PropertiesFileEntry pfe;
    for (Iterator it = obj.secondaryEntries().iterator(); it.hasNext(); ) {
      pfe = (PropertiesFileEntry)it.next();
      tm.put(pfe.getFile().getName(), pfe);
    }  
    
    // move the entries
    entries = new Object[tm.size() + 1];
    entries[0] = obj.getPrimaryEntry(); 
    int index = 0;
    for (Iterator it = tm.keySet().iterator(); it.hasNext(); ) 
      entries[++index] = tm.get(it.next());  
      
    buildKeySet();  
  }                                         
   
  /** Constructs a set of keys from the entries (from scratch) */ 
  protected void buildKeySet() {
    if (keyList == null)
      keyList = new SortedArrayList(String.CASE_INSENSITIVE_ORDER);
    else 
      keyList.clear();
      
    // for all entries add all keys  
    for (int index = 0; index < getEntryCount(); index++) {
      PropertiesFileEntry entry = getNthEntry(index);
      PropertiesStructure ps = entry.getHandler().getStructure();
      if (ps != null) {
        for (Iterator it = ps.nonEmptyItems(); it.hasNext(); )
          keyList.setAdd(((Element.ItemElem)it.next()).getKey());
      }
    }
  }
  
}

/*
 * <<Log>>
 */
