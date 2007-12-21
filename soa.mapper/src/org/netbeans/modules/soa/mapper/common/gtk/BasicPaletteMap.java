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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.Icon;
import org.netbeans.modules.soa.mapper.common.util.SbynStrings;
import org.netbeans.modules.soa.mapper.common.util.SbynSwingUtil;

/**
 * <p>
 *
 * Title: </p> <p>
 *
 * Description: Resource for retrieving a Emanaget Palette Button by
 * it's class name <BR>
 * This Class Assumes that each palette button has a unique class</p>
 * <p>
 *
 * @author    A McLaughlin
 * @created   December 3, 2002
 */
public class BasicPaletteMap {

    private static BasicPaletteMap mInstance = null;
    private ResourceBundle mResourceBundle = null;
    // Map of BasicPalette Buttons key is class Name
    private Map mButtonMap = new HashMap();
    private Map mKeyButtonMap = new HashMap();
    // Map of listeners that need to be instantiated with the canvas outside
    // this class the key is the Basic Palette Button
    private Map mActionListeners = new HashMap();
    private Map mMouseListeners = new HashMap();
    // Map of icons
    private Map mIconClassMap = new HashMap();
    private Map mIconKeyMap = new HashMap();


    private BasicPaletteMap() {
        initialize();
    }

    /**
     * Retreives Singleton instance of this object
     *
     * @return   BasicPalettMap
     */
    public static BasicPaletteMap getInstance() {
        if (mInstance == null) {
            mInstance = new BasicPaletteMap();
        }
        return mInstance;
    }

    /**
     * Retrieves the Basic palette button for the given classs name or
     * key
     *
     * @param value  Description of the Parameter
     * @return       PaletteButton
     */
    public PaletteButton getButton(Object value) {
        if (mKeyButtonMap.get(value) == null) {
            return (PaletteButton) mButtonMap.get(value);
        }
        return (PaletteButton) mKeyButtonMap.get(value);
    }


    /**
     * Retrieves the small icon associated with the given className
     *
     * @param className - the class name
     * @return           Icon
     */
    public Icon getIcon(Object className) {
        return (Icon) mIconClassMap.get(className);
    }

    /**
     * Retrieves the small icon associated with the given palette key
     *
     * @param paletteKey - the palette key to the properties file.
     * @return            Icon
     */
    public Icon getIcon(String paletteKey) {
        return (Icon) mIconKeyMap.get(paletteKey);
    }

    /**
     * Returns the action listener class name for the given
     * PaletteButton
     *
     * @param button  - an PaletteButton
     * @return        String
     */
    public String getActionListenerClassName(Object button) {
        return (String) mActionListeners.get(button);
    }

    /**
     * Returns the mouse listener class name for the given class
     * PaletteButton
     *
     * @param button  - an PaletteButton
     * @return        String.
     */
    public String getMouseListenerClassName(Object button) {
        return (String) mMouseListeners.get(button);
    }

    /**
     * Gets the string attribute of the BasicPaletteMap object
     *
     * @param attKey  Description of the Parameter
     * @return        The string value
     */
    public String getString(String attKey) {
        String fullPath = "PALETTE_BUTTON." + attKey;
        return this.getLocalizedString(fullPath);
    }

    /**
     * Initializes resource bundel and palette button map
     */
    private void initialize() {
        mResourceBundle = ResourceBundle.getBundle("emanager_palette");
        populateButtonMap();
    }

    /**
     * look up a localized string in the resource bundle.
     *
     * @param key  Description of the Parameter
     * @return     The localizedString value
     */
    private String getLocalizedString(String key) {
        String value = null;
        try {
            value = mResourceBundle.getString(key);
        } catch (MissingResourceException ex) {
            // ignore exception may not have resource
        }
        return value;
    }

    private void populateButtonMap() {
        String componentList = getLocalizedString("PALETTE_ALL_itemlist");
        String[] components = SbynStrings.tokenize(componentList);
        for (int i = 0; i < components.length; i++) {
            createButton(components[i]);
        }
    }

    /**
     * creates the button and associated components Then adds it to the
     * Button map
     *
     * @param name  - PaletteButton name.
     */
    public void createButton(String name) {
        String fullButtonPath = "PALETTE_BUTTON." + name;
        String iconStr = getLocalizedString(fullButtonPath + ".icon");
        Icon icon = SbynSwingUtil.getIcon(iconStr);
        String palIconStr = getLocalizedString(fullButtonPath + ".paletteIcon");
        Icon pIcon = null;
        if (palIconStr == null || palIconStr.trim().length() == 0) {
            pIcon = icon;
        } else {
            pIcon = SbynSwingUtil.getIcon(palIconStr);
        }

        String toolTip = getLocalizedString(fullButtonPath
                + ".toolTip");
        String typeString = getLocalizedString(fullButtonPath
                + ".typeId");
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
        String marginToken = getLocalizedString(fullButtonPath + ".margin");
        String[] marginString = null;
        if (marginToken != null) {
            marginString = SbynStrings.tokenize(marginToken, ",");
        }
        String componentPopupName = getLocalizedString(fullButtonPath
                + ".componentPopup");
        String canvasNodeAction = getLocalizedString(fullButtonPath
                + ".canvasNodeAction");
        String key = getLocalizedString(fullButtonPath + ".key");
        String passthroughString = getLocalizedString(fullButtonPath
                + ".passthrough");
        Insets margin = null;
        if (marginString != null && marginString.length == 4) {
            margin = new Insets(Integer.parseInt(marginString[0]),
                Integer.parseInt(marginString[1]),
                Integer.parseInt(marginString[2]),
                Integer.parseInt(marginString[3]));
        }
        String[] upperMarginString = null;
        String upperToken = getLocalizedString(fullButtonPath + ".upperMargin");
        if (upperToken != null) {
            upperMarginString = SbynStrings.tokenize(upperToken, ",");
        }
        String[] lowerMarginString = null;
        String lowerToken = getLocalizedString(fullButtonPath + ".lowerMargin");
        if (lowerToken != null) {
            lowerMarginString = SbynStrings.tokenize(lowerToken, ",");
        }
        Component upperMargin = null;
        Component lowerMargin = null;
        int width = 0;
        int height = 0;
        if (upperMarginString != null && upperMarginString.length == 2) {
            width = Integer.parseInt(upperMarginString[0]);
            height = Integer.parseInt(upperMarginString[1]);
            upperMargin = Box.createRigidArea(new Dimension(width, height));
        }

        if (lowerMarginString != null && lowerMarginString.length == 2) {
            width = Integer.parseInt(lowerMarginString[0]);
            height = Integer.parseInt(lowerMarginString[1]);
            lowerMargin = Box.createRigidArea(new Dimension(width, height));
        }

        int typeId = Integer.parseInt(typeString);
        boolean draggable = Boolean.valueOf(draggableString).booleanValue();

        PaletteButton button = new PaletteButton(icon, pIcon, margin, toolTip
                , typeId);
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

        // Add listeners to their Maps
        if (actionListenerClassName != null && !actionListenerClassName.trim()
            .equals("")) {
            //Class actionListenerClass name
            mActionListeners.put(button,
                actionListenerClassName);
            if (mouseListenerClassName != null
                && !mouseListenerClassName.trim().equals("")) {
                // mouse listener class name
                mMouseListeners.put(button,
                    mouseListenerClassName);
            }
        }

        // Add Button to Map

        mButtonMap.put(componentClassName, button);
        mKeyButtonMap.put(key, button);
        mIconClassMap.put(componentClassName, pIcon);
        mIconKeyMap.put(key, pIcon);
    }

}
