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

package org.netbeans.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.openide.util.NbBundle;

/**
 *  Code Folding Side Bar. Component responsible for drawing folding signs and responding 
 *  on user fold/unfold action.
 *
 *  @author  Martin Roskanin
 */
public class CodeFoldingSideBar extends JComponent implements SettingsChangeListener, Accessible {
    
    protected JTextComponent component;    
    
    protected Font font;
    protected Color foreColor;
    protected Color backColor;
    private boolean enabled;
    
    protected List visibleMarks = new ArrayList();
    
    /** Paint operations */
    public static final int PAINT_NOOP             = 0;
    public static final int PAINT_MARK             = 1;
    public static final int PAINT_LINE             = 2;
    public static final int PAINT_END_MARK         = 3;
    public static final int SINGLE_PAINT_MARK      = 4;
    
    
    /** Creates a new instance of CodeFoldingSideBar */
    public CodeFoldingSideBar() {
        setOpaque(true);
    }
    
    public CodeFoldingSideBar(JTextComponent component){
        super();
        this.component = component;

        Settings.addSettingsChangeListener(this);
        settingsChange(null); // ensure that the settings get initialized

        FoldingMouseListener listener = new FoldingMouseListener();
        addMouseListener(listener);
        FoldHierarchy foldHierarchy = FoldHierarchy.get(component);
        foldHierarchy.addFoldHierarchyListener(new SideBarFoldHierarchyListener());
        Document doc = getDocument();
        doc.addDocumentListener( new DocumentListener() {
                public void insertUpdate(DocumentEvent evt) {
                    if (!(evt instanceof BaseDocumentEvent)) return;
                    
                    BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
                    if (bevt.getLFCount() > 0) { // one or more lines inserted
                        repaint();
                    }    
                }
                
                public void removeUpdate(DocumentEvent evt) {
                    if (!(evt instanceof BaseDocumentEvent)) return;
                    
                    BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
                    if (bevt.getLFCount() > 0) { // one or more lines removed
                        repaint();
                    }    
                }
                
                public void changedUpdate(DocumentEvent evt) {
                }
                
            });
        setOpaque(true);
    }
    
    /**
     * Enable or disable visibility of the side bar.
     *
     * @param enable whether the side bar should be enabled or not.
     * @return whether visibility change occurred or not.
     */
    private boolean enableSideBarComponent(boolean enable){
        if (enable == enabled) {
            return false;
        }
        enabled = enable;
        updatePreferredSize();
        return true;
    }
    
    private void updatePreferredSize() {
        if (enabled) {
            setPreferredSize(new Dimension(getColoringFont().getSize(), component.getHeight()));    
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }else{
            setPreferredSize(new Dimension(0,0));
            setMaximumSize(new Dimension(0,0));
        }
        revalidate();
}
    
    private Font getDefaultColoringFont(){
        // font in folding coloring not available, get default (or inherited)
        EditorUI eui = getEditorUI();
        if (eui != null) {
            Coloring defaultColoring = eui.getDefaultColoring();
            if (defaultColoring!=null) {
                if (defaultColoring.getFont() != null) {
                    return defaultColoring.getFont(); 
                }
            }
        }
        return SettingsDefaults.defaultFont;
    }
    
    protected Font getColoringFont(){
        if (font == null) {
            EditorUI eui = getEditorUI();
            if (eui != null) {
                Coloring foldColoring = eui.getColoring(SettingsNames.CODE_FOLDING_BAR_COLORING);
                if (foldColoring != null) {
                    if (foldColoring.getFont() != null) {
                        font = foldColoring.getFont();
                    }
                }
            }
            
            if (font == null) {
                font = getDefaultColoringFont();
            }
        }
        return font;
    }

    // overriding due to issue #60304
    public @Override void update(Graphics g) {
    }
    
    
    protected Color getForeColor(){
        if (foreColor != null) return foreColor;
        Coloring foldColoring = getEditorUI().getColoring(SettingsNames.CODE_FOLDING_BAR_COLORING);
        if (foldColoring != null && foldColoring.getForeColor()!=null){
            foreColor = foldColoring.getForeColor();
            return foreColor;
        }
        foreColor = getDefaultForeColor();
        return foreColor;
    }
    
    private Color getDefaultForeColor(){
        // font in folding coloring not available, get default (or inherited)
        Coloring defaultColoring = getEditorUI().getDefaultColoring();
        if (defaultColoring!=null && defaultColoring.getForeColor()!=null){
            return defaultColoring.getForeColor();
        }
        return SettingsDefaults.defaultForeColor;
    }
    
    private Color getDefaultBackColor(){
        // font in folding coloring not available, get default (or inherited)
        EditorUI eui = getEditorUI();
        if (eui != null) {
            Coloring defaultColoring = eui.getDefaultColoring();
            if (defaultColoring != null) {
                return defaultColoring.getBackColor();
            }
        }
        return SettingsDefaults.defaultBackColor;
    }
    
    protected Color getBackColor(){
        if (backColor != null) return backColor;
        Coloring foldColoring = getEditorUI().getColoring(SettingsNames.CODE_FOLDING_BAR_COLORING);
        if (foldColoring != null && foldColoring.getBackColor()!=null){
            backColor = foldColoring.getBackColor();
            return backColor;
        }
        backColor = getDefaultBackColor();
        return backColor;
    }
    
    public void settingsChange(SettingsChangeEvent evt) {
        EditorUI editorUI = getEditorUI();
        if (editorUI == null) {
            return;
        }

        // enable/disable the side bar
        Document doc = component.getDocument();
        Class kitClass = (doc instanceof BaseDocument)
            ? Utilities.getKitClass(component)
            : BaseKit.class;
        
        Font origFont = font;
        
        Coloring foldingColoring = editorUI.getColoring(SettingsNames.CODE_FOLDING_BAR_COLORING);
        
        if (foldingColoring != null) {
            font = foldingColoring.getFont();
            foreColor = foldingColoring.getForeColor();
            backColor = foldingColoring.getBackColor();
        }
        
        if (font == null) {
            font = getDefaultColoringFont();
        }
        if (foreColor == null) {
            this.foreColor = getDefaultForeColor();
        }
        if (backColor == null) {
            backColor = getDefaultBackColor();
        }

        Boolean newEnabled = (Boolean)Settings.getValue(kitClass, SettingsNames.CODE_FOLDING_ENABLE);
        boolean change = enableSideBarComponent((newEnabled != null) ? newEnabled.booleanValue() : false);

        if (!change) { // not revalidated yet
            if (font != null && font.equals(origFont)) {
                repaint();
            } else {
                updatePreferredSize();
                revalidate();
            }
        }
    }
    
    protected void collectPaintInfos(Fold fold, Map map, int level, int startIndex, int endIndex){
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) return;
        for (int i=0; i<fold.getFoldCount(); i++){
            Fold childFold = fold.getFold(i);
            int startViewIndex = rootView.getViewIndex(childFold.getStartOffset(), Position.Bias.Forward);
            int endViewIndex = rootView.getViewIndex(childFold.getEndOffset(), Position.Bias.Forward);
            if (endViewIndex>=startIndex && startViewIndex<=endIndex && startViewIndex <= endViewIndex
                    && level < 20 // #90931 - prevent stack overflow by a max fold nesting level
            ) {
                collectPaintInfos(childFold, map, level+1, startIndex, endIndex);
            }
        }
        int foldStartOffset = fold.getStartOffset();
        int foldEndOffset = fold.getEndOffset();
        int docLength = rootView.getDocument().getLength();
        
        if (foldEndOffset > docLength) return;
        
        int startViewIndex = rootView.getViewIndex(foldStartOffset, Position.Bias.Forward);
        int endViewIndex = rootView.getViewIndex(foldEndOffset, Position.Bias.Forward);

        try{
            View view;
            BaseTextUI textUI = (BaseTextUI)component.getUI();
            Shape viewShape;
            Rectangle viewRect;
            int markY = -1;
            int y=-1;
            
            // PAINT_MARK
            if (startIndex <= startViewIndex){
                view = rootView.getView(startViewIndex);
                viewShape = textUI.modelToView(component, view.getStartOffset());
                if (viewShape != null) {
                    viewRect = viewShape.getBounds();
                    y = viewRect.y + viewRect.height;
                    boolean isSingleLineFold = startViewIndex == endViewIndex;
                    if (fold.isCollapsed() || isSingleLineFold){
                        map.put(new Integer(viewRect.y), 
                            new CodeFoldingSideBar.PaintInfo((isSingleLineFold?SINGLE_PAINT_MARK:PAINT_MARK), level, viewRect.y, viewRect.height, fold.isCollapsed()));
                        return;
                    }

                    markY = viewRect.y;
                    map.put(new Integer(viewRect.y), new CodeFoldingSideBar.PaintInfo(PAINT_MARK, level, viewRect.y, viewRect.height, fold.isCollapsed()));
                }
            }
            
            //PAINT_LINE
            if (level == 0){
                int loopStart = (startViewIndex<startIndex)? startIndex : startViewIndex+1;
                int loopEnd = (endViewIndex>endIndex)? endIndex : endViewIndex;
                viewRect = null;
                for (int i=loopStart; i<=loopEnd; i++){
                    view = rootView.getView(i);
                    //viewShape = textUI.modelToView(component, view.getStartOffset());
                    if (view instanceof DrawEngineLineView && y > -1){
                        int h = (int)((DrawEngineLineView)view).getLayoutMajorAxisPreferredSpan();
                        viewRect = new Rectangle(0, y, 0, h);
                        if (i<loopEnd && loopEnd>loopStart) y += h;
                    }else{
                        viewShape = textUI.modelToView(component, view.getStartOffset()); 
                        if (viewShape != null){
                            viewRect = viewShape.getBounds();
                            if (i<loopEnd && loopEnd>loopStart) {
                                y = viewRect.y + viewRect.height;
                            }
                        }
                    }
                    if (viewRect != null && !map.containsKey(new Integer(viewRect.y))){
                        map.put(new Integer(viewRect.y), new CodeFoldingSideBar.PaintInfo(PAINT_LINE, level, viewRect.y, viewRect.height));
                    }
                }
            }
            
            //PAINT_END_MARK
            if (endViewIndex<=endIndex){
                view = rootView.getView(endViewIndex);
                //viewShape = textUI.modelToView(component, view.getStartOffset());
                viewRect = null;
                if (view instanceof DrawEngineLineView && y > -1 && level == 0){
                    int h = (int)((DrawEngineLineView)view).getLayoutMajorAxisPreferredSpan();
                    viewRect = new Rectangle(0, y, 0, h);
                    y += h;
                }else{
                    viewShape = textUI.modelToView(component, view.getStartOffset()); 
                    if (viewShape !=null){
                        viewRect = viewShape.getBounds();
                        y = viewRect.y + viewRect.height;
                    }
                }
                if (viewRect !=null && markY!=viewRect.y){
                    PaintInfo pi = (PaintInfo)map.get(new Integer(viewRect.y));
                    if (pi==null || (pi.getPaintOperation() != PAINT_MARK && pi.getPaintOperation() != SINGLE_PAINT_MARK) || level>=pi.getInnerLevel()){
                        map.put(new Integer(viewRect.y), new CodeFoldingSideBar.PaintInfo(PAINT_END_MARK, level, viewRect.y, viewRect.height));
                    }
                }
            }
        }catch(BadLocationException ble){
            ble.printStackTrace();
        }
        
    }
    
    protected List getPaintInfo(int startPos, int endPos){
        List ret = new ArrayList();
        
        List foldList = getFoldList(startPos, endPos);
        if (foldList.size() == 0) {
            return ret;
        }

        BaseTextUI textUI = (BaseTextUI)component.getUI();
        javax.swing.text.Element rootElem = textUI.getRootView(component).getElement();
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) return ret;

        Document doc = component.getDocument();
        if (!(doc instanceof BaseDocument)) return ret;

        BaseDocument bDoc = (BaseDocument) doc;
        
        Map map = new HashMap();

        bDoc.readLock();

        try {
            FoldHierarchy hierarchy = FoldHierarchy.get(component);
            hierarchy.lock();
            try {

                int startViewIndex = rootView.getViewIndex(startPos,Position.Bias.Forward);
                int endViewIndex = rootView.getViewIndex(endPos,Position.Bias.Forward);

                for (int i=0; i<foldList.size(); i++){
                    Fold fold = (Fold)foldList.get(i);
                    collectPaintInfos(fold, map, 0, startViewIndex, endViewIndex);
                }

            } finally {
                hierarchy.unlock();
            }
                
        } finally {
            bDoc.readUnlock();
        }
        
        return new ArrayList(map.values());
    }

    protected EditorUI getEditorUI(){
        return Utilities.getEditorUI(component);
    }
    
    protected Document getDocument(){
        return component.getDocument();
    }

    
    private Fold getLastLineFold(FoldHierarchy hierarchy, int rowStart, int rowEnd){
        Fold fold = FoldUtilities.findNearestFold(hierarchy, rowStart);
        while (fold != null && fold.getStartOffset()<rowEnd){
            Fold nextFold = FoldUtilities.findNearestFold(hierarchy, (fold.isCollapsed()) ? fold.getEndOffset() : fold.getStartOffset()+1);
            if (nextFold == fold) return fold;
            if (nextFold!=null && nextFold.getStartOffset() < rowEnd){
                fold = nextFold;
            }else{
                return fold;
            }
        }
        return fold;
    }
    
    protected void performAction(Mark mark){
        BaseTextUI textUI = (BaseTextUI)component.getUI();
        javax.swing.text.Element rootElem = textUI.getRootView(component).getElement();

        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) return;
        try{
            int startViewIndex = rootView.getViewIndex(textUI.getPosFromY(mark.y+mark.size/2),
                Position.Bias.Forward);            
            View view = rootView.getView(startViewIndex);
            
            // Find corresponding fold
            FoldHierarchy foldHierarchy = FoldHierarchy.get(component);
            AbstractDocument adoc = (AbstractDocument)foldHierarchy.getComponent().getDocument();
            adoc.readLock();
            try {
                foldHierarchy.lock();
                try {
                    
                    int viewStartOffset = view.getStartOffset();
                    int rowStart = javax.swing.text.Utilities.getRowStart(component, viewStartOffset);
                    int rowEnd = javax.swing.text.Utilities.getRowEnd(component, viewStartOffset);
                    Fold clickedFold = getLastLineFold(foldHierarchy, rowStart, rowEnd);//FoldUtilities.findNearestFold(foldHierarchy, viewStartOffset);
                    if (clickedFold != null && clickedFold.getStartOffset() < view.getEndOffset()) {
                        foldHierarchy.toggle(clickedFold); 
                    }
                } finally {
                    foldHierarchy.unlock();
                }
            } finally {
                adoc.readUnlock();
            }
            //System.out.println((mark.isFolded ? "Unfold" : "Fold") + " action performed on:"+view); //[TEMP]
        }catch(BadLocationException ble){
            ble.printStackTrace();
        }
    }
    
    protected int getMarkSize(Graphics g){
        if (g != null){
            FontMetrics fm = g.getFontMetrics(getColoringFont());
            if (fm != null){
                int ret = fm.getAscent() - fm.getDescent();
                return ret - ret%2;
            }
        }
        return -1;
    }

    protected @Override void paintComponent(Graphics g) {
        
        if (!enabled) return;
        Rectangle clip = getVisibleRect();//g.getClipBounds();
        visibleMarks.clear();
        g.setColor(getBackColor());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        g.setColor(getForeColor());

        javax.swing.plaf.TextUI textUI = component.getUI(); 
        if (!(textUI instanceof BaseTextUI)) return;
        BaseTextUI baseTextUI = (BaseTextUI)textUI;
        
        try{
            int startPos = baseTextUI.getPosFromY(clip.y);
            int endPos = baseTextUI.viewToModel(component, Short.MAX_VALUE/2, clip.y+clip.height);
        
            List ps = getPaintInfo(startPos, endPos);
            Font defFont = getColoringFont();
            
            for (int i = 0; i <ps.size(); i++){

                PaintInfo paintInfo = (PaintInfo)ps.get(i);
                
                if (paintInfo.getPaintOperation() == PAINT_NOOP && paintInfo.getInnerLevel() == 0) continue; //No painting for this view
                
                boolean isFolded = paintInfo.isCollapsed();
                int y = paintInfo.getPaintY();
                int height = paintInfo.getPaintHeight();
                int markSize = getMarkSize(g);
                int halfMarkSize = markSize/2;
                int markX = (defFont.getSize()-markSize)/2; // x position of mark rectangle
                int markY = y + g.getFontMetrics(defFont).getDescent(); // y position of mark rectangle
                int plusGap = (int)Math.round(markSize/3.8); // distance between mark rectangle vertical side and start/end of minus sign
                int lineX = markX + halfMarkSize; // x position of the centre of mark
                
                int paintOperation = paintInfo.getPaintOperation();
                if (paintOperation == PAINT_MARK || paintOperation == SINGLE_PAINT_MARK){
                    g.drawRect(markX, markY, markSize, markSize);
                    g.drawLine(plusGap+markX, markY+halfMarkSize, markSize+markX-plusGap, markY+halfMarkSize);
                    if (isFolded){
                        g.drawLine(lineX, markY+plusGap, lineX, markY+markSize-plusGap);
                    }else{
                        if (paintOperation != SINGLE_PAINT_MARK) g.drawLine(lineX, markY + markSize, lineX, y + height);
                    }
                    if (paintInfo.getInnerLevel() > 0){ //[PENDING]
                        g.drawLine(lineX, y, lineX, markY);
                        g.drawLine(lineX, markY + markSize, lineX, y + height);
                    }
                    visibleMarks.add(new Mark(markX, markY, markSize, isFolded));
                } else if (paintOperation == PAINT_LINE){
                    g.drawLine(lineX, y, lineX, y + height );
                } else if (paintOperation == PAINT_END_MARK){
                    g.drawLine(lineX, y, lineX, y + height/2);
                    g.drawLine(lineX, y + height/2, lineX + halfMarkSize, y + height/2);
                    if (paintInfo.getInnerLevel() > 0){//[PENDING]
                        g.drawLine(lineX, y + height/2, lineX, y + height);
                    }
                }
                
            }
        }catch(BadLocationException ble){
            ble.printStackTrace();
        }
    }

    private List getFoldList(int start, int end) {
        FoldHierarchy hierarchy = FoldHierarchy.get(component);
        
        hierarchy.lock();
        try {
            List ret = new ArrayList();
            Fold rootFold = hierarchy.getRootFold();
            int index = FoldUtilities.findFoldEndIndex(rootFold, start);
            int foldCount = rootFold.getFoldCount();
            while (index < foldCount) {
                Fold f = rootFold.getFold(index);
                if (f.getStartOffset() <= end) {
                    ret.add(f);
                } else {
                    break; // no more relevant folds
                }
                index++;
            }
            return ret;
        } finally {
            hierarchy.unlock();
        }
    }
    
    
    public class PaintInfo{
        
        int paintOperation;
        int innerLevel;
        int paintY;
        int paintHeight;
        boolean isCollapsed;
        
        public PaintInfo(int paintOperation, int innerLevel, int paintY, int paintHeight, boolean isCollapsed){
            this.paintOperation = paintOperation;
            this.innerLevel = innerLevel;
            this.paintY = paintY;
            this.paintHeight = paintHeight;
            this.isCollapsed = isCollapsed;
        }

        public PaintInfo(int paintOperation, int innerLevel, int paintY, int paintHeight){
            this(paintOperation, innerLevel, paintY, paintHeight, false);
        }
        
        public int getPaintOperation(){
            return paintOperation;
        }
        
        public int getInnerLevel(){
            return innerLevel;
        }
        
        public int getPaintY(){
            return paintY;
        }
        
        public int getPaintHeight(){
            return paintHeight;
        }
        
        public boolean isCollapsed(){
            return isCollapsed;
        }
        
        public void setPaintOperation(int paintOperation){
            this.paintOperation = paintOperation;
        }
        
        public void setInnerLevel(int innerLevel){
            this.innerLevel = innerLevel;
        }
        
        public @Override String toString(){
            StringBuffer sb = new StringBuffer("");
            if (paintOperation == PAINT_NOOP){
                sb.append("PAINT_NOOP\n"); // NOI18N
            }else if (paintOperation == PAINT_MARK){
                sb.append("PAINT_MARK\n"); // NOI18N
            }else if (paintOperation == PAINT_LINE){
                sb.append("PAINT_LINE\n"); // NOI18N
            }else if (paintOperation == PAINT_END_MARK) {
                sb.append("PAINT_END_MARK\n"); // NOI18N
            }
            sb.append("level:"+innerLevel); // NOI18N
            sb.append("\ncollapsedFold:"+isCollapsed); // NOI18N
            return sb.toString();
        }
    }
    
    /** Keeps info of visible folding mark */
    public class Mark{
        public int x;
        public int y;
        public int size;
        public boolean isFolded;
        
        public Mark(int x, int y, int size, boolean isFolded){
            this.x = x;
            this.y = y;
            this.size = size;
            this.isFolded = isFolded;
        }
    }
    
    /** Listening on clicking on folding marks */
    class FoldingMouseListener extends MouseAdapter {
        
        public FoldingMouseListener(){
            super();
        }

        private Mark getClickedMark(MouseEvent e){
            if (e == null || !SwingUtilities.isLeftMouseButton(e)) return null;
            int x = e.getX();
            int y = e.getY();
            for (int i=0; i<visibleMarks.size(); i++){
                Mark mark = (Mark)visibleMarks.get(i);
                if (x >= mark.x && x <= (mark.x + mark.size) && y >= mark.y && y <= (mark.y + mark.size)){
                    return mark;
                }
            }
            return null;
        }
        
        @Override
        public void mousePressed (MouseEvent e) {
            Mark mark = getClickedMark(e);
            if (mark!=null){
                e.consume();
                performAction(mark);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // #102288 - missing event consuming caused quick doubleclicks to break
            // fold expanding/collapsing and move caret to the particular line
            e.consume();
        }

    }

    class SideBarFoldHierarchyListener implements FoldHierarchyListener{
    
        public SideBarFoldHierarchyListener(){
        }
        
        public void foldHierarchyChanged(FoldHierarchyEvent evt) {
            repaint();
        }
    }
    
    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                public @Override AccessibleRole getAccessibleRole() {
                    return AccessibleRole.PANEL;
                }
            };
            accessibleContext.setAccessibleName(NbBundle.getMessage(CodeFoldingSideBar.class, "ACSN_CodeFoldingSideBar")); //NOI18N
        accessibleContext.setAccessibleDescription(NbBundle.getMessage(CodeFoldingSideBar.class, "ACSD_CodeFoldingSideBar")); //NOI18N
        }
        return accessibleContext;
    }

}
