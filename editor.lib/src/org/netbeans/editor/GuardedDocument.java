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

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Enumeration;
import java.awt.Color;
import java.awt.Font;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.Style;
import javax.swing.text.Element;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleContext;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import org.openide.util.NbBundle;
/**
* Extension to the guarded document that implements
* StyledDocument interface
*
* @author Miloslav Metelka
* @version 1.00
*/

public class GuardedDocument extends BaseDocument
    implements StyledDocument {

    /** Guarded attribute used for specifying that the inserted block
    * will be guarded.
    */
    public static final String GUARDED_ATTRIBUTE = "guarded"; // NOI18N

    /** AttributeSet with only guarded attribute */
    public static final SimpleAttributeSet guardedSet = new SimpleAttributeSet();

    /** AttributeSet with only break-guarded attribute */
    public static final SimpleAttributeSet unguardedSet = new SimpleAttributeSet();

    private static final boolean debugAtomic = Boolean.getBoolean("netbeans.debug.editor.atomic"); // NOI18N
    private static final boolean debugAtomicStack = Boolean.getBoolean("netbeans.debug.editor.atomic.stack"); // NOI18N

    // Add the attributes to sets
    static {
        guardedSet.addAttribute(GUARDED_ATTRIBUTE, Boolean.TRUE);
        unguardedSet.addAttribute(GUARDED_ATTRIBUTE, Boolean.FALSE);
    }

    public static final String FMT_GUARDED_INSERT_LOCALE = "FMT_guarded_insert"; // NOI18N
    public static final String FMT_GUARDED_REMOVE_LOCALE = "FMT_guarded_remove"; // NOI18N

    MarkBlockChain guardedBlockChain;

    /** Break the guarded flag, so inserts/removals over guarded areas will work */
    boolean breakGuarded;

    boolean atomicAsUser;

    /** Style context to hold the styles */
    protected StyleContext styles;

    /** Style to layer name mapping */
    protected Hashtable stylesToLayers;

    /** Name of the normal style. The normal style is used to reset the effect
    * of all styles applied to the line.
    */
    protected String normalStyleName;

    /**
     * Create a new guarded document.
     * 
     * @param kitClass The implementation class of the editor kit that
     *   should be used for this document.
     * 
     * @deprecated The use of editor kit's implementation classes is deprecated
     *   in favor of mime types.
     */
    public GuardedDocument(Class kitClass) {
        this(kitClass, true, new StyleContext());
    }

    /**
     * Create a new guarded document.
     * 
     * @param mimeType The mime type for this document.
     * 
     * @since 1.26
     */
    public GuardedDocument(String mimeType) {
        this(mimeType, true, new StyleContext());
    }
    
    /** 
     * Creates base document with specified style context.
     * 
    * @param kitClass class used to initialize this document with proper settings
    *   category based on the editor kit for which this document is created
     * @param addToRegistry XXX
    * @param styles style context to use
     * 
     * @deprecated The use of editor kit's implementation classes is deprecated
     *   in favor of mime types.
    */
    public GuardedDocument(Class kitClass, boolean addToRegistry, StyleContext styles) {
        super(kitClass, addToRegistry);
        init(styles);
    }
    
    /**
     * Creates base document with specified style context.
     * 
     * @param mimeType The mime type for this document.
     * @param addToRegistry XXX
     * @param styles style context to use
     * 
     * @since 1.26
     */
    public GuardedDocument(String mimeType, boolean addToRegistry, StyleContext styles) {
        super(addToRegistry, mimeType);
        init(styles);
    }
    
    private void init(StyleContext styles) {
        this.styles = styles;
        stylesToLayers = new Hashtable(5);
        guardedBlockChain = new MarkBlockChain(this) {
            protected @Override Mark createBlockStartMark() {
                MarkFactory.ContextMark startMark = new MarkFactory.ContextMark(Position.Bias.Forward, false);
                return startMark;
            }

            protected @Override Mark createBlockEndMark() {
                MarkFactory.ContextMark endMark = new MarkFactory.ContextMark(Position.Bias.Backward, false);
                return endMark;
            }
        };
    }

    /** Get the chain of the guarded blocks */
    public MarkBlockChain getGuardedBlockChain() {
        return guardedBlockChain;
    }

    public boolean isPosGuarded(int pos) {
        int rel = guardedBlockChain.compareBlock(pos, pos) & MarkBlock.IGNORE_EMPTY;
        return (rel == MarkBlock.INSIDE_BEGIN || rel == MarkBlock.INNER);
    }

    /** This method is called automatically before the document
    * is updated as result of removal. This function can throw
    * BadLocationException or its descendants to stop the ongoing
    * insert from being actually done.
    * @param evt document event containing the change including array
    *  of characters that will be inserted
    */
    protected @Override void preInsertCheck(int offset, String text, AttributeSet a)
    throws BadLocationException {
        super.preInsertCheck(offset, text, a);

        int rel = guardedBlockChain.compareBlock(offset, offset) & MarkBlock.IGNORE_EMPTY;

        if (debugAtomic) {
            System.err.println("GuardedDocument.beforeInsertUpdate() atomicAsUser=" // NOI18N
                               + atomicAsUser + ", breakGuarded=" + breakGuarded // NOI18N
                               + ", inserting text='" + EditorDebug.debugString(text) // NOI18N
                               + "' at offset=" + Utilities.debugPosition(this, offset)); // NOI18N
            if (debugAtomicStack) {
                Thread.dumpStack();
            }
        }

        if (text.length() > 0
                && (rel & MarkBlock.OVERLAP) != 0
                && rel != MarkBlock.INSIDE_END // guarded blocks have insertAfter endMark
                && !(text.charAt(text.length() - 1) == '\n'
                     && rel == MarkBlock.INSIDE_BEGIN)
           ) {
            if (!breakGuarded || atomicAsUser) {
                throw new GuardedException(
                    MessageFormat.format(
                        NbBundle.getBundle(BaseKit.class).getString(FMT_GUARDED_INSERT_LOCALE),
                        new Object [] {
                            new Integer(offset)
                        }
                    ),
                    offset
                );
            }
        }
    }

    /** This method is called automatically before the document
    * is updated as result of removal.
    */
    protected @Override void preRemoveCheck(int offset, int len)
    throws BadLocationException {
        int rel = guardedBlockChain.compareBlock(offset, offset + len);

        if (debugAtomic) {
            System.err.println("GuardedDocument.beforeRemoveUpdate() atomicAsUser=" // NOI18N
                               + atomicAsUser + ", breakGuarded=" + breakGuarded // NOI18N
                               + ", removing text='" + EditorDebug.debugChars(getChars(offset, len)) // NOI18N
                               + "'at offset=" + Utilities.debugPosition(this, offset)); // NOI18N
            if (debugAtomicStack) {
                Thread.dumpStack();
            }
        }

        if ((rel & MarkBlock.OVERLAP) != 0
                || (rel == MarkBlock.CONTINUE_BEGIN
                    && !(offset == 0 || getChars(offset - 1, 1)[0] == '\n'))
           ) {
            if (!breakGuarded || atomicAsUser) {
                // test whether the previous char before removed text is '\n'
                throw new GuardedException(
                    MessageFormat.format(
                        NbBundle.getBundle(BaseKit.class).getString(FMT_GUARDED_REMOVE_LOCALE),
                        new Object [] {
                            new Integer(offset)
                        }
                    ),
                    offset
                );
            }
        }
    }

    public void setCharacterAttributes(int offset, int length, AttributeSet attribs, boolean replace) {
        if (((Boolean)attribs.getAttribute(GUARDED_ATTRIBUTE)).booleanValue() == true) {
            guardedBlockChain.addBlock(offset, offset + length, false); // no concat
            fireChangedUpdate(getDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE, attribs));
        }
        if (((Boolean)attribs.getAttribute(GUARDED_ATTRIBUTE)).booleanValue() == false) {
            guardedBlockChain.removeBlock(offset, offset + length);
            fireChangedUpdate(getDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE, attribs));
        }
    }

    public @Override void runAtomic(Runnable r) {
        if (debugAtomic) {
            System.out.println("GuardedDocument.runAtomic() called"); // NOI18N
            if (debugAtomicStack) {
                Thread.dumpStack();
            }
        }

        boolean completed = false;
        atomicLockImpl ();
        boolean origBreakGuarded = breakGuarded;
        try {
            breakGuarded = true;
            r.run();
            completed = true;
        } finally {
            breakGuarded = origBreakGuarded;
            try {
                if (!completed) {
                    breakAtomicLock();
                }
            } finally {
                atomicUnlockImpl ();
            }
            if (debugAtomic) {
                System.out.println("GuardedDocument.runAtomic() finished"); // NOI18N
            }
        }
    }

    public @Override void runAtomicAsUser(Runnable r) {
        if (debugAtomic) {
            System.out.println("GuardedDocument.runAtomicAsUser() called"); // NOI18N
            if (debugAtomicStack) {
                Thread.dumpStack();
            }
        }

        boolean completed = false;
        atomicLockImpl ();
        boolean origAtomicAsUser = atomicAsUser;
        try {
            atomicAsUser = true;
            r.run();
            completed = true;
        } finally {
            atomicAsUser = origAtomicAsUser;
            try {
                if (!completed) {
                    breakAtomicLock();
                }
            } finally {
                atomicUnlockImpl ();
            }
            if (debugAtomic) {
                System.out.println("GuardedDocument.runAtomicAsUser() finished"); // NOI18N
            }
        }
    }

    protected @Override BaseDocumentEvent createDocumentEvent(int offset, int length,
            DocumentEvent.EventType type) {
        return new GuardedDocumentEvent(this, offset, length, type);
    }

    /** Adds style to the document */
    public Style addStyle(String styleName, Style parent) {
        String layerName = (String)stylesToLayers.get(styleName);
        if (layerName == null) {
            layerName = styleName; // same layer name as style name
            addStyleToLayerMapping(styleName, layerName);
        }

        Style style =  styles.addStyle(styleName, parent);
        if (findLayer(layerName) == null) { // not created by default
            try {
                extWriteLock();
                addStyledLayer(layerName, style);
            } finally {
                extWriteUnlock();
            }
        }
        return style;
    }

    public void addStyleToLayerMapping(String styleName, String layerName) {
        stylesToLayers.put(styleName, layerName);
    }

    /** Removes style from document */
    public void removeStyle(String styleName) {
        styles.removeStyle(styleName);
    }

    /** Fetches style previously added */
    public Style getStyle(String styleName) {
        return styles.getStyle(styleName);
    }

    /** Set the name for normal style. Normal style is used to reset the effect
    * of all aplied styles.
    */
    public void setNormalStyleName(String normalStyleName) {
        this.normalStyleName = normalStyleName;
    }

    /** Fetches the list of style names */
    public Enumeration getStyleNames() {
        return styles.getStyleNames();
    }

    /** Change attributes for part of the text.  */
    public void setParagraphAttributes(int offset, int length, AttributeSet s,
                                       boolean replace) {
        // !!! implement
    }

    /**
     * Sets the logical style to use for the paragraph at the
     * given position.  If attributes aren't explicitly set
     * for character and paragraph attributes they will resolve
     * through the logical style assigned to the paragraph, which
     * in turn may resolve through some hierarchy completely
     * independent of the element hierarchy in the document.
     *
     * @param pos the starting position >= 0
     * @param s the style to set
     */
    public void setLogicalStyle(int pos, Style s) {
        try {
            extWriteLock();
            pos = Utilities.getRowStart(this, pos);
            String layerName = (String)stylesToLayers.get(s.getName());
            // remove all applied styles
            DrawLayer[] layerArray = getDrawLayerList().currentLayers();
            for (int i = 0; i < layerArray.length; i++) {
                if (layerArray[i] instanceof DrawLayerFactory.StyleLayer) {
                    ((DrawLayerFactory.StyleLayer)layerArray[i]).markChain.removeMark(pos);
                }
            }
            // now set the requested style
            DrawLayerFactory.StyleLayer styleLayer
            = (DrawLayerFactory.StyleLayer)findLayer(layerName);
            if (styleLayer != null) {
                styleLayer.markChain.addMark(pos);
            }
            fireChangedUpdate(getDocumentEvent(
                pos, 0, DocumentEvent.EventType.CHANGE, null)); // enough to say length 0
        } catch (BadLocationException e) {
            // do nothing for invalid positions
        } finally {
            extWriteUnlock();
        }
    }

    /** Get logical style for position in paragraph */
    public Style getLogicalStyle(int pos) {
        try {
            pos = Utilities.getRowStart(this, pos);
            DrawLayer[] layerArray = getDrawLayerList().currentLayers();
            for (int i = 0; i < layerArray.length; i++) {
                DrawLayer layer = layerArray[i];
                if (layer instanceof DrawLayerFactory.StyleLayer) {
                    if (((DrawLayerFactory.StyleLayer)layer).markChain.isMark(pos)) {
                        return ((DrawLayerFactory.StyleLayer)layer).style;
                    }
                }
            }
            return getStyle(normalStyleName); // no style found
        } catch (BadLocationException e) {
            return null;
        }
    }

    /**
     * Gets the element that represents the character that
     * is at the given offset within the document.
     *
     * @param pos the offset >= 0
     * @return the element
     */
    public Element getCharacterElement(int pos) {
        return getParagraphElement(pos);
    }


    /**
     * Takes a set of attributes and turn it into a foreground color
     * specification.  This might be used to specify things
     * like brighter, more hue, etc.
     *
     * @param attr the set of attributes
     * @return the color
     */
    public Color getForeground(AttributeSet attr) {
        return null; // !!!
    }

    /**
     * Takes a set of attributes and turn it into a background color
     * specification.  This might be used to specify things
     * like brighter, more hue, etc.
     *
     * @param attr the set of attributes
     * @return the color
     */
    public Color getBackground(AttributeSet attr) {
        return null; // !!!
    }

    /**
     * Takes a set of attributes and turn it into a font
     * specification.  This can be used to turn things like
     * family, style, size, etc into a font that is available
     * on the system the document is currently being used on.
     *
     * @param attr the set of attributes
     * @return the font
     */
    public Font getFont(AttributeSet attr) {
        return new Font("Default",Font.BOLD,12); // NOI18N
    }

    /**
     * Using of <code>DrawLayer</code>s has been deprecated.
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    protected DrawLayer addStyledLayer(String layerName, Style style) {
        if (layerName != null) {
            try {
                int indColon = layerName.indexOf(':'); //NOI18N
                int layerVisibility = Integer.parseInt(layerName.substring(indColon + 1));
                DrawLayer layer = new DrawLayerFactory.StyleLayer(layerName, this, style);

                addLayer(layer, layerVisibility);
                return layer;

            } catch (NumberFormatException e) {
                // wrong name, let it pass
            }
        }
        return null;
    }

    public @Override String toStringDetail() {
        return super.toStringDetail()
               + getDrawLayerList()
               + ",\nGUARDED blocks:\n" + guardedBlockChain; // NOI18N
    }

}
