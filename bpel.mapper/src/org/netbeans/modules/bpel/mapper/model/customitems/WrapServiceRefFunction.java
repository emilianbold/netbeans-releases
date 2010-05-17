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
package org.netbeans.modules.bpel.mapper.model.customitems;

import javax.swing.Icon;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.images.IconLoader;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class WrapServiceRefFunction implements BpelXPathCustomFunction {

    private static String IMAGE_FOLDER_NAME = "org/netbeans/modules/bpel/mapper/resources/"; // NOI18N

    private static WrapServiceRefFunction INSTANCE = new WrapServiceRefFunction();
    
    private WrapServiceRefFunction() {
    }
    
    public static WrapServiceRefFunction getInstance() {
        return INSTANCE;
    }
    
    public String getName() {
        return NbBundle.getMessage(BpelXPathCustomFunction.class
                , "LBL_WrapServiceRefName"); // NOI18N
    }

    public String getDisplayName() {
        return getName();
    }

    public String getShortDescription() {
        return getName();
    }

    public String getLongDescription() {
        return getName();
    }

    public CustomFunctionType getFunctionType() {
        return CustomFunctionType.WRAP_SERVICE_REF;
    }

    public Icon getIcon() {
        return IconLoader.getIcon("wrapServiceRef", IMAGE_FOLDER_NAME); // NOI18N
    }
}
