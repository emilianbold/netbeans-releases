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
package org.netbeans.modules.xml.text.syntax;

import java.util.*;
import java.io.*;

import javax.swing.text.Document;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;

import org.openide.windows.*;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.*;

import org.netbeans.modules.xml.core.lib.EncodingHelper;

/**
 * Editor kit implementation for xml content type.
 * It translates encoding used by document to Unicode encoding.
 * It makes sence for org.epenide.loaders.XMLDataObject that does
 * use default EditorSupport.
 *
 * @author Petr Kuzel
 */
public class UniKit extends NbEditorKit {

    /** Serial Version UID */
    private static final long serialVersionUID = -940485353900594155L;
    
    /** Read document. */
    public void read(InputStream in, Document doc, int pos) throws IOException, BadLocationException {

        // predetect it to get optimalized XmlReader if utf-8
        String enc = EncodingHelper.detectEncoding(in);
        if ( enc == null ) {
            enc = "UTF8"; //!!! // NOI18N
        }
        //    System.err.println("UniKit.reading as " + enc);
        Reader r = new InputStreamReader(in, enc);
        super.read(r, doc, pos);

    }

    /** Hope that it is called by knowing (encoding).
    */  
    public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
        super.read(in, doc, pos);
    }

    /** Write document. */
    public void write(OutputStream out, Document doc, int pos, int len) throws IOException, BadLocationException {
        String enc = EncodingHelper.detectEncoding(doc);
        if ( enc == null ) {
            enc = "UTF8"; //!!! // NOI18N
        }
        //    System.err.println("UniKit.writing as " + enc);
        super.write( new OutputStreamWriter(out, enc), doc, pos, len);
    }

    /** Hope that it is called by knowing (encoding).
    */  
    public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
        super.write(out, doc, pos, len);
    }

}
