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

import com.netbeans.ide.TopManager;
import com.netbeans.ide.NotifyDescriptor;
import com.netbeans.ide.loaders.MultiDataObject;
import com.netbeans.ide.src.nodes.SourceChildren;
import com.netbeans.ide.util.NbBundle;

import com.netbeans.developer.modules.loaders.java.JavaEditor;
import com.netbeans.developer.modules.loaders.form.formeditor.*;
import com.netbeans.developer.modules.loaders.form.forminfo.*;
import com.netbeans.developer.modules.loaders.form.backward.DesignForm;
import com.netbeans.developer.modules.loaders.form.backward.RADNode;
import com.netbeans.developer.modules.loaders.form.backward.RADVisualNode;
import com.netbeans.developer.modules.loaders.form.backward.RADContainerNode;

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
//  transient private DesignForm designForm;

  private FormTopComponent formTopComponent;
  
  private RADComponentNode formRootNode;

  private FormManager formManager;
  
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
      if (!formLoaded)
        if (!loadForm ()) {
          TopManager.getDefault ().setStatusText ("");
          return;
        }
    }

    // 1. open editor
    super.open();

    // 2. open form window
    getFormTopComponent ().open ();
    
    // 3. show the ComponentInspector
    FormEditor.getComponentInspector().open ();

    // clear status line
    TopManager.getDefault ().setStatusText ("");
  }
  
  /** @returns the Form Window */
  FormTopComponent getFormTopComponent () {
    if (!formLoaded) return null;
    if (formTopComponent == null) {
      formTopComponent = new FormTopComponent (formManager);
    }
    return formTopComponent;
  }

  public com.netbeans.ide.nodes.Node getFormRootNode () {
    return formRootNode;
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
  
  /** @return true if the form is already loaded, false otherwise */
  boolean isLoaded () {
    return formLoaded;
  }

  /** @return true if the form is opened, false otherwise */
  public boolean isOpened () {
    return formLoaded;
  }

  /** Loads the DesignForm from the .form file. 
  * @return true if the form was correcly loaded, false if any error occured 
  */
  protected boolean loadForm () {
    InputStream is = null;
    try {
      is = formObject.getFormEntry ().getFile().getInputStream();
    } catch (FileNotFoundException e) {
      String message = java.text.MessageFormat.format(NbBundle.getBundle (FormEditorSupport.class).getString("FMT_ERR_LoadingForm"),
                                            new Object[] { formObject.getName(), e.getClass().getName()} );
      TopManager.getDefault().notify(new NotifyDescriptor.Exception(e, message));
      return false;
    }
    
    ObjectInputStream ois = null;
    try {
      ois = new FormBCObjectInputStream(is); 

      // deserialization from stream
      Object deserializedForm = ois.readObject ();
      
      // create new objects from Backward compatibility classes
      RADForm radForm = null;
      if (deserializedForm instanceof DesignForm) {
        formManager = new FormManager (formObject);
        radForm = new RADForm (new JFrameFormInfo (), formManager);
        RADComponent[] subComps = createHierarchy ((((DesignForm)deserializedForm).getFormManager ().getRootNode ()), formManager);
        radForm.getTopLevelComponent ().initSubComponents (subComps);
      }

      FormEditor.displayErrorLog ();

      TopManager.getDefault ().notify (new NotifyDescriptor.Message (
            NbBundle.getBundle (FormEditorSupport.class).getString ("MSG_BackwardCompatibility_OK"),
            NotifyDescriptor.INFORMATION_MESSAGE
          )
       );
      formLoaded = true;

      // create form hierarchy node and add it to SourceChildren
      SourceChildren sc = (SourceChildren)formObject.getNodeDelegate ().getChildren ();
      formRootNode = new RADComponentNode ((RADComponent)radForm.getTopLevelComponent ());
      sc.add (new RADComponentNode [] { formRootNode });
                      
                  
    } catch (Throwable e) {
      if (System.getProperty ("netbeans.debug.form") != null) {
        e.printStackTrace ();
      }
      TopManager.getDefault ().notify (new NotifyDescriptor.Message (
            NbBundle.getBundle (FormEditorSupport.class).getString ("ERR_BackwardCompatibilityBreach"),
            NotifyDescriptor.WARNING_MESSAGE
          )
       );
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

    return true;
  }

  private static RADComponent[] createHierarchy (RADContainerNode node, FormManager formManager) {
    RADNode nodes[] = node.getSubNodes ();
    RADComponent[] comps = new RADComponent [nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      if (nodes[i] instanceof RADContainerNode) {
        comps[i] = new RADVisualContainer ();
        RADComponent[] subs = createHierarchy ((RADContainerNode)nodes[i], formManager);
        ((ComponentContainer)comps[i]).initSubComponents (subs);
      } else if (nodes[i] instanceof RADVisualNode) {
        comps[i] = new RADVisualComponent ();
      } else {
        comps[i] = new RADComponent ();
      }
      comps[i].setFormManager (formManager);
      comps[i].setComponent (nodes[i].getBeanClass ());
      comps[i].setName (nodes[i].getName ());
    }

    return comps;
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
/*    designForm.getRADWindow().show(); */
  }

  /** Method from FormCookie */
  public void gotoInspector() {
    // show the ComponentInspector
    FormEditor.getComponentInspector().open ();
  }

}

/*
 * Log
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
