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
 * Created on Mar 16, 2004
 *
 * To change the template for this generated file go to Window&gt;Preferences&gt;Java&gt;Code
 * Generation&gt;Code and Comments
 */
package org.netbeans.modules.visualweb.text.actions;

import java.util.List;
import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.css2.ModelViewMapper;
import org.netbeans.modules.visualweb.designer.SelectionManager;
import org.netbeans.modules.visualweb.designer.WebForm;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.text.DesignerPaneBase;
import org.w3c.dom.Element;


/*
 * Action to move the selection by way of the getNextVisualPositionFrom method. Constructor
 * indicates direction to use.
 */
public class NextVisualPositionAction extends TextAction {
    private boolean select;
    private int direction;

    /**
     * Create this action with the appropriate identifier.
     *
     * @param nm
     *            the name of the action, Action.NAME.
     * @param select
     *            whether to extend the selection when changing the caret position.
     */
    public NextVisualPositionAction(String nm, boolean select, int direction) {
        super(nm);
        this.select = select;
        this.direction = direction;
    }

    /** The operation to perform when this action is triggered. */
    public void actionPerformed(ActionEvent e) {
        DesignerPaneBase target = getTextComponent(e);

        if (target != null) {
//            DesignerCaret caret = target.getCaret();
//            if (caret == null) {
            if (!target.hasCaret()) {
                // No caret - no focus, no next visual position...
                // Instead, we should move the selected components.
                // When select is true, the shift key is pressed -
                // in this case we use it to disable snapping
                boolean snapDisabled = select;

                // (we do snapdisabled for selection forward/backward etc.
//                target.getDocument().getWebForm().getActions().moveSelection(direction, snapDisabled);
//                moveSelection(target.getDocument().getWebForm(), direction, snapDisabled);
                moveSelection(target.getWebForm(), direction, snapDisabled);

                return;
            }

//            Position dot = caret.getDot();
//            DomPosition dot = caret.getDot();
//            Point magicPosition = caret.getMagicCaretPosition();
            DomPosition dot = target.getCaretDot();
            Point magicPosition = target.getCaretMagicPosition();

            if ((magicPosition == null) &&
                    ((direction == SwingConstants.NORTH) || (direction == SwingConstants.SOUTH))) {
//                Rectangle r = target.modelToView(dot);
//                WebForm webForm = target.getDocument().getWebForm();
                WebForm webForm = target.getWebForm();
                
                Rectangle r = webForm.modelToView(dot);
                magicPosition = new Point(r.x, r.y);
            }

//            Position originalDot = dot;
            DomPosition originalDot = dot;
            dot = target.getUI().getNextVisualPositionFrom(target, dot, direction);

//            if ((dot == Position.NONE) || !caret.isWithinEditableRegion(dot)) {
//            if ((dot == DomPosition.NONE) || !caret.isWithinEditableRegion(dot)) {
//            if ((dot == DomPosition.NONE) || !target.isCaretWithinEditableRegion(dot)) {
            if ((dot == DomPosition.NONE) || !target.getWebForm().isInsideEditableRegion(dot)) {
                // Can't move caret that way, but I should still clear the selection
                // if any
//                caret.setDot(originalDot);
                target.setCaretDot(originalDot);

                UIManager.getLookAndFeel().provideErrorFeedback(target);

                // beep
                return;
            }

            if (select) {
//                caret.moveDot(dot);
                target.moveCaretDot(dot);
            } else {
//                caret.setDot(dot);
                target.setCaretDot(dot);
            }

            if ((magicPosition != null) &&
                    ((direction == SwingConstants.NORTH) || (direction == SwingConstants.SOUTH))) {
//                target.getCaret().setMagicCaretPosition(magicPosition);
                target.setCaretMagicPosition(magicPosition);
            }
        }
    }

    // XXX Moved from DesignerActions.
    /**
     * Component dragging via the keyboard.
     * Direction is from SwingConstants.
     * It will only move absolutely positioned children.
     */
    private void moveSelection(WebForm webform, int direction, boolean snapDisabled) {
        SelectionManager sm = webform.getSelection();

        if (sm.isSelectionEmpty()) {
            return;
        }

        int numSelected = sm.getNumSelected();
//        ArrayList beans = new ArrayList(numSelected);
//        List<Rectangle> rectangles = new ArrayList<Rectangle>(numSelected);
        List<Point> points = new ArrayList<Point>(numSelected);
        List<CssBox> boxes = new ArrayList<CssBox>(numSelected);
//        Iterator it = sm.iterator();
////        ModelViewMapper mapper = webform.getMapper();
//
//        while (it.hasNext()) {
//            DesignBean bean = (DesignBean)it.next();
        for (Element componentRootElement : sm.getSelectedComponentRootElements()) {
//            DesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
//            CssBox box = mapper.findBox(bean);
            CssBox box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), componentRootElement);

            if (box == null) {
                continue;
            }

            if (!box.getBoxType().isAbsolutelyPositioned()) {
                continue;
            }

//            beans.add(bean);

            //elements.add(box.getElement());
            boxes.add(box);

//            Rectangle r =
//                new Rectangle(box.getAbsoluteX(), box.getAbsoluteY(), box.getWidth(),
//                    box.getHeight());
//            rectangles.add(r);
            points.add(new Point(box.getAbsoluteX(), box.getAbsoluteY()));
        }

//        GridHandler gm = GridHandler.getInstance();
//        GridHandler gm = webform.getGridHandler();
//        GridHandler gm = GridHandler.getDefault();
        
        int offsetX = 0;
        int offsetY = 0;
        int stepSize = 1;

        switch (direction) {
        case SwingConstants.EAST:

            if (snapDisabled) {
                offsetX = stepSize;
            } else {
//                offsetX = gm.getGridWidth();
                offsetX = webform.getGridWidth();
            }

            break;

        case SwingConstants.WEST:

            if (snapDisabled) {
                offsetX = -stepSize;
            } else {
//                offsetX = -gm.getGridWidth();
                offsetX = -webform.getGridWidth();
            }

            break;

        case SwingConstants.NORTH:

            if (snapDisabled) {
                offsetY = -stepSize;
            } else {
//                offsetY = -gm.getGridHeight();
                offsetY = -webform.getGridHeight();
            }

            break;

        case SwingConstants.SOUTH:

            if (snapDisabled) {
                offsetY = stepSize;
            } else {
//                offsetY = gm.getGridHeight();
                offsetY = webform.getGridHeight();
            }

            break;
        }

//        gm.move(webform.getPane(), /*beans,*/ rectangles, boxes, Position.NONE, offsetX, offsetY,
//            snapDisabled);
//        gm.move(webform.getPane(), /*beans,*/ rectangles, boxes, DomPosition.NONE, offsetX, offsetY, snapDisabled);
//        gm.move(webform.getPane(), /*beans,*/ points.toArray(new Point[points.size()]), boxes.toArray(new CssBox[boxes.size()]),
//                DomPosition.NONE, offsetX, offsetY, snapDisabled);
        webform.getDomDocument().moveComponents(
                webform, boxes.toArray(new CssBox[boxes.size()]), points.toArray(new Point[points.size()]),
                DomPosition.NONE, offsetX, offsetY, !snapDisabled);
    }
}
