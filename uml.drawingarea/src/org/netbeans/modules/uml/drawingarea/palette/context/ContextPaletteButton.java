/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.drawingarea.palette.context;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.Border;

/**
 *
 * @author treyspiva
 */
public abstract class ContextPaletteButton extends JPanel
{
    protected static final KeyStroke UP_KEYSTROKE = 
            KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
    protected static final KeyStroke DOWN_KEYSTROKE = 
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
    
    
    
    protected static final int END_CHILD_INDEX = Integer.MAX_VALUE;
    
    public ContextPaletteButton()
    {
        addFocusListener(new FocusListener() 
        {

            public void focusGained(FocusEvent e)
            {
                if(e.isTemporary() == false)
                {
                    setBorder(getFocusBorder());
                    repaintPalette();
                }
            }

            public void focusLost(FocusEvent e)
            {
                if(e.isTemporary() == false)
                {
                    setBorder(getNonFocusedBorder());
                    repaintPalette();
                }
                
            }
        });
        
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        inputMap.put(UP_KEYSTROKE, "MoveToPrevious");
        inputMap.put(DOWN_KEYSTROKE, "MoveToNext");
        getActionMap().put("MoveToPrevious", new MoveToPreviousButtonAction());
        getActionMap().put("MoveToNext", new MoveToNextButtonAction());
    }
    
    protected abstract Border getFocusBorder();
    protected abstract Border getNonFocusedBorder();
    
    private void repaintPalette()
    {
        Container parent = getParent();
        while(!(parent instanceof ContextPalette))
        {
            if(parent == null)
            {
                break;
            }
            
            parent = parent.getParent();
        }

        if(parent != null)
        {
            parent.repaint();
        }
    }

    protected void moveFocusToNextSibling(int startChildIndex, int endChildIndex)
    {
        Component[] components = getParent().getComponents();
        Component next = null;
        
        if(endChildIndex == END_CHILD_INDEX)
        {
            endChildIndex = components.length;
        }
        
        for(int index = 0; index < components.length; index++)
        {
            if(components[index].equals(this) == true)
            {
                int dx = (startChildIndex < endChildIndex ? 1 : -1);
                if((index + dx) == endChildIndex )
                {
                    next = components[startChildIndex];
                }
                else
                {
                    next = components[index + dx];
                }
                break;
            }
        }
        
        if(next != null)
        {
            next.requestFocusInWindow();
        }
    }

    protected void moveFocusToPreviousSibling(int startChildIndex, int endChildIndex)
    {
        Component[] components = getParent().getComponents();
        Component previous = null;
        
        if(endChildIndex == END_CHILD_INDEX)
        {
            endChildIndex = components.length;
        }
        
        for(int index = 0; index < components.length; index++)
        {
            if(components[index].equals(this) == true)
            {
                int dx = (startChildIndex < endChildIndex ? -1 : 1);
                if(((index + dx) < startChildIndex ) && (dx < 0))
                {
                    previous = components[endChildIndex - 1];
                }
                else if(((index + dx) > startChildIndex ) && (dx > 0))
                {
                    previous = components[endChildIndex];
                }
                else
                {
                    previous = components[index + dx];
                }
                break;
            }
        }
        
        if(previous != null)
        {
            previous.requestFocusInWindow();
        }
    }
    
    public class MoveToNextButtonAction extends AbstractAction
    {

        public void actionPerformed(ActionEvent e)
        {
            moveFocusToNextSibling(0, END_CHILD_INDEX);
        }
        
    }
    
    public class MoveToPreviousButtonAction extends AbstractAction
    {

        public void actionPerformed(ActionEvent e)
        {
            moveFocusToPreviousSibling(0, END_CHILD_INDEX);
        }
        
    }
}
