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

package com.netbeans.developer.impl;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Iterator;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.text.Keymap;
import javax.swing.border.*;

import org.openide.util.datatransfer.ExClipboard;
import org.openide.*;
import org.openide.awt.HtmlBrowser;
import org.openide.loaders.*;
import org.openide.actions.*;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.ProjectCookie;
import org.openide.debugger.Debugger;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.filesystems.*;
import org.openide.filesystems.JarFileSystem;
import org.openide.options.ControlPanel;
import org.openide.windows.WindowManager;
import org.openide.windows.OutputWriter;
import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.*;
import org.openide.util.io.*;
import org.openide.nodes.*;
import org.openide.execution.ExecutionEngine;
import org.openide.compiler.CompilationEngine;

import com.netbeans.developer.impl.actions.*;
import com.netbeans.developer.impl.output.OutputTab;
import com.netbeans.developer.impl.windows.WindowManagerImpl;
import com.netbeans.developer.impl.compiler.CompilationEngineImpl;


/** This class is a TopManager for Corona environment.
*
* @author Ales Novak, Jaroslav Tulach, Ian Formanek, Petr Hamernik, Jan Jancura
*/
public class NbTopManager extends TopManager {
  /** stores main shortcut context*/
  private Keymap shortcutContext;

  /** currently used debugger or null if none is in use */
  private Debugger debugger;

  /** default repository */
  private Repository repository;

  /** ExecutionMachine */
  private ExecutionEngine execEngine;

  /** CompilationMachine */
  private CompilationEngine compilationEngine;

  /** WWW browser window. */
  private HtmlBrowser.BrowserComponent htmlViewer;


  /** nodeOperation */
  private NodeOperation nodeOperation;
  /** clipboard */
  private ExClipboard clipboard;

  /** ProjectOperation main variable */
  static NbProjectOperation projectOperation;

  /** support for listeners */
  private PropertyChangeSupport change = new PropertyChangeSupport (this);

  /** repository */
  private Repository defaultRepository;
  
  /** loader pool */
  private DataLoaderPool loaderPool;

  /** the error level code for restarting windows */
  private static final int RESTART_EXIT_CODE = 66;

  /** Constructs a new manager.
  */
  public NbTopManager() {
  }
  
  /** Getter for instance of this manager.
  */
  public static NbTopManager get () {
    return (NbTopManager)TopManager.getDefault ();
  }
  
  //
  // Protected methods that are provided for subclasses (Main) 
  // to plug-in better implementation
  //
  protected FileSystem createDefaultFileSystem () {
    return new LocalFileSystem ();
  }

  
  
  
  
  //
  // Implementation of methods from TopManager
  //
  
  /** Default repository
  */
  public Repository getRepository () {
    if (defaultRepository != null) {
      return defaultRepository;
    }
    
    synchronized (this) {
      if (defaultRepository == null) {
        defaultRepository = new Repository (createDefaultFileSystem ());
      }
      return defaultRepository;
    }
  }

  
  /** Accessor to actions manager.
  */
  public ActionManager getActionManager () {
    return ModuleActions.INSTANCE;
  }

  /** Default repository.
  *

  /** Shows a specified HelpCtx in IDE's help window.
  * @param helpCtx thehelp to be shown
  */
  public void showHelp(HelpCtx helpCtx) {
    Help.getDefault ().showHelp (helpCtx);
  }

  /** Provides support for www documents.
  * @param url Url of WWW document to be showen.
  */
  public void showUrl (URL url) {
    NbPresenter d = NbPresenter.currentModalDialog;
    if (d != null) {
      HtmlBrowser htmlViewer = new HtmlBrowser ();
      htmlViewer.setURL (url);
      JDialog d1 = new JDialog (d);
      d1.getContentPane ().add ("Center", htmlViewer); // NOI18N
      d1.setModal (true);
      d1.setTitle (Main.getString ("CTL_Help"));
      d1.pack ();
      d1.show ();
      return;
    }
    
    
    if (htmlViewer == null) htmlViewer = new NbBrowser ();
    htmlViewer.open ();
    htmlViewer.requestFocus ();
    htmlViewer.setURL (url);
  }

  /** Creates new dialog.
  */
  public Dialog createDialog (DialogDescriptor d) {
    // if there is some modal dialog active, sets it as a parent
    // of created dialog
    if (NbPresenter.currentModalDialog != null) {
      return new NbDialog(d, NbPresenter.currentModalDialog);
    }
    // if there is some active top component and has focus, set its frame as
    // an owner of created dialog, or set main window otherwise
    TopComponent curTc = TopComponent.getRegistry().getActivated();
    Frame mainWindow = TopManager.getDefault().getWindowManager().getMainWindow();
    Frame owner = null;
    // Beware - main window is always set as a parent for non-modal
    // dialogs, because they sometims tend to live longer that currently
    // active top components (find dialog in editor is good example)
    if ((curTc != null) && d.isModal() &&
        (SwingUtilities.findFocusOwner(curTc) != null)) {
      // try to find top component's parent frame
      Component comp = SwingUtilities.windowForComponent(curTc);
      while ((comp != null) && !(comp instanceof Frame)) {
        comp = comp.getParent();
      }
      owner = (Frame)comp;
    }
    if (owner == null) {
      owner = mainWindow;
    }
    return new NbDialog(d, owner);
  }

  /** Interesting places.
  */
  public Places getPlaces () {
    return NbPlaces.getDefault ();
  }

  /** Opens specified project. Asks to save the previously opened project.
  * @exception IOException if error occurs accessing the project
  * @exception UserCancelException if the selection is interrupted by the user
  */
  public void openProject (ProjectCookie project) throws IOException, UserCancelException {
    NbProjectOperation.setProject (project);
  }

  /** Window manager.
  */
  public WindowManager getWindowManager () {
    return WindowManagerImpl.getDefault();
  }

  /** @return default root of keyboard shortcuts */
  public Keymap getGlobalKeymap () {
    if (shortcutContext != null) {
      return shortcutContext;
    }
    
    synchronized (this) {
      if (shortcutContext == null) {
        shortcutContext = new NbKeymap ();
      }
    }
    return shortcutContext;
  }

  /** Returns global clipboard for the whole system. Must be redefined
  * in subclasses.
  *
  * @return the clipboard for whole system
  */
  public ExClipboard getClipboard () {
    if (clipboard != null) {
      return clipboard;
    }
    
    synchronized (this) {
      if (clipboard == null) {
        clipboard = new CoronaClipboard (""); // NOI18N
      }
    }
    return clipboard;
  }

  /** Returns pool of options.
  * @return option pool
  */
  public ControlPanel getControlPanel () {
    return NbControlPanel.getDefault ();
  }

  /** Prints the stack.
  */
  public void notifyException (Throwable t) {
    NotifyException.notify (t);
  }

  /** Notifies user by a dialog.
  * @param descriptor description that contains needed informations
  * @return the option that has been choosen in the notification
  */
  public Object notify (NotifyDescriptor descriptor) {
    Component focusOwner = null;
    Component comp = org.openide.windows.TopComponent.getRegistry ().getActivated ();
    Component win = comp;
    while ((win != null) && (!(win instanceof Window))) win = win.getParent ();
    if (win != null) focusOwner = ((Window)win).getFocusOwner ();

    // set different owner if some modal dialog now active
    NbPresenter presenter = null;
    if (NbPresenter.currentModalDialog != null) {
      presenter = new NbPresenter(descriptor, NbPresenter.currentModalDialog, true);
    } else {
      presenter = new NbPresenter(
        descriptor, 
        TopManager.getDefault().getWindowManager().getMainWindow(),
        true
      );
    }
      
    presenter.setVisible(true);

    if (focusOwner != null) { // if the focusOwner is null (meaning that MainWindow was focused before), the focus will be back on main window
      win.requestFocus ();
      comp.requestFocus ();
      focusOwner.requestFocus (); 
    }
    return descriptor.getValue();
  }

  /** Shows specified text in MainWindow's status line.
  * @param text the text to be shown
  */
  public void setStatusText(String text) {
    StatusLine.setStatusText (text);
  }

  /** Returns currently installed debugger or throws
  *  DebuggerException (when no debugger is installed)
  * @return currently installed  debugger.
  */
  public Debugger getDebugger () throws DebuggerNotFoundException {
    
    Debugger d = debugger;
    if (d == null) {
      throw new DebuggerNotFoundException();
    }
    return d;
  }

  /** Setter for debugger.
  */
  final void setDebugger (Debugger d) {
    Debugger old;
    
    synchronized (this) {
      old = debugger;
      
      if (old != null && d != null) {
        throw new SecurityException ();
      }
      
      debugger = d;
    }
    
    firePropertyChange (PROP_DEBUGGER, old, d);
  }

  /**
  * @return implementation of ExecutionMachine
  */
  public ExecutionEngine getExecutionEngine () {
    if (execEngine != null) {
      return execEngine;
    }
    
    synchronized (this) {
      if (execEngine == null) {
        execEngine = com.netbeans.developer.impl.execution.ExecutionEngine.getExecutionEngine ();
      }
    }
    return execEngine;
  }

  /** @return implementation of CompilationEngine */
  public CompilationEngine getCompilationEngine() {
    if (compilationEngine != null) {
      return compilationEngine;
    }
    
    synchronized (this) {
      if (compilationEngine == null) {
        compilationEngine = new CompilationEngineImpl();
      }
    }
    return compilationEngine;
  }
  
  /** Services.
  */
  public org.openide.ServiceType.Registry getServices () {
    return Services.getDefault ();
  }

  /** Print output writer.
  * @return default system output printer
  */
  public OutputWriter getStdOut () {
    return OutputTab.getStdOut ();
  }

  /** creates new OutputWriter
  * @param name is a name of the writer
  * @return new OutputWriter with given name
  */
  public InputOutput getIO(String name, boolean newIO) {
    return OutputTab.getIO (name, newIO);
  }



  /** Getter for node operations.
  */
  public NodeOperation getNodeOperation () {
    if (nodeOperation != null) {
      return nodeOperation;
    }
    
    synchronized (this) {
      if (nodeOperation == null) {
        nodeOperation = new NbNodeOperation ();
      }
    }
    return nodeOperation;
  }

  /** saves all opened objects */
  public void saveAll () {
    DataObject dobj = null;
    ArrayList bad = new ArrayList ();
    DataObject[] modifs = DataObject.getRegistry ().getModified ();
    for (int i = 0; i < modifs.length; i++) {
      try {
        dobj = modifs[i];
        SaveCookie sc = (SaveCookie)dobj.getCookie(SaveCookie.class);
        if (sc != null) {
          TopManager.getDefault().setStatusText (
            java.text.MessageFormat.format (
              NbBundle.getBundle (NbTopManager.class).getString ("CTL_FMT_SavingMessage"),
              new Object[] { dobj.getName () }
            )
          );
          sc.save();
        }
      } catch (IOException ex) {
        bad.add (dobj);
      }
    }
    NotifyDescriptor descriptor;
    //recode this part to show only one dialog?
    Iterator ee = bad.iterator ();
    while (ee.hasNext ()) {
      descriptor = new NotifyDescriptor.Message(
        MessageFormat.format (
          NbBundle.getBundle (NbTopManager.class).getString("CTL_Cannot_save"),
          new Object[] { ((DataObject)ee.next()).getPrimaryFile().getName() }
        )
      );
      TopManager.getDefault ().notify (descriptor);
    }
    // notify user that everything is done
    TopManager.getDefault().setStatusText(
      NbBundle.getBundle (NbTopManager.class).getString ("MSG_AllSaved"));
  }    

  /** The ide is left after calling this method.
  * The method return iff Runtim.getRuntime().exit() fails
  */
  public void restart () {
    // save project
    exit (RESTART_EXIT_CODE);
  }

  /** Has the same behavior like exit( 0 );
  */
  public void exit ( ) {
    exit( 0 );
  }
  
  /** The ide is left after calling this method. All unsaved files are
  * saved. Modules are asked to exit
  * The method return iff Runtim.getRuntime().exit() fails
  * JVM ends with retValue code.
  */
  public void exit (int retValue) {
    // save all open files
    if ( System.getProperty ("netbeans.close") != null || ExitDialog.showDialog() ) {
      // save project
      NbProjectOperation.storeLastProject ();
    
      if (ModuleInstaller.exit ()) {
        Runtime.getRuntime().exit ( retValue );
      }
    }
  }
  
  
  /** Provides access to data loader pool.
  * @return the loader pool for the system
  */
  public DataLoaderPool getLoaderPool () {
    if (loaderPool != null) {
      return loaderPool;
    }
    
    synchronized (this) {
      if (loaderPool == null) {
        loaderPool = LoaderPoolNode.getNbLoaderPool ();
      }
    }
    return loaderPool;
  }

  /** Obtains current up-to system classloader
  */
  public ClassLoader systemClassLoader () {
    return ModuleClassLoader.systemClassLoader ();
  }

  /** Obtains current up-to data te classloader
  */
  public ClassLoader currentClassLoader () {
    return ClassLoaderSupport.currentClassLoader ();
  }

  
  
  /** Add listener */
  public void addPropertyChangeListener (PropertyChangeListener l) {
    change.addPropertyChangeListener (l);
  }

  /** Removes the listener */
  public void removePropertyChangeListener (PropertyChangeListener l) {
    change.removePropertyChangeListener (l);
  }
  
  /** Fires property change
  */
  public void firePropertyChange (String p, Object o, Object n) {
    change.firePropertyChange (p, o, n);
  }

  
  
  /** Provides support for www documents.
  *
  static HtmlBrowser.BrowserComponent getWWWBrowser () {
    return htmlViewer;
  }
  
  
  
  /**
  * For externalization of HTMLBrowser.
  */
  public static class NbBrowser extends HtmlBrowser.BrowserComponent {
  
    static final long serialVersionUID =5000673049583700380L;
    
    /**
    * For externalization.
    */
    public NbBrowser () {
    }
    
    /* Deserialize this top component.
    * @param in the stream to deserialize from
    */
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
      super.readExternal (in);
      NbTopManager.get ().htmlViewer = this;
    }
  }
}
/*
* Log
*  32   Corona    1.31        07/28/98 Miloslav Metelka No longer
*                                                      getEditorSettings... static
*                                                      method
*  31   Corona    1.30        07/27/98 Miloslav Metelka EditorSettingsTxt instead
*                                                      of EditorSettings
*  30   Corona    1.29        07/27/98 Jaroslav Tulach TopManager.getDefault
*                                                      ().getDefaultMultiFrame ()
*                                                      added
*
*  29   Corona    1.28        07/24/98 Ian Formanek    init in separate thread
*                                                      commented out
*  28   Corona    1.27        07/23/98 Jaroslav Tulach Lazy initialization of
*                                                      Output Window
*  27   Corona    1.26        07/22/98 Petr Hamernik   logging directory changed
*  26   Corona    1.25        07/22/98 Ales Novak
*  25   Corona    1.24        07/22/98 Ales Novak
*  24   Corona    1.23        07/22/98 Ales Novak
*  23   Corona    1.22        07/21/98 Jaroslav Tulach
*  22   Corona    1.21        07/20/98 Jaroslav Tulach Initializes in EventQueue
*  21   Corona    1.20        07/20/98 Jan Jancura     Customize forWindows
*                                                      customizers
*  20   Corona    1.19        07/13/98 Ales Novak
*  19   Corona    1.18        07/13/98 Ian Formanek    fixed bug in setStatusText
*                                                      - it was called before the
*                                                      main window was initialized
*                                                      and caused a
*                                                      NullPointerException
*  18   Corona    1.17        07/07/98 Petr Hamernik   bugfix
*  17   Corona    1.16        07/07/98 Ales Novak
*  16   Corona    1.15        07/07/98 Petr Hamernik   inputLine in Confirmation
*  15   Corona    1.14        07/02/98 Ales Novak
*  14   Corona    1.13        06/29/98 Jan Jancura     PropertySheet settings.
*  13   Corona    1.12        06/29/98 Ian Formanek    Fixed bug with setDefault()
*                                                      in ButtonBar
*  12   Corona    1.11        06/26/98 David Peroutka  Large icons for frames on
*                                                      Solaris
*  11   Corona    1.10        06/26/98 Jaroslav Tulach
*  10   Corona    1.9         06/22/98 Miloslav Metelka
*  9    Corona    1.8         06/19/98 Ales Novak
*  8    Corona    1.7         06/19/98 Ales Novak
*  7    Corona    1.6         06/18/98 Ales Novak
*  6    Corona    1.5         06/17/98 Ian Formanek    improved title behaviour of
*                                                      the SheetFrame
*  5    Corona    1.4         06/17/98 Ales Novak
*  4    Corona    1.3         06/17/98 Ian Formanek    Fixed bug 238 -
*                                                      Serialization does not work
*                                                      when "Properties" are
*                                                      opened.
*  3    Corona    1.2         06/15/98 Ian Formanek    Fixed bug 177 (Updating
*                                                      Look&Feel of menus)
*  2    Corona    1.1         06/15/98 Ian Formanek
*  1    Corona    1.0         06/11/98 David Peroutka
* $
*/
