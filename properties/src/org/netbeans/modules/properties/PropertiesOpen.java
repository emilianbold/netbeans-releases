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
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.LineBorder;
import javax.swing.table.TableColumn;
 
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.FileEntry;
import org.openide.loaders.OpenSupport;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.explorer.propertysheet.PropertyDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;

 
/** Support for opening properties files (OpenCookie) in visual editor */
public class PropertiesOpen extends OpenSupport implements OpenCookie {
                  
  /** Main properties dataobject */                                 
  PropertiesDataObject obj;
  PropertyChangeListener modifL;
  
  JTable theTable = null;

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
  
  PropertiesFileEntry getEntry() {         
    return (PropertiesFileEntry)entry;
  }
    
  /** Opens the table at a given key */  
  public PropertiesOpenAt getOpenerForKey(PropertiesFileEntry entry, String key) {
    return new PropertiesOpenAt(entry, key);
  }
                                                                       
  /** Class for opening at a given key. */                                                                     
  public class PropertiesOpenAt implements OpenCookie {
    
    private String key;                          
    private PropertiesFileEntry entry;
    
    PropertiesOpenAt(PropertiesFileEntry entry, String key) {
      this.entry = entry;
      this.key   = key;
    }                
     
    public void setKey(String key) {
      this.key = key;
    }                            
    
    public String getKey() {
      return key;
    }
    
    public void open() {
      PropertiesOpen.this.open();           
      BundleStructure bs = obj.getBundleStructure();
      // find the entry   
      int entryIndex = bs.getEntryIndexByFileName(entry.getFile().getName());
      int rowIndex   = bs.getKeyIndexByName(key);                                 
      if ((entryIndex != -1) && (rowIndex != -1)) {
      }  
    }
  }

  public class PropertiesCloneableTopComponent extends CloneableTopComponent {                                
    
    private static final int DEFAULT_TABLE_WIDTH = 600;
    private static final int DEFAULT_KEY_WIDTH   = 150;
    
    private ListSelectionModel rowSelections;
    private ListSelectionModel columnSelections;

    private JTextArea textComment;
    private JTextArea textValue;
    private JButton removeButton;
    private JButton addButton;
    private JLabel labelComment;
    private JLabel labelValue;
  
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
        
      initComponents();
    }

    public HelpCtx getHelpCtx () {
      return new HelpCtx (PropertiesCloneableTopComponent.class);
    }

    /** Inits the subcomponents. */ 
    private void initComponents() {
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();
      setLayout (gridbag);
      
      textComment = new JTextArea();
      textValue = new JTextArea();

      theTable = new JTable(ptm/*, ptcm*/);


      // set the cell editor
      JTextField textField = new JTextField();
      textField.setBorder(new LineBorder(Color.black));
      theTable.setDefaultEditor(PropertiesTableModel.StringPair.class, 
        new PropertiesTableCellEditor(textField, textComment, textValue));
      // PENDING      
//      PropertiesCellEditor ed = new PropertiesCellEditor(new PropertyDisplayer());
//      table.setDefaultEditor(PropertiesTableModel.CommentValuePair.class, ed);
//      table.setDefaultEditor(String.class, DefaultCellEditor.class);

      // include in a scroll pane
      JScrollPane scrollPane = new JScrollPane(theTable);
      theTable.setPreferredScrollableViewportSize(new Dimension(DEFAULT_TABLE_WIDTH, 300));
      theTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      theTable.setCellSelectionEnabled(true);

      // set the column widths
      TableColumn column = null;
      for (int i = 0; i < theTable.getColumnModel().getColumnCount(); i++) {
        column = theTable.getColumnModel().getColumn(i);
        if (i == 0)
          column.setPreferredWidth(DEFAULT_KEY_WIDTH);
        else  
          column.setPreferredWidth(((int)theTable.getPreferredScrollableViewportSize().getWidth() - 
            DEFAULT_KEY_WIDTH - scrollPane.getInsets().left - scrollPane.getInsets().right) / 
            (theTable.getColumnModel().getColumnCount() - 1));
      }
      
      // selection listeners
      rowSelections = theTable.getSelectionModel();
      rowSelections.addListSelectionListener(
        new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent e) {
            rowSelections = (ListSelectionModel)e.getSource();
            selectionChanged();
          }
        });
      columnSelections = theTable.getColumnModel().getSelectionModel();
      columnSelections.addListSelectionListener(
        new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent e) {
            columnSelections = (ListSelectionModel)e.getSource();
            selectionChanged();
          }
        });
        
      // property change listener - listens to editing state of the table
      theTable.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          if (evt.getPropertyName().equals("tableCellEditor")) {
            updateEnabled();
          }
        }
      });
      
      c.fill = GridBagConstraints.BOTH;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.gridwidth = GridBagConstraints.REMAINDER; 
      gridbag.setConstraints(scrollPane, c);
      add (scrollPane);
      
      labelComment = new JLabel(PropertiesSettings.getString("LBL_CommentLabel"));
      c.insets = new Insets(3, 3, 3, 3);
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.gridwidth = 1;
      gridbag.setConstraints(labelComment, c);
      add (labelComment);
                                      
      textComment.setRows (2);
      textComment.setLineWrap(true);
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0;         
      c.gridwidth = GridBagConstraints.REMAINDER; 
      scrollPane = new JScrollPane(textComment);
      gridbag.setConstraints(scrollPane, c);
      add (scrollPane);

      labelValue = new JLabel(PropertiesSettings.getString("LBL_ValueLabel"));
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.0;
      c.gridwidth = 1;
      gridbag.setConstraints(labelValue, c);
      add (labelValue);
                                      
      textValue.setRows (2);
      textValue.setLineWrap(true);
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0;         
      c.gridwidth = GridBagConstraints.REMAINDER; 
      scrollPane = new JScrollPane(textValue);
      gridbag.setConstraints(scrollPane, c);
      add (scrollPane);
      
      // add property button
      addButton = new JButton(PropertiesSettings.getString("LBL_AddPropertyButton"));
      c.insets = new Insets(0, 0, 0, 0);
      c.weightx = 1;
      c.gridwidth = 2;
      gridbag.setConstraints(addButton, c);
      add (addButton);
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

      // remove row button
      removeButton = new JButton(PropertiesSettings.getString("LBL_RemovePropertyButton"));
      c.insets = new Insets(0, 0, 0, 0);
      c.weightx = 1;
      c.gridwidth = GridBagConstraints.REMAINDER; 
      gridbag.setConstraints(removeButton, c);
      add (removeButton);
      removeButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            PropertiesTableModel.StringPair sp = 
              (PropertiesTableModel.StringPair)theTable.getModel().getValueAt(rowSelections.getMinSelectionIndex(), 0);
            NotifyDescriptor.Confirmation msg = new NotifyDescriptor.Confirmation(
              java.text.MessageFormat.format(
                NbBundle.getBundle(PropertiesOpen.class).getString("MSG_DeleteKeyQuestion"),
                new Object[] {sp.getValue()}),
              NotifyDescriptor.OK_CANCEL_OPTION);
              if (TopManager.getDefault().notify(msg).equals(NotifyDescriptor.OK_OPTION)) {
                for (int i=0; i < ((PropertiesDataObject)dobj).getBundleStructure().getEntryCount(); i++) {
                  PropertiesFileEntry entry = ((PropertiesDataObject)dobj).getBundleStructure().getNthEntry(i);
                  if (entry != null) {
                    PropertiesStructure ps = entry.getHandler().getStructure();
                    if (ps != null) {
                      ps.deleteItem(sp.getValue());
                    }
                  }  
                }  
              }
          }
        }
      );  
      
      // enable or disable the fields based on selection
      textComment.setEditable(false);
      textComment.setEnabled(false);
      textValue.setEditable(false);
      textValue.setEnabled(false);
//      selectionChanged();
    }
    
    private void selectionChanged() {  
      // label for the key/value
      if (columnSelections.isSelectionEmpty() || (columnSelections.getMaxSelectionIndex() > 0))
        labelValue.setText(NbBundle.getBundle(PropertiesOpen.class).getString("LBL_ValueLabel"));
      else
        labelValue.setText(NbBundle.getBundle(PropertiesOpen.class).getString("LBL_KeyLabel"));
      
      // remove button
      if (rowSelections.isSelectionEmpty() || 
          rowSelections.getMinSelectionIndex()    != rowSelections.getMaxSelectionIndex()) {
        removeButton.setEnabled(false);
      }
      else {
        removeButton.setEnabled(true);
      }
    
      //System.out.println("Selection changed");
      // fields at the bottom
      if (rowSelections.isSelectionEmpty() || columnSelections.isSelectionEmpty() ||
          rowSelections.getMinSelectionIndex()    != rowSelections.getMaxSelectionIndex() ||
          columnSelections.getMinSelectionIndex() != columnSelections.getMaxSelectionIndex()) {
        if (!theTable.isEditing()) {
          textComment.setText("");
          textValue.setText("");
        }  
      }
      else {                         
        //System.out.println("Selection ok " + rowSelections.getMinSelectionIndex() + "," +columnSelections.getMinSelectionIndex());
        if (!theTable.isEditing()) {
          PropertiesTableModel.StringPair sp = 
            (PropertiesTableModel.StringPair)theTable.getModel().getValueAt(rowSelections.getMinSelectionIndex(), 
            columnSelections.getMinSelectionIndex());
          textComment.setText(sp.getComment());
          textValue.setText(sp.getValue());
          
/*          boolean edit = theTable.editCellAt(rowSelections.getMinSelectionIndex(), 
                                             columnSelections.getMinSelectionIndex());*/
        }

        // the selection is ok - edit, if not already editing this field
        if (theTable.getEditingRow()    != rowSelections.getMinSelectionIndex() || 
            theTable.getEditingColumn() != columnSelections.getMinSelectionIndex()) {
          //System.out.println("Editing invokelater-ing now");
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {                                                       
              //System.out.println("Calling editCellAt " + rowSelections.getMinSelectionIndex() + "," + columnSelections.getMinSelectionIndex());
              theTable.editCellAt(rowSelections.getMinSelectionIndex(), 
                                  columnSelections.getMinSelectionIndex());
            }
          });
        }  
      }    
    }                                              
    
    /** Updates the enabled status of the fields */
    private void updateEnabled() {
      // always edit value
      textValue.setEditable(theTable.isEditing());
      textValue.setEnabled(theTable.isEditing());
      // sometimes edit the comment
      if (theTable.isEditing()) {    
        PropertiesTableModel.StringPair sp = 
          (PropertiesTableModel.StringPair)theTable.getCellEditor().getCellEditorValue();
        textComment.setEditable(sp.isCommentEditable());
        textComment.setEnabled(sp.isCommentEditable());
      }
      else {
        textComment.setEditable(false);
        textComment.setEnabled(false);
      }  
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
    
      String PROPERTIES_MODE = "com.netbeans.developer.modules.loaders.properties";
      PropertiesCloneableTopComponent pctc = new PropertiesCloneableTopComponent (dobj, ptm/*, ptcm*/);
      Workspace cur = TopManager.getDefault().getWindowManager().getCurrentWorkspace();
      Mode m = cur.findMode(PROPERTIES_MODE);
      if (m == null) {
        m = cur.createMode(PROPERTIES_MODE, 
                           NbBundle.getBundle(PropertiesModule.class).getString("LAB_PropertiesModeName"),
                           null);
      } 
      // PENDING
      //m.setBounds(new Rectangle(x, y, width, height));
      m.dockInto(pctc);
      return pctc;
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
