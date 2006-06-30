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

package org.netbeans.modules.xml.xam;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.locator.api.ModelSource;

/**
 *
 * @author Chris Webster
 * @author Nam Nguyen
 */
public abstract class AbstractModelFactory<M extends DocumentModel> {
    
    private Map<Document, WeakReference<M>> cachedModels = new WeakHashMap<Document,WeakReference<M>>();
    
    protected abstract M createModel(ModelSource source);
    
    public M getModel(ModelSource source) throws IOException {
        if (source == null) {
            return null;
        }
        Document doc = source.getDocument();
        WeakReference<M> modelRef = cachedModels.get(doc);
        M model = (modelRef == null ? null : modelRef.get());
        if (model == null && doc != null) {
            model = createModel(source);
            cachedModels.put(doc, new WeakReference<M>(model));
            model.sync();
        }
        return model;
    }
}
