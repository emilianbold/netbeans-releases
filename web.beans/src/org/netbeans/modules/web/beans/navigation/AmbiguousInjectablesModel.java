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

package org.netbeans.modules.web.beans.navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * The hierarchy tree model
 * Based on org.netbeans.modules.java.navigation.JavaHierarchyModel.
 *
 * @author ads
 */
public final class AmbiguousInjectablesModel extends DefaultTreeModel {
    
    private static final Logger LOG = Logger.getLogger(AmbiguousInjectablesModel.class.getName());
    
    static Element[] EMPTY_ELEMENTS_ARRAY = new Element[0];
    static ElementHandle[] EMPTY_ELEMENTHANDLES_ARRAY = new ElementHandle[0];

    private static final ClassPath EMPTY_CLASSPATH = ClassPathSupport.createClassPath( new FileObject[0] );

    /**
     * Holds value of property pattern.
     */
    private FileObject fileObject;
    private ElementHandle[] elementHandles;

    /**
     */
    public AmbiguousInjectablesModel(FileObject fileObject, Element[] elements, CompilationInfo compilationInfo) {
        super(null);
        this.fileObject = fileObject;

        if ((elements == null) || (elements.length == 0)) {
            elementHandles = EMPTY_ELEMENTHANDLES_ARRAY;
        } else {
            List<ElementHandle> elementHandlesList = new ArrayList<ElementHandle>(elements.length);

            for (Element element : elements) {
                elementHandlesList.add(ElementHandle.create(element));
            }

            elementHandles = elementHandlesList.toArray(EMPTY_ELEMENTHANDLES_ARRAY);
        }

        update(elements, compilationInfo);
    }
    
    public void update() {
        update(elementHandles);
    }

    private void update(final ElementHandle[] elementHandles) {
        if ((elementHandles == null) && (elementHandles.length == 0)) {
            return;
        }

        JavaSource javaSource = JavaSource.forFileObject(fileObject);

        if (javaSource != null) {
            try {
                javaSource.runUserActionTask(new Task<CompilationController>() {
                        public void run(
                            CompilationController compilationController)
                            throws Exception {
                            compilationController.toPhase(Phase.ELEMENTS_RESOLVED);

                            List<Element> elementsList = new ArrayList<Element>(elementHandles.length);

                            for (ElementHandle elementHandle : elementHandles) {
                                final Element element = elementHandle.resolve(compilationController);
                                if (element != null) {
                                    elementsList.add(element);
                                }
                                else {
                                    LOG.warning(elementHandle.toString()+" cannot be resolved using: " +compilationController.getClasspathInfo());
                                }
                            }

                            Element[] elements = elementsList.toArray(EMPTY_ELEMENTS_ARRAY);
                            update(elements, compilationController);
                        }
                    }, true);

                return;
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }

    private void update(final Element[] elements,
        CompilationInfo compilationInfo) {
        if ((elements == null) && (elements.length == 0)) {
            return;
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        for (Element element : elements) {
            if ((element.getKind() == ElementKind.CLASS) ||
                    (element.getKind() == ElementKind.INTERFACE) ||
                    (element.getKind() == ElementKind.ENUM) ||
                    (element.getKind() == ElementKind.ANNOTATION_TYPE)) {
                /*TODO : if (JavaMembersAndHierarchyOptions.isShowSuperTypeHierarchy()) {
                    root.add(new TypeTreeNode(fileObject,
                            ((TypeElement) element), compilationInfo, null));
                } else*/ {
                    Types types = compilationInfo.getTypes();
                    TypeElement typeElement = ((TypeElement) element);
                    List<TypeElement> superClasses = new ArrayList<TypeElement>();
                    superClasses.add(typeElement);

                    TypeElement superClass = (TypeElement) types.asElement(typeElement.getSuperclass());
                    while (superClass != null) {
                        superClasses.add(0, superClass);
                        superClass = (TypeElement) types.asElement(superClass.getSuperclass());;
                    }
                    DefaultMutableTreeNode parent = root;
                    for(TypeElement superTypeElement:superClasses) {
                        FileObject fileObject = SourceUtils.getFile(ElementHandle.create(superTypeElement), compilationInfo.getClasspathInfo());
                        DefaultMutableTreeNode child = new SimpleTypeTreeNode(fileObject, superTypeElement, compilationInfo, null, typeElement != superTypeElement || typeElement.getQualifiedName().equals(Object.class.getName()));
                        parent.insert(child, 0);
                        parent = child;
                    }
                    // TODO JavaMembersAndHierarchyOptions.setSubTypeHierarchyDepth(superClasses.size()+2);
                }
            }
        }

        setRoot(root);
    }

    void fireTreeNodesChanged() {
        super.fireTreeNodesChanged(this, getPathToRoot((TreeNode)getRoot()), null, null);
    }
    
    private abstract class AbstractHierarchyTreeNode
        extends DefaultMutableTreeNode implements JavaElement {
        private FileObject fileObject;
        private ElementHandle<?extends Element> elementHandle;
        private ElementKind elementKind;
        private Set<Modifier> modifiers;
        private String name = "";
        private String label = "";
        private String FQNlabel = "";
        private String tooltip = null;
        private Icon icon = null;
        private ElementJavadoc javaDoc = null;
        private final ClasspathInfo cpInfo;

        private boolean loaded = false;
        private AbstractHierarchyTreeNode owner;

        AbstractHierarchyTreeNode(FileObject fileObject,
            Element element, CompilationInfo compilationInfo, final AbstractHierarchyTreeNode owner) {
            this( fileObject, element, compilationInfo, owner, false);
        }

        AbstractHierarchyTreeNode(FileObject fileObject,
            Element element, CompilationInfo compilationInfo, final AbstractHierarchyTreeNode owner, boolean lazyLoadChildren) {
            this.fileObject = fileObject;
            this.elementHandle = ElementHandle.create(element);
            this.elementKind = element.getKind();
            this.modifiers = element.getModifiers();
            this.cpInfo = compilationInfo.getClasspathInfo();
            this.owner = owner;

            setName(element.getSimpleName().toString());
            setIcon(ElementIcons.getElementIcon(element.getKind(), element.getModifiers()));
            /*
             TODO
             setLabel(Utils.format(element));
            setFQNLabel(Utils.format(element, false, true));
            setToolTip(Utils.format(element, true, JavaMembersAndHierarchyOptions.isShowFQN()));            
             */
            if (!lazyLoadChildren) {
                try {
                    loadChildren(element, compilationInfo);
                } finally {
                    loaded = true;
                }
            }
        }

        public AbstractHierarchyTreeNode getOwningTreeNode () {
            return this.owner;
        }

        @Override
        public int getChildCount() {
            if (!loaded) {
                try {
                    loadChildren();
                } finally {
                    loaded = true;
                }
            }
            return super.getChildCount();
        }

        public FileObject getFileObject() {
            return fileObject;
        }

        public String getName() {
            return name;
        }

        public Set<Modifier> getModifiers() {
            return modifiers;
        }
        
        public ElementKind getElementKind() {
            return elementKind;
        }
        
        protected void setName(String name) {
            this.name = name;
        }

        public String getLabel() {
            return label;
        }

        protected void setLabel(String label) {
            this.label = label;
        }
        
        public String getFQNLabel() {
            return FQNlabel;
        }

        protected void setFQNLabel(String FQNlabel) {
            this.FQNlabel = FQNlabel;
        }

        public String getTooltip() {
            return tooltip;
        }

        protected void setToolTip(String tooltip) {
            this.tooltip = tooltip;
        }

        public Icon getIcon() {
            return icon;
        }

        protected void setIcon(Icon icon) {
            this.icon = icon;
        }

        protected void setElementHandle(
            ElementHandle<?extends Element> elementHandle) {
            this.elementHandle = elementHandle;
        }

        public ElementJavadoc getJavaDoc() {
            if (javaDoc == null) {
                if (fileObject == null) {
                    // Probably no source filem - so cannot get Javadoc
                    return null;
                }
                
                JavaSource javaSource = JavaSource.forFileObject(fileObject);

                if (javaSource != null) {
                    try {
                        javaSource.runUserActionTask(new Task<CompilationController>() {
                                public void run(
                                    CompilationController compilationController)
                                    throws Exception {
                                    compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                                    Element element = elementHandle.resolve(compilationController);
                                    setJavaDoc(ElementJavadoc.create(compilationController, element));
                                }
                            }, true);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            }
            return javaDoc;
        }

        protected void setJavaDoc(ElementJavadoc javaDoc) {
            this.javaDoc = javaDoc;
        }

        public ElementHandle getElementHandle() {
            return elementHandle;
        }

        public void gotoElement() {
            openElementHandle();
        }

        protected void loadChildren() {
            JavaSource javaSource = JavaSource.create(cpInfo);
            if (javaSource != null) {
                try {
                    javaSource.runUserActionTask(new Task<CompilationController>() {
                            public void run(CompilationController compilationController)
                                throws Exception {
                                compilationController.toPhase(Phase.ELEMENTS_RESOLVED);

                                Element element = elementHandle.resolve(compilationController);
                                if (element instanceof TypeElement && ((TypeElement)element).getQualifiedName().toString().equals(Object.class.getName())) {
                                } else {
                                    loadChildren(element, compilationController);
                                }
                            }
                        }, true);

                    return;
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }

        protected abstract void loadChildren(Element element,
            CompilationInfo compilationInfo);

        public String toString() {
            //TODO :return (JavaMembersAndHierarchyOptions.isShowFQN()? getFQNLabel() : getLabel());
            return "";
        }

        protected void openElementHandle() {
        	if (fileObject == null) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(AmbiguousInjectablesModel.class, "MSG_CouldNotOpenElement", getFQNLabel()));
                return;
            }
        	
            if (elementHandle == null) {
                return;
            }

            if (!ElementOpen.open(cpInfo, elementHandle)) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(AmbiguousInjectablesModel.class, "MSG_CouldNotOpenElement", getFQNLabel()));
            }
        }

    }

    private class TypeTreeNode extends AbstractHierarchyTreeNode {
        private boolean inSuperClassRole;

        TypeTreeNode(FileObject fileObject, TypeElement typeElement,
            CompilationInfo compilationInfo, AbstractHierarchyTreeNode owner) {
            this(fileObject, typeElement, compilationInfo, owner, false);
        }

        TypeTreeNode(FileObject fileObject, TypeElement typeElement,
            CompilationInfo compilationInfo, AbstractHierarchyTreeNode owner, boolean inSuperClassRole) {
            super(fileObject, typeElement, compilationInfo, owner);
            this.inSuperClassRole = inSuperClassRole;
        }

        public boolean isLeaf() {
            return false;
        }

        protected void loadChildren(Element element,
            CompilationInfo compilationInfo) {
            loadChildren(element, compilationInfo, 0);
        }

        protected int loadChildren(Element element,
            CompilationInfo compilationInfo, int index) {
            Types types = compilationInfo.getTypes();

            TypeElement typeElement = (TypeElement) element;

            TypeElement superClass = (TypeElement) types.asElement(typeElement.getSuperclass());
            if (superClass != null && !superClass.getQualifiedName().toString().equals(Object.class.getName())) {
                if(!hasCycle(superClass)){
                    insert(new TypeTreeNode(getFileObject(), superClass, compilationInfo, this, true), index++);
                }
            }
            List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
            for (TypeMirror interfaceMirror:interfaces) {
                TypeElement interfaceElement = (TypeElement) types.asElement(interfaceMirror);
                if (interfaceElement != null) {
                    if(!hasCycle((TypeElement)interfaceElement)){
                        insert(new TypeTreeNode(getFileObject(), (TypeElement)interfaceElement, compilationInfo, this, true), index++);
                    }
                }
            }

            /* TODO
             if (JavaMembersAndHierarchyOptions.isShowInner()) {
                if (!inSuperClassRole) {
                    for (Element childElement:typeElement.getEnclosedElements()) {
                        AbstractHierarchyTreeNode node = null;
                        if ((childElement.getKind() == ElementKind.CLASS) ||
                            (childElement.getKind() == ElementKind.INTERFACE) ||
                            (childElement.getKind() == ElementKind.ENUM) ||
                            (childElement.getKind() == ElementKind.ANNOTATION_TYPE)) {
                            node = new TypeTreeNode(getFileObject(), (TypeElement)childElement, compilationInfo, this, true);
                            if(!hasCycle((TypeElement) childElement)){
                                insert(node, index++);
                            }
                        }
                    }
                }
            }*/
            return index;
        }
        private boolean hasCycle (final TypeElement type) {
            final String binName = ElementUtilities.getBinaryName(type);
            AbstractHierarchyTreeNode node = this;
            while (node != null) {
                if (node instanceof TypeTreeNode) {
                    if (binName.equals(((TypeTreeNode)node).getElementHandle().getBinaryName())) {
                        return true;
                    }
                }
                //getParent cannot be used, ut's not yet filled.
                node = node.getOwningTreeNode();
            }
            return false;
        }
    }

    private class SimpleTypeTreeNode extends AbstractHierarchyTreeNode {
        private boolean inSuperClassRole;

        SimpleTypeTreeNode(FileObject fileObject, TypeElement typeElement,
            CompilationInfo compilationInfo, AbstractHierarchyTreeNode owner) {
            this(fileObject, typeElement, compilationInfo, owner, false);
        }

        SimpleTypeTreeNode(FileObject fileObject, TypeElement typeElement,
            CompilationInfo compilationInfo, AbstractHierarchyTreeNode owner, boolean inSuperClassRole) {
            super(fileObject, typeElement, compilationInfo, owner, inSuperClassRole);
            this.inSuperClassRole = inSuperClassRole;
        }

        public boolean isLeaf() {
            return false;
        }

        protected void loadChildren(Element element,
                    CompilationInfo compilationInfo) {
            if (inSuperClassRole) {
                return;
            }

            TypeElement typeElement = (TypeElement) element;
            // prevent showing sub classes of java.lang.Object
            if (typeElement.getQualifiedName().toString().equals(Object.class.getName())) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(AmbiguousInjectablesModel.class, "MSG_WontShowSubTypesOfObject", Object.class.getName())); // TODO
                return;
            }

            // Get open projects
            Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
            if (openProjects == null) {
                return;
            }
            Set<ElementHandle<TypeElement>> processedImplementorElementHandles = new LinkedHashSet<ElementHandle<TypeElement>>();
            
            ElementHandle<TypeElement> typeElementHandle = ElementHandle.create(typeElement);

            final int[] index = new int[] {0};
            // Walk through open projects
            for (Project project : openProjects) {
                // Get Sources
                Collection<? extends Sources> sourcess = project.getLookup().lookupAll(Sources.class);
                if (sourcess == null) {
                    continue;
                }
                
                // Walk through sources
                for (Sources sources : sourcess) {
                    // Get Source groups of type java
                    SourceGroup[] sourceGroups = sources.getSourceGroups("java");
                    if (sourceGroups == null) {
                        continue;
                    }
                    
                    // Walk through source groups
                    for (SourceGroup sourceGroup : sourceGroups) {
                        // Get root file object
                        FileObject rootFileObject = sourceGroup.getRootFolder();
                        if (rootFileObject == null) {
                            continue;
                        }
                        
                        // Find implementors
                        ClassPath classPath =ClassPathSupport.createClassPath(new FileObject[] {rootFileObject});                        
                        ClassPath bootClassPath =ClassPath.getClassPath(rootFileObject, ClassPath.BOOT);
                        ClassPath compileClassPath =ClassPath.getClassPath(rootFileObject, ClassPath.COMPILE);                            
                        if (classPath != null) {                            
                            ClasspathInfo classpathInfo = ClasspathInfo.create(bootClassPath, compileClassPath, classPath);
                            if (classpathInfo != null) {
                                ClassIndex classIndex = classpathInfo.getClassIndex();
                                if (classIndex != null) {
                                    Set<ElementHandle<TypeElement>> implementors = classIndex.getElements(typeElementHandle,
                                            EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
                                            EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES));
                                    for (ElementHandle<TypeElement> implementorElementHandle: implementors) {
                                        if (processedImplementorElementHandles.contains(implementorElementHandle)) {
                                            continue;
                                        }
                                        processedImplementorElementHandles.add(implementorElementHandle);
                                        final ElementHandle<TypeElement> finalImplementorElementHandle = implementorElementHandle;
                                        final FileObject implementorfileObject = 
                                            SourceUtils.getFile(implementorElementHandle, classpathInfo);
                                        if (implementorfileObject == null) {
                                            continue;
                                        }
                                        JavaSource javaSource = JavaSource.forFileObject(implementorfileObject);
                                        if (javaSource != null) {
                                            try {
                                                javaSource.runUserActionTask(new Task<CompilationController>() {
                                                        public void run(CompilationController compilationController)
                                                            throws Exception {
                                                            compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                                                            Element implementor = finalImplementorElementHandle.resolve(compilationController);
                                                            if (implementor instanceof TypeElement && ((TypeElement)implementor).getNestingKind() != NestingKind.ANONYMOUS) {
                                                                insert(new SimpleTypeTreeNode(implementorfileObject, (TypeElement) implementor, compilationController, SimpleTypeTreeNode.this), index[0]++);
                                                            }
                                                        }
                                                    }, true);
                                            } catch (IOException ioe) {
                                                Exceptions.printStackTrace(ioe);
                                            }
                                        }                                       
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
