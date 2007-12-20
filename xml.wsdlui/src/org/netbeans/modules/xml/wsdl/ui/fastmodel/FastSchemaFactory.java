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

/*
 * Created on Jan 26, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.fastmodel;

import java.io.File;
import java.io.InputStream;
import org.openide.util.NbBundle;


/**
 * @author radval
 *
 * A factory which parses wsdl fast.
 * Just parse some attributes from wsdl and ignore rests.
 */
public abstract class FastSchemaFactory {

    private static FastSchemaFactory factory;

    public FastSchemaFactory() {

    }

    /**
     * Gets the Fast WSDL Definitions factory singleton.
     * @return  a Fast WSDL Definitions factory.
     * @throws  EInsightModelException  When implementing factory class not found.
     */
    public static synchronized FastSchemaFactory getInstance() throws Exception {
        if (null == factory) {
            String fac = System.getProperty(FastSchemaFactory.class.getName(),
            "org.netbeans.modules.xml.wsdl.ui.fastmodel.impl.FastSchemaFactoryImpl");//NOI18N
            try {
                factory = (FastSchemaFactory) Class.forName(fac).newInstance();
            } catch (Exception e) {
                throw new Exception (
                        NbBundle.getMessage(FastSchemaFactory.class, "ERR_MSG_CLASS_NOT_FOUND", fac), e);
            }
        }
        return factory;
    }    
    
    public abstract FastSchema newFastSchema(InputStream in, boolean parseImports);
    
    
    public abstract FastSchema newFastSchema (File file);
    public abstract FastSchema newFastSchema (File file, boolean parseImports);
    
    public abstract FastSchema newFastSchema(String defFileUrl);
    
    public abstract FastSchema newFastSchema(String defFileUrl, 
            boolean parseImports);
    
}
