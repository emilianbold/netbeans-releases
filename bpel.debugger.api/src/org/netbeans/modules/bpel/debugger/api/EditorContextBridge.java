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

package org.netbeans.modules.bpel.debugger.api;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.bpel.debugger.spi.EditorContext;

/**
 * @author Alexander Zgursky
 * @author Vladimir Yaroslavskiy
 */
public final class EditorContextBridge {

    private static EditorContext context;
    
    private EditorContextBridge() {};

    /**
     * Converts given text to html with <code>bold=true</code>.
     *
     * @param text text to be converted
     * 
     * @return html-formatted String or <code>null</code>,
     *         if given text is <code>null</code>
     *
     * @see #toHtml
     */
    public static String toBold(String text) {
        return toHtml(text, true, false, null);
    }
    
    /**
     * Converts given text to html using given attributes.
     * @param text text to be converted
     * @param bold bold flag
     * @param italic italic flag
     * @param color color to be used or <code>null</code> for default color
     * 
     * @return html formatted text or <code>null</code> if
     *         given text is <code>null</code>
     */
    public static String toHtml(
            String text, boolean bold, boolean italic, Color color)
    {
        if (text == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html>"); // NOI18N
        
        if (bold) {
            buffer.append("<b>"); // NOI18N
        }
        if (italic) {
            buffer.append("<i>"); // NOI18N
        }
        if (color != null) {
            buffer.append("<font color=\"#"); // NOI18N
            final int RGB_BITS = 0xffffff;
            buffer.append(String.format("%06x", color.getRGB() & RGB_BITS)); // NOI18N
            buffer.append("\">"); // NOI18N
        }
        buffer.append(text.
                replaceAll("&", "&amp;").       // NOI18N
                replaceAll("<", "&lt;").        // NOI18N
                replaceAll(">", "&gt;")         // NOI18N
        );
        
        if (color != null) {
            buffer.append("</font>"); // NOI18N
        }
        if (italic) {
            buffer.append("</i>"); // NOI18N
        }
        if (bold) {
            buffer.append("</b>"); // NOI18N
        }
        buffer.append("</html>"); // NOI18N
        
        return buffer.toString();
    }
    
    public static String normalizeXpath(String xpath) {
        if (xpath == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer(200);
        StringTokenizer tokenizer = new StringTokenizer(xpath, "/");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.length() > 0) {
                buffer.append('/');
                if (token.equals("process[1]") || token.equals("process")) {
                    buffer.append("process");
                } else {
                    buffer.append(token);
                    if (token.charAt(token.length()-1) != ']') {
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
    public static boolean showSource(String url, String xpath) {
        return getContext().showSource(url, xpath);
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
    public static Object annotate(
            String url, String xpath, AnnotationType annotationType)
    {
        return getContext().annotate(url, xpath, annotationType);
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
    
    public static String getXpath(Object annotation) {
        return getContext().getXpath(annotation);
    }
    
    public static QName getProcessQName(String url) {
        return getContext().getProcessQName(url);
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
    
//    /**
//     * Context provider that embeds two given Context Providers and
//     * delegates methods calls to them.
//     */
//    private static class CompoundContextProvider implements EditorContext {
//        
//        /** First of the two embeded Context Providers. */
//        private EditorContext myContext1;
//        
//        /** Second of the two embeded Context Providers. */
//        private EditorContext myContext2;
//        
//        CompoundContextProvider(EditorContext context1, EditorContext context2) {
//            myContext1 = context1;
//            myContext2 = context2;
//        }
//        
//        public boolean showSource(String url, Position position) {
//            return  myContext1.showSource(url, position) |
//                    myContext2.showSource(url, position);
//        }
//        
//        public Object annotate(
//                String url, Position position, AnnotationType annotationType)
//        {
//            CompoundAnnotation ca = new CompoundAnnotation(
//                        annotate(url, position, annotationType),
//                        annotate(url, position, annotationType)
//                    );
//            return ca;
//        }
//        
//        public void removeAnnotation(Object annotation) {
//            CompoundAnnotation ca = (CompoundAnnotation) annotation;
//            myContext1.removeAnnotation(ca.getAnnotation1());
//            myContext2.removeAnnotation(ca.getAnnotation2());
//        }
//        
//        public int getLineNumber(Object annotation) {
//            int ln = myContext1.getLineNumber(annotation);
//            if (ln >= 0) {
//                return ln;
//            } else {
//                return myContext2.getLineNumber(annotation);
//            }
//        }
//
//        /**
//         * Returns the more appropriate line number for the
//         * given the url and line number.
//         */
//        public int translateBreakpointLine(String url, int lineNumber) {
//            int newLineNumber = myContext1.translateBreakpointLine(url, lineNumber);
//            if (newLineNumber < 0) {
//                newLineNumber = myContext2.translateBreakpointLine(url, lineNumber);
//            }
//            return newLineNumber;
//        }
//        
//        public void addAnnotationListener(Object annotation, PropertyChangeListener l) {
//            //TODO: implement this
//            //the following commented impl will not do since the subscriber
//            //would receive events for the different annotation object from
//            //it has subscribed for
//            
//            CompoundAnnotation ca = (CompoundAnnotation) annotation;
//            myContext1.addAnnotationListener(ca.getAnnotation1(), l);
//            myContext2.addAnnotationListener(ca.getAnnotation2(), l);
//            
//            throw new UnsupportedOperationException(
//                    "Not supported for compound context");  //NOI18N
//            
//        }
//
//        public void removeAnnotationListener(Object annotation, PropertyChangeListener l) {
//            //TODO: implement this
//            //the following commented impl will not do since the subscriber
//            //would receive events for the different annotation object from
//            //it has subscribed for
//            
////            CompoundAnnotation ca = (CompoundAnnotation) annotation;
////            myCp1.removeAnnotationListener(ca.getAnnotation1(), l);
////            myCp2.removeAnnotationListener(ca.getAnnotation2(), l);
//            
//            throw new UnsupportedOperationException(
//                    "Not supported for compound context");  //NOI18N
//        }
//    }
    
//    private static final class CompoundAnnotation {
//        private Object myAnnotation1;
//        private Object myAnnotation2;
//        
//        public CompoundAnnotation(Object annotation1, Object annotation2) {
//            myAnnotation1 = annotation1;
//            myAnnotation2 = annotation2;
//        }
//        
//        public Object getAnnotation1() {
//            return myAnnotation1;
//        }
//        
//        public Object getAnnotation2() {
//            return myAnnotation2;
//        }
//    }
}
