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

import java.util.EventObject;

/** Event type for property bundles.
*
* @author Petr Jiricka
*/
public class PropertyBundleEvent extends EventObject {

  /** Signifies that potentially all the structure has changed. */                                     
  public static final int CHANGE_STRUCT = 1;

  /** Signifies that potentially all the data has changed. */                                     
  public static final int CHANGE_ALL = 2;

  /** Signifies that one file has changed. */
  public static final int CHANGE_FILE = 3;
                                       
  /** Signifies that one item has changed. */
  public static final int CHANGE_ITEM = 4;
                                       
  /** Name of the entry in which the change occurred. */
  protected String entryName;
  
  /** Key of the item in which the change occurred. */
  protected String itemName;
                           
  /** Type of the change that occurred. */                         
  protected int changeType;
  
  static final long serialVersionUID =1702449038200791321L;
  /** Everything has changed, specify the change type */
  public PropertyBundleEvent(Object source, int changeType) {
    super(source);
    this.changeType = changeType;
  }                         
                      
  /** One entry has changed and its node+children / table column needs redrawing */
  public PropertyBundleEvent(Object source, String entryName) {
    super(source);          
    this.entryName = entryName;
    changeType = CHANGE_FILE;
  }                         
                      
  /** One item has changed and its node / table cell needs redrawing */
  public PropertyBundleEvent(Object source, String entryName, String itemName) {
    super(source);          
    this.entryName = entryName;
    this.itemName  = itemName;
    changeType = CHANGE_ITEM;
  }                         
                      
  /** Returns the type of change that occurred. */                    
  public int getChangeType() {
    return changeType;
  }
  
  /** Returns the name of entry in which the change occurred. */                    
  public String getEntryName() {
    return entryName;
  }
  
  /** Returns the key for the item in which the change occurred. */                    
  public String getItemName() {
    return itemName;
  }
  
  public String toString() {
    try {
      String doIdent = (getSource() instanceof BundleStructure) ? 
        ((BundleStructure)getSource()).obj.getPrimaryFile().getName() : "";
      String ct = "?";
      switch (getChangeType()) {
        case CHANGE_STRUCT : ct = "STRUCT"; break;
        case CHANGE_ALL    : ct = "ALL"; break;
        case CHANGE_FILE   : ct = "FILE"; break;
        case CHANGE_ITEM   : ct = "ITEM"; break;
      }
    
      return "PropertyBundleEvent: bundle " + doIdent + ", changeType " + ct + 
        ", entry " + getEntryName() + ", item " + getItemName();
    }
    catch (Exception e) {               
      return "some PropertyBundleEvent (" + e.toString() + ") occurred";
    }    
  }
  
}

/*
 * <<Log>>
 */
