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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.jdesktop.layout;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class that can be used to determine the baseline of a
 * particular component.  The static method <code>getBaseline</code> uses the
 * following algorithm to determine the baseline:
 * <ol>
 * <li>If the component has a <code>getBaseline(Component,int,int)</code> 
 *     method, invoke it.
 * <li>If there is a <code>UIManager</code> property of the name
 *     <code>Baseline.instance</code>, forward the call to that Baseline.
 * <li>Otherwise use the built in support.
 * </ol>
 * <p>
 * This class is primarily useful for JREs prior to 1.6.  In 1.6 API for this
 * was added directly to Component, Component and the
 * appropriate ComponentUIs.  When run on a JRE of 1.6 or greater this will directly
 * call into the getBaseline method of Component.
 *
 * @version $Revision$
 */
public class Baseline {
    //
    // Used by button and label baseline code, cached to avoid excessive
    // garbage.
    //
    private static final Rectangle viewRect = new Rectangle();
    private static final Rectangle textRect = new Rectangle();
    private static final Rectangle iconRect = new Rectangle();

    // 
    // These come from TitleBorder.  NOTE that these are NOT final in
    // TitledBorder
    //
    private static final int EDGE_SPACING = 2;
    private static final int TEXT_SPACING = 2;


    private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);

    // Prototype label for calculating baseline of tables.
    private static Label TABLE_LABEL;

    // Prototype label for calculating baseline of lists.
    private static Label LIST_LABEL;

    // Prototype label for calculating baseline of trees.
    private static Label TREE_LABEL;

    // Corresponds to com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel
    private static Class CLASSIC_WINDOWS;
    // Whether or not we've tried to load WindowsClassicLookAndFeel.
    private static boolean checkedForClassic;

    // Map<Class,Method> 
    private static final Map BASELINE_MAP = Collections.
            synchronizedMap(new HashMap());
    private static Method COMPONENT_BASELINE_METHOD;

    static {
        COMPONENT_BASELINE_METHOD = null;
        try {
            COMPONENT_BASELINE_METHOD = Component.class.getMethod(
                "getBaseline", new Class[] { int.class, int.class});
        } catch (NoSuchMethodException nsme) {
        }
    }

    /**
     * Returns the baseline for the specified component, or -1 if the
     * baseline can not be determined.  The baseline is measured from
     * the top of the component.  This method returns the baseline based
     * on the preferred size.
     *
     * @param component Component to calculate baseline for
     * @return baseline for the specified component
     */
    public static int getBaseline(Component component) {
        Dimension pref = component.getPreferredSize();
        return getBaseline(component, pref.width, pref.height);
    }

    private static Method getBaselineMethod(Component component) {
        if (COMPONENT_BASELINE_METHOD != null) {
            return COMPONENT_BASELINE_METHOD;
        }
        Class klass = component.getClass();
        while (klass != null) {
            if (BASELINE_MAP.containsKey(klass)) {
                Method method = (Method)BASELINE_MAP.get(klass);
                return method;
            }
            klass = klass.getSuperclass();
        }
        klass = component.getClass();
        Method[] methods = klass.getMethods();
        for (int i = methods.length - 1; i >= 0; i--) {
            Method method = methods[i];
            if ("getBaseline".equals(method.getName())) {
                Class[] params = method.getParameterTypes();
                if (params.length == 2 && params[0] == int.class &&
                        params[1] == int.class) {
                    BASELINE_MAP.put(klass, method);
                    return method;
                }
            }
        }
        BASELINE_MAP.put(klass, null);
        return null;
    }

    private static int invokeBaseline(Method method, Component c, int width,
            int height) {
        int baseline = -1;
        try {
            baseline = ((Integer)method.invoke(c,
                    new Object[] { new Integer(width),
                            new Integer(height) })).intValue();
        } catch (IllegalAccessException iae) {
        } catch (IllegalArgumentException iae2) {
        } catch (InvocationTargetException ite2) {
        }
        return baseline;
    }
    
    /**
     * Returns the baseline for the specified component, or a value less 
     * than 0 if the baseline can not be determined.  The baseline is measured 
     * from the top of the component.
     *
     * @param component Component to calculate baseline for
     * @param width Width of the component to determine baseline for.
     * @param height Height of the component to determine baseline for.
     * @return baseline for the specified component
     */
    public static int getBaseline(Component component, int width, int height) {
        if (1 == 1) return -1;
        Method baselineMethod = getBaselineMethod(component);
        if (baselineMethod != null) {
            return invokeBaseline(baselineMethod, component, width, height);
        }
        Object baselineImpl = null; //UIManager.get("Baseline.instance");
        if (baselineImpl != null && (baselineImpl instanceof Baseline)) {
            return ((Baseline)baselineImpl).getComponentBaseline(
                    component, width, height);
        }
        String uid = null;
        if (component instanceof Button) uid = "ButtonUI";
        if (component instanceof Label) uid = "LabelUI";
        if (component instanceof TextField) uid = "TextFieldUI";
        int baseline = -1;
        if (uid == "ButtonUI" || uid == "CheckBoxUI" ||
                uid == "RadioButtonUI" || uid == "ToggleButtonUI") {
            baseline = getButtonBaseline((Button)component,
                                         height);
        }
        else if (uid == "TextAreaUI") {
            return getTextAreaBaseline((TextArea)component, height);
        }
        else if (uid == "FormattedTextFieldUI" ||
                 uid == "PasswordFieldUI" ||
                 uid == "TextFieldUI") {
            baseline = getSingleLineTextBaseline((TextComponent)component,
                                                 height);
        }
        else if (uid == "LabelUI") {
            baseline = getLabelBaseline((Label)component, height);
        }
        else if (uid == "ListUI") {
            baseline = getListBaseline((List)component, height);
        }
        else if (uid == "PanelUI") {
            baseline = getPanelBaseline((Panel)component, height);
        }
        else if (uid == "ScrollPaneUI") {
            baseline = getScrollPaneBaseline((ScrollPane)component, height);
        }        
        return Math.max(baseline, -1);
    }

    private static Insets rotateInsets(Insets topInsets, int targetPlacement) {
          return new Insets(topInsets.top, topInsets.left,
                            topInsets.bottom, topInsets.right);
    }
    
    private static int getTextAreaBaseline(TextArea text, int height) {
        FontMetrics fm = text.getFontMetrics(text.getFont());
        return 6 + fm.getAscent();
    }
    
    private static int getListBaseline(List list, int height) {
        int rowHeight = -1;//list.getFixedCellHeight();
        if (LIST_LABEL == null) {
            LIST_LABEL = new Label("X");
        }
        Label label = LIST_LABEL;
        label.setFont(list.getFont());
        // JList actually has much more complex behavior here.
        // If rowHeight != -1 the rowHeight is either the max of all cell
        // heights (layout orientation != VERTICAL), or is variable depending
        // upon the cell.  We assume a default size.
        // We could theoretically query the real renderer, but that would
        // not work for an empty model and the results may vary with 
        // the content.
        if (rowHeight == -1) {
            rowHeight = label.getPreferredSize().height;
        }
        return getLabelBaseline(label, rowHeight) + 6;
    }

    private static int getScrollPaneBaseline(ScrollPane sp, int height) {
//        Component view = sp.getViewport().getView();
//        if (view instanceof Component) {
//            int baseline = getBaseline((Component)view);
//            if (baseline > 0) {
//                return baseline + sp.getViewport().getY();
//            }
//        }
        return -1;
    }

    private static int getPanelBaseline(Panel panel, int height) {
        return -1;
    }

    private static int getLabelBaseline(Label label, int height) {
        FontMetrics fm = label.getFontMetrics(label.getFont());

        resetRects(label, height);

        return textRect.y + fm.getAscent();
    }

    /**
     * Returns the baseline for single line text components, like
     * <code>JTextField</code>.
     */
    private static int getSingleLineTextBaseline(TextComponent textComponent,
                                                 int h) {
//        View rootView = textComponent.getUI().getRootView(textComponent);
//        if (rootView.getViewCount() > 0) {
//            Insets insets = textComponent.getInsets();
//            int height = h - insets.top - insets.bottom;
//            int y = insets.top;
//            View fieldView = rootView.getView(0);
//	    int vspan = (int)fieldView.getPreferredSpan(View.Y_AXIS);
//	    if (height != vspan) {
//		int slop = height - vspan;
//		y += slop / 2;
//	    }
//            FontMetrics fm = textComponent.getFontMetrics(
//                                 textComponent.getFont());
//            y += fm.getAscent();
//            return y;
//        }
        return -1;
    }

    /**
     * Returns the baseline for buttons.
     */
    private static int getButtonBaseline(Button button, int height) {
        FontMetrics fm = button.getFontMetrics(button.getFont());

        resetRects(button, height);

        String text = button.getLabel();
        if (text != null && text.startsWith("<html>")) {
            return -1;
        }
        return textRect.y + fm.getAscent();
    }

    private static void resetRects(Component c, int height) {
        Insets insets = new Insets(5,5,5,5);//c.getInsets();
        viewRect.x = insets.left;
        viewRect.y = insets.top;
        viewRect.width = c.getWidth() - (insets.right + viewRect.x);
        viewRect.height = height - (insets.bottom + viewRect.y);
        textRect.x = textRect.y = textRect.width = textRect.height = 0;
        iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
    }
   
    /**
     * Creates an instance of Baseline.  You typically don't create a
     * Baseline.  The constructor is provided by look and feels that wish
     * to provide baseline support.
     * <p>
     * A custom look and feel that wants to provide <code>Baseline</code>
     * support should put the instance in the defaults returned
     * from <code>getDefaults</code>.  If you want to override the 
     * baseline suport for a look and feel place the instance in the defaults
     * returned from UIManager.getLookAndFeelDefaults().  Tthis will ensure
     * that if the look and feel changes the appropriate baseline can be used.
     */
    protected Baseline() {
    }
    
    /**
     * Returns the baseline for the specified component, or -1 if the
     * baseline can not be determined.  The baseline is measured from
     * the top of the component.
     *
     * @param component Component to calculate baseline for
     * @param width Width of the component to determine baseline for.
     * @param height Height of the component to determine baseline for.
     * @return baseline for the specified component
     */
    public int getComponentBaseline(Component component, int width,
            int height) {
        return -1;
    }
}
