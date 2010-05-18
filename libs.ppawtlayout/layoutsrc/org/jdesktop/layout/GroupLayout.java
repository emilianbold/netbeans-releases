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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Toolkit;
import java.util.*;

/**
 * GroupLayout is a LayoutManager that hierarchically groups components to
 * achieve common, and not so common, layouts.  Grouping is done by instances
 * of the Group class.  GroupLayout supports two types of groups:
 * <table>
 *   <tr><td valign=top>Sequential:<td>A sequential group positions its child
 *           elements sequentially, one after another.
 *   <tr><td valign=top>Parallel:<td>A parallel group positions its child 
 *           elements in the same space on top of each other.  Parallel groups 
 *           can also align the child elements along their baseline.
 * </table>
 * Each Group can contain any number of child groups, Components or gaps.
 * GroupLayout treats each axis independently.  That is, there is a group
 * representing the horizontal axis, and a separate group representing the
 * vertical axis.  The horizontal group is responsible for setting the x
 * and width of its contents, where as the vertical group is responsible for
 * setting the y and height of its contents.
 * <p>
 * The following code builds a simple layout consisting of two labels in
 * one column, followed by two textfields in the next column:
 * <pre>
 *   JComponent panel = ...;
 *   GroupLayout layout = new GroupLayout(panel);
 *   panel.setLayout(layout);
 *   layout.setAutocreateGaps(true);
 *   layout.setAutocreateContainerGaps(true);
 *   GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
 *   hGroup.add(layout.createParallelGroup().add(label1).add(label2)).
 *          add(layout.createParallelGroup().add(tf1).add(tf2));
 *   layout.setHorizontalGroup(hGroup);
 *   GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
 *   vGroup.add(layout.createParallelGroup(GroupLayout.BASELINE).add(label1).add(tf1)).
 *          add(layout.createParallelGroup(GroupLayout.BASELINE).add(label2).add(tf2));
 *   layout.setVerticalGroup(vGroup);
 * </pre>
 * <p>
 * This layout consists of the following:
 * <ul><li>The horizontal axis consists of a sequential group containing two
 *         parallel groups.  The first parallel group consists of the labels,
 *         with the second parallel group consisting of the text fields.
 *     <li>The vertical axis similarly consists of a sequential group
 *         containing two parallel groups.  The parallel groups align their
 *         contents along the baseline.  The first parallel group consists
 *         of the first label and text field, and the second group consists
 *         of the second label and text field.
 * </ul>
 * There are a couple of things to notice in this code:
 * <ul>
 *   <li>You need not explicitly add the components to the container, this
 *       is indirectly done by using one of the <code>add</code> methods.
 *   <li>The various <code>add</code> methods of <code>Groups</code> return
 *       themselves.  This allows for easy chaining of invocations.  For
 *       example, <code>group.add(label1).add(label2);</code> is equivalent to
 *       <code>group.add(label1);group.add(label2);</code>.
 *   <li>There are no public constructors for the Groups, instead
 *       use the create methods of <code>GroupLayout</code>.
 * </ul>
 * GroupLayout offer the ability to automatically insert the appropriate gap
 * between components.  This can be turned on using the
 * <code>setAutocreateGaps()</code> method.  Similarly you can use
 * the <code>setAutocreateContainerGaps()</code> method to insert gaps
 * between the components and the container.
 * 
 * @version $Revision$
 * @author Tomas Pavek
 * @author Jan Stola
 * @author Scott Violet
 */
public class GroupLayout implements LayoutManager2 {
    // Used in size calculations
    private static final int MIN_SIZE = 0;
    private static final int PREF_SIZE = 1;
    private static final int MAX_SIZE = 2;
    
    private static final int UNSET = Integer.MIN_VALUE;

    /**
     * Possible argument when linking sizes of components.  Specifies the
     * the two component should share the same size along the horizontal
     * axis.
     *
     * @see #linkSize(java.awt.Component[],int)
     */
    public static final int HORIZONTAL = 1;

    /**
     * Possible argument when linking sizes of components.  Specifies the
     * the two component should share the same size along the vertical
     * axis.
     *
     * @see #linkSize(java.awt.Component[],int)
     */
    public static final int VERTICAL = 2;
    
    private static final int NO_ALIGNMENT = 0;
    /**
     * Possible alignment type.  Indicates the elements should be
     * aligned to the origin.  For the horizontal axis with a left to
     * right orientation this means aligned to the left.
     *
     * @see #createParallelGroup(int)
     */
    public static final int LEADING = 1;
    /**
     * Possible alignment type.  Indicates the elements should be
     * aligned to the end.  For the horizontal axis with a left to
     * right orientation this means aligned to the right.
     *
     * @see #createParallelGroup(int)
     */
    public static final int TRAILING = 2;
    /**
     * Possible alignment type.  Indicates the elements should centered in
     * the spaced provided.
     *
     * @see #createParallelGroup(int)
     */
    public static final int CENTER = 3;
    /**
     * Possible alignment type.  Indicates the elements should aligned along
     * their baseline.
     *
     * @see #createParallelGroup(int)
     */
    public static final int BASELINE = 3;
    
    /**
     * Possible value for the add methods that takes a Component.
     * Indicates the size from the component should be used.
     */
    public static final int DEFAULT_SIZE = -1;
    /**
     * Possible value for the add methods that takes a Component.
     * Indicates the preferred size should be used.
     */
    public static final int PREFERRED_SIZE = -2;
    
    // Whether or not we automatically try and create the preferred
    // padding between components.
    private boolean autocreatePadding;
    
    // Whether or not we automatically try and create the preferred
    // padding between containers
    private boolean autocreateContainerPadding;
    
    /**
     * Group responsible for layout along the horizontal axis.  This is NOT
     * the user specified group, use getHorizontalGroup to dig that out.
     */
    private Group horizontalGroup;
    /**
     * Group responsible for layout along the vertical axis.  This is NOT
     * the user specified group, use getVerticalGroup to dig that out.
     */
    private Group verticalGroup;
    
    // Maps from Component to ComponentInfo.  This is used for tracking
    // information specific to a Component.
    private Map componentInfos;
    
    // Container we're doing layout for.
    private Container host;
    
    // Used by areParallelSiblings, cached to avoid excessive garbage.
    private List parallelList;
    
    // Indicates Springs have been added since last change.
    private boolean springsAdded;
    
    // Whether or not any preferred padding (or container padding) springs exist
    private boolean hasPreferredPaddingSprings;
    
    
    private static void checkSize(int min, int pref, int max,
            boolean isComponentSpring) {
        checkResizeType(min, isComponentSpring);
        if (!isComponentSpring && pref < 0) {
            throw new IllegalArgumentException("Pref must be >= 0");
        }
        checkResizeType(max, isComponentSpring);
        checkLessThan(min, pref);
        checkLessThan(min, max);
        checkLessThan(pref, max);
    }

    private static void checkResizeType(int type, boolean isComponentSpring) {
        if (type < 0 && ((isComponentSpring && type != DEFAULT_SIZE &&
                                               type != PREFERRED_SIZE) ||
                          (!isComponentSpring && type != PREFERRED_SIZE))) {
            throw new IllegalArgumentException("Invalid size");
        }
    }
    
    private static void checkLessThan(int min, int max) {
        if (min >= 0 && max >= 0 && min > max) {
            throw new IllegalArgumentException(
                             "Following is not met: min<=pref<=max");
        }
    }
    
    // Makes sure the alignment is one of the known values.
    private static void checkAlignment(int alignment, boolean allowsBaseline) {
        if (alignment == LEADING || alignment == TRAILING ||
                alignment == CENTER) {
            return;
        }
        if (allowsBaseline && alignment != BASELINE) {
            throw new IllegalArgumentException("Alignment must be one of:"+
                    "LEADING, TRAILING, CENTER or BASELINE");
        }
        throw new IllegalArgumentException("Alignment must be one of:"+
                "LEADING, TRAILING or CENTER");
    }

    /**
     * Creates a GroupLayout for the specified JComponent.
     *
     * @param host the Container to layout
     * @throws IllegalArgumentException if host is null
     */
    public GroupLayout(Container host) {
        if (host == null) {
            throw new IllegalArgumentException("Container must be non-null");
        }
        this.host = host;
        setHorizontalGroup(createParallelGroup(LEADING, true));
        setVerticalGroup(createParallelGroup(LEADING, true));
        componentInfos = new HashMap();
        autocreatePadding = false;
        parallelList = new ArrayList();
    }

    /**
     * Returns a textual description of this GroupLayout.  The return value
     * is intended for debugging purposes only.
     *
     * @return textual description of this GroupLayout
     **/
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("HORIZONTAL\n");
        dump(buffer, horizontalGroup, "  ", HORIZONTAL);
        buffer.append("\nVERTICAL\n");
        dump(buffer, verticalGroup, "  ", VERTICAL);
        return buffer.toString();
    }
    
    private void dump(StringBuffer buffer, Spring spring, String indent,
            int axis) {
        String origin = "";
        String padding = "";
        if (spring instanceof ComponentSpring) {
            ComponentSpring cSpring = (ComponentSpring)spring;
            origin = Integer.toString(cSpring.getOrigin()) + " ";
            String name = cSpring.getComponent().getName();
            if (name != null) {
                origin = "name=" + name + ", ";
            }
        }
        if (spring instanceof AutopaddingSpring) {
            AutopaddingSpring paddingSpring = (AutopaddingSpring)spring;
            padding = ", userCreated=" + paddingSpring.getUserCreated() +
                    ", matches=" + paddingSpring.getMatchDescription();
        }
        buffer.append(indent + spring.getClass().getName() + " " +
                Integer.toHexString(spring.hashCode()) + " " +
                origin +
                ", size=" + spring.getSize() +
                ", alignment=" + spring.getAlignment() +
                " prefs=[" + spring.getMinimumSize(axis) +
                " " + spring.getPreferredSize(axis) +
                " " + spring.getMaximumSize(axis) + 
                padding + "]\n");
        if (spring instanceof Group) {
            List springs = ((Group)spring).springs;
            indent += "  ";
            for (int counter = 0; counter < springs.size(); counter++) {
                dump(buffer, (Spring)springs.get(counter), indent, axis);
            }
        }
    }
    
    /**
     * Sets whether or not a gap between components 
     * should automatically be created.  For example, if this is true
     * and you add two components to a <code>SequentialGroup</code> a
     * gap between the two will automatically be created.  The default
     * is false.
     *
     * @param autocreatePadding whether or not to automatically created a gap
     *        between components and the container
     */
    public void setAutocreateGaps(boolean autocreatePadding) {
        this.autocreatePadding = autocreatePadding;
    }
    
    /**
     * Returns true if gaps between components are automatically be created.
     *
     * @return true if gaps between components should automatically be created
     */
    public boolean getAutocreateGaps() {
        return autocreatePadding;
    }

    /**
     * Sets whether or not gaps between the container and the first/last
     * components should automatically be created. The default
     * is false.
     *
     * @param autocreatePadding whether or not to automatically create
     *        gaps between the container and first/last components.
     */
    public void setAutocreateContainerGaps(boolean autocreatePadding) {
        if (autocreatePadding != autocreateContainerPadding) {
            autocreateContainerPadding = autocreatePadding;
            horizontalGroup = createTopLevelGroup(getHorizontalGroup());
            verticalGroup = createTopLevelGroup(getVerticalGroup());
        }
    }
    
    /**
     * Returns whether or not gaps between the container and the
     * first/last components should automatically be created. The default
     * is false.
     *
     * @return whether or not the gaps between the container and the
     *         first/last components should automatically be created
     */
    public boolean getAutocreateContainerGaps() {
        return autocreateContainerPadding;
    }

    /**
     * Sets the <code>Group</code> that is responsible for
     * layout along the horizontal axis.
     *
     * @param group <code>Group</code> responsible for layout along
     *          the horizontal axis
     * @throws IllegalArgumentException if group is null
     */
    public void setHorizontalGroup(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("Group must be non-null");
        }
        horizontalGroup = createTopLevelGroup(group);
    }
    
    /**
     * Returns the <code>Group</code> that is responsible for
     * layout along the horizontal axis.
     *
     * @return <code>ParallelGroup</code> responsible for layout along
     *          the horizontal axis.
     */
    public Group getHorizontalGroup() {
        int index = 0;
        if (horizontalGroup.springs.size() > 1) {
            index = 1;
        }
        return (Group)horizontalGroup.springs.get(index);
    }
    
    /**
     * Sets the <code>Group</code> that is responsible for
     * layout along the vertical axis.
     *
     * @param group <code>Group</code> responsible for layout along
     *          the vertical axis.
     * @throws IllegalArgumentException if group is null.
     */
    public void setVerticalGroup(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("Group must be non-null");
        }
        verticalGroup = createTopLevelGroup(group);
    }
    
    /**
     * Returns the <code>ParallelGroup</code> that is responsible for
     * layout along the vertical axis.
     *
     * @return <code>ParallelGroup</code> responsible for layout along
     *          the vertical axis.
     */
    public Group getVerticalGroup() {
        int index = 0;
        if (verticalGroup.springs.size() > 1) {
            index = 1;
        }
        return (Group)verticalGroup.springs.get(index);
    }

    /**
     * Wraps the user specified group in a sequential group.  If 
     * container gaps should be generate the necessary springs are
     * added.
     */
    private Group createTopLevelGroup(Group specifiedGroup) {
        SequentialGroup group = createSequentialGroup();
        if (getAutocreateContainerGaps()) {
            group.addSpring(new ContainerAutopaddingSpring());
            group.add(specifiedGroup);
            group.addSpring(new ContainerAutopaddingSpring());
        } else {
            group.add(specifiedGroup);
        }
        return group;
    }

    /**
     * Creates and returns a <code>SequentialGroup</code>.
     *
     * @return a new <code>SequentialGroup</code>
     */
    public SequentialGroup createSequentialGroup() {
        return new SequentialGroup();
    }
    
    /**
     * Creates and returns a <code>ParallelGroup</code> with a
     * <code>LEADING</code> alignment.  This is a cover method for the more
     * general <code>createParallelGroup(int)</code> method.
     *
     * @return a new ParallelGroup
     * @see #createParallelGroup(int)
     */
    public ParallelGroup createParallelGroup() {
        return createParallelGroup(LEADING);
    }
    
    /**
     * Creates and returns an <code>ParallelGroup</code>.  The alignment
     * specifies how children elements should be positioned when the
     * the parallel group is given more space than necessary.  For example,
     * if a ParallelGroup with an alignment of TRAILING is given 100 pixels
     * and a child only needs 50 pixels, the child will be positioned at the
     * position 50.
     *
     * @param alignment alignment for the elements of the Group, one
     *        of <code>LEADING</code>, <code>TRAILING</code>,
     *        <code>CENTER</code> or <code>BASELINE</code>.
     * @throws IllegalArgumentException if alignment is not one of
     *         <code>LEADING</code>, <code>TRAILING</code>,
     *         <code>CENTER</code> or <code>BASELINE</code>
     * @return a new <code>ParallelGroup</code>
     */
    public ParallelGroup createParallelGroup(int alignment) {
        return createParallelGroup(alignment, true);
    }
    
    /**
     * Creates and returns an <code>ParallelGroup</code>.  The alignment
     * specifies how children elements should be positioned when the
     * the parallel group is given more space than necessary.  For example,
     * if a ParallelGroup with an alignment of TRAILING is given 100 pixels
     * and a child only needs 50 pixels, the child will be positioned at the
     * position 50.
     *
     * @param alignment alignment for the elements of the Group, one
     *        of <code>LEADING</code>, <code>TRAILING</code>,
     *        <code>CENTER</code> or <code>BASELINE</code>.
     * @param resizable whether or not the group is resizable.  If the group
     *        is not resizable the min/max size will be the same as the
     *        preferred.
     * @throws IllegalArgumentException if alignment is not one of
     *         <code>LEADING</code>, <code>TRAILING</code>,
     *         <code>CENTER</code> or <code>BASELINE</code>
     * @return a new <code>ParallelGroup</code>
     */
    public ParallelGroup createParallelGroup(int alignment, boolean resizable) {
        if (alignment == BASELINE) {
            return new BaselineGroup(resizable);
        }
        return new ParallelGroup(alignment, resizable);
    }
    
    /**
     * Forces the set of components to have the same size.
     * This can be used multiple times to force
     * any number of components to share the same size.
     * <p>
     * Linked Components are not be resizable.
     *
     * @param components Components to force to have same size.
     * @throws IllegalArgumentException if <code>components</code> is
     *         null, or contains null.
     */
    public void linkSize(Component[] components) {
        linkSize(components, HORIZONTAL | VERTICAL);
    }
    
    /**
     * Forces the set of components to have the same size.
     * This can be used multiple times to force
     * any number of components to share the same size.
     * <p>
     * Linked Components are not be resizable.
     *
     * @param components Components to force to have same size.
     * @param axis Axis to bind size, one of HORIZONTAL, VERTICAL or
     *             HORIZONTAL | VERTICAL
     * @throws IllegalArgumentException if <code>components</code> is
     *         null, or contains null.
     * @throws IllegalArgumentException if <code>axis</code> does not
     *         contain <code>HORIZONTAL</code> or <code>VERTICAL</code>
     */
    public void linkSize(Component[] components, int axis) {
        if (components == null) {
            throw new IllegalArgumentException("Components must be non-null");
        }
        boolean horizontal = ((axis & HORIZONTAL) == HORIZONTAL);
        boolean vertical = ((axis & VERTICAL) == VERTICAL);
        if (!vertical && !horizontal) {
            throw new IllegalArgumentException(
                    "Axis must contain HORIZONTAL or VERTICAL");
        }
        for (int counter = components.length - 1; counter >= 0; counter--) {
            Component c = components[counter];
            if (components[counter] == null) {
                throw new IllegalArgumentException(
                        "Components must be non-null");
            }
            // Force the component to be added
            getComponentInfo(c);
        }
        if (horizontal) {
            linkSize0(components, HORIZONTAL);
        }
        if (vertical) {
            linkSize0(components, VERTICAL);
        }
    }
    
    private void linkSize0(Component[] components, int axis) {
        ComponentInfo master = getComponentInfo(
                components[components.length - 1]).getMasterComponentInfo(axis);
        for (int counter = components.length - 2; counter >= 0; counter--) {
            master.addChild(getComponentInfo(components[counter]), axis);
        }
    }

    /**
     * Removes an existing component replacing it with the specified component.
     *
     * @param existingComponent the Component that should be removed and
     *        replaced with newComponent
     * @param newComponent the Component to put in existingComponents place
     * @throws IllegalArgumentException is either of the Components are null or
     *         if existingComponent is not being managed by this layout manager
     */
    public void replace(Component existingComponent, Component newComponent) {
        if (existingComponent == null || newComponent == null) {
            throw new IllegalArgumentException("Components must be non-null");
        }
        ComponentInfo info = (ComponentInfo)componentInfos.
                remove(existingComponent);
        if (info == null) {
            throw new IllegalArgumentException("Component must already exist");
        }
        host.remove(existingComponent);
        host.add(newComponent);
        info.setComponent(newComponent);
        componentInfos.put(newComponent, info);
        invalidateLayout(host);
    }
    
    //
    // LayoutManager
    //
    /**
     * Notification that a <code>Component</code> has been added to
     * the parent container.  Developers should not invoke this method
     * directly, instead you should use one of the <code>Group</code>
     * methods to add a <code>Component</code>.
     *
     * @param name the string to be associated with the component
     * @param component the <code>Component</code> to be added
     */
    public void addLayoutComponent(String name, Component component) {
    }
    
    /**
     * Notification that a <code>Component</code> has been removed from
     * the parent container.  You should not invoke this method
     * directly, instead invoke <code>remove</code> on the parent
     * <code>Container</code>.
     *
     * @param comp the component to be removed
     * @see java.awt.Component#remove
     */
    public void removeLayoutComponent(Component comp) {
    }
    
    /**
     * Returns the preferred size for the specified container.
     *
     * @param parent the container to return size for
     * @throws IllegalArgumentException if <code>parent</code> is not
     *         the same <code>Container</code> that this was created with
     * @throws IllegalStateException if any of the components added to
     *         this layout are not in both a horizontal and vertical group
     * @see java.awt.Container#getPreferredSize
     */
    public Dimension preferredLayoutSize(Container parent) {
        checkParent(parent);
        prepare(PREF_SIZE);
        return adjustSize(horizontalGroup.getPreferredSize(HORIZONTAL),
                verticalGroup.getPreferredSize(VERTICAL));
    }
    
    /**
     * Returns the minimum size for the specified container.
     *
     * @param parent the container to return size for
     * @throws IllegalArgumentException if <code>parent</code> is not
     *         the same <code>Container</code> that this was created with
     * @throws IllegalStateException if any of the components added to
     *         this layout are not in both a horizontal and vertical group
     * @see java.awt.Container#getMinimumSize
     */
    public Dimension minimumLayoutSize(Container parent) {
        checkParent(parent);
        prepare(MIN_SIZE);
        return adjustSize(horizontalGroup.getMinimumSize(HORIZONTAL),
                verticalGroup.getMinimumSize(VERTICAL));
    }
    
    /**
     * Lays out the specified container.
     *
     * @param parent the container to be laid out
     * @throws IllegalStateException if any of the components added to
     *         this layout are not in both a horizontal and vertical group
     */
    public void layoutContainer(Container parent) {
        prepare();
        Insets insets = parent.getInsets();

        if (getAutocreateGaps() || getAutocreateContainerGaps() ||
                hasPreferredPaddingSprings) {
            resetAutopadding(horizontalGroup, HORIZONTAL, -1, 0,
                             parent.getWidth() - insets.left - insets.right);
            resetAutopadding(verticalGroup, VERTICAL, -1, 0,
                             parent.getHeight() - insets.top - insets.bottom);
        }
        horizontalGroup.setSize(HORIZONTAL, 0, parent.
                getWidth() - insets.left - insets.right);
        verticalGroup.setSize(VERTICAL, 0, parent.
                getHeight() - insets.top - insets.bottom);
        
        Iterator componentInfo = componentInfos.values().iterator();
        while (componentInfo.hasNext()) {
            ComponentInfo info = (ComponentInfo)componentInfo.next();
            Component c = info.getComponent();
            info.setBounds(insets);
        }
    }
    
    //
    // LayoutManager2
    //
    /**
     * Notification that a <code>Component</code> has been added to
     * the parent container.  You should not invoke this method
     * directly, instead you should use one of the <code>Group</code>
     * methods to add a <code>Component</code>.
     *
     * @param component The component added
     * @param constraints Description of where to place the component.
     */
    public void addLayoutComponent(Component component, Object constraints) {
    }
    
    /**
     * Returns the maximum size for the specified container.
     *
     * @param parent the container to return size for
     * @throws IllegalArgumentException if <code>parent</code> is not
     *         the same <code>Container</code> that this was created with
     * @throws IllegalStateException if any of the components added to
     *         this layout are not in both a horizontal and vertical group
     * @see java.awt.Container#getMaximumSize
     */
    public Dimension maximumLayoutSize(Container parent) {
        checkParent(parent);
        prepare(MAX_SIZE);
        return adjustSize(horizontalGroup.getMaximumSize(HORIZONTAL),
                verticalGroup.getMaximumSize(VERTICAL));
    }
    
    /**
     * Returns the alignment along the x axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     *
     * @param parent Container hosting this LayoutManager
     * @throws IllegalArgumentException if <code>parent</code> is not
     *         the same <code>Container</code> that this was created with
     * @return alignment
     */
    public float getLayoutAlignmentX(Container parent) {
        checkParent(parent);
        return .5f;
    }
    
    /**
     * Returns the alignment along the y axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     *
     * @param parent Container hosting this LayoutManager
     * @throws IllegalArgumentException if <code>parent</code> is not
     *         the same <code>Container</code> that this was created with
     * @return alignment
     */
    public float getLayoutAlignmentY(Container parent) {
        checkParent(parent);
        return .5f;
    }
    
    /**
     * Invalidates the layout, indicating that if the layout manager
     * has cached information it should be discarded.
     *
     * @param parent Container hosting this LayoutManager
     * @throws IllegalArgumentException if <code>parent</code> is not
     *         the same <code>Container</code> that this was created with
     */
    public void invalidateLayout(Container parent) {
        checkParent(parent);
        // invalidateLayout is called from Container.invalidate, which
        // does NOT grab the treelock.  All other methods do.  To make sure
        // there aren't any possible threading problems we grab the tree lock
        // here.
        synchronized(parent.getTreeLock()) {
            horizontalGroup.setSize(HORIZONTAL, UNSET, UNSET);
            verticalGroup.setSize(VERTICAL, UNSET, UNSET);
            for (Iterator cis = componentInfos.values().iterator();
                     cis.hasNext();) {
                ComponentInfo ci = (ComponentInfo)cis.next();
                ci.clear();
            }
        }
    }
    
    private void resetAutopadding(Group group, int axis, int sizeType,
                                  int origin, int size) {
        group.resetAutopadding();
        switch(sizeType) {
            case MIN_SIZE:
                size = group.getMinimumSize(axis);
                break;
            case PREF_SIZE:
                size = group.getPreferredSize(axis);
                break;
            case MAX_SIZE:
                size = group.getMaximumSize(axis);
                break;
        }
        group.setSize(axis, origin, size);
        group.calculateAutopadding(axis);
    }
    
    private void prepare(int sizeType) {
        prepare();
        if (getAutocreateGaps() || getAutocreateContainerGaps() ||
                hasPreferredPaddingSprings) {
            resetAutopadding(horizontalGroup, HORIZONTAL, sizeType, 0, 0);
            resetAutopadding(verticalGroup, VERTICAL, sizeType, 0, 0);
        }
        
    }
    
    private void prepare() {
        if (springsAdded) {
            registerComponents(horizontalGroup, HORIZONTAL);
            registerComponents(verticalGroup, VERTICAL);
            checkComponents();
            horizontalGroup.removeAutopadding();
            verticalGroup.removeAutopadding();
            if (getAutocreateGaps()) {
                adjustAutopadding(true);
            } else if (hasPreferredPaddingSprings ||
                       getAutocreateContainerGaps()) {
                adjustAutopadding(false);
            }
            springsAdded = false;
        }
    }
    
    private void checkComponents() {
        Iterator infos = componentInfos.values().iterator();
        while (infos.hasNext()) {
            ComponentInfo info = (ComponentInfo)infos.next();
            if (info.horizontalSpring == null) {
                throw new IllegalStateException(info.component +
                        " is not attached to a horizontal group");
            }
            if (info.verticalSpring == null) {
                throw new IllegalStateException(info.component +
                        " is not attached to a vertical group");
            }
        }
    }
    
    private void registerComponents(Group group, int axis) {
        List springs = group.springs;
        for (int counter = springs.size() - 1; counter >= 0; counter--) {
            Spring spring = (Spring)springs.get(counter);
            if (spring instanceof ComponentSpring) {
                ((ComponentSpring)spring).installIfNecessary(axis);
            } else if (spring instanceof Group) {
                registerComponents((Group)spring, axis);
            }
        }
    }
    
    private Dimension adjustSize(int width, int height) {
        Insets insets = host.getInsets();
        return new Dimension(width + insets.left + insets.right,
                height + insets.top + insets.bottom);
    }
    
    private void checkParent(Container parent) {
        if (parent != host) {
            throw new IllegalArgumentException(
                    "GroupLayout can only be used with one Container at a time");
        }
    }
    
    /**
     * Returns the <code>ComponentInfo</code> for the specified Component.
     */
    private ComponentInfo getComponentInfo(Component component) {
        ComponentInfo info = (ComponentInfo)componentInfos.get(component);
        if (info == null) {
            componentInfos.put(component, new ComponentInfo(component));
            host.add(component);
        }
        return info;
    }
    
    /**
     * Adjusts the autopadding springs for the horizontal and vertical
     * groups.  If <code>insert</code> is true this will insert auto padding
     * springs, otherwise this will only adjust the springs that
     * comprise auto preferred padding springs.
     */
    private void adjustAutopadding(boolean insert) {
        horizontalGroup.insertAutopadding(HORIZONTAL, new ArrayList(1),
                new ArrayList(1), new ArrayList(1), new ArrayList(1), insert);
        verticalGroup.insertAutopadding(VERTICAL, new ArrayList(1),
                new ArrayList(1), new ArrayList(1), new ArrayList(1), insert);
    }
    
    /**
     * Returns true if the two Components have a common ParallelGroup ancestor
     * along the particular axis.
     */
    private boolean areParallelSiblings(Component source, Component target,
            int axis) {
        ComponentInfo sourceInfo = getComponentInfo(source);
        ComponentInfo targetInfo = getComponentInfo(target);
        Spring sourceSpring;
        Spring targetSpring;
        if (axis == HORIZONTAL) {
            sourceSpring = sourceInfo.horizontalSpring;
            targetSpring = targetInfo.horizontalSpring;
        } else {
            sourceSpring = sourceInfo.verticalSpring;
            targetSpring = targetInfo.verticalSpring;
        }
        List sourcePath = parallelList;
        sourcePath.clear();
        Spring spring = sourceSpring.getParent();
        while (spring != null) {
            sourcePath.add(spring);
            spring = spring.getParent();
        }
        spring = targetSpring.getParent();
        while (spring != null) {
            if (sourcePath.contains(spring)) {
                while (spring != null) {
                    if (spring instanceof ParallelGroup) {
                        return true;
                    }
                    spring = spring.getParent();
                }
                return false;
            }
            spring = spring.getParent();
        }
        return false;
    }
    
    
    /**
     * Spring consists of a range: min, pref and max a value some where in
     * the middle of that and a location.  Subclasses must override
     * methods to get the min/max/pref and will likely want to override
     * the <code>setSize</code> method.  Spring automatically caches the
     * min/max/pref.  If the min/pref/max has internally changes, or needs
     * to be updated you must invoked clear.
     */
    abstract class Spring {
        private int size;
        private int min;
        private int max;
        private int pref;
        private Spring parent;
        
        private int alignment;
        
        Spring() {
            min = pref = max = UNSET;
        }
        
        abstract int getMinimumSize0(int axis);
        abstract int getPreferredSize0(int axis);
        abstract int getMaximumSize0(int axis);

        /**
         * Sets the parent of this Spring.
         */
        void setParent(Spring parent) {
            this.parent = parent;
        }
        
        /**
         * Returns the parent of this spring.
         */
        Spring getParent() {
            return parent;
        }
        
        // This is here purely as a conveniance for ParallelGroup to avoid
        // having to track alignment separately.
        void setAlignment(int alignment) {
            checkAlignment(alignment, false);
            this.alignment = alignment;
        }
        
        int getAlignment() {
            return alignment;
        }
        
        /**
         * Returns the minimum size.
         */
        final int getMinimumSize(int axis) {
            if (min == UNSET) {
                min = constrain(getMinimumSize0(axis));
            }
            return min;
        }
        
        /**
         * Returns the preferred size.
         */
        final int getPreferredSize(int axis) {
            if (pref == UNSET) {
                pref = constrain(getPreferredSize0(axis));
            }
            return pref;
        }
        
        /**
         * Returns the maximum size.
         */
        final int getMaximumSize(int axis) {
            if (max == UNSET) {
                max = constrain(getMaximumSize0(axis));
            }
            return max;
        }
        
        /**
         * Resets the cached min/max/pref.
         */
        void clear() {
            size = min = pref = max = UNSET;
        }
        
        /**
         * Sets the value and location of the spring.  Subclasses
         * will want to invoke super, then do any additional sizing.
         *
         * @param axis HORIZONTAL or VERTICAL
         * @param origin of this Spring
         * @param size of the Spring.  If size is UNSET, this invokes
         *        clear.
         */
        void setSize(int axis, int origin, int size) {
            this.size = size;
            if (size == UNSET) {
                clear();
            }
        }
        
        /**
         * Returns the current size.
         */
        int getSize() {
            return size;
        }
        
        int constrain(int value) {
            return Math.min(value, Short.MAX_VALUE);
        }
    }
    
    
    /**
     * Group provides for commonality between the two types of operations
     * supported by <code>GroupLayout</code>: laying out components one
     * after another (<code>SequentialGroup</code>) or layout on top
     * of each other (<code>ParallelGroup</code>). Use one of
     * <code>createSequentialGroup</code> or
     * <code>createParallelGroup</code> to create one.
     */
    public abstract class Group extends Spring {
        // private int origin;
        // private int size;
        List springs;
        
        Group() {
            springs = new ArrayList();
        }
        
        int indexOf(Spring spring) {
            return springs.indexOf(spring);
        }
        
        /**
         * Adds the Spring to the list of <code>Spring</code>s and returns
         * the receiver.
         */
        Group addSpring(Spring spring, int index) {
            springs.add(spring);
            spring.setParent(this);
            if (!(spring instanceof AutopaddingSpring)) {
                springsAdded = true;
            }
            return this;
        }
        
        /**
         * Adds the Spring to the list of <code>Spring</code>s and returns
         * the receiver.
         */
        Group addSpring(Spring spring) {
            addSpring(spring, springs.size());
            return this;
        }
        
        //
        // Spring methods
        //
        
        void setParent(Spring parent) {
            super.setParent(parent);
            for (int counter = springs.size() - 1; counter >= 0; counter--) {
                ((Spring)springs.get(counter)).setParent(this);
            }
        }
        
        void setSize(int axis, int origin, int size) {
            super.setSize(axis, origin, size);
            if (size == UNSET) {
                for (int counter = springs.size() - 1; counter >= 0;
                     counter--) {
                    getSpring(counter).setSize(axis, origin, size);
                }
            } else {
                setSize0(axis, origin, size);
            }
        }
        
        /**
         * This is invoked from <code>setSize</code> if passed a value
         * other than UNSET.
         */
        abstract void setSize0(int axis, int origin, int size);
        
        int getMinimumSize0(int axis) {
            return calculateSize(axis, MIN_SIZE);
        }
        
        int getPreferredSize0(int axis) {
            return calculateSize(axis, PREF_SIZE);
        }
        
        int getMaximumSize0(int axis) {
            return calculateSize(axis, MAX_SIZE);
        }
        
        /**
         * Used to compute how the two values representing two springs
         * will be combined.  For example, a group that layed things out
         * one after the next would return <code>a + b</code>.
         */
        abstract int operator(int a, int b);
        
        /**
         * Calculates the specified size.  This is called from
         * one of the <code>getMinimumSize0</code>,
         * <code>getPreferredSize0</code> or
         * <code>getMaximumSize0</code> methods.  This will invoke
         * to <code>operator</code> to combine the values.
         */
        int calculateSize(int axis, int type) {
            int count = springs.size();
            if (count == 0) {
                return 0;
            }
            if (count == 1) {
                return getSize(getSpring(0), axis, type);
            }
            int size = constrain(operator(getSize(getSpring(0), axis, type),
                    getSize(getSpring(1), axis, type)));
            for (int counter = 2; counter < count; counter++) {
                size = constrain(operator(size, getSize(getSpring(counter),
                        axis, type)));
            }
            return size;
        }
        
        Spring getSpring(int index) {
            return (Spring)springs.get(index);
        }
        
        int getSize(Spring spring, int axis, int type) {
            switch(type) {
                case MIN_SIZE:
                    return spring.getMinimumSize(axis);
                case PREF_SIZE:
                    return spring.getPreferredSize(axis);
                case MAX_SIZE:
                    return spring.getMaximumSize(axis);
            }
            return 0;
        }
        
        // Padding
        /**
         * Adjusts the autopadding springs in this group and its children.
         * If <code>insert</code> is true this will insert auto padding
         * springs, otherwise this will only adjust the springs that
         * comprise auto preferred padding springs.
         *
         * @param axis the axis of the springs; HORIZONTAL or VERTICAL
         * @param leadingPadding List of AutopaddingSprings that occur before
         *                       this Group
         * @param trailingPadding any trailing autopadding springs are added
         *                        to this on exit
         * @param leading List of ComponentSprings that occur before this Group
         * @param trailing any trailing ComponentSpring are added to this
         *                 List
         * @param insert Whether or not to insert AutopaddingSprings or just
         *               adjust any existing AutopaddingSprings.
         */
        abstract void insertAutopadding(int axis, List leadingPadding,
                List trailingPadding, List leading, List trailing,
                boolean insert);
        
        /**
         * Removes any AutopaddingSprings.
         */
        void removeAutopadding() {
            for (int counter = springs.size() - 1; counter >= 0; counter--) {
                Spring spring = (Spring)springs.get(counter);
                if (spring instanceof AutopaddingSpring) {
                    if (((AutopaddingSpring)spring).getUserCreated()) {
                        ((AutopaddingSpring)spring).reset();
                    } else {
                        springs.remove(counter);
                    }
                } else if (spring instanceof Group) {
                    ((Group)spring).removeAutopadding();
                }
            }
        }
        
        void resetAutopadding() {
            // Clear cached pref/min/max.
            clear();
            for (int counter = springs.size() - 1; counter >= 0; counter--) {
                Spring spring = (Spring)springs.get(counter);
                if (spring instanceof AutopaddingSpring) {
                    ((AutopaddingSpring)spring).clear();
                } else if (spring instanceof Group) {
                    ((Group)spring).resetAutopadding();
                }
            }
        }
        
        void calculateAutopadding(int axis) {
            for (int counter = springs.size() - 1; counter >= 0; counter--) {
                Spring spring = (Spring)springs.get(counter);
                if (spring instanceof AutopaddingSpring) {
                    // Force size to be reset.
                    spring.clear();
                    ((AutopaddingSpring)spring).calculatePadding(axis);
                } else if (spring instanceof Group) {
                    ((Group)spring).calculateAutopadding(axis);
                }
            }
            // Clear cached pref/min/max.
            clear();
        }
    }
    
    
    /**
     * A <code>Group</code> that lays out its elements sequentially, one
     * after another.  This class has no public constructor, use the
     * <code>createSequentialGroup</code> method to create one.
     *
     * @see #createSequentialGroup()
     */
    public class SequentialGroup extends Group {
        SequentialGroup() {
        }
        
        /**
         * Adds the specified <code>Group</code> to this
         * <code>SequentialGroup</code>
         *
         * @param group the Group to add
         * @return this Group
         */
        public SequentialGroup add(Group group) {
            return (SequentialGroup)addSpring(group);
        }
        
        /**
         * Adds the specified Component.  If the Component's min/max
         * are different from its pref than the component will be resizable.
         *
         * @param component the Component to add
         * @return this <code>SequentialGroup</code>
         */
        public SequentialGroup add(Component component) {
            return add(component, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE);
        }
        
        /**
         * Adds the specified <code>Component</code>.  Min, pref and max
         * can be absolute values, or they can be one of
         * <code>DEFAULT_SIZE</code> or <code>PREFERRED_SIZE</code>.  For
         * example, the following:
         * <pre>
         *   add(component, PREFERRED_SIZE, PREFERRED_SIZE, 1000);
         * </pre>
         * Forces a max of 1000, with the min and preferred equalling that
         * of the preferred size of <code>component</code>.
         *
         * @param component the Component to add
         * @param min the minimum size
         * @param pref the preferred size
         * @param max the maximum size
         * @throws IllegalArgumentException if min, pref or max are
         *         not positive and not one of PREFERRED_SIZE or DEFAULT_SIZE
         * @return this <code>SequentialGroup</code>
         */
        public SequentialGroup add(Component component, int min, int pref,
                int max) {
            return (SequentialGroup)addSpring(new ComponentSpring(
                    component, min, pref, max));
        }
        
        /**
         * Adds a rigid gap.
         *
         * @param pref the size of the gap
         * @throws IllegalArgumentException if min < 0 or pref < 0 or max < 0
         *         or the following is not meant min <= pref <= max
         * @return this <code>SequentialGroup</code>
         */
        public SequentialGroup add(int pref) {
            return add(pref, pref, pref);
        }
        
        /**
         * Adds a gap with the specified size.
         *
         * @param min the minimum size of the gap, or PREFERRED_SIZE
         * @param pref the preferred size of the gap
         * @param max the maximum size of the gap, or PREFERRED_SIZE
         * @throws IllegalArgumentException if min < 0 or pref < 0 or max < 0
         *         or the following is not meant min <= pref <= max
         * @return this <code>SequentialGroup</code>
         */
        public SequentialGroup add(int min, int pref, int max) {
            return (SequentialGroup)addSpring(new GapSpring(min, pref, max));
        }
        
        /**
         * Adds an element representing the preferred gap between the two
         * components.
         * 
         * @param comp1 the first component
         * @param comp2 the second component
         * @param type the type of gap; one of the constants defined by
         *        LayoutStyle
         * @return this <code>SequentialGroup</code>
         * @throws IllegalArgumentException if <code>type</code> is not a
         *         valid LayoutStyle constant
         * @see LayoutStyle
         */
        public SequentialGroup addPreferredGap(Component comp1,
                Component comp2,
                int type) {
            return addPreferredGap(comp1, comp2, type, false);
        }
        
        /**
         * Adds an element representing the preferred gap between the two
         * components.
         * 
         * @param comp1 the first component
         * @param comp2 the second component
         * @param type the type of gap; one of the constants defined by
         *        LayoutStyle
         * @param canGrow true if the gap can grow if more
         *                space is available
         * @return this <code>SequentialGroup</code>
         * @throws IllegalArgumentException if <code>type</code> is not a
         *         valid LayoutStyle constant
         * @see LayoutStyle
         */
        public SequentialGroup addPreferredGap(Component comp1,
                Component comp2,
                int type, boolean canGrow) {
            if (type != LayoutStyle.RELATED &&
                    type != LayoutStyle.UNRELATED &&
                    type != LayoutStyle.INDENT) {
                throw new IllegalArgumentException("Invalid type argument");
            }
            return (SequentialGroup)addSpring(new PaddingSpring(
                    comp1, comp2, type, canGrow));
        }

        /**
         * Adds an element representing the preferred gap between the
         * nearest components.  That is, during layout the neighboring
         * components are found, and the min, pref and max of this
         * element is set based on the preferred gap between the
         * components.  If no neighboring components are found the
         * min, pref and max are set to 0.
         * 
         * @param type the type of gap; one of the LayoutStyle constants
         * @return this SequentialGroup
         * @throws IllegalArgumentException if type is not one of
         *         <code>LayoutStyle.RELATED</code> or
         *         <code>LayoutStyle.UNRELATED</code>
         * @see LayoutStyle
         */
        public SequentialGroup addPreferredGap(int type) {
            return addPreferredGap(type, DEFAULT_SIZE, DEFAULT_SIZE);
        }
        
        /**
         * Adds an element for the preferred gap between the
         * nearest components.  That is, during layout the neighboring
         * components are found, and the min of this
         * element is set based on the preferred gap between the
         * components.  If no neighboring components are found the
         * min is set to 0.  This method allows you to specify the
         * preferred and maximum size by way of the <code>pref</code>
         * and <code>max</code> arguments.  These can either be a
         * value &gt;= 0, in which case the preferred or max is the max
         * of the argument and the preferred gap, of DEFAULT_VALUE in
         * which case the value is the same as the preferred gap.
         * 
         * @param type the type of gap; one of LayoutStyle.RELATED or
         *        LayoutStyle.UNRELATED
         * @param pref the preferred size; one of DEFAULT_SIZE or a value > 0
         * @param max the maximum size; one of DEFAULT_SIZE, PREFERRED_SIZE
         *        or a value > 0
         * @return this SequentialGroup
         * @throws IllegalArgumentException if type is not one of
         *         <code>LayoutStyle.RELATED</code> or
         *         <code>LayoutStyle.UNRELATED</code> or pref/max is
         *         != DEFAULT_SIZE and < 0, or pref > max
         * @see LayoutStyle
         */
        public SequentialGroup addPreferredGap(int type, int pref,
                                                   int max) {
            if (type != LayoutStyle.RELATED && type != LayoutStyle.UNRELATED) {
                throw new IllegalArgumentException(
                        "Padding type must be one of Padding.RELATED or Padding.UNRELATED");
            }
            if ((pref < 0 && pref != DEFAULT_SIZE) ||
                    (max < 0 && max != DEFAULT_SIZE && max != PREFERRED_SIZE) ||
                    (pref >= 0 && max >= 0 && pref > max)) {
                throw new IllegalArgumentException(
                        "Pref and max must be either DEFAULT_VALUE or >= 0 and pref <= max");
            }
            hasPreferredPaddingSprings = true;
            return (SequentialGroup)addSpring(new AutopaddingSpring(
                                       type, pref, max));
        }
        
        /**
         * Adds an element representing the preferred gap between one edge
         * of the container and the next/previous Component.  This will have
         * no effect if the next/previous element is not a Component and does
         * not touch one edge of the parent container. 
         *
         * @return this <code>SequentialGroup</code>.
         */
        public SequentialGroup addContainerGap() {
            return addContainerGap(DEFAULT_SIZE, DEFAULT_SIZE);
        }
        
        /**
         * Adds an element representing the preferred gap between one edge
         * of the container and the next/previous Component.  This will have
         * no effect if the next/previous element is not a Component and does
         * not touch one edge of the parent container. 
         *
         * @param pref the preferred size; one of DEFAULT_SIZE or a value > 0
         * @param max the maximum size; one of DEFAULT_SIZE, PREFERRED_SIZE
         *        or a value > 0.
         * @throws IllegalArgumentException if pref/max is
         *         != DEFAULT_SIZE and < 0, or pref > max
         * @return this <code>SequentialGroup</code>
         */
        public SequentialGroup addContainerGap(int pref, int max) {
            if ((pref < 0 && pref != DEFAULT_SIZE) ||
                    (max < 0 && max != DEFAULT_SIZE && max != PREFERRED_SIZE) ||
                    (pref >= 0 && max >= 0 && pref > max)) {
                throw new IllegalArgumentException(
                        "Pref and max must be either DEFAULT_VALUE or >= 0 and pref <= max");
            }
            hasPreferredPaddingSprings = true;
            return (SequentialGroup)addSpring(
                    new ContainerAutopaddingSpring(pref, max));
        }
        
        int operator(int a, int b) {
            return constrain(a) + constrain(b);
        }
        
        void setSize0(int axis, int origin, int size) {
            int pref = getPreferredSize(axis);
            if ((size - pref) == 0) {
                for (int counter = 0, max = springs.size(); counter < max;
                counter++) {
                    Spring spring = getSpring(counter);
                    int springPref = spring.getPreferredSize(axis);
                    spring.setSize(axis, origin, springPref);
                    origin += springPref;
                }
            } else if (springs.size() == 1) {
                Spring spring = getSpring(0);
                spring.setSize(axis, origin, Math.min(size, spring.
                        getMaximumSize(axis)));
            } else if (springs.size() > 1) {
                // Adjust between min/pref
                resize(axis, origin, size);
            }
        }
        
        private void resize(int axis, int origin, int size) {
            int delta = size - getPreferredSize(axis);
            boolean useMin = (delta < 0);
            int springCount = springs.size();
            if (useMin) {
                delta *= -1;
            }
            
            // First pass, sort the resizable springs into resizable
            List resizable = buildResizableList(axis, useMin);
            int resizableCount = resizable.size();
            
            if (resizableCount > 0) {
                int sDelta = delta / resizableCount;
                int slop = delta - sDelta * resizableCount;
                int[] sizes = new int[springCount];
                int sign = useMin ? -1 : 1;
                // Second pass, accumulate the resulting deltas (relative to
                // preferred) into sizes.
                for (int counter = 0; counter < resizableCount; counter++) {
                    SpringDelta springDelta = (SpringDelta)resizable.
                            get(counter);
                    if ((counter + 1) == resizableCount) {
                        sDelta += slop;
                    }
                    springDelta.delta = Math.min(sDelta, springDelta.delta);
                    delta -= springDelta.delta;
                    if (springDelta.delta != sDelta && counter + 1 <
                            resizableCount) {
                        // Spring didn't take all the space, reset how much
                        // each spring will get.
                        sDelta = delta / (resizableCount - counter - 1);
                        slop = delta - sDelta * (resizableCount - counter - 1);
                    }
                    Spring spring = getSpring(springDelta.index);
                    sizes[springDelta.index] = sign * springDelta.delta;
                }
                
                // And finally set the size of each spring
                for (int counter = 0; counter < springCount; counter++) {
                    Spring spring = getSpring(counter);
                    int sSize = spring.getPreferredSize(axis) + sizes[counter];
                    spring.setSize(axis, origin, sSize);
                    origin += sSize;
                }
            } else {
                // Nothing resizable, use the min or max of each of the
                // springs.
                for (int counter = 0; counter < springCount; counter++) {
                    Spring spring = getSpring(counter);
                    int sSize;
                    if (useMin) {
                        sSize = spring.getMinimumSize(axis);
                    } else {
                        sSize = spring.getMaximumSize(axis);
                    }
                    spring.setSize(axis, origin, sSize);
                    origin += sSize;
                }
            }
        }
        
        /**
         * Returns the sorted list of SpringDelta's for the current set of
         * Springs.
         */
        private List buildResizableList(int axis, boolean useMin) {
            // First pass, figure out what is resizable
            int size = springs.size();
            List sorted = new ArrayList(size);
            for (int counter = 0; counter < size; counter++) {
                Spring spring = getSpring(counter);
                int sDelta;
                if (useMin) {
                    sDelta = spring.getPreferredSize(axis) -
                            spring.getMinimumSize(axis);
                } else {
                    sDelta = spring.getMaximumSize(axis) -
                            spring.getPreferredSize(axis);
                }
                if (sDelta > 0) {
                    sorted.add(new SpringDelta(counter, sDelta));
                }
            }
            Collections.sort(sorted);
            return sorted;
        }

        /**
         * Returns an AutopaddingSpring, or null, at the specified index.
         * If the Spring at index is an AutopaddingSpring and user created
         * it will be returned.  Otherwise if insert is true this will
         * create and AutopaddingSpring and insert it.  If insert is false
         * null will be returned.
         */
        private AutopaddingSpring getNextAutopadding(int index, boolean insert) {
            Spring spring = getSpring(index);
            if (spring instanceof AutopaddingSpring &&
                    ((AutopaddingSpring)spring).getUserCreated()) {
                return (AutopaddingSpring)spring;
            }
            if (insert) {
                AutopaddingSpring autoSpring = new AutopaddingSpring();
                springs.add(index, autoSpring);
                return autoSpring;
            }
            return null;
        }
        
        void insertAutopadding(int axis, List leadingPadding,
                List trailingPadding, List leading, List trailing,
                boolean insert) {
            List newLeadingPadding = new ArrayList(leadingPadding);
            List newTrailingPadding = new ArrayList(1);
            List newLeading = new ArrayList(leading);
            List newTrailing = null;
            for (int counter = 0; counter < springs.size(); counter++) {
                Spring spring = getSpring(counter);
                if (spring instanceof AutopaddingSpring) {
                    AutopaddingSpring padding = (AutopaddingSpring)spring;
                    padding.setSources(newLeading);
                    newLeading.clear();
                    if (counter + 1 == springs.size()) {
                        if (!(padding instanceof ContainerAutopaddingSpring)) {
                            trailingPadding.add(padding);
                        }
                    } else {
                        newLeadingPadding.clear();
                        newLeadingPadding.add(padding);
                    }
                } else {
                    if (newLeading.size() > 0 && insert) {
                        AutopaddingSpring padding = new AutopaddingSpring();
                        // Force this to be revisted by decrementing counter
                        // and breaking
                        springs.add(counter--, padding);
                        continue;
                    }
                    if (spring instanceof ComponentSpring) {
                        ComponentSpring cSpring = (ComponentSpring)spring;
                        for (int i = 0; i < newLeadingPadding.size(); i++) {
                            ((AutopaddingSpring)newLeadingPadding.get(i)).add(
                                    cSpring, axis);
                        }
                        newLeading.clear();
                        newLeadingPadding.clear();
                        if (counter + 1 == springs.size()) {
                            trailing.add(cSpring);
                        } else {
                            newLeading.add(cSpring);
                        }
                    } else if (spring instanceof Group) {
                        if (newTrailing == null) {
                            newTrailing = new ArrayList(1);
                        } else {
                            newTrailing.clear();
                        }
                        newTrailingPadding.clear();
                        ((Group)spring).insertAutopadding(axis, newLeadingPadding,
                                newTrailingPadding, newLeading, newTrailing,
                                insert);
                        newLeading.clear();
                        newLeadingPadding.clear();
                        if (counter + 1 == springs.size()) {
                            trailing.addAll(newTrailing);
                            trailingPadding.addAll(newTrailingPadding);
                        } else {
                            newLeading.addAll(newTrailing);
                            newLeadingPadding.addAll(newTrailingPadding);
                        }
                    } else {
                        newLeadingPadding.clear();
                        newLeading.clear();
                    }
                }
            }
        }
    }
    
    
    /**
     * Used in figuring out how much space to give resizable springs.
     */
    private static class SpringDelta implements Comparable {
        // Original index.
        public int index;
        // Delta, one of pref - min or max - pref.
        public int delta;
        
        public SpringDelta(int index, int delta) {
            this.index = index;
            this.delta = delta;
        }
        
        public int compareTo(Object o) {
            return delta - ((SpringDelta)o).delta;
        }
        
        public String toString() {
            return super.toString() + "[index=" + index + ", delta=" +
                    delta + "]";
        }
    }
    
    
    /**
     * A <code>Group</code> that lays out its elements on top of each
     * other.  If a child element is smaller than the provided space it
     * is aligned based on the alignment of the child (if specified) or
     * on the alignment of the ParallelGroup.
     *
     * @see #createParallelGroup()
     */
    public class ParallelGroup extends Group {
        // How children are layed out.
        private int childAlignment;
        // Whether or not we're resizable.
        private boolean resizable;
        
        ParallelGroup(int childAlignment, boolean resizable) {
            checkAlignment(childAlignment, true);
            this.childAlignment = childAlignment;
            this.resizable = resizable;
        }
        
        /**
         * Adds the specified <code>Group</code>.
         *
         * @param group the Group to add
         * @return this Group
         */
        public ParallelGroup add(Group group) {
            return (ParallelGroup)addSpring(group);
        }
        
        /**
         * Adds the specified Component.  If the Component's min/max
         * are different from its pref than the component will be resizable.
         *
         * @param component the Component to add
         * @return this <code>ParallelGroup</code>
         */
        public ParallelGroup add(Component component) {
            return add(component, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE);
        }
        
        /**
         * Adds the specified <code>Component</code>.  Min, pref and max
         * can be absolute values, or they can be one of
         * <code>DEFAULT_SIZE</code> or <code>PREFERRED_SIZE</code>.  For
         * example, the following:
         * <pre>
         *   add(component, PREFERRED_SIZE, PREFERRED_SIZE, 1000);
         * </pre>
         * Forces a max of 1000, with the min and preferred equalling that
         * of the preferred size of <code>component</code>.
         *
         * @param component the Component to add
         * @param min the minimum size
         * @param pref the preferred size
         * @param max the maximum size
         * @throws IllegalArgumentException if min, pref or max are
         *         not positive and not one of PREFERRED_SIZE or DEFAULT_SIZE.
         * @return this <code>SequentialGroup</code>
         */
        public ParallelGroup add(Component component, int min, int pref,
                int max) {
            return (ParallelGroup)addSpring(new ComponentSpring(
                    component, min, pref, max));
        }
        
        /**
         * Adds a rigid gap.
         *
         * @param pref the size of the gap
         * @throws IllegalArgumentException if min < 0 or pref < 0 or max < 0
         *         or the following is not meant min <= pref <= max.
         * @return this <code>ParallelGroup</code>
         */
        public ParallelGroup add(int pref) {
            return add(pref, pref, pref);
        }
        
        /**
         * Adds a gap with the specified size.
         *
         * @param min the minimum size of the gap
         * @param pref the preferred size of the gap
         * @param max the maximum size of the gap
         * @throws IllegalArgumentException if min < 0 or pref < 0 or max < 0
         *         or the following is not meant min <= pref <= max.
         * @return this <code>ParallelGroup</code>
         */
        public ParallelGroup add(int min, int pref, int max) {
            return (ParallelGroup)addSpring(new GapSpring(min, pref, max));
        }
        
        /**
         * Adds the specified <code>Group</code> as a child of this group.
         *
         * @param alignment the alignment of the Group.
         * @param group the Group to add
         * @return this <code>ParallelGroup</code>
         * @throws IllegalArgumentException if alignment is not one of
         *         <code>LEADING</code>, <code>TRAILING</code> or
         *         <code>CENTER</code>
         */
        public ParallelGroup add(int alignment, Group group) {
            group.setAlignment(alignment);
            return (ParallelGroup)addSpring(group);
        }
        
        /**
         * Adds the specified Component.  If the Component's min/max
         * are different from its pref than the component will be resizable.
         *
         * @param alignment the alignment for the component
         * @param component the Component to add
         * @return this <code>Group</code>
         * @throws IllegalArgumentException if alignment is not one of
         *         <code>LEADING</code>, <code>TRAILING</code> or
         *         <code>CENTER</code>
         */
        public ParallelGroup add(int alignment, Component component) {
            return add(alignment, component, DEFAULT_SIZE, DEFAULT_SIZE,
                    DEFAULT_SIZE);
        }
        
        /**
         * Adds the specified <code>Component</code>.  Min, pref and max
         * can be absolute values, or they can be one of
         * <code>DEFAULT_SIZE</code> or <code>PREFERRED_SIZE</code>.  For
         * example, the following:
         * <pre>
         *   add(component, PREFERRED_SIZE, PREFERRED_SIZE, 1000);
         * </pre>
         * Forces a max of 1000, with the min and preferred equalling that
         * of the preferred size of <code>component</code>.
         *
         * @param alignment the alignment for the component.
         * @param component the Component to add
         * @param min the minimum size
         * @param pref the preferred size
         * @param max the maximum size
         * @throws IllegalArgumentException if min, pref or max are
         *         not positive and not one of PREFERRED_SIZE or DEFAULT_SIZE.
         * @return this <code>Group</code>
         */
        public ParallelGroup add(int alignment, Component component, int min,
                int pref, int max) {
            ComponentSpring spring = new ComponentSpring(component,
                    min, pref, max);
            spring.setAlignment(alignment);
            return (ParallelGroup)addSpring(spring);
        }
        
        boolean isResizable() {
            return resizable;
        }
        
        int operator(int a, int b) {
            return Math.max(a, b);
        }
        
        int getMinimumSize0(int axis) {
            if (!isResizable()) {
                return getPreferredSize(axis);
            }
            return super.getMinimumSize0(axis);
        }
        
        int getMaximumSize0(int axis) {
            if (!isResizable()) {
                return getPreferredSize(axis);
            }
            return super.getMaximumSize0(axis);
        }
        
        void setSize0(int axis, int origin, int size) {
            int alignment = childAlignment;
            if (alignment == BASELINE) {
                // Not having the axis in the constructor forces us
                // to do this.
                alignment = LEADING;
            }
            for (int counter = 0, max = springs.size(); counter < max;
            counter++) {
                Spring spring = getSpring(counter);
                int sAlignment = spring.getAlignment();
                int springSize = Math.min(size,
                        spring.getMaximumSize(axis));
                if (sAlignment == NO_ALIGNMENT) {
                    sAlignment = alignment;
                }
                switch (sAlignment) {
                    case TRAILING:
                        spring.setSize(axis, origin + size - springSize,
                                springSize);
                        break;
                    case CENTER:
                        spring.setSize(axis, origin +
                                (size - springSize) / 2,springSize);
                        break;
                    default: // LEADING or NO_ALIGNMENT
                        spring.setSize(axis, origin, springSize);
                        break;
                }
            }
        }
        
        void insertAutopadding(int axis, List leadingPadding,
                List trailingPadding, List leading, List trailing,
                boolean insert) {
            for (int counter = 0; counter < springs.size(); counter++) {
                Spring spring = getSpring(counter);
                if (spring instanceof ComponentSpring) {
                    for (int i = 0; i < leadingPadding.size(); i++) {
                        ((AutopaddingSpring)leadingPadding.get(i)).add(
                                (ComponentSpring)spring, axis);
                    }
                    trailing.add(spring);
                } else if (spring instanceof Group) {
                    ((Group)spring).insertAutopadding(axis, leadingPadding,
                            trailingPadding, leading, trailing, insert);
                } else if (spring instanceof AutopaddingSpring) {
                    trailingPadding.add(spring);
                }
            }
        }
    }
    
    
    /**
     * An extension of <code>ParallelGroup</code> that aligns its
     * constituent <code>Spring</code>s along the baseline.
     */
    private class BaselineGroup extends ParallelGroup {
        //
        // This class aligns all components that have a baseline along
        // the baseline.  Any components that do not have a baseline are
        // centered.  Addititionally components that have a baseline
        // will NOT be resized!
        //
        private boolean allSpringsHaveBaseline;
        private int prefAscent;
        private int prefDescent;
        
        BaselineGroup(boolean resizable) {
            super(LEADING, resizable);
            prefAscent = prefDescent = -1;
        }
        
        void setSize(int axis, int origin, int size) {
            if (size == UNSET) {
                prefAscent = prefDescent = -1;
            }
            super.setSize(axis, origin, size);
        }
        
        void setSize0(int axis, int origin, int size) {
            if (axis == HORIZONTAL || prefAscent == -1) {
                super.setSize0(axis, origin, size);
            } else {
                // do baseline layout
                baselineLayout(origin, size);
            }
        }
        
        int calculateSize(int axis, int type) {
            if (springs.size() < 2 || axis != VERTICAL) {
                return super.calculateSize(axis, type);
            }
            if (prefAscent == -1) {
                calculateBaseline();
            }
            if (allSpringsHaveBaseline) {
                return prefAscent + prefDescent;
            }
            return Math.max(prefAscent + prefDescent,
                    super.calculateSize(axis, type));
        }
        
        /**
         * Calculates the baseline of children Springs that have a baseline.
         * The results are stored in prefAscent & prefDescent.  If all
         * child springs have a baseline <code>allSpringsHaveBaseline</code>
         * is set to true.
         */
        private void calculateBaseline() {
            // calculate baseline
            prefAscent = 0;
            prefDescent = 0;
            allSpringsHaveBaseline = true;
            for (int counter = springs.size() - 1; counter >= 0; counter--) {
                Spring spring = getSpring(counter);
                int baseline = -1;
                if (spring instanceof ComponentSpring) {
                    baseline = ((ComponentSpring)spring).getBaseline();
                    if (baseline >= 0) {
                        prefAscent = Math.max(prefAscent, baseline);
                        prefDescent = Math.max(prefDescent, spring.
                                getPreferredSize(VERTICAL) - baseline);
                    }
                }
                if (baseline < 0) {
                    allSpringsHaveBaseline = false;
                }
            }
        }
        
        /**
         * Lays out springs that have a baseline along the baseline.  All
         * others are centered.
         */
        private void baselineLayout(int origin, int size) {
            for (int counter = 0, max = springs.size(); counter < max;
            counter++) {
                Spring spring = getSpring(counter);
                int baseline = -1;
                if (spring instanceof ComponentSpring) {
                    baseline = ((ComponentSpring)spring).getBaseline();
                    if (baseline >= 0) {
                        spring.setSize(VERTICAL, origin + prefAscent -
                                baseline, spring.getPreferredSize(VERTICAL));
                    }
                }
                if (baseline < 0) {
                    // PENDING: do we want to offer up alternative alignments
                    // here?
                    int sSize = Math.min(spring.getMaximumSize(VERTICAL),size);
                    spring.setSize(VERTICAL, origin + (size - sSize) / 2,
                            sSize);
                }
            }
        }
    }
    
    
    /**
     * A Spring representing one axis of a Component.
     * There are three ways to configure this:
     * <ul>
     * <li>Use the pref/min/max from the component
     * <li>Use the pref from the component and fix the min to 0 or max
     *     to a big number.
     * <li>Force the min/max/pref to be a certain value.
     * If the Component's size is to be linked to another components than
     * the min/max/pref all come from the ComponentInfo.
     */
    class ComponentSpring extends Spring {
        private Component component;
        private int origin;
        
        private int min;
        private int pref;
        private int max;
        
        // Baseline for the component.
        private int baseline = -1;
        
        // Whether or not the size has been requested yet.
        private boolean installed;
        
        private ComponentSpring(Component component, int min, int pref,
                int max) {
            this.component = component;

            checkSize(min, pref, max, true);
            
            this.min = min;
            this.max = max;
            this.pref = pref;
            
            getComponentInfo(component);
        }
        
        int getMinimumSize0(int axis) {
            if (isLinked(axis)) {
                return getLinkSize(axis, MIN_SIZE);
            }
            return getMinimumSize1(axis);
        }
        
        int getMinimumSize1(int axis) {
            if (min >= 0) {
                return min;
            }
            if (min == PREFERRED_SIZE) {
                return getPreferredSize1(axis);
            }
            return getSizeAlongAxis(axis, component.getMinimumSize());
        }
        
        int getPreferredSize0(int axis) {
            if (isLinked(axis)) {
                return getLinkSize(axis, PREF_SIZE);
            }
            return Math.max(getMinimumSize(axis), getPreferredSize1(axis));
        }
        
        int getPreferredSize1(int axis) {
            if (pref >= 0) {
                return pref;
            }
            return getSizeAlongAxis(axis, component.getPreferredSize());
        }
        
        int getMaximumSize0(int axis) {
            if (isLinked(axis)) {
                return getLinkSize(axis, MAX_SIZE);
            }
            return Math.max(getMinimumSize(axis), getMaximumSize1(axis));
        }
        
        int getMaximumSize1(int axis) {
            if (max >= 0) {
                return max;
            }
            if (max == PREFERRED_SIZE) {
                return getPreferredSize1(axis);
            }
            return getSizeAlongAxis(axis, component.getMaximumSize());
        }
        
        private int getSizeAlongAxis(int axis, Dimension size) {
            return (axis == HORIZONTAL) ? size.width : size.height;
        }
        
        private int getLinkSize(int axis, int type) {
            ComponentInfo ci = getComponentInfo(component);
            return ci.getSize(axis, type);
        }
        
        void setSize(int axis, int origin, int size) {
            super.setSize(axis, origin, size);
            this.origin = origin;
            if (size == UNSET) {
                baseline = -1;
            }
        }
        
        int getOrigin() {
            return origin;
        }
        
        void setComponent(Component component) {
            this.component = component;
        }
        
        Component getComponent() {
            return component;
        }
        
        int getBaseline() {
            if (baseline == -1 && (component instanceof Component)) {
                Spring horizontalSpring = getComponentInfo(component).
                        horizontalSpring;
                int width;
                if (horizontalSpring != null) {
                    width = horizontalSpring.getSize();
                }
                else {
                    width = component.getPreferredSize().width;
                }
                baseline = Baseline.getBaseline((Component)component,
                        width, getPreferredSize(VERTICAL));
            }
            return baseline;
        }
        
        private boolean isLinked(int axis) {
            return getComponentInfo(component).isLinked(axis);
        }
        
        void installIfNecessary(int axis) {
            if (!installed) {
                installed = true;
                if (axis == HORIZONTAL) {
                    getComponentInfo(component).horizontalSpring = this;
                } else {
                    getComponentInfo(component).verticalSpring = this;
                }
            }
        }
    }

    
    /**
     * Spring representing the preferred distance between two components.
     */
    class PaddingSpring extends Spring {
        private Component source;
        private Component target;
        private int type;
        private boolean canGrow;
        
        PaddingSpring(Component source, Component target, int type,
                boolean canGrow) {
            this.source = source;
            this.target = target;
            this.type = type;
            this.canGrow = canGrow;
        }
        
        int getMinimumSize0(int axis) {
            return getPadding(axis);
        }
        
        int getPreferredSize0(int axis) {
            return getPadding(axis);
        }
        
        int getMaximumSize0(int axis) {
            if (canGrow) {
                return Short.MAX_VALUE;
            }
            return getPadding(axis);
        }
        
        private int getPadding(int axis) {
            int position;
            if (axis == HORIZONTAL) {
                position = 3;//SwingConstants.EAST;
            } else {
                position = 5;//SwingConstants.SOUTH;
            }
            return LayoutStyle.getSharedInstance().getPreferredGap(source,
                    target, type, position, host);
        }
    }
    
    
    /**
     * Spring represented a certain amount of space.
     */
    class GapSpring extends Spring {
        private int min;
        private int pref;
        private int max;
        
        GapSpring(int min, int pref, int max) {
            checkSize(min, pref, max, false);
            this.min = min;
            this.pref = pref;
            this.max = max;
        }
        
        int getMinimumSize0(int axis) {
            if (min == PREFERRED_SIZE) {
                return getPreferredSize(axis);
            }
            return min;
        }
        
        int getPreferredSize0(int axis) {
            return pref;
        }
        
        int getMaximumSize0(int axis) {
            if (max == PREFERRED_SIZE) {
                return getPreferredSize(axis);
            }
            return max;
        }
    }
    
    
    /**
     * Spring reprensenting the distance between any number of sources and
     * targets.  The targets and sources are computed during layout.  An
     * instance of this can either be dynamically created when
     * autocreatePadding is true, or explicitly created by the developer.
     */
    private class AutopaddingSpring extends Spring {
        List sources;
        ComponentSpring source;
        private List matches;
        int size;
        int lastSize;
        private int pref;
        private int max;
        private int type;
        private boolean userCreated;
        
        private AutopaddingSpring() {
            this.pref = PREFERRED_SIZE;
            this.max = PREFERRED_SIZE;
            this.type = LayoutStyle.RELATED;
        }

        AutopaddingSpring(int pref, int max) {
            this.pref = pref;
            this.max = max;
        }
        
        AutopaddingSpring(int type, int pref, int max) {
            this.type = type;
            this.pref = pref;
            this.max = max;
            this.userCreated = true;
        }
        
        public void setSource(ComponentSpring source) {
            this.source = source;
        }
        
        public void setSources(List sources) {
            this.sources = new ArrayList(sources.size());
            this.sources.addAll(sources);
        }
        
        public void setUserCreated(boolean userCreated) {
            this.userCreated = userCreated;
        }
        
        public boolean getUserCreated() {
            return userCreated;
        }
        
        void clear() {
            lastSize = getSize();
            super.clear();
            size = 0;
        }
        
        public void reset() {
            size = 0;
            sources = null;
            source = null;
            matches = null;
        }
        
        public void calculatePadding(int axis) {
            size = 0;
            int maxPadding = 0;
            if (matches != null) {
                LayoutStyle p = LayoutStyle.getSharedInstance();
                // PENDING: rtl
                int position = (axis == HORIZONTAL) ? 3 : 5;
                for (int i = matches.size() - 1; i >= 0; i--) {
                    AutopaddingMatch match = (AutopaddingMatch)matches.get(i);
                    maxPadding = Math.max(maxPadding,
                            calculatePadding(p, position, match.source,
                            match.target));
                }
            }
            if (lastSize != UNSET) {
                size += Math.min(maxPadding, lastSize);
            }
        }
        
        private int calculatePadding(LayoutStyle p, int position,
                ComponentSpring source,
                ComponentSpring target) {
            int delta = target.getOrigin() - (source.getOrigin() +
                    source.getSize());
            if (delta >= 0) {
                int padding;
                if ((source.getComponent() instanceof Component) &&
                        (target.getComponent() instanceof Component)) {
                    padding = p.getPreferredGap((Component)source.getComponent(),
                        (Component)target.getComponent(), type, position, host);
                } else {
                    padding = 10;
                }
                if (padding > delta) {
                    size = Math.max(size, padding - delta);
                }
                return padding;
            }
            return 0;
        }
        
        public void add(ComponentSpring spring, int axis) {
            int oAxis = (axis == HORIZONTAL) ? VERTICAL : HORIZONTAL;
            if (source != null) {
                if (areParallelSiblings(source.getComponent(),
                        spring.getComponent(), oAxis)) {
                    addMatch(source, spring);
                }
            } else {
                Component component = spring.getComponent();
                for (int counter = sources.size() - 1; counter >= 0; counter--){
                    ComponentSpring source = (ComponentSpring)sources.
                            get(counter);
                    if (areParallelSiblings(source.getComponent(),
                            component, oAxis)) {
                        addMatch(source, spring);
                    }
                }
            }
        }
        
        private void addMatch(ComponentSpring source, ComponentSpring target) {
            if (matches == null) {
                matches = new ArrayList(1);
            }
            matches.add(new AutopaddingMatch(source, target));
        }
        
        int getMinimumSize0(int axis) {
            return size;
        }
        int getPreferredSize0(int axis) {
            if (pref == PREFERRED_SIZE || pref == DEFAULT_SIZE) {
                return size;
            }
            return Math.max(size, pref);
        }
        int getMaximumSize0(int axis) {
            if (max >= 0) {
                return Math.max(getPreferredSize(axis), max);
            }
            return size;
        }
  
        String getMatchDescription() {
            return (matches == null) ? "" : matches.toString();
        }
        
        public String toString() {
            return super.toString() + getMatchDescription();
        }
    }
    
    
    /**
     * Represents two springs that should have autopadding inserted between
     * them.
     */
    private static class AutopaddingMatch {
        public ComponentSpring source;
        public ComponentSpring target;

        AutopaddingMatch(ComponentSpring source, ComponentSpring target) {
            this.source = source;
            this.target = target;
        }
        
        private String toString(ComponentSpring spring) {
            return spring.getComponent().getName();
        }

        public String toString() {
            return "[" + toString(source) + "-" + toString(target) + "]";
        }
    }
    
    
    /**
     * An extension of AutopaddingSpring used for container level padding.
     */
    private class ContainerAutopaddingSpring extends AutopaddingSpring {
        private List targets;
        
        ContainerAutopaddingSpring() {
            super();
            setUserCreated(true);
        }

        ContainerAutopaddingSpring(int pref, int max) {
            super(pref, max);
            setUserCreated(true);
        }

        public void add(ComponentSpring spring, int axis) {
            if (targets == null) {
                targets = new ArrayList(1);
            }
            targets.add(spring);
        }

        public void calculatePadding(int axis) {
            LayoutStyle p = LayoutStyle.getSharedInstance();
            int maxPadding = 0;
            size = 0;
            if (targets != null) {
                // Indicates leading
                // PENDING: rtl
                int position = (axis == HORIZONTAL) ? 7 : 1;
                for (int i = targets.size() - 1; i >= 0; i--) {
                    ComponentSpring targetSpring = (ComponentSpring)targets.
                                                                    get(i);
                    int padding = 10;
                    if (targetSpring.getComponent() instanceof Component) {
                        padding = p.getContainerGap(
                                (Component)targetSpring.getComponent(),
                                position, host);
                        maxPadding = Math.max(padding, maxPadding);
                        padding -= targetSpring.getOrigin();
                    } else {
                        maxPadding = Math.max(padding, maxPadding);
                    }
                    size = Math.max(size, padding);
                }
            }
            else {
                // Trailing
                // PENDING: rtl
                int position = (axis == HORIZONTAL) ? 3 : 5;
                if (sources != null) {
                    for (int i = sources.size() - 1; i >= 0; i--) {
                        ComponentSpring sourceSpring = (ComponentSpring)sources.
                                get(i);
                        maxPadding = Math.max(maxPadding,
                                updateSize(p, sourceSpring, position));
                    }
                }
                else if (source != null) {
                    maxPadding = updateSize(p, source, position);
                }
            }
            if (lastSize != UNSET) {
                size += Math.min(maxPadding, lastSize);
            }
        }

        private int updateSize(LayoutStyle p, ComponentSpring sourceSpring,
                int position) {
            int padding = 10;
            if (sourceSpring.getComponent() instanceof Component) {
                padding = p.getContainerGap(
                        (Component)sourceSpring.getComponent(), position,
                        host);
            }
            int delta = Math.max(0, getParent().getSize() -
                    sourceSpring.getSize() - sourceSpring.getOrigin());
            size = Math.max(size, padding - delta);
            return padding;
        }
        
        String getMatchDescription() {
            if (targets != null) {
                return "leading: " + targets.toString();
            }
            if (sources != null) {
                return "trailing: " + sources.toString();
            }
            return "--";
        }
}
    
    /**
     * Tracks the horizontal/vertical Springs for a Component.
     * This class is also used to handle Springs that have their sizes
     * linked.
     */
    private static class ComponentInfo {
        // Component being layed out
        private Component component;
        
        ComponentSpring horizontalSpring;
        ComponentSpring verticalSpring;
        
        // If there is a chain of components that share the
        // same size this will be the master CI.  If this is the
        // masterCI, masterCI == this.
        private ComponentInfo horizontalMaster;
        private ComponentInfo verticalMaster;
        // If we're the master, this is the dependant ComponentInfos
        private List horizontalDependants;
        private List verticalDependants;
        // If we're the masterCI this gives the min/pref/max size of all
        // children.  This is indexed by one of the size operators.  Horizontal
        // first, then vertical.
        private int[] horizontalSizes;
        private int[] verticalSizes;
        
        ComponentInfo(Component component) {
            this.component = component;
            clear();
        }
        
        public void setBounds(Insets insets) {
            int x = 0;
            int y = 0;
            int w = 0;
            int h = 0;
            
            if (horizontalSpring != null) {
                x = horizontalSpring.getOrigin();
                w = horizontalSpring.getSize();
            }
            if (verticalSpring != null) {
                y = verticalSpring.getOrigin();
                h = verticalSpring.getSize();
            }
            /* TODO fix up
             * There is problem, that right (also bottom sometimes) is not
             * well aligned. The magic constant 2 ensures that componets are 2px from
             * bottom and right. The side effect is, that user can not put component to full screen size.
             */
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            if (x + insets.left + w  + insets.right + 2 > d.width){
                w = d.width - (insets.left + insets.right + x + 2);
            }
            if (y + insets.top + h  + insets.bottom + 2> d.height){
                h = d.height - (insets.top + insets.bottom + y + 2);
            }
            //end fix up
            component.setBounds(x + insets.left, y + insets.top, w, h);
        }
        
        public void setComponent(Component component) {
            this.component = component;
            if (horizontalSpring != null) {
                horizontalSpring.setComponent(component);
            }
            if (verticalSpring != null) {
                verticalSpring.setComponent(component);
            }
        }
        
        public Component getComponent() {
            return component;
        }
        
        /**
         * Returns true if this component has its size linked to
         * other components.
         */
        public boolean isLinked(int axis) {
            if (axis == HORIZONTAL) {
                return horizontalMaster != null;
            }
            return (verticalMaster != null);
        }
        
        public ComponentInfo getMasterComponentInfo(int axis) {
            if (axis == HORIZONTAL) {
                if (horizontalMaster == null) {
                    horizontalMaster = this;
                    horizontalDependants = new ArrayList(1);
                    horizontalDependants.add(this);
                    horizontalSizes = new int[3];
                    clear();
                }
                return horizontalMaster;
            } else {
                if (verticalMaster == null) {
                    verticalMaster = this;
                    verticalDependants = new ArrayList(1);
                    verticalDependants.add(this);
                    verticalSizes = new int[3];
                    clear();
                }
                return verticalMaster;
            }
        }
        
        public void addChild(ComponentInfo child, int axis) {
            if (axis == HORIZONTAL) {
                addChild0(child, HORIZONTAL);
            } else {
                addChild0(child, VERTICAL);
            }
        }
        
        private void addChild0(ComponentInfo child, int axis) {
            if (axis == HORIZONTAL) {
                if (child.horizontalMaster == child) {
                    // Child is already marked as a master, subsume all it's
                    // children and reset it's master to us.
                    horizontalDependants.addAll(child.horizontalDependants);
                    child.horizontalDependants = null;
                    child.horizontalSizes = null;
                } else {
                    // Child isn't a master.
                    horizontalDependants.add(child);
                }
                child.horizontalMaster = this;
            } else {
                if (child.verticalMaster == child) {
                    // Child is already marked as a master, subsume all it's
                    // children and reset it's master to us.
                    verticalDependants.addAll(child.verticalDependants);
                    child.verticalDependants = null;
                    child.verticalSizes = null;
                } else {
                    // Child isn't a master.
                    verticalDependants.add(child);
                }
                child.verticalMaster = this;
            }
        }
        
        public void clear() {
            clear(horizontalSizes);
            clear(verticalSizes);
        }
        
        private void clear(int[] sizes) {
            if (sizes != null) {
                for (int counter = sizes.length - 1; counter >= 0; counter--) {
                    sizes[counter] = UNSET;
                }
            }
        }

        // NOTE:
        // There is a lot of code that follows relating to the type of
        // size being requested (min/pref/max).  It's currently ignored
        // because we can't deal with resizable components that are linked.
        // I've left the code in place in case we come up with a way to
        // solve resizing linked components.
        int getSize(int axis, int type) {
            int sizes[] = null;
            List dependants = null;
            if (axis == HORIZONTAL) {
                if (horizontalMaster != this) {
                    return horizontalMaster.getSize(axis, type);
                }
                sizes = horizontalSizes;
                dependants = horizontalDependants;
            } else if (axis == VERTICAL) {
                if (verticalMaster != this) {
                    return verticalMaster.getSize(axis, type);
                }
                sizes = verticalSizes;
                dependants = verticalDependants;
            }
            if (sizes[type] == UNSET) {
                sizes[type] = calcSize(dependants, axis, type);
            }
            return sizes[type];
        }
        
        private int calcSize(List dependants, int axis, int type) {
            int count = dependants.size() - 1;
            int size = getSize(dependants, axis, type, count--);
            while (count >= 0) {
                size = Math.max(size, getSize(dependants, axis, type,count--));
            }
            return size;
        }
        
        private int getSize(List dependants, int axis, int type, int index) {
            ComponentInfo ci = (ComponentInfo)dependants.get(index);
            ComponentSpring spring;
            if (axis == HORIZONTAL) {
                spring = ci.horizontalSpring;
            } else {
                spring = ci.verticalSpring;
            }
            return spring.getPreferredSize1(axis);
//            switch(type) {
//                case MIN_SIZE:
//                    return spring.getMinimumSize1(axis);
//                case PREF_SIZE:
//                    return spring.getPreferredSize1(axis);
//                default:
//                    return spring.getMaximumSize1(axis);
//            }
        }
    }
}
