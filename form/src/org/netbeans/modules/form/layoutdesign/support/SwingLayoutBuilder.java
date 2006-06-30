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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutdesign.support;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import org.jdesktop.layout.LayoutStyle;
import org.jdesktop.layout.GroupLayout;

import org.netbeans.modules.form.layoutdesign.*;

/**
 * This class constructs real layout of AWT/Swing components based on the
 * layout model.
 *
 * @author Jan Stola, Tomas Pavek
 */
public class SwingLayoutBuilder {

    private LayoutModel layoutModel;

    /**
     * Container being layed out.
     */
    private Container container;

    /**
     * LayoutComponent for the container.
     */
    private LayoutComponent containerLC;

    /**
     * Maps from component ID to Component.
     */
    private Map/*<String,Component>*/ componentIDMap;

    private boolean designMode;

    public SwingLayoutBuilder(LayoutModel layoutModel,
                              Container container, String containerId,
                              boolean designMode)
    {
        componentIDMap = new HashMap/*<String,Component>*/();
        this.layoutModel = layoutModel;
        this.container = container;
        this.containerLC = layoutModel.getLayoutComponent(containerId);
        this.designMode = designMode;
    }

    /**
     * Sets up layout of a container and adds all components to it according
     * to the layout model. This method is used for initial construction of
     * the layout visual representation (layout view).
     */
    public void setupContainerLayout(Component[] components, String[] compIds) {
//        addComponentsToContainer(components, compIds);
        for (int counter = 0; counter < components.length; counter++) {
            componentIDMap.put(compIds[counter], components[counter]);
        }
        createLayout();
    }

    /**
     * Adds new components to a container (according to the layout model).
     * This method is used for incremental updates of the layout view.
     */
//    public void addComponentsToContainer(Component[] components, String[] compIds) {
//        if (components.length != compIds.length) {
//            throw new IllegalArgumentException("Sizes must match");
//        }
//        for (int counter = 0; counter < components.length; counter++) {
//            componentIDMap.put(compIds[counter], components[counter]);
//        }
//        layout();
//    }

    /**
     * Removes components from a container. This method is used for incremental
     * updates of the layout view.
     */
    public void removeComponentsFromContainer(Component[] components, String[] compIds) {
        if (components.length != compIds.length) {
            throw new IllegalArgumentException("Sizes must match"); // NOI18N
        }
        for (int counter = 0; counter < components.length; counter++) {
            componentIDMap.remove(compIds[counter]);
        }
        createLayout();
    }

    /**
     * Clears given container - removes all components. This method is used
     * for incremental updates of the layout view.
     */
    public void clearContainer() {
        container.removeAll();
        componentIDMap.clear();
    }

    public void createLayout() {
        Throwable th = null;
        boolean reset = true;
        container.removeAll();
        try {
            GroupLayout layout = new GroupLayout(container);
            container.setLayout(layout);
            LayoutInterval horizontalInterval = containerLC.getLayoutRoot(LayoutConstants.HORIZONTAL);
            GroupLayout.Group horizontalGroup = composeGroup(layout, horizontalInterval, true, true);
            layout.setHorizontalGroup(horizontalGroup);
            LayoutInterval verticalInterval = containerLC.getLayoutRoot(LayoutConstants.VERTICAL);
            GroupLayout.Group verticalGroup = composeGroup(layout, verticalInterval, true, true);
            layout.setVerticalGroup(verticalGroup);
            composeLinks(layout);
            // Try to create the layout (to be able to reset it in case of some problem)
            layout.layoutContainer(container);
            layout.invalidateLayout(container);
            reset = false;
        } finally {
            if (reset) {
                container.setLayout(null);
            }
        }
    }
    
    public void doLayout() {
        container.doLayout();
    }

    public static boolean isRelevantContainer(Container cont) {
        return cont.getLayout() != null
               && cont.getLayout().getClass().getName().equals("org.jdesktop.layout.GroupLayout"); // NOI18N
    }

    // -----

    private GroupLayout.Group composeGroup(GroupLayout layout, LayoutInterval interval,
                                            boolean first, boolean last) {
        GroupLayout.Group group = null;
        if (interval.isGroup()) {            
            if (interval.isParallel()) {
                int groupAlignment = convertAlignment(interval.getGroupAlignment());
                boolean notResizable = interval.getMaximumSize(designMode) == LayoutConstants.USE_PREFERRED_SIZE;
                group = layout.createParallelGroup(groupAlignment, !notResizable);
            } else if (interval.isSequential()) {
                group = layout.createSequentialGroup();
            } else {
                assert false;
            }
            Iterator subIntervals = interval.getSubIntervals();
            while (subIntervals.hasNext()) {
                LayoutInterval subInterval = (LayoutInterval)subIntervals.next();
                fillGroup(layout, group, subInterval,
                          first,
                          last && (!interval.isSequential() || !subIntervals.hasNext()));
                if (first && interval.isSequential()) {
                    first = false;
                }
            }
        } else {
            group = layout.createSequentialGroup();
            fillGroup(layout, group, interval, true, true);
        }
        return group;
    }
    
    private void fillGroup(GroupLayout layout, GroupLayout.Group group, LayoutInterval interval,
                           boolean first, boolean last) {
        int alignment = getIntervalAlignment(interval);
        if (interval.isGroup()) {
            if (group instanceof GroupLayout.SequentialGroup) {
                ((GroupLayout.SequentialGroup)group).add(composeGroup(layout, interval, first, last));
            } else {
                ((GroupLayout.ParallelGroup)group).add(
                        convertAlignment(alignment),
                        composeGroup(layout, interval, first, last));
            }
        } else {
            int minimum = interval.getMinimumSize(designMode);
            int preferred = interval.getPreferredSize(designMode);
            int min = convertSize(minimum, interval);
            int pref = convertSize(preferred, interval);
            int max = convertSize(interval.getMaximumSize(designMode), interval);
            if (interval.isComponent()) {
                LayoutComponent layoutComp = interval.getComponent();
                Component comp = (Component)componentIDMap.get(layoutComp.getId());
                assert (comp != null);
                if (minimum == LayoutConstants.NOT_EXPLICITLY_DEFINED) {
                    int dimension = (layoutComp.getLayoutInterval(LayoutConstants.HORIZONTAL) == interval) ? LayoutConstants.HORIZONTAL : LayoutConstants.VERTICAL;
                    if ((dimension == LayoutConstants.HORIZONTAL) && comp.getClass().getName().equals("javax.swing.JComboBox")) { // Issue 68612 // NOI18N
                        min = 0;
                    } else if (preferred >= 0) {
                        Dimension minDim = comp.getMinimumSize();
                        int compMin = (dimension == LayoutConstants.HORIZONTAL) ? minDim.width : minDim.height;
                        if (compMin > preferred) {
                            min = convertSize(LayoutConstants.USE_PREFERRED_SIZE, interval);
                        }
                    }
                }
                if (group instanceof GroupLayout.SequentialGroup) {
                    ((GroupLayout.SequentialGroup)group).add(comp, min, pref, max);
                } else {
                    GroupLayout.ParallelGroup pGroup = (GroupLayout.ParallelGroup)group;
                    pGroup.add(convertAlignment(alignment), comp, min, pref, max);
                }
            } else {
                assert interval.isEmptySpace();
                if (interval.isDefaultPadding(designMode)) {
                    assert (group instanceof GroupLayout.SequentialGroup);
                    GroupLayout.SequentialGroup seqGroup = (GroupLayout.SequentialGroup)group;
                    if (first || last) {
                        seqGroup.addContainerGap(pref, max);
                    } else {
                        seqGroup.addPreferredGap(LayoutStyle.RELATED, pref, max);
                    }
                } else {
                    if (min < 0) min = pref; // min == GroupLayout.PREFERRED_SIZE
                    min = Math.min(pref, min);
                    max = Math.max(pref, max);
                    if (group instanceof GroupLayout.SequentialGroup) {
                        ((GroupLayout.SequentialGroup)group).add(min, pref, max);
                    } else {
                        ((GroupLayout.ParallelGroup)group).add(min, pref, max);
                    }
                }
            }
        }
    }

    /**
     * Filters out invalid use of BASELINE alignment (see issue 78035).
     * This method is a last resort to avoid failure in building the view.
     * See also LayoutModel.checkAndFixGroup method.
     */
    private static int getIntervalAlignment(LayoutInterval interval) {
        int alignment = interval.getAlignment();
        LayoutInterval group = interval.getParent();
        if (group.isParallel()) {
            int groupAlignment = group.getGroupAlignment();
            if ((alignment == LayoutConstants.BASELINE && groupAlignment != LayoutConstants.BASELINE)
                || (alignment != LayoutConstants.BASELINE && groupAlignment == LayoutConstants.BASELINE))
            {   // illegal combination, follow the group alignment
                alignment = groupAlignment;
                System.err.println("WARNING: Illegal use of baseline alignment, ignoring interval's alignment."); // NOI18N
//                assert false;
            }
        }
        else if (alignment != LayoutConstants.DEFAULT) {
            System.err.println("WARNING: Ignoring non-default alignment of interval in sequential group."); // NOI18N
//            assert false;
        }

        return alignment;
    }

    private static int convertAlignment(int alignment) {
        int groupAlignment = 0;
        switch (alignment) {
            case LayoutConstants.DEFAULT: groupAlignment = GroupLayout.LEADING; break;
            case LayoutConstants.LEADING: groupAlignment = GroupLayout.LEADING; break;
            case LayoutConstants.TRAILING: groupAlignment = GroupLayout.TRAILING; break;
            case LayoutConstants.CENTER: groupAlignment = GroupLayout.CENTER; break;
            case LayoutConstants.BASELINE: groupAlignment = GroupLayout.BASELINE; break;
            default: assert false; break;
        }
        return groupAlignment;
    }
    
    private int convertSize(int size, LayoutInterval interval) {
        int convertedSize;
        switch (size) {
            case LayoutConstants.NOT_EXPLICITLY_DEFINED: convertedSize = GroupLayout.DEFAULT_SIZE; break;
            case LayoutConstants.USE_PREFERRED_SIZE:
                convertedSize = interval.isEmptySpace() ?
                                convertSize(interval.getPreferredSize(designMode), interval) :
                                GroupLayout.PREFERRED_SIZE;
                break;
            default: assert (size >= 0); convertedSize = size; break;
        }
        return convertedSize;
    }

    private void composeLinks(GroupLayout layout) {
        composeLinks(layout, LayoutConstants.HORIZONTAL);
        composeLinks(layout, LayoutConstants.VERTICAL);
    }
    
    private void composeLinks(GroupLayout layout, int dimension) {

        Map links = SwingLayoutUtils.createLinkSizeGroups(containerLC, dimension);
        
        Set linksSet = links.keySet();
        Iterator i = linksSet.iterator();
        while (i.hasNext()) {
            List group = (List)links.get(i.next());
            List components = new ArrayList();
            for (int j=0; j < group.size(); j++) {
                String compId = (String)group.get(j);
                LayoutComponent lc = layoutModel.getLayoutComponent(compId);
                if (lc != null) {
                    Component comp = (Component)componentIDMap.get(lc.getId());
                    if (comp == null) {
                        return;
                    } else {
                        components.add(comp);
                    }
                }
            }
            Component[] compArray = (Component[])components.toArray(new Component[components.size()]);
            if (compArray != null) {
                if (dimension == LayoutConstants.HORIZONTAL) {
                    layout.linkSize(compArray, GroupLayout.HORIZONTAL);
                }
                if (dimension == LayoutConstants.VERTICAL) {
                    layout.linkSize(compArray, GroupLayout.VERTICAL);
                }
            }
        }
    }

}
