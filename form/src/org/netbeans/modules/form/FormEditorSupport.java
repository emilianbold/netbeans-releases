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

import java.io.*;
import java.util.Iterator;

import org.openide.awt.UndoRedo;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.src.nodes.SourceChildren;
import org.openide.text.*;
import org.openide.util.NbBundle;

import com.netbeans.developer.modules.loaders.java.JavaEditor;

/** 
*
* @author Ian Formanek
*/
public class FormEditorSupport extends JavaEditor implements FormCookie {
  
  /** The reference to FormDataObject */
  private FormDataObject formObject;
  /** True, if the design form has been loaded from the form file */
  transient private boolean formLoaded;

  private UndoRedo.Manager undoManager;

  private RADComponentNode formRootNode;

  private FormManager2 formManager;

  private PersistenceManager saveManager;

  /** lock for opening form */
  private static final Object OPEN_FORM_LOCK = new Object ();

  public FormEditorSupport (MultiDataObject.Entry javaEntry, FormDataObject formObject) {
    super (javaEntry);
    this.formObject = formObject;
    formLoaded = false;
  }
    
  /** Focuses existing component to open, or if none exists creates new.
  * @see OpenCookie#open
  */
  public void open () {
    // status line - Opening form
    TopManager.getDefault ().setStatusText (
      java.text.MessageFormat.format (
        NbBundle.getBundle (FormEditorSupport.class).getString ("FMT_OpeningForm"),
        new Object[] { formObject.getName () }
      )
    );

    // load the form
    synchronized (OPEN_FORM_LOCK) {
      if (!formLoaded) {
        if (!loadForm ()) {
          TopManager.getDefault ().setStatusText ("");
          return;
        }
      }
    }

    // 1. show the ComponentInspector
    FormEditor.getComponentInspector().focusForm (getFormManager ());
    FormEditor.getComponentInspector().open ();

    // 2. open editor
    super.open();

    // 3. Open and focus form window
    getFormTopComponent ().open ();
    getFormTopComponent ().requestFocus ();
    
    // clear status line
    TopManager.getDefault ().setStatusText ("");
  }
  
  /* Calls superclass.
  * @param pos Where to place the caret.
  * @return always non null editor
  */
  protected EditorSupport.Editor openAt(PositionRef pos) {
    // status line - Opening form
    TopManager.getDefault ().setStatusText (
      java.text.MessageFormat.format (
        NbBundle.getBundle (FormEditorSupport.class).getString ("FMT_OpeningForm"),
        new Object[] { formObject.getName () }
      )
    );

    // load the form
    synchronized (OPEN_FORM_LOCK) {
      if (!formLoaded) {
        if (!loadForm ()) {
          TopManager.getDefault ().setStatusText ("");
          return null; // [PENDING]
        }
      }
    }

    // 1. open form window
    getFormTopComponent ().open ();
    
    // 2. show the ComponentInspector
    FormEditor.getComponentInspector().focusForm (getFormManager ());
    FormEditor.getComponentInspector().open ();

    // 3. Focus form window
    getFormTopComponent ().requestFocus ();
    
    // clear status line
    TopManager.getDefault ().setStatusText ("");

    // 4. open editor
    return super.openAt (pos);

  }

  public FormDataObject getFormObject () {
    return formObject;
  }
  
  public org.openide.nodes.Node getFormRootNode () {
    return formRootNode;
  }
  
  /** Create an undo/redo manager.
  * This manager is then attached to the document, and listens to
  * all changes made in it.
  * <P>
  * The default implementation simply uses <code>UndoRedo.Manager</code>.
  *
  * @return the undo/redo manager
  */
  protected UndoRedo.Manager createUndoRedoManager () {
    undoManager = super.createUndoRedoManager ();
    return undoManager;
  }

  UndoRedo.Manager getUndoManager () {
    return undoManager;
  }

  /** @returns the FormManager2 of this form */
  FormManager2 getFormManager () {
    return formManager;
  }
  
  /** @returns the Form Window */
  FormTopComponent getFormTopComponent () {
    if (!formLoaded) return null;
    return formManager.getFormTopComponent ();
  }

// -----------------------------------------------------------------------------
// Form Loading

  protected void notifyClose () {
    super.notifyClose ();
    if (getFormTopComponent () != null) getFormTopComponent ().close ();
    FormEditor.getComponentInspector().focusForm (null);
    SourceChildren sc = (SourceChildren)formObject.getNodeDelegate ().getChildren ();
    sc.remove (new RADComponentNode [] { formRootNode });
    formRootNode = null;
    formManager = null;
    formLoaded = false;
  }
  
  /** @return true if the form is already loaded, false otherwise */
  boolean isLoaded () {
    return formLoaded;
  }

  boolean supportsAdvancedFeatures () {
    return saveManager.supportsAdvancedFeatures ();
  }

  /** @return true if the form is opened, false otherwise */
  public boolean isOpened () {
    return formLoaded;
  }

  /** Loads the DesignForm from the .form file. 
  * @return true if the form was correcly loaded, false if any error occured 
  */
  protected boolean loadForm () {
    PersistenceManager loadManager = null;
    for (Iterator it = PersistenceManager.getManagers (); it.hasNext (); ) {
      PersistenceManager man = (PersistenceManager)it.next ();
      try {
        if (man.canLoadForm (formObject)) {
          loadManager = man;
          break;
        }
      } catch (IOException e) {
        // ignore error and try the next manager
      }
    }

    if (loadManager == null) {
      // [PENDING - notify user]
      return false;
    }

    if (!loadManager.supportsAdvancedFeatures ()) {
      Object result = TopManager.getDefault().notify(
        new NotifyDescriptor.Confirmation("The form is saved in an older format. Do you want to convert the form to the new XML persistence format?\nNote: If you answer No, some new features of the form editor will not be available",
                                          NotifyDescriptor.YES_NO_OPTION,
                                          NotifyDescriptor.QUESTION_MESSAGE)
        );
      if (NotifyDescriptor.YES_OPTION.equals(result)) {
        saveManager = new GandalfPersistenceManager ();
      } else {
        saveManager = loadManager;
      }
    } else {
      saveManager = loadManager;
    }

    FileObject formFile = formObject.getFormEntry ().getFile ();
    try {
      formManager = loadManager.loadForm (formObject);
      if (formManager == null) {
        return false;
        // [PENDING] - solve the failure
      }
      formManager.initialize ();
      
      // create form hierarchy node and add it to SourceChildren
      SourceChildren sc = (SourceChildren)formObject.getNodeDelegate ().getChildren ();
      formRootNode = new RADComponentNode (formManager.getRADForm ().getTopLevelComponent ());
      enforceNodesCreation (formRootNode);
      sc.add (new RADComponentNode [] { formRootNode });
        
      formLoaded = true;
    } catch (IOException e) {
      TopManager.getDefault ().notifyException (e);
      return false;
    }

    return true;
  }

  private void enforceNodesCreation (org.openide.nodes.Node node) {
    org.openide.nodes.Children ch = node.getChildren ();
    if (ch != org.openide.nodes.Children.LEAF) {
      org.openide.nodes.Node[] nodes = ch.getNodes ();
      for (int i = 0; i < nodes.length; i++) {
        enforceNodesCreation (nodes[i]);
      }
    }
  }

// -----------------------------------------------------------------------------
// Form Saving

  /** Save the document in this thread and start reparsing it.
  * @exception IOException on I/O error
  */
  public void saveDocument () throws IOException {
    super.saveDocument ();
    saveForm ();
  }

  /** Save the document in this thread.
  * @param parse true if the parser should be started, otherwise false
  * @exception IOException on I/O error
  */
  protected void saveDocumentIfNecessary(boolean parse) throws IOException {
    super.saveDocumentIfNecessary(parse);
    saveForm ();
  }

  private void saveForm () {
    if (formLoaded) {
      formManager.fireFormToBeSaved ();
      FileObject formFile = formObject.getFormEntry ().getFile ();
      try {
        saveManager.saveForm (formObject, formManager);
      } catch (IOException e) {
        e.printStackTrace ();
      }
    }
  }

// -----------------------------------------------------------------------------
// FormCookie implementation
  
  /** Method from FormCookie */
  public void gotoEditor() {
    synchronized (OPEN_FORM_LOCK) {
      if (!formLoaded)
        if (!loadForm ()) return;
    } 
    super.open();

  }

  /** Method from FormCookie */
  public void gotoForm() {
    synchronized (OPEN_FORM_LOCK) {
      if (!formLoaded)
        if (!loadForm ()) return;
    }
    getFormTopComponent ().open ();
    getFormTopComponent ().requestFocus ();
  }

}

/*
 * Log
 *  27   Gandalf   1.26        8/1/99   Ian Formanek    Fixed potential problem 
 *       with closing forms which were not loaded
 *  26   Gandalf   1.25        7/27/99  Ian Formanek    Fixed bug 2638 - Undo in
 *       an editor pane with guarded blocks screws up the guards.
 *  25   Gandalf   1.24        7/24/99  Ian Formanek    Fixed bug with opening 
 *       form via class element nodes.
 *  24   Gandalf   1.23        7/14/99  Ian Formanek    supportsAdvancedFeatures
 *       is checked before the form is loaded
 *  23   Gandalf   1.22        7/12/99  Ian Formanek    Notifies form load 
 *       exceptions
 *  22   Gandalf   1.21        7/11/99  Ian Formanek    
 *  21   Gandalf   1.20        7/11/99  Ian Formanek    Better work with 
 *       persistence managers, supportsAdvancedFeatures added
 *  20   Gandalf   1.19        7/3/99   Ian Formanek    Fires formToBeSaved 
 *       before saving...
 *  19   Gandalf   1.18        6/10/99  Ian Formanek    Fixed bug which caused 
 *       that forms saved using "Compile" saved only the source and not the form
 *  18   Gandalf   1.17        6/10/99  Ian Formanek    Patched bug which 
 *       prevented components to be selecteable by mouse before their nodes were
 *       created
 *  17   Gandalf   1.16        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  16   Gandalf   1.15        5/17/99  Ian Formanek    Fixed bug 1820 - An 
 *       exception is thrown when form is created from a template.
 *  15   Gandalf   1.14        5/16/99  Ian Formanek    
 *  14   Gandalf   1.13        5/15/99  Ian Formanek    
 *  13   Gandalf   1.12        5/12/99  Ian Formanek    
 *  12   Gandalf   1.11        5/11/99  Ian Formanek    Build 318 version
 *  11   Gandalf   1.10        5/10/99  Ian Formanek    
 *  10   Gandalf   1.9         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  9    Gandalf   1.8         5/2/99   Ian Formanek    
 *  8    Gandalf   1.7         4/29/99  Ian Formanek    
 *  7    Gandalf   1.6         4/29/99  Ian Formanek    
 *  6    Gandalf   1.5         4/12/99  Ian Formanek    Improved form loading 
 *       debug messages
 *  5    Gandalf   1.4         4/7/99   Ian Formanek    Backward-compatible 
 *       deserialization finalized for Gandalf beta
 *  4    Gandalf   1.3         3/28/99  Ian Formanek    
 *  3    Gandalf   1.2         3/27/99  Ian Formanek    
 *  2    Gandalf   1.1         3/26/99  Ian Formanek    
 *  1    Gandalf   1.0         3/24/99  Ian Formanek    
 * $
 */
