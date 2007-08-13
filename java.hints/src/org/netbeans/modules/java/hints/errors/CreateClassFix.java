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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan lahoda
 */
public abstract class CreateClassFix implements Fix {
    
    protected Set<Modifier> modifiers;
    private List<TypeMirrorHandle> argumentTypes; //if a specific constructor should be created
    private List<String> argumentNames; //dtto.
    private List<TypeMirrorHandle> superTypes;
    protected ElementKind kind;
    private int numTypeParameters;
    
    public CreateClassFix(CompilationInfo info, Set<Modifier> modifiers, List<? extends TypeMirror> argumentTypes, List<String> argumentNames, TypeMirror superType, ElementKind kind, int numTypeParameters) {
        this.modifiers = modifiers;
        
        if (argumentTypes != null && argumentNames != null) {
            this.argumentTypes = new ArrayList<TypeMirrorHandle>();
            
            for (TypeMirror tm : argumentTypes) {
                this.argumentTypes.add(TypeMirrorHandle.create(tm));
            }
            
            this.argumentNames = argumentNames;
        }
        
        if (superType != null) {
            superTypes = new LinkedList<TypeMirrorHandle>();
            
            if (superType.getKind() == TypeKind.DECLARED && "".equals(info.getElementUtilities().getBinaryName((TypeElement) ((DeclaredType) superType).asElement()))) {
                for (TypeMirror tm : info.getTypes().directSupertypes(superType)) {
                    superTypes.add(TypeMirrorHandle.create(tm));
                }
            } else {
                superTypes.add(TypeMirrorHandle.create(superType));
            }
        }
        
        this.kind = kind;
        this.numTypeParameters = numTypeParameters;
    }
    
    protected ClassTree createConstructor(WorkingCopy working, TreePath targetTreePath) {
        TreeMaker make = working.getTreeMaker();
        ClassTree targetTree = (ClassTree)targetTreePath.getLeaf();
        boolean removeDefaultConstructor = (kind == ElementKind.INTERFACE) || (kind == ElementKind.ANNOTATION_TYPE);
        
        if (argumentNames != null) {
            List<VariableTree>         argTypes = new ArrayList<VariableTree>();
            Iterator<TypeMirrorHandle> typeIt   = argumentTypes.iterator();
            Iterator<String>           nameIt   = argumentNames.iterator();
            
            while (typeIt.hasNext() && nameIt.hasNext()) {
                TypeMirrorHandle tmh = typeIt.next();
                String           argName = nameIt.next();
                
                argTypes.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), argName, make.Type(tmh.resolve(working)), null));
            }
            
            MethodTree constr = make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC/*!!!*/)), "<init>", null, Collections.<TypeParameterTree>emptyList(), argTypes, Collections.<ExpressionTree>emptyList(), "{}" /*XXX*/, null); // NOI18N
            
            targetTree = GeneratorUtils.insertClassMember(working, targetTreePath, constr);
            
            removeDefaultConstructor = true;
        }
        
        if (removeDefaultConstructor) {
            //remove the original constructor:
            for (Tree t : targetTree.getMembers()) {
                if (t.getKind() == Kind.METHOD) {
                    MethodTree mt = (MethodTree) t;
                    
                    if ("<init>".equals(mt.getName().toString()) && mt.getParameters().size() == 0) { // NOI18N
                        targetTree = make.removeClassMember(targetTree, mt);
                        break;
                    }
                }
            }
        }
            
        Tree extendsClause = null;
        List<Tree> implementsClause = Collections.<Tree>emptyList();
        
        if (superTypes != null) {
            DeclaredType extendsType = null;
            List<DeclaredType> implementsTypes = new LinkedList<DeclaredType>();
            
            for (TypeMirrorHandle h : superTypes) {
                TypeMirror tm = h.resolve(working);
                
                if (tm == null) {
                    //XXX: log
                    continue;
                }
                
                if (tm.getKind() != TypeKind.DECLARED) {
                    //XXX: log
                    continue;
                }
                
                DeclaredType dt = (DeclaredType) tm;
                
                if (dt.asElement().getKind().isClass()) {
                    if (extendsType != null) {
                        //XXX: log
                    }
                    
                    extendsType = dt;
                } else {
                    implementsTypes.add(dt);
                }
            }
            
            if (extendsType != null && !"java.lang.Object".equals(((TypeElement) extendsType.asElement()).getQualifiedName().toString())) { // NOI18N
                extendsClause = make.Type(extendsType);
            }
            
            if (!implementsTypes.isEmpty()) {
                implementsClause = new LinkedList<Tree>();
                
                for (DeclaredType dt : implementsTypes) {
                    implementsClause.add(make.Type(dt));
                }
            }
        }
        
        ModifiersTree nueModifiers = make.Modifiers(modifiers);
        List<TypeParameterTree> typeParameters = new LinkedList<TypeParameterTree>();
        
        for (int cntr = 0; cntr < numTypeParameters; cntr++) {
            typeParameters.add(make.TypeParameter(numTypeParameters == 1 ? "T" : "T" + cntr, Collections.<ExpressionTree>emptyList())); // NOI18N
        }
        
        switch (kind) {
            case CLASS:
                return make.Class(nueModifiers, targetTree.getSimpleName(), typeParameters, extendsClause, implementsClause, targetTree.getMembers());
            case INTERFACE:
                return make.Interface(nueModifiers, targetTree.getSimpleName(), typeParameters, implementsClause, targetTree.getMembers());
            case ANNOTATION_TYPE:
                return make.AnnotationType(nueModifiers, targetTree.getSimpleName(), targetTree.getMembers());
            case ENUM:
                return make.Enum(nueModifiers, targetTree.getSimpleName(), implementsClause, targetTree.getMembers());
            default:
                assert false : kind;
                return null;
        }
    }
    
    private static int valueForBundle(ElementKind kind) {
        switch (kind) {
        case CLASS:
            return 0;
        case INTERFACE:
            return 1;
        case ENUM:
            return 2;
        case ANNOTATION_TYPE:
            return 3;
        default:
            assert false : kind;
            return 0;
        }
    }
    
    public abstract String toDebugString(CompilationInfo info);
    
    static final class CreateOuterClassFix extends CreateClassFix {
        private FileObject targetSourceRoot;
        private String packageName;
        private String simpleName;
        
        public CreateOuterClassFix(CompilationInfo info, FileObject targetSourceRoot, String packageName, String simpleName, Set<Modifier> modifiers, List<? extends TypeMirror> argumentTypes, List<String> argumentNames, TypeMirror superType, ElementKind kind, int numTypeParameters) {
            super(info, modifiers, argumentTypes, argumentNames, superType, kind, numTypeParameters);
            
            this.targetSourceRoot = targetSourceRoot;
            this.packageName = packageName;
            this.simpleName = simpleName;
        }

        public String getText() {
            return NbBundle.getMessage(CreateClassFix.class, "FIX_CreateClassInPackage", simpleName, packageName, valueForBundle(kind));
        }
        
        private static String template(ElementKind kind) {
            switch (kind) {
                case CLASS: return "Templates/Classes/Class.java"; // NOI18N
                case INTERFACE: return "Templates/Classes/Interface.java"; // NOI18N
                case ANNOTATION_TYPE: return "Templates/Classes/AnnotationType.java"; // NOI18N
                case ENUM: return "Templates/Classes/Enum.java"; // NOI18N
                default: throw new IllegalStateException();
            }
        }

        public ChangeInfo implement() throws IOException {
            FileObject pack = FileUtil.createFolder(targetSourceRoot, packageName.replace('.', '/')); // NOI18N
            FileObject classTemplate/*???*/ = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject(template(kind));
            DataObject classTemplateDO = DataObject.find(classTemplate);
            DataObject od = classTemplateDO.createFromTemplate(DataFolder.findFolder(pack), simpleName);
            FileObject target = od.getPrimaryFile();
            
            JavaSource.forFileObject(target).runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);
                    
                    ClassTree source = (ClassTree) parameter.getCompilationUnit().getTypeDecls().get(0);
                    ClassTree nue = createConstructor(parameter, TreePath.getPath(parameter.getCompilationUnit(), source));
                    
                    parameter.rewrite(source, nue);
                }
            }).commit();
            
            return new ChangeInfo(target, null, null);
        }
        
        public String toDebugString(CompilationInfo info) {
            return "CreateClass:" + packageName + "." + simpleName + ":" + modifiers.toString() + ":" + kind; // NOI18N
        }
        
    }
    
    static final class CreateInnerClassFix extends CreateClassFix {

        private FileObject targetFile;
        private ElementHandle<TypeElement> target;
        private ClasspathInfo cpInfo;
        private String name;
        private String inFQN;
        
        public CreateInnerClassFix(CompilationInfo info, String name, Set<Modifier> modifiers, TypeElement target, List<? extends TypeMirror> argumentTypes, List<String> argumentNames, TypeMirror superType, ElementKind kind, int numTypeParameters) {
            super(info, modifiers, argumentTypes, argumentNames, superType, kind, numTypeParameters);
            this.name = name;
            this.target = ElementHandle.create(target);
            this.inFQN = target.getQualifiedName().toString();
            this.cpInfo = info.getClasspathInfo();
            this.targetFile = SourceUtils.getFile(target, cpInfo);
        }
            
        public String getText() {
            return NbBundle.getMessage(CreateClassFix.class, "FIX_CreateInnerClass", name, inFQN, valueForBundle(kind));
        }

        public ChangeInfo implement() throws Exception {
            //use the original cp-info so it is "sure" that the target can be resolved:
            JavaSource js = JavaSource.create(cpInfo, targetFile);
            
            ModificationResult diff = js.runModificationTask(new Task<WorkingCopy>() {

                public void run(final WorkingCopy working) throws IOException {
                    working.toPhase(Phase.RESOLVED);
                    TypeElement targetType = target.resolve(working);
                    
                    if (targetType == null) {
                        ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve target."); // NOI18N
                        return;
                    }
                    
                    TreePath targetTree = working.getTrees().getPath(targetType);
                    
                    if (targetTree == null) {
                        ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve target tree: " + targetType.getQualifiedName() + "."); // NOI18N
                        return;
                    }
                    
                    TreeMaker make = working.getTreeMaker();
                    MethodTree constr = make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC)), "<init>", null, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{}" /*XXX*/, null); // NOI18N
                    ClassTree innerClass = make.Class(make.Modifiers(modifiers), name, Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), Collections.<Tree>singletonList(constr));
                    
                    innerClass = createConstructor(working, new TreePath(targetTree, innerClass));
                    
                    working.rewrite(targetTree.getLeaf(), GeneratorUtils.insertClassMember(working, targetTree, innerClass));
                }
            });
            
            return Utilities.commitAndComputeChangeInfo(targetFile, diff);
        }
        
        public String toDebugString(CompilationInfo info) {
            return "CreateInnerClass:" + inFQN + "." + name + ":" + modifiers.toString() + ":" + kind; // NOI18N
        }
        
    }
    
}
