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

package org.netbeans.modules.xml.schema.abe;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;

/**
 *
 *
 * @author Todd Fast, todd.fast@sun.com
 */
public interface InstanceDesignConstants {
    public static final Color LIGHT_YELLOW=new Color(255,255,196);
    public static final Color LIGHT_BLUE=new Color(236,240,252);
    public static final Color DARK_BLUE=new Color(120,152,181);
    public static final Color XP_ORANGE=new Color(255,199,61);
    public static final Color SHADOW_COLOR=new Color(228,228,228);
    //public static final Color TAG_BG=LIGHT_BLUE;
    public static final Color TAG_NAME_COLOR = new Color(0, 0, 178);
    //public static final Color TAG_NAME_SELECTED_COLOR=Color.BLACK;
    public static final Color TAG_NAME_SHARED_COLOR = TAG_NAME_COLOR;
    public static final Color TAG_NAME_READONLY_COLOR = new Color(132, 132, 132);
    
    public static final Font TAG_FONT=new JLabel().getFont();//.deriveFont(36f);
    public static final int TAG_INDENT=TAG_FONT.getSize();
    public static final int COMPOSITOR_CHILDREN_INDENT = 10;
    
    public static int GLOBAL_ELEMENT_PANEL_INDENT = 5;
    
    public final static String NO_NAMESPACE = "NO_NAMESPACE";
    
    
    public static final Color COMPOSITOR_TYPE_LABEL_COLOR = Color.lightGray;
    
    public static final Color COMPOSITOR_TYPE_LABEL_SELECTED_COLOR = Color.BLACK;
    
    public static final Color ITEM_COUNT_COLOR = Color.lightGray;
    
    static Color ATTRIBUTE_NORMAL_BG_COLOR = Color.LIGHT_GRAY;
    
    static Color ATTRIBUTE_COLOR = new Color(0, 126, 0);
    
    static Color NAMESPACE_COLOR = new Color(88, 88, 88);
    
    static Color TAG_BG_SHARED_BOTTOM_GRADIENT_COLOR = new Color( 197, 210, 245);
    static Color TAG_BG_SHARED_TOP_GRADIENT_COLOR = new Color( 215, 224, 247);
    
    static Color TAG_BG_READONLY_BOTTOM_GRADIENT_COLOR = new Color( 219, 219, 219);
    static Color TAG_BG_READONLY_TOP_GRADIENT_COLOR = new Color( 225, 225, 225);
    
    static Color TAG_BG_NORMAL_BOTTOM_GRADIENT_COLOR = new Color( 236, 240, 252);
    static Color TAG_BG_NORMAL_TOP_GRADIENT_COLOR = Color.WHITE;
    
    static Color ATTR_BG_SHARED_COLOR = new Color(161, 182, 238);
    static Color ATTR_BG_READONLY_COLOR = TAG_BG_READONLY_TOP_GRADIENT_COLOR;

    static Color TAG_OUTLINE_COLOR = new Color(120, 152, 181);
    
    static int PROPS_FONT_SIZE = new JLabel().getFont().getSize();

    static Color MOUSEOVER_EXPAND_BUTTON_COLOR = new Color(62, 64, 132);

    static Color NO_BACKGROUND_COLOR = new Color(123, 123, 123);

    static String NEW_ELEMENT_NAME = "newElement";

    static String NEW_ATTRIBUTE_NAME = "newAttribute";

    static String NEW_COMPLEXTYPE_NAME = "newComplexType";

    static String PROP_SHUTDOWN = "shutdown";
}
