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

import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.Model;
import org.openide.nodes.Node;

/**
 * An ExternalReferenceDecorator is used to control the appearance of the
 * nodes in the ExternalReferenceCustomizer.
 *
 * @author  Nathan Fiedler
 */
public interface ExternalReferenceDecorator {

    /**
     * Create an ExternalReferenceNode with the given delegate node.
     * Implementors may wish to delegate to the customizer.
     *
     * @param  node  delegate Node.
     * @return  new ExternalReferenceNode.
     */
    ExternalReferenceDataNode createExternalReferenceNode(Node original);

    /**
     * Generate a unique prefix value for the document containing the
     * customized component. The selected node is provided, which permits
     * customizing the prefix based on the model represented by the node.
     *
     * @param  node  the currently selected node.
     * @return  unique prefix value (e.g. "ns1"); must not be null.
     */
    String generatePrefix(ExternalReferenceNode node);

    /**
     * Return the document type that this decorator wants to show in the
     * file chooser.
     *
     * @return  the desired document type.
     */
    Utilities.DocumentTypesEnum getDocumentType();

    /**
     * Generate the HTML display name for the node.
     *
     * @param  name  original display name (may be HTML form).
     * @param  node  external reference node to decorate.
     */
    String getHtmlDisplayName(String name, ExternalReferenceNode node);

    /**
     * Return the namespace appropriate for the model.
     *
     * @param  model  the Model from which to acquire the namespace.
     * @return  the namespace value, or null if none.
     */
    String getNamespace(Model model);

    /**
     * Validate the given node, returning a non-null value if the node
     * is not a valid selection. Otherwise, return null if it is valid.
     *
     * @param  node  external reference node to validate.
     * @return  message describing the issue; null if valid.
     */
    String validate(ExternalReferenceNode node);
}
