/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.indent;

import java.io.IOException;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.FormatLayer;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.ExtFormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseDocument;

import org.netbeans.modules.xml.text.syntax.javacc.lib.JJEditorSyntax;

/**
 * @author  Libor Kramolis
 * @version 0.1
 */
public class XMLFormatter extends ExtFormatter {
    /** */
    private static final boolean DEBUG = false;

    //
    // init
    //

    /** */
    public XMLFormatter (Class kitClass) {
        super (kitClass);
    }


    //
    // ExtFormatter
    //

    /**
     */
    public FormatSupport createFormatSupport (FormatWriter fw) {
        return new XMLFormatSupport (fw);
    }

    /**
     */
    protected boolean acceptSyntax (Syntax syntax) {
        return (syntax instanceof JJEditorSyntax);
    }

    /**
     */
    protected void initFormatLayers() {
        addFormatLayer (new StripEndWhitespaceLayer());
    }    

   /** Inserts new line at given position and indents the new line with
    * spaces.
    *
    * @param doc the document to work on
    * @param offset the offset of a character on the line
    * @return new offset to place cursor to
    */
    public int indentNewLine (Document doc, int offset) {
        if ( DEBUG ) {
            Util.debug ("\n+ XMLFormatter::indentNewLine: doc = " + doc); // NOI18N
            Util.debug ("+             ::indentNewLine: offset = " + offset); // NOI18N
        }

        if (doc instanceof BaseDocument) {
            BaseDocument bdoc = (BaseDocument)doc;

            bdoc.atomicLock();
            try {
                bdoc.insertString (offset, "\n", null); // NOI18N
                offset++;

                int fullLine = Utilities.getFirstNonWhiteBwd (bdoc, offset - 1);
                int indent = Utilities.getRowIndent (bdoc, fullLine);
                
                if ( DEBUG ) {
                    Util.debug ("+             ::indentNewLine: fullLine = " + fullLine); // NOI18N
                    Util.debug ("+             ::indentNewLine: indent   = " + indent); // NOI18N
                }

                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < indent; i++) {
                    sb.append (' ');
                };

                if ( DEBUG ) {
                    Util.debug ("+             ::indentNewLine: offset   = " + offset); // NOI18N
                    Util.debug ("+             ::indentNewLine: sb       = '" + sb.toString() + "'"); // NOI18N
                }

                bdoc.insertString (offset, sb.toString(), null);
                offset += indent;

                if ( DEBUG ) {
                    Util.debug ("+             ::indentNewLine: offset = " + offset); // NOI18N
                }
            } catch (BadLocationException e) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            } finally {
                bdoc.atomicUnlock();
            }

            return offset;
        }

        return super.indentNewLine (doc, offset);
    }

    
    //
    // class StripEndWhitespaceLayer
    //

    /**
     *
     */
    public class StripEndWhitespaceLayer extends AbstractFormatLayer {
       
        //
        // init
        //

        /** */
        public StripEndWhitespaceLayer() {
            super("xml-strip-whitespace-at-line-end-layer"); // NOI18N
        }
        
        //
        // AbstractFormatLayer
        //

        /**
         */
        protected FormatSupport createFormatSupport (FormatWriter fw) {
            return new XMLFormatSupport (fw);
        }
        
        /**
         */
        public void format (FormatWriter fw) {
            XMLFormatSupport xfs = (XMLFormatSupport)createFormatSupport (fw);
            
            FormatTokenPosition pos = xfs.getFormatStartPosition();
            
            if ( (xfs.isLineStart (pos) == false) ||
                 xfs.isIndentOnly() ) { // don't do anything
                
            } else { // remove end-line whitespace
                while (pos.getToken() != null) {
                    if ( DEBUG ) {
                        Util.debug ("XMLFormatSupport::StripEndWhitespaceLayer::format: position = " + pos); // NOI18N
                    }

                    pos = xfs.removeLineEndWhitespace (pos);
                    if (pos.getToken() != null) {
                        pos = xfs.getNextPosition (pos);
                    }
                }
            }
        }

    } // end: class StripEndWhitespaceLayer

}
