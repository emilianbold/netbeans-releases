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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.Timer;


import com.netbeans.ide.util.WeakListener;
import com.netbeans.ide.text.EditorSupport;
import com.netbeans.ide.cookies.ViewCookie;
import com.netbeans.ide.cookies.SaveCookie;
import com.netbeans.ide.loaders.MultiDataObject;
import com.netbeans.ide.loaders.DataObject;
import com.netbeans.ide.windows.CloneableTopComponent;

 
/** Support for viewing porperties files (ViewCookie) by opening them in a text editor */
public class PropertiesEditorSupport extends EditorSupport implements ViewCookie {

  /** Timer which countdowns the auto-reparsing time. */
  javax.swing.Timer timer;

  /** Properties Settings */
  static final PropertiesSettings settings = new PropertiesSettings();

  /** Constructor */
  public PropertiesEditorSupport(PropertiesFileEntry entry) {
    super (entry);
    setMIMEType ("text/plain");
    initTimer();
    //PENDING
    // set actions
    /*setActions (new SystemAction [] {
      SystemAction.get (CutAction.class),
      SystemAction.get (CopyAction.class),
      SystemAction.get (PasteAction.class),
    });*/
  }      
                                 
  /** Visible view of underlying file entry */
  PropertiesFileEntry myEntry = (PropertiesFileEntry)entry;
  
  /** Implementation of ViewCookie interface */
  public void view () {
    open ();
  }
   
  /** Launches the timer for autoreparse */              
  private void initTimer() {
    // initialize timer
    timer = new Timer(0, new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        myEntry.getHandler().autoParse();
      }
    });
    timer.setInitialDelay(settings.getAutoParsingDelay());
    timer.setRepeats(false);

    // create document listener
    final DocumentListener docListener = new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { change(e); }
      public void changedUpdate(DocumentEvent e) { }
      public void removeUpdate(DocumentEvent e) { change(e); }
      
      private void change(DocumentEvent e) {
        int delay = settings.getAutoParsingDelay();
        if (delay > 0) {
          timer.setInitialDelay(delay);
          timer.restart();
        }
      }
    };
  }              
  
  /* A method to create a new component. Overridden in subclasses.
  * @return the {@link Editor} for this support
  */
  protected CloneableTopComponent createCloneableTopComponent () {
    // initializes the document if not initialized
    prepareDocument ();

    DataObject obj = findDataObject ();
    Editor editor = new PropertiesEditor (obj, (PropertiesFileEntry)entry);
    return editor;
  }
  
  
  /** Cloneable top component to hold the editor kit.
  */
  class PropertiesEditor extends EditorSupport.Editor {
                                          
    /** Holds the file being edited */                                                                   
    protected PropertiesFileEntry entry;
                                                                       
    /** Listener for entry's save cookie changes */
    private PropertyChangeListener saveCookieLNode;

    /** Creates new editor */
    public PropertiesEditor(DataObject obj, PropertiesFileEntry entry) {
      super(obj);
      this.entry = entry;
      updateName();

      // entry to the set of listeners
      saveCookieLNode = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          if (PresentableFileEntry.PROP_COOKIE.equals(evt.getPropertyName()) ||
              PresentableFileEntry.PROP_NAME.equals(evt.getPropertyName())) {
            updateName();
          }
        }
      };
      this.entry.addPropertyChangeListener(
        new WeakListener.PropertyChange(saveCookieLNode));
    }

    /** Updates the name of this top component according to
    * the existence of the save cookie in ascoiated data object
    */
    protected void updateName () {
      if (entry == null) {
        setName("");
        return;
      }
      else {
        String name = entry.getFile().getName();
        if (entry.getCookie(SaveCookie.class) != null)
          setName(name + PropertiesEditorSupport.this.modifiedAppendix);
        else
          setName(name);
      }  
    }
  
  } // end of PropertiesEditor inner class
  
}
