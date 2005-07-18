/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.errorstripe;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationType.Severity;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProviderCreator;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.lookup.ProxyLookup;
import org.netbeans.modules.editor.errorstripe.apimodule.SPIAccessor;

/**
 *
 * @author Jan Lahoda
 */
final class AnnotationViewDataImpl implements PropertyChangeListener, AnnotationViewData, Annotations.AnnotationsListener {
    
    private static final ErrorManager ERR = AnnotationView.ERR;
    
    private AnnotationView view;
    private JTextComponent pane;
    private BaseDocument document;
    
    private List/*<MarkProvider>*/ providers;
    private List/*<UpToDateStatusProvider>*/ upToDateStatusProviders;
    
    private List/*<Mark>*/ currentMarks = null;
    private SortedMap/*<Mark>*/ marksMap = null;
    
    /** Creates a new instance of AnnotationViewData */
    public AnnotationViewDataImpl(AnnotationView view, JTextComponent pane) {
        this.view = view;
        this.pane = pane;
        this.document = null;
    }
    
    public void register(BaseDocument document) {
        this.document = document;
        
        gatherProviders(pane);
        addListenersToProviders();
        
        if (document != null) {
            document.getAnnotations().addAnnotationsListener(this);
        }
        
        currentMarks = null;
        marksMap     = null;
    }
    
    public void unregister() {
        //TODO: remove listeners!
    }
    
    private void gatherProviders(JTextComponent pane) {
        long start = System.currentTimeMillis();
        try {
            BaseKit kit = Utilities.getKit(pane);
            
            if (kit == null)
                return ;
            
            String content = kit.getContentType();
            BaseDocument document = (BaseDocument) pane.getDocument();
            FileObject baseFolder = Repository.getDefault().getDefaultFileSystem().findResource("Editors/text/base/Services"); // NOI18N
            FileObject contentFolder = Repository.getDefault().getDefaultFileSystem().findResource("Editors/" + content + "/Services"); // NOI18N
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "baseFolder = " + baseFolder );
            }
            
            DataObject baseDO = baseFolder != null ? DataObject.find(baseFolder) : null;
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "baseDO = " + baseDO );
            }
            
            Lookup baseLookup = baseFolder != null ? new FolderLookup((DataFolder) baseDO).getLookup() : Lookup.EMPTY;
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "contentFolder = " + contentFolder );
            }
            
            DataObject contentDO = contentFolder != null ? DataObject.find(contentFolder) : null;
            Lookup contentLookup = contentFolder != null ? new FolderLookup((DataFolder) contentDO).getLookup() : Lookup.EMPTY;
            
            Lookup lookup = new ProxyLookup(new Lookup[] {baseLookup, contentLookup});
            
            Result creators = lookup.lookup(new Template(MarkProviderCreator.class));
            
            List markProviders = new ArrayList();
            
            for (Iterator i = creators.allInstances().iterator(); i.hasNext(); ) {
                MarkProviderCreator creator = (MarkProviderCreator) i.next();
                
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "creator = " + creator );
                }
                
                MarkProvider provider = creator.createMarkProvider(pane);
                
                if (provider != null)
                    markProviders.add(provider);
            }
            
            this.providers = markProviders;
            
            Result updsCreators = lookup.lookup(new Template(UpToDateStatusProviderFactory.class));
            List updsProviders = new ArrayList();
            
            for (Iterator i = updsCreators.allInstances().iterator(); i.hasNext(); ) {
                UpToDateStatusProviderFactory creator = (UpToDateStatusProviderFactory) i.next();
                
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "creator = " + creator );
                }
                
                UpToDateStatusProvider provider = creator.createUpToDateStatusProvider(pane.getDocument());
                
                if (provider != null)
                    updsProviders.add(provider);
            }
            
            this.upToDateStatusProviders = updsProviders;
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        
        long end = System.currentTimeMillis();
        
        if (AnnotationView.TIMING_ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            AnnotationView.TIMING_ERR.log(ErrorManager.INFORMATIONAL, "gather providers took: " + (end - start));
        }
    }
    
    private void addListenersToProviders() {
        for (Iterator p = upToDateStatusProviders.iterator(); p.hasNext(); ) {
            UpToDateStatusProvider provider = (UpToDateStatusProvider) p.next();
            
            SPIAccessor.getDefault().addPropertyChangeListener(provider, this);
        }
        
        for (Iterator p = providers.iterator(); p.hasNext(); ) {
            MarkProvider provider = (MarkProvider) p.next();
            
            provider.addPropertyChangeListener(this);
        }
    }

    /*package private*/ static List/*<Mark>*/ createMergedMarks(List/*<MarkProvider>*/ providers) {
        List result = new ArrayList();
        
        for (Iterator p = providers.iterator(); p.hasNext(); ) {
            MarkProvider provider = (MarkProvider) p.next();
            
            result.addAll(provider.getMarks());
        }
        
        return result;
    }
    
    /*package private for tests*/synchronized List/*<Mark>*/ getMergedMarks() {
        if (currentMarks == null) {
            currentMarks = createMergedMarks(providers);
        }
        
        return currentMarks;
    }
    
    /*package private*/ static List/*<Mark>*/ getStatusesForLineImpl(int line, SortedMap marks) {
        List inside = (List) marks.get(new Integer(line));
        
        if (inside == null)
            return Collections.EMPTY_LIST;
        
        return inside;
    }
    
    public Mark getMainMarkForBlock(int startLine, int endLine) {
        Mark m1 = getMainMarkForBlockImpl(startLine, endLine, getMarkMap());
        Mark m2 = getMainMarkForBlockAnnotations(startLine, endLine);
        
        if (m1 == null)
            return m2;
        
        if (m2 == null)
            return m1;
        
        if (m1.getStatus().compareTo(m2.getStatus()) > 0)
            return m1;
        else
            return m2;
    }
    
    /*package private*/ static Mark getMainMarkForBlockImpl(int startLine, int endLine, SortedMap marks) {
        int current = startLine - 1;
        Mark found = null;
        
        while ((current = findNextUsedLine(current, marks)) != Integer.MAX_VALUE && current <= endLine) {
            for (Iterator i = getStatusesForLineImpl(/*doc, */current, marks).iterator(); i.hasNext(); ) {
                Mark newMark = (Mark) i.next();
                
                if (found == null || newMark.getStatus().compareTo(found.getStatus()) > 0) {
                    found = newMark;
                }
            }
            current++;
        }
        
        return found;
    }
    
    private boolean isMoreImportant(AnnotationDesc a1, AnnotationDesc a2) {
        return a1.getAnnotationTypeInstance().getSeverity().compareTo(a2.getAnnotationTypeInstance().getSeverity()) > 0;
    }
    
    private boolean isValidForErrorStripe(AnnotationDesc a) {
        return a.getAnnotationTypeInstance().getSeverity() != AnnotationType.Severity.STATUS_NONE;
    }
    
    private Mark getMainMarkForBlockAnnotations(int startLine, int endLine) {
        int line = startLine;
        AnnotationDesc foundDesc = null;
        Annotations annotations = document.getAnnotations();
        
        while ((line = annotations.getNextLineWithAnnotation(line)) <= endLine && line != (-1)) {
            AnnotationDesc desc = annotations.getActiveAnnotation(line);
            
            if (desc != null) {
                if ((foundDesc == null || isMoreImportant(desc, foundDesc)) && isValidForErrorStripe(desc))
                    foundDesc = desc;
            }
            
            if (annotations.getNumberOfAnnotations(line) > 1) {
                AnnotationDesc[] descriptions = annotations.getPasiveAnnotations(line);
                
                for (int cntr = 0; cntr < descriptions.length; cntr++) {
                    if ((foundDesc == null || isMoreImportant(descriptions[cntr], foundDesc)) && isValidForErrorStripe(descriptions[cntr]))
                        foundDesc = descriptions[cntr];
                }
            }
            
            line++;
        }
        
        if (foundDesc != null)
            return new AnnotationMark(foundDesc);
        else
            return null;
    }

    public int findNextUsedLine(int from) {
        int line1 = findNextUsedLine(from, getMarkMap());
        int line2 = document.getAnnotations().getNextLineWithAnnotation(from + 1);
        
        if (line2 == (-1))
            line2 = Integer.MAX_VALUE;
        
        return line1 < line2 ? line1 : line2;
    }
    
    /*package private*/ static int findNextUsedLine(int from, SortedMap/*<Mark>*/ marks) {
        SortedMap next = marks.tailMap(new Integer(from + 1));
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log("AnnotationView.findNextUsedLine from: " + from);
            ERR.log("AnnotationView.findNextUsedLine marks: " + marks);
            ERR.log("AnnotationView.findNextUsedLine next: " + next);
        }
        
        if (next.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        
        Integer nextLine = (Integer) next.firstKey();
        
        return nextLine.intValue();
    }
    
    private void registerMark(Mark mark) {
        int[] span = mark.getAssignedLines();
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log("AnnotationView.registerMark mark: " + mark);
            ERR.log("AnnotationView.registerMark lines from-to: " + span[0] + "-" + span[1]);
        }
        
        for (int line = span[0]; line <= span[1]; line++) {
            Integer lineInt = new Integer(line);
            
            List inside = (List) marksMap.get(lineInt);
            
            if (inside == null) {
                marksMap.put(lineInt, inside = new ArrayList());
            }
            
            inside.add(mark);
        }
    }
    
    private void unregisterMark(Mark mark) {
        int[] span = mark.getAssignedLines();
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log("AnnotationView.unregisterMark mark: " + mark);
            ERR.log("AnnotationView.unregisterMark lines from-to: " + span[0] + "-" + span[1]);
        }
        
        for (int line = span[0]; line <= span[1]; line++) {
            Integer lineInt = new Integer(line);
            
            List inside = (List) marksMap.get(lineInt);
            
            if (inside != null) {
                inside.remove(mark);
                
                if (inside.size() == 0) {
                    marksMap.remove(lineInt);
                }
            }
        }
    }
    
    /*package private for tests*/synchronized SortedMap getMarkMap() {
        if (marksMap == null) {
            List/*<Mark>*/ marks = getMergedMarks();
            marksMap = new TreeMap();
            
            for (Iterator i = marks.iterator(); i.hasNext(); ) {
                Mark mark = (Mark) i.next();
                
                registerMark(mark);
            }
        }
        
        return marksMap;
    }

    public Status computeTotalStatus() {
        Status targetStatus = Status.STATUS_OK;
        Collection/*<Mark>*/ marks = getMergedMarks();
        
        for (Iterator m = marks.iterator(); m.hasNext(); ) {
            Mark mark = (Mark) m.next();
            Status s = mark.getStatus();
            
            targetStatus = Status.getCompoundStatus(s, targetStatus);
        }

        Annotations annotations = document.getAnnotations();
        int line = -1;
        
        while ((line = annotations.getNextLineWithAnnotation(line)) != (-1)) {
            AnnotationDesc desc = annotations.getActiveAnnotation(line);
            
            if (desc != null) {
                Status s = get(desc.getAnnotationTypeInstance());
                
                if (s != null)
                    targetStatus = Status.getCompoundStatus(s, targetStatus);
            }
            
            if (annotations.getNumberOfAnnotations(line) > 1) {
                AnnotationDesc[] descriptions = annotations.getPasiveAnnotations(line);
                
                for (int cntr = 0; cntr < descriptions.length; cntr++) {
                    Status s = get(descriptions[cntr].getAnnotationTypeInstance());
                    
                    if (s != null)
                        targetStatus = Status.getCompoundStatus(s, targetStatus);
                }
            }
            
            line++;
        }
        
        return targetStatus;
    }
    
    public UpToDateStatus computeTotalStatusType() {
        UpToDateStatus statusType = UpToDateStatus.UP_TO_DATE_OK;
        
        for (Iterator p = upToDateStatusProviders.iterator(); p.hasNext(); ) {
            UpToDateStatusProvider provider = (UpToDateStatusProvider) p.next();
            UpToDateStatus newType = provider.getUpToDate();
            
            if (newType.compareTo(statusType) > 0) {
                statusType = newType;
            }
        }
        
        return statusType;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("marks".equals(evt.getPropertyName())) {
            synchronized (this) {
                Collection nue = (Collection) evt.getNewValue();
                Collection old = (Collection) evt.getOldValue();
                
                if (nue == null && evt.getSource() instanceof MarkProvider)
                    nue = ((MarkProvider) evt.getSource()).getMarks();
                
                if (old != null && nue != null) {
                    List added = new ArrayList(nue);
                    List removed = new ArrayList(old);
                    
                    added.removeAll(old);
                    removed.removeAll(nue);
                    
                    if (marksMap != null) {
                        for (Iterator i = removed.iterator(); i.hasNext(); ) {
                            unregisterMark((Mark) i.next());
                        }
                        
                        for (Iterator i = added.iterator(); i.hasNext(); ) {
                            registerMark((Mark) i.next());
                        }
                    }
                    
                    if (currentMarks != null) {
                        currentMarks.removeAll(removed);
                        currentMarks.addAll(added);
                    }
                    
                    view.fullRepaint();
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "For performance reasons, the providers should fill both old and new value in property changes. Problematic event: " + evt);
                    clear();
                    view.fullRepaint();
                }
                return ;
            }
        }
        
        if (UpToDateStatusProvider.PROP_UP_TO_DATE.equals(evt.getPropertyName())) {
            view.fullRepaint(false);
            return ;
        }
    }

    public void clear() {
        currentMarks = null;
        marksMap = null;
    }
    
    public int[] computeErrorsAndWarnings() {
        int errors = 0;
        int warnings = 0;
        Collection/*<Mark>*/ marks = getMergedMarks();
        
        for (Iterator m = marks.iterator(); m.hasNext(); ) {
            Mark mark = (Mark) m.next();
            Status s = mark.getStatus();
            
            errors += s == Status.STATUS_ERROR ? 1 : 0;
            warnings += s == Status.STATUS_WARNING ? 1 : 0;
        }
        
        Annotations annotations = document.getAnnotations();
        int line = -1;
        
        while ((line = annotations.getNextLineWithAnnotation(line)) != (-1)) {
            AnnotationDesc desc = annotations.getActiveAnnotation(line);
            
            if (desc != null) {
                Status s = get(desc.getAnnotationTypeInstance());
                
                if (s != null) {
                    errors += s == Status.STATUS_ERROR ? 1 : 0;
                    warnings += s == Status.STATUS_WARNING ? 1 : 0;
                }
            }
            
            if (annotations.getNumberOfAnnotations(line) > 1) {
                AnnotationDesc[] descriptions = annotations.getPasiveAnnotations(line);
                
                for (int cntr = 0; cntr < descriptions.length; cntr++) {
                    Status s = get(descriptions[cntr].getAnnotationTypeInstance());
                    
                    if (s != null) {
                        errors += s == Status.STATUS_ERROR ? 1 : 0;
                        warnings += s == Status.STATUS_WARNING ? 1 : 0;
                    }
                }
            }
            
            line++;
        }
        
        return new int[] {errors, warnings};
    }
    
    public void changedLine(int Line) {
        changedAll();
    }
    
    public void changedAll() {
        view.fullRepaint(false);
    }
    
    static Status get(Severity severity) {
        if (severity == Severity.STATUS_ERROR)
            return Status.STATUS_ERROR;
        if (severity == Severity.STATUS_WARNING)
            return Status.STATUS_WARNING;
        if (severity == Severity.STATUS_OK)
            return Status.STATUS_OK;
        
        return null;
    }
    
    static Status get(AnnotationType ann) {
        return get(ann.getSeverity());
    }
}
