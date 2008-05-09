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

package org.netbeans.modules.xslt.tmap.nodes;

import org.netbeans.modules.xslt.tmap.model.api.Import;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class DecoratedImport extends DecoratedTMapComponentAbstract<Import>{

    public DecoratedImport(Import orig) {
        super(orig);
    }

    @Override
    public String getName() {
        Import ref = getOriginal();
        String location = null;
        if (ref != null) {
            location = ref.getLocation();
        }
        return location == null ? super.getName() : location;
    }

    @Override
    public String getHtmlDisplayName() {
        Import ref = getOriginal();
        String namespace = null;
        if (ref != null) {
            namespace = ref.getNamespace();
        }
        String addon = null;
        
        if (namespace != null) {
            addon = (addon == null ? TMapComponentNode.EMPTY_STRING 
                    : addon+TMapComponentNode.WHITE_SPACE)+ TMapComponentNode.WHITE_SPACE+namespace; // NOI18N
        }
        
        return Util.getGrayString(super.getHtmlDisplayName(), addon);
    }

    @Override
    public String getTooltip() {
        Import ref = getOriginal();
        StringBuffer attributesTooltip = new StringBuffer();
        
        if (ref != null) {
            attributesTooltip.append(
                    Util.getLocalizedAttribute(ref.getLocation()
                    , Import.LOCATION));
                    
            attributesTooltip.append(
                    Util.getLocalizedAttribute(ref.getNamespace()
                    , Import.NAMESPACE));
        }
       
        return NbBundle.getMessage(TMapComponentNode.class, 
                "LBL_LONG_TOOLTIP_HTML_TEMPLATE", super.getName(), 
                attributesTooltip.toString());
    }
}
