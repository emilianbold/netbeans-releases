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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.Runnable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationHolder implements ChangeListener, PropertyChangeListener {
    
    private Map<String, List<Annotation>> layer2Annotations;
    private Map<Annotation, Integer> annotations2Lines;
    private Set<JEditorPane> openedComponents;
    private Reference<EditorCookie.Observable> editorCookie;
    private DataObject file;//!!!
            
    /** Creates a new instance of VisibleRangeListener */
    public AnnotationHolder(FileObject file) {
        if (file == null)
            return ;
        
        layer2Annotations = new HashMap<String, List<Annotation>>();
        annotations2Lines = new HashMap<Annotation, Integer>();
        openedComponents = new HashSet<JEditorPane>();
        
        try {
            this.file = DataObject.find(file);
            
            EditorCookie.Observable editorCookie = this.file.getCookie(EditorCookie.Observable.class);
            
            editorCookie.addPropertyChangeListener(WeakListeners.propertyChange(this, editorCookie));
            this.editorCookie = new WeakReference(editorCookie);
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, "Error", e);
        }
        
        propertyChange(null);
    }

    public void stateChanged(ChangeEvent evt) {
        updateVisibleRanges();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                EditorCookie.Observable editorCookie = AnnotationHolder.this.editorCookie.get();
                
                if (editorCookie == null)
                    return ;
                
                JEditorPane[] panes = editorCookie.getOpenedPanes();
                
                if (panes == null) {
                    //clear all:
                    layer2Annotations = new HashMap<String, List<Annotation>>();
                    annotations2Lines = new HashMap<Annotation, Integer>();
                    openedComponents = new HashSet<JEditorPane>();
                    return ;
                }
                
                Set<JEditorPane> addedPanes = new HashSet(Arrays.asList(panes));
                Set<JEditorPane> removedPanes = new HashSet(openedComponents);
                
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
        for (String layer : layer2Annotations.keySet()) {
            List<Annotation> annotations = layer2Annotations.get(layer);
            
            for (Annotation a : annotations) {
                int line = annotations2Lines.get(a);
                
                if (startLine <= line && endLine >= line) {
                    LazyFixList l = ((ParseErrorAnnotation) a ).getDescription().getFixes();
                    
                    if (l.probablyContainsFixes() && !l.isComputed())
                        l.getFixes();
                }
            }
        }
    }

    public synchronized void setErrorDescriptions(String layer, /*???*/Document doc, Map<Integer, List<ErrorDescription>> errors) throws IOException {
        if (file == null)
            return ;
        
        List<Annotation> old = layer2Annotations.get(layer);
        
        if (old != null) {
            for (Annotation a : old) {
                a.detach();
                annotations2Lines.remove(a);
            }
        }
        
        LineCookie lc = (LineCookie) file.getCookie(LineCookie.class);
        
        List<Annotation> annotations = new ArrayList<Annotation>();
        
        for (Iterator<Entry<Integer, List<ErrorDescription>>> i = errors.entrySet().iterator(); i.hasNext(); ) {
            Entry<Integer, List<ErrorDescription>> e = i.next();
            List<ErrorDescription> descs = e.getValue();
            int lineNumber = e.getKey().intValue();
            Line line = lc.getLineSet().getCurrent(lineNumber);
            ErrorDescription desc = HintsControllerImpl.createCompoundErrorDescription(HintsControllerImpl.fullLine(line), doc, descs);
            Annotation a = new ParseErrorAnnotation(desc);
            
            a.attach(line);
            annotations.add(a);
            annotations2Lines.put(a, e.getKey());
        }
        
        layer2Annotations.put(layer, annotations);
        
        updateVisibleRanges();
    }
    
    public synchronized boolean hasErrors() {
        for (Annotation a : annotations2Lines.keySet()) {
            if (((ParseErrorAnnotation) a).getDescription().getSeverity() == Severity.ERROR)
                return true;
        }
        
        return false;
    }

    public synchronized List<ErrorDescription> getErrors() {
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
	
        for (Annotation a : annotations2Lines.keySet()) {
            result.add(((ParseErrorAnnotation) a).getDescription());
        }
        
        return result;
    }
    
    public synchronized List<Annotation> getAnnotations() {
        return new ArrayList(annotations2Lines.keySet());
    }
    
    private static final RequestProcessor INSTANCE = new RequestProcessor("AnnotationHolder");
    
}
