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


/*
 * Abstract.java
 *
 * Created on 19 Èþíü 2006 ã., 20:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.umllib.vrf;

import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.customelements.LifelineOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;

/**
 *
 * @author ak153256
 */
public class GenericVerifier {
    
    protected EventTool eventTool = new EventTool();
    protected DiagramOperator dia = null;
    
    protected PrintStream log = null;
    protected Robot robot = null;
    
    
    protected String DELETE_DLG = "Delete";
    protected String DELETE_PKG_DLG = "Deleting a Package";
    protected String YES_BTN = "Yes";
    protected String OK_BTN = "Ok";
    protected String CANCEL_BTN = "Cancel";
    protected String FONT_DLG = "Font";
    protected String BOLD_CHB = "Bold";
    protected String ITALIC_CHB = "Italic";
    
    protected final String CLASSIFIER_PREFIX = "C";
    
    /** Creates a new instance of Abstract */
    public GenericVerifier(DiagramOperator dia) {
        this(dia, null);
    }
    public GenericVerifier(DiagramOperator dia, PrintStream log) {
        this.dia = dia;
        this.log = log;
        try{
            this.robot = new Robot();
        }catch(Exception e){}
    }
    
    public DiagramElementOperator createElement(String name, ElementTypes elementType) throws NotFoundException{
        Point p = dia.getDrawingArea().getFreePoint();
        return createElement(name, elementType, p.x, p.y);
    }
    
    public DiagramElementOperator createElement(String name, ElementTypes elementType, int x, int y) throws NotFoundException{
        if (elementType.equals(ElementTypes.LIFELINE)){
            return dia.putElementOnDiagram(name + ":" + CLASSIFIER_PREFIX + name, elementType, x, y);
        } else if (elementType.equals(ElementTypes.ACTOR_LIFELINE)){
            dia.createGenericElementOnDiagram(name + ":" + CLASSIFIER_PREFIX + name, elementType, x, y);
            return new LifelineOperator(dia, name, CLASSIFIER_PREFIX + name);
            // TMP workaround for tests....
        } else if (elementType.equals(ElementTypes.COMPOSITE_STATE)){
            return dia.putElementOnDiagram(name, elementType, x, y, new DiagramElementOperator.PropertyNamer());
        } else if(isElementWithoutEditControl(elementType)){
            return dia.putElementOnDiagram(name, elementType, x, y, new DiagramElementOperator.PropertyNamer());
        } else {
            return dia.putElementOnDiagram(name, elementType, x, y);
        }
    }
    
    public DiagramElementOperator getElement(String name) throws NotFoundException{
        return getElement(name, ElementTypes.ANY, 0);
    }
    
    public DiagramElementOperator getElement(String name, ElementTypes elementType, int index) throws NotFoundException{
        if (elementType.equals(ElementTypes.LIFELINE) || elementType.equals(ElementTypes.ACTOR_LIFELINE)){
            String aName = name + ":" + CLASSIFIER_PREFIX + name;
            int semicolonPos = aName.indexOf(':');
            String lineName = aName.substring(0,semicolonPos);
            String classifierName = aName.substring(semicolonPos+1);
            return new LifelineOperator(dia, lineName, classifierName, index);
        }else{
            return new DiagramElementOperator(dia, name, elementType, index);
        }
    }
    
    private boolean isElementWithoutEditControl(ElementTypes elementType){
        return  elementType.equals(ElementTypes.COMMENT) ||
                elementType.equals(ElementTypes.LINK_COMMENT) ||
                // activity
                elementType.equals(ElementTypes.INITIAL_NODE) ||
                elementType.equals(ElementTypes.ACTIVITY_FINAL_NODE) ||
                elementType.equals(ElementTypes.FLOW_FINAL) ||
                elementType.equals(ElementTypes.VERTICAL_FORK) ||
                elementType.equals(ElementTypes.HORIZONTAL_FORK) ||
                // state
                elementType.equals(ElementTypes.INITIAL_STATE) ||
                elementType.equals(ElementTypes.VERTICAL_JOIN_MERGE) ||
                elementType.equals(ElementTypes.HORIZONTAL_JOIN_MERGE) ||
                elementType.equals(ElementTypes.FINAL_STATE) ||
                elementType.equals(ElementTypes.ABORTED_FINAL_STATE) ||
                elementType.equals(ElementTypes.CHOICE_PSEUDO_STATE) ||
                elementType.equals(ElementTypes.SHALLOW_HISTORY_STATE) ||
                elementType.equals(ElementTypes.DEEP_HISTORY_STATE) ||
                elementType.equals(ElementTypes.ENTRY_POINT_STATE) ||
                elementType.equals(ElementTypes.JUNCTION_POINT_STATE) ||
                elementType.equals(ElementTypes.CHOICE_PSEUDO_STATE);
    }
    
    public void safeDeleteAllElements(){
        
        long timeoutVal = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
        JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 2000);
        eventTool.waitNoEvent(500);
        try{
            dia = new DiagramOperator(dia.getName());
            if (dia != null){
                //deleting the element
                Point point = dia.getDrawingArea().getFreePoint();
                dia.getDrawingArea().clickMouse(point.x, point.y, 1);
                
                pushShortcut(KeyEvent.VK_A, KeyEvent.VK_CONTROL);
                
                // 2000 milis were changed to 1000. Let's look if this will broke smth
                eventTool.waitNoEvent(1000);
                
                pushKey(KeyEvent.VK_DELETE);
                
                new Thread(new Runnable() {
                    public void run() {
                        new JCheckBoxOperator(new JDialogOperator(DELETE_DLG)).clickMouse();
                        new JButtonOperator(new JDialogOperator(DELETE_DLG), YES_BTN).push();
                    }
                }).start();
                
                new Thread(new Runnable() {
                    public void run() {
                        try{
                            for(;;){
                                new JButtonOperator(new JDialogOperator(DELETE_PKG_DLG), YES_BTN).push();
                            }
                        }catch(TimeoutExpiredException e){}
                    }
                }).start();
            } // end if dia != null
        }catch(Exception e){
            e.printStackTrace(log);
        } finally{
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutVal);
        }
    }
    
    protected void pushKey(int key){
        robot.keyPress(key);
        eventTool.waitNoEvent(100);
        robot.keyRelease(key);
    }
    
    protected void pushShortcut(int key, int mask){
        robot.keyPress(mask);
        eventTool.waitNoEvent(100);
        pushKey(key);
        eventTool.waitNoEvent(100);
        robot.keyRelease(mask);
    }
    
    protected void log(String message){
        if (log != null){
            log.println(message);
        }
    }
    
}
