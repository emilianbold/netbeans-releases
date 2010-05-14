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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xslt.tmap.ui.importchooser;

import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;

/**
 * Utility class to generate uniqiue names for TMapModel
 * @author Vitaly Bychkov
 */
public class NameGenerator {

    // Default Prefix for the namespace 
    private static final String PREFIX_PREFIX = "ns"; // NOI18N

    private static NameGenerator INSTANCE = new NameGenerator();
    
    private NameGenerator() {
    }
    
    public static NameGenerator getInstance() {
        return INSTANCE;
    }
    
    /**
     * Generate a unique namespace prefix for the given model. This is
     * the same as generateNamespacePrefix(String, BpelModel, int) with
     * a value of zero for the counter parameter.
     *
     * @param  prefix  the desired prefix for the namespace prefix;
     *                 if null, a default of "ns" will be used.
     * @param  model   model in which to find unique prefix.
     * @return  the unique namespace prefix (e.g. "ns0").
     */
    public String generateNamespacePrefix(String prefix,
            TMapModel model) {
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
            TMapModel model, int counter) {
        String prefixStr = prefix == null ? PREFIX_PREFIX : prefix;
        String generated = prefixStr + counter;
        while (isPrefixExist(generated, model)) {
            counter++;
            generated = prefixStr + counter;
        }
        return generated;
    }

    /**
     * Determine if the given namespace prefix is used in the model.
     *
     * @param  prefix  namespace prefix to look up.
     * @param  model   the model in which to look.
     * @return  true if exists, false otherwise.
     */
    public static boolean isPrefixExist(String prefix, TMapModel model) {
        return getNamespaceURI(prefix, model) != null ? true : false;
    }

    /**
     * Get the prefix for the given namespace, for this given element.
     *
     * @param  namespace  the namespace to lookup.
     * @param  model   the model in which to look.
     * @return  the prefix, or null if none.
     */
    public static String getNamespacePrefix(String namespace,
            TMapModel model) 
    {
        if (model != null && namespace != null) {
            return getNamespacePrefix(namespace, model.getTransformMap());
        }
        return null;
    }

    /**
     * Get the prefix for the given namespace, for this given element.
     *
     * @param  namespace  the namespace to lookup.
     * @param  element    the element to look at.
     * @return  the prefix, or null if none.
     */
    public static String getNamespacePrefix(String namespace,
            TMapComponent component) {
        if (component != null && namespace != null) {
            ExNamespaceContext nsContext = component.getNamespaceContext();
            if (nsContext != null) {
                String oldPrefix = nsContext.getPrefix(namespace);
                if (oldPrefix != null && oldPrefix.length() > 0) {
                    return oldPrefix;
                }
            }
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
    public static String getNamespaceURI(String prefix, TMapModel model) {
        if (model != null && prefix != null) {
            TMapComponent component = model.getTransformMap();
            ExNamespaceContext nsContext = component != null 
                        ? component.getNamespaceContext() : null;
            if (nsContext != null) {
                return nsContext.getNamespaceURI(prefix);
            }
        }
        return null;
    }
}
