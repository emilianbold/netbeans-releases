/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.*;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** NetBeans security manager implementation.
* @author Ales Novak, Jesse Glick
*/
public class TopSecurityManager extends SecurityManager {

    private static boolean check = !Boolean.getBoolean("netbeans.security.nocheck"); // NOI18N

    private Permission allPermission;
    
    /* JVMPI sometimes deadlocks sync getForeignClassLoader
        and Class.forName
    */
    private static final Class classLoaderClass = ClassLoader.class;
    private static final Class URLClass = URL.class;
    private static final Class runtimePermissionClass = RuntimePermission.class;
    private static final Class accessControllerClass = AccessController.class;

    private static List delegates = new ArrayList(); // List<SecurityManager>
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

    public void checkExit(int status) throws SecurityException {
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

    public boolean checkTopLevelWindow(Object window) {
        synchronized (delegates) {
            Iterator it = delegates.iterator();
            while (it.hasNext()) {
                ((SecurityManager)it.next()).checkTopLevelWindow(window);
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
    public final void checkPropertyAccess(String x) {
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
                            System.err.println("Warning: use of system property netbeans.debug.exceptions in " + n + " has been obsoleted in favor of ErrorManager"); // NOI18N
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
        
        if ("javax.xml.parsers.SAXParserFactory".equals(x)) {
            if (Thread.currentThread().getContextClassLoader().getResource(
                    "org/netbeans/core/startup/SAXFactoryImpl.class") != null) return;
            throw new SecurityException ("");            
        }
        
        if ("javax.xml.parsers.DocumentBuilderFactory".equals(x)) {
            if (Thread.currentThread().getContextClassLoader().getResource(
                    "org/netbeans/core/startup/DOMFactoryImpl.class") != null) return;
            throw new SecurityException ("");            
        }
        
        return;
    }
    private final Set warnedClassesNDE = new HashSet(25); // Set<String>
    private static final Set warnedClassesNH = new HashSet(25); // Set<String>
    static {
        warnedClassesNH.add ("org.netbeans.core.LookupCache"); // NOI18N
        warnedClassesNH.add ("org.netbeans.updater.UpdateTracking"); // NOI18N
        warnedClassesNH.add("org.netbeans.core.ui.ProductInformationPanel"); // #47429; NOI18N
    }

    /* ----------------- private methods ------------- */

    /**
     * The method is empty. This is not "secure", but on the other hand,
     * it reduces performance penalty of startup about 10%
     */
    public void checkRead(String file) {
        // XXX reconsider!
    }
    
    public void checkRead(FileDescriptor fd) {
    }

    public void checkWrite(FileDescriptor fd) {
    }

    /** The method has awful performance in super class */
    public void checkDelete(String file) {
        try {
            checkPermission(allPermission);
            return;
        } catch (SecurityException e) {
            super.checkDelete(file);
        }
    }
    /** The method has awful performance in super class */
    public void checkWrite(String file) {
        try {
            checkPermission(allPermission);
            return;
        } catch (SecurityException e) {
            super.checkWrite(file);
        }
    }
    
    /** Checks connect */
    public void checkConnect(String host, int port) {
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

    public void checkConnect(String s, int port, Object context) {
        checkConnect(s, port);
    }

    public void checkPermission(Permission perm) {
        checkSetSecurityManager(perm);
        
        //
        // part of makeSwingUseSpecialClipboard that makes it work on
        // JDK 1.5
        //
        if (perm instanceof java.awt.AWTPermission) {
            if ("accessClipboard".equals (perm.getName ())) { // NOI18N
                if (CLIPBOARD_FORBIDDEN.get () != null) {
                    CLIPBOARD_FORBIDDEN.set (this);
                    throw new SecurityException ();
                } else {
                    checkWhetherAccessedFromSwingTransfer ();
                }
            }
        }
        return;
    }
    
    public void checkPermission(Permission perm, Object context) {
        checkSetSecurityManager(perm);
        return;
    }
    
    /** Prohibits to set another SecurityManager */
    private static void checkSetSecurityManager(Permission perm) {
        if (runtimePermissionClass.isInstance(perm)) {
            if ("setSecurityManager".equals(perm.getName())) { // NOI18N - hardcoded in java.lang
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
        ClassLoader cloader;
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
            } else if ((cloader = ctx[i].getClassLoader()) != null) {
                final Class hisClass = cloader.getClass();

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
    
    private static ThreadLocal CLIPBOARD_FORBIDDEN = new ThreadLocal ();
    
    /** Convinces Swing components that they should use special clipboard
     * and not Toolkit.getSystemClipboard.
     *
     * @param clip clipboard to use
     */
    public static void makeSwingUseSpecialClipboard (java.awt.datatransfer.Clipboard clip) {
        try {
            javax.swing.JComponent source = new javax.swing.JPanel ();
            CLIPBOARD_FORBIDDEN.set (clip);
            javax.swing.TransferHandler.getPasteAction ().actionPerformed (
                new java.awt.event.ActionEvent (source, 0, "")
            );
            javax.swing.TransferHandler.getCopyAction ().actionPerformed (
                new java.awt.event.ActionEvent (source, 0, "")
            );
            javax.swing.TransferHandler.getCutAction ().actionPerformed (
                new java.awt.event.ActionEvent (source, 0, "")
            );
            if (! (CLIPBOARD_FORBIDDEN.get () instanceof TopSecurityManager) ) {
                System.err.println("Cannot install our clipboard to swing components, TopSecurityManager is not the security manager"); // NOI18N
                return;
            }

            Class appContextClass = Class.forName ("sun.awt.AppContext"); // NOI18N
            Method getAppContext = appContextClass.getMethod ("getAppContext", new Class[0]); // NOI18N
            Object appContext = getAppContext.invoke (null, new Object[0]);
            Class c = appContext.getClass();
            
            Class actionClass = javax.swing.TransferHandler.getCopyAction ().getClass ();
            java.lang.reflect.Field sandboxKeyField = actionClass.getDeclaredField ("SandboxClipboardKey"); // NOI18N
            sandboxKeyField.setAccessible (true);
            Object value = sandboxKeyField.get (null);
            
            Method put = appContextClass.getMethod ("put", new Class[] { Object.class, Object.class }); // NOI18N
            put.invoke (appContext, new Object[] { value, clip });
        } catch (ThreadDeath ex) {
            throw ex;
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            CLIPBOARD_FORBIDDEN.set (null);
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


    private static final class PrivilegedCheck implements PrivilegedExceptionAction {
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
