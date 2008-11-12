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

package org.netbeans.lib.editor.codetemplates.textsync;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TextAction;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;

/**
 * Class providing services for editing of text syncs.
 *
 * @author Miloslav Metelka
 */
public final class TextRegionEditing {
    
    // -J-Dorg.netbeans.lib.editor.codetemplates.textsync.TextRegionEditing.level=FINE
    static final Logger LOG = Logger.getLogger(TextRegionEditing.class.getName());
    
    private static final TextSyncGroupEditingNotify EMPTY_GROUP_EDITING = new TextSyncGroupEditingNotify() {
        public void deactivated(TextRegionEditing textRegionEditing, TextSync lastActiveTextSync) { }
        public void released(TextRegionEditing textRegionEditing) { }
        public void textSyncActivated(TextRegionEditing textRegionEditing, int origTextSyncIndex) { }
        public void textSyncModified(TextRegionEditing textRegionEditing) { }
    };
    
    public static TextRegionEditing get(JTextComponent component) {
        TextRegionEditing tse = (TextRegionEditing)component.getClientProperty(TextRegionEditing.class);
        if (tse == null) {
            tse = new TextRegionEditing(component);
            component.putClientProperty(TextRegionEditing.class, tse);
        }
        return tse;
    }
    
    private JTextComponent component;
    
    private Stack<GroupEditing> groupEditings;
    
    private Highlighting highlighting;
    
    private boolean overridingKeys;
    
    private ActionMap origActionMap;
    private ActionMap overrideActionMap;
    
    private TextRegionEditing(JTextComponent component) {
        this.component = component;
        this.component.addPropertyChangeListener(DocChangeListener.INSTANCE);
        this.highlighting = new Highlighting(this);
        this.groupEditings = new Stack<GroupEditing>();
    }
    
    /**
     * Start or change text sync editing mode.
     * <br/>
     * This method should only be called from AWT thread.
     * 
     * @param textSyncGroup
     * @param textSyncIndex
     */
    public void startGroupEditing(TextSyncGroup textSyncGroup, TextSyncGroupEditingNotify groupEditingNotify) {
        if (textSyncGroup == null)
            throw new IllegalArgumentException("textSyncGroup cannot be null"); // NOI18N
        if (textSyncGroup.textRegionManager() != textRegionManager())
            throw new IllegalArgumentException("textSyncGroup=" + textSyncGroup + // NOI18N
                    " does not belong to textRegionManager=" + textRegionManager()); // NOI18N
        if (groupEditingNotify == null)
            groupEditingNotify = EMPTY_GROUP_EDITING;
        
        // Notify possible parent group of temporal deactivation
        GroupEditing activeGroupEditing = activeGroupEditing();
        if (activeGroupEditing != null) {
            activeGroupEditing.notify.deactivated(this, activeGroupEditing.activeTextSync());
        }

        groupEditings.push(new GroupEditing(textSyncGroup, groupEditingNotify));
        activateTextSync(findEditableTextSyncIndex(0, +1, true, false), true);
    }
    
    public void stopGroupEditing(TextSyncGroup textSyncGroup) {
        if (activeTextSyncGroup() == textSyncGroup) {
            releaseActiveGroup(false);
        }
    }
    
    public TextSyncGroup activeTextSyncGroup() {
        GroupEditing groupEditing = activeGroupEditing();
        return (groupEditing != null) ? groupEditing.group : null;
    }
    
    public int activeTextSyncIndex() {
        GroupEditing groupEditing = activeGroupEditing();
        return (groupEditing != null) ? groupEditing.activeTextSyncIndex : -1;
    }

    public TextSync activeTextSync() {
        GroupEditing groupEditing = activeGroupEditing();
        return (groupEditing != null) ? groupEditing.activeTextSync() : null;
    }
    
    public JTextComponent component() {
        return component;
    }

    int findEditableTextSyncIndex(int textSyncIndex, int direction, boolean cycle, boolean skipCaretMarkers) {
        TextSyncGroup activeTextSyncGroup = activeTextSyncGroup();
        int tsCount = activeTextSyncGroup.textSyncsModifiable().size();
        if (tsCount == 0) { // Cannot sync without sync items
            return -1;
        }

        int startTextSyncIndex = -1;
        do { // Check whether the index is valid
            if (textSyncIndex >= tsCount) {
                if (cycle)
                    textSyncIndex = 0;
                else
                    break;
            } else if (textSyncIndex < 0) {
                if (cycle)
                    textSyncIndex = tsCount - 1;
                else
                    break;
            }
            if (startTextSyncIndex == -1)
                startTextSyncIndex = textSyncIndex;

            TextSync textSync = activeTextSyncGroup.textSyncs().get(textSyncIndex);
            if (textSync.isEditable() || (!skipCaretMarkers && textSync.isCaretMarker())) {
                return textSyncIndex;
            }
            textSyncIndex += direction;
        } while (textSyncIndex != startTextSyncIndex);
        return -1;
    }
    
    /**
     * Activate the particular textSyncIndex.
     * 
     * @param textSyncIndex index of the text sync to be activated. -1 means to release the current group.
     */
    private void activateTextSync(int textSyncIndex, boolean selectText) {
        if (textSyncIndex == -1) {
            releaseActiveGroup(false);
        } else { // Only switch inside active sync group
            GroupEditing activeGroupEditing = activeGroupEditing();
            int origIndex = activeGroupEditing.activeTextSyncIndex;
            activeGroupEditing.activeTextSyncIndex = textSyncIndex;
            TextSync activeTextSync = activeGroupEditing.activeTextSync();
            if (activeTextSync.isCaretMarker()) {
                // Place the caret at the marker and release this group
                int offset = activeTextSync.regions().get(0).startOffset();
                component.setCaretPosition(offset);
                releaseActiveGroup(false);

            } else { // Not caret marker
                textRegionManager().setActiveTextSync(activeTextSync,
                        new TextRegionManager.EditingNotify() {
                            public void modified(DocumentEvent evt) {
                                docActiveTextSyncModified();
                            }
                            public void outsideModified(DocumentEvent evt) {
                                docActiveTextSyncReleased();
                            }
                        }
                );
                ((BaseTextUI)component.getUI()).getEditorUI().getWordMatch().clear();
                if (selectText) {
                    TextRegion activeRegion = activeTextSync.masterRegion();
                    component.select(activeRegion.startOffset(), activeRegion.endOffset());
                }

                activeGroupEditing.notify.textSyncActivated(this, origIndex);
                if (!overridingKeys) {
                    overridingKeys = true;
                    // Not adding listener permanently in constructor since
                    // it grabbed the TAB key from code-template expansion
                    component.addKeyListener(OverrideKeysListener.INSTANCE);
                    ActionMap [] maps = OverrideAction.installOverrideActionMap(component);
                    origActionMap = maps[0];
                    overrideActionMap = maps[1];
                }
            }
        }
        highlighting.requestRepaint();
    }
    
    void docActiveTextSyncModified() {
        GroupEditing activeGroupEditing = activeGroupEditing();
        activeGroupEditing.notify.textSyncModified(this);
        highlighting.requestRepaint();
    }
    
    void docActiveTextSyncReleased() {
        stopSyncEditing(); // Stop sync editing when writing outside of active region bounds
    }
    
    private void releaseActiveGroup(boolean releaseAllGroups) {
        if (!groupEditings.isEmpty()) {
            GroupEditing groupEditing = groupEditings.pop();
            textRegionManager().clearActiveTextSync();
            groupEditing.notify.released(this);
            if (groupEditings.isEmpty() || releaseAllGroups) {
                if (overridingKeys) {
                    overridingKeys = false;
                    component.removeKeyListener(OverrideKeysListener.INSTANCE);

                    // check if the action map is still our overrideActionMap
                    if (overrideActionMap != component.getActionMap()) {
                        LOG.warning("The action map got tampered with! component=" //NOI18
                            + component.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(component)) //NOI18N
                            + "; doc=" + component.getDocument()); //NOI18N
                    } else {
                        component.setActionMap(origActionMap);
                    }
                    
                    overrideActionMap.clear();
                    origActionMap = null;
                    overrideActionMap = null;
                }
            }

            if (releaseAllGroups) {
                while (!groupEditings.isEmpty()) {
                    groupEditing = groupEditings.pop();
                    groupEditing.notify.released(this);
                }
            } else {
                groupEditing = activeGroupEditing();
                if (groupEditing != null) {
                    activateTextSync(groupEditing.activeTextSyncIndex, false);
                }
            }
        }
        highlighting.requestRepaint();
    }
    
    private void stopSyncEditing() {
        releaseActiveGroup(true); // release all groups
    }
    
    private GroupEditing activeGroupEditing() {
        return groupEditings.empty() ? null : groupEditings.peek();
    }
    
    boolean enterAction() {
        TextSync textSync = activeTextSync();
        if (textSync != null) {
            TextRegion<?> master = textSync.validMasterRegion();
            if (master.startOffset() <= component.getCaretPosition() && component.getCaretPosition() <= master.endOffset()) {
                activateTextSync(findEditableTextSyncIndex(activeGroupEditing().activeTextSyncIndex + 1, +1, false, false),
                        true);
                return true;
            }
            releaseActiveGroup(false);
        } else {
            // #145443 - I'm not sure why this is called when there is no active
            // TextSync, but apparently it happens under certain circumstances. So
            // let's try closing all active groups, which will terminate the special editing mode.
            releaseActiveGroup(true);
        }
        return false;
    }
    
    void escapeAction() {
        releaseActiveGroup(false);
    }
    
    void tabAction() {
        int index = findEditableTextSyncIndex(activeGroupEditing().activeTextSyncIndex + 1, +1, true, true);
        if (index != -1) {
            activateTextSync(index, true);
        } // otherwise stay on current text sync
    }
    
    void shiftTabAction() {
        int index = findEditableTextSyncIndex(activeGroupEditing().activeTextSyncIndex - 1, -1, true, true);
        if (index != -1) {
            activateTextSync(index, true);
        } // otherwise stay on current text sync
    }
    
    
    TextRegionManager textRegionManager() {
        return TextRegionManager.get(component.getDocument());
    }
    
    boolean isActive() {
        return !groupEditings.isEmpty();
    }
    
    Document document() {
        return component.getDocument();
    }
    
    private static final class GroupEditing {
        
        TextSyncGroup group;
        
        TextSyncGroupEditingNotify notify;
        
        int activeTextSyncIndex;
        
        GroupEditing(TextSyncGroup group, TextSyncGroupEditingNotify groupEditingNotify) {
            assert (group != null);
            assert (groupEditingNotify != null);
            this.group = group;
            this.notify = groupEditingNotify;
            this.activeTextSyncIndex = -1;
        }
        
        TextSync activeTextSync() {
            return group.textSyncs().get(activeTextSyncIndex);
        }

    }

    private static final class DocChangeListener implements PropertyChangeListener {

        static final DocChangeListener INSTANCE = new DocChangeListener();

        public void propertyChange(PropertyChangeEvent evt) {
            if ("document".equals(evt.getPropertyName())) {
                TextRegionEditing.get(((JTextComponent)evt.getSource())).stopSyncEditing();
            }
        }
        
    }

    private static final class OverrideAction extends TextAction {

        private static final String ORIGINAL_ACTION_PROPERTY = "original-action"; // NOI18N

        private static final int TAB = 1;
        private static final int SHIFT_TAB = 2;
        private static final int ENTER = 3;

        public static ActionMap [] installOverrideActionMap(JTextComponent component) {
            ActionMap origActionMap = component.getActionMap();
            ActionMap actionMap = new ActionMap();
            OverrideAction[] actions = new OverrideAction[]{
                new OverrideAction(TAB),
                new OverrideAction(SHIFT_TAB),
                new OverrideAction(ENTER),
            };

            // Install the actions into new action map
            for (OverrideAction action : actions) {
                Object actionKey = (String) action.getValue(Action.NAME);
                assert (actionKey != null);
                // Translate to the real key in the action map
                actionKey = action.findActionKey(component);
                if (actionKey != null) { // == null may happen during unit tests
                    Action origAction = origActionMap.get(actionKey);
                    action.putValue(ORIGINAL_ACTION_PROPERTY, origAction);
                    actionMap.put(actionKey, action);
                }
            }
            actionMap.setParent(origActionMap);
            // Install the new action map and return the original action map
            component.setActionMap(actionMap);
            return new ActionMap [] { origActionMap, actionMap };
        }

        private static String actionType2Name(int actionType) {
            switch (actionType) {
                case TAB:
                    return BaseKit.insertTabAction;
                case SHIFT_TAB:
                    return BaseKit.removeTabAction;
                case ENTER:
                    return DefaultEditorKit.insertBreakAction;
                default:
                    throw new IllegalArgumentException();
            }
        }
        private final int actionType;

        private OverrideAction(int actionType) {
            super(actionType2Name(actionType));
            this.actionType = actionType;
        }

        private TextRegionEditing textRegionEditing(ActionEvent evt) {
            JTextComponent component = getTextComponent(evt);
            if (component != null) {
                return TextRegionEditing.get(component);
            }
            return null;
        }

        public void actionPerformed(ActionEvent evt) {
            TextRegionEditing editing = textRegionEditing(evt);
            if (editing != null) {
                switch (actionType) {
                    case TAB:
                        editing.tabAction();
                        break;
                    case SHIFT_TAB:
                        editing.shiftTabAction();
                        break;
                    case ENTER:
                        if (!editing.enterAction()) {
                            Action original = (Action)getValue(ORIGINAL_ACTION_PROPERTY);
                            original.actionPerformed(evt);
                        }
                        break;
                }
            }
        }

        Object findActionKey(JTextComponent component) {
            KeyStroke keyStroke;
            switch (actionType) {
                case TAB:
                    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
                    break;
                case SHIFT_TAB:
                    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK);
                    break;
                case ENTER:
                    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            // Assume the 'a' character will trigger defaultKeyTypedAction
            Object key = component.getInputMap().get(keyStroke);
            return key;
        }
    }

    private static final class OverrideKeysListener implements KeyListener {
        
        static OverrideKeysListener INSTANCE = new OverrideKeysListener();

        public void keyPressed(KeyEvent evt) {
            TextRegionEditing editing = textRegionEditing(evt);
            if (!editing.isActive())
                return;

            KeyStroke evtKeyStroke = KeyStroke.getKeyStrokeForEvent(evt);
            if (KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0) == evtKeyStroke) {
                editing.escapeAction();
                evt.consume();

//            } else if (KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0) == evtKeyStroke) {
//                if (editing.enterAction())
//                    evt.consume();
//
//            } else if (KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0) == evtKeyStroke) {
//                if (editing.tabAction())
//                    evt.consume();
//
//            } else if (KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK) == evtKeyStroke) {
//                if (editing.shiftTabAction())
//                    evt.consume();
//
            }
        }

        public void keyTyped(KeyEvent evt) {
        }

        public void keyReleased(KeyEvent evt) {
        }
        
        private TextRegionEditing textRegionEditing(KeyEvent evt) {
            return TextRegionEditing.get(((JTextComponent)evt.getSource()));
        }
        
    }
    
    private static final class Highlighting {
        
        private static final String BAG_DOC_PROPERTY = TextRegionEditing.class.getName() + "-OffsetsBag"; // NOI18N
        
        private TextRegionEditing textRegionEditing;
        
        private AttributeSet attribs = null;
        private AttributeSet attribsLeft = null;
        private AttributeSet attribsRight = null;
        private AttributeSet attribsMiddle = null;
        private AttributeSet attribsAll = null;
        
        Highlighting(TextRegionEditing textRegionEditing) {
            this.textRegionEditing = textRegionEditing;
        }
        
        void requestRepaint() {
            Document doc = textRegionEditing.document();
            TextSync activeTextSync = textRegionEditing.activeTextSync();
            TextRegion masterRegion;
            if (activeTextSync != null && (masterRegion = activeTextSync.masterRegion()) != null) {
                // Compute attributes
                if (attribs == null) {
                    attribs = getSyncedTextBlocksHighlight();
                    Color foreground = (Color) attribs.getAttribute(StyleConstants.Foreground);
                    Color background = (Color) attribs.getAttribute(StyleConstants.Background);
                    attribsLeft = createAttribs(
                            StyleConstants.Background, background,
                            EditorStyleConstants.LeftBorderLineColor, foreground,
                            EditorStyleConstants.TopBorderLineColor, foreground,
                            EditorStyleConstants.BottomBorderLineColor, foreground);
                    attribsRight = createAttribs(
                            StyleConstants.Background, background,
                            EditorStyleConstants.RightBorderLineColor, foreground,
                            EditorStyleConstants.TopBorderLineColor, foreground,
                            EditorStyleConstants.BottomBorderLineColor, foreground);
                    attribsMiddle = createAttribs(
                            StyleConstants.Background, background,
                            EditorStyleConstants.TopBorderLineColor, foreground,
                            EditorStyleConstants.BottomBorderLineColor, foreground);
                    attribsAll = createAttribs(
                            StyleConstants.Background, background,
                            EditorStyleConstants.LeftBorderLineColor, foreground,
                            EditorStyleConstants.RightBorderLineColor, foreground,
                            EditorStyleConstants.TopBorderLineColor, foreground,
                            EditorStyleConstants.BottomBorderLineColor, foreground);
                }

                OffsetsBag nue = new OffsetsBag(doc);
                try {
                    
                    int startOffset = masterRegion.startOffset();
                    int endOffset = masterRegion.endOffset();
                    int startLine = Utilities.getLineOffset((BaseDocument) doc, startOffset);
                    int endLine = Utilities.getLineOffset((BaseDocument) doc, endOffset);

                    for (int i = startLine; i <= endLine; i++) {
                        int s = Math.max(Utilities.getRowStartFromLineOffset((BaseDocument) doc, i), startOffset);
                        int e = Math.min(Utilities.getRowEnd((BaseDocument) doc, s), endOffset);
                        int size = e - s;

                        if (size == 1) {
                            nue.addHighlight(s, e, attribsAll);
                        } else if (size > 1) {
                            nue.addHighlight(s, s + 1, attribsLeft);
                            nue.addHighlight(e - 1, e, attribsRight);
                            if (size > 2) {
                                nue.addHighlight(s + 1, e - 1, attribsMiddle);
                            }
                        }
                    }
                } catch (BadLocationException ble) {
                    LOG.log(Level.WARNING, null, ble);
                }

                OffsetsBag bag = getBag(doc);
                bag.setHighlights(nue);

            } else { // No active text sync
                OffsetsBag bag = getBag(doc);
                bag.clear();
                attribs = null;
            }
        }

        private static synchronized OffsetsBag getBag(Document doc) {
            OffsetsBag bag = (OffsetsBag) doc.getProperty(BAG_DOC_PROPERTY);
            if (bag == null) {
                bag = new OffsetsBag(doc);
                doc.putProperty(BAG_DOC_PROPERTY, bag);
            }
            return bag;
        }

        private static AttributeSet getSyncedTextBlocksHighlight() {
            FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
            AttributeSet as = fcs.getFontColors("synchronized-text-blocks-ext"); //NOI18N
            return as == null ? SimpleAttributeSet.EMPTY : as;
        }

        private static AttributeSet createAttribs(Object... keyValuePairs) {
            assert keyValuePairs.length % 2 == 0 : "There must be even number of prameters. " +
                    "They are key-value pairs of attributes that will be inserted into the set.";

            List<Object> list = new ArrayList<Object>(keyValuePairs.length);

            for (int i = keyValuePairs.length / 2 - 1; i >= 0; i--) {
                Object attrKey = keyValuePairs[2 * i];
                Object attrValue = keyValuePairs[2 * i + 1];

                if (attrKey != null && attrValue != null) {
                    list.add(attrKey);
                    list.add(attrValue);
                }
            }

            return AttributesUtilities.createImmutable(list.toArray());
        }

        public static final class HLFactory implements HighlightsLayerFactory {

            public HighlightsLayer[] createLayers(Context context) {
                return new HighlightsLayer[]{
                            HighlightsLayer.create(
                            "org.netbeans.lib.editor.codetemplates.CodeTemplateParametersHighlights", //NOI18N
                            ZOrder.SHOW_OFF_RACK.forPosition(490),
                            true,
                            getBag(context.getDocument()))
                        };
            }
        } // End of HLFactory class

    }

}
