/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.event.KeyEvent;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;

/**
 *
 * @author jyothi
 */
public class EdgeLabelIteratorAction extends WidgetAction.Adapter {

    DesignerScene scene;
    LabelManager mgr = null;
    private Object[] edgeLabels = null;
    private String name;
    private LabelManager.LabelType type;
    private static final int NEXT = 100;
    private static final int PREVIOUS = 200;
    
    @Override
    public State keyPressed(Widget widget, WidgetKeyEvent event)
    {
        State retVal = State.REJECTED;
        scene = (DesignerScene) widget.getScene();
        boolean status = false;
        
        if(event.isShiftDown() == true) 
        {
            if(event.getKeyCode() == KeyEvent.VK_UP)
            {
                status = selectNextLabel(widget, PREVIOUS);
                if (status) 
                {
                    //we successfully selected a label..
                    retVal = State.CONSUMED;
                }    
            }
            else if(event.getKeyCode() == KeyEvent.VK_DOWN)
            {
                status = selectNextLabel(widget, NEXT);
                if (status) 
                {
                    //we successfully selected a label..
                    retVal = State.CONSUMED;
                }                       
            }
        }        
        return retVal;
    }

    private Object[] populateEdgeLabels(Widget widget)
    {
        if (widget instanceof UMLEdgeWidget)
        {            
            mgr = ((UMLEdgeWidget)widget).getLookup().lookup(LabelManager.class);
            HashMap<String, Widget> labelMap =  mgr.getLabelMap();
            if (labelMap != null && !labelMap.isEmpty())
            {
                edgeLabels = new Object[labelMap.size()];
                edgeLabels = (Object[]) labelMap.keySet().toArray();
            }
        }
        return edgeLabels;
    }
    
    private String getSelectedLabel(Widget widget)
    {
        if (widget instanceof UMLEdgeWidget) {
            populateEdgeLabels(widget);
            if (edgeLabels == null) {
                return null;
            }
            Object[] values = new Object[2];
            for (int i = 0; i < edgeLabels.length; i++) {
                values = getNameAndType(i);
                
                if (mgr != null && mgr.isLabelSelected((String) values[0], (LabelManager.LabelType) values[1])) {
                    //we already have a selected label..
                    return (String)edgeLabels[i]; //I need the full name here..
                }
            }
        }
        return null;
    }
    
    
    private boolean selectNextLabel(Widget widget, int direction)
    {
        if (widget instanceof UMLEdgeWidget) {
            Object[] values = new Object[2];
            String selectedLabel = getSelectedLabel(widget);

            if (selectedLabel == null) {
                //select the first label..
                values = getNameAndType(0);

            }
            else {
                //find the next label in the labelMap..
                if (edgeLabels != null && edgeLabels.length > 0) {
                    for (int i = 0; i < edgeLabels.length; i++) {
                        String string = (String)edgeLabels[i];
                        if (selectedLabel.equalsIgnoreCase(string)) {
                            if (direction == NEXT)
                            {
                                values = getNameAndType(i < edgeLabels.length - 1 ? i+1 : 0);
                            }
                            else if (direction == PREVIOUS)
                            {
                                values = getNameAndType(i > 0 ? i-1 : edgeLabels.length - 1);
                            }                            
                        }
                    }
                }
            }
            if (mgr != null && values[0] != null && values[1] != null)
            {
                mgr.selectLabel((String) values[0], (LabelManager.LabelType) values[1]);
                return true;
            }            
        }
        return false;
    }

    private Object[] getNameAndType(int index)
    {
        Object[] retVal = new Object[2];

        if (edgeLabels != null && edgeLabels.length > 0) {
            String label = (String)edgeLabels[index];
            if (label.contains("_")) {
                int underscore = label.indexOf("_");
                retVal[0] = label.substring(0, underscore);
                retVal[1] = LabelManager.LabelType.valueOf(label.substring(underscore + 1));
            }
        }
        return retVal;
    }
    
}
