/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.text.AttributedCharacterIterator;
import javax.swing.text.AttributeSet;
import javax.swing.JEditorPane;
import javax.swing.Timer;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.PrintContainer;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.Utilities;
import org.openide.text.NbDocument;
import org.openide.text.AttributedCharacters;
import org.openide.text.IndentEngine;

/**
* BaseDocument extension managing the readonly blocks of text
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorDocument extends GuardedDocument
implements NbDocument.PositionBiasable, NbDocument.WriteLockable,
NbDocument.Printable, NbDocument.CustomEditor {

    /** Name of the formatter setting. */
    public static final String FORMATTER = "formatter";

    /** Mime type of the document. The name of this property corresponds
     * to the property that is filled in the document by CloneableEditorSupport.
     */
    public static final String MIME_TYPE_PROP = "mimeType";

    /** Indent engine for the given kitClass. */
    public static final String INDENT_ENGINE = "indentEngine";

    /** Whether formatting debug messages should be displayed */
    private static final boolean debugFormat
        = Boolean.getBoolean("netbeans.debug.editor.format"); // NOI18N

    /** Formatter being used. */
    private Formatter formatter;

    private IndentEngine lastFoundIndentEngine;

    private Formatter lastFoundFormatter;

    public NbEditorDocument(Class kitClass) {
        super(kitClass);
        addStyleToLayerMapping(NbDocument.BREAKPOINT_STYLE_NAME,
                               NbDocument.BREAKPOINT_STYLE_NAME + "Layer:5000"); // NOI18N
        addStyleToLayerMapping(NbDocument.ERROR_STYLE_NAME,
                               NbDocument.ERROR_STYLE_NAME + "Layer:6000"); // NOI18N
        addStyleToLayerMapping(NbDocument.CURRENT_STYLE_NAME,
                               NbDocument.CURRENT_STYLE_NAME + "Layer:7000"); // NOI18N
        setNormalStyleName(NbDocument.NORMAL_STYLE_NAME);
    }

    public void settingsChange(SettingsChangeEvent evt) {
        super.settingsChange(evt);

        // Refresh formatter
        formatter = (Formatter)Settings.getValue(getKitClass(), FORMATTER);

        // Check whether the mimeType is set
        Object o = getProperty(MIME_TYPE_PROP);
        if (!(o instanceof String)) {
            BaseKit kit = BaseKit.getKit(getKitClass());
            putProperty(MIME_TYPE_PROP, kit.getContentType());
        }

        // Fill in the indentEngine property
        putProperty(INDENT_ENGINE, Settings.getValue(getKitClass(), INDENT_ENGINE));

        if (debugFormat) {
            System.err.println("NbEditorDocument.settingsChange() doc=" + this
                + ", mimeType=" + getProperty(MIME_TYPE_PROP)
                + ", indentEngine=" + getProperty(INDENT_ENGINE)
                + ", formatter=" + formatter
            );
        }
    }

    public void setCharacterAttributes(int offset, int length, AttributeSet s,
                                       boolean replace) {
        if (s != null) {
            Object val = s.getAttribute(NbDocument.GUARDED);
            if (val != null && val instanceof Boolean) {
                if (((Boolean)val).booleanValue() == true) { // want make guarded
                    super.setCharacterAttributes(offset, length, guardedSet, replace);
                } else { // want make unguarded
                    super.setCharacterAttributes(offset, length, unguardedSet, replace);
                }
            } else { // not special values, just pass
                super.setCharacterAttributes(offset, length, s, replace);
            }
        }
    }

    public java.text.AttributedCharacterIterator[] createPrintIterators() {
        NbPrintContainer npc = new NbPrintContainer();
        print(npc);
        return npc.getIterators();
    }

    public Component createEditor(JEditorPane j) {
        return Utilities.getEditorUI(j).getExtComponent();
    }

    public Formatter getFormatter() {
        Formatter f = formatter;
        if (f == null) {
            String mimeType = (String)getProperty(MIME_TYPE_PROP);
            if (mimeType != null) {
                IndentEngine eng = IndentEngine.find(this);
                if (eng != null) {
                    if (eng == lastFoundIndentEngine) {
                        f = lastFoundFormatter;
                    } else {
                        if (eng instanceof FormatterIndentEngine) {
                            f = ((FormatterIndentEngine)eng).getFormatter();

                        } else { // generic indent engine
                            f = new IndentEngineFormatter(getKitClass(), eng);
                        }

                        lastFoundIndentEngine = eng;
                        lastFoundFormatter = f;
                    }
                }
            }
        }

        return (f != null) ? f : super.getFormatter();
    }

    class NbPrintContainer extends AttributedCharacters implements PrintContainer {

        ArrayList acl = new ArrayList();

        AttributedCharacters a;

        NbPrintContainer() {
            a = new AttributedCharacters();
        }

        public void add(char[] chars, Font font, Color foreColor, Color backColor) {
            a.append(chars, font, foreColor);
        }

        public void eol() {
            acl.add(a);
            a = new AttributedCharacters();
        }

        public boolean initEmptyLines() {
            return true;
        }

        public AttributedCharacterIterator[] getIterators() {
            int cnt = acl.size();
            AttributedCharacterIterator[] acis = new AttributedCharacterIterator[cnt];
            for (int i = 0; i < cnt; i++) {
                AttributedCharacters ac = (AttributedCharacters)acl.get(i);
                acis[i] = ac.iterator();
            }
            return acis;
        }

    }

}
