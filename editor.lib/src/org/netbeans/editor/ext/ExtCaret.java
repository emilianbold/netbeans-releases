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

package org.netbeans.editor.ext;

import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.prefs.Preferences;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseCaret;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.util.Lookup;

/**
* Extended caret implementation
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ExtCaret extends BaseCaret {

    /** 
     * Highlight row draw layer name.
     * 
     * <p>Using <code>DrawLayer</code>s has been deprecated and this constant
     * has no longer any meaning.
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public static final String HIGHLIGHT_ROW_LAYER_NAME = "highlight-row-layer"; // NOI18N

    /**
     * Highlight row draw layer visibility.
     * 
     * <p>Using <code>DrawLayer</code>s has been deprecated and this constant
     * has no longer any meaning.
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public static final int HIGHLIGHT_ROW_LAYER_VISIBILITY = 2050;

    /** 
     * Highlight matching brace draw layer name
     * 
     * @deprecated Please use Braces Matching SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-bracesmatching@/overview-summary.html">Editor Braces Matching</a>.
     */
    public static final String HIGHLIGHT_BRACE_LAYER_NAME = "highlight-brace-layer"; // NOI18N

    /** 
     * Highlight matching brace draw layer visibility 
     * 
     * @deprecated Please use Braces Matching SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-bracesmatching@/overview-summary.html">Editor Braces Matching</a>.
     */
    public static final int HIGHLIGHT_BRACE_LAYER_VISIBILITY = 11000;

// XXX: remove
//    /** Highlight a brace matching character before the caret */
//    public static final int MATCH_BRACE_BEFORE = -1;
//    
//    /** Highlight a brace matching character after (at) the caret */
//    public static final int MATCH_BRACE_AFTER = 0;
//    
//    /** Highlight a brace matching character either before or after caret;
//        the character before takes precedence. */
//    public static final int MATCH_BRACE_EITHER = java.lang.Integer.MAX_VALUE;
//
//    /** Whether to hightlight the matching brace */
//    boolean highlightBrace;
//
//    /** Coloring used for highlighting the matching brace */
//    Coloring highlightBraceColoring;
//
//    /** Mark holding the starting position of the matching brace. */
//    MarkFactory.DrawMark highlightBraceStartMark;
//
//    /** Mark holding the ending position of the matching brace. */
//    MarkFactory.DrawMark highlightBraceEndMark;
//
//    /** Timer that fires when the matching brace should be displayed */
//    private Timer braceTimer;
//    private ActionListener braceTimerListener; // because of unwanted GC
//
//    /** Signal that the next matching brace update
//    * will be immediate without waiting for the brace
//    * timer to fire the action.
//    */
//    private boolean matchBraceUpdateSync;
//
//    /** Whether the brace starting and ending marks are currently valid or not.
//     * If they are not valid the block they delimit is not highlighted.
//     */
//    boolean braceMarksValid;
//
//    boolean simpleMatchBrace;
//
//    private int matchBraceOffset = MATCH_BRACE_EITHER;
    
    private boolean popupMenuEnabled;

    static final long serialVersionUID =-4292670043122577690L;

// XXX: remove    
//    protected void modelChanged(BaseDocument oldDoc, BaseDocument newDoc) {
//        // Fix for #7108
//        braceMarksValid = false; // brace marks are out of date - new document
//        if (highlightBraceStartMark != null) {
//            try {
//                highlightBraceStartMark.remove();
//            } catch (InvalidMarkException e) {
//            }
//            highlightBraceStartMark = null;
//        }
//
//        if (highlightBraceEndMark != null) {
//            try {
//                highlightBraceEndMark.remove();
//            } catch (InvalidMarkException e) {
//            }
//            highlightBraceEndMark = null;
//        }
//
//        super.modelChanged( oldDoc, newDoc );
//    }

// XXX: remove
//    /** Called when settings were changed. The method is called
//    * also in constructor, so the code must count with the evt being null.
//    */
//    public void settingsChange(SettingsChangeEvent evt) {
//        super.settingsChange(evt);
//        JTextComponent c = component;
//        if (c != null) {
//            Class kitClass = Utilities.getKitClass(c);
//// XXX: remove
////            EditorUI editorUI = Utilities.getEditorUI(c);
////            highlightBraceColoring = editorUI.getColoring(
////                                           ExtSettingsNames.HIGHLIGHT_MATCH_BRACE_COLORING);
////
////            highlightBrace = SettingsUtil.getBoolean(kitClass,
////                               ExtSettingsNames.HIGHLIGHT_MATCH_BRACE,
////                               ExtSettingsDefaults.defaultHighlightMatchBrace);
////            int highlightBraceDelay = SettingsUtil.getInteger(kitClass,
////                                        ExtSettingsNames.HIGHLIGHT_MATCH_BRACE_DELAY,
////                                        ExtSettingsDefaults.defaultHighlightMatchBraceDelay);
////
////            if (highlightBrace) {
////                if (highlightBraceDelay > 0) {
////                    // jdk12 compiler doesn't allow inside run()
////                    final JTextComponent c2 = component;
////
////                    braceTimer = new Timer(highlightBraceDelay, null);
////                    braceTimerListener = 
////                         new ActionListener() {
////                             public void actionPerformed(ActionEvent evt2) {
////                                 SwingUtilities.invokeLater(
////                                     new Runnable() {
////                                         public void run() {
////                                             if (c2 != null) {
////                                                 BaseDocument doc = Utilities.getDocument(c2);
////                                                 if( doc != null ) {
////                                                     doc.readLock();
////                                                     try {
////                                                         updateMatchBrace();
////                                                     } finally {
////                                                         doc.readUnlock();
////                                                     }
////                                                 }
////                                             }
////                                         }
////                                     }
////                                 );
////                             }
////                         };
////                         
////                    braceTimer.addActionListener(new WeakTimerListener(braceTimerListener));
////                    braceTimer.setRepeats(false);
////                } else {
////                    braceTimer = null; // signal no delay
////                }
////                c.repaint();
////            }
////
////            simpleMatchBrace = SettingsUtil.getBoolean(kitClass,
////                                    ExtSettingsNames.CARET_SIMPLE_MATCH_BRACE,
////                                    ExtSettingsDefaults.defaultCaretSimpleMatchBrace);
////            
//            popupMenuEnabled = SettingsUtil.getBoolean(kitClass,
//                ExtSettingsNames.POPUP_MENU_ENABLED, true);
//        }
//    }
// XXX: remove
//    public void install(JTextComponent c) {
//        EditorUI editorUI = Utilities.getEditorUI(c);
//        editorUI.addLayer(new HighlightBraceLayer(), HIGHLIGHT_BRACE_LAYER_VISIBILITY);
//        super.install(c);
//    }
//
//    public void deinstall(JTextComponent c) {
//        EditorUI editorUI = Utilities.getEditorUI(c);
//        editorUI.removeLayer(HIGHLIGHT_BRACE_LAYER_NAME);
//        super.deinstall(c);
//    }
//    
//    /** Set the match brace offset.
//     * @param offset One of <code>MATCH_BRACE_BEFORE</code>,
//     * <code>MATCH_BRACE_AFTER</code> * or <code>MATCH_BRACE_EITHER</code>.
//     */
//    public void setMatchBraceOffset(int offset) {
//        if(offset != MATCH_BRACE_BEFORE && offset != MATCH_BRACE_AFTER
//           && offset != MATCH_BRACE_EITHER) {
//            throw new IllegalArgumentException("Offset "+ offset + " not allowed\n");
//        }
//        matchBraceOffset = offset;
//        BaseDocument doc = Utilities.getDocument(component);
//        if( doc != null ) {
//            doc.readLock();
//            try {
//                updateMatchBrace();
//            } finally {
//                doc.readUnlock();
//            }
//        }
//    }
//    
//    /** Fetch the match brace offset. */
//    public int getMatchBraceOffset() {
//        return matchBraceOffset;
//    }

    /** 
     * Update the matching brace of the caret. The document is read-locked
     * while this method is called.
     * 
     * @deprecated Please use Braces Matching SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-bracesmatching@/overview-summary.html">Editor Braces Matching</a>.
     */
    protected void updateMatchBrace() {
// XXX: remove
//        JTextComponent c = component;
//        if (c != null && highlightBrace) {
//            try {
//                EditorUI editorUI = Utilities.getEditorUI(c);
//                BaseDocument doc = (BaseDocument)c.getDocument();
//                int dotPos = getDot();
//                ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
//                boolean madeValid = false; // whether brace marks display were validated
//                int[] matchBlk = null;
//                if(dotPos > 0 && (matchBraceOffset == MATCH_BRACE_BEFORE
//                                  || matchBraceOffset == MATCH_BRACE_EITHER)) {
//                    matchBlk = sup.findMatchingBlock(dotPos - 1, simpleMatchBrace);
//                }
//                if(matchBlk == null && (matchBraceOffset == MATCH_BRACE_AFTER
//                                        || matchBraceOffset == MATCH_BRACE_EITHER)) {
//                    matchBlk = sup.findMatchingBlock(dotPos, simpleMatchBrace);
//                }
//                if (matchBlk != null) {
//                    if (highlightBraceStartMark != null) {
//                        int markStartPos = highlightBraceStartMark.getOffset();
//                        int markEndPos = highlightBraceEndMark.getOffset();
//                        if (markStartPos != matchBlk[0] || markEndPos != matchBlk[1]) {
//                            editorUI.repaintBlock(markStartPos, markEndPos);
//                            Utilities.moveMark(doc, highlightBraceStartMark, matchBlk[0]);
//                            Utilities.moveMark(doc, highlightBraceEndMark, matchBlk[1]);
//                            editorUI.repaintBlock(matchBlk[0], matchBlk[1]);
//                        } else { // on the same position
//                            if (!braceMarksValid) { // was not valid, must repaint
//                                editorUI.repaintBlock(matchBlk[0], matchBlk[1]);
//                            }
//                        }
//                    } else { // highlight mark is null
//                        highlightBraceStartMark = new MarkFactory.DrawMark(
//                                                    HIGHLIGHT_BRACE_LAYER_NAME, editorUI);
//                        highlightBraceEndMark = new MarkFactory.DrawMark(
//                                                    HIGHLIGHT_BRACE_LAYER_NAME, editorUI);
//                        highlightBraceStartMark.setActivateLayer(true);
//                        Utilities.insertMark(doc, highlightBraceStartMark, matchBlk[0]);
//                        Utilities.insertMark(doc, highlightBraceEndMark, matchBlk[1]);
//                        editorUI.repaintBlock(matchBlk[0], matchBlk[1]);
//                    }
//                    braceMarksValid = true;
//                    madeValid = true;
//                }
//
//                if (!madeValid) {
//                    if (braceMarksValid) {
//                        braceMarksValid = false;
//                        editorUI.repaintBlock(highlightBraceStartMark.getOffset(),
//                                highlightBraceEndMark.getOffset());
//                    }
//                }
//            } catch (BadLocationException e) {
//                Utilities.annotateLoggable(e);
//                highlightBrace = false;
//            } catch (InvalidMarkException e) {
//                Utilities.annotateLoggable(e);
//                highlightBrace = false;
//            }
//        }
    }
// XXX: remove
//    protected void update(boolean scrollViewToCaret) {
//        if (highlightBrace) {
//            if (matchBraceUpdateSync || braceTimer == null) {
//                updateMatchBrace();
//                matchBraceUpdateSync = false;
//
//            } else { // delay the brace update
//                braceTimer.restart();
//            }
//        }
//
//        super.update(scrollViewToCaret);
//    }

    /** 
     * Signal that the next matching brace update
     * will be immediate without waiting for the brace
     * timer to fire the action. This is usually done
     * for the key-typed action.
     * 
     * @deprecated Please use Braces Matching SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-bracesmatching@/overview-summary.html">Editor Braces Matching</a>.
     */
    public void requestMatchBraceUpdateSync() {
// XXX: remove
//        matchBraceUpdateSync = true;
    }

    private boolean dontTryTheOldCompletion = false;
    public @Override void mousePressed(MouseEvent evt) {
        if (!dontTryTheOldCompletion) {
// XXX: remove, this was replaced by the reflection code below to preserv backwards compatibility
//        Completion completion = ExtUtilities.getCompletion(component);
//        if (completion != null && completion.isPaneVisible()) {
//            // Hide completion if visible
//            completion.setPaneVisible(false);
//        }
            try {
                ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                Class extUtilitiesClass = loader.loadClass("org.netbeans.editor.ext.ExtUtilities"); //NOI18N
                Method getCompletionMethod = extUtilitiesClass.getMethod("getCompletion", JTextComponent.class); //NOI18N
                Object completion = getCompletionMethod.invoke(null, component);
                if (completion != null) {
                    Method isPaneVisibleMethod = completion.getClass().getMethod("isPaneVisible"); //NOI18N
                    if (((Boolean) isPaneVisibleMethod.invoke(completion)).booleanValue()) {
                        Method setPaneVisibleMethod = completion.getClass().getMethod("setPaneVisible", Boolean.TYPE); //NOI18N
                        setPaneVisibleMethod.invoke(completion, Boolean.FALSE);
                    }
                }
            } catch (Exception e) {
                dontTryTheOldCompletion = true;
            }
        }
        
        super.mousePressed(evt);
	showPopup(evt);        
    }
    
    private boolean showPopup (MouseEvent evt) {
        // Show popup menu for right click
        if (component != null && evt.isPopupTrigger()) {
            Preferences prefs = MimeLookup.getLookup(DocumentUtilities.getMimeType(component)).lookup(Preferences.class);
            if (prefs.getBoolean(SimpleValueNames.POPUP_MENU_ENABLED, true)) {
                Utilities.getEditorUI(component).showPopupMenu(evt.getX(), evt.getY());
                return true;
            }
        }
        return false;
    }
    
    public @Override void mouseReleased(MouseEvent evt) {
        if (!showPopup(evt)) {
            super.mouseReleased(evt);
        }
    }
// XXX: remove
//    /* package */ static boolean NO_HIGHLIGHT_BRACE_LAYER = Boolean.getBoolean("nbeditor-no-HighlightBraceLayer");
//    
//    /** 
//     * Draw layer to highlight the matching brace.
//     * 
//     * XXX: The HighlightBraceLayer needs to be rewritten using the new Highlighting SPI.
//     */
//    class HighlightBraceLayer extends DrawLayer.AbstractLayer {
//
//        public HighlightBraceLayer() {
//            super(HIGHLIGHT_BRACE_LAYER_NAME);
//        }
//
//        public void init(DrawContext ctx) {
//        }
//
//        public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
//            if (!NO_HIGHLIGHT_BRACE_LAYER && braceMarksValid) {
//                if (mark != null)
//                    return mark.getActivateLayer();
//                try {
//                    if (ctx.getStartOffset() > highlightBraceEndMark.getOffset())
//                        return highlightBraceEndMark.getActivateLayer();
//                    if (ctx.getStartOffset() > highlightBraceStartMark.getOffset())
//                        return highlightBraceStartMark.getActivateLayer();
//                } catch (InvalidMarkException ex) {                    
//                }
//            }
//
//            return false;
//        }
//
//        public void updateContext(DrawContext ctx) {
//            if (!NO_HIGHLIGHT_BRACE_LAYER && highlightBraceColoring != null) {
//                highlightBraceColoring.apply(ctx);
//            }
//        }
//
//    }

}
