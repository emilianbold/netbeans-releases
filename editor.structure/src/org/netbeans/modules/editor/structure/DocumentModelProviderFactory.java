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

package org.netbeans.modules.editor.structure;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.editor.structure.spi.DocumentModelProvider;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 * Document model factory that obtains the DocumentModel instancies
 * by reading the xml layer.
 * <br>
 * The registration are read from the following folder in the system FS:
 * <pre>
 *     Editors/&lt;mime-type&gt;/DocumentModel
 * </pre>
 *
 * @author Marek Fukala
 */
public class DocumentModelProviderFactory {
    
    static final String FOLDER_NAME = "DocumentModel"; //NOI18N
    
    private Map<String, DocumentModelProvider> mime2provider;
    
    private static DocumentModelProviderFactory defaultProvider = null;
    
    public static DocumentModelProviderFactory getDefault() {
        if(defaultProvider == null) {
            defaultProvider = new DocumentModelProviderFactory();
        }
        return defaultProvider;
    }
    
    private DocumentModelProviderFactory() {
        mime2provider = new WeakHashMap<String, DocumentModelProvider>();
    }
    
    /* returns a DocumentModelFactory according to the layer */
    public DocumentModelProvider getDocumentModelProvider(String mimeType) {
        DocumentModelProvider provider = null; // result
        if(mimeType != null) {
            provider = mime2provider.get(mimeType);
            if (provider == null) { // not cached yet
                MimeLookup mimeLookup = MimeLookup.getMimeLookup(mimeType);
                Collection<? extends DocumentModelProvider> providers = 
                        mimeLookup.lookup(new Lookup.Template<DocumentModelProvider>(DocumentModelProvider.class)).allInstances();
                if(providers.size() > 1)
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Only one DocumentModelProvider can be registered for one mimetype!");
                
                if(providers.size() == 0)
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("There isn't any DocumentModelProvider registered for " + mimeType + " mimetype!"));
                
                provider = providers.size() > 0 ? (DocumentModelProvider)providers.iterator().next() : null;
                mime2provider.put(mimeType, provider);
                
            } else return provider;
        } else
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new NullPointerException("mimeType cannot be null!"));
        
        return provider;
    }
    
    
}
