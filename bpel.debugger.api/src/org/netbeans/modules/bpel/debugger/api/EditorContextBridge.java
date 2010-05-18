/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.api;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.bpel.debugger.api.psm.ProcessStaticModel;
import org.netbeans.modules.bpel.debugger.spi.EditorContext;

/**
 * @author Alexander Zgursky
 * @author Vladimir Yaroslavskiy
 */
public final class EditorContextBridge {

    private static EditorContext context;
    
    private EditorContextBridge() {};

    public static String normalizeXpath(String xpath) {
        if (xpath == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer(200);
        StringTokenizer tokenizer = new StringTokenizer(xpath, "/");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.length() > 0) {
                String noPrefix = token.substring(token.indexOf(':') + 1);
                buffer.append('/').append(ProcessStaticModel.BPEL_NAMESPACE_PREFIX).append(':');
                if (noPrefix.equals("process[1]") || noPrefix.equals("process")) {
                    buffer.append("process");
                } else {
                    buffer.append(noPrefix);
                    if (noPrefix.charAt(noPrefix.length()-1) != ']') {
                        buffer.append("[1]");
                    }
                }
            }
        }
        
        return buffer.toString();
    }
    
    /**
     * Opens given file (url) in the editor and navigates to the given position.
     * Method delegates this call to all registered EditorContext
     * implementations and returns <code>true</code> if any of them succeeded.
     *
     * @param url full path to the source file to show
     * @param position position to navigate to
     *
     * @return <code>true</code> if any of the registered EditorContext
     *         implementations succeeded to show the source.
     */
    public static boolean showSource(String url, String xpath, String view) {
        return getContext().showSource(url, xpath, view);
    }
    
    /**
     * Annotates the given position in the given file (url) with the given
     * annotation type.
     *
     * @param url full path to the source file to add annotation for
     * @param position annotation position
     * @param annotationType annotation type
     *
     * @return a reference to the created annotation object. This object should
     *         be supplied as a parameter to subsequent
     *         {@link #removeAnnotation} call
     */
    public static Object addAnnotation(
            final String url, 
            final String xpath, 
            final int lineNumber,
            final AnnotationType annotationType) {
        
        return getContext().addAnnotation(
                url, xpath, lineNumber, annotationType);
    }
    
    /**
     * Removes the given annotation.
     *
     * @param annotation a reference to the annotation object that is returned
     *                   from {@link #annotate} method
     */
    public static void removeAnnotation(Object annotation) {
        getContext().removeAnnotation(annotation);
    }
    
    public static boolean isAttached(Object annotation) {
        return getContext().isAttached(annotation);
    }
    
    public static boolean isValid(Object annotation) {
        return getContext().isValid(annotation);
    }
    
    public static AnnotationType getAnnotationType(Object annotation) {
        return getContext().getAnnotationType(annotation);
    }
    
    public static String getXpath(Object annotation) {
        return getContext().getXpath(annotation);
    }
    
    public static int getLineNumber(Object annotation) {
        return getContext().getLineNumber(annotation);
    }
    
    public static QName getProcessQName(String url) {
        return getContext().getProcessQName(url);
    }
    
    public static QName getCurrentProcessQName() {
        return getContext().getCurrentProcessQName();
    }
    
    /**
     * Returns the more appropriate line number for the
     * given the url and line number.
     */
    public static int translateBreakpointLine(String url, int lineNumber) {
        return getContext().translateBreakpointLine(url, lineNumber);
    }
    
    public static void addAnnotationListener(Object annotation, PropertyChangeListener l) {
        getContext().addAnnotationListener(annotation, l);
    }
    
    public static void removeAnnotationListener(Object annotation, PropertyChangeListener l) {
        getContext().removeAnnotationListener(annotation, l);
    }
    
    private static EditorContext getContext() {
        if (context == null) {
            List l = DebuggerManager.getDebuggerManager().lookup
                    (null, EditorContext.class);
            context = (EditorContext) l.get(0);
            int i;
            int k = l.size();
            for (i = 1; i < k; i++) {
                assert false : "compound editor context is not supported";
//                context = new CompoundContextProvider(
//                        (EditorContext) l.get(i), context);
            }
        }
        return context;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DESIGN_VIEW = "orch-designer";
    public static final String SOURCE_VIEW = "bpelsource";
}
