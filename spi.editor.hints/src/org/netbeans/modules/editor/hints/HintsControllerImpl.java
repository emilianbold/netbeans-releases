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
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.Coloring;
import org.netbeans.modules.editor.highlights.spi.DefaultHighlight;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.netbeans.modules.editor.highlights.spi.Highlighter;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.ProvidersList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;

/**
 *
 * @author Jan Lahoda
 */
public final class HintsControllerImpl {
    
    private static Map<FileObject, AnnotationHolder> doc2Annotation = new HashMap<FileObject, AnnotationHolder>();
    
    private final static Map<Severity, Coloring> COLORINGS;
    
    static {
        COLORINGS = new EnumMap<Severity, Coloring>(Severity.class);
        COLORINGS.put(Severity.DISABLED, new Coloring());
        COLORINGS.put(Severity.ERROR, new Coloring(null, 0, null, null, null, null, new Color(0xFF, 0x00, 0x00)));
        COLORINGS.put(Severity.WARNING, new Coloring(null, 0, null, null, null, null, new Color(0xC0, 0xC0, 0x00)));
        COLORINGS.put(Severity.VERIFIER, new Coloring(null, 0, null, null, null, null, new Color(0xFF, 0xD5, 0x55)));
        COLORINGS.put(Severity.HINT, new Coloring());
        COLORINGS.put(Severity.TODO, new Coloring(Font.decode(null).deriveFont(Font.BOLD), Coloring.FONT_MODE_APPLY_STYLE, Color.BLUE, null));
    };
    
    /**
     * Creates a new instance of HintsControllerImpl
     */
    private HintsControllerImpl() {
    }
    
    static AnnotationHolder getLayersForDocument(Document doc) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od == null) {
            return new AnnotationHolder(null);
        }

        AnnotationHolder layers = doc2Annotation.get(od.getPrimaryFile());
        
        if (layers == null) {
            doc2Annotation.put(od.getPrimaryFile(), layers = new AnnotationHolder(od.getPrimaryFile()));
        }
        
        return layers;
    }
    
    public static Collection<FileObject> coveredFiles() {
        return Collections.unmodifiableSet(doc2Annotation.keySet());
    }
    
    public static List<ErrorDescription> getErrors(FileObject file) {
        Map<String, List<Annotation>> key2Annotations = (Map<String, List<Annotation>>) doc2Annotation.get(file);
        
        if (key2Annotations == null)
            return Collections.<ErrorDescription>emptyList();
        
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        
        for (List<Annotation> l : key2Annotations.values()) {
            for (Annotation a : l) {
                result.add(((ParseErrorAnnotation) a).getDescription());
            }
        }
        
        return result;
    }
    
    public static void setErrors(Document doc, String layer, Collection<? extends ErrorDescription> errors) {
        try {
            setErrorsImpl(doc, layer, errors);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public static void setErrors(FileObject file, String layer, Collection<? extends ErrorDescription> errors) {
        try {
            setErrorsImpl(file, layer, errors);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private static void setErrorsImpl(FileObject file, String layer, Collection<? extends ErrorDescription> errors) throws IOException {
        DataObject od = DataObject.find(file);
        EditorCookie ec = (EditorCookie) od.getCookie(EditorCookie.class);
        
        if (ec == null) {
            //cannot set:
            //TODO: log
            return ;
        }
        
        setErrorsImpl(ec.openDocument(), od, layer, errors);
    }
    
    private static void setErrorsImpl(Document doc, String layer, Collection<? extends ErrorDescription> errors) throws IOException {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od == null) {
            //cannot set:
            //TODO: log
            return ;
        }
        
        setErrorsImpl(doc, od, layer, errors);
    }
    
    private static void setErrorsImpl(Document doc, DataObject od, String layer, Collection<? extends ErrorDescription> errors) throws IOException {
        LineCookie lc = (LineCookie) od.getCookie(LineCookie.class);
        
        if (lc == null) {
            //cannot set:
            //TODO: log
            return ;
        }
        
        Map<Integer, List<ErrorDescription>> line2Descriptions = new HashMap<Integer, List<ErrorDescription>>();
        
        for (Iterator i = errors.iterator(); i.hasNext(); ) {
            ErrorDescription desc = (ErrorDescription) i.next();
            PositionBounds span = desc.getRange();
            
            if (span == null || desc.getSeverity() == Severity.DISABLED)
                 continue;
            
            Integer lineInt = new Integer(span.getBegin().getLine());
            List<ErrorDescription> descs = line2Descriptions.get(lineInt);
            
            if (descs == null) {
                line2Descriptions.put(lineInt, descs = new ArrayList<ErrorDescription>());
            }
            
            descs.add(desc);
        }
        
        AnnotationHolder holder = getLayersForDocument(doc);
        
        holder.setErrorDescriptions(layer, doc, line2Descriptions);
        
        List<Highlight> highlights  = new ArrayList<Highlight>();
        
        for (ErrorDescription current : errors) {
            PositionBounds span = current.getRange();
            
            if (span == null || current.getSeverity() == Severity.DISABLED)
                continue;
            
            highlights.add(new DefaultHighlight(COLORINGS.get(current.getSeverity()), span.getBegin().getPosition(), span.getEnd().getPosition()));
        }
        
        Highlighter.getDefault().setHighlights(od.getPrimaryFile(), "HintsControllerImpl-" + layer, highlights);
        
        FileObject f = od.getPrimaryFile();
        
        updateInError(f);
        
        fireChanges();
    }
    
    static ErrorDescription createCompoundErrorDescription(Line.Part part, Document doc, List<ErrorDescription> descriptions) throws IOException {
        StringBuffer description = new StringBuffer();
        List<LazyFixList> fixes = new ArrayList<LazyFixList>();
        Severity severity = Severity.TODO;
        
        for (Iterator<ErrorDescription> i = descriptions.iterator(); i.hasNext(); ) {
            ErrorDescription desc = i.next();
            
            description.append(desc.getDescription());
            
            if (i.hasNext())
                description.append("\n\n");
            
            fixes.add(desc.getFixes());
            
            if (severity.compareTo(desc.getSeverity()) > 0)
                severity = desc.getSeverity();
        }
        
        PositionBounds bounds = boundsForPart(part);
        
        return ErrorDescriptionFactory.createErrorDescription(severity, description.toString(), new CompoundLazyFixList(fixes), doc, bounds.getBegin().getPosition(), bounds.getEnd().getPosition());
    }
    
    static Line.Part fullLine(Line line) {
        String text = line.getText();
        
        if (text == null) {
            //document closed, cannot create:
            return null;
        }
        
        int column = 0;
        int length = text.length();
        
        while (column < text.length() && Character.isWhitespace(text.charAt(column))) {
            column++;
        }
        
        while (length > 0 && Character.isWhitespace(text.charAt(length - 1)))
            length--;
        
        return line.createPart(column, length);
    }
    
    public static PositionBounds boundsForPart(Line.Part part) {
        DataObject file = part.getLine().getLookup().lookup(DataObject.class);
        
        if (file == null)
            return null;
        
        EditorCookie ec = (EditorCookie) file.getCookie(EditorCookie.class);
        
        if (ec == null)
            return null;
        
        try {
            StyledDocument doc = ec.openDocument();
            
            int lineStartOffset = NbDocument.findLineOffset(doc, part.getLine().getLineNumber());
            
            return linePart(file.getPrimaryFile(), lineStartOffset + part.getColumn(), lineStartOffset + part.getColumn() + part.getLength());
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
    public static PositionBounds fullLine(Document doc, int lineNumber) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od == null)
            return null;
        
        LineCookie lc = od.getCookie(LineCookie.class);
        
        return boundsForPart(fullLine(lc.getLineSet().getCurrent(lineNumber - 1)));
    }

    public static PositionBounds linePart(Document doc, final Position start, final Position end) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od == null)
            return null;
        
        EditorCookie ec = (EditorCookie) od.getCookie(EditorCookie.class);
        
        if (!(ec instanceof CloneableEditorSupport)) {
            return null;
        }
        
        final CloneableEditorSupport ces = (CloneableEditorSupport) ec;
        
        final PositionRef[] refs = new PositionRef[2];
        
        doc.render(new Runnable() {
            public void run() {
                refs[0] = ces.createPositionRef(start.getOffset(), Position.Bias.Forward);
                refs[1] = ces.createPositionRef(end.getOffset(), Position.Bias.Backward);
            }
        });
        
        return new PositionBounds(refs[0], refs[1]);
    }

    public static PositionBounds linePart(FileObject file, int start, int end) {
        try {
            DataObject od = DataObject.find(file);
            
            if (od == null)
                return null;
            
            EditorCookie ec = od.getCookie(EditorCookie.class);
            
            if (!(ec instanceof CloneableEditorSupport)) {
                return null;
            }
            
            final CloneableEditorSupport ces = (CloneableEditorSupport) ec;
            
            return new PositionBounds(ces.createPositionRef(start, Position.Bias.Forward), ces.createPositionRef(end, Position.Bias.Backward));
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return null;
        }
    }

    public static boolean isInError(Set<FileObject> fos) {
        for (Iterator<FileObject> i = fos.iterator(); i.hasNext(); ) {
            FileObject f = (FileObject) i.next();
            
            if (isInError(f)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean isInError(FileObject fo) {
//        if (/*ProvidersList.getEagerness() == ProvidersList.EAGER_ON_DEMAND && */fo.isData() && !PersistentCache.getDefault().isKnown(fo)) {
//            //never heard of it:
//            HintsOperator.getDefault().enqueue(fo);
//        }
        
        //temporarily disabling error markers on file icons:
//        return PersistentCache.getDefault().hasErrors(fo);
        return false;
    }
    
    private static void updateInError(FileObject fo) {
        //temporarily disabling error markers on file icons:
        if (true)
            return;
        
        boolean hasErrors = false;
        AnnotationHolder layers = doc2Annotation.get(fo);
        
        if (layers != null) {
            hasErrors = layers.hasErrors();
        }
        
        FileObject recursive = fo;
        
        while (recursive != null) {
            if (hasErrors) {
                PersistentCache.getDefault().addErrorFile(recursive, fo);
            } else {
                PersistentCache.getDefault().removeErrorFile(recursive, fo);
            }
            recursive = recursive.getParent();
        }
    }
    
    private static List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    public static synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public static synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private static void fireChanges() {
        List<ChangeListener> ls;
        
        synchronized (HintsControllerImpl.class) {
            ls = new ArrayList<ChangeListener>(listeners);
        }
        
        ChangeEvent e = new ChangeEvent(HintsControllerImpl.class);
        
        for (ChangeListener l : ls) {
            l.stateChanged(e);
        }
    }
    
    public static class CompoundLazyFixList implements LazyFixList, PropertyChangeListener {
        
        private List<LazyFixList> delegates;
        
        private List<Fix> fixesCache;
        private Boolean computedCache;
        private Boolean probablyContainsFixesCache;
        
        private PropertyChangeSupport pcs;
        
        public CompoundLazyFixList(List<LazyFixList> delegates) {
            this.delegates = delegates;
            this.pcs = new PropertyChangeSupport(this);
            
            for (LazyFixList l : delegates) {
                l.addPropertyChangeListener(this);
            }
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

        public synchronized boolean probablyContainsFixes() {
            if (probablyContainsFixesCache == null) {
                boolean result = false;
                
                for (LazyFixList l : delegates) {
                    result |= l.probablyContainsFixes();
                }
                
                probablyContainsFixesCache = Boolean.valueOf(result);
            }
            
            return probablyContainsFixesCache;
        }

        public synchronized List<Fix> getFixes() {
            if (fixesCache == null) {
                fixesCache = new ArrayList<Fix>();
                
                for (LazyFixList l : delegates) {
                    fixesCache.addAll(l.getFixes());
                }
            }
            
            return fixesCache;
        }

        public synchronized boolean isComputed() {
            if (computedCache == null) {
                boolean result = true;
                
                for (LazyFixList l : delegates) {
                    result &= l.isComputed();
                }
                
                computedCache = Boolean.valueOf(result);
            }
            
            return computedCache;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (PROP_FIXES.equals(evt.getPropertyName())) {
                synchronized (this) {
                    fixesCache = null;
                }
                pcs.firePropertyChange(PROP_FIXES, null, null);
                return;
            }
                
            if (PROP_COMPUTED.equals(evt.getPropertyName())) {
                synchronized (this) {
                    computedCache = null;
                }
                pcs.firePropertyChange(PROP_COMPUTED, null, null);
            }
        }
        
    }
    
}
