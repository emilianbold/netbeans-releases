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

package org.netbeans.modules.i18n;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.Dialog;
import java.beans.Introspector;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.KeyStroke;
import javax.swing.event.*;
import javax.swing.text.*;

import org.openide.awt.UndoRedo;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.cookies.SourceCookie;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.src.*;
import org.openide.text.NbDocument;
import org.openide.TopManager;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;

import org.netbeans.modules.properties.PropertiesDataObject;
import org.netbeans.modules.properties.Util;

import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADComponent.RADProperty;
import org.netbeans.modules.form.RADConnectionPropertyEditor.RADConnectionDesignValue;


/**
 * I18N support class. Dependent on the editor module and the form module.
 *
 * @author   Petr Jiricka
 */
public class I18nSupport {
    
    /** Variable for debug purposes. */
    private static final String DEBUG = "netbeans.debug.exception"; // NOI18N

    /** Name of property in document holding I18nFinder reference. */
    public static final String I18N_FINDER_PROP = "org.netbeans.modules.i18n.I18N_FINDER"; // NOI18N
    
    /** Common name for I18N mode. */
    public static final String I18N_MODE = "internationalization"; // NOI18N
    
    /** Holds the only instance of I18nSupport. */
    private static I18nSupport instance;
    
    /** Instance of i18n info. */
    private I18nInfo i18nInfoInstance;
    
    /** Reference to top component providing internationalize dialog. */
    private TopComponent topComponent;
    
    /** Reference to <code>i18nPanel</code>, part of internationalize dialog. */
    private I18nPanel i18nPanel;
    
    /** Reference to current pane containing searched java document. */
    private JEditorPane currentComponent;
    
    /** Reference to editor cookie of currently searched java documetnt. */
    private EditorCookie editCook;
    
    /** Saves position after last founded just-internationalized string */
    private Position lastPos;
    
    /** Collection for holding properties of form and their components, if search is in form performed. */
    private TreeSet formProperties;
    
    /** Holds document object. */
    private StyledDocument document;
    
    /** Holds <code>DataObject</code> which document is internationalized. 
     * @see org.openide.loaders.DataObject */
    private DataObject targetDataObject;
    
    /** Listener which listens on changes of form document. */
    DocumentListener listener;
    
    /** Helper variable. Holds info if last action performed skip or replace respectivelly. */
    private boolean skipped;
    
    /** Helper variable. Holds last found ValidFormProperty from formProperties collection, to increment
     * skip count in case skip operation on that property was performed. */
    private ValidFormProperty validProp;

    /** Helper variable used in <code>isGuardedPosition(int)</code> method.
     * @see #isGuardedPosition */
    private boolean guardedPosition;
    
    
    /** Constructor. Don't call this. Use getI18nSupport method instead. */
    private I18nSupport(StyledDocument document, DataObject targetDataObject) {
        initialize(document, targetDataObject);
    }

    
    /** Initializes the <code>document</code> and <code>targetDataObject</code> variables. */
    private void initialize(StyledDocument document, DataObject targetDataObject) {
        if(this.document == null || !this.document.equals(document) )
            this.document = document;
        if(this.targetDataObject == null || !this.targetDataObject.equals(targetDataObject) )
            this.targetDataObject = targetDataObject;
    }
    
    /** Gets the only insance of I18nSupport. */
    public static I18nSupport getI18nSupport(StyledDocument document, DataObject targetDataObject) {
        if(instance == null)
            instance = new I18nSupport(document, targetDataObject);
        else {
            instance.initialize(document, targetDataObject);
        }
            
        return instance;
    }
    
    /** The 'heart' method called by <code>I18nAction</code>. */
    public void internationalize() {
        reset(); // always reset the dialog
       
        // initialize the component
        editCook = (EditorCookie)targetDataObject.getCookie(EditorCookie.class);
        if (editCook == null)
            return;  // PENDING
        JEditorPane[] panes = editCook.getOpenedPanes();
        if (panes == null) {
            NotifyDescriptor.Message message = new NotifyDescriptor.Message(NbBundle.getBundle(I18nModule.class).
            getString("MSG_CouldNotOpen"), NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(message);
            return;
        }
        currentComponent = panes[0];
        currentComponent.getCaret().setDot(0);
        // initializes the finder
        getI18nFinder().initialize();
        
        // add listener on form in case the guarded block was changed add new components to the formProperties,
        // PENDING 1) how to reset the position of lastPosition
        //         2) how to remove from formProperties removed components
        if(targetDataObject instanceof FormDataObject) {
            // create form properties
            createFormProperties();
            
            listener = new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                }
                
                public void insertUpdate(DocumentEvent e) {
                    updateFormProperties();
                }
                
                public void removeUpdate(DocumentEvent e) {
                }
            };
            document.addDocumentListener(listener);
        }
        
        // do the search
        if (find()) {
            createDialog();
            fillDialogValues();
            showDialog();
        } else {
            NotifyDescriptor.Message message = new NotifyDescriptor.Message(NbBundle.getBundle(I18nModule.class).
            getString("MSG_NoInternationalizableString"), NotifyDescriptor.INFORMATION_MESSAGE); // to info message
            TopManager.getDefault().notify(message);
        }
    }
    
    
    /** Ensures that the component has not been closed.
     *  @param position position to set the cursor to. If position == -1, sets to the cursor position in
     *  the previously used component.
     * Returns true if the component was validated. */
    private boolean ensureComponentValid(int position) {
        JEditorPane[] panes = editCook.getOpenedPanes();
        if (panes == null)
            return false;
        // try the ones which are open now
        for (int i=0; i < panes.length; i++) {
            if (panes[i] == currentComponent) {
                currentComponent.requestFocus();
                if (position != -1)
                    currentComponent.getCaret().setDot(position);
                return true;
            }
        }
        // not found
        int dot = (position != -1) ?
        position :                                // case 1
            ((currentComponent == null) ?             // case 2
            0 :                                     // case 2a
                currentComponent.getCaret().getDot());  // case 2b
                currentComponent = panes[0];
                currentComponent.getCaret().setDot(dot);
                return true;
    }
    
    /** Gets i18n info. */
    private I18nInfo getI18nInfo() {
        if (i18nInfoInstance == null)
            i18nInfoInstance = new I18nInfo();
        return i18nInfoInstance;
    }
    
    /** Resets variables when restarting <code>I18nAction</code>. */
    private void reset() {
        i18nInfoInstance = null;
        currentComponent = null;
        lastPos = null;
        validProp = null;
        skipped = false;
        closeDialog();
    }
    
    /* Replace button handler. */
    private void doReplace() {
        ResourceBundleString rbString = null;
        try {
            rbString = (ResourceBundleString)i18nPanel.getResourceBundlePanel().getPropertyValue();
        } catch (IllegalStateException e) {
            NotifyDescriptor.Message nd = new NotifyDescriptor.Message(NbBundle.getBundle(I18nModule.class).
            getString("EXC_BadKey"), NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(nd);
            return;
        }
        
        ResourceBundleStringEditor rbStringEditor = new ResourceBundleStringEditor();
        rbStringEditor.setValue(rbString);
        
        if (rbString instanceof ResourceBundleStringForm) {
            // Form.
            // Note: Last argument (replace string) is get via getReplace method, cause the getJavaInitialization method
            // which causes also (there is a hook) creating field for bundle (if needed) is invoked from form module while regenerating guarded blocks.
            replaceInForm(getI18nInfo().getNodeProperty(), (ResourceBundleStringForm)rbString, rbStringEditor.getReplaceString());
        } else {
            // PENDING
            // Hack for setting targetDataObject.
            rbStringEditor.setTargetDataObject(targetDataObject);
            
            // No form (could be guarded, see guarded blocks in beaninfo, but this case doesn't fear us).
            // Note: Lst argument (replace string) is get via getJavaInitialization method which causes also (teher is a hook) cretaing field for bundle
            // (if needed).
            replaceDirect(getI18nInfo().getPosition().getOffset(), getI18nInfo().getLength(), rbStringEditor.getJavaInitializationString());
        }
        
        skipped = false;
        
        if (find()) {
            createDialog();
            fillDialogValues();
            showDialog();
        }
        else
            doCancel();
    }
    
    /* Replace All button handler. At the time does nothing. */
    private void doReplaceAll() {
        // PENDING
    }
    
    /* Skip button handler. */
    private void doSkip() {
        skipped = true;
        if (find()) {
            createDialog();
            fillDialogValues();
            showDialog();
        }
        else
            doCancel();
    }
    
    /* Cancel button handler. */
    private void doCancel() {
        // no memory leaks
        document = null;
        targetDataObject = null;
        lastPos = null;
        currentComponent = null;
        i18nInfoInstance = null;
        editCook = null;
        validProp = null;
        formProperties = null;
        listener = null;
        closeDialog();
    }
    
    /** Replaces found hard coded string outside of guarded blocks (or beaninfo guarded blocks respectivelly). */
    private void replaceDirect(final int startPos, final int length, final String replaceString) {
        if (ensureComponentValid(startPos)) {
            // Call runAtomic method to break guarded flag if it is necessary. (For non-guarded works as well).
            NbDocument.runAtomic(
            document,
            new Runnable() {
                public void run() {
                    try {
                        if (length > 0) {
                            document.remove(startPos, length);
                        }
                        if (replaceString != null && replaceString.length() > 0) {
                            document.insertString(startPos, replaceString, null);
                        }
                    } catch (BadLocationException ble) {
                        NotifyDescriptor.Message message = new NotifyDescriptor.Message(NbBundle.getBundle(I18nModule.class).
                        getString("MSG_CouldNotReplace"), NotifyDescriptor.ERROR_MESSAGE);
                        TopManager.getDefault().notify(message);
                    }
                }
            });
        }
    }
    
    /** Replaces found hard coded string in guarded blocks. */
    private void replaceInForm(Node.Property property, ResourceBundleStringForm rbStringForm, String replaceString) {
        try {
            // remember position offset before change of guarded block
            int pos;
            if(lastPos != null)
                pos = lastPos.getOffset();
            else
                pos = currentComponent.getCaret().getDot();
            
            // new value to set
            Object newValue;
            
            // old value
            Object oldValue = property.getValue();
            
            // RAD property -> like text, title etc.
            if(property instanceof RADProperty) {
                if(oldValue instanceof RADConnectionDesignValue
                && ((RADConnectionDesignValue)oldValue).getType() == RADConnectionDesignValue.TYPE_CODE) {
                    // The old value is set via RADConnectionPropertyEditor,
                    // (in our case if value was RADConnectionDesignValue of type TYPE_CODE (= user code))
                    String oldString = (String)((RADConnectionDesignValue)oldValue).getDesignValue(((RADProperty)property).getRADComponent());
                    StringBuffer buff = new StringBuffer(oldString);
                    
                    int index = indexOfNonI18nString(oldString, getI18nInfo().getHardString(), validProp.getSkip());
                    if (index == -1) {
                        NotifyDescriptor.Message message = new NotifyDescriptor.Message(NbBundle.getBundle(I18nModule.class).
                        getString("MSG_StringNotFoundInGuarded"), NotifyDescriptor.ERROR_MESSAGE);
                        TopManager.getDefault().notify(message);
                        return;
                    }
                    
                    int startOffset = index;
                    // The last operand in expression + 2 stands for double quotes for hard string.
                    int endOffset = startOffset + getI18nInfo().getHardString().length() + 2;
                    
                    buff.replace(startOffset, endOffset, replaceString);
                    
                    RADConnectionDesignValue newConnectionValue = new RADConnectionDesignValue(buff.toString());
                    newValue = newConnectionValue;
                } else {
                    // The old value is set via ResourceBundleStringFormEditor,
                    // (in our case if value was "plain string" or RADConnectionDesignValue of type TYPE_VALUE.
                    ((RADProperty)property).setCurrentEditor(new ResourceBundleStringFormEditor());
                    newValue = rbStringForm;
                }
            } else {
                // Node.Property -> code generation properties.
                // Replace the part of old value which matches "quoted" hardString only.
                String oldString = (String)oldValue;
                StringBuffer buff = new StringBuffer(oldString);
                
                int index = indexOfNonI18nString(oldString, getI18nInfo().getHardString(), validProp.getSkip());
                
                if (index == -1) {
                    NotifyDescriptor.Message message = new NotifyDescriptor.Message(NbBundle.getBundle(I18nModule.class).
                    getString("MSG_StringNotFoundInGuarded"), NotifyDescriptor.ERROR_MESSAGE); // NOI18N
                    TopManager.getDefault().notify(message);
                    return;
                }
                
                int startOffset = index;
                // The last operand in expression + 2 stands for double quotes to hard string.
                int endOffset = startOffset + getI18nInfo().getHardString().length() + 2;
                
                buff.replace(startOffset, endOffset, replaceString);
                
                newValue = buff.toString();
            }
            
            // Finally set the new value to property.
            property.setValue(newValue);
            
            // Little trick to reset new position after guarded block was regenerated.
            if (document instanceof AbstractDocument)
                ((AbstractDocument)document).readLock();
            try {
                pos += replaceString.length() - getI18nInfo().getHardString().length() + 2;
                lastPos = document.createPosition(pos);
            } catch (BadLocationException ble) {
                if(Boolean.getBoolean(DEBUG))
                    System.err.println("I18nSupport: Position reset in guarded block not successful."); // NOI18N
            } catch (Exception e) {
                if(Boolean.getBoolean(DEBUG))
                    e.printStackTrace();
            } finally {
                if(document instanceof AbstractDocument)
                    ((AbstractDocument)document).readUnlock();
            }
        } catch (IllegalAccessException iae) {
            if(Boolean.getBoolean(DEBUG))
                iae.printStackTrace();
        } catch (InvocationTargetException ite) {
            if(Boolean.getBoolean(DEBUG))
                ite.printStackTrace();
        }
    }
    
    /** Finds from the <code>lastPos</code> (or the current position if position == -1).
     * @return true if a hardcoded string was found
     * @see #lastPos
     */
    private boolean find() {
        if (ensureComponentValid(-1)) {
            int dotPos = -1;
            int[] ret;
            
            do {
                // Clean values from possibly previous search.
                getI18nInfo().cleanFormValues();                
                
                Caret caret = currentComponent.getCaret();
                // get position from previous search
                if (lastPos != null) {
                    dotPos = lastPos.getOffset();
                } else {
                    // set caret position
                    dotPos = caret.getDot();
                }

                // Find hard coded string in currentComponent startting at dotPos position.
                ret = find(currentComponent, dotPos, -1);

                if (ret != null) {
                    try {
                        // Set i18n info values.
                        getI18nInfo().setPosition(document.createPosition(ret[0]));
                        getI18nInfo().setLength(ret[1]);
                        getI18nInfo().setHardString(extractString(document.getText(ret[0], ret[1])));
                        try {
                            javax.swing.text.Element paragraph = document.getParagraphElement(ret[0]);
                            getI18nInfo().setHardLine(document.getText(paragraph.getStartOffset(), paragraph.getEndOffset()-paragraph.getStartOffset()).trim());
                        } catch (BadLocationException ble) {
                            getI18nInfo().setHardLine(""); // NOI18N
                        }
                        getI18nInfo().setGuarded(isGuardedPosition(ret[0]));

                    }
                    catch (BadLocationException e) {
                        throw new InternalError();
                    }
                    // Highlight found hard coded string.
                    caret.setDot(ret[0]);
                    caret.moveDot(ret[0] + ret[1]);
                } else {
                    // not found in entire source document
                    return false;
                }

                // save position after just found string
                try {
                    lastPos = document.createPosition(ret[0]+ret[1]);
                } catch (BadLocationException ble) {
                    lastPos = null;
                }

            // Skip found hardcoded string if is in form, guarded and not found appropriate form component property.
            } while (getI18nInfo().isGuarded() && targetDataObject instanceof FormDataObject && !findInForm());

            return true;
        }
        return false;
    }
    
    /** 
     * Ugly tricky method for testing the position in document, if it is in guarded block or not.
     * It works the way it tries to insert a piece of text in document
     * at the specified offset via <code>org.openide.text.NbDocument.runAtomicAsUser</code> method.
     * If the exception is thrown the it's considered as in guarded block.
     * (Note: You have to be sure before the offset is valid in the document)
     * If not its non-guarded and immediatelly afterwards the inserted piece of text is undone.
     * @see org.openide.text.NbDocument#runAtomicAsUser
     * */
    synchronized boolean isGuardedPosition(final int position) {
        guardedPosition = false ;

        final StyledDocument document = this.document;
        
        try {
            NbDocument.runAtomicAsUser(
            document,
            new Runnable() {
                public void run() {
                    try {
                        document.insertString(position, "x", null);
                    } catch (BadLocationException ble) {
                        // Is in guarded
                        // It is possible to set it this way cause this method is called directly in the same thread.
                        // The new thread is not invoked in runAtomicAsUser method.
                        guardedPosition = true;
                    }
                }
            }
            );
        } catch (BadLocationException ble) {
            // Shouldn't happen it was catched already.
        } finally {
            // If was notguarded -> means "x" test string was inserted -> undo it.
            if(!guardedPosition) {
                SourceCookie.Editor sec = (SourceCookie.Editor)targetDataObject.getCookie(SourceCookie.Editor.class);
                if(sec != null) {
                    JEditorPane[] panes = sec.getOpenedPanes();
                    if(panes != null && panes.length > 0) {
                        TopComponent tp = (TopComponent)SwingUtilities.getAncestorOfClass(TopComponent.class, panes[0]);
                        if(tp != null) {
                            UndoRedo undoRedo = tp.getUndoRedo();
                            if(undoRedo != null) {
                                if(undoRedo.canUndo())
                                    undoRedo.undo();
                            }

                        }

                    }
                }
            }

        }

        return guardedPosition;
    }
    
    /** Extracts pure string from hard coded one (without start and end double quotes). */
    private String extractString(String sourceString) {
        if ((sourceString.length() >= 2) &&
        (sourceString.charAt(0) == '"') &&
        (sourceString.charAt(sourceString.length() - 1) == '"'))
            sourceString = sourceString.substring(1, sourceString.length() - 1);
        return sourceString;
    }
    
    /** Find the searched expression
     * @startPos position where the start of the search will occur. It must
     *   be valid position greater or equal than zero
     * @endPos position where the search will stop. -1 doesn't mean the end
     *   of document in this case but rather the default behavior which
     *   depends on the direction and wrapping.
     * @return position and length of the text found provided in array
     *    containing these two ints; returns null if nothing is found
     */
    private int[] find(JTextComponent c, int startPos, int endPos) {
        boolean wrap = false;
        if (c != null) {
            I18nFinder finder = getI18nFinder();
            int pos = -1;
            try {
                final int docLen = document.getLength();
                if (startPos == -1) {
                    startPos = docLen;
                }
                
                int restPatch = 2; // !!! quick infinite loop patch
                while (true && restPatch-- > 0) {
                    int limitPos;
                    if (endPos == -1) { // invalid pos
                        limitPos = docLen;
                    } else {
                        if (startPos < endPos) {
                            limitPos = endPos;
                        } else {
                            limitPos = docLen;
                        }
                    }
                    
                    // Call find method passing finder and start and end position.
                    pos = find(finder, startPos, limitPos);
                    
                    if (pos != -1) {
                        break;
                    }
                    
                    if (wrap) {
                        if (endPos == -1) {
                            if (limitPos == docLen) {
                                startPos = 0;
                            } else {
                                break;
                            }
                        } else { // endPos != -1 (&& wrap)
                            if (limitPos == endPos) {
                                break;
                            }
                            startPos = 0;
                        }
                    } else { // no wrap set
                        break;
                    }
                } // end of while
                
            } catch (BadLocationException e) {
                throw new Error(); // shouldn't happen
            }
            
            if (pos != -1) {
                int[] ret = new int[4];
                ret[0] = pos;
                ret[1] = finder.getFoundLength();
                ret[2] = finder.getLineStart();
                ret[3] = finder.getLineLength();
                return ret;
            }
        }
        return null;
    }
    
    /** Method which actually calls finder to find a string in document.
     * Note: Code copied and adjusted from org.netbeans.editor.DocCache to avoid dependency on editor module. */
    private int find(I18nFinder finder, int startPos, int endPos) throws BadLocationException {
        int docLen = document.getLength();
        
        if (startPos == -1) {
            startPos = docLen;
        }
        if (endPos == -1) {
            endPos = docLen;
        }

        // Check bounds.
        if (startPos < 0 || startPos > docLen) {
            throw new BadLocationException("DocCache: Invalid offset " + startPos // NOI18N
                                           + ". Document length is " + docLen, startPos); // NOI18N
        }
        if (endPos < 0 || endPos > docLen) {
            throw new BadLocationException("DocCache: Invalid offset " + endPos // NOI18N
                                           + ". Document length is " + docLen, endPos); // NOI18N
        }

        // Resets finder.
        finder.reset();
        if (startPos == endPos) { 
            // Immediate return to avoid needless search.
            return -1;
        }
        boolean forward = (startPos < endPos);
        int pos = forward ? startPos : (startPos - 1);

        while (true) {
            // Call finder to find a string in the buffer.
            pos = finder.find(0,
                // Gets the all document text.
                document.getText(0, docLen).toCharArray(),
                forward ? startPos : endPos,
                forward ? endPos : startPos,
                pos, endPos);

            if (finder.isFound()) {
                if (forward) {
                    if (pos < startPos || pos > endPos) {
                        return -1; // invalid position returned
                    }
                } else { // searching backward
                    if (pos < endPos || pos > startPos) {
                        return -1; // invalid position returned
                    }
                }
                return pos;

            } else { // not yet found

                // Check position correctness. It eliminates
                // also the equalities because the empty buffer
                // would be pzssed in these cases to the finder
                if (forward) { // searching forward
                    if (pos < startPos || pos >= endPos) {
                        return -1;
                    }
                } else { // searching backward
                    if (pos < endPos || pos >= startPos) {
                        return -1; // not found
                    }
                }
            }
        } // End of infinite loop.

    }
     
    /** Get i18n finder */
    private I18nFinder getI18nFinder() {
        I18nFinder i18nFinder;
        i18nFinder = (I18nFinder)document.getProperty(I18N_FINDER_PROP);
        
        if (i18nFinder == null) {
            i18nFinder = new I18nFinder();
            document.putProperty(I18N_FINDER_PROP, i18nFinder);
        }
        
        return i18nFinder;
    }
    
    /** Creates dialog. In our case it is a top component. */
    private void createDialog() {
        if (topComponent == null) {
            
            // prepare panel which will reside inside top component
            i18nPanel = new I18nPanel();
            
            final JButton[] buttons = i18nPanel.getButtons();
            
            for (int i = 0; i < buttons.length; i++) {
                buttons[i].addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        if (evt.getSource() == buttons[0])
                            doReplace();
                        if (evt.getSource() == buttons[1])
                            doReplaceAll();
                        if (evt.getSource() == buttons[2])
                            doSkip();
                        if (evt.getSource() == buttons[3])
                            doCancel();
                    }
                });
            }
            
            // actually create the dialog as top component
            topComponent = new TopComponent();
            topComponent.setCloseOperation(TopComponent.CLOSE_EACH);
            topComponent.setLayout(new BorderLayout());
            topComponent.add(i18nPanel, BorderLayout.CENTER);
            topComponent.setName(targetDataObject.getName());
            
            // dock into I18N mode if possible
            Workspace[] currentWs = TopManager.getDefault().getWindowManager().getWorkspaces();
            for (int i = currentWs.length; --i >= 0; ) {
                Mode i18nMode = currentWs[i].findMode(I18N_MODE);
                if (i18nMode == null) {
                    i18nMode = currentWs[i].createMode(
                    I18N_MODE,
                    NbBundle.getBundle(I18nModule.class).getString("CTL_I18nDialogTitle"),
                    PropertiesDataObject.class.getResource("/org/netbeans/modules/i18n/i18nAction.gif")); // NOI18N
                }
                i18nMode.dockInto(topComponent);
            }
        }
        
        // Hook for setting target data object for resource bundle panel.
        i18nPanel.getResourceBundlePanel().setTargetDataObject(targetDataObject);
    }
    
    /** Shows dialog. In our case it is a top component. */
    private void showDialog() {
        topComponent.open();
        topComponent.requestFocus();
    }
    
    /** Closes dialog. In our case it is a top component. */
    private void closeDialog() {
        if (topComponent != null) {
            topComponent.close();
            
            topComponent = null;
            i18nPanel = null;
        }
        getI18nInfo().cleanFormValues();
    }
    
    /** Fills values presented in internationalize dialog. */
    private void fillDialogValues() {
        ResourceBundleString oldRbString = i18nPanel.getResourceBundlePanel().getResourceBundleString();
        PropertiesDataObject pdo = (oldRbString == null) ? null : i18nPanel.getResourceBundlePanel().getResourceBundleString().getResourceBundle();
        ResourceBundleString newRbString = getI18nInfo().getDefaultBundleString(pdo);
        // Passes old field element from previous search if exist.
        if(oldRbString != null)
            newRbString.setIdentifier(oldRbString.getIdentifier());
        i18nPanel.setResourceBundleString(newRbString);
        i18nPanel.setI18nInfo(getI18nInfo());
    }
    
    /** Cretaes collection of properties of form which are value type of String.class
     * and could have null value (it saves time to determine the cases the value was changed).
     * Collection is referenced to formProperties variable.
     * @return True if sorted collection was created. */
    private synchronized boolean createFormProperties() {
        if(!(targetDataObject instanceof FormDataObject))
            // is not form
            return false;
        
        // creates new collection
        formProperties = new TreeSet(new ValidFormPropertyComparator());
        updateFormProperties();
        
        return true;
    }
    
    /** Updates collection in formProperties variable. */
    private synchronized void updateFormProperties() {
        
        if(formProperties == null)
            return;
        
        // all components in current FormDataObject
        Collection c = ((FormDataObject)targetDataObject).getFormEditor().getFormManager().getAllComponents();
        Iterator it = c.iterator();
        
        // search thru all RADComponents in the form
        while(it.hasNext()) {
            RADComponent radComponent = (RADComponent)it.next();
            Node.PropertySet[] propSets = radComponent.getProperties();
            
            // go thru properties sets
            for(int i=0; i<propSets.length; i++) {
                String setName = propSets[i].getName();
                // go just thru these property sets
                if(setName != "properties" && setName != "synthetic") // NOI18N
                    continue;
                Node.Property[] properties = propSets[i].getProperties();
                
                // go thru properties in sets
                for(int j=0; j<properties.length; j++) {
                    Node.Property property = properties[j];
                    
                    // skip hidden properties
                    if(property.isHidden())
                        continue;
                    
                    // get value
                    Object value;
                    try {
                        value = property.getValue();
                    } catch(IllegalAccessException iae) {
                        continue; // next property
                    } catch(InvocationTargetException ite) {
                        continue; // next property
                    }
                    
                    // Property have to have "value type" of String (don't confuse with the type of object referred by value variable!!) and is not null.
                    if(property.getValueType().equals(String.class)) {
                        // actually add the property to the list
                        // Note: add only ValidFormProperty instances
                        formProperties.add(new ValidFormProperty(radComponent, property));
                    }
                }
            }
        }
    }
    
    /** Analyzes the text in a guraded block, tries to find the name
     *  of the component and of the property which value matches
     *  with just found hardcoded string.
     */
    private synchronized boolean findInForm() {
        // must be in guarded block (returns true -> hardstring found already)
        if (!(getI18nInfo().isGuarded()))
            return true;
        
        // must be a form (returns true -> hardstring found already)
        if (!(targetDataObject instanceof FormDataObject))
            return true;
        
        boolean found = false;
        
        String hardString = getI18nInfo().getHardString();
        
        // if skip operation on previous found property was performed, increment count of skips
        if(skipped && validProp != null) {
            validProp.incrementSkip();
        }
        
        Iterator it  = formProperties.iterator();
        while(it.hasNext()) {
            validProp = (ValidFormProperty)it.next();
            Node.Property property = validProp.getProperty();
            RADComponent radComp = validProp.getRADComponent();
            // get value
            Object value;
            try {
                value = property.getValue();
            } catch(IllegalAccessException iae) {
                continue; // next property
            } catch(InvocationTargetException ite) {
                continue; // next property
            }
            
            // property have to have "value type" of String (don't confuse with the type of object referred by value variable!!) and not be null
            if(property.getValueType().equals(String.class) && value != null) {
                String string;
                if(property instanceof RADProperty) {
                    // RADProperty, the value could be constructed from one of PropertyEditors
                    if(value instanceof ResourceBundleStringForm) {
                        // resource bundle value, do not replace, is internationalized already !!
                        continue; // next property
                    } else if (value instanceof RADConnectionDesignValue) {
                        // is Form connection value
                        string = ""; // NOI18N
                        RADConnectionDesignValue connectionValue = (RADConnectionDesignValue)value;
                        if(connectionValue.getType() == RADConnectionDesignValue.TYPE_VALUE) {
                            // is type of VALUE
                            string = (String)connectionValue.getDesignValue(radComp);
                            if(validProp.getSkip()==0 && string.equals(hardString))
                                found = true;
                        } else if (connectionValue.getType() == RADConnectionDesignValue.TYPE_CODE) {
                            // is type of USER_CODE
                            string = (String)connectionValue.getDesignValue(radComp);
                            
                            if(indexOfNonI18nString(string, hardString, validProp.getSkip()) != -1)
                                found = true;
                        }
                    } else {
                        // should be plain String, hope no other Property Editors for String RAD Property
                        string = (String)value;
                        if(validProp.getSkip()==0 && string.equals(hardString))
                            found = true;
                    }
                } else {
                    // Node.Property, the value should be plain String
                    string = (String)value;
                    if(indexOfNonI18nString(string, hardString, validProp.getSkip()) != -1) {
                        // non-internationalized hardString found.
                        found = true;
                    }
                }
            }
            if(found) {
                getI18nInfo().setNodeProperty(property);
                getI18nInfo().setComponentName(radComp.getName());
                getI18nInfo().setPropertyName(property.getName());
                
                break;
            }
        } // end of while
        return found;
    }
    
    /** Helper method, which finds (skip+1)th occurence of non-internationalized string in source string.
     * @param source Source string
     * @param hardString Hard coded string to find (note is without double quotes)
     * @param skip How many hard coded strings have to be skipped.
     * @return Index of found hardString or -1 if not found
     */
    private int indexOfNonI18nString (String source, String hardString, int skip) {
        // find firts part of old value which matches hardstring (with double quotes)
        // and is not internationalized already
        String quotedHardString = new String("\""+hardString+"\""); // NOI18N
        int oldIndex=0;
        int newIndex;
        while( (newIndex=source.indexOf(quotedHardString, oldIndex))!= -1) {
            // very ugly trick to cheat out regexp, should be repaired (only last " char is valid for our purpose)
            String string = new String(source.substring(oldIndex, newIndex).replace('\"', '_')+"\""); // NOI18N
            if(getI18nFinder().lastJavaStringNotI18n(string)) {
                if(--skip < 0)
                    return newIndex;
            }
            oldIndex = newIndex + quotedHardString.length();
        }
        return -1;
    }
    
    
    /** Inner class providing information about i18n parameters
     * used in I18nPanel.
     */
    public class I18nInfo {
        
        private ResourceBundleStringEditor     rbStringEditor = new ResourceBundleStringEditor ();
        private ResourceBundleStringFormEditor rbStringFormEditor = new ResourceBundleStringFormEditor ();
        
        /** Holds value of property hardString. */
        private String hardString;
        
        /** Holds value of property hardLine. */
        private String hardLine;
        
        /** Holds value of property guarded. */
        private boolean guarded;
        
        /** Holds the position where hardcored string was found. */
        private Position position;
        
        /** Holds the lenght of the hardcored string. */
        private int len;
        
        /** Holds name of component which property has hardcoded string. */
        private String componentName = ""; // NOI18N
        
        /** Holds name of property with found hardcoded string */
        private String propertyName = ""; // NOI18N
        
        /** Holds property with found hardcoded string */
        private Node.Property nodeProperty;
        
        /** Getter for property hardString.
         *@return Value of property hardString.
         */
        public String getHardString() {
            return hardString;
        }
        
        /** Setter for property hardString.
         *@param hardString New value of property hardString.
         */
        public void setHardString(String hardString) {
            this.hardString = hardString;
        }
        
        /** Getter for property hardLine.
         *@return Value of property hardLine.
         */
        public String getHardLine() {
            return hardLine;
        }
        
        /** Setter for property hardLine.
         *@param hardLine New value of property hardLine.
         */
        public void setHardLine(String hardLine) {
            this.hardLine = hardLine;
        }
        
        /** Getter for property position.
         *@return Value of property position.
         */
        public Position getPosition() {
            return position;
        }
        
        /** Setter for property position.
         *@param position New value of property position.
         */
        public void setPosition(Position position) {
            this.position = position;
        }
        
        /** Getter for property lenght.
         *@return Value of property length.
         */
        public int getLength() {
            return len;
        }
        
        /** Setter for property length.
         *@param position New value of property length.
         */
        public void setLength(int len) {
            this.len = len;
        }
        
        /** Getter for property guarded.
         *@return Value of property guarded.
         */
        public boolean isGuarded() {
            return guarded;
        }
        
        /** Setter for property guarded.
         *@param guarded New value of property guarded. */
        public void setGuarded(boolean guarded) {
            this.guarded = guarded;
        }
        
        /** Setter for property componentName. */
        public void setComponentName(String name) {
            componentName = name;
        }
        
        /** Getter for property componentName.
         * @return Value of property componentName. */
        public String getComponentName() {
            return componentName;
        }
        
        /** Setter for property componentName. */
        public void setPropertyName(String name) {
            propertyName = name;
        }
        
        /** Getter for property propertyName.
         *@return Value of property propertyName. */
        public String getPropertyName() {
            return propertyName;
        }
        
        /** Setter for property componentName. */
        public void setNodeProperty(Node.Property prop) {
            nodeProperty = prop;
        }
        
        /** Getter for property nodeProperty. */
        public Node.Property getNodeProperty() {
            return nodeProperty;
        }
        
        /** Cleans values RAD Property used in Info panel. */
        public void cleanFormValues() {
            nodeProperty = null;
            componentName = ""; // NOI18N
            propertyName = ""; // NOI18N
        }
        
        /** Gets the default bundle string given a PropertiesDataObject. May return null if the value
         * can not be replaced (i.e. is in a guraded block and a form property has not been found). */
        private ResourceBundleString getDefaultBundleString(PropertiesDataObject pdo) {
            if (isGuarded() && targetDataObject instanceof FormDataObject) {
                // guarded block in form
                Node.Property  prop = getI18nInfo().getNodeProperty();
                if (prop != null) {
                    // form
                    if (prop instanceof RADProperty) { // RADProperty
                        ResourceBundleStringForm rbStringForm = new ResourceBundleStringForm();
                        rbStringForm.setResourceBundle(pdo);
                        rbStringForm.setDefaultValue(hardString);
                        
                        rbStringFormEditor.setRADComponent(((RADProperty)prop).getRADComponent(), (RADProperty)prop);
                        rbStringFormEditor.setValue(rbStringForm);
                        return (ResourceBundleStringForm)rbStringFormEditor.getValue();
                    } else { // Node.Property
                        ResourceBundleString rbString;
                        if (pdo == null) {
                            rbStringEditor.setValue(null);
                            rbString = (ResourceBundleString)rbStringEditor.getValue();
                        }
                        else {
                            rbString = new ResourceBundleString();
                            rbString.setResourceBundle(pdo);
                        }
                        setDefaultKey(rbString);
                        rbString.setDefaultValue(hardString);
                        return new ResourceBundleStringForm(rbString);
                    }
                } else {
                    // not found
                    ResourceBundleString.InvalidResourceBundleString invalidRbString = new ResourceBundleString.InvalidResourceBundleString();
                    invalidRbString.setResourceBundle(pdo);
                    return invalidRbString;
                }
            } else {
                // not guarded block in form or guarded in BeanInfo
                ResourceBundleString rbString;
                if (pdo == null) {
                    rbStringEditor.setValue(null);
                    rbString = (ResourceBundleString)rbStringEditor.getValue();
                }
                else {
                    rbString = new ResourceBundleString();
                    rbString.setResourceBundle(pdo);
                }
                setDefaultKey(rbString);
                rbString.setDefaultValue(hardString);
                return rbString;
            }
        }

        /** Sets default key value. */
        private void setDefaultKey(ResourceBundleString rbString) {
            String baseKey = Util.stringToKey(hardString);
            int index = 0;
            rbString.setKey(baseKey);
            while (rbString.getExistingValue() != null) {
                index ++;
                rbString.setKey(baseKey + "." + index); // NOI18N
            }
        }
        
    } // end of I18nInfo inner class
    
    /** Inner class for holding info about form proeprties which can include hardcoded string.
     * see formProperties variable in enclosing class. */
    private class ValidFormProperty {
        /** Holds property of form. */
        private Node.Property property;
        /** Holds RAD component which belongs the property to. */
        private RADComponent radComponent;
        
        /** How many occurences of found string should be skipped in this property.
         * 0 means find the first occurence.
         * All this just means that the property (mostly 'code generation-> pre-init, post-init etc.' properties)
         * could contain more than one occurence of found string
         * and in that case is very important to match and replace the same found in document. */
        private int skip;
        
        /** Constructor. */
        public ValidFormProperty(RADComponent radComponent, Node.Property property) {
            this.radComponent = radComponent;
            this.property  = property;
            this.skip      = 0;
        }
        
        /** Getter for RAD component.
         * @return component which can contain property with hard-coded string */
        public RADComponent getRADComponent() {
            return radComponent;
        }
        
        /** Getter for property.
         * @return property can contain hard-coded string */
        public Node.Property getProperty() {
            return property;
        }
        
        /** Getter for skip.
         * @return amount of occurences of hard-coded string to skip */
        public int getSkip() {
            return skip;
        }
        
        /** Increment the amount of occurences to skip. */
        public void incrementSkip() {
            skip++;
        }
    } // end of ValidFormProperty inner class
    
    /** Helper inner class for formProperties variable in enclosing class.
     * Provides sorting of ValidPropertyComparator classes with intenetion to get the order of
     * properties to match order like they are generated to initComponents form guarded block.
     * It has four stages of comparing two properies.
     * 1) the property which belongs to creation block (preCreationCode, customCreationCode, postCreationCode)
     *   is less (will be generated sooner) then property which is from init block(other names).
     * 2) than the property which component was added to form sooner is less then property which component was
     *   added later. (Top-level component is the least one.)
     * 3) than a) creation block: preCreationCode < (is less) customCreationCode < postCreationCode
     *         b) init block: preInitCode < set-method-properties < postInitCode
     * 4) than (for init block only) in case of set-method-properties. The property is less which has less index in
     *   array returned by method getAllProperties on component.
     * */
    private class ValidFormPropertyComparator implements Comparator {
        
        private static final String CREATION_CODE_PRE    = "creationCodePre"; // NOI18N
        private static final String CREATION_CODE_CUSTOM = "creationCodeCustom"; // NOI18N
        private static final String CREATION_CODE_POST   = "creationCodePost"; // NOI18N
        
        private static final String INIT_CODE_PRE  = "initCodePre"; // NOI18N
        private static final String INIT_CODE_POST = "initCodePost"; // NOI18N
        
        /** Array of all components in current FormDataObject */
        private final Object[] components = ((FormDataObject)targetDataObject).getFormEditor().getFormManager().getAllComponents().toArray();
        
        
        public int compare(Object o1, Object o2) {
            // 1st stage
            String propName1 = ((ValidFormProperty)o1).getProperty().getName();
            String propName2 = ((ValidFormProperty)o2).getProperty().getName();
            
            boolean isInCreation1 = false;
            boolean isInCreation2 = false;
            
            if(propName1.equals(CREATION_CODE_PRE) || propName1.equals(CREATION_CODE_CUSTOM) || propName1.equals(CREATION_CODE_POST))
                isInCreation1 = true;
            
            if(propName2.equals(CREATION_CODE_PRE) || propName2.equals(CREATION_CODE_CUSTOM) || propName2.equals(CREATION_CODE_POST))
                isInCreation2 = true;
            
            if(isInCreation1 != isInCreation2)
                return isInCreation1 ? -1 : 1; // end of 1st stage
                
                // 2nd stage
                RADComponent comp1 = ((ValidFormProperty)o1).getRADComponent();
                RADComponent comp2 = ((ValidFormProperty)o2).getRADComponent();
                
                int index1 = -1;
                int index2 = -1;
                
                if(!comp1.equals(comp2)) {
                    for(int i=0; i<components.length; i++) {
                        if(comp1.equals(components[i]))
                            index1 = i;
                        
                        if(comp2.equals(components[i]))
                            index2 = i;
                        
                        if(index1!=-1 && index2!=-1)
                            break;
                    }
                    return index1 - index2;
                } // end of 2nd stage
                
                // 3rd stage
                if(isInCreation1) {
                    // 3a) stage
                    index1 = -1;
                    index2 = -1;
                    
                    if(propName1.equals(CREATION_CODE_PRE)) index1 = 0;
                    else if(propName1.equals(CREATION_CODE_CUSTOM)) index1 = 1;
                    else if(propName1.equals(CREATION_CODE_POST)) index1 = 2;
                    
                    if(propName2.equals(CREATION_CODE_PRE)) index2 = 0;
                    else if(propName2.equals(CREATION_CODE_CUSTOM)) index2 = 1;
                    else if(propName2.equals(CREATION_CODE_POST)) index2 = 2;
                    
                    return index1 - index2; // end of 3a) stage
                } else {
                    // 3b) stage
                    index1 = -1;
                    index2 = -1;
                    
                    if(propName1.equals(INIT_CODE_PRE)) index1 = 0;
                    else if(propName1.equals(INIT_CODE_POST)) index1 = 2;
                    else index1 = 1; // is one of set-method property
                    
                    if(propName2.equals(INIT_CODE_PRE)) index2 = 0;
                    else if(propName2.equals(INIT_CODE_POST)) index2 = 2;
                    else index2 = 1; // is one of set-method property
                    
                    if (index1 != 1 || index2 != 1)
                        return index1 - index2; // end of 3b) stage
                } // end of 3rd stage
                
                // 4th stage
                Node.PropertySet[] propSets = comp1.getProperties();
                Object[] properties = new Node.Property[0];
                
                ArrayList aList = new ArrayList();
                for(int i=0; i<propSets.length; i++) {
                    if(propSets[i].getName().equals("properties") // NOI18N
                    || propSets[i].getName().equals("synthetic")) { // NOI18N
                        aList.addAll(Arrays.asList(propSets[i].getProperties()));
                    }
                }
                properties = aList.toArray();
                
                index1 = -1;
                index2 = -1;
                
                Node.Property prop1 = ((ValidFormProperty)o1).getProperty();
                Node.Property prop2 = ((ValidFormProperty)o2).getProperty();
                
                for(int i=0; i<properties.length; i++) {
                    if(prop1.equals(properties[i]))
                        index1 = i;
                    
                    if(prop2.equals(properties[i]))
                        index2 = i;
                    
                    if(index1!=-1 && index2!=-1)
                        break;
                }
                
                return index1 - index2; // end of 4th stage
        } // end of compare method
        
        public boolean equals(Object obj) {
            return equals(obj);
        }
    } // End of ValidFormPropertyCompoarator inner class.
    
}