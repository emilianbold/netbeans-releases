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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteButtonModel;

/**
 *
 * @author treyspiva
 */
public class ComboButton extends JPanel
{
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
                Border border = BorderFactory.createEmptyBorder(0, 4, 0, 0);
                for (ContextPaletteButtonModel curDesc : popupContents)
                {
                    PaletteButton curBtn = new PaletteButton(associatedWidget, 
                                                             curDesc, 
                                                             getDirection(),
                                                             false);
                    curBtn.addContextButtonListener(myButtonListener);
                    curBtn.setBorder(border);

                    container.add(curBtn);
                }
            }
            else
            {
                Border border = BorderFactory.createEmptyBorder(0, 0, 0, 4);
                for (int index = popupContents.size() - 1; index >= 0; index--)
                {
                    ContextPaletteButtonModel curDesc = popupContents.get(index);
                    PaletteButton curBtn = new PaletteButton(associatedWidget, 
                                                             curDesc, 
                                                             getDirection(),
                                                             false);
                    
                    curBtn.addContextButtonListener(myButtonListener);
                    curBtn.setBorder(border);

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
        
        if(getDirection() == PaletteDirection.RIGHT)
        {
            container.add(btn);
            container.add(popupBtn);
        }
        else
        {
            container.add(popupBtn);
            container.add(btn);
        }

        
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
