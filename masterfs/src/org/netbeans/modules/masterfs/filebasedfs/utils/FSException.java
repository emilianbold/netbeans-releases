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

package org.netbeans.modules.masterfs.filebasedfs.utils;

import java.util.MissingResourceException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;

import java.io.IOException;

/**
 * Copy/pasted from org.openide.filesystems
 * <p/>
 * Localized IOException for filesystems.
 *
 * @author Jaroslav Tulach
 */
public final class FSException extends IOException {
    /** name of resource to use for localized message */
    //  private String resource;
    /**
     * arguments to pass to the resource
     */
    private final Object[] args;

    /**
     * Creates new FSException.
     */
    private FSException(final String resource, final Object[] args) {
        super(resource);
        this.args = args;
    }

    /**
     * Message should be meaning full, but different from localized one.
     */
    public String getMessage() {
        return " " + getLocalizedMessage(); // NOI18N
    }

    /**
     * Localized message.
     */
    public String getLocalizedMessage() {
        final String res = super.getMessage();
        /*This call to getBundle should ensure that currentClassLoader is not used to load resources from. 
         This should prevent from deadlock, that occured: one waits for FileObject and has resource, 
         second one waits for resource and has FileObject*/
        String format = null;
        try{
            format = NbBundle.getBundle("org.netbeans.modules.masterfs.filebasedfs.Bundle", java.util.Locale.getDefault(), FileBasedFileSystem.class.getClassLoader()).getString(res);//NOI18N                        
        } catch (MissingResourceException mex) {
            if (format == null) {
                NbBundle.getBundle("org.openide.filesystems.Bundle", java.util.Locale.getDefault(), FileSystem.class.getClassLoader()).getString(res);//NOI18N    
            }
        }
                
        if (args != null) {
            return java.text.MessageFormat.format(format, args);
        } else {
            return format;
        }
    }

    /**
     * Creates the localized exception.
     *
     * @param resource to take localization string from
     * @throws the exception
     */
    public static void io(final String resource) throws IOException {
        final FSException fsExc = new FSException(resource, null);
        ErrorManager.getDefault().annotate(fsExc, ErrorManager.WARNING, null,
                fsExc.getLocalizedMessage(), null, null);
        throw fsExc;
    }

    public static void io(final String resource, final Object[] args) throws IOException {
        final FSException fsExc = new FSException(resource, args);
        ErrorManager.getDefault().annotate(fsExc, ErrorManager.WARNING, null,
                fsExc.getLocalizedMessage(), null, null);
        throw fsExc;
    }

    public static void io(final String resource, final Object arg1) throws IOException {
        final FSException fsExc = new FSException(resource, new Object[]{arg1});
        ErrorManager.getDefault().annotate(fsExc, ErrorManager.WARNING, null,
                fsExc.getLocalizedMessage(), null, null);
        throw fsExc;
    }

    public static void io(final String resource, final Object arg1, final Object arg2) throws IOException {
        final FSException fsExc = new FSException(resource, new Object[]{arg1, arg2});
        ErrorManager.getDefault().annotate(fsExc, ErrorManager.WARNING, null,
                fsExc.getLocalizedMessage(), null, null);
        throw fsExc;
    }

    public static void io(final String resource, final Object arg1, final Object arg2, final Object arg3) throws IOException {
        final FSException fsExc = new FSException(resource, new Object[]{arg1, arg2, arg3});
        ErrorManager.getDefault().annotate(fsExc, ErrorManager.WARNING, null,
                fsExc.getLocalizedMessage(), null, null);
        throw fsExc;
    }

    public static void io(final String resource, final Object arg1, final Object arg2, final Object arg3, final Object arg4) throws IOException {
        final FSException fsExc = new FSException(resource, new Object[]{arg1, arg2, arg3, arg4});
        ErrorManager.getDefault().annotate(fsExc, ErrorManager.WARNING, null,
                fsExc.getLocalizedMessage(), null, null);
        throw fsExc;
    }

    public static void annotateException(final Throwable t) {
        ErrorManager.getDefault().annotate(t, ErrorManager.WARNING, null,
                t.getLocalizedMessage(), null, null);
    }
}
