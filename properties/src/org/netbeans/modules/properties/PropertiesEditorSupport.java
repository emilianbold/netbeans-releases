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

package org.netbeans.modules.properties;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.lang.reflect.*;
import java.util.Iterator;
import java.text.MessageFormat;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.Timer;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.EditorKit;
import javax.swing.text.BadLocationException;


import org.openide.util.WeakListener;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.RequestProcessor;
import org.openide.text.EditorSupport;          
import org.openide.text.PositionRef;
import org.openide.cookies.EditCookie;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;

 
/** Support for viewing porperties files (EditCookie) by opening them in a text editor */
public class PropertiesEditorSupport extends EditorSupport implements EditCookie, Serializable {

  /** Timer which countdowns the auto-reparsing time. */
  javax.swing.Timer timer;

  /** New lines in this file was delimited by '\n' */
  static final byte NEW_LINE_N = 0;

  /** New lines in this file was delimited by '\r' */
  static final byte NEW_LINE_R = 1;

  /** New lines in this file was delimited by '\r\n' */
  static final byte NEW_LINE_RN = 2;
  
  /** The type of new lines */
  byte newLineType = NEW_LINE_N;

  /** The flag saying if we should listen to the document modifications */
  private boolean listenToEntryModifs = true;
  
  private Document listenDocument;

  /** Listener to the document changes - entry. The superclass holds a saving manager 
  * for the whole dataobject. */
  private EntrySavingManager entryModifL;

  /** Properties Settings */
  static final PropertiesSettings settings = new PropertiesSettings();
                                 
  static final long serialVersionUID =1787354011149868490L;
  /** Constructor */
  public PropertiesEditorSupport(PropertiesFileEntry entry) {
    super (entry);
//System.out.println("editor support constructor - " + entry.getFile().getName());
//Thread.dumpStack();    
    initialize();
  }
  
  public void initialize() {
    myEntry = (PropertiesFileEntry)entry;
    super.setModificationListening(false);
    setMIMEType (PropertiesDataObject.MIME_PROPERTIES);
    initTimer();

    // listen to myself so I can add a listener for changes when the document is loaded
    addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        if (isDocumentLoaded()) {
          setListening(true);
        }
      }
    });

    //PENDING
    // set actions
    /*setActions (new SystemAction [] {
      SystemAction.get (CutAction.class),
      SystemAction.get (CopyAction.class),
      SystemAction.get (PasteAction.class),
    });*/
  }
  
  void setRef(CloneableTopComponent.Ref ref) {
    allEditors = ref;
  }
  
  Object writeReplace() throws ObjectStreamException {
    return new SerialProxy(myEntry);
  }
  
  public static class SerialProxy implements Serializable {
    
    static final long serialVersionUID =2675098551717845346L;
    public SerialProxy(PropertiesFileEntry serialEntry) {
      this.serialEntry = serialEntry;
    }
  
    private PropertiesFileEntry serialEntry;
     
    Object readResolve() throws ObjectStreamException {
//System.out.println("deserializing properties editor");
//System.out.println("serialEntry " + serialEntry);
//System.out.println("dataobject " + serialEntry.getDataObject());
//Thread.dumpStack();
      Object pe = serialEntry.getPropertiesEditor();
//System.out.println("deserializing properties editor END");
      return pe;
    }
  }
  
  /** Visible view of underlying file entry */
  transient PropertiesFileEntry myEntry;
  
  /** Focuses existing component to open, or if none exists creates new.
  * @see OpenCookie#open
  */
  public void open () {
    CloneableTopComponent editor = openCloneableTopComponent2();
    editor.requestFocus();
  }
  
  
  /** Simply open for an editor. */
  protected final CloneableTopComponent openCloneableTopComponent2() {
    MessageFormat mf = new MessageFormat (NbBundle.getBundle(PropertiesEditorSupport.class).
      getString ("CTL_PropertiesOpen"));
    
    synchronized (allEditors) {
      try {
        CloneableTopComponent ret = (CloneableTopComponent)allEditors.getAnyComponent ();
        ret.open();
        return ret;
      } catch (java.util.NoSuchElementException ex) {
        // no opened editor
        TopManager.getDefault ().setStatusText (mf.format (
          new Object[] {entry.getFile().getName()}));
        
        CloneableTopComponent editor = createCloneableTopComponent ();
        allEditors = editor.getReference ();
        editor.open();
        
        TopManager.getDefault ().setStatusText (NbBundle.getBundle(DataObject.class).getString ("CTL_ObjectOpened"));
        return editor;
      }
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
  }              
  
  /** Returns whether there is an open component (editor or open). */
  public synchronized boolean hasOpenComponent() {
    return (hasOpenTableComponent() || hasOpenEditorComponent());
  }  
  
  private synchronized boolean hasOpenTableComponent() {
//System.out.println("hasOpenComponent (table) " + myEntry.getFile().getPackageNameExt('/','.') + " " + ((PropertiesDataObject)myEntry.getDataObject()).getOpenSupport().hasOpenComponent());
    return ((PropertiesDataObject)myEntry.getDataObject()).getOpenSupport().hasOpenComponent();
  }

  /** Returns whether there is an open editor component. */
  public synchronized boolean hasOpenEditorComponent() {
    java.util.Enumeration en = allEditors.getComponents ();
//System.out.println("hasOpenComponent (editor) " + myEntry.getFile().getPackageNameExt('/','.') + " " + en.hasMoreElements ());
    return en.hasMoreElements ();
  }  

  public void saveThisEntry() throws IOException {
    super.saveDocument();
    myEntry.setModified(false);
  }
  
  public boolean close() {
    SaveCookie savec = (SaveCookie) myEntry.getCookie(SaveCookie.class);
    if ((savec != null) && hasOpenTableComponent()) {
      return false;
    }
//System.out.println("closing");      
    if (!super.close())
      return false;
      
//System.out.println("closed - document open = " + isDocumentLoaded());
                      
    closeDocumentEntry();                  
    myEntry.getHandler().reparseNowBlocking();  
    return true;  
  }

  /** Clears all data from memory.
  */
/*  protected void closeDocument () {
    super.closeDocument();
    closeDocumentEntry();
  }*/
  
  /** Utility method which enables or disables listening to modifications
  * on asociated document.
  * <P>
  * Could be useful if we have to modify document, but do not want the
  * Save and Save All actions to be enabled/disabled automatically.
  * Initially modifications are listened to.
  * @param listenToModifs whether to listen to modifications
  */
  public void setModificationListening (final boolean listenToModifs) {
//System.out.println("set modification listening - " + listenToModifs);
    this.listenToEntryModifs = listenToModifs;
    if (getDocument() == null) return;
    setListening(listenToEntryModifs);
  }

  /* A method to create a new component. Overridden in subclasses.
  * @return the {@link Editor} for this support
  */
  protected CloneableTopComponent createCloneableTopComponent () {
    // initializes the document if not initialized
    prepareDocument ();

    DataObject obj = myEntry.getDataObject ();
    Editor editor = new PropertiesEditor (obj, this);
    return editor;
  }
                
  
  /** Should test whether all data is saved, and if not, prompt the user
  * to save. Called by my topcomponent when it wants to close its last topcomponent, but the table editor may still be open
  *
  * @return <code>true</code> if everything can be closed
  */
  protected boolean canClose () {
    SaveCookie savec = (SaveCookie) myEntry.getCookie(SaveCookie.class);
    if (savec != null) {                                                           
      // if the table is open, can close without worries, don't remove the save cookie
      if (hasOpenTableComponent())
        return true;
      
      // PENDING - is not thread safe
      MessageFormat format = new MessageFormat(NbBundle.getBundle(PropertiesEditorSupport.class).
        getString("MSG_SaveFile"));
      String msg = format.format(new Object[] { entry.getFile().getName()});
      NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_CANCEL_OPTION);
      Object ret = TopManager.getDefault().notify(nd);
       
      // cancel 
      if (NotifyDescriptor.CANCEL_OPTION.equals(ret))
        return false;
               
      // yes         
      if (NotifyDescriptor.YES_OPTION.equals(ret)) {
        try {
          savec.save();
        }
        catch (IOException e) {
          TopManager.getDefault().notifyException(e);
          return false;
        }
      }
            
      // no      
      if (NotifyDescriptor.NO_OPTION.equals(ret)) {
        return true;  
      }    
      
    }
    return true;
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
//System.out.println("saving - doc = " + doc);
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

  
  /** Does part of the cleanup - removes a listener.
  */
  private void closeDocumentEntry () {
    // listen to modifs
    if (listenToEntryModifs) {
      getEntryModifL().clearSaveCookie();

      setListening(false);
    }
  }
  
  private void setListening(boolean listen) {
    if (listen) {
      if ((getDocument() == null) || (listenDocument == getDocument()))
        return;
      if (listenDocument != null) // also holds that listenDocument != getDocument()
        listenDocument.removeDocumentListener(getEntryModifL());
      listenDocument = getDocument();
      listenDocument.addDocumentListener(getEntryModifL());
    }
    else {
      if (listenDocument != null) {
        listenDocument.removeDocumentListener(getEntryModifL());
        listenDocument = null;
      }
    }
  }
     
  /** Visible view of the underlying method. */
  public Editor openAt(PositionRef pos) {
    return super.openAt(pos);
  }
                                      
  /** Returns a EditCookie for editing at a given position. */
  public PropertiesEditAt getViewerAt(String key) {
    return new PropertiesEditAt (key);
  }
     
  /** Class for opening at a given key. */
  public class PropertiesEditAt implements EditCookie {
    
    private String key;                          
    
    PropertiesEditAt(String key) {
      this.key   = key;
    }                
     
    public void setKey(String key) {
      this.key = key;
    }                            
    
    public String getKey() {
      return key;
    }
    
    public void edit() {   
      Element.ItemElem item = myEntry.getHandler().getStructure().getItem(key);
      if (item != null) {                   
        PositionRef pos = item.getKeyElem().getBounds().getBegin();
        PropertiesEditorSupport.this.openAt(pos);
      }
      else {
        PropertiesEditorSupport.this.edit();
      }          
    }
    
  }
  

  /** Returns an entry saving manager. */
  private synchronized EntrySavingManager getEntryModifL () {
    if (entryModifL == null) {
      entryModifL = new EntrySavingManager();
      // listens whether to add or remove SaveCookie
      myEntry.addPropertyChangeListener(entryModifL);
    }
    return entryModifL;
  }
                           
  /** Make modifiedApendix accessible for inner classes. */
  String getModifiedAppendix() {
    return modifiedAppendix;
  }                       

  /** Cloneable top component to hold the editor kit.
  */
  public static class PropertiesEditor extends EditorSupport.Editor {
                                          
    /** Holds the file being edited */                                                                   
    protected transient PropertiesFileEntry entry;
    
    private transient PropertiesEditorSupport propSupport;
                                                                       
    /** Listener for entry's save cookie changes */
    private transient PropertyChangeListener saveCookieLNode;
    /** Listener for entry's name changes */
    private transient NodeAdapter nodeL;
           
    static final long serialVersionUID =-2702087884943509637L;
    /** Constructor for deserialization */       
    public PropertiesEditor() {
      super();
    }

    /** Creates new editor */
    public PropertiesEditor(DataObject obj, PropertiesEditorSupport support) {
      super(obj, support);
      this.propSupport = support;
      initMe();
    }
    
    /** initialization after construction and deserialization */
    private void initMe() {
      this.entry = propSupport.myEntry;

      // add to EditorSupport - patch for a bug in deserialization
      propSupport.setRef(getReference());
      
      entry.getNodeDelegate().addNodeListener (
        new WeakListener.Node(nodeL = 
        new NodeAdapter () {
          public void propertyChange (PropertyChangeEvent ev) {
            if (ev.getPropertyName ().equals (Node.PROP_DISPLAY_NAME)) {
              updateName();
            }         
          }  
        }
      ));
      Node n = entry.getNodeDelegate ();
      setActivatedNodes (new Node[] { n });
      
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

    /** When closing last view, also close the document.
     * @return <code>true</code> if close succeeded
    */
    protected boolean closeLast () {
      // instead of super
      if (!propSupport.canClose ()) {
        // if we cannot close the last window
        return false;
      }
               
      boolean doCloseDoc = !propSupport.hasOpenTableComponent();
      //SaveCookie savec = (SaveCookie) entry.getCookie(SaveCookie.class);
      try {
        if (doCloseDoc) {
          // propSupport.closeDocument (); by reflection
          Method closeDoc = EditorSupport.class.getDeclaredMethod("closeDocument", new Class[0]);
          closeDoc.setAccessible(true);
          closeDoc.invoke(propSupport, new Object[0]);
        }  
      
        /* if (propSupport.lastSelected == this) {
          propSupport.lastSelected = null; by reflection */
        Field lastSel = EditorSupport.class.getDeclaredField("lastSelected");
        lastSel.setAccessible(true);
        if (lastSel.get(propSupport) == this)
          lastSel.set(propSupport, null);
      } 
      catch (Exception e) { 
        if (Boolean.getBoolean("netbeans.debug.exceptions"))
          e.printStackTrace(); 
      }  
      
      // end super
      /*boolean canClose = super.closeLast();
      if (!canClose)
        return false;*/
      if (doCloseDoc) {  
        propSupport.closeDocumentEntry();
        entry.getHandler().reparseNowBlocking();  
      }  
      return true;
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
          setName(name + propSupport.getModifiedAppendix());
        else
          setName(name);
      }  
    }
  
    /* Serialize this top component.
    * @param out the stream to serialize to
    */
    public void writeExternal (ObjectOutput out)
                throws IOException {
      super.writeExternal(out);
      out.writeObject(propSupport);
    }

    /* Deserialize this top component.
    * @param in the stream to deserialize from
    */
    public void readExternal (ObjectInput in)
              throws IOException, ClassNotFoundException {
      super.readExternal(in);
      propSupport = (PropertiesEditorSupport)in.readObject();
      initMe();
    }

  } // end of PropertiesEditor inner class
  
  /** EntrySavingManager manages two tasks concerning saving:<P>
  * 1) It tracks changes in document asociated with ther entry and
  *    sets modification flag appropriately.<P>
  * 2) This class also implements functionality of SaveCookie interface
  */
  private final class EntrySavingManager implements DocumentListener, SaveCookie, PropertyChangeListener {

    /*********** Implementation of the DocumentListener *******/

    /** Gives notification that an attribute or set of attributes changed.
    * @param ev event describing the action
    */
    public void changedUpdate(DocumentEvent ev) {
      // do nothing - just an attribute
    }

    /** Gives notification that there was an insert into the document.
    * @param ev event describing the action
    */
    public void insertUpdate(DocumentEvent ev) {
      modified();
      changeStructureStatus();
    }

    /** Gives notification that a portion of the document has been removed.
    * @param ev event describing the action
    */
    public void removeUpdate(DocumentEvent ev) {
      modified();
      changeStructureStatus();
    }
    
    private void changeStructureStatus() {
      int delay = settings.getAutoParsingDelay();
      myEntry.getHandler().setDirty(true);
      if (delay > 0) {
        timer.setInitialDelay(delay);
        timer.restart();
      }
    }

    /** Gives notification that the DataObject was changed.
    * @param ev PropertyChangeEvent
    */
    public void propertyChange(PropertyChangeEvent ev) {
      if ((ev.getSource() == myEntry) &&
          (PropertiesFileEntry.PROP_MODIFIED.equals(ev.getPropertyName()))) {
        
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
      saveThisEntry();
    }

    void clearSaveCookie() {
      // remove save cookie (if save was succesfull)
      myEntry.setModified(false);
    }

    /** Sets modification flag.
    */
    private void modified () {
      myEntry.setModified(true);
    }
    /** Adds save cookie to the DO. Only if a component is open, otherwise saves it right away
    */
    private void addSaveCookie() {
      if (myEntry.getCookie(SaveCookie.class) == null) {
        myEntry.getCookieSet().add(this);
      }
      ((PropertiesDataObject)myEntry.getDataObject()).updateModificationStatus();
      if (!hasOpenComponent()) {
        RequestProcessor.postRequest(new Runnable() {
          public void run() {
            myEntry.getPropertiesEditor().open();
          }
        });
      }  
        
    }
    /** Removes save cookie from the DO.
    */
    private void removeSaveCookie() {                   
      // remove Save cookie from the data object
      if (myEntry.getCookie(SaveCookie.class) == this) {
        myEntry.getCookieSet().remove(this);
      }
      ((PropertiesDataObject)myEntry.getDataObject()).updateModificationStatus();
    }
    
  } // end of EntrySavingManager inner class


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
        while (nextToRead == '\r')
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
  static class NewLineOutputStream extends OutputStream {
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
      if (b == '\r')
        return;
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
