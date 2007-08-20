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


package org.netbeans.modules.vmd.inspector;

import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataEditorViewLookupFactory;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.navigator.NavigatorLookupPanelsPolicy;
import org.openide.util.Lookup;

import java.util.Arrays;
import java.util.Collection;
import org.netbeans.modules.vmd.api.properties.common.PropertiesSupport;
import org.openide.util.lookup.InstanceContent;

/**
 * @author Karol Harezlak
 */

public class InspectorLookupFactory implements DataEditorViewLookupFactory {
    
    private static InstanceContent ic;
    
    public Collection<?> getLookupObjects(DataObjectContext context, DataEditorView view) {
        InspectorManagerView.register(context);
        if (view.canShowSideWindows()  &&  view.getKind() == DataEditorView.Kind.MODEL) {
            return Arrays.asList( new NavigatorLookupHint() {
                public String getContentType() {
                    return "vmd"; // NOI18N
                }
            }, new NavigatorLookupPanelsPolicy() {
                public int getPanelsPolicy() {
                    return NavigatorLookupPanelsPolicy.LOOKUP_HINTS_ONLY;
                }
            });
        }        
        return null;
    }
    
    public Collection<? extends Lookup> getLookups(DataObjectContext context, DataEditorView view) {
        if (view.canShowSideWindows()  &&  view.getKind() == DataEditorView.Kind.MODEL) {
            if (ic == null) {
                ic = InspectorPanel.getInstance().getInstanceContent();
                ic.add(context.getDataObject().getLookup());
            }
            PropertiesSupport.addInstanceContent(view, ic);
        }
        return null;
    }
    
}
