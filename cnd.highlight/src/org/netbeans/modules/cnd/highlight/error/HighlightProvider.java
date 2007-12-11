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
package org.netbeans.modules.cnd.highlight.error;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.netbeans.editor.BaseDocument;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.cnd.highlight.CppHighlightsLayerFactory;
import org.openide.cookies.EditorCookie;
import org.openide.text.Annotation;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Alexander Simon
 */
public class HighlightProvider implements CsmModelListener, CsmProgressListener, PropertyChangeListener {
    
    public static final boolean TRACE_ANNOTATIONS = Boolean.getBoolean("cnd.highlight.trace.annotations"); // NOI18N
    
    private static final HighlightProvider instance = new HighlightProvider();
    
    private static final String threadName = "Error highlighting provider thread"; //NOI18N
    
    public static HighlightProvider getInstance(){
        return instance;
    }
    
    /** Creates a new instance of HighlightProvider */
    private HighlightProvider() {
        CsmModelAccessor.getModel().addModelListener(this);
        CsmModelAccessor.getModel().addProgressListener(this);
        TopComponent.getRegistry().addPropertyChangeListener(this);
    }
    
    public void startup() {
// we don't need this: projectOpened is empty!
// (iz #112280)	
//        for(Object o : CsmModelAccessor.getModel().projects()){
//            CsmProject project = (CsmProject)o;
//            projectOpened(project);
//        }
    }
    
    public void shutdown() {
        CsmModelAccessor.getModel().removeModelListener(this);
        TopComponent.getRegistry().removePropertyChangeListener(this);
        List<CsmFile> toDelete = new ArrayList<CsmFile>(annotations.keySet());
        for(CsmFile file : toDelete) {
            removeAnnotations(file);
        }
        BadgeProvider.getInstance().removeAllProjects();
    }
    
    public void close() {
    }
    
    public void projectOpened(CsmProject project) {
    }
    
    public void projectClosed(CsmProject project) {
        List<CsmFile> toDelete = new ArrayList<CsmFile>();
        for(Iterator it = annotations.keySet().iterator(); it.hasNext();){
            CsmFile file = (CsmFile)it.next();
            if (file.getProject() == project){
                toDelete.add(file);
            }
        }
        for( CsmFile file : toDelete ) {
            removeAnnotations(file);
        }
        BadgeProvider.getInstance().removeProject(project);
    }
    
    public void modelChanged(CsmChangeEvent e) {
        for(Iterator it = e.getRemovedFiles().iterator(); it.hasNext();){
            CsmFile file = (CsmFile)it.next();
            if (TRACE_ANNOTATIONS)  System.out.println("Removed file: "+file.getName()); // NOI18N
            removeAnnotations(file);
            BadgeProvider.getInstance().onFileRemoved(file);
        }
    }

    public void projectParsingStarted(CsmProject project) {
    }

    public void projectFilesCounted(CsmProject project, int filesCount) {
    }

    public void projectParsingFinished(CsmProject project) {
    }

    public void projectParsingCancelled(CsmProject project) {
    }

    public void fileInvalidated(CsmFile file) {
    }

    public void fileParsingStarted(CsmFile file) {
    }

    public void fileParsingFinished(CsmFile file) {
        checkFile(file, null);
        BadgeProvider.getInstance().invalidateFile(file);
    }

    public void projectLoaded(CsmProject project) {
	checkNodes();
	long time = 0;
	if( TRACE_ANNOTATIONS ) {
	    System.err.printf("HighlightProvider.projectLoaded - start checking files for %s\n", project.getName()); //NOI18N
	    time = System.currentTimeMillis();
	}
        BadgeProvider.getInstance().invalidateProject(project);
	if( TRACE_ANNOTATIONS ) {
	    time = System.currentTimeMillis() - time;
	    System.err.printf("HighlightProvider checking files for %s took %d ms\n", project.getName(), time); //NOI18N
	}
    }
    
    public void parserIdle() {
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
	Runnable r = null;
        if (TopComponent.Registry.PROP_CURRENT_NODES.equals(evt.getPropertyName())){
	    r = new Runnable() {
		public void run() {
		    checkNodes();
		}
	    };
        } else if (TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())){
	    r = new Runnable() {
		public void run() {
		    checkClosed();
		}
	    };
//        } else if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(evt.getPropertyName())){
//            checkClosed();
        }
	if( r != null ) {
	    CsmModelAccessor.getModel().enqueue(r, threadName);
	}
    }
    
    private void checkFile(CsmFile file, Document doc) {
        if (doc == null) {
            doc = findDocument(file);
        }
        if (doc instanceof BaseDocument){
            addAnnotations((BaseDocument)doc, file);
            // one more point to make a global scheduler
            // todo: optimize to check dependency between changed file and updated one
            CppHighlightsLayerFactory.getInactiveCodeHighlighter(doc).run();
        }
    }

    private Document findDocument(CsmFile searchFor) {
        DataObject dao = CsmUtilities.getDataObject(searchFor);
        if (dao == null) {
            return null;
        }
        EditorCookie editor = dao.getCookie(EditorCookie.class);
        return editor != null ? editor.getDocument() : null;
//        for (JTextComponent component : EditorRegistry.componentList()) {
//            if (component.isShowing()) {
//                Document doc = component.getDocument();
//                DataObject found = NbEditorUtilities.getDataObject(doc);
//                if (dao.equals(found)) {
//                    return doc;
//                }
//            }
//        }
//        return null;
    }
    
    private void checkClosed(){
        checkClosed(EditorRegistry.componentList());
    }

    private void checkClosed(List<? extends JTextComponent> panes) {
        if (annotations.size() == 0) {
            return;
        }
        Set<CsmFile> opened = new HashSet<CsmFile>();
        for (JTextComponent component : panes) {
            if (component.isShowing()) {
                Document doc = component.getDocument();
                CsmFile file = CsmUtilities.getCsmFile(doc, false);
                if (file != null && file.isParsed()) {
                    opened.add(file);
                }
            }
        }
        List<CsmFile> toDelete = new ArrayList<CsmFile>();
        for(Iterator<CsmFile> it = annotations.keySet().iterator(); it.hasNext();){
            CsmFile file = it.next();
            if (!opened.contains(file)){
                toDelete.add(file);
            }
        }
        for( Iterator<CsmFile> it = toDelete.iterator(); it.hasNext(); ) {
            CsmFile file = it.next();
            removeAnnotations(file);
        }
    }
    
    private void checkNodes(){
        checkNodes(EditorRegistry.componentList());
    }

    private void checkNodes(List<? extends JTextComponent> panes){
        for (JTextComponent component : panes) {
            if (component.isShowing()) {
                Document doc = component.getDocument();
                CsmFile file = CsmUtilities.getCsmFile(doc, false);
                if (file != null && file.isParsed()) {
                    //if (TRACE_ANNOTATIONS)  System.out.println("Activate node: "+file.getName()); // NOI18N
                    checkFile(file, doc);
                }
            }
        }
    }

    private boolean isNeededUpdateAnnotations(BaseDocument doc, CsmFile file) {
        if (doc == null || file == null) {
            return false;
        }
        List<Annotation> fileAnnotations = annotations.get(file);
        List<Integer> positions  = new ArrayList<Integer>();
        List<String> names  = new ArrayList<String>();
        if (fileAnnotations != null){
            for (Iterator<Annotation> it = fileAnnotations.iterator(); it.hasNext();){
                MyAnnotation annotation = (MyAnnotation)it.next();
                positions.add(new Integer(annotation.getOffset()));
                names.add(annotation.getName());
            }
        }
        int i = 0;
        for (Iterator<CsmInclude> it = file.getIncludes().iterator(); it.hasNext();){
            CsmInclude incl = it.next();
            if (incl.getIncludeFile() == null){
                int offset = incl.getStartPosition().getOffset();
                if (i < positions.size()){
                    if ((positions.get(i)).intValue() != offset) {
                        return true;
                    }
                    String name = getIncludeText(incl);
                    if (!name.equals(names.get(i))){
                        return true;
                    }
                } else {
                    return true;
                }
                i++;
            }
        }
        if (i < positions.size()){
            return true;
        }
        return false;
    }
    
    private void addAnnotations(BaseDocument doc, CsmFile file) {
        if (!isNeededUpdateAnnotations(doc, file)) {
            return;
        }
        List<Annotation> fileAnnotations = annotations.get(file);
        if (fileAnnotations != null){
            if (TRACE_ANNOTATIONS)  System.out.println("Update annotations: "+file.getName()); // NOI18N
            for (Iterator<Annotation> it = fileAnnotations.iterator(); it.hasNext();){
                MyAnnotation annotation = (MyAnnotation)it.next();
                NbDocument.removeAnnotation((StyledDocument)doc, annotation);
            }
        } else {
            if (TRACE_ANNOTATIONS)  System.out.println("Add annotations: "+file.getName()); // NOI18N
        }
        fileAnnotations = new ArrayList<Annotation>();
        for (Iterator<CsmInclude> it = file.getIncludes().iterator(); it.hasNext();){
            Annotation annotation =  createAnnotation(it.next(), doc);
            if (annotation != null){
                fileAnnotations.add(annotation);
            }
        }
        if (fileAnnotations.size()>0){
            annotations.put(file,fileAnnotations);
        } else {
            annotations.remove(file);
        }
    }
    
    private void removeAnnotations(CsmFile file) {
        BaseDocument doc = null;
        Document sdoc = findDocument(file);
        if (sdoc instanceof BaseDocument){
            doc = (BaseDocument)sdoc;
        }
        List<Annotation> fileAnnotations = annotations.get(file);
        if (fileAnnotations != null){
            if (TRACE_ANNOTATIONS)  System.out.println("Clear annotations: "+file.getName()); // NOI18N
            annotations.remove(file);
            if (doc != null) {
                for (Annotation annotation : fileAnnotations){
                    if (annotation != null) {
                        NbDocument.removeAnnotation((StyledDocument)doc, annotation);
                    }
                }
            }
        }
    }
    
    private Annotation createAnnotation(CsmInclude incl, BaseDocument doc){
        CsmFile file = incl.getIncludeFile();
        if (file == null){
            Position position = new MyPosition(incl);
            Annotation annotation = new MyAnnotation(position.getOffset(), getIncludeText(incl));
            NbDocument.addAnnotation((StyledDocument)doc, position, -1, annotation);
            return annotation;
        }
        return null;
    }
    
    private String getIncludeText(CsmInclude incl){
        if (incl.isSystem()){
            return "<"+incl.getIncludeName()+">"; // NOI18N
        }
        return "\""+incl.getIncludeName()+"\""; // NOI18N
    }
    
    public static class MyPosition implements Position {
        private int offset;
        public MyPosition(CsmInclude incl){
            offset = incl.getStartPosition().getOffset();
        }
        public int getOffset() {
            return offset;
        }
    }
    
    public static class MyAnnotation extends Annotation {
        private int offset;
        private String name;
        public MyAnnotation(int offset, String name){
            this.offset = offset;
            this.name = name;
        }
        public int getOffset() {
            return offset;
        }
        public String getName() {
            return name;
        }
        public String getAnnotationType() {
            return "org-netbeans-modules-cnd-cpp-parser_annotation_err"; // NOI18N
        }
        public String getShortDescription() {
            return NbBundle.getMessage(HighlightProvider.class, "CppParserAnnotationInclide", name);
        }
    }
    
    private Map<CsmFile,List<Annotation>> annotations = Collections.synchronizedMap(new HashMap<CsmFile,List<Annotation>>());
}
