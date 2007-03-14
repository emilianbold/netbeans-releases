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

package org.netbeans.modules.java.navigation;

import com.sun.javadoc.Doc;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.UiUtils;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * The tree model for hierarchy pop up window.
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
public final class JavaHierarchyModel extends DefaultTreeModel {
    static Element[] EMPTY_ELEMENTS_ARRAY = new Element[0];
    static ElementHandle[] EMPTY_ELEMENTHANDLES_ARRAY = new ElementHandle[0];

    /**
     * Holds value of property pattern.
     */
    private String pattern = ""; // NOI18N
    private String patternLowerCase = ""; // NOI18N
    private FileObject fileObject;
    private ElementHandle[] elementHandles;

    /**
     */
    public JavaHierarchyModel(FileObject fileObject, Element[] elements, CompilationInfo compilationInfo) {
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

    /**
     * Getter for property pattern.
     * @return Value of property pattern.
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * Setter for property pattern.
     * @param pattern New value of property pattern.
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
        if (pattern == null) {
            patternLowerCase = null;
        } else {
            patternLowerCase = pattern.toLowerCase();
        }
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
                javaSource.runUserActionTask(new CancellableTask<CompilationController>() {
                        public void cancel() {
                        }

                        public void run(
                            CompilationController compilationController)
                            throws Exception {
                            compilationController.toPhase(Phase.ELEMENTS_RESOLVED);

                            List<Element> elementsList = new ArrayList<Element>(elementHandles.length);

                            for (ElementHandle elementHandle : elementHandles) {
                                elementsList.add(elementHandle.resolve(
                                        compilationController));
                            }

                            Element[] elements = elementsList.toArray(EMPTY_ELEMENTS_ARRAY);
                            update(elements, compilationController);
                        }
                    }, false);

                return;
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
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
                    (element.getKind() == ElementKind.ENUM)) {
                if (JavaMembersAndHierarchyOptions.isShowSuperTypeHierarchy()) {
                    root.add(new TypeTreeNode(fileObject,
                            ((TypeElement) element), compilationInfo));
                } else {
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
                        FileObject fileObject = SourceUtils.getFile(superTypeElement, compilationInfo.getClasspathInfo());
                        DefaultMutableTreeNode child = new SimpleTypeTreeNode(fileObject, superTypeElement, compilationInfo, typeElement != superTypeElement || typeElement.getQualifiedName().equals(Object.class.getName()));
                        if (typeElement.getQualifiedName().toString().equals(Object.class.getName())) {
                            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaHierarchyModel.class, "MSG_WontShowSubTypesOfObject", Object.class.getName())); // TODO
                        }
                        parent.insert(child, 0);
                        parent = child;
                    }
                }
            }
        }

        setRoot(root);
    }

    public boolean patternMatch(JavaElement javaToolsJavaElement) {
        return Utils.patternMatch(javaToolsJavaElement, pattern, patternLowerCase);
    }

   private abstract class AbstractHierarchyTreeNode
        extends DefaultMutableTreeNode implements JavaElement {
        private FileObject fileObject;
        private ElementHandle<?extends Element> elementHandle;
        private ElementKind elementKind;
        private Set<Modifier> modifiers;
        private String name = "";
        private String label = "";
        private String tooltip = null;
        private Icon icon = null;
        private String javaDoc = "";

        private boolean loaded = false;

        AbstractHierarchyTreeNode(FileObject fileObject,
            Element element, CompilationInfo compilationInfo) {
            this( fileObject, element, compilationInfo, false);
        }

        AbstractHierarchyTreeNode(FileObject fileObject,
            Element element, CompilationInfo compilationInfo, boolean lazyLoadChildren) {
            this.fileObject = fileObject;
            this.elementHandle = ElementHandle.create(element);
            this.elementKind = element.getKind();
            this.modifiers = element.getModifiers();

            setName(element.getSimpleName().toString());
            setIcon(UiUtils.getElementIcon(element.getKind(),
                    element.getModifiers()));
            setLabel(Utils.format(element));
            setToolTip(Utils.format(element, true));
            Doc doc = compilationInfo.getElementUtilities().javaDocFor(element);
            if (doc != null) {
                StringBuilder stringBuilder = new StringBuilder();
                setJavaDoc(doc.getRawCommentText());
            }
            // prevent showing sub classes of java.lang.Object
            if (element instanceof TypeElement && ((TypeElement)element).getQualifiedName().toString().equals(Object.class.getName())) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaHierarchyModel.class, "MSG_WontShowSubTypesOfObject", Object.class.getName())); // TODO
            } else {
                if (!lazyLoadChildren) {
                    try {
                        loadChildren(element, compilationInfo);
                    } finally {
                        loaded = true;
                    }
                }
            }
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

        protected void setName(String name) {
            this.name = name;
        }

        public String getLabel() {
            return label;
        }

        protected void setLabel(String label) {
            this.label = label;
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

        public String getJavaDoc() {
            return javaDoc;
        }

        public void setJavaDoc(String javaDoc) {
            this.javaDoc = javaDoc;
        }

        public Set<Modifier> getModifiers() {
            return modifiers;
        }

        public ElementHandle getElementHandle() {
            return elementHandle;
        }

        public void gotoElement() {
            openElementHandle();
        }

        protected void loadChildren() {
            JavaSource javaSource = JavaSource.forFileObject(fileObject);

            if (javaSource != null) {
                try {
                    javaSource.runUserActionTask(new CancellableTask<CompilationController>() {
                            public void cancel() {
                            }

                            public void run(CompilationController compilationController)
                                throws Exception {
                                compilationController.toPhase(Phase.ELEMENTS_RESOLVED);

                                Element element = elementHandle.resolve(compilationController);
                                if (element instanceof TypeElement && ((TypeElement)element).getQualifiedName().toString().equals(Object.class.getName())) {
                                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaHierarchyModel.class, "MSG_WontShowSubTypesOfObject", Object.class.getName())); // TODO
                                }
                                loadChildren(element, compilationController);
                            }
                        }, false);

                    return;
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
        }

        protected abstract void loadChildren(Element element,
            CompilationInfo compilationInfo);

        public String toString() {
            return getLabel();
        }

        protected void openElementHandle() {
            if (elementHandle == null) {
                return;
            }

            UiUtils.open(fileObject, elementHandle);
        }
    }

    private class TypeTreeNode extends AbstractHierarchyTreeNode {
        private boolean inSuperClassRole;

        TypeTreeNode(FileObject fileObject, TypeElement typeElement,
            CompilationInfo compilationInfo) {
            this(fileObject, typeElement, compilationInfo, false);
        }

        TypeTreeNode(FileObject fileObject, TypeElement typeElement,
            CompilationInfo compilationInfo, boolean inSuperClassRole) {
            super(fileObject, typeElement, compilationInfo);
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
                FileObject fileObject = SourceUtils.getFile(superClass, compilationInfo.getClasspathInfo());
                insert(new TypeTreeNode(fileObject, superClass, compilationInfo, true), index++);
            }
            List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
            for (TypeMirror interfaceMirror:interfaces) {
                TypeElement interfaceElement = (TypeElement) types.asElement(interfaceMirror);
                if (interfaceElement != null) {
                    FileObject fileObject = SourceUtils.getFile(interfaceElement, compilationInfo.getClasspathInfo());
                    insert(new TypeTreeNode(fileObject, (TypeElement)interfaceElement, compilationInfo, true), index++);
                }
            }

            if (JavaMembersAndHierarchyOptions.isShowInner()) {
                if (!inSuperClassRole) {
                    for (Element childElement:typeElement.getEnclosedElements()) {
                        AbstractHierarchyTreeNode node = null;
                        if ((childElement.getKind() == ElementKind.CLASS) ||
                            (childElement.getKind() == ElementKind.INTERFACE) ||
                            (childElement.getKind() == ElementKind.ENUM)) {
                            node = new TypeTreeNode(fileObject, (TypeElement)childElement, compilationInfo, true);
                            insert(node, index++);
                        }
                    }
                }
            }
            return index;
        }
    }

    private class SimpleTypeTreeNode extends AbstractHierarchyTreeNode {
        private boolean inSuperClassRole;

        SimpleTypeTreeNode(FileObject fileObject, TypeElement typeElement,
            CompilationInfo compilationInfo) {
            this(fileObject, typeElement, compilationInfo, false);
        }

        SimpleTypeTreeNode(FileObject fileObject, TypeElement typeElement,
            CompilationInfo compilationInfo, boolean inSuperClassRole) {
            super(fileObject, typeElement, compilationInfo, inSuperClassRole);
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
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaHierarchyModel.class, "MSG_WontShowSubTypesOfObject", Object.class.getName())); // TODO
                return;
            }

            ClassIndex classIndex = ClasspathInfo.create(getFileObject()).getClassIndex();
            Set<ElementHandle<TypeElement>> implementors = classIndex.getElements(ElementHandle.create(typeElement),
                    EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
                    EnumSet.of(ClassIndex.SearchScope.SOURCE
                    , ClassIndex.SearchScope.DEPENDENCIES
                    ));
            int index = 0;
            for (ElementHandle<TypeElement> implementorElementHandle: implementors) {
                Element implementor = implementorElementHandle.resolve(compilationInfo);
                if (implementor instanceof TypeElement) {
                    FileObject fileObject = SourceUtils.getFile(implementor, compilationInfo.getClasspathInfo());
                    if (fileObject != null) {
                        insert(new SimpleTypeTreeNode(fileObject, (TypeElement) implementor, compilationInfo), index++);
                    }
                }
            }
        }
    }

}
