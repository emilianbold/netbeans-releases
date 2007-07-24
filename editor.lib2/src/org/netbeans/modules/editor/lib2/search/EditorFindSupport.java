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

package org.netbeans.modules.editor.lib2.search;

import java.awt.Insets;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.modules.editor.lib2.ComponentUtils;
import org.netbeans.modules.editor.lib2.DocUtils;
import org.netbeans.modules.editor.lib2.highlighting.BlockHighlighting;
import org.netbeans.modules.editor.lib2.highlighting.Factory;
import org.netbeans.modules.editor.lib2.search.DocumentFinder.FindReplaceResult;
import org.openide.util.NbBundle;

/**
* Find management
*
* @author Miloslav Metelka
* @version 1.00
*/

public final class EditorFindSupport {

    private static final Logger LOG = Logger.getLogger(EditorFindSupport.class.getName());
    
    /* Find properties.
    * They are read by FindSupport when its instance is being initialized.
    * FIND_WHAT: java.lang.String - search expression
    * FIND_REPLACE_BY: java.lang.String - replace string
    * FIND_HIGHLIGHT_SEARCH: java.lang.Boolean - highlight matching strings in text
    * FIND_INC_SEARCH: java.lang.Boolean - show matching strings immediately
    * FIND_BACKWARD_SEARCH: java.lang.Boolean - search in backward direction
    * FIND_WRAP_SEARCH: java.lang.Boolean - if end of doc reached, start from begin
    * FIND_MATCH_CASE: java.lang.Boolean - match case of letters
    * FIND_SMART_CASE: java.lang.Boolean - case insensitive search if FIND_MATCH_CASE
    *   is false and all letters of FIND_WHAT are small, case sensitive otherwise
    * FIND_WHOLE_WORDS: java.lang.Boolean - match only whole words
    * FIND_REG_EXP: java.lang.Boolean - use regular expressions in search expr
    * FIND_HISTORY: java.util.List - History of search expressions
    * FIND_HISTORY_SIZE: java.lang.Integer - Maximum size of the history
    * FIND_BLOCK_SEARCH: java.lang.Boolean - search in block
    * FIND_BLOCK_SEARCH_START: java.lang.Integer - start offset of the block
    * FIND_BLOCK_SEARCH_END: java.lang.Integer - end offset of the block
    * 
    */
    public static final String FIND_WHAT = "find-what"; // NOI18N
    public static final String FIND_REPLACE_WITH = "find-replace-with"; // NOI18N
    public static final String FIND_HIGHLIGHT_SEARCH = "find-highlight-search"; // NOI18N
    public static final String FIND_INC_SEARCH = "find-inc-search"; // NOI18N
    public static final String FIND_INC_SEARCH_DELAY = "find-inc-search-delay"; // NOI18N
    public static final String FIND_BACKWARD_SEARCH = "find-backward-search"; // NOI18N
    public static final String FIND_WRAP_SEARCH = "find-wrap-search"; // NOI18N
    public static final String FIND_MATCH_CASE = "find-match-case"; // NOI18N
    public static final String FIND_SMART_CASE = "find-smart-case"; // NOI18N
    public static final String FIND_WHOLE_WORDS = "find-whole-words"; // NOI18N
    public static final String FIND_REG_EXP = "find-reg-exp"; // NOI18N
    public static final String FIND_HISTORY = "find-history"; // NOI18N
    public static final String FIND_HISTORY_SIZE = "find-history-size"; // NOI18N
    public static final String FIND_BLOCK_SEARCH = "find-block-search"; //NOI18N
    public static final String FIND_BLOCK_SEARCH_START = "find-block-search-start"; //NOI18N
    public static final String FIND_BLOCK_SEARCH_END = "find-block-search-end"; //NOI18N

    private static final String FOUND_LOCALE = "find-found"; // NOI18N
    private static final String NOT_FOUND_LOCALE = "find-not-found"; // NOI18N
    private static final String WRAP_START_LOCALE = "find-wrap-start"; // NOI18N
    private static final String WRAP_END_LOCALE = "find-wrap-end"; // NOI18N
    private static final String WRAP_BLOCK_START_LOCALE = "find-block-wrap-start"; // NOI18N
    private static final String WRAP_BLOCK_END_LOCALE = "find-block-wrap-end"; // NOI18N
    private static final String ITEMS_REPLACED_LOCALE = "find-items-replaced"; // NOI18N
    /** It's public only to keep backwards compatibility of th FindSupport class. */
    public static final String REVERT_MAP = "revert-map"; // NOI18N

    private static final String SEARCH_BLOCK_START="search-block-start"; //NOI18N
    private static final String SEARCH_BLOCK_END="search-block-end"; //NOI18N
    
    /** It's public only to keep backwards compatibility of th FindSupport class. */
    public static final String FIND_HISTORY_PROP = "find-history-prop"; //NOI18N
    /** It's public only to keep backwards compatibility of th FindSupport class. */
    public static final String FIND_HISTORY_CHANGED_PROP = "find-history-changed-prop"; //NOI18N
    
    /** Shared instance of FindSupport class */
    private static EditorFindSupport findSupport;

    /** Find properties */
    private Map<String, Object> findProps;

    private WeakHashMap<JTextComponent, Map<String, WeakReference<BlockHighlighting>>> comp2layer = 
        new WeakHashMap<JTextComponent, Map<String, WeakReference<BlockHighlighting>>>();
    
    /** Support for firing change events */
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    private SPW lastSelected;
    private List<SPW> historyList;

    private EditorFindSupport() {
        // prevent instance creation
    }

    /** Get shared instance of find support */
    public static EditorFindSupport getInstance() {
        if (findSupport == null) {
            findSupport = new EditorFindSupport();
        }
        return findSupport;
    }

    public Map<String, Object> getDefaultFindProperties() {
        HashMap<String, Object> props = new HashMap<String, Object>();
        
        props.put(FIND_WHAT, null);
        props.put(FIND_REPLACE_WITH, null);
        props.put(FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(FIND_INC_SEARCH, Boolean.TRUE);
        props.put(FIND_BACKWARD_SEARCH, Boolean.FALSE);
        props.put(FIND_WRAP_SEARCH, Boolean.TRUE);
        props.put(FIND_MATCH_CASE, Boolean.FALSE);
        props.put(FIND_SMART_CASE, Boolean.FALSE);
        props.put(FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(FIND_REG_EXP, Boolean.FALSE);
        props.put(FIND_HISTORY, new Integer(30));

        return props;
    }

    private int getBlockEndOffset(){
        Position pos = (Position) getFindProperties().get(FIND_BLOCK_SEARCH_END);
        return (pos != null) ? pos.getOffset() : -1;
    }
    
    public Map<String, Object> getFindProperties() {
        if (findProps == null) {
            findProps = getDefaultFindProperties();
        }
        return findProps;
    }

    /** Get find property with specified name */
    public Object getFindProperty(String name) {
        return getFindProperties().get(name);
    }

    private Map<String, Object> getValidFindProperties(Map<String, Object> props) {
        return (props != null) ? props : getFindProperties();
    }

    /**
     * <p><b>IMPORTANT:</b> This method is public only for keeping backwards
     * compatibility of the {@link org.netbeans.editor.FindSupport} class.
     */
    public int[] getBlocks(int[] blocks, Document doc,
                    int startPos, int endPos) throws BadLocationException {
        Map props = getValidFindProperties(null);
        
        Boolean b = (Boolean)props.get(FIND_BLOCK_SEARCH);
        boolean blockSearch = (b != null && b.booleanValue());
        Integer i = (Integer) props.get(FIND_BLOCK_SEARCH_START);
        int blockSearchStart = (i != null) ? i.intValue() : -1;
        int blockSearchEnd = getBlockEndOffset();

        if (blockSearch && blockSearchStart>-1 && blockSearchEnd >0){
            if (endPos>=blockSearchStart && startPos <=blockSearchEnd){
                startPos = Math.max(blockSearchStart, startPos);
                endPos = Math.min(blockSearchEnd, endPos);
            }else{
                return blocks;
            }
        }
        return DocumentFinder.findBlocks(doc, startPos, endPos, props, blocks);
    }

    /**
     * Get find property without performing initialization
     * of find properties. This is useful for example for base document
     * when it wants to query whether it should do highlight search.
     *
     * <p><b>IMPORTANT:</b> This method is public only for keeping backwards
     * compatibility of the {@link org.netbeans.editor.FindSupport} class.
     */
    public Object getPropertyNoInit(String name) {
        if (findProps == null) {
            return null;
        } else {
            return getFindProperty(name);
        }
    }

    /** Set find property with specified name and fire change.
    */
    public void putFindProperty(String name, Object newValue) {
        Object oldValue = getFindProperty(name);
        if ((oldValue == null && newValue == null)
                || (oldValue != null && oldValue.equals(newValue))
           ) {
            return;
        }
        if (newValue != null) {
            getFindProperties().put(name, newValue);
        } else {
            getFindProperties().remove(name);
        }
        firePropertyChange(name, oldValue, newValue);
    }

    /** Add/replace properties from some other map
    * to current find properties. If the added properties
    * are different than the original ones,
    * the property change is fired.
    */
    public void putFindProperties(Map<String, Object> propsToAdd) {
        if (!getFindProperties().equals(propsToAdd)) {
            getFindProperties().putAll(propsToAdd);
            firePropertyChange(null, null, null);
        }
    }
    
    public void setBlockSearchHighlight(int startSelection, int endSelection){
        JTextComponent comp = EditorRegistry.lastFocusedComponent();
        BlockHighlighting layer = comp == null ? null : findLayer(comp, Factory.BLOCK_SEARCH_LAYER);

        if (layer != null) {
            
            if (startSelection >= 0 && endSelection >= 0 && startSelection < endSelection ) {
                layer.highlightBlock(
                    startSelection, endSelection, FontColorNames.BLOCK_SEARCH_COLORING);
            } else {
                layer.highlightBlock(-1, -1, FontColorNames.BLOCK_SEARCH_COLORING);
            }
            
        }
        
// TODO: remove
//        JTextComponent c = DocumentsRegistry.getMostActiveComponent();
//        if (c==null) return;
//        EditorUI editorUI = ((BaseTextUI)c.getUI()).getEditorUI();
//        DrawLayerFactory.BlockSearchLayer blockLayer
//        = (DrawLayerFactory.BlockSearchLayer)editorUI.findLayer(
//              DrawLayerFactory.BLOCK_SEARCH_LAYER_NAME);
//        Boolean b = (Boolean)getFindProperties().get(FIND_BACKWARD_SEARCH);
//        boolean back = (b != null && b.booleanValue());
//        
//        if (startSelection >= endSelection){
//            if (blockLayer != null) {
//                if (blockLayer.isEnabled()) {
//                    blockLayer.setEnabled(false);
//                    try {
//                        editorUI.repaintBlock(blockLayer.getOffset(), blockLayer.getOffset()+blockLayer.getLength());
//                    } catch (BadLocationException e) {
//                        LOG.log(Level.WARNING, e.getMessage(), e);
//                    }
//                }
//            }
//        }else{
//            //init layer
//            if (blockLayer == null) {
//                blockLayer = new DrawLayerFactory.BlockSearchLayer();
//                if (!editorUI.addLayer(blockLayer,
//                    DrawLayerFactory.BLOCK_SEARCH_LAYER_VISIBILITY)
//                ) {
//                    return; // couldn't add layer
//                }
//            } else {
//                if (blockLayer.isEnabled()) {
//                    blockLayer.setEnabled(false);
//                    try {
//                        editorUI.repaintOffset(blockLayer.getOffset());
//                    } catch (BadLocationException e) {
//                        LOG.log(Level.WARNING, e.getMessage(), e);
//                    }
//                }
//            }
//            
//            blockLayer.setEnabled(true);
//            blockLayer.setArea(startSelection, endSelection-startSelection);
//            try {
//                editorUI.repaintBlock(startSelection, endSelection);
//            } catch (BadLocationException e) {
//                LOG.log(Level.WARNING, e.getMessage(), e);
//                return;
//            }
//            c.getCaret().setDot(back ? endSelection : startSelection);
//        }
    }
    
    public boolean incSearch(Map<String, Object> props, int caretPos) {
        props = getValidFindProperties(props);

        // if regexp terminate incSearch
        Boolean b = (Boolean)props.get(FIND_REG_EXP);
        if (b !=null && b.booleanValue()){
            return false;
        }
        
        b = (Boolean)props.get(FIND_INC_SEARCH);
        if (b != null && b.booleanValue()) { // inc search enabled
            JTextComponent comp = EditorRegistry.lastFocusedComponent();
            
            if (comp != null) {
                b = (Boolean)props.get(FIND_BACKWARD_SEARCH);
                boolean back = (b != null && b.booleanValue());
                b = (Boolean)props.get(FIND_BLOCK_SEARCH);
                boolean blockSearch = (b != null && b.booleanValue());
                Integer i = (Integer) props.get(FIND_BLOCK_SEARCH_START);
                int blockSearchStart = (i != null) ? i.intValue() : -1;
                
                Position endPos = (Position) props.get(FIND_BLOCK_SEARCH_END);
                int blockSearchEnd = (endPos != null) ? endPos.getOffset() : -1;
                int endOffset = (back) ? 0 : -1;
                int pos;
                try {
                    int start = (blockSearch && blockSearchStart > -1) ? blockSearchStart : 0;
                    int end = (blockSearch && blockSearchEnd > 0) ? blockSearchEnd : -1;
                    if (start>0 && end == -1) return false;
                    int findRet[] = findInBlock(comp, caretPos, 
                        start, 
                        end, 
                        props, false);
                            
                    if (findRet == null) {
                        incSearchReset();
                        return false;
                    }
                    pos = findRet[0];
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, e.getMessage(), e);
                    return false;
                }
                
                // Find the layer
                BlockHighlighting layer = (BlockHighlighting)findLayer(comp, Factory.INC_SEARCH_LAYER);

                if (pos >= 0) {
                    String s = (String)props.get(FIND_WHAT);
                    int len = (s != null) ? s.length() : 0;
                    if (len > 0) {
                        if (comp.getSelectionEnd() > comp.getSelectionStart()){
                            comp.select(caretPos, caretPos);
                        }
                        
// TODO: remove
//                        incLayer.setInversion(!blockSearch);
//                        incLayer.setEnabled(true);
//                        incLayer.setArea(pos, len);
                        
                        if (layer != null) {
                            layer.highlightBlock(
                                pos,
                                pos + len,
                                blockSearch ? FontColorNames.INC_SEARCH_COLORING : FontColorNames.SELECTION_COLORING
                            );
                        }

                        // reset higlighting
                        Map<String, Object> defaultProps = getValidFindProperties(null);
                        String findWhatDef = (String)defaultProps.get(FIND_WHAT);
                        if (findWhatDef!=null && findWhatDef.length()>0){
                            defaultProps.put(FIND_WHAT, ""); //NOI18N
                            comp.repaint();
                        }

                        ensureVisible(comp, pos, pos);
                        return true;
                    }
                } else { // string not found
                    // !!!          ((BaseCaret)c.getCaret()).dispatchUpdate();
                }
               
            }
        } else { // inc search not enabled
            incSearchReset();
        }
        return false;
    }

    public void incSearchReset() {
        // Find the layer
        JTextComponent comp = EditorRegistry.lastFocusedComponent();
        BlockHighlighting layer = comp == null ? null : (BlockHighlighting)findLayer(comp, Factory.INC_SEARCH_LAYER);
        
        if (layer != null) {
            layer.highlightBlock(-1, -1, null);
        }
    }
    
    private boolean isBackSearch(Map props, boolean oppositeDir) {
        Boolean b = (Boolean)props.get(FIND_BACKWARD_SEARCH);
        boolean back = (b != null && b.booleanValue());
        if (oppositeDir) {
            back = !back;
        }
        return back;
    }

    private void selectText(JTextComponent c, int start, int end, boolean back){
        Caret caret = c.getCaret();
        ensureVisible(c, start, end);
        if (back) {
            caret.setDot(end);
            caret.moveDot(start);
        } else { // forward direction
            caret.setDot(start);
            caret.moveDot(end);
        }
    }
    
    private void ensureVisible(JTextComponent c, int startOffset, int endOffset) {
        // TODO: read insets from settings
        ensureVisible(c, startOffset, endOffset, new Insets(10, 10, 10, 10));
    }
    
    /**
     * Ensure that the given region will be visible in the view
     * with the appropriate find insets.
     */
    private void ensureVisible(JTextComponent c, int startOffset, int endOffset, Insets extraInsets) {
        try {
            Rectangle startBounds = c.modelToView(startOffset);
            Rectangle endBounds = c.modelToView(endOffset);
            if (startBounds != null && endBounds != null) {
                startBounds.add(endBounds);
                if (extraInsets != null) {
                    Rectangle visibleBounds = c.getVisibleRect();
                    int extraTop = (extraInsets.top < 0)
                        ? -extraInsets.top * visibleBounds.height / 100 // percentage
                        : extraInsets.top * endBounds.height; // line count
                    startBounds.y -= extraTop;
                    startBounds.height += extraTop;
                    startBounds.height += (extraInsets.bottom < 0)
                        ? -extraInsets.bottom * visibleBounds.height / 100 // percentage
                        : extraInsets.bottom * endBounds.height; // line count
                    int extraLeft = (extraInsets.left < 0)
                        ? -extraInsets.left * visibleBounds.width / 100 // percentage
                        : extraInsets.left * endBounds.width; // char count
                    startBounds.x -= extraLeft;
                    startBounds.width += extraLeft;
                    startBounds.width += (extraInsets.right < 0)
                        ? -extraInsets.right * visibleBounds.width / 100 // percentage
                        : extraInsets.right * endBounds.width; // char count
                }
                c.scrollRectToVisible(startBounds);
            }
        } catch (BadLocationException e) {
            // do not scroll
        }
    }
    
    private FindReplaceResult findReplaceImpl(String replaceExp, Map<String, Object> props, boolean oppositeDir){
        incSearchReset();
        props = getValidFindProperties(props);
        boolean back = isBackSearch(props, oppositeDir);
        JTextComponent c = EditorRegistry.lastFocusedComponent();
        Object findWhat = props.get(FIND_WHAT);
        if (findWhat == null) { // nothing to search for
            return null;
        }

        String exp = "'" + findWhat + "' "; // NOI18N
        if (c != null) {
            ComponentUtils.clearStatusText(c);
            Caret caret = c.getCaret();
            int dotPos = caret.getDot();
            if (findWhat.equals(c.getSelectedText())) {
                Object dp = props.get(FIND_BACKWARD_SEARCH);
                boolean direction = (dp != null) ? ((Boolean)dp).booleanValue() : false;
                
                if (dotPos == (oppositeDir ^ direction ? c.getSelectionEnd() : c.getSelectionStart()))
                    dotPos += (oppositeDir ^ direction ? -1 : 1);
            }
            
            Boolean b = (Boolean)props.get(FIND_BLOCK_SEARCH);
            boolean blockSearch = (b != null && b.booleanValue());
            Integer i = (Integer) props.get(FIND_BLOCK_SEARCH_START);
            int blockSearchStart = (i != null) ? i.intValue() : -1;
            int blockSearchEnd = getBlockEndOffset();

            try {
                FindReplaceResult result = findReplaceInBlock(replaceExp, c, dotPos, 
                        (blockSearch && blockSearchStart > -1) ? blockSearchStart : 0, 
                        (blockSearch && blockSearchEnd > 0) ? blockSearchEnd : -1, 
                        props, oppositeDir);
                int[] blk = null; 
                if (result != null){
                    blk = result.getFoundPositions();
                }
                if (blk != null) {
                    selectText(c, blk[0], blk[1], back);
                    String msg = exp + NbBundle.getBundle(EditorFindSupport.class).getString(FOUND_LOCALE)
                                 + ' ' + DocUtils.debugPosition(c.getDocument(), blk[0]);
                    if (blk[2] == 1) { // wrap was done
                        msg += "; "; // NOI18N
                        if (blockSearch && blockSearchEnd>0 && blockSearchStart >-1){
                            msg += back ? NbBundle.getBundle(EditorFindSupport.class).getString(WRAP_BLOCK_END_LOCALE)
                                   : NbBundle.getBundle(EditorFindSupport.class).getString(WRAP_BLOCK_START_LOCALE);
                        }else{
                            msg += back ? NbBundle.getBundle(EditorFindSupport.class).getString(WRAP_END_LOCALE)
                                   : NbBundle.getBundle(EditorFindSupport.class).getString(WRAP_START_LOCALE);
                        }
                        ComponentUtils.setStatusText(c, msg);
                        c.getToolkit().beep();
                    } else {
                        ComponentUtils.setStatusText(c, msg);
                    }
                    return result;
                } else { // not found
                    ComponentUtils.setStatusBoldText(c, exp + NbBundle.getBundle(EditorFindSupport.class).getString(
                                                    NOT_FOUND_LOCALE));
                    // issue 14189 - selection was not removed
                    c.getCaret().setDot(c.getCaret().getDot());
                }
            } catch (BadLocationException e) {
                LOG.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return null;
    }
    
    /** Find the text from the caret position.
    * @param props search properties
    * @param oppositeDir whether search in opposite direction
    */
    public boolean find(Map<String, Object> props, boolean oppositeDir) {
        FindReplaceResult result = findReplaceImpl(null, props, oppositeDir);
        return (result != null);
    }

    private FindReplaceResult findReplaceInBlock(String replaceExp, JTextComponent c, int startPos, int blockStartPos,
                             int blockEndPos, Map<String, Object> props, boolean oppositeDir) throws BadLocationException {
        if (c != null) {
            props = getValidFindProperties(props);
            Document doc = (Document)c.getDocument();
            int pos = -1;
            boolean wrapDone = false;
            String replaced = null;

            boolean back = isBackSearch(props, oppositeDir);
            Boolean b = (Boolean)props.get(FIND_WRAP_SEARCH);
            boolean wrap = (b != null && b.booleanValue());
            int docLen = doc.getLength();
            if (blockEndPos == -1) {
                blockEndPos = docLen;
            }

            int retFind[];
            while (true) {
                //pos = doc.find(sf, startPos, back ? blockStartPos : blockEndPos);
                int off1 = startPos;
                int off2 = back ? blockStartPos : blockEndPos;
                FindReplaceResult result = DocumentFinder.findReplaceResult(replaceExp, doc, Math.min(off1, off2), Math.max(off1, off2), 
                       props, oppositeDir );
                if (result == null){
                    return null;
                }
                retFind = result.getFoundPositions();
                replaced = result.getReplacedString();
                if (retFind == null){
                    break;
                }
                pos = retFind[0];
                
                if (pos != -1) {
                    break;
                }

                if (wrap) {
                    if (back) {
                        //Bug #20552 the wrap search check whole document
                        //instead of just the remaining not-searched part to be
                        //able to find expressions with the cursor in it

                        //blockStartPos = startPos;
                        startPos = blockEndPos;
                    } else {
                        //blockEndPos = startPos;
                        startPos = blockStartPos;
                    }
                    wrapDone = true;
                    wrap = false; // only one loop
                } else { // no wrap set
                    break;
                }

            }

            if (pos != -1) {
                int[] ret = new int[3];
                ret[0] = pos;
                ret[1] = retFind[1];
                ret[2] = wrapDone ? 1 : 0;
                return new FindReplaceResult(ret, replaced);
            }
        }
        return null;
    }
    
    /** Find the searched expression
    * @param startPos position from which to search. It must be inside the block.
    * @param blockStartPos starting position of the block. It must
    *   be valid position greater or equal than zero. It must be lower than
    *   or equal to blockEndPos (except blockEndPos=-1).
    * @param blockEndPos ending position of the block. It can be -1 for the end
    *   of document. It must be greater or equal than blockStartPos (except blockEndPos=-1).
    * @param props search properties
    * @param oppositeDir whether search in opposite direction
    * @param displayWrap whether display messages about the wrapping
    * @return either null when nothing was found or integer array with three members
    *    ret[0] - starting position of the found string
    *    ret[1] - ending position of the found string
    *    ret[2] - 1 or 0 when wrap was or wasn't performed in order to find the string 
    */
    public int[] findInBlock(JTextComponent c, int startPos, int blockStartPos,
                             int blockEndPos, Map<String, Object> props, boolean oppositeDir) throws BadLocationException {
        FindReplaceResult result = findReplaceInBlock(null, c, startPos, blockStartPos,
                             blockEndPos, props, oppositeDir);
        return result == null ? null : result.getFoundPositions();
    }

    public boolean replace(Map<String, Object> props, boolean oppositeDir)
    throws BadLocationException {
        incSearchReset();
        props = getValidFindProperties(props);
        Boolean b = (Boolean)props.get(FIND_BACKWARD_SEARCH);
        boolean back = (b != null && b.booleanValue());
        if (oppositeDir) {
            back = !back;
        }

        b = (Boolean)props.get(FIND_BLOCK_SEARCH);
        boolean blockSearch = (b != null && b.booleanValue());
        Integer i = (Integer) props.get(FIND_BLOCK_SEARCH_START);
        int blockSearchStart = (i != null) ? i.intValue() : -1;
        int blockSearchEnd = getBlockEndOffset();

        JTextComponent c = EditorRegistry.lastFocusedComponent();
        if (c != null) {
            String s = (String)props.get(FIND_REPLACE_WITH);
            Caret caret = c.getCaret();
            if (caret.isSelectionVisible()){
                int dotPos = caret.getDot();
                Object dp = props.get(FIND_BACKWARD_SEARCH);
                boolean direction = (dp != null) ? ((Boolean)dp).booleanValue() : false;
                dotPos = (oppositeDir ^ direction ? c.getSelectionEnd() : c.getSelectionStart());
                c.setCaretPosition(dotPos);
            }
            
            FindReplaceResult result = findReplaceImpl(s, props, oppositeDir);
            if (result!=null){
                s  = result.getReplacedString();
            } else {
                return false;
            }

            Document doc = (Document)c.getDocument();
            int startPos = c.getSelectionStart();
            int len = c.getSelectionEnd() - startPos;
            DocUtils.atomicLock(doc);
            try {
                if (len > 0) {
                    doc.remove(startPos, len);
                }
                if (s != null && s.length() > 0) {
                    doc.insertString(startPos, s, null);
                }
            } finally {
                DocUtils.atomicUnlock(doc);
                if (blockSearch){
                    setBlockSearchHighlight(blockSearchStart, getBlockEndOffset());
                }
            }
            
            // adjust caret pos after replace operation
            int adjustedCaretPos = (back || s == null) ? startPos : startPos + s.length();
            caret.setDot(adjustedCaretPos);
            
        }
        
        return true;
    }

    public void replaceAll(Map<String, Object> props) {
        incSearchReset();
        JTextComponent c = EditorRegistry.lastFocusedComponent();
        Document doc = (Document)c.getDocument();
        int maxCnt = doc.getLength();
        int replacedCnt = 0;
        int totalCnt = 0;

        props = getValidFindProperties(props);
        props = new HashMap<String, Object>(props);
        String replaceWithOriginal = (String)props.get(FIND_REPLACE_WITH);
        
        Boolean b = (Boolean)props.get(FIND_BLOCK_SEARCH);
        boolean blockSearch = (b != null && b.booleanValue());
        b = (Boolean)props.get(FIND_WRAP_SEARCH);
        boolean wrapSearch = (b != null && b.booleanValue());
        b = (Boolean)props.get(FIND_BACKWARD_SEARCH);
        boolean backSearch = (b != null && b.booleanValue());
        
        if (wrapSearch){
            props.put(FIND_WRAP_SEARCH, Boolean.FALSE);
            props.put(FIND_BACKWARD_SEARCH, Boolean.FALSE);
            firePropertyChange(null, null, null); 
        }
        
        Integer i = (Integer) props.get(FIND_BLOCK_SEARCH_START);
        int blockSearchStart = (i != null) ? i.intValue() : -1;
        int blockSearchEnd = getBlockEndOffset();

        if (c != null) {
            DocUtils.atomicLock(doc);
            try {
                int startPosWholeSearch = 0;
                int endPosWholeSearch = -1;
                int caretPos = c.getCaret().getDot();

                if (!wrapSearch){
                    if (backSearch){
                        startPosWholeSearch = 0;
                        endPosWholeSearch = caretPos;
                    }else{
                        startPosWholeSearch = caretPos;
                        endPosWholeSearch = -1;
                    }
                }
                
                int actualPos = wrapSearch ? 0 : c.getCaret().getDot();
                
                int pos = (blockSearch && blockSearchStart > -1) ? ( backSearch ? blockSearchEnd : blockSearchStart) : actualPos; // actual position
                
                while (true) {
                    blockSearchEnd = getBlockEndOffset();
                    FindReplaceResult result = findReplaceInBlock(replaceWithOriginal, c, pos, 
                            (blockSearch && blockSearchStart > -1) ? blockSearchStart : startPosWholeSearch, 
                            (blockSearch && blockSearchEnd > 0) ? blockSearchEnd : endPosWholeSearch, 
                            props, false);
                    if (result == null){
                        break;
                    }
                    int[] blk = result.getFoundPositions();
                    String replaceWith = result.getReplacedString();
                    if (blk == null) {
                        break;
                    }
                    totalCnt++;
                    int len = blk[1] - blk[0];
                    boolean skip = false; // cannot remove (because of guarded block)?
                    try {
                        doc.remove(blk[0], len);
                    } catch (BadLocationException e) {
                        // replace in guarded block
                        if (ComponentUtils.isGuardedException(e)) {
                            skip = true;
                        } else {
                            throw e;
                        }
                    }
                    if (skip) {
                        pos = blk[0] + len;

                    } else { // can and will insert the new string
                        if (replaceWith != null && replaceWith.length() > 0) {
                            doc.insertString(blk[0], replaceWith, null);
                        }
                        pos = blk[0] + ((replaceWith != null) ? replaceWith.length() : 0);
                        replacedCnt++;
                    }
                }
                
                // Display message about replacement
                if (totalCnt == 0){
                    Object findWhat = props.get(FIND_WHAT);
                    String exp = "'' "; //NOI18N
                    if (findWhat != null) { // nothing to search for
                        exp = "'" + findWhat + "' "; // NOI18N
                    }
                    ComponentUtils.setStatusBoldText(c, exp + NbBundle.getBundle(EditorFindSupport.class).getString(
                                NOT_FOUND_LOCALE));
                }else{
                    MessageFormat fmt = new MessageFormat(
                                            NbBundle.getBundle(EditorFindSupport.class).getString(ITEMS_REPLACED_LOCALE));
                    String msg = fmt.format(new Object[] { new Integer(replacedCnt), new Integer(totalCnt) });
                    ComponentUtils.setStatusText(c, msg);
                }

            } catch (BadLocationException e) {
                LOG.log(Level.WARNING, e.getMessage(), e);
            } finally {
                DocUtils.atomicUnlock(doc);
                if (blockSearch){
                    setBlockSearchHighlight(blockSearchStart, getBlockEndOffset());
                }
            }
        }
    }

    public void hookLayer(BlockHighlighting layer, JTextComponent component) {
        synchronized (comp2layer) {
            Map<String, WeakReference<BlockHighlighting>> type2layer = comp2layer.get(component);

            if (type2layer == null) {
                type2layer = new HashMap<String, WeakReference<BlockHighlighting>>();
                comp2layer.put(component, type2layer);
            }

            type2layer.put(layer.getLayerTypeId(), new WeakReference<BlockHighlighting>(layer));
        }
    }
    
    public void unhookLayer(BlockHighlighting layer, JTextComponent component) {
        synchronized (comp2layer) {
            Map<String, WeakReference<BlockHighlighting>> type2layer = comp2layer.get(component);

            if (type2layer != null) {
                type2layer.remove(layer.getLayerTypeId());
                if (type2layer.isEmpty()) {
                    comp2layer.remove(component);
                }
            }
        }
    }
    
    public BlockHighlighting findLayer(JTextComponent component, String layerId) {
        synchronized (comp2layer) {
            Map<String, WeakReference<BlockHighlighting>> type2layer = comp2layer.get(component);
            BlockHighlighting layer = null;

            if (type2layer != null) {
                WeakReference<BlockHighlighting> ref = type2layer.get(layerId);
                if (ref != null) {
                    layer = ref.get();
                }
            }

            return layer;
        }
    }
    
// TODO: remove
//    /** Get position of wrap mark for some document */
//    public int getWrapSearchMarkPos(Document doc) {
//        Mark mark = (Mark)doc.getProperty(Document.WRAP_SEARCH_MARK_PROP);
//        try {
//            return (mark != null) ? mark.getOffset() : doc.getLength();
//        } catch (InvalidMarkException e) {
//            throw new RuntimeException(); // shouldn't happen
//        }
//    }
//
//    /** Set new position of wrap mark for some document */
//    public void setWrapSearchMarkPos(Document doc, int pos) {
//        //!!!
//    }

    /** Add weak listener to listen to change of any property. The caller must
    * hold the listener object in some instance variable to prevent it
    * from being garbage collected.
    */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }

    public synchronized void addPropertyChangeListener(String findPropertyName,
            PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(findPropertyName, l);
    }

    /** Remove listener for changes in properties */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    /**
     * <p><b>IMPORTANT:</b> This method is public only for keeping backwards
     * compatibility of the {@link org.netbeans.editor.FindSupport} class.
     */
    public void firePropertyChange(String settingName, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(settingName, oldValue, newValue);
    }

    public void setHistory(List<SPW> spwList){
        this.historyList = new ArrayList<SPW>(spwList);
        firePropertyChange(FIND_HISTORY_CHANGED_PROP,null,null);
    }
    
    public List<SPW> getHistory(){
        return historyList;
    }
    
    public void setLastSelected(SPW spw){
        this.lastSelected = spw;
        Map<String, Object> props = getFindProperties();
        if (spw == null) return;
        props.put(FIND_WHAT, spw.getSearchExpression());
        props.put(FIND_MATCH_CASE, Boolean.valueOf(spw.isMatchCase()));
        props.put(FIND_REG_EXP, Boolean.valueOf(spw.isRegExp()));
        props.put(FIND_WHOLE_WORDS, Boolean.valueOf(spw.isWholeWords()));
    }
    
    public SPW getLastSelected(){
        return lastSelected;
    }
    
    public void addToHistory(SPW spw){
        if (spw == null) return;
        firePropertyChange(FIND_HISTORY_PROP, null, spw);
    }
    
    public final static class SPW{
        private String searchExpression;
        private boolean wholeWords;
        private boolean matchCase;
        private boolean regExp;
        
        public SPW(String searchExpression, boolean wholeWords,
            boolean matchCase, boolean regExp){
            this.searchExpression = searchExpression;
            this.wholeWords = wholeWords;
            this.matchCase = matchCase;
            this.regExp = regExp;
        }
        
        /** @return searchExpression */
        public String getSearchExpression(){
            return searchExpression;
        }

        /** @return true if the wholeWords parameter was used during search performing */
        public boolean isWholeWords(){
            return wholeWords;
        }

        /** @return true if the matchCase parameter was used during search performing */
        public boolean isMatchCase(){
            return matchCase;
        }

        /** @return true if the regExp parameter was used during search performing */
        public boolean isRegExp(){
            return regExp;
        }
        
        public boolean equals(Object obj){
            if (!(obj instanceof SPW)){
                return false;
            }
            SPW sp = (SPW)obj;
            return (this.searchExpression.equals(sp.getSearchExpression()) &&
                    this.wholeWords == sp.isWholeWords() &&
                    this.matchCase == sp.isMatchCase() &&
                    this.regExp == sp.isRegExp());
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + (this.wholeWords ? 1:0);
            result = 37*result + (this.matchCase ? 1:0);
            result = 37*result + (this.regExp ? 1:0);
            result = 37*result + this.searchExpression.hashCode();
            return result;
        }
        
        public String toString(){
            StringBuffer sb = new StringBuffer("[SearchPatternWrapper:]\nsearchExpression:"+searchExpression);//NOI18N
            sb.append('\n');
            sb.append("wholeWords:");//NOI18N
            sb.append(wholeWords);
            sb.append('\n');
            sb.append("matchCase:");//NOI18N
            sb.append(matchCase);
            sb.append('\n');
            sb.append("regExp:");//NOI18N
            sb.append(regExp);
            return  sb.toString();
        }
    } // End of SPW class
}
