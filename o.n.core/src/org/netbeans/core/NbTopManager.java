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

import com.netbeans.ide.util.datatransfer.ExClipboard;
import com.netbeans.ide.*;
import com.netbeans.ide.loaders.*;
import com.netbeans.ide.actions.*;
import com.netbeans.ide.cookies.SaveCookie;
import com.netbeans.ide.debugger.Debugger;
import com.netbeans.ide.debugger.DebuggerNotFoundException;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.filesystems.JarFileSystem;
import com.netbeans.ide.options.ControlPanel;
import com.netbeans.ide.windows.*;
import com.netbeans.ide.explorer.*;
import com.netbeans.ide.explorer.view.BeanTreeView;


import com.netbeans.developer.impl.actions.*;
import com.netbeans.developer.impl.output.OutputTab;
import com.netbeans.ide.util.*;
import com.netbeans.ide.util.io.*;
import com.netbeans.ide.nodes.*;

/** This class is a TopManager for Corona environment.
*
* @author Ales Novak, Jaroslav Tulach, Ian Formanek, Petr Hamernik, Jan Jancura
*/
public class NbTopManager extends TopManager {
  /** stores main shortcut context*/
  static private Keymap shortcutContext;

  /** currently used debugger or null if none is in use */
  private static Debugger debugger;

  /** default repository */
  private static Repository repository;

  /** ExecutionMachine */
  private com.netbeans.ide.execution.ExecutionEngine execEngine;

  /** CompilationMachine */
  private com.netbeans.ide.compiler.CompilationEngine compilationEngine;

  /** WWW browser window. */
  static HtmlViewer htmlViewer;


  /** nodeOperation */
  static NodeOperation nodeOperation;
  /** clipboard */
  private static ExClipboard clipboard;

  /** ProjectOperation main variable */
  static NbProjectOperation projectOperation;

  /** window manager */
  static NbWindowManager windowManager;

  /** support for listeners */
  static PropertyChangeSupport change;

  /** repository */
  static Repository defaultRepository;


  /** Constructs a new manager.
  */
  NbTopManager() {
    change = new PropertyChangeSupport (this);
  }

  /** Default repository
  */
  public Repository getRepository () {
    return defaultRepository;
  }

  /** Default repository.
  */
  static Repository getDefaultRepository () {
    return defaultRepository;
  }

  /** Default repository.
  *

  /** Shows a specified HelpCtx in IDE's help window.
  * @param helpCtx thehelp to be shown
  */
  public void showHelp(HelpCtx helpCtx) {
    URL helpURL = helpCtx.getHelp();
    if (helpURL != null)
      showUrl(helpURL);
    else
      notify(new NotifyDescriptor.Message(
        NbBundle.getBundle(getClass()).getString("MSG_NoURL")));
  }

  /** Provides support for www documents.
  * @param url Url of WWW document to be showen.
  */
  public void showUrl (URL url) {
    if (htmlViewer == null) htmlViewer = new HtmlViewer ();
    htmlViewer.setURL (url);
  }

  /** Creates new dialog.
  */
  public Dialog createDialog (DialogDescriptor d) {
    return new NbDialog (d);
  }

  /** Interesting places.
  */
  public Places getPlaces () {
    return NbPlaces.getDefault ();
  }

  /** Window manager.
  */
  public WindowManager getWindowManager () {
    if (windowManager == null) {
      windowManager = new NbWindowManager ();
    }
    return windowManager;
  }

  /** Provides support for www documents.
  */
  static HtmlViewer getWWWBrowser () {
    return htmlViewer;
  }

  /** @return default root of keyboard shortcuts */
  public Keymap getGlobalKeymap () {
    if (shortcutContext == null) {
      shortcutContext = new NbKeymap ();
    }
    return shortcutContext;
  }

  /** Returns global clipboard for the whole system. Must be redefined
  * in subclasses.
  *
  * @return the clipboard for whole system
  */
  public ExClipboard getClipboard () {
    if (clipboard == null) {
      clipboard = new CoronaClipboard ("");
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
    if (t instanceof NotImplementedException) {
      NotifyDescriptor nd = new NotifyDescriptor.Message("This feature is not yet implemented.");
      nd.setTitle("Information");
      notify(nd);
    }
    else {
      t.printStackTrace();
      super.notifyException (t);
    }
  }

  /** Notifies user by a dialog.
  * @param descriptor description that contains needed informations
  * @return the option that has been choosen in the notification
  */
  public Object notify (NotifyDescriptor descriptor) {
    final NotifyPresenter presenter = new NotifyPresenter(descriptor);
    presenter.setVisible(true);
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
  static synchronized void setDebugger (Debugger d) {
    if (debugger == null || d == null) {
      Debugger old = debugger;
      debugger = d;
      change.firePropertyChange (PROP_DEBUGGER, old, d);
    } else {
      throw new SecurityException ();
    }
  }

  /**
  * @return implementation of ExecutionMachine
  */
  public com.netbeans.ide.execution.ExecutionEngine getExecutionEngine () {
    if (execEngine == null) execEngine =
      com.netbeans.developer.impl.execution.ExecutionEngine.getExecutionEngine();
    return execEngine;
  }

  /** @return implementation of CompilationEngine */
  public com.netbeans.ide.compiler.CompilationEngine getCompilationEngine() {
    if (compilationEngine == null) compilationEngine =
      new com.netbeans.developer.impl.compiler.CompilationEngineImpl();
    return compilationEngine;
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
  public InputOutput getIO(String name) {
    return OutputTab.getIO (name);
  }



  /** Getter for node operations.
  */
  public NodeOperation getNodeOperation () {
    if (nodeOperation == null) {
      nodeOperation = new NbNodeOperation ();
    }
    return nodeOperation;
  }

  // [LIGHT]
  /*
  static ModuleInstaller.Storage getModulesStorage() {
    return modules;
  }*/
  // [LIGHT END]



  /** saves all opened objects */
  public void saveAll () {
    DataObject dobj = null;
    ArrayList bad = new ArrayList ();
    Iterator ee = DataObject.getRegistry ().getModifiedSet ().iterator ();
    while (ee.hasNext ()) {
      try {
        dobj = (DataObject) ee.next ();
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
    ee = bad.iterator ();
    while (ee.hasNext ()) {
      descriptor = new NotifyDescriptor.Message(
        MessageFormat.format (
          NbBundle.getBundle (NbTopManager.class).getString("CTL_Cannot_save"),
          new Object[] { ((DataObject)ee.next()).getPrimaryFile().getName() }
        )
      );
      final NotifyPresenter presenter = new NotifyPresenter(descriptor);
      presenter.setVisible(true);
    }
  }

  /** The ide is left after calling this method.
  * The method return iff Runtim.getRuntime().exit() fails
  */
  public void exit () {
    NbProjectOperation.exit ();
    // PENDING - will be in the project
    //NbWorkspacePool.exit ();

    if (ModuleInstaller.exit ()) {
      Runtime.getRuntime().exit (0);
    }
  }

  /** @return the workspace pool for this manager
  */
  public WorkspacePool getWorkspacePool () {
    return NbWorkspacePool.getDefault ();
  }

  /** Provides access to data loader pool.
  * @return the loader pool for the system
  */
  public DataLoaderPool getLoaderPool () {
    return LoaderPoolNode.getNbLoaderPool ();
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
