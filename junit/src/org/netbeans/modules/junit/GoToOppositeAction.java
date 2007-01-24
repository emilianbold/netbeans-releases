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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.junit.plugin.JUnitPlugin;
import org.netbeans.modules.junit.plugin.JUnitPlugin.Location;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CallableSystemAction;
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
@SuppressWarnings("serial")
public final class GoToOppositeAction extends CallableSystemAction {
    
    /**
     *
     */
    public GoToOppositeAction() {
        super();
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        String trimmedName = NbBundle.getMessage(
                                        getClass(),
                                        "LBL_Action_GoToTest_trimmed"); //NOI18N
        putValue("PopupMenuText", trimmedName);                         //NOI18N
        putValue("trimmed-text", trimmedName);                          //NOI18N
    }
    
    /**
     */
    @Override
    public void performAction() {
        assert EventQueue.isDispatchThread();
        
        TopComponent comp;
        JEditorPane editorPane;
        FileObject fileObj;
        ClassPath srcCP;
        FileObject fileObjRoot;
        Project project;
        
        boolean sourceToTest = true;
        comp = TopComponent.getRegistry().getActivated();
        if (comp instanceof CloneableEditorSupport.Pane) {
            editorPane = ((CloneableEditorSupport.Pane) comp).getEditorPane();
            if (editorPane == null) {
                return;
            }
            
            fileObj = getFileObject(editorPane.getDocument());
        } else {
            editorPane = null;
            
            Node[] selectedNodes = comp.getActivatedNodes();
            if ((selectedNodes == null) || (selectedNodes.length != 1)) {
                return;
            }
            
            DataObject dataObj = selectedNodes[0].getLookup().lookup(DataObject.class);
            if (dataObj == null) {
                return;
            }
            
            fileObj = dataObj.getPrimaryFile();
        }
        
        if ((fileObj == null)
          || !TestUtil.isJavaFile(fileObj)
          || ((srcCP = ClassPath.getClassPath(fileObj, ClassPath.SOURCE)) == null)
          || ((fileObjRoot = srcCP.findOwnerRoot(fileObj)) == null)
          || ((project = FileOwnerQuery.getOwner(fileObjRoot)) == null)
          || (UnitTestForSourceQuery.findUnitTests(fileObjRoot).length == 0)
              && !(sourceToTest = false)         //side effect - assignment
              && (UnitTestForSourceQuery.findSources(fileObjRoot).length == 0)) {
            return;
        }
        
        JUnitPlugin plugin = TestUtil.getPluginForProject(project);
        assert plugin != null;
        
        SourceGroup[] srcGroups;
        FileObject[] srcRoots;
        srcGroups = ProjectUtils.getSources(project)
                    .getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        srcRoots = new FileObject[srcGroups.length];
        for (int i = 0; i < srcGroups.length; i++) {
            srcRoots[i] = srcGroups[i].getRootFolder();
        }
        ClassPath srcClassPath = ClassPathSupport.createClassPath(srcRoots);

        /*
        ClasspathInfo cpInfo = ClasspathInfo.create(
                        ClassPath.getClassPath(fileObj, ClassPath.BOOT),
                        ClassPath.getClassPath(fileObj, ClassPath.COMPILE),
                        srcClassPath);
        int caretPos = editorPane.getCaretPosition();
        boolean fromSourceToTest = sourceToTest;
        
        JavaSource javaSource = JavaSource.create(
                cpInfo,
                Collections.<FileObject>singleton(fileObj));
        
        ElementFinder elementFinder = new ElementFinder(caretPos);
        try {
            javaSource.runUserActionTask(elementFinder, true);
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);     //NOI18N
        }
        Element element = elementFinder.getElement();
        */
        RequestProcessor.getDefault().post(
                new ActionImpl(plugin,
                               new Location(fileObj/*, element*/),
                               sourceToTest,
                               srcClassPath));
    }
    
    /**
     * Determines an element at the current cursor position.
     */
    private class ElementFinder implements CancellableTask<CompilationController> {
        
        /** */
        private final int caretPosition;
        /** */
        private volatile boolean cancelled;
        /** */
        private Element element = null;
        
        /**
         */
        private ElementFinder(int caretPosition) {
            this.caretPosition = caretPosition;
        }
    
        /**
         */
        public void run(CompilationController controller) throws IOException {
            controller.toPhase(Phase.RESOLVED);     //cursor position needed
            if (cancelled) {
                return;
            }

            TreePath treePath = controller.getTreeUtilities()
                                          .pathFor(caretPosition);
            if (treePath != null) {
                if (cancelled) {
                    return;
                }
                
                TreePath parent = treePath.getParentPath();
                while (parent != null) {
                    Tree.Kind parentKind = parent.getLeaf().getKind();
                    if ((parentKind == Tree.Kind.CLASS)
                            || (parentKind == Tree.Kind.COMPILATION_UNIT)) {
                        break;
                    }
                    treePath = parent;
                    parent = treePath.getParentPath();
                }

            }

            if (treePath != null) {
                if (cancelled) {
                    return;
                }

                try {
                    element = controller.getTrees().getElement(treePath);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger("global").log(Level.WARNING, null, ex);
                }
            }
        }
        
        /**
         */
        public void cancel() {
            cancelled = true;
        }
        
        /**
         */
        Element getElement() {
            return element;
        }

    }
    
    /**
     * 
     */
    private class ActionImpl implements Runnable {
        
        private final JUnitPlugin plugin;
        private final Location currLocation;
        private final boolean sourceToTest;
        private final ClassPath srcClassPath;
        
        private Location oppoLocation;
        
        ActionImpl(JUnitPlugin plugin,
                   Location currLocation,
                   boolean sourceToTest,
                   ClassPath srcClassPath) {
            this.plugin = plugin;
            this.currLocation = currLocation;
            this.sourceToTest = sourceToTest;
            this.srcClassPath = srcClassPath;
        }
        
        public void run() {
            if (!EventQueue.isDispatchThread()) {
                findOppositeLocation();
                if ((oppoLocation != null) || sourceToTest) {
                    EventQueue.invokeLater(this);
                }
            } else {
                if (oppoLocation != null) {
                    goToOppositeLocation();
                } else if (sourceToTest) {
                    displayNoOppositeLocationFound();
                }
            }
        }
        
        /**
         */
        private void findOppositeLocation() {
            oppoLocation = sourceToTest
                  ? JUnitPluginTrampoline.DEFAULT.getTestLocation(plugin,
                                                                  currLocation)
                  : JUnitPluginTrampoline.DEFAULT.getTestedLocation(plugin,
                                                                  currLocation);
        }
        
        /**
         */
        private void goToOppositeLocation() {
            assert oppoLocation != null;
            assert oppoLocation.getFileObject() != null;

            final FileObject oppoFile = oppoLocation.getFileObject();
//            final ElementHandle<Element> elementHandle
//                                         = oppoLocation.getElementHandle();
//            if (elementHandle != null) {
//                OpenTestAction.openFileAtElement(oppoFile, elementHandle);
//            } else {
                OpenTestAction.openFile(oppoFile);
//            }
        }
        
        /**
         */
        private void displayNoOppositeLocationFound() {
            String sourceClsName;
            FileObject fileObj = currLocation.getFileObject();
            sourceClsName = srcClassPath.getResourceName(fileObj, '.', false);
            String msgKey = !fileObj.isFolder()
                            ? "MSG_test_class_not_found"                //NOI18N
                            : (sourceClsName.length() != 0)
                              ? "MSG_testsuite_class_not_found"         //NOI18N
                              : "MSG_testsuite_class_not_found_def_pkg";//NOI18N
            TestUtil.notifyUser(
                    NbBundle.getMessage(getClass(), msgKey, sourceClsName),
                    NotifyDescriptor.INFORMATION_MESSAGE);
        }

    }
    
    /**
     */
    @Override
    public boolean isEnabled() {
        assert EventQueue.isDispatchThread();
        
        return checkDirection() != null;
    }
    
    /**
     */
    public String getName() {
        return NbBundle.getMessage(getClass(),
                                   checkDirection() == Boolean.FALSE
                                        ? "LBL_Action_GoToSource"       //NOI18N
                                        : "LBL_Action_GoToTest");       //NOI18N
    }
    
    /**
     * Checks whether this action should be enabled for &quot;Go To Test&quot;
     * or for &quot;Go To Tested Class&quot or whether it should be disabled.
     * 
     * @return  {@code Boolean.TRUE} if this action should be enabled for
     *          &quot;Go To Test&quot;,<br />
     *          {@code Boolean.FALSE} if this action should be enabled for
     *          &quot;Go To Tested Class&quot;,<br />
     *          {@code null} if this action should be disabled
     */
    private Boolean checkDirection() {
        TopComponent comp = TopComponent.getRegistry().getActivated();
        FileObject fileObj = null;
        
        if (comp instanceof CloneableEditorSupport.Pane) {
            JEditorPane editorPane = ((CloneableEditorSupport.Pane) comp).getEditorPane();
            if (editorPane != null) {
                fileObj = getFileObject(editorPane.getDocument());
            }
        } else {
            Node[] selectedNodes = comp.getActivatedNodes();
            if ((selectedNodes != null) && (selectedNodes.length == 1)) {
                DataObject dataObj = selectedNodes[0].getLookup().lookup(DataObject.class);
                if (dataObj != null) {
                    fileObj = dataObj.getPrimaryFile();
                }
            }
        }
        
        ClassPath srcCP;
        FileObject fileObjRoot;
        
        boolean sourceToTest = true;
        boolean enabled = (fileObj != null)
          && TestUtil.isJavaFile(fileObj)
          && ((srcCP = ClassPath.getClassPath(fileObj, ClassPath.SOURCE)) != null)
          && ((fileObjRoot = srcCP.findOwnerRoot(fileObj)) != null)
          && ((UnitTestForSourceQuery.findUnitTests(fileObjRoot).length != 0)
              || (sourceToTest = false)         //side effect - assignment
              || (UnitTestForSourceQuery.findSources(fileObjRoot).length != 0));
        
        return enabled ? Boolean.valueOf(sourceToTest)
                       : null;
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
    @Override
    protected boolean asynchronous() {
        return false;
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
