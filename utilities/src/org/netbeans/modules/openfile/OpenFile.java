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
import java.io.IOException;
import java.net.InetAddress;
import org.openide.DialogDisplayer;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListener;


/**
 * Opens files when requested. Main functionality.
 *
 * @author Jaroslav Tulach, Jesse Glick
 * @author Marian Petras
 */
public final class OpenFile {

    /** holds an instance of this class */
    private static final OpenFile instance = new OpenFile();
    
    /** holds (the only) instance of the implementation manager */
    private final ImplManager implManager;
    
    /**
     * Opens the specified file.
     *
     * @param  fileName  name of file to open
     * @usecase  API
     */
    public static void open(final String fileName) {
        RequestProcessor.getDefault().post(
                new Runnable() {
                    public void run() {
                        getDefault().openFile(fileName);
                    }
                },
                10000); //XXX  Waiting for IDE initialization - see issue #23341
    }
    
    /**
     * @usecase  API
     */
    public static void open(FileObject fileObject) {
        getDefault().openFile(fileObject);
    }
    
    /**
     * Returns an instance of this class.
     *
     * @return  instance of this class
     */
    static OpenFile getDefault() {
        return instance;
    }
    
    /**
     * Creates a new instance and initializes the
     * {@linkplain ImplManager implementation manager}.
     */
    private OpenFile() {
        implManager = new ImplManager();
    }
    
    /**
     * Opens the specified file.
     *
     * @param  fileName  name of file to open
     * @usecase  API
     */
    public void openFile(String fileName) {
        openFile(fileName, -1);
    }
    
    /**
     * Opens the specified file at the specified line
     *
     * @param  fileName  name of file to open
     * @param  line  line number to move a cursor to within the file
     * @usecase  API
     */
    public void openFile(final String fileName, final int line) {
        openFile(new File(fileName),
                 false,                 //wait?
                 (InetAddress) null,    //host
                 -1,                    //port
                 -1);                   //line
    }
    
    /**
     *
     * @usecase  API
     */
    public void openFile(FileObject fileObject) {
        openFile(fileObject, -1);
    }
    
    /**
     *
     * @usecase  API
     */
    public void openFile(FileObject fileObject, int line) {
        implManager.getImpl().open(fileObject,
                                   (String) null,
                                   false,
                                   (InetAddress) null,
                                   -1,
                                   line);
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
     * @usecase  API
     * @usecase  OpenFileServer
     */
    void openFile(File file,
                  boolean wait,
                  InetAddress address,
                  int port,
                  int line) {
        try {
            file = file.getCanonicalFile();
        } catch (IOException ex) {
            /* failed - never mind, continue... */
        }
        
        if (!checkFileExists(file)) {
            return;
        }
                              
        FileObject fileObject;
        OpenFileImpl impl = implManager.getImpl();
        if ((fileObject = impl.findFileObject(file)) != null) {
            impl.open(fileObject,
                      (String) null,
                      wait,
                      address,
                      port,
                      line);
        }
    }
    
    /**
     * Checks whether the specified file exists.
     * If the file doesn't exists, displays a message.
     * <p>
     * The code for displaying the message is running in a separate thread
     * so that it does not block the current thread.
     *
     * @param  file  file to check for existence
     * @return  <code>true</code> if the file exists and is a plain file,
     *          <code>false</code> otherwise
     */
    private boolean checkFileExists(File file) {
        if (file.exists() && file.isFile()) {
            return true;
        }
        
        final String fileName = file.toString();
        final String msg = NbBundle.getMessage(OpenFileImpl.class,
                                               "MSG_fileNotFound",      //NOI18N
                                               fileName);
        new Thread(new Runnable() {
                public void run() {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(msg));
                }
            }).start();
        return false;
    }
    
    /**
     * Manages changes of Open file implementations.
     * It listens for changes of Open file implementations (listens on a lookup
     * result) and ensures that its method {@link #getImpl()} always returns
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
