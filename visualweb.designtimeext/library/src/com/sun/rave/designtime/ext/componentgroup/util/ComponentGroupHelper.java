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
package com.sun.rave.designtime.ext.componentgroup.util;

import com.sun.rave.designtime.DesignBean;
import java.awt.Color;
import java.util.Map;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.ext.componentgroup.ColorWrapper;
import com.sun.rave.designtime.ext.componentgroup.ComponentGroup;
import com.sun.rave.designtime.ext.componentgroup.ComponentGroupDesignInfo;
import com.sun.rave.designtime.ext.componentgroup.ComponentGroupHolder;
import com.sun.rave.designtime.ext.componentgroup.impl.ColorWrapperImpl;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * <p>Helper class to paint Component Group colors, etc.</p>
 * @author mbohm
 */
public class ComponentGroupHelper {
    
    /**
     * <p>Get a design context data key to store a component group color.</p>
     */ 
    public static String getComponentGroupColorKey(String holderName, String groupName) {
        return ComponentGroupHolder.COLOR_KEY_PREFIX + holderName + ":" + groupName; //NOI18N
    }
    
    /**
     * <p>Populate a two-dimensional array of <code>ComponentGroup</code> based on the supplied 
     * <code>holders</code>, adding color information if necessary.</p>
     * @param dcontext The design context.
     * @param holders The array of existing holders.
     * @param groupArr The two-dimensional array of <code>ComponentGroup</code> to populate based on <code>holders</code>.
     */ 
    public static void populateColorGroupArray(DesignContext dcontext, ComponentGroupHolder[] holders, ComponentGroup[][] groupArr) {
        populateColorModels(dcontext, holders, groupArr, null);
    }
    
    
    /**
     * <p>Populate a <code>Map&lt;String,Color&gt;</code> based on the supplied 
     * <code>holders</code>, adding color information if necessary.</p>
     * @param dcontext The design context.
     * @param holders The array of existing holders.
     * @param groupArr The <code>Map&lt;String,Color&gt;</code> to populate based on <code>holders</code>.
     */ 
    public static void populateColorMap(DesignContext dcontext, ComponentGroupHolder[] holders, Map<String,Color> colorMap) {
        populateColorModels(dcontext, holders, null, colorMap);
    }
    
    /**
     * <p>Populate a two-dimensional array of <code>ComponentGroup</code> and
     * a <code>Map&lt;String,Color&gt;</code> based on the supplied 
     * <code>holders</code>, adding color information to both if necessary.</p>
     * @param dcontext The design context.
     * @param holders The array of existing holders.
     * @param groupArr The two-dimensional array of <code>ComponentGroup</code> to populate based on <code>holders</code>.
     * @param groupArr The <code>Map&lt;String,Color&gt;</code> to populate based on <code>holders</code>.
     */ 
    public static void populateColorModels(DesignContext dcontext, ComponentGroupHolder[] holders, ComponentGroup[][] groupArr, Map<String,Color> colorMap) {
        if (holders == null || holders.length == 0) {
            return;
        }
        if (groupArr == null) {
            groupArr = new ComponentGroup[holders.length][];
        }
        if (colorMap == null) {
            colorMap = new HashMap<String,Color>();
        }
        List<ComponentGroup> unassignedComponentGroups = new ArrayList<ComponentGroup>();//existing groups that don't have a color assigned in colorMap
        List<String> unassignedComponentGroupKeys = new ArrayList<String>(); //keys in dcontextData corresponding to unassignedComponentGroups 
        for (int h = 0; h < holders.length; h++) {
            ComponentGroupHolder holder = holders[h];
            ComponentGroup[] groups = holder.getComponentGroups(dcontext);
            groupArr[h] = groups;
            if (groups != null) {
                for (int i = 0; i < groups.length; i++) {
                    String holderName = holder.getName();
                    String name = groups[i].getName();
                    String key = ComponentGroupHelper.getComponentGroupColorKey(holderName, name);
                    Object o = dcontext.getContextData(key);
                    Color color = null;
                    if (o instanceof ColorWrapper) {
                        color = ((ColorWrapper)o).getColor();
                        if (color != null) {
                            //color will already be in groups[i]
                            
                            //check to make sure the color is not already in the map
                            Collection<Color> values = colorMap.values();
                            if (values.contains(color)) {
                                groups[i].setColor(null);
                                unassignedComponentGroups.add(groups[i]);
                                unassignedComponentGroupKeys.add(key);
                            } else {
                                colorMap.put(key, color);
                            }
                        } else {
                            unassignedComponentGroups.add(groups[i]);
                            unassignedComponentGroupKeys.add(key);
                        }
                    } else if (o instanceof String) {
                        ColorWrapper cw = new ColorWrapperImpl((String)o);
                        color = cw.getColor();
                        if (color != null) {
                            dcontext.setContextData(key, cw);
                            
                            //color will already be in groups[i]
                            
                            //check to make sure the color is not already in the map
                            Collection<Color> values = colorMap.values();
                            if (values.contains(color)) {
                                groups[i].setColor(null);
                                unassignedComponentGroups.add(groups[i]);
                                unassignedComponentGroupKeys.add(key);
                            } else {
                                colorMap.put(key, color);
                            }
                        } else {
                            unassignedComponentGroups.add(groups[i]);
                            unassignedComponentGroupKeys.add(key);
                        }
                    } else {
                        unassignedComponentGroups.add(groups[i]);
                        unassignedComponentGroupKeys.add(key);
                    }
                }
            }
        }
        for (int i = 0; i < unassignedComponentGroups.size(); i++) {
            ComponentGroup group = unassignedComponentGroups.get(i);
            String key = unassignedComponentGroupKeys.get(i);
            Color c = getLeastUsedColor(colorMap);
            group.setColor(c);
            colorMap.put(key, c);
        }
    }
    
    /**
     * <p>The colors which can be assigned to a Component Group.</p>
     */ 
    public static Color[] DEFAULT_COLOR_SET = new Color[] {
        Color.blue,
        Color.green,
        Color.red,
        Color.yellow,
        Color.magenta,
        Color.orange,
        Color.cyan,
        Color.pink,
        new Color(0,0,128),       //navy blue
        new Color(255,250,205),   //lemon chifon
        new Color(0,100,0),       //dark green     
        new Color(255,228,225),   //misty rose
        new Color(250,128,114),   //salmon
        new Color(224,255,255),   //light cyan
        new Color(255,105,180),   //hot pink
        new Color(205,92,92),     //indian red
        new Color(0,0,205),       //medium blue
        new Color(143,188,143),   //dark sea green
        new Color(238,221,130),   //light goldenrod
        new Color(238,130,238),   //violet
        new Color(244,238,224),   //honeydew 2
        new Color(64,224,208),    //turquoise
        new Color(255,218,185),   //peach puff
        new Color(240,128,128),   //light coral
        new Color(135,206,250),   //light sky blue
        new Color(46,139,87),     //sea green
        new Color(218,165,32),    //goldenrod
        new Color(230,230,250),   //lavender
        new Color(189,183,107),   //dark khaki
        new Color(208,32,144),    //violet red
        new Color(173,255,47),    //green yellow
        new Color(70,130,180),    //steel blue
        new Color(205,133,63),    //peru
        new Color(175,238,238),   //pale turquoise
        new Color(60,179,113),    //medium sea green
        new Color(176,196,222),   //light steel blue
        new Color(186,85,211),    //medium orchid
        new Color(244,164,96),    //sandy brown
        new Color(32,178,170),    //light sea green
        new Color(165,42,42),     //brown
        new Color(50,205,50),     //lime green
    };
    
    /**
     * <p>Get the appropriate <code>Color</code> in the supplied 
     * <code>Map</code>, assigning one if necessary.</p>
     */ 
    public static Color getMappedColor(String key, Map colorMap) {
        Color c = (Color)colorMap.get(key);
        if (c != null) {
            return c;
        }
        c = getLeastUsedColor(colorMap);
        colorMap.put(key, c);
        return c;
    }
    
    /**
     * <p>Of the colors in the default set, get one that appears least in the 
     * supplied <code>colorMap</code>.</p>
     */
    private static Color getLeastUsedColor(Map colorMap) {
        Map<Color,Integer> timesUsed = new HashMap();
        for (Iterator<Color> iter = colorMap.values().iterator(); iter.hasNext(); ) {
            Color c = iter.next();
            Integer times = timesUsed.get(c);
            if (times == null) {
                timesUsed.put(c, new Integer(1));
            }
            else {
                int t = times.intValue();
                timesUsed.put(c, new Integer(t+1));
            }
        }
        
        Color leastUsedColor = null;
        int leastTimesUsed = -1;
        for (int i = 0; i < DEFAULT_COLOR_SET.length; i++) {
            Color c = DEFAULT_COLOR_SET[i];
            Integer times = timesUsed.get(c);
            if (times == null) {    //color c not yet used
                return c;
            }
            else {
                int t = times.intValue();   //color c used t times
                if (leastTimesUsed < 0 || t < leastTimesUsed) {
                    leastTimesUsed = t;
                    leastUsedColor = c;
                }
            }
        }
        return leastUsedColor;
    }
    
    /**
     * <p>Get all the <code>ComponentGroupHolder</code> instances associated
     * with the supplied <code>DesignContext</code>.</p>
     * @param dcontext The design context.
     */ 
    public static ComponentGroupHolder[] getComponentGroupHolders(DesignContext dcontext) {
        //get flat list of beans for this dcontext
        DesignBean[] dcontextBeans = dcontext.getBeans();
        if (dcontextBeans == null || dcontextBeans.length == 0) {
            return null;
        }
        Set<String> designInfoSet = new HashSet<String>();
        List<ComponentGroupHolder> holderList = new ArrayList<ComponentGroupHolder>();
        for (int b = 0; b < dcontextBeans.length; b++) {
            DesignInfo designInfo = dcontextBeans[b].getDesignInfo();
            if (! (designInfo instanceof ComponentGroupDesignInfo)) {
                continue;
            }
            String designInfoClassName = designInfo.getClass().getName();
            if (designInfoSet.contains(designInfoClassName)) {
                continue;
            }
            designInfoSet.add(designInfoClassName);
            ComponentGroupDesignInfo componentGroupDesignInfo = (ComponentGroupDesignInfo)designInfo;
            ComponentGroupHolder[] designInfoHolders = componentGroupDesignInfo.getComponentGroupHolders();
            if (designInfoHolders != null) {
                holderList.addAll(Arrays.asList(designInfoHolders));
            }
        }
        return holderList.toArray(new ComponentGroupHolder[holderList.size()]);
    }
}
