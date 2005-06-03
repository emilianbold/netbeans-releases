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

package org.netbeans.modules.form;

import java.awt.*;
import javax.swing.*;

import org.netbeans.modules.form.fakepeer.*;

/**
 * A JPanel subclass holding components presented in FormDesigner (contains
 * also the resizable border and the white area around). Technically, this is
 * a layer in FormDesigner, placed under HandleLayer.
 *
 * ComponentLayer
 *  +- DesignerPanel
 *      +- FakePeerContainer
 *          +- top visual component of the designed form (by VisualReplicator)
 *              +- subcomponents of the designed form
 *              +- ...
 *
 * @author Tomas Pavek
 */

class ComponentLayer extends JPanel
{
    private static final int HORIZONTAL_MARGIN = 12;
    private static final int VERTICAL_MARGIN = 12;

    /** The container holding the top visual component of the form. */
    private Container componentContainer;

    /** A panel (with a resizable border) positioning the component container
     * in the whole ComponentLayer area. */
    private DesignerPanel designerPanel;

    ComponentLayer() {
        componentContainer = new FakePeerContainer();
        componentContainer.setLayout(new BorderLayout());
        componentContainer.setBackground(Color.white);
        componentContainer.setFont(FakePeerSupport.getDefaultAWTFont());

        designerPanel = new DesignerPanel();
        designerPanel.setLayout(new BorderLayout());
        designerPanel.add(componentContainer, BorderLayout.CENTER);

        setLayout(new FlowLayout(FlowLayout.LEFT,
                                 HORIZONTAL_MARGIN,
                                 VERTICAL_MARGIN));
        add(designerPanel);

        updateBackground();
    }

    Container getComponentContainer() {
        return componentContainer;
    }

    Rectangle getDesignerInnerBounds() {
        Rectangle r = new Rectangle(designerPanel.getDesignerSize());
        Insets i = designerPanel.getInsets();
        r.x = HORIZONTAL_MARGIN + i.left;
        r.y = VERTICAL_MARGIN + i.top;
        return r;
    }

    Rectangle getDesignerOuterBounds() {
        return designerPanel.getBounds();
    }

    Insets getDesignerOutsets() {
        return designerPanel.getInsets();
    }

    Dimension getDesignerSize() {
        return designerPanel.getDesignerSize();
    }

    void setDesignerSize(Dimension size) {
        if (size != null && !size.equals(designerPanel.getDesignerSize())) {
            designerPanel.setDesignerSize(size);
//            revalidate();
//            repaint();
        }
    }

    void setTopDesignComponent(Component component) {
        if (componentContainer.getComponentCount() > 0)
            componentContainer.removeAll();
        componentContainer.add(component, BorderLayout.CENTER);
    }

    void updateVisualSettings() {
        updateBackground();
        designerPanel.updateBorder();
    }

    private void updateBackground() {
        setBackground(FormLoaderSettings.getInstance().getFormDesignerBackgroundColor());
    }

    // ---------

    private static class DesignerPanel extends JPanel {
        private static int BORDER_THICKNESS = 4; // [could be changeable]

        private Dimension designerSize = new Dimension(400, 300);

        DesignerPanel() {
            updateBorder();
        }

        void updateBorder() {
            setBorder(new javax.swing.border.LineBorder(
                FormLoaderSettings.getInstance().getFormDesignerBorderColor(),
                BORDER_THICKNESS));
        }

        Dimension getDesignerSize() {
            return designerSize;
        }

        void setDesignerSize(Dimension size) {
            designerSize = size;
        }

        public Dimension getPreferredSize() {
            Dimension size = new Dimension(designerSize);
            Insets insets = getInsets();
            size.width += insets.left + insets.right;
            size.height += insets.top + insets.bottom;
            return size;
        }
    }
}
