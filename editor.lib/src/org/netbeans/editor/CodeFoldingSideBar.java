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

package org.netbeans.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
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
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib.SettingsConversions;
import org.netbeans.modules.editor.lib2.view.ViewHierarchy;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyEvent;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyListener;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *  Code Folding Side Bar. Component responsible for drawing folding signs and responding 
 *  on user fold/unfold action.
 *
 *  @author  Martin Roskanin
 */
public class CodeFoldingSideBar extends JComponent implements Accessible {

    private static final Logger LOG = Logger.getLogger(CodeFoldingSideBar.class.getName());

    /** This field should be treated as final. Subclasses are forbidden to change it. 
     * @deprecated Without any replacement.
     */
    protected Color backColor;
    /** This field should be treated as final. Subclasses are forbidden to change it. 
     * @deprecated Without any replacement.
     */
    protected Color foreColor;
    /** This field should be treated as final. Subclasses are forbidden to change it. 
     * @deprecated Without any replacement.
     */
    protected Font font;
    
    /** This field should be treated as final. Subclasses are forbidden to change it. */
    protected /*final*/ JTextComponent component;
    private volatile AttributeSet attribs;
    private Lookup.Result<? extends FontColorSettings> fcsLookupResult;
    private final LookupListener fcsTracker = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            attribs = null;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //EMI: This is needed as maybe the DEFAULT_COLORING is changed, the font is different
                    // and while getMarkSize() is used in paint() and will make the artifacts bigger,
                    // the component itself will be the same size and it must be changed.
                    // See http://www.netbeans.org/issues/show_bug.cgi?id=153316
                    updatePreferredSize();
                    CodeFoldingSideBar.this.repaint();
                }
            });
        }
    };
    private final Listener listener = new Listener();
    
    private boolean enabled = false;
    
    protected List<Mark> visibleMarks = new ArrayList<Mark>();
    
    /** Paint operations */
    public static final int PAINT_NOOP             = 0;
    /**
     * Normal opening +- marker
     */
    public static final int PAINT_MARK             = 1;
    
    /**
     * Vertical line - typically at the end of the screen
     */
    public static final int PAINT_LINE             = 2;
    
    /**
     * End angled line, without a sign
     */
    public static final int PAINT_END_MARK         = 3;
    
    /**
     * Single-line marker, both start and end
     */
    public static final int SINGLE_PAINT_MARK      = 4;
    
    private final Preferences prefs;
    private final PreferenceChangeListener prefsListener = new PreferenceChangeListener() {
        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt == null ? null : evt.getKey();
            if (key == null || SimpleValueNames.CODE_FOLDING_ENABLE.equals(key)) {
                updateColors();
                
                boolean newEnabled = prefs.getBoolean(SimpleValueNames.CODE_FOLDING_ENABLE, EditorPreferencesDefaults.defaultCodeFoldingEnable);
                if (enabled != newEnabled) {
                    enabled = newEnabled;
                    updatePreferredSize();
                }
            }
            SettingsConversions.callSettingsChange(CodeFoldingSideBar.this);
        }
    };
    
    private void checkRepaint(ViewHierarchyEvent vhe) {
        if (!vhe.isChangeY()) {
            // does not obscur sidebar graphics
            return;
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updatePreferredSize();
                CodeFoldingSideBar.this.repaint();
            }
        });
    }
    
    /**
     * @deprecated Don't use this constructor, it does nothing!
     */
    public CodeFoldingSideBar() {
        component = null;
        prefs = null;
        throw new IllegalStateException("Do not use this constructor!"); //NOI18N
    }

    public CodeFoldingSideBar(JTextComponent component){
        super();
        this.component = component;

        addMouseListener(listener);

        FoldHierarchy foldHierarchy = FoldHierarchy.get(component);
        foldHierarchy.addFoldHierarchyListener(WeakListeners.create(FoldHierarchyListener.class, listener, foldHierarchy));

        Document doc = getDocument();
        doc.addDocumentListener(WeakListeners.document(listener, doc));
        setOpaque(true);
        
        prefs = MimeLookup.getLookup(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(component)).lookup(Preferences.class);
        prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs));
        prefsListener.preferenceChange(null);
        
        ViewHierarchy.get(component).addViewHierarchyListener(new ViewHierarchyListener() {

            @Override
            public void viewHierarchyChanged(ViewHierarchyEvent evt) {
                checkRepaint(evt);
            }
            
        });
    }
    
    private void updatePreferredSize() {
        if (enabled) {
            setPreferredSize(new Dimension(getColoring().getFont().getSize(), component.getHeight()));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }else{
            setPreferredSize(new Dimension(0,0));
            setMaximumSize(new Dimension(0,0));
        }
        revalidate();
    }

    private void updateColors() {
        Coloring c = getColoring();
        this.backColor = c.getBackColor();
        this.foreColor = c.getForeColor();
        this.font = c.getFont();
    }

    /**
     * This method should be treated as final. Subclasses are forbidden to override it.
     * @return The background color used for painting this component.
     * @deprecated Without any replacement.
     */
    protected Color getBackColor() {
        if (backColor == null) {
            updateColors();
        }
        return backColor;
    }
    
    /**
     * This method should be treated as final. Subclasses are forbidden to override it.
     * @return The foreground color used for painting this component.
     * @deprecated Without any replacement.
     */
    protected Color getForeColor() {
        if (foreColor == null) {
            updateColors();
        }
        return foreColor;
    }
    
    /**
     * This method should be treated as final. Subclasses are forbidden to override it.
     * @return The font used for painting this component.
     * @deprecated Without any replacement.
     */
    protected Font getColoringFont() {
        if (font == null) {
            updateColors();
        }
        return font;
    }
    
    // overriding due to issue #60304
    public @Override void update(Graphics g) {
    }
    
    protected void collectPaintInfos(
        View rootView, Fold fold, Map<Integer, PaintInfo> map, int level, int startIndex, int endIndex
    ) throws BadLocationException {
        //never called
    }
    
    /*
     * Even collapsed fold MAY contain a continuation line, IF one of folds on the same line is NOT collapsed. Such a fold should
     * then visually span multiple lines && be marked as collapsed.
     */

    protected List<? extends PaintInfo> getPaintInfo(Rectangle clip) throws BadLocationException {
        javax.swing.plaf.TextUI textUI = component.getUI();
        if (!(textUI instanceof BaseTextUI)) {
            return Collections.<PaintInfo>emptyList();
        }
        BaseTextUI baseTextUI = (BaseTextUI)textUI;
        BaseDocument bdoc = Utilities.getDocument(component);
        if (bdoc == null) {
            return Collections.<PaintInfo>emptyList();
        }

        bdoc.readLock();
        try {
            int startPos = baseTextUI.getPosFromY(clip.y);
            int endPos = baseTextUI.viewToModel(component, Short.MAX_VALUE / 2, clip.y + clip.height);
            
            if (startPos < 0 || endPos < 0) {
                // editor window is not properly sized yet; return no infos
                return Collections.<PaintInfo>emptyList();
            }

            startPos = Utilities.getRowStart(bdoc, startPos);
            endPos = Utilities.getRowEnd(bdoc, endPos);
            
            FoldHierarchy hierarchy = FoldHierarchy.get(component);
            hierarchy.lock();
            try {
                View rootView = Utilities.getDocumentView(component);
                if (rootView != null) {
                    Object [] arr = getFoldList(hierarchy.getRootFold(), startPos, endPos);
                    @SuppressWarnings("unchecked")
                    List<? extends Fold> foldList = (List<? extends Fold>) arr[0];
                    int idxOfFirstFoldStartingInsideClip = (Integer) arr[1];

                    /*
                     * Note:
                     * 
                     * The Map is keyed by Y-VISUAL position of the fold mark, not the textual offset of line start.
                     * This is because several folds may occupy the same line, while only one + sign is displayed,
                     * and affect the last fold in the row.
                     */
                    Map<Integer, PaintInfo> map = new TreeMap<Integer, PaintInfo>();
                    // search backwards
                    for(int i = idxOfFirstFoldStartingInsideClip - 1; i >= 0; i--) {
                        Fold fold = foldList.get(i);
                        if (!traverseBackwards(fold, bdoc, baseTextUI, startPos, endPos, 0, map)) {
                            break;
                        }
                    }

                    // search forward
                    for(int i = idxOfFirstFoldStartingInsideClip; i < foldList.size(); i++) {
                        Fold fold = foldList.get(i);
                        if (!traverseForward(fold, bdoc, baseTextUI, startPos, endPos, 0, map)) {
                            break;
                        }
                    }

                    if (map.isEmpty() && foldList.size() > 0) {
                        assert foldList.size() == 1;
                        return Collections.singletonList(new PaintInfo(PAINT_LINE, 0, clip.y, clip.height, -1, -1));
                    } else {
                        return new ArrayList<PaintInfo>(map.values());
                    }
                } else {
                    return Collections.<PaintInfo>emptyList();
                }
            } finally {
                hierarchy.unlock();
            }
        } finally {
            bdoc.readUnlock();
        }
    }
    
    /**
     * Adds a paint info to the map. If a paintinfo already exists, it merges
     * the structures, so the painting process can just follow the instructions.
     * 
     * @param infos
     * @param yOffset
     * @param nextInfo 
     */
    private void addPaintInfo(Map<Integer, PaintInfo> infos, int yOffset, PaintInfo nextInfo) {
        PaintInfo prevInfo = infos.get(yOffset);
        nextInfo.mergeWith(prevInfo);
        infos.put(yOffset, nextInfo);
    }

    private boolean traverseForward(Fold f, BaseDocument doc, BaseTextUI btui, int lowerBoundary, int upperBoundary,int level,  Map<Integer, PaintInfo> infos) throws BadLocationException {
//        System.out.println("~~~ traverseForward<" + lowerBoundary + ", " + upperBoundary
//                + ">: fold=<" + f.getStartOffset() + ", " + f.getEndOffset() + "> "
//                + (f.getStartOffset() > upperBoundary ? ", f.gSO > uB" : "")
//                + ", level=" + level);
        
        if (f.getStartOffset() > upperBoundary) {
            return false;
        }

        int lineStartOffset1 = Utilities.getRowStart(doc, f.getStartOffset());
        int lineStartOffset2 = Utilities.getRowStart(doc, f.getEndOffset());
        int y1 = btui.getYFromPos(lineStartOffset1);
        int h = btui.getEditorUI().getLineHeight();
        int y2 = btui.getYFromPos(lineStartOffset2);

        if (y1 == y2) {
            // whole fold is on a single line
            addPaintInfo(infos, y1, new PaintInfo(SINGLE_PAINT_MARK, level, y1, h, f.isCollapsed(), lineStartOffset1, lineStartOffset2));
        } else {
            // fold spans multiple lines
            addPaintInfo(infos, y1, new PaintInfo(PAINT_MARK, level, y1, h, f.isCollapsed(), lineStartOffset1, lineStartOffset2));
        }

        // Handle end mark after possible inner folds were processed because
        // otherwise if there would be two nested folds both ending at the same line
        // then the end mark for outer one would be replaced by an end mark for inner one
        // (same key in infos map) and the painting code would continue to paint line marking a fold
        // until next fold is reached (or end of doc).
        if (y1 != y2 && !f.isCollapsed() && f.getEndOffset() <= upperBoundary) {
            addPaintInfo(infos, y2, new PaintInfo(PAINT_END_MARK, level, y2, h, lineStartOffset1, lineStartOffset2));
        }

        if (!f.isCollapsed()) {
            Object [] arr = getFoldList(f, lowerBoundary, upperBoundary);
            @SuppressWarnings("unchecked")
            List<? extends Fold> foldList = (List<? extends Fold>) arr[0];
            int idxOfFirstFoldStartingInsideClip = (Integer) arr[1];

            // search backwards
            for(int i = idxOfFirstFoldStartingInsideClip - 1; i >= 0; i--) {
                Fold fold = foldList.get(i);
                if (!traverseBackwards(fold, doc, btui, lowerBoundary, upperBoundary, level + 1, infos)) {
                    break;
                }
            }

            // search forward
            for(int i = idxOfFirstFoldStartingInsideClip; i < foldList.size(); i++) {
                Fold fold = foldList.get(i);
                if (!traverseForward(fold, doc, btui, lowerBoundary, upperBoundary, level + 1, infos)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private boolean traverseBackwards(Fold f, BaseDocument doc, BaseTextUI btui, int lowerBoundary, int upperBoundary, int level, Map<Integer, PaintInfo> infos) throws BadLocationException {
//        System.out.println("~~~ traverseBackwards<" + lowerBoundary + ", " + upperBoundary
//                + ">: fold=<" + f.getStartOffset() + ", " + f.getEndOffset() + "> "
//                + (f.getEndOffset() < lowerBoundary ? ", f.gEO < lB" : "")
//                + ", level=" + level);

        if (f.getEndOffset() < lowerBoundary) {
            return false;
        }

        int lineStartOffset1 = Utilities.getRowStart(doc, f.getStartOffset());
        int lineStartOffset2 = Utilities.getRowStart(doc, f.getEndOffset());
        int h = btui.getEditorUI().getLineHeight();

        if (lineStartOffset1 == lineStartOffset2) {
            // whole fold is on a single line
            int y1 = btui.getYFromPos(lineStartOffset1);
            addPaintInfo(infos, y1, new PaintInfo(SINGLE_PAINT_MARK, level, y1, h, f.isCollapsed(), lineStartOffset1, lineStartOffset1));
        } else {
            // fold spans multiple lines
            if (f.getStartOffset() >= upperBoundary) {
                int y1 = btui.getYFromPos(lineStartOffset1);
                addPaintInfo(infos, y1, new PaintInfo(PAINT_MARK, level, y1, h, f.isCollapsed(), lineStartOffset1, lineStartOffset2));
            }

            if (!f.isCollapsed() && f.getEndOffset() <= upperBoundary) {
                int y2 = btui.getYFromPos(lineStartOffset2);
                addPaintInfo(infos, y2, new PaintInfo(PAINT_END_MARK, level, y2, h, lineStartOffset1, lineStartOffset2));
            }
        }

        if (!f.isCollapsed()) {
            Object [] arr = getFoldList(f, lowerBoundary, upperBoundary);
            @SuppressWarnings("unchecked")
            List<? extends Fold> foldList = (List<? extends Fold>) arr[0];
            int idxOfFirstFoldStartingInsideClip = (Integer) arr[1];

            // search backwards
            for(int i = idxOfFirstFoldStartingInsideClip - 1; i >= 0; i--) {
                Fold fold = foldList.get(i);
                if (!traverseBackwards(fold, doc, btui, lowerBoundary, upperBoundary, level + 1, infos)) {
                    return false;
                }
            }

            // search forward
            for(int i = idxOfFirstFoldStartingInsideClip; i < foldList.size(); i++) {
                Fold fold = foldList.get(i);
                if (!traverseForward(fold, doc, btui, lowerBoundary, upperBoundary, level + 1, infos)) {
                    break;
                }
            }
        }

        return true;
    }
    
    protected EditorUI getEditorUI(){
        return Utilities.getEditorUI(component);
    }
    
    protected Document getDocument(){
        return component.getDocument();
    }


    private Fold getLastLineFold(FoldHierarchy hierarchy, int rowStart, int rowEnd, boolean shift){
        Fold fold = FoldUtilities.findNearestFold(hierarchy, rowStart);
        Fold prevFold = fold;
        while (fold != null && fold.getStartOffset()<rowEnd){
            Fold nextFold = FoldUtilities.findNearestFold(hierarchy, (fold.isCollapsed()) ? fold.getEndOffset() : fold.getStartOffset()+1);
            if (nextFold == fold) return fold;
            if (nextFold!=null && nextFold.getStartOffset() < rowEnd){
                prevFold = shift ? fold : nextFold;
                fold = nextFold;
            }else{
                return prevFold;
            }
        }
        return prevFold;
    }
    
    protected void performAction(Mark mark) {
        performAction(mark, false);
    }
    
    private void performActionAt(Mark mark, int mouseY) throws BadLocationException {
        if (mark != null) {
            return;
        }
        BaseDocument bdoc = Utilities.getDocument(component);
        BaseTextUI textUI = (BaseTextUI)component.getUI();

        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) return;
        
        int yOffset = textUI.getPosFromY(mouseY);
        bdoc.readLock();
        try {
            FoldHierarchy hierarchy = FoldHierarchy.get(component);
            hierarchy.lock();
            try {
                Fold f = FoldUtilities.findOffsetFold(hierarchy, yOffset);
                if (f == null) {
                    return;
                }
                if (f.isCollapsed()) {
                    LOG.log(Level.WARNING, "Clicked on a collapsed fold {0} at {1}", new Object[] { f, mouseY });
                    return;
                }
                int startOffset = f.getStartOffset();
                int endOffset = f.getEndOffset();
                
                int startY = textUI.getYFromPos(startOffset);
                int nextLineOffset = Utilities.getRowStart(bdoc, startOffset, 1);
                int nextY = textUI.getYFromPos(nextLineOffset);

                if (mouseY >= startY && mouseY <= nextY) {
                    LOG.log(Level.FINEST, "Starting line clicked, ignoring. MouseY={0}, startY={1}, nextY={2}",
                            new Object[] { mouseY, startY, nextY });
                    return;
                }

                startY = textUI.getYFromPos(endOffset);
                nextLineOffset = Utilities.getRowStart(bdoc, endOffset, 1);
                nextY = textUI.getYFromPos(nextLineOffset);

                if (mouseY >= startY && mouseY <= nextY) {
                    LOG.log(Level.FINEST, "End line clicked, ignoring. MouseY={0}, startY={1}, nextY={2}",
                            new Object[] { mouseY, startY, nextY });
                    return;
                }
                
                LOG.log(Level.FINEST, "Collapsing fold: {0}", f);
                hierarchy.collapse(f);
            } finally {
                hierarchy.unlock();
            }
        } finally {
            bdoc.readUnlock();
        }        
    }
    
    private void performAction(Mark mark, boolean shiftFold) {
        BaseTextUI textUI = (BaseTextUI)component.getUI();

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
                    Fold clickedFold = getLastLineFold(foldHierarchy, rowStart, rowEnd, shiftFold);//FoldUtilities.findNearestFold(foldHierarchy, viewStartOffset);
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
        } catch (BadLocationException ble) {
            LOG.log(Level.WARNING, null, ble);
        }
    }
    
    protected int getMarkSize(Graphics g){
        if (g != null){
            FontMetrics fm = g.getFontMetrics(getColoring().getFont());
            if (fm != null){
                int ret = fm.getAscent() - fm.getDescent();
                return ret - ret%2;
            }
        }
        return -1;
    }
    
    protected @Override void paintComponent(Graphics g) {
        if (!enabled) {
            return;
        }
        
        Rectangle clip = getVisibleRect();//g.getClipBounds();
        visibleMarks.clear();
        
        Coloring coloring = getColoring();
        g.setColor(coloring.getBackColor());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        g.setColor(coloring.getForeColor());

        AbstractDocument adoc = (AbstractDocument)component.getDocument();
        adoc.readLock();
        try {
            List<? extends PaintInfo> ps = getPaintInfo(clip);
            Font defFont = coloring.getFont();
            int markSize = getMarkSize(g);
            int halfMarkSize = markSize / 2;
            int markX = (defFont.getSize() - markSize) / 2; // x position of mark rectangle
            int plusGap = (int)Math.round(markSize / 3.8); // distance between mark rectangle vertical side and start/end of minus sign
            int lineX = markX + halfMarkSize; // x position of the centre of mark

            LOG.fine("CFSBar: PAINT START ------\n");
            int descent = g.getFontMetrics(defFont).getDescent();
            PaintInfo previousInfo = null;
            for(PaintInfo paintInfo : ps) {
                boolean isFolded = paintInfo.isCollapsed();
                int y = paintInfo.getPaintY();
                int height = paintInfo.getPaintHeight();
                int markY = y + descent; // y position of mark rectangle
                int paintOperation = paintInfo.getPaintOperation();

                if (previousInfo == null) {
                    if (paintInfo.hasLineIn()) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("prevInfo=NULL; y=" + y + ", PI:" + paintInfo + "\n"); // NOI18N
                        }
                        g.drawLine(lineX, clip.y, lineX, y);
                    }
                } else {
                    if (previousInfo.hasLineOut()) {
                        // Draw middle vertical line
                        int prevY = previousInfo.getPaintY();
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "prevInfo={0}; y=" + y + ", PI:" + paintInfo + "\n", previousInfo); // NOI18N
                        }
                        g.drawLine(lineX, prevY + previousInfo.getPaintHeight(), lineX, y);
                    }
                }

                if (paintInfo.hasSign()) {
                    g.drawRect(markX, markY, markSize, markSize);
                    g.drawLine(plusGap + markX, markY + halfMarkSize, markSize + markX - plusGap, markY + halfMarkSize);
                    String opStr = (paintOperation == PAINT_MARK) ? "PAINT_MARK" : "SINGLE_PAINT_MARK"; // NOI18N
                    if (isFolded) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine(opStr + ": folded; y=" + y + ", PI:" + paintInfo + "\n"); // NOI18N
                        }
                        g.drawLine(lineX, markY + plusGap, lineX, markY + markSize - plusGap);
                    }
                    if (paintOperation != SINGLE_PAINT_MARK) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine(opStr + ": non-single; y=" + y + ", PI:" + paintInfo + "\n"); // NOI18N
                        }
                    }
                    if (paintInfo.hasLineIn()) { //[PENDING]
                        g.drawLine(lineX, y, lineX, markY);
                    }
                    if (paintInfo.hasLineOut()) {
                        // This is an error in case there's a next paint info at the same y which is an end mark
                        // for this mark (it must be cleared explicitly).
                        g.drawLine(lineX, markY + markSize, lineX, y + height);
                    }
                    visibleMarks.add(new Mark(markX, markY, markSize, isFolded));

                } else if (paintOperation == PAINT_LINE) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("PAINT_LINE: y=" + y + ", PI:" + paintInfo + "\n"); // NOI18N
                    }
                    g.drawLine(lineX, y, lineX, y + height );

                } else if (paintOperation == PAINT_END_MARK) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("PAINT_END_MARK: y=" + y + ", PI:" + paintInfo + "\n"); // NOI18N
                    }
                    if (previousInfo == null || y != previousInfo.getPaintY()) {
                        g.drawLine(lineX, y, lineX, y + height / 2);
                        g.drawLine(lineX, y + height / 2, lineX + halfMarkSize, y + height / 2);
                        if (paintInfo.getInnerLevel() > 0) {//[PENDING]
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("  PAINT middle-line\n"); // NOI18N
                            }
                            g.drawLine(lineX, y + height / 2, lineX, y + height);
                        }
                    }
                }

                previousInfo = paintInfo;
            }

            if (previousInfo != null &&
                (previousInfo.getInnerLevel() > 0 ||
                 (previousInfo.getPaintOperation() == PAINT_MARK && !previousInfo.isCollapsed()))
            ) {
                g.drawLine(lineX, previousInfo.getPaintY() + previousInfo.getPaintHeight(), lineX, clip.y + clip.height);
            }

        } catch (BadLocationException ble) {
            LOG.log(Level.WARNING, null, ble);
        } finally {
            LOG.fine("CFSBar: PAINT END ------\n\n");
            adoc.readUnlock();
        }
    }

    private static Object [] getFoldList(Fold parentFold, int start, int end) {
        List<Fold> ret = new ArrayList<Fold>();

        int index = FoldUtilities.findFoldEndIndex(parentFold, start);
        int foldCount = parentFold.getFoldCount();
        int idxOfFirstFoldStartingInside = -1;
        while (index < foldCount) {
            Fold f = parentFold.getFold(index);
            if (f.getStartOffset() <= end) {
                ret.add(f);
            } else {
                break; // no more relevant folds
            }
            if (idxOfFirstFoldStartingInside == -1 && f.getStartOffset() >= start) {
                idxOfFirstFoldStartingInside = ret.size() - 1;
            }
            index++;
        }

        return new Object [] { ret, idxOfFirstFoldStartingInside != -1 ? idxOfFirstFoldStartingInside : ret.size() };
    }

    /**
     * This class should be never used by other code; will be made private
     */
    public class PaintInfo {
        
        int paintOperation;
        /**
         * level of the 1st marker on the line
         */
        int innerLevel;
        
        /**
         * Y-coordinate of the cell
         */
        int paintY;
        
        /**
         * Height of the paint cell
         */
        int paintHeight;
        
        /**
         * State of the marker (+/-)
         */
        boolean isCollapsed;
        
        /**
         * all markers on the line are collapsed
         */
        boolean allCollapsed;
        int startOffset;
        int endOffset;
        /**
         * nesting level of the last marker on the line
         */
        int outgoingLevel;
        
        /**
         * Force incoming line (from above) to be present
         */
        boolean lineIn;
        
        /**
         * Force outgoing line (down from marker) to be present
         */
        boolean lineOut;
        
        
        public PaintInfo(int paintOperation, int innerLevel, int paintY, int paintHeight, boolean isCollapsed, int startOffset, int endOffset){
            this.paintOperation = paintOperation;
            this.innerLevel = this.outgoingLevel = innerLevel;
            this.paintY = paintY;
            this.paintHeight = paintHeight;
            this.isCollapsed = this.allCollapsed = isCollapsed;
            this.startOffset = startOffset;
            this.endOffset = endOffset;

            switch (paintOperation) {
                case PAINT_MARK:
                    lineIn = false;
                    lineOut = true;
                    outgoingLevel++;
                    break;
                case SINGLE_PAINT_MARK:
                    lineIn = false;
                    lineOut = false;
                    break;
                case PAINT_END_MARK:
                    lineIn = true;
                    lineOut = false;
                    isCollapsed = true;
                    allCollapsed = true;
                    break;
                case PAINT_LINE:
                    lineIn = lineOut = true;
                    break;
            }
        }
        
        boolean hasLineIn() {
            return lineIn || innerLevel > 0;
        }
        
        boolean hasLineOut() {
            return lineOut || outgoingLevel > 0 || (paintOperation != SINGLE_PAINT_MARK && !isAllCollapsed());
        }

        public PaintInfo(int paintOperation, int innerLevel, int paintY, int paintHeight, int startOffset, int endOffset){
            this(paintOperation, innerLevel, paintY, paintHeight, false, startOffset, endOffset);
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
        
         boolean isAllCollapsed() {
            return allCollapsed;
        }
        
        public void setPaintOperation(int paintOperation){
            this.paintOperation = paintOperation;
        }
        
        public void setInnerLevel(int innerLevel){
            this.innerLevel = innerLevel;
        }
        
        public @Override String toString(){
            StringBuffer sb = new StringBuffer("");
            if (paintOperation == PAINT_MARK){
                sb.append("PAINT_MARK"); // NOI18N
            }else if (paintOperation == PAINT_LINE){
                sb.append("PAINT_LINE"); // NOI18N
            }else if (paintOperation == PAINT_END_MARK) {
                sb.append("PAINT_END_MARK"); // NOI18N
            }else if (paintOperation == SINGLE_PAINT_MARK) {
                sb.append("SINGLE_PAINT_MARK");
            }
            sb.append(",L:").append(innerLevel).append("/").append(outgoingLevel); // NOI18N
            sb.append(',').append(isCollapsed ? "C" : "E"); // NOI18N
            sb.append(", start=").append(startOffset).append(", end=").append(endOffset);
            sb.append(", lineIn=").append(lineIn).append(", lineOut=").append(lineOut);
            return sb.toString();
        }
        
        boolean hasSign() {
            return paintOperation == PAINT_MARK || paintOperation == SINGLE_PAINT_MARK;
        }
        
        
        void mergeWith(PaintInfo prevInfo) {
            if (prevInfo == null) {
                return;
            }

            int operation = this.paintOperation;
            boolean lineIn = prevInfo.lineIn;
            boolean lineOut = prevInfo.lineOut;
            
            LOG.log(Level.FINE, "Merging {0} with {1}: ", new Object[] { this, prevInfo });
            if (prevInfo.getPaintOperation() == PAINT_END_MARK) {
                // merge with start|single -> start mark + line-in
                lineIn = true;
            } else {
                operation = PAINT_MARK;
            }

            int level1 = Math.min(prevInfo.innerLevel, innerLevel);
            int level2 = prevInfo.outgoingLevel;

            if (getPaintOperation() == PAINT_END_MARK 
                && innerLevel == prevInfo.outgoingLevel) {
                // if merging end marker at the last level, update to the new outgoing level
                level2 = outgoingLevel;
            } else if (!isCollapsed) {
                level2 = Math.max(prevInfo.outgoingLevel, outgoingLevel);
            }

            if (prevInfo.getInnerLevel() < getInnerLevel()) {
                int paintFrom = Math.min(prevInfo.paintY, paintY);
                int paintTo = Math.max(prevInfo.paintY + prevInfo.paintHeight, paintY + paintHeight);
                // at least one collapsed -> paint plus sign
                boolean collapsed = prevInfo.isCollapsed() || isCollapsed();
                int offsetFrom = Math.min(prevInfo.startOffset, startOffset);
                int offsetTo = Math.max(prevInfo.endOffset, endOffset);
                
                this.paintY = paintFrom;
                this.paintHeight = paintTo - paintFrom;
                this.isCollapsed = collapsed;
                this.startOffset = offsetFrom;
                this.endOffset = offsetTo;
            }
            this.paintOperation = operation;
            this.allCollapsed = prevInfo.allCollapsed && allCollapsed;
            this.innerLevel = level1;
            this.outgoingLevel = level2;
            this.lineIn |= lineIn;
            this.lineOut |= lineOut;
            
            LOG.log(Level.FINE, "Merged result: {0}", this);
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
    
    private final class Listener extends MouseAdapter implements FoldHierarchyListener, DocumentListener, Runnable {
    
        public Listener(){
        }

        // --------------------------------------------------------------------
        // FoldHierarchyListener implementation
        // --------------------------------------------------------------------

        public void foldHierarchyChanged(FoldHierarchyEvent evt) {
            refresh();
        }

        // --------------------------------------------------------------------
        // DocumentListener implementation
        // --------------------------------------------------------------------

        public void insertUpdate(DocumentEvent evt) {
            if (!(evt instanceof BaseDocumentEvent)) return;

            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            if (bevt.getLFCount() > 0) { // one or more lines inserted
                refresh();
            }
        }

        public void removeUpdate(DocumentEvent evt) {
            if (!(evt instanceof BaseDocumentEvent)) return;

            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            if (bevt.getLFCount() > 0) { // one or more lines removed
                refresh();
            }
        }

        public void changedUpdate(DocumentEvent evt) {
        }

        // --------------------------------------------------------------------
        // MouseListener implementation
        // --------------------------------------------------------------------

        @Override
        public void mousePressed (MouseEvent e) {
            Mark mark = getClickedMark(e);
            if (mark!=null){
                e.consume();
                performAction(mark, (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // #102288 - missing event consuming caused quick doubleclicks to break
            // fold expanding/collapsing and move caret to the particular line
            if (e.getClickCount() > 1) {
                LOG.log(Level.FINEST, "Mouse {0}click at {1}", new Object[] { e.getClickCount(), e.getY()});
                Mark mark = getClickedMark(e);
                try {
                    performActionAt(mark, e.getY());
                } catch (BadLocationException ex) {
                    LOG.log(Level.WARNING, "Error during fold expansion using sideline", ex);
                }
            } else {
                e.consume();
            }
        }

        // --------------------------------------------------------------------
        // private implementation
        // --------------------------------------------------------------------

        private Mark getClickedMark(MouseEvent e){
            if (e == null || !SwingUtilities.isLeftMouseButton(e)) {
                return null;
            }
            
            int x = e.getX();
            int y = e.getY();
            for (Mark mark : visibleMarks) {
                if (x >= mark.x && x <= (mark.x + mark.size) && y >= mark.y && y <= (mark.y + mark.size)) {
                    return mark;
                }
            }
            return null;
        }

        private void refresh() {
            SwingUtilities.invokeLater(this);
        }

        @Override
        public void run() {
            repaint();
        }
    } // End of Listener class
    
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

    private Coloring getColoring() {
        if (attribs == null) {
            if (fcsLookupResult == null) {
                fcsLookupResult = MimeLookup.getLookup(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(component))
                        .lookupResult(FontColorSettings.class);
                fcsLookupResult.addLookupListener(WeakListeners.create(LookupListener.class, fcsTracker, fcsLookupResult));
            }
            
            FontColorSettings fcs = fcsLookupResult.allInstances().iterator().next();
            AttributeSet attr = fcs.getFontColors(FontColorNames.CODE_FOLDING_BAR_COLORING);
            if (attr == null) {
                attr = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
            } else {
                attr = AttributesUtilities.createComposite(attr, fcs.getFontColors(FontColorNames.DEFAULT_COLORING));
            }
            attribs = attr;
        }        
        return Coloring.fromAttributeSet(attribs);
    }
    
}
