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

package org.netbeans.lib.editor.codetemplates;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.ErrorManager;


/**
 * Abbreviation detection detects typing of an abbreviation
 * in the document.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

final class AbbrevDetection implements SettingsChangeListener, DocumentListener,
PropertyChangeListener, KeyListener {
    
    private static final AbbrevExpander[] abbrevExpanders = { new CodeTemplateAbbrevExpander() };
    
    public static AbbrevDetection get(JTextComponent component) {
        AbbrevDetection ad = (AbbrevDetection)component.getClientProperty(AbbrevDetection.class);
        if (ad == null) {
            ad = new AbbrevDetection(component);
            component.putClientProperty(AbbrevDetection.class, ad);
        }
        return ad;
    }
    
    private JTextComponent component;
    
    /** Document for which this abbreviation detection was constructed. */
    private Document doc;
    
    private Class kitClass;

    /**
     * Offset after the last typed character of the collected abbreviation.
     */
    private int abbrevEndOffset;

    /**
     * Abbreviation characters captured from typing.
     */
    private StringBuffer abbrevChars = new StringBuffer();

    /** Chars on which to expand acceptor */
    private Acceptor expandAcceptor;

    /** Which chars reset abbreviation accounting */
    private Acceptor resetAcceptor;
    
    private AbbrevDetection(JTextComponent component) {
        this.component = component;
        doc = component.getDocument();
        if (doc != null) {
            doc.addDocumentListener(this);
        }

        kitClass = (doc instanceof BaseDocument)
            ? ((BaseDocument)doc).getKitClass()
            : org.netbeans.editor.BaseKit.class;

        Settings.addSettingsChangeListener(this);
        
        // Load the settings
        settingsChange(null);
        
        component.addKeyListener(this);
        component.addPropertyChangeListener(this);
    }

    public void settingsChange(SettingsChangeEvent evt) {
        expandAcceptor = SettingsUtil.getAcceptor(kitClass, SettingsNames.ABBREV_EXPAND_ACCEPTOR, AcceptorFactory.FALSE);
        resetAcceptor = SettingsUtil.getAcceptor(kitClass, SettingsNames.ABBREV_RESET_ACCEPTOR, AcceptorFactory.TRUE);
    }
    
    public void insertUpdate(DocumentEvent evt) {
        if (DocumentUtilities.isTypingModification(evt)) {
            int offset = evt.getOffset();
            int length = evt.getLength();
            appendTypedText(offset, length);
        } else { // not typing modification -> reset abbreviation collecting
            resetAbbrevChars();
        }
    }

    public void removeUpdate(DocumentEvent evt) {
        if (DocumentUtilities.isTypingModification(evt)) {
            int offset = evt.getOffset();
            int length = evt.getLength();
            removeAbbrevText(offset, length);
        } else { // not typing modification -> reset abbreviation collecting
            resetAbbrevChars();
        }
    }

    public void changedUpdate(DocumentEvent evt) {
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("document".equals(evt.getPropertyName())) {
            if (doc != null) {
                doc.removeDocumentListener(this);
            }
            
            doc = component.getDocument();
            if (doc != null) {
                doc.addDocumentListener(this);
            }
        }
    }
    
    public void keyPressed(KeyEvent evt) {
        checkExpansionKeystroke(evt);
    }
    
    public void keyReleased(KeyEvent evt) {
        checkExpansionKeystroke(evt);
    }
    
    public void keyTyped(KeyEvent evt) {
        checkExpansionKeystroke(evt);
    }
    
    private void checkExpansionKeystroke(KeyEvent evt) {
        KeyStroke expandKeyStroke = AbbrevSettings.getDefaultExpansionKeyStroke();
        if (component != null) {
            Document doc = component.getDocument();
            String mimeType = (String)doc.getProperty("mimeType"); // NOI18N
            if (mimeType != null) {
                expandKeyStroke = AbbrevSettings.get(mimeType).getExpandKeyStroke();
            }
            
            if (expandKeyStroke.equals(KeyStroke.getKeyStrokeForEvent(evt))) {
                if (expand()) {
                    evt.consume();
                }
            }
        }
    }

    /**
     * Get current abbreviation string.
     */
    private CharSequence getAbbrevText() {
        return abbrevChars;
    }

    /**
     * Reset abbreviation string collecting.
     */
    private void resetAbbrevChars() {
        abbrevChars.setLength(0);
        abbrevEndOffset = -1;
    }
    
    private void appendTypedText(int offset, int insertLength) {
        if (offset != abbrevEndOffset) { // does not follow previous insert
            resetAbbrevChars();
        }
        if (abbrevEndOffset == -1) { // starting the new string
            try {
                // Start new accounting if previous char would reset abbrev
                // i.e. check that not start typing 'u' after existing 'p' which would
                // errorneously expand to 'public'
                if (offset == 0
                        || resetAcceptor.accept(doc.getText(offset - 1, 1).charAt(0))
                ) {
                    abbrevEndOffset = offset;
                }
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        if (abbrevEndOffset != -1) {
            try {
                String typedText = doc.getText(offset, insertLength); // typically just one char
                boolean textAccepted = true;
                for (int i = typedText.length() - 1; i >= 0; i--) {
                    if (resetAcceptor.accept(typedText.charAt(i))) {
                        // In theory there could be more than one character in the typed text
                        // and the resetting could occur on the very first char
                        // the next chars would not be accumulated as the insert
                        // is treated as a batch.
                        textAccepted = false;
                        break;
                    }
                }
                
                if (textAccepted) {
                    abbrevChars.append(typedText);
                    abbrevEndOffset += typedText.length();
                } else {
                    resetAbbrevChars();
                }

            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
                resetAbbrevChars();
            }
        }
    }
    
    private void removeAbbrevText(int offset, int removeLength) {
        if (abbrevEndOffset != -1) {
            if (offset + removeLength == abbrevEndOffset) { // removed at end
                abbrevChars.setLength(abbrevChars.length() - removeLength);
                abbrevEndOffset -= removeLength;
            } else {
                resetAbbrevChars();
            }
        }
    }

    public boolean expand() {
        CharSequence abbrevText = getAbbrevText();
        for (int i = 0; i < abbrevExpanders.length; i++) {
            if (abbrevExpanders[i].expand(component, 
                    abbrevEndOffset - abbrevText.length(), abbrevText)
            ) {
                resetAbbrevChars();
                return true;
            }
        }
        return false;
    }

}
