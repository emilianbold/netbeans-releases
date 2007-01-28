/*
 * AutoCompleteComboBox.java
 *
 * Created on March 27, 2005, 9:24 AM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import org.netbeans.modules.visualweb.ejb.load.EjbLoaderHelper;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URLClassLoader;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Provides auto-completion of the given combo box
 *
 * @author  cao
 */
public class ComboBoxAutoCompletion extends PlainDocument {
    private JComboBox comboBox;
    private ComboBoxModel model;
    private JTextComponent editor;
    
    // flag to indicate if setSelectedItem has been called
    // subsequent calls to remove/insertString should be ignored
    private boolean selecting=false;
    private boolean hidePopupOnFocusLoss;
    private boolean hitBackspace=false;
    private boolean hitBackspaceOnSelection;
    private boolean listContainsSelectedItem;
    
    // Flag to indicate if the enter key just pressed
    private boolean enterKeyPressed = false;
    
    private EjbGroup ejbGroup;
    private MethodInfo methodInfo;
    private URLClassLoader classloader;
    
    public ComboBoxAutoCompletion(JComboBox combobox) {
        this.comboBox = combobox;
 
        model = comboBox.getModel();
        editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
        editor.setDocument(this);
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!selecting) 
                    highlightCompletedText(0);
                
                if( methodInfo != null ) 
                {
                    // Only validate if the enter key is pressed
                    if( enterKeyPressed )
                    {
                        validateColElemClassName( (String)comboBox.getSelectedItem() );
                    }
                }
            }
        });
        editor.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (comboBox.isDisplayable()) 
                    comboBox.setPopupVisible(true);
                hitBackspace=false;
                enterKeyPressed=false;
   
                switch (e.getKeyCode()) {
                    // determine if the pressed key is backspace (needed by the remove method)
                    case KeyEvent.VK_BACK_SPACE : hitBackspace=true;
                    hitBackspaceOnSelection=editor.getSelectionStart()!=editor.getSelectionEnd();
                    break;
                    
                    case KeyEvent.VK_ENTER:
                    enterKeyPressed = true;
                    break;
                }
            }
        });
        // Bug 5100422 on Java 1.5: Editable JComboBox won't hide popup when tabbing out
        hidePopupOnFocusLoss=System.getProperty("java.version").startsWith("1.5");
        // Highlight whole text when gaining focus
        editor.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                highlightCompletedText(0);
            }
            public void focusLost(FocusEvent e) {
                // Workaround for Bug 5100422 - Hide Popup on focus loss
                if (hidePopupOnFocusLoss) comboBox.setPopupVisible(false);
            }
        });
        // Handle initially selected object
        Object selected = comboBox.getSelectedItem();
        if (selected!=null) setText(selected.toString());
        highlightCompletedText(0);
    }
    
    public void setEjbGroup( EjbGroup ejbGroup )
    {
        this.ejbGroup = ejbGroup;
    }
    
    public void setMethodInfo( MethodInfo methodInfo )
    {
        this.methodInfo = methodInfo;
    }
    
    public void validateColElemClassName( String className ) {
        
        // Only if the method has return type of Collection
        if( !methodInfo.getReturnType().isCollection() )
            return;
        
        // TODO ignore null or empty for now
        if( className == null || className.trim().length() == 0 )
            return;
        
        if( classloader == null )
            classloader = EjbLoaderHelper.getEjbGroupClassLoader( ejbGroup );
        
        // Make sure that the element class specified by the user is a valid one
        try {
            Class c = Class.forName( className, true,  classloader );
        }catch ( java.lang.ClassNotFoundException ce ) {
            NotifyDescriptor d = new NotifyDescriptor.Message( "Class " + className + " not found", /*NbBundle.getMessage(MethodNode.class, "PARAMETER_NAME_NOT_UNIQUE", name ),*/ NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify( d );
            return;
        }
    }
    
    public void remove(int offs, int len) throws BadLocationException {
        // return immediately when selecting an item
        if (selecting) return;
        if (hitBackspace) {
            if (listContainsSelectedItem) {
                // move the selection backwards
                // old item keeps being selected
                if (offs>0) {
                    if (hitBackspaceOnSelection) offs--;
                } else {
                    // User hit backspace with the cursor positioned on the start => beep
                    comboBox.getToolkit().beep(); // when available use: UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
                }
                highlightCompletedText(offs);
                return;
            } else {
                super.remove(offs, len);
                String currentText = getText(0, getLength());
                // lookup if a matching item exists
                Object item = lookupItem(currentText);
                if (item != null) {
                    //System.out.println("'" + item + "' matches.");
                    setSelectedItem(item);
                    listContainsSelectedItem=true;
                } else {
                    //System.out.println("No match. Selecting '" + currentText + "'.");
                    // no item matches => use the current input as selected item
                    item=currentText;
                    setSelectedItem(item);
                    listContainsSelectedItem=false;
                }
                // display the completed string
                String itemString=item.toString();
                setText(itemString);
                if (listContainsSelectedItem) highlightCompletedText(offs);
            }
        } else {
            super.remove(offs, len);
            setSelectedItem(getText(0, getLength()));
            listContainsSelectedItem=false;
        }
    }
    
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        // return immediately when selecting an item
        if (selecting) return;
        // insert the string into the document
        super.insertString(offs, str, a);
        // lookup and select a matching item
        Object item = lookupItem(getText(0, getLength()));
        
        listContainsSelectedItem = true;
        if (item == null) {
            // no item matches => use the current input as selected item
            item=getText(0, getLength());
            listContainsSelectedItem=false;
        }
        setSelectedItem(item);
        setText(item.toString());
        // select the completed part
        if (listContainsSelectedItem) highlightCompletedText(offs+str.length());
    }
    
    private void setText(String text) {
        try {
            // remove all text and insert the completed string
            super.remove(0, getLength());
            super.insertString(0, text, null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e.toString());
        }
    }
    
    private void highlightCompletedText(int start) {
        editor.setCaretPosition(getLength());
        editor.moveCaretPosition(start);
    }
    
    private void setSelectedItem(Object item) {
        selecting = true;
        model.setSelectedItem(item);
        selecting = false;
    }
    
    private Object lookupItem(String pattern) {
        Object selectedItem = model.getSelectedItem();
        // only search for a different item if the currently selected does not match
        if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern) && listContainsSelectedItem) {
            return selectedItem;
        } else {
            // iterate over all items
            for (int i=0, n=model.getSize(); i < n; i++) {
                Object currentItem = model.getElementAt(i);
                // current item starts with the pattern?
                //if (startsWithIgnoreCase(currentItem.toString(), pattern)) {
                if (currentItem.toString().startsWith(pattern)) { // Case sensitive
                    return currentItem;
                }
            }
        }
        // no item starts with the pattern => return null
        return null;
    }
    
    // checks if str1 starts with str2 - ignores case
    private boolean startsWithIgnoreCase(String str1, String str2) {
        return str1.toUpperCase().startsWith(str2.toUpperCase());
    }
    
    private static void createAndShowGUI() {
        // the combo box (add/modify items if you like to)
        JComboBox comboBox = new JComboBox(new Object[] {"Ester", "Jordi", "Jordina", "Jorge", "Sergi"});
        // has to be editable
        comboBox.setEditable(true);
        // change the editor's document
        new ComboBoxAutoCompletion(comboBox);
        
        // create and show a window containing the combo box
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(3);
        frame.getContentPane().add(comboBox);
        frame.pack(); frame.setVisible(true);
    }
    
    
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
