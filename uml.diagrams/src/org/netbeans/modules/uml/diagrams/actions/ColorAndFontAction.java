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
package org.netbeans.modules.uml.diagrams.actions;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.actions.SceneNodeAction;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.ui.support.drawingproperties.FontChooser;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public class ColorAndFontAction extends SceneNodeAction
{

    public static final int FONT = 0;
    public static final int FOREGROUND = 1;
    public static final int BACKGROUND = 2;
    public static final int BORDER = 3;
    private ActionListener menuItemListener;
    private JMenu popupMenu;
    private Node[] activatedNodes;
    private DesignerScene scene;

    @Override
    public Action createContextAwareInstance(Lookup actionContext)
    {
        scene = actionContext.lookup(DesignerScene.class);
        return (Action)this;
    }

    @Override
    public JMenuItem getMenuPresenter()
    {
        return getPopupPresenter();
    }

    /**
     * Returns a JMenuItem that presents this action in a Popup Menu.
     * @return the JMenuItem representation for the action
     */
    @Override
    public JMenuItem getPopupPresenter()
    {
        popupMenu = new JMenu(NbBundle.getMessage(ColorAndFontAction.class, "ACT_ColorFont")); // NOI18N
        popupMenu.setEnabled((scene != null && scene.isReadOnly() == false));

        ResourceBundle bundle = NbBundle.getBundle(ColorAndFontAction.class);
        JMenuItem font = new FontMenuItem(bundle.getString("CTL_Font"), FONT); // NOI18N
        JMenuItem foreground = new FontMenuItem(bundle.getString("CTL_Foreground"), FOREGROUND); // NOI18N
        JMenuItem background = new FontMenuItem(bundle.getString("CTL_Background"), BACKGROUND); // NOI18N
        JMenuItem border = new FontMenuItem(bundle.getString("CTL_Border"), BORDER); // NOI18N
        
        JMenuItem[] items = new JMenuItem[]
        {
            font, foreground, background
        };
        
        for (int i = 0; i < items.length; i++)
        {
            items[i].addActionListener(new FontMenuItemListener());
            items[i].setEnabled(scene != null);
            popupMenu.add(items[i]);
        }
        
        popupMenu.getMenuComponentCount();
        // TODO: sub menu enabling logic
        return popupMenu;
    }

    private static class FontMenuItem extends JMenuItem
    {

        int actionType;

        FontMenuItem(String text, int action)
        {
            super(text);
            actionType = action;
        }

        int getActionType()
        {
            return actionType;
        }
    }

    private class FontMenuItemListener implements ActionListener
    {

        public void actionPerformed(ActionEvent evt)
        {
            Object source = evt.getSource();
            if (!(source instanceof FontMenuItem))
            {
                return;
            }
            FontMenuItem mi = (FontMenuItem) source;
            if (!mi.isEnabled())
            {
                return;
            }
            switch (mi.getActionType())
            {
                case FONT:
                    setFont();
                    break;
                case FOREGROUND:
                    setForeground();
                    break;
                case BACKGROUND:
                    setBackground();
                    break;
                case BORDER:
                    setBorder();
                    break;
            }           
        }
    }

    private IPresentationElement[] getSelectedElements()
    {

        Set<IPresentationElement> selected = (Set<IPresentationElement>) scene.getSelectedObjects();

        IPresentationElement[] elements = new IPresentationElement[selected.size()];
        selected.toArray(elements);
        selected.toArray(elements);
        return elements;
    }

    private void setFont()
    {
        Font font = scene.getDefaultFont();
        IPresentationElement[] elements = getSelectedElements();
        Widget w = scene.findWidget(elements[0]);
        if (w != null)
        {
            font = w.getFont();
        }
        Font f = FontChooser.selectFont(font);
        for (IPresentationElement p : elements)
        {
            w = scene.findWidget(p);
            if (w instanceof UMLNodeWidget)
            {
                ((UMLNodeWidget) w).setNodeFont(f);
                scene.getEngine().getTopComponent().setDiagramDirty(true);
            }
            
            w.revalidate();
        }
        
        scene.validate();
    }

    private void setForeground()
    {
        IPresentationElement[] elements = getSelectedElements();
        Widget w = scene.findWidget(elements[0]);
        Color oldColor = w.getForeground();
        Color color = JColorChooser.showDialog(scene.getView(), NbBundle.getMessage(ColorAndFontAction.class, "COLOR_CHOOSER_TITLE"), oldColor);
        for (IPresentationElement p : getSelectedElements())
        {
            w=scene.findWidget(p);
            if (w instanceof UMLNodeWidget)
            {
                ((UMLNodeWidget) w).setNodeForeground(color);
                scene.getEngine().getTopComponent().setDiagramDirty(true);
            }
            else if (w instanceof ConnectionWidget)
            {
                ((ConnectionWidget) w).setLineColor(color);
                scene.getEngine().getTopComponent().setDiagramDirty(true);
            }
            w.revalidate();
        }
        w.revalidate();
        scene.revalidate();
        scene.validate();
    }

    private void setBackground()
    {
        IPresentationElement[] elements = getSelectedElements();
        Widget w1 = scene.findWidget(elements[0]);
        Color oldColor = Color.WHITE;
        if (w1 instanceof UMLNodeWidget)
        {
            oldColor = (Color) ((UMLNodeWidget)w1).getNodeBackground();
        }
        Color color = JColorChooser.showDialog(scene.getView(), NbBundle.getMessage(ColorAndFontAction.class, "COLOR_CHOOSER_TITLE"), oldColor);
        for (IPresentationElement p : getSelectedElements())
        {
            Widget w = scene.findWidget(p);
            if (w instanceof UMLNodeWidget)
            {
                ((UMLNodeWidget) w).setNodeBackground(color);
                scene.getEngine().getTopComponent().setDiagramDirty(true);
            }
            w.revalidate();
        }
        
        scene.validate();
    }

    private void setBorder()
    {
        Color color = JColorChooser.showDialog(scene.getView(), NbBundle.getMessage(ColorAndFontAction.class, "COLOR_CHOOSER_TITLE"), Color.BLACK);
        for (IPresentationElement p : getSelectedElements())
        {
            Widget w = scene.findWidget(p);
            if (w != null)
            {
                w.setBorder(BorderFactory.createLineBorder(color));
                scene.getEngine().getTopComponent().setDiagramDirty(true);
            }
        }
    }

    public String getName()
    {
        return NbBundle.getMessage(ColorAndFontAction.class, "ACT_ColorFont");
    }

    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    protected void performAction(Node[] activatedNodes)
    {

        this.activatedNodes = activatedNodes;
    }

    protected boolean enable(Node[] activatedNodes)
    {
        if(super.enable(activatedNodes) == false)
        {
            return false;
        }
        
        for (Node node : activatedNodes)
        {
            IPresentationElement pe = node.getLookup().lookup(IPresentationElement.class);
            DesignerScene scene = node.getLookup().lookup(DesignerScene.class);
            if (pe == null || scene == null)
            {
                return false;
            }
        }
        return true;
    }
}