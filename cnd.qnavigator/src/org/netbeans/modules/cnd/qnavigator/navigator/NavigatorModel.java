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

package org.netbeans.modules.cnd.qnavigator.navigator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.Timer;
import javax.swing.text.Caret;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.loaders.CppEditorSupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Alexander Simon
 */
public class NavigatorModel implements CsmProgressListener, CsmModelListener {
 
    private static final int DEFAULT_PARSING_DELAY = 2000;

    private DataObject cdo;
    private NavigatorPanelUI ui;
    private NavigatorComponent busyListener;
    private Action[] actions;
    private AbstractNode root;
    
    private CsmFileModel fileModel;
    private Timer checkModifiedTimer;
    private long lastModified = -1;
    private Timer checkCursorTimer;
    private long lastCursorPos = -1;
    private long lastCursorPosWhenChecked = 0;
    private final Object lock = new String("NavigatorModel lock"); // NOI18N
    
    public NavigatorModel(DataObject cdo, NavigatorPanelUI ui, NavigatorComponent component) {
        this.cdo = cdo;
        this.ui = ui;
        actions = new Action[]{
            new ShowForwardFunctionDeclarationsAction(),
            new ShowMacroAction(),
            new ShowIncludeAction(),
            new ShowTypedefAction(),
            new ShowVariableAction(),
            new ShowUsingAction()
                              };
        root = new AbstractNode(new Children.Array()) {
            @Override
            public Action[] getActions(boolean context) {
                return actions;
            }
        };
        fileModel = new CsmFileModel(new CsmFileFilter(), actions);
        update(getCsmFile());
        if (getParsingDelay() > 0) {
            checkModifiedTimer = new Timer(getParsingDelay(), new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    checkModified();
                }
            });
            checkModifiedTimer.start();
        }
        checkCursorTimer = new Timer(250, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                checkCursor();
            }
        });
        checkCursorTimer.start();
    }
    
    private int getParsingDelay(){
        //return CppSettings.getDefault().getParsingDelay();
        return DEFAULT_PARSING_DELAY;
    }
    
    private CsmFile getCsmFile() {
        CsmFile csmFile = CsmUtilities.getCsmFile(cdo, false);
        if (csmFile != null && !csmFile.isValid()) {
            csmFile = null;
        }
        return csmFile;
    }
      
    
    void removeNotify() {
        stopTimers();
    }
    
    void addNotify() {
        update(getCsmFile());
    }
    
    void addBusyListener(NavigatorComponent navigatorComponent) {
        busyListener = navigatorComponent;
    }
    
    void removeBusyListener(NavigatorComponent navigatorComponent) {
        busyListener = null;
    }
    
    public Node getRoot(){
        return root;
    }
    
    private void update(final CsmFile csmFile) {
        try {
            if (busyListener != null) {
                busyListener.busyStart();
            }
            System.out.println("CppNavigator update:"+csmFile);
            synchronized(lock) {
                if (fileModel.setFile(csmFile)){
                    final Children children = root.getChildren();
                    if (!Children.MUTEX.isReadAccess()){
                         Children.MUTEX.writeAccess(new Runnable(){
                            public void run() {
                                children.remove(children.getNodes());
                                children.add(fileModel.getNodes());
                            }
                        });
                    }
                }
            }
        } finally {
            if (busyListener != null) {
                busyListener.busyEnd();
            }
        }
        ui.newContentReady();
    }
    
    private void checkModified() {
        stopTimers();
        updateNodesIfModified(getCppEditorSupport(), findCurrentJEditorPane());
        restartTimers();
    }
    
    private void updateNodesIfModified(CppEditorSupport cppEditorSupport, JEditorPane jEditorPane) {
        if (jEditorPane == null || cppEditorSupport.getLastModified() <= lastModified) {
            return;
        }
        long timeSinceLastModification = System.currentTimeMillis() - cppEditorSupport.getLastModified();
        if (timeSinceLastModification < getParsingDelay()){
            return;
        }
        lastModified = cppEditorSupport.getLastModified();
        lastCursorPos = -1;
        lastCursorPosWhenChecked = 0;
    }
    
    private void checkCursor() {
        if (checkCursorTimer != null) {
            checkCursorTimer.stop();
        }
        JEditorPane jEditorPane = findCurrentJEditorPane();
        if (jEditorPane != null) {
            Caret caret = jEditorPane.getCaret();
            if (caret.getDot() != lastCursorPos && caret.getDot() == lastCursorPosWhenChecked) {
                lastCursorPos = caret.getDot();
                lastCursorPosWhenChecked = lastCursorPos;
                setSelection(lastCursorPos);
            }
            lastCursorPosWhenChecked = caret.getDot();
        }
        if (checkCursorTimer != null) {
            checkCursorTimer.restart();
        }
    }
    
    private void setSelection(long caretLineNo) {
        synchronized(lock) {
            Node node = fileModel.setSelection(caretLineNo);
            if (node != null) {
                ui.selectNode(node);
            }
        }
    }
    
    private JEditorPane findCurrentJEditorPane() {
        JEditorPane currentJEditorPane = null;
        CppEditorSupport support = getCppEditorSupport();
        if (support != null) {
            JEditorPane[] jEditorPanes = support.getOpenedPanes();
            if (jEditorPanes == null)
                return null;
            if (jEditorPanes.length >= 1) {
                currentJEditorPane = jEditorPanes[0];
            }
        }
        return currentJEditorPane;
    }
    
    private CppEditorSupport getCppEditorSupport(){
        if (cdo != null) {
            return cdo.getCookie(CppEditorSupport.class);
        }
        return null;
    }
    
    private void restartTimers() {
        if (checkModifiedTimer != null) {
            checkModifiedTimer.restart();
        }
        if (checkCursorTimer != null) {
            checkCursorTimer.restart();
        }
    }
    
    private void stopTimers() {
        if (checkModifiedTimer != null) {
            checkModifiedTimer.stop();
        }
        if (checkCursorTimer != null) {
            checkCursorTimer.stop();
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
    
    public void projectLoaded(CsmProject project) {
	CsmFile file = getCsmFile();
	if( file != null && project.equals(file.getProject()) ) {
	    if( file.isParsed() ) {
		fileParsedOrProjectLoaded(file); 
	    }
	}
    }    
    
    public void fileParsingFinished(CsmFile file) {
        if (file.equals(getCsmFile())) {
	    fileParsedOrProjectLoaded(file);
        }
    }
    
    private void fileParsedOrProjectLoaded(CsmFile file) {
	stopTimers();
	update(file);
	restartTimers();
    }
    
    public void parserIdle() {
    }
    
    public void projectOpened(CsmProject project) {
    }
    
    public void projectClosed(CsmProject project) {
        CsmFile file = getCsmFile();
        if (file == null || file.getProject() == project) {
            stopTimers();
            update(null);
            restartTimers();
        } else {
            stopTimers();
            update(file);
            restartTimers();
        }
    }
    
    public void modelChanged(CsmChangeEvent e) {
        if (e.getRemovedFiles().size()>0) {
            CsmFile file = getCsmFile();
            if (file == null || e.getRemovedFiles().contains(file)){
                stopTimers();
                update(null);
                restartTimers();
            }
        }
    }

    private class ShowForwardFunctionDeclarationsAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public ShowForwardFunctionDeclarationsAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowForwardFunctionDeclarationsText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME)); 
            menuItem.setAction(this);
        }
 
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowForwardFunctionDeclarations(!fileModel.getFilter().isShowForwardFunctionDeclarations());
            update(getCsmFile());
        }

        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowForwardFunctionDeclarations());
            return menuItem;
        }
    }

    private class ShowMacroAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public ShowMacroAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowMacroText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME)); 
            menuItem.setAction(this);
        }
 
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowMacro(!fileModel.getFilter().isShowMacro());
            update(getCsmFile());
        }

        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowMacro());
            return menuItem;
        }
    }

    private class ShowIncludeAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public ShowIncludeAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowIncludeText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME)); 
            menuItem.setAction(this);
        }
 
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowInclude(!fileModel.getFilter().isShowInclude());
            update(getCsmFile());
        }

        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowInclude());
            return menuItem;
        }
    }

    private class ShowTypedefAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public ShowTypedefAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowTypedefText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME)); 
            menuItem.setAction(this);
        }
 
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowTypedef(!fileModel.getFilter().isShowTypedef());
            update(getCsmFile());
        }

        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowTypedef());
            return menuItem;
        }
    }
    
    private class ShowVariableAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public ShowVariableAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowVariableText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME)); 
            menuItem.setAction(this);
        }
 
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowVariable(!fileModel.getFilter().isShowVariable());
            update(getCsmFile());
        }

        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowVariable());
            return menuItem;
        }
    }
    private class ShowUsingAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public ShowUsingAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowUsingText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME)); 
            menuItem.setAction(this);
        }
 
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowUsing(!fileModel.getFilter().isShowUsing());
            update(getCsmFile());
        }

        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowUsing());
            return menuItem;
        }
    }
}
