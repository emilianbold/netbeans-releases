/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import java.awt.EventQueue;
import javax.jmi.reflect.RefFeatured;
import javax.jmi.reflect.RefObject;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.Feature;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.junit.plugin.JUnitPlugin;
import org.netbeans.modules.junit.plugin.JUnitPlugin.Location;
import org.netbeans.modules.junit.wizards.Utils;
import org.openide.ErrorManager;
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
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        String trimmedName = NbBundle.getMessage(getClass(), "LBL_Action_GoToTest_trimmed"); // NOI18N
        putValue("PopupMenuText", trimmedName);
        putValue("trimmed-text", trimmedName);
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

        if ((nodes == null)
          || (nodes.length != 1)
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
             && (selectedFO.isFolder()
                        ||
                 ((oppositeRootsRaw = utils.getSourceFoldersRaw(selectedFORoot))
                 .length == 0)
                        || ((oppositeRoots = Utils.skipNulls(oppositeRootsRaw))
                            .length == 0)
                        || (sourceToTest = false))) { //side effect - assignment
          return;
        }
        
        JUnitPlugin plugin = TestUtil.getPluginForProject(project);
        assert plugin != null;
        
        Location baseLocation = new Location(selectedFO,
                                             getNavigationElement());
        Location oppoLocation = sourceToTest
                                ? JUnitPluginTrampoline.DEFAULT
                                  .getTestLocation(plugin, baseLocation)
                                : JUnitPluginTrampoline.DEFAULT
                                  .getTestedLocation(plugin, baseLocation);
        
        if (oppoLocation == null) {
            if (sourceToTest) {
                String sourceClsName;
                sourceClsName = srcCP.getResourceName(selectedFO, '/', false)
                                     .replace('/', '.');
                String msgKey = 
                        !selectedFO.isFolder()
                        ? "MSG_test_class_not_found"                    //NOI18N
                        : (sourceClsName.length() != 0)
                              ? "MSG_testsuite_class_not_found"         //NOI18N
                              : "MSG_testsuite_class_not_found_def_pkg";//NOI18N
                TestUtil.notifyUser(
                        NbBundle.getMessage(getClass(), msgKey, sourceClsName),
                        ErrorManager.INFORMATIONAL);
            }
            return;
        }
        
        assert oppoLocation.getFileObject() != null;
        
        FileObject oppoFile = oppoLocation.getFileObject();
        Feature oppoElement = oppoLocation.getJavaElement();
        if (oppoElement == null) {
            OpenTestAction.openFile(oppoFile);
        } else {
            OpenTestAction.openFileAtElement(oppoFile, oppoElement);
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
          (nodes != null)
          && (nodes.length == 1)
          && ((selectedFO = TestUtil.getFileObjectFromNode(nodes[0])) != null)
          && ((project = FileOwnerQuery.getOwner(selectedFO)) != null)
          && ((srcCP = ClassPath.getClassPath(selectedFO, ClassPath.SOURCE))
                                        != null)
          && ((selectedFORoot = srcCP.findOwnerRoot(selectedFO)) != null)
          && ((utils = new Utils(project)) != null)   //side effect - assignment
          && ((  (oppositeRootsRaw = utils.getTestFoldersRaw(selectedFORoot))
                 .length != 0)
                        && (Utils.skipNulls(oppositeRootsRaw).length != 0)
              || !selectedFO.isFolder()
                        &&
                 ((oppositeRootsRaw = utils.getSourceFoldersRaw(selectedFORoot))
                 .length != 0)
                        && (Utils.skipNulls(oppositeRootsRaw).length != 0));
    }
    
    /**
     */
    public String getName() {
        
        Node[] nodes;
        FileObject selectedFO;
        FileObject selectedFORoot;
        Project project;
        ClassPath srcCP, tstCP;
        Utils utils;
        FileObject[] oppositeRootsRaw;
        
        boolean sourceToTest = true;   //false .. navigation from test to source

        boolean disabled = 
          ((nodes = TopComponent.getRegistry().getCurrentNodes()) == null)
          || (nodes.length != 1)
          || ((selectedFO = TestUtil.getFileObjectFromNode(nodes[0])) == null)
          || ((project = FileOwnerQuery.getOwner(selectedFO)) == null)
          || ((srcCP = ClassPath.getClassPath(selectedFO, ClassPath.SOURCE))
                                      == null)
          || ((selectedFORoot = srcCP.findOwnerRoot(selectedFO)) == null)
          || ((utils = new Utils(project)) == null)   //side effect - assignment
          || (((oppositeRootsRaw = utils.getTestFoldersRaw(selectedFORoot))
                 .length == 0)
                        || (Utils.skipNulls(oppositeRootsRaw).length == 0))
             && (selectedFO.isFolder()
                        ||
                 ((oppositeRootsRaw = utils.getSourceFoldersRaw(selectedFORoot))
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
    protected void initialize () {
	super.initialize ();
        putProperty(Action.SHORT_DESCRIPTION,
                    NbBundle.getMessage(getClass(),
                                        "HINT_Action_GoToTest"));       //NOI18N
    }

    /**
     */
    protected boolean asynchronous() {
        return false;
    }
    
    /**
     * Finds method or class element at the current editor cursor position.
     *
     * @return  the element, or <code>null</code> if the editor was not the
     *          active component or if no such element was found
     */
    private Feature getNavigationElement() {
        RefFeatured javaElement = getJavaElement();
        
        while ((javaElement instanceof RefObject)
                && (!(javaElement instanceof Method))
                && (!(javaElement instanceof JavaClass))) {
            javaElement = ((RefObject) javaElement).refImmediateComposite();
        }
        
        return ((javaElement instanceof Method)
                    || (javaElement instanceof JavaClass))
               ? (Feature) javaElement
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
