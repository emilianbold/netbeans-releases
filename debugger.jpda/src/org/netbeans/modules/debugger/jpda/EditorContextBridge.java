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
 * Software is Sun Micro//S ystems, Inc. Portions Copyright 1997-2007 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.EditorContext.MethodArgument;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;


/**
 *
 * @author Jan Jancura
 */
public class EditorContextBridge {

    public static final String FIELD = "field";
    public static final String METHOD = "method";
    public static final String CLASS = "class";
    public static final String LINE = "line";

    private static EditorContext context;
    
    public static EditorContext getContext () {
        if (context == null) {
            List l = DebuggerManager.getDebuggerManager ().lookup 
                (null, EditorContext.class);
            context = (EditorContext) l.get (0);
            int i, k = l.size ();
            for (i = 1; i < k; i++)
                context = new CompoundContextProvider (
                    (EditorContext) l.get (i),
                    context
                );
        }
        return context;
    }

    
    // Utility methods .........................................................

    public static String getFileName (LineBreakpoint b) { 
        try {
            return new File (new URL (b.getURL ()).getFile ()).getName ();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static boolean showSource (LineBreakpoint b, Object timeStamp) {
        if (b.getLineNumber () < 1)
            return getContext().showSource (
                b.getURL (),
                1,
                timeStamp
            );
        return getContext().showSource (
            b.getURL (),
            b.getLineNumber (),
            timeStamp
        );
    }

    public static String getRelativePath (
        JPDAThread thread,
        String stratumn
    ) {
        try {
            return convertSlash (thread.getSourcePath (stratumn));
        } catch (AbsentInformationException e) {
            return getRelativePath (thread.getClassName ());
        }
    }

    public static String getRelativePath (
        CallStackFrame csf,
        String stratumn
    ) {
        try {
            return convertSlash (csf.getSourcePath (stratumn));
        } catch (AbsentInformationException e) {
            return getRelativePath (csf.getClassName ());
        }
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
    
    
    // innerclasses ............................................................
    
    private static class CompoundContextProvider extends EditorContext {

        private EditorContext cp1, cp2;
        
        CompoundContextProvider (
            EditorContext cp1,
            EditorContext cp2
        ) {
            this.cp1 = cp1;
            this.cp2 = cp2;
        }

        public void createTimeStamp (Object timeStamp) {
            cp1.createTimeStamp (timeStamp);
            cp2.createTimeStamp (timeStamp);
        }

        public void disposeTimeStamp (Object timeStamp) {
            cp1.disposeTimeStamp (timeStamp);
            cp2.disposeTimeStamp (timeStamp);
        }
        
        public void updateTimeStamp (Object timeStamp, String url) {
            cp1.updateTimeStamp (timeStamp, url);
            cp2.updateTimeStamp (timeStamp, url);
        }

        public String getCurrentClassName () {
            String s = cp1.getCurrentClassName ();
            if (s.trim ().length () < 1)
                return cp2.getCurrentClassName ();
            return s;
        }

        public String getCurrentURL () {
            String s = cp1.getCurrentURL ();
            if (s.trim ().length () < 1)
                return cp2.getCurrentURL ();
            return s;
        }
        
        public String getCurrentFieldName () {
            String s = cp1.getCurrentFieldName ();
            if ( (s == null) || (s.trim ().length () < 1))
                return cp2.getCurrentFieldName ();
            return s;
        }
        
        public int getCurrentLineNumber () {
            int i = cp1.getCurrentLineNumber ();
            if (i < 1)
                return cp2.getCurrentLineNumber ();
            return i;
        }
        
        public String getCurrentMethodName () {
            String s = cp1.getCurrentMethodName ();
            if ( (s == null) || (s.trim ().length () < 1))
                return cp2.getCurrentMethodName ();
            return s;
        }
        
        public String getSelectedIdentifier () {
            String s = cp1.getSelectedIdentifier ();
            if ( (s == null) || (s.trim ().length () < 1))
                return cp2.getSelectedIdentifier ();
            return s;
        }
        
        public String getSelectedMethodName () {
            String s = cp1.getSelectedMethodName ();
            if ( (s == null) || (s.trim ().length () < 1))
                return cp2.getSelectedMethodName ();
            return s;
        }
        
        public void removeAnnotation (Object annotation) {
            CompoundAnnotation ca = (CompoundAnnotation) annotation;
            cp1.removeAnnotation (ca.annotation1);
            cp2.removeAnnotation (ca.annotation2);
        }

        public Object annotate (
            String sourceName,
            int lineNumber,
            String annotationType,
            Object timeStamp
        ) {
            CompoundAnnotation ca = new CompoundAnnotation ();
            ca.annotation1 = cp1.annotate
                (sourceName, lineNumber, annotationType, timeStamp);
            ca.annotation2 = cp2.annotate
                (sourceName, lineNumber, annotationType, timeStamp);
            return ca;
        }

        public int getLineNumber (Object annotation, Object timeStamp) {
            CompoundAnnotation ca = new CompoundAnnotation ();
            int ln = cp1.getLineNumber (ca.annotation1, timeStamp);
            if (ln >= 0) return ln;
            return cp2.getLineNumber (ca.annotation2, timeStamp);
        }

        public boolean showSource (String sourceName, int lineNumber, Object timeStamp) {
            return cp1.showSource (sourceName, lineNumber, timeStamp) |
                   cp2.showSource (sourceName, lineNumber, timeStamp);
        }
    
        public int getFieldLineNumber (
            String url, 
            String className, 
            String fieldName
        ) {
            int ln = cp1.getFieldLineNumber (url, className, fieldName);
            if (ln != -1) return ln;
            return cp2.getFieldLineNumber (url, className, fieldName);
        }
    
        public String getClassName (
            String url, 
            int lineNumber
        ) {
            String className = cp1.getClassName (url, lineNumber);
            if (className != null) return className;
            return cp2.getClassName (url, lineNumber);
        }
    
        public String[] getImports (String url) {
            String[] r1 = cp1.getImports (url);
            String[] r2 = cp2.getImports (url);
            String[] r = new String [r1.length + r2.length];
            System.arraycopy (r1, 0, r, 0, r1.length);
            System.arraycopy (r2, 0, r, r1.length, r2.length);
            return r;
        }
        
        public void addPropertyChangeListener (PropertyChangeListener l) {
            cp1.addPropertyChangeListener (l);
            cp2.addPropertyChangeListener (l);
        }
        
        public void removePropertyChangeListener (PropertyChangeListener l) {
            cp1.removePropertyChangeListener (l);
            cp2.removePropertyChangeListener (l);
        }
        
        public void addPropertyChangeListener (
            String propertyName, 
            PropertyChangeListener l
        ) {
            cp1.addPropertyChangeListener (propertyName, l);
            cp2.addPropertyChangeListener (propertyName, l);
        }
        
        public void removePropertyChangeListener (
            String propertyName, 
            PropertyChangeListener l
        ) {
            cp1.removePropertyChangeListener (propertyName, l);
            cp2.removePropertyChangeListener (propertyName, l);
        }
    }
    
    private static class CompoundAnnotation {
        public CompoundAnnotation() {}
        
        Object annotation1;
        Object annotation2;
    }
}

