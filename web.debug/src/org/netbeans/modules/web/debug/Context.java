/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Micro//S ystems, Inc. Portions Copyright 1997-2004 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.debug;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.beans.PropertyChangeListener;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.api.debugger.jpda.*;
import org.netbeans.spi.debugger.jpda.*;

import org.netbeans.modules.web.debug.breakpoints.JspLineBreakpoint;

/**
 *
 * @author Martin Grebac
 */
public class Context {

    public static final String LINE = "line";

    private static ContextProvider context;
    
    private static ContextProvider getContext () {
        if (context == null) {
            List l = DebuggerManager.getDebuggerManager().lookup(ContextProvider.class);
            context = (ContextProvider) l.get (0);
        }
        return context;
    }

    
    // ContextProvider methods .................................................
    
    /**
     * Shows source with given url on given line number.
     *
     * @param url a url of source to be shown
     * @param lineNumber a number of line to be shown
     */
    public static boolean showSource (
        String url,
        int lineNumber
    ) {
        return getContext ().showSource (url, lineNumber);
    }

    /**
     * Adds annotation to given url on given line.
     *
     * @param url a url of source annotation should be set into
     * @param lineNumber a number of line annotation should be set into
     * @param annotationType a type of annotation to be set
     *
     * @return annotation
     */
    public static Object annotate (
        String url,
        int lineNumber,
        String annotationType
    ) {
        return getContext ().annotate (url, lineNumber, annotationType);
    }

    /**
     * Removes given annotation.
     *
     * @return true if annotation has been successfully removed
     */
    public static boolean removeAnnotation (
        Object annotation
    ) {
        return getContext ().removeAnnotation (annotation);
    }
    
    /**
     * Returns number of line currently selected in editor or <code>null</code>.
     *
     * @return number of line currently selected in editor or <code>0</code>
     */
    public static int getCurrentLineNumber () {
        return getContext ().getCurrentLineNumber ();
    }

//    /**
//     * Returns name of class currently selected in editor or <code>null</code>.
//     *
//     * @return name of class currently selected in editor or <code>null</code>
//     */
//    public static String getCurrentClassName () {
//        return getContext ().getCurrentClassName ();
//    }

    /**
     * Returns URL of source currently selected in editor or <code>null</code>.
     *
     * @return URL of source currently selected in editor or <code>null</code>
     */
    public static String getCurrentURL () {
        return getContext ().getCurrentURL ();
    }

//    /**
//     * Returns name of method currently selected in editor or <code>null</code>.
//     *
//     * @return name of method currently selected in editor or <code>null</code>
//     */
//    public static String getCurrentMethodName () {
//        return getContext ().getCurrentMethodName ();
//    }
//
//    /**
//     * Returns name of field currently selected in editor or <code>null</code>.
//     *
//     * @return name of field currently selected in editor or <code>null</code>
//     */
//    public static String getCurrentFieldName () {
//        return getContext ().getCurrentFieldName ();
//    }

//    /**
//     * Returns identifier currently selected in editor or <code>null</code>.
//     *
//     * @return identifier currently selected in editor or <code>null</code>
//     */
//    public static String getSelectedIdentifier () {
//        return getContext ().getSelectedIdentifier ();
//    }

    public static void addPropertyChangeListener (PropertyChangeListener l) {
        getContext ().addPropertyChangeListener (l);
    }

    public static void removePropertyChangeListener (PropertyChangeListener l) {
        getContext ().removePropertyChangeListener (l);
    }    
    
    // utility methods .........................................................

    public static String getFileName (JspLineBreakpoint b) { 
        try {
            return new File(new URL(b.getURL()).getFile ()).getName ();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static boolean showSource(JspLineBreakpoint b) {
        if (b.getLineNumber () < 1)
            return Context.showSource (
                b.getURL (),
                1
            );
        return Context.showSource (
            b.getURL (),
            b.getLineNumber ()
        );
    }

    public static String getDefaultType() {
        return LINE;
    }

    public static Object annotate(JspLineBreakpoint b) {
        String url = b.getURL ();
        int lineNumber = b.getLineNumber ();
        if (lineNumber < 1) return null;
        String condition = b.getCondition ();
        boolean isConditional = (condition != null) &&
            !condition.trim ().equals (""); // NOI18N
        String annotationType = b.isEnabled () ?
            (isConditional ? ContextProvider.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             ContextProvider.BREAKPOINT_ANNOTATION_TYPE) :
            (isConditional ? ContextProvider.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             ContextProvider.DISABLED_BREAKPOINT_ANNOTATION_TYPE);

        return annotate (
            url,
            lineNumber,
            annotationType
        );
    }
    
    public static String getRelativePath (
        String className
    ) {
        int i = className.indexOf ('$');
        if (i > 0) className = className.substring (0, i);
        String sourceName = className.replace 
            ('.', '/') + ".java";
        return sourceName;
    }
    
    private static String convertSlash (String original) {
        return original.replace (File.separatorChar, '/');
    }

}

