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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.schema.completion.spi;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.editor.BaseDocument;
import org.openide.filesystems.FileObject;

/**
 * Represents code completion context at the current cursor location.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class CompletionContext {
    
    /**
     * Returns true if schemaLocation attribute is specified
     * in the root element, false otherwise.
     */
    public abstract boolean isSchemaAwareCompletion();
    
    /**
     * Returns the list of schemas as specified in the schemaLocation
     * attribute of the root element. Null if the attribute is not specified.
     */
    public abstract List<URI> getSchemas();

    /**
     * Returns the default namespace for the document.
     */
    public abstract String getDefaultNamespace();
    
    /**
     * Returns the type of completion.
     */
    public abstract CompletionType getCompletionType();
        
    /**
     * Returns the path from root element to the cursor location.
     */
    public abstract List<QName> getPathFromRoot();
    
    /**
     * Returns the FileObject for the document.
     */
    public abstract FileObject getPrimaryFile();
    
    /**
     * Returns the BaseDocument for the document.
     */
    public abstract BaseDocument getBaseDocument();
    
    /**
     * Returns all the namespaces declared in the document as a HashMap.
     */
    public abstract HashMap<String, String> getDeclaredNamespaces();
    
    /**
     * Returns the typed characters during completion.
     */
    public abstract String getTypedChars();
            
    /**
     * CompletionType.
     */
    public static enum CompletionType {
        COMPLETION_TYPE_UNKNOWN,
        COMPLETION_TYPE_ATTRIBUTE,
        COMPLETION_TYPE_VALUE,
        COMPLETION_TYPE_ELEMENT,
        COMPLETION_TYPE_ENTITY,
        COMPLETION_TYPE_NOTATION,
        COMPLETION_TYPE_DTD
    }
}
