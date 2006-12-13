/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.*;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/** 
 * Defines bridge to editor and src hierarchy. It allows use of different 
 * source viewers for debugger.
 * @author Jan Jancura and Gordon Prieur
 */
public abstract class EditorContext {
    
    /** Annotation type constant. */
    public static final String BREAKPOINT_ANNOTATION_TYPE = new String("Breakpoint"); //NOI18N
    
    /** Annotation type constant. */
    public static final String DISABLED_BREAKPOINT_ANNOTATION_TYPE =  new String("DisabledBreakpoint"); //NOI18N
    
    /** Annotation type constant. */
    public static final String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE =  new String("CondBreakpoint"); //NOI18N

    /** Annotation type constant. */
    public static final String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE =  new String("DisabledCondBreakpoint"); //NOI18N

    /** Annotation type constant. */
    public static final String CURRENT_LINE_ANNOTATION_TYPE =  new String("CurrentPC"); //NOI18N

    /** Annotation type constant. */
    public static final String CALL_STACK_FRAME_ANNOTATION_TYPE =  new String("CallSite"); //NOI18N

    /** Property name constant. */
    public static final String PROP_LINE_NUMBER = new String("lineNumber"); //NOI18N


    /**
     * Shows source with given url on given line number.
     *
     * @param url a url of source to be shown
     * @param lineNumber a number of line to be shown
     * @param timeStamp a time stamp to be used
     */
    public abstract boolean showSource(String url,  int lineNumber, Object timeStamp);

    /**
     * Creates a new time stamp.
     *
     * @param timeStamp a new time stamp
     */
    public abstract void createTimeStamp(Object timeStamp);

    /**
     * Disposes given time stamp.
     *
     * @param timeStamp a time stamp to be disposed
     */
    public abstract void disposeTimeStamp(Object timeStamp);
    
    /**
     * Updates timeStamp for gived url.
     *
     * @param timeStamp time stamp to be updated
     * @param url an url
     */
    public abstract void updateTimeStamp(Object timeStamp, String url);

    /**
     * Adds annotation to given url on given line.
     *
     * @param url a url of source annotation should be set into
     * @param lineNumber a number of line annotation should be set into
     * @param annotationType a type of annotation to be set
     * @param timeStamp a time stamp to be used
     *
     * @return annotation or <code>null</code>, when the annotation can not be
     *         created at the given URL or line number.
     */
    public abstract Object annotate(String url, int lineNumber, String annotationType, Object timeStamp);

    /**
     * Returns line number given annotation is associated with.
     *
     * @param annotation a annotation
     * @param timeStamp a time stamp to be used
     *
     * @return line number given annotation is associated with
     */
    public abstract int getLineNumber (
        Object annotation,
        Object timeStamp
    );

    /**
     * Removes given annotation.
     */
    public abstract void  removeAnnotation(Object annotation );

    /**
     * Returns number of line currently selected in editor or <code>-1</code>.
     *
     * @return number of line currently selected in editor or <code>-1</code>
     */
    public abstract int getCurrentLineNumber();

    /**
     * Returns number of line most recently selected in editor or <code>-1</code>.
     *
     * @return number of line most recently selected in editor or <code>-1</code>
     */
    public abstract int getMostRecentLineNumber();

    /**
     * Returns URL of source currently selected in editor or empty string.
     *
     * @return URL of source currently selected in editor or empty string
     */
    public abstract String getCurrentURL();
    
    /**
     *  Return the most recent URL or empty string. The difference between this and getCurrentURL()
     *  is that this one will return a URL when the editor has lost focus.
     *
     *  @return url in string form
     */
    public abstract String getMostRecentURL();

    /**
     * Returns name of function currently selected in editor or empty string.
     *
     * @return name of function currently selected in editor or empty string
     */
    public abstract String getCurrentFunctionName();

    /**
     * Returns method name currently selected in editor or empty string.
     *
     * @return method name currently selected in editor or empty string
     */
    public abstract String getSelectedFunctionName();
    
    /**
     * Returns line number of given field in given class.
     *
     * @param url the url of file the class is deined in
     * @param className the name of class (or innerclass) the field is 
     *                  defined in
     * @param fieldName the name of field
     *
     * @return line number or -1
     */
    public abstract int getFieldLineNumber(String url, String className, String fieldName);
    
    /**
     * Get the MIME type of the current file.
     *
     * @return The MIME type of the current file
     */
    public abstract String getCurrentMIMEType();
    
    /**
     * Get the MIME type of the most recently selected file.
     *
     * @return The MIME type of the most recent selected file
     */
    public abstract String getMostRecentMIMEType();
    
    /**
     * Adds a property change listener.
     *
     * @param l the listener to add
     */
    public abstract void addPropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Removes a property change listener.
     *
     * @param l the listener to remove
     */
    public abstract void removePropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Adds a property change listener.
     *
     * @param propertyName the name of property
     * @param l the listener to add
     */
    public abstract void addPropertyChangeListener(String propertyName, PropertyChangeListener l);
    
    /**
     * Removes a property change listener.
     *
     * @param propertyName the name of property
     * @param l the listener to remove
     */
    public abstract void removePropertyChangeListener(String propertyName, PropertyChangeListener l);
}
