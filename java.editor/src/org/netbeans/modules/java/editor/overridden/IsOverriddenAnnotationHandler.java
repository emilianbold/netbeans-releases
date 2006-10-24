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

package org.netbeans.modules.java.editor.overridden;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.timers.TimesCollector;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Lahoda
 */
public class IsOverriddenAnnotationHandler implements CancellableTask<CompilationInfo> {

    private FileObject file;
    
    final List<IsOverriddenAnnotation> annotations;
    
    static final Map<FileObject, Reference<IsOverriddenAnnotationHandler>> file2Annotations = new WeakHashMap<FileObject, Reference<IsOverriddenAnnotationHandler>>();
    
    static IsOverriddenAnnotationHandler getHandler(FileObject file) {
        Reference<IsOverriddenAnnotationHandler> ref = file2Annotations.get(file);
        IsOverriddenAnnotationHandler handler = ref != null ? ref.get() : null;
        
        if (handler == null) {
            file2Annotations.put(file, new CleaneableReference(handler = new IsOverriddenAnnotationHandler(file), handler.annotations));
        }
        
        return handler;
    }
    
    /** Creates a new instance of SemanticHighlighter */
    private IsOverriddenAnnotationHandler(FileObject file) {
        this.file = file;
        this.annotations = new ArrayList<IsOverriddenAnnotation>();
    }
    
    private static class CleaneableReference extends WeakReference implements Runnable {
        
        List<IsOverriddenAnnotation> toClear;
        
        public CleaneableReference(Object ref, List<IsOverriddenAnnotation> toClear) {
            super(ref, org.openide.util.Utilities.activeReferenceQueue());
            this.toClear = toClear;
        }
        
        public void run() {
            for (IsOverriddenAnnotation a : toClear) {
                a.detach();
            }
            
            this.toClear = null;
        }
        
    }
    
    public Document getDocument() {
        try {
            DataObject d = DataObject.find(file);
            EditorCookie ec = (EditorCookie) d.getCookie(EditorCookie.class);
            
            if (ec == null)
                return null;
            
            return ec.getDocument();
        } catch (IOException e) {
            Logger.global.log(Level.INFO, "SemanticHighlighter: Cannot find DataObject for file: " + FileUtil.getFileDisplayName(file), e);
            return null;
        }
    }
    
    private IsOverriddenVisitor visitor;
    
    public void run(CompilationInfo info) {
        resume();
        
        Document doc = getDocument();
        
        if (doc == null) {
            Logger.global.log(Level.INFO, "SemanticHighlighter: Cannot get document!");
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
            
            TimesCollector.getDefault().reportTime(file, "is-overridden", "Overridden in", System.currentTimeMillis() - startTime);
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
                Set<FileObject> reverseSourceRootsInt = new HashSet(ReverseSourceRootsLookup.reverseSourceRootsLookup(thisSourceRoot));
                long endTime = System.currentTimeMillis();
                
                TimesCollector.getDefault().reportTime(thisFile, "findReverseSourceRoots", "Find Reverse Source Roots", endTime - startTime);
                
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
    
    List<IsOverriddenAnnotation> process(CompilationInfo info, Document doc) {
        IsOverriddenVisitor v;
        
        synchronized (this) {
            if (isCanceled())
                return null;
            
            v = visitor = new IsOverriddenVisitor(doc, info);
        }
        
        CompilationUnitTree unit = info.getCompilationUnit();
        
        v.scan(unit, null);
        
        ClassIndex uq = info.getJavaSource().getClasspathInfo().getClassIndex();
        FileObject thisSourceRoot = findSourceRoot();
        if (thisSourceRoot == null) {
            return null;
        }
        Set<FileObject> reverseSourceRoots = findReverseSourceRoots(thisSourceRoot, info.getFileObject());
        
        //XXX: special case "this" source root (no need to create a new JS and load the classes again for it):
        reverseSourceRoots.add(thisSourceRoot);
        
        List<IsOverriddenAnnotation> annotations = new ArrayList<IsOverriddenAnnotation>();
        
        for (ElementHandle<TypeElement> td : v.type2Declaration.keySet()) {
            if (isCanceled())
                return null;
            
            String typeOverridden = null;
            AnnotationType typeType = null;
            List<TypeElement> typeImplementors = new ArrayList<TypeElement>();
            TypeElement resolved = td.resolve(info);
            
            
            if (resolved == null) {
                Logger.getLogger("global").log(Level.SEVERE, "IsOverriddenAnnotationHandler: resolved == null!");
                continue;
            }
            
            if (resolved.getKind().isInterface()) {
                typeOverridden = "Has Implementations";
                typeType = AnnotationType.HAS_IMPLEMENTATION;
            }
            
            if (resolved.getKind().isClass()) {
                typeOverridden = "Is Overridden:";
                typeType = AnnotationType.IS_OVERRIDDEN;
            }
            
            int position = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), v.declaration2Tree.get(resolved));
            final Map<ElementHandle<ExecutableElement>, Integer> methodsPositions = new HashMap<ElementHandle<ExecutableElement>, Integer>();
            
            for (ElementHandle<ExecutableElement> methodHandle : v.declaration2Tree.keySet()) {
                if (isCanceled())
                    return null;
                
                Tree t = v.declaration2Tree.get(methodHandle);
                
                methodsPositions.put(methodHandle, (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t));
                
                //overrides:
                ExecutableElement ee = methodHandle.resolve(info);
                
                if (ee == null)
                    continue;
                
                IsOverriddenAnnotation ann = checkDefines(file, doc, info, (TypeElement) ee.getEnclosingElement(), methodHandle, v, false);
                
                if (ann != null) {
                    annotations.add(ann);
                }
            }
            
            final Map<ElementHandle<ExecutableElement>, List<ElementDescription>> overriding = new HashMap<ElementHandle<ExecutableElement>, List<ElementDescription>>();
            final List<ElementDescription> overridingClasses = new ArrayList<ElementDescription>();
            
            for (FileObject sourceRoot : reverseSourceRoots) {
                if (isCanceled())
                    return null;
                
                findOverriddenAnnotations(sourceRoot, resolved, td, v.type2Declaration.get(td), overriding, overridingClasses);
            }
            
            if (!overridingClasses.isEmpty()) {
                Tree t = v.declaration2Class.get(td);
                
                if (t != null) {
                    Line classLine = getLine(doc, (int) info.getTrees().getSourcePositions().getStartPosition(unit, t));
                    
                    annotations.add(new IsOverriddenAnnotation(file, typeType, classLine, typeOverridden.toString(), overridingClasses));
                }
            }
            
            for (ElementHandle<ExecutableElement> original : overriding.keySet()) {
                if (isCanceled())
                    return null;
                
                Line l = getLine(doc, methodsPositions.get(original));
                Set<Modifier> mods = original.resolve(info).getModifiers();
                String tooltip = null;
                
                if (mods.contains(Modifier.ABSTRACT)) {
                    tooltip = "Has Implementations";
                } else {
                    tooltip = "Is Overridden";
                }
                
                IsOverriddenAnnotation ann = new IsOverriddenAnnotation(file, mods.contains(Modifier.ABSTRACT) ? AnnotationType.HAS_IMPLEMENTATION : AnnotationType.IS_OVERRIDDEN, l, tooltip, overriding.get(original));
                
                annotations.add(ann);
            }
            
        }
        
        if (isCanceled())
            return null;
        else
            return annotations;
    }
    
    private void findOverriddenAnnotations(
            FileObject sourceRoot,
            TypeElement resolvedElement,
            final ElementHandle<TypeElement> originalType,
            final List<ElementHandle<ExecutableElement>> methods,
            final Map<ElementHandle<ExecutableElement>, List<ElementDescription>> overriding,
            final List<ElementDescription> overridingClasses) {
        ClasspathInfo cpinfo = ClasspathInfo.create(sourceRoot);
        
        //XXX:IMPLEMENTORS_RECURSIVE removed
        final Set<ElementHandle<TypeElement>> users = new HashSet(cpinfo.getClassIndex().getElements(ElementHandle.create(resolvedElement), Collections.singleton(SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        
        if (!users.isEmpty()) {
            JavaSource js = JavaSource.create(cpinfo);
            
            try {
                js.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {
                        cancel();
                    }
                    public void run(CompilationController controller) throws Exception {
                        for (ElementHandle<TypeElement> typeHandle : users) {
                            if (isCanceled())
                                return;
                            TypeElement type = typeHandle.resolve(controller);
                            Element resolvedOriginalType = originalType.resolve(controller);
                            if (controller.getTypes().isSubtype(type.asType(), resolvedOriginalType.asType())) {
                                overridingClasses.add(new ElementDescription(controller, type));
                                
                                for (ElementHandle<ExecutableElement> originalMethodHandle : methods) {
                                    ExecutableElement originalMethod = originalMethodHandle.resolve(controller);
                                    
                                    if (originalMethod != null) {
                                        ExecutableElement overrider = (ExecutableElement) /*SourceUtils.*/getImplementationOf(controller, originalMethod, (TypeElement) type);
                                        
                                        if (overrider == null)
                                            continue;
                                        
                                        List<ElementDescription> overriddingMethods = overriding.get(originalMethodHandle);
                                        
                                        if (overriddingMethods == null) {
                                            overriding.put(originalMethodHandle, overriddingMethods = new ArrayList<ElementDescription>());
                                        }
                                        
                                        overriddingMethods.add(new ElementDescription(controller, overrider));
                                    } else {
                                        Logger.getLogger("global").log(Level.SEVERE, "IsOverriddenAnnotationHandler: originalMethod == null!");
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
        if (annotations != null) {
            for (IsOverriddenAnnotation a : annotations) {
                a.detach();
            }
        }
        
        annotations.clear();
        annotations.addAll(as);
        
        for (IsOverriddenAnnotation a : annotations) {
            a.attach();
        }
    }

    private IsOverriddenAnnotation checkDefines(FileObject context, Document doc, CompilationInfo info, TypeElement td, ElementHandle<ExecutableElement> overrider, IsOverriddenVisitor v, boolean useTD) {
        ExecutableElement overriderResolved = overrider.resolve(info);
        
        if (useTD) {
            for (ExecutableElement overridee : ElementFilter.methodsIn(td.getEnclosedElements())) {
                if (info.getElements().overrides(overriderResolved, overridee, SourceUtils.getEnclosingTypeElement(overriderResolved))) {
                    Line l = getLine(doc, (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), v.declaration2Tree.get(overrider)));
                    StringBuffer tooltip = new StringBuffer();
                    
                    if (td.getKind().isInterface()) {
                        tooltip.append("Implements: " + overridee);
                    } else {
                        tooltip.append("Overrides: " + overridee);
                    }
                    
                    tooltip.append(" in ");
                    tooltip.append(td);
                    
                    return new IsOverriddenAnnotation(context, td.getKind().isInterface() ? AnnotationType.IMPLEMENTS : AnnotationType.OVERRIDES, l, tooltip.toString(), Collections.singletonList(new ElementDescription(info, overridee)));
                }
            }
        }
        
        IsOverriddenAnnotation ann = null;
        
        if (td.getKind().isClass()) {
            TypeMirror superClass = td.getSuperclass();
            
            if (superClass.getKind() == TypeKind.DECLARED) {
                ann = checkDefines(context, doc, info, (TypeElement) ((DeclaredType)superClass).asElement(), overrider, v, true);
            }
        }
        
        if (ann == null) {
            for (TypeMirror type : td.getInterfaces()) {
                ann = checkDefines(context, doc, info, (TypeElement) ((DeclaredType)type).asElement(), overrider, v, true);
                
                if (ann != null)
                    break;
            }
        }
        
        return ann;
    }
    
    private static Line getLine(Document doc, int offset) {
        StyledDocument sdoc = (StyledDocument) doc;
        DataObject dObj = (DataObject)doc.getProperty(doc.StreamDescriptionProperty );
        LineCookie lc = (LineCookie) dObj.getCookie(LineCookie.class);
        int lineNumber = NbDocument.findLineNumber(sdoc, offset);
        Line line = lc.getLineSet().getCurrent(lineNumber);
        
        return line;
    }
    
}
