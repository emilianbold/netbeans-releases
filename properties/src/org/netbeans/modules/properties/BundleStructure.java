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

import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.datatransfer.*;
import org.openide.actions.InstantiateAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.*;
import org.openide.loaders.*;
import org.openide.*;

/** General abstract structure for properties files, in its use similar to source hierarchy.
*   Interoperates with Document, PropertiesTableModel, Nodes and other display-specific models
*   Implementations of the model interfaces generally reference this structure.
*   <br>This structure provides support for sorting entries and fast mapping of integers to entries.
*
* @author Petr Jiricka
*/
public class BundleStructure extends PropertyChangeSupport {
    // PENDING
    // do lazy initialization of entries, don't parse all at the beginning

    /** generated Serialized Version UID */
    //  static final long serialVersionUID = -7882925922830244768L;

    /** Main dataobject */
    PropertiesDataObject obj;

    /** Array of PropertiesFileEntry */
    private Object entries[];

    /** List of keys. */
    private ArrayList keyList;
    
    /** Comapartor which sorts keylist. Default set is sort according keys 
     * in ascending order. */
    private KeyComparator comparator = new KeyComparator(0, true);

    protected PropertyBundleSupport support = new PropertyBundleSupport(this);

    /** Listens to changes on the underlying dataobject */
    private PropertyChangeListener pcl;

    static final long serialVersionUID =-7537975919604619884L;
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
                          support.fireBundleStructureChanged();
                      }
                  }

              };
        obj.addPropertyChangeListener(WeakListener.propertyChange(pcl, obj));

        //PENDING move the column corresponding to curNode to the beginning
    }

    /** Retrieves n-th entry from the list, indexed from 0 */
    public PropertiesFileEntry getNthEntry(int i) {
        if (entries != null)
            return (PropertiesFileEntry)entries[i];
        else throw new InternalError(getClass().getName() +" - Entries not initialized");
    }

    /** Retrieves the index of a file entry (primary or secondary) by the name of its file
    *  @return index for entry with the given filename or -1 if not found
    */
    public int getEntryIndexByFileName(String fileName) {
        if (entries != null) {
            for (int i = 0; i < getEntryCount(); i++) {
                if (((PropertiesFileEntry)entries[i]).getFile().getName().equals(fileName))
                    return i;
            }
            return -1;
        }
        else throw new InternalError(getClass().getName() +" - Entries not initialized");
    }

    /** Retrieves a file entry (primary or secondary) by the name of its file
    *  @return entry with the given filename or null if not found
    */
    public PropertiesFileEntry getEntryByFileName(String fileName) {
        int index = getEntryIndexByFileName(fileName);
        return ((index == -1) ? null : (PropertiesFileEntry)entries[index]);
    }

    /** Retrieves number of all entries */
    public int getEntryCount() {
        if (entries != null)
            return entries.length;
        else
            throw new InternalError(getClass().getName() +" - Entries not initialized");
    }

    public String[] getKeys() {
        if (keyList == null)
            throw new InternalError(getClass().getName() +" - KeyList not initialized");
        Object keyArray[] = keyList.toArray();
        String stringArray[] = new String[keyArray.length];
        System.arraycopy(keyArray, 0, stringArray, 0, keyArray.length);
        return stringArray;
    }

    /** Retrieves n-th key from the list, indexed from 0 */
    public String getNthKey(int keyIndex) {
        if (keyList == null)
            throw new InternalError(getClass().getName() +" - KeyList not initialized");
        if ((keyIndex >= keyList.size()) || (keyIndex < 0))
            return null;
        return (String)keyList.get(keyIndex);
    }

    /** Retrieves index for a key from the list, by name */
    public int getKeyIndexByName(String keyName) {
        return keyList.indexOf(keyName);
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
    
    /** Resorts the keylist according the values of entry which index is given to this method.
     * @param index sorts accordinng nth-1 entry values, 0 means sort by keys,
     * if less than 0 it re-compares keylist with the same un-changed comparator.
     */
    public void sort(int index) {
        if(index >= 0)
            comparator.setIndex(index);
        Collections.sort(keyList, comparator);
        support.fireBundleDataChanged();
    }

    /** Gets index accoring which is bundle key list sorted.
     * @return index, 0 means accrding keys */
    public int getSortIndex() {
        return comparator.getIndex();
    }
    
    /** Gets current order of sort. 
     @return true if ascending, alse descending order */
    public boolean getSortOrder() {
        return comparator.isAscending();
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
    protected synchronized void buildKeySet() {
        keyList = new ArrayList() {
            public boolean equals(Object obj) {
                if(!(obj instanceof ArrayList))
                    return false;
                ArrayList list2 = (ArrayList)obj;
                
                if(this.size() != list2.size())
                    return false;
                for(int i=0; i<this.size(); i++) {
                    if(!this.contains(list2.get(i)) || !list2.contains(this.get(i)))
                        return false;
                }
                return true;
            }
        };

        // for all entries add all keys
        for (int index = 0; index < getEntryCount(); index++) {
            PropertiesFileEntry entry = getNthEntry(index);
            PropertiesStructure ps = entry.getHandler().getStructure();
            if (ps != null) {
                for (Iterator it = ps.nonEmptyItems(); it.hasNext(); ) {
                    String key = ((Element.ItemElem)it.next()).getKey();  
                    if(!(keyList.contains(key)))
                        keyList.add(key);
                }
            }
        }
        
        Collections.sort(keyList, comparator);
    }

    // event methods

    /** Add a listener to the list that's notified each time a change
     * to the property bundle occurs.
     * @param	l		the PropertyBundleListener
     */
    public void addPropertyBundleListener(PropertyBundleListener l) {
        support.addPropertyBundleListener(l);
    }

    /** Remove a listener from the list that's notified each time a
     * change to the property bundle occurs.
     * @param	l		the PropertyBundleListener
     */
    public void removePropertyBundleListener(PropertyBundleListener l) {
        support.removePropertyBundleListener(l);
    }



    // notification methods from lower layers of the structure
    /** One item in a properties file has changed.
    * Fires a change event for this item.
    */
    void itemChanged(Element.ItemElem item) {
        support.fireItemChanged(item.getParent()/*PropertiesStructure*/
                                .getParent()/*StructHandler*/
                                .getEntry()/*PropertiesFileEntry*/
                                .getFile().getName(),
                                item.getKey());
    }

    /** One file in the bundle has changed - no further information.
    * Fires changes for a bundle or a file according to the changes in the keys.
    */
    void oneFileChanged(StructHandler handler) {
        // PENDING - events should be finer
        // find out whether global key table has changed and fire a change according to that
        ArrayList oldKeyList = keyList;         
        
        buildKeySet();
        if (!keyList.equals(oldKeyList)) {
            support.fireBundleDataChanged();
        }
        else {
            support.fireFileChanged(handler.getEntry().getFile().getName());
        }
    }

    /** One file in the bundle has changed, carries information about what particular items have changed.
    * Fires changes for a bundle or a file according to the changes in the keys.
    */
    void oneFileChanged(StructHandler handler, ArrayMapList itemsChanged,
                        ArrayMapList itemsAdded, ArrayMapList itemsDeleted) {
        // PENDING - events should be finer
        // find out whether global key table has changed
        // should use a faster algorithm of building the keyset
        buildKeySet();
        support.fireBundleDataChanged();
    }

    /** Inner class. Comparator which compares keys according which locale (column on table was selected) */
    private final class KeyComparator implements Comparator {

        /** Index of column to compare with. */
        private int index;
        
        /** Flag if ascending order should be performed. */
        private boolean ascending;
        
        public KeyComparator(int index, boolean ascending) {
            this.index = index;
            this.ascending = ascending;
        }
        
        public void setIndex(int index) {
            // if same column toggle order
            if(this.index == index)
                ascending = !ascending;
            else
                ascending = true;
            this.index = index;
        }
        
        public int getIndex() {
            return index;
        }
        
        public boolean isAscending() {
            return ascending;
        }
        
        public int compare(Object o1, Object o2) {
            String str1;
            String str2;
            
            // key column
            if (index==0) {
                str1 = (String)o1;
                str2 = (String)o2;
            } else {
                Element.ItemElem item1 = getItem(index-1, getKeyIndexByName((String)o1));
                Element.ItemElem item2 = getItem(index-1, getKeyIndexByName((String)o2));
                if(item1 == null) {
                    if(item2 == null)
                        return 0;
                    else
                        return ascending ? 1 : -1;
                } else
                    if(item2 == null)
                        return ascending ? -1 : 1;
                
                str1 = item1.getValue();
                str2 = item2.getValue();

                if(str1 == null) {
                    if(str2 == null)
                        return 0;
                    else
                        return ascending ? 1 : -1;
                } else
                    if(str2 == null)
                        return ascending ? -1 : 1;
            }
            
            int res = str1.compareToIgnoreCase(str2);

            return ascending ? res : -res;
        }
        
    } // end of inner KeyComparator
    
}