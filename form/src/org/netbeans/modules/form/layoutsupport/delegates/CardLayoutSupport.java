/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import java.beans.*;
import java.util.*;

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.FormProperty;

/**
 * @author Tran Duc Trung, Tomas Pavek
 */

public class CardLayoutSupport extends AbstractLayoutSupport
                               implements LayoutSupportArranging
{
    private CardConstraints currentCard;


    public Class getSupportedClass() {
        return CardLayout.class;
    }

    public void addComponents(CodeElement[] newCompElements,
                              LayoutConstraints[] newConstraints)
    {
        super.addComponents(newCompElements, newConstraints);

        int count = getComponentCount();
        if (currentCard == null && count > 0)
            currentCard = (CardConstraints) getConstraints(count - 1);
    }

    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        if (!(containerDelegate.getLayout() instanceof CardLayout))
            return -1;
        return containerDelegate.getComponentCount();
    }

    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        if (!(containerDelegate.getLayout() instanceof CardLayout))
            return false;

        Dimension sz = containerDelegate.getSize();
        Insets insets = containerDelegate.getInsets();
        sz.width -= insets.left + insets.right;
        sz.height -= insets.top + insets.bottom;
        
        g.drawRect(0, 0, sz.width, sz.height);
        return true;
    }

    // ---------

    protected LayoutConstraints readConstraintsCode(
                                    CodeElement constrElement,
                                    CodeConnectionGroup constrCode,
                                    CodeElement compElement)
    {
        CardConstraints constr = new CardConstraints("card"); // NOI18N
        FormCodeSupport.readPropertyElement(constrElement,
                                            constr.getProperties()[0],
                                            false);
        return constr;
    }

    protected CodeElement createConstraintsCode(CodeConnectionGroup constrCode,
                                                LayoutConstraints constr,
                                                CodeElement compElement,
                                                int index)
    {
        if (!(constr instanceof CardConstraints))
            return null; // should not happen

        return getCodeStructure().createElement(
                   FormCodeSupport.createOrigin(constr.getProperties()[0]));
    }

    protected LayoutConstraints createDefaultConstraints() {
        return new CardConstraints("card"+(getComponentCount()+1)); // NOI18N
    }

    // ------------------------
    // LayoutSupportArranging

    public void processMouseClick(Point p, Container cont) {
    }

    public void selectComponent(int index) {
        LayoutConstraints constraints = getConstraints(index);
        if (constraints instanceof CardConstraints)
            currentCard = (CardConstraints) constraints;
    }

    public void arrangeContainer(Container container,
                                 Container containerDelegate)
    {
        LayoutManager lm = containerDelegate.getLayout();
        if (!(lm instanceof CardLayout) || currentCard == null)
            return;

        ((CardLayout)lm).show(containerDelegate,
                              (String)currentCard.getConstraintsObject());
    }

    // ----------------

    public static class CardConstraints implements LayoutConstraints {
        private String card;

        private Node.Property[] properties;

        public CardConstraints(String card) {
            this.card = card;
        }

        public Node.Property[] getProperties() {
            if (properties == null)
                properties = new Node.Property[] {
                    new FormProperty("cardName", // NOI18N
                                     String.class,
                                 getBundle().getString("PROP_cardName"), // NOI18N
                                 getBundle().getString("HINT_cardName")) { // NOI18N

                        public Object getTargetValue() {
                            return card;
                        }

                        public void setTargetValue(Object value) {
                            card = (String)value;
                        }
                    }
                };

            return properties;
        }

        public Object getConstraintsObject() {
            return card;
        }

        public LayoutConstraints cloneConstraints() {
            return new CardConstraints(card);
        }
    }
}
