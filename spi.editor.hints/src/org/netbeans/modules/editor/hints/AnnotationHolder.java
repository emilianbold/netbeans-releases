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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.editor.hints;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.Runnable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.Coloring;
import org.netbeans.modules.editor.highlights.spi.DefaultHighlight;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.netbeans.modules.editor.highlights.spi.Highlighter;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

import org.netbeans.api.timers.TimesCollector;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationHolder implements ChangeListener, PropertyChangeListener {
    
    final static Map<Severity, Coloring> COLORINGS;
    
    static {
        COLORINGS = new EnumMap<Severity, Coloring>(Severity.class);
        COLORINGS.put(Severity.DISABLED, new Coloring());
        COLORINGS.put(Severity.ERROR, new Coloring(null, 0, null, null, null, null, new Color(0xFF, 0x00, 0x00)));
        COLORINGS.put(Severity.WARNING, new Coloring(null, 0, null, null, null, null, new Color(0xC0, 0xC0, 0x00)));
        COLORINGS.put(Severity.VERIFIER, new Coloring(null, 0, null, null, null, null, new Color(0xFF, 0xD5, 0x55)));
        COLORINGS.put(Severity.HINT, new Coloring());
        COLORINGS.put(Severity.TODO, new Coloring(Font.decode(null).deriveFont(Font.BOLD), Coloring.FONT_MODE_APPLY_STYLE, Color.BLUE, null));
    };
    
    private Map<ErrorDescription, List<Integer>> errors2Lines;
    private Map<Integer, List<ErrorDescription>> line2Errors;
    private Map<Integer, List<Highlight>> line2Highlights;
    private Map<Integer, ParseErrorAnnotation> line2Annotations;
    private Map<String, List<ErrorDescription>> layer2Errors;
    
    private Set<JEditorPane> openedComponents;
    private EditorCookie.Observable editorCookie;
    private FileObject file;
    private DataObject od;
    private Document doc;
    
    private static Map<FileObject, AnnotationHolder> file2Holder = new HashMap<FileObject, AnnotationHolder>();
    
    public static synchronized AnnotationHolder getInstance(FileObject file) {
        if (file == null)
            return null;
        
        AnnotationHolder result = file2Holder.get(file);
        
        if (result == null) {
            try {
                DataObject od = DataObject.find(file);
                EditorCookie.Observable editorCookie = od.getCookie(EditorCookie.Observable.class);
                
                if (editorCookie == null) {
                    Logger.getLogger("global").log(Level.WARNING, "No EditorCookie.Observable for file: " + FileUtil.getFileDisplayName(file));
                } else {
                    Document doc = editorCookie.getDocument();
                    
                    if (doc != null) {
                        file2Holder.put(file, result = new AnnotationHolder(file, od, doc, editorCookie));
                    }
                }
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        
        return result;
    }
    
    /**temporary*/
    static synchronized Collection<FileObject> coveredFiles() {
        return new ArrayList<FileObject>(file2Holder.keySet());
    }
    
    private AnnotationHolder(FileObject file, DataObject od, Document doc, EditorCookie.Observable editorCookie) throws IOException {
        if (file == null)
            return ;
        
        init();
        
        this.file = file;
        this.od = od;
        this.doc = doc;
        editorCookie.addPropertyChangeListener(WeakListeners.propertyChange(this, editorCookie));
        this.editorCookie = editorCookie;
        
        propertyChange(null);
        
        TimesCollector.getDefault().reportReference(file, "annotation-holder", "[M] Annotation Holder", this);
    }
    
    private synchronized void init() {
        errors2Lines = new HashMap<ErrorDescription, List<Integer>>();
        line2Errors = new HashMap<Integer, List<ErrorDescription>>();
        line2Highlights = new HashMap<Integer, List<Highlight>>();
        line2Annotations = new HashMap<Integer, ParseErrorAnnotation>();
        layer2Errors = new HashMap<String, List<ErrorDescription>>();
        openedComponents = new HashSet<JEditorPane>();
    }

    public void stateChanged(ChangeEvent evt) {
        updateVisibleRanges();
    }
    
    private static final boolean useLinesToAttachAnnotations = false;
    
    void attachAnnotation(int line, ParseErrorAnnotation a) throws BadLocationException {
        if (useLinesToAttachAnnotations) {
            LineCookie lc = od.getCookie(LineCookie.class);
            Line lineRef = lc.getLineSet().getCurrent(line);
            
            a.attach(lineRef);
        } else {
            Position lineStart = doc.createPosition(NbDocument.findLineOffset((StyledDocument) doc, line));
            
            NbDocument.addAnnotation((StyledDocument) doc, lineStart, -1, a);
        }
    }

    void detachAnnotation(Annotation a) {
        if (useLinesToAttachAnnotations) {
            a.detach();
        } else {
            if (doc != null) {
                NbDocument.removeAnnotation((StyledDocument) doc, a);
            }
        }
    }
    
    private synchronized void clearAll() {
        //remove all annotations:
        for (ParseErrorAnnotation a : line2Annotations.values()) {
            detachAnnotation(a);
        }
        
        file2Holder.remove(file);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JEditorPane[] panes = editorCookie.getOpenedPanes();
                
                if (panes == null) {
                    clearAll();
                    return ;
                }
                
                Set<JEditorPane> addedPanes = new HashSet<JEditorPane>(Arrays.asList(panes));
                Set<JEditorPane> removedPanes = new HashSet<JEditorPane>(openedComponents);
                
                removedPanes.removeAll(addedPanes);
                addedPanes.removeAll(openedComponents);
                
                for (JEditorPane pane : addedPanes) {
                    Container parent = pane.getParent();
                    
                    if (parent instanceof JViewport) {
                        JViewport viewport = (JViewport) parent;
                        
                        viewport.addChangeListener(WeakListeners.change(AnnotationHolder.this, viewport));
                    }
                }
                
                openedComponents.removeAll(removedPanes);
                openedComponents.addAll(addedPanes);
                
                updateVisibleRanges();
                return ;
            }
        });
    }
    
    private void updateVisibleRanges() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final List<int[]> visibleRanges = new ArrayList<int[]>();
                
                synchronized(AnnotationHolder.this) {
                    for (JEditorPane pane : openedComponents) {
                        Container parent = pane.getParent();
                        
                        if (parent instanceof JViewport) {
                            JViewport viewport = (JViewport) parent;
                            Point start = viewport.getViewPosition();
                            Dimension size = viewport.getExtentSize();
                            Point end = new Point(start.x + size.width, start.y + size.height);
                            
                            int startPosition = pane.viewToModel(start);
                            int endPosition = pane.viewToModel(end);
                            int startLine = NbDocument.findLineNumber((StyledDocument) pane.getDocument(), startPosition);
                            int endLine = NbDocument.findLineNumber((StyledDocument) pane.getDocument(), endPosition);
                            //TODO: check differences against last:
                            
                            visibleRanges.add(new int[] {startLine, endLine});
                        }
                    }
                }
                
                INSTANCE.post(new Runnable() {
                    public void run() {
                        for (int[] span : visibleRanges) {
                            updateAnnotations(span[0], span[1]);
                        }
                    }
                });
            }
        });
    }
    
    private synchronized void updateAnnotations(int startLine, int endLine) {
        for (int cntr = startLine; cntr <= endLine; cntr++) {
            List<ErrorDescription> errors = line2Errors.get(cntr);
            
            if (errors != null) {
                for (ErrorDescription e : errors) {
                    LazyFixList l = e.getFixes();
                    
                    if (l.probablyContainsFixes() && !l.isComputed())
                        l.getFixes();
                }
            }
        }
    }
    
    private List<ErrorDescription> getErrorsForLayer(String layer) {
        List<ErrorDescription> errors = layer2Errors.get(layer);
        
        if (errors == null) {
            layer2Errors.put(layer, errors = new ArrayList<ErrorDescription>());
        }
        
        return errors;
    }
    
    private List<ErrorDescription> getErrorsForLine(Integer line) {
        List<ErrorDescription> errors = line2Errors.get(line);
        
        if (errors == null) {
            line2Errors.put(line, errors = new ArrayList<ErrorDescription>());
        }
        
        return errors;
    }
    
    private List<ErrorDescription> filter(List<ErrorDescription> errors, boolean onlyErrors) {
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        
        for (ErrorDescription e : errors) {
            if (e.getSeverity() == Severity.ERROR) {
                if (onlyErrors)
                    result.add(e);
            } else {
                if (!onlyErrors)
                    result.add(e);
            }
        }
        
        return result;
    }
    
    private void concatDescription(List<ErrorDescription> errors, StringBuffer description) {
        boolean first = true;
        
        for (ErrorDescription e : errors) {
            if (!first) {
                description.append("\n\n");
            }
            description.append(e.getDescription());
            first = false;
        }
    }
    
    private List<LazyFixList> computeFixes(List<ErrorDescription> errors) {
        List<LazyFixList> result = new ArrayList<LazyFixList>();
        
        for (ErrorDescription e : errors) {
            result.add(e.getFixes());
        }
        
        return result;
    }
    
    private void updateAnnotationOnLine(Integer line) throws BadLocationException {
        List<ErrorDescription> errorDescriptions = getErrorsForLine(line);
        
        if (errorDescriptions.isEmpty()) {
            //nothing to do, remove old:
            Annotation ann = line2Annotations.remove(line);
            
            detachAnnotation(ann);
            return;
        }
        
        List<ErrorDescription> trueErrors = filter(errorDescriptions, true);
        List<ErrorDescription> others = filter(errorDescriptions, false);
        boolean hasErrors = !trueErrors.isEmpty();
        
        //build up the description of the annotation:
        StringBuffer description = new StringBuffer();
        
        concatDescription(trueErrors, description);
        
        if (!trueErrors.isEmpty() && !others.isEmpty()) {
            description.append("\n\n");
        }
        
        concatDescription(others, description);
        
        Severity mostImportantSeverity = hasErrors ? Severity.ERROR : Severity.WARNING; //XXX compute correct severity
        LazyFixList fixes = ErrorDescriptionFactory.lazyListForDelegates(computeFixes(hasErrors ? trueErrors : others));
        
        ParseErrorAnnotation pea = new ParseErrorAnnotation(mostImportantSeverity, fixes, description.toString(), line, this);
        Annotation previous = line2Annotations.put(line, pea);
        
        if (previous != null) {
            detachAnnotation(previous);
        }
        
        attachAnnotation(line, pea);
    }
    
    private void doUpdateHighlight(FileObject file) {
        List<Highlight> highlights = new ArrayList<Highlight>();
        
        for (List<Highlight> hls : line2Highlights.values()) {
            highlights.addAll(hls);
        }
        
        Highlighter.getDefault().setHighlights(file, AnnotationHolder.class.getName(), highlights);
    }
    
    void updateHighlightsOnLine(Integer line) throws IOException {
        List<ErrorDescription> errorDescriptions = getErrorsForLine(line);
        
        if (errorDescriptions.isEmpty()) {
            //nothing to do, remove old:
            line2Highlights.remove(line);
            
            return;
        }
        
        List<Highlight> highlights = new ArrayList<Highlight>();
        
        try {
            computeHighlights(doc, line, errorDescriptions, highlights);
            line2Highlights.put(line, highlights);
        } catch (BadLocationException ex) {
            throw (IOException) new IOException().initCause(ex);
        }
    }
    
    static void computeHighlights(Document doc, Integer line, List<ErrorDescription> errorDescriptions, List<Highlight> highlights) throws IOException, BadLocationException {
        for (Severity s : Arrays.asList(Severity.ERROR, Severity.WARNING, Severity.VERIFIER)) {
            Coloring c = COLORINGS.get(s);
            List<ErrorDescription> filteredDescriptions = new ArrayList<ErrorDescription>();
            
            for (ErrorDescription e : errorDescriptions) {
                if (e.getSeverity() == s) {
                    filteredDescriptions.add(e);
                }
            }
            
            List<Highlight> currentHighlights = new ArrayList<Highlight>();
            
            for (ErrorDescription e : filteredDescriptions) {
                Highlight h = new DefaultHighlight(c, e.getRange().getBegin().getPosition(), e.getRange().getEnd().getPosition());
                
                OUT: for (Iterator<Highlight> it = currentHighlights.iterator(); it.hasNext() && h != null; ) {
                    Highlight hl = it.next();
                    
                    switch (detectCollisions(hl, h)) {
                        case 0:
                            break;
                        case 1:
                            it.remove();
                            break;
                        case 2:
                            h = null; //nothing to add, hl is bigger:
                            break OUT;
                        case 4:
                        case 3:
                            int start = Math.min(hl.getStart(), h.getStart());
                            int end = Math.max(hl.getEnd(), h.getEnd());
                            
                                h = new DefaultHighlight(c, doc.createPosition(start), doc.createPosition(end));
                                it.remove();
                            break;
                    }
                }
                
                if (h != null) {
                    currentHighlights.add(h);
                }
            }
            
            OUTER: while (!currentHighlights.isEmpty()) {
                for (Iterator<Highlight> lowerIt = currentHighlights.iterator(); lowerIt.hasNext(); ) {
                    Highlight current = lowerIt.next();
                    
                    lowerIt.remove();
                    
                    for (Iterator<Highlight> higherIt = highlights.iterator(); higherIt.hasNext() && current != null; ) {
                        Highlight higher = higherIt.next();
                        
                        switch (detectCollisions(higher, current)) {
                            case 0:
                                //no problem
                                break;
                            case 1:
                            {
                                int currentStart = higher.getEnd() + 1;
                                int currentEnd = higher.getStart() - 1;
                                
                                if (currentStart < doc.getLength() && currentStart < current.getEnd()) {
                                    currentHighlights.add(new DefaultHighlight(current.getColoring(), doc.createPosition(currentStart), doc.createPosition(current.getEnd())));
                                }
                                
                                if (currentEnd < doc.getLength() && current.getStart() < currentEnd) {
                                    currentHighlights.add(new DefaultHighlight(current.getColoring(), doc.createPosition(current.getStart()), doc.createPosition(currentEnd)));
                                }
                                continue OUTER;
                            }
                            case 2:
                                current = null;
                                break;
                            case 3:
                                int currentStart = higher.getEnd() + 1;
                                
                                if (currentStart < doc.getLength() && currentStart < current.getEnd()) {
                                    current = new DefaultHighlight(current.getColoring(), doc.createPosition(currentStart), doc.createPosition(current.getEnd()));
                                } else {
                                    current = null;
                                }
                                break;
                            case 4:
                                int currentEnd = higher.getStart() - 1;
                                
                                if (currentEnd < doc.getLength() && current.getStart() < currentEnd) {
                                    current = new DefaultHighlight(current.getColoring(), doc.createPosition(current.getStart()), doc.createPosition(currentEnd));
                                } else {
                                    current = null;
                                }
                                break;
                        }
                    }
                    
                    if (current != null) {
                        highlights.add(current);
                    }
                }
            }
        }
    }
    
    private static int detectCollisions(Highlight h1, Highlight h2) {
        if (h2.getEnd() < h1.getStart())
            return 0;//no collision
        if (h1.getEnd() < h2.getStart())
            return 0;//no collision
        if (h2.getStart() < h1.getStart() && h2.getEnd() > h1.getEnd())
            return 1;//h2 encapsulates h1
        if (h1.getStart() < h2.getStart() && h1.getEnd() > h2.getEnd())
            return 2;//h1 encapsulates h2
        
        if (h1.getStart() < h2.getStart())
            return 3;//collides
        else
            return 4;
    }
    
    public synchronized void setErrorDescriptions(final String layer, final Collection<? extends ErrorDescription> errors) {
        doc.render(new Runnable() {
            public void run() {
                try {
                    setErrorDescriptionsImpl(file, layer, errors);
                } catch (IOException e) {
                    Logger.getLogger("global").log(Level.WARNING, e.getMessage(), e);
                }
            }
        });
    }
    
    private synchronized void setErrorDescriptionsImpl(FileObject file, String layer, Collection<? extends ErrorDescription> errors) throws IOException {
        long start = System.currentTimeMillis();
        
        try {
            if (file == null)
                return ;
            
            List<ErrorDescription> layersErrors = getErrorsForLayer(layer);
            
            Set<Integer> primaryLines = new HashSet<Integer>();
            Set<Integer> allLines = new HashSet<Integer>();
            
            for (ErrorDescription ed : layersErrors) {
                List<Integer> lines = errors2Lines.remove(ed);
                assert lines != null;
                
                boolean first = true;
                
                for (Integer line : lines) {
                    getErrorsForLine(line).remove(ed);
                    
                    if (first) {
                        primaryLines.add(line);
                    }
                    
                    allLines.add(line);
                    first = false;
                }
            }
            
            for (ErrorDescription ed : errors) {
                List<Integer> lines = new ArrayList<Integer>();
                int startLine = ed.getRange().getBegin().getLine();
                int endLine = ed.getRange().getEnd().getLine();
                
                for (int cntr = startLine; cntr <= endLine; cntr++) {
                    lines.add(cntr);
                }
                
                errors2Lines.put(ed, lines);
                
                boolean first = true;
                
                for (Integer line : lines) {
                    getErrorsForLine(line).add(ed);
                    
                    if (first) {
                        primaryLines.add(line);
                    }
                    
                    allLines.add(line);
                    first = false;
                }
            }
            
            layersErrors.clear();
            layersErrors.addAll(errors);
            
            for (Integer line : primaryLines) {
                updateAnnotationOnLine(line);
            }
            
            for (Integer line : allLines) {
                updateHighlightsOnLine(line);
            }
            
            doUpdateHighlight(file);
            
            updateVisibleRanges();
        } catch (BadLocationException ex) {
            throw (IOException) new IOException().initCause(ex);
        } finally {
            long end = System.currentTimeMillis();
            TimesCollector.getDefault().reportTime(file, "annotation-holder-" + layer, "Errors update for " + layer, end - start);
        }
    }
    
    public synchronized boolean hasErrors() {
        for (ErrorDescription e : errors2Lines.keySet()) {
            if (e.getSeverity() == Severity.ERROR)
                return true;
        }
        
        return false;
    }

    public synchronized List<ErrorDescription> getErrors() {
        return new ArrayList<ErrorDescription>(errors2Lines.keySet());
    }
    
    public synchronized List<Annotation> getAnnotations() {
        return new ArrayList<Annotation>(line2Annotations.values());
    }
    
    private static final RequestProcessor INSTANCE = new RequestProcessor("AnnotationHolder");
    
}
