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

package org.netbeans.modules.xml.text.completion;

import java.awt.Color;
import java.io.IOException;

import javax.swing.text.*;
import javax.swing.Icon;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;
import org.netbeans.editor.Utilities;
import javax.swing.JLabel;

/** 
 * This class carries result information required by NetBeans Editor module.
 *
 * @author  Petr Kuzel
 * @author  Sandeep Randhawa
 */
class XMLResultItem implements CompletionQuery.ResultItem {
    
    // text to be diplayed to user
    public String displayText;
    private String replacementText;

    // icon to be diplayed
    public javax.swing.Icon icon;

    public Color foreground = Color.black;
    public Color background = Color.white;
    public Color selectionForeground = Color.black;
    public Color selectionBackground = new Color(204, 204, 255);
    
    private static JLabel rubberStamp = new JLabel();
    static {
        rubberStamp.setOpaque( true );
    }

    /**
     *
     * @param replacementText replacement text that is used as display name too
     */
    public XMLResultItem(String replacementText){
        this(replacementText, null);
    }

    /**
     * @param displayText text to display or null if replacementText is OK
     */
    public XMLResultItem(String replacementText, String displayText) {
        this.replacementText = replacementText;
        this.displayText = displayText != null ? displayText : replacementText;
    }

    /** Creates new XMLResultItem
     * @param displayText The string value that will be displayed in the completion window and will hence
     * be the replacement text if selected.
     * @param icon The icon that will be displayed for this element.
     * @param foreground The foreground color of the text
     * @param background The background color of the text
     * @param selectionForeground The foreground color of the selected text
     * @param selectionBackground The background color of the selected text
     */
    public XMLResultItem(String displayText, javax.swing.Icon icon, Color foreground, Color background, Color selectionForeground, Color selectionBackground) {
        this.displayText = displayText;
        this.icon = icon;
        this.foreground = foreground;
        this.background = background;
        this.selectionForeground = selectionForeground;
        this.selectionBackground = selectionBackground;
    }

    /**
     * Insert following text into document.
     */
    public String getReplacementText(int modifiers){
        return displayText;
    }
    

    protected Icon getIcon(){
        return icon;
    }
    
    /**
     * Actually replaces a piece of document by passes text.
     * @param component a document source
     * @param text a string to be inserted
     * @param offset the target offset
     * @param len a length that should be removed before inserting text
     */
    boolean replaceText( JTextComponent component, String text, int offset, int len) {
        BaseDocument doc = (BaseDocument)component.getDocument();
        doc.atomicLock();
        try {
            doc.remove( offset, len );
            doc.insertString( offset, text, null);
            //reformat the line
            ((ExtFormatter)doc.getFormatter()).reformat(doc, Utilities.getRowStart(doc, offset), offset+text.length(), true);
        } catch( BadLocationException exc ) {
            return false;    //not sucessfull
        } catch (IOException e) {
            return false;
        } finally {
            doc.atomicUnlock();
        }
        return true;
    }

    public boolean substituteCommonText( JTextComponent c, int offset, int len, int subLen ) {
        return replaceText( c, getReplacementText(0).substring( 0, subLen ), offset, len );
    }

    /**
     * Just translate <code>shift</code> to proper modifier
     */
    public final boolean substituteText( JTextComponent c, int offset, int len, boolean shift ) {
        int modifier = shift ? java.awt.event.InputEvent.SHIFT_MASK : 0;
        return substituteText(c, offset, len, modifier);
    }

    public boolean substituteText( JTextComponent c, int offset, int len, int modifiers ){
        return replaceText(c, getReplacementText(modifiers), offset, len);
    }

    /** @return Properly colored JLabel with text gotten from <CODE>getPaintText()</CODE>. */
    public java.awt.Component getPaintComponent( javax.swing.JList list, boolean isSelected, boolean cellHasFocus ) {
        // The space is prepended to avoid interpretation as HTML Label
        if (getIcon() != null) rubberStamp.setIcon(getIcon());

        rubberStamp.setText( displayText );
        if (isSelected) {
            rubberStamp.setBackground(selectionBackground);
            rubberStamp.setForeground(selectionForeground);
        } else {
            rubberStamp.setBackground(background);
            rubberStamp.setForeground(foreground);
        }
        return rubberStamp;
    }

    public final String getItemText() {
        return replacementText;
    }
    
    public String toString() {
        return getItemText();
    }
}
