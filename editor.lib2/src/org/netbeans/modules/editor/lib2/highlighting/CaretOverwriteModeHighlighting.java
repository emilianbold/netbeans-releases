/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.highlighting;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.CaretInfo;
import org.netbeans.api.editor.EditorCaret;
import org.netbeans.api.editor.EditorCaretEvent;
import org.netbeans.api.editor.EditorCaretListener;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.lib.editor.util.ListenerList;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * Color a single character under the block caret with inverse colors in
 * overwrite mode when the caret blinking timer ticks.
 *
 * @author Miloslav Metelka
 */
public final class CaretOverwriteModeHighlighting implements HighlightsContainer, PropertyChangeListener, EditorCaretListener {

    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.lib2.highlighting.CaretOverwriteModeHighlighting"; //NOI18N

    // -J-Dorg.netbeans.modules.editor.lib2.highlighting.CaretOverwriteModeHighlighting.level=FINE
    private static final Logger LOG = Logger.getLogger(CaretOverwriteModeHighlighting.class.getName());
    
    private final JTextComponent component;

    private boolean inited;
    
    private boolean active;

    private boolean caretChanged;

    private MimePath mimePath;

    private EditorCaret editorCaret;

    private EditorCaretListener editorCaretListener;
    
    private ListenerList<HighlightsChangeListener> listenerList;
    
    private AttributeSet coloringAttrs;
    
    private LookupListener lookupListener;
    
    private List<CaretInfo> sortedCarets;

    /** Creates a new instance of CaretSelectionLayer */
    protected CaretOverwriteModeHighlighting(JTextComponent component) {
        this.component = component;
    }
    
    private void init() {
        // Determine the mime type
        String mimeType = BlockHighlighting.getMimeType(component);
        mimePath = mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType);

        component.addPropertyChangeListener(WeakListeners.propertyChange(this, component));

        Caret caret = component.getCaret();
        if (editorCaret != null) {
            editorCaretListener = WeakListeners.create(EditorCaretListener.class, this, editorCaret);
            editorCaret.addEditorCaretListener(editorCaretListener);
        }

        update(false);
    }
    
    protected final JTextComponent component() {
        return component;
    }
    
    protected final Caret caret() {
        return editorCaret;
    }
    
    // ------------------------------------------------
    // AbstractHighlightsContainer implementation
    // ------------------------------------------------
    
    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        if (!inited) {
            inited = true;
            init();
        }
        return null; // TODO
    }

    @Override
    public void addHighlightsChangeListener(HighlightsChangeListener listener) {
        listenerList.add(listener);
    }

    @Override
    public void removeHighlightsChangeListener(HighlightsChangeListener listener) {
        listenerList.remove(listener);
    }
    
    private void fireHighlightsChange() {
        // TODO
    }

    // ------------------------------------------------
    // PropertyChangeListener implementation
    // ------------------------------------------------
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null || "caret".equals(evt.getPropertyName())) { //NOI18N
            if (editorCaret != null) {
                editorCaret.removeEditorCaretListener(editorCaretListener);
                editorCaretListener = null;
            }
            
            Caret caret = component.getCaret();
            if (caret instanceof EditorCaret) {
                editorCaret = (EditorCaret) caret;
            }
            
            if (editorCaret != null) {
                editorCaretListener = WeakListeners.create(EditorCaretListener.class, this, editorCaret);
                editorCaret.addEditorCaretListener(editorCaretListener);
            }
            update(true);
        }
    }
    
    // ------------------------------------------------
    // private implementation
    // ------------------------------------------------
    
    private final void update(boolean fire) {
        ((AbstractDocument) component.getDocument()).readLock();
        try {
            sortedCarets = editorCaret.getSortedCarets();
        } finally {
            ((AbstractDocument) component.getDocument()).readUnlock();
        }
    }
    
    protected final AttributeSet getAttribs() {
        if (lookupListener == null) {
            lookupListener = new LookupListener() {
                @Override
                public void resultChanged(LookupEvent ev) {
                    @SuppressWarnings("unchecked")
                    final Lookup.Result<FontColorSettings> result = (Lookup.Result<FontColorSettings>) ev.getSource();
                    setAttrs(result);
                }
            };
            Lookup lookup = MimeLookup.getLookup(mimePath);
            Lookup.Result<FontColorSettings> result = lookup.lookupResult(FontColorSettings.class);
            setAttrs(result);
            result.addLookupListener(WeakListeners.create(LookupListener.class,
                    lookupListener, result));
        }
        return coloringAttrs;
    }
        
    private void setAttrs(Lookup.Result<FontColorSettings> result) {
        if (Boolean.TRUE.equals(component.getClientProperty("AsTextField"))) {
            if (UIManager.get("TextField.selectionBackground") != null) {
                coloringAttrs = AttributesUtilities.createImmutable(
                        StyleConstants.Background, (Color) UIManager.get("TextField.selectionBackground"),
                        StyleConstants.Foreground, (Color) UIManager.get("TextField.selectionForeground"));
            } else {
                final JTextField referenceTextField = (JTextField) new JComboBox<String>().getEditor().getEditorComponent();
                coloringAttrs = AttributesUtilities.createImmutable(
                        StyleConstants.Background, referenceTextField.getSelectionColor(),
                        StyleConstants.Foreground, referenceTextField.getSelectedTextColor());
            }
        } else {
            FontColorSettings fcs = result.allInstances().iterator().next();
            coloringAttrs = fcs.getFontColors(FontColorNames.CARET_COLOR_OVERWRITE_MODE);
            if (coloringAttrs == null) {
                coloringAttrs = SimpleAttributeSet.EMPTY;
            }
        }
    }

    @Override
    public void caretChanged(EditorCaretEvent evt) {
        caretChanged = true;
        if (active) {
            update(true);
        }
    }

    private final class HS implements HighlightsSequence {
        
        int caretOffset;

        @Override
        public boolean moveNext() {
            return true; // TODOO
        }

        @Override
        public int getStartOffset() {
            return caretOffset;
        }

        @Override
        public int getEndOffset() {
            return caretOffset + 1;
        }

        @Override
        public AttributeSet getAttributes() {
            return coloringAttrs;
        }
        
        
    }
}
