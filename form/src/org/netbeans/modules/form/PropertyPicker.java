/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.form;

import java.beans.*;
import java.lang.reflect.Method;
import java.util.Vector;

import com.netbeans.ide.TopManager;
import com.netbeans.ide.util.Utilities;

/** The PropertyPicker is a form which allows user to choose from property set
* of specified required class.
*
* @author  Ian Formanek
* @version 1.00, Aug 29, 1998
*/
public class PropertyPicker extends javax.swing.JDialog {

  public static final int CANCEL = 0;
  public static final int OK = 1;

  /** Initializes the Form */
  public PropertyPicker (java.awt.Frame parent, FormManager manager, RADComponent componentToSelect, Class requiredType) {
    super (parent != null ? parent : TopManager.getDefault ().getWindowManager ().getMainWindow (), true);

    this.manager = manager;
    this.requiredType = requiredType;
    initComponents ();

    setDefaultCloseOperation (javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
    addWindowListener (new java.awt.event.WindowAdapter () {
        public void windowClosing (java.awt.event.WindowEvent evt) {
          cancelDialog ();
        }
      }
    );
    
    // attach cancel also to Escape key
    getRootPane().registerKeyboardAction(
      new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          cancelDialog ();
        }
      },
      javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0, true),
      javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
    );
 
    Vector allComponents = new Vector ();
    allComponents.addElement (manager.getRADForm ().getTopLevelComponent ());
/*    RADComponent[] nodes = manager.getComponentsRoot ().getNonVisualsNode ().getSubComponents ();
    for (int i = 0; i < nodes.length; i++)
      allComponents.addElement (nodes[i]); */
 // [PENDING]
    addComponentsRecursively ((ComponentContainer)manager.getRADForm ().getTopLevelComponent (), allComponents); // [PENDING - incorrect cast]
    RADComponent[] components = new RADComponent [allComponents.size ()];
    allComponents.copyInto (components);
    int selIndex = -1;
    for (int i = 0; i < components.length; i++) {
      componentsCombo.addItem (components[i].getName ());
      if ((componentToSelect != null) && (componentToSelect.equals (components[i])))
        selIndex = i;
    }

    if (selIndex != -1) {
      selectedComponent = components[selIndex];
      componentsCombo.setSelectedIndex (selIndex);
    }

    propertyList.setSelectionMode (javax.swing.ListSelectionModel.SINGLE_SELECTION);
    updatePropertyList ();
    
    // localize components
    setTitle ( java.text.MessageFormat.format (
        FormEditor.getFormBundle ().getString ("CTL_FMT_CW_SelectProperty"),
        new Object[] { Utilities.getShortClassName (requiredType) }
      )
    );
    componentLabel.setText (FormEditor.getFormBundle ().getString ("CTL_CW_Component")); // "Component:"
    okButton.setText ("OK"); // [PENDING]
//        com.netbeans.developer.util.NetbeansBundle.getBundle("com.netbeans.developer.locales.BaseBundle").getString ("CTL_OK"));   // "OK");
    cancelButton.setText ("Cancel"); // [PENDING]
//        com.netbeans.developer.util.NetbeansBundle.getBundle("com.netbeans.developer.locales.BaseBundle").getString ("CTL_CANCEL")); // "Cancel");

    updateButtons ();

    pack ();
    FormUtils.centerWindow (this);
  }

  public java.awt.Dimension getPreferredSize () {
    java.awt.Dimension pref = super.getPreferredSize ();
    return new java.awt.Dimension (Math.max (pref.width, 250), Math.max (pref.height, 300));
  }
  
  int getReturnStatus () {
    return returnStatus;
  }

  RADComponent getSelectedComponent () {
    return selectedComponent;
  }

  PropertyDescriptor getSelectedProperty () {
    if ((selectedComponent == null) || (propertyList.getSelectedIndex () == -1))
      return null;
    return descriptors [propertyList.getSelectedIndex ()];
  }

// ----------------------------------------------------------------------------
// private methods

  private void addComponentsRecursively (ComponentContainer cont, Vector vect) {
    RADComponent[] children = cont.getSubBeans ();
    for (int i = 0; i < children.length; i++) {
      vect.addElement (children[i]);
      if (children[i] instanceof ComponentContainer)
        addComponentsRecursively ((ComponentContainer)children[i], vect);
    }
  }

  private void updatePropertyList () {
    RADComponent sel = getSelectedComponent ();
    if (sel == null) {
      propertyList.setListData (new Object [0]);
      propertyList.revalidate ();
      propertyList.repaint ();
    } else {
      PropertyDescriptor[] descs = sel.getBeanInfo ().getPropertyDescriptors ();
      Vector filtered = new Vector ();
      for (int i = 0; i < descs.length; i ++) {
        if ((descs[i].getReadMethod () != null) &&       // filter out non-readable properties
            (descs[i].getPropertyType () != null) &&  // indexed properties return null from getPropertyType
            requiredType.isAssignableFrom (descs[i].getPropertyType ())) {
          filtered.addElement (descs[i]);
        }
      }
      descriptors = new PropertyDescriptor[filtered.size ()];
      filtered.copyInto (descriptors);

      // sort the properties by name
/*      QuickSorter sorter = new QuickSorter () {
        public final int compare(Object o1, Object o2) {
          return (((PropertyDescriptor)o1).getName ()).compareTo(((PropertyDescriptor)o2).getName ());
        }
      };
      sorter.sort (descriptors); */ // [PENDING]

      String[] items = new String [descriptors.length];
      for (int i = 0; i < descriptors.length; i++)
        items[i] = descriptors[i].getName ();
      propertyList.setListData (items);
      propertyList.revalidate ();
      propertyList.repaint ();
    }
  }

  private void updateButtons () {
    okButton.setEnabled ((getSelectedComponent () != null) && (getSelectedProperty () != null));
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
  private void initComponents () {//GEN-BEGIN:initComponents
    addWindowListener (new java.awt.event.WindowAdapter () {
        public void windowClosing (java.awt.event.WindowEvent evt) {
          closeDialog (evt);
        }
      }
    );
    getContentPane ().setLayout (new java.awt.BorderLayout ());

    insidePanel = new javax.swing.JPanel ();
    insidePanel.setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(8, 8, 3, 8)));
    insidePanel.setLayout (new java.awt.BorderLayout (0, 5));

      propertiesScrollPane = new javax.swing.JScrollPane ();

        propertyList = new javax.swing.JList ();
        propertyList.addListSelectionListener (new javax.swing.event.ListSelectionListener () {
            public void valueChanged (javax.swing.event.ListSelectionEvent evt) {
              propertyListValueChanged (evt);
            }
          }
        );

      propertiesScrollPane.setViewportView (propertyList);
      insidePanel.add (propertiesScrollPane, "Center");

      jPanel1 = new javax.swing.JPanel ();
      jPanel1.setLayout (new java.awt.BorderLayout (8, 0));

        componentLabel = new javax.swing.JLabel ();
        componentLabel.setText ("Component:");
        jPanel1.add (componentLabel, "West");

        componentsCombo = new javax.swing.JComboBox ();
        componentsCombo.addItemListener (new java.awt.event.ItemListener () {
            public void itemStateChanged (java.awt.event.ItemEvent evt) {
              componentsComboItemStateChanged (evt);
            }
          }
        );
        jPanel1.add (componentsCombo, "Center");

      insidePanel.add (jPanel1, "North");

    getContentPane ().add (insidePanel, "Center");

    buttonsPanel = new javax.swing.JPanel ();
    buttonsPanel.setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(0, 5, 5, 5)));
    buttonsPanel.setLayout (new java.awt.BorderLayout ());

      leftButtonsPanel = new javax.swing.JPanel ();
      leftButtonsPanel.setLayout (new java.awt.FlowLayout (0, 5, 5));

      buttonsPanel.add (leftButtonsPanel, "West");

      rightButtonsPanel = new javax.swing.JPanel ();
      rightButtonsPanel.setLayout (new java.awt.FlowLayout (2, 5, 5));

        okButton = new javax.swing.JButton ();
        okButton.setText ("OK");
        okButton.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
              previousButtonActionPerformed (evt);
            }
          }
        );
        rightButtonsPanel.add (okButton);

        cancelButton = new javax.swing.JButton ();
        cancelButton.setText ("Cancel");
        cancelButton.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
              cancelButtonActionPerformed (evt);
            }
          }
        );
        rightButtonsPanel.add (cancelButton);

      buttonsPanel.add (rightButtonsPanel, "East");

    getContentPane ().add (buttonsPanel, "South");

  }//GEN-END:initComponents

  private void propertyListValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_propertyListValueChanged
    if (propertyList.getSelectedIndex () == -1)
      selectedProperty = null;
    else
      selectedProperty = descriptors[propertyList.getSelectedIndex ()];
    updateButtons ();
  }//GEN-LAST:event_propertyListValueChanged

  private void componentsComboItemStateChanged (java.awt.event.ItemEvent evt) {//GEN-FIRST:event_componentsComboItemStateChanged
    if (componentsCombo.getSelectedIndex () == -1)
      selectedComponent = null;
    else
      selectedComponent = components[componentsCombo.getSelectedIndex ()];
    updatePropertyList ();
  }//GEN-LAST:event_componentsComboItemStateChanged

  private void previousButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
    returnStatus = OK;
    setVisible (false);
  }//GEN-LAST:event_previousButtonActionPerformed

  private void cancelButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    cancelDialog ();
  }//GEN-LAST:event_cancelButtonActionPerformed

  /** Closes the dialog */
  private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:closeDialog
    cancelDialog ();
  }//GEN-LAST:closeDialog

  private void cancelDialog () {
    returnStatus = CANCEL;
    setVisible (false);
  }
  
// Variables declaration - do not modify//GEN-BEGIN:variables
  javax.swing.JPanel insidePanel;
  javax.swing.JPanel buttonsPanel;
  javax.swing.JScrollPane propertiesScrollPane;
  javax.swing.JPanel jPanel1;
  javax.swing.JList propertyList;
  javax.swing.JLabel componentLabel;
  javax.swing.JComboBox componentsCombo;
  javax.swing.JPanel leftButtonsPanel;
  javax.swing.JPanel rightButtonsPanel;
  javax.swing.JButton okButton;
  javax.swing.JButton cancelButton;
// End of variables declaration//GEN-END:variables


  private FormManager manager;
  private int returnStatus = CANCEL;

  private RADComponent[] components;
  private Class requiredType;
  private PropertyDescriptor[] descriptors;
  private RADComponent selectedComponent;
  private PropertyDescriptor selectedProperty;

}

/*
 * Log
 *  1    Gandalf   1.0         5/13/99  Ian Formanek    
 * $
 */

