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
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.PrintContainer;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.Formatter;
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

    /** Mime type of the document. The name of this property corresponds
     * to the property that is filled in the document by CloneableEditorSupport.
     */
    public static final String MIME_TYPE_PROP = "mimeType";

    private static final HashMap kit2Formatter = new HashMap();
    private static final int CLEAR_FORMATTER_CACHE_DELAY = 5000;
    private static final Timer clearTimer = new Timer(CLEAR_FORMATTER_CACHE_DELAY,
        new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                synchronized (kit2Formatter) {
                    kit2Formatter.clear();
                }
            }
        }
    );

    static {
        clearTimer.setRepeats(true);
        clearTimer.start();
    }

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
        String mimeType = (String)getProperty(MIME_TYPE_PROP);
        Formatter f = null;
        if (mimeType != null) {
            synchronized (kit2Formatter) {
                f = (Formatter)kit2Formatter.get(mimeType);
                if (f == null) {
                    IndentEngine eng = IndentEngine.find(mimeType);
                    if (eng != null) {
                        if (eng instanceof FormatterIndentEngine) {
                            f = ((FormatterIndentEngine)eng).getFormatter();

                        } else { // generic indent engine
                            f = new IndentEngineFormatter(getKitClass(), eng);
                        }

                        kit2Formatter.put(mimeType, f);
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

/*
 * Log
 *  15   Jaga      1.12.1.0.1.03/15/00  Miloslav Metelka Structural change
 *  14   Gandalf-post-FCS1.12.1.0    3/8/00   Miloslav Metelka 
 *  13   Gandalf   1.12        1/13/00  Miloslav Metelka Localization
 *  12   Gandalf   1.11        11/14/99 Miloslav Metelka 
 *  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         9/10/99  Miloslav Metelka 
 *  9    Gandalf   1.8         8/27/99  Miloslav Metelka 
 *  8    Gandalf   1.7         7/9/99   Miloslav Metelka 
 *  7    Gandalf   1.6         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         5/7/99   Miloslav Metelka improved setChar.Attr.()
 *  5    Gandalf   1.4         5/5/99   Miloslav Metelka 
 *  4    Gandalf   1.3         4/22/99  Miloslav Metelka 
 *  3    Gandalf   1.2         4/8/99   Miloslav Metelka 
 *  2    Gandalf   1.1         3/23/99  Miloslav Metelka 
 *  1    Gandalf   1.0         3/18/99  Miloslav Metelka 
 * $
 */

