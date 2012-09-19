/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import javax.swing.JComponent;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Model of an inspected web-page.
 *
 * @author Jan Stola
 */
public abstract class PageModel {
    /** Name of the property that is fired when a new document is loaded into the inspected browser pane. */
    public static final String PROP_DOCUMENT = "document"; // NOI18N
    /** Name of the property that is fired when the set of selected elements is changed. */
    public static final String PROP_SELECTED_NODES = "selectedNodes"; // NOI18N
    /** Name of the property that is fired when the set of highlighted nodes is changed. */
    public static final String PROP_HIGHLIGHTED_NODES = "highlightedNodes"; // NOI18N
    /** Name of the property that is fired when the selection mode is switched on/off. */
    public static final String PROP_SELECTION_MODE = "selectionMode"; // NOI18N
    /** Name of the property that is fired when the synchronization of the selection is switched on/off. */
    public static final String PROP_SYNCHRONIZE_SELECTION = "synchronizeSelection"; // NOI18N
    /** Property change support. */
    private PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);

    /**
     * Returns the document node.
     * 
     * @return document node.
     */
    public abstract Node getDocumentNode();

    /**
     * Returns the document URL.
     * 
     * @return document URL.
     */
    public abstract String getDocumentURL();

    /**
     * Sets the selected nodes.
     * 
     * @param nodes nodes to select in the page.
     */
    public abstract void setSelectedNodes(List<? extends Node> nodes);

    /**
     * Returns selected nodes.
     * 
     * @return selected nodes.
     */
    public abstract List<? extends Node> getSelectedNodes();

    /**
     * Sets the highlighted nodes.
     * 
     * @param nodes highlighted nodes.
     */
    public abstract void setHighlightedNodes(List<? extends Node> nodes);

    /**
     * Switches the selection mode on or off.
     * 
     * @param selectionMode determines whether the selection mode should
     * be switched on or off.
     */
    public abstract void setSelectionMode(boolean selectionMode);

    /**
     * Determines whether the selection mode is switched on or off.
     * 
     * @return {@code true} when the selection mode is switched on,
     * returns {@code false} otherwise.
     */
    public abstract boolean isSelectionMode();

    /**
     * Sets whether the selection between the IDE and the browser pane should
     * be synchronized or not.
     *
     * @param synchronizeSelection determines whether the selection should
     * be synchronized or not.
     */
    public abstract void setSynchronizeSelection(boolean synchronizeSelection);

    /**
     * Determines whether the selection between the IDE and the browser pane
     * should be synchronized or not.
     *
     * @return {@code true} when the selection should be synchronized,
     * returns {@code false} otherwise.
     */
    public abstract boolean isSynchronizeSelection();

    /**
     * Returns highlighted nodes.
     * 
     * @return highlighted nodes.
     */
    public abstract List<? extends Node> getHighlightedNodes();

    /**
     * Returns CSS Styles view for this page.
     *
     * @return CSS Styles view for this page.
     */
    public abstract CSSStylesView getCSSStylesView();

    /**
     * Disposes this page model.
     */
    protected abstract void dispose();

    /**
     * Adds a property change listener.
     * 
     * @param listener listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener.
     * 
     * @param listener listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Fires the specified property change.
     * 
     * @param propName name of the property.
     * @param oldValue old value of the property or {@code null}.
     * @param newValue new value of the property or {@code null}.
     */
    protected void firePropertyChange(String propName, Object oldValue, Object newValue) {
        propChangeSupport.firePropertyChange(propName, oldValue, newValue);
    }

    /**
     * CSS Styles view.
     */
    public static interface CSSStylesView {

        /**
         * Returns the visual representation of CSS Styles.
         *
         * @return visual representation of CSS Styles.
         */
        JComponent getView();

        /**
         * Returns the lookup of this view. This lookup will be included
         * in the lookup of the enclosing {@code TopComponent}.
         * 
         * @return lookup of this view.
         */
        Lookup getLookup();

        /**
         * The enclosing {@code TopComponent} has been activated.
         */
        void activated();

        /**
         * The enclosing {@code TopComponent} has been deactivated.
         */
        void deactivated();

    }

}
