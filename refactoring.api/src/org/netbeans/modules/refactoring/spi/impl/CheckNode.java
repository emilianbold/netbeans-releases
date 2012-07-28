/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.refactoring.spi.impl;

import java.io.IOException;
import java.util.Enumeration;
import javax.swing.Icon;
import javax.swing.tree.*;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.openide.text.PositionBounds;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * @author Pavel Flaska
 */
public final class CheckNode extends DefaultMutableTreeNode {

    public final static int SINGLE_SELECTION = 0;
    public final static int DIG_IN_SELECTION = 4;
  
    private int selectionMode;
    private boolean isSelected;

    private String nodeLabel;
    private Icon icon;
    
    private boolean disabled = false;
    private boolean needsRefresh = false;
    private static Icon found = ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/api/resources/found_item_orange.png", false);
    
    public CheckNode(Object userObject, String nodeLabel, Icon icon, boolean isQuery) {
        super(userObject, !(userObject instanceof RefactoringElement));
        this.isSelected = true;
        setSelectionMode(DIG_IN_SELECTION);
        this.nodeLabel = nodeLabel;
        this.icon = icon;
        if (userObject instanceof TreeElement) {
            if (((TreeElement)userObject).getUserObject() instanceof RefactoringElement) {
                RefactoringElement ree = (RefactoringElement) ((TreeElement)userObject).getUserObject();
                int s = ree.getStatus();
                
                PositionBounds bounds = getPosition();
                if (isQuery && bounds != null) {
                    int line = 0;
                    try {
                        line = bounds.getBegin().getLine() + 1;
                    } catch (IOException ioe) {
                    }


                    this.nodeLabel = "<font color='!controlShadow'>" + getLineString(line,4) + "</font>" + nodeLabel; //NOI18N
                    if (this.icon==null) {
                        this.icon = found;
                    }
                }
                
                if (s==RefactoringElement.GUARDED || s==RefactoringElement.READ_ONLY) {
                    isSelected = false;
                    disabled = true;
                    this.nodeLabel = "[<font color=#CC0000>" // NOI18N
                            + NbBundle.getMessage(CheckNode.class, s==RefactoringElement.GUARDED?"LBL_InGuardedBlock":"LBL_InReadOnlyFile")
                            + "</font>]" + this.nodeLabel; // NOI18N
                }
            }
        }
    }
    
    String getLabel() {
        return nodeLabel;
    }

    void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }
    
    Icon getIcon() {
        return icon;
    }
    
    public void setDisabled() {
        disabled = true;
        isSelected = false;
        removeAllChildren();
    }
    
    boolean isDisabled() {
        return disabled;
    }

    void setNeedsRefresh() {
        needsRefresh = true;
        setDisabled();
    }
    
    boolean needsRefresh() {
        return needsRefresh;
    }
    
    public void setSelectionMode(int mode) {
        selectionMode = mode;
    }

    public int getSelectionMode() {
        return selectionMode;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        if (userObject instanceof TreeElement) {
            Object ob = ((TreeElement) userObject).getUserObject();
            if (ob instanceof RefactoringElement) {
                    ((RefactoringElement) ob).setEnabled(isSelected);
            }
        }
        if ((selectionMode == DIG_IN_SELECTION) && (children != null)) {
            Enumeration e = children.elements();      
            while (e.hasMoreElements()) {
                CheckNode node = (CheckNode)e.nextElement();
                node.setSelected(isSelected);
            }
        }
    }

    public boolean isSelected() {
        if (userObject instanceof TreeElement) {
            Object ob = ((TreeElement) userObject).getUserObject();
            if (ob instanceof RefactoringElement) {
                return ((RefactoringElement) ob).isEnabled() &&
                        !((((RefactoringElement) ob).getStatus() == RefactoringElement.GUARDED) || (((RefactoringElement) ob).getStatus() == RefactoringElement.READ_ONLY));
            }
        }
        return isSelected;
    }
    
    public PositionBounds getPosition() {
        if (userObject instanceof TreeElement) {
            Object re = ((TreeElement) userObject).getUserObject();
            if (re instanceof RefactoringElement)
                return ((RefactoringElement) re).getPosition();
        }
        return null;
    }
    
    private String tooltip;
    public String getToolTip() {
        if (tooltip==null) {
            if (userObject instanceof TreeElement) {
                Object re = ((TreeElement) userObject).getUserObject();
                if (re instanceof RefactoringElement) {
                    RefactoringElement ree = (RefactoringElement) re;
                    PositionBounds bounds = getPosition();
                    FileObject file = ree.getParentFile();
                    if (bounds != null && file!=null) {
                        int line;
                        try {
                            line = bounds.getBegin().getLine() + 1;
                        } catch (IOException ioe) {
                            return null;
                        }
                        tooltip = FileUtil.getFileDisplayName(file) + ':' + line;
                    }
                }
            }
        }
        return tooltip;
    }

    private String getLineString(int line, int size) {
        String l = Integer.toString(line);
        int length = size-l.length();
        for (int i=0; i < length*2; i++) {
            l = "&nbsp;" + l; //NOI18N
        }
        return l + ":&nbsp;&nbsp;"; //NOI18N
    }
}
