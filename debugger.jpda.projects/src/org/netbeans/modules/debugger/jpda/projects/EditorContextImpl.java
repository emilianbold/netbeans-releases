/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

package org.netbeans.modules.debugger.jpda.projects;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;
import javax.swing.JEditorPane;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import java.util.Collections;
import java.util.Date;
import java.util.WeakHashMap;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.tools.Diagnostic;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LineBreakpoint;

import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.editor.JumpList;

import org.netbeans.modules.java.preprocessorbridge.api.JavaSourceUtil;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.text.NbDocument;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Jancura
 */
public class EditorContextImpl extends EditorContext {

    private static String fronting =
        System.getProperty ("netbeans.debugger.fronting");

    private PropertyChangeSupport   pcs;
    private Map                     annotationToURL = new HashMap ();
    private PropertyChangeListener  dispatchListener;
    private EditorContextDispatcher contextDispatcher;
    private final Map<JavaSource, JavaSourceUtil.Handle> sourceHandles = new WeakHashMap<JavaSource, JavaSourceUtil.Handle>();
    private final Map<JavaSource, Date> sourceModifStamps = new WeakHashMap<JavaSource, Date>();
    private DebuggerManagerListener sessionsListener; // cleans up sourceHandles


    {
        pcs = new PropertyChangeSupport (this);
        dispatchListener = new EditorContextDispatchListener();
        contextDispatcher = EditorContextDispatcher.getDefault();
        contextDispatcher.addPropertyChangeListener("text/x-java",
                WeakListeners.propertyChange(dispatchListener, contextDispatcher));
        sessionsListener = new SessionsListener();
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_SESSIONS,
                WeakListeners.create(DebuggerManagerListener.class,
                                     sessionsListener,
                                     new SessionsListenerRemoval()));
    }


    /**
     * Shows source with given url on given line number.
     *
     * @param url a url of source to be shown
     * @param lineNumber a number of line to be shown
     * @param timeStamp a time stamp to be used
     */
    public boolean showSource (String url, int lineNumber, Object timeStamp) {
        Line l = showSourceLine(url, lineNumber, timeStamp);
        if (l != null) {
            addPositionToJumpList(url, l, 0);
        }
        return l != null;
    }

    static Line showSourceLine (String url, int lineNumber, Object timeStamp) {
        Line l = LineTranslations.getTranslations().getLine (url, lineNumber, timeStamp); // false = use original ln
        if (l == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Show Source: Have no line for URL = "+url+", line number = "+lineNumber);
            return null;
        }
        Properties p = Properties.getDefault().getProperties("debugger.options.JPDA");
        boolean reuseEditorTabs = p.getBoolean("ReuseEditorTabs", true);
        if ("true".equalsIgnoreCase(fronting) || Utilities.isWindows()) {
            if (reuseEditorTabs) {
                l.show (ShowOpenType.REUSE, ShowVisibilityType.FOCUS);
            }
            l.show (ShowOpenType.OPEN, ShowVisibilityType.FRONT); //FIX 47825
        } else {
            if (reuseEditorTabs) {
                l.show (ShowOpenType.REUSE, ShowVisibilityType.FOCUS);
            } else {
                l.show (ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
            }
        }
        return l;
    }

    /**
     * Shows source with given url on given line number.
     *
     * @param url a url of source to be shown
     * @param lineNumber a number of line to be shown
     * @param timeStamp a time stamp to be used
     */
    public boolean showSource (String url, int lineNumber, int column, int length, Object timeStamp) {
        Line l = LineTranslations.getTranslations().getLine (url, lineNumber, timeStamp); // false = use original ln
        if (l == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Show Source: Have no line for URL = "+url+", line number = "+lineNumber);
            return false;
        }
        if ("true".equalsIgnoreCase(fronting) || Utilities.isWindows()) {
            l.show (ShowOpenType.OPEN, ShowVisibilityType.FRONT, column); //FIX 47825
        } else {
            l.show (ShowOpenType.OPEN, ShowVisibilityType.FOCUS, column);
        }
        addPositionToJumpList(url, l, column);
        return true;
    }

    /** Add the line offset into the jump history */
    private void addPositionToJumpList(String url, Line l, int column) {
        DataObject dataObject = getDataObject (url);
        if (dataObject != null) {
            EditorCookie ec = dataObject.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                try {
                    StyledDocument doc = ec.openDocument();
                    JEditorPane[] eps = ec.getOpenedPanes();
                    if (eps != null && eps.length > 0) {
                        JumpList.addEntry(eps[0], NbDocument.findLineOffset(doc, l.getLineNumber()) + column);
                    }
                } catch (java.io.IOException ioex) {
                    ErrorManager.getDefault().notify(ioex);
                }
            }
        }
    }


    /**
     * Creates a new time stamp.
     *
     * @param timeStamp a new time stamp
     */
    public void createTimeStamp (Object timeStamp) {
        LineTranslations.getTranslations().createTimeStamp(timeStamp);
    }

    /**
     * Disposes given time stamp.
     *
     * @param timeStamp a time stamp to be disposed
     */
    public void disposeTimeStamp (Object timeStamp) {
        LineTranslations.getTranslations().disposeTimeStamp(timeStamp);
    }

    public Object annotate (
        String url,
        int lineNumber,
        String annotationType,
        Object timeStamp
    ) {
        return annotate(url, lineNumber, annotationType, timeStamp, null);
    }
    @Override
    public Object annotate (
        String url,
        int lineNumber,
        String annotationType,
        Object timeStamp,
        JPDAThread thread
    ) {
        Line l =  LineTranslations.getTranslations().getLine (
            url,
            lineNumber,
            (timeStamp instanceof Breakpoint) ? null : timeStamp
        );
        if (l == null) return null;
        Annotation annotation;
        if (timeStamp instanceof Breakpoint) {
            annotation = new DebuggerBreakpointAnnotation(annotationType, l, (Breakpoint) timeStamp);
        } else {
            annotation = new DebuggerAnnotation (annotationType, l, thread);
        }
        annotationToURL.put (annotation, url);

        return annotation;
    }

    @Override
    public Object annotate (
        String url,
        int startPosition,
        int endPosition,
        String annotationType,
        Object timeStamp
    ) {
        AttributeSet attrs;
        if (EditorContext.CURRENT_LAST_OPERATION_ANNOTATION_TYPE.equals(annotationType)) {
            attrs = AttributesUtilities.createImmutable(EditorStyleConstants.WaveUnderlineColor, getColor(annotationType));
        } else {
            attrs = AttributesUtilities.createImmutable(StyleConstants.Background, getColor(annotationType));
        }
        DebuggerAnnotation annotation;
        try {
            annotation = new DebuggerAnnotation(annotationType, attrs, startPosition, endPosition,
                    URLMapper.findFileObject(new URL(url)));
        } catch (MalformedURLException ex) {
            RuntimeException rex = new RuntimeException("Bad URL: "+url);
            rex.initCause(ex);
            throw rex;
        }
        annotationToURL.put (annotation, url);

        return annotation;
    }

    private static Color getColor(String annotationType) {
        if (annotationType.endsWith("_broken")) {
            annotationType = annotationType.substring(0, annotationType.length() - "_broken".length());
        }
        if (EditorContext.BREAKPOINT_ANNOTATION_TYPE.equals(annotationType)) {
            return new Color(0xFC9D9F);
        } else if (EditorContext.CURRENT_LINE_ANNOTATION_TYPE.equals(annotationType) ||
                   EditorContext.CURRENT_OUT_OPERATION_ANNOTATION_TYPE.equals(annotationType)) {
            return new Color(0xBDE6AA);
        } else if (EditorContext.CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE.equals(annotationType)) {
            return new Color(0xE9FFE6); // 0xE3FFD2// 0xD1FFBC
        } else if (EditorContext.CURRENT_LAST_OPERATION_ANNOTATION_TYPE.equals(annotationType)) {
            return new Color(0x99BB8A);
        } else {
            return new Color(0x0000FF);
        }
    }

    /**
     * Removes given annotation.
     *
     * @return true if annotation has been successfully removed
     */
    public void removeAnnotation (
        Object a
    ) {
        if (a instanceof Collection) {
            Collection annotations = ((Collection) a);
            for (Iterator it = annotations.iterator(); it.hasNext(); ) {
                removeAnnotation((Annotation) it.next());
            }
        } else {
            removeAnnotation((Annotation) a);
        }
    }

    private void removeAnnotation(Annotation annotation) {
        annotation.detach ();
        annotationToURL.remove (annotation);
    }

    /**
     * Returns line number given annotation is associated with.
     *
     * @param annotation an annotation, or an array of "url" and new Integer(line number)
     * @param timeStamp a time stamp to be used
     *
     * @return line number given annotation is associated with
     */
    public int getLineNumber (
        Object annotation,
        Object timeStamp
    ) {
        if (annotation instanceof LineBreakpoint) {
            // A sort of hack to be able to retrieve the original line.
            LineBreakpoint lb = (LineBreakpoint) annotation;
            return LineTranslations.getTranslations().getOriginalLineNumber(lb, timeStamp);
        }
        /*if (annotation instanceof Object[]) {
            // A sort of hack to be able to retrieve the original line.
            Object[] urlLine = (Object[]) annotation;
            String url = (String) urlLine[0];
            int line = ((Integer) urlLine[1]).intValue();
            return LineTranslations.getTranslations().getOriginalLineNumber(url, line, timeStamp);
        }*/
        Line line;
        if (annotation instanceof DebuggerBreakpointAnnotation) {
            line = ((DebuggerBreakpointAnnotation) annotation).getLine();
        } else {
            line = ((DebuggerAnnotation) annotation).getLine();
        }
        if (timeStamp == null)
            return line.getLineNumber () + 1;
        String url = (String) annotationToURL.get (annotation);
        Line.Set lineSet = LineTranslations.getTranslations().getLineSet (url, timeStamp);
        return lineSet.getOriginalLineNumber (line) + 1;
    }

    /**
     * Updates timeStamp for gived url.
     *
     * @param timeStamp time stamp to be updated
     * @param url an url
     */
    public void updateTimeStamp (Object timeStamp, String url) {
        LineTranslations.getTranslations().updateTimeStamp(timeStamp, url);
    }

    /**
     * Returns number of line currently selected in editor or <code>-1</code>.
     *
     * @return number of line currently selected in editor or <code>-1</code>
     */
    public int getCurrentLineNumber () {
        return contextDispatcher.getCurrentLineNumber();
    }

    /**
     * Returns number of line currently selected in editor or <code>-1</code>.
     *
     * @return number of line currently selected in editor or <code>-1</code>
     */
    public int getCurrentOffset () {
        JEditorPane ep = contextDispatcher.getCurrentEditor();
        if (ep == null) return -1;
        Caret caret = ep.getCaret ();
        if (caret == null) return -1;
        return caret.getDot();
    }

    /**
     * Returns name of class currently selected in editor or empty string.
     *
     * @return name of class currently selected in editor or empty string
     */
    public String getCurrentClassName () {
        String currentClass = getCurrentElement(ElementKind.CLASS);
        if (currentClass == null) return "";
        else return currentClass;
    }

    /**
     * Returns name of class recently selected in editor or empty string.
     *
     * @return name of class recently selected in editor or empty string
     */
    public String getMostRecentClassName () {
        String clazz = getMostRecentElement(ElementKind.CLASS);
        if (clazz == null) return "";
        else return clazz;
    }

    /**
     * Returns URL of source currently selected in editor or empty string.
     *
     * @return URL of source currently selected in editor or empty string
     */
    public String getCurrentURL () {
        return contextDispatcher.getCurrentURLAsString();
    }

    /**
     * Returns name of method currently selected in editor or empty string.
     *
     * @return name of method currently selected in editor or empty string
     */
    public String getCurrentMethodName () {
        String currentMethod = getCurrentElement(ElementKind.METHOD);
        if (currentMethod == null) return "";
        else return currentMethod;
    }

    /**
     * Returns name of method recently selected in editor or empty string.
     *
     * @return name of method recently selected in editor or empty string
     */
    public String getMostRecentMethodName () {
        String method = getMostRecentElement(ElementKind.METHOD);
        if (method == null) return "";
        else return method;
    }

    /**
     * Returns signature of method currently selected in editor or null.
     *
     * @return signature of method currently selected in editor or null
     */
    public String getCurrentMethodSignature () {
        final Element[] elementPtr = new Element[] { null };
        try {
            getCurrentElement(ElementKind.METHOD, elementPtr);
        } catch (final java.awt.IllegalComponentStateException icse) {
            throw new java.awt.IllegalComponentStateException() {
                @Override
                public String getMessage() {
                    icse.getMessage();
                    return createSignature((ExecutableElement) elementPtr[0]);
                }
            };
        }
        if (elementPtr[0] != null) {
            return createSignature((ExecutableElement) elementPtr[0]);
        } else {
            return null;
        }
    }

    public String getMostRecentMethodSignature () {
        final Element[] elementPtr = new Element[] { null };
        try {
            getMostRecentElement(ElementKind.METHOD, elementPtr);
        } catch (final java.awt.IllegalComponentStateException icse) {
            throw new java.awt.IllegalComponentStateException() {
                @Override
                public String getMessage() {
                    icse.getMessage();
                    return createSignature((ExecutableElement) elementPtr[0]);
                }
            };
        }
        if (elementPtr[0] != null) {
            return createSignature((ExecutableElement) elementPtr[0]);
        } else {
            return null;
        }
    }

    /**
     * Returns name of field currently selected in editor or <code>null</code>.
     *
     * @return name of field currently selected in editor or <code>null</code>
     */
    public String getCurrentFieldName () {
        String currentField = getCurrentElement(ElementKind.FIELD);
        if (currentField == null) return "";
        else return currentField;
        //return getSelectedIdentifier ();
    }

    /**
     * Returns name of field recently selected in editor or <code>null</code>.
     *
     * @return name of field recently selected in editor or <code>null</code>
     */
    public String getMostRecentFieldName () {
        String field = getMostRecentElement(ElementKind.FIELD);
        if (field == null) return "";
        else return field;
    }


    /**
     * Returns identifier currently selected in editor or <code>null</code>.
     *
     * @return identifier currently selected in editor or <code>null</code>
     */
    public String getSelectedIdentifier () {
        JEditorPane ep = contextDispatcher.getCurrentEditor ();
        if (ep == null) return null;
        String s = ep.getSelectedText ();
        if (s == null) return null;
        if (Utilities.isJavaIdentifier (s)) return s;
        return null;
    }

    /**
     * Returns method name currently selected in editor or empty string.
     *
     * @return method name currently selected in editor or empty string
     */
    public String getSelectedMethodName () {
        if (SwingUtilities.isEventDispatchThread()) {
            return getSelectedMethodName_();
        } else {
            final String[] mn = new String[1];
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        mn[0] = getSelectedMethodName_();
                    }
                });
            } catch (InvocationTargetException ex) {
                ErrorManager.getDefault().notify(ex.getTargetException());
            } catch (InterruptedException ex) {
                // interrupted, ignored.
            }
            return mn[0];
        }
    }

    private String getSelectedMethodName_() {
        JEditorPane ep = contextDispatcher.getCurrentEditor ();
        if (ep == null) return "";
        StyledDocument doc = (StyledDocument) ep.getDocument ();
        if (doc == null) return "";
        int offset = ep.getCaret ().getDot ();
        String t = null;
//        if ( (ep.getSelectionStart () <= offset) &&
//             (offset <= ep.getSelectionEnd ())
//        )   t = ep.getSelectedText ();
//        if (t != null) return t;

        int line = NbDocument.findLineNumber (
            doc,
            offset
        );
        int col = NbDocument.findLineColumn (
            doc,
            offset
        );
        try {
            javax.swing.text.Element lineElem =
                org.openide.text.NbDocument.findLineRootElement (doc).
                getElement (line);

            if (lineElem == null) return "";
            int lineStartOffset = lineElem.getStartOffset ();
            int lineLen = lineElem.getEndOffset () - lineStartOffset;
            // t contains current line in editor
            t = doc.getText (lineStartOffset, lineLen);

            int identStart = col;
            while ( identStart > 0 &&
                    Character.isJavaIdentifierPart (
                        t.charAt (identStart - 1)
                    )
            )   identStart--;

            int identEnd = col;
            while (identEnd < lineLen &&
                   Character.isJavaIdentifierPart (t.charAt (identEnd))
            ) {
                identEnd++;
            }
            int i = t.indexOf ('(', identEnd);
            if (i < 0) return "";
            if (t.substring (identEnd, i).trim ().length () > 0) return "";

            if (identStart == identEnd) return "";
            return t.substring (identStart, identEnd);
        } catch (javax.swing.text.BadLocationException ex) {
            return "";
        }
    }

    private static TypeElement getTypeElement(CompilationController ci,
                                              String binaryName,
                                              String[] classExcludeNames) {
        ClassScanner cs = new ClassScanner(ci.getTrees(), ci.getElements(),
                                           binaryName, classExcludeNames);
        TypeElement te = cs.scan(ci.getCompilationUnit(), null);
        if (te != null) {
            return te;
        } else {
            return null;
        }
    }

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
    public int getFieldLineNumber (
        String url,
        final String className,
        final String fieldName
    ) {
        final DataObject dataObject = getDataObject (url);
        if (dataObject == null) return -1;
        Future<Integer> fi = getFieldLineNumber(dataObject.getPrimaryFile(), className, fieldName);
        if (fi == null) {
            return -1;
        }
        try {
            return fi.get();
        } catch (InterruptedException ex) {
            return -1;
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
            return -1;
        }
    }

    /**
     * @param fo
     * @param className
     * @param fieldName
     * @return <code>null</code> or Future with the line number
     */
    static Future<Integer> getFieldLineNumber (
        FileObject fo,
        final String className,
        final String fieldName
    ) {
        JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) return null;
        final int[] result = new int[] {-1};
        final StyledDocument doc = findDocument(fo);
        if (doc == null) {
            return null;
        }

        try {
            final Future f = ParserManager.parseWhenScanFinished(Collections.singleton(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    CompilationController ci = retrieveController(resultIterator, doc);
                    if (ci == null) return;
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {//TODO: ELEMENTS_RESOLVED may be sufficient
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    Elements elms = ci.getElements();
                    TypeElement classElement = getTypeElement(ci, className, null);
                    if (classElement == null) return ;
                    if (fieldName == null) {
                        // If no field name is provided, just find the beginning of the class:
                        SourcePositions positions =  ci.getTrees().getSourcePositions();
                        Tree tree = ci.getTrees().getTree(classElement);
                        int pos = (int)positions.getStartPosition(ci.getCompilationUnit(), tree);
                        if (pos == Diagnostic.NOPOS) {
                            ErrorManager.getDefault().log(ErrorManager.WARNING,
                                    "No position for tree "+tree+" in "+className);
                            return;
                        }
                        int l = doc.getLength();
                        while (pos < l && doc.getText(pos, 1).charAt(0) != '{') {
                            pos++;
                        }
                        result[0] = NbDocument.findLineNumber(doc, pos) + 2;
                        return ;
                    }
                    List classMemberElements = elms.getAllMembers(classElement);
                    for (Iterator it = classMemberElements.iterator(); it.hasNext(); ) {
                        Element elm = (Element) it.next();
                        if (elm.getKind() == ElementKind.FIELD) {
                            String name = ((VariableElement) elm).getSimpleName().toString();
                            if (name.equals(fieldName)) {
                                SourcePositions positions =  ci.getTrees().getSourcePositions();
                                Tree tree = ci.getTrees().getTree(elm);
                                int pos = (int)positions.getStartPosition(ci.getCompilationUnit(), tree);
                                if (pos == Diagnostic.NOPOS) {
                                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                                            "No position for tree "+tree+" of element "+elm+" in "+className);
                                    continue;
                                }
                                result[0] = NbDocument.findLineNumber(doc, pos) + 1;
                                //return elms.getSourcePosition(elm).getLine();
                            }
                        }
                    }
                }
            });
            if (!f.isDone()) {
                return new Future<Integer>() {

                    public boolean cancel(boolean mayInterruptIfRunning) {
                        return f.cancel(mayInterruptIfRunning);
                    }

                    public boolean isCancelled() {
                        return f.isCancelled();
                    }

                    public boolean isDone() {
                        return f.isDone();
                    }

                    public Integer get() throws InterruptedException, ExecutionException {
                        f.get();
                        return result[0];
                    }

                    public Integer get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        f.get(timeout, unit);
                        return result[0];
                    }

                };
            }
        } catch (ParseException pex) {
            ErrorManager.getDefault().notify(pex);
            return null;
        }
        return new DoneFuture<Integer>(result[0]);
    }

    /**
     * Returns line number of given method in given class.
     *
     * @param url the url of file the class is deined in
     * @param className the name of class (or innerclass) the method is
     *                  defined in
     * @param methodName the name of method
     * @param methodSignature the JNI-style signature of the method.
     *        If <code>null</code>, then the first method found is returned.
     *
     * @return line number or -1
     */
    @Override
    public int getMethodLineNumber (
        String url,
        final String className,
        final String methodName,
        final String methodSignature
    ) {
        final DataObject dataObject = getDataObject (url);
        if (dataObject == null) return -1;
        Future<int[]> flns = getMethodLineNumbers(dataObject.getPrimaryFile(), className, null, methodName, methodSignature);
        if (flns == null) {
            return -1;
        }
        int[] lns;
        try {
            lns = flns.get();
        } catch (InterruptedException ex) {
            return -1;
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
            return -1;
        }
        if (lns.length == 0) {
            return -1;
        } else {
            return lns[0];
        }
    }

    /**
     * @param fo
     * @param className
     * @param classExcludeNames
     * @param methodName
     * @param methodSignature
     * @return <code>null</code> or Future with line numbers
     */
    static Future<int[]> getMethodLineNumbers(
        FileObject fo,
        final String className,
        final String[] classExcludeNames,
        final String methodName,
        final String methodSignature
    ) {
        JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) return null;
        final List<Integer> result = new ArrayList<Integer>();
        final StyledDocument doc = findDocument(fo);
        if (doc == null) return null;
        try {
            final Future f = ParserManager.parseWhenScanFinished(Collections.singleton(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    CompilationController ci = retrieveController(resultIterator, doc);
                    if (ci == null) return;
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {//TODO: ELEMENTS_RESOLVED may be sufficient
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    TypeElement classElement = getTypeElement(ci, className, classExcludeNames);
                    if (classElement == null) return ;
                    List classMemberElements = ci.getElements().getAllMembers(classElement);
                    for (Iterator it = classMemberElements.iterator(); it.hasNext(); ) {
                        Element elm = (Element) it.next();
                        if (elm.getKind() == ElementKind.METHOD || elm.getKind() == ElementKind.CONSTRUCTOR) {
                            String name;
                            if (elm.getKind() == ElementKind.CONSTRUCTOR && !methodName.equals("<init>")) {
                                name = elm.getEnclosingElement().getSimpleName().toString();
                            } else {
                                name = elm.getSimpleName().toString();
                            }
                            if (name.equals(methodName)) {
                                if (methodSignature == null || egualMethodSignatures(methodSignature, createSignature((ExecutableElement) elm))) {
                                    SourcePositions positions =  ci.getTrees().getSourcePositions();
                                    Tree tree = ci.getTrees().getTree(elm);
                                    if (tree == null) {
                                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                                "Null tree for element "+elm+" in "+className);
                                        continue;
                                    }
                                    int pos = (int)positions.getStartPosition(ci.getCompilationUnit(), tree);
                                    if (pos == Diagnostic.NOPOS) {
                                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                                "No position for tree "+tree+" of element "+elm+" in "+className);
                                        continue;
                                    }
                                    { // Find the method name
                                        int origPos = pos;
                                        if (tree.getKind() == Tree.Kind.METHOD) {
                                            MethodTree mt = (MethodTree) tree;
                                            ModifiersTree modt = mt.getModifiers();
                                            if (modt != null) {
                                                List<? extends AnnotationTree> annotations = modt.getAnnotations();
                                                if (annotations != null && annotations.size() > 0) {
                                                    pos = (int) positions.getEndPosition(ci.getCompilationUnit(), annotations.get(annotations.size() - 1));
                                                    if (pos == Diagnostic.NOPOS) {
                                                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                                                "No position for tree "+annotations.get(annotations.size() - 1)+" in "+className);
                                                        continue;
                                                    }
                                                }
                                            }
                                        }
                                        String text = ci.getText();
                                        int l = text.length();
                                        char c = 0;
                                        while (pos < l && (c = text.charAt(pos)) != '(' && c != ')') pos++;
                                        if (pos >= l) {
                                            // We went somewhere wrong. Re-initialize original values
                                            c = 0;
                                            pos = origPos;
                                        }
                                        if (c == '(') {
                                            pos--;
                                            while (pos > 0 && Character.isWhitespace(text.charAt(pos))) pos--;
                                        }
                                    }
                                    result.add(new Integer(NbDocument.findLineNumber(doc, pos) + 1));
                                }
                            }
                        }
                    }
                }
            });
            if (!f.isDone()) {
                return new Future<int[]>() {

                    public boolean cancel(boolean mayInterruptIfRunning) {
                        return f.cancel(mayInterruptIfRunning);
                    }

                    public boolean isCancelled() {
                        return f.isCancelled();
                    }

                    public boolean isDone() {
                        return f.isDone();
                    }

                    public int[] get() throws InterruptedException, ExecutionException {
                        f.get();
                        return getResultArray();
                    }

                    public int[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        f.get(timeout, unit);
                        return getResultArray();
                    }

                    private int[] getResultArray() {
                        final int[] resultArray = new int[result.size()];
                        for (int i = 0; i < resultArray.length; i++) {
                            resultArray[i] = result.get(i).intValue();
                        }
                        return resultArray;
                    }

                };
            }
        } catch (ParseException pex) {
            ErrorManager.getDefault().notify(pex);
            return null;
        }
        int[] resultArray = new int[result.size()];
        for (int i = 0; i < resultArray.length; i++) {
            resultArray[i] = result.get(i).intValue();
        }
        return new DoneFuture<int[]>(resultArray);
    }

    private static boolean egualMethodSignatures(String s1, String s2) {
        int i = s1.lastIndexOf(")");
        if (i > 0) s1 = s1.substring(0, i);
        i = s2.lastIndexOf(")");
        if (i > 0) s2 = s2.substring(0, i);
        return s1.equals(s2);
    }

    /**
     * @param fo
     * @param className
     * @param classExcludeNames
     * @return <code>null</code> or Future with line number
     */
    static Future<Integer> getClassLineNumber(
        FileObject fo,
        final String className,
        final String[] classExcludeNames
    ) {
        JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) return null;
        final Integer[] result = new Integer[] { null };
        final StyledDocument doc = findDocument(fo);
        if (doc == null) return null;
        try {
            final Future f = ParserManager.parseWhenScanFinished(Collections.singleton(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    CompilationController ci = retrieveController(resultIterator, doc);
                    if (ci == null) return;
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {//TODO: ELEMENTS_RESOLVED may be sufficient
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    TypeElement classElement = getTypeElement(ci, className, classExcludeNames);
                    if (classElement == null) return ;
                    SourcePositions positions =  ci.getTrees().getSourcePositions();
                    Tree tree = ci.getTrees().getTree(classElement);
                    if (tree == null) {
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "Null tree for element "+classElement+" in "+className);
                        return;
                    }
                    int pos = (int)positions.getStartPosition(ci.getCompilationUnit(), tree);
                    if (pos == Diagnostic.NOPOS) {
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "No position for tree "+tree+" of element "+classElement+" ("+className+")");
                        return;
                    }
                    if (tree.getKind() == Kind.CLASS) {
                        boolean shifted = false;
                        ModifiersTree mtree = ((ClassTree) tree).getModifiers();
                        for (AnnotationTree atree : mtree.getAnnotations()) {
                            int aend = (int) positions.getEndPosition(ci.getCompilationUnit(), atree);
                            if (aend != Diagnostic.NOPOS && pos < aend) {
                                shifted = true;
                                pos = aend + 1;
                            }
                        }
                        if (shifted) {
                            String text = ci.getText();
                            int l = text.length();
                            while (pos < l && Character.isWhitespace(text.charAt(pos))) {
                                pos++;
                            }
                        }
                    }
                    result[0] = new Integer(NbDocument.findLineNumber(doc, pos) + 1);
                }
            });
            if (!f.isDone()) {
                return new Future<Integer>() {

                    public boolean cancel(boolean mayInterruptIfRunning) {
                        return f.cancel(mayInterruptIfRunning);
                    }

                    public boolean isCancelled() {
                        return f.isCancelled();
                    }

                    public boolean isDone() {
                        return f.isDone();
                    }

                    public Integer get() throws InterruptedException, ExecutionException {
                        f.get();
                        return result[0];
                    }

                    public Integer get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        f.get(timeout, unit);
                        return result[0];
                    }

                };
            }
        } catch (ParseException pex) {
            ErrorManager.getDefault().notify(pex);
            return null;
        }
        return new DoneFuture<Integer>(result[0]);
    }

    /** @return declared class name
     */
    public String getCurrentClassDeclaration() {
        FileObject fo = contextDispatcher.getCurrentFile();
        if (fo == null) return null;
        JEditorPane ep = contextDispatcher.getCurrentEditor();
        JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) return null;
        final int currentOffset = (ep == null) ? 0 : ep.getCaretPosition();
        //final int currentOffset = org.netbeans.editor.Registry.getMostActiveComponent().getCaretPosition();
        final String[] currentClassPtr = new String[] { null };
        final Future<Void> scanFinished;
        try {
            scanFinished = js.runWhenScanFinished(new CancellableTask<CompilationController>() {
                public void cancel() {
                }
                public void run(CompilationController ci) throws Exception {
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {//TODO: ELEMENTS_RESOLVED may be sufficient
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    int offset = currentOffset;
                    //Scope scope = ci.getTreeUtilities().scopeFor(offset);
                    String text = ci.getText();
                    int l = text.length();
                    char c = 0;
                    while (offset < l && (c = text.charAt(offset)) != '{' && c != '}' && c != '\n' && c != '\r') offset++;
                    if (offset >= l) {
                        return ;
                    }
                    offset--;
                    TreePath path = ci.getTreeUtilities().pathFor(offset);
                    Tree tree;
                    do {
                        tree = path.getLeaf();
                        if (tree.getKind() != Tree.Kind.CLASS) {
                            path = path.getParentPath();
                            if (path == null) {
                                break;
                            }
                        } else {
                            break;
                        }
                    } while (true);
                    if (tree.getKind() == Tree.Kind.CLASS) {
                        SourcePositions positions =  ci.getTrees().getSourcePositions();
                        int pos = (int) positions.getStartPosition(ci.getCompilationUnit(), tree);
                        if (pos == Diagnostic.NOPOS) {
                            return ; // We do not know where we are!
                        }
                        if (offset < pos) {
                            return ; // We are before the class declaration!
                        }
                        int hend = getHeaderEnd((ClassTree) tree, positions, ci.getCompilationUnit());
                        if (hend > 0) {
                            pos = hend;
                        }
                        while (pos < l && text.charAt(pos) != '{') pos++;
                        if (pos < offset) { // We are after the class declaration!
                            return ;
                        }
                        Element el = ci.getTrees().getElement(ci.getTrees().getPath(ci.getCompilationUnit(), tree));
                        if (el != null && (el.getKind() == ElementKind.CLASS || el.getKind() == ElementKind.INTERFACE)) {
                            currentClassPtr[0] = ElementUtilities.getBinaryName((TypeElement) el);
                        }
                    }
                }

                private int getHeaderEnd(ClassTree classTree, SourcePositions positions, CompilationUnitTree compilationUnit) {
                    int max = -1;
                    int pos = (int) positions.getEndPosition(compilationUnit, classTree.getExtendsClause());
                    if (pos != Diagnostic.NOPOS) {
                        max = Math.max(max, pos);
                    }
                    pos = (int) positions.getEndPosition(compilationUnit, classTree.getModifiers());
                    if (pos != Diagnostic.NOPOS) {
                        max = Math.max(max, pos);
                    }
                    for (Tree t : classTree.getImplementsClause()) {
                        pos = (int) positions.getEndPosition(compilationUnit, t);
                        if (pos != Diagnostic.NOPOS) {
                            max = Math.max(max, pos);
                        }
                    }
                    for (Tree t : classTree.getTypeParameters()) {
                        pos = (int) positions.getEndPosition(compilationUnit, t);
                        if (pos != Diagnostic.NOPOS) {
                            max = Math.max(max, pos);
                        }
                    }
                    return max;
                }
            }, true);
            if (!scanFinished.isDone()) {
                if (java.awt.EventQueue.isDispatchThread()) {
                    // Hack: We should not wait for the scan in AWT!
                    //       Thus we throw IllegalComponentStateException,
                    //       which returns the data upon call to getMessage()
                    throw new java.awt.IllegalComponentStateException() {

                        private void waitScanFinished() {
                            try {
                                scanFinished.get();
                            } catch (InterruptedException iex) {
                            } catch (java.util.concurrent.ExecutionException eex) {
                                ErrorManager.getDefault().notify(eex);
                            }
                        }

                        @Override
                        public String getMessage() {
                            waitScanFinished();
                            return currentClassPtr[0];
                        }

                    };
                } else {
                    try {
                        scanFinished.get();
                    } catch (InterruptedException iex) {
                        return null;
                    } catch (java.util.concurrent.ExecutionException eex) {
                        ErrorManager.getDefault().notify(eex);
                        return null;
                    }
                }
            }
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ioex);
            return null;
        }
        return currentClassPtr[0];
    }

    /** @return { "method name", "method signature", "enclosing class name" }
     */
    @Override
    public String[] getCurrentMethodDeclaration() {
        FileObject fo = contextDispatcher.getCurrentFile();
        if (fo == null) return null;
        JEditorPane ep = contextDispatcher.getCurrentEditor();
        JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) return null;
        final int currentOffset = (ep == null) ? 0 : ep.getCaretPosition();
        final String[] currentMethodPtr = new String[] { null, null, null };
        final Future<Void> scanFinished;
        try {
            scanFinished = js.runWhenScanFinished(new CancellableTask<CompilationController>() {
                public void cancel() {
                }
                public void run(CompilationController ci) throws Exception {
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {//TODO: ELEMENTS_RESOLVED may be sufficient
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    int offset = currentOffset;
                    //Scope scope = ci.getTreeUtilities().scopeFor(offset);
                    String text = ci.getText();
                    int l = text.length();
                    char c = 0;
                    while (offset < l && (c = text.charAt(offset)) != '(' && c != ')' && c != '\n' && c != '\r') offset++;
                    if (offset >= l) {
                        return ;
                    }
                    if (c == '(') offset--;

                    Tree tree = ci.getTreeUtilities().pathFor(offset).getLeaf();
                    if (tree.getKind() == Tree.Kind.METHOD) {
                        Element el = ci.getTrees().getElement(ci.getTrees().getPath(ci.getCompilationUnit(), tree));

                        //Element el = ci.getTrees().getElement(ci.getTreeUtilities().pathFor(offset));
                        if (el != null && (el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.CONSTRUCTOR)) {
                            currentMethodPtr[0] = el.getSimpleName().toString();
                            if (currentMethodPtr[0].equals("<init>")) {
                                // The constructor name is the class name:
                                currentMethodPtr[0] = el.getEnclosingElement().getSimpleName().toString();
                            }
                            currentMethodPtr[1] = createSignature((ExecutableElement) el);
                            Element enclosingClassElement = el;
                            TypeElement te = null; // SourceUtils.getEnclosingTypeElement(el);
                            while (enclosingClassElement != null) {
                                ElementKind kind = enclosingClassElement.getKind();
                                if (kind == ElementKind.CLASS || kind == ElementKind.INTERFACE) {
                                    te = (TypeElement) enclosingClassElement;
                                    break;
                                } else {
                                    enclosingClassElement = enclosingClassElement.getEnclosingElement();
                                }
                            }
                            if (te != null) {
                                currentMethodPtr[2] = ElementUtilities.getBinaryName(te);
                            }
                        }
                    }
                }
            }, true);
            if (!scanFinished.isDone()) {
                if (java.awt.EventQueue.isDispatchThread()) {
                    // Hack: We should not wait for the scan in AWT!
                    //       Thus we throw IllegalComponentStateException,
                    //       which returns the data upon call to getMessage()
                    throw new java.awt.IllegalComponentStateException() {

                        private void waitScanFinished() {
                            try {
                                scanFinished.get();
                            } catch (InterruptedException iex) {
                            } catch (java.util.concurrent.ExecutionException eex) {
                                ErrorManager.getDefault().notify(eex);
                            }
                        }

                        @Override
                        public String getMessage() {
                            waitScanFinished();
                            return currentMethodPtr[0];
                        }

                        @Override
                        public String getLocalizedMessage() {
                            waitScanFinished();
                            return currentMethodPtr[1];
                        }
                    };
                } else {
                    try {
                        scanFinished.get();
                    } catch (InterruptedException iex) {
                        return null;
                    } catch (java.util.concurrent.ExecutionException eex) {
                        ErrorManager.getDefault().notify(eex);
                        return null;
                    }
                }
            }
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ioex);
            return null;
        }
        if (currentMethodPtr[0] != null) {
            return currentMethodPtr;
        } else {
            return null;
        }
    }


    private static String createSignature(ExecutableElement elm) {
        StringBuilder signature = new StringBuilder("(");
        for (VariableElement param : elm.getParameters()) {
            String paramType = param.asType().toString();
            signature.append(getSignature(paramType));
        }
        signature.append(')');
        String returnType = elm.getReturnType().toString();
        signature.append(getSignature(returnType));
        return signature.toString();
    }

    private static String getSignature(String javaType) {
        if (javaType.equals("boolean")) {
            return "Z";
        } else if (javaType.equals("byte")) {
            return "B";
        } else if (javaType.equals("char")) {
            return "C";
        } else if (javaType.equals("short")) {
            return "S";
        } else if (javaType.equals("int")) {
            return "I";
        } else if (javaType.equals("long")) {
            return "J";
        } else if (javaType.equals("float")) {
            return "F";
        } else if (javaType.equals("double")) {
            return "D";
        } else if (javaType.endsWith("[]")) {
            return "["+getSignature(javaType.substring(0, javaType.length() - 2));
        } else {
            return "L"+javaType.replace('.', '/')+";";
        }
    }

    /**
     * Returns binary class name for given url and line number or null.
     *
     * @param url a url
     * @param lineNumber a line number
     *
     * @return binary class name for given url and line number or null
     */
    public String getClassName (
        String url,
        int lineNumber
    ) {
        DataObject dataObject = getDataObject (url);
        if (dataObject == null) return null;
        JavaSource js = JavaSource.forFileObject(dataObject.getPrimaryFile());
        if (js == null) return "";
        final StyledDocument doc = findDocument(dataObject);
        if (doc == null) {
            return "";
        }
        try {
            final int offset = NbDocument.findLineOffset(doc, lineNumber - 1);
            final String[] result = new String[] {""};
            ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    CompilationController ci = retrieveController(resultIterator, doc);
                    if (ci == null) return;
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {//TODO: ELEMENTS_RESOLVED may be sufficient
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    TreePath p = ci.getTreeUtilities().pathFor(offset);
                    while  (p != null && p.getLeaf().getKind() != Kind.CLASS) {
                        p = p.getParentPath();
                    }
                    TypeElement te;
                    if (p != null) {
                        te = (TypeElement) ci.getTrees().getElement(p);
                    } else {
                        Scope scope = ci.getTreeUtilities().scopeFor(offset);
                        te = scope.getEnclosingClass();
                    }
                    if (te != null) {
                        result[0] = ElementUtilities.getBinaryName(te);
                    } else {
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "No enclosing class for "+ci.getFileObject()+", offset = "+offset);
                    }
                }
            });
            return result[0];
        } catch (ParseException pex) {
            ErrorManager.getDefault().notify(pex);
            return "";
        } catch (IndexOutOfBoundsException ioobex) {
            //XXX: log the exception?
            return null;
        }
        /*
        SourceCookie.Editor sc = (SourceCookie.Editor) dataObject.getCookie
            (SourceCookie.Editor.class);
        if (sc == null) return null;
        StyledDocument sd = null;
        try {
            sd = sc.openDocument ();
        } catch (IOException ex) {
        }
        if (sd == null) return null;
        int offset;
        try {
            offset = NbDocument.findLineOffset (sd, lineNumber - 1);
        } catch (IndexOutOfBoundsException ioobex) {
            return null;
        }
        Element element = sc.findElement (offset);

        if (element == null) return "";
        if (element instanceof ClassElement)
            return getClassName ((ClassElement) element);
        if (element instanceof ConstructorElement)
            return getClassName (((ConstructorElement) element).getDeclaringClass ());
        if (element instanceof FieldElement)
            return getClassName (((FieldElement) element).getDeclaringClass ());
        if (element instanceof InitializerElement)
            return getClassName (((InitializerElement) element).getDeclaringClass());
        return "";
         */
    }

    private CompilationController getPreferredCompilationController(FileObject fo, JavaSource js) throws IOException {
        CompilationController preferredCI;
        if (fo != null) {
            if (JavaSource.forFileObject(fo) == null) {
                // No JavaSource, we can not ask for a compilation controller
                return null;
            }
            Date lastModified = fo.lastModified();
            Date storedStamp = null;
            JavaSourceUtil.Handle handle;
            synchronized (sourceHandles) {
                handle = sourceHandles.get(js);
                storedStamp = sourceModifStamps.get(js);
            }
            if (handle == null || (storedStamp != null && lastModified.after(storedStamp))) {
                handle = JavaSourceUtil.createControllerHandle(fo, handle);
                synchronized (sourceHandles) {
                    sourceHandles.put(js, handle);
                    sourceModifStamps.put(js, lastModified);
                }
            }
            preferredCI = (CompilationController) handle.getCompilationController();
        } else {
            preferredCI = null;
        }
        return preferredCI;
    }

    @Override
    public Operation[] getOperations(String url, final int lineNumber,
                                     final BytecodeProvider bytecodeProvider) {
        DataObject dataObject = getDataObject (url);
        if (dataObject == null) return null;
        JavaSource js = JavaSource.forFileObject(dataObject.getPrimaryFile());
        if (js == null) return null;
        EditorCookie ec = (EditorCookie) dataObject.getCookie(EditorCookie.class);
        if (ec == null) return null;
        final StyledDocument doc;
        try {
            doc = ec.openDocument();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
            return null;
        }
        final int offset = findLineOffset(doc, (int) lineNumber);
        final Object[] result = new Object[1];
        //long t1, t2, t3, t4;
        //t1 = System.nanoTime();
        if (SourceUtils.isScanInProgress()) {
            try {
                ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        CompilationController ci = retrieveController(resultIterator, doc);
                        if (ci == null) return;
                        result[0] = computeOperations(ci, offset, lineNumber, bytecodeProvider);
                    }
                });
            } catch (ParseException pex) {
                ErrorManager.getDefault().notify(pex);
                return null;
            }
        } else {
            try {
                CompilationController ci = getPreferredCompilationController(dataObject.getPrimaryFile(), js);
                if (ci == null) {
                    return new Operation[] {};
                }
                synchronized (ci) {
                    result[0] = computeOperations(ci, offset, lineNumber, bytecodeProvider);
                }
                //t4 = System.nanoTime();
                //System.err.println("PARSE TIMES 2: "+(t2-t1)/1000000+", "+(t3-t2)/1000000+", "+(t4-t3)/1000000+" TOTAL: "+(t4-t1)/1000000+" ms.");
            } catch (IOException ioex) {
                ErrorManager.getDefault().notify(ioex);
                return null;
            }
        }
        return (Operation[])result[0];
    }

    private void assignNextOperations(Tree methodTree,
                                      CompilationUnitTree cu,
                                      CompilationController ci,
                                      BytecodeProvider bytecodeProvider,
                                      List<Tree> treeNodes,
                                      ExpressionScanner.ExpressionsInfo info,
                                      Map<Tree, Operation> nodeOperations) {
        int length = treeNodes.size();
        for (int treeIndex = 0; treeIndex < length; treeIndex++) {
            Tree node = treeNodes.get(treeIndex);
            Set<Tree> nextNodes = info.getNextExpressions(node);
            if (nextNodes != null) {
                EditorContext.Operation op = nodeOperations.get(node);
                if (op == null) {
                    for (int backIndex = treeIndex - 1; backIndex >= 0; backIndex--) {
                        node = treeNodes.get(backIndex);
                        op = nodeOperations.get(node);
                        if (op != null) break;
                    }
                }
                if (op != null) {
                    for (Tree t : nextNodes) {
                        EditorContext.Operation nextOp = nodeOperations.get(t);
                        if (nextOp == null) {
                            SourcePositions sp = ci.getTrees().getSourcePositions();
                            int treeStartLine =
                                    (int) cu.getLineMap().getLineNumber(
                                        sp.getStartPosition(cu, t));
                            if (treeStartLine == Diagnostic.NOPOS) {
                                continue;
                            }
                            ExpressionScanner scanner = new ExpressionScanner(treeStartLine, cu, ci.getTrees().getSourcePositions());
                            ExpressionScanner.ExpressionsInfo newInfo = new ExpressionScanner.ExpressionsInfo();
                            List<Tree> newExpTrees = methodTree.accept(scanner, newInfo);
                            if (newExpTrees == null) {
                                continue;
                            }
                            treeStartLine =
                                    (int) cu.getLineMap().getLineNumber(
                                        sp.getStartPosition(cu, newExpTrees.get(0)));
                            int treeEndLine =
                                    (int) cu.getLineMap().getLineNumber(
                                        sp.getEndPosition(cu, newExpTrees.get(newExpTrees.size() - 1)));

                            if (treeStartLine == Diagnostic.NOPOS || treeEndLine == Diagnostic.NOPOS) {
                                continue;
                            }
                            int[] indexes = bytecodeProvider.indexAtLines(treeStartLine, treeEndLine);
                            Map<Tree, Operation> newNodeOperations = new HashMap<Tree, Operation>();
                            Operation[] newOps = AST2Bytecode.matchSourceTree2Bytecode(
                                    cu,
                                    ci,
                                    newExpTrees, newInfo, bytecodeProvider.byteCodes(),
                                    indexes,
                                    bytecodeProvider.constantPool(),
                                    new OperationCreationDelegateImpl(),
                                    newNodeOperations);
                            nextOp = newNodeOperations.get(t);
                            if (nextOp == null) {
                                // Next operation not found
                                System.err.println("Next operation not found!");
                                continue;
                            }
                        }
                        addNextOperationTo(op, nextOp);
                    }
                }
            }
        }

    }

    /** return the offset of the first non-whitespace character on the line,
               or -1 when the line does not exist
     */
    private static int findLineOffset(StyledDocument doc, int lineNumber) {
        int offset;
        try {
            offset = NbDocument.findLineOffset (doc, lineNumber - 1);
            int offset2 = NbDocument.findLineOffset (doc, lineNumber);
            try {
                String lineStr = doc.getText(offset, offset2 - offset);
                for (int i = 0; i < lineStr.length(); i++) {
                    if (!Character.isWhitespace(lineStr.charAt(i))) {
                        offset += i;
                        break;
                    }
                }
            } catch (BadLocationException ex) {
                // ignore
            }
        } catch (IndexOutOfBoundsException ioobex) {
            return -1;
        }
        return offset;
    }

    @Override
    public MethodArgument[] getArguments(String url, final Operation operation) {
        DataObject dataObject = getDataObject (url);
        if (dataObject == null) return null;
        JavaSource js = JavaSource.forFileObject(dataObject.getPrimaryFile());
        if (js == null) return null;
        final StyledDocument doc = findDocument(dataObject);
        if (doc == null) return null;
        final MethodArgument args[][] = new MethodArgument[1][];
        try {
            ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    CompilationController ci = retrieveController(resultIterator, doc);
                    if (ci == null) return;
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    int offset = operation.getMethodEndPosition().getOffset();
                    Scope scope = ci.getTreeUtilities().scopeFor(offset);
                    Element method = scope.getEnclosingMethod();
                    if (method == null) {
                        return ;
                    }
                    Tree methodTree = ci.getTrees().getTree(method);
                    CompilationUnitTree cu = ci.getCompilationUnit();
                    MethodArgumentsScanner scanner =
                            new MethodArgumentsScanner(offset, cu, ci.getTrees().getSourcePositions(), true,
                                                       new OperationCreationDelegateImpl());
                    args[0] = methodTree.accept(scanner, null);
                    args[0] = scanner.getArguments();
                }
            });
        } catch (ParseException pex) {
            ErrorManager.getDefault().notify(pex);
            return null;
        }
        return args[0];
    }

    @Override
    public MethodArgument[] getArguments(String url, final int methodLineNumber) {
        DataObject dataObject = getDataObject (url);
        if (dataObject == null) return null;
        JavaSource js = JavaSource.forFileObject(dataObject.getPrimaryFile());
        if (js == null) return null;
        final StyledDocument doc = findDocument(dataObject);
        if (doc == null) {
            return null;
        }
        final int offset = findLineOffset(doc, methodLineNumber);
        final MethodArgument args[][] = new MethodArgument[1][];
        try {
            ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    CompilationController ci = retrieveController(resultIterator, doc);
                    if (ci == null) return;
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    Scope scope = ci.getTreeUtilities().scopeFor(offset);
                    Element clazz = scope.getEnclosingClass();
                    if (clazz == null) {
                        return ;
                    }
                    Tree methodTree = ci.getTrees().getTree(clazz);
                    CompilationUnitTree cu = ci.getCompilationUnit();
                    MethodArgumentsScanner scanner =
                            new MethodArgumentsScanner(methodLineNumber, cu, ci.getTrees().getSourcePositions(), false,
                                                       new OperationCreationDelegateImpl());
                    args[0] = methodTree.accept(scanner, null);
                    args[0] = scanner.getArguments();
                }
            });
        } catch (ParseException pex) {
            ErrorManager.getDefault().notify(pex);
            return null;
        }
        return args[0];
    }

    /**
     * Returns list of imports for given source url.
     *
     * @param url the url of source file
     *
     * @return list of imports for given source url
     */
    public String[] getImports (
        String url
    ) {
        DataObject dataObject = getDataObject (url);
        if (dataObject == null) return new String [0];
        JavaSource js = JavaSource.forFileObject(dataObject.getPrimaryFile());
        if (js == null) return new String [0];
        final StyledDocument doc = findDocument(dataObject);
        if (doc == null) {
            return null;
        }
        if (doc == null) return new String [0];
        final List<String> imports = new ArrayList<String>();
        try {
            ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    CompilationController ci = retrieveController(resultIterator, doc);
                    if (ci == null) return;
                    if (ci.toPhase(Phase.PARSED).compareTo(Phase.PARSED) < 0) {
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    List importDecl = ci.getCompilationUnit().getImports();
                    int i = 0;
                    for (Iterator it = importDecl.iterator(); it.hasNext(); i++) {
                        ImportTree itree = (ImportTree) it.next();
                        String importStr = itree.getQualifiedIdentifier().toString();
                        imports.add(importStr);
                    }
                }
            });
        } catch (ParseException pex) {
            ErrorManager.getDefault().notify(pex);
            return new String[0];
        }
        return imports.toArray(new String[0]);
        /*
        SourceCookie.Editor sc = (SourceCookie.Editor) dataObject.getCookie
            (SourceCookie.Editor.class);
        if (sc == null) return new String [0];
        Import[] is = sc.getSource ().getImports ();
        int i, k = is.length;
        String[] is2 = new String [k];
        for (i = 0; i < k; i++)
            is2 [i] = is [i].getIdentifier ().getFullName ();
        return is2;
         */
    }

    private JavaSource getJavaSource(SourcePathProvider sp) {
        String[] roots = sp.getOriginalSourceRoots();
        List<FileObject> sourcePathFiles = new ArrayList<FileObject>();
        for (String root : roots) {
            FileObject fo = FileUtil.toFileObject (new java.io.File(root));
            if (fo != null && FileUtil.isArchiveFile (fo)) {
                fo = FileUtil.getArchiveRoot (fo);
            }
            sourcePathFiles.add(fo);
        }
        ClassPath bootPath = ClassPathSupport.createClassPath(new FileObject[] {});
        ClassPath classPath = ClassPathSupport.createClassPath(new FileObject[] {});
        ClassPath sourcePath = ClassPathSupport.createClassPath(sourcePathFiles.toArray(new FileObject[] {}));
        return JavaSource.create(ClasspathInfo.create(bootPath, classPath, sourcePath), new FileObject[] {});
    }

    /**
     * Parse the expression into AST tree and traverse is via the provided visitor.
     *
     * @return the visitor value or <code>null</code>.
     */
    public <R,D> R parseExpression(final String expression, String url, final int line,
                                   final TreePathScanner<R,D> visitor, final D context,
                                   final SourcePathProvider sp) throws InvalidExpressionException {
        JavaSource js = null;
        FileObject fo = null;
        if (url != null) {
            try {
                fo = URLMapper.findFileObject(new URL(url));
                if (fo != null) {
                    js = JavaSource.forFileObject(fo);
                }
            } catch (MalformedURLException ex) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
            }
        }
        if (js == null) {
            js = getJavaSource(sp);
        }
        //long t1, t2, t3, t4;
        //t1 = System.nanoTime();
        try {
            CompilationController ci = getPreferredCompilationController(fo, js);
            //t2 = System.nanoTime();
            final ParseExpressionTask task = new ParseExpressionTask(expression, line, context);
            if (fo != null && SourceUtils.isScanInProgress()) {
                final StyledDocument doc = findDocument(fo);
                if (doc == null) {
                    return null;
                }
                try {
                    ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            CompilationController ci = retrieveController(resultIterator, doc);
                            if (ci != null) {
                                task.run(ci);
                            }
                        }
                    });
                } catch (ParseException pex) {
                    ErrorManager.getDefault().notify(pex);
                    return null;
                }
            } else if (ci == null) {
                js.runUserActionTask(task, false);
            } else {
                try {
                    synchronized (ci) {
                        task.run(ci);
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            }
            TreePath treePath = task.getTreePath();
            Tree tree = task.getTree();
            //t3 = System.nanoTime();
            R retValue;
            if (treePath != null) {
                retValue = visitor.scan(treePath, context);
            } else {
                if (tree == null) {
                    throw new InvalidExpressionException(NbBundle.getMessage(EditorContextImpl.class, "MSG_NoParseNoEval"));
                }
                retValue = tree.accept(visitor, context);
            }
            //t4 = System.nanoTime();
            //System.err.println("PARSE TIMES 1: "+(t2-t1)/1000000+", "+(t3-t2)/1000000+", "+(t4-t3)/1000000+" TOTAL: "+(t4-t1)/1000000+" ms.");
            return retValue;
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ioex);
            return null;
        }
    }

    private static class ParseExpressionTask<D> implements Task<CompilationController> {

        private int line;
        private String expression;
        private D context;
        private TreePath treePath;
        private Tree tree;

        public ParseExpressionTask(String expression, int line, D context) {
            this.expression = expression;
            this.line = line;
            this.context = context;
        }

        public void run(CompilationController ci) throws Exception {
            if (ci.toPhase(Phase.PARSED).compareTo(Phase.PARSED) < 0)
                return ;
            Scope scope = null;
            int offset = 0;
            StyledDocument doc = (StyledDocument) ci.getDocument();
            if (doc != null) {
                offset = findLineOffset(doc, line);
                scope = ci.getTreeUtilities().scopeFor(offset);
            }
            SourcePositions[] sourcePtr = new SourcePositions[] { null };
            // first, try to parse as a block of statements
            tree = ci.getTreeUtilities().parseStatement(
                    "{\n" + expression + ";\n}", // NOI18N
                    sourcePtr
            );
            if (isErroneous(tree)) {
                Tree asBlockTree = tree;
                // when block parsing fails, try to parse an expression
                tree = ci.getTreeUtilities().parseExpression(
                        expression,
                        sourcePtr
                );
                if (isErroneous(tree)) {
                    tree = asBlockTree;
                }
            }
            if (scope != null) {
                ci.getTreeUtilities().attributeTree(tree, scope);
            }
            try {
                //context.setTrees(ci.getTrees());
                java.lang.reflect.Method setTreesMethod =
                        context.getClass().getMethod("setTrees", new Class[] { Trees.class });
                setTreesMethod.invoke(context, ci.getTrees());
            } catch (Exception ex) {}
            try {
                //context.setCompilationUnit(ci.getCompilationUnit());
                java.lang.reflect.Method setCompilationUnitMethod =
                        context.getClass().getMethod("setCompilationUnit", new Class[] { CompilationUnitTree.class });
                setCompilationUnitMethod.invoke(context, ci.getCompilationUnit());
            } catch (Exception ex) {}
            treePath = null;
            try {
                //context.setTrees(ci.getTrees());
                java.lang.reflect.Method setTreePathMethod =
                        context.getClass().getMethod("setTreePath", new Class[] { TreePath.class });
                if (doc != null) {
                    treePath = ci.getTreeUtilities().pathFor(offset);
                    treePath = new TreePath(treePath, tree);
                    setTreePathMethod.invoke(context, treePath);
                }
            } catch (Exception ex) { return;}
        }

        public TreePath getTreePath() {
            return treePath;
        }

        public Tree getTree() {
            return tree;
        }
    }

    private static boolean isErroneous(Tree tree) {

        class TreeChecker extends TreePathScanner<Boolean,Void> {

            @Override
            public Boolean scan(Tree tree, Void p) {
                if (tree == null) {
                    return Boolean.FALSE;
                }
                if (tree.getKind() == Tree.Kind.ERRONEOUS) {
                    return Boolean.TRUE;
                }
                return tree.accept(this, p);
            }

            public Boolean visitErrorneous(ErroneousTree tree, Void p) {
                return Boolean.TRUE;
            }

        }

        Boolean result = new TreeChecker().scan(tree, null);
        return result != null && result.booleanValue();
    }

    /**
     * Adds a property change listener.
     *
     * @param l the listener to add
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
     * Removes a property change listener.
     *
     * @param l the listener to remove
     */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }

    /**
     * Adds a property change listener.
     *
     * @param propertyName the name of property
     * @param l the listener to add
     */
    public void addPropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    ) {
        pcs.addPropertyChangeListener (propertyName, l);
    }

    /**
     * Removes a property change listener.
     *
     * @param propertyName the name of property
     * @param l the listener to remove
     */
    public void removePropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (propertyName, l);
    }


    // private helper methods ..................................................

//    public void fileChanged (FileEvent fe) {
//	pcs.firePropertyChange (PROP_LINE_NUMBER, null, null);
//    }
//
//    public void fileDeleted (FileEvent fe) {}
//    public void fileAttributeChanged (org.openide.filesystems.FileAttributeEvent fe) {}
//    public void fileDataCreated (FileEvent fe) {}
//    public void fileFolderCreated (FileEvent fe) {}
//    public void fileRenamed (org.openide.filesystems.FileRenameEvent fe) {}


    private String getCurrentElement(ElementKind kind) {
        return getCurrentElement(kind, null);
    }

    private String getMostRecentElement(ElementKind kind) {
        return getMostRecentElement(kind, null);
    }

    /** throws IllegalComponentStateException when can not return the data in AWT. */
    private String getCurrentElement(final ElementKind kind, final Element[] elementPtr)
            throws java.awt.IllegalComponentStateException {
        return getCurrentElement(contextDispatcher.getCurrentFile(),
                                 contextDispatcher.getCurrentEditor(),
                                 kind, elementPtr);
    }

    /** throws IllegalComponentStateException when can not return the data in AWT. */
    private String getMostRecentElement(final ElementKind kind, final Element[] elementPtr)
            throws java.awt.IllegalComponentStateException {
        return getCurrentElement(contextDispatcher.getMostRecentFile(),
                                 contextDispatcher.getMostRecentEditor(),
                                 kind, elementPtr);
    }

    /** throws IllegalComponentStateException when can not return the data in AWT. */
    private String getCurrentElement(FileObject fo, JEditorPane ep,
                                     final ElementKind kind, final Element[] elementPtr)
            throws java.awt.IllegalComponentStateException {

        if (fo == null) return null;
        JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) return null;
        final int currentOffset;
        final String selectedIdentifier;
        if (ep != null) {
            String s = ep.getSelectedText ();
            currentOffset = ep.getCaretPosition();
            if (ep.getSelectionStart() > currentOffset || ep.getSelectionEnd() < currentOffset) {
                s = null; // caret outside of the selection
            }
            if (s != null && Utilities.isJavaIdentifier (s)) {
                selectedIdentifier = s;
            } else {
                selectedIdentifier = null;
            }
        } else {
            selectedIdentifier = null;
            currentOffset = 0;
        }

        final String[] currentElementPtr = new String[] { null };
        final Future<Void> scanFinished;
        try {
            scanFinished = js.runWhenScanFinished(new CancellableTask<CompilationController>() {
                public void cancel() {
                }
                public void run(CompilationController ci) throws Exception {
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {//TODO: ELEMENTS_RESOLVED may be sufficient
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    Element el = null;
                    if (kind == ElementKind.CLASS) {
                        boolean isMemberClass = false;
                        if (selectedIdentifier != null) {
                            Tree tree = ci.getTreeUtilities().pathFor(currentOffset).getLeaf();
                            if (tree.getKind() == Tree.Kind.MEMBER_SELECT) {
                                MemberSelectTree mst = (MemberSelectTree) tree;
                                el = ci.getTrees().getElement(ci.getTrees().getPath(ci.getCompilationUnit(), mst.getExpression()));
                                if (el != null) {
                                    TypeMirror tm = el.asType();
                                    if (tm.getKind().equals(TypeKind.DECLARED)) {
                                        currentElementPtr[0] = tm.toString();
                                        isMemberClass = true;
                                    }
                                }
                            }
                        }
                        if (!isMemberClass) {
                            TreePath currentPath = ci.getTreeUtilities().pathFor(currentOffset);
                            Tree tree = currentPath.getLeaf();
                            TypeElement te;
                            if (tree.getKind() == Tree.Kind.CLASS) {
                                te = (TypeElement) ci.getTrees().getElement(currentPath);
                            } else {
                                Scope scope = ci.getTreeUtilities().scopeFor(currentOffset);
                                te = scope.getEnclosingClass();
                            }
                            if (te != null) {
                                currentElementPtr[0] = ElementUtilities.getBinaryName(te);
                            }
                            el = te;
                        }
                    } else if (kind == ElementKind.METHOD) {
                        Scope scope = ci.getTreeUtilities().scopeFor(currentOffset);
                        el = scope.getEnclosingMethod();
                        if (el != null) {
                            currentElementPtr[0] = el.getSimpleName().toString();
                            if (currentElementPtr[0].equals("<init>")) {
                                // The constructor name is the class name:
                                currentElementPtr[0] = el.getEnclosingElement().getSimpleName().toString();
                            }
                        } else {
                            TreePath path = ci.getTreeUtilities().pathFor(currentOffset);
                            Tree tree = path != null ? path.getLeaf() : null;
                            while (tree != null && !(tree instanceof MethodTree || tree instanceof ClassTree)) {
                                path = path.getParentPath();
                                tree = path != null ? path.getLeaf() : null;
                            }
                            if (tree instanceof MethodTree) {
                                String name = ((MethodTree)tree).getName().toString();
                                if (name.equals("<init>")) {
                                    el = scope.getEnclosingClass();
                                    name = el.getSimpleName().toString();
                                }
                                currentElementPtr[0] = name;
                            }
                        }
                    } else if (kind == ElementKind.FIELD) {
                        int offset = currentOffset;

                        if (selectedIdentifier == null) {
                            String text = ci.getText();
                            int l = text.length();
                            char c = 0; // Search for the end of the field declaration
                            while (offset < l && (c = text.charAt(offset)) != ';' && c != ',' && c != '\n' && c != '\r') offset++;
                            if (offset < l && c == ';' || c == ',') { // we have it, but there might be '=' sign somewhere before
                                int endOffset = --offset;
                                int setOffset = -1;
                                while(offset >= 0 && (c = text.charAt(offset)) != ';' && c != ',' && c != '\n' && c != '\r') {
                                    if (c == '=') setOffset = offset;
                                    offset--;
                                }
                                if (setOffset > -1) {
                                    offset = setOffset;
                                } else {
                                    offset = endOffset;
                                }
                                while (offset >= 0 && Character.isWhitespace(text.charAt(offset))) offset--;
                            }
                            if (offset < 0) offset = 0;
                        }
                        Tree tree = ci.getTreeUtilities().pathFor(offset).getLeaf();
                        if (tree.getKind() == Tree.Kind.VARIABLE) {
                            el = ci.getTrees().getElement(ci.getTrees().getPath(ci.getCompilationUnit(), tree));
                            if (el != null && (el.getKind() == ElementKind.FIELD || el.getKind() == ElementKind.ENUM_CONSTANT)) {
                                currentElementPtr[0] = ((VariableTree) tree).getName().toString();
                            }
                        } else if (tree.getKind() == Tree.Kind.IDENTIFIER && selectedIdentifier != null) {
                            IdentifierTree it = (IdentifierTree) tree;
                            String fieldName = it.getName().toString();
                            Scope scope = ci.getTreeUtilities().scopeFor(offset);
                            TypeElement te = scope.getEnclosingClass();
                            List<? extends Element> enclosedElms = te.getEnclosedElements();
                            for (Element elm : enclosedElms) {
                                if (elm.getKind().equals(ElementKind.FIELD) && elm.getSimpleName().contentEquals(fieldName)) {
                                    currentElementPtr[0] = fieldName;
                                    break;
                                }
                            }

                        } else if (tree.getKind() == Tree.Kind.MEMBER_SELECT && selectedIdentifier != null) {
                            MemberSelectTree mst = (MemberSelectTree) tree;
                            String fieldName = mst.getIdentifier().toString();
                            el = ci.getTrees().getElement(ci.getTrees().getPath(ci.getCompilationUnit(), mst.getExpression()));
                            if (el != null && el.asType().getKind().equals(TypeKind.DECLARED)) {
                                List<? extends Element> enclosedElms = ((DeclaredType) el.asType()).asElement().getEnclosedElements();
                                for (Element elm : enclosedElms) {
                                    if (elm.getKind().equals(ElementKind.FIELD) && elm.getSimpleName().contentEquals(fieldName)) {
                                        currentElementPtr[0] = fieldName;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (elementPtr != null) {
                        elementPtr[0] = el;
                    }
                }
            }, true);
            if (!scanFinished.isDone()) {
                if (java.awt.EventQueue.isDispatchThread()) {
                    // Hack: We should not wait for the scan in AWT!
                    //       Thus we throw IllegalComponentStateException,
                    //       which returns the data upon call to getMessage()
                    throw new java.awt.IllegalComponentStateException() {

                        private void waitScanFinished() {
                            try {
                                scanFinished.get();
                            } catch (InterruptedException iex) {
                            } catch (java.util.concurrent.ExecutionException eex) {
                                ErrorManager.getDefault().notify(eex);
                            }
                        }

                        @Override
                        public String getMessage() {
                            waitScanFinished();
                            return currentElementPtr[0];
                        }

                    };
                } else {
                    try {
                        scanFinished.get();
                    } catch (InterruptedException iex) {
                        return null;
                    } catch (java.util.concurrent.ExecutionException eex) {
                        ErrorManager.getDefault().notify(eex);
                        return null;
                    }
                }
            }
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ioex);
            return null;
        }
        return currentElementPtr[0];
    }

    private static DataObject getDataObject (String url) {
        FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
        } catch (MalformedURLException e) {
            return null;
        }

        if (file == null) return null;
        try {
            return DataObject.find (file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
    }

    private static StyledDocument findDocument(FileObject fo) {
        DataObject dataObject;
        try {
            dataObject = DataObject.find (fo);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
        EditorCookie ec = (EditorCookie) dataObject.getCookie(EditorCookie.class);
        if (ec == null) return null;
        StyledDocument doc;
        try {
            doc = ec.openDocument();
        } catch (IOException ex) {
            return null;
        }
        return doc;
    }

    private static StyledDocument findDocument(DataObject dataObject) {
        EditorCookie ec = (EditorCookie) dataObject.getCookie(EditorCookie.class);
        if (ec == null) return null;
        StyledDocument doc;
        try {
            doc = ec.openDocument();
        } catch (IOException ex) {
            return null;
        }
        return doc;
    }

    private static CompilationController retrieveController(ResultIterator resIt, StyledDocument doc) throws ParseException {
        Result res = resIt.getParserResult();
        CompilationController ci = res != null ? CompilationController.get(res) : null;
        if (ci == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Unable to get compilation controller " + doc);
        }
        return ci;
    }

    private Operation[] computeOperations(CompilationController ci, int offset, int lineNumber, BytecodeProvider bytecodeProvider) throws IOException {
        if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {//TODO: ELEMENTS_RESOLVED may be sufficient
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                    "\nDiagnostics = "+ci.getDiagnostics()+
                    "\nFree memory = "+Runtime.getRuntime().freeMemory());
            return new Operation[] {};
        }
        Scope scope = ci.getTreeUtilities().scopeFor(offset);
        Element method = scope.getEnclosingMethod();
        if (method == null) {
            return new Operation[] {};
        }
        Tree methodTree = ci.getTrees().getTree(method);
        if (methodTree == null) { // method not found
            return new Operation[] {};
        }
        CompilationUnitTree cu = ci.getCompilationUnit();
        ExpressionScanner scanner = new ExpressionScanner(lineNumber, cu, ci.getTrees().getSourcePositions());
        ExpressionScanner.ExpressionsInfo info = new ExpressionScanner.ExpressionsInfo();
        List<Tree> expTrees = methodTree.accept(scanner, info);

        //com.sun.source.tree.ExpressionTree expTree = scanner.getExpressionTree();
        if (expTrees == null || expTrees.size() == 0) {
            return new Operation[] {};
        }
        SourcePositions sp = ci.getTrees().getSourcePositions();
        int treeStartLine =
                (int) cu.getLineMap().getLineNumber(
                    sp.getStartPosition(cu, expTrees.get(0)));
        int treeEndLine =
                (int) cu.getLineMap().getLineNumber(
                    sp.getEndPosition(cu, expTrees.get(expTrees.size() - 1)));

        if (treeStartLine == Diagnostic.NOPOS || treeEndLine == Diagnostic.NOPOS) {
            return null;
        }
        //t3 = System.nanoTime();
        int[] indexes = bytecodeProvider.indexAtLines(treeStartLine, treeEndLine);
        if (indexes == null) {
            return null;
        }
        Map<Tree, Operation> nodeOperations = new HashMap<Tree, Operation>();
        Operation[] ops = AST2Bytecode.matchSourceTree2Bytecode(
                cu,
                ci,
                expTrees, info, bytecodeProvider.byteCodes(),
                indexes,
                bytecodeProvider.constantPool(),
                new OperationCreationDelegateImpl(),
                nodeOperations);
        if (ops != null) {
            assignNextOperations(methodTree, cu, ci, bytecodeProvider, expTrees, info, nodeOperations);
        }
        return ops;
    }


    // Support classes:

    private static final class DoneFuture<T> implements Future<T> {

        private final T result;

        public DoneFuture(T result) {
            this.result = result;
        }

        public boolean cancel(boolean mayInterruptIfRunning) { return false; }
        public boolean isCancelled() { return false; }
        public boolean isDone() { return true; }

        public T get() throws InterruptedException, ExecutionException {
            return result;
        }
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return result;
        }
    }

    private class EditorContextDispatchListener extends Object implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            pcs.firePropertyChange (org.openide.windows.TopComponent.Registry.PROP_CURRENT_NODES, null, null);
        }

    }

    private class OperationCreationDelegateImpl implements AST2Bytecode.OperationCreationDelegate {
        /*
         public Operation createOperation(
                 Position startPosition,
                 Position endPosition,
                 int bytecodeIndex) {
             return EditorContextImpl.this.createOperation(
                     startPosition,
                     endPosition,
                     bytecodeIndex);
         }
         */
         public Operation createMethodOperation(
                 Position startPosition,
                 Position endPosition,
                 Position methodStartPosition,
                 Position methodEndPosition,
                 String methodName,
                 String methodClassType,
                 int bytecodeIndex) {
             return EditorContextImpl.this.createMethodOperation(
                     startPosition,
                     endPosition,
                     methodStartPosition,
                     methodEndPosition,
                     methodName,
                     methodClassType,
                     bytecodeIndex);
         }
         public Position createPosition(
                 int offset,
                 int line,
                 int column) {
             return EditorContextImpl.this.createPosition(
                     offset,
                     line,
                     column);
         }
         public void addNextOperationTo(Operation operation, Operation next) {
             EditorContextImpl.this.addNextOperationTo(operation, next);
         }
    }

    private class SessionsListener extends DebuggerManagerAdapter {

        @Override
        public void sessionRemoved(Session session) {
            int numSession = DebuggerManager.getDebuggerManager().getSessions().length;
            if (numSession > 0) {
                // Trigger the check for live values
                sourceHandles.size();
                sourceModifStamps.size();
            } else {
                // No debugger sessions - clean the map
                sourceHandles.clear();
                sourceModifStamps.clear();
            }
        }

    }

    private static class SessionsListenerRemoval {

        public void removeDebuggerListener (DebuggerManagerListener l) {
            DebuggerManager.getDebuggerManager().removeDebuggerListener(
                    DebuggerManager.PROP_SESSIONS, l);
        }
        
    }

}
