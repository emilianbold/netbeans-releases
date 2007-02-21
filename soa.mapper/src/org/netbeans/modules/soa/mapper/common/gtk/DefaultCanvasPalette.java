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

package org.netbeans.modules.soa.mapper.common.gtk;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.netbeans.modules.soa.mapper.common.util.SbynStrings;
import org.netbeans.modules.soa.mapper.common.util.SbynSwingUtil;

/**
 * <p>
 *
 * Title: AbstractCanvasPalette </p> <p>
 *
 * Description: The base class to be used with canvas</p> <p>
 *
 * @author    Charles Zhu
 * @created   December 3, 2002
 */

public class DefaultCanvasPalette
     extends JToolBar {

    /**
     * Description of the Field
     */
    protected ResourceBundle mResourceBundle = null;
    private List mButtonList = new ArrayList();

    /**
     * Constructor
     */
    public DefaultCanvasPalette() {
        initialize();
    }

    /**
     * Constructor
     *
     * @param buttonList  Description of the Parameter
     */
    public DefaultCanvasPalette(List buttonList) {
        this();
        loadButtonList(buttonList);
    }

    /**
     * Constructor
     *
     * @param spec        Description of the Parameter
     * @param key         Description of the Parameter
     */
    protected DefaultCanvasPalette(String spec, String key) {
        this();
        initializeButtons(spec, key);

    }

    /**
     * Description of the Method
     *
     * @param spec  Description of the Parameter
     * @param key   Description of the Parameter
     */
    protected void initializeButtons(String spec, String key) {
        mResourceBundle = ResourceBundle.getBundle(spec);
        List buttonList = createButtons(key);
        loadButtonList(buttonList);
    }


    /**
     * Initializer
     */
    protected void initialize() {
        this.setBackground(Color.white);
        this.setOrientation(JToolBar.HORIZONTAL);
        this.setBorder(BorderFactory.createLineBorder(Color.lightGray));
    }

    /**
     * Load a list of components into palette.
     *
     * @param buttonList - list of buttons
     */
    protected void loadButtonList(List buttonList) {
        if (buttonList != null) {
            Iterator iter = buttonList.iterator();
            Iterator iter2 = null;
            JComponent buttonComponent = null;
            while (iter.hasNext()) {
                iter2 = ((List) iter.next()).iterator();
                while (iter2.hasNext()) {
                    buttonComponent = (JComponent) iter2.next();
                    this.add(buttonComponent);
                }

            }

        }
    }

    /**
     * Add a list of components to the palette.
     *
     * @param buttonList - list of buttons
     */
    public void addButtons(List buttonList) {
        loadButtonList(buttonList);
    }

    /**
     * Creates the buttons
     *
     * @param key  Description of the Parameter
     * @return     Description of the Return Value
     */
    private List createButtons(String key) {
        List retList = new ArrayList();
        String buttonList = getLocalizedString(key);
        String[] buttons = SbynStrings.tokenize(buttonList);
        for (int i = 0; i < buttons.length; i++) {
            retList.add(createButton(buttons[i]));
        }
        return retList;
    }

    /**
     * look up a localized string in the resource bundle.
     *
     * @param key  Description of the Parameter
     * @return     The localizedString value
     */
    protected String getLocalizedString(String key) {
        String value = null;
        try {
            value = mResourceBundle.getString(key);
        } catch (MissingResourceException ex) {
            value = "";
        }
        return value;
    }

    /**
     * Creates a button of the given name.
     *
     * @param name  button name
     * @return      a list components
     */
    protected List createButton(String name) {
        String fullButtonPath = "PALETTE_BUTTON." + name;
        String iconStr = getLocalizedString(fullButtonPath + ".icon");
        Icon icon = SbynSwingUtil.getIcon(iconStr);

        String toolTip = getLocalizedString(fullButtonPath + ".toolTip");
        String typeString = getLocalizedString(fullButtonPath + ".typeId");
        String draggableString = getLocalizedString(fullButtonPath
                + ".draggable");
        String defaultName = getLocalizedString(fullButtonPath
                + ".defaultName");
        String componentClassName = getLocalizedString(fullButtonPath
                + ".componentClassName");
        String actionListenerClassName = getLocalizedString(fullButtonPath
                + ".actionListener");
        String mouseListenerClassName = getLocalizedString(fullButtonPath
                + ".mouseListener");
        String isGroup = getLocalizedString(fullButtonPath + ".isGroup");
        String labelIconName = getLocalizedString(fullButtonPath
                + ".labelIcon");
        String[] marginString = SbynStrings.tokenize(getLocalizedString(
                fullButtonPath + ".margin"), ",");
        String componentPopupName = getLocalizedString(fullButtonPath
                + ".componentPopup");
        String canvasNodeAction = getLocalizedString(fullButtonPath
                + ".canvasNodeAction");
        String key = getLocalizedString(fullButtonPath + ".key");
        String passthroughString = getLocalizedString(fullButtonPath
                + ".passthrough");
        String controlNode = getLocalizedString(fullButtonPath
                + ".controlNode");
        String controlNodeJCompnonent = getLocalizedString(fullButtonPath
                + ".controlNodeJComponent");

        Insets margin = null;
        if (marginString.length == 4) {
            margin = new Insets(Integer.parseInt(marginString[0]),
                Integer.parseInt(marginString[1]),
                Integer.parseInt(marginString[2]),
                Integer.parseInt(marginString[3]));
        }
        String[] upperMarginString = SbynStrings.tokenize(getLocalizedString(
                fullButtonPath + ".upperMargin"), ",");
        String[] lowerMarginString = SbynStrings.tokenize(getLocalizedString(
                fullButtonPath + ".lowerMargin"), ",");
        Component upperMargin = null;
        Component lowerMargin = null;
        int width = 0;
        int height = 0;
        if (upperMarginString.length == 2) {
            width = Integer.parseInt(upperMarginString[0]);
            height = Integer.parseInt(upperMarginString[1]);
            upperMargin = Box.createRigidArea(new Dimension(width, height));
        }

        if (lowerMarginString.length == 2) {
            width = Integer.parseInt(lowerMarginString[0]);
            height = Integer.parseInt(lowerMarginString[1]);
            lowerMargin = Box.createRigidArea(new Dimension(width, height));
        }

        System.err.println("");
        int typeId = Integer.parseInt(typeString);
        boolean draggable = Boolean.valueOf(draggableString).booleanValue();

        PaletteButton button = new PaletteButton(icon, margin, toolTip);
        //(icon,margin,toolTip,typeId);
        button.setDraggable(draggable);
        //Sets the data specifid items
        button.addData("defaultName", defaultName);
        button.addData("componentClassName", componentClassName);
        button.addData("isGroup", isGroup);
        button.addData("labelIcon", labelIconName);
        button.addData("componentPopup", componentPopupName);
        String componentAuxPopupName = this.getLocalizedString(fullButtonPath
                + ".componentAuxPopup");
        button.addData("componentAuxPopup", componentAuxPopupName);
        button.addData("canvasNodeAction", canvasNodeAction);
        button.addData("key", key);

        // if passthroughString is not filled out, then pass in a null object
        if ((passthroughString != null) && (passthroughString.length() == 0)) {
            passthroughString = null;
        }
        button.addData("passthrough", passthroughString);

        List list = new ArrayList();
        if (upperMargin != null) {
            list.add(upperMargin);
        }
        list.add(button);
        mButtonList.add(button);

        // Create separator if specified
        String separator = getLocalizedString(fullButtonPath + ".separator");
        if (separator != null && separator.length() != 0) {
            Icon sepIcon = SbynSwingUtil.getIcon(separator);
            if (sepIcon != null) {
                list.add(new PaletteButton(sepIcon, null, ""));
            }
        }
        if (lowerMargin != null) {
            list.add(lowerMargin);
        }
        //button.add(Box.createRigidArea(new Dimension(1,10)));
        return list;
    }

}
