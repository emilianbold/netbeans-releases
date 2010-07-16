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

import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.ParamType;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class DecoratedParam extends DecoratedTMapComponentAbstract<Param> {

    public DecoratedParam(Param orig) {
        super(orig);
    }
    

    @Override
    public String getHtmlDisplayName() {
        Param ref = getReference();
        ParamType type = ref == null ? null : ref.getType();

        String typeStr = null;
        StringBuffer addon = new StringBuffer();
        if (type != null) {
            typeStr = type.toString();
            switch (type) {
                case PART:
                    String value = ref.getValue();
                    if (value != null) {
                        typeStr += " part=";// NOI18N
                        typeStr += value;
                    }
                    break;
                default:
                    typeStr += " ...";
            }


            addon.append(TMapComponentNode.WHITE_SPACE).append(typeStr); // NOI18N
        }
        
        return Util.getGrayString(super.getHtmlDisplayName(), addon.toString());
    }

    @Override
    public String getTooltip() {
        Param ref = getReference();
        StringBuffer attributesTooltip = new StringBuffer();
        if (ref != null) {
            attributesTooltip.append(
                    Util.getLocalizedAttribute(ref.getName()
                    , Param.NAME_PROPERTY));

            attributesTooltip.append(
                    Util.getLocalizedAttribute(ref.getValue()
                    , Param.VALUE));

            attributesTooltip.append(
                    Util.getLocalizedAttribute(ref.getType().toString()
                    , Param.TYPE));
        }
        return  NbBundle.getMessage(TMapComponentNode.class, 
                "LBL_LONG_TOOLTIP_HTML_TEMPLATE", super.getName(), 
                attributesTooltip.toString());      
    }    
}

