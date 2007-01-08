/*
 * OutputFileFormatter.java
 *
 * Created on November 7, 2006, 4:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
        
        System.err.println(" >>> Generating file - " + fileObject.getPath());
        
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
