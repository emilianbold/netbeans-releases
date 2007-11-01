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


package org.netbeans.modules.bpel.design.selection;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.geometry.FPoint;

import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CompensatableActivityHolder;
import org.netbeans.modules.bpel.design.model.patterns.CompensatePattern;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.model.api.Else;
import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.util.NbBundle;

public class PlaceHolderManager implements DnDTool {
    
    private List<PlaceHolder> placeHolders = new ArrayList<PlaceHolder>();
    private PlaceHolder currentPlaceHolder;
    private DesignView designView;
    private Pattern draggedPattern;
    
    private PlaceHolder defaultPlaceholder;
    
    public PlaceHolderManager(DesignView designView) {
        this.designView = designView;
    }
    
    
    public boolean isEmpty() {
        return placeHolders.isEmpty();
    }
    
    
    public void clear() {
        if (!isEmpty()) {
            placeHolders.clear();
            currentPlaceHolder = null;
            draggedPattern = null;
            getDesignView().repaint();
            defaultPlaceholder = null;
        }
        //getDesignView().getButtonsManager().update();
    }
    
    
    
    public DesignView getDesignView() {
        return designView;
    }
    
    public Pattern getDraggedPattern() {
        return draggedPattern;
    }
    
    
    
    public PlaceHolder findPlaceholder(float px, float py) {
        for (int i = placeHolders.size() - 1; i >= 0; i--) {
            PlaceHolder p = placeHolders.get(i);
            if (p.contains(px, py)) { return p; }
        }
        return defaultPlaceholder;
    }
    
    
    public void init(Pattern draggedPattern) {
        this.draggedPattern = draggedPattern;
        this.defaultPlaceholder = null;
        createPlaceHolders(getDesignView().getRootPattern());
        //getDesignView().getButtonsManager().clear();
    }
    
    
    /**
     * Function builds the popup menu to show on diagram element.
     * Menu actions are taken from Node wrapping the underlying BpelOM element.
     * Derived classes should override this method to add extra menu items.
     * @returns menu to show.
     **/
    public JPopupMenu createPopupMenu() {
        if (getDesignView().isDesignMode()) {
            return null;
        }
        
        JPopupMenu menu = new JPopupMenu(NbBundle.
                getMessage(DesignView.class, "LBL_PLACEHOLDER"));

        //populate a list of actions
        Action paste = getDesignView().getCopyPasteHandler().getPasteAction();
        
        if (paste != null ){
            menu.add(new Actions.MenuItem(paste, false));
        }
        return menu;
    }
    
    private void createPlaceHolders(Pattern pattern) {
        
        if (getDraggedPattern() == pattern) return;
        if (pattern.isNestedIn(getDraggedPattern())) return;
        
        //special case for Compensate activity which breaks common rule. see bug 6364908
        if (!(getDraggedPattern() instanceof CompensatePattern &&
                !(pattern.getOMReference() instanceof CompensatableActivityHolder))) {
            pattern.createPlaceholders(getDraggedPattern(), placeHolders);
        }
        
        if (pattern instanceof CompositePattern) {
            CompositePattern composite = (CompositePattern) pattern;
            for (Pattern p : composite.getNestedPatterns()) {
                createPlaceHolders(p);
            }
        }

        //Try to create "implicit sequence" placeholders
        if (getDraggedPattern().getOMReference() instanceof Activity){
            ImpliciteSequencePlaceHolder.create(getDraggedPattern(), pattern,
                    placeHolders);
        }
        
        
        /*
         * Find first placeholder marked as default
         * This placheholder will recieve drop if element was dropped on whitespace
         */
       
        for (PlaceHolder placeHolder: placeHolders ){
            if (placeHolder instanceof DefaultPlaceholder){
                defaultPlaceholder = placeHolder;
                break;
            }
        }
    }
    
    public List<PlaceHolder> getPlaceHolders() {
        return placeHolders;
    }
    
    public void move(FPoint mp) {
        if (isEmpty()) { return; }
        
        PlaceHolder newCurrentPlaceHolder = findPlaceholder(mp.x, mp.y);
        PlaceHolder oldCurrentPlaceHolder = currentPlaceHolder;
        
        if (oldCurrentPlaceHolder != newCurrentPlaceHolder) {
            if (oldCurrentPlaceHolder != null) {
                oldCurrentPlaceHolder.dragExit();
            }
            
            if (newCurrentPlaceHolder != null) {
                newCurrentPlaceHolder.dragEnter();
            }
            
            currentPlaceHolder = newCurrentPlaceHolder;
            getDesignView().repaint();
        }
    }
    public boolean isValidLocation(){
        return (currentPlaceHolder != null);
    }
    
    
    public void drop(FPoint mp) {
        if (isEmpty()) {
            return;
        }
        PlaceHolder targetPlaceholder = findPlaceholder(mp.x, mp.y);
        
        if (targetPlaceholder == null) {
            targetPlaceholder = defaultPlaceholder;
        }
        if (targetPlaceholder != null){
            try {
                
                BpelEntity activity = (BpelEntity)getDraggedPattern().getOMReference();
                BpelEntity activityParent = (activity != null) 
                        ? activity.getParent() : null;
                
                if (activityParent != null){
                    activity.cut();
                    
                    if (activityParent instanceof Else) {
                        BpelContainer elseParent = activityParent.getParent();
                        if (elseParent != null) {
                            elseParent.remove(activityParent);
                        }
                    }
                }
                targetPlaceholder.drop();
                
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
        
        
        clear();
    }
    
    
    public void paint(Graphics2D g2) {
        if (isEmpty()) {
            return;
        }
        for (PlaceHolder p : placeHolders) {
            p.paint(g2);
        }
    }
    
    
}
