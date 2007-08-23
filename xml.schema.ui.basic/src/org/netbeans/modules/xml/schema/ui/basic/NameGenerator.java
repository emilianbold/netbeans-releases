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

package org.netbeans.modules.xml.schema.ui.basic;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 * Utility class for generating names for elements within a schema model.
 *
 * @author Nathan Fiedler
 */
public class NameGenerator {
    /** Default starting value for the uniqueness counter (e.g. 0 or 1). */
//    private static final int COUNTER_START = 1;
    /** Prefix for the namespace prefix values (e.g. "ns"). */
    private static final String PREFIX_PREFIX = "ns"; // NOI18N
    /** The singleton instance of this class. */
    private static NameGenerator theInstance;
    /** hashmap containing generated prefixes */
    private List generatedPrefixes = new ArrayList<String>();

    /**
     * Creates a new instance of NameGenerator.
     */
    private NameGenerator() {
    }

    /**
     * Return the singleton instance of this class.
     *
     * @return  instance of this class.
     */
    public static synchronized NameGenerator getInstance() {
        if (theInstance == null) {
            theInstance = new NameGenerator();
        }
        return theInstance;
    }

    /**
     * Generate a unique namespace prefix for the given model. This is
     * the same as generateNamespacePrefix(String, SchemaModel, int) with
     * a value of zero for the counter parameter.
     *
     * @param  prefix  the desired prefix for the namespace prefix;
     *                 if null, a default of "ns" will be used.
     * @param  model   model in which to find unique prefix.
     * @return  the unique namespace prefix (e.g. "ns0").
     */
    public String generateNamespacePrefix(String prefix,
            SchemaModel model) {
        // WSDL uses zero for the namespace prefix, so let's do the same.
        return generateNamespacePrefix(prefix, model, 0);
    }

    /**
     * Generate a unique namespace prefix for the given model.
     *
     * @param  prefix   the desired prefix for the namespace prefix;
     *                  if null, a default of "ns" will be used.
     * @param  model    model in which to find unique prefix.
     * @param  counter  minimum number to use as suffix (results in a
     *                  prefix such as "ns" plus the value of counter).
     * @return  the unique namespace prefix (e.g. "ns0").
     */
    public String generateNamespacePrefix(String prefix,
            SchemaModel model, int counter) {
        String prefixStr = prefix == null ? PREFIX_PREFIX : prefix;
        //we need to keep track of the generated prefixes because even if we increase
        //the counter here to get a unique prefix, it doesn update the 
        // original counter that is passed as an arg
        //as a result, dup prefixes will be generated
        //for eg: if a schema already references another schema with ns1 prefix, then 
        //the following code (without the fix) will generate 2 prefixes with ns02 as follows
        //for counter = 0, generated = ns1
        //for counter = 1; generated = ns2 (since model already has prefix ns1)
        //for counter = 2; generated = ns2 
        if(counter == 0)
            generatedPrefixes.clear();
        String generated = prefixStr + counter;
        while (isPrefixExist(generated, model)  || generatedPrefixes.contains(generated)) {
            counter++;
            generated = prefixStr + counter;
        }
        generatedPrefixes.add(generated);
        return generated;
    }

    /**
     * Determine if the given namespace prefix is used in the model.
     *
     * @param  prefix  namespace prefix to look up.
     * @param  model   the model in which to look.
     * @return  true if exists, false otherwise.
     */
    public static boolean isPrefixExist(String prefix, SchemaModel model) {
        return getNamespaceURI(prefix, model) != null ? true : false;
    }

    /**
     * Get the prefix for the given namespace, for this given element.
     *
     * @param  namespace  the namespace to lookup.
     * @param  element    the element to look at.
     * @return  the prefix, or null if none.
     */
    public static String getNamespacePrefix(String namespace,
            SchemaComponent element) {
        if (element != null && namespace != null) {
            return ((AbstractDocumentComponent) element).lookupPrefix(namespace);
        }
        return null;
    }

    /**
     * Retrieve the namespace for the given prefix, if any.
     *
     * @param  prefix  namespace prefix to look up.
     * @param  model   the model in which to look.
     * @return  the namespace for the prefix, or null if none.
     */
    public static String getNamespaceURI(String prefix, SchemaModel model) {
        if (model != null && prefix != null) {
            return ((AbstractDocumentComponent) model.getSchema()).
                    lookupNamespaceURI(prefix, true);
        }
        return null;
    }
}
