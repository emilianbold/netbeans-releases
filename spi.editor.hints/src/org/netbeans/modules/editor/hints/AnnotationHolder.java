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
package org.netbeans.modules.editor.hints;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationHolder implements ChangeListener, PropertyChangeListener, DocumentListener {
    
    final static Map<Severity, AttributeSet> COLORINGS;
    
    static {
        COLORINGS = new EnumMap<Severity, AttributeSet>(Severity.class);
        COLORINGS.put(Severity.DISABLED,  AttributesUtilities.createImmutable());
        COLORINGS.put(Severity.ERROR, AttributesUtilities.createImmutable(EditorStyleConstants.WaveUnderlineColor, new Color(0xFF, 0x00, 0x00)));
        COLORINGS.put(Severity.WARNING, AttributesUtilities.createImmutable(EditorStyleConstants.WaveUnderlineColor, new Color(0xC0, 0xC0, 0x00)));
        COLORINGS.put(Severity.VERIFIER, AttributesUtilities.createImmutable(EditorStyleConstants.WaveUnderlineColor, new Color(0xFF, 0xD5, 0x55)));
        COLORINGS.put(Severity.HINT, AttributesUtilities.createImmutable());
        COLORINGS.put(Severity.TODO, AttributesUtilities.createImmutable());
    };
    
    private Map<ErrorDescription, List<Position>> errors2Lines;
    private Map<Position, List<ErrorDescription>> line2Errors;
    private Map<Position, ParseErrorAnnotation> line2Annotations;
    private Map<String, List<ErrorDescription>> layer2Errors;
    
    private Set<JEditorPane> openedComponents;
    private EditorCookie.Observable editorCookie;
    private FileObject file;
    private DataObject od;
    private BaseDocument doc;
    
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
                    
                    if (doc instanceof BaseDocument) {
                        file2Holder.put(file, result = new AnnotationHolder(file, od, (BaseDocument) doc, editorCookie));
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
    
    private AnnotationHolder(FileObject file, DataObject od, BaseDocument doc, EditorCookie.Observable editorCookie) throws IOException {
        if (file == null)
            return ;
        
        init();
        
        this.file = file;
        this.od = od;
        this.doc = doc;
        this.doc.addDocumentListener(this);
        editorCookie.addPropertyChangeListener(WeakListeners.propertyChange(this, editorCookie));
        this.editorCookie = editorCookie;
        
        propertyChange(null);
        
        Logger.getLogger("TIMER").log(Level.FINE, "Annotation Holder",
                    new Object[] {file, this});
    }
    
    private synchronized void init() {
        errors2Lines = new HashMap<ErrorDescription, List<Position>>();
        line2Errors = new HashMap<Position, List<ErrorDescription>>();
        line2Annotations = new HashMap<Position, ParseErrorAnnotation>();
        layer2Errors = new HashMap<String, List<ErrorDescription>>();
        openedComponents = new HashSet<JEditorPane>();
    }

    public void stateChanged(ChangeEvent evt) {
        updateVisibleRanges();
    }
    
    Attacher attacher = new NbDocumentAttacher();
    
    void attachAnnotation(Position line, ParseErrorAnnotation a) throws BadLocationException {
        attacher.attachAnnotation(line, a);
    }

    void detachAnnotation(Annotation a) {
        attacher.detachAnnotation(a);
    }
    
    static interface Attacher {
        public void attachAnnotation(Position line, ParseErrorAnnotation a) throws BadLocationException;
        public void detachAnnotation(Annotation a);
    }
    
    final class LineAttacher implements Attacher {
        public void attachAnnotation(Position line, ParseErrorAnnotation a) throws BadLocationException {
            throw new UnsupportedOperationException();
//            LineCookie lc = od.getCookie(LineCookie.class);
//            Line lineRef = lc.getLineSet().getCurrent(line);
//            
//            a.attach(lineRef);
        }
        public void detachAnnotation(Annotation a) {
            a.detach();
        }
    }
    
    final class NbDocumentAttacher implements Attacher {
        public void attachAnnotation(Position lineStart, ParseErrorAnnotation a) throws BadLocationException {
            NbDocument.addAnnotation((StyledDocument) doc, lineStart, -1, a);
        }
        public void detachAnnotation(Annotation a) {
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
        doc.removeDocumentListener(this);
        
        getBag(doc).clear();
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
    
    public void insertUpdate(DocumentEvent e) {
        int line = NbDocument.findLineNumber((StyledDocument) doc, e.getOffset());
        
        handleChange(line, 0);
    }
    
    public void removeUpdate(DocumentEvent e) {
        DocumentEvent.ElementChange change = e.getChange(doc.getDefaultRootElement());
        
        if (change != null) {
            handleChange(change.getIndex(), change.getChildrenRemoved().length - 1);
        } else {
            int line = NbDocument.findLineNumber((StyledDocument) doc, e.getOffset());
            
            handleChange(line, 0);
        }
    }
    
    private synchronized void handleChange(int start, int size) {
        size = size < 0 ? 0 : size;
        try {
            Set<Position> modifiedLines = new HashSet<Position>();
            
            for (int lineOffset = start; lineOffset <= (start + size); lineOffset++) {
                Position line = getPosition(lineOffset, false);
                
                if (line == null)
                    continue;
                
                List<ErrorDescription> eds = line2Errors.get(line);
                
                if (eds == null || eds.isEmpty())
                    continue ;
                
                eds = new LinkedList<ErrorDescription>(eds);
                
                for (ErrorDescription ed : eds) {
                    for (Position i : errors2Lines.remove(ed)) {
                        line2Errors.get(i).remove(ed);
                        modifiedLines.add(i);
                    }
                    for (List<ErrorDescription> edsForLayer : layer2Errors.values()) {
                        edsForLayer.remove(ed);
                    }
                }
                
                line2Errors.remove(line);
            }
            
            for (Position line : modifiedLines) {
                updateAnnotationOnLine(line);
                updateHighlightsOnLine(line);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void changedUpdate(DocumentEvent e) {
        //ignored
    }
    
    private void updateVisibleRanges() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                long startTime = System.currentTimeMillis();
                final List<int[]> visibleRanges = new ArrayList<int[]>();
                
                doc.render(new Runnable() {
                    public void run() {
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
                                    //TODO: check differences against last:
                                    visibleRanges.add(new int[]{startPosition, endPosition});
                                }
                            }
                        }
                    }
                });

                INSTANCE.post(new Runnable() {
                    public void run() {
                        for (int[] span : visibleRanges) {
                            updateAnnotations(span[0], span[1]);
                        }
                    }
                });
                
                long endTime = System.currentTimeMillis();
                
                Logger.getLogger(AnnotationHolder.class.getName()).log(Level.FINE, "updateVisibleRanges: time={0}", endTime - startTime);
            }
        });
    }
    
    private void updateAnnotations(final int startPosition, final int endPosition) {
        long startTime = System.currentTimeMillis();
        final List<ErrorDescription> errorsToUpdate = new ArrayList<ErrorDescription>();
        
        doc.render(new Runnable() {
            public void run() {
                synchronized (this) {
                    try {
                        int startLine = Utilities.getRowStart(doc, startPosition < doc.getLength() ? startPosition : (doc.getLength() - 1));
                        int endLine = Utilities.getRowEnd(doc, endPosition < doc.getLength() ? endPosition : (doc.getLength() - 1)) + 1;
                        
                        int index = findPositionGE(startLine);
                        
                        while (index < knownPositions.size()) {
                            Reference<Position> r = knownPositions.get(index++);
                            Position lineToken = r.get();
                            
                            if (lineToken == null)
                                continue;
                            
                            if (lineToken.getOffset() > endLine)
                                break;
                            
                            List<ErrorDescription> errors = line2Errors.get(lineToken);
                            
                            if (errors != null) {
                                errorsToUpdate.addAll(errors);
                            }
                        }
                    } catch (BadLocationException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
                
            }
        });
        
        Logger.getLogger(AnnotationHolder.class.getName()).log(Level.FINE, "updateAnnotations: errorsToUpdate={0}", errorsToUpdate);
        
        for (ErrorDescription e : errorsToUpdate) {
            LazyFixList l = e.getFixes();

            if (l.probablyContainsFixes() && !l.isComputed()) {
                l.getFixes();
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        Logger.getLogger(AnnotationHolder.class.getName()).log(Level.FINE, "updateAnnotations: time={0}", endTime - startTime);
    }
    
    private List<ErrorDescription> getErrorsForLayer(String layer) {
        List<ErrorDescription> errors = layer2Errors.get(layer);
        
        if (errors == null) {
            layer2Errors.put(layer, errors = new ArrayList<ErrorDescription>());
        }
        
        return errors;
    }
    
    private List<ErrorDescription> getErrorsForLine(Position line, boolean create) {
        List<ErrorDescription> errors = line2Errors.get(line);
        
        if (errors == null && create) {
            line2Errors.put(line, errors = new ArrayList<ErrorDescription>());
        }
        
        if (errors != null && errors.isEmpty() && !create) {
            //clean:
            line2Errors.remove(line);
            errors = null;
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
    
    private void updateAnnotationOnLine(Position line) throws BadLocationException {
        List<ErrorDescription> errorDescriptions = getErrorsForLine(line, false);
        
        if (errorDescriptions == null) {
            //nothing to do, remove old:
            Annotation ann = line2Annotations.remove(line);
            
            detachAnnotation(ann);
            return;
        }
        
        errorDescriptions = getErrorsForLine(line, true);
        
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
        
        Severity mostImportantSeverity;
        
        if (hasErrors) {
            mostImportantSeverity = Severity.ERROR;
        } else {
            mostImportantSeverity = Severity.HINT;
            
            for (ErrorDescription e : others) {
                if (mostImportantSeverity.compareTo(e.getSeverity()) > 0) {
                    mostImportantSeverity = e.getSeverity();
                }
            }
        }
        
        LazyFixList fixes = ErrorDescriptionFactory.lazyListForDelegates(computeFixes(hasErrors ? trueErrors : others));
        
        ParseErrorAnnotation pea = new ParseErrorAnnotation(mostImportantSeverity, fixes, description.toString(), line, this);
        Annotation previous = line2Annotations.put(line, pea);
        
        if (previous != null) {
            detachAnnotation(previous);
        }
        
        attachAnnotation(line, pea);
    }
    
    void updateHighlightsOnLine(Position line) throws IOException {
        List<ErrorDescription> errorDescriptions = getErrorsForLine(line, false);
        
        OffsetsBag bag = getBag(doc);
        
        updateHighlightsOnLine(bag, doc, line, errorDescriptions);
    }
    
    static void updateHighlightsOnLine(OffsetsBag bag, BaseDocument doc, Position line, List<ErrorDescription> errorDescriptions) throws IOException {
        try {
            int rowStart = line.getOffset();
            int rowEnd = Utilities.getRowEnd(doc, rowStart);
            int rowHighlightStart = Utilities.getRowFirstNonWhite(doc, rowStart);
            int rowHighlightEnd = Utilities.getRowLastNonWhite(doc, rowStart) + 1;

            bag.removeHighlights(rowStart, rowEnd, false);

            if (errorDescriptions != null) {
                bag.addAllHighlights(computeHighlights(doc, errorDescriptions).getHighlights(rowHighlightStart, rowHighlightEnd));
            }
        } catch (BadLocationException ex) {
            throw (IOException) new IOException().initCause(ex);
        }
    }
    
    static OffsetsBag computeHighlights(Document doc, List<ErrorDescription> errorDescriptions) throws IOException, BadLocationException {
        OffsetsBag bag = new OffsetsBag(doc);
        for (Severity s : Arrays.asList(Severity.VERIFIER, Severity.WARNING, Severity.ERROR)) {
            List<ErrorDescription> filteredDescriptions = new ArrayList<ErrorDescription>();
            
            for (ErrorDescription e : errorDescriptions) {
                if (e.getSeverity() == s) {
                    filteredDescriptions.add(e);
                }
            }
            
            List<int[]> currentHighlights = new ArrayList<int[]>();
            
            for (ErrorDescription e : filteredDescriptions) {
                int[] h = new int[] {e.getRange().getBegin().getPosition().getOffset(), e.getRange().getEnd().getPosition().getOffset()};
                
                OUT: for (Iterator<int[]> it = currentHighlights.iterator(); it.hasNext() && h != null; ) {
                    int[] hl = it.next();
                    
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
                            int start = Math.min(hl[0], h[0]);
                            int end = Math.max(hl[1], h[1]);
                            
                                h = new int[] {start, end};
                                it.remove();
                            break;
                    }
                }
                
                if (h != null) {
                    currentHighlights.add(h);
                }
            }
            
            for (int[] h : currentHighlights) {
                bag.addHighlight(h[0], h[1], COLORINGS.get(s));
            }
        }
        
        return bag;
    }
    
    private static int detectCollisions(int[] h1, int[] h2) {
        if (h2[1] < h1[0])
            return 0;//no collision
        if (h1[1] < h2[0])
            return 0;//no collision
        if (h2[0] < h1[0] && h2[1] > h1[1])
            return 1;//h2 encapsulates h1
        if (h1[0] < h2[0] && h1[1] > h2[1])
            return 2;//h1 encapsulates h2
        
        if (h1[0] < h2[0])
            return 3;//collides
        else
            return 4;
    }
    
    public void setErrorDescriptions(final String layer, final Collection<? extends ErrorDescription> errors) {
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
            
            Set<Position> primaryLines = new HashSet<Position>();
            Set<Position> allLines = new HashSet<Position>();
            
            for (ErrorDescription ed : layersErrors) {
                List<Position> lines = errors2Lines.remove(ed);
                assert lines != null;
                
                boolean first = true;
                
                for (Position line : lines) {
                    List<ErrorDescription> errorsForLine = getErrorsForLine(line, false);
                    
                    if (errorsForLine != null) {
                        errorsForLine.remove(ed);
                    }
                    
                    if (first) {
                        primaryLines.add(line);
                    }
                    
                    allLines.add(line);
                    first = false;
                }
            }
            
            List<ErrorDescription> validatedErrors = new ArrayList<ErrorDescription>();
            
            for (ErrorDescription ed : errors) {
                if (ed == null) {
                    Logger.getLogger(AnnotationHolder.class.getName()).log(Level.WARNING, "'null' ErrorDescription in layer {0}.", layer); //NOI18N
                }
                
                if (ed.getRange() == null)
                    continue;
                
                validatedErrors.add(ed);
                
                List<Position> lines = new ArrayList<Position>();
                int startLine = ed.getRange().getBegin().getLine();
                int endLine = ed.getRange().getEnd().getLine();
                
                for (int cntr = startLine; cntr <= endLine; cntr++) {
                    Position p = getPosition(cntr, true);
                    lines.add(p);
                }
                
                errors2Lines.put(ed, lines);
                
                boolean first = true;
                
                for (Position line : lines) {
                    getErrorsForLine(line, true).add(ed);
                    
                    if (first) {
                        primaryLines.add(line);
                    }
                    
                    allLines.add(line);
                    first = false;
                }
            }
            
            layersErrors.clear();
            layersErrors.addAll(validatedErrors);
            
            for (Position line : primaryLines) {
                updateAnnotationOnLine(line);
            }
            
            for (Position line : allLines) {
                updateHighlightsOnLine(line);
            }
            
            updateVisibleRanges();
        } catch (BadLocationException ex) {
            throw (IOException) new IOException().initCause(ex);
        } finally {
            long end = System.currentTimeMillis();
            Logger.getLogger("TIMER").log(Level.FINE, "Errors update for " + layer,
                    new Object[] {file, end - start});
        }
    }
    
    private List<Reference<Position>> knownPositions = new ArrayList<Reference<Position>>();
    
    private static class Abort extends RuntimeException {
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
    
    private static RuntimeException ABORT = new Abort();
    
    private synchronized int findPositionGE(int offset) {
        while (true) {
            try {
                int index = Collections.binarySearch(knownPositions, offset, new PositionComparator());
                
                if (index >= 0) {
                    return index;
                } else {
                    return - (index + 1);
                }
            } catch (Abort a) {
                Logger.getLogger(AnnotationHolder.class.getName()).log(Level.FINE, "a null Position detected - clearing");
                int removedCount = 0;
                for (Iterator<Reference<Position>> it = knownPositions.iterator(); it.hasNext(); ) {
                    if (it.next().get() == null) {
                        removedCount++;
                        it.remove();
                    }
                }
                Logger.getLogger(AnnotationHolder.class.getName()).log(Level.FINE, "clearing finished, {0} positions cleared", removedCount);
            }
        }
    }
    
    private synchronized Position getPosition(int lineNumber, boolean create) throws BadLocationException {
        try {
            while (true) {
                int lineStart = Utilities.getRowStartFromLineOffset(doc, lineNumber);
                try {
                    int index = Collections.binarySearch(knownPositions, lineStart, new PositionComparator());

                    if (index >= 0) {
                        Reference<Position> r = knownPositions.get(index);
                        Position p = r.get();

                        if (p != null) {
                            return p;
                        }
                    }

                    if (!create)
                        return null;

                    Position p = NbDocument.createPosition(doc, lineStart, Position.Bias.Backward);

                    knownPositions.add(- (index + 1), new WeakReference<Position>(p));

                    Logger.getLogger("TIMER").log(Level.FINE, "Annotation Holder - Line Token",
                            new Object[] {file, p});
                    
                    return p;
                } catch (Abort a) {
                    Logger.getLogger(AnnotationHolder.class.getName()).log(Level.FINE, "a null Position detected - clearing");
                    int removedCount = 0;
                    for (Iterator<Reference<Position>> it = knownPositions.iterator(); it.hasNext(); ) {
                        if (it.next().get() == null) {
                            removedCount++;
                            it.remove();
                        }
                    }
                    Logger.getLogger(AnnotationHolder.class.getName()).log(Level.FINE, "clearing finished, {0} positions cleared", removedCount);
                }
            }
        } finally {
            Logger.getLogger(AnnotationHolder.class.getName()).log(Level.FINE, "knownPositions.size={0}", knownPositions.size());
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
    
    public static OffsetsBag getBag(Document doc) {
        OffsetsBag ob = (OffsetsBag) doc.getProperty(AnnotationHolder.class);
        
        if (ob == null) {
            doc.putProperty(AnnotationHolder.class, ob = new OffsetsBag(doc));
        }
        
        return ob;
    }
    
    public int lineNumber(final Position offset) {
        final int[] result = new int[] {-1};
        
        doc.render(new Runnable() {
            public void run() {
                try {
                    result[0] = Utilities.getLineOffset(doc, offset.getOffset());
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        
        return result[0];
    }
    private static class PositionComparator implements Comparator<Object> {

        private PositionComparator() {
        }

        public int compare(Object o1, Object o2) {
            int left = -1;

            if (o1 instanceof Reference) {
                Position value = (Position) ((Reference) o1).get();

                if (value == null) {
                    //already collected...
                    throw ABORT;
                }

                left = value.getOffset();
            }

            if (o1 instanceof Integer) {
                left = ((Integer) o1);
            }

            assert left != -1;

            int right = -1;

            if (o2 instanceof Reference) {
                Position value = (Position) ((Reference) o2).get();

                if (value == null) {
                    //already collected...
                    throw ABORT;
                }

                right = value.getOffset();
            }

            if (o2 instanceof Integer) {
                right = ((Integer) o2);
            }

            assert right != -1;

            return left - right;
        }
    }
}
