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

package org.netbeans.modules.xml.wsdl.validator;

import java.util.Collection;
import java.util.Hashtable;

import org.netbeans.modules.xml.wsdl.validator.spi.ValidatorSchemaFactory;
import org.openide.util.Lookup;

public class ValidatorSchemaFactoryRegistry {
    
    private static ValidatorSchemaFactoryRegistry registry;
    private Hashtable<String, ValidatorSchemaFactory> schemaFactories;
    
    private ValidatorSchemaFactoryRegistry() {
        initialize();
    }
    
    public static ValidatorSchemaFactoryRegistry getDefault() {
        if (registry == null) {
            registry = new ValidatorSchemaFactoryRegistry();
        }
        return registry;
    }
    
    private void initialize() {
        schemaFactories = new Hashtable<String, ValidatorSchemaFactory>();
        Lookup.Result results = Lookup.getDefault().lookup(new Lookup.Template(ValidatorSchemaFactory.class));
        for (Object service : results.allInstances()){
            ValidatorSchemaFactory factory = (ValidatorSchemaFactory) service;
            schemaFactories.put(factory.getNamespaceURI(), factory);
        }
    }
     
    public ValidatorSchemaFactory getValidatorSchemaFactory(String namespace) {
        return schemaFactories.get(namespace);
    }
    
    public Collection<ValidatorSchemaFactory> getAllValidatorSchemaFactories() {
        return schemaFactories.values();
    }

}
