/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
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
import org.netbeans.core.output.OutputTabTerm;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.compiler.CompilationEngineImpl;
import org.netbeans.core.execution.TopSecurityManager;
import org.netbeans.core.perftool.StartLog;
import org.netbeans.core.modules.ModuleManager;
import org.netbeans.core.modules.ModuleSystem;
import org.openide.xml.EntityCatalog;
import org.openide.loaders.Environment;

/** This class is a TopManager for Corona environment.
*
* @author Ales Novak, Jaroslav Tulach, Ian Formanek, Petr Hamernik, Jan Jancura, Jesse Glick
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
    private static ErrorManager defaultErrorManager;

    /** CompilationMachine */
    private CompilationEngine compilationEngine;

    /** WWW browser window. */
    private HtmlBrowser.BrowserComponent htmlViewer;


    /** nodeOperation */
    private NodeOperation nodeOperation;

    /** ProjectOperation main variable */
    static NbProjectOperation projectOperation;

    /** support for listeners */
    private PropertyChangeSupport change = new PropertyChangeSupport (this);

    /** repository */
    private Repository defaultRepository;

    /** loader pool */
    private DataLoaderPool loaderPool;

    /** status text */
    private String statusText = " "; // NOI18N

    /** the debugger listener listening on adding/removing debugger*/
    private static LookupListener debuggerLsnr = null;
    /** the lookup query finding all registered debuggers */
    private static Lookup.Result debuggerLkpRes = null;
    
    /** initializes properties about builds etc. */
    static {
        // Set up module-versioning properties, which logger prints.
        Package p = Package.getPackage ("org.openide"); // NOI18N
        
        putSystemProperty ("org.openide.specification.version", p.getSpecificationVersion (), "3.0"); // NOI18N
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
            TopSecurityManager.exit(1);
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
        // Awkward but should work.
        try {
            Class c = systemClassLoader().loadClass("org.netbeans.api.javahelp.Help"); // NOI18N
            Object o = Lookup.getDefault().lookup(c);
            if (o != null) {
                Method m = c.getMethod("showHelp", new Class[] {HelpCtx.class}); // NOI18N
                m.invoke(o, new Object[] {helpCtx});
                return;
            }
        } catch (ClassNotFoundException cnfe) {
            // ignore - maybe javahelp module is not installed, not so strange
        } catch (Exception e) {
            // potentially more serious
            getErrorManager().notify(ErrorManager.INFORMATIONAL, e);
        }
        // Did not work.
        Toolkit.getDefaultToolkit().beep();
    }

    /** Provides support for www documents.
    * @param url Url of WWW document to be showen.
    */
    public void showUrl (URL url) {
        if (htmlViewer == null) htmlViewer = new NbBrowser ();

	((NbBrowser)htmlViewer).showUrl (url);
    }


    /** Adds new explorer manager that will rule the selection of current
    * nodes until the runnable is running.
    *
    * @param run runnable to execute (till it is running the explorer manager is in progress)
    * @param em explorer manager 
    */
    public void attachExplorer (Runnable run, ExplorerManager em) {
        ((WindowManagerImpl)WindowManager.getDefault()).attachExplorer (run, em);
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
        if (ExitDialog.showDialog (null, true)) {
            NbProjectOperation.setOpeningProject (project);
        }
        else {
            throw new UserCancelException ();
        }
    }

    /** Getter of the default Environment.Provider
     */
    public Environment.Provider getEnvironmentProvider () {
        return org.netbeans.core.xml.XML.getEnvironmentProvider ();
    }

    /** Getter of the EntityCatalog of the system.
     */
    public EntityCatalog getEntityCatalog () {
        return org.netbeans.core.xml.XML.getEntityCatalog ();
    }
    
    /** Get the default exception manager for the IDE. It can be used to rafine
    * handling of exception and the way they are presented to the user.
    * @deprecated Do not call directly! Use ErrorManager.getDefault. This method
    * is here only for the purpose of being referenced from the core layer.
    * @return the manager
    */
    public static synchronized ErrorManager getDefaultErrorManager () {
        if (defaultErrorManager == null) {
            String className = System.getProperty("org.openide.ErrorManager"); // NOI18N
            if (className != null) {
                try {
                    // Use the startup classloader. This will be called early anyway, long before
                    // modules are ready to be used. So there is no point in trying systemClassLoader.
                    Class c = Class.forName(className);
                    defaultErrorManager = (ErrorManager)c.newInstance();
                    System.err.println("WARNING - use of the system property org.openide.ErrorManager is deprecated."); // NOI18N
                    System.err.println("Please register Services/Hidden/" + className.replace('.', '-') + ".instance instead."); // NOI18N
                    System.err.println("This should be placed before Services/Hidden/org-netbeans-core-default-error-manager.instance."); // NOI18N
                } catch (Exception e) {
                    System.err.println("Cannot create ErrorManager: " + className); // NOI18N
                    e.printStackTrace();
                }
            }
        }
        if (defaultErrorManager == null) {
            defaultErrorManager = new NbErrorManager();
            //System.err.println("Creating NbErrorManager");
        }
        return defaultErrorManager;
    }

    /** Accessor for window manager implementation. Used in core.lookup.TMLookup
     * for window manager isntance creation. 
     * Delegates to windows.WindowManagerImpl.getInstance()
    */
    public WindowManager getWindowManager () {
        return WindowManagerImpl.getInstance();
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
        return NbClipboard.getDefault();
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
    public final void setStatusText(String text) {
        if (text == null || text.length () == 0) {
            text = " "; // NOI18N
        }
        if (text.equals(statusText)) return;
        String old = statusText;
        statusText = text;
        setStatusTextImpl(text);
        firePropertyChange (PROP_STATUS_TEXT, old, text);
    }
    protected abstract void setStatusTextImpl(String text);

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
        Iterator it = getDebuggerResult().allInstances().iterator();
        if (it.hasNext()) return (Debugger) it.next();
        throw new DebuggerNotFoundException();
    }
    
    /** get the lookup query finding all registered debuggers */
    private synchronized Lookup.Result getDebuggerResult() {
        if (debuggerLkpRes == null) {
            debuggerLkpRes = Lookup.getDefault().lookup(new Lookup.Template(Debugger.class));
        }
        return debuggerLkpRes;
    }
    
    /** fire property change PROP_DEBUGGER */
    private void fireDebuggerChange() {
        firePropertyChange (PROP_DEBUGGER, null, null);
    }
    
    /** initialize listening on adding/removing debugger. */
    private void initDebuggerListener() {
        Lookup.Result res;
        synchronized (this) {
            if (debuggerLsnr != null) return;
            res = getDebuggerResult();
            debuggerLsnr = new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    fireDebuggerChange();
                }
            };
            res.addLookupListener(debuggerLsnr);
        }
        res.allClasses();
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
        return OutputTabTerm.getStdOut ();
    }

    /** creates new OutputWriter
    * @param name is a name of the writer
    * @return new OutputWriter with given name
    */
    public InputOutput getIO(String name, boolean newIO) {
        return OutputTabTerm.getIO (name, newIO);
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
            if ( System.getProperty ("netbeans.close") != null || ExitDialog.showDialog(null, true) ) {
                if (getModuleSystem().shutDown()) {
                    try {
                        // save project
                        NbProjectOperation.storeLastProject ();
                        org.netbeans.core.projects.SessionManager.getDefault().close();
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable t) {
                        // Do not let problems here prevent system shutdown. The module
                        // system is down; the IDE cannot be used further.
                        ErrorManager.getDefault().notify(t);
                    }
                    TopSecurityManager.exit(0);
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
            // #16265: do not go straight to ModuleManager
            return ms.getSystemClassLoader();
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
        initDebuggerListener();
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
                ErrorManager.getDefault ().notify (
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

	/** Show URL in browser
	 * @param url URL to be shown 
	 */
	private void showUrl (URL url) {
	    if (Boolean.TRUE.equals (getClientProperty ("InternalBrowser"))) { // NOI18N
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
	    }
            open ();
            requestFocus ();
            setURL (url);
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
    public static final class Lkp extends ProxyLookup {

        private static final boolean suppressMetaInfServicesLookup = !Boolean.getBoolean("netbeans.lookup.usemetainfservices"); // NOI18N

        /** Initialize the lookup to delegate to NbTopManager.
        */
        public Lkp () {
            super (suppressMetaInfServicesLookup ? 
                   new Lookup[] {
                       new org.netbeans.core.lookup.TMLookup(),
                       createInitialErrorManagerLookup(),
                   } :
                   new Lookup[] {
                       new org.netbeans.core.lookup.TMLookup(),
                       // #14722: pay attention also to META-INF/services/class.Name resources:
                       createMetaInfServicesLookup(false),
                       createInitialErrorManagerLookup(),
                   });
            //System.err.println("creating default lookup; suppressMetaInfServicesLookup=" + suppressMetaInfServicesLookup);
        }
        
        /** @param modules if true, use module classloader, else not */
        private static Lookup createMetaInfServicesLookup(boolean modules) {
            try {
                Class clazz = Class.forName("org.openide.util.MetaInfServicesLookup"); // NOI18N
                Constructor c = clazz.getDeclaredConstructor(new Class[] {ClassLoader.class});
                c.setAccessible(true);
                ClassLoader loader;
                if (modules) {
                    loader = get().getModuleSystem().getManager().getClassLoader();
                } else {
                    loader = Lkp.class.getClassLoader();
                }
                return (Lookup)c.newInstance(new Object[] {loader});
            } catch (Exception e) {
                e.printStackTrace();
                return Lookup.EMPTY;
            }
        }
        
        private static Lookup createInitialErrorManagerLookup() {
            InstanceContent c = new InstanceContent();
            c.add(Boolean.TRUE, new ConvertorListener());
            return new AbstractLookup(c);
        }
        
        private static int propModulesReceived = 0;
        private static boolean folderLookupFinished = false;
        private static final class ConvertorListener
                implements InstanceContent.Convertor, TaskListener, PropertyChangeListener {
            public Object convert(Object obj) {
                //System.err.println("IEMC.convert");
                return getDefaultErrorManager();
            }
            public Class type(Object obj) {
                return ErrorManager.class;
            }
            public String id(Object obj) {
                return "NbTopManager.defaultErrorManager"; // NOI18N
            }
            public String displayName(Object obj) {
                return id(obj); // ???
            }
            public void taskFinished(Task task) {
                //System.err.println("FolderLookup finished, removing old EM");
                folderLookupFinished = true;
                // FolderLookup has finished recognizing things. Remove the forced ErrorManager
                // override from the set of lookups.
                task.removeTaskListener(this);
                Lookup lookup = Lookup.getDefault();
                if (lookup instanceof Lkp) {
                    Lkp lkp = (Lkp)lookup;
                    Lookup[] old = lkp.getLookups();
                    if (old.length != (suppressMetaInfServicesLookup ? 5 : 6)) throw new IllegalStateException();
                    Lookup[] nue = suppressMetaInfServicesLookup ?
                        new Lookup[] {
                            old[0], // TMLookup
                            // do NOT include initialErrorManagerLookup; this is now replaced by the layer entry
                            // Services/Hidden/org-netbeans-core-default-error-manager.instance
                            old[2], // NbTM.instanceLookup
                            old[3], // FolderLookup
                            old[4], // moduleLookup
                        } :
                        new Lookup[] {
                            old[0], // TMLookup
                            // maybe replace it now with module-based lookup, if PROP_ENABLED_MODULES
                            // has not taken care of it yet
                            propModulesReceived > 0 ? old[1] : createMetaInfServicesLookup(true),
                            // do NOT include initialErrorManagerLookup; this is now replaced by the layer entry
                            // Services/Hidden/org-netbeans-core-default-error-manager.instance
                            old[3], // NbTM.instanceLookup
                            old[4], // FolderLookup
                            old[5], // moduleLookup
                        };
                    lkp.setLookups(nue);
                }
            }
            public void propertyChange(PropertyChangeEvent evt) {
                if (ModuleManager.PROP_ENABLED_MODULES.equals(evt.getPropertyName())) {
                    //System.err.println("modules changed; changing metaInfServicesLookup");
                    propModulesReceived++;
                    if (propModulesReceived == 1 && folderLookupFinished) {
                        // Just called from startup code. But we already set it to the full
                        // lookup in taskFinished; don't do it twice.
                        //System.err.println("skipping first modules change");
                        return;
                    }
                    // Time to refresh META-INF/services/ lookup; modules turned on or off.
                    Lookup lookup = Lookup.getDefault();
                    if (lookup instanceof Lkp) {
                        Lkp lkp = (Lkp)lookup;
                        Lookup[] old = lkp.getLookups();
                        Lookup[] nue = (Lookup[])old.clone();
                        nue[1] = createMetaInfServicesLookup(true);
                        lkp.setLookups(nue);
                        //System.err.println("lookups: " + java.util.Arrays.asList(arr));
                    }
                    /* just testing:
                    {
                        try {
                            Class c = get().systemClassLoader().loadClass("org.foo.Interface");
                            System.err.println("org.foo.Interface: " + Lookup.getDefault().lookup(new Lookup.Template(c)).allInstances());
                        } catch (Exception e) {
                            System.err.println(e.toString());
                        }
                    }
                    */
                }
            }
        }
        
        /** Called when a system classloader changes.
         */
        public static final void systemClassLoaderChanged () {
            Lookup lookup = Lookup.getDefault ();
            if (lookup instanceof Lkp) {
                lookup = ((Lkp)lookup).getLookups ()[0];
                if (lookup instanceof org.netbeans.core.lookup.TMLookup) {
                    ((org.netbeans.core.lookup.TMLookup)lookup).systemClassLoaderChanged ();
                }
            }
        }

        /** When all module classes are accessible thru systemClassLoader, this
         * method is called to initialize the FolderLookup.
         */
	    
        public static final synchronized void modulesClassPathInitialized () {
            //System.err.println("mCPI");
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
                    folder.addTaskListener(new ConvertorListener());
		    StartLog.logProgress ("created FolderLookup"); // NOI18N
                    
                    // extend the lookup
                    Lookup[] arr = suppressMetaInfServicesLookup ?
                        new Lookup[] {
                            lkp.getLookups ()[0], // TMLookup
                            // Include initialErrorManagerLookup provisionally, until the folder lookup
                            // is actually ready and usable
                            lkp.getLookups()[1], // initialErrorManagerLookup
                            NbTopManager.get ().getInstanceLookup (),
                            folder.getLookup (),
                            NbTopManager.get().getModuleSystem().getManager().getModuleLookup(),
                        } :
                        new Lookup[] {
                            lkp.getLookups ()[0], // TMLookup
                            lkp.getLookups()[1], // metaInfServicesLookup; still keep classpath one till later...
                            // Include initialErrorManagerLookup provisionally, until the folder lookup
                            // is actually ready and usable
                            lkp.getLookups()[2], // initialErrorManagerLookup
                            NbTopManager.get ().getInstanceLookup (),
                            folder.getLookup (),
                            NbTopManager.get().getModuleSystem().getManager().getModuleLookup(),
                        };
		    StartLog.logProgress ("prepared other Lookups"); // NOI18N

                    lkp.setLookups (arr);
		    StartLog.logProgress ("Lookups set"); // NOI18N
                    
                    if (!suppressMetaInfServicesLookup) {
                        // Also listen for changes in modules, as META-INF/services/ would change:
                        get().getModuleSystem().getManager().addPropertyChangeListener(new ConvertorListener());
                    }
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                    throw new IllegalStateException ("Cannot initialize folder Services"); // NOI18N
                }
            }
	    StartLog.logEnd ("NbTopManager$Lkp: initialization of FolderLookup"); // NOI18N
        }
        
        /* for testing only:
        protected void beforeLookup(Lookup.Template t) {
            super.beforeLookup(t);
            if (t.getType() == ErrorManager.class) {
                System.err.println("looking up ErrorManager; lookups=" + getLookups() + " length=" + getLookups().length); // NOI18N
            }
        }
         */
    }
}
