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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import javax.swing.*;

import org.netbeans.modules.form.layoutsupport.*;

/**
 * Dedicated layout support class for JToolBar.
 *
 * @author Tomas Pavek
 */

public class JToolBarSupport extends AbstractLayoutSupport {

    /** Gets the supported layout manager class - JToolBar.
     * @return the class supported by this delegate
     */
    public Class getSupportedClass() {
        return JToolBar.class;
    }

    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index) {
        // Issue 63955 and JDK bug 4294758
        LayoutManager lm = containerDelegate.getLayout();
        // Cannot use instanceof BoxLayout because JToolBar
        // uses DefaultToolBarLayout wrapper around BoxLayout
        if (lm instanceof LayoutManager2) {
            ((LayoutManager2)lm).invalidateLayout(containerDelegate);
        }
        super.addComponentsToContainer(container, containerDelegate, components, index);
    }

    /** This method calculates position (index) for a component dragged
     * over a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     * @param component the real component being dragged; not needed here
     * @param index position (index) of the component in its current container;
     *        not needed here
     * @param posInCont position of mouse in the container delegate
     * @param posInComp position of mouse in the dragged component;
     *        not needed here
     * @return index corresponding to the position of the component in the
     *         container
     */
    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        if (!(container instanceof JToolBar))
            return -1;

        int orientation = ((JToolBar)container).getOrientation();
        
        assistantParams = 0;
        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (component == components[i]) {
                assistantParams--;
                continue;
            }
            Rectangle b = components[i].getBounds();
            if (orientation == SwingConstants.HORIZONTAL) {
                if (posInCont.x < b.x + b.width / 2) {
                    assistantParams += i;
                    return i;
                }
            }
            else {
                if (posInCont.y < b.y + b.height / 2) {   
                    assistantParams += i;
                    return i;
                }
            }
        }

        assistantParams += components.length;
        return components.length;
    }

    private int assistantParams;
    public String getAssistantContext() {
        return "toolbarLayout"; // NOI18N
    }

    public Object[] getAssistantParams() {
        return new Object[] {Integer.valueOf(assistantParams+1)};
    }

    /** This method paints a dragging feedback for a component dragged over
     * a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     *        (for layout managers we always use container delegate instead of
     *        the container)
     * @param component the real component being dragged, not needed here
     * @param newConstraints component layout constraints to be presented;
     *        not used for JToolBar
     * @param newIndex component's index position to be presented
     * @param g Graphics object for painting (with color and line style set)
     * @return whether any feedback was painted (true in this case)
     */
    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        if (!(container instanceof JToolBar))
            return false;

        int orientation = ((JToolBar)container).getOrientation();
        Component[] components = container.getComponents();
        Rectangle rect;

        if ((newIndex >= 0) && (newIndex < components.length) && (component == components[newIndex])) {
            newIndex++;
        }
        if ((components.length == 0) || ((components.length == 1) && (components[0] == component))) {
            Insets ins = container.getInsets();
            rect = orientation == SwingConstants.HORIZONTAL ?
                   new Rectangle(ins.left,
                                 ins.top + (container.getHeight() - ins.top
                                            - ins.bottom - 20) / 2,
                                 30, 20) :
                   new Rectangle(ins.left + (container.getWidth() - ins.left
                                 - ins.right - 30) / 2,
                                 ins.top,
                                 30, 20);
        }
        else if (newIndex < 0 || newIndex >= components.length) {
            int index = (components[components.length-1] == component) ? components.length-2 : components.length-1;
            Rectangle b = components[index].getBounds();
            rect = orientation == SwingConstants.HORIZONTAL ?
                   new Rectangle(b.x + b.width - 10, b.y, 20, b.height) :
                   new Rectangle(b.x, b.y + b.height - 10, b.width, 20);
        }
        else {
            Rectangle b = components[newIndex].getBounds();
            rect = orientation == SwingConstants.HORIZONTAL ?
                   new Rectangle(b.x - 10, b.y, 20, b.height) :
                   new Rectangle(b.x, b.y - 10, b.width, 20);
        }

        g.drawRect(rect.x, rect.y, rect.width, rect.height);

        return true;
    }
}
