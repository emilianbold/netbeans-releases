/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.openfile;

import java.io.File;
import java.net.InetAddress;

import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListener;


/**
 * Opens files when requested. Main functionality.
 *
 * @author Jaroslav Tulach, Jesse Glick
 * @author Marian Petras
 */
public class OpenFile extends Object {

    /** holds an instance of this class */
    private static final OpenFile instance = new OpenFile();
    
    /**
     * Returns an instance of this class.
     *
     * @return  instance of this class; subsequent invocations may return
     *          different objects
     * @see  #instanceRef
     */
    private static OpenFile getInstance() {
        return instance;
    }
    
    /**
     * Opens the file either by calling {@link OpenCookie} ({@link ViewCookie}),
     * or by showing it in the Explorer.
     * Uses {@link #find} to figure out what the right file object is.
     *
     * @param fileName file name to open
     */
    public static void open(final String fileName) {
        ErrorManager em = ErrorManager.getDefault().getInstance(
                                  "org.netbeans.modules.openfile");     //NOI18N
        em.log("OpenFile.open: " + fileName);                           //NOI18N
        
        getInstance().openFile(fileName);
    }

    /**
     * Opens the specified file.
     *
     * @param  file  file to open (must exist)
     * @param  wait  whether to wait until requested to return a status
     * @param  address address to send reply to, valid only if wait set
     * @param  port  port to send reply to, valid only if wait set
     * @param  line  line number to try to open to (starting at zero),
     *               or <code>-1</code> to ignore
     */
    public static void open(File file,
                            boolean wait,
                            InetAddress address,
                            int port,
                            int line) {
        ErrorManager em = ErrorManager.getDefault().getInstance(
                                  "org.netbeans.modules.openfile");     //NOI18N
        em.log("OpenFile.open: " + file);                               //NOI18N
        getInstance().openFile(file, wait, address, port, line);
    }
    
    /** holds (the only) instance of the implementation manager */
    private final ImplManager implManager;
    
    /**
     * Creates a new instance and initializes the
     * {@linkplain ImplManager implementation manager}.
     */
    private OpenFile() {
        implManager = new ImplManager();
    }
    
    /**
     * Opens a file.
     *
     * @param  fileName  full path of a file to open
     */
    private void openFile(final String fileName) {
        final File f = new File(fileName);
        RequestProcessor.getDefault().post(
                new Runnable() {
                    public void run() {
                        implManager.getImpl().open(f, false, null, -1, -1);
                    }
                },
                10000); //!!! Waiting for IDE initialization
    }

    /**
     * Opens a file.
     *
     * @param  file  file to open (must exist)
     * @param  wait  whether to wait until requested to return a status
     * @param  address address to send reply to, valid only if wait set
     * @param  port  port to send reply to, valid only if wait set
     * @param  line  line number to try to open to (starting at zero),
     *               or <code>-1</code> to ignore
     */
    private void openFile(File file,
                          boolean wait,
                          InetAddress address,
                          int port,
                          int line) {
        implManager.getImpl().open(file, wait, address, port, line);
    }

    /**
     * Manages changes of Open file implementations.
     * It listens for changes of Open file implementations (listens on a lookup
     * result) and ensures that its method {@link getImpl()} always returns
     * the current implementation.
     */
    static final class ImplManager implements LookupListener {
        
        /**
         * holds the current implementation of Open File functionality;
         * or <code>null</code> if it needs to be updated
         *
         * @see  #getImpl()
         */
        private OpenFileImpl impl;
        /** holds result of query about the current Open File implementation */
        private final Lookup.Result lookupResult;
        
        /** Creates an instance of this class. */
        ImplManager() {
            Lookup.Template template = new Lookup.Template(OpenFileImpl.class);
            lookupResult = Lookup.getDefault().lookup(template);
            lookupResult.addLookupListener(
                (LookupListener) WeakListener.create(LookupListener.class,
                                                     this,
                                                     lookupResult));
        }
        
        /**
         * Called when a set of registered Open File implementation changes.
         * <p>
         * This method is public only as an implementation side-effect.
         */
        public synchronized void resultChanged(LookupEvent e) {
            impl = null;
        }
        
        /**
         * Returns the current implementation of Open File functionality.
         * If neccessary, asks lookup for the current implementation.
         *
         * @return  up-to-date implementation of Open File functionality
         * @exception  java.lang.IllegalStateException
         *             if no implementation is available
         */
        synchronized OpenFileImpl getImpl() {
            if (impl == null) {
                impl = (OpenFileImpl)
                       Lookup.getDefault().lookup(OpenFileImpl.class);
                if (impl == null) {
                    throw new IllegalStateException();
                }
            }
            return impl;
        }
        
    }
    
}
