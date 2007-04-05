/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://woodstock.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://woodstock.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.rave.designtime.ext.componentgroup.util;

import java.awt.Color;
import java.util.Map;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.ext.componentgroup.ColorWrapper;
import com.sun.rave.designtime.ext.componentgroup.ComponentGroup;
import com.sun.rave.designtime.ext.componentgroup.ComponentGroupHolder;
import com.sun.rave.designtime.ext.componentgroup.impl.ColorWrapperImpl;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ComponentGroupHelper {
    
    public static String getComponentGroupColorKey(String holderName, String groupName) {
        return ComponentGroupHolder.COLOR_KEY_PREFIX + ":" + holderName + ":" + groupName; //NOI18N
    }
    
    public static void populateColorGroupArray(DesignContext dcontext, ComponentGroupHolder[] holders, ComponentGroup[][] groupArr) {
        populateColorModels(dcontext, holders, groupArr, null);
    }
    
    public static void populateColorMap(DesignContext dcontext, ComponentGroupHolder[] holders, Map<String,Color> colorMap) {
        populateColorModels(dcontext, holders, null, colorMap);
    }
    
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
                            colorMap.put(key, color);
                        }
                    } else if (o instanceof String) {
                        ColorWrapper cw = new ColorWrapperImpl((String)o);
                        color = cw.getColor();
                        if (color != null) {
                            dcontext.setContextData(key, cw);
                            colorMap.put(key, color);
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
    
    public static Color getWrappedColor(String groupName, Map colorMap) {
        Color c = (Color)colorMap.get(groupName);
        if (c != null) {
            return c;
        }
        c = getLeastUsedColor(colorMap);
        colorMap.put(groupName, c);
        return c;
    }
    
    //of the colors in the default set, get one that appears least in the colorMap supplied
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
}
