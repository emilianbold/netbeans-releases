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

import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.ParamType;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.util.Exceptions;

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
        Param ref = getOriginal();
        ParamType type = ref == null ? null : ref.getType();

        String typeStr = null;
        StringBuffer addon = new StringBuffer();
        if (type != null) {
            try {
                typeStr = type.toString();
                switch (type) {
                    case URI:
                        URI uri = ref.getUri();
                        String uriStr = uri == null ? "" : uri.getPath();
                        typeStr += " uri=" + uriStr;
                        break;
                    case PART:
                        ref.getValue();
                        typeStr += " part=";
                        break;
                    default:
                        typeStr += " ...";
                }


                addon.append(TMapComponentNode.WHITE_SPACE).append(typeStr); // NOI18N
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return Util.getGrayString(super.getHtmlDisplayName(), addon.toString());
    }
}

