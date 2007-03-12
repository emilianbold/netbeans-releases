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
import org.netbeans.modules.visualweb.designer.GridHandler;
import org.netbeans.modules.visualweb.designer.SelectionManager;
import org.netbeans.modules.visualweb.designer.WebForm;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.netbeans.modules.visualweb.text.DesignerCaret;
import org.netbeans.modules.visualweb.text.DesignerPaneBase;
import org.netbeans.modules.visualweb.text.Position;
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
            DesignerCaret caret = target.getCaret();

            if (caret == null) {
                // No caret - no focus, no next visual position...
                // Instead, we should move the selected components.
                // When select is true, the shift key is pressed -
                // in this case we use it to disable snapping
                boolean snapDisabled = select;

                // (we do snapdisabled for selection forward/backward etc.
//                target.getDocument().getWebForm().getActions().moveSelection(direction, snapDisabled);
                moveSelection(target.getDocument().getWebForm(), direction, snapDisabled);

                return;
            }

            Position dot = caret.getDot();
            Point magicPosition = caret.getMagicCaretPosition();

            if ((magicPosition == null) &&
                    ((direction == SwingConstants.NORTH) || (direction == SwingConstants.SOUTH))) {
//                Rectangle r = target.modelToView(dot);
                WebForm webForm = target.getDocument().getWebForm();
                Rectangle r = webForm.modelToView(dot);
                magicPosition = new Point(r.x, r.y);
            }

            Position originalDot = dot;
            dot = target.getUI().getNextVisualPositionFrom(target, dot, direction);

            if ((dot == Position.NONE) || !caret.isWithinEditableRegion(dot)) {
                // Can't move caret that way, but I should still clear the selection
                // if any
                caret.setDot(originalDot);

                UIManager.getLookAndFeel().provideErrorFeedback(target);

                // beep
                return;
            }

            if (select) {
                caret.moveDot(dot);
            } else {
                caret.setDot(dot);
            }

            if ((magicPosition != null) &&
                    ((direction == SwingConstants.NORTH) || (direction == SwingConstants.SOUTH))) {
                target.getCaret().setMagicCaretPosition(magicPosition);
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
        List<Rectangle> rectangles = new ArrayList<Rectangle>(numSelected);
        List<CssBox> boxes = new ArrayList<CssBox>(numSelected);
//        Iterator it = sm.iterator();
////        ModelViewMapper mapper = webform.getMapper();
//
//        while (it.hasNext()) {
//            DesignBean bean = (DesignBean)it.next();
        for (Element componentRootElement : sm.getSelectedComponentRootElements()) {
//            DesignBean bean = WebForm.getHtmlDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
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

            Rectangle r =
                new Rectangle(box.getAbsoluteX(), box.getAbsoluteY(), box.getWidth(),
                    box.getHeight());
            rectangles.add(r);
        }

//        GridHandler gm = GridHandler.getInstance();
        GridHandler gm = webform.getGridHandler();
        int offsetX = 0;
        int offsetY = 0;
        int stepSize = 1;

        switch (direction) {
        case SwingConstants.EAST:

            if (snapDisabled) {
                offsetX = stepSize;
            } else {
                offsetX = gm.getGridWidth();
            }

            break;

        case SwingConstants.WEST:

            if (snapDisabled) {
                offsetX = -stepSize;
            } else {
                offsetX = -gm.getGridWidth();
            }

            break;

        case SwingConstants.NORTH:

            if (snapDisabled) {
                offsetY = -stepSize;
            } else {
                offsetY = -gm.getGridHeight();
            }

            break;

        case SwingConstants.SOUTH:

            if (snapDisabled) {
                offsetY = stepSize;
            } else {
                offsetY = gm.getGridHeight();
            }

            break;
        }

        gm.move(webform.getPane(), /*beans,*/ rectangles, boxes, Position.NONE, offsetX, offsetY,
            snapDisabled);
    }
}
