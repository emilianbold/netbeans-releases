/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Micro//S ystems, Inc. Portions Copyright 1997-2006 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.debugger.jpda.ui;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.StackFrame;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.openide.ErrorManager;

/**
 * Utility methods for sources.
 *
 * @see Similar class in debuggerjpda when modifying this.
 *
 * @author Jan Jancura
 */
public class SourcePath {

    private ContextProvider         contextProvider;
    private SourcePathProvider      sourcePathProvider;
    private JPDADebugger            debugger;
    

    public SourcePath (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
        getContext();// To initialize the source path provider
    }

    private SourcePathProvider getContext () {
        if (sourcePathProvider == null) {
            List l = contextProvider.lookup (null, SourcePathProvider.class);
            sourcePathProvider = (SourcePathProvider) l.get (0);
            int i, k = l.size ();
            for (i = 1; i < k; i++) {
                sourcePathProvider = new CompoundContextProvider (
                    (SourcePathProvider) l.get (i), 
                    sourcePathProvider
                );
            }
            initSourcePaths ();
        }
        return sourcePathProvider;
    }
    
    static SourcePathProvider getDefaultContext() {
        List providers = DebuggerManager.getDebuggerManager().
                lookup("netbeans-JPDASession", SourcePathProvider.class);
        for (Iterator it = providers.iterator(); it.hasNext(); ) {
            Object provider = it.next();
            // Hack - find our provider:
            if (provider.getClass().getName().equals("org.netbeans.modules.debugger.jpda.projects.SourcePathProviderImpl")) {
                return (SourcePathProvider) provider;
            }
        }
        return null;
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
        String url = getContext ().getURL (relativePath, global);
        if (url != null) {
            try {
                new java.net.URL(url);
            } catch (java.net.MalformedURLException muex) {
                Logger.getLogger(SourcePath.class.getName()).log(Level.WARNING,
                        "Malformed URL '"+url+"' produced by "+getContext (), muex);
                return null;
            }
        }
        return url;
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
        String relativePath,
        boolean global
    ) {
        return getURL (relativePath, global) != null;
    }

    public boolean sourceAvailable (
        JPDAThread t,
        String stratumn,
        boolean global
    ) {
        try {
            return sourceAvailable (
                convertSlash (t.getSourcePath (stratumn)), global
            );
        } catch (AbsentInformationException e) {
            return sourceAvailable (
                convertClassNameToRelativePath (t.getClassName ()), global
            );
        }
    }

    public boolean sourceAvailable (
        Field f
    ) {
        String className = f.getClassName ();
        return sourceAvailable (className, true);
    }

    public boolean sourceAvailable (
        CallStackFrame csf,
        String stratumn
    ) {
        try {
            return sourceAvailable (
                convertSlash (csf.getSourcePath (stratumn)), true
            );
        } catch (AbsentInformationException e) {
            return sourceAvailable (
                convertClassNameToRelativePath (csf.getClassName ()), true
            );
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
                convertClassNameToRelativePath (csf.getClassName ()), true
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
            return EditorContextBridge.getContext().showSource (
                getURL (convertSlash (t.getSourcePath (stratumn)), true),
                lineNumber,
                debugger
            );
        } catch (AbsentInformationException e) {
            return EditorContextBridge.getContext().showSource (
                getURL (
                    convertClassNameToRelativePath (t.getClassName ()), true
                ),
                lineNumber,
                debugger
            );
        }
    }

    public boolean showSource (CallStackFrame csf, String stratumn) {
        try {
            String url = getURL (
                convertSlash (csf.getSourcePath (stratumn)), true
            );
            if (url == null) {
                stratumn = csf.getDefaultStratum ();
                url = getURL (
                    convertSlash (csf.getSourcePath (stratumn)), true
                );
            }
            if (url == null) {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "Show Source: No URL for source path "+csf.getSourcePath (stratumn)+
                        "\nThe reason is likely no opened project for this source file.");
                return false;
            }
            int lineNumber = csf.getLineNumber (stratumn);
            if (lineNumber < 1) lineNumber = 1;
            return EditorContextBridge.getContext().showSource (
                url,
                lineNumber,
                debugger
            );
        } catch (AbsentInformationException e) {
            String url = getURL (
                convertClassNameToRelativePath (csf.getClassName ()), true
            );
            if (url == null) {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "Show Source: No source URL for class "+csf.getClassName()+
                        "\nThe reason is likely no opened project for the source file.");
                return false;
            }
            return EditorContextBridge.getContext().showSource (
                url,
                1,
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
        int lineNumber = lineNumber = EditorContextBridge.getContext().getFieldLineNumber (
            url,
            className,
            fieldName
        );
        if (lineNumber < 1) lineNumber = 1;
        return EditorContextBridge.getContext().showSource (
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
        return annotate(t, stratumn, true);
    }
    
    public Object annotate (
        JPDAThread t,
        String stratumn,
        boolean isCurrent
    ) {
        int lineNumber = t.getLineNumber (stratumn);
        if (lineNumber < 1) return null;
        String url;
        try {
            url = getURL (convertSlash (t.getSourcePath (stratumn)), true);
        } catch (AbsentInformationException e) {
            url = getURL (convertClassNameToRelativePath (t.getClassName ()), true);
        }
        Operation operation;
        List operationsAnn;
        if (isCurrent) {
            operation = t.getCurrentOperation();
            operationsAnn = annotateOperations(debugger, url, operation, t.getLastOperations(), lineNumber);
        } else {
            operation = null;
            operationsAnn = Collections.EMPTY_LIST;
        }
        if (operation == null) {
            if (operationsAnn.size() == 0) {
                return EditorContextBridge.getContext().annotate (
                    url,
                    lineNumber,
                    isCurrent ?
                        EditorContext.CURRENT_LINE_ANNOTATION_TYPE :
                        EditorContext.OTHER_THREAD_ANNOTATION_TYPE,
                    debugger,
                    t
                );
            }
        }
        return operationsAnn;
    }

    public Object annotate (
        CallStackFrame csf,
        String stratumn
    ) {
        int lineNumber = csf.getLineNumber (stratumn);
        if (lineNumber < 1) return null;
        Operation operation = csf.getCurrentOperation(stratumn);
        try {
            if (operation != null) {
                int startOffset;
                int endOffset;
                if (operation.getMethodName() != null) {
                    startOffset = operation.getMethodStartPosition().getOffset();
                    endOffset = operation.getMethodEndPosition().getOffset();
                } else {
                    startOffset = operation.getStartPosition().getOffset();
                    endOffset = operation.getEndPosition().getOffset();
                }
                return EditorContextBridge.getContext().annotate (
                    getURL (convertSlash (csf.getSourcePath (stratumn)), true),
                    startOffset,
                    endOffset,
                    EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE,
                    debugger
                );
            } else {
                return EditorContextBridge.getContext().annotate (
                    getURL (convertSlash (csf.getSourcePath (stratumn)), true),
                    lineNumber,
                    EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE,
                    debugger
                );
            }
        } catch (AbsentInformationException e) {
            return EditorContextBridge.getContext().annotate (
                getURL (
                    convertClassNameToRelativePath (csf.getClassName ()), true
                ),
                lineNumber,
                EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE,
                debugger
            );
        }
    }
    
    private static List annotateOperations(JPDADebugger debugger, String url,
                                           Operation currentOperation, List lastOperations,
                                           int locLineNumber) {
        List annotations = null;
        int currentOperationLine = -1;
        if (currentOperation != null) {
            annotations = new ArrayList();
            Object ann = createAnnotation(debugger, url, currentOperation,
                                          EditorContext.CURRENT_LINE_ANNOTATION_TYPE,
                                          true);
            if (ann != null) annotations.add(ann);
            int lineNumber;
            if (currentOperation.getMethodName() != null) {
                lineNumber = currentOperation.getMethodStartPosition().getLine();
            } else {
                lineNumber = currentOperation.getStartPosition().getLine();
            }
            ann = EditorContextBridge.getContext().annotate (
                url,
                lineNumber,
                EditorContext.CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE,
                debugger
            );
            currentOperationLine = lineNumber;
            if (ann != null) annotations.add(ann);
        }
        boolean isNewLineExp = false;
        if (lastOperations != null && lastOperations.size() > 0) {
            if (annotations == null) {
                annotations = new ArrayList();
            }
            isNewLineExp = currentOperation == null;
            for (int i = 0; i < lastOperations.size(); i++) {
                Operation lastOperation = (Operation) lastOperations.get(i);
                if (currentOperation == lastOperation && i == lastOperations.size() - 1) {
                    Object ann = createAnnotation(debugger, url,
                                                  lastOperation,
                                                  EditorContext.CURRENT_OUT_OPERATION_ANNOTATION_TYPE,
                                                  false);
                    if (ann != null) annotations.add(ann);
                    int lineNumber = lastOperation.getEndPosition().getLine();
                    if (currentOperationLine != lineNumber) {
                        ann = EditorContextBridge.getContext().annotate (
                            url,
                            lineNumber,
                            EditorContext.CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE,
                            debugger
                        );
                        if (ann != null) annotations.add(ann);
                    }
                    isNewLineExp = false;
                } else {
                    Object ann = createAnnotation(debugger, url,
                                                  lastOperation,
                                                  EditorContext.CURRENT_LAST_OPERATION_ANNOTATION_TYPE,
                                                  true);
                    if (ann != null) annotations.add(ann);
                }
            }
        }
        if (isNewLineExp) {
            Object ann = EditorContextBridge.getContext().annotate (
                url,
                locLineNumber,
                EditorContext.CURRENT_LINE_ANNOTATION_TYPE,
                debugger
            );
            if (ann != null) annotations.add(ann);
        }
        if (annotations != null) {
            return annotations;
        } else {
            return Collections.EMPTY_LIST;
        }
    }
    
    private static Object createAnnotation(JPDADebugger debugger, String url,
                                           Operation operation, String type,
                                           boolean method) {
        int startOffset;
        int endOffset;
        if (method && operation.getMethodName() != null) {
            startOffset = operation.getMethodStartPosition().getOffset();
            endOffset = operation.getMethodEndPosition().getOffset();
        } else {
            startOffset = operation.getStartPosition().getOffset();
            endOffset = operation.getEndPosition().getOffset();
        }
        return EditorContextBridge.getContext().annotate (
            url,
            startOffset,
            endOffset,
            type,
            debugger
        );
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
            if (p1 != null) {
                try {
                    new java.net.URL(p1);
                    return p1;
                } catch (java.net.MalformedURLException muex) {
                    Logger.getLogger(SourcePath.class.getName()).log(Level.WARNING,
                            "Malformed URL '"+p1+"' produced by "+cp1, muex);
                } 
            }
            p1 = cp2.getURL (relativePath, global);
            if (p1 != null) {
                try {
                    new java.net.URL(p1);
                } catch (java.net.MalformedURLException muex) {
                    Logger.getLogger(SourcePath.class.getName()).log(Level.WARNING,
                            "Malformed URL '"+p1+"' produced by "+cp2, muex);
                    p1 = null;
                }
            }
            return p1;
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
    
    private void initSourcePaths () {
        Properties properties = Properties.getDefault ().
            getProperties ("debugger").getProperties ("sources");
        Set originalSourceRoots = new HashSet (Arrays.asList (
            sourcePathProvider.getOriginalSourceRoots ()
        ));
        Set sourceRoots = new HashSet (Arrays.asList (
            sourcePathProvider.getSourceRoots ()
        ));

        Iterator enabledSourceRoots = properties.getProperties ("source_roots").
            getCollection ("enabled", Collections.EMPTY_SET).iterator ();
        while (enabledSourceRoots.hasNext ()) {
            String root = (String) enabledSourceRoots.next ();
            if (originalSourceRoots.contains (root)) 
                sourceRoots.add (root);
        }
        Iterator additionalSourceRoots = properties.getProperties("additional_source_roots").
                getCollection("src_roots", Collections.EMPTY_LIST).iterator();
        while (additionalSourceRoots.hasNext()) {
            String root = (String) additionalSourceRoots.next ();
            sourceRoots.add (root);
        }
        Iterator disabledSourceRoots = properties.getProperties ("source_roots").
            getCollection ("disabled", Collections.EMPTY_SET).iterator ();
        while (disabledSourceRoots.hasNext ()) {
            String root = (String) disabledSourceRoots.next ();
            sourceRoots.remove (root);
        }
        String[] ss = new String [sourceRoots.size ()];
        sourcePathProvider.setSourceRoots ((String[]) sourceRoots.toArray (ss));
    }

    private static class CompoundAnnotation {
        Object annotation1;
        Object annotation2;
    }
}

