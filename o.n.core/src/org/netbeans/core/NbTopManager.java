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

package org.netbeans.core;

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
import org.openide.filesystems.FileSystem;
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
import org.openide.util.lookup.*;

import org.netbeans.core.actions.*;
import org.netbeans.core.output.OutputTab;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.compiler.CompilationEngineImpl;
import org.netbeans.core.perftool.StartLog;
import org.netbeans.core.modules.ModuleSystem;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;

/** This class is a TopManager for Corona environment.
*
* @author Ales Novak, Jaroslav Tulach, Ian Formanek, Petr Hamernik, Jan Jancura
*/
public abstract class NbTopManager extends TopManager {
    /* masks to define the interactivity level */

    /** initialize the main window?
    * if not set the main window is not create nor shown.
    */
    public static final int IL_MAIN_WINDOW = 0x0001;
    /** initialize window system?
    * if not set the selected node is taken from the top manager.
    */
    public static final int IL_WINDOWS = 0x0002;
    /** initialize workspaces when not created?
    */
    public static final int IL_WORKSPACES = 0x0004;


    /** Initialize everything.
    */
    public static final int IL_ALL = 0xffff;


    /** property for status text */
    public static final String PROP_STATUS_TEXT = "statusText"; // NOI18N
    
    /** property for system class loader */
    public static final String PROP_SYSTEM_CLASS_LOADER = "systemClassLoader"; // NOI18N
    /** property for current class loader */
    public static final String PROP_CURRENT_CLASS_LOADER = "currentClassLoader"; // NOI18N

    /** stores main shortcut context*/
    private Keymap shortcutContext;

    /** inner access to dynamic lookup service for this top mangager */
    private InstanceContent instanceContent;
    /** dynamic lookup service for this top mangager */
    private Lookup instanceLookup;

    /** default repository */
    private Repository repository;

    /** ExecutionMachine */
    private ExecutionEngine execEngine;

    /** error manager */
    private ErrorManager errorManager;

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

    /** status text */
    private String statusText;

    /** initializes properties about builds etc. */
    static {
        // Set up module-versioning properties, which logger prints.
        Package p = Package.getPackage ("org.openide"); // NOI18N
        
        putSystemProperty ("org.openide.specification.version", p.getSpecificationVersion (), "1.1.6"); // NOI18N
        putSystemProperty ("org.openide.version", p.getImplementationVersion (), "OwnBuild"); // NOI18N
        putSystemProperty ("org.openide.major.version", p.getSpecificationTitle (), "IDE/1"); // NOI18N
        putSystemProperty ("netbeans.buildnumber", p.getImplementationVersion (), "OwnBuild"); // NOI18N
        
        if (System.getProperties ().get ("org.openide.util.Lookup") == null) { // NOI18N
          // update the top manager to our main if it has not been provided yet
          System.getProperties().put (
            "org.openide.util.Lookup", // NOI18N
            "org.netbeans.core.NbTopManager$Lkp" // NOI18N
          );
        }
        
        // Enforce JDK 1.3+ since we would not work without it.
        if (Dependency.JAVA_SPEC.compareTo(new SpecificationVersion("1.3")) < 0) { // NOI18N
            System.err.println("The IDE requires JDK 1.3 or higher to run."); // XXX I18N?
            System.exit(1);
        }

        // System property jdk.home points to the JDK root directory. 
        // This is for JDKs which does not have jre directory (currently Mac OS X)
        {   String jdkHome=System.getProperty("java.home");  // NOI18N
        
            if (jdkHome!=null) {
                if (Utilities.getOperatingSystem()!=Utilities.OS_MAC) {
                    jdkHome+=File.separator+"..";  // NOI18N
                }
                System.setProperty ("jdk.home",jdkHome);  // NOI18N
            }
        }
        // read environment properties from external file, if any
        readEnvMap ();

        // initialize the URL factory
        Object o = org.openide.execution.NbClassLoader.class;
    }
    
    /** Puts a property into the system ones, but only if the value is not null.
     * @param propName name of property
     * @param value value to assign or null
     * @param failbackValue value to assign if the previous value is null
     */
    private static void putSystemProperty (
        String propName, String value, String failbackValue
    ) {
        if (System.getProperty (propName) == null) {
            // only set it if not null
            if (value != null) {
                System.setProperty (propName, value);
            } else {
                System.err.println(
                    "Warning: Versioning property \"" + propName + // NOI18N
                    "\" is not set. Defaulting to \"" + failbackValue + '"' // NOI18N
                ); 
                System.setProperty (propName, failbackValue);
            }
        }
    }

    /** Constructs a new manager.
    */
    public NbTopManager() {
        instanceContent = new InstanceContent ();
        instanceLookup = new AbstractLookup (instanceContent);
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
    protected abstract FileSystem createDefaultFileSystem ();

    /** Test method to check whether some level of interactivity is enabled.
    * @param il mask composed of the constants of IL_XXXX
    * @return true if such level is enabled
    */
    public abstract boolean isInteractive (int il);
    
    /** Creates error logger.
     */
    protected abstract PrintWriter createErrorLogger (int minLogSeverity);

    /** Allows subclasses to override this method and return different default set of nodes
    * the should be "selected". If no top component is active then this method is called to
    * allow the top manager to decide which nodes should be pointed as selected.
    *
    * @param activated true if the result cannot be null
    * @return the array of nodes to return from TopComponent.getRegistry ().getSelectedNodes or
    *    getActivatedNodes ()
    */
    public Node[] getDefaultNodes (boolean activated) {
        return activated ? new Node[0] : null;
    }
    
    //
    // The main method allows access to registration service
    //
    
    
    /** Register new instance.
     */
    public final void register (Object obj) {
        instanceContent.add (obj);
    }
    
    /** Register new instance.
     * @param obj source
     * @param conv convertor which postponing an instantiation
     */
    public final void register(Object obj, InstanceContent.Convertor conv) {
        instanceContent.add(obj, conv);
    }
    
    /** Unregisters the service.
     */
    public final void unregister (Object obj) {
        instanceContent.remove (obj);
    }
    /** Unregisters the service registered with a convertor.
     */
    public final void unregister (Object obj, InstanceContent.Convertor conv) {
        instanceContent.remove (obj, conv);
    }
    
    /** Private get instance lookup.
     */
    private final Lookup getInstanceLookup () {
        return instanceLookup;
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
            // [PENDING] if nonmodal, better for the dialog to be reused...
            // (but better nonmodal than modal here)
            d1.setModal (false);
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


    /** Adds new explorer manager that will rule the selection of current
    * nodes until the runnable is running.
    *
    * @param run runnable to execute (till it is running the explorer manager is in progress)
    * @param em explorer manager 
    */
    public void attachExplorer (Runnable run, ExplorerManager em) {
        WindowManagerImpl.getDefault().attachExplorer (run, em);
    }

    /** Creates new dialog.
    */
    public Dialog createDialog (final DialogDescriptor d) {
        return (Dialog)Mutex.EVENT.readAccess (new Mutex.Action () {
            public Object run () {
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
        });
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

    /** Get the exception manager for the IDE. It can be used to rafine
    * handling of exception and the way they are presented to the user.
    * 
    * @return the manager
    */
    public ErrorManager getErrorManager () {
        if (errorManager != null) {
            return errorManager;
        }

        synchronized (this) {
            if (errorManager == null) {
                errorManager = initErrorManager ();
            }            
        }
        return errorManager;
    }
    /** Allows to use another implementation of ErrorManager. 
     *  Configuration is easy and needs add system property: org.openide.ErrorManager.
     *  For example:  -J-Dorg.openide.ErrorManager=org.netbeans.core.NbTraceErrorManager
     *  @return implementation of ErrorManager
     */
    private ErrorManager initErrorManager () {
        String className = System.getProperty ("org.openide.ErrorManager");// NOI18N
        if (className == null) 
            return new NbErrorManager();

        try {
            Class c = Class.forName (className, true, systemClassLoader ());
            return (ErrorManager) c.newInstance();
        } catch(Exception e) {
            System.err.println("Cannot create ErrorManager: "+className);// NOI18N
            e.printStackTrace();
        }
        return new NbErrorManager();    
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

    /** Notifies user by a dialog.
    * @param descriptor description that contains needed informations
    * @return the option that has been choosen in the notification
    */
    public Object notify (final NotifyDescriptor descriptor) {
        return Mutex.EVENT.readAccess (new Mutex.Action () {
                public Object run () {
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
                        presenter = new NbPresenter
                            (descriptor,
                             TopManager.getDefault().getWindowManager().getMainWindow(),
                             true);
                    }
                    
                    //Bugfix #8551
                    presenter.getRootPane().requestDefaultFocus();
                    presenter.setVisible(true);

                    if (focusOwner != null) { // if the focusOwner is null (meaning that MainWindow was focused before), the focus will be back on main window
                        win.requestFocus ();
                        comp.requestFocus ();
                        focusOwner.requestFocus ();
                    }
                    return descriptor.getValue();
                }
            });
    }

    /** Shows specified text in MainWindow's status line.
    * @param text the text to be shown
    */
    public void setStatusText(String text) {
        if (Utilities.compareObjects(statusText, text)) return;
        if (text == null || text.length () == 0) {
            text = " "; // NOI18N
        }

        statusText = text;
        firePropertyChange (PROP_STATUS_TEXT, null, text);
    }

    /** Getter for status text.
    */
    public String getStatusText () {
        return statusText;
    }

    /** Returns currently installed debugger or throws
    *  DebuggerException (when no debugger is installed)
    * @return currently installed  debugger.
    */
    public Debugger getDebugger () throws DebuggerNotFoundException {
        Debugger d = (Debugger) Lookup.getDefault().lookup(Debugger.class);
        if (d == null) {
            throw new DebuggerNotFoundException();
        }
        return d;
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
                execEngine = org.netbeans.core.execution.ExecutionEngine.getExecutionEngine ();
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

    private boolean doingExit=false;
    public void exit ( ) {
        synchronized (this) {
            if (doingExit) {
                return ;
            }
            doingExit = true;
        }
        // save all open files
        try {
            if ( System.getProperty ("netbeans.close") != null || ExitDialog.showDialog() ) {
                if (getModuleSystem().shutDown()) {
                    // save project
                    NbProjectOperation.storeLastProject ();
                    org.netbeans.core.projects.SessionManager.getDefault().close();
                    if (Boolean.getBoolean("netbeans.windows.layers")) {
                        // temporary - save window manager
                        // (will be removed when SessionManager support will be
                        // completed)
                        try {
                            WindowManagerImpl.getDefault().writeXML();
                        } catch (IOException exc) {
                            exc.printStackTrace();
                        }
                    }
                    Runtime.getRuntime().exit ( 0 );
                }
            }
        } finally {
            synchronized (this) {
                doingExit = false; 
            }
        }
    }

    /** Shows exit dialog for activated File system nodes
    * after unmounting filesystem(s)
    * @return result of dialog (mount or unmount)
    */    
    public static boolean showExitDialog (Node[] activatedNodes) {
        return ExitDialog.showDialog(activatedNodes);
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
    
    /** Get the module subsystem. */
    public abstract ModuleSystem getModuleSystem();

    /** Obtains current up-to system classloader
    */
    public ClassLoader systemClassLoader () {
        ModuleSystem ms = getModuleSystem();
        if (ms != null) {
            return ms.getManager().getClassLoader();
        } else {
            // This can be called very early: if lookup asks for ClassLoader.
            // For now, just give the startup classloader.
            //System.err.println("Warning: giving out bogus systemClassLoader for now");
            //Thread.dumpStack();
            return NbTopManager.class.getClassLoader();
        }
    }
    // Access from ModuleSystem and from subclasses when moduleSystem is created:
    public final void fireSystemClassLoaderChange() {
        //System.err.println("change: systemClassLoader");
        firePropertyChange(PROP_SYSTEM_CLASS_LOADER, null, null);
    }

    /** Obtains current up-to data te classloader
    */
    public ClassLoader currentClassLoader () {
        ClassLoader l = ClassLoaderSupport.currentClassLoader ();
        if (l == null) {
            System.err.println("SHOULD NEVER HAPPEN: currentClassLoader==null"); // NOI18N
            l = systemClassLoader ();
        }
        return l;
    }
    // Access from ClassLoaderSupport:
    final void fireCurrentClassLoaderChange() {
        //System.err.println("change: currentClassLoader");
        firePropertyChange(PROP_CURRENT_CLASS_LOADER, null, null);
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

    /** Reads system properties from a file on a disk and stores them 
     * in System.getPropeties ().
     */
    private static void readEnvMap () {
        java.util.Properties env = System.getProperties ();
        String envfile = System.getProperty("netbeans.osenv"); // NOI18N
        if (envfile != null) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(envfile)));
                
                while (true) {
                    String line = in.readLine();
                    if (line == null)
                        break;
                    
                    int i = line.indexOf("="); // NOI18N
                    if (i == -1) {
                        continue;
                    }

                    String key = line.substring(0, i);
                    String value = line.substring(i + 1);
                    if (i >= 0) {
                        env.put("Env-" + key, value); // NOI18N
                        env.put("env-" + key.toLowerCase (), value); // NOI18N
                    }
                }
            }
            catch (IOException ignore) {
                TopManager.getDefault ().getErrorManager ().notify (
                    ErrorManager.INFORMATIONAL, ignore
                );
            }
        }
    }


    /**
    * For externalization of HTMLBrowser.
    */
    public static class NbBrowser extends HtmlBrowser.BrowserComponent {

        static final long serialVersionUID =5000673049583700380L;

        private transient PropertyChangeListener idePCL = null;
        /**
        * For externalization.
        */
        public NbBrowser () {
            super (((IDESettings)IDESettings.findObject (IDESettings.class, true)).getWWWBrowser (), true, true);
            setListener ();
        }
        
        /** 
         * Release resources and also allow to create new browser later using another implementation
         * @return result from ancestor is returned 
         */
        protected boolean closeLast () {
            if (idePCL != null) {
                ((IDESettings)IDESettings.findObject (IDESettings.class, true)).removePropertyChangeListener (idePCL);
                idePCL = null;
            }
            NbTopManager.get ().htmlViewer = null;
            return super.closeLast ();
        }

        /* Deserialize this top component.
        * @param in the stream to deserialize from
        */
        public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal (in);
            setListener ();
            NbTopManager.get ().htmlViewer = this;
        }

        /**
         *  Sets listener that invalidates this as main IDE's browser if user changes the settings
         */
        private void setListener () {
            if (idePCL != null)
                return;
            try {
                // listen on preffered browser change
                idePCL = new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        String name = evt.getPropertyName ();
                        if (name == null) return;
                        if (name.equals (IDESettings.PROP_WWWBROWSER)) {
                            NbTopManager.get ().htmlViewer = null;
                            if (idePCL != null) {
                                ((IDESettings)IDESettings.findObject (IDESettings.class, true))
                                .removePropertyChangeListener (idePCL);
                                idePCL = null;
                            }
                        }
                    }
                };
                ((IDESettings)IDESettings.findObject (IDESettings.class, true)).addPropertyChangeListener (idePCL);
            }
            catch (Exception ex) {
                NbTopManager.get ().notifyException (ex);
            }
        }
    }
    
    /** The default lookup for the system.
     */
    public static final class Lkp extends org.openide.util.lookup.ProxyLookup {
        private FolderLookup lookup;

        /** Initialize the lookup to delegate to NbTopManager.
        */
        public Lkp () {
            super (new Lookup[] { new org.netbeans.core.lookup.TMLookup () });
        }

        /** When all module classes are accessible thru systemClassLoader, this
         * method is called to initialize the FolderLookup.
         */
	    
        public static final synchronized void modulesClassPathInitialized () {
	    StartLog.logStart ("NbTopManager$Lkp: initialization of FolderLookup"); // NOI18N

            // replace the lookup by new one
            Lookup lookup = Lookup.getDefault ();
	    StartLog.logProgress ("Got Lookup"); // NOI18N

            if (lookup instanceof Lkp) {
                Lkp lkp = (Lkp)lookup;

                
                try {
                    DataFolder rootFolder = DataFolder.findFolder (
                        org.openide.TopManager.getDefault ().getRepository ().getDefaultFileSystem ().getRoot ()
                    );
                    DataFolder df = DataFolder.create (rootFolder, "Services"); // NOI18N
		    StartLog.logProgress ("Got Services folder"); // NOI18N

                    FolderLookup folder = new FolderLookup (df, "SL["); // NOI18N
                    lkp.lookup = folder;
		    StartLog.logProgress ("created FolderLookup"); // NOI18N
                    
                    // extend the lookup
                    Lookup[] arr = new org.openide.util.Lookup[] {
                        lkp.getLookups ()[0],
                        NbTopManager.get ().getInstanceLookup (),
                        folder.getLookup (),
                        NbTopManager.get().getModuleSystem().getManager().getModuleLookup(),
                    };
		    StartLog.logProgress ("prepared other Lookups"); // NOI18N

                    lkp.setLookups (arr);
		    StartLog.logProgress ("Lookups set"); // NOI18N
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                    throw new IllegalStateException ("Cannot initialize folder Services"); // NOI18N
                }
            }
	    StartLog.logEnd ("NbTopManager$Lkp: initialization of FolderLookup"); // NOI18N
        }
    }
}
