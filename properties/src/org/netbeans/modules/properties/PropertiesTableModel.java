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
import java.io.*;
import java.lang.reflect.InvocationTargetException;
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

/** Model for the properties edit table
*
* @author Petr Jiricka
*/
public class PropertiesTableModel extends AbstractTableModel {

  /** generated Serialized Version UID */
//  static final long serialVersionUID = -7882925922830244768L;
                    
  /** Main dataobject */                  
  PropertiesDataObject obj;
                     
  /** Listens to changes on the underlying dataobject */
  private PropertyChangeListener pcl;
  /** Listens to changes on the bundle structure */
  private PropertyBundleListener pbl;
  
  /** Create a data node for a given data object.
  * The provided children object will be used to hold all child nodes.
  * @param obj object to work with
  * @param ch children container for the node
  */
  public PropertiesTableModel (PropertiesDataObject obj) {
    super ();                 
    this.obj = obj;                 
    
    // listener for the DataObject
    pcl = new PropertyChangeListener () {                
    
      public void propertyChange(PropertyChangeEvent evt) { 
        // PENDING - should be finer
        if (evt.getPropertyName().equals(PropertiesDataObject.PROP_FILES)) {
          System.out.println("PropertyChange in PropertiesTableModel");
          fireTableStructureChanged();                                        
        }
      }
      
    };
    obj.getBundleStructure().addPropertyChangeListener(new WeakListener.PropertyChange(pcl));
    
    // listener for the BundleStructure
    pbl = new PropertyBundleListener () {                
    
      public void bundleChanged(PropertyBundleEvent evt) {
        // PENDING - should be finer
        System.out.println("BundleChange in PropertiesTableModel");
        fireTableStructureChanged();                                        
      }
      
    };
    obj.getBundleStructure().addPropertyBundleListener(new WeakListenerPropertyBundle(pbl));

    //PENDING move the column corresponding to curNode to the beginning
  }
                                                             
                                                             
  /** Returns the number of rows in the model */ 
  public int getRowCount() {
    return obj.getBundleStructure().getKeyCount();
  }  
                                                
  /** Returns the number of columns in the model */ 
  public int getColumnCount() {
    return obj.getBundleStructure().getEntryCount() + 1;
  }
  
  /** Returns the value for the given row and column */ 
  public Object getValueAt(int row, int column) {
    BundleStructure bs = obj.getBundleStructure();
    switch (column) {
      case 0:
        return bs.getNthKey(row);
      default:
        Element.ItemElem item = bs.getItem(column - 1, row);
        if (item == null)
          return "";
        else  
          return item.getValue();
    }         
  }                    
  
  /** Returns the name for a column */ 
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return NbBundle.getBundle(PropertiesTableModel.class).getString("LAB_KeyColumnLabel");
      default:
        return Util.getPropertiesLabel (obj.getBundleStructure().getNthEntry(column - 1));
    }         
  }

}

/*
 * <<Log>>
 */
