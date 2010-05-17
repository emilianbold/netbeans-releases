/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.mobility.javon;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.IndentEngine;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.ErrorManager;

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
    public OutputFileFormatter( FileObject fileObject ) 
        throws DataObjectNotFoundException, IOException {
        
        this.fileObject = fileObject;
        
        dataObject = DataObject.find( fileObject );
        EditorCookie editorCookie = dataObject.getCookie( EditorCookie.class );
        
        styledDocument = editorCookie.openDocument();
        try {
            styledDocument.remove( 0, styledDocument.getLength());
        }
        catch (BadLocationException e ){
            ErrorManager.getDefault().notify( e );
        }
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
