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

import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JScrollPane;
//import javax.swing.DefaultCellEditor;
 
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.FileEntry;
import org.openide.loaders.OpenSupport;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.explorer.propertysheet.PropertyDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;

 
/** Support for opening properties files (OpenCookie) in visual editor */
public class PropertiesOpen extends OpenSupport implements OpenCookie {
                  
  /** Main properties dataobject */                                 
  PropertiesDataObject obj;
  PropertyChangeListener modifL;

  /** Constructor */
  public PropertiesOpen(PropertiesFileEntry fe) {
    super(fe);
    this.obj = (PropertiesDataObject)fe.getDataObject();
    this.obj.addPropertyChangeListener(new WeakListener.PropertyChange(modifL =
      new ModifiedListener()));
  }        
  
  /** A method to create a new component. Must be overridden in subclasses.
  * @return the cloneable top component for this support
  */
  protected CloneableTopComponent createCloneableTopComponent () {
    PropertiesTableModel ptm = new PropertiesTableModel(obj);
    
    return new PropertiesCloneableTopComponent(obj, ptm/*, ptcm*/);
  }
  
  public static class PropertiesCloneableTopComponent extends CloneableTopComponent {                                
    private DataObject dobj;
    private PropertyChangeListener cookieL;
    private PropertiesTableModel ptm;
//    PropertiesTableColumnModel ptcm;

    /** The string which will be appended to the name of top component
    * when top component becomes modified */
    protected String modifiedAppendix = " *";

    /** Constructor
    * @param obj data object we belong to
    */
    public PropertiesCloneableTopComponent (final DataObject obj, PropertiesTableModel ptm/*, PropertiesTableColumnModel ptcm*/) {
      super (obj);
      dobj  = obj;               
      setName(dobj.getNodeDelegate().getDisplayName());
      this.ptm  = ptm;
//      this.ptcm = ptcm;                
      
      // listen to saving
      dobj.addPropertyChangeListener(new WeakListener.PropertyChange(cookieL =
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == DataObject.PROP_COOKIE)
              setName(dobj.getNodeDelegate().getDisplayName());
          }
        }));

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
              if (((PropertiesFileEntry)((MultiDataObject)dobj).getPrimaryEntry()).
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
    
    /** Set the name of this top component. Handles saved/not saved state.
    * Notifies the window manager.
    * @param displayName the new display name
    */
    public void setName (final String name) {
      String saveAwareName = name;
      if (dobj != null)
        if (dobj.getCookie(SaveCookie.class) != null)
          saveAwareName = name + modifiedAppendix;
        else
          saveAwareName = name;
        
      if ((saveAwareName != null) && (saveAwareName.equals(getName())))
        return;
      super.setName(saveAwareName);
    }
  
    /** Is called from the clone method to create new component from this one.
    * This implementation only clones the object by calling super.clone method.
    * @return the copy of this object
    */
    protected CloneableTopComponent createClonedObject () {
      return new PropertiesCloneableTopComponent (dobj, ptm/*, ptcm*/);
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


  /** Listens to modifications and updates save cookie. */
  private final class ModifiedListener implements SaveCookie, PropertyChangeListener {

    /** Gives notification that the DataObject was changed.
    * @param ev PropertyChangeEvent
    */
    public void propertyChange(PropertyChangeEvent ev) {
      if ((ev.getSource() == obj) &&
          (ev.getPropertyName() == DataObject.PROP_MODIFIED)) {
        
        if (((Boolean) ev.getNewValue()).booleanValue()) {
          addSaveCookie();
        } else {
          removeSaveCookie();
        }
      }
    }

    /******* Implementation of the Save Cookie *********/

    public void save () throws IOException {
      // do saving job
      saveDocument();
    }

    /** Save the document in this thread.
    * Create "orig" document for the case that the save would fail.
    * @exception IOException on I/O error
    */
    public void saveDocument () throws IOException {
      final FileObject file = obj.getPrimaryEntry().getFile();
      PropertiesFileEntry pfe = (PropertiesFileEntry)obj.getPrimaryEntry();
      SaveCookie save = (SaveCookie)pfe.getCookie(SaveCookie.class);
      if (save != null)
        save.save();
      for (Iterator it = obj.secondaryEntries().iterator(); it.hasNext();) {
        save = (SaveCookie)((PropertiesFileEntry)it.next()).getCookie(SaveCookie.class);
        if (save != null)
          save.save();
      }  
    }

    /** Adds save cookie to the DO.
    */
    private void addSaveCookie() {
      if (obj.getCookie(SaveCookie.class) == null) {
        obj.getCookieSet().add(this);
      }
    }
    /** Removes save cookie from the DO.
    */
    private void removeSaveCookie() {
      if (obj.getCookie(SaveCookie.class) == this) {
        obj.getCookieSet().remove(this);
      }
    }
    
  } // end of SavingManager inner class



  
}
