/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.editor.completion;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;

/**
 *  HTML documentation view. 
 *  Javadoc content is displayed in JEditorPane pane using HTMLEditorKit.
 *
 *  @author  Martin Roskanin
 *  @since   03/2002
 */
public class HTMLDocView extends JEditorPane {
    
    private HTMLEditorKit htmlKit;
    
    /** Creates a new instance of HTMLJavaDocView */
    public HTMLDocView(Color bgColor) {
        setEditable(false);
        setBackground(bgColor);
        setMargin(new Insets(0,3,3,3));
    }

    /** Sets the javadoc content as HTML document */
    public void setContent(final String content) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                Reader in = new StringReader("<HTML><BODY>"+content+"</BODY></HTML>");//NOI18N                
                try{
                    Document doc = getDocument();
                    doc.remove(0, doc.getLength());
                    getEditorKit().read(in, getDocument(), 0);  //!!! still too expensive to be called from AWT
                    setCaretPosition(0);
                    scrollRectToVisible(new Rectangle(0,0,0,0));            
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }catch(BadLocationException ble){
                    ble.printStackTrace();
                }
            }
        });
    }
    
    protected EditorKit createDefaultEditorKit() {
        // it is extremelly slow to init it
        if (htmlKit == null){
            htmlKit= new HTMLEditorKit ();
            setEditorKit(htmlKit);

            // override the Swing default CSS to make the HTMLEditorKit use the
            // same font as the rest of the UI.
            
            // XXX the style sheet is shared by all HTMLEditorKits.  We must
            // detect if it has been tweaked by ourselves or someone else
            // (template description for example) and avoid doing the same
            // thing again
            
            if (htmlKit.getStyleSheet().getStyleSheets() != null)
                return htmlKit;
            
            javax.swing.text.html.StyleSheet css = new javax.swing.text.html.StyleSheet();
            java.awt.Font f = getFont();
            css.addRule(new StringBuffer("body { font-size: ").append(f.getSize()) // NOI18N
                        .append("; font-family: ").append(f.getName()).append("; }").toString()); // NOI18N
            css.addStyleSheet(htmlKit.getStyleSheet());
            htmlKit.setStyleSheet(css);
        }
        return htmlKit;
    }
}
