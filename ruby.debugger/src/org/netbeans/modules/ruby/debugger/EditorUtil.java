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

package org.netbeans.modules.ruby.debugger;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Annotatable;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.windows.TopComponent;

/**
 * @author Martin Krauskopf
 */
public final class EditorUtil {
    
    private static DebuggerAnnotation[] currentLineDA;
    
    public static boolean contains(final Object currentLine, final Line line) {
        if (currentLine == null) return false;
        final Annotatable[] annotables = (Annotatable[]) currentLine;
        for (Annotatable ann : annotables) {
            if (ann.equals(line)) {
                return true;
            }
            if (ann instanceof Line.Part && ((Line.Part) ann).getLine().equals(line)) {
                return true;
            }
        }
        return false;
    }

    private static DebuggerAnnotation createCurrentLineAnnotation(final Annotatable annotable) {
        String annType = (annotable instanceof Line.Part)
                ? DebuggerAnnotation.CURRENT_LINE_PART_ANNOTATION_TYPE2
                : DebuggerAnnotation.CURRENT_LINE_ANNOTATION_TYPE2;
        return new DebuggerAnnotation(annType, annotable);
    }
    
    public static void removeAnnotation(DebuggerAnnotation[] annotations) {
        for (DebuggerAnnotation annotation : annotations) {
            annotation.detach();
        }
    }

    /**
     * Make line in the editor current - shows the line and colorizes it
     * appropriately (green-striped by default). Note that editor counts lines
     * from zero.
     *
     * @see unmarkCurrent()
     */
    static void markCurrent(final String filePath, final int lineNumber) {
        markCurrent(getLine(filePath, lineNumber));
    }
    
    private static void markCurrent(final Object line) {
        unmarkCurrent();
        if (line == null) {
            return;
        }
        currentLineDA = createDebuggerAnnotation(line,
                DebuggerAnnotation.CURRENT_LINE_ANNOTATION_TYPE,
                DebuggerAnnotation.CURRENT_LINE_PART_ANNOTATION_TYPE);
        showLine(line);
    }
    
    public static DebuggerAnnotation[] createDebuggerAnnotation(final Object line,
            final String lineAnnotation, final String partAnnotation) {
        Annotatable[] annotables = (Annotatable[]) line;
        DebuggerAnnotation[] annotations = new DebuggerAnnotation[annotables.length];
        
        // first line with icon in gutter
        String annType = (partAnnotation != null && annotables[0] instanceof Line.Part) ? partAnnotation : lineAnnotation;
        annotations[0] = new DebuggerAnnotation(annType, annotables[0]);
        
        // other lines
        for (int i = 1; i < annotables.length; i++) {
            annotations[i] = createCurrentLineAnnotation(annotables[i]);
        }
        return annotations;
    }
    
    public static DebuggerAnnotation[] createDebuggerAnnotation(final Object line,
            final String lineAnnotation) {
        return createDebuggerAnnotation(line, lineAnnotation, null);
    }
    
    /**
     * Cancel effect of {@link #markCurrent(String, int)} method. I.e. removes
     * annotation, usually the green stripe.
     */
    static void unmarkCurrent() {
        if (currentLineDA != null) {
            removeAnnotation(currentLineDA);
            currentLineDA = null;
        }
    }
    
    public static Object getLine(final String filePath, final int lineNumber) {
        Annotatable[] annotables = null;
        
        if (filePath == null || lineNumber < 0) {
            return null;
        }
        
        File file = new File(filePath);
        FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileObject == null) {
            Util.info("Cannot resolve \"" + filePath + '"');
            return null;
        }
        
        LineCookie lineCookie = getLineCookie(fileObject);
        assert lineCookie != null;
        Line line = lineCookie.getLineSet().getCurrent(lineNumber);
        annotables = new Annotatable[] { line };
        return annotables;
    }
    
    public static LineCookie getLineCookie(final FileObject fo) {
        LineCookie result = null;
        try {
            DataObject dataObject = DataObject.find(fo);
            if (dataObject != null) {
                result = dataObject.getCookie(LineCookie.class);
            }
        } catch (DataObjectNotFoundException e) {
            Util.LOGGER.log(Level.FINE, "Cannot find DataObject for: " + fo, e.getMessage());
        }
        return result;
    }
    
    public static void showLine(final Object line) {
        if (line == null) {
            return;
        }
        Annotatable[] a = (Annotatable[]) line;
        final Line lineToShow;
        if (a[0] instanceof Line) {
            lineToShow = ((Line) a[0]);
        } else if (a[0] instanceof Line.Part) {
            lineToShow = ((Line.Part) a[0]).getLine();
        } else {
            throw new RuntimeException("Cannot cast to neither Line nor Line.Part: " + a[0]); // NOI18N
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                lineToShow.show(Line.SHOW_GOTO);
            }
        });
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Editor boilerplate ">
    /**
     * Returns current editor line (the line where the carret currently is).
     * Might be <code>null</code>. Works only for {@link Util#isRubySource
     * supported mime-types}. For unsupported ones returns <code>null</code>.
     */
    public static Line getCurrentLine() {
        Node[] nodes = TopComponent.getRegistry().getCurrentNodes();
        if (nodes == null) return null;
        if (nodes.length != 1) return null;
        Node n = nodes [0];
        FileObject fo = n.getLookup().lookup(FileObject.class);
        if (fo == null) {
            DataObject dobj = n.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                fo = dobj.getPrimaryFile();
            }
        }
        if (fo == null) { return null; }
        if (!Util.isRubySource(fo)) {
            return null;
        }
        LineCookie lineCookie = n.getCookie(LineCookie.class);
        if (lineCookie == null) return null;
        EditorCookie editorCookie = n.getCookie(EditorCookie.class);
        if (editorCookie == null) return null;
        JEditorPane jEditorPane = getEditorPane(editorCookie);
        if (jEditorPane == null) return null;
        StyledDocument document = editorCookie.getDocument();
        if (document == null) return null;
        Caret caret = jEditorPane.getCaret();
        if (caret == null) return null;
        int lineNumber = NbDocument.findLineNumber(document, caret.getDot());
        try {
            Line.Set lineSet = lineCookie.getLineSet();
            assert lineSet != null : lineCookie;
            return lineSet.getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }
    
    private static JEditorPane getEditorPane_(EditorCookie editorCookie) {
        JEditorPane[] op = editorCookie.getOpenedPanes();
        if ((op == null) || (op.length < 1)) return null;
        return op [0];
    }
    
    private static JEditorPane getEditorPane(final EditorCookie editorCookie) {
        if (SwingUtilities.isEventDispatchThread()) {
            return getEditorPane_(editorCookie);
        } else {
            final JEditorPane[] ce = new JEditorPane[1];
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    public void run() {
                        ce[0] = getEditorPane_(editorCookie);
                    }
                });
            } catch (InvocationTargetException ex) {
                Util.severe(ex);
            } catch (InterruptedException ex) {
                Util.severe(ex);
                Thread.currentThread().interrupt();
            }
            return ce[0];
        }
    }
    // </editor-fold>
    
}
