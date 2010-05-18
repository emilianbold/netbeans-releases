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
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.border.*;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Convenience class that can be used to determine the baseline of a
 * particular component.  The static method <code>getBaseline</code> uses the
 * following algorithm to determine the baseline:
 * <ol>
 * <li>If the component has a <code>getBaseline(JComponent,int,int)</code> 
 *     method, invoke it.
 * <li>If there is a <code>UIManager</code> property of the name
 *     <code>Baseline.instance</code>, forward the call to that Baseline.
 * <li>Otherwise use the built in support.
 * </ol>
 * <p>
 * This class is primarily useful for JREs prior to 1.6.  In 1.6 API for this
 * was added directly to Component, JComponent and the
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
    private static JLabel TABLE_LABEL;

    // Prototype label for calculating baseline of lists.
    private static JLabel LIST_LABEL;

    // Prototype label for calculating baseline of trees.
    private static JLabel TREE_LABEL;

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
     * @param component JComponent to calculate baseline for
     * @return baseline for the specified component
     */
    public static int getBaseline(JComponent component) {
        Dimension pref = component.getPreferredSize();
        return getBaseline(component, pref.width, pref.height);
    }

    private static Method getBaselineMethod(JComponent component) {
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

    private static int invokeBaseline(Method method, JComponent c, int width,
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
     * @param component JComponent to calculate baseline for
     * @param width Width of the component to determine baseline for.
     * @param height Height of the component to determine baseline for.
     * @return baseline for the specified component
     */
    public static int getBaseline(JComponent component, int width, int height) {
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
        String lookAndFeelID = "GTK"; // UIManager.getLookAndFeel().getID();
        if (lookAndFeelID != "Windows" && lookAndFeelID != "Metal" &&
                lookAndFeelID != "GTK" && lookAndFeelID != "Aqua") {
            return -1;
        }
//        String uid = component.getUIClassID();
        String uid = null;
        if (component instanceof JButton) uid = "ButtonUI";
        if (component instanceof JLabel) uid = "LabelUI";
        if (component instanceof JTextField) uid = "TextFieldUI";
        int baseline = -1;
        if (uid == "ButtonUI" || uid == "CheckBoxUI" ||
                uid == "RadioButtonUI" || uid == "ToggleButtonUI") {
            baseline = getButtonBaseline((AbstractButton)component,
                                         height);
        }
        else if (uid == "ComboBoxUI") {
            return getComboBoxBaseline((JComboBox)component,
                                       height);
        }
        else if (uid == "TextAreaUI") {
            return getTextAreaBaseline((JTextArea)component, height);
        }
        else if (uid == "FormattedTextFieldUI" ||
                 uid == "PasswordFieldUI" ||
                 uid == "TextFieldUI") {
            baseline = getSingleLineTextBaseline((JTextComponent)component,
                                                 height);
        }
        else if (uid == "LabelUI") {
            baseline = getLabelBaseline((JLabel)component, height);
        }
        else if (uid == "ListUI") {
            baseline = getListBaseline((JList)component, height);
        }
        else if (uid == "PanelUI") {
            baseline = getPanelBaseline((JPanel)component, height);
        }
        else if (uid == "ProgressBarUI") {
            baseline = getProgressBarBaseline((JProgressBar)component, height);
        }
        else if (uid == "SliderUI") {
            baseline = getSliderBaseline((JSlider)component, height);
        }
        else if (uid == "SpinnerUI") {
            baseline = getSpinnerBaseline((JSpinner)component, height);
        }
        else if (uid == "ScrollPaneUI") {
            baseline = getScrollPaneBaseline((JScrollPane)component, height);
        }
        else if (uid == "TabbedPaneUI") {
            baseline = getTabbedPaneBaseline((JTabbedPane)component, height);
        }
        else if (uid == "TableUI") {
            baseline = getTableBaseline((JTable)component, height);
        }
        else if (uid == "TreeUI") {
            baseline = getTreeBaseline((JTree)component, height);
        }
        return Math.max(baseline, -1);
    }

    private static Insets rotateInsets(Insets topInsets, int targetPlacement) {
        switch(targetPlacement) {
          case JTabbedPane.LEFT:
              return new Insets(topInsets.left, topInsets.top, 
                                topInsets.right, topInsets.bottom);
          case JTabbedPane.BOTTOM:
              return new Insets(topInsets.bottom, topInsets.left,
                                topInsets.top, topInsets.right);
          case JTabbedPane.RIGHT:
              return new Insets(topInsets.left, topInsets.bottom,
                                topInsets.right, topInsets.top);
          default:
              return new Insets(topInsets.top, topInsets.left,
                                topInsets.bottom, topInsets.right);
        }
    }

    private static int getMaxTabHeight(JTabbedPane tp) {
        int fontHeight = tp.getFontMetrics(tp.getFont()).getHeight();
        int height = fontHeight;
        boolean tallerIcons = false;
        for (int counter = tp.getTabCount() - 1; counter >= 0; counter--) {
            Icon icon = tp.getIconAt(counter);
            if (icon != null) {
                int iconHeight = icon.getIconHeight();
                height = Math.max(height, iconHeight);
                if (iconHeight > fontHeight) {
                    tallerIcons = true;
                }
            }
        }
        Insets tabInsets = new Insets(6,6,6,6); //UIManager.getInsets("TabbedPane.tabInsets");
        height += 2;
        if (!isMetal() || !tallerIcons) {
            height += tabInsets.top + tabInsets.bottom;
        }
        return height;
    }

    private static int getTabbedPaneBaseline(JTabbedPane tp, int height) {
        if (tp.getTabCount() > 0) {
            if (isAqua()) {
                return getAquaTabbedPaneBaseline(tp, height);
            }
            Insets insets = tp.getInsets();
            Insets contentBorderInsets = new Insets(6,6,6,6); //UIManager.getInsets(
//                "TabbedPane.contentBorderInsets");
            Insets tabAreaInsets = rotateInsets(new Insets(6,6,6,6) /*UIManager.getInsets(
                                                 "TabbedPane.tabAreaInsets")*/,
                                                tp.getTabPlacement());
            FontMetrics metrics = tp.getFontMetrics(tp.getFont());
            int maxHeight = getMaxTabHeight(tp);
            iconRect.setBounds(0, 0, 0, 0);
            textRect.setBounds(0, 0, 0, 0);
            viewRect.setBounds(0, 0, Short.MAX_VALUE, maxHeight);
            SwingUtilities.layoutCompoundLabel(tp, metrics, "A", null,
                                               SwingUtilities.CENTER,
                                               SwingUtilities.CENTER,
                                               SwingUtilities.CENTER,
                                               SwingUtilities.TRAILING,
                                               viewRect,
                                               iconRect,
                                               textRect,
                                               0);
            int baseline = textRect.y + metrics.getAscent();
            switch(tp.getTabPlacement()) {
            case JTabbedPane.TOP:
                baseline += insets.top + tabAreaInsets.top;
                if (isWindows()) {
                    if (tp.getTabCount() > 1) {
                        baseline += 1;
                    }
                    else {
                        baseline -= 1;
                    }
                }
                return baseline;
            case JTabbedPane.BOTTOM:
                baseline = tp.getHeight() - insets.bottom -
                    tabAreaInsets.bottom - maxHeight + baseline;
                if (isWindows()) {
                    if (tp.getTabCount() > 1) {
                        baseline += -1;
                    }
                    else {
                        baseline += 1;
                    }
                }
                return baseline;
            case JTabbedPane.LEFT:
            case JTabbedPane.RIGHT:
                if (isAqua()) {
                    // Aqua rotates left/right text, so that there isn't a good
                    // baseline.
                    return -1;
                }
                baseline += insets.top + tabAreaInsets.top;
                if (isWindows()) {
                    baseline += (maxHeight % 2);
                }
                return baseline;
            }
        }
        return -1;
    }

    private static int getAquaTabbedPaneBaseline(JTabbedPane tp, int height) {
        Font font = tp.getFont();
        FontMetrics metrics = tp.getFontMetrics(font);
        int ascent = metrics.getAscent();
        int offset;
        switch(tp.getTabPlacement()) {
            case JTabbedPane.TOP:
                offset = 5;
                if (tp.getFont().getSize() > 12) {
                    offset = 6;
                }
                int yOffset = 20 - metrics.getHeight();
                yOffset /= 2;
                return offset + yOffset + ascent - 1;
            case JTabbedPane.BOTTOM:
                if (tp.getFont().getSize() > 12) {
                    offset = 6;
                } else {
                    offset = 4;
                }
                return height - (20 -
                        ((20 - metrics.getHeight()) / 2 + ascent)) - offset;
            case JTabbedPane.LEFT:
            case JTabbedPane.RIGHT:
                // Aqua rotates left/right text, so that there isn't a good
                // baseline.
                return -1;
        }
        return -1;
    }
    
    private static int getSliderBaseline(JSlider slider, int height) {
        // We don't handle GTK as too much is hidden to be able to calculate it
        if (isAqua()) {
            // This is temporary, need to resolve how this is calculated
            // later
            return -1;
        }
        if (slider.getPaintLabels() && !isGTK()) {
            FontMetrics metrics = slider.getFontMetrics(slider.getFont());
            Insets insets = slider.getInsets();
            Insets focusInsets = new Insets(6,6,6,6); //(Insets)UIManager.get("Slider.focusInsets");
	    if (slider.getOrientation() == JSlider.HORIZONTAL) {
                int tickLength = 8;
                int contentHeight = height - insets.top - insets.bottom -
                    focusInsets.top - focusInsets.bottom;
                int thumbHeight = 20;
                if (isMetal()) {
                    tickLength = 10; /*((Integer)UIManager.get(
                                      "Slider.majorTickLength")).intValue() + 5;*/
                    thumbHeight = 8; /*UIManager.getIcon(
                        "Slider.horizontalThumbIcon" ).getIconHeight();*/
                }
                else if (isWindows() && isXP()) {
                    // NOTE: this is not correct, this should come from
                    // the skin (in >= 1.5), but short of reflection
                    // hacks we don't have access to the real value.
                    thumbHeight++;
                }
                int centerSpacing = thumbHeight;
                if (slider.getPaintTicks()) {
                    // centerSpacing += getTickLength();
                    centerSpacing += tickLength;
                }
                // Assume uniform labels.
                centerSpacing += metrics.getAscent() + metrics.getDescent();
                int trackY = insets.top + focusInsets.top +
                    (contentHeight - centerSpacing - 1) / 2;
                int trackHeight = thumbHeight;
                int tickY = trackY + trackHeight;
                int tickHeight = tickLength;
                if (!slider.getPaintTicks()) {
                    tickHeight = 0;
                }
                int labelY = tickY + tickHeight;
                if (!slider.getPaintTicks() && isAqua()) {
                    labelY += 4;
                }
                return labelY + metrics.getAscent();
            }
            else { // vertical
                boolean inverted = slider.getInverted();
                Integer value = inverted ? getMinSliderValue(slider) :
                                           getMaxSliderValue(slider);
                if (value != null) {
                    int thumbHeight = 11;
                    if (isMetal()) {
                        thumbHeight = 10; /*UIManager.getIcon(
                            "Slider.verticalThumbIcon").getIconHeight();*/
                    }
                    int trackBuffer = Math.max(metrics.getHeight() / 2,
                                               thumbHeight / 2);
                    int contentY = focusInsets.top + insets.top;
                    int trackY = contentY + trackBuffer;
                    int trackHeight = height - focusInsets.top -
                        focusInsets.bottom - insets.top - insets.bottom -
                        trackBuffer - trackBuffer;
                    int maxValue = getMaxSliderValue(slider).intValue();
                    int min = slider.getMinimum();
                    int max = slider.getMaximum();
                    double valueRange = (double)max - (double)min;
                    double pixelsPerValue = (double)trackHeight /
                        (double)valueRange;
                    int trackBottom = trackY + (trackHeight - 1);
                    int yPosition;

                    if (!inverted) {
                        yPosition = trackY;
                        yPosition += Math.round(pixelsPerValue *
                                            ((double)max - value.intValue()));
                    }
                    else {
                        yPosition = trackY;
                        yPosition += Math.round(pixelsPerValue *
                                           ((double)value.intValue() - min) );
                    }
                    yPosition = Math.max(trackY, yPosition);
                    yPosition = Math.min(trackBottom, yPosition);
                    return yPosition - metrics.getHeight() / 2 +
                        metrics.getAscent();
                }
            }
        }
        return -1;
    }

    private static Integer getMaxSliderValue(JSlider slider) {
        Dictionary dictionary = slider.getLabelTable();
        if (dictionary != null) {
            Enumeration keys = dictionary.keys();
            int max = slider.getMinimum() - 1;
            while (keys.hasMoreElements()) {
                max = Math.max(max, ((Integer)keys.nextElement()).intValue());
            }
            if (max == slider.getMinimum() - 1) {
                return null;
            }
            return new Integer(max);
        }
        return null;
    }

    private static Integer getMinSliderValue(JSlider slider) {
        Dictionary dictionary = slider.getLabelTable();
        if (dictionary != null) {
            Enumeration keys = dictionary.keys();
            int min = slider.getMaximum() + 1;
            while (keys.hasMoreElements()) {
                min = Math.min(min, ((Integer)keys.nextElement()).intValue());
            }
            if (min == slider.getMaximum() + 1) {
                return null;
            }
            return new Integer(min);
        }
        return null;
    }

    private static int getProgressBarBaseline(JProgressBar pb, int height) {
        if (pb.isStringPainted() &&
                pb.getOrientation() == JProgressBar.HORIZONTAL) {
            FontMetrics metrics = pb.getFontMetrics(pb.getFont());
            Insets insets = pb.getInsets();
            int y = insets.top;
            if (isWindows() && isXP()) {
                if (pb.isIndeterminate()) {
                    y = -1;
                    height--;
                }
                else {
                    y = 0;
                    height -= 3;
                }
            }
            else if (isGTK()) {
                return (height - metrics.getAscent() - 
                        metrics.getDescent()) / 2 + metrics.getAscent();
            }
            else if (isAqua()) {
                if (pb.isIndeterminate()) {
                    // Aqua doesn't appear to support text on indeterminate
                    // progress bars.
                    return -1;
                }
                y -= 1;
                height -= (insets.top + insets.bottom);
            }
            else {
                height -= insets.top + insets.bottom;
            }
            return y + (height + metrics.getAscent() -
                        metrics.getLeading() -
                        metrics.getDescent()) / 2;
        }
        return -1;
    }

    private static int getTreeBaseline(JTree tree, int height) {
        int rowHeight = tree.getRowHeight();
        if (TREE_LABEL == null) {
            TREE_LABEL = new JLabel("X");
            TREE_LABEL.setIcon(null /*UIManager.getIcon("Tree.closedIcon")*/);
        }
        JLabel label = TREE_LABEL;
        label.setFont(tree.getFont());
        if (rowHeight <= 0) {
            rowHeight = label.getPreferredSize().height;
        }
        return getLabelBaseline(label, rowHeight) + tree.getInsets().top;
    }

    private static int getTableBaseline(JTable table, int height) {
        if (TABLE_LABEL == null) {
            TABLE_LABEL = new JLabel("");
            TABLE_LABEL.setBorder(new EmptyBorder(1, 1, 1, 1));
        }
        JLabel label = TABLE_LABEL;
        label.setFont(table.getFont());
        int rowMargin = table.getRowMargin();
        int baseline = getLabelBaseline(label, table.getRowHeight() -
                                        rowMargin);
        return baseline += rowMargin / 2;
    }

    private static int getTextAreaBaseline(JTextArea text, int height) {
        Insets insets = text.getInsets();
        FontMetrics fm = text.getFontMetrics(text.getFont());
        return insets.top + fm.getAscent();
    }
    
    private static int getListBaseline(JList list, int height) {
        int rowHeight = list.getFixedCellHeight();
        if (LIST_LABEL == null) {
            LIST_LABEL = new JLabel("X");
            LIST_LABEL.setBorder(new EmptyBorder(1, 1, 1, 1));
        }
        JLabel label = LIST_LABEL;
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
        return getLabelBaseline(label, rowHeight) + list.getInsets().top;
    }

    private static int getScrollPaneBaseline(JScrollPane sp, int height) {
        Component view = sp.getViewport().getView();
        if (view instanceof JComponent) {
            int baseline = getBaseline((JComponent)view);
            if (baseline > 0) {
                return baseline + sp.getViewport().getY();
            }
        }
        return -1;
    }

    private static int getPanelBaseline(JPanel panel, int height) {
        Border border = panel.getBorder();
        if (border instanceof TitledBorder) {
            TitledBorder titledBorder = (TitledBorder)border;
            if (titledBorder.getTitle() != null &&
                      !"".equals(titledBorder.getTitle())) {
                Font font = titledBorder.getTitleFont();
                if (font == null) {
                    font = panel.getFont();
                    if (font == null) {
                        font = new Font("Dialog", Font.PLAIN, 12);
                    }
                }
                Border border2 = titledBorder.getBorder();
                Insets borderInsets;
                if (border2 != null) {
                    borderInsets = border2.getBorderInsets(panel);
                }
                else {
                    borderInsets = EMPTY_INSETS;
                }
                FontMetrics fm = panel.getFontMetrics(font);
                int fontHeight = fm.getHeight();
                int descent = fm.getDescent();
                int ascent = fm.getAscent();
                int y = EDGE_SPACING;
                int h = height - EDGE_SPACING * 2;
                int diff;
                switch (((TitledBorder)border).getTitlePosition()) {
                case TitledBorder.ABOVE_TOP:
                    diff = ascent + descent + (Math.max(EDGE_SPACING,
                                    TEXT_SPACING*2) - EDGE_SPACING);
                    return y + diff - (descent + TEXT_SPACING);
                case TitledBorder.TOP:
                case TitledBorder.DEFAULT_POSITION:
                    diff = Math.max(0, ((ascent/2) + TEXT_SPACING) -
                                    EDGE_SPACING);
                    return (y + diff - descent) +
                           (borderInsets.top + ascent + descent)/2;
                case TitledBorder.BELOW_TOP:
                    return y + borderInsets.top + ascent + TEXT_SPACING;
                case TitledBorder.ABOVE_BOTTOM:
                    return (y + h) -
                        (borderInsets.bottom + descent + TEXT_SPACING);
                case TitledBorder.BOTTOM:
                    h -= fontHeight / 2;
                    return ((y + h) - descent) +
                           ((ascent + descent) - borderInsets.bottom)/2;
                case TitledBorder.BELOW_BOTTOM:
                    h -= fontHeight;
                    return y + h + ascent + TEXT_SPACING;
                }
            }
        }
        return -1;
    }

    private static int getSpinnerBaseline(JSpinner spinner, int height) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor defaultEditor = (JSpinner.DefaultEditor)
                                          editor;
            JTextField tf = defaultEditor.getTextField();
            Insets spinnerInsets = spinner.getInsets();
            Insets editorInsets = defaultEditor.getInsets();
            int offset = spinnerInsets.top + editorInsets.top;
            height -= (offset + spinnerInsets.bottom + editorInsets.bottom);
            if (height <= 0) {
                return -1;
            }
            return offset + getSingleLineTextBaseline(tf, height);
        }
        Insets insets = spinner.getInsets();
        FontMetrics fm = spinner.getFontMetrics(spinner.getFont());
        return insets.top + fm.getAscent();
    }

    private static int getLabelBaseline(JLabel label, int height) {
        Icon icon = (label.isEnabled()) ? label.getIcon() :
                           label.getDisabledIcon();
        FontMetrics fm = label.getFontMetrics(label.getFont());

        resetRects(label, height);

        SwingUtilities.layoutCompoundLabel(label, fm,
            "a", icon, label.getVerticalAlignment(),
            label.getHorizontalAlignment(), label.getVerticalTextPosition(),
            label.getHorizontalTextPosition(), viewRect, iconRect, textRect,
            label.getIconTextGap());

        return textRect.y + fm.getAscent();
    }

    private static int getComboBoxBaseline(JComboBox combobox, int height) {
        Insets insets = combobox.getInsets();
        int y = insets.top;
        height -= (insets.top + insets.bottom);
        if (combobox.isEditable()) {
            ComboBoxEditor editor = combobox.getEditor();
            if (editor != null && (editor.getEditorComponent() instanceof
                                   JTextField)) {
                JTextField tf = (JTextField)editor.getEditorComponent();
                return y + getSingleLineTextBaseline(tf, height);
            }
        }
        // Use the renderer to calculate baseline
        if (isMetal()) {
            if (isOceanTheme()) {
                y += 2;
                height -= 4;
            }
        }
        else if (isWindows()) {
            // This doesn't guarantee an XP style will be active,
            // but we don't offer public API to detect if XP is active.
            String osVersion = System.getProperty("os.version");
            if (osVersion != null) {
                Float version = Float.valueOf(osVersion);
                if (version.floatValue() > 4.0) {
                    y += 2;
                    height -= 4;
                }
            }
        }
        ListCellRenderer renderer = combobox.getRenderer();
        if (renderer instanceof JLabel) {
            int baseline = y + getLabelBaseline((JLabel)renderer, height);
            if (isAqua()) {
                return baseline - 1;
            }
            return baseline;
        }
        // Renderer isn't a label, use metrics directly.
        FontMetrics fm = combobox.getFontMetrics(combobox.getFont());
        return y + fm.getAscent();
    }

    /**
     * Returns the baseline for single line text components, like
     * <code>JTextField</code>.
     */
    private static int getSingleLineTextBaseline(JTextComponent textComponent,
                                                 int h) {
        View rootView = textComponent.getUI().getRootView(textComponent);
        if (rootView.getViewCount() > 0) {
            Insets insets = textComponent.getInsets();
            int height = h - insets.top - insets.bottom;
            int y = insets.top;
            View fieldView = rootView.getView(0);
	    int vspan = (int)fieldView.getPreferredSpan(View.Y_AXIS);
	    if (height != vspan) {
		int slop = height - vspan;
		y += slop / 2;
	    }
            FontMetrics fm = textComponent.getFontMetrics(
                                 textComponent.getFont());
            y += fm.getAscent();
            return y;
        }
        return -1;
    }

    /**
     * Returns the baseline for buttons.
     */
    private static int getButtonBaseline(AbstractButton button, int height) {
        FontMetrics fm = button.getFontMetrics(button.getFont());

        resetRects(button, height);

        String text = button.getText();
        if (text != null && text.startsWith("<html>")) {
            return -1;
        }
        // NOTE: that we use "a" here to make sure we get a valid value, if
        // we were to pass in an empty string or null we would not get
        // back the right thing.
        SwingUtilities.layoutCompoundLabel(
            button, fm, "a", button.getIcon(), 
            button.getVerticalAlignment(), button.getHorizontalAlignment(),
            button.getVerticalTextPosition(),
            button.getHorizontalTextPosition(),
            viewRect, iconRect, textRect, 
            text == null ? 0 : button.getIconTextGap());

        if (isAqua()) {
            return textRect.y + fm.getAscent() + 1;
        }
        return textRect.y + fm.getAscent();
    }

    private static void resetRects(JComponent c, int height) {
        Insets insets = c.getInsets();
        viewRect.x = insets.left;
        viewRect.y = insets.top;
        viewRect.width = c.getWidth() - (insets.right + viewRect.x);
        viewRect.height = height - (insets.bottom + viewRect.y);
        textRect.x = textRect.y = textRect.width = textRect.height = 0;
        iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
    }
    
    private static boolean isOceanTheme() {
        try {
            java.lang.reflect.Field field = MetalLookAndFeel.class.getDeclaredField("currentTheme");
            field.setAccessible(true);
            Object theme = field.get(null);
            return "javax.swing.plaf.metal.OceanTheme".equals(theme.getClass().getName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private static boolean isWindows() {
        return true; // UIManager.getLookAndFeel().getID() == "Windows";
    }

    private static boolean isMetal() {
        return false; // UIManager.getLookAndFeel().getID() == "Metal";
    }

    private static boolean isGTK() {
        return false; // UIManager.getLookAndFeel().getID() == "GTK";
    }

    private static boolean isAqua() {
        return false; //UIManager.getLookAndFeel().getID() == "Aqua";
    }

    private static boolean isXP() {
        if (!checkedForClassic) {
            try {
                CLASSIC_WINDOWS = Class.forName(
                  "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
            } catch (ClassNotFoundException e) {
            }
            checkedForClassic = true;
        }
/*        if (CLASSIC_WINDOWS != null && CLASSIC_WINDOWS.
                    isInstance(UIManager.getLookAndFeel())) {
            return false;
        }
 */
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Boolean themeActive = (Boolean)toolkit.getDesktopProperty(
                                       "win.xpstyle.themeActive");
        if (themeActive == null) {
            themeActive = Boolean.FALSE;
        }
        return themeActive.booleanValue();
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
     * @param component JComponent to calculate baseline for
     * @param width Width of the component to determine baseline for.
     * @param height Height of the component to determine baseline for.
     * @return baseline for the specified component
     */
    public int getComponentBaseline(JComponent component, int width,
            int height) {
        return -1;
    }
}
