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

import java.io.*;
import java.util.Iterator;

import com.netbeans.ide.text.PositionBounds;

/** General abstract structure for properties files, in its use similar to source hierarchy.
*   Interoperates with Document, PropertiesTableModel, Nodes and other display-specific models
*   Implementations of the model interfaces generally reference this structure.
*
* @author Petr Jiricka
*/
public class PropertiesStructure extends Element {

  /** Holds individual items - Element.ItemElem */
  private ArrayMapList items;

  /** If active, contains link to its handler (parent) */
  private StructHandler handler;
              
  /** Constructs a new PropertiesStructure for the given bounds and items. */
  public PropertiesStructure(PositionBounds bounds, ArrayMapList items) {
    super(bounds);                                                    
    // set this structure as a parent for all elements
    for (Iterator it = items.iterator(); it.hasNext(); )
      ((Element.ItemElem)it.next()).setParent(this);
    this.items = items;
  } 
  
  /** Updates the current structure by the new structure obtained by reparsing the document.
  * Looks for changes between the structures and according to them calls update methods.
  */
  public synchronized void update(PropertiesStructure struct) {       
    boolean structChanged = false;
    Element.ItemElem curItem;
    Element.ItemElem oldItem;
    
    ArrayMapList new_items = struct.items;
    ArrayMapList changed = new ArrayMapList();

    for (Iterator it = new_items.iterator(); it.hasNext(); ) {
      curItem = (Element.ItemElem)it.next();
      curItem.setParent(this);
      oldItem = getItem(curItem.getKey());
      if (oldItem == null)
        structChanged = true;
      else {
        if (!curItem.equals(oldItem))        
          changed.add(curItem.getKey(), curItem);
        items.remove(oldItem.getKey());
      }                                
    }
    
    if (items.size() > 0)
      structChanged = true;

System.out.println("struct : " + structChanged);
System.out.println("items : " + changed.size());

    // assign the new structure
    items = new_items;        
    
    // notification
    if (structChanged)
      structureChanged();
    else {
      // notify about changes in all items
      for (Iterator it = changed.iterator(); it.hasNext(); )
        itemChanged((Element.ItemElem)it.next());
    }
  }
                                                         
  /** Sets the parent of this element. */
  void setParent(StructHandler parent) {
    if (handler != null)
      System.out.println("Parent already set - " + getClass().getName());
    handler = parent;
  }
  
  private StructHandler getParent() {
    if (handler == null)
      throw new InternalError();
    return handler;
  }
  
  private BundleStructure getParentBundleStructure() {
    return ((PropertiesDataObject)getParent().getEntry().getDataObject()).getBundleStructure();
  }
                                                         
  /** Get a string representation of the element for printing.
  * @return the string
  */
  public String printString() {
    StringBuffer sb = new StringBuffer();
    Element.ItemElem item;
    for (Iterator it = items.iterator(); it.hasNext(); ) {
      item = (Element.ItemElem)it.next();
      sb.append(item.printString());
    }                                    
    return sb.toString();
  }


  /** Get a value string of the element.
  * @return the string
  */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    Element.ItemElem item;
    for (Iterator it = items.iterator(); it.hasNext(); ) {
      item = (Element.ItemElem)it.next();
      sb.append(item.toString());
      sb.append("- - -\n");
    }                                    
    return sb.toString();
  }
  
  /** Adds an item to the end, should be only used by the parser (no notification) 
  *  Used by the update actions.
  */
  public void parserAddItem(Element.ItemElem item) {
    item.setParent(this);
    items.add(item.getKey(), item);
  }
                                 
  /** Retrieves an item by key (property name) or null if does not exist. */
  public Element.ItemElem getItem(String key) {
    return (Element.ItemElem)items.get(key);
  }                                         
  
  /** Returns an iterator iterating through items which have non-empty key */
  public Iterator nonEmptyItems() {
    return new Iterator() {
      // iterator which relies on the list's iterator
      private Iterator innerIt;       
      /** Next non-empty element in the underlying iterator */
      private Element.ItemElem nextElem;
      
      {
        innerIt = items.iterator();                       
        fetchNext();
      }       
                             
      /** Fetches internally the next non-empty element */
      private void fetchNext() {
        do {
          if (innerIt.hasNext())           
            nextElem = (Element.ItemElem)innerIt.next();
          else
            nextElem = null;
        }
        while (nextElem != null && nextElem.getKey().length() == 0);
      }                                                         
      
      public boolean hasNext() {
        return nextElem != null;
      }     
                          
      public Object next() {                                        
        Object ne = nextElem;
        fetchNext();
        return ne;
      }
      
      public void remove() {
        throw new UnsupportedOperationException();
      }
      
    };
  }
                     
  /** Returns iterator thropugh all items, including empty ones */                   
  public Iterator allItems() {                                    
    return items.iterator();
  }

  /** Notification that the given item has changed (its value or comment) */
  void itemChanged(Element.ItemElem elem) {
System.out.println("PropStr - item " + elem.getKey());
    getParentBundleStructure().itemChanged(elem);
  }

  /** Notification that the structure has changed (items have been added or deleted,
  * also includes changing an item's key). */
  void structureChanged() {
System.out.println("PropStr - struct");
    getParentBundleStructure().oneFileChanged(getParent());
  }                        
  
  /** Notification that the an item's key has changed. Subcase of structureChanged(). */
  void itemKeyChanged(String oldKey, Element.ItemElem newElem) {
    // update the element in the structure, because now it is in with the wrong key
    int index = items.indexOf(oldKey);
    if (index < 0)
      throw new InternalError();
    items.set(index, newElem.getKey(), newElem);  
    structureChanged();
  }
}

/*
 * <<Log>>
 */
