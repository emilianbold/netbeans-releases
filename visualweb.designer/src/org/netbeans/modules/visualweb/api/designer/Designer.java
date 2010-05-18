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

package org.netbeans.modules.visualweb.api.designer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.EventListener;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomRange;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.openide.util.Lookup;
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
    public void inlineEditComponents(Element[] componentRootElements);
    // XXX Hack
    public void invokeDeleteNextCharAction(ActionEvent evt); 
    // XXX
    public Transferable inlineCopyText(boolean isCut);

    public void selectComponent(Element componentRootElement);
    // XXX This is bad, get rid of.
    public void selectComponentDelayed(Element componentRootElement);
    
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
//    // XXX Get rid of
////    public void updateSelection();
//    public void updateNodes();

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
        
        public int getBlockWidth();
        public int getBlockHeight();
        
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
        public void list(PrintWriter writer, int indent);
    } // End of Box.
    
    
    /** Box representing external page (e.g. fragment). */
    public interface ExternalBox extends Box {
        public DomProvider getExternalDomProvider();
    } // End od ExternalBox.

    
    public Box getPageBox();
    
    public Box findBox(int x, int y);
    // XXX Get rid of.
    public Box findBoxForSourceElement(Element sourceElement);
    
    public Box findBoxForComponentRootElement(Element componentRootElement);
    
    // XXX Get rid of.
    public Box findBoxForElement(Element element);
    
    public int snapX(int x, Box positionedBy);
    public int snapY(int y, Box positionedBy);
    // <<< Boxes stuff
    
    
//    public Point getCurrentPos();
//    public void clearCurrentPos();
//    public Element getPositionElement();
//    public int getGridWidth();
//    public int getGridHeight();
    
    public Point getPastePoint();
    
//    // XXX Designer settings properties
//    /** show grid */
//    public static final String PROP_GRID_SHOW = "gridShow"; // NOI18N
//    public static final String PROP_GRID_SNAP = "gridSnap"; // NOI18N
//    public static final String PROP_GRID_WIDTH = "gridWidth"; // NOI18N
//    public static final String PROP_GRID_HEIGHT = "gridHeight"; // NOI18N
//    public static final String PROP_PAGE_SIZE = "pageSize"; // NOI18N
//    public static final String PROP_SHOW_DECORATIONS = "showDecorations"; // NOI18N
//    public static final String PROP_DEFAULT_FONT_SIZE = "defaultFontSize"; // NOI18N
//    // XXX Make not weak, and also add removal
//    public void addWeakPreferenceChangeListener(PreferenceChangeListener l);
    
//    public void registerListeners();
//    public void unregisterListeners();
    
    public ActionMap getPaneActionMap();
    public void paneRequestFocus();
    // XXX This should be the designer itself.
    public JComponent createPaneComponent();
    // XXX Get rid of.
    public void updatePaneViewPort();
    
    public boolean hasPaneCaret();
    public DomRange getPaneCaretRange();
    public void setPaneCaret(DomPosition pos);
    
    public void resetPanePageBox();
    public void redoPaneLayout(boolean immediate);
    
//    public void setRenderFailureShown(boolean shown);
//    public boolean isRenderFailureShown();
//    public void updateErrors();
//    public void showErrors(boolean on);
    
//    public void updateGridMode();
    
//    // XXX Get rid of this
//    public void performEscape();

    public void resetAll();
    public void changeNode(Node node, Node parent, Element[] changedElements);
    public void removeNode(Node node, Node parent);
    public void insertNode(Node node, Node parent);
    
    public void setPaneGrid(boolean gridMode);
    
    public void detachDomDocument();
    
    public void showDropMatch(Element componentRootElement, Element regionElement, int dropType);
    public void clearDropMatch();
    

    
    public void addDesignerListener(DesignerListener l);
    public void removeDesignerListener(DesignerListener l);
    
    public interface DesignerListener extends EventListener {
        /** When user performed action on the box. */
        public void userActionPerformed(DesignerEvent evt);
        /** When user invoked popup. */
        public void userPopupActionPerformed(DesignerPopupEvent evt);
        /** When user clicked on some element. */
        public void userElementClicked(DesignerClickEvent evt);
        /** When selection was changed. */
        public void selectionChanged(DesignerEvent evt);
    } // End of DesignerListener.
    
    public interface DesignerEvent {
        public Designer getDesigner();
        public Box getBox();
    } // End of DesignerEvent.
    
    public interface DesignerPopupEvent extends DesignerEvent {
        public Component getComponent();
        public Action[] getActions();
        public Lookup getContext();
        public int getX();
        public int getY();
    } // End of DesignerPopupEvent.
    
    public interface DesignerClickEvent extends DesignerEvent {
        public boolean isConsumed();
        public void consume();
        public int getClickCount();
    } // DesignerClickEvent
    
    /** XXX Render Context. */
    public interface RenderContext {
//        public DesignBean[] getBeansOfType(Class clazz);
        public Dimension getVieportDimension();
        public Point getViewportPosition();
        public int getNonTabbedTextWidth(char[] s, int offset, int length, FontMetrics metrics);
//        public Rectangle getBoundsForDesignBean(DesignBean designBean);
        public Rectangle getBoundsForComponent(Element componentRootElement);
    } // End of RenderContext.
    
    public RenderContext createRenderContext();

    
    public DomProvider[] getExternalDomProviders();
    
    /** Sets whether to paint size mask, typically used for fragments and portlets. */
    public void setPaintSizeMask(boolean paintSizeMask);
    /** Specifies whether the component paints size mask. */
    public boolean isPaintSizeMask();

//    public void setPageSizeWidth(int pageSizeWidth);
//    public void setPageSizeHeight(int pageSizeHeight);
    public void setPageSize(Dimension pageSize);
    public void setGridShow(boolean gridShow);
    public void setGridSnap(boolean gridSnap);
    public void setGridWidth(int gridWidth);
    public void setGridHeight(int gridHeight);
    public void setGridTraceWidth(int gridTraceWidth);
    public void setGridTraceHeight(int gridTraceHeight);
    public void setShowDecorations(boolean showDecorations);
    public void setDefaultFontSize(int defaultFontSize);
    
    
    public void copyBoxForElement(Element fromElement, Element toElement);
}
