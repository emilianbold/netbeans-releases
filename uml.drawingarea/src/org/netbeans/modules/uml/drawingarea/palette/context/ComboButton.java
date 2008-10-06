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
package org.netbeans.modules.uml.drawingarea.palette.context;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author treyspiva
 */
public class ComboButton extends ContextPaletteButton
{
    private static final Border RIGHT_POPOUT_BORDER = BorderFactory.createEmptyBorder(1, 4, 1, 1);
    private static final Border RIGHT_POPOUT_FOCUSBORDER = 
            BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIManager.getColor("List.selectionBackground"), 1),
                                               BorderFactory.createEmptyBorder(0, 2, 0, 0));
    private static final Border LEFT_POPOUT_BORDER = BorderFactory.createEmptyBorder(1, 1, 1, 4);
    private static final Border LEFT_POPOUT_FOCUSBORDER = 
            BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIManager.getColor("List.selectionBackground"), 1),
                                               BorderFactory.createEmptyBorder(0, 0, 0, 3));
    
    private Widget associatedWidget = null;
    private ContextPaletteButtonModel model = null;
    private ArrayList < ComboButtonListener > listeners = 
            new ArrayList < ComboButtonListener >();
    private boolean expanded = false;
    private PaletteDirection direction = PaletteDirection.RIGHT;
    
    private ArrayList < ContextButtonListener > buttonListeners = 
            new ArrayList < ContextButtonListener >();
    private ButtonListener myButtonListener = new ButtonListener();
    
    public ComboButton(Widget context, ContextPaletteButtonModel desc)
    {
        setContext(context);
        setModel(desc);
        
        setLayout(new BorderLayout());
        
        setBackground(ContextPalette.BACKGROUND);
        setExpanded(false);
        
        addFocusListener(new FocusAdapter() 
        {
            @Override
            public void focusLost(FocusEvent e)
            {
//                setExpanded(false);
            }
            
        });
        
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LeftAction");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RightAction");
        getActionMap().put("LeftAction", new LeftMoveButtonAction());
        getActionMap().put("RightAction", new RightMoveButtonAction());
    }
    
    protected Border getFocusBorder()
    {
        Border retVal = BorderFactory.createLineBorder(Color.BLUE, 1);
        if(isExpanded() == true)
        {
            retVal = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        }
        
        return retVal;
    }
    
    protected Border getNonFocusedBorder()
    {
        return BorderFactory.createEmptyBorder(1, 1, 1, 1);
    }
    
    /**
     * Initializes the control to contain all of the buttons need to support the
     * expanded state.  If the expand state is true the hidden actions are 
     * also shown.  If the add parameter is true, the initialized component will 
     * be added to the button.
     * 
     * @param expand build the component as if the button is expanded.
     * @param add adds the components to the button.
     */
    private JComponent initializeComponents(boolean expand, boolean add)
    {
        Box container = Box.createHorizontalBox();
        
        if(getDirection() == PaletteDirection.RIGHT)
        {
            createMainButton(container);
            container.add(Box.createHorizontalStrut(5));
            createPopout(container, expand);
        }
        else
        {
            createPopout(container, expand);
            container.add(Box.createHorizontalStrut(5));
            createMainButton(container);
        }
        
        if(add == true)
        {
            removeAll();
            
            JPanel filler = new JPanel();
            filler.setOpaque(false);
            filler.setPreferredSize(new Dimension(0, 0));

            setLayout(new BorderLayout());
            
            if(getDirection() == PaletteDirection.RIGHT)
            {
                add(container, BorderLayout.WEST);
            }
            else
            {
                add(container, BorderLayout.EAST);
            }
            
            add(filler, BorderLayout.CENTER);

            setSize(getPreferredSize());
        }
        
//        container.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return container;
    }
    
    protected void createPopout(Box container, boolean expand)
    {
        if (expand == true)
        {
            ArrayList<ContextPaletteButtonModel> popupContents = model.getChildren();
            if(getDirection() == PaletteDirection.RIGHT)
            {
                
                for (ContextPaletteButtonModel curDesc : popupContents)
                {
                    PaletteButton curBtn = new PaletteButton(associatedWidget, 
                                                             curDesc, 
                                                             getDirection(),
                                                             false)
                    {
                        protected Border getFocusBorder()
                        {
                            return RIGHT_POPOUT_FOCUSBORDER;
                        }

                        protected Border getNonFocusedBorder()
                        {
                            return RIGHT_POPOUT_BORDER;
                        }
                    };
                    
                    curBtn.addContextButtonListener(myButtonListener);
//                    curBtn.setBorder(RIGHT_POPOUT_BORDER);
                    
                    InputMap inputMap = curBtn.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                    inputMap.remove(UP_KEYSTROKE);
                    inputMap.remove(DOWN_KEYSTROKE);

                    container.add(curBtn);
                }
            }
            else
            {
                for (int index = popupContents.size() - 1; index >= 0; index--)
                {
                    ContextPaletteButtonModel curDesc = popupContents.get(index);
                    PaletteButton curBtn = new PaletteButton(associatedWidget, 
                                                             curDesc, 
                                                             getDirection(),
                                                             false)
                    {
                        protected Border getFocusBorder()
                        {
                            return LEFT_POPOUT_FOCUSBORDER;
                        }

                        protected Border getNonFocusedBorder()
                        {
                            return LEFT_POPOUT_BORDER;
                        }
                    };
                    
                    curBtn.addContextButtonListener(myButtonListener);
                    curBtn.setBorder(LEFT_POPOUT_BORDER);
                    curBtn.getInputMap().remove(UP_KEYSTROKE);
                    curBtn.getInputMap().remove(DOWN_KEYSTROKE);

                    container.add(curBtn);
                }
            }
        }
    }
    
    protected void createMainButton(Box container)
    {
        ArrowButton popupBtn = new ArrowButton();
        ContextPaletteButtonModel mainDesc = model.getChildren().get(0);
        PaletteButton btn = new PaletteButton(associatedWidget, 
                                              mainDesc,  
                                              getDirection(),
                                              false);
        
        Box mainBtnPanel = Box.createHorizontalBox();
        if(getDirection() == PaletteDirection.RIGHT)
        {
            mainBtnPanel.add(btn);
            mainBtnPanel.add(popupBtn);
        }
        else
        {
            mainBtnPanel.add(popupBtn);
            mainBtnPanel.add(btn);
        }

        mainBtnPanel.getPreferredSize();
        //mainBtnPanel.setBorder(getNonFocusedBorder());
        container.add(mainBtnPanel);
    }
    
    protected void fireComboExpandChanged()
    {
        for(ComboButtonListener listener : listeners)
        {
            listener.expandStateChanged(this, expanded);
        }
    }
    
    public void addComboButtonListener(ComboButtonListener listener)
    {
        listeners.add(listener);
    }
    
    public void removeComboBUttonListener(ComboButtonListener listener)
    {
        listeners.remove(listener);
    }
    
    ///////////////////////////////////////////////////////////////
    // Button listener methods.
    
    public void addContextButtonListener(ContextButtonListener listener)
    {
        buttonListeners.add(listener);
    }
    
    public void removeContextButtonListener(ContextButtonListener listener)
    {
        buttonListeners.remove(listener);
    }
    
    /////////////////////////////////////////////////////////
    // Getter/Setters
    
    public Widget getContext()
    {
        return associatedWidget;
    }

    public void setContext(Widget context)
    {
        this.associatedWidget = context;
    }

    public ContextPaletteButtonModel getModel()
    {
        return model;
    }

    public void setModel(ContextPaletteButtonModel description)
    {
        this.model = description;
    }
    
    public boolean isExpanded()
    {
        return expanded;
    }

    public void setExpanded(boolean expanded)
    {
        this.expanded = expanded;
        initializeComponents(expanded, true);
        revalidate();
        
        setOpaque(expanded);
        
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fireComboExpandChanged();
    }
    
    
    int getExpandedWidth()
    {
        int retVal = getPreferredSize().width;
        
        if(isExpanded() == false)
        {
            JComponent item = initializeComponents(true, false);
            retVal = item.getPreferredSize().width;
        }
        
        return retVal;
    }

    public void setDirection(PaletteDirection direction)
    {
        this.direction = direction;
        
        initializeComponents(isExpanded(), true);
        revalidate();
    }

    public PaletteDirection getDirection()
    {
        return direction;
    }
    
    /////////////////////////////////////////////////////////
    // Inner Classes
    
    public class ArrowButton extends JPanel
    {
        private static final int ARROW_HEIGHT = 6;
        private static final int ARROW_WIDTH = ARROW_HEIGHT / 2;
        
        public ArrowButton()
        {
            setBorder(null);
            setPreferredSize(new Dimension(ARROW_WIDTH + 4, ARROW_HEIGHT));
            setMaximumSize(new Dimension(ARROW_WIDTH + 4, 64));
            
            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent event)
                {
                    setExpanded(!isExpanded());
                }
                
            });
        }

        @Override
        public void paint(Graphics g)
        {
            int height = getBounds().height;

            int halfHeight = ARROW_HEIGHT / 2;
            int startX = 2;
            int startY = height / 2 - halfHeight;

            int[] xPoints = {startX, startX + ARROW_WIDTH, startX};
            int[] yPoints = {startY, startY + halfHeight, startY + ARROW_HEIGHT};

            if(getDirection() == PaletteDirection.LEFT)
            {
                startX = getWidth() - 2;
                xPoints = new int[] {startX, startX - ARROW_WIDTH, startX};
            }
            
            Color origColor = g.getColor();            
            g.setColor(Color.BLACK);
            g.fillPolygon(xPoints, yPoints, 3);
            g.setColor(origColor);
        }
    }
    
    private void setFocusToFirstChild()
    {
        if (getComponent(0) instanceof Container)
        {
            Container container = (Container) getComponent(0);
            
            if(isExpanded() == true)
            {
                if(getDirection() == PaletteDirection.RIGHT)
                {
                    container.getComponent(2).requestFocusInWindow();
                }
                else
                {
                    int firstChild = container.getComponentCount() - 4;
                    container.getComponent(firstChild).requestFocusInWindow();
                }
            }
            else
            {
                requestFocusInWindow();
            }
        }
    }

    @Override
    protected void moveFocusToNextSibling(int startChildIndex, int endChildIndex)
    {
        setExpanded(false);
        super.moveFocusToNextSibling(startChildIndex, endChildIndex);
    }

    @Override
    protected void moveFocusToPreviousSibling(int startChildIndex, int endChildIndex)
    {
        setExpanded(false);
        super.moveFocusToPreviousSibling(startChildIndex, endChildIndex);
    }

    
    private PaletteButton getFocusedChild()
    {
        PaletteButton retVal = null;
        
        if (getComponent(0) instanceof Container)
        {
            Container container = (Container) getComponent(0);
            for(Component child : container.getComponents())
            {
                if (child instanceof PaletteButton)
                {
                    PaletteButton button = (PaletteButton) child;
                    if(button.isFocusOwner() == true)
                    {
                        retVal = button;
                        break;
                    }
                }
            }
        }
        return retVal;
    }
    
    /**
     * LeftMoveButtonAction handles the left arrow keystroke.  When the combo 
     * button is collapsed the button will first be expanded, then the first
     * child will be selected.  
     */
    public class LeftMoveButtonAction extends AbstractAction
    {

        public void actionPerformed(ActionEvent e)
        {
            if(isExpanded() == false)
            {
                if(getDirection() == PaletteDirection.LEFT)
                {
                    setExpanded(true);
                    setFocusToFirstChild();
                }
                else
                {
                    setExpanded(false);
                    setFocusToFirstChild();
                }
            }
            else
            {
                PaletteButton curFocus = getFocusedChild();
                if(curFocus != null)
                {
                    if(getDirection() == PaletteDirection.LEFT)
                    {
                        int endChildIndex = getModel().getChildren().size() - 1;
                        curFocus.moveFocusToNextSibling(endChildIndex, -1);
                    }
                    else
                    {
                        curFocus.moveFocusToPreviousSibling(2, END_CHILD_INDEX);
                    }
                }
            }
        }
        
    }
    
    public class RightMoveButtonAction extends AbstractAction
    {

        public void actionPerformed(ActionEvent e)
        {
            if(isExpanded() == false)
            {
                if(getDirection() == PaletteDirection.LEFT)
                {
                    setExpanded(false);
                    setFocusToFirstChild();
                }
                else
                {
                    setExpanded(true);
                    setFocusToFirstChild();
                }
            }
            else
            {
                PaletteButton curFocus = getFocusedChild();
                if(curFocus != null)
                {
                    if(getDirection() == PaletteDirection.RIGHT)
                    {
                        curFocus.moveFocusToNextSibling(2, END_CHILD_INDEX);
                    }
                    else
                    {
                        int endChildIndex = getModel().getChildren().size() - 1;
                        curFocus.moveFocusToPreviousSibling(endChildIndex, 0);
                    }
                }
            }
        }
        
    }
    
    public class ButtonListener implements ContextButtonListener
    {
        public void actionPerformed(PaletteButton source, boolean locked)
        {
            for(ContextButtonListener listener : buttonListeners)
            {
                listener.actionPerformed(source, locked);
            }
        }
        
    }
}
