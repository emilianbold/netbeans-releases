/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core;

import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.JDialog;
import javax.swing.event.ChangeListener;
import org.netbeans.TopSecurityManager;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.core.startup.ModuleLifecycleManager;
import org.netbeans.core.startup.ModuleSystem;
import org.netbeans.core.startup.layers.SessionManager;
import org.netbeans.core.ui.SwingBrowser;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Main switchboard for the NetBeans core.
 * Manages startup sequence, holds references to important objects, etc.
 */
public abstract class NbTopManager {
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
    
    /** Constructs a new manager.
    */
    public NbTopManager() {
        assert defaultTopManager == null : "Only one instance allowed"; // NOI18N
        defaultTopManager = this;
        
        Lookup lookup = Lookup.getDefault();
        if (!(lookup instanceof MainLookup)) {
            throw new ClassCastException("Wrong Lookup impl found: " + lookup);
        }
        MainLookup.startedNbTopManager();
    }

    /** Getter for instance of this manager.
    */
    public static NbTopManager get () {
        assert defaultTopManager != null : "Must be initialized already"; // NOI18N
        return defaultTopManager;
    }
    
    /** Danger method for clients who think they want an NbTM but don't actually
     * care whether it is ready or not. Should be removed eventually by getting
     * rid of useless protected methods in this class, and using Lookup to find
     * each configurable piece of impl.
     * @return a maybe half-constructed NbTM
     */
    public static NbTopManager getUninitialized() {
        return get();
    }
        
    private static NbTopManager defaultTopManager; 

    /**
     * Checks whether the top manager has been loaded already.
     * Used during early startup sequence.
     */
    public static synchronized boolean isInitialized () {
        return defaultTopManager != null;
    }
    
    /** Test method to check whether some level of interactivity is enabled.
     * XXX this method is unused; can it be deleted?
    * @param il mask composed of the constants of IL_XXXX
    * @return true if such level is enabled
    */
    public abstract boolean isInteractive (int il);
    
    //
    // The main method allows access to registration service
    //
    
    /** Register new instance.
     */
    public final void register (Object obj) {
        MainLookup.register (obj);
    }
    
    /** Register new instance.
     * @param obj source
     * @param conv convertor which postponing an instantiation
     */
    public final <T,R> void register(T obj, InstanceContent.Convertor<T,R> conv) {
        MainLookup.register (obj, conv);
    }
    
    /** Unregisters the service.
     */
    public final void unregister (Object obj) {
        MainLookup.unregister (obj);
    }
    /** Unregisters the service registered with a convertor.
     */
    public final <T,R> void unregister (T obj, InstanceContent.Convertor<T,R> conv) {
        MainLookup.unregister (obj, conv);
    }
    
    
    //
    // Implementation of methods from TopManager
    //

    /** Shows a specified HelpCtx in IDE's help window.
    * @param helpCtx thehelp to be shown
     * @deprecated Better to use org.netbeans.api.javahelp.Help
    */
    public void showHelp(HelpCtx helpCtx) {
        // Awkward but should work.
        try {
            Class<?> c = (Lookup.getDefault().lookup(ClassLoader.class)).loadClass("org.netbeans.api.javahelp.Help"); // NOI18N
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
            Logger.getLogger(NbTopManager.class.getName()).log(Level.WARNING, null, e);
        }
        // Did not work.
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Implementation of URL displayer, which shows documents in the configured web browser.
     */
    @org.openide.util.lookup.ServiceProvider(service=org.openide.awt.HtmlBrowser.URLDisplayer.class)
    public static final class NbURLDisplayer extends HtmlBrowser.URLDisplayer {
        /** Default constructor for lookup. */
        public NbURLDisplayer() {}
        /** WWW browser window. */
        private NbBrowser htmlViewer;
        public void showURL(final URL u) {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    if (htmlViewer == null) {
                        htmlViewer = new NbBrowser();
                    }
                    htmlViewer.showUrl(u);
                }
            });
        }
    }


    /**
     * Default status displayer implementation; GUI is in StatusLine.
     */
    @org.openide.util.lookup.ServiceProvider(service=org.openide.awt.StatusDisplayer.class)
    public static final class NbStatusDisplayer extends org.openide.awt.StatusDisplayer {
        /** Default constructor for lookup. */
        public NbStatusDisplayer() {}
        private final ChangeSupport cs = new ChangeSupport(this);
        //list of status messages sorted by their importance in descending order
        private List<WeakReference<MessageImpl>> messages = new ArrayList<WeakReference<MessageImpl>>(30);

        private static int SURVIVING_TIME = Integer.getInteger("org.openide.awt.StatusDisplayer.DISPLAY_TIME", 5000);

        public void setStatusText(String text) {
            //unimportant message are cleared automatically after some time
            add( text, 0 ).clear(SURVIVING_TIME);
        }

        @Override
        public Message setStatusText(String text, int importance) {
            if( importance <= 0 )
                throw new IllegalArgumentException("Invalid importance value: " + importance);
            return add( text, importance );
        }
        
        public synchronized String getStatusText() {
            String text = null;
            synchronized( this ) {
                MessageImpl msg = getCurrent();
                text = null == msg ? "" : msg.text;
            }
            return text;
        }

        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        /**
         * @return The most important status message in the list or null
         */
        private MessageImpl getCurrent() {
            while( !messages.isEmpty() ) {
                WeakReference<MessageImpl> ref = messages.get(0);
                MessageImpl impl = ref.get();
                if( null != impl ) {
                    return impl;
                } else {
                    messages.remove(0);
                }
            }
            return null;
        }

        /**
         * Add new status message and fire change event. The message won't show
         * in the status line if there's already a message with higher importance.
         * @param text Status line message
         * @param importance Message importance
         * @return New status Message
         */
        private MessageImpl add( String text, int importance ) {
            MessageImpl newMsg = new MessageImpl(text, importance);
            WeakReference<MessageImpl> newRef = new WeakReference<MessageImpl>(newMsg);
            synchronized( this ) {
                boolean added = false;
                for( int i=0; i<messages.size() && !added; i++ ) {
                    WeakReference<MessageImpl> ref = messages.get(0);
                    MessageImpl impl = ref.get();
                    if (impl == null)
                        continue;
                    if( impl.importance == importance ) {
                        messages.set(i, newRef);
                        added = true;
                    } else if( impl.importance < importance ) {
                        messages.add(i, newRef);
                        added = true;
                    }
                }
                if( !added ) {
                    messages.add(newRef);
                }
            }
            cs.fireChange();
            Logger.getLogger(NbStatusDisplayer.class.getName()).log(Level.FINE, 
                    "Status text updated: {0}, importance: {1}", new Object[] {text, Integer.valueOf(importance)} );
            return newMsg;
        }

        /**
         * Remove given message and fire change event. If there's a less important
         * message in the list, it will show in status line.
         * @param toRemove
         */
        private void remove( MessageImpl toRemove ) {
            synchronized( this ) {
                WeakReference<MessageImpl> refToRemove = null;
                for( WeakReference<MessageImpl> ref : messages ) {
                    if( toRemove == ref.get() ) {
                        refToRemove = ref;
                        break;
                    }
                }
                if( null != refToRemove )
                    messages.remove(refToRemove);
            }
            cs.fireChange();
        }

        /**
         * Status line message which clears itself when garbage collected.
         */
        private class MessageImpl implements StatusDisplayer.Message, Runnable {
            private final String text;
            private final int importance;

            public MessageImpl( String text, int importance ) {
                this.text = text;
                this.importance = importance;
            }

            public void clear(int timeInMillis) {
                RequestProcessor.getDefault().post(this, timeInMillis);
            }

            @Override
            protected void finalize() throws Throwable {
                run();
            }

            public void run() {
                remove( this );
            }
        }
    }

    /** saves all opened objects */
    private static void saveAll () {
        DataObject dobj = null;
        ArrayList<DataObject> bad = new ArrayList<DataObject> ();
        DataObject[] modifs = DataObject.getRegistry ().getModified ();
        if (modifs.length == 0) {
            // Do not show MSG_AllSaved
            return;
        }
        for (int i = 0; i < modifs.length; i++) {
            try {
                dobj = modifs[i];
                SaveCookie sc = dobj.getLookup().lookup(SaveCookie.class);

                if (sc != null) {
                    org.openide.awt.StatusDisplayer.getDefault().setStatusText(
                        MessageFormat.format(
                            NbBundle.getBundle(NbTopManager.class).getString(
                                "CTL_FMT_SavingMessage"
                            ), new Object[]{dobj.getName()}
                    ));
                    sc.save();
                }
            }
            catch (IOException ex) {
                Logger.getLogger(NbTopManager.class.getName()).log(Level.WARNING, null, ex);
                bad.add(dobj);
            }
        }
        NotifyDescriptor descriptor;
        //recode this part to show only one dialog?
        Iterator<DataObject> ee = bad.iterator ();
        while (ee.hasNext ()) {
            descriptor = new NotifyDescriptor.Message(
                        MessageFormat.format (
                            NbBundle.getBundle (NbTopManager.class).getString("CTL_Cannot_save"),
                            new Object[] { ee.next().getPrimaryFile().getName() }
                        )
                    );
            org.openide.DialogDisplayer.getDefault().notify (descriptor);
        }
        // notify user that everything is done
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(
            NbBundle.getBundle (NbTopManager.class).getString ("MSG_AllSaved"));
    }
    
    /** Interface describing basic control over window system. 
     * @since 1.15 */
    public interface WindowSystem {
        void init();
        void show();
        void hide();
        void load();
        void save();
        void clear();
    } // End of WindowSystem interface.
    
    public static boolean isModalDialogPresent() {
        return hasModalDialog(WindowManager.getDefault().getMainWindow())
            // XXX Trick to get the shared frame instance.
            || hasModalDialog(new JDialog().getOwner());
    }
    
    private static boolean hasModalDialog(Window w) {
        Window[] ws = w.getOwnedWindows();
        for(int i = 0; i < ws.length; i++) {
            if(ws[i] instanceof Dialog && ((Dialog)ws[i]).isModal()) {
                return true;
            } else if(hasModalDialog(ws[i])) {
                return true;
            }
        }
        
        return false;
    }

    private static class ExitActions implements Runnable {
        private final int type;
        ExitActions(int type) {
            this.type = type;
        }

        public void run() {
            switch (type) {
                case 0: 
                    doExit();
                    break;
                case 1:
                    org.netbeans.CLIHandler.stopServer();
                    final WindowSystem windowSystem = Lookup.getDefault().lookup(WindowSystem.class);
                    boolean gui = org.netbeans.core.startup.CLIOptions.isGui();
                    if (windowSystem != null && gui) {
                        windowSystem.hide();
                        windowSystem.save();
                    }
                    if (Boolean.getBoolean("netbeans.close.when.invisible")) {
                        // hook to permit perf testing of time to *apparently* shut down
                        TopSecurityManager.exit(0);
                    }
                    break;
                case 2:
                    if (!Boolean.getBoolean("netbeans.close.no.exit")) { // NOI18N
                        TopSecurityManager.exit(0);
                    }
                    break;
                default:
                    throw new IllegalStateException("Type: " + type); // NOI18N
            }
        }
    } // end of ExitActions
    
    private static boolean doingExit=false;
    private static final Runnable DO_EXIT = new ExitActions(0);
    public static void exit ( ) {
        // #37160 So there is avoided potential clash between hiding GUI in AWT
        // and accessing AWTTreeLock from saving routines (winsys).
        Mutex.EVENT.readAccess(DO_EXIT);
    }
    
    /**
     * @return True if the IDE is shutting down.
     */
    public static boolean isExiting() {
        return doingExit;
    }
    
    private static void doExit() {
        if (doingExit) {
            return ;
        }
        doingExit = true;
        // save all open files
        try {
            if ( System.getProperty ("netbeans.close") != null || ExitDialog.showDialog() ) {
                if (org.netbeans.core.startup.Main.getModuleSystem().shutDown(new ExitActions(1))) {
                    try {
                        try {
                            NbLoaderPool.store();
                        } catch (IOException ioe) {
                            Logger.getLogger(NbTopManager.class.getName()).log(Level.WARNING, null, ioe);
                        }
//#46940 -saving just once..                        
//                        // save window system, [PENDING] remove this after the winsys will
//                        // persist its state automaticaly
//                        if (windowSystem != null) {
//                            windowSystem.save();
//                        }
                        SessionManager.getDefault().close();
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable t) {
                        // Do not let problems here prevent system shutdown. The module
                        // system is down; the IDE cannot be used further.
                        Exceptions.printStackTrace(t);
                    }
                    // #37231 Someone (e.g. Jemmy) can install its own EventQueue and then
                    // exit is dispatched through that proprietary queue and it
                    // can be refused by security check. So, we need to replan
                    // to RequestProcessor to avoid security problems.
                    Task exitTask = new Task(new ExitActions(2));
                    RequestProcessor.getDefault().post(exitTask);
                    exitTask.waitFinished();
                }
            }
        } finally {
            doingExit = false; 
        }
    }

    /**
     * Default implementation of the lifecycle manager interface that knows
     * how to save all modified DataObject's, and to exit the IDE safely.
     */
    @ServiceProvider(service=LifecycleManager.class, supersedes="org.netbeans.core.startup.ModuleLifecycleManager")
    public static final class NbLifecycleManager extends LifecycleManager {
        /** Default constructor for lookup. */
        public NbLifecycleManager() {}
        public void saveAll() {
            NbTopManager.saveAll();
        }
        public void exit() {
            NbTopManager.exit();
        }
        public @Override void markForRestart() throws UnsupportedOperationException {
            new ModuleLifecycleManager().markForRestart();
        }
    }

    /** Get the module subsystem. */
    public abstract ModuleSystem getModuleSystem();
    
    public static Lookup getModuleLookup() {
        Lookup l = Lookup.getDefault();
        if (l instanceof MainLookup) {
            l = org.netbeans.core.startup.Main.getModuleSystem().getManager().getModuleLookup();
        }
        return l;
    }

    public static List<File> getModuleJars() {
        return org.netbeans.core.startup.Main.getModuleSystem().getModuleJars();
    }

    /**
     * Able to reuse HtmlBrowserComponent.
     */
    public static class NbBrowser {
        
        private HtmlBrowserComponent brComp;
        private PreferenceChangeListener idePCL;
        private static Lookup.Result factoryResult;
        
        static {            
            factoryResult = Lookup.getDefault().lookupResult(HtmlBrowser.Factory.class);
            factoryResult.allItems();
            factoryResult.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    ((NbURLDisplayer)org.openide.awt.HtmlBrowser.URLDisplayer.getDefault()).htmlViewer = null;
                }
            });                                    
        }
        
        public NbBrowser() {
            HtmlBrowser.Factory browser = IDESettings.getWWWBrowser();
            if (browser == null) {
                // Fallback.
                browser = new SwingBrowser();
            }
            // try if an internal browser is set and possibly try to reuse an 
            // existing component
            if (browser.createHtmlBrowserImpl().getComponent() != null) {
                brComp = findOpenedBrowserComponent();
            }
            if (brComp == null) {
                brComp = new HtmlBrowserComponent(browser, true, true);
                brComp.putClientProperty("TabPolicy", "HideWhenAlone"); // NOI18N
            }
            setListener();
        }

        /** 
         * Tries to find already opened <code>HtmlBrowserComponent</code>. In
         * the case of success returns the instance, null otherwise.         
         */
        private HtmlBrowserComponent findOpenedBrowserComponent() {
            for (Iterator it = WindowManager.getDefault().getModes().iterator(); it.hasNext(); ) {
                Mode m = (Mode) it.next();
                if ("editor".equals(m.getName())) { // NOI18N
                    TopComponent[] tcs = m.getTopComponents();
                    for (int i = 0; i < tcs.length; i++) {
                        if (tcs[i] instanceof HtmlBrowserComponent) {
                            return (HtmlBrowserComponent) tcs[i];
                        }
                    }
                    break;
                }
            }
            return null;
        }

        /** Show URL in browser
         * @param url URL to be shown
         */
        private void showUrl(URL url) {
            brComp.open();
            brComp.requestActive();
            brComp.setURL(url);
        }

        /**
         *  Sets listener that invalidates this as main IDE's browser if user changes the settings
         */
        private void setListener () {
            if (idePCL != null) {
                return;
            }
            try {                
                // listen on preffered browser change
                idePCL = new PreferenceChangeListener() {
                    public void preferenceChange(PreferenceChangeEvent evt) {
                        if (IDESettings.PROP_WWWBROWSER.equals(evt.getKey())) {
                            ((NbURLDisplayer) HtmlBrowser.URLDisplayer.getDefault()).htmlViewer = null;
                            if (idePCL != null) {
                                IDESettings.getPreferences().removePreferenceChangeListener(idePCL);
                                idePCL = null;
                                brComp = null;
                            }
                        }
                    }
                };
                IDESettings.getPreferences().addPreferenceChangeListener(idePCL);
            }
            catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
