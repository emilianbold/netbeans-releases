/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.swingapp;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADProperty;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectNotFoundException;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.beans.Introspector;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.form.FormEditorSupport;
import org.netbeans.modules.form.FormModelEvent;
import org.netbeans.modules.form.FormModelListener;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;

/**
 * The ActionManager is a singleton which tracks all actions throughout the project.
 * It allows other parts of the SwingApp module search for actions, get action properties,
 * access the list of components bound to actions, and create/update/delete actions.
 *
 * There is one ActionManager singleton per project. This singleton can be obtained
 * by passing any file in the project to the static getActionManager() method.
 *
 * 
 * TODOs:   the internal maps are currently never cleared. They should be cleared when the project
 * they are attached to is closed. Perhaps more often.
 * 
 * We need a way to scan when a new class is created or added to the project. We need to do this without
 * rescaning the *entire* project.
 * 
 * 
 * @author joshua.marinacci@sun.com
 */
public class ActionManager {
    
    private static Map<Project,ActionManager> ams;
    
    public static ActionManager getActionManager(FileObject fileInProject) {
        if(ams == null) {
            ams = Collections.synchronizedMap(new HashMap<Project,ActionManager>());
        }
        Project proj = getProject(fileInProject);
        ActionManager am = ams.get(proj);
        if(am == null && canHaveActions(fileInProject)) {
            synchronized(ActionManager.class) {
                if (ams.get(proj) == null) {
                    ClassPath cp = ClassPath.getClassPath(fileInProject, ClassPath.SOURCE);
                    FileObject root = cp.findOwnerRoot(fileInProject);
                    am = new ActionManager(proj, root);
                    ams.put(proj,am);
                }
            }
        }
        return am;
    }
    
    public static ActionManager getActionManager(Project project) {
        Sources srcs = project.getLookup().lookup(Sources.class);
        if(srcs == null) return null;
        SourceGroup groups[] = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if(groups != null && groups.length > 0) {
            return ActionManager.getActionManager(groups[0].getRootFolder());
        } else {
            return null;
        }
    }

    static boolean canHaveActions(FileObject fileInProject) {
        return AppFrameworkSupport.isFrameworkLibAvailable(fileInProject);
    }

    // a map of all actions by classname
    private Map<String,List<ProxyAction>> actions;
    // a list of all actions
    private List<ProxyAction> actionList;
    
    // maps actions (by id) to rad components with action properties set to that action
    private Map<String,List<RADComponent>> boundComponents =
            new HashMap<String,List<RADComponent>>();
    // the property change listeners for monitoring changes to the list of actions
    private List<PropertyChangeListener> pcls;
    // the listener for changes to individual actions (their own properties)
    private List<ActionChangedListener> acls;
    // the root object of this ActionManager's project
    private FileObject root;
    // the ActionManager's project
    private Project project;
    
    public Project getProject() {
        return project;
    }
    
    public FileObject getApplicationClassFile() {
        String appClassName = AppFrameworkSupport.getApplicationClassName(getRoot());
        return (appClassName == null) ? null : getFileForClass(appClassName);
    }
    
    /** Creates a new instance of ActionManager */
    private ActionManager(Project project, FileObject root) {
        this.project = project;
        this.root = root;
        actionList = new ArrayList<ProxyAction>();
        pcls = new ArrayList<PropertyChangeListener>();
        acls = new ArrayList<ActionChangedListener>();
        actions = new HashMap<String, List<ProxyAction>>();
    }
    
    public FileObject getRoot() {
        return root;
    }
    
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        this.pcls.add(pcl);
    }
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        this.pcls.remove(pcl);
    }
    
    public void addActionChangedListener(ActionChangedListener acl) {
        this.acls.add(acl);
    }
    public void removeActionChangedListener(ActionChangedListener acl) {
        this.acls.remove(acl);
    }
    
    public static interface ActionChangedListener {
        public void actionChanged(ProxyAction action);
    }
    
    
    
    /** Rescan the entire project.  This could be slow. Should be optimized
     *  in the future so that we don't need to do the full scan very often
     */
    public void rescan() {
        actions = new HashMap<String,List<ProxyAction>>();
        FileObject root = getRoot();
        if (root != null) {
            scanFolderForActions(root, actions);
        }
        actionList.clear();
        for(String appClsName : actions.keySet()) {
            actionList.addAll(actions.get(appClsName));
        }
        fireStructureChanged();
    }
    
    /** request the specified file be rescanned in the near future. */
    public static synchronized void lazyRescan(FileObject fo) {
        if (scanQueue != null && !scanQueue.contains(fo)) {
            scanQueue.add(fo);
        }
    }

    //the scan queue is traversed every 3 seconds to look for new files that must
    //be scanned. they are removed from the queue after scanning.
    //this ensures a file is never scanned more than once every three seconds
    private static List<FileObject> scanQueue;
    private static Timer rescanTimer;

    private static synchronized void startAutoRescan() {
        if (scanQueue == null) {
            scanQueue = Collections.synchronizedList(new ArrayList<FileObject>());
            rescanTimer = new Timer(3000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Note: this code is called in AWT thread, as well stopAutoRescan.
                    if (scanQueue == null || scanQueue.isEmpty()) {
                        return;
                    }
                    List<FileObject> queue;
                    synchronized(scanQueue) {
                        queue = new ArrayList<FileObject>(scanQueue);
                        scanQueue.clear();
                    }
                    for (FileObject fo : queue) {
                        if (fo == null || !fo.isValid()) { // might have been deleted meanwhile
                            continue;
                        }
                        //clear all of the actions for this file out of the master list
                        String className = AppFrameworkSupport.getClassNameForFile(fo);
                        ActionManager am = ActionManager.getActionManager(fo);
                        Map<String,List<ProxyAction>> actions = am.actions;
                        if (actions.containsKey(className)) {
                            List<ProxyAction> oldActions = actions.get(className);
                            for(ProxyAction oldAct : oldActions) {
                                Iterator<ProxyAction> ait = am.actionList.iterator();
                                while(ait.hasNext()) {
                                    if(actionsMatch(ait.next(),oldAct)) {
                                        ait.remove();
                                    }
                                }
                            }
                            //then remove the actions for this file from the master hashtable
                            actions.remove(className);
                        }
                        //rescans this file and replaces the list of actions for this file
                        getActionsFromFile(fo, actions);
                        if (actions.containsKey(className)) {
                            List<ProxyAction> newActions = actions.get(className);
                            am.actionList.addAll(newActions);
                        }
                        am.fireStructureChanged();
                    }
                }
            });
            rescanTimer.start();
        }
    }

    private static synchronized void stopAutoRescan() {
        if (scanQueue != null) {
            rescanTimer.stop();
            scanQueue = null;
            rescanTimer = null;
        }
    }

    public List<ProxyAction> getAllActions() {
        return actionList;
    }
    
    public Collection<String> getAllClasses() {
        return actions.keySet();
    }
    
    public List<ProxyAction> getActions(String defClass, boolean rescan) {
        if (rescan) {
            getActionsFromFile(getFileForClass(defClass), actions);
        }
        List<ProxyAction> list = actions.get(defClass);
        return list != null ? list : Collections.<ProxyAction>emptyList();
    }
    
    public static List<ProxyAction> getActions(FileObject sourceFile, boolean rescan) {
        ActionManager am = getActionManager(sourceFile);
        if (rescan) {
            getActionsFromFile(sourceFile, am.actions);
        }
        return am.getActions(AppFrameworkSupport.getClassNameForFile(sourceFile), false);
    }

    void jumpToActionSource(ProxyAction action) {
        FileObject sourceFile = getFileForClass(action.getClassname());
        try {
            Integer result = new ActionMethodTask<Integer>(sourceFile, action.getMethodName()) {
                @Override
                Integer run(CompilationController controller, MethodTree methodTree, ExecutableElement methodElement) {
                    return (int) controller.getTrees().getSourcePositions().getStartPosition(
                            controller.getCompilationUnit(), methodTree);
                }
            }.execute();

            int position = result.intValue();
            Line lineObj = null;
            EditorCookie editorCookie = DataObject.find(sourceFile).getCookie(EditorCookie.class);
            if (editorCookie != null) {
                // make sure the document is opened
                if(editorCookie.getDocument() == null) {
                    editorCookie.openDocument();
                }
                // make sure the editor window is open
                editorCookie.open();

                StyledDocument doc = editorCookie.getDocument();
                String sub = doc.getText(position, doc.getLength()-position);
                int i = sub.indexOf('{');
                if (i >= 0) {
                    while (++i < sub.length()) {
                        if (sub.charAt(i) > ' ') {
                            position += i; // first non-white char after opening brace
                            break;
                        }
                    }
                }
            
                Line.Set lineSet = editorCookie.getLineSet();
                int line = doc.getParagraphElement(0).getParentElement().getElementIndex(position);
                lineObj = lineSet.getCurrent(line);
            }

            if (lineObj == null) {
                Toolkit.getDefaultToolkit().beep();
            } else {
                lineObj.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
            }
        } catch (Exception ex) {
            Logger.getLogger(ActionMethodTask.class.getName()).log(Level.INFO, null, ex);
        }
    }

    boolean isExistingMethod(String className, String methodName) {
        FileObject sourceFile = getFileForClass(className);
        try {
            Boolean result = new ActionMethodTask<Boolean>(sourceFile, methodName) {
                @Override
                Boolean run(CompilationController controller, MethodTree methodTree, ExecutableElement methodElement) {
                    return true;
                }
            }.execute();
            return Boolean.TRUE.equals(result);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        }
    }

    /**
     * @return true if the method was successfuly created or already existed
     */
    boolean createActionMethod(final ProxyAction action) {
        if (isExistingMethod(action.getClassname(), action.getMethodName())) {
            return true;
        }

        try {
            final FileObject sourceFile = getFileForClass(action.getClassname());
            final String taskName;
            final String newTaskName;
            if (action.isTaskEnabled()) {
                taskName = taskNameForAction(action);
                newTaskName = getNonExistingTaskName(action.getClassname(), taskName);
            } else {
                taskName = null;
                newTaskName = null;
            }
            JavaSource js = JavaSource.forFileObject(sourceFile);
            ModificationResult result = js.runModificationTask(new CancellableTask<WorkingCopy>() {
                @Override
                public void cancel() {
                }
                @Override
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cut = workingCopy.getCompilationUnit();
                    // get the class tree
                    ClassTree classTree = null;
                    for (Tree t: cut.getTypeDecls()) {
                        if (t.getKind() == Tree.Kind.CLASS) {
                            ClassTree classT = (ClassTree) t;
                            if (sourceFile.getName().equals(classT.getSimpleName().toString())) {
                                classTree = classT;
                                break;
                            }
                        }
                    }
                    if (classTree == null) {
                        return;
                    }
                    // create annotation
                    TreeMaker make = workingCopy.getTreeMaker();
                    AnnotationTree annotation = createAnnotation(action, make, workingCopy);
                    // create new method tree
                    MethodTree newMethod;
                    ModifiersTree methodModifiers = make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PUBLIC),
                        Collections.<AnnotationTree>singletonList(annotation));
                    if (action.isTaskEnabled()) { // method returns a new Task
                        boolean hasAppGetter = AppFrameworkSupport.isViewClass(classTree, workingCopy);
                        newMethod = make.Method(
                                methodModifiers,
                                action.getMethodName(),
                                make.QualIdent(workingCopy.getElements().getTypeElement("org.jdesktop.application.Task")), // NOI18N
                                Collections.<TypeParameterTree>emptyList(),
                                Collections.<VariableTree>emptyList(),
                                Collections.<ExpressionTree>emptyList(),
                                "{\n" + getTaskInstantiationCode(taskName, sourceFile, hasAppGetter) + "}", // NOI18N
                                null);
                    } else {
                        newMethod = make.Method(
                            methodModifiers,
                            action.getMethodName(),
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            Collections.<VariableTree>emptyList(),
                            Collections.<ExpressionTree>emptyList(),
                            make.Block(Collections.<StatementTree>emptyList(), false), // "// put your action code here",
                            null);
                    }
                    // determine where to add the new method (ideally at the end
                    // of class but before the guarded block of field variables)
                    int insertIndex = -1;
                    List<? extends Tree> members = classTree.getMembers();
                    int index = members.size();
                    ListIterator<? extends Tree> it = members.listIterator(index);
                    while (it.hasPrevious()) {
                        Tree t = it.previous();
                        if (t.getKind() == Tree.Kind.METHOD || t.getKind() == Tree.Kind.CLASS) {
                            insertIndex = index;
                            break;
                        }
                        index--;
                    }
                    // add the method tree to the class tree
                    ClassTree modifiedClassTree;
                    if (insertIndex < 0) {
                        modifiedClassTree = make.addClassMember(classTree, newMethod);
                    } else {
                        modifiedClassTree = make.insertClassMember(classTree, insertIndex, newMethod);
                    }
                    workingCopy.rewrite(classTree, modifiedClassTree);
                }
            });
            result.commit();

            if (newTaskName != null) { // create the Task impl class
                DataObject dobj = DataObject.find(sourceFile);
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                if (ec == null) {
                    return false;
                }
                if (ec.getDocument() == null) {
                    ec.openDocument();
                }
                Document doc = ec.getDocument();
                Integer methodEndPosition = new ActionMethodTask<Integer>(sourceFile, action.getMethodName()) {
                    @Override
                    Integer run(CompilationController controller, MethodTree methodTree, ExecutableElement methodElement) {
                        return (int) controller.getTrees().getSourcePositions().getEndPosition(
                                controller.getCompilationUnit(), methodTree);
                    }
                }.execute();
                javax.swing.text.Element docRoot = doc.getDefaultRootElement();
                int pos = docRoot.getElement(docRoot.getElementIndex(methodEndPosition.intValue() + 1))
                        .getStartOffset();

                StringBuilder buf = new StringBuilder();
                buf.append("\n") // NOI18N
                   .append(getTaskClassImplCode(newTaskName, null))
                   .append("\n"); // NOI18N
                doc.insertString(pos, buf.toString(), null);
            }

            //generate selected and enabled properties if they don't already exist
            generateProperties(action, sourceFile);
            
            return true;
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        }
    }

    private static String taskNameForAction(ProxyAction action) {
        String actionName = action.getId();
        return actionName.substring(0, 1).toUpperCase() + actionName.substring(1) + "Task"; // NOI18N
    }

    /**
     * Checks if given task already exists. Returns the task name if not,
     * otherwise null - meaning it is not a task that needs to be created.
     */
    private String getNonExistingTaskName(String className, final String taskName) {
        FileObject sourceFile = getFileForClass(className);
        try {
            String result = new ClassTask<String>(sourceFile) {
                @Override
                String run(CompilationController controller, ClassTree classTree, TypeElement classElement) {
                    for (TypeElement el: ElementFilter.typesIn(classElement.getEnclosedElements())) {
                        if (el.getSimpleName().toString().equals(taskName)) {
                            return null;
                        }
                    }
                    // TODO check if found type is really a Task, find free name
                    return taskName;
                }
            }.execute();
            return result;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return taskName;
        }
    }

    private static AnnotationTree createAnnotation(ProxyAction action, TreeMaker make, WorkingCopy workingCopy) {
        List<AssignmentTree> annAttrs = new LinkedList<AssignmentTree>();
        for (String attrName : ProxyAction.getAnnotationAttributeNames()) {
            if (action.isAnnotationAttributeSet(attrName)) {
                Object value = action.getAnnotationAttributeValue(attrName);
                ExpressionTree expTree;
                if (value instanceof String) {
                    expTree = make.Literal(value);
                } else if (value instanceof ProxyAction.BlockingType) {
                    expTree = make.MemberSelect(
                            make.MemberSelect(make.QualIdent(workingCopy.getElements().getTypeElement("org.jdesktop.application.Task")), // NOI18N
                                              "BlockingScope"), // NOI18N
                            value.toString());
                } else {
                    continue;
                }
                ExpressionTree identTree = make.Identifier(attrName);
                AssignmentTree attrTree = make.Assignment(identTree, expTree);
                annAttrs.add(attrTree);
            }
        }
        return make.Annotation(make.QualIdent(workingCopy.getElements().getTypeElement("org.jdesktop.application.Action")), annAttrs); // NOI18N
    }

    private static String getTaskInstantiationCode(String taskName, FileObject sourceFile, boolean hasAppGetter) {
        return "return new " + taskName + "(" // NOI18N
                + (hasAppGetter ? "getApplication()" : AppFrameworkSupport.getApplicationCode(sourceFile)) // NOI18N
                + ");\n"; // NOI18N
    }

    private static final String TASK_CLASS_TEMPLATE =
            "    private class MyTask extends org.jdesktop.application.Task<Object, Void> {\n" // NOI18N
          + "        MyTask(org.jdesktop.application.Application app) {\n" // NOI18N
          + "            // Runs on the EDT.  Copy GUI state that\n" // NOI18N
          + "            // doInBackground() depends on from parameters\n" // NOI18N
          + "            // to MyTask fields, here.\n" // NOI18N
          + "            super(app);\n" // NOI18N
          + "__CTOR_CODE__" // NOI18N
          + "        }\n" // NOI18N
          + "        @Override protected Object doInBackground() {\n" // NOI18N
          + "            // Your Task's code here.  This method runs\n" // NOI18N
          + "            // on a background thread, so don't reference\n" // NOI18N
          + "            // the Swing GUI from here.\n" // NOI18N
          + "            return null;  // return your result\n" // NOI18N
          + "        }\n" // NOI18N
          + "        @Override protected void succeeded(Object result) {\n" // NOI18N
          + "            // Runs on the EDT.  Update the GUI based on\n" // NOI18N
          + "            // the result computed by doInBackground().\n" // NOI18N
          + "        }\n" // NOI18N
          + "    }\n"; // NOI18N

    private String getTaskClassImplCode(String taskName, String ctorCode) {
        if (ctorCode == null) {
            ctorCode = "";
        }
        if (ctorCode.length() > 0 && !ctorCode.endsWith("\n")) { // NOI18N
            ctorCode = ctorCode + "\n"; // NOI18N
        }
        if (ctorCode.length() > 0) { // provisional indentation, PENDING...
            StringBuilder buf = new StringBuilder();
            String indent = "            "; // NOI18N
            int index = 0;
            boolean lineStart = true;
            for (int i=0; i < ctorCode.length(); i++) {
                char c = ctorCode.charAt(i);
                if (c == '\n') { // NOI18N
                    if (lineStart) {
                        buf.append("\n"); // NOI18N
                    } else {
                        buf.append(ctorCode.substring(index, i+1));
                    }
                    lineStart = true;
                    index = i + 1;
                } else if (c > ' ' && lineStart) {
                    buf.append(indent);
                    lineStart = false;
                    index = i;
                }
            }
            ctorCode = buf.toString();
        }
        return TASK_CLASS_TEMPLATE.replace("__CTOR_CODE__", ctorCode) // NOI18N
                .replace("MyTask", taskName); // NOI18N
    }

    public List<RADComponent> getBoundComponents(ProxyAction act) {
        if(!boundComponents.containsKey(getKey(act))) {
            return new ArrayList<RADComponent>();
        }
        return boundComponents.get(getKey(act));
    }
    
    
    public void removeAllBoundComponents(FormModel model) {
        for(String key : boundComponents.keySet()) {
            List<RADComponent> comps = boundComponents.get(key);
            
            Iterator<RADComponent> it = comps.iterator();
            while(it.hasNext()) {
                RADComponent comp = it.next();
                if(comp != null && comp.getFormModel() == model) {
                    it.remove();
                }
            }
        }
    }
    
    public void addNewAction(ProxyAction act) {
        List<ProxyAction> list = actions.get(act.getClassname());
        if( list == null) {
            list = new ArrayList<ProxyAction>();
            actions.put(act.getClassname(), list);
        }
        list.add(act);
        actionList.add(act);
        fireStructureChanged();
    }
    
    //returns true if the action was found and replaced
    private boolean safeReplace(List<ProxyAction> actions, ProxyAction action) {
        for(int i=0; i<actionList.size(); i++) {
            ProxyAction target = actionList.get(i);
            if(actionsMatch(action, target)) {
                actionList.remove(target);
                actionList.add(i,action);
                return true;
            }
        }
        return false;
    }

    public void updateAction(ProxyAction action) {
        List<ProxyAction> actions = getActions(action.getClassname(), false);
        boolean replaced = false;
        for(ProxyAction a : actions) {
            if(a.getId().equals(action.getId())) {
                //actions.remove(a);
                // do a replace instead of a remove
                int n = actions.indexOf(a);
                if(n >= 0) {
                    actions.remove(n);
                    actions.add(n, action);
                }
                // do a special search remove because remove(a) isn't working
                //safeRemove(actionList,a);
                for(int i=0; i<actionList.size(); i++) {
                    ProxyAction target = actionList.get(i);
                    if(actionsMatch(action, target)) {
                        actionList.remove(target);
                        actionList.add(i,action);
                    }
                }
                replaced = true;
                break;
            }
        }

        //if there is no class file, then it's an action from the framework itself
        if(getFileForClass(action.getClassname()) == null) {
            replaced = safeReplace(actionList,action);
        }
        
        if(!replaced) {
            //don't add to the list of actions for this class if the class is null
            // that probably means this action comes from the framework itself
            if(getFileForClass(action.getClassname()) != null) {
                actions.add(action);
            }
            actionList.add(action);
        }
        
        // check for null in case this is an action in the framework itself
        if(getFileForClass(action.getClassname()) != null) {
                    updateActionMethod(action, getFileForClass(action.getClassname()));
                }
        // this will update the global action table
        //fireStructureChanged();
        fireActionChanged(action); //josh: is this enough of an update?

        // update any form components which use this action, if they are open.
        List<RADComponent> boundList = getBoundComponents(action);
        for(RADComponent comp : boundList) {
            if(comp != null) {
                RADProperty prop = comp.getBeanProperty("action");//NOI18N
                if(prop != null) {
                    try {
                        if (!(prop.getValue() instanceof ProxyAction)) {
                            continue; // Hack to fix issue 112040
                        }
                        //set to null then to the proxy to force an update
                        prop.setValue(null);
                        prop.setValue(action);
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                    } catch (InvocationTargetException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    
    private void updateActionMethod(final ProxyAction action, final FileObject sourceFile) {
        try {
            final String taskName;
            final String newTaskName;
            final String[] oldBodyText;
            if (action.isTaskEnabled()) {
                taskName = taskNameForAction(action);
                newTaskName = getNonExistingTaskName(action.getClassname(), taskName);
                oldBodyText = new String[1];
            } else {
                taskName = null;
                newTaskName = null;
                oldBodyText = null;
            }
            JavaSource js = JavaSource.forFileObject(sourceFile);
            ModificationResult result = js.runModificationTask(new CancellableTask<WorkingCopy>() {
                @Override
                public void cancel() {
                }
                @Override
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cut = workingCopy.getCompilationUnit();
                    // get the class tree
                    ClassTree classTree = null;
                    for (Tree t: cut.getTypeDecls()) {
                        if (t.getKind() == Tree.Kind.CLASS) {
                            ClassTree classT = (ClassTree) t;
                            if (sourceFile.getName().equals(classT.getSimpleName().toString())) {
                                classTree = classT;
                                break;
                            }
                        }
                    }
                    if (classTree == null) {
                        return;
                    }
                    // get the action method tree
                    Trees trees = workingCopy.getTrees();
                    TypeElement classEl = (TypeElement) trees.getElement(trees.getPath(cut, classTree));
                    MethodTree method = null;
                    ExecutableElement methodEl = null;
                    AnnotationTree annotation = null;
                    for (ExecutableElement el : ElementFilter.methodsIn(classEl.getEnclosedElements())) {
                        if (el.getSimpleName().toString().equals(action.getMethodName())
                                && el.getModifiers().contains(Modifier.PUBLIC)) {
                            // method name matches, now check the annotation
                            MethodTree mt = trees.getTree(el);
                            for (AnnotationTree at : mt.getModifiers().getAnnotations()) {
                                TypeElement annEl = (TypeElement) trees.getElement(
                                        trees.getPath(cut, at.getAnnotationType()));
                                if (annEl.getQualifiedName().toString().equals("org.jdesktop.application.Action")) { // NOI18N
                                    annotation = at;
                                    break;
                                }
                            }
                            if (annotation != null) {
                                method = mt;
                                methodEl = el;
                                break;
                            }
                        }
                    }
                    if (method == null) {
                        return;
                    }
                    TreeMaker make = workingCopy.getTreeMaker();
                    // update annotation
                    AnnotationTree newAnnotation = createAnnotation(action, make, workingCopy);
                    workingCopy.rewrite(annotation, newAnnotation);
                    // update the method return type (task)
                    if (isAsyncActionMethod(methodEl) != action.isTaskEnabled()) {
                        MethodTree newMethod;
                        BlockTree body = method.getBody();
                        SourcePositions sp = trees.getSourcePositions();
                        int start = (int) sp.getStartPosition(cut, body);
                        int end = (int) sp.getEndPosition(cut, body);
                        String bodyText = getMethodBodyWithoutBraces(workingCopy.getText().substring(start, end));
                        if (action.isTaskEnabled()) { // switch to Task
                            if (newTaskName != null) {
                                oldBodyText[0] = bodyText;
                                bodyText = ""; // NOI18N
                            } else {
                                bodyText = getCommentedBodyText(bodyText);
                            }
                            boolean hasAppGetter = AppFrameworkSupport.isViewClass(classTree, workingCopy);
                            newMethod = make.Method(
                                    method.getModifiers(),
                                    method.getName(),
                                    make.QualIdent(workingCopy.getElements().getTypeElement("org.jdesktop.application.Task")), // NOI18N
                                    method.getTypeParameters(),
                                    method.getParameters(),
                                    method.getThrows(),
                                    "{\n" + getTaskInstantiationCode(taskName, sourceFile, hasAppGetter) + bodyText + "}", // NOI18N
                                    null);
                        } else { // switch to void
                            newMethod = make.Method(
                                    method.getModifiers(),
                                    method.getName(),
                                    make.PrimitiveType(TypeKind.VOID),
                                    method.getTypeParameters(),
                                    method.getParameters(),
                                    method.getThrows(),
                                    "{\n" + getCommentedBodyText(bodyText) + "}", // NOI18N
                                    null);
                        }
                        workingCopy.rewrite(method, newMethod);
                    }
                }
            });
            result.commit();

            DataObject dobj = DataObject.find(sourceFile);
            EditorCookie ec = dobj.getCookie(EditorCookie.class);
            if (ec == null) {
                return;
            }
            // make sure it's open before we access it
            if (ec.getDocument() == null) {
                ec.openDocument();
            }
            Document doc = ec.getDocument();

            // generate Task impl class if does not already exist
            if (newTaskName != null) {
                Integer methodEndPosition = new ActionMethodTask<Integer>(sourceFile, action.getMethodName()) {
                    @Override
                    Integer run(CompilationController controller, MethodTree methodTree, ExecutableElement methodElement) {
                        return (int) controller.getTrees().getSourcePositions().getEndPosition(
                                controller.getCompilationUnit(), methodTree);
                    }
                }.execute();
                javax.swing.text.Element docRoot = doc.getDefaultRootElement();
                int pos = docRoot.getElement(docRoot.getElementIndex(methodEndPosition.intValue()) + 1)
                        .getStartOffset();
                doc.insertString(pos,
                                 "\n" + getTaskClassImplCode(newTaskName, oldBodyText[0]), // NOI18N
                                 null);
            }

            //generate selected and enabled properties if they don't already exist
            generateProperties(action, sourceFile);
            
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    private void generateProperties(ProxyAction action, FileObject sourceFile) {
        if(action.isAnnotationAttributeSet("enabledProperty")){ // NOI18N
            String enabledProperty = (String) action.getAnnotationAttributeValue("enabledProperty"); // NOI18N
            if(!findBooleanProperty(enabledProperty, sourceFile)) {
                generatePropertyGetterAndSetter(enabledProperty,sourceFile);
            }
        }
        if(action.isAnnotationAttributeSet("selectedProperty")){ // NOI18N
            String enabledProperty = (String) action.getAnnotationAttributeValue("selectedProperty"); // NOI18N
            if(!findBooleanProperty(enabledProperty, sourceFile)) {
                generatePropertyGetterAndSetter(enabledProperty,sourceFile);
            }
        }
    }
    
    private static boolean findBooleanProperty(String enabledProperty, FileObject sourceFile) {
        List<String> props = findBooleanProperties(sourceFile);
        for(String prop : props) {
            if(prop.equals(enabledProperty)) {
                return true;
            }
        }
        return false;
    }

    //this code should switch to the propery java code generation infrastructure in the future
    private static boolean generatePropertyGetterAndSetter(String propertyName, FileObject sourceFile) {
        try {
            DataObject dobj = DataObject.find(sourceFile);
            EditorCookie ec = dobj.getCookie(EditorCookie.class);
            if (ec == null) {
                return false;
            }
            if(ec.getDocument() == null) {
                ec.openDocument();
            }
            //ec.open(); //josh: we fail if the document isn't opened yet. is there a better way to do this?
            Document doc = ec.getDocument();
            int pos;
            if (ec instanceof FormEditorSupport) {
                // in form's source add before the variables section
                doc = ec.getDocument();
                pos = ((FormEditorSupport)ec).getVariablesSection().getStartPosition().getOffset();
            } else {
                // in general java source add at the end of the class
                Integer result = new ClassTask<Integer>(sourceFile) {
                    @Override
                    Integer run(CompilationController controller, ClassTree classTree, TypeElement classElement) {
                        return (int) controller.getTrees().getSourcePositions().getEndPosition(
                                controller.getCompilationUnit(), classTree);
                    }
                }.execute();
                javax.swing.text.Element docRoot = doc.getDefaultRootElement();
                pos = docRoot.getElement(docRoot.getElementIndex(result.intValue()))
                        .getStartOffset();
            }
            
            String code = getPropertyGetterAndSetterBodyText(propertyName);
            doc.insertString(pos, code.toString(), null);
            return true;
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        }        
    }
    
    private static String getPropertyGetterAndSetterBodyText(String prop) {
        StringBuilder buf = new StringBuilder();
        String indent = "    "; // NOI18N
        String getterName = "is"+prop.substring(0,1).toUpperCase() + prop.substring(1); // NOI18N
        String setterName = "set"+prop.substring(0,1).toUpperCase() + prop.substring(1); // NOI18N
        
        buf.append(indent).append("private boolean ").append(prop).append(" = false;\n"); // NOI18N
        buf.append(indent).append("public boolean ").append(getterName).append("() {\n"); // NOI18N
        buf.append(indent).append(indent).append("return ").append(prop).append(";\n"); // NOI18N
        buf.append(indent).append("}\n"); // NOI18N
        buf.append("\n"); // NOI18N
        buf.append(indent).append("public void ").append(setterName).append("(boolean b) {\n"); // NOI18N
        buf.append(indent).append(indent).append("boolean old = ").append(getterName).append("();\n"); // NOI18N
        buf.append(indent).append(indent).append("this.").append(prop).append(" = b;\n"); // NOI18N
        buf.append(indent).append(indent).append("firePropertyChange(\"").append(prop).append("\", old, ").append(getterName).append("());\n"); // NOI18N
        buf.append(indent).append("}\n\n"); // NOI18N
        return buf.toString();
    }
    
    private static String getCommentedBodyText(String bodyText) {
        bodyText = getMethodBodyWithoutBraces(bodyText);
        StringBuilder buf = new StringBuilder();
        int lineStart = 0;
        for (int i=0; i < bodyText.length(); i++) {
            char c = bodyText.charAt(i);
            if (c == '\n' || i+1 == bodyText.length()) {
                buf.append("// "); // NOI18N
                buf.append(bodyText.substring(lineStart, i+1));
                lineStart = i + 1;
            }
        }
        return buf.toString();
    }

    private static String getMethodBodyWithoutBraces(String bodyText) {
        int first = -1;
        int last = -1;
        for (int i=0; i < bodyText.length(); i++) {
            char c = bodyText.charAt(i);
            if (c > ' ' && (c != '{' || first >= 0)) {
                break;
            } else if (c == '\n' && first >= 0) {
                first = i + 1;
                break;
            } else if (c == '{') {
                first = i + 1;
            }
        }
        for (int i=bodyText.length()-1; i >= 0 ; i--) {
            char c = bodyText.charAt(i);
            if (c > ' ' && (c != '}' || last >= 0)) {
                break;
            } else if (c == '\n' && first >= 0) {
                last = i + 1;
                break;
            } else if (c == '}') {
                last = i;
            }
        }
        return bodyText.substring(first >= 0 ? first : 0, last >= 0 ? last : bodyText.length());
    }

    private static int[] getAnnotationPositions(ProxyAction action, FileObject sourceFile) throws IOException {
        return new ActionMethodTask<int[]>(sourceFile, action.getMethodName()) {
            @Override
            int[] run(CompilationController controller, MethodTree methodTree, ExecutableElement methodElement) {
                CompilationUnitTree cut = controller.getCompilationUnit();
                Trees trees = controller.getTrees();
                ModifiersTree modifiers = methodTree.getModifiers();
                for (AnnotationTree at : modifiers.getAnnotations()) {
                    TypeElement annEl = (TypeElement) trees.getElement(
                            trees.getPath(cut, at.getAnnotationType()));
                    if (annEl.getQualifiedName().toString().equals("org.jdesktop.application.Action")) { // NOI18N
                        SourcePositions positions = trees.getSourcePositions();
                        return new int[] {
                                (int) positions.getStartPosition(cut, at),
                                (int) positions.getEndPosition(cut, at)
                        };
                    }
                }
                return null;
            }
        }.execute();
    }

    public void deleteAction(ProxyAction action) {
        String defClass = action.getClassname();
        FileObject file = getFileForClass(defClass);
        DesignResourceMap map = ResourceUtils.getDesignResourceMap(file, true);
        //String actionKey = action.getId()+".Action";
        //ResourceValueImpl res = map.getResourceValue(actionKey+".text",String.class);
        
        // delete the resources
        Collection<String> col = map.collectKeys(action.getId()+"\\..*",true); //NOI18N
        for(String s : col) {
            ResourceValueImpl res = map.getResourceValue(s,String.class);
            if(res != null) {
                map.removeResourceValue(res);
            }
        }
        
        // remove from main map
        Iterator<ProxyAction> it = getActions(defClass, false).iterator();
        while (it.hasNext()) {
            ProxyAction a = it.next();
            if (a.getId().equals(action.getId())) {
                it.remove();
                
                //actionList.remove(a); // use this safer remove 
                Iterator<ProxyAction> it2 = actionList.iterator();
                while(it2.hasNext()) {
                    ProxyAction pact = it2.next();
                    if(actionsMatch(pact, a)) {
                        it2.remove();
                    }
                }
                break;
            }
        }
        
        // delete actions from the form
        // only works if the action is stored in the form it's used.
        // must search all forms in the future
        if(hasFormFile(file)) {
            FileObject formfile = getFormFile(file);
            if(formfile.canRead()) {
                try {
                    FormModel mod = getFormModel(formfile);
                    deleteAction(action, mod);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        // remove the annotation
        deleteActionAnnotation(action, file);
        
        // comment out the action code.
        // josh: we can't comment out code yet.
        // josh: We also don't wany to delete the method either. leave alone for now.
        //AppFrameworkSupport.deleteMethod(classDef,action);
        
    }
    
    private static void deleteActionAnnotation(ProxyAction action, FileObject sourceFile) {
        try {
            int[] positions = getAnnotationPositions(action, sourceFile);
            if (positions == null) {
                return;
            }
            DataObject dobj = DataObject.find(sourceFile);
            EditorCookie ec = dobj.getCookie(EditorCookie.class);
            if (ec == null) {
                return;
            }
            Document doc = ec.getDocument();
            int startPos = positions[0];
            int endPos = positions[1];
            String annotationText = doc.getText(startPos, endPos-startPos);
            javax.swing.text.Element docRoot = doc.getDefaultRootElement();
            javax.swing.text.Element line = docRoot.getElement(docRoot.getElementIndex(startPos));
            int lineStart = line.getStartOffset();
            int lineEnd = line.getEndOffset();
            if (doc.getText(lineStart, lineEnd-lineStart).trim().equals(annotationText)) {
                // annotation is on separate line - remove the whole line
                startPos = lineStart;
                endPos = lineEnd;
            }
            doc.remove(startPos, endPos-startPos);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    /** attach a RAD component to the specified action. This will
     * trigger an update to any listeners. */
    public void addRADComponent(ProxyAction act, RADComponent comp) {
        if(comp == null) return;
        if(!boundComponents.containsKey(getKey(act))) {
            boundComponents.put(getKey(act),new ArrayList<RADComponent>());
        }
        if(boundComponents.get(getKey(act)).contains(comp)) {
            return;
        }
        boundComponents.get(getKey(act)).add(comp);
        fireActionChanged(act);
    }
    
    /** un-attach a RAD component from the specified action. This will trigger
     * an update to any listeners. */
    void removeRADComponent(ProxyAction act, RADComponent radComponent) {
        if(boundComponents.containsKey(getKey(act))) {
            boundComponents.get(getKey(act)).remove(radComponent);
        }
        fireActionChanged(act);
    }
    
    private static Project getProject(final FileObject fileInProject) {
        Project project = FileOwnerQuery.getOwner(fileInProject);
        return project;
    }
    
    private static void scanFolderForActions(FileObject folder,
            Map<String, List<ProxyAction>> classNameToActions) {
        for (FileObject fo : folder.getChildren()) {
            if (fo.isFolder()) { // dive into subfolders after scanning files
                scanFolderForActions(fo, classNameToActions);
            } else if (fo.getExt().equalsIgnoreCase("java")) { // NOI18N
                getActionsFromFile(fo, classNameToActions);
            }
        }
    }
    
    private static void getActionsFromFile(FileObject sourceFile,
            Map<String, List<ProxyAction>> classNameToActions) {
        if (sourceFile == null) {
            return;
        }
        try {
            List<ProxyAction> result = new ClassTask<List<ProxyAction>>(sourceFile) {
                @Override
                List<ProxyAction> run(CompilationController controller, ClassTree classTree, TypeElement classElement) {
                    // collect the superclasses (e.g. to get actions also from the base Application class)
                    List<TypeElement> classList = new LinkedList<TypeElement>();
                    Tree superT = classTree.getExtendsClause();
                    if (superT != null) {
                        TreePath superTPath = controller.getTrees().getPath(controller.getCompilationUnit(), superT);
                        Element superEl = controller.getTrees().getElement(superTPath);
                        while (superEl != null && superEl.getKind() == ElementKind.CLASS) {
                            TypeElement superClassEl = (TypeElement) superEl;
                            classList.add(0, superClassEl);
                            TypeMirror superType = superClassEl.getSuperclass();
                            superEl = (superType.getKind() == TypeKind.DECLARED)
                                ? ((DeclaredType)superType).asElement() : null;
                        }
                    }
                    classList.add(classElement);

                    // go through the classes and look for annotated methods
                    List<ProxyAction> list = null;
                    for (TypeElement cls : classList) {
                        for (ExecutableElement el : ElementFilter.methodsIn(cls.getEnclosedElements())) {
                            if (el.getModifiers().contains(Modifier.PUBLIC)) {
                                org.jdesktop.application.Action ann = el.getAnnotation(org.jdesktop.application.Action.class);
                                if (ann != null) {
                                    String name = el.getSimpleName().toString();
                                    //the annotation can override the name
                                    if(ann.name() != null && !"".equals(ann.name())) {
                                        name = ann.name();
                                    }
                                    ProxyAction action = new ProxyAction(cls.getQualifiedName().toString(),
                                            name,
                                            el.getSimpleName().toString());
                                    initActionFromAnnotation(action, el, ann);
                                    if (list == null) {
                                        list = new ArrayList<ProxyAction>();
                                    }
                                    list.add(action);
                                }
                            }
                        }
                    }
                    return list;
                }
            }.execute();
            
            // issue #155337 - getDesignResourceMap() returns null rarely ...
            DesignResourceMap resourceMap =
                    ResourceUtils.getDesignResourceMap(sourceFile, true);

            if (result != null && !result.isEmpty() && resourceMap != null) {
                // remember the actions, load resources
                String className = AppFrameworkSupport.getClassNameForFile(sourceFile);
                classNameToActions.put(className, result);
                for (ProxyAction action : result) {
                    action.setResourceMap(resourceMap);
                    action.loadFromResourceMap();
                }
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        startAutoRescan();
    }

    static List<String> findBooleanProperties(FileObject fo) {
        try {
            return new ClassTask<List<String>>(fo) {
                @Override
                List<String> run(CompilationController controller, ClassTree classTree, TypeElement classElement) {
                    List<String> props = new java.util.ArrayList<String>();
                    // loop through the methods in this class
                    for(ExecutableElement el : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
                        if(el.getModifiers().contains(Modifier.PUBLIC)) {
                            if(TypeKind.BOOLEAN.equals(el.getReturnType().getKind())) {
                                String name = el.getSimpleName().toString();
                                if(name.startsWith("is") && name.length()>2) { // NOI18N
                                    props.add(Introspector.decapitalize(name.substring(2)));
                                }
                            }
                        }
                    }
                    return props;
                }
            }.execute();
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return new ArrayList<String>();
        }
    }
    
    static void initActionFromSource(final ProxyAction action, FileObject sourceFile) {
        try {
            new ActionMethodTask<Object>(sourceFile, action.getMethodName()) {
                @Override
                Object run(CompilationController controller, MethodTree methodTree, ExecutableElement methodElement) {
                    org.jdesktop.application.Action ann = methodElement.getAnnotation(org.jdesktop.application.Action.class);
                    if (ann != null) {
                        initActionFromAnnotation(action, methodElement, ann);
                    }
                    return null;
                }
            }.execute();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    static void initActionFromAnnotation(ProxyAction action, ExecutableElement methodElement, org.jdesktop.application.Action annotation) {
        boolean returnsTask = isAsyncActionMethod(methodElement);
        action.setTaskEnabled(returnsTask);
        action.setEnabledName(annotation.enabledProperty());
        action.setSelectedName(annotation.selectedProperty());
        action.setBlockingType(ProxyAction.BlockingType.valueOf(annotation.block().toString()));
        // TBD 'name' attr
    }

    private static boolean isAsyncActionMethod(ExecutableElement methodElement) {
        TypeMirror retType = methodElement.getReturnType();
        return (retType.getKind() != TypeKind.VOID);
        // [TODO we need a precise way to determine that a Task or its subclass is returned]
        //        boolean returnsTask = false;
        //        if (retType.getKind() == TypeKind.DECLARED) {
        //            Element retEl = ((DeclaredType)retType).asElement();
        //            if (retEl.getKind() == ElementKind.CLASS
        //                    && "org.jdesktop.application.Task".equals(((TypeElement)retEl).getQualifiedName())) { // NOI18N
        //                returnsTask = true; // [does not cover if Task implementation is used as return type]
        //            }
        //        }
    }

    private void deleteAction(final ProxyAction action, final FormModel mod) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException {
        // remove the entry from the form file
        List<RADComponent> comps = mod.getComponentList();
        for(RADComponent comp : comps) {
            RADProperty prop = comp.getBeanProperty("action");//NOI18N
            if(prop != null) {
                ProxyAction pact = (ProxyAction) prop.getValue();
                if(actionsMatch(pact,action)) {
                    prop.setValue(null);
                }
            }
        }
    }
    
    FileObject getFileForClass(String className) {
        return AppFrameworkSupport.getFileForClass(getRoot(), className);
    }
    
    private FormModel getFormModel(final FileObject formfile) throws DataObjectNotFoundException {
        FormDataObject obj = (FormDataObject) FormDataObject.find(formfile);
        if(!obj.getFormEditor().isOpened()) {
            obj.getFormEditor().loadForm();
        }
        FormModel mod = obj.getFormEditor().getFormModel();
        return mod;
    }
    
    private boolean hasFormFile(FileObject file) {
        if(file.existsExt("form")) {//NOI18N
            return true;
        } else {
            return false;
        }
    }
    
    private FileObject getFormFile(FileObject javaFile) {
        return javaFile.getParent().getFileObject(javaFile.getName()+".form"); // NOI18N
    }
    
    public static boolean actionsMatch(ProxyAction pact, ProxyAction action) {
        if(pact == null || action == null) {
            return false;
        }
        if(pact.getId().equals(action.getId())) {
            if(pact.getClassname().equals(action.getClassname())) {
                return true;
            }
        }
        return false;
    }
    
    
    
    private String getKey(ProxyAction act) {
        if(act == null) { return "null"; } // NOI18N
        String s = (act.getId()+":"+act.getClassname()).intern(); //NOI18N
        return s;
    }
    
    
    private void fireStructureChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PropertyChangeEvent pce = new PropertyChangeEvent(ActionManager.this,"allActions",null,actions);//NOI18N
                for(PropertyChangeListener pcl : pcls) {
                    pcl.propertyChange(pce);
                }
            }
        });
    }
    
    // hack: make it change just the action
    private void fireActionChanged(final ProxyAction act) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for(ActionChangedListener acl : acls) {
                    acl.actionChanged(act);
                }
            }
        });
    }
    
    // -----
    // helper classes for java source analysis tasks
    
    /**
     * Task for analysing structure of class of give source file.
     */
    abstract static class ClassTask<T> implements CancellableTask<CompilationController> {
        FileObject sourceFile;
        
        private T result;
        
        ClassTask(FileObject sourceFile) {
            this.sourceFile = sourceFile;
        }
        
        T execute() throws IOException {
            JavaSource.forFileObject(sourceFile).runUserActionTask(this, true);
            return result;
        }
        
        abstract T run(CompilationController controller, ClassTree classTree, TypeElement classElement);
        
        // CancellableTask
        @Override
        public void cancel() {
        }
        
        // CancellableTask
        @Override
        public void run(CompilationController controller) throws Exception {
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            for (Tree t: controller.getCompilationUnit().getTypeDecls()) {
                if (t.getKind() == Tree.Kind.CLASS) {
                    ClassTree classT = (ClassTree) t;
                    if (sourceFile.getName().equals(classT.getSimpleName().toString())) {
                        TreePath classTPath = controller.getTrees().getPath(controller.getCompilationUnit(), classT);
                        TypeElement classEl = (TypeElement) controller.getTrees().getElement(classTPath);
                        result = run(controller, classT, classEl);
                        return;
                    }
                }
            }
        }
    }
    
    /**
     * Task for analysing an action method of given source file and method name.
     */
    abstract static class ActionMethodTask<T> extends ClassTask<T> {
        String methodName;
        
        ActionMethodTask(FileObject sourceFile, String methodName) {
            super(sourceFile);
            this.methodName = methodName;
        }
        
        @Override
        T run(CompilationController controller, ClassTree classTree, TypeElement classElement) {
            for (ExecutableElement el : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
                if (el.getSimpleName().toString().equals(methodName)
                        && el.getModifiers().contains(Modifier.PUBLIC)) {
                    MethodTree mTree = controller.getTrees().getTree(el);
                    return run(controller, mTree, el);
                }
            }
            return null;
        }
        
        abstract T run(CompilationController controller, MethodTree methodTree, ExecutableElement methodElement);
    }
    
    
    private static Set<FormModel> registeredForms = new HashSet<FormModel>();
    
    public static void registerFormModel(final FormModel formModel, final FileObject sourceFile) {
        if(formModel == null) return;
        if(sourceFile == null) return;
        
        if(registeredForms.contains(formModel)) {
            return;
        }
        
        formModel.addFormModelListener(new FormModelListener() {
            @Override
            public void formChanged(FormModelEvent[] events) {
                if(events != null) {
                    for(FormModelEvent e : events) {
                        if(e.getChangeType() == FormModelEvent.FORM_TO_BE_CLOSED) {
                            ActionManager am = ActionManager.getActionManager(sourceFile);
                            if(am != null) {
                                am.removeAllBoundComponents(e.getFormModel());
                            }
                            final FormModelListener ths = this;
                            final FormModel mod = e.getFormModel();
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    mod.removeFormModelListener(ths);
                                    registeredForms.remove(mod);
                                    if (registeredForms.isEmpty()) {
                                        stopAutoRescan();
                                        ams.clear();
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
        registeredForms.add(formModel);
    }

    static boolean anyFormOpened() {
        return registeredForms != null && !registeredForms.isEmpty();
    }
}
