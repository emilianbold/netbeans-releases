/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

/**
 * Text-field that validates whether its text is a valid class name (may be
 * package-classified or not) and may notify a registered listener of status
 * changes (empty/valid/invalid).
 * <p>
 * To start listening on validity of the entered class name, register
 * a <code>ChangeListener</code>.
 * <p>
 * Example:
 * <pre><code>     ...
 *     final ClassNameTextField tfClassName = new ClassNameTextField();
 *     tfClassName.setChangeListener(new ChangeListener() {
 *         public void stateChanged(ChangeEvent e) {
 *              int state = tfClassName.getState();
 *              switch (state) {
 *                  case ClassNameTextField.STATUS_EMPTY:
 *                      System.out.println("Empty class name!");
 *                      break;
 *                  case ClassNameTextField.STATUS_INVALID:
 *                      System.out.println("Invalid class name!");
 *                      break;
 *                  case ClassNameTextField.VALID:
 *                      System.out.println("Thank you!");
 *                      break;
 *              }
 *         }
 *     });
 *     panel.add(tfClassName);
 *     ...
 * </code></pre>
 *
 * @author  Marian Petras
 */
public final class ClassNameTextField extends JTextField {
    
    /** status: the class name is valid */
    public static final int STATUS_VALID = 0;
    /** status: the class name is empty */
    public static final int STATUS_EMPTY = 1;
    /** status: the class name is not valid */
    public static final int STATUS_INVALID = 2;
    /** */
    public static final int STATUS_VALID_NOT_DEFAULT = 3;

    /**
     * internal status - when the text is empty or when it ends with a dot
     * (<code>'.'</code>) and appending one legal character to it would make
     * it a legal class name
     */
    static final int STATUS_BEFORE_PART = 3;
    
    /** */
    private TextListener documentListener;
    /** */
    private int externalStatus = 0;
    /** */
    private boolean externalStatusValid = false;
    /** */
    private ChangeListener changeListener;
    /** */
    private ChangeEvent changeEvent;
    /** */
    private String defaultText;

    /**
     * Creates an empty text-field.
     */
    public ClassNameTextField() {
        this((String) null);
        setupDocumentListener();
    }
    
    /**
     * Creates an empty with initial text.
     *
     * @param  text  initial text of the text-field
     *               (for empty text, use <code>&quot;&quot;</code>
     *               or <code>null</code>)
     */
    public ClassNameTextField(String text) {
        super(text == null ? "" : text);                                //NOI18N
        setupDocumentListener();
    }
    
    /**
     */
    public void setDefaultText(String defaultText) {
        if ((defaultText == null) && (this.defaultText == null)
             || (defaultText != null) && defaultText.equals(this.defaultText)) {
            return;
        }
        
        this.defaultText = defaultText;
        
        if ((defaultText != null)
                || (externalStatusValid
                    && (externalStatus == STATUS_VALID_NOT_DEFAULT))) {
            statusMaybeChanged();
        }
    }
    
    /**
     */
    private void setupDocumentListener() {
        getDocument().addDocumentListener(
                documentListener = new TextListener());
    }

    /**
     * Determines internal status for the current text.
     * The status may be one of
     * <code>STATUS_VALID</code>, <code>STATUS_INVALID</code> and
     * <code>STATUS_BEFORE_PART</code>.
     *
     * @return  status for the current text
     */
    int determineStatus() {
        String text = getText();
        
        int status = STATUS_BEFORE_PART;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (status) {
                case STATUS_BEFORE_PART:
                    if (!Character.isJavaIdentifierStart(c)) {
                        return STATUS_INVALID;
                    }
                    status = STATUS_VALID;
                    break;
                case STATUS_VALID:
                    if (c == '.') {
                        status = STATUS_BEFORE_PART;
                    } else if (Character.isJavaIdentifierPart(c)) {
                        status = STATUS_VALID;
                    } else {
                        return STATUS_INVALID;
                    }
                    break;
                default:
                    assert false;
            }
        }
        return status;
    }
    
    /**
     * Returns status of the text.
     *
     * @return  one of <code>STATUS_EMPTY</code>,
     *                 <code>STATUS_VALID</code>,
     *                 <code>STATUS_INVALID</code>
     */
    public int getStatus() {
        if (!externalStatusValid) {
            updateExternalStatus();
        }
        return externalStatus;
    }

    /**
     */
    private void updateExternalStatus() {
        assert externalStatusValid == false;

        int internalStatus = documentListener.status;
        switch (internalStatus) {
            case STATUS_VALID:
                externalStatus = (defaultText == null)
                                        || defaultText.equals(getText())
                                 ? STATUS_VALID
                                 : STATUS_VALID_NOT_DEFAULT;
                break;
            case STATUS_BEFORE_PART:
                externalStatus = (getText().length() == 0) ? STATUS_EMPTY
                                                           : STATUS_INVALID;
                break;
            case STATUS_INVALID:
                externalStatus = STATUS_INVALID;
                break;
            default:
                assert false;
                externalStatus = STATUS_INVALID;
                break;
        }
        externalStatusValid = true;
    }
    
    /**
     * Registers a change listener.
     * The listener will be notified each time status of this text-field
     * (valid/invalid/empty) changes.
     *
     * <!-- PENDING: The listener cannot be unregistered. -->
     * <!-- PENDING: Only one listener can be registered at a time. -->
     *
     * @param  listener  change listener to be registered
     * @see  #getStatus
     */
    public void setChangeListener(ChangeListener listener) {
        changeEvent = new ChangeEvent(this);
        this.changeListener = listener;
    }
    
    /**
     */
    private void statusMaybeChanged() {
        externalStatusValid = false;

        if (changeListener != null) {
            final int prevExternalStatus = externalStatus;
            externalStatus = getStatus();
            if (externalStatus != prevExternalStatus) {
                changeListener.stateChanged(changeEvent);
            }
        }
    }
    
    /**
     */
    private final class TextListener implements DocumentListener {

        /** internal status of the class name */
        private int status;
        /** */
        private int length;

        /**
         */
        public TextListener() {
            status = determineStatus();
            length = ClassNameTextField.this.getText().length();
        }

        /**
         */
        public void changedUpdate(DocumentEvent documentEvent) {
            length = documentEvent.getDocument().getLength();
            int newStatus = determineStatus();

            if (newStatus != status) {
                status = newStatus;
                statusMaybeChanged();
            } else if ((status == STATUS_VALID) && (defaultText != null)) {
                statusMaybeChanged();     //maybe default <--> not default
            }

            assert length == getDocument().getLength();
        }

        /**
         */
        public void insertUpdate(DocumentEvent documentEvent) {
            int newStatus;
            boolean wasEmpty = (length == 0);

            if (documentEvent.getLength() != 1
                    || (documentEvent.getOffset() != length)) {
                length += documentEvent.getLength();
                newStatus = determineStatus();
            } else {
                char c;

                /* now we know that a single character was appended */
                try {
                    c = documentEvent.getDocument().getText(length++, 1)
                        .charAt(0);
                    switch (status) {
                        case STATUS_VALID:
                            newStatus = (c == '.')
                                        ? newStatus = STATUS_BEFORE_PART
                                        : (Character.isJavaIdentifierPart(c))
                                          ? STATUS_VALID
                                          : STATUS_INVALID;
                            break;
                        case STATUS_BEFORE_PART:
                            newStatus = (Character.isJavaIdentifierStart(c))
                                        ? STATUS_VALID
                                        : STATUS_INVALID;
                            break;
                        case STATUS_INVALID:
                            newStatus = determineStatus();
                            break;
                        default:
                            assert false;
                            newStatus = determineStatus();
                            break;
                    }
                } catch (BadLocationException ex) {
                    assert false;
                    
                    length = documentEvent.getDocument().getLength();
                    newStatus = determineStatus();
                }
            }

            /*
             * We must handle addition of a text to an empty text field
             * specially because it may not change internal state
             * (if it becomes STATUS_BEFORE_PART after the addition).
             */
            if ((newStatus != status) || wasEmpty) {
                status = newStatus;
                statusMaybeChanged();
            } else if ((status == STATUS_VALID) && (defaultText != null)) {
                statusMaybeChanged();     //maybe default <--> not default
            }

            assert length == getDocument().getLength();
        }

        /**
         */
        public void removeUpdate(DocumentEvent documentEvent) {
            int newStatus;

            if (documentEvent.getLength() != 1
                    || (documentEvent.getOffset() != (length - 1))) {
                length -= documentEvent.getLength();
                newStatus = determineStatus();
            } else {

                /*
                 * now we know that a single character was deleted
                 * from the end
                 */
                length--;
                switch (status) {
                    case STATUS_VALID:
                        try {
                            newStatus = ((length == 0)
                                         || (documentEvent.getDocument()
                                             .getText(length - 1, 1))
                                             .charAt(0) == '.')
                                        ? STATUS_BEFORE_PART
                                        : STATUS_VALID;
                        } catch (BadLocationException ex) {
                            assert false;
                            
                            newStatus = determineStatus();
                            length = documentEvent.getDocument().getLength();
                        }
                        break;
                    case STATUS_BEFORE_PART:
                        newStatus = STATUS_VALID;       //trailing dot deleted
                        break;
                    case STATUS_INVALID:
                        newStatus = (length == 0) ? STATUS_VALID
                                                  : determineStatus();
                        break;
                    default:
                        assert false;
                        newStatus = determineStatus();
                        break;
                }
            }

                
            /*
             * We must handle deletion of the whole text specially because
             * it may not change internal state (if it was STATUS_BEFORE_PART
             * before the deletion).
             */
            if ((newStatus != status) || (length == 0)) {
                status = newStatus;
                statusMaybeChanged();
            } else if ((status == STATUS_VALID) && (defaultText != null)) {
                statusMaybeChanged();     //maybe default <--> not default
            }

            assert length == getDocument().getLength();
        }

    }
    
}
