/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xsd;

import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import java.util.WeakHashMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.enum.SingletonEnumeration;
import org.netbeans.modules.xml.api.model.*;
import org.netbeans.api.xml.services.UserCatalog;

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
            return new SingletonEnumeration(ret.getTarget());
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