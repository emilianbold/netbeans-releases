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
package org.netbeans.modules.bpel.design.decoration.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.nodes.BpelNode.NodeBadges.WARNING;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.util.NbBundle;

/**
 * @author aa160298
 */
public class ShowGlassPaneButton extends AbstractGlassPaneButton {
    
    public ShowGlassPaneButton(List<ResultItem> resultItems) {
        super(ERROR_ICON);
        fillGlassPaneHeader(resultItems);
        fillGlassPaneContent(resultItems);
        updatePreferredSize();
    }

    public void setResultItems(List<ResultItem> resultItems) {
        getGlassPane().removeHeaders();
        getGlassPane().removeHTML();
        fillGlassPaneHeader(resultItems);
        fillGlassPaneContent(resultItems);
    }
    
    private void fillGlassPaneHeader(List<ResultItem> resultItems) {
        int errorsCount = 0;
        int warningsCount = 0;
        int advicesCount = 0;
        
        for (ResultItem item : resultItems) {
            switch (item.getType()) {
                case ERROR: 
                    errorsCount++; 
                    break;
                case WARNING: 
                    warningsCount++; 
                    break;
                case ADVICE: 
                    advicesCount++; 
                    break;
            }
        }
        
        if (errorsCount > 0) {
            if (errorsCount > 1) {
                getGlassPane().addHeader(ERROR_ICON, "" + errorsCount + " "
                        + NbBundle.getMessage(getClass(), 
                        "LBL_ShowGlassPaneButton_N_Errors").trim()); // NOI18N
            } else {
                getGlassPane().addHeader(ERROR_ICON, NbBundle.getMessage(getClass(), 
                        "LBL_ShowGlassPaneButton_1_Error").trim()); // NOI18N
            }
        } 

        if (warningsCount > 0) {
            if (warningsCount > 1) {
                getGlassPane().addHeader(WARNING_ICON, "" + warningsCount + " " 
                        + NbBundle.getMessage(getClass(), 
                        "LBL_ShowGlassPaneButton_N_Warnings").trim()); // NOI18N
            } else {
                getGlassPane().addHeader(WARNING_ICON, NbBundle.getMessage(getClass(), 
                        "LBL_ShowGlassPaneButton_1_Warning").trim()); // NOI18N
            }
        } 

        if (advicesCount > 0) {
            if (advicesCount > 1) {
                getGlassPane().addHeader(ADVICE_ICON, "" + advicesCount + " "
                        + NbBundle.getMessage(getClass(), 
                        "LBL_ShowGlassPaneButton_N_Advices").trim()); // NOI18N
            } else {
                getGlassPane().addHeader(ADVICE_ICON, NbBundle.getMessage(getClass(), 
                        "LBL_ShowGlassPaneButton_1_Advice").trim()); // NOI18N
            }
        } 
        
        
        if (errorsCount > 0) {
            setMyIcon(ERROR_ICON);
        } else if (warningsCount > 0) {
            setMyIcon(WARNING_ICON);
        } else if (advicesCount > 0) {
            setMyIcon(ADVICE_ICON);
        }
    }
    
    private void fillGlassPaneContent(List<ResultItem> resultItems) {
        addToGlassPane(resultItems, ResultType.ERROR);
        addToGlassPane(resultItems, ResultType.ADVICE);
        addToGlassPane(resultItems, ResultType.WARNING);
        getGlassPane().updateHTML();
    }
    
    private void addToGlassPane(List<ResultItem> resultItems, ResultType type) 
    {
        List<ResultItem> items = filterResultItems(resultItems, type);

        String icon = null;
        String slowIcon = null;
        
        switch (type) {
            case ERROR:
                icon = ERROR_ICON_PATH;
                slowIcon = ERROR_SLOW_ICON_PATH;
                break;
            case WARNING:
                icon = WARNING_ICON_PATH;
                slowIcon = WARNING_SLOW_ICON_PATH;
                break;
            case ADVICE:
                icon = ADVICE_ICON_PATH;
                slowIcon = ADVICE_SLOW_ICON_PATH;
        }
        
        for (ResultItem item : items) {
            if ("BPELSchemaValidator".equals(item.getValidator().getName())) { // NOI18N
                getGlassPane().addListItem(slowIcon, item.getDescription());
            } else {
                getGlassPane().addListItem(icon, item.getDescription());
            }
        }
    }
    
    private List<ResultItem> filterResultItems(List<ResultItem> resultItems, 
            ResultType type) 
    {
        List<ResultItem> result = new ArrayList<ResultItem>();
        for (ResultItem item : resultItems) {
            if (item.getType() == type) {
                result.add(item);
            }
        }
        return result;
    }
    
    protected void paintComponent(Graphics g) {
        ButtonModel model = getModel();
        
        if (model.isPressed()) {
            ButtonRenderer.paintButton(this, g, 
                    ButtonRenderer.PRESSED_FILL_COLOR, false, 
                    ButtonRenderer.PRESSED_BORDER_COLOR, 
                    ButtonRenderer.PRESSED_STROKE_WIDTH, getMyIcon());
        } else if (model.isRollover()) {
            ButtonRenderer.paintButton(this, g, 
                    ButtonRenderer.ROLLOVER_FILL_COLOR, true, 
                    ButtonRenderer.ROLLOVER_BORDER_COLOR, 
                    ButtonRenderer.ROLLOVER_STROKE_WIDTH, getMyIcon());
            
        } else if (model.isSelected()) {
            ButtonRenderer.paintButton(this, g, BACKGROUND, false, 
                    ButtonRenderer.PRESSED_BORDER_COLOR, 
                    ButtonRenderer.PRESSED_STROKE_WIDTH, getMyIcon());
        } else {
            ButtonRenderer.paintButton(this, g, BACKGROUND, false, 
                    null, ButtonRenderer.NORMAL_STROKE_WIDTH, getMyIcon());
        }
    }
    
    private static Color BACKGROUND = new Color(0xCCFFFFFF, true);
    
    private static final String ERROR_ICON_PATH = "resources/error.png"; // NOI18N
    private static final String ERROR_SLOW_ICON_PATH = "resources/error_explicit.png"; // NOI18N
    
    private static final String WARNING_ICON_PATH = "resources/warning.png"; // NOI18N
    private static final String WARNING_SLOW_ICON_PATH = "resources/warning_explicit.png"; // NOI18N
    
    private static final String ADVICE_ICON_PATH = "resources/advice.png"; // NOI18N
    private static final String ADVICE_SLOW_ICON_PATH = "resources/advice_explicit.png"; // NOI18N
    
    private static final Icon ERROR_ICON;
    private static final Icon ERROR_SLOW_ICON;
    
    private static final Icon WARNING_ICON;
    private static final Icon WARNING_SLOW_ICON;

    private static final Icon ADVICE_ICON;
    private static final Icon ADVICE_SLOW_ICON;
    
    static {
        ERROR_ICON        = new ImageIcon(Decoration.class.getResource(ERROR_ICON_PATH));
        ERROR_SLOW_ICON   = new ImageIcon(Decoration.class.getResource(ERROR_SLOW_ICON_PATH));
        WARNING_ICON      = new ImageIcon(Decoration.class.getResource(WARNING_ICON_PATH));
        WARNING_SLOW_ICON = new ImageIcon(Decoration.class.getResource(WARNING_SLOW_ICON_PATH));
        ADVICE_ICON       = new ImageIcon(Decoration.class.getResource(ADVICE_ICON_PATH));
        ADVICE_SLOW_ICON  = new ImageIcon(Decoration.class.getResource(ADVICE_SLOW_ICON_PATH));
    }
}
