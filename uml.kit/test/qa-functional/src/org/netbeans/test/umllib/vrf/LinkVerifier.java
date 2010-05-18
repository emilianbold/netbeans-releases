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

import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.customelements.LifelineOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.exceptions.UnexpectedElementSelectionException;
import org.netbeans.test.umllib.util.PopupConstants;

public class LinkVerifier  extends GenericVerifier{
    private LinkTypes linkType = null;
    private String prefix = "";
    
    public LinkVerifier(DiagramOperator dia, LinkTypes linkType) {
        this(dia, linkType, "", null);
    }

    public LinkVerifier(DiagramOperator dia, LinkTypes linkType, String prefix) {
        this(dia, linkType, prefix, null);
    }
    
    public LinkVerifier(DiagramOperator dia, LinkTypes linkType, PrintStream log) {
        this(dia, linkType, "", log);
    }
    
    public LinkVerifier(DiagramOperator dia, LinkTypes linkType, String prefix, PrintStream log) {
        super(dia, log);
        this.linkType = linkType;
        this.prefix = prefix;
    }
    

    
    public boolean checkDeleteByPopup(ElementTypes sourceElementType, ElementTypes targetElementType){
        final String EL_NAME = prefix + "DelP";// + linkType.toString().substring(0, 3);
        try{
            //create source element
            DiagramElementOperator el1 = createElement(EL_NAME + "S", sourceElementType);
            eventTool.waitNoEvent(500);
            
            //create target element
            //Point point = dia.getDrawingArea().getFreePoint(170);
            Point point = new Point(200, 300);
            DiagramElementOperator el2 = createElement(EL_NAME + "T", targetElementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            
            //create link
            dia.createGenericRelationshipOnDiagram(linkType, el1, el2);
            eventTool.waitNoEvent(500);

            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            
            //deleting
            new Thread(new Runnable() {
                public void run() {
                    eventTool.waitNoEvent(1000);
                    new JCheckBoxOperator(new JDialogOperator(DELETE_DLG)).clickMouse();
                    new JButtonOperator(new JDialogOperator(DELETE_DLG), YES_BTN).push();
                }
            }).start();
            
            LinkOperator link = new LinkOperator(el1, el2);
            JPopupMenuOperator popup = link.getPopup();
            //to close automatically opened submenu before searching for menu item
            eventTool.waitNoEvent(500);
            popup.pushKey(KeyEvent.VK_LEFT);
            popup = new JPopupMenuOperator();
            popup.pushMenu(PopupConstants.EDIT + "|" + PopupConstants.DELETE);
            eventTool.waitNoEvent(2000);
            
            //checking the link has disappeared
            long timeoutVal = JemmyProperties.getCurrentTimeout("LinkOperator.WaitLinkTime");
            JemmyProperties.setCurrentTimeout("LinkOperator.WaitLinkTime", 2000);
            try{
                new LinkOperator(el1, el2);
                log("Link still present on diagram after " + PopupConstants.DELETE + " action");
                return false;
            }catch(Exception e){
            }finally{
                JemmyProperties.setCurrentTimeout("LinkOperator.WaitLinkTime", timeoutVal);
            }
            
            return true;
        }catch(Exception e){
            if (log != null){e.printStackTrace(log);}
            return false;
        }finally{
            safeDeleteAllElements();
        }
    }

    public boolean checkDeleteByShortcut(ElementTypes sourceElementType, ElementTypes targetElementType){
        final String EL_NAME = prefix + "DelS";// + linkType.toString().substring(0, 3);
        try{
            //create source element
            DiagramElementOperator el1 = createElement(EL_NAME + "S", sourceElementType);
            eventTool.waitNoEvent(500);
            
            //create target element
            //Point point = dia.getDrawingArea().getFreePoint(170);
            Point point = new Point(200, 300);
            DiagramElementOperator el2 = createElement(EL_NAME + "T", targetElementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            
            //create link
            dia.createGenericRelationshipOnDiagram(linkType, el1, el2);
            eventTool.waitNoEvent(500);

            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            
            //deleting
            new Thread(new Runnable() {
                public void run() {
                    eventTool.waitNoEvent(1000);
                    new JCheckBoxOperator(new JDialogOperator(DELETE_DLG)).clickMouse();
                    new JButtonOperator(new JDialogOperator(DELETE_DLG), YES_BTN).push();
                }
            }).start();
            
            LinkOperator link = new LinkOperator(el1, el2);
            link.select();
            pushKey(KeyEvent.VK_DELETE);
            eventTool.waitNoEvent(2000);
            
            //checking the link has disappeared
            long timeoutVal = JemmyProperties.getCurrentTimeout("LinkOperator.WaitLinkTime");
            JemmyProperties.setCurrentTimeout("LinkOperator.WaitLinkTime", 2000);
            try{
                new LinkOperator(el1, el2);
                log("Link still present on diagram after " + PopupConstants.DELETE + " action");
                return false;
            }catch(Exception e){
            }finally{
                JemmyProperties.setCurrentTimeout("LinkOperator.WaitLinkTime", timeoutVal);
            }
            
            return true;
        }catch(Exception e){
            if (log != null){e.printStackTrace(log);}
            return false;
        }finally{
            safeDeleteAllElements();
        }
    }
    
    public boolean checkSelectAllByPopup(ElementTypes sourceElementType, ElementTypes targetElementType, LinkTypes lnkType){
        final String EL_NAME = prefix + "SelAllP";// + linkType.toString().substring(0, 3);
        try{
            //create source element
            DiagramElementOperator el1 = createElement(EL_NAME + "S", sourceElementType);
            eventTool.waitNoEvent(500);
            
            //create target element
            //Point point = dia.getDrawingArea().getFreePoint(170);
            Point point = new Point(200, 300);
            DiagramElementOperator el2 = createElement(EL_NAME + "T", targetElementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            
            //create link
            dia.createGenericRelationshipOnDiagram(linkType, el1, el2);
            eventTool.waitNoEvent(500);
            
            point = dia.getDrawingArea().getFreePoint(170);
            DiagramElementOperator el3 = createElement(EL_NAME + "S2", sourceElementType, point.x, point.y);
            eventTool.waitNoEvent(500);

            point = dia.getDrawingArea().getFreePoint(170);
            DiagramElementOperator el4 = createElement(EL_NAME + "T2", targetElementType, point.x, point.y);
            eventTool.waitNoEvent(500);

            dia.createGenericRelationshipOnDiagram(lnkType, el3, el4);
            eventTool.waitNoEvent(500);

            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            
            //select all
            LinkOperator link = new LinkOperator(el1, el2);
            JPopupMenuOperator popup = link.getPopup();
            //to close automatically opened submenu before searching for menu item
            eventTool.waitNoEvent(500);
            popup.pushKey(KeyEvent.VK_LEFT);
            popup = new JPopupMenuOperator();
            popup.pushMenu(PopupConstants.EDIT + "|" + PopupConstants.SELECT_ALL);
            eventTool.waitNoEvent(2000);
            
            //checking everything was selected
            
            el1 = getElement(EL_NAME + "S", sourceElementType, 0);
            if (!el1.isSelected()){
                log("Element '" + EL_NAME + "S" + "' not selected");
                return false;
            }
            el2 = getElement(EL_NAME + "T", targetElementType, 0);
            if (!el2.isSelected()){
                log("Element '" + EL_NAME + "T" + "' not selected");
                return false;
            }
            el3 = getElement(EL_NAME + "S2", sourceElementType, 0);
            if (!el3.isSelected()){
                log("Element '" + EL_NAME + "S2" + "' not selected");
                return false;
            }
            el4 = getElement(EL_NAME + "T2", targetElementType, 0);
            if (!el4.isSelected()){
                log("Element '" + EL_NAME + "T2" + "' not selected");
                return false;
            }
            
            LinkOperator lnk1 = new LinkOperator(el1, el2);
            if (!lnk1.isSelected()){
                log(lnk1.getType() + "(1) link not selected");
                return false;
            }
            LinkOperator lnk2 = new LinkOperator(el3, el4);
            if (!lnk2.isSelected()){
                log(lnk2.getType() + "(2) link not selected");
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

    public boolean checkInvertSelection(ElementTypes sourceElementType, ElementTypes targetElementType, LinkTypes lnkType){
        final String EL_NAME = prefix + "InvSel";// + linkType.toString().substring(0, 3);
        try{
            //create source element
            DiagramElementOperator el1 = createElement(EL_NAME + "S", sourceElementType);
            eventTool.waitNoEvent(500);
            
            //create target element
            //Point point = dia.getDrawingArea().getFreePoint(170);
            Point point = new Point(200, 300);
            DiagramElementOperator el2 = createElement(EL_NAME + "T", targetElementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            
            //create link
            dia.createGenericRelationshipOnDiagram(linkType, el1, el2);
            eventTool.waitNoEvent(500);
            
            point = dia.getDrawingArea().getFreePoint(170);
            DiagramElementOperator el3 = createElement(EL_NAME + "S2", sourceElementType, point.x, point.y);
            eventTool.waitNoEvent(500);

            point = dia.getDrawingArea().getFreePoint(170);
            DiagramElementOperator el4 = createElement(EL_NAME + "T2", targetElementType, point.x, point.y);
            eventTool.waitNoEvent(500);

            dia.createGenericRelationshipOnDiagram(lnkType, el3, el4);
            eventTool.waitNoEvent(500);

            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(1500);
            
            //
            LinkOperator link = new LinkOperator(el1, el2);
            JPopupMenuOperator popup = link.getPopup();
            //to close automatically opened submenu before searching for menu item
            eventTool.waitNoEvent(500);
            popup.pushKey(KeyEvent.VK_LEFT);
            popup = new JPopupMenuOperator();
            popup.pushMenu(PopupConstants.EDIT + "|" + PopupConstants.INVERT_SELECTION);
            eventTool.waitNoEvent(2000);
            
            //checking selection
            
            el1 = getElement(EL_NAME + "S", sourceElementType, 0);
            if (!el1.isSelected()){
                log("Element '" + EL_NAME + "S" + "' not selected");
                return false;
            }
            el2 = getElement(EL_NAME + "T", targetElementType, 0);
            if (!el2.isSelected()){
                log("Element '" + EL_NAME + "T" + "' not selected");
                return false;
            }
            el3 = getElement(EL_NAME + "S2", sourceElementType, 0);
            if (!el3.isSelected()){
                log("Element '" + EL_NAME + "S2" + "' not selected");
                return false;
            }
            el4 = getElement(EL_NAME + "T2", targetElementType, 0);
            if (!el4.isSelected()){
                log("Element '" + EL_NAME + "T2" + "' not selected");
                return false;
            }
            
            LinkOperator lnk1 = new LinkOperator(el1, el2);
            if (lnk1.isSelected()){
                log(lnk1.getType() + "link selected");
                return false;
            }
            LinkOperator lnk2 = new LinkOperator(el3, el4);
            if (!lnk2.isSelected()){
                log(lnk2.getType() + " link not selected");
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
    
    public boolean checkFindSourceElement(ElementTypes sourceElementType, ElementTypes targetElementType){
        final String EL_NAME = prefix + "FindSrc";// + linkType.toString().substring(0, 3);
        try{
            //create source element
            DiagramElementOperator el1 = createElement(EL_NAME + "S", sourceElementType);
            eventTool.waitNoEvent(500);
            
            //create target element
            //Point point = dia.getDrawingArea().getFreePoint(170);
            Point point = new Point(200, 300);
            DiagramElementOperator el2 = createElement(EL_NAME + "T", targetElementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            
            //create link
            dia.createGenericRelationshipOnDiagram(linkType, el1, el2);
            eventTool.waitNoEvent(1000);
            
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(1500);
            
            //call popup
            LinkOperator link = new LinkOperator(el1, el2);
            JPopupMenuOperator popup = link.getPopup();
            //to close automatically opened submenu before searching for menu item
            eventTool.waitNoEvent(500);
            popup.pushKey(KeyEvent.VK_LEFT);
            popup = new JPopupMenuOperator();
            popup.pushMenu(PopupConstants.FIND + "|" + PopupConstants.SOURCE_ELEMENT);
            eventTool.waitNoEvent(2000);
            
            //checking selection
            
            el1 = getElement(EL_NAME + "S", sourceElementType, 0);
            if (!el1.isSelected()){
                String description = "Element '" + EL_NAME + "S" + "' not selected";
                log(description);
                throw new UnexpectedElementSelectionException(description, UnexpectedElementSelectionException.Status.NOTSELECTED, el1 );
                //return false;
            }
            
            el2 = getElement(EL_NAME + "T", targetElementType, 0);
            if (el2.isSelected()){
                String description = "Element '" + EL_NAME + "T" + "' selected";
                log(description);
                throw new UnexpectedElementSelectionException(description, UnexpectedElementSelectionException.Status.SELECTED, el2 );
                //return false;
            }
            
            LinkOperator lnk = new LinkOperator(el1, el2);
            if (lnk.isSelected()){
                String description = lnk.getType() + "link selected";
                log(description);
                throw new UnexpectedElementSelectionException(description, UnexpectedElementSelectionException.Status.SELECTED, lnk );
                //return false;
            }
            
            return true;
        //}catch(Exception e){
        //    if (log != null){e.printStackTrace(log);}
        //    return false;
        }finally{
            safeDeleteAllElements();
        }
    }

    public boolean checkFindTargetElement(ElementTypes sourceElementType, ElementTypes targetElementType){
        final String EL_NAME = prefix + "FindTgt";// + linkType.toString().substring(0, 3);
        try{
            //create source element
            DiagramElementOperator el1 = createElement(EL_NAME + "S", sourceElementType);
            eventTool.waitNoEvent(500);
            
            //create target element
            //Point point = dia.getDrawingArea().getFreePoint(170);
            Point point = new Point(200, 300);
            DiagramElementOperator el2 = createElement(EL_NAME + "T", targetElementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            
            //create link
            dia.createGenericRelationshipOnDiagram(linkType, el1, el2);
            eventTool.waitNoEvent(1000);
            
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(1500);
            
            //call popup
            LinkOperator link = new LinkOperator(el1, el2);
            JPopupMenuOperator popup = link.getPopup();
            //to close automatically opened submenu before searching for menu item
            eventTool.waitNoEvent(500);
            popup.pushKey(KeyEvent.VK_LEFT);
            popup = new JPopupMenuOperator();
            popup.pushMenu(PopupConstants.FIND + "|" + PopupConstants.TARGET_ELEMENT);
            eventTool.waitNoEvent(2000);
            
            //checking selection
            
            el1 = getElement(EL_NAME + "S", sourceElementType, 0);
            if (el1.isSelected()){
                String description = "Element '" + EL_NAME + "S" + "' selected";
                log(description);
                throw new UnexpectedElementSelectionException(description, UnexpectedElementSelectionException.Status.SELECTED, el1 );
                //return false;
            }
            
            el2 = getElement(EL_NAME + "T", targetElementType, 0);
            if (!el2.isSelected()){
                String description ="Element '" + EL_NAME + "T" + "' not selected";
                log(description);
                throw new UnexpectedElementSelectionException(description, UnexpectedElementSelectionException.Status.NOTSELECTED, el2 );
                //return false;
            }
            
            LinkOperator lnk = new LinkOperator(el1, el2);
            if (lnk.isSelected()){
                String description = lnk.getType() + "link selected";
                log(description);
                throw new UnexpectedElementSelectionException(description, UnexpectedElementSelectionException.Status.SELECTED, lnk );
                //return false;
            }
            
            return true;
        //}catch(Exception e){
        //    if (log != null){e.printStackTrace(log);}
        //    return false;
        }finally{
            safeDeleteAllElements();
        }
    }

    public boolean checkRedirectSourceElement(ElementTypes sourceElementType, ElementTypes targetElementType, ElementTypes redirectElementType){
        final String EL_NAME = prefix + "RdrSrc";// + linkType.toString().substring(0, 3);
        try{
            //create source element
            DiagramElementOperator el1 = createElement(EL_NAME + "S", sourceElementType);
            eventTool.waitNoEvent(500);
            
            //create target element
            //Point point = dia.getDrawingArea().getFreePoint(170);
            Point point = new Point(200, 300);
            DiagramElementOperator el2 = createElement(EL_NAME + "T", targetElementType, point.x, point.y);
            eventTool.waitNoEvent(500);

            //create element to redirect
            //point = dia.getDrawingArea().getFreePoint(170);
            point = new Point(100, 250);
            DiagramElementOperator el3 = createElement(EL_NAME + "R", redirectElementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            
            //create link
            dia.createGenericRelationshipOnDiagram(linkType, el1, el2);
            eventTool.waitNoEvent(1000);
            
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            
            //redirect source
            LinkOperator lnk = new LinkOperator(el1, el2);
            point = lnk.getNearSourcePoint();
            dia.getDrawingArea().clickMouse(point.x, point.y, 1);
            eventTool.waitNoEvent(500);
            
            el3.clickOnCenter();
            eventTool.waitNoEvent(1000);
            
            //checking
            lnk = new LinkOperator(el3, el2);
            return true;
        }catch(Exception e){
            if (log != null){e.printStackTrace(log);}
            return false;
        }finally{
            safeDeleteAllElements();
        }
    }

    public boolean checkRedirectTargetElement(ElementTypes sourceElementType, ElementTypes targetElementType, ElementTypes redirectElementType){
        final String EL_NAME = prefix + "RdrTgt";// + linkType.toString().substring(0, 3);
        try{
            //create source element
            DiagramElementOperator el1 = createElement(EL_NAME + "S", sourceElementType);
            eventTool.waitNoEvent(500);
            
            //create target element
            //Point point = dia.getDrawingArea().getFreePoint(170);
            Point point = new Point(200, 300);
            DiagramElementOperator el2 = createElement(EL_NAME + "T", targetElementType, point.x, point.y);
            eventTool.waitNoEvent(500);

            //create element to redirect
            //point = dia.getDrawingArea().getFreePoint(170);
            point = new Point(100, 400);
            DiagramElementOperator el3 = createElement(EL_NAME + "R", redirectElementType, point.x, point.y);
            eventTool.waitNoEvent(500);
            
            //create link 
            dia.createGenericRelationshipOnDiagram(linkType, el1, el2);
            eventTool.waitNoEvent(1000);
            
            new SaveAllAction().performAPI();
            eventTool.waitNoEvent(500);
            
            //redirect source
            LinkOperator lnk = new LinkOperator(el1, el2);
            
            //6.5 Need find correct point manually
            point = lnk.getNearTargetPoint();
            dia.getDrawingArea().clickMouse(point.x, point.y, 1);
            eventTool.waitNoEvent(500);
            
            el3.clickOnCenter();
            eventTool.waitNoEvent(1000);
            
            //checking
            lnk = new LinkOperator(el1, el3);
            
            return true;
        }catch(Exception e){
            if (log != null){e.printStackTrace(log);}
            return false;
        }finally{
            safeDeleteAllElements();
        }
    }
    
//------------------------------------------------------------------------------    
    
}
