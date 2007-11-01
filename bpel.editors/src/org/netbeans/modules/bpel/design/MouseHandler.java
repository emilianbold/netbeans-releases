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

package org.netbeans.modules.bpel.design;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

import org.netbeans.modules.bpel.design.selection.EntitySelectionModel;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.design.selection.PlaceHolderManager;


public class MouseHandler extends MouseAdapter {
    
    private DesignView designView;
 

    public MouseHandler(DesignView designView) {
        this.designView = designView;
        designView.addMouseListener(this);
    }
    
    public DesignView getDesignView() {
        return designView;
    }
    
    
    public void cancel() {
        getNameEditor().cancelEdit();
    }
    
    
    public void mousePressed(MouseEvent e) {
        getDesignView().requestFocus();
        
        if (getDesignView().isDesignMode()) {
            Pattern p = getDesignView().findPattern(e.getPoint());

            getSelectionModel().setSelectedPattern(p);
        } else {
            PlaceHolder ph = getDesignView().findPlaceholder(e.getPoint());
            
            if (ph != null) {
                getDesignView().getPhSelectionModel().setSelectedPlaceHolder(ph);
                getDesignView().repaint();
            }
        }

        maybeShowPopup(e);
    }

    
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }
    
    
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            getDesignView().getNameEditor().startEdit(e.getPoint());
            if (!getDesignView().getNameEditor().isActive()) {
                Pattern selected = getSelectionModel().getSelectedPattern();
                if (selected != null) {
                    getDesignView().performDefaultAction(selected);
                }
            }
        }
    }
    
    public NameEditor getNameEditor() {
        return getDesignView().getNameEditor();
    }
    
    public EntitySelectionModel getSelectionModel() {
        return getDesignView().getSelectionModel();
    }
    
    public PlaceHolderManager getPlaceHolderManager() {
        return getDesignView().getPlaceHolderManager();
    }
    
    public boolean maybeShowPopup(MouseEvent e) {
        if (!e.isPopupTrigger()) return false;

        JPopupMenu popup = null;
        if (getDesignView().isDesignMode()) {
            Pattern pattern = getSelectionModel().getSelectedPattern();

            if (pattern == null) return false;

            popup = pattern.createPopupMenu();
        } else {
            PlaceHolder ph = getDesignView().findPlaceholder(e.getPoint());
            if (ph == null)  return false;
            
            popup = getPlaceHolderManager().createPopupMenu();
        }
        
        if (popup == null) return false;
        
        popup.show(e.getComponent(), e.getX(), e.getY());
        
        return true;
    }

}
