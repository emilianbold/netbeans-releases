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

package org.netbeans.modules.openfile;

import java.io.File;
import org.netbeans.modules.openfile.cli.Callback;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Opens files when requested. Main functionality.
 * @author Jaroslav Tulach, Jesse Glick
 * @author Marian Petras
 */
public final class OpenFile {

    /** do not instantiate */
    private OpenFile() {}
    
    private static OpenFileImpl getImpl() {
        return (OpenFileImpl)Lookup.getDefault().lookup(OpenFileImpl.class);
    }
    
    /**
     * Opens the specified file.
     *
     * @param  fileName  name of file to open
     * @usecase  API
     */
    public static void open(String fileName) {
        openFile(new File(fileName), -1, null);
    }
    
    /**
     * Open a file (object) at the beginning.
     * @param fileObject the file to open
     * @usecase  API
     */
    public static void open(FileObject fileObject) {
        getImpl().open(fileObject, -1, null);
    }
    
    /**
     * Opens a file.
     *
     * @param  file  file to open (must exist)
     * @param  line  line number to try to open to (starting at zero),
     *               or <code>-1</code> to ignore
     * @param waiter double-callback or null
     * @return true on success, false on failure
     * @usecase CallbackImpl, OpenFileAction
     */
    static boolean openFile(File file, int line, Callback.Waiter waiter) {
        if (!checkFileExists(file)) {
            return false;
        }
                              
        FileObject fileObject;
        OpenFileImpl impl = getImpl();
        if ((fileObject = impl.findFileObject(file)) != null) {
            return impl.open(fileObject, line, waiter);
        } else {
            return false;
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
    private static boolean checkFileExists(File file) {
        final String errMsgKey;
        if (!file.exists()) {
            errMsgKey = "MSG_fileNotFound";                             //NOI18N
        } else if (isSpecifiedByUNCPath(file)) {
            errMsgKey = "MSG_UncNotSupported";                          //NOI18N
        } else if (!file.isFile()) {
            errMsgKey = "MSG_fileNotFound";                             //NOI18N
        } else {
            return true;
        }
        
        final String fileName = file.toString();
        final String msg = NbBundle.getMessage(OpenFileImpl.class,
                                               errMsgKey,
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
     * Checks whether a given file is specified by an UNC path.
     *
     * @param  file  existing file to check
     * @return  <code>true</code> if the file is specified by UNC path;
     *          <code>false</code> otherwise
     */
    static boolean isSpecifiedByUNCPath(File file) {
        assert file != null && file.exists();

        file = FileUtil.normalizeFile(file);
        return file.getPath().startsWith("\\\\");                       //NOI18N
    }
    
}
