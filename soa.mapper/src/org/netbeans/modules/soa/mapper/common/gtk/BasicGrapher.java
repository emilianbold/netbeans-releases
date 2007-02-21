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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;

//import com.stc.egate.gui.common.gtk.jgo.JGoCanvasObjectFactory;
// to be loaded
import org.netbeans.modules.soa.mapper.common.util.SbynStrings;
import org.netbeans.modules.soa.mapper.common.util.SbynSwingUtil;

/**
 * For DemoGrapher, make sure you know that this class itself is a view
 * in our application's MVC framework, despite of the fact that it also
 * contains a sub-MVC universe, which make it somewhat confusing at
 * times.<p>
 *
 * <p>
 *
 * Copyright: Copyright (c) 2002</p> <p>
 *
 * Company: </p>
 *
 * @author    Jone Lin
 * @created   December 3, 2002
 * @version   1.0
 */

public class BasicGrapher {

    private static final String DEFAULT_CANVAS_FACTORY = "com.stc.egate.gui.common.gtk.jgo.JGoCanvasObjectFactory";
    /**
     * Description of the Field
     */
    protected static HashMap mConfigData = new HashMap();
    // factory should take the mConfigData as input parameter
    /**
     * Description of the Field
     */
    protected static ICanvasObjectFactory mFactory = null;

    /**
     * Loger used for loging messages
     */
    private final static Logger mTheLogger = Logger.getLogger(BasicGrapher.class.getName());

    /**
     * Description of the Field
     */
    protected HashMap mClassNameToIconMap = new HashMap();
    /**
     * Node class name to palette button map
     */
    protected HashMap mClassNameToButtonMap = new HashMap();
    /**
     * Description of the Field
     */
    protected JComponent mTop = null;
    /**
     * Description of the Field
     */
    protected ICanvasModel mModel = null;
    /**
     * Description of the Field
     */
    protected ICanvas mCanvas = null;
    /**
     * Description of the Field
     */
    protected DefaultCanvasPalette mPalette = null;
    /**
     * Description of the Field
     */
    protected BasicCanvasController mController = null;

    static {
        try {
            Class klass = Class.forName(DEFAULT_CANVAS_FACTORY);
            Method getInstanceMethod = klass.getMethod("getInstance", null);
            mFactory = (ICanvasObjectFactory) getInstanceMethod.invoke(klass, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Constructs BasicGrapher object. It calls the following method in
     * order.
     * <ul>
     *   <li> createTopComponent - creates a Component that hosts the
     *   palette and cavas. The default implementation creates a JPanel
     *   and assigned it to <code>mTop</code></li>
     *   <li> createModel - creates canvas model</li>
     *   <li> createCanvas - create canvas view</li>
     *   <li> createPalette - create canvas palette</li>
     *   <li> createContorller - create canvas controller</li>
     * </ul>
     *
     *
     * @param spec  reserved for extenral configuration information.
     */
    public BasicGrapher(String spec) {
        mTop = createTopComponent();
        mModel = createModel();
        mCanvas = createCanvas();
        if (mModel != null) {
            mCanvas.setModel(mModel);
        }
        mPalette = createPalette();
        initLayout();
        mController = createController();
    }

    /**
     * Constructor for the BasicGrapher object
     *
     * @param src  Description of the Parameter
     */
    public BasicGrapher(BasicGrapher src) {
        mTop = createTopComponent();
        mModel = src.getModel();
        mCanvas = createCanvas();
        if (mModel != null) {
            mCanvas.setModel(mModel);
        }
        mPalette = createPalette();
        initLayout();
        mController = createController();
    }

    /**
     * Initializes the layout of the palette and canvas. The default
     * implementation set <code>mTop</code> with a BorderLayout, puts
     * palette to the NORTH, and puts cavas in the CENTER.
     */
    protected void initLayout() {
        mTop.setLayout(new BorderLayout());
        if (mPalette != null) {
            mTop.add(mPalette, BorderLayout.NORTH);
        }
        if (mCanvas.getUIComponent() != null) {
            mTop.add(mCanvas.getUIComponent(), BorderLayout.CENTER);
        }
    }

    /**
     * Creates the top Components that hosts the palette and canvas.
     * This default implementation creates a JPanel.
     *
     * @return   Description of the Return Value
     */
    protected JComponent createTopComponent() {
        return new JPanel();
    }

    /**
     * Creates a default palette. This sets up the <code>mPalette</code>
     * .
     *
     * @return   Description of the Return Value
     */
    protected DefaultCanvasPalette createPalette() {
        return new DefaultCanvasPalette();
    }

    /**
     * Creates a default canvas model. This sets up the <code>mModel</code>
     * .
     *
     * @return   Description of the Return Value
     */
    protected ICanvasModel createModel() {
        return mFactory.createCanvasModel(null);
    }

    /**
     * Creates a default canvas. This sets up the <code>mCanvas</code>.
     *
     * @return   Description of the Return Value
     */
    protected ICanvas createCanvas() {
        return mFactory.createCanvas(null);
    }

    /**
     * Creates a default controller. This implementation creates a
     * BasicCanvasController.
     *
     * @return   Description of the Return Value
     */
    protected BasicCanvasController createController() {
        return new BasicCanvasController(mCanvas);
    }

    /**
     * Retrieves the top level component of this grapher. Usually, this
     * is used so that it may be added to a container.
     *
     * @return   top level component of type Component.
     */
    public JComponent getTopComponent() {
        return mTop;
    }

    /**
     * Gets the Canvas
     *
     * @return   returns a canvas.
     */
    protected ICanvas getCanvas() {
        return mCanvas;
    }

    /**
     * Gets the Canvas
     *
     * @return   returns a canvas.
     */
    protected ICanvasModel getModel() {
        return mModel;
    }

    /**
     * Set the controller where application specific behavior may be
     * implemented.
     *
     * @param controller  a controller that implements ICanvasController
     */
    public void setCanvasController(ICanvasController controller) {
        mCanvas.setCanvasController(controller);
        controller.setCanvas(mCanvas);
    }

    /**
     * Initialize palette based on the spec. This default implementation
     * treat the spec as a resource bundle name. Since the spec may be
     * shared by other palette, so a key is provided to get a list of
     * button names to be created.
     *
     * @param spec         palette spec
     * @param itemListKey  the key to access the spec
     */
    public void initPalette(String spec, String itemListKey) {
        ResourceBundle resBundle = ResourceBundle.getBundle(spec);
        List buttonList = createButtons(resBundle, itemListKey);
        mPalette.addButtons(buttonList);
    }

    /**
     * Creates the buttons.
     *
     * @param resBundle  resource bundle
     * @param key        to access the resource bundle
     * @return           a list of component created for the palette.
     */
    protected List createButtons(ResourceBundle resBundle, String key) {
        List retList = new ArrayList();
        String buttonList = getLocalizedString(resBundle, key);
        String[] buttons = SbynStrings.tokenize(buttonList);
        for (int i = 0; i < buttons.length; i++) {
            retList.add(createButton(resBundle, buttons[i]));
        }
        return retList;
    }

    /**
     * look up a localized string in the resource bundle.
     *
     * @param resBundle  Description of the Parameter
     * @param key        Description of the Parameter
     * @return           The localizedString value
     */
    protected String getLocalizedString(ResourceBundle resBundle, String key) {
        String value = "";
        try {
            value = resBundle.getString(key);
        } catch (MissingResourceException ex) {
            mTheLogger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return value;
    }

    /**
     * Create a palette button based on the the name (used as a key) and
     * the given resouces bundle. The button spec may contain
     * information to cause additional components (such as spacer) to be
     * created.
     *
     * @param resBundle  resource bundle that contains the palette
     *      information
     * @param name       name of the button, used as a key to access
     *      resource bundle
     * @return           a list of component, depending on the button
     *      spec.
     */
    //JL, should conisder explicit creation of the spacer; i.e. this
    //    file should simply create a button.
    protected List createButton(ResourceBundle resBundle, String name) {
        mTheLogger.finest("Creating button " + name);
        String fullButtonPath = "PALETTE_BUTTON." + name;
        String rIconStr = getLocalizedString(resBundle, fullButtonPath + ".withRuleIcon");
        String iconStr = getLocalizedString(resBundle, fullButtonPath + ".icon");
        Icon icon = SbynSwingUtil.getIcon(iconStr);
        String toolTip = getLocalizedString(resBundle, fullButtonPath + ".toolTip");
        String draggableString = getLocalizedString(resBundle, fullButtonPath + ".draggable");
        String defaultName = getLocalizedString(resBundle, fullButtonPath + ".defaultName");
        String componentClassName = getLocalizedString(resBundle, fullButtonPath + ".componentClassName");
        String actionListenerClassName = getLocalizedString(resBundle, fullButtonPath + ".actionListener");
        String mouseListenerClassName = getLocalizedString(resBundle, fullButtonPath + ".mouseListener");
        String isGroup = getLocalizedString(resBundle, fullButtonPath + ".isGroup");
        String labelIconName = getLocalizedString(resBundle, fullButtonPath + ".labelIcon");
        String[] marginString = SbynStrings.tokenize(getLocalizedString(resBundle, fullButtonPath + ".margin"), ",");
        String componentPopupName = getLocalizedString(resBundle, fullButtonPath + ".componentPopup");
        String canvasNodeAction = getLocalizedString(resBundle, fullButtonPath + ".canvasNodeAction");
        String controlNodeStr = getLocalizedString(resBundle, fullButtonPath + ".controlNode");
        String controlNodeJComponentStr = getLocalizedString(resBundle, fullButtonPath + ".controlNodeJComponent");
        String key = getLocalizedString(resBundle, fullButtonPath + ".key");
        Insets margin = null;
        if (marginString.length == 4) {
            margin = new Insets(Integer.parseInt(marginString[0]),
                Integer.parseInt(marginString[1]),
                Integer.parseInt(marginString[2]),
                Integer.parseInt(marginString[3]));
        }
        String[] upperMarginString = SbynStrings.tokenize(getLocalizedString(
                resBundle, fullButtonPath + ".upperMargin"), ",");
        String[] lowerMarginString = SbynStrings.tokenize(getLocalizedString(
                resBundle, fullButtonPath + ".lowerMargin"), ",");
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

        // add an entry in component_calss_name to icon map
        mClassNameToIconMap.put(componentClassName, icon);

        //int typeId = Integer.parseInt(typeString);
        boolean draggable = Boolean.valueOf(draggableString).booleanValue();

        PaletteButton button = new PaletteButton(icon, margin, toolTip);
        mClassNameToButtonMap.put(componentClassName, button);

        button.setDraggable(draggable);
        //Sets the data specific items
        button.addData("defaultName", defaultName);
        button.addData("componentClassName", componentClassName);
        button.addData("isGroup", isGroup);
        button.addData("icon", iconStr);
        button.addData("labelIcon", labelIconName);
        button.addData("componentPopup", componentPopupName);
        String componentAuxPopupName = this.getLocalizedString(resBundle
                , fullButtonPath + ".componentAuxPopup");
        button.addData("componentAuxPopup", componentAuxPopupName);
        button.addData("canvasNodeAction", canvasNodeAction);
        button.addData("controlNode", controlNodeStr);
        button.addData("controlNodeJComponent", controlNodeJComponentStr);
        button.addData("key", key);
        if (rIconStr != null && rIconStr.length() > 0) {
            Icon withRuleIcon = SbynSwingUtil.getIcon(rIconStr);
            button.addData("withRuleIcon", withRuleIcon);
        }
        List list = new ArrayList();
        if (upperMargin != null) {
            list.add(upperMargin);
        }
        list.add(button);
        /*
        // Create separator if specified
        String separator = getLocalizedString(resBundle, fullButtonPath + ".separator");
        if (separator != null && separator.length() != 0) {
            Icon sepIcon = SbynSwingUtil.getIcon(separator);
            if (sepIcon != null) {
                list.add(new PaletteButton(sepIcon, null, ""));
            }
        }*/
        if (lowerMargin != null) {
            list.add(lowerMargin);
        }
        return list;
    }

    /**
     * Gets an icon for this class. The map is initially built up from
     * during palette initialization.
     *
     * @param className  model component class name
     * @return           an icon
     */
    protected Icon getIcon(String className) {
        return (Icon) mClassNameToIconMap.get(className);
    }

    /**
     * Creates and adds a node in the canvas.
     *
     * @param className  class name of the component, used to obtain the icon
     * @param label      canvas label, usually the name
     * @param size       node size
     * @param location   node location
     */
    protected void addNode(String className, String label, Dimension size, Point location) {
        mCanvas.createCanvasNode(location, size, getIcon(className), label);
    }
}
