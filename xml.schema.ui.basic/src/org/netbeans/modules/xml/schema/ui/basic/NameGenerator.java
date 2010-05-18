/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
