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

package org.netbeans.modules.e2e.wsdl.wsdl2java;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import javax.swing.text.StyledDocument;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.IndentEngine;

/**
 *
 * @author Michal Skvor
 */
public class OutputFileFormatter {
    
    private FileObject fileObject;
    private DataObject dataObject;
    private StyledDocument styledDocument;
    private IndentEngine indentEngine;
    private Writer writer;
    private StringWriter stringWriter;
    
    /** Creates a new instance of OutputFileFormatter */
    public OutputFileFormatter( FileObject fileObject ) throws Exception {
        
//        System.err.println(" >>> Generating file - " + fileObject.getPath());
        
        this.fileObject = fileObject;
        
        dataObject = DataObject.find( fileObject );
        EditorCookie editorCookie = dataObject.getCookie( EditorCookie.class );
        
        styledDocument = editorCookie.openDocument();
        styledDocument.remove( 0, styledDocument.getLength());
        indentEngine = IndentEngine.find( styledDocument );
        
        stringWriter = new StringWriter( 4096 );
        writer = indentEngine.createWriter( styledDocument, 0, stringWriter );
    }
    
    /**
     * Appends to the end of well indented generated file
     *
     * @param data string to be written
     */
    public void write( String data ) {
        try {
            writer.write( data );
        }
        catch( IOException ex ) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                                                             ex.getMessage(), ex);
        }
    }
    
    /**
     * Closes file generation and flushes all caches
     */
    public void close() {
        try {
            writer.flush();
            writer.close();
            
            styledDocument.insertString( 0, stringWriter.getBuffer().toString(), null );
            
            SaveCookie save = dataObject.getCookie( SaveCookie.class );
            if( save != null ) {
                save.save();
            }
            
        } catch( Exception ex ) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                                                             ex.getMessage(), ex);
        }        
        
    }
}
