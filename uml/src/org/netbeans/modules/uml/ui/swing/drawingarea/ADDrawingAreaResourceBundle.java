/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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


package org.netbeans.modules.uml.ui.swing.drawingarea;

import java.util.*;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import com.tomsawyer.util.TSSystem;
import com.tomsawyer.editor.TSELocalization;
import com.tomsawyer.editor.TSEImage;
import com.tomsawyer.editor.TSEGraphWindow;
import javax.swing.border.Border;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramEnums;
import org.netbeans.modules.uml.resources.images.ImageUtil;

public abstract class ADDrawingAreaResourceBundle extends ListResourceBundle
{
    
    Vector appButtons = new Vector(90, 10);
    
    ADParameterReader reader;
    
    ADDrawingAreaControl m_drawingArea;
    
    Hashtable radioControllers = new Hashtable(11, 0.9f);
    
    private AbstractButton defaultButton;
    private HashMap layoutButtonMap = new HashMap<String, AbstractButton>();
    
    public void setDrawingArea(ADDrawingAreaControl pDrawingArea)
    {
        this.m_drawingArea = pDrawingArea;
    }
    
    /**
     * This method returns the number of resources held in this
     * resource bundle.
     */
    public int size()
    {
        return (this.getContents().length);
    }
    
    /**
     * This method redefines the standard behavior of the <code>
     * getObject</code> method of the ListResourceBundle class.
     * Instead of throwing an exception, it returns null if the given
     * resource is not found.
     */
    public Object getObjectResource(String key)
    {
        Object value = null;
        
        try
        {
            value = this.getObject(key);
        }
        catch (MissingResourceException resourceError)
        {
        }
        
        return (value);
    }
    
    /**
     * This method redefines the standard behavior of the <code>
     * getString</code> method of the ListResourceBundle class.
     * Instead of throwing an exception, it returns null if the given
     * resource is not found.
     */
    public String getStringResource(String key)
    {
        String value = null;
        
        if (this.reader != null)
        {
            value = this.reader.getParameter(key);
        }
        
        if (value == null)
        {
            try
            {
                value = this.getString(key);
            }
            catch (MissingResourceException resourceError)
            {
            }
        }
        
        return (value);
    }
    
    /**
     * This method returns an icon resource from the resource bundle
     * associated with the specified class. It returns null if the
     * icon resource could not be loaded.
     */
    public ImageIcon getIconResource(String resourceName, Class resourceClass)
    {
        ImageIcon icon = null;
        
        String imagePath = this.getStringResource(resourceName);
        
        if (imagePath != null)
        {
            Image image = TSEImage.loadImage(resourceClass, imagePath);
            
            if (image != null)
            {
                icon = new ImageIcon(image);
            }
        }
        
        return (icon);
    }
    
    /**
     * This method returns the first field(key) of the resource table
     * at the specified index.
     */
    public Object getKeyAt(int index)
    {
        return (this.getContents()[index][0]);
    }
    
    /**
     * This method returns the second field(value) of the resource
     * table at the specified index.
     */
    public Object getValueAt(int index)
    {
        return (this.getContents()[index][1]);
    }
    
    /**
     * This method locates the first occurence of the resource
     * starting with the specific name in the resource table. It
     * returns the index of the resource in the table if it is found,
     * and the length of the table plus 1 otherwise.
     */
    public int locate(String name)
    {
        Object[][] resourceTable = this.getContents();
        
        // skip resources until the first item starting with the given
        // name is found
        
        for (int index = 0; index < resourceTable.length; index++)
        {
            String resource = (String) resourceTable[index][0];
            
            if (resource.startsWith(name))
            {
                return (index);
            }
        }
        
        return (resourceTable.length + 1);
    }
    
    /**
     * This method sets the parameter reader that provides access to
     * the parameters that were re-specified at the execution time,
     * rather than compile-time.
     */
    public void setParameterReader(ADParameterReader reader)
    {
        this.reader = reader;
    }
    
    // ---------------------------------------------------------------------
    // Section: GUI building
    // ---------------------------------------------------------------------
    
    /**
     * This method creates a toolbar specified by a given name in the
     * resource table. If the toolbar is not present, it returns null.
     */
    public JToolBar createToolBar(String name, ActionListener listener)
    {
        // locate resources associated with the named toolbar
        int index = this.locate(name);
        
        // if index is outside the legal range there is no such toolbar
        
        if (index >= this.size())
        {
            return (null);
        }
        
        // OK, we have some resources; add toolbar items
        JToolBar toolbar = new JToolBar();
        
        // set the floatable property of the toolbar
        toolbar.setFloatable("true".equals(this.getStringResource(name + ".floatable")));
        Border b = (Border)UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        toolbar.setBorder(b);
        // set the orientation of the toolbar: either vertical or horizontal
        String orientation = this.getStringResource(name + ".orientation");
        if ("vertical".equals(orientation))
        {
            toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        }
        
        this.populateToolbar(toolbar, index + 1, listener);
        
        TSELocalization.setComponentOrientation(toolbar);
        
        return (toolbar);
    }
    
    /**
     * This method populates a toolbar with plain and toggle buttons.
     * It returns the index of the last entry scanned in the resource
     * table.
     */
    public int populateToolbar(JToolBar toolbar, int index, ActionListener listener)
    {
        String tooltipFormat = TSEGraphWindow.getToolTipFormat();
        String type = (String) this.getKeyAt(index);
        boolean needSeparator = false;
        
        while ("item".equals(type))
        {
            String name = (String) this.getValueAt(index);
            
            if ((name == null) || (name.length() <= 0))
            {
                needSeparator = true;
            }
            else
            {
                JComponent item = null;
                
                // implementation for zoom combo box
                if (name.equals("zoom.comboBox"))
                {
                    JComboBox comboBox = this.m_drawingArea.getZoomComboBox();
                    comboBox.addActionListener(listener);
                    item = comboBox;
                }
                else
                {
                    AbstractButton button;
                    
                    // see if it is a radio or plain menu item
                    String group = this.getStringResource(name + ".group");
                    
                    if (group != null)
                    {
                        button = new JToggleButton();
                        this.getRadioController(group + ".button").add(button);
                        String checked = this.getStringResource(name + ".checked");
                        if ("true".equals(checked))
                            button.setSelected(true);
                        
                        
                    }
                    else
                    {
                        // see if the item is to be checked
                        String checked = this.getStringResource(name + ".checked");
                        
                        if (checked == null)
                        {
                            /*Added by Smitha - Fix for bug# 6253669*/
                            //							if(name.equals("main.showFriendly"))
                            //							{
                            //								button = new JToggleButton();
                            //							}else
                            //							{
                            button = new JButton();
                            //							}
                        }
                        else
                        {
                            button = new JToggleButton();
                            button.setSelected("true".equals(checked));
                        }
                    }
                    if ("true".equals(getStringResource(name + ".default")))
                        defaultButton = button;
                    
                    button.setRequestFocusEnabled(false);
                    
                    // since the button icons are 16x15 pixels add an extra
                    // pixel to the bottom pad.
                    
                    button.setMargin(new Insets(1, 1, 2, 1));
                    
                    // check if there is a command associated with the
                    // item. If there is the item gets a command listener.
                    // Otherwise disable it.
                    
                    String command = this.getStringResource(name + ".command");
                    
                    if (command != null)
                    {
                        button.setActionCommand(command);
                        button.addActionListener(listener);
                        
                        // only buttons with commands are remembered
                        this.appButtons.addElement(button);
                    }
                    else
                    {
                        button.setEnabled(false);
                    }
                    
                    ImageIcon icon = this.getIconResource(name + ".icon.pressed", ImageUtil.class);
                    
                    icon = this.getIconResource(name + ".icon", ImageUtil.class);
                    
                    if (icon != null)
                    {
                        button.setIcon(icon);
                        
                        // This is a workaround for a Swing bug (ID 4363569)
                        // in JDK 1.3
                        
                        if (TSSystem.isJVM13())
                        {
                            ImageIcon disabledIcon = new ImageIcon(GrayFilter.createDisabledImage(icon.getImage()));
                            
                            button.setDisabledIcon(disabledIcon);
                        }
                    }
                    else
                    {
                        String text = this.getStringResource(name + ".text");
                        
                        if (text != null)
                        {
                            button.setText(text);
                        }
                    }
                    
                    item = button;
                    if ("layout".equals(group)) // NOI18N
                    {
                        layoutButtonMap.put(command, button);
                    }
                }
                
                
                
                if (item != null)
                {
                    String tooltip = this.getStringResource(name + ".tooltip");
                    
                    if (tooltip != null)
                    {
                        if (TSSystem.isJVM13orAbove())
                        {
                            item.setToolTipText(
                                    TSSystem.replace(tooltipFormat, TSEGraphWindow.TOOLTIP_PLACEHOLDER, tooltip));
                        }
                        else
                        {
                            item.setToolTipText(tooltip);
                        }
                    }
                    
                    if (needSeparator)
                    {
                        toolbar.addSeparator();
                        needSeparator = false;
                    }
                    //Jyothi: Fix for Bug#6252914 - Tool bar menu: layout sequence diagram should be shown on sequence diagram only.
                    if ( (m_drawingArea.getDiagramKind() != DiagramEnums.DK_SEQUENCE_DIAGRAM ) && (name.equals("main.layout.layoutSequenceDiagram")))
                    {
                        //do NOT add item to the toolbar.. we don't want seqlayout button on non-seq diagrams
                    }
                    else if ((m_drawingArea.getDiagramKind() == DiagramEnums.DK_SEQUENCE_DIAGRAM) &&
                            (
                            (name.equals("main.layout.hierarchicalLayout")) ||
                            (name.equals("main.layout.orthogonalLayout")) ||
                            (name.equals("main.layout.symmetricLayout")) ||
                            (name.equals("main.layout.incrementalLayout"))
                            )
                            )
                    {
                        //do NOT add the item to the toolbar -- we don't want other layout buttons on sequence diagram
                    }
                    else
                    {
                        toolbar.add(item);
                    }
                }
            }
            
            type = (String) this.getKeyAt(++index);
        }
        
        return (index);
    }
    
    
    /**
     * This method returns an enumeration of all buttons created by
     * this resource.
     */
    public Enumeration getAllButtons()
    {
        return (this.appButtons.elements());
    }
    
    /**
     * This method returns the button group controller with a given
     * name. It returns null if it cannot find one.
     */
    public ButtonGroup getRadioController(String name)
    {
        ButtonGroup group = (ButtonGroup) this.radioControllers.get(name);
        
        if (group == null)
        {
            group = new ButtonGroup();
            this.radioControllers.put(name, group);
        }
        
        return (group);
    }
    
    public void setDefault()
    {
        if (defaultButton != null)
            defaultButton.setSelected(true);
    }
    
    public void setLayoutStyle(int style)
    {
        String command = ADDrawingAreaConstants.APPLY_LAYOUT + "." + style;
        AbstractButton button = (AbstractButton)layoutButtonMap.get(command);
        if (button != null && !button.isSelected())
            button.setSelected(true);
    }
}
