/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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
    
    private Map mime2provider;
    
    private static DocumentModelProviderFactory defaultProvider = null;
    
    public static DocumentModelProviderFactory getDefault() {
        if(defaultProvider == null) {
            defaultProvider = new DocumentModelProviderFactory();
        }
        return defaultProvider;
    }
    
    private DocumentModelProviderFactory() {
        mime2provider = new WeakHashMap();
    }
    
    /* returns a DocumentModelFactory according to the layer */
    public DocumentModelProvider getDocumentModelProvider(String mimeType) {
        DocumentModelProvider provider = null; // result
        if(mimeType != null) {
            provider = (DocumentModelProvider)mime2provider.get(mimeType);
            if (provider == null) { // not cached yet
                MimeLookup mimeLookup = MimeLookup.getMimeLookup(mimeType);
                Collection providers = mimeLookup.lookup(new Lookup.Template(DocumentModelProvider.class)).allInstances();
                if(providers.size() > 1)
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Only one DocumentModelProvider can be registered for one mimetype!");
                
                if(providers.size() == 0)
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("There isn't any DocumentModelProvider registered for " + mimeType + " mimetype!"));
                
                provider = providers.size() > 0 ? (DocumentModelProvider)providers.iterator().next() : null;
            }
        } else
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new NullPointerException("mimeType cannot be null!"));
        
        return provider;
    }
    
    
}
