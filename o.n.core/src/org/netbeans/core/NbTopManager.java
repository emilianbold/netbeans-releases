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
import com.netbeans.ide.filesystems.jar.JarFileSystem;
import com.netbeans.ide.filesystems.local.*;
import com.netbeans.ide.options.ControlPanel;
import com.netbeans.ide.windows.*;
import com.netbeans.ide.explorer.*;
import com.netbeans.ide.explorer.view.BeanTreeView;


import com.netbeans.developer.impl.actions.*;
import com.netbeans.developer.impl.output.OutWindow;
/*
import com.netbeans.developer.impl.presenters.MenuContext;
import com.netbeans.developer.impl.presenters.ShortcutNode;
import com.netbeans.developer.impl.presenters.ToolbarContext;
*/
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.util.RequestProcessor;
import com.netbeans.ide.util.UserCancelException;
import com.netbeans.ide.util.actions.ActionPerformer;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.ide.util.io.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.util.Utilities;

/** This class is a TopManager for Corona environment.
*
* @author Ales Novak, Jaroslav Tulach, Ian Formanek, Petr Hamernik, Jan Jancura
*/
public class NbTopManager extends TopManager {
  /** stores main shortcut context*/
  static private Keymap shortcutContext;

  /** currently used debugger or null if none is in use */
  private static Debugger debugger;

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


  /** Constructs a new manager.
  */
  NbTopManager() {
    change = new PropertyChangeSupport (this);
  }

  /** Shows a specified HelpCtx in IDE's help window.
  * @param helpCtx thehelp to be shown
  */
  public void showHelp(com.netbeans.ide.util.HelpCtx helpCtx) {
    showUrl (helpCtx.getHelp ());
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
    throw new com.netbeans.ide.util.NotImplementedException ();
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
    t.printStackTrace();
    super.notifyException (t);
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
    return OutWindow.getStdOut ();
  }

  /** creates new OutputWriter
  * @param name is a name of the writer
  * @return new OutputWriter with given name
  */
  public InputOutput getIO(String name) {
    return OutWindow.getIO (name);
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
              NbBundle.getBundle (this).getString ("CTL_FMT_SavingMessage"),
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
          NbBundle.getBundle (this).getString("CTL_Cannot_save"),
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
    
    if (ModuleInstaller.exit ()) {
      Runtime.getRuntime().exit (0);
    }
  }

  /** @return the workspace pool for this manager
  */
  public WorkspacePool getWorkspacePool () {
    return CoronaWorkspacePool.getDefault ();
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
* Beta Change History:
*  0    Tuborg    0.16        --/--/98 Jan Formanek    setStatusText impl moved here from TopManager
*  0    Tuborg    0.17        --/--/98 Petr Hamernik   Confirmation added
*  0    Tuborg    0.18        --/--/98 Ales Novak      TopOutput impl
*  0    Tuborg    0.20        --/--/98 Jan Formanek    added some stuff - method "main", ...
*  0    Tuborg    0.21        --/--/98 Jan Formanek    reflecting move of com.netbeans.developer.system to com.netbeans.developer.impl
*  0    Tuborg    0.22        --/--/98 Jaroslav Tulach added image loader + changes in constructor + two
*  0    Tuborg    0.22        --/--/98 Jaroslav Tulach abstact method implemented
*  0    Tuborg    0.25        --/--/98 Jan Formanek    reflecting changes with Output system
*  0    Tuborg    0.26        --/--/98 Jan Jancura     InvocationTargetException in notifyException () method.
*  0    Tuborg    0.27        --/--/98 Jan Formanek    getIO works
*  0    Tuborg    0.28        --/--/98 Jaroslav Tulach changes to notifyException
*  0    Tuborg    0.29        --/--/98 Jan Formanek
*  0    Tuborg    0.30        --/--/98 Petr Hamernik   compiler system options added to ControlPanel
*  0    Tuborg    0.31        --/--/98 Jan Formanek    file system pool initialization from the CLASSPATH
*  0    Tuborg    0.33        --/--/98 Jaroslav Tulach change support of projects
*  0    Tuborg    0.34        --/--/98 Jan Jancura     cut a copy tracker-y
*  0    Tuborg    0.36        --/--/98 Jan Jancura     Exception in notify exc.
*  0    Tuborg    0.37        --/--/98 Ales Novak      saveAll impl
*  0    Tuborg    0.38        --/--/98 Jan Jancura     CoronaFileSystem
*  0    Tuborg    0.39        --/--/98 Jan Jancura     CoronaFileSystem moved and changed
*  0    Tuborg    0.40        --/--/98 Jan Formanek    command line options parsing added
*  0    Tuborg    0.41        --/--/98 Jan Formanek    command line option -locale added
*  0    Tuborg    0.42        --/--/98 Jan Formanek    slight change to reflect changes in swing 0.7 API
*  0    Tuborg    0.43        --/--/98 Jan Jancura     Debuger actions added
*  0    Tuborg    0.44        --/--/98 Jan Formanek    Localization of compiler messages
*  0    Tuborg    0.45        --/--/98 Ales Novak      projects
*  0    Tuborg    0.46        --/--/98 Jan Jancura     DebuggerActions changed
*  0    Tuborg    0.47        --/--/98 Jan Formanek    repaired to reflect moving loaders to coronax
*  0    Tuborg    0.50        --/--/98 Jan Formanek    SWITCHED TO NODES
*  0    Tuborg    0.52        --/--/98 Jan Jancura     System FS to the constructor of FSPool
*  0    Tuborg    0.53        --/--/98 Ales Novak      getNodeOpertaion
*  0    Tuborg    0.54        --/--/98 Jan Jancura     NodeOperation added
*  0    Tuborg    0.55        --/--/98 Ales Novak      BaseProjectOperation
*  0    Tuborg    0.56        --/--/98 Petr Hamernik   shortcut manager added
*  0    Tuborg    0.57        --/--/98 Jan Formanek    reflecting changes in CoronaDialog
*  0    Tuborg    0.58        --/--/98 Petr Hamernik   shortcut manager changed
*  0    Tuborg    0.59        --/--/98 Jan Formanek    reflecting move of SystemFileSystem
*  0    Tuborg    0.60        --/--/98 Jan Jancura     getDebugger added
*  0    Tuborg    0.61        --/--/98 Ales Novak      workspacecontext changed to nodes
*  0    Tuborg    0.62        --/--/98 Jan Formanek    FileSelector bugfix
*  0    Tuborg    0.63        --/--/98 Ales Novak      exit
*  0    Tuborg    0.65        --/--/98 Jan Formanek    reflecting move of Actions class to com.netbeans.developer.impl
*  0    Tuborg    0.67        --/--/98 Jan Formanek    added method explore()
*  0    Tuborg    0.68        --/--/98 Petr Hamernik   JavaLoaderSettings added
*  0    Tuborg    0.69        --/--/98 Jan Formanek    netbeansDir property used for system file system
*  0    Tuborg    0.69        --/--/98 Jan Formanek    commented out JavaDataLoader (due to bug in parser)
*  0    Tuborg    0.70        --/--/98 Jan Formanek    added DataSystemFilter for Templates to the TemplatesExplorer
*  0    Tuborg    0.71        --/--/98 Jan Formanek    additional checking for system filesystem
*  0    Tuborg    0.72        --/--/98 Jan Jancura     debugger options
*  0    Tuborg    0.74        --/--/98 Petr Hamernik   output    -""-
*  0    Tuborg    0.75        --/--/98 Jan Formanek    startup behaviour&look improved
*  0    Tuborg    0.76        --/--/98 Petr Hamernik   outWindow.getDefault() added
*  0    Tuborg    0.77        --/--/98 Jan Formanek    reflecting the creation of TopLogging class:
*  0    Tuborg    0.78        --/--/98 Jan Formanek    removed adding a RemoteFileSystem to the FileSystemPool
*  0    Tuborg    0.79        --/--/98 Jan Formanek    bugfix for system directory
*  0    Tuborg    0.80        --/--/98 Jan Formanek    the system jars/classes do not get mounted
*  0    Tuborg    0.81        --/--/98 Jan Formanek    if opened without project, the IDE shows a new explorer on startup
*  0    Tuborg    0.84        --/--/98 Jan Formanek    the system jars/classes are mounted, but in "hidden" state
*  0    Tuborg    0.85        --/--/98 Jan Formanek    bugfix Templates Explorer (BUGID: 00140080)
*  0    Tuborg    0.86        --/--/98 Jan Formanek    FormDataLoader enabled
*  0    Tuborg    0.87        --/--/98 Jan Formanek    catching exception during initialization (main method),
*  0    Tuborg    0.87        --/--/98 Jan Formanek    possible errors during this phase are written to console err
*  0    Tuborg    0.87        --/--/98 Jan Formanek    output only (to avoid problems with logging)
*  0    Tuborg    0.88        --/--/98 Petr Hamernik   default multi object frame added
*  0    Tuborg    0.90        --/--/98 Petr Hamernik   placeFrame added
*  0    Tuborg    0.91        --/--/98 Jaroslav Tulach DataLoaderPool changed to CoronaLoaderPool
*  0    Tuborg    0.92        --/--/98 Petr Hamernik   New action renamed
*  0    Tuborg    0.93        --/--/98 Ales Novak      projects made static
*  0    Tuborg    0.94        --/--/98 Jan Formanek    added methods showProperties to CoronaNodeOperation
*  0    Tuborg    0.95        --/--/98 Jan Formanek    templates explorer improvement
*  0    Tuborg    0.96        --/--/98 Jan Formanek    updateUI method added
*  0    Tuborg    0.97        --/--/98 Jan Formanek    default context menu actions modified
*  0    Tuborg    0.98        --/--/98 Jan Formanek    OpenProjectAction's performer commented out
*  0    Tuborg    0.99        --/--/98 Jan Jancura     HtmlDataObject Added
*  0    Tuborg    0.100       --/--/98 Jan Formanek    iconification/deiconification of main window (w/ the opened TopFrames)
*  0    Tuborg    0.101       --/--/98 Jan Formanek    IDESettings added
*  0    Tuborg    0.102       --/--/98 Jaroslav Tulach ExClipboard
*  0    Tuborg    0.103       --/--/98 Jaroslav Tulach placeWorkspaceElement
*  0    Tuborg    0.104       --/--/98 Petr Hamernik   security manager changes...
*  0    Tuborg    0.105       --/--/98 Petr Hamernik   light commented
*  0    Tuborg    0.106       --/--/98 Petr Hamernik   small change
*  0    Tuborg    0.107       --/--/98 Petr Hamernik   new root node
*  0    Tuborg    0.108       --/--/98 Jan Formanek    default popup menu actions reduced to PropertiesAction
*  0    Tuborg    0.109       --/--/98 Jan Formanek    SystemFileSystem is mounted as "hidden"
*  0    Tuborg    0.110       --/--/98 Jan Formanek    FormLoaderSettings added to the options pool
*  0    Tuborg    0.111       --/--/98 Petr Hamernik   systemFS changes...
*  0    Tuborg    0.112       --/--/98 Petr Hamernik   bugfix
*  0    Tuborg    0.113       --/--/98 Jan Formanek    Workspaces change - Running workspace added, names taken from ResourceBundle
*  0    Tuborg    0.114       --/--/98 Jan Formanek    getMainWindow changed
*  0    Tuborg    0.115       --/--/98 Jan Palka       Startup dialog is showing in the end of main()
*  0    Tuborg    0.116       --/--/98 Jan Formanek    changed showing startup tips
*  0    Tuborg    0.117       --/--/98 Jan Formanek    Windows L&F is default on Windows platforms
*  0    Tuborg    0.118       --/--/98 Jan Formanek    mounted JAR filesystems are hidden without full.hack
*  0    Tuborg    0.119       --/--/98 Jan Formanek    non-modal PropertySheet
*  0    Tuborg    0.120       --/--/98 Jan Jancura     showUrl method
*  0    Tuborg    0.121       --/--/98 Jan Formanek    added default windows on dektops
*  0    Tuborg    0.122       --/--/98 Petr Hamernik   Template node changes
*  0    Tuborg    0.123       --/--/98 Petr Hamernik   Positioning of frames improved
*  0    Tuborg    0.124       --/--/98 Petr Hamernik   FileSelector removed
*  0    Tuborg    0.125       --/--/98 Jan Formanek    Properties SheetFrame's help & icon
*  0    Tuborg    0.126       --/--/98 Jan Formanek    canonical path as system name for local filesystems
*  0    Tuborg    0.127       --/--/98 Jan Formanek    reflecting changes in cookies
*  0    Tuborg    0.128       --/--/98 Jan Formanek    explore moved to NodeOperation
*  0    Tuborg    0.129       --/--/98 Jan Formanek    changes in TemplateExplorer
*  0    Tuborg    0.130       --/--/98 Jan Formanek    shows a message in status line during exit
*  0    Tuborg    0.131       --/--/98 Ales Novak      method getWorkspacePoolContextNode added
*  0    Tuborg    0.132       --/--/98 Jan Formanek    Tuborg -> Netbeans
*  0    Tuborg    0.133       --/--/98 Jan Formanek    the failure to mount the normal "bad" items in the classpath
*  0    Tuborg    0.134       --/--/98 Jan Formanek    new SplashWindow
*  0    Tuborg    0.135       --/--/98 Jan Formanek    removed full.hack
*  0    Tuborg    0.136       --/--/98 Jaroslav Tulach RequestProcessor.startProcessing
*  0    Tuborg    0.137       --/--/98 Jan Formanek    new Splash system
*  0    Tuborg    0.138       --/--/98 Jan Formanek    fixed mounting JAR FSs
*/
