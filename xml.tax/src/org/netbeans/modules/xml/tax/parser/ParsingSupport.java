/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.parser;

import java.io.StringReader;
import java.io.IOException;
import java.net.URL;
import java.lang.ref.WeakReference;

import javax.swing.text.Document;

import org.xml.sax.InputSource;

import org.openide.nodes.*;  //CookieFactory
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeDocumentRoot;

/**
 * Provides support for parsing differnt sources. Subclasses must implement
 * accordingly <code>parse(InputSource)</code> method.
 *
 * @author  Petr Kuzel
 * @version 0.9
 */
public abstract class ParsingSupport {
        
    /**
     * Parse DataObject returning its model or null (on parse failure).
     */
    public abstract TreeDocumentRoot parse(InputSource in)  throws IOException, TreeException;

    
    /**
     * Converts to InputSource and pass it.
     */
    protected TreeDocumentRoot parse(TreeDocumentRoot doc) throws IOException, TreeException {
        
        return doc;
        
//        InputSource in = new InputSource();
//        in.setCharacterStream(new TreeReader(doc));
//        return parse(in);
    }

    
    /**
     * Converts to InputSource and pass it.
     */    
    protected TreeDocumentRoot parse(final Document doc) throws IOException, TreeException {
        
        InputSource in = new InputSource();
        
        // safely take the text from the document
        //??? what about DocumentReader
        
        final String[] str = new String[1];
        
        Runnable run = new Runnable() {
            public void run () {
                try {
                    str[0] = doc.getText(0, doc.getLength());
                } catch (javax.swing.text.BadLocationException e) {
                    // impossible
                    e.printStackTrace();
                }
            }
        };

        doc.render(run);
        in.setCharacterStream(new StringReader(str[0]));
        return parse(in);
    }
    
    
    /**
     * Converts to InputSource and pass it.
     */    
    protected TreeDocumentRoot parse(FileObject fo) throws IOException, TreeException{
        
        try {
            URL url = fo.getURL();
            InputSource in = new InputSource(url.toExternalForm());  //!!! we could try ti get encoding from MIME content type
            in.setByteStream(fo.getInputStream());
            return parse(in);
            
        } catch (IOException ex) {
            ErrorManager emgr = ErrorManager.getDefault();
            emgr.annotate(ex, Util.THIS.getString("MSG_can_not_create_URL"));
            emgr.notify(ex);
        }           
        return null;
    }
    
}
