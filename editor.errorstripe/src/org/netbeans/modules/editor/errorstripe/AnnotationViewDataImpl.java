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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.errorstripe;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationType.Severity;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProviderCreator;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.openide.ErrorManager;
import org.netbeans.modules.editor.errorstripe.apimodule.SPIAccessor;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
final class AnnotationViewDataImpl implements PropertyChangeListener, AnnotationViewData, Annotations.AnnotationsListener {
    
    private static final Logger LOG = Logger.getLogger(AnnotationViewDataImpl.class.getName());
    
    private static final String UP_TO_DATE_STATUS_PROVIDER_FOLDER_NAME = "UpToDateStatusProvider"; //NOI18N
    private static final String TEXT_BASE_PATH = "Editors/text/base/"; //NOI18N

    private AnnotationView view;
    private JTextComponent pane;
    private BaseDocument document;
    
    private List<MarkProvider> markProviders = new ArrayList<MarkProvider>();
    private List<UpToDateStatusProvider> statusProviders = new ArrayList<UpToDateStatusProvider>();
    
    private Collection<Mark> currentMarks = null;
    private SortedMap<Integer, List<Mark>> marksMap = null;

    private static WeakHashMap<String, Collection<? extends MarkProviderCreator>> mime2Creators = new WeakHashMap<String, Collection<? extends MarkProviderCreator>>();
    private static WeakHashMap<String, Collection<? extends UpToDateStatusProviderFactory>> mime2StatusProviders = new WeakHashMap<String, Collection<? extends UpToDateStatusProviderFactory>>();

    private static LegacyCrapProvider legacyCrap;
    
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
        
        clear();
    }
    
    public void unregister() {
        if (document != null) {
            document.getAnnotations().removeAnnotationsListener(this);
        }
        
        removeListenersFromProviders();
        
        document = null;
    }

    public static void initProviders(String mimeType) {
        // Legacy mime path (text/base)
        MimePath legacyMimePath = MimePath.parse("text/base");
        legacyCrap = MimeLookup.getLookup(legacyMimePath).lookup(LegacyCrapProvider.class);
        MimePath mimePath = MimePath.parse(mimeType);
        // Mark providers
        mime2Creators.put(mimeType, MimeLookup.getLookup(mimePath).lookupAll(MarkProviderCreator.class));
        // Status providers
        mime2StatusProviders.put(mimeType, MimeLookup.getLookup(mimePath).lookupAll(UpToDateStatusProviderFactory.class));
    }
    
    private void gatherProviders(JTextComponent pane) {
        long start = System.currentTimeMillis();

        // Collect legacy mark providers
        List<MarkProvider> newMarkProviders = new ArrayList<MarkProvider>();
        if (legacyCrap != null) {
            createMarkProviders(legacyCrap.getMarkProviderCreators(), newMarkProviders, pane);
        }
        
        // Collect mark providers
        String mimeType = pane.getUI().getEditorKit(pane).getContentType();
        Collection<? extends MarkProviderCreator> creators = 
            mime2Creators.get(mimeType);
        createMarkProviders(creators, newMarkProviders, pane);

        this.markProviders = newMarkProviders;

        
        // Collect legacy status providers
        List<UpToDateStatusProvider> newStatusProviders = new ArrayList<UpToDateStatusProvider>();
        if (legacyCrap != null) {
            createStatusProviders(legacyCrap.getUpToDateStatusProviderFactories(), newStatusProviders, pane);
        }
        
        // Collect status providers
        Collection<? extends UpToDateStatusProviderFactory> factories = 
            mime2StatusProviders.get(mimeType);
        createStatusProviders(factories, newStatusProviders, pane);

        this.statusProviders = newStatusProviders;
        
        
        long end = System.currentTimeMillis();
        if (AnnotationView.TIMING_ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            AnnotationView.TIMING_ERR.log(ErrorManager.INFORMATIONAL, "gather providers took: " + (end - start));
        }
    }

    private static void createMarkProviders(Collection<? extends MarkProviderCreator> creators, List<MarkProvider> providers, JTextComponent pane) {
        for (MarkProviderCreator creator : creators) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("creator = " + creator);
            }

            MarkProvider provider = creator.createMarkProvider(pane);
            if (provider != null) {
                providers.add(provider);
            }
        }
    }

    private static void createStatusProviders(Collection<? extends UpToDateStatusProviderFactory> factories, List<UpToDateStatusProvider> providers, JTextComponent pane) {
        for(UpToDateStatusProviderFactory factory : factories) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("factory = " + factory);
            }

            UpToDateStatusProvider provider = factory.createUpToDateStatusProvider(pane.getDocument());
            if (provider != null) {
                providers.add(provider);
            }
        }
    }
    
    private void addListenersToProviders() {
        for (Iterator p = statusProviders.iterator(); p.hasNext(); ) {
            UpToDateStatusProvider provider = (UpToDateStatusProvider) p.next();
            
            SPIAccessor.getDefault().addPropertyChangeListener(provider, this);
        }
        
        for (Iterator p = markProviders.iterator(); p.hasNext(); ) {
            MarkProvider provider = (MarkProvider) p.next();
            
            provider.addPropertyChangeListener(this);
        }
    }

    private void removeListenersFromProviders() {
        for (Iterator p = statusProviders.iterator(); p.hasNext(); ) {
            UpToDateStatusProvider provider = (UpToDateStatusProvider) p.next();
            
            SPIAccessor.getDefault().removePropertyChangeListener(provider, this);
        }
        
        for (Iterator p = markProviders.iterator(); p.hasNext(); ) {
            MarkProvider provider = (MarkProvider) p.next();
            
            provider.removePropertyChangeListener(this);
        }
    }
    
    /*package private*/ static Collection<Mark> createMergedMarks(List<MarkProvider> providers) {
        Collection<Mark> result = new LinkedHashSet<Mark>();
        
        for(MarkProvider provider : providers) {
            result.addAll(provider.getMarks());
        }
        
        return result;
    }
    
    /*package private for tests*/synchronized Collection<Mark> getMergedMarks() {
        if (currentMarks == null) {
            currentMarks = createMergedMarks(markProviders);
        }
        
        return currentMarks;
    }
    
    /*package private*/ static List<Mark> getStatusesForLineImpl(int line, SortedMap<Integer, List<Mark>> marks) {
        List<Mark> inside = marks.get(line);
        return inside == null ? Collections.<Mark>emptyList() : inside;
    }
    
    public Mark getMainMarkForBlock(int startLine, int endLine) {
        Mark m1;
        synchronized(this) {
            m1 = getMainMarkForBlockImpl(startLine, endLine, getMarkMap());
        }
        Mark m2 = getMainMarkForBlockAnnotations(startLine, endLine);
        
        if (m1 == null)
            return m2;
        
        if (m2 == null)
            return m1;
        
        if (isMoreImportant(m1, m2))
            return m1;
        else
            return m2;
    }
    
    /*package private*/ static Mark getMainMarkForBlockImpl(int startLine, int endLine, SortedMap<Integer, List<Mark>> marks) {
        int current = startLine - 1;
        Mark found = null;
        
        while ((current = findNextUsedLine(current, marks)) != Integer.MAX_VALUE && current <= endLine) {
            for (Mark newMark : getStatusesForLineImpl(/*doc, */current, marks)) {
                if (found == null || isMoreImportant(newMark, found)) {
                    found = newMark;
                }
            }
        }
        
        return found;
    }
    
    private static boolean isMoreImportant(Mark m1, Mark m2) {
        int compared = m1.getStatus().compareTo(m2.getStatus());
        
        if (compared == 0)
            return m1.getPriority() < m2.getPriority();
        
        return compared > 0;
    }
    
    private boolean isMoreImportant(AnnotationDesc a1, AnnotationDesc a2) {
        AnnotationType t1 = a1.getAnnotationTypeInstance();
        AnnotationType t2 = a2.getAnnotationTypeInstance();
        
        int compared = t1.getSeverity().compareTo(t2.getSeverity());
        
        if (compared == 0)
            return t1.getPriority() < t2.getPriority();
        
        return compared > 0;
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

                if (descriptions != null) {
                    for (int cntr = 0; cntr < descriptions.length; cntr++) {
                        if ((foundDesc == null || isMoreImportant(descriptions[cntr], foundDesc)) && isValidForErrorStripe(descriptions[cntr]))
                            foundDesc = descriptions[cntr];
                    }
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
        int line1;
        synchronized (this) {
            line1 = findNextUsedLine(from, getMarkMap());
        }

        int line2 = document.getAnnotations().getNextLineWithAnnotation(from + 1);
        
        if (line2 == (-1))
            line2 = Integer.MAX_VALUE;
        
        return line1 < line2 ? line1 : line2;
    }
    
    /*package private*/ static int findNextUsedLine(int from, SortedMap<Integer, List<Mark>> marks) {
        SortedMap<Integer, List<Mark>> next = marks.tailMap(from + 1);
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("AnnotationView.findNextUsedLine from: " + from + "; marks: " + marks + "; next: " + next); //NOI18N
        }
        
        if (next.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        
        return next.firstKey().intValue();
    }
    
    private void registerMark(Mark mark) {
        int[] span = mark.getAssignedLines();
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("AnnotationView.registerMark mark: " + mark + "; from-to: " + span[0] + "-" + span[1]); //NOI18N
        }
        
        for (int line = span[0]; line <= span[1]; line++) {
            List<Mark> inside = marksMap.get(line);
            
            if (inside == null) {
                inside = new ArrayList<Mark>();
                marksMap.put(line, inside);
            }
            
            inside.add(mark);
        }
    }
    
    private void unregisterMark(Mark mark) {
        int[] span = mark.getAssignedLines();
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("AnnotationView.unregisterMark mark: " + mark + "; from-to: " + span[0] + "-" + span[1]); //NOI18N
        }
        
        for (int line = span[0]; line <= span[1]; line++) {
            List<Mark> inside = marksMap.get(line);
            
            if (inside != null) {
                inside.remove(mark);
                
                if (inside.size() == 0) {
                    marksMap.remove(line);
                }
            }
        }
    }
    
    /*package private for tests*/synchronized SortedMap<Integer, List<Mark>> getMarkMap() {
        if (marksMap == null) {
            Collection<Mark> marks = getMergedMarks();
            marksMap = new TreeMap<Integer, List<Mark>>();
            
            for (Mark mark : marks) {
                registerMark(mark);
            }
        }
        
        return marksMap;
    }

    public Status computeTotalStatus() {
        Status targetStatus = Status.STATUS_OK;
        Collection<Mark> marks = getMergedMarks();
        
        for(Mark mark : marks) {
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
            
            AnnotationDesc[] descriptions = annotations.getPasiveAnnotations(line);
            if(descriptions!=null) {
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
        if (statusProviders.isEmpty())
            return UpToDateStatus.UP_TO_DATE_DIRTY;
        
        UpToDateStatus statusType = UpToDateStatus.UP_TO_DATE_OK;
        
        for (Iterator p = statusProviders.iterator(); p.hasNext(); ) {
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
                @SuppressWarnings("unchecked")
                Collection<Mark> nue = (Collection<Mark>) evt.getNewValue();
                @SuppressWarnings("unchecked")
                Collection<Mark> old = (Collection<Mark>) evt.getOldValue();
                
                if (nue == null && evt.getSource() instanceof MarkProvider)
                    nue = ((MarkProvider) evt.getSource()).getMarks();
                
                if (old != null && nue != null) {
                    Collection<Mark> added = new LinkedHashSet<Mark>(nue);
                    Collection<Mark> removed = new LinkedHashSet<Mark>(old);
                    
                    added.removeAll(old);
                    removed.removeAll(nue);
                    
                    if (marksMap != null) {
                        for(Mark mark : removed) {
                            unregisterMark(mark);
                        }
                        
                        for(Mark mark : added) {
                            registerMark(mark);
                        }
                    }
                    
                    if (currentMarks != null) {
                        currentMarks.removeAll(removed);
                        currentMarks.addAll(added);
                    }
                    
                    view.fullRepaint();
                } else {
                    LOG.warning("For performance reasons, the providers should fill both old and new value in property changes. Problematic event: " + evt);
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

    public synchronized void clear() {
        currentMarks = null;
        marksMap = null;
    }
    
    public int[] computeErrorsAndWarnings() {
        int errors = 0;
        int warnings = 0;
        Collection<Mark> marks = getMergedMarks();
        
        for(Mark mark : marks) {
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
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.editor.mimelookup.Class2LayerFolder.class)
    public static final class UpToDateStatusProviderFactoriesProvider implements Class2LayerFolder {

        public UpToDateStatusProviderFactoriesProvider() {
            
        }
        
        public Class getClazz() {
            return UpToDateStatusProviderFactory.class;
        }

        public String getLayerFolderName() {
            return UP_TO_DATE_STATUS_PROVIDER_FOLDER_NAME;
        }

        public InstanceProvider getInstanceProvider() {
            return null;
        }
    } // End of UpToDateStatusProviderFactoriesProvider class

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.editor.mimelookup.Class2LayerFolder.class)
    public static final class MarkProviderCreatorsProvider implements Class2LayerFolder {

        public MarkProviderCreatorsProvider() {
            
        }
        
        public Class getClazz() {
            return MarkProviderCreator.class;
        }

        public String getLayerFolderName() {
            return UP_TO_DATE_STATUS_PROVIDER_FOLDER_NAME;
        }

        public InstanceProvider getInstanceProvider() {
            return null;
        }
    } // End of UpToDateStatusProviderFactoriesProvider class
    
    // XXX: This is here to help to deal with legacy code
    // that registered stuff in text/base. The artificial text/base
    // mime type is deprecated and should not be used anymore.
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.editor.mimelookup.Class2LayerFolder.class)
    public static final class LegacyCrapProvider implements Class2LayerFolder, InstanceProvider {

        private final List<FileObject> instanceFiles;
        private List<MarkProviderCreator> creators;
        private List<UpToDateStatusProviderFactory> factories;
        
        public LegacyCrapProvider() {
            this(null);
        }

        public LegacyCrapProvider(List<FileObject> files) {
            this.instanceFiles = files;
        }
        
        public Collection<? extends MarkProviderCreator> getMarkProviderCreators() {
            if (creators == null) {
                computeInstances();
            }
            return creators;
        }

        public Collection<? extends UpToDateStatusProviderFactory> getUpToDateStatusProviderFactories() {
            if (factories == null) {
                computeInstances();
            }
            return factories;
        }
        
        public Class getClazz(){
            return LegacyCrapProvider.class;
        }

        public String getLayerFolderName(){
            return UP_TO_DATE_STATUS_PROVIDER_FOLDER_NAME;
        }

        public InstanceProvider getInstanceProvider() {
            return new LegacyCrapProvider();
        }

        public Object createInstance(List fileObjectList) {
            ArrayList<FileObject> textBaseFilesList = new ArrayList<FileObject>();

            for(Object o : fileObjectList) {
                FileObject fileObject = null;

                if (o instanceof FileObject) {
                    fileObject = (FileObject) o;
                } else {
                    continue;
                }

                String fullPath = fileObject.getPath();
                int idx = fullPath.lastIndexOf(UP_TO_DATE_STATUS_PROVIDER_FOLDER_NAME);
                assert idx != -1 : "Expecting files with '" + UP_TO_DATE_STATUS_PROVIDER_FOLDER_NAME + "' in the path: " + fullPath; //NOI18N

                String path = fullPath.substring(0, idx);
                if (TEXT_BASE_PATH.equals(path)) {
                    textBaseFilesList.add(fileObject);
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.warning("The 'text/base' mime type is deprecated, please move your file to the root. Offending file: " + fullPath); //NOI18N
                    }
                }
            }

            return new LegacyCrapProvider(textBaseFilesList);
        }
        
        private void computeInstances() {
            ArrayList<MarkProviderCreator> newCreators = new ArrayList<MarkProviderCreator>();
            ArrayList<UpToDateStatusProviderFactory> newFactories = new ArrayList<UpToDateStatusProviderFactory>();
            
            for(FileObject f : instanceFiles) {
                if (!f.isValid() || !f.isData()) {
                    continue;
                }
                
                try {
                    DataObject d = DataObject.find(f);
                    InstanceCookie ic = d.getLookup().lookup(InstanceCookie.class);
                    if (ic != null) {
                        if (MarkProviderCreator.class.isAssignableFrom(ic.instanceClass())) {
                            MarkProviderCreator creator = (MarkProviderCreator) ic.instanceCreate();
                            newCreators.add(creator);
                        } else if (UpToDateStatusProviderFactory.class.isAssignableFrom(ic.instanceClass())) {
                            UpToDateStatusProviderFactory factory = (UpToDateStatusProviderFactory) ic.instanceCreate();
                            newFactories.add(factory);
                        }
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, null, e);
                }
            }
            
            this.creators = newCreators;
            this.factories = newFactories;
        }
    } // End of LegacyToolbarActionsProvider class
}
