/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.xsd;

import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import java.util.WeakHashMap;
import org.netbeans.modules.xml.api.model.*;

/**
 * Provide XSD grammar. It must be registered at layer.
 *
 * @author  Ales Novak <ales.novak@sun.com>
 */
public class XSDGrammarQueryProvider extends GrammarQueryManager {

    private static final WeakHashMap schemas = new WeakHashMap();
 
    public XSDGrammarQueryProvider() {
    }
    
    public Enumeration enabled(GrammarEnvironment ctx) {
        
        if (ctx.getFileObject() == null) {
            return null;
        }
        
        XSDGrammarResolver ret = XSDGrammarResolver.createResolver(ctx);
        if (ret != null) {
            // remember this schema
            schemas.put(ctx.getFileObject(), ret);
            return org.openide.util.Enumerations.singleton (ret.getTarget());
        }
        
	return null;
    }
    
    public FeatureDescriptor getDescriptor() {
        return new FeatureDescriptor();
    }
    
    public GrammarQuery getGrammar(GrammarEnvironment env) {
        return  (XSDGrammarResolver) schemas.remove(env.getFileObject());
    }
}