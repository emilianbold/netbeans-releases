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

package org.netbeans.modules.java.editor.overridden;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;


/**
 *
 * @author Jan Lahoda
 */
public class IsOverriddenAnnotationHandler implements CancellableTask<CompilationInfo> {
    
    private static final boolean enableReverseLookups = Boolean.getBoolean("org.netbeans.java.editor.enableReverseLookups");
    static final Logger LOG = Logger.getLogger(IsOverriddenAnnotationHandler.class.getName());

    private FileObject file;
    
    public IsOverriddenAnnotationHandler(FileObject file) {
        this.file = file;
        Logger.getLogger("TIMER").log(Level.FINE, "IsOverriddenAnnotationHandler", new Object[] {file, this}); //NOI18N
    }
    
    private IsOverriddenVisitor visitor;
    
    public void run(CompilationInfo info) throws IOException {
        resume();
        
        StyledDocument doc = (StyledDocument) info.getDocument();
        
        if (doc == null) {
            LOG.log(Level.FINE, "Cannot get document!"); //NOI18N
            return ;
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            List<IsOverriddenAnnotation> annotations = process(info, doc);
            
            if (annotations == null) {
                //cancelled:
                return ;
            }
            
            newAnnotations(annotations);
        } finally {
            synchronized (this) {
                visitor = null;
            }
            
            Logger.getLogger("TIMER").log(Level.FINE, "Overridden in", //NOI18N
                    new Object[] {file, System.currentTimeMillis() - startTime});
        }
    }
    
    private FileObject findSourceRoot() {
        final ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
        if (cp != null) {
            for (FileObject root : cp.getRoots()) {
                if (FileUtil.isParentOf(root, file))
                    return root;
            }
        }
        //Null is a valid value for files which have no source path (default filesystem).
        return null;
    }
    
    //temporary hack:
    private synchronized Set<FileObject> findReverseSourceRoots(final FileObject thisSourceRoot, final FileObject thisFile) {
        final Object o = new Object();
        final Set<FileObject> reverseSourceRoots = new HashSet<FileObject>();
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                long startTime = System.currentTimeMillis();
                Set<FileObject> reverseSourceRootsInt = new HashSet<FileObject>(ReverseSourceRootsLookup.reverseSourceRootsLookup(thisSourceRoot));
                long endTime = System.currentTimeMillis();
                
                Logger.getLogger("TIMER").log(Level.FINE, "Find Reverse Source Roots", //NOI18N
                    new Object[] {thisFile, endTime - startTime});
                
                synchronized (o) {
                    reverseSourceRoots.addAll(reverseSourceRootsInt);
                }
                
                wakeUp();
            }
        });
        
        try {
            wait();
        } catch (InterruptedException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        return reverseSourceRoots;
    }
    
    public static AnnotationType detectOverrides(CompilationInfo info, TypeElement type, ExecutableElement ee, List<ElementDescription> result) {
        final Map<Name, List<ExecutableElement>> name2Method = new HashMap<Name, List<ExecutableElement>>();
        
        sortOutMethods(info, name2Method, type, false);
        
        List<ExecutableElement> lee = name2Method.get(ee.getSimpleName());
        
        if (lee == null || lee.isEmpty()) {
            return null;
        }
        
        Set<ExecutableElement> seenMethods = new HashSet<ExecutableElement>();
        
        for (ExecutableElement overridee : lee) {
            if (info.getElements().overrides(ee, overridee, SourceUtils.getEnclosingTypeElement(ee))) {
                if (seenMethods.add(overridee)) {
                    result.add(new ElementDescription(info, overridee));
                }
            }
        }
        
        if (!result.isEmpty()) {
            for (ElementDescription ed : result) {
                if (!ed.getModifiers().contains(Modifier.ABSTRACT)) {
                    return AnnotationType.OVERRIDES;
                }
            }
            
            return AnnotationType.IMPLEMENTS;
        }
        
        return null;
    }
    
    public List<IsOverriddenAnnotation> process(CompilationInfo info, final StyledDocument doc) {
        IsOverriddenVisitor v;
        
        synchronized (this) {
            if (isCanceled())
                return null;
            
            v = visitor = new IsOverriddenVisitor(doc, info);
        }
        
        CompilationUnitTree unit = info.getCompilationUnit();
        
        long startTime1 = System.currentTimeMillis();
        
        v.scan(unit, null);
        
        long endTime1 = System.currentTimeMillis();
        
        Logger.getLogger("TIMER").log(Level.FINE, "Overridden Scanner", //NOI18N
                    new Object[] {file, endTime1 - startTime1});
        
        Set<FileObject> reverseSourceRoots;
        
        if (enableReverseLookups) {
            FileObject thisSourceRoot = findSourceRoot();
            if (thisSourceRoot == null) {
                return null;
            }
            
            reverseSourceRoots = findReverseSourceRoots(thisSourceRoot, info.getFileObject());
            
            //XXX: special case "this" source root (no need to create a new JS and load the classes again for it):
            reverseSourceRoots.add(thisSourceRoot);
        } else {
            reverseSourceRoots = null;
        }
        
        LOG.log(Level.FINE, "reverseSourceRoots: {0}", reverseSourceRoots); //NOI18N
        
        List<IsOverriddenAnnotation> annotations = new ArrayList<IsOverriddenAnnotation>();
        
        for (ElementHandle<TypeElement> td : v.type2Declaration.keySet()) {
            if (isCanceled())
                return null;
            
            LOG.log(Level.FINE, "type: {0}", td.getQualifiedName()); //NOI18N
            
            final Map<Name, List<ExecutableElement>> name2Method = new HashMap<Name, List<ExecutableElement>>();
            
            TypeElement resolvedType = td.resolve(info);
            
            if (resolvedType == null)
                continue;
            
            sortOutMethods(info, name2Method, resolvedType, false);
            
            for (ElementHandle<ExecutableElement> methodHandle : v.type2Declaration.get(td)) {
                if (isCanceled())
                    return null;
                
                ExecutableElement ee = methodHandle.resolve(info);
                
                if (ee == null)
                    continue;
                
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "method: {0}", ee.toString()); //NOI18N
                }
                
                List<ExecutableElement> lee = name2Method.get(ee.getSimpleName());
                
                if (lee == null || lee.isEmpty()) {
                    continue;
                }
                
                Set<ExecutableElement> seenMethods = new HashSet<ExecutableElement>();
                List<ElementDescription> overrides = new ArrayList<ElementDescription>();
                
                for (ExecutableElement overridee : lee) {
                    if (info.getElements().overrides(ee, overridee, SourceUtils.getEnclosingTypeElement(ee))) {
                        if (seenMethods.add(overridee)) {
                            overrides.add(new ElementDescription(info, overridee));
                        }
                    }
                }
                
                if (!overrides.isEmpty()) {
                    int position = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), v.declaration2Tree.get(methodHandle));
                    Position pos = getPosition(doc, position);
                    
                    if (pos == null) {
                        //cannot compute the position, skip
                        continue;
                    }
                    
                    StringBuffer tooltip = new StringBuffer();
                    boolean wasOverrides = false;
                    
                    boolean newline = false;
                    
                    for (ElementDescription ed : overrides) {
                        if (newline) {
                            tooltip.append("\n"); //NOI18N
                        }
                        
                        newline = true;
                        
                        if (ed.getModifiers().contains(Modifier.ABSTRACT)) {
                            tooltip.append(NbBundle.getMessage(IsOverriddenAnnotationHandler.class, "TP_Implements", ed.getDisplayName()));
                        } else {
                            tooltip.append(NbBundle.getMessage(IsOverriddenAnnotationHandler.class, "TP_Overrides", ed.getDisplayName()));
                            wasOverrides = true;
                        }
                    }
                    
                    annotations.add(new IsOverriddenAnnotation(doc, pos, wasOverrides ? AnnotationType.OVERRIDES : AnnotationType.IMPLEMENTS, tooltip.toString(), overrides));
                }
            }
            
            if (enableReverseLookups) {
                String typeOverridden = null;
                AnnotationType typeType = null;
                TypeElement resolved = td.resolve(info);
                
                
                if (resolved == null) {
                    Logger.getLogger("global").log(Level.SEVERE, "IsOverriddenAnnotationHandler: resolved == null!"); //NOI18N
                    continue;
                }
                
                if (resolved.getKind().isInterface()) {
                    typeOverridden = NbBundle.getMessage(IsOverriddenAnnotationHandler.class, "CAP_HasImplementations");
                    typeType = AnnotationType.HAS_IMPLEMENTATION;
                }
                
                if (resolved.getKind().isClass()) {
                    typeOverridden = NbBundle.getMessage(IsOverriddenAnnotationHandler.class, "CAP_IsOverridden");
                    typeType = AnnotationType.IS_OVERRIDDEN;
                }
                
                final Map<ElementHandle<ExecutableElement>, List<ElementDescription>> overriding = new HashMap<ElementHandle<ExecutableElement>, List<ElementDescription>>();
                final List<ElementDescription> overridingClasses = new ArrayList<ElementDescription>();
                
                long startTime = System.currentTimeMillis();
                long[] classIndexTime = new long[1];
                final Map<FileObject, Set<ElementHandle<TypeElement>>> users = computeUsers(reverseSourceRoots, ElementHandle.create(resolved), classIndexTime);
                long endTime = System.currentTimeMillis();
                
                if (users == null) {
                    return null;
                }
                
                Logger.getLogger("TIMER").log(Level.FINE, "Overridden Users Class Index", //NOI18N
                    new Object[] {file, classIndexTime[0]});
                Logger.getLogger("TIMER").log(Level.FINE, "Overridden Users", //NOI18N
                    new Object[] {file, endTime - startTime});
                
                for (Map.Entry<FileObject, Set<ElementHandle<TypeElement>>> data : users.entrySet()) {
                    if (isCanceled())
                        return null;
                    
                    findOverriddenAnnotations(data.getKey(), data.getValue(), td, v.type2Declaration.get(td), overriding, overridingClasses);
                }
                
                if (!overridingClasses.isEmpty()) {
                    Tree t = v.declaration2Class.get(td);
                    
                    if (t != null) {
                        Position pos = getPosition(doc, (int) info.getTrees().getSourcePositions().getStartPosition(unit, t));
                        
                        if (pos == null) {
                            //cannot compute the position, skip
                            continue;
                        }
                        
                        annotations.add(new IsOverriddenAnnotation(doc, pos, typeType, typeOverridden.toString(), overridingClasses));
                    }
                }
                
                for (ElementHandle<ExecutableElement> original : overriding.keySet()) {
                    if (isCanceled())
                        return null;
                    
                    Position pos = getPosition(doc, (int) info.getTrees().getSourcePositions().getStartPosition(unit, v.declaration2Tree.get(original)));
                    
                    if (pos == null) {
                        //cannot compute the position, skip
                        continue;
                    }
                    
                    Set<Modifier> mods = original.resolve(info).getModifiers();
                    String tooltip = null;
                    
                    if (mods.contains(Modifier.ABSTRACT)) {
                        tooltip = NbBundle.getMessage(IsOverriddenAnnotationHandler.class, "TP_HasImplementations");
                    } else {
                        tooltip = NbBundle.getMessage(IsOverriddenAnnotationHandler.class, "TP_IsOverridden");
                    }
                    
                    IsOverriddenAnnotation ann = new IsOverriddenAnnotation(doc, pos, mods.contains(Modifier.ABSTRACT) ? AnnotationType.HAS_IMPLEMENTATION : AnnotationType.IS_OVERRIDDEN, tooltip, overriding.get(original));
                    
                    annotations.add(ann);
                }
            }
        }
        
        if (isCanceled())
            return null;
        else
            return annotations;
    }
    
    private static final ClassPath EMPTY = ClassPathSupport.createClassPath(new URL[0]);
    
    private Set<ElementHandle<TypeElement>> computeUsers(FileObject source, Set<ElementHandle<TypeElement>> base, long[] classIndexCumulative) {
        ClasspathInfo cpinfo = ClasspathInfo.create(/*source);/*/EMPTY, EMPTY, ClassPathSupport.createClassPath(new FileObject[] {source}));
        
        long startTime = System.currentTimeMillis();
        
        try {
            List<ElementHandle<TypeElement>> l = new LinkedList<ElementHandle<TypeElement>>(base);
            Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();
            
            while (!l.isEmpty()) {
                ElementHandle<TypeElement> eh = l.remove(0);
                
                result.add(eh);
                Set<ElementHandle<TypeElement>> typeElements = cpinfo.getClassIndex().getElements(eh, Collections.singleton(SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE));
                //XXX: Canceling
                if (typeElements != null) {
                    l.addAll(typeElements);
                }
            }
            return result;
        } finally {
            classIndexCumulative[0] += (System.currentTimeMillis() - startTime);
        }
    }
    
    private Map<FileObject, Set<ElementHandle<TypeElement>>> computeUsers(Set<FileObject> sources, ElementHandle<TypeElement> base, long[] classIndexCumulative) {
        Map<FileObject, Collection<FileObject>> edges = new HashMap<FileObject, Collection<FileObject>>();
        Map<FileObject, Collection<FileObject>> dependsOn = new HashMap<FileObject, Collection<FileObject>>();
        
        for (FileObject source : sources) {
            edges.put(source, new ArrayList<FileObject>());
        }
        
        for (FileObject source : sources) {
            List<FileObject> deps = new ArrayList<FileObject>();
            
            dependsOn.put(source, deps);
            
            for (Entry entry : ClassPath.getClassPath(source, ClassPath.COMPILE).entries()) { //TODO: should also check BOOT?
                for (FileObject s : SourceForBinaryQuery.findSourceRoots(entry.getURL()).getRoots()) {
                    Collection<FileObject> targets = edges.get(s);
                    
                    if (targets != null) {
                        targets.add(source);
                    }
                    
                    deps.add(s);
                }
            }
        }
        
        List<FileObject> sourceRoots = new ArrayList<FileObject>(sources);
        
        try {
            Utilities.topologicalSort(sourceRoots, edges);
        } catch (TopologicalSortException ex) {
            LOG.log(Level.WARNING, "internal error", ex); //NOI18N
            return null;
        }
        
        Map<FileObject, Set<ElementHandle<TypeElement>>> result = new HashMap<FileObject, Set<ElementHandle<TypeElement>>>();
        
        for (FileObject file : sourceRoots) {
            Set<ElementHandle<TypeElement>> baseTypes = new HashSet<ElementHandle<TypeElement>>();
            
            baseTypes.add(base);
            
            for (FileObject dep : dependsOn.get(file)) {
                Set<ElementHandle<TypeElement>> depTypes = result.get(dep);
                
                if (depTypes != null) {
                    baseTypes.addAll(depTypes);
                }
            }
            
            Set<ElementHandle<TypeElement>> types = computeUsers(file, baseTypes, classIndexCumulative);
            
            types.removeAll(baseTypes);
            
            result.put(file, types);
        }
        
        return result;
    }
    private void findOverriddenAnnotations(
            FileObject sourceRoot,
            final Set<ElementHandle<TypeElement>> users,
            final ElementHandle<TypeElement> originalType,
            final List<ElementHandle<ExecutableElement>> methods,
            final Map<ElementHandle<ExecutableElement>, List<ElementDescription>> overriding,
            final List<ElementDescription> overridingClasses) {
        ClasspathInfo cpinfo = ClasspathInfo.create(sourceRoot);
        
        if (!users.isEmpty()) {
            JavaSource js = JavaSource.create(cpinfo);
            
            try {
                js.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController controller) throws Exception {
                        Set<Element> seenElements = new HashSet<Element>();
                        
                        for (ElementHandle<TypeElement> typeHandle : users) {
                            if (isCanceled())
                                return;
                            TypeElement type = typeHandle.resolve(controller);
                            Element resolvedOriginalType = originalType.resolve(controller);
                            
                            if (!seenElements.add(resolvedOriginalType))
                                continue;
                            
                            if (controller.getTypes().isSubtype(type.asType(), resolvedOriginalType.asType())) {
                                overridingClasses.add(new ElementDescription(controller, type));
                                
                                for (ElementHandle<ExecutableElement> originalMethodHandle : methods) {
                                    ExecutableElement originalMethod = originalMethodHandle.resolve(controller);
                                    
                                    if (originalMethod != null) {
                                        ExecutableElement overrider = getImplementationOf(controller, originalMethod, type);
                                        
                                        if (overrider == null)
                                            continue;
                                        
                                        List<ElementDescription> overriddingMethods = overriding.get(originalMethodHandle);
                                        
                                        if (overriddingMethods == null) {
                                            overriding.put(originalMethodHandle, overriddingMethods = new ArrayList<ElementDescription>());
                                        }
                                        
                                        overriddingMethods.add(new ElementDescription(controller, overrider));
                                    } else {
                                        Logger.getLogger("global").log(Level.SEVERE, "IsOverriddenAnnotationHandler: originalMethod == null!"); //NOI18N
                                    }
                                }
                            }
                        }
                    }
                },true);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    private ExecutableElement getImplementationOf(CompilationInfo info, ExecutableElement overridee, TypeElement implementor) {
        for (ExecutableElement overrider : ElementFilter.methodsIn(implementor.getEnclosedElements())) {
            if (info.getElements().overrides(overrider, overridee, implementor)) {
                return overrider;
            }
        }
        
        return null;
    }
            
    private boolean canceled;
    
    public synchronized void cancel() {
        canceled = true;
        
        if (visitor != null) {
            visitor.cancel();
        }
        
        wakeUp();
    }
    
    private synchronized void resume() {
        canceled = false;
    }
    
    private synchronized void wakeUp() {
        notifyAll();
    }
    
    private synchronized boolean isCanceled() {
        return canceled;
    }
    
    private void newAnnotations(List<IsOverriddenAnnotation> as) {
        AnnotationsHolder a = AnnotationsHolder.get(file);
        
        if (a != null) {
            a.setNewAnnotations(as);
        }
    }

    private static void sortOutMethods(CompilationInfo info, Map<Name, List<ExecutableElement>> where, Element td, boolean current) {
        if (current) {
            Map<Name, List<ExecutableElement>> newlyAdded = new HashMap<Name, List<ExecutableElement>>();
            
            OUTTER: for (ExecutableElement ee : ElementFilter.methodsIn(td.getEnclosedElements())) {
                Name name = ee.getSimpleName();
                List<ExecutableElement> alreadySeen = where.get(name);
                
                if (alreadySeen != null) {
                    for (ExecutableElement seen : alreadySeen) {
                        if (info.getElements().overrides(seen, ee, (TypeElement) seen.getEnclosingElement())) {
                            continue OUTTER; //a method that overrides this one was already handled, ignore
                        }
                    }
                }
                
                List<ExecutableElement> lee = newlyAdded.get(name);
                
                if (lee == null) {
                    newlyAdded.put(name, lee = new ArrayList<ExecutableElement>());
                }
                
                lee.add(ee);
            }
            
            for (Map.Entry<Name, List<ExecutableElement>> e : newlyAdded.entrySet()) {
                List<ExecutableElement> lee = where.get(e.getKey());
                
                if (lee == null) {
                    where.put(e.getKey(), e.getValue());
                } else {
                    lee.addAll(e.getValue());
                }
            }
        }
        
        for (TypeMirror superType : info.getTypes().directSupertypes(td.asType())) {
            if (superType.getKind() == TypeKind.DECLARED) {
                sortOutMethods(info, where, ((DeclaredType) superType).asElement(), true);
            }
        }
    }
    
    private static Position getPosition(final StyledDocument doc, final int offset) {
        class Impl implements Runnable {
            private Position pos;
            public void run() {
                if (offset < 0 || offset >= doc.getLength())
                    return ;
                
                try {
                    pos = doc.createPosition(offset - NbDocument.findLineColumn(doc, offset));
                } catch (BadLocationException ex) {
                    //should not happen?
                    LOG.log(Level.FINE, null, ex);
                }
            }
        }
        
        Impl i = new Impl();
        
        doc.render(i);
        
        return i.pos;
    }
    
}
