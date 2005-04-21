/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.explorer.view;

import org.openide.ErrorManager;
import org.openide.awt.HtmlRenderer;
import org.openide.awt.ListPane;
import org.openide.nodes.Node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.KeyboardFocusManager;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;


/** Default renderer for nodes. Can paint either Nodes directly or
 * can be used to paint objects produced by NodeTreeModel, etc.
 *
 * @see org.openide.nodes.Node
 *
 * @author Jaroslav Tulach, Tim Boudreau
 */
public class NodeRenderer extends Object implements TreeCellRenderer, ListCellRenderer {
    private static NodeRenderer instance = null;

    // ********************
    // Support for dragging
    // ********************

    /** Value of the cell with 'drag under' visual feedback */
    private static VisualizerNode draggedOver;

    /** Flag indicating if to use big icons. */
    private boolean bigIcons = false;
    private HtmlRenderer.Renderer renderer = HtmlRenderer.createRenderer();

    /** Creates default renderer. */
    public NodeRenderer() {
    }

    /** Creates renderer.
     * @param bigIcons use big icons if possible
     * @deprecated bigIcons was only used by IconView, and not used by that
     * anymore.  NodeRenderer will automatically detect if the view it's
     * rendering for is an instance of IconView.
     */
    public NodeRenderer(boolean bigIcons) {
        this.bigIcons = bigIcons;
    }

    /** Get the singleton instance used by all explorer views.
     *
     * @deprecated This method no longer returns a shared instance, as it
     *  caused problems with one view setting properties (such as enabled
     *  state) on the renderer and the renderer then being used in its altered
     *  state by a different view.   Views should create their own instance of
     *  NodeRenderer instead.
     */
    public static NodeRenderer sharedInstance() {
        if (instance == null) {
            instance = new NodeRenderer();
        }

        IllegalStateException ise = new IllegalStateException(
                "NodeRenderer." + "sharedInstance() is deprecated.  Create an instance of NodeRenderer" + "instead"
            );
        ErrorManager.getDefault().notify(ErrorManager.WARNING, ise);

        return instance;
    }

    /** Finds the component that is capable of drawing the cell in a tree.
     * @param value value can be either <code>Node</code>
     * or a <code>VisualizerNode</code>.
     * @return component to draw the value
     */
    public Component getTreeCellRendererComponent(
        JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus
    ) {
        VisualizerNode vis = findVisualizerNode(value);

        if (vis == draggedOver) {
            sel = true;
        }

        String text = vis.getHtmlDisplayName();
        boolean isHtml = text != null;

        if (!isHtml) {
            text = vis.getDisplayName();
        }

        //Get our result value - really it is ren, but this call causes
        //it to configure itself with the passed values
        Component result = renderer.getTreeCellRendererComponent(tree, text, sel, expanded, leaf, row, hasFocus);

        result.setEnabled(true);
        renderer.setHtml(isHtml);

        //Do our additional configuration - set up the icon and possibly
        //do some hacks to make it look focused for TreeTableView
        configureFrom(renderer, tree, expanded, sel, vis);

        return result;
    }

    /** This is the only method defined by <code>ListCellRenderer</code>.  We just
     * reconfigure the <code>Jlabel</code> each time we're called.
     */
    public Component getListCellRendererComponent(
        JList list, Object value, int index, boolean sel, boolean cellHasFocus
    ) {
        VisualizerNode vis = findVisualizerNode(value);

        if (vis == draggedOver) {
            sel = true;
        }

        String text = vis.getHtmlDisplayName();
        boolean isHtml = text != null;

        if (!isHtml) {
            text = vis.getDisplayName();
        }

        //Get our result value - really it is ren, but this call causes
        //it to configure itself with the passed values
        Component result = renderer.getListCellRendererComponent(
                list, text, index, sel, cellHasFocus || (value == draggedOver)
            );
        renderer.setHtml(isHtml);
        result.setEnabled(true);

        //Do our additional configuration - set up the icon and possibly
        //do some hacks to make it look focused for TreeTableView
        int iconWidth = configureFrom(renderer, list, false, sel, vis);

        boolean bigIcons = this.bigIcons || list instanceof ListPane;

        if (bigIcons) {
            renderer.setCentered(true);
        } else {
            //Indent elements in a ListView/ChoiceView relative to their position
            //in the node tree.  Only does anything if you've subclassed and
            //overridden createModel().  Does anybody do that?
            if (list.getModel() instanceof NodeListModel && (((NodeListModel) list.getModel()).getDepth() > 1)) {
                int indent = iconWidth * NodeListModel.findVisualizerDepth(list.getModel(), vis);

                renderer.setIndent(indent);
            }
        }

        return result;
    }

    /** Utility method which performs configuration which is common to all of the renderer
     * implementations - sets the icon and focus properties on the renderer
     * from the VisualizerNode.
     *
     */
    private int configureFrom(
        HtmlRenderer.Renderer ren, Container target, boolean useOpenedIcon, boolean sel, VisualizerNode vis
    ) {
        Icon icon = vis.getIcon(useOpenedIcon, bigIcons);

        if (icon.getIconWidth() > 0) {
            //Max annotated icon width is 24, so to have all the text and all
            //the icons come out aligned, set the icon text gap to the difference
            //plus a two pixel margin
            ren.setIconTextGap(24 - icon.getIconWidth());
        } else {
            //If the icon width is 0, fill the space and add in
            //the extra two pixels so the node names are aligned (btw, this
            //does seem to waste a frightful amount of horizontal space in
            //a tree that can use all it can get)
            ren.setIndent(26);
        }

        ren.setIcon(icon);

        if (target instanceof TreeTable.TreeTableCellRenderer) {
            TreeTable tt = ((TreeTable.TreeTableCellRenderer) target).getTreeTable();
            ren.setParentFocused(tt.hasFocus() || tt.isEditing());
        }

        return (icon.getIconWidth() == 0) ? 24 : icon.getIconWidth();
    }

    /** Utility method to find a visualizer node for the object passed to
     * any of the cell renderer methods as the value */
    private static final VisualizerNode findVisualizerNode(Object value) {
        VisualizerNode vis = (value instanceof Node) ? VisualizerNode.getVisualizer(null, (Node) value)
                                                     : (VisualizerNode) value;

        if (vis == null) {
            vis = VisualizerNode.EMPTY;
        }

        return vis;
    }

    /** DnD operation enters. Update look and feel to the 'drag under' state.
     * @param dragged the value of cell which should have 'drag under' visual feedback
         */
    static void dragEnter(Object dragged) {
        draggedOver = (VisualizerNode) dragged;
    }

    /** DnD operation exits. Revert to the normal look and feel. */
    static void dragExit() {
        draggedOver = null;
    }
}
