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

package org.netbeans.modules.visualweb.api.designer;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.PrintStream;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider.DomPosition;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Interface representing the designer component.
 *
 * @author Peter Zavadsky
 */
public interface Designer {

//    public JComponent getDesignerComponent();

//    // XXX Temp after moved TopComponent impl out >>>
//    public JComponent getVisualRepresentation();
//    public JComponent getToolbarRepresentation();
//    public Action[] getActions();
//    public Lookup getLookup();
//    public void componentOpened();
//    public void componentClosed();
//    public void componentShowing();
//    public void componentHidden();
//    public void componentActivated();
//    public void componentDeactivated();
//    public UndoRedo getUndoRedo();
//    public void setMultiViewCallback(MultiViewElementCallback multiViewElementCallback);
//    public CloseOperationState canCloseElement();
//    // XXX Temp after moved TopComponent impl out <<<

    public void startInlineEditing(Element componentRootElement, String propertyName);
    public void finishInlineEditing(boolean cancel);
    public boolean isInlineEditing();
    // XXX Hack
    public void invokeDeleteNextCharAction(ActionEvent evt); 
    // XXX
    public Transferable inlineCopyText(boolean isCut);

    public void selectComponent(Element componentRootElement);
    public int getSelectedCount();
    /** Gets selected componets (component root elements). */
    public Element[] getSelectedComponents();
    // XXX Suspicious? Also pick up if not ready.
    public Element getPrimarySelectedComponent();
    // XXX See above (this one doesn't pick up)
    public Element getPrimarySelection();
    
    public Element getSelectedContainer();
    
    // XXX Modification, get rid of update parameter.
    public void setSelectedComponents(Element[] componentRootElements, boolean update);
    public void clearSelection(boolean update);
            
    // XXX Get rid of
    public void syncSelection(boolean update);
    // XXX Get rid of
    public void updateSelectedNodes();
    // XXX 
    public void updateSelection();

//    public enum Alignment {
//        SNAP_TO_GRID,
//        TOP,
//        MIDDLE,
//        BOTTOM,
//        LEFT,
//        CENTER,
//        RIGHT
//    }
//    public void align(Alignment alignment);
//    public void snapToGrid();

    // XXX Move to document >>>
    public DomPosition computeNextPosition(DomPosition pos);
    public DomPosition computePreviousPosition(DomPosition pos);
    public boolean isInsideEditableRegion(DomPosition pos);
    // XXX Move to document <<<

    // >>> Boxes stuff
    /** Representing the individual box. Providing accessors (getters) only! */
    public interface Box {
        // XXX Get rid of this. See CssBox.UNINITIALIZED.
        public static final int UNINITIALIZED = Integer.MAX_VALUE - 2; // debugging
        
        public Element getComponentRootElement();
        public Element getElement();
        
        public Box getParent();
        public Box[] getChildren();
        
        // XXX Get rid of this.
        public HtmlTag getTag();
        // XXX Get rid of this.
        public Element getSourceElement();
        
        public int getWidth();
        public int getHeight();
        
        public int getX();
        public int getY();
        public int getZ();
        
        public int getAbsoluteX();
        public int getAbsoluteY();
        
        public int getRightMargin();
        public int getLeftMargin();
        
        public int getEffectiveTopMargin();
        
        // XXX Get rid of.
        public boolean isPositioned();
        public boolean isAbsolutelyPositioned();
        // XXX Very suspicious.
        public Box getPositionedBy();
        
        public void list(PrintStream outputStream, int indent);
    } // End of Box.
    
    public Box getPageBox();
    
    public Box findBox(int x, int y);
    // XXX Get rid of.
    public Box findBoxForSourceElement(Element sourceElement);
    
    public Box findBoxForComponentRootElement(Element componentRootElement);
    
    public int snapX(int x, Box positionedBy);
    public int snapY(int y, Box positionedBy);
    // <<< Boxes stuff
    
    
    public Point getCurrentPos();
    public void clearCurrentPos();
    public Element getPositionElement();
    public int getGridWidth();
    public int getGridHeight();
    
    // XXX Designer settings properties
    /** show grid */
    public static final String PROP_GRID_SHOW = "gridShow"; // NOI18N
    public static final String PROP_GRID_SNAP = "gridSnap"; // NOI18N
    public static final String PROP_GRID_WIDTH = "gridWidth"; // NOI18N
    public static final String PROP_GRID_HEIGHT = "gridHeight"; // NOI18N
    public static final String PROP_PAGE_SIZE = "pageSize"; // NOI18N
    public static final String PROP_SHOW_DECORATIONS = "showDecorations"; // NOI18N
    public static final String PROP_DEFAULT_FONT_SIZE = "defaultFontSize"; // NOI18N
    // XXX Make not weak, and also add removal
    public void addWeakPreferenceChangeListener(PreferenceChangeListener l);
    
    public void registerListeners();
    public void unregisterListeners();
    
    public ActionMap getPaneActionMap();
    public void paneRequestFocus();
    // XXX This should be the designer itself.
    public JComponent createPaneComponent();
    // XXX Get rid of.
    public void updatePaneViewPort();
    
    public boolean hasPaneCaret();
    public void setPaneCaret(DomPosition pos);
    
    public void resetPanePageBox();
    public void redoPaneLayout(boolean immediate);
    
//    public void setRenderFailureShown(boolean shown);
//    public boolean isRenderFailureShown();
//    public void updateErrors();
//    public void showErrors(boolean on);
    
//    public void updateGridMode();
    
    // XXX Get rid of this
    public void performEscape();

    public void resetAll();
    public void changeNode(Node node, Node parent, boolean wasMove);
    public void removeNode(Node node, Node parent);
    public void insertNode(Node node, Node parent);
    
    public void setPaneGrid(boolean gridMode);
    
    public void detachDomDocument();
    
    public void showDropMatch(Element componentRootElement, Element regionElement, int dropType);
    public void clearDropMatch();
}
