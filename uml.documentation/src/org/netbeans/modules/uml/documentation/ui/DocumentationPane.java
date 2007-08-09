/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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


package org.netbeans.modules.uml.documentation.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


public class DocumentationPane extends JPanel
        implements KeyListener, FocusListener, DocumentListener
{
    private DocUndoableEditListener undoableEditListner = new DocUndoableEditListener();
    private boolean displaySave = true;
    
    private JTextPane m_TextPane = null;
    private HTMLEditorKit htmlKit = null;
    private HTMLDocument htmlDoc = null;
    
    private JToolBar toolbar;
    private StyledEditorKit.BoldAction boldAction;
    private StyledEditorKit.ItalicAction italicAction;
    private StyledEditorKit.UnderlineAction underlineAction;
    private ColorAction colorAction;
    private StyledEditorKit.AlignmentAction leftAction;
    private StyledEditorKit.AlignmentAction centerAction;
    private StyledEditorKit.AlignmentAction rightAction;
    private UndoManager undoMgr;
    private UndoAction undoAction;
    private RedoAction redoAction;
    private SaveAction saveAction;
    
    public static final String PROP_DIRTY = "dirty_state";
    
    
    public DocumentationPane()
    {
        this(true);
    }
    
    public DocumentationPane(boolean display)
    {
        this.displaySave = display;
        init();
    }
    
    
    private void init()
    {
        m_TextPane = new JTextPane();
        htmlKit = new DocumentationEditorKit();
        htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
        Cursor textCur = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
        htmlKit.setDefaultCursor(textCur);
        m_TextPane.setCursor(textCur);
        
        /* Set up the text pane */
        m_TextPane.setEditorKit(htmlKit);
        m_TextPane.setDocument(htmlDoc);
        m_TextPane.setMargin(new Insets(5,5,5,5));
        m_TextPane.addKeyListener(this);
        m_TextPane.addFocusListener(this);
        
        m_TextPane.getStyledDocument().addDocumentListener(this);
        
        setLayout(new BorderLayout());
        JScrollPane pane = new JScrollPane(m_TextPane);
        add(pane, BorderLayout.CENTER);
        add(createToolBar(), BorderLayout.NORTH);
        
        m_TextPane.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(DocumentationPane.class,"ACDS_EDITOR"));
        m_TextPane.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(DocumentationPane.class,"ACNS_EDITOR"));
        
        /* Set up the undo features */
        undoMgr = new UndoManager();
        undoAction = new UndoAction();
        redoAction = new RedoAction();
        
        m_TextPane.getStyledDocument().addDocumentListener(this);
        m_TextPane.getDocument().addUndoableEditListener(undoableEditListner);
        //        m_TextPane.setText("<HTML><BODY></BODY></HTML>");
        m_TextPane.setCaretPosition(0);
        
        registerAccelerator();
        getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(DocumentationPane.class,"ACDS_DOCUMENTATION"));
        
        setEnabled(false);
    }
    
    
    protected JTextPane getTextPane()
    {
        return m_TextPane;
    }
    
    
    public HTMLEditorKit getEditorKit()
    {
        return htmlKit;
    }
    
    
    public void purgeUndos()
    {
        if(undoMgr != null)
        {
            undoMgr.discardAllEdits();
            undoAction.updateState();
            redoAction.updateState();
        }
    }
    
    
    
    private JToolBar createToolBar()
    {
        
        toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        toolbar.setAlignmentX(JToolBar.LEFT_ALIGNMENT);
        
        boldAction        = new StyledEditorKit.BoldAction();
        italicAction      = new StyledEditorKit.ItalicAction();
        underlineAction   = new StyledEditorKit.UnderlineAction();
        leftAction        = new StyledEditorKit.AlignmentAction(NbBundle.getMessage(DocumentationPane.class, "IDS_ALIGNLEFT"), StyleConstants.ALIGN_LEFT);
        centerAction      = new StyledEditorKit.AlignmentAction(NbBundle.getMessage(DocumentationPane.class, "IDS_CENTER"), StyleConstants.ALIGN_CENTER);
        rightAction       = new StyledEditorKit.AlignmentAction(NbBundle.getMessage(DocumentationPane.class, "IDS_ALIGNRIGHT"), StyleConstants.ALIGN_RIGHT);
        colorAction       = new ColorAction();
        saveAction        = new SaveAction();
        
        if (displaySave)
        {
            JButton saveBtn = new JButton(saveAction);
            saveBtn.setRequestFocusEnabled(false);
            saveBtn.setIcon(ImageUtil.instance().getIcon("save.png"));
            saveBtn.setText(null);
            saveBtn.setToolTipText(NbBundle.getMessage(DocumentationPane.class, "IDS_SAVE"));
            saveBtn.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DocumentationPane.class, "ACNS_SAVE"));
            toolbar.add(saveBtn);
            
            toolbar.addSeparator();
        }
        
        JButton boldBtn = new JButton(boldAction);
        boldBtn.setRequestFocusEnabled(false);
        boldBtn.setIcon(ImageUtil.instance().getIcon("bold.png"));
        boldBtn.setText(null);
        boldBtn.setToolTipText(NbBundle.getMessage(DocumentationPane.class, "IDS_BOLD"));
        boldBtn.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DocumentationPane.class, "ACNS_BOLD"));
        toolbar.add(boldBtn);
        
        JButton italicBtn = new JButton(italicAction);
        italicBtn.setRequestFocusEnabled(false);
        italicBtn.setIcon(ImageUtil.instance().getIcon("italics.png"));
        italicBtn.setText(null);
        italicBtn.setToolTipText(NbBundle.getMessage(DocumentationPane.class, "IDS_ITALIC"));
        italicBtn.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DocumentationPane.class, "ACNS_ITALIC"));
        toolbar.add(italicBtn);
        
        JButton underlineBtn = new JButton(underlineAction);
        underlineBtn.setRequestFocusEnabled(false);
        underlineBtn.setIcon(ImageUtil.instance().getIcon("underline.png"));
        underlineBtn.setText(null);
        underlineBtn.setToolTipText(NbBundle.getMessage(DocumentationPane.class, "IDS_UNDER"));
        underlineBtn.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DocumentationPane.class,"ACNS_UNDER"));
        toolbar.add(underlineBtn);
        
        JButton colorBtn = toolbar.add(colorAction);
        colorBtn.setRequestFocusEnabled(false);
        colorBtn.setToolTipText(NbBundle.getMessage(DocumentationPane.class, "IDS_FONTCOLOR"));
        colorBtn.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DocumentationPane.class, "ACNS_FONTCOLOR"));
        
        toolbar.addSeparator();
        
        JButton leftBtn = new JButton(leftAction);
        leftBtn.setRequestFocusEnabled(false);
        leftBtn.setIcon(ImageUtil.instance().getIcon("align-left-text.png"));
        leftBtn.setText(null);
        leftBtn.setToolTipText(NbBundle.getMessage(DocumentationPane.class, "IDS_ALIGNLEFT"));
        leftBtn.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DocumentationPane.class, "ACNS_ALIGNLEFT"));
        toolbar.add(leftBtn);
        
        JButton centerBtn = new JButton(centerAction);
        centerBtn.setRequestFocusEnabled(false);
        centerBtn.setIcon(ImageUtil.instance().getIcon("align-center-text.png"));
        centerBtn.setText(null);
        centerBtn.setToolTipText(NbBundle.getMessage(DocumentationPane.class, "IDS_CENTER"));
        centerBtn.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DocumentationPane.class, "ACNS_CENTER"));
        toolbar.add(centerBtn);
        
        JButton rightBtn = new JButton(rightAction);
        rightBtn.setRequestFocusEnabled(false);
        rightBtn.setIcon(ImageUtil.instance().getIcon("align-right-text.png"));
        rightBtn.setText(null);
        rightBtn.setToolTipText(NbBundle.getMessage(DocumentationPane.class, "IDS_ALIGNRIGHT"));
        rightBtn.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DocumentationPane.class, "ACNS_ALIGNRIGHT"));
        toolbar.add(rightBtn);
        
        return toolbar;
    }
    
    private void registerAccelerator()
    {
        registerKeyboardAction(boldAction, KeyStroke.getKeyStroke('B', KeyEvent.CTRL_MASK, false),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        registerKeyboardAction(italicAction, KeyStroke.getKeyStroke('I', KeyEvent.CTRL_MASK, false),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        registerKeyboardAction(underlineAction, KeyStroke.getKeyStroke('U', KeyEvent.CTRL_MASK, false),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        registerKeyboardAction(colorAction, KeyStroke.getKeyStroke('A', KeyEvent.CTRL_MASK + KeyEvent.ALT_MASK, false),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        registerKeyboardAction(leftAction, KeyStroke.getKeyStroke('L', KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK, false),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        registerKeyboardAction(centerAction, KeyStroke.getKeyStroke('C', KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK, false),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        registerKeyboardAction(rightAction, KeyStroke.getKeyStroke('R', KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK, false),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        
        registerKeyboardAction(undoAction, KeyStroke.getKeyStroke('Z', KeyEvent.CTRL_MASK, false),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        registerKeyboardAction(redoAction, KeyStroke.getKeyStroke('Y', KeyEvent.CTRL_MASK, false),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        
        registerKeyboardAction(saveAction, KeyStroke.getKeyStroke('S', KeyEvent.CTRL_MASK, false),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
    }
    
    
    /* KeyListener methods */
    public void keyTyped(KeyEvent ke)
    {
        if(ke.getKeyChar() == KeyEvent.VK_ENTER)
        {
            // do not add <br> line break for pre-format text
            AttributeSet set = getTextPane().getParagraphAttributes();
            for(Enumeration e = set.getAttributeNames(); e.hasMoreElements();)
            {
                Object key = e.nextElement();
                Object val = set.getAttribute(key);
                if (val.equals("pre") || val == HTML.Tag.PRE)
                    return;
            }
            
            try
            {
                insertBreak();
            }
            catch (Exception e)
            {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    public void keyPressed(KeyEvent e)
    {
    }
    
    public void keyReleased(KeyEvent e)
    {
    }
    
    /* FocusListener methods */
    public void focusGained(FocusEvent fe)
    {
        // fixed 111959.
        // Need to call selectAll() to get the end position of the text,
        // then set the caret to that position.
        m_TextPane.selectAll();
        int caretPos = m_TextPane.getSelectionEnd();
        String selectedText = m_TextPane.getSelectedText();
        if (selectedText != null && selectedText.trim().length() > 0 ) {
            m_TextPane.setCaretPosition(caretPos);
        }
    }
    
    public void focusLost(FocusEvent fe)
    {
    }
    
    /* DocumentListener methods */
    public void changedUpdate(DocumentEvent de)
    {
        handleDocumentChange(de);
    }
    
    public void insertUpdate(DocumentEvent de)
    {
        handleDocumentChange(de);
    }
    
    public void removeUpdate(DocumentEvent de)
    {
        handleDocumentChange(de);
    }
    
    public void handleDocumentChange(DocumentEvent de)
    {
        //        dirty = true;
        //        undoMgr.canUndo() || undoMgr.canRedo();
        //        saveAction.setEnabled(dirty);
    }
    
    
    public boolean isDirty()
    {
        return undoMgr.canUndo();
    }
    
    public String getDocumentText()
    {
        return getTextPane().getText();
    }
    
    
    public synchronized void setDocumentText(String sText)
    {
        getTextPane().getDocument().removeDocumentListener(this);
        getTextPane().getDocument().removeUndoableEditListener(undoableEditListner);
        
        getTextPane().removeAll();
        getTextPane().setText(sText);
        getTextPane().getDocument().addDocumentListener(this);
        getTextPane().getDocument().addUndoableEditListener(undoableEditListner);
        getTextPane().setCaretPosition(0);
        Cursor textCur = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
        getEditorKit().setDefaultCursor(textCur);
        purgeUndos();
        
        setEnabled(true);
    }
    
    
    public void setEnabled(boolean enable)
    {
        if (!enable && getTextPane().isEditable())
        {
            getTextPane().getDocument().removeDocumentListener(this);
            getTextPane().getDocument().removeUndoableEditListener(undoableEditListner);
            getTextPane().setText("");
        }
        
        getTextPane().setEditable(enable);
        saveAction.setEnabled(enable && undoMgr.canUndo());
        boldAction.setEnabled(enable);
        italicAction.setEnabled(enable);
        underlineAction.setEnabled(enable);
        colorAction.setEnabled(enable);
        leftAction.setEnabled(enable);
        centerAction.setEnabled(enable);
        rightAction.setEnabled(enable);
        
        super.setEnabled(enable);
    }
    
    
    private void insertBreak()
            throws IOException, BadLocationException, RuntimeException
    {
        int caretPos = getTextPane().getCaretPosition();
        htmlKit.insertHTML(htmlDoc, caretPos, "<BR>", 0, 0, HTML.Tag.BR);
        getTextPane().setCaretPosition(caretPos + 1);
    }
    
    
    private String getSubText(String containingTag)
    {
        
        String docTextCase = getTextPane().getText().toLowerCase();
        int tagStart       = docTextCase.indexOf("<" + containingTag.toLowerCase());
        int tagStartClose  = docTextCase.indexOf(">", tagStart) + 1;
        String closeTag    = "</" + containingTag.toLowerCase() + ">";
        int tagEndOpen     = docTextCase.indexOf(closeTag);
        
        if(tagStartClose < 0)
            tagStartClose = 0;
        
        if(tagEndOpen < 0 || tagEndOpen > docTextCase.length())
            tagEndOpen = docTextCase.length();
        
        return getTextPane().getText().substring(tagStartClose, tagEndOpen);
    }
    
    /** utility to get the document text contained within the BODY tags
     */
    public String getDocumentBody()
    {
        return getSubText("body");
    }
    
    
    
    public String getTrimmedDocumentation()
    {
        String doc = getDocumentBody().trim();
        return removeHTMLComments(doc);
    }
    
    private String removeHTMLComments(String text)
    {
        String doc = text;
        int commentStart       = doc.indexOf("<!--");
        int commentClose  = doc.indexOf("-->", commentStart);
        
        while (commentStart >= 0 && commentClose > commentStart + 4)
        {
            doc = doc.substring(0, commentStart) + doc.substring(commentClose + 3);
            commentStart  = doc.indexOf("<!--");
            commentClose  = doc.indexOf("-->", commentStart);
        }
        
        return doc;
    }
    
    
    /* Inner Classes --------------------------------------------- */
    // they are not currently included in toolbar UI
    
    class UndoAction extends AbstractAction
    {
        public UndoAction()
        {
            super(NbBundle.getMessage(DocumentationPane.class, "Undo"));
            setEnabled(false);
        }
        
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                undoMgr.undo();
            }
            catch(CannotUndoException ex)
            {
                ex.printStackTrace();
            }
            updateState();
            redoAction.updateState();
            saveAction.updateState();
        }
        
        protected void updateState()
        {
            setEnabled(undoMgr.canUndo());
        }
    }
    
    /** Class for implementing Redo as an autonomous action
     */
    class RedoAction extends AbstractAction
    {
        public RedoAction()
        {
            super(NbBundle.getMessage(DocumentationPane.class, "Redo"));
            setEnabled(false);
        }
        
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                undoMgr.redo();
            }
            catch(CannotUndoException ex)
            {
                ex.printStackTrace();
            }
            updateState();
            undoAction.updateState();
            saveAction.updateState();
        }
        
        protected void updateState()
        {
            setEnabled(undoMgr.canRedo());
        }
    }
    
    class DocUndoableEditListener implements UndoableEditListener
    {
        public void undoableEditHappened(UndoableEditEvent e)
        {
            undoMgr.addEdit(e.getEdit());
            undoAction.updateState();
            redoAction.updateState();
            saveAction.setEnabled(undoMgr.canUndo());
        }
    }
    
    
    /** custom color action to bring up standard color chooser
     */
    class ColorAction extends StyledEditorKit.ForegroundAction
    {
        public ColorAction()
        {
            super("", Color.BLACK);
            putValue(Action.SMALL_ICON, ImageUtil.instance().getIcon("color-chooser.png"));
            setToolTipText(NbBundle.getMessage(DocumentationPane.class, "IDS_FONTCOLOR"));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            getTextPane().requestFocus();
            Color c = JColorChooser.showDialog(DocumentationPane.this,
                    NbBundle.getMessage(DocumentationPane.class, "IDS_TITLE"), m_TextPane.getForeground());
            if (c != null)
            {
                MutableAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setForeground(attr, c);
                setCharacterAttributes(getTextPane(), attr, false);
            }
        }
    }
    
    class SaveAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            int pos = getTextPane().getCaretPosition();
            DocumentationPane.this.firePropertyChange(PROP_DIRTY, true, false);
            saveAction.setEnabled(false);
            purgeUndos();
            int length = getTextPane().getDocument().getLength();
            if (pos > length)
                getTextPane().setCaretPosition(length);
            else
                getTextPane().setCaretPosition(pos);
        }
        
        protected void updateState()
        {
            setEnabled(undoMgr.canUndo());
        }
    }
}


