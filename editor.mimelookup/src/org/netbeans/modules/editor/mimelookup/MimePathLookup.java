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

package org.netbeans.modules.editor.mimelookup;

import java.util.ArrayList;
import java.util.Iterator;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.editor.mimelookup.MimeLookupInitializer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author vita
 */
public final class MimePathLookup extends ProxyLookup implements LookupListener {
    
    private MimePath mimePath;
    private Lookup.Result dataProviders;
    private Lookup.Result mimeInitializers; // This is supported for backwards compatibility only.
    
    /** Creates a new instance of MimePathLookup */
    public MimePathLookup(MimePath mimePath) {
        super();
        
        if (mimePath == null) {
            throw new NullPointerException("Mime path can't be null."); //NOI18N
        }
        
        this.mimePath = mimePath;

        dataProviders = Lookup.getDefault().lookup(new Lookup.Template(MimeDataProvider.class));
        dataProviders.addLookupListener((LookupListener) WeakListeners.create(LookupListener.class, this, dataProviders));

        mimeInitializers = Lookup.getDefault().lookup(new Lookup.Template(MimeLookupInitializer.class));
        mimeInitializers.addLookupListener((LookupListener) WeakListeners.create(LookupListener.class, this, mimeInitializers));
        
        rebuild();
    }

    public MimePath getMimePath() {
        return mimePath;
    }
    
    private void rebuild() {
        ArrayList lookups = new ArrayList();
        
        for (Iterator i = dataProviders.allInstances().iterator(); i.hasNext(); ) {
            MimeDataProvider provider = (MimeDataProvider) i.next();
            Lookup mimePathLookup = provider.getLookup(mimePath);
            if (mimePathLookup != null) {
                lookups.add(mimePathLookup);
            }
        }
        
        for (Iterator i = mimeInitializers.allInstances().iterator(); i.hasNext(); ) {
            MimeLookupInitializer initializer = (MimeLookupInitializer) i.next();
            for (int j = 0; j < mimePath.size(); j++) {
                Lookup.Result children = initializer.child(mimePath.getMimeType(j));
                for (Iterator k = children.allInstances().iterator(); k.hasNext(); ) {
                    MimeLookupInitializer mli = (MimeLookupInitializer) k.next();
                    Lookup mimePathLookup = mli.lookup();
                    if (mimePathLookup != null) {
                        lookups.add(mimePathLookup);
                    }
                }
            }
        }
        
        setLookups((Lookup [])lookups.toArray(new Lookup[lookups.size()]));
    }
    
    //-------------------------------------------------------------
    // LookupListener implementation
    //-------------------------------------------------------------

    public void resultChanged(LookupEvent ev) {
        rebuild();
    }
    
}
