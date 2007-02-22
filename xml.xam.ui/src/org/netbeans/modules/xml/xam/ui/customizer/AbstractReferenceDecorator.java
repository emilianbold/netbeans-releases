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

package org.netbeans.modules.xml.xam.ui.customizer;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.xml.xam.Model;

/**
 * An abstract implementation of ExternalReferenceDecorator that provides
 * some common functionality for all concrete implementations to share.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractReferenceDecorator implements
        ExternalReferenceDecorator {
    /** Set of namespace prefixes, keyed by Model instances. */
    private Map<Model, String> prefixMap;

    /**
     * Creates a new instance of AbstractReferenceDecorator.
     */
    public AbstractReferenceDecorator() {
        prefixMap = new HashMap<Model, String>();
    }

    /**
     * Generate a unique namespace prefix. The model is provided as a
     * means of possibly creating a prefix that reflects the model in
     * some fashion (e.g. using its namespace).
     *
     * @param  model  XAM model, which may be used to generate the prefix.
     * @return  unique prefix value (e.g. "ns1"); must not be null.
     */
    protected abstract String generatePrefix(Model model);

    public String generatePrefix(ExternalReferenceNode node) {
        // It only makes sense to generate a prefix for nodes that have a
        // model, otherwise folders and non-XML files would have them.
        if (node.hasModel()) {
            // Use the model as the key, rather than the node itself, since
            // there could be multiple nodes representing a single model.
            Model model = node.getModel();
            String prefix = prefixMap.get(model);
            if (prefix == null) {
                prefix = generatePrefix(model);
                prefixMap.put(model, prefix);
            }
            return prefix;
        }
        return "";
    }
}
