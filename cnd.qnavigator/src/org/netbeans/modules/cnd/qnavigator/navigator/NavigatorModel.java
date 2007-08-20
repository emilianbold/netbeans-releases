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

package org.netbeans.modules.cnd.qnavigator.navigator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.Timer;
import javax.swing.text.Caret;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.loaders.CppEditorSupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
public class NavigatorModel implements CsmProgressListener, CsmModelListener {
    private DataObject cdo;
    private CsmUID<CsmFile> uid;
    private NavigatorPanelUI ui;
    private NavigatorComponent busyListener;
    private AbstractNode root = new AbstractNode(new Children.Array());
    private List<IndexOffsetNode> lineNumberIndex = new ArrayList<IndexOffsetNode>(5);
    private Timer checkModifiedTimer;
    private long lastModified = -1;
    private Timer checkCursorTimer;
    private long lastCursorPos = -1;
    private long lastCursorPosWhenChecked = 0;
    
    public NavigatorModel(DataObject cdo, NavigatorPanelUI ui, NavigatorComponent component) {
        this.cdo = cdo;
        this.ui = ui;
        update(getCsmFile());
        if (CppSettings.getDefault().getParsingDelay() > 0) {
            checkModifiedTimer = new Timer(CppSettings.getDefault().getParsingDelay(), new ActionListener() {
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
    
    private CsmFile getCsmFile() {
        CsmFile csmFile = null;
        if (uid == null) {
            if (cdo != null) {
                csmFile = CsmUtilities.getCsmFile(cdo, false);
            }
            if (csmFile != null) {
                uid = csmFile.getUID();
            }
        } else {
            csmFile = uid.getObject();
        }
        if (csmFile != null && !csmFile.isValid()) {
            uid = null;
            csmFile = null;
        }
        return csmFile;
    }
    
    
    void removeNotify() {
        stopTimers();
    }
    
    void addNotify() {
        update(getCsmFile());
        ui.newContentReady();
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
            if (csmFile != null){
                //fileID = csmFile.getAbsolutePath();
                final Children children = root.getChildren();
                if (!Children.MUTEX.isReadAccess()){
                    Children.MUTEX.writeAccess(new Runnable(){
                        public void run() {
                            children.remove(children.getNodes());
                            List<CppDeclarationNode> list = new ArrayList<CppDeclarationNode>();
                            lineNumberIndex.clear();
                            if (csmFile.isValid()){
                                for(CsmInclude element : csmFile.getIncludes()){
                                    CppDeclarationNode node = CppDeclarationNode.nodeFactory((CsmObject)element, lineNumberIndex, false);
                                    if (node != null){
                                        list.add(node);
                                    }
                                }
                                for(CsmMacro element : csmFile.getMacros()){
                                    CppDeclarationNode node = CppDeclarationNode.nodeFactory((CsmObject)element, lineNumberIndex, false);
                                    if (node != null){
                                        list.add(node);
                                    }
                                }
                                for(CsmOffsetableDeclaration element : csmFile.getDeclarations()){
                                    CppDeclarationNode node = CppDeclarationNode.nodeFactory((CsmObject)element, lineNumberIndex, false);
                                    if (node != null){
                                        list.add(node);
                                    }
                                }
                            }
                            if (csmFile.isValid()){
                                Collections.<CppDeclarationNode>sort(list);
                                Collections.<IndexOffsetNode>sort(lineNumberIndex);
                                children.add(list.toArray(new Node[0]));
                            }
                        }
                    });
                }
            } else {
                final Children children = root.getChildren();
                if (!Children.MUTEX.isReadAccess()){
                    Children.MUTEX.writeAccess(new Runnable(){
                        public void run() {
                            children.remove(children.getNodes());
                            lineNumberIndex.clear();
                        }
                    });
                }
            }
        } finally {
            if (busyListener != null) {
                busyListener.busyEnd();
            }
        }
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
        if (timeSinceLastModification < CppSettings.getDefault().getParsingDelay()){
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
        // Find nearest Node
        int index = Collections.<IndexOffsetNode>binarySearch(lineNumberIndex, new IndexOffsetNode(null, caretLineNo));
        if (index < 0) {
            // exact line not found, but insersion index (-1) returned instead
            index = -index-2;
        }
        if (index > -1 && index < lineNumberIndex.size()) {
            IndexOffsetNode node  = lineNumberIndex.get(index);
            ui.selectNode(node.getNode());
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
	ui.newContentReady();
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
            ui.newContentReady();
            restartTimers();
        }
    }
    
    public void modelChanged(CsmChangeEvent e) {
        if (e.getRemovedFiles().size()>0) {
            CsmFile file = getCsmFile();
            if (file == null){
                stopTimers();
                update(null);
                ui.newContentReady();
                restartTimers();
            }
        }
    }
}
