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
package org.netbeans.test.umllib.vrf;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.EditControlOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.actions.BackgroundColorElementAction;
import org.netbeans.test.umllib.actions.BorderColorElementAction;
import org.netbeans.test.umllib.actions.DeleteElementAction;
import org.netbeans.test.umllib.actions.FontColorElementAction;
import org.netbeans.test.umllib.actions.FontElementAction;
import org.netbeans.test.umllib.actions.HideChildrenAllLevelsElementAction;
import org.netbeans.test.umllib.actions.HideChildrenOneLevelElementAction;
import org.netbeans.test.umllib.actions.HideParentsAllLevelsElementAction;
import org.netbeans.test.umllib.actions.HideParentsOneLevelElementAction;
import org.netbeans.test.umllib.actions.InvertSelectionElementAction;
import org.netbeans.test.umllib.actions.LockEditElementAction;
import org.netbeans.test.umllib.actions.SelectAllElementAction;
import org.netbeans.test.umllib.actions.SelectAllSimilarElementAction;
import org.netbeans.test.umllib.actions.ShowChildrenAllLevelsElementAction;
import org.netbeans.test.umllib.actions.ShowChildrenOneLevelElementAction;
import org.netbeans.test.umllib.actions.ShowParentsAllLevelsElementAction;
import org.netbeans.test.umllib.actions.ShowParentsOneLevelElementAction;
import org.netbeans.test.umllib.customelements.LifelineOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.util.PopupConstants;

public class DiagramElementVerifier extends GenericVerifier {

    private ElementTypes elementType = null;
    private String prefix = "";

    public DiagramElementVerifier(DiagramOperator dia, ElementTypes elementType) {
        this(dia, elementType, "", null);
    }

    public DiagramElementVerifier(DiagramOperator dia, ElementTypes elementType, String prefix) {
        this(dia, elementType, prefix, null);
    }

    public DiagramElementVerifier(DiagramOperator dia, ElementTypes elementType, PrintStream log) {
        this(dia, elementType, "", log);
    }

    public DiagramElementVerifier(DiagramOperator dia, ElementTypes elementType, String prefix, PrintStream log) {
        super(dia, log);
        this.elementType = elementType;
        this.prefix = prefix;
    }

    public boolean checkCopyPasteByPopup() {
        final String EL_NAME = prefix + "CpyP";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            DiagramElementOperator elem = createElement(EL_NAME, elementType);
            eventTool.waitNoEvent(500);
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            DiagramElementOperator el = getElement(EL_NAME);
            //now coping element
            JPopupMenuOperator popup = el.getPopup();
            JMenuItemOperator item = popup.showMenuItem(PopupConstants.COPY);
            if (!item.isEnabled()) {
                log("Popup menu item " + PopupConstants.COPY + " disabled");
                return false;
            }
            item.clickMouse();

            //now pasting element
            eventTool.waitNoEvent(500);
            // Point point = dia.getDrawingArea().getFreePoint(100);
            Point point = new Point(300, 300);
            //6.5 workaround 
            dia.getDrawingArea().clickMouse(point.x, point.y, 1, true);
            dia.getDrawingArea().clickForPopup(point.x, point.y);
            popup = new JPopupMenuOperator();
            item = popup.showMenuItem(PopupConstants.PASTE);
            if (!item.isEnabled()) {
                log("Popup menu item " + PopupConstants.PASTE + " disabled");
                return false;
            }
            item.clickMouse();
            dia.getDrawingArea().clickMouse(point.x, point.y, 1);
            eventTool.waitNoEvent(1000);

            //now lets check that there are two copies:
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            getElement(EL_NAME, elementType, 0).select();
            eventTool.waitNoEvent(500);
            getElement(EL_NAME, elementType, 1).select();

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkCopyPasteByShortcut() {
        final String EL_NAME = prefix + "CpyS";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            createElement(EL_NAME, elementType);
            eventTool.waitNoEvent(500);
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            DiagramElementOperator el = getElement(EL_NAME);

            //now coping element
            el.select();
            pushShortcut(KeyEvent.VK_C, KeyEvent.VK_CONTROL);
            eventTool.waitNoEvent(1000);

            //now pasting element
            eventTool.waitNoEvent(500);
            Point point = dia.getDrawingArea().getFreePoint(100);
            dia.getDrawingArea().clickMouse(point.x, point.y, 1);
            pushShortcut(KeyEvent.VK_V, KeyEvent.VK_CONTROL);
            dia.getDrawingArea().clickMouse(point.x, point.y, 1);

            //now lets check that there are two copies:
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            getElement(EL_NAME, elementType, 0).select();
            eventTool.waitNoEvent(500);
            getElement(EL_NAME, elementType, 1).select();

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkCutPasteByPopup() {
        final String EL_NAME = prefix + "CutP";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            createElement(EL_NAME, elementType);
            eventTool.waitNoEvent(500);
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            DiagramElementOperator el = getElement(EL_NAME, elementType, 0);

            //Cut element
            JPopupMenuOperator popup = el.getPopup();
            JMenuItemOperator item = popup.showMenuItem(PopupConstants.CUT);
            if (!item.isEnabled()) {
                log("Popup menu item " + PopupConstants.CUT + " disabled");
                return false;
            }
            item.clickMouse();
            eventTool.waitNoEvent(1000);

            //checking that element has disappeared
            long timeoutVal = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 2000);
            try {
                getElement(EL_NAME, elementType, 0);
                log("Element still present on diagram after " + PopupConstants.CUT + " action");
                return false;
            } catch (Exception e) {
            } finally {
                JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutVal);
            }

            //now pasting element
            eventTool.waitNoEvent(500);
            Point point = dia.getDrawingArea().getFreePoint(100);
            dia.getDrawingArea().getPopup();
            popup = new JPopupMenuOperator();
            item = popup.showMenuItem(PopupConstants.PASTE);
            if (!item.isEnabled()) {
                log("Popup menu item " + PopupConstants.PASTE + " disabled");
                return false;
            }
            item.clickMouse();
            dia.getDrawingArea().clickMouse(point.x, point.y, 1);
            eventTool.waitNoEvent(1000);

            //now checking that element appeared on diagram:
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            el = getElement(EL_NAME, elementType, 0);
            el.select();

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkCutPasteByShortcut() {
        final String EL_NAME = prefix + "CutS";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            createElement(EL_NAME, elementType);
            eventTool.waitNoEvent(500);
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            DiagramElementOperator el = getElement(EL_NAME, elementType, 0);

            //Cut element
            el.select();
            pushShortcut(KeyEvent.VK_X, KeyEvent.VK_CONTROL);
            eventTool.waitNoEvent(1000);

            //checking that element has disappeared
            long timeoutVal = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 2000);
            try {
                getElement(EL_NAME, elementType, 0);
                log("Element still present on diagram after " + PopupConstants.CUT + " action");
                return false;
            } catch (Exception e) {
            } finally {
                JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutVal);
            }

            //now pasting element
            eventTool.waitNoEvent(500);
            Point point = dia.getDrawingArea().getFreePoint(100);
            dia.getDrawingArea().clickMouse(point.x, point.y, 1);
            pushShortcut(KeyEvent.VK_V, KeyEvent.VK_CONTROL);
            dia.getDrawingArea().clickMouse(point.x, point.y, 1);
            eventTool.waitNoEvent(1000);

            //now checking that element appeared on diagram:
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            el = getElement(EL_NAME, elementType, 0);
            el.select();

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkDeleteByPopup() {
        final String EL_NAME = prefix + "DelP";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            createElement(EL_NAME, elementType);
            eventTool.waitNoEvent(500);
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            final DiagramElementOperator el = getElement(EL_NAME, elementType, 0);

            //deleting
            new Thread(new Runnable() {

                public void run() {
                    eventTool.waitNoEvent(500);
                    new JCheckBoxOperator(new JDialogOperator(DELETE_DLG)).clickMouse();
                    new JButtonOperator(new JDialogOperator(DELETE_DLG), YES_BTN).push();
                }
            }).start();
            new Thread(new Runnable() {

                public void run() {
                    eventTool.waitNoEvent(500);
                    new JButtonOperator(new JDialogOperator(DELETE_PKG_DLG), YES_BTN).push();
                }
            }).start();

            new DeleteElementAction().performPopup(el);
            eventTool.waitNoEvent(1500);

            //checking the element has disappeared
            long timeoutVal = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 2000);
            try {
                getElement(EL_NAME, elementType, 0);
                log("Element still present on diagram after " + PopupConstants.DELETE + " action");
                return false;
            } catch (Exception e) {
            } finally {
                JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutVal);
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkDeleteByShortcut() {
        final String EL_NAME = prefix + "DelS";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            createElement(EL_NAME, elementType);
            eventTool.waitNoEvent(500);
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            final DiagramElementOperator el = getElement(EL_NAME, elementType, 0);
            el.select();

            //deleting
            new Thread(new Runnable() {

                public void run() {
                    eventTool.waitNoEvent(500);
                    new JCheckBoxOperator(new JDialogOperator(DELETE_DLG)).clickMouse();
                    new JButtonOperator(new JDialogOperator(DELETE_DLG), YES_BTN).push();
                }
            }).start();
            new Thread(new Runnable() {

                public void run() {
                    eventTool.waitNoEvent(500);
                    new JButtonOperator(new JDialogOperator(DELETE_PKG_DLG), YES_BTN).push();
                }
            }).start();
            pushKey(KeyEvent.VK_DELETE);
            eventTool.waitNoEvent(1500);

            //checking the element has disappeared
            long timeoutVal = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 2000);
            try {
                getElement(EL_NAME, elementType, 0);
                log("Element still present on diagram after " + PopupConstants.DELETE + " action");
                return false;
            } catch (Exception e) {
            } finally {
                JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutVal);
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkLockEdit() {
        final String EL_NAME = prefix + "Lck";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            createElement(EL_NAME, elementType);

            eventTool.waitNoEvent(500);
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);

            DiagramElementOperator el = getElement(EL_NAME, elementType, 0);

            //lock edit
            new LockEditElementAction().performPopup(el);
            eventTool.waitNoEvent(500);

            //trying to edit object
            el = getElement(EL_NAME, elementType, 0);
            CompartmentOperator comp = new CompartmentOperator(el, CompartmentTypes.NAME_COMPARTMENT);
            comp.clickOnCenter(2, InputEvent.BUTTON1_MASK);
            eventTool.waitNoEvent(1000);

            long timeoutVal = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
            try {
                EditControlOperator editcontrol = new EditControlOperator();
                log("Edit box appears after double-click on locked element but should not be");
                return false;
            } catch (Exception e) {
            } finally {
                JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutVal);
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkSelectAllByPopup(ElementTypes[] elementTypes) {
        final String EL_NAME = prefix + "SelAP";//+elementType.toString().substring(0, 2);

        try {
            createElement(EL_NAME, elementType);
            eventTool.waitNoEvent(500);
            Point p = null;
            for (int i = 0; i < elementTypes.length; i++) {
                p = dia.getDrawingArea().getFreePoint(100);
                createElement(EL_NAME + i, elementTypes[i], p.x, p.y);
                eventTool.waitNoEvent(500);
            }

            eventTool.waitNoEvent(500);
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);

            //select all
            DiagramElementOperator el = getElement(EL_NAME, elementType, 0);
            new SelectAllElementAction().performPopup(el);
            eventTool.waitNoEvent(500);

            //checking everything was selected

            el = getElement(EL_NAME, elementType, 0);
            if (!el.isSelected()) {
                log("Element '" + EL_NAME + "' not selected");
                return false;
            }

            for (int i = 0; i < elementTypes.length; i++) {
                el = getElement(EL_NAME + i, elementTypes[i], 0);
                if (!el.isSelected()) {
                    log("Element '" + EL_NAME + i + "' not selected");
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkSelectAllByShortcut(ElementTypes[] elementTypes) {
        final String EL_NAME = prefix + "SelAS";//+elementType.toString().substring(0, 2);

        try {
            createElement(EL_NAME, elementType);
            eventTool.waitNoEvent(500);
            Point p = null;
            for (int i = 0; i < elementTypes.length; i++) {
                p = dia.getDrawingArea().getFreePoint(100);
                createElement(EL_NAME + i, elementTypes[i]);
                eventTool.waitNoEvent(500);
            }

            eventTool.waitNoEvent(500);
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);

            //select all
            Point point = dia.getDrawingArea().getFreePoint();
            dia.getDrawingArea().clickMouse(point.x, point.y, 1);
            pushShortcut(KeyEvent.VK_A, KeyEvent.VK_CONTROL);
            eventTool.waitNoEvent(500);

            //checking everything was selected

            DiagramElementOperator el = getElement(EL_NAME, elementType, 0);
            if (!el.isSelected()) {
                log("Element '" + EL_NAME + "' not selected");
                return false;
            }

            for (int i = 0; i < elementTypes.length; i++) {
                el = getElement(EL_NAME + i, elementTypes[i], 0);
                if (!el.isSelected()) {
                    log("Element '" + EL_NAME + i + "' not selected");
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkSelectAllSimilar(ElementTypes[] typesToPut) {
        final String EL_NAME = prefix + "SelS";//+elementType.toString().substring(0, 2);

        try {
            Point point = dia.getDrawingArea().getFreePoint();
            createElement(EL_NAME, elementType, point.x, point.y);
            eventTool.waitNoEvent(500);

            for (int i = 0; i < typesToPut.length; i++) {
                point = dia.getDrawingArea().getFreePoint(100);
                createElement(EL_NAME + i, typesToPut[i], point.x, point.y);
                eventTool.waitNoEvent(500);
            }

            eventTool.waitNoEvent(500);
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);

            //select all
            DiagramElementOperator el = getElement(EL_NAME, elementType, 0);
            new SelectAllSimilarElementAction().performPopup(el);
            eventTool.waitNoEvent(500);

            //checking everything was selected correctly
            el = getElement(EL_NAME, elementType, 0);
            if (!el.isSelected()) {
                log("Element '" + EL_NAME + "' not selected");
                return false;
            }

            for (int i = 0; i < typesToPut.length; i++) {
                el = getElement(EL_NAME + i, typesToPut[i], 0);
                if (el.getElementType().equals(elementType.toString()) && !el.isSelected()) {
                    log("Element '" + EL_NAME + i + "' is not selected");
                    return false;
                }
                if (!el.getElementType().equals(elementType.toString()) && el.isSelected()) {
                    log("Element '" + EL_NAME + i + "' is selected");
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkInvertSelection(ElementTypes[] typesToSelect, ElementTypes[] typesNotToSelect) {
        final String EL_NAME = prefix + "Inv";//+elementType.toString().substring(0, 2);

        try {
            Point point = dia.getDrawingArea().getFreePoint();
            createElement(EL_NAME, elementType, point.x, point.y);
            eventTool.waitNoEvent(500);

            // 6.5 Workaround, getFreePoint() not quite working
            int c = 0;
            for (int i = 0; i < typesToSelect.length; i++) {
                c = c + 100;
                //6.1 point = dia.getDrawingArea().getFreePoint();
                point = new Point(50 + c, 110 + c);
                createElement(EL_NAME + "SEL" + i, typesToSelect[i], point.x, point.y);
                eventTool.waitNoEvent(500);
            }

            for (int i = 0; i < typesNotToSelect.length; i++) {
                c = c + 100;
                //6.1 point = dia.getDrawingArea().getFreePoint();
                point = new Point(50 + c, 100 + c);
                createElement(EL_NAME + "NOTSEL" + i, typesNotToSelect[i], point.x, point.y);
                eventTool.waitNoEvent(500);
            }

            eventTool.waitNoEvent(500);
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);

            //selecting and inverting selection
            DiagramElementOperator[] arr = new DiagramElementOperator[typesToSelect.length + 1];
            arr[0] = getElement(EL_NAME, elementType, 0);
            for (int i = 1; i < arr.length; i++) {
                arr[i] = getElement(EL_NAME + "SEL" + (i - 1), typesToSelect[i - 1], 0);
            }
            new InvertSelectionElementAction().performPopup(arr);
            eventTool.waitNoEvent(1000);

            //checking
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].isSelected()) {
                    log("Element '" + arr[i].toString() + "' is selected");
                    return false;
                }
            }

            for (int i = 0; i < typesNotToSelect.length; i++) {
                DiagramElementOperator el = getElement(EL_NAME + "NOTSEL" + i, typesNotToSelect[i], 0);
                if (!el.isSelected()) {
                    log("Element '" + EL_NAME + "NOTSEL" + i + "' is not selected");
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    /*
    public boolean checkResetEdges() throws ElementVerificationException{
    final String EL_NAME = prefix+"ResetEdges";//+elementType;
    
    try{
    //create main element:
    Point point = dia.getDrawingArea().getFreePoint();
    createElement(EL_NAME, elementType, point.x, point.y);
    
    eventTool.waitNoEvent(500);
    new SaveAllAction().performAPI();
    eventTool.waitNoEvent(500);
    
    DiagramElementOperator el = getElement(EL_NAME);
    //checking
    JPopupMenuOperator popup = el.getPopup();
    JMenuItemOperator item = popup.showMenuItem(PopupConstants.RESET_EDGES);
    if(item.isEnabled()){
    return false;
    }
    return true;
    }catch(Exception e){
    if (log != null){e.printStackTrace(log);}
    throw new ElementVerificationException(e);
    }finally{
    safeDeleteElement(EL_NAME);
    }
    }
     */
    /*
    public boolean checkResizeElementToContents(){
    final String EL_NAME = prefix+"ResToCont";//+elementType.toString().substring(0, 2);
    
    try{
    //create main element:
    Point point = dia.getDrawingArea().getFreePoint();
    DiagramElementOperator el = createElement(EL_NAME, elementType, point.x, point.y);
    
    el.resize(200, 200);
    
    eventTool.waitNoEvent(500);
    new SaveAllAction().performAPI();
    eventTool.waitNoEvent(500);
    
    el = getElement(EL_NAME);
    new ResizeElementToContentsElementAction().performPopup(el);
    eventTool.waitNoEvent(500);
    Rectangle rect = el.getElementRectangle();
    
    //checking the size
    //TODO: This check is incorrect!. Another check should be added. Now it is just
    //checked that the size has changed
    el = getElement(EL_NAME);
    el.getGraphObject().sizeToContents();
    eventTool.waitNoEvent(500);
    Rectangle rectReset = el.getElementRectangle();
    
    if (!(rectReset.width==rect.width && rectReset.height==rect.height)){
    return false;
    }
    return true;
    }catch(Exception e){
    if (log != null){e.printStackTrace(log);}
    return false;
    }finally{
    safeDeleteAllElements();
    }
    }
     */
    public boolean checkHideChildrenOneLevel(int levelsNum, int elementsOnLevel, LinkTypes linkType, ElementTypes childType) {
        final String EL_NAME = "HChOne";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            Point point = dia.getDrawingArea().getFreePoint();
            DiagramElementOperator el = createElement(EL_NAME, elementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            //creating children Elements

            ArrayList<String> elementNames = createLinkedElements(el, 0, elementsOnLevel, levelsNum, EL_NAME, linkType, childType, false);

            //hiding children 1 level
            eventTool.waitNoEvent(500);
            dia.toolbar().selectDefault();
            new HideChildrenOneLevelElementAction().performPopup(el);
            eventTool.waitNoEvent(500);

            //gathering 1 level children element' names
            ArrayList<String> children = new ArrayList<String>();
            for (int j = 0; j < elementsOnLevel; j++) {
                children.add(EL_NAME + "C" + j);
            }
            eventTool.waitNoEvent(1000);

            //checking everything was hidden correctly
            long timeoutVal = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 2000);
            try {
                for (int i = 0; i < elementNames.size(); i++) {
                    String elName = elementNames.get(i);
                    if (children.indexOf(elName) > -1) {
                        try {
                            DiagramElementOperator op = new DiagramElementOperator(dia, elName, childType, 0);
                            log("Element '" + elName + "' not found on diagram");
                            return false;
                        } catch (Exception tee) {
                        }
                    } else {
                        new DiagramElementOperator(dia, elName);
                    }
                }
            } catch (Exception e) {
                if (log != null) {
                    e.printStackTrace(log);
                }
                return false;
            } finally {
                JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutVal);
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkHideChildrenAllLevels(int levelsNum, int elementsOnLevel, LinkTypes linkType, ElementTypes childType) {
        final String EL_NAME = "HChAll";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            Point point = dia.getDrawingArea().getFreePoint();
            DiagramElementOperator el = createElement(EL_NAME, elementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            //creating children Elements
            ArrayList<String> elementNames = createLinkedElements(el, 0, elementsOnLevel, levelsNum, EL_NAME, linkType, childType, false);

            //hiding children 1 level
            eventTool.waitNoEvent(500);
            dia.toolbar().selectDefault();
            new HideChildrenAllLevelsElementAction().performPopup(el);
            eventTool.waitNoEvent(1000);

            //checking everything was hidden correctly
            long timeoutVal = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 2000);
            try {
                for (int i = 0; i < elementNames.size(); i++) {
                    String elName = elementNames.get(i);
                    try {
                        DiagramElementOperator op = new DiagramElementOperator(dia, elName, childType, 0);
                        log("Element '" + elName + "' not found on diagram");
                        return false;
                    } catch (Exception tee) {
                    }
                }
            } catch (Exception e) {
                if (log != null) {
                    e.printStackTrace(log);
                }
                return false;
            } finally {
                JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutVal);
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkHideParentsOneLevel(int levelsNum, int elementsOnLevel, LinkTypes linkType, ElementTypes childType) {
        final String EL_NAME = "HPOne";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            Point point = dia.getDrawingArea().getFreePoint();
            DiagramElementOperator el = createElement(EL_NAME, elementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            //creating children Elements
            ArrayList<String> elementNames = createLinkedElements(el, 0, elementsOnLevel, levelsNum, EL_NAME, linkType, childType, true);

            //hiding children 1 level
            eventTool.waitNoEvent(500);
            dia.toolbar().selectDefault();
            new HideParentsOneLevelElementAction().performPopup(el);
            eventTool.waitNoEvent(500);

            //gathering 1 level children element' names
            ArrayList<String> parents = new ArrayList<String>();
            for (int j = 0; j < elementsOnLevel; j++) {
                parents.add(EL_NAME + "C" + j);
            }
            eventTool.waitNoEvent(1000);

            //checking everything was hidden correctly
            long timeoutVal = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 2000);
            try {
                for (int i = 0; i < elementNames.size(); i++) {
                    String elName = elementNames.get(i);
                    if (parents.indexOf(elName) > -1) {
                        try {
                            DiagramElementOperator op = new DiagramElementOperator(dia, elName, childType, 0);
                            log("Element '" + elName + "' not found on diagram");
                            return false;
                        //eventTool.waitNoEvent(5000);
                        //throw new ElementVerificationException(elName+op.getGraphObject().isVisible(),new Exception());
                        } catch (Exception tee) {
                        }
                    } else {
                        new DiagramElementOperator(dia, elName);
                    }
                }
            } catch (Exception e) {
                if (log != null) {
                    e.printStackTrace(log);
                }
                return false;
            } finally {
                JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutVal);
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkHideParentsAllLevels(int levelsNum, int elementsOnLevel, LinkTypes linkType, ElementTypes childType) {
        final String EL_NAME = "HPAll";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            Point point = dia.getDrawingArea().getFreePoint();
            DiagramElementOperator el = createElement(EL_NAME, elementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            //creating children Elements
            ArrayList<String> elementNames = createLinkedElements(el, 0, elementsOnLevel, levelsNum, EL_NAME, linkType, childType, true);

            //hiding children 1 level
            eventTool.waitNoEvent(500);
            dia.toolbar().selectDefault();
            new HideParentsAllLevelsElementAction().performPopup(el);
            eventTool.waitNoEvent(1000);

            //checking everything was hidden correctly
            long timeoutVal = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 2000);
            try {
                for (int i = 0; i < elementNames.size(); i++) {
                    String elName = elementNames.get(i);
                    try {
                        DiagramElementOperator op = new DiagramElementOperator(dia, elName, childType, 0);
                        log("Element '" + elName + "' not found on diagram");
                        return false;
                    } catch (Exception tee) {
                    }
                }
            } catch (Exception e) {
                if (log != null) {
                    e.printStackTrace(log);
                }
                return false;
            } finally {
                JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutVal);
            }
            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkShowChildrenOneLevel(int levelsNum, int elementsOnLevel, LinkTypes linkType, ElementTypes childType) {
        final String EL_NAME = "ShChOne";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            Point point = dia.getDrawingArea().getFreePoint();
            DiagramElementOperator el = createElement(EL_NAME, elementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            //creating children Elements
            ArrayList<String> elementNames = createLinkedElements(el, 0, elementsOnLevel, levelsNum, EL_NAME, linkType, childType, false);

            //hiding children 1 level
            eventTool.waitNoEvent(500);
            dia.toolbar().selectDefault();
            new HideChildrenAllLevelsElementAction().performPopup(el);
            eventTool.waitNoEvent(500);
            new ShowChildrenOneLevelElementAction().performPopup(el);

            //gathering 1 level linked element' names
            ArrayList<String> children = new ArrayList<String>();
            for (int j = 0; j < elementsOnLevel; j++) {
                children.add(EL_NAME + "C" + j);
            }
            eventTool.waitNoEvent(1000);

            //checking everything was hidden correctly
            long timeoutVal = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 2000);
            try {
                for (int i = 0; i < elementNames.size(); i++) {
                    String elName = elementNames.get(i);
                    if (!(children.indexOf(elName) > -1)) {
                        try {
                            DiagramElementOperator op = new DiagramElementOperator(dia, elName, childType, 0);
                            log("Element '" + elName + "' not found on diagram");
                            return false;
                        } catch (Exception tee) {
                        }
                    } else {
                        DiagramElementOperator childEl = new DiagramElementOperator(dia, elName);
                        new LinkOperator(childEl, el, linkType);
                    }
                }
            } catch (Exception e) {
                if (log != null) {
                    e.printStackTrace(log);
                }
                return false;
            } finally {
                JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutVal);
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkShowChildrenAllLevels(int levelsNum, int elementsOnLevel, LinkTypes linkType, ElementTypes childType) {
        final String EL_NAME = "ShChAll";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            Point point = dia.getDrawingArea().getFreePoint();
            DiagramElementOperator el = createElement(EL_NAME, elementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            //creating children Elements
            ArrayList<String> elementNames = createLinkedElements(el, 0, elementsOnLevel, levelsNum, EL_NAME, linkType, childType, false);

            //hiding children 1 level
            eventTool.waitNoEvent(500);
            dia.toolbar().selectDefault();
            new HideChildrenAllLevelsElementAction().performPopup(el);
            eventTool.waitNoEvent(500);
            new ShowChildrenAllLevelsElementAction().performPopup(el);
            eventTool.waitNoEvent(1000);

            //checking everything was hidden correctly
            long timeoutVal = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 2000);
            try {
                for (int i = 0; i < elementNames.size(); i++) {
                    String elName = elementNames.get(i);
                    new DiagramElementOperator(dia, elName);
                }
            } catch (Exception e) {
                if (log != null) {
                    e.printStackTrace(log);
                }
                return false;
            } finally {
                JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutVal);
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkShowParentsOneLevel(int levelsNum, int elementsOnLevel, LinkTypes linkType, ElementTypes childType) {
        final String EL_NAME = "ShPOne";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            Point point = dia.getDrawingArea().getFreePoint();
            DiagramElementOperator el = createElement(EL_NAME, elementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            //creating children Elements
            ArrayList<String> elementNames = createLinkedElements(el, 0, elementsOnLevel, levelsNum, EL_NAME, linkType, childType, true);

            //hiding children 1 level
            eventTool.waitNoEvent(500);
            dia.toolbar().selectDefault();
            new HideParentsAllLevelsElementAction().performPopup(el);
            eventTool.waitNoEvent(500);
            new ShowParentsOneLevelElementAction().performPopup(el);

            //gathering 1 level children element' names
            ArrayList<String> parents = new ArrayList<String>();
            for (int j = 0; j < elementsOnLevel; j++) {
                parents.add(EL_NAME + "C" + j);
            }
            eventTool.waitNoEvent(1000);

            //checking everything was hidden correctly
            long timeoutVal = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 2000);
            try {
                for (int i = 0; i < elementNames.size(); i++) {
                    String elName = elementNames.get(i);
                    if (!(parents.indexOf(elName) > -1)) {
                        try {
                            DiagramElementOperator op = new DiagramElementOperator(dia, elName, childType, 0);
                            log("Element '" + elName + "' not found on diagram");
                            return false;
                        } catch (Exception tee) {
                        }
                    } else {
                        new LinkOperator(el, new DiagramElementOperator(dia, elName), linkType);
                    }
                }
            } catch (Exception e) {
                if (log != null) {
                    e.printStackTrace(log);
                }
                return false;
            } finally {
                JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutVal);
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkShowParentsAllLevels(int levelsNum, int elementsOnLevel, LinkTypes linkType, ElementTypes childType) {
        final String EL_NAME = "ShPAll";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            Point point = dia.getDrawingArea().getFreePoint();
            DiagramElementOperator el = createElement(EL_NAME, elementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            //creating children Elements
            ArrayList<String> elementNames = createLinkedElements(el, 0, elementsOnLevel, levelsNum, EL_NAME, linkType, childType, true);

            //hiding children 1 level
            eventTool.waitNoEvent(500);
            dia.toolbar().selectDefault();
            new HideParentsAllLevelsElementAction().performPopup(el);
            eventTool.waitNoEvent(500);
            new ShowParentsAllLevelsElementAction().performPopup(el);
            eventTool.waitNoEvent(1000);

            //checking everything was hidden correctly
            long timeoutVal = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 2000);
            try {
                for (int i = 0; i < elementNames.size(); i++) {
                    String elName = elementNames.get(i);
                    DiagramElementOperator op = new DiagramElementOperator(dia, elName, childType, 0);
                }
            } catch (Exception e) {
                if (log != null) {
                    e.printStackTrace(log);
                }
                return false;
            } finally {
                JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutVal);
            }
            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkFont(int fontSize, String fontFamily, boolean isBold, boolean isItalic) {
        final String EL_NAME = "Fnt";// + elementType.toString().substring(0, 2);

        try {
            //create main element:
            Point point = dia.getDrawingArea().getFreePoint();
            final DiagramElementOperator el = createElement(EL_NAME, elementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            //creating children Elements

            //invoking Font Dialof
            eventTool.waitNoEvent(500);
            new Thread(new  

                  Runnable() {




                     
                
            
                
            public void run() {
                    //new FontElementAction(elementType).performPopup(el);
                    new FontElementAction().performPopup(el);
                }
            }).start();
            
            //setting the font
            JDialogOperator fontDlg = new JDialogOperator(FONT_DLG);
            eventTool.waitNoEvent(1000);
            //setting font family
            if ((fontFamily != null) && (fontFamily.length() != 0)) {
                JListOperator list = new JListOperator(fontDlg, 0);
                int index = list.findItemIndex(fontFamily);
                log("index for element '" + fontFamily + " ' = " + index);
                if (index >= 0) {
                    list.selectItem(index);
                }
            }
            //setting font size
            new JListOperator(fontDlg, 1).selectItem(String.valueOf(fontSize));
            //setting bold
            new JCheckBoxOperator(fontDlg, BOLD_CHB).changeSelection(isBold);
            //setting italic
            new JCheckBoxOperator(fontDlg, ITALIC_CHB).changeSelection(isItalic);

            eventTool.waitNoEvent(500);
            new JButtonOperator(fontDlg, OK_BTN).push();

            eventTool.waitNoEvent(2000);

            //checking everything was changed correctly
            DiagramElementOperator elem = getElement(EL_NAME, elementType, 0);
            Font font = new CompartmentOperator(elem, CompartmentTypes.NAME_COMPARTMENT).getFont();

            if (font == null) {
                log("Font is null");
                return false;
            }

            if (!font.getFamily().equals(fontFamily) || !(font.getSize() == fontSize) || !(font.isBold() == isBold) || !(font.isItalic() == isItalic)) {
                log("Font failed: " + font.getFamily() + " " + font.getSize() + " " + font.isBold() + " " + font.isItalic());
                log(elem.getFont().toString());
                return false;
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkFont() {
        final String fontFamily = "Arial";
        final int fontSize = 24;
        final boolean isBold = true;
        final boolean isItalic = true;

        return checkFont(fontSize, fontFamily, isBold, isItalic);
    }

    public boolean checkBorderColor(int red, int green, int blue) {
        final String EL_NAME = "BrdClr";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            Point point = dia.getDrawingArea().getFreePoint();
            final DiagramElementOperator el = createElement(EL_NAME, elementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            //creating children Elements

            //invoking Font Dialof
            eventTool.waitNoEvent(500);
            new Thread(new  

                  Runnable() {




                     
                
            
                
            public void run() {
                    new BorderColorElementAction().performPopup(el);
                }
            }).start();
            
            //setting the color
            setColor(new JDialogOperator(),  red, green, blue);
            
            eventTool.waitNoEvent(2000);

            //checking everything was changed correctly
            DiagramElementOperator elem = getElement(EL_NAME, elementType, 0);
            Color color = new CompartmentOperator(elem, CompartmentTypes.NAME_COMPARTMENT).getBorderColor();
            if (!colorsEqual(color, new Color(red, green, blue))) {
                log("Border color failed: (" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")");
                return false;
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkBackgroundColor(int red, int green, int blue) {
        final String EL_NAME = "BgClr";//+elementType.toString().substring(0, 2);

        Color checkColor = null;
        try {
            //create main element:
            Point point = dia.getDrawingArea().getFreePoint();
            final DiagramElementOperator el = createElement(EL_NAME, elementType, point.x, point.y);
            eventTool.waitNoEvent(500);

            // remember old fillColor and lightGradientFillColor
            CompartmentOperator elCompartment = new CompartmentOperator(el, CompartmentTypes.NAME_COMPARTMENT);
            Color fillColorOld = elCompartment.getFillColor();
            Color lightGradientFillColorOld = elCompartment.getLightGradientFillColor();

            //invoking Font Dialof
            eventTool.waitNoEvent(500);
            new Thread(new  

                  Runnable() {




                     
                
            
                
            public void run() {
                    new BackgroundColorElementAction().performPopup(el);
                }
            }).start();
            
            //setting the color
            setColor(new JDialogOperator(),  red, green, blue);
            
            eventTool.waitNoEvent(2000);

            // get new fillColor and lightGradientFillColor
            DiagramElementOperator elem = getElement(EL_NAME, elementType, 0);
            CompartmentOperator elemCompartment = new CompartmentOperator(elem, CompartmentTypes.NAME_COMPARTMENT);
            Color fillColorNew = elemCompartment.getFillColor();
            Color lightGradientFillColorNew = elemCompartment.getLightGradientFillColor();
            //Select which of colors was changed
            if (!colorsEqual(fillColorOld, fillColorNew)) {
                checkColor = fillColorNew;
            } else if (!colorsEqual(lightGradientFillColorOld, lightGradientFillColorNew)) {
                checkColor = lightGradientFillColorNew;
            } else {
                log("Neither fillColor Nor lightGradientFillColor was changed");
                return false;
            }

            //checking everything was changed correctly
            if (!colorsEqual(checkColor, new Color(red, green, blue))) {
                log("Background color failed: (" + checkColor.getRed() + ", " + checkColor.getGreen() + ", " + checkColor.getBlue() + ")");
                return false;
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

    public boolean checkFontColor(int red, int green, int blue) {
        final String EL_NAME = "FntClr";//+elementType.toString().substring(0, 2);

        try {
            //create main element:
            Point point = dia.getDrawingArea().getFreePoint();
            final DiagramElementOperator el = createElement(EL_NAME, elementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            //creating children Elements

            //invoking Font Dialof
            eventTool.waitNoEvent(500);
            new Thread(new  

                  Runnable() {




                     
                
            
                
            public void run() {
                    new FontColorElementAction().performPopup(el);
                }
            }).start();
            
            //setting the color
            setColor(new JDialogOperator(),  red, green, blue);
            
            eventTool.waitNoEvent(2000);

            //checking everything was changed correctly
            DiagramElementOperator elem = getElement(EL_NAME, elementType, 0);
            Color color = new CompartmentOperator(elem, CompartmentTypes.NAME_COMPARTMENT).getFontColor();

            if (!colorsEqual(color, new Color(red, green, blue))) {
                log("Font color failed: (" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")");
                return false;
            }

            return true;
        } catch (Exception e) {
            if (log != null) {
                e.printStackTrace(log);
            }
            return false;
        } finally {
            safeDeleteAllElements();
        }
    }

//------------------------------------------------------------------------------
    private boolean colorsEqual(Color color1, Color color2) {
        boolean equality = false;
        if ((color1 != null) && (color2 != null)) {
            equality = ((color1.getRed() == color2.getRed()) && (color1.getGreen() == color2.getGreen()) && (color1.getBlue() == color2.getBlue()));
        }
        return equality;

    }

    private void setColor(JDialogOperator dlg, int red, int green, int blue) {
        JTabbedPaneOperator tabbedPane = new JTabbedPaneOperator(dlg);
        tabbedPane.selectPage("RGB");

        new JTextComponentOperator(tabbedPane, 0).clearText();
        new JTextComponentOperator(tabbedPane, 0).typeText(String.valueOf(red));

        new JTextComponentOperator(tabbedPane, 1).clearText();
        new JTextComponentOperator(tabbedPane, 1).typeText(String.valueOf(green));

        new JTextComponentOperator(tabbedPane, 2).clearText();
        new JTextComponentOperator(tabbedPane, 2).typeText(String.valueOf(blue));

        eventTool.waitNoEvent(3500);
        JButtonOperator btn = new JButtonOperator(dlg, OK_BTN);
        new MouseRobotDriver(new Timeout("", 10)).clickMouse(btn, btn.getCenterXForClick(), btn.getCenterYForClick(), 1, InputEvent.BUTTON1_MASK, 0, new Timeout("", 10));
    }

    private ArrayList<String> getStandardLinkedElementNames(String baseName, int level, int elNum, int levelsTotal) {
        ArrayList<String> names = new ArrayList<String>();
        if (level >= levelsTotal) {
            return names;
        }
        for (int i = 0; i < elNum; i++) {
            names.add(baseName + "C" + i);
            ArrayList<String> al = getStandardLinkedElementNames(baseName + "C" + i, level + 1, elNum, levelsTotal);
            names.addAll(al);
        }
        return names;
    }

    private ArrayList<String> createLinkedElements(DiagramElementOperator parent, int level, int elementsOnLevel, int levelsTotal, String baseName, LinkTypes linkType, ElementTypes childType, boolean createParents) throws NotFoundException {
        ArrayList<String> names = new ArrayList<String>();
        if (level >= levelsTotal) {
            return names;
        }
        for (int i = 0; i < elementsOnLevel; i++) {
            DiagramElementOperator el = createLinkedElement(parent, childType, linkType, baseName + "C" + i, createParents);
            names.add(baseName + "C" + i);
            ArrayList<String> al = createLinkedElements(el, level + 1, elementsOnLevel, levelsTotal, baseName + "C" + i, linkType, childType, createParents);
            names.addAll(al);
        }
        return names;
    }

    private DiagramElementOperator createLinkedElement(DiagramElementOperator existingEl, ElementTypes newElType, LinkTypes linkType, String childName, boolean createParent) throws NotFoundException {
        Point point = dia.getDrawingArea().getFreePoint(140);
        DiagramElementOperator el = createElement(childName, newElType, point.x, point.y);
        eventTool.waitNoEvent(500);
        if (createParent) {
            dia.createLinkOnDiagram(linkType, existingEl, el);
        } else {
            dia.createLinkOnDiagram(linkType, el, existingEl);
        }
        eventTool.waitNoEvent(500);
        return el;
    }

    public void safeDeleteElement(String elementName) {
        long timeoutVal = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
        JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 2000);
        eventTool.waitNoEvent(500);
        try {
            final DiagramElementOperator el = new DiagramElementOperator(dia, elementName);
            //deleting the element
            new Thread(new  

                  Runnable() {



                      
                       
                      
                        public void run() {
                    new JCheckBoxOperator(new JDialogOperator(DELETE_DLG)).clickMouse();
                    new JButtonOperator(new JDialogOperator(DELETE_DLG), YES_BTN).push();
                    if (el.getElementType().equals(ElementTypes.PACKAGE.toString())){
                        eventTool.waitNoEvent(500);
                        new JButtonOperator(new JDialogOperator(DELETE_PKG_DLG), YES_BTN).push();
                    }
                }
            }).start();

            new DeleteElementAction().performShortcut(el);
        } catch (Exception e) {
        } finally {
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutVal);
        }
    }
}
