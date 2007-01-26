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

package org.netbeans.modules.xml.schema.completion;

import javax.swing.text.JTextComponent;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider;
import org.netbeans.modules.xml.schema.completion.util.DefaultModelProvider;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SchemaBasedCompletionProvider implements CompletionProvider {
    
    /**
     * Creates a new instance of SchemaBasedCompletionProvider
     */
    public SchemaBasedCompletionProvider() {
    }
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        FileObject primaryFile = getPrimaryFile();
        if(primaryFile == null)
            return 0;
        
        //only for xml instance document and wsdl files.
//        String extension = primaryFile.getExt();
//        if( !"xml".equals(extension) && !"wsdl".equals(extension) ) { //NOI18N
//            return 0;
//        }
        
        return COMPLETION_QUERY_TYPE;
    }
    
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == COMPLETION_QUERY_TYPE)
            return new AsyncCompletionTask(new CompletionQuery(getPrimaryFile()), component);
        
        return null;
    }
    
    private FileObject getPrimaryFile() {
        TopComponent activatedTC = TopComponent .getRegistry().getActivated();
        if(activatedTC == null)
            return null;
        DataObject activeFile = (DataObject)activatedTC.getLookup().lookup(DataObject.class);
        if(activeFile == null)
            return null;
        
        return activeFile.getPrimaryFile();
    }
    
}
