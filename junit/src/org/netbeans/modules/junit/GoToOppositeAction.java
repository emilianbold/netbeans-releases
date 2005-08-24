/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.jmi.reflect.RefFeatured;
import javax.jmi.reflect.RefObject;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.ClassDefinition;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.NamedElement;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.junit.wizards.Utils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;

/**
 * Jumps to the opposite class or method.
 * If the cursor is currently in a source method, this action will jump to the
 * corresponding test method and vice versa. If the cursor is currently in a
 * source class but not in any method, this action will switch to the beginning
 * of the corresponding class.
 *
 * @see  OpenTestAction
 * @author  Marian Petras
 */
public final class GoToOppositeAction extends NodeAction {
    
    /**
     *
     */
    public GoToOppositeAction() {
        super();
    }
    
    /**
     */
    protected void performAction(Node[] nodes) {
        assert EventQueue.isDispatchThread();
        
        FileObject selectedFO;
        FileObject selectedFORoot;
        Project project;
        ClassPath srcCP, tstCP;
        Utils utils;
        FileObject[] oppositeRootsRaw;
        FileObject[] oppositeRoots;
        boolean sourceToTest = true;   //false .. navigation from test to source

        if ((nodes.length != 1)
          || ((selectedFO = TestUtil.getFileObjectFromNode(nodes[0])) == null)
          || ((project = FileOwnerQuery.getOwner(selectedFO)) == null)
          || ((srcCP = ClassPath.getClassPath(selectedFO, ClassPath.SOURCE))
                                      == null)
          || ((selectedFORoot = srcCP.findOwnerRoot(selectedFO)) == null)
          || ((utils = new Utils(project)) == null)   //side effect - assignment
          || (((oppositeRootsRaw = utils.getTestFoldersRaw(selectedFORoot))
                 .length == 0)
                        || ((oppositeRoots = Utils.skipNulls(oppositeRootsRaw))
                            .length == 0))
             && (((oppositeRootsRaw = utils.getSourceFoldersRaw(selectedFORoot))
                 .length == 0)
                        || ((oppositeRoots = Utils.skipNulls(oppositeRootsRaw))
                            .length == 0)
                        || (sourceToTest = false))) { //side effect - assignment
          return;
        }
        
        String baseResName = srcCP.getResourceName(selectedFO, '/', false);
        String oppoResName = sourceToTest
                             ? getTestResName(baseResName, selectedFO.getExt())
                             : getSrcResName(baseResName, selectedFO.getExt());
        assert ((oppoResName != null) || (sourceToTest == false));
        if (oppoResName == null) {
            return;     //if the selectedFO is not a test class (by name)
        }
        
        List/*<FileObject>*/ oppoFiles = ClassPathSupport
                                         .createClassPath(oppositeRoots)
                                         .findAllResources(oppoResName);
        if (oppoFiles.isEmpty()) {
            if (sourceToTest) {
                return;             //PENDING - offer creation of new test class
            } else {
                return;
            }
        }
        
        NamedElement element = getNavigationElement();
        if (element == null) {
            OpenTestAction.openFile((FileObject) oppoFiles.get(0));
            return;
        }

        assert (element instanceof Method)
               || (element instanceof ClassDefinition);
            
        ClassDefinition clazz;
        String oppoMethodName = null;
        String baseClassName, oppoClassName;
        String pkgName;

        if (element instanceof Method) {
            Method method = (Method) element;
            String baseMethodName = method.getName();
            oppoMethodName = sourceToTest
                             ? getTestMethodName(baseMethodName)
                             : getSourceMethodName(baseMethodName);
            clazz = method.getDeclaringClass();
        } else {
            clazz = (ClassDefinition) element;
        }
        baseClassName = clazz.getName();            //PENDING - inner classes!!!
        oppoClassName = sourceToTest
                        ? getTestClassName(baseClassName)
                        : getSourceClassName(baseClassName);
        
        if (oppoClassName == null) {
            OpenTestAction.openFile((FileObject) oppoFiles.get(0));
            return;
        }
        
        FileObject foWithClass = null;
        FileObject foWithMethod = null;
        ClassDefinition theClassDef = null;
        Method theMethod = null;

        for (Iterator/*<FileObject>*/ i = oppoFiles.iterator();
                                      i.hasNext(); 
                                      ) {
            FileObject fileObj = (FileObject) i.next();
            Resource resource = JavaModel.getResource(fileObj);
            assert resource != null;

            ClassDefinition classDef = findClassDef(resource,
                                                    oppoClassName);
            if (classDef != null) {
                if (foWithClass == null) {
                    foWithClass = fileObj;
                    theClassDef = classDef;
                }
                if (oppoMethodName != null) {
                    Method method = sourceToTest
                                    ? findTestMethod(classDef,
                                                     oppoMethodName)
                                    : findSourceMethod(classDef,
                                                       oppoMethodName);
                    if (method != null) {
                        theMethod = method;
                        foWithMethod = fileObj;
                        break;
                    }
                }
            }
        }
        
        if (foWithClass == null) {
            OpenTestAction.openFile((FileObject) oppoFiles.get(0));
            return;
        }
        
        int offset;
        if (foWithMethod != null) {
            OpenTestAction.openFileAtElement(foWithMethod, theMethod);
        } else {
            OpenTestAction.openFileAtElement(foWithClass, theClassDef);
        }
    }

    /**
     */
    protected boolean enable(Node[] nodes) {
        assert EventQueue.isDispatchThread();
        
        FileObject selectedFO;
        FileObject selectedFORoot;
        Project project;
        ClassPath srcCP;
        Utils utils;
        FileObject[] oppositeRootsRaw;
        
        return
            (nodes.length == 1)
          && ((selectedFO = TestUtil.getFileObjectFromNode(nodes[0])) != null)
          && ((project = FileOwnerQuery.getOwner(selectedFO)) != null)
          && ((srcCP = ClassPath.getClassPath(selectedFO, ClassPath.SOURCE))
                                        != null)
          && ((selectedFORoot = srcCP.findOwnerRoot(selectedFO)) != null)
          && ((utils = new Utils(project)) != null)   //side effect - assignment
          && ((  (oppositeRootsRaw = utils.getTestFoldersRaw(selectedFORoot))
                 .length != 0)
                        && (Utils.skipNulls(oppositeRootsRaw).length != 0)
              || ((oppositeRootsRaw = utils.getSourceFoldersRaw(selectedFORoot))
                 .length != 0)
                        && (Utils.skipNulls(oppositeRootsRaw).length != 0));
    }
    
    /**
     */
    public String getName() {
        assert EventQueue.isDispatchThread();

        Node[] nodes;
        FileObject selectedFO;
        FileObject selectedFORoot;
        Project project;
        ClassPath srcCP, tstCP;
        Utils utils;
        FileObject[] oppositeRootsRaw;
        
        boolean sourceToTest = true;   //false .. navigation from test to source

        boolean disabled = 
          ((nodes = TopComponent.getRegistry().getCurrentNodes()).length != 1)
          || ((selectedFO = TestUtil.getFileObjectFromNode(nodes[0])) == null)
          || ((project = FileOwnerQuery.getOwner(selectedFO)) == null)
          || ((srcCP = ClassPath.getClassPath(selectedFO, ClassPath.SOURCE))
                                      == null)
          || ((selectedFORoot = srcCP.findOwnerRoot(selectedFO)) == null)
          || ((utils = new Utils(project)) == null)   //side effect - assignment
          || (((oppositeRootsRaw = utils.getTestFoldersRaw(selectedFORoot))
                 .length == 0)
                        || (Utils.skipNulls(oppositeRootsRaw).length == 0))
             && (((oppositeRootsRaw = utils.getSourceFoldersRaw(selectedFORoot))
                 .length == 0)
                        || (Utils.skipNulls(oppositeRootsRaw).length == 0)
                        || (sourceToTest = false));

        return NbBundle.getMessage(
                        getClass(), disabled || sourceToTest
                                    ? "LBL_Action_GoToTest"             //NOI18N
                                    : "LBL_Action_GoToSource");         //NOI18N
    }
    
    /**
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    /**
     */
    protected boolean asynchronous() {
        return false;
    }
    
    /**
     */
    private static String getTestResName(String baseResName, String ext) {
        StringBuffer buf = new StringBuffer(baseResName.length() + ext.length()
                                            + 10);
        buf.append(baseResName).append("Test");                         //NOI18N
        if (ext.length() != 0) {
            buf.append('.').append(ext);
        }
        return buf.toString();
    }
    
    /**
     */
    private static String getSrcResName(String testResName, String ext) {
        if (!testResName.endsWith("Test")) {                            //NOI18N
            return null;
        }
        
        StringBuffer buf = new StringBuffer(testResName.length() +ext.length());
        buf.append(testResName.substring(0, testResName.length() - 4));
        if (ext.length() != 0) {
            buf.append('.').append(ext);
        }
        return buf.toString();
    }
    
    /**
     */
    private static String getTestClassName(String baseClassName) {
        return baseClassName + "Test";                                  //NOI18N
    }
    
    /**
     */
    private static String getSourceClassName(String testClassName) {
        final String suffix = "Test";                                   //NOI18N
        final int suffixLen = suffix.length();
        
        return ((testClassName.length() > suffixLen)
                    && testClassName.endsWith(suffix))
               ? testClassName.substring(0, testClassName.length() - suffixLen)
               : null;
    }
    
    /**
     */
    private static String getTestMethodName(String baseMethodName) {
        final String prefix = "test";                                   //NOI18N
        final int prefixLen = prefix.length();
        
        StringBuffer buf = new StringBuffer(prefixLen
                                            + baseMethodName.length());
        buf.append(prefix).append(baseMethodName);
        buf.setCharAt(prefixLen,
                      Character.toUpperCase(baseMethodName.charAt(0)));
        return buf.toString();
    }
    
    /**
     */
    private static String getSourceMethodName(String testMethodName) {
        final String prefix = "test";                                   //NOI18N
        final int prefixLen = prefix.length();
        
        return ((testMethodName.length() > prefixLen)
                    && testMethodName.startsWith(prefix))
               ? new StringBuffer(testMethodName.length() - prefixLen)
                 .append(Character.toLowerCase(testMethodName.charAt(prefixLen)))
                 .append(testMethodName.substring(prefixLen + 1))
                 .toString()
               : null;
    }
    
    /**
     * Finds method or class element at the current editor cursor position.
     *
     * @return  the element, or <code>null</code> if the editor was not the
     *          active component or if no such element was found
     */
    private NamedElement getNavigationElement() {
        RefFeatured javaElement = getJavaElement();
        
        while ((javaElement instanceof RefObject)
                && (!(javaElement instanceof Method))
                && (!(javaElement instanceof ClassDefinition))) {
            javaElement = ((RefObject) javaElement).refImmediateComposite();
        }
        
        return ((javaElement instanceof Method)
                    || (javaElement instanceof ClassDefinition))
               ? (NamedElement) javaElement
               : null;
    }
    
    /**
     * Finds Java element at the current editor cursor position.
     *
     * @return  the element, or <code>null</code> if the editor was not the
     *          active component or if no such element was found
     */
    private Element getJavaElement() {
        TopComponent comp = TopComponent.getRegistry().getActivated();
        if (!(comp instanceof CloneableEditor)) {
            return null;
        }
        
        final JEditorPane editorPane = ((CloneableEditor) comp).getEditorPane();
        final int caretPos = editorPane.getCaretPosition();
        
        final FileObject fileObj = getFileObject(editorPane.getDocument());
        if (fileObj == null) {
            return null;
        }
        
        final Resource resource = JavaModel.getResource(fileObj);
        if (resource == null) {
            return null;
        }
        
        return (caretPos < resource.getEndOffset())
               ? resource.getElementByOffset(caretPos)
               : null;
    }

    /**
     * Finds class of the given name in the given resource.
     *
     * @return  the found class, or <code>null</code> if the class was not found
     */
    private static ClassDefinition findClassDef(Resource resource,
                                                String className) {
        for (Iterator/*<Element>*/ i = resource.getChildren().iterator();
                i.hasNext();
                ) {
            Element e = (Element) i.next();
            if ((e instanceof ClassDefinition)
                    && ((ClassDefinition) e).getName().equals(className)) {
                return (ClassDefinition) e;
            }
        }
        return null;
    }
    
    /**
     * Finds a no-arg method with void return type of the given name.
     *
     * @param  classDef  class to find the method in
     * @param  methodName  requested name of the method
     * @return  found method, or <code>null</code> if not found
     */
    private static Method findTestMethod(ClassDefinition classDef,
                                         String methodName) {
        
        Method method = classDef.getMethod(methodName,
                                           Collections.EMPTY_LIST,
                                           false);
        return (method != null)
               && Modifier.isPublic(method.getModifiers())
               && method.getTypeName().getName().equals("void")         //NOI18N
                      ? method
                      : null;
    }
    
    /**
     * Finds a method with of the given name.
     *
     * @param  classDef  class to find the method in
     * @param  methodName  requested name of the method
     * @return  found method, or <code>null</code> if not found
     */
    private static Method findSourceMethod(ClassDefinition classDef,
                                           String methodName) {
        List/*<Element>*/ classChildren = classDef.getChildren();
        
        if ((classChildren == null) || (classChildren.isEmpty())) {
            return null;
        }
        
        for (Iterator/*<Element>*/ i = classChildren.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof Method) {
                Method method = (Method) o;
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
        }
        return null;
    }
    
    /**
     */
    private static FileObject getFileObject(Document document) {
        
        /* copied from org.netbeans.modules.editor.NbEditorUtilities */
        
        Object sdp = document.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof FileObject) {
            return (FileObject) sdp;
        }
        if (sdp instanceof DataObject) {
            return ((DataObject) sdp).getPrimaryFile();
        }
        return null;
    }

}
