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
package org.netbeans.modules.vmd.io.editor;

import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataEditorViewLookupFactory;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author David Kaspar
 */
final class DataEditorViewLookupFactoryRegistry {

    private static final Lookup.Result<DataEditorViewLookupFactory> factoriesLookupResult = Lookup.getDefault ().lookupResult (DataEditorViewLookupFactory.class);

    static ArrayList<Object> getLookupObjects (DataObjectContext context, String viewID, DataEditorView.Kind viewKind) {
        ArrayList<Object> list = new ArrayList<Object> ();
        for (DataEditorViewLookupFactory factory : factoriesLookupResult.allInstances ()) {
            Collection<? extends Object> objects = factory.getLookupObjects (context, viewID, viewKind);
            if (objects != null)
                list.addAll (objects);
        }
        return list;
    }

}
