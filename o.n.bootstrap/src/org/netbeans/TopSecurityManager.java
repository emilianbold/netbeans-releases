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

package org.netbeans;

import java.awt.AWTPermission;
import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.Permission;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/** NetBeans security manager implementation.
* @author Ales Novak, Jesse Glick
*/
public class TopSecurityManager extends SecurityManager {
    private static final boolean check = !Boolean.getBoolean("netbeans.security.nocheck"); // NOI18N
    private static final Logger LOG = Logger.getLogger(TopSecurityManager.class.getName());

    private Permission allPermission;
    
    /* JVMPI sometimes deadlocks sync getForeignClassLoader
        and Class.forName
    */
    private static final Class<?> classLoaderClass = ClassLoader.class;
    private static final Class URLClass = URL.class;
    private static final Class runtimePermissionClass = RuntimePermission.class;
    private static final Class accessControllerClass = AccessController.class;
    private static final Class awtPermissionClass = AWTPermission.class;
    private static SecurityManager fsSecManager;

    private static final List<SecurityManager> delegates = new ArrayList<SecurityManager>();
    /** Register a delegate security manager that can handle some checks for us.
     * Currently only checkExit and checkTopLevelWindow are supported.
     * @param sm the delegate to register
     * @throws SecurityException without RuntimePermission "TopSecurityManager.register"
     */
    public static void register(SecurityManager sm) throws SecurityException {
/*        if (check) {
            try {
                AccessController.checkPermission(new RuntimePermission("TopSecurityManager.register")); // NOI18N
            } catch (SecurityException se) {
                // Something is probably wrong; debug it better.
                ProtectionDomain pd = sm.getClass().getProtectionDomain();
                CodeSource cs = pd.getCodeSource();
                System.err.println("Code source of attempted secman: " + (cs != null ? cs.getLocation().toExternalForm() : "<none>")); // NOI18N
                System.err.println("Its permissions: " + pd); // NOI18N
                throw se;
            }
        }
*/
        synchronized (delegates) {
            if (delegates.contains(sm)) throw new SecurityException();
            delegates.add(sm);
            if (fsSecManager == null) {
                for (Lookup.Item<SecurityManager> item : Lookup.getDefault().lookupResult(SecurityManager.class).allItems()) {
                    if (item != null && "org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager".equals(item.getId())) {//NOI18N
                        fsSecManager = item.getInstance();
                        break;
                    }
                }
                assert fsSecManager != null;
            }            
        }
    }
    /** Unregister a delegate security manager.
     * @param sm the delegate to unregister
     * @throws SecurityException without RuntimePermission "TopSecurityManager.unregister"
     */
    public static void unregister(SecurityManager sm) throws SecurityException {
/*        if (check) {
            AccessController.checkPermission(new RuntimePermission("TopSecurityManager.unregister")); // NOI18N
        }
*/
        synchronized (delegates) {
            if (!delegates.contains(sm)) throw new SecurityException();
            delegates.remove(sm);
        }
    }

    /**
    * constructs new TopSecurityManager
    */
    public TopSecurityManager () {
        allPermission = new AllPermission();
    }

    public @Override void checkExit(int status) throws SecurityException {
        if (! check) {
            return;
        }
        
        synchronized (delegates) {
            Iterator it = delegates.iterator();
            while (it.hasNext()) {
                ((SecurityManager)it.next()).checkExit(status);
            }
        }
        
        PrivilegedCheck.checkExit(status, this);
    }

    SecurityManager getSecurityManager() {
        if (fsSecManager == null) {
            synchronized (delegates) {
                return fsSecManager;
            }        
        }
        return fsSecManager;
    }
    
    private void notifyDelete(String file) {
        SecurityManager s = getSecurityManager();
        if (s != null) {
            s.checkDelete(file);
        }
    }

    private void notifyWrite(String file) {
        SecurityManager s = getSecurityManager();
        if (s != null) {
            s.checkWrite(file);
        }
    }
    
    private static boolean officialExit = false;
    /** Can be called from core classes to exit the system.
     * Direct calls to System.exit will not be honored, for safety.
     * @param status the status code to exit with
     * @see "#20751"
     */
    public static void exit(int status) {
        officialExit = true;
        System.exit(status);
    }

    final void checkExitImpl(int status, AccessControlContext acc) throws SecurityException {             
        if (!officialExit) {
            throw new ExitSecurityException("Illegal attempt to exit early"); // NOI18N
        }

        super.checkExit(status);
    }

    public @Override boolean checkTopLevelWindow(Object window) {
        synchronized (delegates) {
            for (SecurityManager sm : delegates) {
                sm.checkTopLevelWindow(window);
            }
        }
        
        return super.checkTopLevelWindow(window);
    }

    /* XXX probably unnecessary:
    // Hack against permissions of Launcher$AppLoader.
    public void checkPackageAccess(String pckg) {
        if (pckg == null) return;
        if (pckg.startsWith("sun.")) { // NOI18N
            if (inClazz("sun.misc.Launcher") || inClazz("java.lang.Class")) { // NOI18N
                return;
            }
        }
        super.checkPackageAccess(pckg);
    }

    private boolean inClazz(String s) {
        Class[] classes = getClassContext();
        int i = 0;
        for (; (i < classes.length) && (classes[i] == TopSecurityManager.class); i++);
        if (i == classes.length) {
            return false;
        }
        return classes[i].getName().startsWith(s);
    }
     */

    /** Performance - all props accessible */
    public @Override final void checkPropertyAccess(String x) {
        if ("netbeans.debug.exceptions".equals(x)) { // NOI18N
            // Get rid of this old system property.
            Class[] ctxt = getClassContext();
            for (int i = 0; i < ctxt.length; i++) {
                Class c = ctxt[i];
                if (c != TopSecurityManager.class &&
                        c != System.class &&
                        c != Boolean.class) {
                    String n = c.getName();
                    synchronized (warnedClassesNDE) {
                        if (warnedClassesNDE.add(n)) {
                            System.err.println("Warning: use of system property netbeans.debug.exceptions in " + n + " has been obsoleted in favor of java.util.logging.Logger"); // NOI18N
                        }
                    }
                    break;
                }
            }
        }
        if ("netbeans.home".equals(x)) { // NOI18N
            // Get rid of this old system property.
            Class[] ctxt = getClassContext();
            for (int i = 0; i < ctxt.length; i++) {
                Class c = ctxt[i];
                if (c != TopSecurityManager.class &&
                        c != System.class &&
                        c != Boolean.class) {
                    String n = c.getName();
                    synchronized (warnedClassesNH) {
                        if (warnedClassesNH.add(n)) {
                            System.err.println("Warning: use of system property netbeans.home in " + n + " has been obsoleted in favor of InstalledFileLocator"); // NOI18N
                        }
                    }
                    break;
                }
            }
        }
    }
    private final Set<String> warnedClassesNDE = new HashSet<String>(25);
    private static final Set<String> warnedClassesNH = new HashSet<String>(25);
    static {
        warnedClassesNH.add("org.netbeans.MainImpl"); // NOI18N
        warnedClassesNH.add("org.netbeans.Stamps"); // NOI18N
        warnedClassesNH.add ("org.netbeans.core.LookupCache"); // NOI18N
        warnedClassesNH.add ("org.netbeans.updater.UpdateTracking"); // NOI18N
        warnedClassesNH.add("org.netbeans.core.ui.ProductInformationPanel"); // #47429; NOI18N
        warnedClassesNH.add("org.netbeans.lib.uihandler.LogFormatter");
        warnedClassesNH.add("org.netbeans.modules.j2ee.sun.ide.j2ee.PluginProperties"); // AS bundle is not in any cluster
    }

    /* ----------------- private methods ------------- */

    /**
     * The method is empty. This is not "secure", but on the other hand,
     * it reduces performance penalty of startup about 10%
     */
    public @Override void checkRead(String file) {
        // XXX reconsider!
    }
    
    public @Override void checkRead(FileDescriptor fd) {
    }

    
    public @Override void checkWrite(FileDescriptor fd) {
    }

    /** The method has awful performance in super class */
    public @Override void checkDelete(String file) {
        notifyDelete(file);
        try {
            checkPermission(allPermission);
            return;
        } catch (SecurityException e) {
            super.checkDelete(file);
        }
    }
           
    /** The method has awful performance in super class */
    public @Override void checkWrite(String file) {
        notifyWrite(file);
        try {
            checkPermission(allPermission);
            return;
        } catch (SecurityException e) {
            super.checkWrite(file);
        }
    }
    
    /** Checks connect */
    public @Override void checkConnect(String host, int port) {
        if (! check) {
            return;
        }
        
        try {
            checkPermission(allPermission);
            return;
        } catch (SecurityException e) {
        }
        
        try {
            super.checkConnect(host, port);
            return;
        } catch (SecurityException e) {
        }
        
        PrivilegedCheck.checkConnect(host, port, this);
    }
     
    final void checkConnectImpl(String host, int port) {
        Class insecure = getInsecureClass();
        if (insecure != null) {  
            URL ctx = getClassURL(insecure);
            if (ctx != null) {
                try {
                    String fromHost = ctx.getHost();
                    InetAddress ia2 = InetAddress.getByName(host);
                    InetAddress ia3 = InetAddress.getByName(fromHost);
                    if (ia2.equals(ia3)) {
                        return;
                    }
                } catch (UnknownHostException e) { // ignore
                    e.printStackTrace();
                }
            }
            throw new SecurityException();
        }
    }

    public @Override void checkConnect(String s, int port, Object context) {
        checkConnect(s, port);
    }

    public @Override void checkPermission(Permission perm) {
        checkSetSecurityManager(perm);
        
        //
        // part of makeSwingUseSpecialClipboard that makes it work on
        // JDK 1.5
        //
        if (awtPermissionClass.isInstance(perm)) {
            if ("accessClipboard".equals (perm.getName ())) { // NOI18N
                ThreadLocal<Object> t;
                synchronized (TopSecurityManager.class) {
                    t = CLIPBOARD_FORBIDDEN;
                }
                if (t == null) {
                    return;
                }
                
                if (t.get () != null) {
                    t.set (this);
                    throw new SecurityException ();
                } else {
                    checkWhetherAccessedFromSwingTransfer ();
                }
            }
        }
        return;
    }
    
    public @Override void checkPermission(Permission perm, Object context) {
        checkSetSecurityManager(perm);
        return;
    }
    
    public static void install() {
        try {
            System.setSecurityManager(new TopSecurityManager());
        } catch (SecurityException ex) {
            LOG.log(Level.WARNING, "Cannot associated own security manager"); // NOI18N
            LOG.log(Level.INFO, "Cannot associated own security manager", ex); // NOI18N
        }
    }
    static void uninstall() {
        System.setSecurityManager(null);
    }
    
    /** Prohibits to set another SecurityManager */
    private void checkSetSecurityManager(Permission perm) {
        if (runtimePermissionClass.isInstance(perm)) {
            if (perm.getName().equals("setSecurityManager")) { // NOI18N - hardcoded in java.lang
                Class[] arr = getClassContext();
                boolean seenJava = false;
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i].getName().equals("org.netbeans.TopSecurityManager")) { // NOI18N
                        if (seenJava) {
                            // if the change of security manager is called from my own
                            // class or the class loaded by other classloader, then it is likely ok
                            return;
                        } else {
                            continue;
                        }
                    }
                    if (arr[i] != System.class) {
                        // if there is a non-java class on stack, skip and throw exception
                        break;
                    }
                    seenJava = true;
                }
                throw new SecurityException();
            }
        }
    }

//    
//    public void checkMemberAccess(Class clazz, int which) {
//        if ((which == java.lang.reflect.Member.PUBLIC) ||
//                javax.swing.text.JTextComponent.class.isAssignableFrom(clazz)) {
//            return;
//        } else {
//            super.checkMemberAccess(clazz, which);
//        }
//    }
//
    private Class getInsecureClass() {

        Class[] ctx = getClassContext();
        boolean firstACClass = false;

LOOP:   for (int i = 0; i < ctx.length; i++) {

            if (ctx[i] == accessControllerClass) {
                // privileged action is on the stack before an untrusted class loader
                // #3950
                if (firstACClass) {
                    return null;
                } else {
                    firstACClass = true;
                    continue LOOP;
                }
            } else if (ctx[i].getClassLoader() != null) {

                if (isSecureClass(ctx[i])) {
                    if (classLoaderClass.isAssignableFrom(ctx[i])) {
                        return null;
                    } else {
                        // OK process next one
                        continue LOOP;
                    }
                }

                return ctx[i];
            } else if (classLoaderClass.isAssignableFrom(ctx[i])) { // cloader == null
                return null; // foreign classloader wants to do work...
            }
        }

        return null;
    }

    /** Checks if the class is loaded through the nbfs URL */
    static boolean isSecureClass(final Class clazz) {
        URL source = getClassURL(clazz);
        if (source != null) {
            return isSecureProtocol(source.getProtocol());
        } else {
            return true;
        }
    }
    
    /** @return a protocol through which was the class loaded (file://...) or null
    */
    static URL getClassURL(Class clazz) {
        java.security.CodeSource cs = clazz.getProtectionDomain().getCodeSource();                                                     
        if (cs != null) {
            URL url = cs.getLocation();
            return url;
        } else { // PROXY CLASS?
            return null;
        }
    }

    static Field getUrlField(Class clazz) {
        if (urlField == null) {
            try {
                Field[] fds = clazz.getDeclaredFields();
                for (int i = 0; i < fds.length; i++) {
                    if (fds[i].getType() == URLClass) {
                        fds[i].setAccessible(true);
                        urlField = fds[i];
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return urlField;
    }

    private static Field urlField;

    /** @return Boolean.TRUE iff the string is a safe protocol (file, nbfs, ...) */
    static boolean isSecureProtocol(String protocol) {
        if (protocol.equals("http") || // NOI18N
            protocol.equals("ftp") || // NOI18N
            protocol.equals("rmi")) { // NOI18N
            return false;
        } else {
            return true;
        }
    }

    // Workaround for bug 
    // 
    // http://developer.java.sun.com/developer/bugParade/bugs/4818143.html
    //
    // sun.awt.datatransfer.ClipboardTransferable.getClipboardData() can hang
    // for very long time (maxlong == eternity).  We tries to avoid the hang by
    // access the system clipboard from a separate thread.  If the hang happens
    // the thread will wait for the system clipboard forever but not the whole
    // IDE.  See also NbClipboard
    
    private static ThreadLocal<Object> CLIPBOARD_FORBIDDEN;
    
    /** Convinces Swing components that they should use special clipboard
     * and not Toolkit.getSystemClipboard.
     *
     * @param clip clipboard to use
     */
    public static void makeSwingUseSpecialClipboard (java.awt.datatransfer.Clipboard clip) {
        try {
            synchronized (TopSecurityManager.class) {
                if (! (System.getSecurityManager() instanceof TopSecurityManager)) {
                    LOG.warning("Our manager has to be active: " + System.getSecurityManager());
                    return;
                } // NOI18N
                if (CLIPBOARD_FORBIDDEN != null) {
                    return;
                }
                CLIPBOARD_FORBIDDEN = new ThreadLocal<Object>();
                CLIPBOARD_FORBIDDEN.set (clip);
            }
            
            javax.swing.JComponent source = new javax.swing.JPanel ();
            javax.swing.TransferHandler.getPasteAction ().actionPerformed (
                new java.awt.event.ActionEvent (source, 0, "")
            );
            javax.swing.TransferHandler.getCopyAction ().actionPerformed (
                new java.awt.event.ActionEvent (source, 0, "")
            );
            javax.swing.TransferHandler.getCutAction ().actionPerformed (
                new java.awt.event.ActionEvent (source, 0, "")
            );
            Object forb = CLIPBOARD_FORBIDDEN.get ();
            CLIPBOARD_FORBIDDEN.set(null);
            if (! (forb instanceof TopSecurityManager) ) {
                System.err.println("Cannot install our clipboard to swing components, TopSecurityManager is not the security manager: " + forb); // NOI18N
                return;
            }

            Class<?> appContextClass = Class.forName ("sun.awt.AppContext"); // NOI18N
            Method getAppContext = appContextClass.getMethod ("getAppContext"); // NOI18N
            Object appContext = getAppContext.invoke (null, new Object[0]);
            
            Class actionClass = javax.swing.TransferHandler.getCopyAction ().getClass ();
            java.lang.reflect.Field sandboxKeyField = actionClass.getDeclaredField ("SandboxClipboardKey"); // NOI18N
            sandboxKeyField.setAccessible (true);
            Object value = sandboxKeyField.get (null);
            
            Method put = appContextClass.getMethod ("put", Object.class, Object.class); // NOI18N
            put.invoke (appContext, new Object[] { value, clip });
        } catch (ThreadDeath ex) {
            throw ex;
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (CLIPBOARD_FORBIDDEN != null) {
                CLIPBOARD_FORBIDDEN.set (null);
            }
        }
    }
    
    /** the class that needs to be non accessible */
    private static Class transferHandlerTransferAction;
    /** Throws exception if accessed from javax.swing.TransferHandler class
     */
    private void checkWhetherAccessedFromSwingTransfer () throws SecurityException {
        if (transferHandlerTransferAction == null) {
            try {
                transferHandlerTransferAction = Class.forName ("javax.swing.TransferHandler$TransferAction"); // NOI18N
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                throw new SecurityException (ex.getMessage ());
            }
        }
        Class[] arr = getClassContext ();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == transferHandlerTransferAction) {
                throw new SecurityException ("All swing access to clipboard should be redirected to ExClipboard"); // NOI18N
            }
        }
    }


    private static final class PrivilegedCheck implements PrivilegedExceptionAction<Object> {
        int action;
        TopSecurityManager tsm;
        
        // exit
        int status;
        AccessControlContext acc;

        // connect
        String host;
        int port;
        
        
        public PrivilegedCheck(int action, TopSecurityManager tsm) {
            this.action = action;
            this.tsm = tsm;
            
            if (action == 0) {
                acc = AccessController.getContext();
            }
        }
        
        public Object run() throws Exception {
            switch (action) {
                case 0 : 
                    tsm.checkExitImpl(status, acc);
                    break;
                case 1 :
                    tsm.checkConnectImpl(host, port);
                    break;
                default :
            }
            return null;
        }
        
        static void checkExit(int status, TopSecurityManager tsm) {
            PrivilegedCheck pea = new PrivilegedCheck(0, tsm);
            pea.status = status;
            check(pea);
        }
        
        static void checkConnect(String host, int port, TopSecurityManager tsm) {
            PrivilegedCheck pea = new PrivilegedCheck(1, tsm);
            pea.host = host;
            pea.port = port;
            check(pea);
        }
        
        private static void check(PrivilegedCheck action) {
            try {
                AccessController.doPrivileged(action);
            } catch (PrivilegedActionException e) {
                Exception orig = e.getException();
                if (orig instanceof RuntimeException) {
                    throw ((RuntimeException) orig);
                }
                orig.printStackTrace();
            }
        }
    }
    
}
