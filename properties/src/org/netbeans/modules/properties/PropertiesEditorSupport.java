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
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.Timer;
import javax.swing.text.StyledDocument;
import javax.swing.text.EditorKit;
import javax.swing.text.BadLocationException;


import com.netbeans.ide.util.WeakListener;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.text.EditorSupport;
import com.netbeans.ide.cookies.ViewCookie;
import com.netbeans.ide.cookies.SaveCookie;
import com.netbeans.ide.loaders.MultiDataObject;
import com.netbeans.ide.loaders.DataObject;
import com.netbeans.ide.windows.CloneableTopComponent;
import com.netbeans.ide.windows.TopComponent;
import com.netbeans.ide.TopManager;

 
/** Support for viewing porperties files (ViewCookie) by opening them in a text editor */
public class PropertiesEditorSupport extends EditorSupport implements ViewCookie {

  /** Timer which countdowns the auto-reparsing time. */
  javax.swing.Timer timer;

  /** New lines in this file was delimited by '\n' */
  static final byte NEW_LINE_N = 0;

  /** New lines in this file was delimited by '\r' */
  static final byte NEW_LINE_R = 1;

  /** New lines in this file was delimited by '\r\n' */
  static final byte NEW_LINE_RN = 2;
  
  /** The type of new lines */
  byte newLineType;

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
  
   
  /** Focuses existing component to open, or if none exists creates new.
  * @see OpenCookie#open
  */
  public void open () {
    try {
      MessageFormat mf = new MessageFormat (NbBundle.getBundle(PropertiesEditorSupport.class).
        getString ("CTL_PropertiesOpen"));
      
      TopManager.getDefault ().setStatusText (mf.format (
        new Object[] {
          entry.getFile().getName()
        }
      ));
      synchronized (allEditors) {
        try {
          TopComponent editor = (TopComponent)allEditors.getAnyComponent ();
          editor.requestFocus ();
        } catch (java.util.NoSuchElementException ex) {
          // no opened editor
          CloneableTopComponent editor = createCloneableTopComponent ();
          allEditors = editor.getReference ();
          editor.open ();
          editor.requestFocus();
        }
      }
    } finally {
      TopManager.getDefault ().setStatusText (NbBundle.getBundle(PropertiesEditorSupport.class).
        getString ("CTL_PropertiesOpened"));
    }
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
        myEntry.getHandler().setDirty(true);
        if (delay > 0) {
          timer.setInitialDelay(delay);
          timer.restart();
        }
      }
    };

    // add change listener
    addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        if (isDocumentLoaded()) {
          getDocument().addDocumentListener(docListener);
        }
      }
    });
  }              
  
  /* A method to create a new component. Overridden in subclasses.
  * @return the {@link Editor} for this support
  */
  protected CloneableTopComponent createCloneableTopComponent () {
    // initializes the document if not initialized
    prepareDocument ();

    DataObject obj = findDataObject ();
    if (obj == null)
      System.out.println("object not found");
    Editor editor = new PropertiesEditor (obj, (PropertiesFileEntry)entry);
    return editor;
  }
  
  /** Read the file from the stream, filter the guarded section
  * comments, and mark the sections in the editor.
  *
  * @param doc the document to read into
  * @param stream the open stream to read from
  * @param kit the associated editor kit
  * @throws IOException if there was a problem reading the file
  * @throws BadLocationException should not normally be thrown
  * @see #saveFromKitToStream
  */
  protected void loadFromStreamToKit (StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
    
    NewLineInputStream is = new NewLineInputStream(stream);
    try {
      kit.read(is, doc, 0);
      newLineType = is.getNewLineType();
    }
    finally {
      is.close();
    }
  }

  /** Store the document and add the special comments signifying
  * guarded sections.
  *
  * @param doc the document to write from
  * @param kit the associated editor kit
  * @param stream the open stream to write to
  * @throws IOException if there was a problem writing the file
  * @throws BadLocationException should not normally be thrown
  * @see #loadFromStreamToKit
  */
  protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
    OutputStream os = new NewLineOutputStream(stream, newLineType);
    try {
      kit.write(os, doc, 0, doc.getLength());
    }
    finally {
      if (os != null) {
        try {
          os.close();
        }
        catch (IOException e) {
        }
      }
    }
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
      super(obj, PropertiesEditorSupport.this);
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
  

  /** This stream is able to filter various new line delimiters and replace them by \n.
  */
  static class NewLineInputStream extends InputStream {
    /** Encapsulated input stream */
    BufferedInputStream bufis;

    /** Next character to read. */
    int nextToRead;
    
    /** The count of types new line delimiters used in the file */
    int[] newLineTypes;
    
    /** Creates new stream.
    * @param is encapsulated input stream.
    * @param justFilter The flag determining if this stream should
    *        store the guarded block information. True means just filter,
    *        false means store the information.
    */
    public NewLineInputStream(InputStream is) throws IOException {
      bufis = new BufferedInputStream(is);
      nextToRead = bufis.read();
      newLineTypes = new int[] { 0, 0, 0 };
    }

    /** Reads one character.
    * @return next char or -1 if the end of file was reached.
    * @exception IOException if any problem occured.
    */
    public int read() throws IOException {
      if (nextToRead == -1)
        return -1;
              
      if (nextToRead == '\r') { 
        nextToRead = bufis.read();
        if (nextToRead == '\n') {     
          nextToRead = bufis.read();
          newLineTypes[NEW_LINE_RN]++;
          return '\n';
        }
        else {
          newLineTypes[NEW_LINE_R]++;
          return '\n';
        }
      }            
      if (nextToRead == '\n') {
        nextToRead = bufis.read();
        newLineTypes[NEW_LINE_N]++;
        return '\n';
      }
      int oldNextToRead = nextToRead;
      nextToRead = bufis.read();
      return oldNextToRead;
    }  
      
    public byte getNewLineType() {
      if (newLineTypes[0] > newLineTypes[1]) {
        return (newLineTypes[0] > newLineTypes[2]) ? (byte) 0 : 2;
      }
      else {
        return (newLineTypes[1] > newLineTypes[2]) ? (byte) 1 : 2;
      }
    }
  }


  /** This stream is used for changing the new line delimiters.
  * It replaces the '\n' by '\n', '\r' or "\r\n"
  */
  private static class NewLineOutputStream extends OutputStream {
    /** Underlaying stream. */
    OutputStream stream;
    
    /** The type of new line delimiter */
    byte newLineType;
    
    /** Creates new stream.
    * @param stream Underlaying stream
    * @param newLineType The type of new line delimiter
    */
    public NewLineOutputStream(OutputStream stream, byte newLineType) {
      this.stream = stream;
      this.newLineType = newLineType;
    }

    /** Write one character.
    * @param b char to write.
    */
    public void write(int b) throws IOException {
      if (b == '\n') {
        switch (newLineType) {
        case NEW_LINE_R:
          stream.write('\r');
          break;
        case NEW_LINE_RN:
          stream.write('\r');
        case NEW_LINE_N:
          stream.write('\n');
          break;
        }
      }
      else {
        stream.write(b);
      }
    }

    /** Closes the underlaying stream.
    */
    public void close() throws IOException {
      stream.flush();
      stream.close();
    }
  }
  

}
