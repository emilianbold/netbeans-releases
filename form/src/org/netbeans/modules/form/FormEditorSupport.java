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

import java.io.IOException;

import com.netbeans.ide.TopManager;
import com.netbeans.ide.loaders.MultiDataObject;
import com.netbeans.developer.modules.loaders.java.JavaEditor;
import com.netbeans.developer.modules.loaders.form.formeditor.*;

/** 
*
* @author Ian Formanek
*/
public class FormEditorSupport extends JavaEditor implements FormCookie {
  
  /** The reference to FormDataObject */
  private FormDataObject formObject;
  /** True, if the design form has been loaded from the form file */
  transient private boolean formLoaded;
  /** The DesignForm of this form */
  transient private DesignForm designForm;
  
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
    System.out.println("Form: opening: "+formObject.getName ());
    TopManager.getDefault ().setStatusText (
      java.text.MessageFormat.format (
        com.netbeans.ide.util.NbBundle.getBundle (FormEditorSupport.class).getString ("FMT_OpeningForm"),
        new Object[] { formObject.getName () }
      )
    );

    synchronized (OPEN_FORM_LOCK) {
      if (!formLoaded)
        if (!loadForm ()) return;
    }

    // show the ComponentInspector
    FormEditor.getComponentInspector().setVisible(true);

/*    designForm.getRADWindow().show(); */
    super.open();
    TopManager.getDefault ().setStatusText ("");
  }
  
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
/*    designForm.getRADWindow().show(); */
  }

  /** Method from FormCookie */
  public void gotoInspector() {
    // show the ComponentInspector
//    FormEditor.getComponentInspector().setVisible(true);
  }

  /** @returns the DesignForm of this Form */
/*  public DesignForm getDesignForm() {
    if (!formLoaded)
      loadForm ();
    return designForm;
  } */

  /** @returns the root Node of the nodes representing the AWT hierarchy */
/*  public RADFormNode getComponentsRoot() {
    if (!formLoaded)
      if (!loadForm ()) return null;
    return designForm.getFormManager().getComponentsRoot();
  } */

// -----------------------------------------------------------------------------
// Form Loading
  
  boolean isLoaded () {
    return formLoaded;
  }

  public boolean isOpened () {
    return formLoaded;
  }

  /** Loads the DesignForm from the .form file */
  protected boolean loadForm () {
    System.out.println("FormDataObject.java:74:loadForm");
    java.io.InputStream is = null;
    try {
      is = formObject.getFormEntry ().getFile().getInputStream();
    } catch (java.io.FileNotFoundException e) {
/*      String message = java.text.MessageFormat.format(formBundle.getString("FMT_ERR_LoadingForm"),
                                            new Object[] {getName(), e.getClass().getName()});
      TopManager.getDefault().notify(new NotifyDescriptor.Exception(e, message)); */
      e.printStackTrace ();
      return false;
    }
    
    java.io.ObjectInputStream ois = null;
    try {
      ois = new java.io.ObjectInputStream(is);
      designForm = (DesignForm) ois.readObject ();
//      FormEditor.displayErrorLog ();

      formLoaded = true;
//      designForm.initialize (this);
//      if (!modifiedInit) {
//        setModified (false);
/*        if (editorLock != null) {
          editorLock.releaseLock();
          editorLock = null;
        } */
        // though we regenerated, it should
        // not be different (AKA modified)
//      }
    } catch (Throwable e) {
/*      if (System.getProperty ("netbeans.full.hack") != null) {
        e.printStackTrace ();
        System.out.println ("IOException during opening form: Opening empty form");
        switch (new FormLoaderSettings ().getEmptyFormType ()) {
          default:
          case 0: designForm = new JFrameForm(); break;
          case 1: designForm = new JDialogForm(); break;
          case 2: designForm = new JAppletForm(); break;
          case 3: designForm = new JPanelForm(); break;
          case 4: designForm = new FrameForm(); break;
          case 5: designForm = new DialogForm(); break;
          case 6: designForm = new AppletForm(); break;
          case 7: designForm = new PanelForm(); break;
          case 8: designForm = new JInternalFrameForm(); break;
        }

        formLoaded = true;
        designForm.initialize (this);
        if (!modifiedInit)
          setModified (false); // though we regenerated, it should not be different (AKA modified)
      } else {
        Stringring message = MessageFormat.format(formBundle.getString("FMT_ERR_LoadingForm"),
                                              new Object[] {getName(), e.getClass().getName()});
        TopManager.getDefault().notify(new NotifyDescriptor.Exception(e, message));
        return false;
      } */
      e.printStackTrace ();
      return false;
    }
    finally {
      if (ois != null) {
        try {
          ois.close();
        }
        catch (IOException e) {
        }
      }
    }

    // enforce recreation of children
//    ((FormDataNode)nodeDelegate).updateFormNode ();
    return true;
  }

}

/*
 * Log
 *  1    Gandalf   1.0         3/24/99  Ian Formanek    
 * $
 */
