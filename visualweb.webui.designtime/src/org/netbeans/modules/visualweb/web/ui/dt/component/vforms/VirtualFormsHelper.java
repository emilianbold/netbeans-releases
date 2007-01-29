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
package org.netbeans.modules.visualweb.web.ui.dt.component.vforms;

import java.awt.Color;
import java.util.Map;
import javax.faces.component.ActionSource;
import javax.faces.component.EditableValueHolder;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.web.ui.component.Form;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class VirtualFormsHelper {

    public static DisplayAction getContextItem(DesignContext context) {
        DesignBean formBean = null;
        DesignBean rootBean = context.getRootContainer();
        if (rootBean.getInstance() instanceof Form) {   //just in case
            formBean = rootBean;
        }
        else {
            formBean = findFormBeanFromRoot(rootBean);
        }
        if (formBean != null) {
            return new VirtualFormsCustomizerAction(formBean);
        }
        return null;
    }

    public static DisplayAction getContextItem(DesignBean bean) {
        if (findFormBean(bean) == null) {
            return null;
        }
        if (bean.getInstance() instanceof EditableValueHolder ||
            bean.getInstance() instanceof ActionSource) {
            return new EditVirtualFormsCustomizerAction(bean);
        }
        return null;
    }

    public static DisplayAction getContextItem(DesignBean[] beans) {
        if (findFormBean(beans) == null) {
            return null;
        }
        for (int i = 0; beans != null && i < beans.length; i++) {
            if (beans[i].getInstance() instanceof EditableValueHolder ||
                beans[i].getInstance() instanceof ActionSource) {
                return new EditVirtualFormsCustomizerAction(beans);
            }
        }
        return null;
    }

    public static DesignBean findFormBean(DesignBean[] beans) {
        if (beans == null) {
            return null;
        }
        for (int i = 0; i < beans.length; i++) {
            DesignBean formBean = findFormBean(beans[i]);
            if (formBean != null) {
                return formBean;
            }
        }
        return null;
    }

    public static DesignBean findFormBean(DesignBean bean) {
        if (bean == null) {
            return null;
        }
        if (bean.getInstance() instanceof Form) {
            return bean;
        }
        return findFormBean(bean.getBeanParent());
    }

    private static DesignBean findFormBeanFromRoot(DesignBean parent) {
        if (parent == null) {
            return null;
        }
        DesignBean[] childBeans = parent.getChildBeans();
        for (int i = 0; childBeans != null && i < childBeans.length; i++) {
            DesignBean bean = childBeans[i];
            if (bean.getInstance() instanceof Form) {
                return bean;
            }
            DesignBean formBean = findFormBeanFromRoot(bean);
            if (formBean != null) {
                return formBean;
            }
        }
        return null;
    }

    public static void fillColorMap(DesignBean formBean, Map colorMap) {
        DesignContext context = formBean.getDesignContext();
        Form form = (Form)formBean.getInstance();
        Form.VirtualFormDescriptor[] vforms = form.getVirtualForms();
        List unassignedVForms = new ArrayList();    //existing vforms that don't have a color assigned in colorMap
        for (int i = 0; vforms != null && i < vforms.length; i++) {
            String name = vforms[i].getName();
            String key = VFORMS_COLOR_KEY_PREFIX + name;
            Object o = context.getContextData(key);
            if (o instanceof FormColor && ((FormColor)o).getColor() != null) {
                colorMap.put(name, ((FormColor)o).getColor());
            } else if (o instanceof String) {
                FormColor fc = new FormColor((String)o);
                if (fc.getColor() != null) {
                    context.setContextData(key, fc);
                    colorMap.put(name, fc.getColor());
                } else {
                    unassignedVForms.add(vforms[i]);
                }
            } else {
                unassignedVForms.add(vforms[i]);
            }
        }
        for (Iterator iter = unassignedVForms.iterator(); iter.hasNext(); ) {
            Form.VirtualFormDescriptor vform = (Form.VirtualFormDescriptor)iter.next();
            Color c = getLeastUsedColor(colorMap);
            colorMap.put(vform.getName(), c);
        }
    }
    
    static String VFORMS_COLOR_KEY_PREFIX = "virtualFormColor:"; // NOI18N
    
    static Color[] VFORM_DEFAULT_COLOR_SET = new Color[] {
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
    
    public static Color getFormColor(String formName, Map colorMap) {
        Color c = (Color)colorMap.get(formName);
        if (c != null) {
            return c;
        }
        c = getLeastUsedColor(colorMap);
        colorMap.put(formName, c);
        return c;
    }
    
    //of the colors in the default set, get one that appears least in the colorMap supplied
    private static Color getLeastUsedColor(Map colorMap) {
        Map timesUsed = new HashMap();
        for (Iterator iter = colorMap.values().iterator(); iter.hasNext(); ) {
            Color c = (Color)iter.next();
            Integer times = (Integer)timesUsed.get(c);
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
        for (int i = 0; i < VFORM_DEFAULT_COLOR_SET.length; i++) {
            Color c = VFORM_DEFAULT_COLOR_SET[i];
            Integer times = (Integer)timesUsed.get(c);
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
    
    public static String getNewVirtualFormName(List vformsList) {
        List nameList = new ArrayList();
        for (int i = 0; vformsList != null && i < vformsList.size(); i++) {
            nameList.add(((Form.VirtualFormDescriptor)vformsList.get(i)).getName());
        }
        
        String name = java.util.ResourceBundle.getBundle("com/sun/rave/web/ui/dt/component/vforms/Bundle").getString("newVirtualForm"); // NOI18N
        
        for (int i = 1; i < 999; i++) {
            if (!nameList.contains(name + i)) {
                name = name + i;
                break;
            }
        }
        
        return name;
    }
    
    static class FormColor {
        public FormColor(Color color) {
            this.color = color;
        }
        public FormColor(String fromString) {
            String[] split = fromString.split(","); // NOI18N
            if (split.length > 2) {
                int r = Integer.parseInt(split[0]);
                int g = Integer.parseInt(split[1]);
                int b = Integer.parseInt(split[2]);
                this.color = new Color(r, g, b);
            }
        }
        private Color color;
        public Color getColor() {
            return color;
        }
        public String toString() {
            if (color != null) {
                return color.getRed() + "," + // NOI18N
                       color.getGreen() + "," +  // NOI18N
                       color.getBlue();
            }
            return "";  // NOI18N
        }
    }
}
