/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutdesign.support;

import java.io.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import org.netbeans.modules.form.layoutdesign.*;

/**
 * Generates Java layout code based on the passed layout model. 
 *
 * @author Jan Stola
 */
public class SwingLayoutCodeGenerator {
    private static final String LAYOUT_VAR_NAME = "layout"; // NOI18N
    /** Layout model of the form. */
    private LayoutModel layoutModel;
    private String layoutVarName;
    private boolean useLayoutLibrary;

    /**
     * Maps from component ID to <code>ComponentInfo</code>.
     */
    private Map/*<String,ComponentInfo>*/ componentIDMap;

    /**
     * Creates new <code>SwingLayoutCodeGenerator</code>.
     *
     * @param layoutModel layout model of the form.
     */
    public SwingLayoutCodeGenerator(LayoutModel layoutModel) {
        componentIDMap = new HashMap/*<String,ComponentInfo>*/();
        this.layoutModel = layoutModel;
    }

    /**
     * Generates Java layout code for the specified container. The generated
     * code is written to the <code>writer</code>.
     *
     * @param writer the writer to generate the code into.
     * @param container container whose code should be generated.
     * @param contVarName code expression for the container.
     * @param compIds IDs of subcomponents.
     * @param compVarNames code expressions of subcomponents.
     */
    public void generateContainerLayout(Writer writer, LayoutComponent container,
        String contExprStr, String contVarName, ComponentInfo infos[],
        boolean useLibrary) throws IOException {
        useLayoutLibrary = useLibrary;
        if (contVarName == null) {
            layoutVarName = LAYOUT_VAR_NAME;
        } else {
            layoutVarName = contVarName + Character.toUpperCase(LAYOUT_VAR_NAME.charAt(0))
                + LAYOUT_VAR_NAME.substring(1);
        }
        fillMap(infos);
        generateInstantiation(writer, contExprStr);
        StringBuffer sb = new StringBuffer();
        LayoutInterval horizontalInterval = container.getLayoutRoot(LayoutConstants.HORIZONTAL);
        composeGroup(sb, horizontalInterval, true, true);
        String horizontalGroup = sb.toString();
        writer.write(layoutVarName + ".setHorizontalGroup(\n" + horizontalGroup + "\n);\n"); // NOI18N
                
        sb = new StringBuffer();
        composeLinks(sb, container, layoutVarName, LayoutConstants.HORIZONTAL);
        String horizontalLinks = sb.toString();
        writer.write(horizontalLinks);

        sb = new StringBuffer();
        LayoutInterval verticalInterval = container.getLayoutRoot(LayoutConstants.VERTICAL);
        composeGroup(sb, verticalInterval, true, true);
        String verticalGroup = sb.toString();
        writer.write(layoutVarName + ".setVerticalGroup(\n" + verticalGroup + "\n);\n"); // NOI18N


        sb = new StringBuffer();
        composeLinks(sb, container, layoutVarName, LayoutConstants.VERTICAL);
        String verticalLinks = sb.toString();
        writer.write(verticalLinks);
    }                                       

    /**
     * Fills the <code>componentIDMap</code>.
     *
     * @param infos information about components.
     */
    private void fillMap(ComponentInfo[] infos) {
        for (int counter = 0; counter < infos.length; counter++) {
            componentIDMap.put(infos[counter].id, infos[counter]);
        }
    }
    
    /**
     * Generates the "header" of the code e.g. instantiation of the layout
     * and call to the <code>setLayout</code> method.
     */
    private void generateInstantiation(Writer writer, String contExprStr) throws IOException {
        writer.write(getLayoutName() + " " + layoutVarName + " "); // NOI18N
        writer.write("= new " + getLayoutName() + "(" + contExprStr + ");\n"); // NOI18N
        writer.write(contExprStr + ".setLayout(" + layoutVarName + ");\n"); // NOI18N
    }
    
    /**
     * Generates layout code for a group that corresponds
     * to the <code>interval</code>.
     *
     * @param layout buffer to generate the code into.
     * @param interval layout model of the group.
     */
    private void composeGroup(StringBuffer layout, LayoutInterval interval,
        boolean first, boolean last) throws IOException {
        if (interval.isGroup()) {
            int groupAlignment = interval.getGroupAlignment();
            if (interval.isParallel()) {
                boolean notResizable = interval.getMaximumSize(false) == LayoutConstants.USE_PREFERRED_SIZE;
                String alignmentStr = convertAlignment(groupAlignment);
                layout.append(layoutVarName).append(".createParallelGroup("); // NOI18N
                layout.append(alignmentStr);
                if (notResizable) {
                    layout.append(", false"); // NOI18N
                }
                layout.append(")"); // NOI18N
            } else if (interval.isSequential()) {
                layout.append(layoutVarName).append(".createSequentialGroup()"); // NOI18N
            } else {
                assert false;
            }
            if (interval.getSubIntervalCount() > 0) {
                layout.append("\n"); // NOI18N
            }
            Iterator subIntervals = interval.getSubIntervals();
            while (subIntervals.hasNext()) {
                LayoutInterval subInterval = (LayoutInterval)subIntervals.next();
                fillGroup(layout, subInterval, first,
                          last && (!interval.isSequential() || !subIntervals.hasNext()),
                          groupAlignment);
                if (first && interval.isSequential()) {
                    first = false;
                }
                if (subIntervals.hasNext()) {
                    layout.append("\n"); // NOI18N
                }
            }
        } else {
            layout.append(layoutVarName).append(".createSequentialGroup()\n"); // NOI18N
            fillGroup(layout, interval, true, true, LayoutConstants.DEFAULT);
        }
    }
    
    /**
     * Generate layout code for one element in the group.
     *
     * @param layout buffer to generate the code into.
     * @param interval layout model of the element.
     * @param groupAlignment alignment of the enclosing group.
     */
    private void fillGroup(StringBuffer layout, LayoutInterval interval,
        boolean first, boolean last, int groupAlignment) throws IOException {
        if (interval.isGroup()) {
            layout.append(getAddGroupStr());
            int alignment = interval.getAlignment();
            if ((alignment != LayoutConstants.DEFAULT) && interval.getParent().isParallel() && alignment != groupAlignment
                    && alignment != LayoutConstants.BASELINE && groupAlignment != LayoutConstants.BASELINE) {
                String alignmentStr = convertAlignment(alignment);
                layout.append(alignmentStr).append(", "); // NOI18N
            }
            composeGroup(layout, interval, first, last);
        } else {
            int min = interval.getMinimumSize(false);
            int pref = interval.getPreferredSize(false);
            int max = interval.getMaximumSize(false);
            if (interval.isComponent()) {
                layout.append(getAddComponentStr());
                int alignment = interval.getAlignment();
                LayoutComponent layoutComp = interval.getComponent();
                ComponentInfo info = (ComponentInfo)componentIDMap.get(layoutComp.getId());
                if (min == LayoutConstants.NOT_EXPLICITLY_DEFINED) {
                    int dimension = (layoutComp.getLayoutInterval(LayoutConstants.HORIZONTAL) == interval) ? LayoutConstants.HORIZONTAL : LayoutConstants.VERTICAL;
                    if ((dimension == LayoutConstants.HORIZONTAL) && info.clazz.getName().equals("javax.swing.JComboBox")) { // Issue 68612 // NOI18N
                        min = 0;
                    } else if (pref >= 0) {
                        int compMin = (dimension == LayoutConstants.HORIZONTAL) ? info.minSize.width : info.minSize.height;
                        if (compMin > pref) {
                            min = LayoutConstants.USE_PREFERRED_SIZE;
                        }
                    }
                }
                assert (info.variableName != null);
                if (interval.getParent().isSequential() || (alignment == LayoutConstants.DEFAULT) || (alignment == groupAlignment)
                        || alignment == LayoutConstants.BASELINE || groupAlignment == LayoutConstants.BASELINE) {
                    layout.append(info.variableName);
                } else {
                    String alignmentStr = convertAlignment(alignment);
                    if (useLayoutLibrary())
                        layout.append(alignmentStr).append(", ").append(info.variableName); // NOI18N
                    else // in JDK the component comes first
                        layout.append(info.variableName).append(", ").append(alignmentStr); // NOI18N
                }
                int status = SwingLayoutUtils.getResizableStatus(info.clazz);
                
                if (!((pref == LayoutConstants.NOT_EXPLICITLY_DEFINED) &&
                    ((min == LayoutConstants.NOT_EXPLICITLY_DEFINED)
                     || ((min == LayoutConstants.USE_PREFERRED_SIZE)
                        && !info.sizingChanged
                        && (status == SwingLayoutUtils.STATUS_NON_RESIZABLE))) &&
                    ((max == LayoutConstants.NOT_EXPLICITLY_DEFINED)
                     || ((max == LayoutConstants.USE_PREFERRED_SIZE)
                        && !info.sizingChanged
                        && (status == SwingLayoutUtils.STATUS_NON_RESIZABLE))
                     || ((max == Short.MAX_VALUE)
                        && !info.sizingChanged
                        && (status == SwingLayoutUtils.STATUS_RESIZABLE))))) {                
                    layout.append(", "); // NOI18N
                    generateSizeParams(layout, min, pref, max);
                }
            } else if (interval.isEmptySpace()) {
                if (interval.isDefaultPadding(false)) {
                    if (first || last) {
                        layout.append(getAddContainerGapStr());
                    } else {
                        layout.append(getAddPreferredGapStr());
                        layout.append(getLayoutStyleName());
                        if (!useLayoutLibrary())
                            layout.append(".ComponentPlacement"); // NOI18N
                        layout.append(".RELATED"); // NOI18N
                    }
                    if ((pref != LayoutConstants.NOT_EXPLICITLY_DEFINED)
                        || ((max != LayoutConstants.NOT_EXPLICITLY_DEFINED)
                            // NOT_EXPLICITLY_DEFINED is the same as USE_PREFERRED_SIZE in this case
                            && (max != LayoutConstants.USE_PREFERRED_SIZE))) {
                        if (!first && !last) {
                            layout.append(',').append(' ');
                        }
                        layout.append(convertSize(pref)).append(", "); // NOI18N
                        layout.append(convertSize(max));
                    }
                } else {
                    if (min == LayoutConstants.USE_PREFERRED_SIZE) {
                        min = pref;
                    }
                    if (max == LayoutConstants.USE_PREFERRED_SIZE) {
                        max = pref;
                    }
                    layout.append(getAddGapStr());
                    if (min < 0) min = pref; // min == GroupLayout.PREFERRED_SIZE
                    min = Math.min(pref, min);
                    max = Math.max(pref, max);
                    generateSizeParams(layout, min, pref, max);
                }
            } else {
                assert false;
            }
        }
        layout.append(")"); // NOI18N
    }
    
    /**
     * Generates minimum/preferred/maximum size parameters..
     *
     * @param layout buffer to generate the code into.
     * @param min minimum size.
     * @param pref preffered size.
     * @param max maximum size.
     */
    private void generateSizeParams(StringBuffer layout, int min, int pref, int max) {
        layout.append(convertSize(min)).append(", "); // NOI18N
        layout.append(convertSize(pref)).append(", "); // NOI18N
        layout.append(convertSize(max));
    }

    /**
     * Converts alignment from the layout model constants
     * to <code>GroupLayout</code> constants.
     *
     * @param alignment layout model alignment constant.
     * @return <code>GroupLayout</code> alignment constant that corresponds
     * to the given layout model one.
     */
    private String convertAlignment(int alignment) {
        String groupAlignment = null;
        switch (alignment) {
            case LayoutConstants.LEADING: groupAlignment = "LEADING"; break; // NOI18N
            case LayoutConstants.TRAILING: groupAlignment = "TRAILING"; break; // NOI18N
            case LayoutConstants.CENTER: groupAlignment = "CENTER"; break; // NOI18N
            case LayoutConstants.BASELINE: groupAlignment = "BASELINE"; break; // NOI18N
            default: assert false; break;
        }
        return useLayoutLibrary() ?
            getLayoutName() + "." + groupAlignment : // NOI18N
            getLayoutName() + ".Alignment." + groupAlignment; // NOI18N
    }
    
    /**
     * Converts minimum/preferred/maximums size from the layout model constants
     * to <code>GroupLayout</code> constants.
     *
     * @param size minimum/preferred/maximum size from layout model.
     * @return minimum/preferred/maximum size or <code>GroupLayout</code> constant
     * that corresponds to the given layout model one.
     */
    private String convertSize(int size) {
        String convertedSize;
        switch (size) {
            case LayoutConstants.NOT_EXPLICITLY_DEFINED: convertedSize = getLayoutName() + ".DEFAULT_SIZE"; break; // NOI18N
            case LayoutConstants.USE_PREFERRED_SIZE: convertedSize = getLayoutName() + ".PREFERRED_SIZE"; break; // NOI18N
            case Short.MAX_VALUE: convertedSize = "Short.MAX_VALUE"; break; // NOI18N
            default: assert (size >= 0); convertedSize = Integer.toString(size); break;
        }
        return convertedSize;
    }

    private void composeLinks(StringBuffer layout, LayoutComponent containerLC, String layoutVarName, int dimension) throws IOException {

        Map linkSizeGroups = SwingLayoutUtils.createLinkSizeGroups(containerLC, dimension);
        
        Collection linkGroups = linkSizeGroups.values();
        Iterator linkGroupsIt = linkGroups.iterator();
        while (linkGroupsIt.hasNext()) {
            List l = (List)linkGroupsIt.next();
            // sort so that the generated line is always the same when no changes were made
            Collections.sort(l, new Comparator() {
                public int compare(Object o1, Object o2) {
                    String id1 =(String)o1;
                    String id2 =(String)o2;
                    ComponentInfo info1 = (ComponentInfo)componentIDMap.get(id1);
                    ComponentInfo info2 = (ComponentInfo)componentIDMap.get(id2);                    
                    return info1.variableName.compareTo(info2.variableName);
                }
            });
            if (l.size() > 1) {
                layout.append("\n\n" + layoutVarName + ".linkSize("); // NOI18N
                if (!useLayoutLibrary()) {
                    layout.append("javax.swing.SwingConstants"); // NOI18N
                    layout.append(dimension == LayoutConstants.HORIZONTAL ?
                                  ".HORIZONTAL, " : ".VERTICAL, "); // NOI18N
                }
                layout.append("new java.awt.Component[] {"); //NOI18N
                Iterator i = l.iterator();
                boolean first = true;
                while (i.hasNext()) {
                    String cid = (String)i.next();
                    ComponentInfo info = (ComponentInfo)componentIDMap.get(cid);
                    if (first) {
                        first = false;
                        layout.append(info.variableName);
                    } else {
                        layout.append(", " + info.variableName); // NOI18N
                    }
                }
                layout.append( "}"); // NOI18N
                if (useLayoutLibrary()) {
                    layout.append( ", "); // NOI18N
                    layout.append(getLayoutName());
                    layout.append(dimension == LayoutConstants.HORIZONTAL ?
                                  ".HORIZONTAL" : ".VERTICAL"); // NOI18N
                }
                layout.append(");\n\n"); // NOI18N
            }
        }
    }

    /**
     * Information about one component.
     */
    public static class ComponentInfo {
        /** ID of the component. */
        public String id;
        /** Variable name of the component. */
        public String variableName;
        /** The component's class. */
        public Class clazz;
        /** The component's minimum size. */
        public Dimension minSize;
        /**
         * Determines whether size properties (e.g. minimumSize, preferredSize
         * or maximumSize properties of the component has been changed).
         */
        public boolean sizingChanged;
    }

    // -----
    // type of generated code: swing-layout library vs JDK

    boolean useLayoutLibrary() {
        return useLayoutLibrary;
    }

    private String getLayoutName() {
        return useLayoutLibrary() ? "org.jdesktop.layout.GroupLayout" : "javax.swing.GroupLayout"; // NOI18N
    }

    private String getLayoutStyleName() {
        return useLayoutLibrary() ? "org.jdesktop.layout.LayoutStyle" : "javax.swing.LayoutStyle"; // NOI18N
    }

    private String getAddComponentStr() {
        return useLayoutLibrary() ? ".add(" : ".addComponent("; // NOI18N
    }

    private String getAddGapStr() {
        return useLayoutLibrary() ? ".add(" : ".addGap("; // NOI18N
    }

    private String getAddPreferredGapStr() {
        return ".addPreferredGap("; // NOI18N
    }

    private String getAddContainerGapStr() {
        return ".addContainerGap("; // NOI18N
    }

    private String getAddGroupStr() {
        return useLayoutLibrary() ? ".add(" : ".addGroup("; // NOI18N
    }
}
