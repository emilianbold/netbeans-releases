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
package org.openide.explorer.view;

import org.openide.awt.ListPane;
import org.openide.explorer.*;
import org.openide.nodes.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import javax.swing.*;


/* TODO:
 - improve cell renderer (two lines of text or hints)
 - better behaviour during scrolling (ListPane)
 - external selection bug (BUG ID: 01110034)
 -
 - XXX if doing anything with this class other than deleting it, rewrite it to use a JTable - that would be
 - much more sensible and scalable.  -Tim
 -
*/

/** A view displaying {@link Node}s as icons.
 * <p>
 * This class is a <q>view</q>
 * to use it properly you need to add it into a component which implements
 * {@link Provider}. Good examples of that can be found 
 * in {@link ExplorerUtils}. Then just use 
 * {@link Provider#getExplorerManager} call to get the {@link ExplorerManager}
 * and control its state.
 * </p>
 * <p>
 * There can be multiple <q>views</q> under one container implementing {@link Provider}. Select from
 * range of predefined ones or write your own:
 * </p>
 * <ul>
 *      <li>{@link org.openide.explorer.view.BeanTreeView} - shows a tree of nodes</li>
 *      <li>{@link org.openide.explorer.view.ContextTreeView} - shows a tree of nodes without leaf nodes</li>
 *      <li>{@link org.openide.explorer.view.ListView} - shows a list of nodes</li>
 *      <li>{@link org.openide.explorer.view.IconView} - shows a rows of nodes with bigger icons</li>
 *      <li>{@link org.openide.explorer.view.ChoiceView} - creates a combo box based on the explored nodes</li>
 *      <li>{@link org.openide.explorer.view.TreeTableView} - shows tree of nodes together with a set of their {@link Property}</li>
 *      <li>{@link org.openide.explorer.view.MenuView} - can create a {@link JMenu} structure based on structure of {@link Node}s</li>
 * </ul>
 * <p>
 * All of these views use {@link ExplorerManager#find} to walk up the AWT hierarchy and locate the
 * {@link ExplorerManager} to use as a controler. They attach as listeners to
 * it and also call its setter methods to update the shared state based on the
 * user action. Not all views make sence together, but for example
 * {@link org.openide.explorer.view.ContextTreeView} and {@link org.openide.explorer.view.ListView} were designed to complement
 * themselves and behaves like windows explorer. The {@link org.openide.explorer.propertysheet.PropertySheetView}
 * for example should be able to work with any other view.
 * </p>
 *
 * @author   Jaroslav Tulach
 */
public class IconView extends ListView implements Externalizable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -9129850245819731264L;

    public IconView() {
    }

    /** Creates the list that will display the data.
    */
    protected JList createList() {
        JList list = new ListPane() {
                /**
                 * Overrides JComponent's getToolTipText method in order to allow
                 * renderer's tips to be used if it has text set.
                 * @param event the MouseEvent that initiated the ToolTip display
                 */
                public String getToolTipText(MouseEvent event) {
                    if (event != null) {
                        Point p = event.getPoint();
                        int index = locationToIndex(p);

                        if (index >= 0) {
                            VisualizerNode v = (VisualizerNode) getModel().getElementAt(index);
                            String tooltip = v.getShortDescription();
                            String displayName = v.getDisplayName();

                            if ((tooltip != null) && !tooltip.equals(displayName)) {
                                return tooltip;
                            }
                        }
                    }

                    return null;
                }
            };

        list.setCellRenderer(new NodeRenderer());

        return list;
    }
}
