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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JScrollPane;
//import javax.swing.DefaultCellEditor;
 
import com.netbeans.ide.cookies.OpenCookie;
import com.netbeans.ide.loaders.MultiDataObject;
import com.netbeans.ide.loaders.FileEntry;
import com.netbeans.ide.loaders.OpenSupport;
import com.netbeans.ide.loaders.DataObject;
import com.netbeans.ide.windows.CloneableTopComponent;
import com.netbeans.ide.windows.TopComponent;
import com.netbeans.ide.explorer.propertysheet.PropertyDisplayer;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.NotifyDescriptor;
import com.netbeans.ide.TopManager;

 
/** Support for opening properties files (OpenCookie) in visual editor */
public class PropertiesOpen extends OpenSupport implements OpenCookie {
                  
  /** Main properties dataobject */                                 
  PropertiesDataObject obj;

  /** Constructor */
  public PropertiesOpen(PropertiesFileEntry fe) {
    super(fe);
    this.obj = (PropertiesDataObject)fe.getDataObject();
  }
   
  /** A method to create a new component. Must be overridden in subclasses.
  * @return the cloneable top component for this support
  */
  protected CloneableTopComponent createCloneableTopComponent () {
    PropertiesTableModel ptm = new PropertiesTableModel(obj);
    
    return new PropertiesCloneableTopComponent(obj, ptm/*, ptcm*/);
  }
  
  public static class PropertiesCloneableTopComponent extends CloneableTopComponent {                                
    DataObject obj;
    PropertiesTableModel ptm;
//    PropertiesTableColumnModel ptcm;

    /** Constructor
    * @param obj data object we belong to
    */
    public PropertiesCloneableTopComponent (final DataObject obj, PropertiesTableModel ptm/*, PropertiesTableColumnModel ptcm*/) {
      super (obj);
      this.obj  = obj;               
      this.ptm  = ptm;
//      this.ptcm = ptcm;

      setLayout (new BorderLayout ());
      
      JTable table = new JTable(ptm/*, ptcm*/);
      // PENDING      
//      PropertiesCellEditor ed = new PropertiesCellEditor(new PropertyDisplayer());
//      table.setDefaultEditor(PropertiesTableModel.CommentValuePair.class, ed);
//      table.setDefaultEditor(String.class, DefaultCellEditor.class);
      JScrollPane scrollPane = new JScrollPane(table);
      table.setPreferredScrollableViewportSize(new Dimension(500, 70));
      add (scrollPane, BorderLayout.CENTER);
      JButton addButton = new JButton(PropertiesSettings.getString("LBL_AddPropertyButton"));
      add (addButton, BorderLayout.SOUTH);
      addButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            NewPropertyDialog dia = new NewPropertyDialog();
            Dialog d = dia.getDialog();
            d.setVisible(true);
            if (dia.getOKPressed ()) {                            
              if (((PropertiesFileEntry)((MultiDataObject)obj).getPrimaryEntry()).
                    getHandler().getStructure().addItem(
                    dia.getKeyText(), dia.getValueText(), dia.getCommentText()))
                ;
              else {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                  java.text.MessageFormat.format(
                    NbBundle.getBundle(PropertiesLocaleNode.class).getString("MSG_KeyExists"),
                    new Object[] {dia.getKeyText()}),
                  NotifyDescriptor.ERROR_MESSAGE);
                TopManager.getDefault().notify(msg);
              }  
            }
          }
        }
      );  
    }

    /** Is called from the clone method to create new component from this one.
    * This implementation only clones the object by calling super.clone method.
    * @return the copy of this object
    */
    protected CloneableTopComponent createClonedObject () {
      return new PropertiesCloneableTopComponent (obj, ptm/*, ptcm*/);
    }
                          
    /** Mode where properties windows belong by defaut */
    public TopComponent.Mode getDefaultMode () {    
      return PropertiesModule.propertiesDefaultMode;
    }
    /** This method is called when parent window of this component has focus,
    * and this component is preferred one in it.
    * Override this method to perform special action on component activation.
    * (Typical thing to do here is set performers for your actions)
    * Remember to call superclass to
    */
    protected void componentActivated () {
    }

    /**
    * This method is called when parent window of this component losts focus,
    * or when this component losts preferrence in the parent window.
    * Override this method to perform special action on component deactivation.
    * (Typical thing to do here is unset performers for your actions)
    */
    protected void componentDeactivated () {
    }
  }



  
}
