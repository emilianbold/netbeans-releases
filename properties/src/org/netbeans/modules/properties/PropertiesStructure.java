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
  
  ArrayMapList items;
              
  // PENDING
  // boolean dirty;            
              
  //PENDING javadoc       
  public PropertiesStructure(PositionBounds bounds, ArrayMapList items) {
    super(bounds);
    this.items = items;
  } 
  
  //PENDING javadoc       
  public void update(PropertiesStructure struct) {
    // PENDING
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
      sb.append("- - -");
    }                                    
    return sb.toString();
  }
  
  /** Adds an item to the end, should be only used by the parser (no notification) */
  public void parserAddItem(Element.ItemElem item) {
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
}

/*
 * <<Log>>
 */
