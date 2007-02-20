/*
 * JSFConfigEditorViewFactorySupport.java
 *
 * Created on February 7, 2007, 5:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorViewFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author petr
 */
public class JSFConfigEditorViewFactorySupport {
    
    private static final Lookup.Result factoriesLookupResult = Lookup.getDefault ().lookupResult (JSFConfigEditorViewFactory.class);

    public static Collection<MultiViewDescription> createViewDescriptions (JSFConfigEditorContext facesContext) {
        ArrayList<MultiViewDescription> list = new ArrayList<MultiViewDescription> ();
        for (Object factory : factoriesLookupResult.allInstances ()) {
            MultiViewDescription desc = ((JSFConfigEditorViewFactory) factory).createMultiViewDescriptor (facesContext);
            if (desc != null)
                list.add (desc);
        }
        return Collections.unmodifiableCollection (list);
    }
    
}
