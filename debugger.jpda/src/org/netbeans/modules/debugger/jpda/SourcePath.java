/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.StackFrame;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.spi.debugger.jpda.EditorContext;

/**
 *
 * @author Jan Jancura
 */
public class SourcePath {

    private ContextProvider          lookupProvider;
    private SourcePathProvider   contextProvider;
    private JPDADebugger            debugger;
    

    public SourcePath (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = (JPDADebugger) lookupProvider.lookupFirst 
            (null, JPDADebugger.class);
    }

    private SourcePathProvider getContext () {
        if (contextProvider == null) {
            List l = lookupProvider.lookup (null, SourcePathProvider.class);
            contextProvider = (SourcePathProvider) l.get (0);
            int i, k = l.size ();
            for (i = 1; i < k; i++) {
                contextProvider = new CompoundContextProvider (
                    (SourcePathProvider) l.get (i), 
                    contextProvider
                );
            }
        }
        return contextProvider;
    }

    
    // ContextProvider methods .................................................
    
    /**
     * Returns relative path for given url.
     *
     * @param url a url of resource file
     * @param directorySeparator a directory separator character
     * @param includeExtension whether the file extension should be included 
     *        in the result
     *
     * @return relative path
     */
    public String getRelativePath (
        String url, 
        char directorySeparator, 
        boolean includeExtension
    ) {
        return getContext ().getRelativePath 
            (url, directorySeparator, includeExtension);
    }

    /**
     * Translates a relative path ("java/lang/Thread.java") to url 
     * ("file:///C:/Sources/java/lang/Thread.java"). Uses GlobalPathRegistry
     * if global == true.
     *
     * @param relativePath a relative path (java/lang/Thread.java)
     * @param global true if global path should be used
     * @return url
     */
    public String getURL (String relativePath, boolean global) {
        return getContext ().getURL (relativePath, global);
    }
    
    public String getURL (
        StackFrame sf,
        String stratumn
    ) {
        try {
            return getURL (
                convertSlash (sf.location ().sourcePath (stratumn)),
                true
            );
        } catch (AbsentInformationException e) {
            return getURL (
                convertClassNameToRelativePath (
                    sf.location ().declaringType ().name ()
                ),
                true
            );
        }
    }
    
    /**
     * Returns array of source roots.
     */
    public String[] getSourceRoots () {
        return getContext ().getSourceRoots ();
    }
    
    /**
     * Sets array of source roots.
     *
     * @param sourceRoots a new array of sourceRoots
     */
    public void setSourceRoots (String[] sourceRoots) {
        getContext ().setSourceRoots (sourceRoots);
    }
    
    /**
     * Returns set of original source roots.
     *
     * @return set of original source roots
     */
    public String[] getOriginalSourceRoots () {
        return getContext ().getOriginalSourceRoots ();
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        getContext ().addPropertyChangeListener (l);
    }

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public void removePropertyChangeListener (
        PropertyChangeListener l
    ) {
        getContext ().removePropertyChangeListener (l);
    }
    
    
    // utility methods .........................................................

    public boolean sourceAvailable (
        String relativePath
    ) {
        return getURL (relativePath, true) != null;
    }

    public boolean sourceAvailable (
        JPDAThread t,
        String stratumn
    ) {
        try {
            return sourceAvailable (convertSlash (t.getSourcePath (stratumn)));
        } catch (AbsentInformationException e) {
            return sourceAvailable (convertClassNameToRelativePath (t.getClassName ()));
        }
    }

    public boolean sourceAvailable (
        Field f
    ) {
        String className = f.getClassName ();
        return sourceAvailable (className);
    }

    public boolean sourceAvailable (
        CallStackFrame csf,
        String stratumn
    ) {
        try {
            return sourceAvailable (convertSlash (csf.getSourcePath (stratumn)));
        } catch (AbsentInformationException e) {
            return sourceAvailable (convertClassNameToRelativePath (csf.getClassName ()));
        }
    }

    public String getURL (
        CallStackFrame csf,
        String stratumn
    ) {
        try {
            return getURL (convertSlash (csf.getSourcePath (stratumn)), true);
        } catch (AbsentInformationException e) {
            return getURL (
                convertClassNameToRelativePath (csf.getClassName ()),
                true
            );
        }
    }

    public boolean showSource (
        JPDAThread t,
        String stratumn
    ) {
        int lineNumber = t.getLineNumber (stratumn);
        if (lineNumber < 1) lineNumber = 1;
        try {
            return EditorContextBridge.showSource (
                getURL (convertSlash (t.getSourcePath (stratumn)), true),
                lineNumber,
                debugger
            );
        } catch (AbsentInformationException e) {
            return EditorContextBridge.showSource (
                getURL (
                    convertClassNameToRelativePath (t.getClassName ()), 
                    true
                ),
                lineNumber,
                debugger
            );
        }
    }

    public boolean showSource (CallStackFrame csf, String stratumn) {
        int lineNumber = csf.getLineNumber (stratumn);
        if (lineNumber < 1) lineNumber = 1;
        try {
            return EditorContextBridge.showSource (
                getURL (convertSlash (csf.getSourcePath (stratumn)), true),
                lineNumber,
                debugger
            );
        } catch (AbsentInformationException e) {
            return EditorContextBridge.showSource (
                getURL (
                    convertClassNameToRelativePath (csf.getClassName ()), true
                ),
                lineNumber,
                debugger
            );
        }
    }

    public boolean showSource (Field v) {
        String fieldName = ((Field) v).getName ();
        String className = className = ((Field) v).getClassName ();
        String url = getURL (
            EditorContextBridge.getRelativePath (className), true
        );
        if (url == null) return false;
        int lineNumber = lineNumber = EditorContextBridge.getFieldLineNumber (
            url,
            className,
            fieldName
        );
        if (lineNumber < 1) lineNumber = 1;
        return EditorContextBridge.showSource (
            url,
            lineNumber,
            debugger
        );
    }

    private static String convertSlash (String original) {
        return original.replace (File.separatorChar, '/');
    }

    public static String convertClassNameToRelativePath (
        String className
    ) {
        int i = className.indexOf ('$');
        if (i > 0) className = className.substring (0, i);
        String sourceName = className.replace
            ('.', '/') + ".java";
        return sourceName;
    }

    public Object annotate (
        JPDAThread t,
        String stratumn
    ) {
        int lineNumber = t.getLineNumber (stratumn);
        if (lineNumber < 1) return null;
        try {
            return EditorContextBridge.annotate (
                getURL (convertSlash (t.getSourcePath (stratumn)), true),
                lineNumber,
                EditorContext.CURRENT_LINE_ANNOTATION_TYPE,
                debugger
            );
        } catch (AbsentInformationException e) {
            return EditorContextBridge.annotate (
                getURL (
                    convertClassNameToRelativePath (t.getClassName ()), true
                ),
                lineNumber,
                EditorContext.CURRENT_LINE_ANNOTATION_TYPE,
                debugger
            );
        }
    }

    public Object annotate (
        CallStackFrame csf,
        String stratumn
    ) {
        int lineNumber = csf.getLineNumber (stratumn);
        if (lineNumber < 1) return null;
        try {
            return EditorContextBridge.annotate (
                getURL (convertSlash (csf.getSourcePath (stratumn)), true),
                lineNumber,
                EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE,
                debugger
            );
        } catch (AbsentInformationException e) {
            return EditorContextBridge.annotate (
                getURL (
                    convertClassNameToRelativePath (csf.getClassName ()), true
                ),
                lineNumber,
                EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE,
                debugger
            );
        }
    }

    
    // innerclasses ............................................................

    private static class CompoundContextProvider extends SourcePathProvider {

        private SourcePathProvider cp1, cp2;

        CompoundContextProvider (
            SourcePathProvider cp1,
            SourcePathProvider cp2
        ) {
            this.cp1 = cp1;
            this.cp2 = cp2;
        }

        public String getURL (String relativePath, boolean global) {
            String p1 = cp1.getURL (relativePath, global);
            if (p1 != null) return p1;
            return cp2.getURL (relativePath, global);
        }

        public String getRelativePath (
            String url, 
            char directorySeparator, 
            boolean includeExtension
        ) {
            String p1 = cp1.getRelativePath (
                url, 
                directorySeparator, 
                includeExtension
            );
            if (p1 != null) return p1;
            return cp2.getRelativePath (
                url, 
                directorySeparator, 
                includeExtension
            );
        }
    
        public String[] getSourceRoots () {
            String[] fs1 = cp1.getSourceRoots ();
            String[] fs2 = cp2.getSourceRoots ();
            String[] fs = new String [fs1.length + fs2.length];
            System.arraycopy (fs1, 0, fs, 0, fs1.length);
            System.arraycopy (fs2, 0, fs, fs1.length, fs2.length);
            return fs;
        }
    
        public String[] getOriginalSourceRoots () {
            String[] fs1 = cp1.getOriginalSourceRoots ();
            String[] fs2 = cp2.getOriginalSourceRoots ();
            String[] fs = new String [fs1.length + fs2.length];
            System.arraycopy (fs1, 0, fs, 0, fs1.length);
            System.arraycopy (fs2, 0, fs, fs1.length, fs2.length);
            return fs;
        }

        public void setSourceRoots (String[] sourceRoots) {
            cp1.setSourceRoots (sourceRoots);
            cp2.setSourceRoots (sourceRoots);
        }

        public void addPropertyChangeListener (PropertyChangeListener l) {
            cp1.addPropertyChangeListener (l);
            cp2.addPropertyChangeListener (l);
        }

        public void removePropertyChangeListener (PropertyChangeListener l) {
            cp1.removePropertyChangeListener (l);
            cp2.removePropertyChangeListener (l);
        }
    }

    private static class CompoundAnnotation {
        CompoundAnnotation () {}
        
        Object annotation1;
        Object annotation2;
    }
}

