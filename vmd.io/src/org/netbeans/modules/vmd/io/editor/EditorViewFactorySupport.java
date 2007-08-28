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

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataEditorViewFactory;
import org.openide.util.Lookup;

import java.util.*;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;

/**
 * @author David Kaspar
 */
public class EditorViewFactorySupport {

    private static final Lookup.Result factoriesLookupResult = Lookup.getDefault ().lookupResult (DataEditorViewFactory.class);

    public static Collection<DataEditorView> createEditorViews (DataObjectContext context) {
        String projectType = IOSupport.resolveProjectType (context);
        if (projectType == null)
            return Collections.EMPTY_LIST;
        ArrayList<DataEditorView> list = new ArrayList<DataEditorView> ();
        for (Object factory : factoriesLookupResult.allInstances ()) {
            DataEditorView desc = ((DataEditorViewFactory) factory).createEditorView (context);
            if (desc != null)
                list.add (desc);
        }
        Collections.sort (list, new Comparator<DataEditorView>() {
            public int compare (DataEditorView o1, DataEditorView o2) {
                return o1.getOrder () - o2.getOrder ();
            }
        });
        return Collections.unmodifiableCollection (list);
    }

}
