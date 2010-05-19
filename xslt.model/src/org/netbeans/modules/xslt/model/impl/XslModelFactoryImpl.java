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

package org.netbeans.modules.xslt.model.impl;

import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.model.spi.XslModelFactory;


/**
 * @author ads
 *
 */
public class XslModelFactoryImpl extends AbstractModelFactory<XslModel> 
    implements XslModelFactory
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.AbstractModelFactory#createModel(org.netbeans.modules.xml.xam.ModelSource)
     */
    @Override
    protected XslModel createModel( ModelSource source )
    {
        return new XslModelImpl( source );
    }
    
    @Override
    public XslModel getModel(ModelSource source) {
        if (source == null) {
            return null;
        }

        return super.getModel(source);
//        return super.getModel(source);
    }

}
