/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.editor;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.editor.view.spi.LockView;

/**
 * View over collapsed area of the fold.
 * <br>
 * The collapsed area spans one or more lines and it is presented as three dots.
 *
 * @author Martin Roskanin
 */
/* package */ class CollapsedView extends View implements SettingsChangeListener {

    private static final int MARGIN_WIDTH = 4;
    
    private Position startPos;
    
    private Position endPos;
    
    private String foldDescription;

    private Font font;
    
    private Color foreColor;
    
    private Color backColor;
    
    
    /** Creates a new instance of CollapsedView */
    public CollapsedView(Element elem, Position startPos, Position endPos, String foldDescription) {
        super(elem);
        
        this.startPos = startPos;
        this.endPos = endPos;
        this.foldDescription = foldDescription;
        Settings.addSettingsChangeListener(this);
    }
    
    private JTextComponent getComponent() {
        return (JTextComponent)getContainer();
    }
    
    private BaseTextUI getBaseTextUI(){
        JTextComponent comp = getComponent();
        return (comp!=null)?(BaseTextUI)comp.getUI():null;
    }
    
    private EditorUI getEditorUI(){
        BaseTextUI btui = getBaseTextUI();
        return (btui!=null) ? btui.getEditorUI() : null;
    }
    
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    }
    
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    }
    
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    }
    
    public Document getDocument() {
        View parent = getParent();
        return (parent == null) ?  null : parent.getDocument();
    }
    
    public int getStartOffset() {
        return startPos.getOffset();
    }
    
    public int getEndOffset() {
        return endPos.getOffset();
    }
    
    protected void forwardUpdate(DocumentEvent.ElementChange ec, 
				      DocumentEvent e, Shape a, ViewFactory f) {
    }
    
    protected void forwardUpdateToView(View v, DocumentEvent e, 
					   Shape a, ViewFactory f) {
    }
    
    public float getAlignment(int axis) {
	return 0f;
    }
    
    public float getPreferredSpan(int axis){
        switch (axis) {
            case Y_AXIS:
                return getEditorUI().getLineHeight();
            case X_AXIS:
                return getCollapsedFoldStringWidth();
        }
        return 1f;
    }

    
    private int getCollapsedFoldStringWidth() {
        JTextComponent comp = getComponent();
        if (comp==null) return 0;
        FontMetrics fm = FontMetricsCache.getFontMetrics(getColoringFont(), comp);
        if (fm==null) return 0;
        return fm.stringWidth(foldDescription) + 2 * MARGIN_WIDTH;
    }
    
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        return new Rectangle(a.getBounds().x, a.getBounds().y, getCollapsedFoldStringWidth(), getEditorUI().getLineHeight());
    }
    
    public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
        return getStartOffset();
    }
    
    public void paint(Graphics g, Shape allocation){
        Rectangle r = allocation.getBounds();

        g.setColor(getBackColor());
        g.fillRect(r.x, r.y, r.width, r.height);
        
        g.setColor(getForeColor());
        g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
        
        g.setFont(getColoringFont());
        g.drawString(foldDescription, r.x + MARGIN_WIDTH, r.y + getEditorUI().getLineAscent() - 1);
    }
    
    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, 
					 int direction, Position.Bias[] biasRet) 
      throws BadLocationException {
	biasRet[0] = Position.Bias.Forward;
	switch (direction) {
	case NORTH:
	case SOUTH:
	{
	    JTextComponent target = (JTextComponent) getContainer();
	    Caret c = (target != null) ? target.getCaret() : null;
	    // YECK! Ideally, the x location from the magic caret position
	    // would be passed in.
	    Point mcp;
	    if (c != null) {
		mcp = c.getMagicCaretPosition();
	    }
	    else {
		mcp = null;
	    }
	    int x;
	    if (mcp == null) {
		Rectangle loc = target.modelToView(pos);
		x = (loc == null) ? 0 : loc.x;
	    }
	    else {
		x = mcp.x;
	    }
	    if (direction == NORTH) {
		pos = Utilities.getPositionAbove(target, pos, x);
	    }
	    else {
		pos = Utilities.getPositionBelow(target, pos, x);
	    }
	}
	    break;
	case WEST:
	    if(pos == -1) {
		pos = Math.max(0, getStartOffset());
	    }
	    else {
                if (b == Position.Bias.Backward){
                    pos = Math.max(0, getStartOffset());
                }else{
                    pos = Math.max(0, getStartOffset() - 1);
                }
	    }
	    break;
	case EAST:
	    if(pos == -1) {
		pos = getStartOffset();
	    }
	    else {
		pos = Math.min(getEndOffset(), getDocument().getLength());
                //JTextComponent target = (JTextComponent) getContainer();
                //if (target!=null && Utilities.getRowEnd(target, pos) == pos) pos = Math.min(pos+1, getDocument().getLength());
	    }
	    break;
	default:
	    throw new IllegalArgumentException("Bad direction: " + direction); // NOI18N
	}
	return pos;
    }    

    private View getExpandedView(){
        Element parentElem = getElement().getParentElement();
        int sei = parentElem.getElementIndex(getStartOffset());
        int so = parentElem.getElement(sei).getStartOffset();
        
        int eei = parentElem.getElementIndex(getEndOffset());
        int eo = parentElem.getElement(eei).getEndOffset();
        
        LockView fakeView = new LockView(
        new DrawEngineFakeDocView(parentElem, so, eo, false)
        );
        RootView rootView = new RootView();
        rootView.setView(fakeView);
        return fakeView;
    }
    
    public String getToolTipText(float x, float y, Shape allocation){
        ToolTipSupport tts = ((ExtEditorUI)getEditorUI()).getToolTipSupport();
        JComponent toolTip = new FoldingToolTip(getExpandedView(), getEditorUI());
        tts.setToolTip(toolTip, PopupManager.ScrollBarBounds, PopupManager.Largest, -FoldingToolTip.BORDER_WIDTH, 0);
        return "";
    }
    
    public void settingsChange(SettingsChangeEvent evt) {
        if (evt == null || org.netbeans.editor.Utilities.getKitClass(getComponent()) != evt.getKitClass()) return;
        
        String defaultColoringName = SettingsNames.DEFAULT_COLORING+SettingsNames.COLORING_NAME_SUFFIX;
        String foldingColoringName = SettingsNames.CODE_FOLDING_COLORING+SettingsNames.COLORING_NAME_SUFFIX;
        EditorUI editorUI = getEditorUI();
        if (editorUI==null) return;
        Coloring foldingColoring = editorUI.getColoring(SettingsNames.CODE_FOLDING_COLORING);
        Coloring defaultColoring = editorUI.getDefaultColoring();
        
        Font foldingFont = null;
        Color foldingForeColor = null;
        Color foldingBackColor = null;
        if (foldingColoring!=null){
            foldingFont = foldingColoring.getFont();
            foldingForeColor = foldingColoring.getForeColor();
            foldingBackColor = foldingColoring.getBackColor();
        }
        
        if (defaultColoringName.equals(evt.getSettingName())){
            
            if (foldingForeColor == null){
                // inherited fore color
                Color tempColor = getDefaultForeColor();
                if (!tempColor.equals(foreColor)){
                    foreColor = tempColor;
                }
            }

            if (foldingBackColor == null){
                // inherited back color
                Color tempColor = getDefaultBackColor();
                if (!tempColor.equals(backColor)){
                    backColor = tempColor;
                }
            }
            
            // Font size change
            Font tempFont = getDefaultColoringFont();
            if (!tempFont.equals(font) && foldingFont==null){
                // if different font and foldingFont is inherited
                font = tempFont;
                //setPreferredSize(new Dimension(font.getSize(), component.getHeight()));
            }
            //repaint();
            
        }else if (foldingColoringName.equals(evt.getSettingName())){
            // Code folding coloring change
            if (foldingColoring == null) return;

            Color tempColor = foldingColoring.getForeColor();
            foreColor = (tempColor!=null) ? tempColor : getDefaultForeColor();
            
            tempColor = foldingColoring.getBackColor();
            backColor = (tempColor!=null) ? tempColor : getDefaultBackColor();
            
            if (foldingFont == null){ //inherit
                Font tempFont = getDefaultColoringFont();
                if (!tempFont.equals(font)){
                    font = tempFont;
                    //setPreferredSize(new Dimension(font.getSize(), component.getHeight()));
                }
            }else{
                if (!foldingFont.equals(font)){
                    font = foldingFont;
                    //setPreferredSize(new Dimension(font.getSize(), component.getHeight()));
                }
            }
            
            //repaint();
        }
    }
    
    private Font getDefaultColoringFont(){
        // font in folding coloring not available, get default (or inherited)
        EditorUI editorUI = getEditorUI();
        if (editorUI!=null){
            Coloring defaultColoring = editorUI.getDefaultColoring();
            if (defaultColoring!=null){
                if (defaultColoring.getFont() != null){
                    return defaultColoring.getFont(); 
                }
            }
        }
        return SettingsDefaults.defaultFont;
    }
    
    protected Font getColoringFont(){
        if (font != null) return font;
        EditorUI editorUI = getEditorUI();
        if (editorUI!=null){
            Coloring foldColoring = editorUI.getColoring(SettingsNames.CODE_FOLDING_COLORING);
            if (foldColoring != null){
                if (foldColoring.getFont()!=null){
                    font = foldColoring.getFont();
                    return font;
                }
            }
        }
        font = getDefaultColoringFont();
        return font;
    }
    
    protected Color getForeColor(){
        if (foreColor != null) return foreColor;
        EditorUI editorUI = getEditorUI();
        if (editorUI!=null){
            Coloring foldColoring = editorUI.getColoring(SettingsNames.CODE_FOLDING_COLORING);
            if (foldColoring != null && foldColoring.getForeColor()!=null){
                foreColor = foldColoring.getForeColor();
                return foreColor;
            }
        }
        foreColor = getDefaultForeColor();
        return foreColor;
    }
    
    private Color getDefaultForeColor(){
        EditorUI editorUI = getEditorUI();
        if (editorUI!=null){
            // font in folding coloring not available, get default (or inherited)
            Coloring defaultColoring = editorUI.getDefaultColoring();
            if (defaultColoring!=null && defaultColoring.getForeColor()!=null){
                return defaultColoring.getForeColor();
            }
        }
        return SettingsDefaults.defaultForeColor;
    }
    
    private Color getDefaultBackColor(){
        EditorUI editorUI = getEditorUI();
        if (editorUI!=null){
            // font in folding coloring not available, get default (or inherited)
            Coloring defaultColoring = editorUI.getDefaultColoring();
            if (defaultColoring!=null){
                return defaultColoring.getBackColor();
            }
        }
        return SettingsDefaults.defaultBackColor;
    }
    
    protected Color getBackColor(){
        if (backColor != null) return backColor;
        EditorUI editorUI = getEditorUI();
        if (editorUI!=null){
            Coloring foldColoring = editorUI.getColoring(SettingsNames.CODE_FOLDING_COLORING);
            if (foldColoring != null && foldColoring.getBackColor()!=null){
                backColor = foldColoring.getBackColor();
                return backColor;
            }
        }
        backColor = getDefaultBackColor();
        return backColor;
    }

    
    class RootView extends View {

        RootView() {
            super(null);
        }

        void setView(View v) {
            if (view != null) {
                // get rid of back reference so that the old
                // hierarchy can be garbage collected.
                view.setParent(null);
            }
            view = v;
            if (view != null) {
                view.setParent(this);
            }
        }

	/**
	 * Fetches the attributes to use when rendering.  At the root
	 * level there are no attributes.  If an attribute is resolved
	 * up the view hierarchy this is the end of the line.
	 */
        public AttributeSet getAttributes() {
	    return null;
	}

        /**
         * Determines the preferred span for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the span the view would like to be rendered into.
         *         Typically the view is told to render into the span
         *         that is returned, although there is no guarantee.
         *         The parent may choose to resize or break the view.
         */
        public float getPreferredSpan(int axis) {
            if (view != null) {
                return view.getPreferredSpan(axis);
            }
            return 10;
        }

        /**
         * Determines the minimum span for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the span the view would like to be rendered into.
         *         Typically the view is told to render into the span
         *         that is returned, although there is no guarantee.
         *         The parent may choose to resize or break the view.
         */
        public float getMinimumSpan(int axis) {
            if (view != null) {
                return view.getMinimumSpan(axis);
            }
            return 10;
        }

        /**
         * Determines the maximum span for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the span the view would like to be rendered into.
         *         Typically the view is told to render into the span
         *         that is returned, although there is no guarantee.
         *         The parent may choose to resize or break the view.
         */
        public float getMaximumSpan(int axis) {
	    return Integer.MAX_VALUE;
        }

        /**
         * Specifies that a preference has changed.
         * Child views can call this on the parent to indicate that
         * the preference has changed.  The root view routes this to
         * invalidate on the hosting component.
         * <p>
         * This can be called on a different thread from the
         * event dispatching thread and is basically unsafe to
         * propagate into the component.  To make this safe,
         * the operation is transferred over to the event dispatching 
         * thread for completion.  It is a design goal that all view
         * methods be safe to call without concern for concurrency,
         * and this behavior helps make that true.
         *
         * @param child the child view
         * @param width true if the width preference has changed
         * @param height true if the height preference has changed
         */ 
        public void preferenceChanged(View child, boolean width, boolean height) {
            
        }

        /**
         * Determines the desired alignment for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the desired alignment, where 0.0 indicates the origin
         *     and 1.0 the full span away from the origin
         */
        public float getAlignment(int axis) {
            if (view != null) {
                return view.getAlignment(axis);
            }
            return 0;
        }

        /**
         * Renders the view.
         *
         * @param g the graphics context
         * @param allocation the region to render into
         */
        public void paint(Graphics g, Shape allocation) {
            if (view != null) {
                Rectangle alloc = (allocation instanceof Rectangle) ?
		          (Rectangle)allocation : allocation.getBounds();
		setSize(alloc.width, alloc.height);
                view.paint(g, allocation);
            }
        }
        
        /**
         * Sets the view parent.
         *
         * @param parent the parent view
         */
        public void setParent(View parent) {
            throw new Error("Can't set parent on root view"); // NOI18N
        }

        /** 
         * Returns the number of views in this view.  Since
         * this view simply wraps the root of the view hierarchy
         * it has exactly one child.
         *
         * @return the number of views
         * @see #getView
         */
        public int getViewCount() {
            return 1;
        }

        /** 
         * Gets the n-th view in this container.
         *
         * @param n the number of the view to get
         * @return the view
         */
        public View getView(int n) {
            return view;
        }

	/**
	 * Returns the child view index representing the given position in
	 * the model.  This is implemented to return the index of the only
	 * child.
	 *
	 * @param pos the position >= 0
	 * @return  index of the view representing the given position, or 
	 *   -1 if no view represents that position
	 * @since 1.3
	 */
        public int getViewIndex(int pos, Position.Bias b) {
	    return 0;
	}
    
        /**
         * Fetches the allocation for the given child view. 
         * This enables finding out where various views
         * are located, without assuming the views store
         * their location.  This returns the given allocation
         * since this view simply acts as a gateway between
         * the view hierarchy and the associated component.
         *
         * @param index the index of the child
         * @param a  the allocation to this view.
         * @return the allocation to the child
         */
        public Shape getChildAllocation(int index, Shape a) {
            return a;
        }

        /**
         * Provides a mapping from the document model coordinate space
         * to the coordinate space of the view mapped to it.
         *
         * @param pos the position to convert
         * @param a the allocated region to render into
         * @return the bounding box of the given position
         */
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            if (view != null) {
                return view.modelToView(pos, a, b);
            }
            return null;
        }

	/**
	 * Provides a mapping from the document model coordinate space
	 * to the coordinate space of the view mapped to it.
	 *
	 * @param p0 the position to convert >= 0
	 * @param b0 the bias toward the previous character or the
	 *  next character represented by p0, in case the 
	 *  position is a boundary of two views. 
	 * @param p1 the position to convert >= 0
	 * @param b1 the bias toward the previous character or the
	 *  next character represented by p1, in case the 
	 *  position is a boundary of two views. 
	 * @param a the allocated region to render into
	 * @return the bounding box of the given position is returned
	 * @exception BadLocationException  if the given position does
	 *   not represent a valid location in the associated document
	 * @exception IllegalArgumentException for an invalid bias argument
	 * @see View#viewToModel
	 */
	public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a) throws BadLocationException {
	    if (view != null) {
		return view.modelToView(p0, b0, p1, b1, a);
	    }
	    return null;
	}

        /**
         * Provides a mapping from the view coordinate space to the logical
         * coordinate space of the model.
         *
         * @param x x coordinate of the view location to convert
         * @param y y coordinate of the view location to convert
         * @param a the allocated region to render into
         * @return the location within the model that best represents the
         *    given point in the view
         */
        public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
            if (view != null) {
                int retValue = view.viewToModel(x, y, a, bias);
		return retValue;
            }
            return -1;
        }

        /**
         * Provides a way to determine the next visually represented model 
         * location that one might place a caret.  Some views may not be visible,
         * they might not be in the same order found in the model, or they just
         * might not allow access to some of the locations in the model.
         *
         * @param pos the position to convert >= 0
         * @param a the allocated region to render into
         * @param direction the direction from the current position that can
         *  be thought of as the arrow keys typically found on a keyboard.
         *  This may be SwingConstants.WEST, SwingConstants.EAST, 
         *  SwingConstants.NORTH, or SwingConstants.SOUTH.  
         * @return the location within the model that best represents the next
         *  location visual position.
         * @exception BadLocationException
         * @exception IllegalArgumentException for an invalid direction
         */
        public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, 
                                             int direction,
                                             Position.Bias[] biasRet) 
            throws BadLocationException {
            if( view != null ) {
                int nextPos = view.getNextVisualPositionFrom(pos, b, a,
						     direction, biasRet);
		if(nextPos != -1) {
		    pos = nextPos;
		}
		else {
		    biasRet[0] = b;
		}
            } 
            return pos;
        }

        /**
         * Gives notification that something was inserted into the document
         * in a location that this view is responsible for.
         *
         * @param e the change information from the associated document
         * @param a the current allocation of the view
         * @param f the factory to use to rebuild if the view has children
         */
        public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.insertUpdate(e, a, f);
            }
        }
        
        /**
         * Gives notification that something was removed from the document
         * in a location that this view is responsible for.
         *
         * @param e the change information from the associated document
         * @param a the current allocation of the view
         * @param f the factory to use to rebuild if the view has children
         */
        public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.removeUpdate(e, a, f);
            }
        }

        /**
         * Gives notification from the document that attributes were changed
         * in a location that this view is responsible for.
         *
         * @param e the change information from the associated document
         * @param a the current allocation of the view
         * @param f the factory to use to rebuild if the view has children
         */
        public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.changedUpdate(e, a, f);
            }
        }

        /**
         * Returns the document model underlying the view.
         *
         * @return the model
         */
        public Document getDocument() {
            EditorUI editorUI = getEditorUI();
            return (editorUI==null) ? null : editorUI.getDocument();
        }
        
        /**
         * Returns the starting offset into the model for this view.
         *
         * @return the starting offset
         */
        public int getStartOffset() {
            if (view != null) {
                return view.getStartOffset();
            }
            return getElement().getStartOffset();
        }

        /**
         * Returns the ending offset into the model for this view.
         *
         * @return the ending offset
         */
        public int getEndOffset() {
            if (view != null) {
                return view.getEndOffset();
            }
            return getElement().getEndOffset();
        }

        /**
         * Gets the element that this view is mapped to.
         *
         * @return the view
         */
        public Element getElement() {
            if (view != null) {
                return view.getElement();
            }
            return view.getDocument().getDefaultRootElement();
        }

        /**
         * Breaks this view on the given axis at the given length.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @param len specifies where a break is desired in the span
         * @param the current allocation of the view
         * @return the fragment of the view that represents the given span
         *   if the view can be broken, otherwise null
         */
        public View breakView(int axis, float len, Shape a) {
            throw new Error("Can't break root view"); // NOI18N
        }

        /**
         * Determines the resizability of the view along the
         * given axis.  A value of 0 or less is not resizable.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the weight
         */
        public int getResizeWeight(int axis) {
            if (view != null) {
                return view.getResizeWeight(axis);
            }
            return 0;
        }

        /**
         * Sets the view size.
         *
         * @param width the width
         * @param height the height
         */
        public void setSize(float width, float height) {
            if (view != null) {
                view.setSize(width, height);
            }
        }

        /**
         * Fetches the container hosting the view.  This is useful for
         * things like scheduling a repaint, finding out the host 
         * components font, etc.  The default implementation
         * of this is to forward the query to the parent view.
         *
         * @return the container
         */
        public Container getContainer() {
            EditorUI editorUI = getEditorUI();
            return (editorUI==null) ? null : editorUI.getComponent();
        }
        
        /**
         * Fetches the factory to be used for building the
         * various view fragments that make up the view that
         * represents the model.  This is what determines
         * how the model will be represented.  This is implemented
         * to fetch the factory provided by the associated
         * EditorKit unless that is null, in which case this
         * simply returns the BasicTextUI itself which allows
         * subclasses to implement a simple factory directly without
         * creating extra objects.  
         *
         * @return the factory
         */
        public ViewFactory getViewFactory() {
            EditorUI editorUI = getEditorUI();
            if (editorUI!=null){
                BaseKit kit = Utilities.getKit(editorUI.getComponent());

                ViewFactory f = kit.getViewFactory();
                if (f != null) {
                    return f;
                }
            }
            return getBaseTextUI();
        }

        private View view;

    }
    
    
}
