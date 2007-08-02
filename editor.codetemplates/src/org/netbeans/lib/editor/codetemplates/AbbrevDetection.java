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

package org.netbeans.lib.editor.codetemplates;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 * Abbreviation detection detects typing of an abbreviation
 * in the document.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

final class AbbrevDetection implements SettingsChangeListener, DocumentListener,
PropertyChangeListener, KeyListener, CaretListener {
    
    /**
     * Document property which determines whether an ongoing document modification
     * should be completely ignored by the abbreviation framework.
     * <br/>
     * This is useful e.g. for code templates parameter replication.
     */
    private static final String ABBREV_IGNORE_MODIFICATION_DOC_PROPERTY
            = "abbrev-ignore-modification"; // NOI18N

    private static final String COMPLETION_ACTIVE = "completion-active"; // NOI18N

    private static final String SURROUND_WITH = NbBundle.getMessage(SurroundWithFix.class, "TXT_SurroundWithHint_Label"); //NOI18N
    private static final int SURROUND_WITH_DELAY = 250;
    
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
    
    /**
     * Offset after the last typed character of the collected abbreviation.
     */
    private Position abbrevEndPosition;

    /**
     * Abbreviation characters captured from typing.
     */
    private StringBuffer abbrevChars = new StringBuffer();

    /** Chars on which to expand acceptor */
    private Acceptor expandAcceptor;

    /** Which chars reset abbreviation accounting */
    private Acceptor resetAcceptor;
    
    private ErrorDescription errorDescription = null;
    private List<Fix> surrounsWithFixes = null;
    private Timer surroundsWithTimer;
    
    private AbbrevDetection(JTextComponent component) {
        this.component = component;
        component.addCaretListener(this);
        doc = component.getDocument();
        if (doc != null) {
            doc.addDocumentListener(this);
        }

        Settings.addSettingsChangeListener(this);
        
        // Load the settings
        settingsChange(null);
        
        component.addKeyListener(this);
        component.addPropertyChangeListener(this);
        
        surroundsWithTimer = new Timer(0, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSurroundWithHint();
            }
        });
        surroundsWithTimer.setRepeats(false);
    }

    public void settingsChange(SettingsChangeEvent evt) {
        Document d = doc;
        Class kitClass = (d instanceof BaseDocument)
            ? ((BaseDocument)d).getKitClass()
            : org.netbeans.editor.BaseKit.class;

        expandAcceptor = SettingsUtil.getAcceptor(kitClass, SettingsNames.ABBREV_EXPAND_ACCEPTOR, AcceptorFactory.FALSE);
        resetAcceptor = SettingsUtil.getAcceptor(kitClass, SettingsNames.ABBREV_RESET_ACCEPTOR, AcceptorFactory.TRUE);
    }
    
    public void insertUpdate(DocumentEvent evt) {
        if (!isIgnoreModification()) {
            if (DocumentUtilities.isTypingModification(evt) && !isAbbrevDisabled()) {
                int offset = evt.getOffset();
                int length = evt.getLength();
                appendTypedText(offset, length);
            } else { // not typing modification -> reset abbreviation collecting
                resetAbbrevChars();
            }
        }
    }

    public void removeUpdate(DocumentEvent evt) {
        if (!isIgnoreModification()) {
            if (DocumentUtilities.isTypingModification(evt) && !isAbbrevDisabled()) {
                int offset = evt.getOffset();
                int length = evt.getLength();
                removeAbbrevText(offset, length);
            } else { // not typing modification -> reset abbreviation collecting
                resetAbbrevChars();
            }
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
            
            settingsChange(null);
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
    
    public void caretUpdate(CaretEvent evt) {
        if (evt.getDot() != evt.getMark()) {
            surroundsWithTimer.setInitialDelay(SURROUND_WITH_DELAY);
            surroundsWithTimer.restart();
        } else {
            surroundsWithTimer.stop();
            hideSurroundWithHint();
        }
    }

    private boolean isIgnoreModification() {
        return Boolean.TRUE.equals(doc.getProperty(ABBREV_IGNORE_MODIFICATION_DOC_PROPERTY));
    }
    
    private boolean isAbbrevDisabled() {
        return org.netbeans.editor.Abbrev.isAbbrevDisabled(component) || Boolean.TRUE.equals(component.getClientProperty(COMPLETION_ACTIVE));
    }
    
    private void checkExpansionKeystroke(KeyEvent evt) {
        if (abbrevEndPosition != null && component != null
                && component.getCaretPosition() == abbrevEndPosition.getOffset()
        ) {
            Document doc = component.getDocument();
            CodeTemplateManagerOperation operation = CodeTemplateManagerOperation.get(doc);
            KeyStroke expandKeyStroke = operation.getExpansionKey();
            
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
        synchronized(abbrevChars) {
            abbrevChars.setLength(0);
            abbrevEndPosition = null;
        }
    }
    
    private void appendTypedText(int offset, int insertLength) {
        if (abbrevEndPosition == null
            || offset + insertLength != abbrevEndPosition.getOffset()
        ) {
            // Does not follow previous insert
            resetAbbrevChars();
        }

        if (abbrevEndPosition == null) { // starting the new string
            try {
                // Start new accounting if previous char would reset abbrev
                // i.e. check that not start typing 'u' after existing 'p' which would
                // errorneously expand to 'public'
                if (offset == 0
                        || resetAcceptor.accept(DocumentUtilities.getText(doc, offset - 1, 1).charAt(0))
                ) {
                    abbrevEndPosition = doc.createPosition(offset + insertLength);
                }
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }

        if (abbrevEndPosition != null) {
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
                    // abbrevEndPosition should move appropriately
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
        synchronized(abbrevChars) {
            if (abbrevEndPosition != null) {
                // Abbrev position should already move appropriately
                if (offset == abbrevEndPosition.getOffset()
                    && abbrevChars.length() >= removeLength
                ) { // removed at end
                    abbrevChars.setLength(abbrevChars.length() - removeLength);

                } else {
                    resetAbbrevChars();
                }
            }
        }
    }

    public boolean expand() {
        CharSequence abbrevText = getAbbrevText();
        int abbrevEndOffset = abbrevEndPosition.getOffset();
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
    
    private void showSurroundWithHint() {
        surrounsWithFixes = SurroundWithFix.getFixes(component);
        if (!surrounsWithFixes.isEmpty()) {
            try {                
                Position pos = doc.createPosition(component.getCaretPosition());
                errorDescription = ErrorDescriptionFactory.createErrorDescription(
                        Severity.HINT, SURROUND_WITH, surrounsWithFixes, doc, pos, pos);
                
                HintsController.setErrors(doc, SURROUND_WITH, Collections.singleton(errorDescription));
            } catch (BadLocationException ble) {
                Logger.getLogger("global").log(Level.WARNING, ble.getMessage(), ble);
            }
        } else {
            hideSurroundWithHint();
        }
    }

    private void hideSurroundWithHint() {
        if (surrounsWithFixes != null)
            surrounsWithFixes = null;
        if (errorDescription != null) {
            errorDescription = null;
            HintsController.setErrors(doc, SURROUND_WITH, Collections.<ErrorDescription>emptySet());
        }
    }
}
