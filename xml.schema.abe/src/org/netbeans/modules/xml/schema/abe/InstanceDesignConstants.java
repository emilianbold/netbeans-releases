/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
