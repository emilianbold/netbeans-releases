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
package org.netbeans.modules.tasklist.todo;

import java.awt.Frame;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Set;
import javax.swing.Action;
import org.openide.nodes.Node;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;

/**
 *
 * @author pzajac
 */
public class WindowManagerMock extends WindowManager {

    
    public WindowManagerMock() {
    }

    @Override
    public boolean isEditorTopComponent(TopComponent tc) {
        return true;
    }

    
    public Mode findMode(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Mode findMode(TopComponent tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<? extends Mode> getModes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Frame getMainWindow() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateUI() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected Component createTopComponentManager(TopComponent c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Workspace createWorkspace(String name, String displayName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Workspace findWorkspace(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Workspace[] getWorkspaces() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWorkspaces(Workspace[] workspaces) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Workspace getCurrentWorkspace() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TopComponentGroup findTopComponentGroup(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentOpen(TopComponent tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentOpenAtTabPosition(TopComponent tc, int position) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected int topComponentGetTabPosition(TopComponent tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentClose(TopComponent tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentRequestActive(TopComponent tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentRequestVisible(TopComponent tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentDisplayNameChanged(TopComponent tc,
                                                  String displayName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentHtmlDisplayNameChanged(TopComponent tc,
                                                      String htmlDisplayName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentToolTipChanged(TopComponent tc, String toolTip) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentIconChanged(TopComponent tc, Image icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentActivatedNodesChanged(TopComponent tc,
                                                     Node[] activatedNodes) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected boolean topComponentIsOpened(TopComponent tc) {
       return false;
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    protected Action[] topComponentDefaultActions(TopComponent tc) {
        
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected String topComponentID(TopComponent tc, String preferredID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TopComponent findTopComponent(String tcID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
