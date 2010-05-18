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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.designer;


import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.Designer.Box;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerEvent;

import org.netbeans.modules.visualweb.api.designer.DomProviderService.ResizeConstraint;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition.Bias;
import org.netbeans.modules.visualweb.css2.BoxType;
import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.css2.ExternalDocumentBox;
import org.netbeans.modules.visualweb.css2.ModelViewMapper;
import org.netbeans.modules.visualweb.css2.PageBox;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;

import org.openide.ErrorManager;
import org.openide.nodes.Node;

import org.w3c.dom.Element;

/**
 * This class is the central point for handling selections, mouse interactions,
 * keyboard interactions, etc., with the page editor / the WYSIWYG designer.
 * It for example listens to mouse events and decides whether this translates
 * into a change in selected components, or a drag of a component, etc.
 *
 * <p>
 * This class manages selections in the page designer.
 *
 * <p>
 * @todo Rename this class to "InteractionManager", formalize the remaining interactions
 *  that aren't using the Interaction interface yet (like InlineEditors, selection cycling),
 *  pull the selection-set specific code into a separate SelectionHandler (set & painter),
 *  pull the utility methods into the Utilities class.
 * @todo Handle getComponentBounds replacement to getComponentRectangles
 *   later! And speed up caching of component rectangle list!
 * @todo Get rid of all traces of "View" in the selection handling!
 * @todo Remove the inlineEditor (finish it) if the user deletes a component! Even via the
 *    app outline! (perhaps only if it's the component we're inline editing or some child of it)
 * <p>
 *
 * @author  Tor Norbye
 */
public class SelectionManager {
    // The source file is organized into groups of related functionality:
    // mouse handling, painting, selection manipulation, etc.
    // Please keep this in mind when editing the file - don't just add
    // methods at the end of the file for example.
    private static final int NODE_REFRESH_DELAY = 300; // ms

    // Dreamweaver-like hierarchy bar
    static final boolean PAINT_SELECTION_HIERARCHY =
        System.getProperty("designer.paintSelHierarchy") != null; // NOI18N

    /** Width/height of the selection handles - these are drawn at
     * all four corners and the middle of all four edges */
    private static final int BARSIZE = 5;
    private static final int SELECTIONVIEW_LEFT = 1;
    private BasicStroke selStroke;
    private final WebForm webform;
//    private DocumentComp documentComponent = null;

//    /** Set of selected objects. Contains FormObjects only. */
//    private Set<FormObject> selected = new HashSet<FormObject>();
    
//    private final Set<SelectedComponent> selectedComponents = new HashSet<SelectedComponent>();
    private final List<SelectedComponent> selectedComponents = new ArrayList<SelectedComponent>();

//    /** The "primary" selected object - e.g. you may have selected
//        10 components, but the most recently clicked one is the
//        primary item. Right clicking on an item makes it the primary. */
//    private MarkupDesignBean primary = null;
//    /** Previous selection, in case new selected item is an ancestor
//     * of the previously selected item
//     */
//    private MarkupDesignBean leaf = null;
    /** The "primary" selected object - e.g. you may have selected
        10 components, but the most recently clicked one is the
        primary item. Right clicking on an item makes it the primary. */
    private Element primary = null;
    /** Previous selection, in case new selected item is an ancestor
     * of the previously selected item
     */
    private Element leaf = null;
    
    
    private AffineTransform transform = new AffineTransform();
    private boolean paintBeanIcon = false;
    int selectionViewPos = Integer.MAX_VALUE;
    private Timer refreshTimer;
//    private Node[] prevNodes = null;

//    /** When set, ignore selection requests */
//    private boolean ignoreSelection;

    SelectionManager(WebForm webform) {
        this.webform = webform;

        webform.getPane();
    }

    /** Clear out the selection - after this has run,
     * no components are selected
     * @param update If true, update property sheets etc. to reflect
     *  the new selection.
     */
    public void clearSelection(boolean update) {
        selectionViewPos = Integer.MAX_VALUE;
        leaf = null;

//        if ((selected == null) || (selected.size() == 0)) {
        if (selectedComponents.isEmpty()) {
            if (update) { // XXX why are we doing this? It should already be correct
//                updateSelection();
                updateNodes();

                // is a repaint needed?
            }

            return;
        }

//        selected = new HashSet<FormObject>();
        selectedComponents.clear();
        
        primary = null;

        if (update) {
//            updateSelection();
            updateNodes();
//            webform.getTopComponent().disableCutCopyDelete(); // not true for paste
//            webform.tcDisableCutCopyDelete();
            webform.getPane().repaint();
        }
    }

    /** XXX Altered copy of clear selection, to immediatelly update the nodes.
     * FIXME Get rid of the delayed node updates. */
    public void clearSelectionImmediate() {
        selectionViewPos = Integer.MAX_VALUE;
        leaf = null;

//        if ((selected == null) || (selected.size() == 0)) {
        if (selectedComponents.isEmpty()) {
            // XXX why are we doing this? It should already be correct
            updateSelectionImmediate();

            // is a repaint needed?
            return;
        }

//        selected = new HashSet<FormObject>();
        selectedComponents.clear();
        primary = null;

        updateSelectionImmediate();
//        webform.getTopComponent().disableCutCopyDelete(); // not true for paste
//        webform.tcDisableCutCopyDelete();
        webform.getPane().repaint();
    }
    
//    /** Update IDE to visually reflect the current selection */
//    public void updateSelection() {
//        updateNodes();
//        
////        updateSelectionInOldOutline();
//    }
    
    public void updateSelectionImmediate() {
        updateNodesImmediate();
        
//        updateSelectionInOldOutline();
    }

//    // XXX Get rid of this method after getting rid of old outline.
//    private void updateSelectionInOldOutline() {
//        // Sync document outline view
//        if ((selected == null) || (selected.size() == 0)) {
////            OutlineTopComp.getInstance().selectBeans(null);
//        } else {
//            DesignBean[] lbs = new DesignBean[selected.size()];
//            Iterator it = selected.iterator();
//            int index = 0;
//
//            while (it.hasNext()) {
//                FormObject fob = (FormObject)it.next();
//                lbs[index++] = fob.component;
//            }
//
//            try {
//                ignoreSelection = true;
////                OutlineTopComp.getInstance().selectBeans(lbs);
//            } finally {
//                ignoreSelection = false;
//            }
//        }
//
//        // TODO: Move enable/disable cut,copy,delete into this method!
//    }

    /** Synchronize the selection with the model. This should be called
     * when DesignBeans may have changed underneath us, for example because
     * the user modified the backing file, and it's reparsed by insync.
     * At this point the DesignBeans we're pointing to in our selection set
     * may be stale, so for each selected bean check if it's still live
     * and if not, find its new representative if any.
     * @param update If true, update property sheets etc. to reflect
     *  the new selection.
     */
    public void syncSelection(boolean update) {
//        if ((selected == null) || (selected.size() == 0)) {
        if (selectedComponents.isEmpty()) {
            return;
        }

        leaf = null;
        selectionViewPos = Integer.MAX_VALUE;
        primary = null;

//        LiveUnit unit = webform.getModel().getLiveUnit();

//        Iterator it = selected.iterator();
//        List<FormObject> remove = new ArrayList<FormObject>();

//        while (it.hasNext()) {
//            FormObject fo = (FormObject)it.next();
//        for (FormObject fo : selected) {
//
//            if (fo.component != null) {
//                // Gotta find the new DesignBean for bean fo.component
//                MarkupDesignBean oldBean = fo.component;
//                // XXX Big architectural flaw, the model itself is not consistent (doesn't fire changes about itself),
//                // one needs to get 'newer' beans from the 'old' ones.
//                // TODO find out better solution.
////                fo.component = (MarkupDesignBean)unit.getBeanEquivalentTo(oldBean);
//                fo.component = webform.getMarkupDesignBeanEquivalentTo(oldBean);
//
//                if (fo.component == null) {
//                    remove.add(fo);
//                }
//            }
//        }
        
        List<SelectedComponent> remove = new ArrayList<SelectedComponent>();
        for (SelectedComponent sc : selectedComponents) {
            if (sc.componentRootElement != null) {
                // Gotta find the new DesignBean for bean fo.component
                Element oldComponentRootElement = sc.componentRootElement;
                // XXX Big architectural flaw, the model itself is not consistent (doesn't fire changes about itself),
                // one needs to get 'newer' beans from the 'old' ones.
                // TODO find out better solution.
//                fo.component = (MarkupDesignBean)unit.getBeanEquivalentTo(oldBean);
                sc.componentRootElement = webform.getComponentRootElementEquivalentTo(oldComponentRootElement);

                if (sc.componentRootElement == null) {
                    remove.add(sc);
                }
            }
        }


        // Remove the items in the selection hashmap that we couldn't find
        // a new DesignBean for. Not done during the iteration since it confuses
        // the iterator.
//        it = remove.iterator();
//
//        while (it.hasNext()) {
//        for (FormObject fo : remove) {
////            FormObject fo = (FormObject)it.next();
//            selected.remove(fo);
//        }
        for (SelectedComponent sc : remove) {
            selectedComponents.remove(sc);
        }

        webform.getPane().repaint(); // XXX should this only be done for update=true?

        if (update) {
//            updateSelection();
            updateNodes();

            // XXX why are we disabling cut copy paste? That depends on the
            // selection, doesn't it?
//            webform.getTopComponent().disableCutCopyDelete(); // not true for paste
//            webform.tcDisableCutCopyDelete();
        }
    }

//    /** After a sync, try to position the caret close to where it was,
//     * and if not, hide the caret
//     * Hmmmmmm shouldn't hide the caret in flow mode!
//     */
//    public void syncCaret() {
//        // Fix the caret
//        DesignerCaret dc = webform.getPane().getCaret();
//
//        if ((dc != null) && (dc.getDot() != Position.NONE)) {
//            /*
//            LiveUnit unit = webform.getModel().getLiveUnit();
//            org.w3c.dom.Node n = dc.getDot().getNode();
//            Element newElement = null;
//            while (n != null) {
//                if (n instanceof XhtmlElement) {
//                    XhtmlElement e = (XhtmlElement)n;
//                    if (e.getDesignBean() != null) {
//                        DesignBean newBean = unit.getBeanByName(e.getDesignBean().getInstanceName());
//                        if (newBean != null) {
//                            newElement = FacesSupport.getElement(newBean);
//                            break;
//                        }
//                    }
//                }
//                n = n.getParentNode();
//            }
//            if (newElement != null) {
//                Position newPos = webform.getMapper().
//                // Can't do this because this will update the range
//                // in two steps, and as soon as one of the end points
//                // is in a different
//                //getPane().setCaretPosition(newPos);
//
//                dc = getPane().getPaneUI().createCaret();
//                getPane().setCaret(dc);
//                getPane().setCaretPosition(newPos);
//            } else if (!webform.getDocument().isGridMode()) {
//                getPane().showCaretAtBeginning();
//            } else {
//                getPane().setCaret(null);
//            }
//             */
//
//            // DONE IN PAGEBOX.layout!
//            //            dc.setDot(Position.NONE); // Ensure range gets detached since we could be in a new DOM
//            //            getPane().showCaretAtBeginning();
//        }
//    }

    /** Add the given view/component to the selection.
     * @param update If true, update property sheets etc. to reflect
     *  the new selection.
     */
    public void addSelected(/*MarkupDesignBean component*/Element componentRootElement, boolean update) {
//        boolean wasEmpty = ((selected == null) || (selected.size() == 0));
        boolean wasEmpty = selectedComponents.isEmpty();
//        FormObject fo = new FormObject();
//        fo.component = component;
        SelectedComponent sc = new SelectedComponent();
        sc.componentRootElement = componentRootElement;
        
//        primary = component;
        primary = sc.componentRootElement;

//        if (fo.component == null) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new NullPointerException("MarkupDesignBean is null!")); // NOI18N
//        }
        if (sc.componentRootElement == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new NullPointerException("ComponentRootElement is null!")); // NOI18N
        }

//        fo.resizeConstraints = Resizer.getResizeConstraints(webform, component);
        sc.resizeConstraints = Resizer.getResizeConstraints(webform, componentRootElement);

//        selected.add(fo);
//        leaf = fo.component;
        if (!selectedComponents.contains(sc)) {
            selectedComponents.add(sc);
        }
//        leaf = component;
        leaf = sc.componentRootElement;

        if (update) {
//            updateSelection();
            updateNodes();
        }

        if (wasEmpty) {
            // Enable Cut, Copy, Delete
//            webform.getTopComponent().enableCutCopyDelete();
//            webform.tcEnableCutCopyDelete();
        }
    }

    /** Set the selection to the given component.
     * Attempt to preserve the current component as a leaf, provided
     * the new component to be selected is an ancestor of the current leaf.
     */
    public void setSelected(/*MarkupDesignBean component*/Element componentRootElement, boolean update) {
//        MarkupDesignBean oldLeaf = leaf;
//        primary = component;
        Element oldLeaf = leaf;
        primary = componentRootElement;
        
        clearSelection(false);
//        addSelected(component, update);
        addSelected(componentRootElement, update);

        // Is the new component an ancestor of the old leaf?
//        MarkupDesignBean component = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
        if (PAINT_SELECTION_HIERARCHY) {
//            DesignBean curr = oldLeaf;
//            DesignBean curr = WebForm.getDomProviderService().getMarkupDesignBeanForElement(oldLeaf);
            Element curr = oldLeaf;

            while (curr != null) {
//                if (component == curr) {
                if (componentRootElement == curr) {
                    // Yes!
                    leaf = oldLeaf;

                    return;
                }

//                curr = curr.getBeanParent();
                curr = webform.getDomProviderService().getParentComponent(curr);
            }

            // No. This is the new leaf.
//            assert leaf == component;
//            if (leaf != WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean(component)) {
            if (leaf != componentRootElement) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Leaf is different from expected, leaf=" + leaf
                        + ", expected=" + componentRootElement)); // NOI18N
            }
        }
    }

    /** Remove the given view/component from the selection.
     * @param update If true, update property sheets etc. to reflect
     *  the new selection.
     */
    public void removeSelected(/*MarkupDesignBean component*/Element componentRootElement, boolean update) {
//        boolean wasEmpty = ((selected == null) || (selected.size() == 0));
        boolean wasEmpty = selectedComponents.isEmpty();

        if (!wasEmpty) {
//            Iterator it = selected.iterator();
//
//            while (it.hasNext()) {
//                FormObject fo = (FormObject)it.next();
//            for (FormObject fo : selected) {
//
//                if (fo.component == component) {
//                    selected.remove(fo);
//
//                    break;
//                }
//            }
            for (SelectedComponent sc : selectedComponents) {
                if (sc.componentRootElement == componentRootElement) {
                    selectedComponents.remove(sc);
                    break;
                }
            }
        }

//        if (component == primary) {
        if (componentRootElement == primary) {
            primary = null;

//            if (selected.size() > 1) {
            if (selectedComponents.size() > 1) {
                pickPrimary();
            }
        }

        if (update) {
//            updateSelection();
            updateNodes();
        }

//        if (wasEmpty && (selected.size() == 0)) { // removed last component
        if (wasEmpty && selectedComponents.isEmpty()) { // removed last component
//            webform.getTopComponent().disableCutCopyDelete(); // not true for paste
//            webform.tcDisableCutCopyDelete();
        }
    }

    /** Return true iff the given view is in the set of selected views.
        @param component The view to check. May not be null.
        @return true iff the component is currently selected.
    */
    public boolean isSelected(/*DesignBean component*/Element componentRootElement) {
//        if ((selected == null) || (selected.size() == 0)) {
        if (componentRootElement == null || selectedComponents.isEmpty()) {
            return false;
        }

//        Iterator it = selected.iterator();
//
//        while (it.hasNext()) {
//            FormObject fo = (FormObject)it.next();
//        for (FormObject fo : selected) {
//            if (fo.component == component) {
//                return true;
//            }
//        }
        for (SelectedComponent sc : selectedComponents) {
            if (sc.componentRootElement == componentRootElement) {
                return true;
            }
        }

        return false;
    }

    /** Return true iff the given view is in the set of selected views
     * or is a child of a selected view.
     * @param box The box to check. May not be null.
     * @return true iff the box or one of its ancestors is currently selected.
     */
    public boolean isBelowSelected(CssBox box) {
        if (box.getBoxType() == BoxType.TEXT) {
            box = box.getParent();
        }

//        if ((box.getDesignBean() != null) && isSelected(box.getDesignBean())) {
//        MarkupDesignBean markupDesignBean = CssBox.getMarkupDesignBeanForCssBox(box);
//        if ((markupDesignBean != null) && isSelected(markupDesignBean)) {
        Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
        if (isSelected(componentRootElement)) {
            return true;
        }

        return getSelectedAncestor(box) != null;
    }

    /** Check to see if any ancestors of the given view (or the view
     * itself) is in the selection set, and if so, return it. This will
     * return the closest (e.g. furthest down the element tree) match.
     *
     *   @param box The box to check. May not be null.
     *   @return The selected view or ancestor of the box
     */
    public CssBox getSelectedAncestor(CssBox box) {
        if (box != null) {
            // Skip the immediate box
            box = box.getParent();
        }

        while (box != null) {
//            if ((box.getDesignBean() != null) && isSelected(box.getDesignBean())) {
//            MarkupDesignBean markupDesignBean = CssBox.getMarkupDesignBeanForCssBox(box);
//            if ((markupDesignBean != null) && isSelected(markupDesignBean)) {
            Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
            if (isSelected(componentRootElement)) {
                return box;
            }

            box = box.getParent();
        }

        return null;
    }

    /** Return the first container component found in the selection.
     * If no direct selected component is a container, return the first
     * container ancestor for the first selected item.
     */
    public /*DesignBean*/Element getSelectedContainer() {
//        Iterator it = selected.iterator();
//
//        while (it.hasNext()) {
//            FormObject fo = (FormObject)it.next();
//        for (FormObject fo : selected) {
//            DesignBean lb = fo.component;
//
//            if (lb.isContainer()) {
//                return lb;
//            }
//        }
        for (SelectedComponent sc : selectedComponents) {
//            DesignBean lb = WebForm.getDomProviderService().getMarkupDesignBeanForElement(sc.componentRootElement);
//            if (lb.isContainer()) {
            if (webform.getDomProviderService().isContainerTypeComponent(sc.componentRootElement)) {
//                return lb;
                return sc.componentRootElement;
            }
        }

//        it = selected.iterator();
//
//        while (it.hasNext()) {
//            FormObject fo = (FormObject)it.next();
//        for (FormObject fo : selected) {
//            DesignBean lb = fo.component;
//
//            if (lb.getBeanParent() != null) {
//                return lb.getBeanParent();
//            }
//        }
        for (SelectedComponent sc : selectedComponents) {
//            DesignBean lb = WebForm.getDomProviderService().getMarkupDesignBeanForElement(sc.componentRootElement);
//            if (lb.getBeanParent() != null) {
            Element parentComponentRootElement = webform.getDomProviderService().getParentComponent(sc.componentRootElement);
            if (parentComponentRootElement != null) {
//                return lb.getBeanParent();
                return parentComponentRootElement;
            }
        }

        return null;
    }

    /** Returned the first positioned element in the selection. Gets the component root element (rendered element). */
    Element getPositionElement() {
//        Iterator it = selected.iterator();
//
//        while (it.hasNext()) {
//            FormObject fo = (FormObject)it.next();
//        for (FormObject fo : selected) {
////            CssBox box = webform.getMapper().findBox(fo.component);
//            CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), fo.component);
//
//            if ((box != null) && box.getBoxType().isPositioned()) {
////                return box.getDesignBean().getElement();
//                // XXX Shouldn't be here the fo.component? See above.
//                return CssBox.getMarkupDesignBeanForCssBox(box).getElement();
//            }
//        }
        for (SelectedComponent sc : selectedComponents) {
//            CssBox box = webform.getMapper().findBox(fo.component);
            CssBox box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), sc.componentRootElement);

            if ((box != null) && box.getBoxType().isPositioned()) {
//                return box.getDesignBean().getElement();
                // XXX Shouldn't be here the fo.component? See above.
//                return CssBox.getMarkupDesignBeanForCssBox(box).getElement();
                return CssBox.getElementForComponentRootCssBox(box);
            }
        }

        return null;
    }

    /** Select all components on the designer surface (except components
     * that are children of other components, and except components that
     * are "special", such as Form/Body/Head/Html, etc.
     */
    public void selectAll() {
        //DesignBean root = webform.getModel().getRootBean();
//        RaveElement element = webform.getBody();
        Element element = webform.getHtmlBody();
//        DesignBean root = element.getDesignBean();
//        DesignBean root = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
//        DesignBean root = WebForm.getDomProviderService().getMarkupDesignBeanForElement(element);
//        
//        if (root == null) {
//            return;
//        }
        if (element == null) {
            return;
        }
        
        // XXX #109439 The same components appeared more times in the selection.
        clearSelection(false);

        Element[] children = webform.getDomProviderService().getChildComponents(element);
//        for (int i = 0, n = root.getChildBeanCount(); i < n; i++) {
//            DesignBean child = root.getChildBean(i);
        for (Element child : children) {

//            if (child instanceof MarkupDesignBean) {
//                selectAll((MarkupDesignBean)child);
//                selectAll(WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)child));
//            }
            selectAll(child);
        }

//        // We should not select the two special <br> components that are added
//        // to flow-positioned documents: the last break in the form component,
//        // and the first br in the body component. These are there to ensure that
//        // there are caret-viable lines in the document.
//        if (!webform.isGridMode()) {
////            if ((root.getChildBeanCount() > 0) &&
////                    ((MarkupDesignBean)root.getChildBean(0)).getElement().getTagName().equals(HtmlTag.BR.name)) {
//            if (children.length > 0 && HtmlTag.BR.name.equals(children[0].getTagName())) {
////                removeSelected((MarkupDesignBean)root.getChildBean(0), false);
////                removeSelected(WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)root.getChildBean(0)), false);
//                removeSelected(children[0], false);
//            }
//
//            // Look for last bean in the default parent
////            MarkupDesignBean defaultBean =
////                FacesSupport.getDesignBean(webform.getModel().getFacesUnit().getDefaultParent()
////                                                  .getElement());
////            MarkupDesignBean defaultBean =
//////                    WebForm.getDomProviderService().getMarkupDesignBeanForElement(webform.getModel().getFacesUnit().getDefaultParent()
//////                                                  .getElement());
////                    WebForm.getDomProviderService().getMarkupDesignBeanForElement(
////                        webform.getDefaultParentMarkupBeanElement());
//            
////            Element defaultElement = webform.getDefaultParentMarkupBeanElement();
//            Element defaultElement = webform.getDefaultParentComponent();
//            
////            if (defaultBean != null) {
////                int n = defaultBean.getChildBeanCount();
////
////                if ((n > 0) &&
////                        ((MarkupDesignBean)defaultBean.getChildBean(n - 1)).getElement().getTagName()
////                             .equals(HtmlTag.BR.name)) {
//////                    removeSelected((MarkupDesignBean)defaultBean.getChildBean(n - 1), false);
////                    removeSelected(WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)defaultBean.getChildBean(n - 1)), false);
////                }
////            }
//            if (defaultElement != null) {
//                Element[] defaultElementChildren = WebForm.getDomProviderService().getChildComponents(defaultElement);
//                int n = defaultElementChildren.length;
//                if ((n > 0) && HtmlTag.BR.name.equals(defaultElementChildren[n-1].getTagName())) {
//                    removeSelected(defaultElementChildren[n - 1], false);
//                }
//            }
//        }
        if (!webform.isGridMode()) {
            // XXX Just the same what did the original code, but without JSF specific API.
            if (!selectedComponents.isEmpty()) {
                Element firstSelectedComponent = selectedComponents.get(0).componentRootElement;
                Element lastSelectedComponent = selectedComponents.get(selectedComponents.size() - 1).componentRootElement;
                if (firstSelectedComponent != null && HtmlTag.BR.name.equals(firstSelectedComponent.getTagName())) {
                    removeSelected(firstSelectedComponent, false);
                }
                if (lastSelectedComponent != null && HtmlTag.BR.name.equals(lastSelectedComponent.getTagName())) {
                    removeSelected(lastSelectedComponent, false);
                }
            }
        }

//        updateSelection();
        updateNodes();

//        if (selected.size() > 0) {
        if (!selectedComponents.isEmpty()) {
//            webform.getTopComponent().enableCutCopyDelete();
//            webform.tcEnableCutCopyDelete();
        }

        webform.getPane().repaint();
    }

    private void selectAll(/*MarkupDesignBean bean*/Element componentRootElement) {
//        if (!FacesSupport.isSpecialBean(/*webform, */bean)) {
//        if (!Util.isSpecialBean(bean)) {
//        MarkupDesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
        if (!webform.getDomProviderService().isSpecialComponent(componentRootElement)) {
//            addSelected(bean, false);
            addSelected(componentRootElement, false);
        }

//        for (int i = 0, n = bean.getChildBeanCount(); i < n; i++) {
//            DesignBean child = bean.getChildBean(i);
//
//            if (child instanceof MarkupDesignBean) {
////                selectAll((MarkupDesignBean)child);
//                selectAll(WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)child));
//            }
//        }
        Element[] children = webform.getDomProviderService().getChildComponents(componentRootElement);
        for (Element child : children) {
            selectAll(child);
        }
    }

    /** Select all views that intersect the given bound rectangle.
     * @todo Decide if views should be entirely within the bounds, or
     * merely intersect the rectangle
     * @param pane Pane to look for selection in
     * @param bounds Bounds to scan within
     * @param exclusive If true, only include components that are fully
     *             contained within the bounds. If false, include any
     *             component that touches the bounds.
     * @todo Get rid of the pane parameter - it is redundant; should be webform.getPane()
     */
    public void selectComponentRectangle(Rectangle bounds, boolean contained) {
        List<Element> list = new ArrayList<Element>();
        DesignerPane pane = webform.getPane();
        selectViews(pane.getPageBox(), list, bounds, contained, 0);

//        Iterator it = list.iterator();
        clearSelection(false);

//        while (it.hasNext()) {
//            MarkupDesignBean component = (MarkupDesignBean)it.next();
        for (Element componentRootElement : list) {
//            addSelected(component, false);
            addSelected(componentRootElement, false);

            // The Marquee will call repaint
        }

//        updateSelection();
        updateNodes();

        // XXX do I need to go and set copy selection here?
        pane.repaint();
    }

    private static void selectViews(CssBox box, List<Element> matches, Rectangle bounds, boolean contained,
        int depth) {
        if (DesignerUtils.intersects(bounds, box.getAbsoluteX(), box.getAbsoluteY(),
                    box.getWidth(), box.getHeight())) {
            // TODO - use DesignerCaret's _contains instead!
            if ((contained &&
                    bounds.contains(box.getAbsoluteX(), box.getAbsoluteY(), box.getWidth(),
                        box.getHeight())) ||
                    (!contained &&
                    DesignerUtils.intersects(bounds, box.getAbsoluteX(), box.getAbsoluteY(),
                        box.getWidth(), box.getHeight()))) {
//                if (box.getDesignBean() != null) {
//                    matches.add(box.getDesignBean());
//                MarkupDesignBean markupDesignBean = CssBox.getMarkupDesignBeanForCssBox(box);
//                if (markupDesignBean != null) {
//                    matches.add(markupDesignBean);
//                }
                Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
                if (componentRootElement != null) {
                    matches.add(componentRootElement);
                }
            }
        }

        // XXX Should only have to do this for intersecting boxes!!!
        // (plus those intersecting with the -extents- of the box, e.g.
        // including absolutely positioned boxes!)
        for (int i = 0, n = box.getBoxCount(); i < n; i++) {
            CssBox child = box.getBox(i);

            if (child instanceof ExternalDocumentBox) {
                continue;
            }

            selectViews(child, matches, bounds, contained, depth + 1);
        }
    }

    /**
     * Focus the default property. Return false if no default property was
     * found, or if the property is read only, if no component is selected,
     * etc.
     * @todo check for readonly prop, and immediately sync prop sheet
     * to avoid delay problem!
     */
    public boolean focusDefaultProperty(ActionEvent event) {
//        if ((selected != null) && (selected.size() > 0)) {
        if (!selectedComponents.isEmpty()) {
            DesignerPane pane = webform.getPane();
//            ModelViewMapper mapper = webform.getMapper();

            // Find the first selected component that has a default
            // property or is inline editing capable
//            Iterator it = selected.iterator();
//
//            while (it.hasNext()) {
//                FormObject fo = (FormObject)it.next();
//            for (FormObject fo : selected) {
//                DesignBean lb = fo.component;
            for (SelectedComponent sc : selectedComponents) {
//                if (lb instanceof MarkupDesignBean) {
//                    MarkupDesignBean bean = (MarkupDesignBean)lb;
//                    CssBox box = mapper.findBox(bean);
                CssBox box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), sc.componentRootElement);
                // TODO - pass in keystroke too
                boolean inlineEdited =
//                    webform.getManager().startInlineEditing(bean, null, box, true, false,
                    webform.getManager().startInlineEditing(sc.componentRootElement, null, box, true, false,
                        event.getActionCommand(), false);
                if (inlineEdited) {
                    return true;
                }
//                }

//                DesignBean lb = WebForm.getDomProviderService().getMarkupDesignBeanForElement(sc.componentRootElement);
//                BeanInfo bi = lb.getBeanInfo();
//                if (bi != null) {
//                    int defaultProp = bi.getDefaultPropertyIndex();
//
//                    if (defaultProp != -1) {
                if (webform.getDomProviderService().hasDefaultProperty(sc.componentRootElement)) {
//                        FeatureDescriptor defProp = bi.getPropertyDescriptors()[defaultProp];
//
//                        // How do we launch the property sheet editing a
//                        // particular property?
//                        final JTable jt =
////                            org.netbeans.modules.visualweb.designer.DesignerUtils.findPropSheetTable(true, true);
//                                findPropSheetTable(true, true);
//
//                        if (jt == null) {
//                            return false;
//                        }
//
//                        TableModel model = jt.getModel();
//
//                        // Set focus of jt?
//                        for (int row = 0, n = model.getRowCount(); row < n; row++) {
//                            Object o = model.getValueAt(row, 0);
//
//                            if (!(o instanceof FeatureDescriptor)) {
//                                continue;
//                            }
//
//                            FeatureDescriptor desc = (FeatureDescriptor)o;
//
//                            if (defProp.getName().equals(desc.getName())) {
//                                // Edit the cell XXX only if readonly!
//                                if (desc instanceof Node.Property) {
//                                    Node.Property prop = (Node.Property)desc;
//
//                                    if (!prop.canWrite()) {
//                                        return false;
//                                    }
//                                }
//
//                                final int r = row;
//                                final String content = event.getActionCommand();
//                                SwingUtilities.invokeLater(new Runnable() {
//                                        public void run() {
//                                            jt.editCellAt(r, 1, null);
//                                            jt.requestFocus();
//
//                                            Object ce = jt.getCellEditor(r, 1);
//
//                                            // Hack Alert: try to transfer the
//                                            // original keypress into the text field
//                                            Component comp =
//                                                getInplaceEditorComponentForSheetCellEditor(ce);
//
//                                            if (comp instanceof javax.swing.text.JTextComponent) {
//                                                javax.swing.text.JTextComponent jtc =
//                                                    (javax.swing.text.JTextComponent)comp;
//                                                jtc.replaceSelection(content);
//                                            }
//                                        }
//                                    });
//
//                                return true;
//                            }
//                        }
                        return webform.getDomProviderService().focusDefaultProperty(sc.componentRootElement, event.getActionCommand());
                    } else {
                        // Is it a MarkupBean that has a TextNode child?
                        // (for example a <div> or a <table>. If so,
                        // try to set the caret to the given position
                        // and repeat the insert.)
//                        Element element = FacesSupport.getElement(lb);
//                        Element element = Util.getElement(lb);
//                        Element element = WebForm.getDomProviderService().getElement(lb);
                        Element element = webform.getDomProviderService().getSourceElement(sc.componentRootElement);

                        if (element != null) {
//                            org.w3c.dom.Node text = DesignerUtils.findFirstTextChild(element);
                            org.w3c.dom.Node text = findFirstTextChild(element);

                            if (text != null) {
//                                DesignerCaret dc = pane.getPaneUI().createCaret();
//                                pane.setCaret(dc);
                                pane.createCaret();

//                                Position newPos = new Position(text, 0, Bias.FORWARD);
//                                Position newPos = Position.create(text, 0, Bias.FORWARD);
                                DomPosition newPos = webform.createDomPosition(text, 0, Bias.FORWARD);
                                
//                                pane.setCaretPosition(newPos);
                                pane.setCaretDot(newPos);

//                                Document doc = webform.getDocument();

////                                UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(DefaultKeyTypedAction.class,
////                                        "InsertChar"));
//                                DomProvider.WriteLock writeLock = webform.writeLock(NbBundle.getMessage(DefaultKeyTypedAction.class,
//                                        "InsertChar"));
//                                try {
//                                    doc.writeLock(NbBundle.getMessage(DefaultKeyTypedAction.class,
//                                            "InsertChar"));

                                    final String content = event.getActionCommand();
//                                    pane.getCaret().replaceSelection(content);
                                    pane.replaceSelection(content);
//                                } finally {
////                                    doc.writeUnlock();
////                                    webform.getModel().writeUnlock(undoEvent);
//                                    webform.writeUnlock(writeLock);
//                                }

                                return true;
                            }
                        }
                    }
//                }
            }
        }

        return false;
    }

    // XXX Moved from DesignerUtils.
    /** Find the first TextNode child under this element, or null
     * if no such node is found.
     * @todo How do we avoid returning for example the blank
     * textnode between <table> and <tr> in a table? I want
     * the <td> !
     */
    private static org.w3c.dom.Node findFirstTextChild(org.w3c.dom.Node node) {
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".findFirstTextChild(Node)");
//        }
        if(node == null) {
            return(null);
        }
        if ((node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) ||
                (node.getNodeType() == org.w3c.dom.Node.CDATA_SECTION_NODE)) {
            return node;
        }
        
        org.w3c.dom.NodeList nl = node.getChildNodes();
        
        for (int i = 0, n = nl.getLength(); i < n; i++) {
            org.w3c.dom.Node result = findFirstTextChild(nl.item(i));
            
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }

    
//    // XXX Moved from DesignerUtils.
//    /** Locate the JTable within the property sheet in the IDE.
//     * WARNING: Implementation hacks!
//     * @param focus If set, focus the top component
//     * @param visible If set, ensure the top component is fronted
//     */
//    private static JTable findPropSheetTable(boolean focus, boolean visible) {
//        WindowManager mgr = WindowManager.getDefault();
//        TopComponent properties = mgr.findTopComponent("properties"); // NOI18N
//        
//        if ((properties != null) && (visible || properties.isShowing())) {
//            if (focus) {
//                properties.requestActive();
//            }
//            
//            if (visible) {
//                properties.requestVisible();
//            }
//            
//            return findTable(properties);
//        }
//        
//        return null;
//    }
//    
//    /** Fish the given Container hierarchy for a JTable */
//    private static JTable findTable(Container c) {
////        if(DEBUG) {
////            debugLog(DesignerUtils.class.getName() + ".findTable(Container)");
////        }
//        if(c == null) {
//            return(null);
//        }
//        if (c instanceof JTable) {
//            return (JTable)c;
//        }
//        
//        int n = c.getComponentCount();
//        
//        for (int i = 0; i < n; i++) {
//            Component comp = c.getComponent(i);
//            
//            if (comp instanceof JTable) {
//                return (JTable)comp;
//            }
//            
//            if (comp instanceof Container) {
//                JTable table = findTable((Container)comp);
//                
//                if (table != null) {
//                    return table;
//                }
//            }
//        }
//        
//        return null;
//    }
//
//    // XXX Using reflection, But it is still better than changing NB code
//    // The task from UI point of view looks very strange... why the text isn't inserted into the component, as user expect,
//    // but surprisinlgy the focus is moved into property sheet? That kind of solutions cause problems like this.
//    private static Component getInplaceEditorComponentForSheetCellEditor(Object ce) {
//        if (ce == null) {
//            return null;
//        }
//
//        Object inplaceEditor;
//
//        try {
//            ClassLoader cl =
//                org.openide.explorer.propertysheet.PropertySheet.class.getClassLoader();
//            Class sheetCellEditorClass =
//                Class.forName("org.openide.explorer.propertysheet.SheetCellEditor", true, cl); // NOI18N
//            java.lang.reflect.Method getInplaceEditorMethod =
//                sheetCellEditorClass.getDeclaredMethod("getInplaceEditor", new Class[0]); // NOI18N
//            getInplaceEditorMethod.setAccessible(true);
//            inplaceEditor = getInplaceEditorMethod.invoke(ce, new Object[0]);
//        } catch (ClassNotFoundException cnfe) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, cnfe);
//            inplaceEditor = null;
//        } catch (NoSuchMethodException nsme) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nsme);
//            inplaceEditor = null;
//        } catch (IllegalAccessException iae) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, iae);
//            inplaceEditor = null;
//        } catch (java.lang.reflect.InvocationTargetException ite) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ite);
//            inplaceEditor = null;
//        }
//
//        if (inplaceEditor instanceof org.openide.explorer.propertysheet.InplaceEditor) {
//            return ((org.openide.explorer.propertysheet.InplaceEditor)inplaceEditor).getComponent();
//        } else {
//            return null;
//        }
//    }

    /**
     * Determine if the given point overlaps a selection handle.
     * @param editor The editor whose selection we're checking
     * @param x The x coordinate of the position we want to check
     * @param y The y coordinate of the position we want to check
     * @return Cursor.DEFAULT_CURSOR if the given point (x,y) is not
     * over any of the selection handles drawn for the current selection.
     * If it is, return the right edge direction for the overlap.
     * If there are multiple overlapping selection handles, it's arbitrary
     * which one is chosen.
     * @see overSelection
     */
    public int getSelectionHandleDir(int x, int y, int maxWidth, int maxHeight) {
//        if ((selected != null) && (selected.size() > 0)) {
//            Iterator it = selected.iterator();
////            ModelViewMapper mapper = webform.getMapper();
//
//            while (it.hasNext()) {
//                FormObject fo = (FormObject)it.next();
//
//                // XXX I should cache these!!!
////                ArrayList rectangles = mapper.getComponentRectangles(fo.component);
//                List rectangles = ModelViewMapper.getComponentRectangles(webform.getPane().getPageBox(), fo.component);
//
//                for (int i = 0; i < rectangles.size(); i++) {
//                    int over =
//                        overSelection(x, y, (Rectangle)rectangles.get(i), fo.resizeConstraints,
//                            maxWidth, maxHeight);
//
//                    if (over != Cursor.DEFAULT_CURSOR) {
//                        return over;
//                    }
//                }
//            }
//        }
        for (SelectedComponent sc : selectedComponents) {
            // XXX I should cache these!!!
//                ArrayList rectangles = mapper.getComponentRectangles(fo.component);
            List rectangles = ModelViewMapper.getComponentRectangles(
                    webform.getPane().getPageBox(), sc.componentRootElement);
            for (int i = 0; i < rectangles.size(); i++) {
                int over = overSelection(x, y, (Rectangle)rectangles.get(i), sc.resizeConstraints, maxWidth, maxHeight);
                if (over != Cursor.DEFAULT_CURSOR) {
                    return over;
                }
            }
        }

        return Cursor.DEFAULT_CURSOR;
    }

    /**
     * Similar to getSelectionHandleDir, but returns the selected
     * object whose selection handles are pointed at, rather than the
     * selection handle direction.
     */
    public /*MarkupDesignBean*/Element getSelectionHandleView(int x, int y, int maxWidth, int maxHeight) {
//        if ((selected != null) && (selected.size() > 0)) {
////            ModelViewMapper mapper = webform.getMapper();
//
//            Iterator it = selected.iterator();
//
//            while (it.hasNext()) {
//                FormObject fo = (FormObject)it.next();
////                ArrayList rectangles = mapper.getComponentRectangles(fo.component);
//                List rectangles = ModelViewMapper.getComponentRectangles(webform.getPane().getPageBox(), fo.component);
//
//                for (int i = 0; i < rectangles.size(); i++) {
//                    int dir =
//                        overSelection(x, y, (Rectangle)rectangles.get(i), fo.resizeConstraints,
//                            maxWidth, maxHeight);
//
//                    if (dir != Cursor.DEFAULT_CURSOR) {
//                        return fo.component;
//                    }
//                }
//            }
//        }
        for (SelectedComponent sc : selectedComponents) {
//                ArrayList rectangles = mapper.getComponentRectangles(fo.component);
            List<Rectangle> rectangles = ModelViewMapper.getComponentRectangles(
                    webform.getPane().getPageBox(), sc.componentRootElement);

            for (Rectangle rectangle : rectangles) {
                int dir = overSelection(x, y, rectangle, sc.resizeConstraints, maxWidth, maxHeight);
                if (dir != Cursor.DEFAULT_CURSOR) {
                    return sc.componentRootElement;
                }
            }
        }

        return null;
    }

    /**
     * Return true iff no components are selected
     */
    public boolean isSelectionEmpty() {
//        return (selected == null) || (selected.size() == 0);
        return selectedComponents.isEmpty();
    }

    /**
     * Report the number of selected items.
     * @return the number of selected items in the designer
     */
    public int getNumSelected() {
//        return (selected == null) ? 0 : selected.size();
        return selectedComponents.size();
    }

    /** Make a particular element selected
     */
    public void selectComponents(final /*DesignBean[] components*/Element[] componentRootElements, final boolean update) {
//        if (ignoreSelection) {
//            return;
//        }

        // Make sure the component is selected
        // Unfortunately we have to do this in a delay;
        // see Document.handleEvent for details (look near
        // DOM_NODE_INSERTED - and note that we're called
        // from DndHandler when components
        // are inserted)
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
//                    assert components != null;
//
//                    clearSelection(false);
//
//                    for (int i = 0; i < components.length; i++) {
//                        if (components[i] instanceof MarkupDesignBean) {
////                            addSelected((MarkupDesignBean)components[i], false);
//                            addSelected(getComponentRootElementForMarkupDesignBean((MarkupDesignBean)components[i]), false);
//                        }
//                    }

                    clearSelection(false);

                    if (componentRootElements != null) {
                        for (Element componentRootElement : componentRootElements) {
                            if (componentRootElement != null) {
                                addSelected(componentRootElement, false);
                            }
                        }
                    }

                    if (update) {
//                        updateSelection();
                        updateNodes();
                    }

                    DesignerPane dp = webform.getPane();

                    // #6258586 NPE fix.
                    if (dp != null) {
                        dp.repaint(); // tray wants a repaint but no node update!!! update means node update!
                    }
                }
            });
    }

    /** Increase the size of the given rectangle to accomodate
     * the selection handles. Typically used when computing dirty/repaint
     * rectangles
     * @todo Should probably be a static method
     */
    public void enlarge(Rectangle r) {
        r.x -= (BARSIZE + 1);
        r.y -= (BARSIZE + 1);
        r.width += ((2 * BARSIZE) + 2);
        r.height += ((2 * BARSIZE) + 2);
    }

    private BasicStroke getSelectionStroke() {
        if (selStroke == null) {
            int width = 1;
            selStroke =
                new BasicStroke((float)width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
                    10.0f, new float[] { 6 * width, (6 * width) + width }, 0.0f);
        }

        return selStroke;
    }

    /** Draw selection rectangles around the currently selected views */
    public void paintSelection(Graphics2D g2d) {
        if (webform.getManager().isInlineEditing()) {
            return;
        }

//        if ((selected != null) && (selected.size() > 0)) {
//            boolean selectedMany = selected.size() > 1;
        if (!selectedComponents.isEmpty()) {
//            boolean selectedMany = selected.size() > 1;
            boolean selectedMany = selectedComponents.size() > 1;
//            ModelViewMapper mapper = webform.getMapper();
            ColorManager colors = webform.getColors();

//            Iterator it = selected.iterator();
            PageBox pageBox = webform.getPane().getPageBox();
            int maxWidth = pageBox.getWidth();
            int maxHeight = pageBox.getHeight();

//            while (it.hasNext()) {
//                FormObject fo = (FormObject)it.next();
//            for (FormObject fo : selected) {
//
//                // XXX I should cache these!!!
////                ArrayList rectangles = mapper.getComponentRectangles(fo.component);
//                List rectangles = ModelViewMapper.getComponentRectangles(pageBox, fo.component);
////                Rectangle bounds = mapper.getComponentBounds(fo.component);
//                Rectangle bounds = ModelViewMapper.getComponentBounds(pageBox, fo.component);

            // XXX #95626 Hack (model might be changing underneath without proper notifications).
            // The elements might have been changed while the beans remain same.
            if (primary != null) {
                Element equivalentElement = webform.getComponentRootElementEquivalentTo(primary);
                if (equivalentElement != null && equivalentElement != primary) {
                    primary = equivalentElement;
                }
            }
            
            for (SelectedComponent sc : selectedComponents) {
                // XXX #95626 Hack (model might be changing underneath without proper notifications).
                // The elements might have been changed while the beans remain same.
                Element equivalentElement = webform.getComponentRootElementEquivalentTo(sc.componentRootElement);
                if (equivalentElement != null && equivalentElement != sc.componentRootElement) {
                    sc.componentRootElement = equivalentElement;
                }
                
                // XXX I should cache these!!!
//                ArrayList rectangles = mapper.getComponentRectangles(fo.component);
                List rectangles = ModelViewMapper.getComponentRectangles(pageBox, sc.componentRootElement);
//                Rectangle bounds = mapper.getComponentBounds(fo.component);
                Rectangle bounds = ModelViewMapper.getComponentBounds(pageBox, sc.componentRootElement);
                
                int n = rectangles.size();

                // Draw bounds rectangle if we have multiple rectangles?
                if (n > 0) {
                    Rectangle r1 = (Rectangle)rectangles.get(0);

                    if ((r1.x != bounds.x) || (r1.y != bounds.y) || (r1.width != bounds.width) ||
                            (r1.height != bounds.height)) {
                        g2d.setColor(colors.selectionBoundsColor);

                        Stroke oldStroke = g2d.getStroke();
                        BasicStroke selStroke = getSelectionStroke();
                        g2d.setStroke(selStroke);
                        g2d.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
                        g2d.setStroke(oldStroke);
                    }
                }

                for (int i = 0; i < n; i++) {
//                    paintSelected(g2d, selectedMany && (fo.component == primary),
//                        (Rectangle)rectangles.get(i), fo.resizeConstraints, maxWidth, maxHeight);
                    paintSelected(g2d, 
                            selectedMany && (sc.componentRootElement == primary),
                            (Rectangle)rectangles.get(i), sc.resizeConstraints, maxWidth, maxHeight);
                }
            }
        }
    }

    /**
     * Draw selection for the given view.
     * @param g The graphics to draw with
     * @param insertMode If true, draw a thick insert-mode rectangle
     *        around the selection
     * @param rect The selection rectangle
     * @param constraints Which sides are resizable
     */
    private void paintSelected(Graphics2D g2d, boolean isPrimary, Rectangle rect, /*int constraints,*/
    ResizeConstraint[] resizeConstraints, int maxWidth, int maxHeight) {
        int x = rect.x;
        int y = rect.y;
        int w = rect.width;
        int h = rect.height;

        if (x < (BARSIZE + 1)) {
            w -= ((BARSIZE + 1) - x);
            x = BARSIZE + 1;
        }

        if (y < (BARSIZE + 1)) {
            h -= ((BARSIZE + 1) - y);
            y = BARSIZE + 1;
        }

        if ((x + w + BARSIZE) > maxWidth) {
            w = maxWidth - BARSIZE - x;
        }

        if ((y + h + BARSIZE) > maxHeight) {
            h = maxHeight - BARSIZE - y;
        }

        int w2 = (w / 2) - (BARSIZE / 2);
        int h2 = (h / 2) - (BARSIZE / 2);

        //g2d.setColor(Marquee.marqueeColor); // TODO - make selectable
        //g2d.setXORMode(view.getContainer().getBackground());
        //g2d.setPaintMode(); // Just in case...
        // I've gotta clip! I'm drawing outside of the canvas on OSX!
        ColorManager colors = webform.getColors();
        g2d.setColor(colors.selectionColor); // TODO - make selectable

        g2d.drawRect(x - BARSIZE - 1, y - BARSIZE - 1, BARSIZE, BARSIZE);
        g2d.drawRect(x + w, y - BARSIZE - 1, BARSIZE, BARSIZE);
        g2d.drawRect(x + w, y + h, BARSIZE, BARSIZE);
        g2d.drawRect(x - BARSIZE - 1, y + h, BARSIZE, BARSIZE);

        // Middles
//        if ((constraints & Constants.ResizeConstraints.TOP) != 0) {
        if (Resizer.hasTopResizeConstraint(resizeConstraints)) {
            g2d.drawRect(x + w2, y - BARSIZE - 1, BARSIZE, BARSIZE); // top
        }

//        if ((constraints & Constants.ResizeConstraints.LEFT) != 0) {
        if (Resizer.hasLeftResizeConstraint(resizeConstraints)) {
            g2d.drawRect(x - BARSIZE - 1, y + h2, BARSIZE, BARSIZE); // left
        }

//        if ((constraints & Constants.ResizeConstraints.BOTTOM) != 0) {
        if (Resizer.hasBottomResizeConstraint(resizeConstraints)) {
            g2d.drawRect(x + w2, y + h, BARSIZE, BARSIZE); // bottom
        }

//        if ((constraints & Constants.ResizeConstraints.RIGHT) != 0) {
        if (Resizer.hasRightResizeConstraint(resizeConstraints)) {
            g2d.drawRect(x + w, y + h2, BARSIZE, BARSIZE); // right
        }

        // Reverse video within the outer rectangle to increase visibility
        // over areas different from the dominant background color
        g2d.setColor(colors.selectionColorReverse);
        g2d.drawRect(x - BARSIZE - 1 + 1, y - BARSIZE - 1 + 1, BARSIZE - 2, BARSIZE - 2);
        g2d.drawRect(x + w + 1, y - BARSIZE - 1 + 1, BARSIZE - 2, BARSIZE - 2);
        g2d.drawRect(x + w + 1, y + h + 1, BARSIZE - 2, BARSIZE - 2);
        g2d.drawRect(x - BARSIZE - 1 + 1, y + h + 1, BARSIZE - 2, BARSIZE - 2);

//        if ((constraints & Constants.ResizeConstraints.TOP) != 0) {
        if (Resizer.hasTopResizeConstraint(resizeConstraints)) {
            g2d.drawRect(x + w2 + 1, y - BARSIZE - 1 + 1, BARSIZE - 2, BARSIZE - 2); // top
        }

//        if ((constraints & Constants.ResizeConstraints.LEFT) != 0) {
        if (Resizer.hasLeftResizeConstraint(resizeConstraints)) {
            g2d.drawRect(x - BARSIZE - 1 + 1, y + h2 + 1, BARSIZE - 2, BARSIZE - 2); // left
        }

//        if ((constraints & Constants.ResizeConstraints.BOTTOM) != 0) {
        if (Resizer.hasBottomResizeConstraint(resizeConstraints)) {
            g2d.drawRect(x + w2 + 1, y + h + 1, BARSIZE - 2, BARSIZE - 2); // bottom
        }

//        if ((constraints & Constants.ResizeConstraints.RIGHT) != 0) {
        if (Resizer.hasRightResizeConstraint(resizeConstraints)) {
            g2d.drawRect(x + w + 1, y + h2 + 1, BARSIZE - 2, BARSIZE - 2); // right
        }

        if (isPrimary) {
            // Fill in the selection squares to indicate the primary
            // item. We only fill in the corners to make it stand out a
            // bit more.
            g2d.setColor(colors.primaryColor);
            g2d.fillRect(x - BARSIZE - 1 + 1, y - BARSIZE - 1 + 1, (BARSIZE + 1) - 2,
                (BARSIZE + 1) - 2);
            g2d.fillRect(x + w + 1, y - BARSIZE - 1 + 1, (BARSIZE + 1) - 2, (BARSIZE + 1) - 2);
            g2d.fillRect(x + w + 1, y + h + 1, (BARSIZE + 1) - 2, (BARSIZE + 1) - 2);
            g2d.fillRect(x - BARSIZE - 1 + 1, y + h + 1, (BARSIZE + 1) - 2, (BARSIZE + 1) - 2);

//            if ((constraints & Constants.ResizeConstraints.TOP) != 0) {
            if (Resizer.hasTopResizeConstraint(resizeConstraints)) {
                g2d.drawRect(x + w2 + 1, y - BARSIZE - 1 + 1, BARSIZE - 2, BARSIZE - 2); // top
            }

//            if ((constraints & Constants.ResizeConstraints.LEFT) != 0) {
            if (Resizer.hasLeftResizeConstraint(resizeConstraints)) {
                g2d.drawRect(x - BARSIZE - 1 + 1, y + h2 + 1, BARSIZE - 2, BARSIZE - 2); // left
            }

//            if ((constraints & Constants.ResizeConstraints.BOTTOM) != 0) {
            if (Resizer.hasBottomResizeConstraint(resizeConstraints)) {
                g2d.drawRect(x + w2 + 1, y + h + 1, BARSIZE - 2, BARSIZE - 2); // bottom
            }

//            if ((constraints & Constants.ResizeConstraints.RIGHT) != 0) {
            if (Resizer.hasRightResizeConstraint(resizeConstraints)) {
                g2d.drawRect(x + w + 1, y + h2 + 1, BARSIZE - 2, BARSIZE - 2); // right
            }
        }

        //g2d.setPaintMode();
    }

    /**
     * Draw selection for the given view.
     * @param g The graphics to draw with
     * @param insertMode If true, draw a thick insert-mode rectangle
     *        around the selection
     * @param rect The selection rectangle
     * @param constraints Which sides are resizable
     */
    void paintInlineEditorBox(Graphics2D g2d, Rectangle rect) {
        int x = rect.x;
        int y = rect.y;
        int w = rect.width;
        int h = rect.height;

        if (x < (BARSIZE + 1)) {
            w -= ((BARSIZE + 1) - x);
            x = BARSIZE + 1;
        }

        if (y < (BARSIZE + 1)) {
            h -= ((BARSIZE + 1) - y);
            y = BARSIZE + 1;
        }

        PageBox pageBox = webform.getPane().getPageBox();
        int maxWidth = pageBox.getWidth();
        int maxHeight = pageBox.getHeight();

        if ((x + w + BARSIZE) > maxWidth) {
            w = maxWidth - BARSIZE - x;
        }

        if ((y + h + BARSIZE) > maxHeight) {
            h = maxHeight - BARSIZE - y;
        }

        //g2d.setColor(Marquee.marqueeColor); // TODO - make selectable
        //g2d.setXORMode(view.getContainer().getBackground());
        //g2d.setPaintMode(); // Just in case...
        // I've gotta clip! I'm drawing outside of the canvas on OSX!
        ColorManager colors = webform.getColors();
        g2d.setColor(colors.selectionColor); // TODO - make selectable

        g2d.drawRect(x - BARSIZE - 1, y - BARSIZE - 1, BARSIZE, BARSIZE);
        g2d.drawRect(x + w, y - BARSIZE - 1, BARSIZE, BARSIZE);
        g2d.drawRect(x + w, y + h, BARSIZE, BARSIZE);
        g2d.drawRect(x - BARSIZE - 1, y + h, BARSIZE, BARSIZE);

        // Reverse video within the outer rectangle to increase visibility
        // over areas different from the dominant background color
        g2d.setColor(colors.selectionColorReverse);
        g2d.drawRect(x - BARSIZE - 1 + 1, y - BARSIZE - 1 + 1, BARSIZE - 2, BARSIZE - 2);
        g2d.drawRect(x + w + 1, y - BARSIZE - 1 + 1, BARSIZE - 2, BARSIZE - 2);
        g2d.drawRect(x + w + 1, y + h + 1, BARSIZE - 2, BARSIZE - 2);
        g2d.drawRect(x - BARSIZE - 1 + 1, y + h + 1, BARSIZE - 2, BARSIZE - 2);

        // Solid text bars
        g2d.setColor(colors.insertColor); // TODO - make selectable
        g2d.fillRect(x + 1, y - BARSIZE - 1, w - 2, (BARSIZE + 1) - 1);
        g2d.fillRect(x + 1, y + h + 1, w - 2, (BARSIZE + 1) - 1);
        g2d.fillRect(x - BARSIZE - 1, y + 1, (BARSIZE + 1) - 1, h - 2);
        g2d.fillRect(x + w + 1, y + 1, (BARSIZE + 1) - 1, h - 2);

        //g2d.setPaintMode();
    }

    /** Check to see if the cursor position is over a selection rectangle.
     * This method is here next to the drawing routine since it's closely
     * tied to how the selection is rendered - it should be pixel accurate
     * about whether the given position is on top of a selection
     * handle bar.
     * @param px The x coordinate of the position we want to check
     * @param py The y coordinate of the position we want to check
     * @param rect The rectangle for a selected view/component
     * @param constraints Resizability constraints
     * @return An integer equal to either Cursor.DEFAULT_CURSOR if there
     *  is no overlap, or Cursor.N_RESIZE_CURSOR, Cursor.NE_RESIZE_CURSOR,
     *  (etc. etc. for all the directions) to indicate that there is a match,
     *  an in particular on which edge/corner.  Thus, there is overlap
     *  if return value is not equal to Cursor.DEFAULT_CURSOR, and the
     *  specific return value can be used to for example pick an appropriate
     *  Cursor to draw.
     */
    private int overSelection(int px, int py, Rectangle rect, /*int constraints,*/
    ResizeConstraint[] resizeConstraints, int maxWidth, int maxHeight) {
        if (py >= selectionViewPos) {
            return Cursor.DEFAULT_CURSOR;
        }

        int x = rect.x;
        int y = rect.y;
        int w = rect.width;
        int h = rect.height;

        if (x < (BARSIZE + 1)) {
            w -= ((BARSIZE + 1) - x);
            x = BARSIZE + 1;
        }

        if (y < (BARSIZE + 1)) {
            h -= ((BARSIZE + 1) - y);
            y = BARSIZE + 1;
        }

        // Quick elimination - not within the rectangle that surrounds the
        // view of width BARSIZE. This is true for most points in the canvas
        // so is a quick way to speed up the search
        if (px < (x - BARSIZE - 1)) {
            return Cursor.DEFAULT_CURSOR;
        }

        if (py < (y - BARSIZE - 1)) {
            return Cursor.DEFAULT_CURSOR;
        }

        if ((x + w + BARSIZE) > maxWidth) {
            w = maxWidth - BARSIZE - x;
        }

        if ((y + h + BARSIZE) > maxHeight) {
            h = maxHeight - BARSIZE - y;
        }

        if (px > (x + w + BARSIZE)) {
            return Cursor.DEFAULT_CURSOR;
        }

        if (py > (y + h + BARSIZE)) {
            return Cursor.DEFAULT_CURSOR;
        }

        // Okay, do some more fine tuned checking
        if (DesignerUtils.inside(px, py, x - BARSIZE - 1, y - BARSIZE - 1, BARSIZE, BARSIZE)) {
//            if ((constraints &
//                    (Constants.ResizeConstraints.TOP | Constants.ResizeConstraints.LEFT)) == (Constants.ResizeConstraints.TOP |
//                    Constants.ResizeConstraints.LEFT)) {
            if (Resizer.hasTopResizeConstraint(resizeConstraints) && Resizer.hasLeftResizeConstraint(resizeConstraints)) {
                return Cursor.NW_RESIZE_CURSOR;
//            } else if ((constraints & Constants.ResizeConstraints.TOP) != 0) {
            } else if (Resizer.hasTopResizeConstraint(resizeConstraints)) { 
                return Cursor.N_RESIZE_CURSOR;
//            } else if ((constraints & Constants.ResizeConstraints.LEFT) != 0) {
            } else if (Resizer.hasLeftResizeConstraint(resizeConstraints)) {
                return Cursor.W_RESIZE_CURSOR;
            }
        }

        if (DesignerUtils.inside(px, py, x + w, y - BARSIZE - 1, BARSIZE, BARSIZE)) {
//            if ((constraints &
//                    (Constants.ResizeConstraints.TOP | Constants.ResizeConstraints.RIGHT)) == (Constants.ResizeConstraints.TOP |
//                    Constants.ResizeConstraints.RIGHT)) {
            if (Resizer.hasTopResizeConstraint(resizeConstraints) && Resizer.hasRightResizeConstraint(resizeConstraints)) {
                return Cursor.NE_RESIZE_CURSOR;
//            } else if ((constraints & Constants.ResizeConstraints.TOP) != 0) {
            } else if (Resizer.hasTopResizeConstraint(resizeConstraints)) {
                return Cursor.N_RESIZE_CURSOR;
//            } else if ((constraints & Constants.ResizeConstraints.RIGHT) != 0) {
            } else if (Resizer.hasRightResizeConstraint(resizeConstraints)) {
                return Cursor.E_RESIZE_CURSOR;
            }
        }

        if (DesignerUtils.inside(px, py, x + w, y + h, BARSIZE, BARSIZE)) {
//            if ((constraints &
//                    (Constants.ResizeConstraints.BOTTOM | Constants.ResizeConstraints.RIGHT)) == (Constants.ResizeConstraints.BOTTOM |
//                    Constants.ResizeConstraints.RIGHT)) {
            if (Resizer.hasBottomResizeConstraint(resizeConstraints) && Resizer.hasRightResizeConstraint(resizeConstraints)) {
                return Cursor.SE_RESIZE_CURSOR;
//            } else if ((constraints & Constants.ResizeConstraints.BOTTOM) != 0) {
            } else if (Resizer.hasBottomResizeConstraint(resizeConstraints)) {
                return Cursor.S_RESIZE_CURSOR;
//            } else if ((constraints & Constants.ResizeConstraints.RIGHT) != 0) {
            } else if (Resizer.hasRightResizeConstraint(resizeConstraints)) {
                return Cursor.E_RESIZE_CURSOR;
            }
        }

        if (DesignerUtils.inside(px, py, x - BARSIZE - 1, y + h, BARSIZE, BARSIZE)) {
//            if ((constraints &
//                    (Constants.ResizeConstraints.BOTTOM | Constants.ResizeConstraints.LEFT)) == (Constants.ResizeConstraints.BOTTOM |
//                    Constants.ResizeConstraints.LEFT)) {
            if (Resizer.hasBottomResizeConstraint(resizeConstraints) && Resizer.hasLeftResizeConstraint(resizeConstraints)) {
                return Cursor.SW_RESIZE_CURSOR;
//            } else if ((constraints & Constants.ResizeConstraints.BOTTOM) != 0) {
            } else if (Resizer.hasBottomResizeConstraint(resizeConstraints)) {
                return Cursor.S_RESIZE_CURSOR;
//            } else if ((constraints & Constants.ResizeConstraints.LEFT) != 0) {
            } else if (Resizer.hasLeftResizeConstraint(resizeConstraints)) {
                return Cursor.W_RESIZE_CURSOR;
            }
        }

        // In inline editing mode we can drag the border to start moving the component
        // PENDING: Look for a movable parent first to decide?
        // Or only do this for positioned components?
        if (webform.getManager().isInlineEditing() &&
                DesignerUtils.inside(px, py, x - BARSIZE - 1, y - BARSIZE - 1, w + (2 * BARSIZE),
                    h + (2 * BARSIZE)) && !DesignerUtils.inside(px, py, x, y, w, h)) {
            return Cursor.MOVE_CURSOR;
        }

        int w2 = (w / 2) - (BARSIZE / 2);
        int h2 = (h / 2) - (BARSIZE / 2);

//        if ((constraints & Constants.ResizeConstraints.TOP) != 0) {
        if (Resizer.hasTopResizeConstraint(resizeConstraints)) {
            if (DesignerUtils.inside(px, py, (x + w2) - (BARSIZE / 2), y - BARSIZE - 1, BARSIZE,
                        BARSIZE)) {
                return Cursor.N_RESIZE_CURSOR;
            }
        }

//        if ((constraints & Constants.ResizeConstraints.LEFT) != 0) {
        if (Resizer.hasLeftResizeConstraint(resizeConstraints)) {
            if (DesignerUtils.inside(px, py, x - BARSIZE - 1, (y + h2) - (BARSIZE / 2), BARSIZE,
                        BARSIZE)) {
                return Cursor.W_RESIZE_CURSOR;
            }
        }

//        if ((constraints & Constants.ResizeConstraints.BOTTOM) != 0) {
        if (Resizer.hasBottomResizeConstraint(resizeConstraints)) {
            if (DesignerUtils.inside(px, py, (x + w2) - (BARSIZE / 2), y + h, BARSIZE, BARSIZE)) {
                return Cursor.S_RESIZE_CURSOR;
            }
        }

//        if ((constraints & Constants.ResizeConstraints.RIGHT) != 0) {
        if (Resizer.hasRightResizeConstraint(resizeConstraints)) {
            if (DesignerUtils.inside(px, py, x + w, (y + h2) - (BARSIZE / 2), BARSIZE, BARSIZE)) {
                return Cursor.E_RESIZE_CURSOR;
            }
        }

        return Cursor.DEFAULT_CURSOR;
    }

    // XXX HACKY CODE! Clean up once we agree on the UI & operation for this!
    public void paintSelHierarchy(Graphics2D g2d) {
        if (!PAINT_SELECTION_HIERARCHY) {
            return;
        }

        if (isSelectionEmpty()) {
            return;
        }

        PageBox pageBox = webform.getPane().getPageBox();

        if (pageBox == null) {
            return;
        }

        JViewport viewport = pageBox.getViewport();
        Dimension d = viewport.getExtentSize();
        Point p = viewport.getViewPosition();
        FontMetrics metrics = webform.getPane().getMetrics();
        FontMetrics boldMetrics = webform.getPane().getBoldMetrics();

        // Keep this positioning logic in sync with notifyScrolled!
        int h = metrics.getHeight() + 1;
        int x = SELECTIONVIEW_LEFT;
        int y = (p.y + d.height) - h;
        int yadj = (y + metrics.getHeight()) - metrics.getDescent();
        selectionViewPos = y;

        ColorManager colors = webform.getColors();
        g2d.setColor(colors.hierarchyBackgroundColor);
        g2d.fillRect(x, y, d.width, h);
        g2d.setColor(colors.hierarchyForegroundColor);

        // Draw for the first selected item
//        Iterator it = selected.iterator();
//        assert it.hasNext();
//
//        FormObject fo = (FormObject)it.next();
//        MarkupDesignBean currentBean = fo.component;
        Iterator<SelectedComponent> it = selectedComponents.iterator();
        assert it.hasNext();
        SelectedComponent sc = it.next();
//        MarkupDesignBean currentBean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(sc.componentRootElement);
        
//        MarkupDesignBean leafBean = leaf;
//        MarkupDesignBean leafBean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(leaf);
        
//        if (leafBean == null) {
//            leafBean = currentBean;
//        }
        if (leaf == null) {
            leaf = sc.componentRootElement;
        }

//        paintOrSelectAncestor(g2d, metrics, boldMetrics, leafBean, x, y, yadj, currentBean, -1);
        paintOrSelectAncestor(g2d, metrics, boldMetrics, leaf, x, y, yadj, sc.componentRootElement, -1);
    }

    // XXX HACKY CODE! Clean up once we agree on the UI & operation for this!
    public void notifyScrolled() {
        if (!PAINT_SELECTION_HIERARCHY) {
            return;
        }

        // Gotta repaint to ensure that we place the selection
        // hierarchy view on the right line
        if (isSelectionEmpty()) {
            return;
        }

        PageBox pageBox = webform.getPane().getPageBox();

        if (pageBox == null) {
            return;
        }

        JViewport viewport = pageBox.getViewport();
        Point p = viewport.getViewPosition();
        Dimension d = viewport.getExtentSize();
        FontMetrics metrics = webform.getPane().getMetrics();
        int h = metrics.getHeight() + 1;
        int y = (p.y + d.height) - h;

        if (y != selectionViewPos) {
            webform.getPane().repaint();
        }
    }

    // XXX HACKY CODE! Clean up once we agree on the UI & operation for this!
    private int paintOrSelectAncestor(Graphics2D g2d, FontMetrics metrics, FontMetrics boldMetrics,
    Element componentRootElement, /*MarkupDesignBean bean,*/ int x, int ytop, int y, Element selectedComponentRootElement, /*DesignBean selected,*/ int target) {
//        if (bean.getBeanParent() instanceof MarkupDesignBean &&
////                !FacesSupport.isSpecialBean(/*webform, */bean)) {
////                !Util.isSpecialBean(bean)) {
//                !WebForm.getDomProviderService().isSpecialComponent(
//                    WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean(bean))) {
        if (!webform.getDomProviderService().isSpecialComponent(componentRootElement)) {
//            x = paintOrSelectAncestor(g2d, metrics, boldMetrics,
//                    (MarkupDesignBean)bean.getBeanParent(), x, ytop, y, selected, target);
            x = paintOrSelectAncestor(g2d, metrics, boldMetrics,
                    webform.getDomProviderService().getParentComponent(componentRootElement), x, ytop, y, selectedComponentRootElement, target);

            if (x < 0) {
                return x;
            }
        }

//        String s = bean.getInstanceName();
//
//        if (s == null) {
//            s = "<unknown>";
//        }
//
//        if (x > SELECTIONVIEW_LEFT) {
//            s = "- " + bean.getInstanceName();
//        }
        String s = webform.getDomProviderService().getInstanceName(componentRootElement);
        if (s == null) {
            s = "<unknown>"; // TODO I18N
        }

        if (g2d != null) { // painting, not selecting

//            if (bean == selected) {
            if (componentRootElement == selectedComponentRootElement) {
                g2d.setFont(boldMetrics.getFont());
            } else {
                g2d.setFont(metrics.getFont());
            }

            // TODO - icon here?
        }

        if (paintBeanIcon) {
//            BeanInfo bi = bean.getBeanInfo();
//            Image icon = null;
//
//            if (bi != null) {
//                icon = bi.getIcon(BeanInfo.ICON_COLOR_16x16);
            Image icon = webform.getDomProviderService().getIcon(componentRootElement);

                if (icon != null) {
                    if (g2d != null) {
                        float ix = (float)x;
                        float iy = (float)ytop;
                        transform.setToTranslation(ix, iy);
                        g2d.drawImage(icon, transform, null);
                    }

                    x += (16 + 2); // spacing
                }
//            }
        }

        if (g2d != null) {
            g2d.drawString(s, x, y);

            // XXX restore font?
        }

//        if (bean == selected) {
        if (componentRootElement == selectedComponentRootElement) {
            x += boldMetrics.stringWidth(s);
        } else {
            x += metrics.stringWidth(s);
        }

        x += 5; // spacing

        if (g2d == null) {
            // See if we've gone past the x position where the user
            // clicked on a component, thus this must be the item we're
            // interested in
            if (x > target) {
//                setSelected(bean, true);
                setSelected(componentRootElement, true);

                return -1;
            }
        }

        return x;
    }

    // XXX HACKY CODE! Clean up once we agree on the UI & operation for this!
    void selectAncestor(int targetx, int targety) {
        if (isSelectionEmpty()) {
            return;
        }

        PageBox pageBox = webform.getPane().getPageBox();

        if (pageBox == null) {
            return;
        }

        FontMetrics metrics = webform.getPane().getMetrics();
        FontMetrics boldMetrics = webform.getPane().getBoldMetrics();
        int x = 1;
        int y = selectionViewPos;
        int yadj = (y + metrics.getHeight()) - metrics.getDescent();
//        Iterator it = selected.iterator();
//        assert it.hasNext();
//
//        FormObject fo = (FormObject)it.next();
//        MarkupDesignBean currentBean = fo.component;
        Iterator<SelectedComponent> it = selectedComponents.iterator();
        assert it.hasNext();
        SelectedComponent sc = it.next();
//        MarkupDesignBean currentBean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(sc.componentRootElement);
        
//        MarkupDesignBean leafBean = leaf;
//        MarkupDesignBean leafBean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(leaf);

//        if (leafBean == null) {
//            leafBean = currentBean;
//        }
        if (leaf == null) {
            leaf = sc.componentRootElement;
        }

//        paintOrSelectAncestor(null, metrics, boldMetrics, leafBean, x, y, yadj, currentBean, targetx);
        paintOrSelectAncestor(null, metrics, boldMetrics, leaf, x, y, yadj, sc.componentRootElement, targetx);
    }

    /**
     * Set the activated nodes for the top component bound to this selection
     * manager to match the current component.
     * If there is a component (or multiple components) selected,
     * the nodes will reflect these components, otherwise a document node
     * is chosen.
     * @todo If the selection MATCHES what is already shown, don't do anything.
     */
    public void updateNodes() {
        /* No - this is too risky; for example, if you right click on
           a component it must be selected immediately (since the context
           menu is keyed off the activated nodes list). So I've gotta
           call updateNodesImmediate() in that case. Gotta think about
           this some more before doing it.
         */
        if (refreshTimer != null) {
            refreshTimer.stop();
            refreshTimer = null;
        }

        refreshTimer =
            new Timer(NODE_REFRESH_DELAY,
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        refreshTimer = null;
                        updateNodesImmediate();
                    }
                });
        refreshTimer.setRepeats(false);
        refreshTimer.setCoalesce(true);
        refreshTimer.start();
    }

    /** Return true iff there is a pending node update scheduled */
    public boolean isNodeUpdatePending() {
        return refreshTimer != null;
    }

//    private void releaseNodes() {
//        // There is most likely a better way to do this Tor, its to allow me to get further with
//        // leaks.
//        if (prevNodes != null) {
////            // Ensure that the property change listeners are cleared out.
////            // We could consider using weak listeners too.
////            for (int i = 0; i < prevNodes.length; i++) {
////                if (prevNodes[i] instanceof DesignBeanNode) {
////                    ((DesignBeanNode)prevNodes[i]).setDataObject(null);
//////                } else if (prevNodes[i] instanceof DocumentCompNode) {
//////                    ((DocumentCompNode)prevNodes[i]).setDataObject(null);
////                } else {
////                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
////                            new IllegalStateException("Not expected node=" + prevNodes[i])); // NOI18N
////                }
////            }
//
//            prevNodes = null;
//        }
//    }

    /**
     * Same as {@link updateNodes} but this request is processed immediately/synchronously
     */
    public void updateNodesImmediate() {
        if (refreshTimer != null) {
            refreshTimer.stop();
            refreshTimer = null;
        }

////        releaseNodes();
//
////        DataObject dobj = webform.getDataObject();
////        DesignerTopComp topcomp = webform.getTopComponent();
//
////        // Ensure that the tray is no longer appearing selected
////        topcomp.clearTraySelection();
//
////        if ((selected != null) && (selected.size() > 0)) {
//////            Iterator it = selected.iterator();
////
////            //Node[] nodes = new Node[selected.size()];
////            ArrayList nodes = new ArrayList(selected.size());
////
//////            while (it.hasNext()) {
//////                FormObject fo = (FormObject)it.next();
////            for (FormObject fo : selected) {
////                DesignBean component = fo.component;
//        if (!selectedComponents.isEmpty()) {
//            //Node[] nodes = new Node[selected.size()];
//            List<Node> nodes = new ArrayList<Node>(selectedComponents.size());
//            for (SelectedComponent sc : selectedComponents) {
////                DesignBean component = WebForm.getDomProviderService().getMarkupDesignBeanForElement(sc.componentRootElement);
////                Node n = DesigntimeIdeBridgeProvider.getDefault().getNodeRepresentation(component);
//                Node n = WebForm.getDomProviderService().getNodeRepresentation(sc.componentRootElement);
////                n.setDataObject(dobj);
//                nodes.add(n);
//            }
//
//            Node[] nds = nodes.toArray(new Node[nodes.size()]);
//
////            if (topcomp.isShowing()) {
////                topcomp.requestActive();
////            }
//
////            DesignerUtils.setActivatedNodes(topcomp, nds);
//            webform.tcSetActivatedNodes(nds);
////            prevNodes = nds;
//            
//        } else {
//// <>
////            Node[] nodes = new Node[1];
////
////            if (documentComponent == null) {
////                documentComponent = new DocumentComp(webform);
////            } else {
////                documentComponent.sync();
////            }
////
////            DocumentCompNode node = new DocumentCompNode(documentComponent, dobj);
////
////            //node.setDataObject(dobj);
////            nodes[0] = node;
////
////            if (topcomp.isShowing()) {
////                topcomp.requestActive();
////            }
////
////            DesignerUtils.setActivatedNodes(topcomp, nodes);
////            prevNodes = nodes;
//// ====
//            // XXX Ugly way of maintaining activated nodes.
//            // TODO Redesign activated nodes retrieval/setting.
//            Node[] nodes;
////            FacesModel facesModel = webform.getModel();
////            DesignBean rootBean = facesModel.getRootBean();
////            if (rootBean == null) {
////                // XXX If the model is busted then it is supposed to be OK, there is an error, see e.g. #6478860.
////                if (!facesModel.isBusted()) {
////                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
////                            new IllegalStateException("Invalid FacesModel, it is not busted and its root design bean is null, facesModel=" + facesModel)); // NOI18N
////                }
////                nodes = new Node[0];
////            } else {
////                Node n = DesigntimeIdeBridgeProvider.getDefault().getNodeRepresentation(rootBean);
//////                n.setDataObject(dobj);
////                nodes = new Node[] {n};
////            }
//            Node rootNode = webform.getRootBeanNode();
//            nodes = rootNode == null ? new Node[0] : new Node[] {rootNode};
//            
////            DesignerUtils.setActivatedNodes(topcomp, nodes);
//            webform.tcSetActivatedNodes(nodes);
////            // XXX Why is this here? Why it should get active based on node setting?
////            if (topcomp.isShowing()) {
////                topcomp.requestActive();
////            }
////            prevNodes = nodes;
//// </>
//        }
//
//        // XXX #101248 This seems to be needless.
////        if(!webform.isGridMode()) {
////            selectTextBetweenSelectedNodes();
////        }
        webform.fireSelectionChanged(new SelectionDesignerEvent(webform));
    }
    
    
    private static class SelectionDesignerEvent implements DesignerEvent {
        private final WebForm webForm;
        
        public SelectionDesignerEvent(WebForm webForm) {
            this.webForm = webForm;
        }

        public Designer getDesigner() {
            return webForm;
        }

        public Box getBox() {
            return null;
        }
    } // End of SelectionDesignerEvent.
    
    
//    private void selectTextBetweenSelectedNodes() {
////        Position start = null, end = null;
//        DomPosition start = null;
//        DomPosition end = null;
//        
////        if ((selected != null) && (selected.size() > 0)) {
//////            Iterator it = selected.iterator();
////            Position startNode, endNode;
//////            while (it.hasNext()) {
//////                FormObject fo = (FormObject)it.next();
////            for (FormObject fo : selected) {
////                startNode = Position.create(fo.component.getElement(), false);
//        if (!selectedComponents.isEmpty()) {
////            Position startNode, endNode;
//            DomPosition startNode;
//            DomPosition endNode;
//            
//            for (SelectedComponent sc : selectedComponents) {
////                Element sourceElement = WebForm.getDomProviderService().getMarkupDesignBeanForElement(sc.componentRootElement).getElement();
//                Element sourceElement = WebForm.getDomProviderService().getSourceElement(sc.componentRootElement);
////                startNode = Position.create(sourceElement, false);
//                startNode = webform.createDomPosition(sourceElement, false);
//                
//                if(start == null || startNode.isEarlierThan(start)) {
//                    start = startNode;
//                }
////                endNode = Position.create(fo.component.getElement(), true);
////                endNode = Position.create(sourceElement, true);
//                endNode = webform.createDomPosition(sourceElement, true);
//                
//                if(end == null || end.isEarlierThan(endNode)) {
//                    end = endNode;
//                }
//            }
//        } else {
////            start = Position.NONE;
////            end   = Position.NONE;
//            start = DomPosition.NONE;
//            end   = DomPosition.NONE;
//        }
////        if(webform.getPane().getCaret() != null) {
//        if(webform.getPane().hasCaret()) {
////            webform.getPane().getCaret().setDot(start);
////            webform.getPane().getCaret().moveDot(end);
//            webform.getPane().setCaretDot(start);
//            webform.getPane().moveCaretDot(end);
//        }
//    }

//    /** Return set of currently selected nodes */
//    public Node[] getSelectedNodes() {
//        if (refreshTimer != null) {
//            // The nodes may not be current - there's a pending update. Cancel the
//            // update request and update the nodes immediately instead
//            refreshTimer.stop();
//            refreshTimer = null;
//            updateNodesImmediate();
//        }
//
////        return webform.getTopComponent().getActivatedNodes();
//        return webform.tcGetActivatedNodes();
//    }

    /** Return the primary selection, if any. The primary is the most recently
     * "touched" DesignBean in the selection. For example, if you swipe select
     * a set of 10 components and then right click on one of them, all are selected
     * but the clicked-on component is primary. Similarly, if you shift-click toggle
     * components in the selection, the most recently clicked component is primary.
     * This component is highlighted using a different color than the rest.
     */
    public /*MarkupDesignBean*/Element getPrimary() {
        return primary;
//        return WebForm.getDomProviderService().getMarkupDesignBeanForElement(primary);
    }

    /**
     * Choose a bean as the primary selection bean
     */
    public void setPrimary(/*MarkupDesignBean*/Element primary) {
        this.primary = primary;
//        this.primary = getComponentRootElementForMarkupDesignBean(primary);
    }

    /**
     * XXX Bad architecture.
     * TODO Get rid of this, and update the primary as selection changes.
     * 
     * Ask the selection manager to pick a primary selection object, if it has
     * one or more selection objects but none designated as primary.
     * If one is already primary, it is kept as the primary.
     */
    public void pickPrimary() {
        if (primary == null && !selectedComponents.isEmpty()) {
//            Iterator it = selected.iterator();
//            FormObject fob = (FormObject)it.next();
//            primary = fob.component;
            Iterator<SelectedComponent> it = selectedComponents.iterator();
            SelectedComponent sc = it.next();
            primary = sc.componentRootElement;
        }
    }

//    /**
//     * Return an iterator for iterating through the MarkupDesignBeans in the
//     * selection set.
//     */
//    public Iterator<MarkupDesignBean> iterator() {
//        return new BeanIterator();
//    }
    
//    public MarkupDesignBean[] getSelectedMarkupDesignBeans() {
//        List<MarkupDesignBean> markupDesignBeans = new ArrayList<MarkupDesignBean>();
//        for (Iterator<MarkupDesignBean> it = iterator(); it.hasNext(); ) {
//            MarkupDesignBean markupDesignBean = it.next();
//            if (markupDesignBean != null && !markupDesignBeans.contains(markupDesignBean)) {
//                markupDesignBeans.add(markupDesignBean);
//            }
//        }
//        return markupDesignBeans.toArray(new MarkupDesignBean[markupDesignBeans.size()]);
//    }
    public Element[] getSelectedComponentRootElements() {
        List<Element> componentRootElements = new ArrayList<Element>();
        List<SelectedComponent> scs = new ArrayList<SelectedComponent>(selectedComponents);
        for (SelectedComponent selectedComponent : scs) {
            if (selectedComponent == null) {
                continue;
            }
            Element componentRootElement = selectedComponent.componentRootElement;
            if (componentRootElement != null) {
                componentRootElements.add(componentRootElement);
            }
        }
        return componentRootElements.toArray(new Element[componentRootElements.size()]);
    }

    // --------------------------------------------------------------------
    // Selection Management: Deal with the selection-set
    // --------------------------------------------------------------------
    // The selection set consists of FormObjects, where each page object
    // represents a selectable object on the page (which may correspond
    // to toolbar components).

//    // TODO JSF specific, get rid of it, replace it with the latter.
//    /** Represents a "component" in the view. Not called component to avoid
//     * confusion with the component class of the actual component from the
//     * toolbar. This is simply a wrapper object used to hold information
//     * pertaining to selections etc. */
//    private static class FormObject { // TODO Rename SelectedComponent
//
//        MarkupDesignBean component = null;
//
//        /** Resizability of this component. A bit mask representing state
//         * according to Constants.ResizeConstraints. */
//        int resizeConstraints;
//    }

    // TODO This should replace the above usage.
    /** Represents a "component" in the view. Not called component to avoid
     * confusion with the component class of the actual component from the
     * toolbar. This is simply a wrapper object used to hold information
     * pertaining to selections etc. */
    private static class SelectedComponent {
        private Element componentRootElement;
        /** Resizability of this component. A bit mask representing state
         * according to Constants.ResizeConstraints. */
//        private int resizeConstraints;
        private ResizeConstraint[] resizeConstraints;
    } // End of SelectedComponent.

    
//    /** Iterator for iterating through the selection contents */
//    private class BeanIterator implements Iterator<MarkupDesignBean> {
//        private Iterator<SelectedComponent> iterator;
//
//        private BeanIterator() {
////            if ((selected != null) && (selected.size() > 0)) {
////                iterator = selected.iterator();
////            }
//            iterator = selectedComponents.iterator();
//        }
//
//        public boolean hasNext() {
//            if (iterator != null) {
//                return iterator.hasNext();
//            } else {
//                return false;
//            }
//        }
//
//        public MarkupDesignBean next() {
////            FormObject fo = (FormObject)iterator.next();
////
////            return fo.component;
//            SelectedComponent sc = iterator.next();
//            return WebForm.getDomProviderService().getMarkupDesignBeanForElement(sc.componentRootElement);
//        }
//
//        public void remove() {
//            throw new UnsupportedOperationException();
//        }
//    }
    
    void selectBean(/*DesignBean select*/Element componentRootElement) {
//        // XXX SelectionManager properly dispatches to DesignerTopComp now
//        if (select != null) {
////            if (DesignerTopComp.SHOW_TRAY && LiveUnit.isTrayBean(select)) {
////                webform.getTopComponent().selectTrayBean(select);
////            } else
////            if (LiveUnit.isVisualBean(select)) {
//                selectComponents(new DesignBean[] { select }, true);
////            } else {
////                // Some non-tray, non-visual bean, such as a validator.
////                // Simply show it as selected for now.
////                webform.getTopComponent().selectNonTrayBeans(new DesignBean[] { select });
////            }
//
//            select = null;
//        }
        if (componentRootElement != null) {
            selectComponents(new Element[] {componentRootElement}, true);
        }
    }
    
    
//    /** XXX Copied from DesignerActions. */
//    public static void selectParent(/*DesignBean designBean*/Element componentRootElement) {
////        if (designBean == null) {
////            return;
////        }
////        WebForm webform = WebForm.findWebFormForDesignContext(designBean.getDesignContext());
////        if (webform == null) {
////            return;
////        }
////        
////        webform.getSelection().doSelectParent(designBean);
//        if (componentRootElement == null) {
//            return;
//        }
//        WebForm webform = WebForm.findWebFormForElement(componentRootElement);
//        if (webform == null) {
//            return;
//        }
//        
//        webform.getSelection().doSelectParent(componentRootElement);
//    }
//    
//    private void doSelectParent(/*DesignBean designBean*/Element componentRootElement) {
//        webform.getManager().finishInlineEditing(false);
//
////        SelectionManager sm = webform.getSelection();
//
////        DesignBean designBean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
////        if (designBean == null) {
////            return;
////        }
//        if (componentRootElement == null) {
//            return;
//        }
//        
////        if (!canSelectParent(designBean)) {
//        if (!canSelectParent(componentRootElement)) {
//            if (!webform.isGridMode()) {
//                // in flow mode, allow you to escape your way to the top;
//                // clear selection and show document properties
//                clearSelection(true);
//            }
//
//            return;
//        }
//
//        // Find the closest selectable parent that is actually
//        // selectable!
////        DesignBean parent = designBean.getBeanParent();
//        Element parentComponentRootElement = WebForm.getDomProviderService().getParentComponent(componentRootElement);
////        ModelViewMapper mapper = webform.getMapper();
//
////        while (parent != null) {
//        while (parentComponentRootElement != null) {
//            // Find the visual (non-form, non-root) parent and select it
////            Element element = FacesSupport.getElement(parent);
////            Element element = Util.getElement(parent);
////            Element element = WebForm.getDomProviderService().getElement(parent);
//            Element element = WebForm.getDomProviderService().getSourceElement(parentComponentRootElement);
//
//            if (element != null) {
////                CssBox box = mapper.findBox(element);
//                CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), element);
//
//                if (box != null) {
////                    selectComponents(new DesignBean[] { parent }, true);
////                    if (parent instanceof MarkupDesignBean) {
//                        selectComponents(new Element[] { parentComponentRootElement }, true);
//                        break;
////                    }
//                }
//            }
//
////            parent = parent.getBeanParent();
//            parentComponentRootElement = WebForm.getDomProviderService().getParentComponent(parentComponentRootElement);
//        }
//    }
    
    void selectComponent(Element componentRootElement) {
        if (componentRootElement == null) {
            return;
        }
        
        CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), componentRootElement);
        if (box != null) {
            selectComponents(new Element[] { componentRootElement }, true);
        }
    }

}
