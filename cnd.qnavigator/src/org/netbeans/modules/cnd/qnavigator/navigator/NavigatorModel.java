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

package org.netbeans.modules.cnd.qnavigator.navigator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.Timer;
import javax.swing.text.Caret;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.qnavigator.navigator.CsmFileFilter.SortMode;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.ImageUtilities;
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
    private long lastModified = 0;
    private long lastDocVersion = 0;
    private Timer checkCursorTimer;
    private long lastCursorPos = -1;
    private long lastCursorPosWhenChecked = 0;
    private final static class Lock {}
    private final Object lock = new Lock();

    public NavigatorModel(DataObject cdo, NavigatorPanelUI ui, NavigatorComponent component, String mimeType) {
        this.cdo = cdo;
        this.ui = ui;
        actions = new Action[]{
            new SortByNameAction(),
            new SortBySourceAction(),
            new GroupByKindAction(),
            new ExpandAllAction(),
            null,
            new FilterSubmenuAction(mimeType),
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
                @Override
                public void actionPerformed(ActionEvent evt) {
                    checkModified();
                }
            });
            checkModifiedTimer.start();
        }
        checkCursorTimer = new Timer(250, new ActionListener() {
            @Override
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
        CsmFile csmFile = CsmUtilities.getCsmFile(cdo, false, false);
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
        update(csmFile, false);
    }

    private void update(final CsmFile csmFile, boolean force) {
        try {
            if (CsmModelAccessor.getModelState() != CsmModelState.ON) {
                return;
            }
            if (busyListener != null) {
                busyListener.busyStart();
            }
            synchronized(lock) {
                if (fileModel.setFile(csmFile, force)){
                    final Children children = root.getChildren();
                    if (!Children.MUTEX.isReadAccess()){
                         Children.MUTEX.writeAccess(new Runnable(){
                            @Override
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
        updateNodesIfModified(findCurrentJEditorPane());
        restartTimers();
    }

    private boolean isUpdateNeeded(JEditorPane jEditorPane) {
        if (jEditorPane == null || jEditorPane.getDocument() == null) {
            return false;
        }
        if (DocumentUtilities.getDocumentVersion(jEditorPane.getDocument()) <= lastDocVersion) {
            return false;
        }
        long timeSinceLastModification = System.currentTimeMillis() - lastModified;
        if (timeSinceLastModification < getParsingDelay()) {
            return false;
        }
        return true;
    }

    private void updateNodesIfModified(JEditorPane jEditorPane) {
        if (!isUpdateNeeded(jEditorPane)) {
            return;
        }
        lastDocVersion = DocumentUtilities.getDocumentVersion(jEditorPane.getDocument());
        lastModified = System.currentTimeMillis();
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
        if (cdo != null) {
            EditorCookie ec = cdo.getCookie(EditorCookie.class);
            currentJEditorPane = NbDocument.findRecentEditorPane(ec);
        }
        return currentJEditorPane;
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

    @Override
    public void projectParsingStarted(CsmProject project) {
    }

    @Override
    public void projectFilesCounted(CsmProject project, int filesCount) {
    }

    @Override
    public void projectParsingFinished(CsmProject project) {
        projectLoaded(project);
    }

    @Override
    public void projectParsingCancelled(CsmProject project) {
    }

    @Override
    public void fileInvalidated(CsmFile file) {
    }

    @Override
    public void fileAddedToParse(CsmFile file) {
    }

    @Override
    public void fileParsingStarted(CsmFile file) {
    }

    @Override
    public void projectLoaded(CsmProject project) {
        CsmFileModel.logger.log(Level.FINE, "projectLoaded {0}", project); // NOI18N
        CsmFile file = getCsmFile();
        if( file != null && project.equals(file.getProject()) ) {
            if( file.isParsed() ) {
                fileParsedOrProjectLoaded(file);
            }
        }
    }

    @Override
    public void fileParsingFinished(CsmFile file) {
        if (file.equals(getCsmFile())) {
    	    fileParsedOrProjectLoaded(file);
        }
    }

    private void fileParsedOrProjectLoaded(CsmFile file) {
        CsmFileModel.logger.log(Level.FINE, "File parsed {0}", file); // NOI18N
        stopTimers();
        update(file);
        restartTimers();
    }

    @Override
    public void parserIdle() {
    }

    @Override
    public void projectOpened(CsmProject project) {
    }

    @Override
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

    @Override
    public void modelChanged(CsmChangeEvent e) {
        if (e.getRemovedFiles().size()>0) {
            CsmFile file = getCsmFile();
            if (file == null || e.getRemovedFiles().contains(file)){
                CsmFileModel.logger.log(Level.FINE, "File removed"); // NOI18N
                stopTimers();
                update(null);
                restartTimers();
            }
        }
    }

    private int storeSelection(){
        if (ui != null) {
            Node[] selection = ui.getExplorerManager().getSelectedNodes();
            if (selection != null && selection.length == 1) {
                Node selected = selection[0];
                if (selected instanceof CppDeclarationNode) {
                    return ((CppDeclarationNode) selected).getOffset();
                }
            }
        }
        return -1;
    }

    public CsmFileFilter getFilter(){
        return fileModel.getFilter();
    }

    private class ShowForwardFunctionDeclarationsAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public ShowForwardFunctionDeclarationsAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowForwardFunctionDeclarationsText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ShowForwardFunctionDeclarationsAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowForwardFunctionDeclarations(!fileModel.getFilter().isShowForwardFunctionDeclarations());
            int selection = storeSelection();
            update(getCsmFile());
            if (selection >= 0) {
                setSelection(selection);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowForwardFunctionDeclarations());
            return menuItem;
        }
    }

    private class ShowForwardClassDeclarationsAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public ShowForwardClassDeclarationsAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowForwardClassDeclarationsText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ShowForwardClassDeclarationsAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowForwardClassDeclarations(!fileModel.getFilter().isShowForwardClassDeclarations());
            int selection = storeSelection();
            update(getCsmFile());
            if (selection >= 0) {
                setSelection(selection);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowForwardClassDeclarations());
            return menuItem;
        }
    }


    private class ShowMacroAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public ShowMacroAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowMacroText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ShowMacroAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowMacro(!fileModel.getFilter().isShowMacro());
            int selection = storeSelection();
            update(getCsmFile());
            if (selection >= 0) {
                setSelection(selection);
            }
        }

        @Override
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
            menuItem.setAction(ShowIncludeAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowInclude(!fileModel.getFilter().isShowInclude());
            int selection = storeSelection();
            update(getCsmFile());
            if (selection >= 0) {
                setSelection(selection);
            }
        }

        @Override
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
            menuItem.setAction(ShowTypedefAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowTypedef(!fileModel.getFilter().isShowTypedef());
            int selection = storeSelection();
            update(getCsmFile());
            if (selection >= 0) {
                setSelection(selection);
            }
        }

        @Override
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
            menuItem.setAction(ShowVariableAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowVariable(!fileModel.getFilter().isShowVariable());
            int selection = storeSelection();
            update(getCsmFile());
            if (selection >= 0) {
                setSelection(selection);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowVariable());
            return menuItem;
        }
    }

    private class ShowFieldAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public ShowFieldAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowFieldText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ShowFieldAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowField(!fileModel.getFilter().isShowField());
            int selection = storeSelection();
            update(getCsmFile());
            if (selection >= 0) {
                setSelection(selection);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowField());
            return menuItem;
        }
    }

    private class ShowUsingAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public ShowUsingAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ShowUsingText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ShowUsingAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setShowUsing(!fileModel.getFilter().isShowUsing());
            int selection = storeSelection();
            update(getCsmFile());
            if (selection >= 0) {
                setSelection(selection);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isShowUsing());
            return menuItem;
        }
    }
    private class SortByNameAction extends AbstractAction implements Presenter.Popup {
        private JRadioButtonMenuItem menuItem;
        public SortByNameAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "SortByNameText")); // NOI18N
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/qnavigator/resources/sortAlpha.png", false)); // NOI18N
            menuItem = new JRadioButtonMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(SortByNameAction.this);
            //Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (fileModel.getFilter().getSortMode() != SortMode.Name) {
                fileModel.getFilter().setSortMode(SortMode.Name);
                int selection = storeSelection();
                update(getCsmFile(), true);
                if (selection >= 0) {
                    setSelection(selection);
                }
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().getSortMode()== SortMode.Name);
            return menuItem;
        }
    }

    private class SortBySourceAction extends AbstractAction implements Presenter.Popup {
        private JRadioButtonMenuItem menuItem;
        public SortBySourceAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "SortBySourceText")); // NOI18N
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/qnavigator/resources/sortPosition.png", false)); // NOI18N
            menuItem = new JRadioButtonMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(SortBySourceAction.this);
            //Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (fileModel.getFilter().getSortMode() != SortMode.Offset) {
                fileModel.getFilter().setSortMode(SortMode.Offset);
                int selection = storeSelection();
                update(getCsmFile(), true);
                if (selection >= 0) {
                    setSelection(selection);
                }
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().getSortMode() == SortMode.Offset);
            return menuItem;
        }
    }

    private class GroupByKindAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public GroupByKindAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "GroupByKindText")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(GroupByKindAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setGroupByKind(!fileModel.getFilter().isGroupByKind());
            int selection = storeSelection();
            update(getCsmFile(), true);
            if (selection >= 0) {
                setSelection(selection);
            }
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isGroupByKind());
            return menuItem;
        }
    }

    private class ExpandAllAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public ExpandAllAction() {
            putValue(Action.NAME, NbBundle.getMessage(NavigatorModel.class, "ExpandAll")); // NOI18N
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(ExpandAllAction.this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fileModel.getFilter().setExpandAll(!fileModel.getFilter().isExpandAll());
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(fileModel.getFilter().isExpandAll());
            return menuItem;
        }
    }

    private class FilterSubmenuAction extends AbstractAction implements Presenter.Popup {
        private String mimeType;

        public FilterSubmenuAction(String mimeType) {
            this.mimeType = mimeType;
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
        }

        @Override
        public final JMenuItem getPopupPresenter() {
            return createSubmenu();
        }

        private JMenuItem createSubmenu () {
            JMenuItem menu = new JMenu(NbBundle.getMessage(NavigatorModel.class, "FilterSubmenu")); //NOI18N
            boolean isC = MIMENames.isHeaderOrCppOrC(mimeType);
            boolean isCpp = MIMENames.isHeaderOrCpp(mimeType);
            boolean isCnd = MIMENames.isFortranOrHeaderOrCppOrC(mimeType);
            if (isC) {
                menu.add(new ShowForwardClassDeclarationsAction().getPopupPresenter());
                menu.add(new ShowForwardFunctionDeclarationsAction().getPopupPresenter());
                menu.add(new ShowMacroAction().getPopupPresenter());
            }
            if (isCnd) {
                menu.add(new ShowIncludeAction().getPopupPresenter());
            }
            if (isC) {
                menu.add(new ShowTypedefAction().getPopupPresenter());
            }
            if (isCnd) {
                menu.add(new ShowVariableAction().getPopupPresenter());
                menu.add(new ShowFieldAction().getPopupPresenter());
            }
            if (isCpp) {
                menu.add(new ShowUsingAction().getPopupPresenter());
            }
            return menu;
        }
    }
}
