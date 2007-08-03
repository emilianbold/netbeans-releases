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
package org.netbeans.modules.xslt.mapper.model;

import org.netbeans.modules.xslt.mapper.view.XsltMapper;

/**
 *
 * @author Alexey
 * Keeps mapper trees in-sync with schema models
 * Subscribes on changes in schema model and reloads left and right trees
 *
 */
public class SchemaModelBridge extends ModelBridge {

    public SchemaModelBridge(XsltMapper mapper) {
        super(mapper);
        if (mapper.getContext().getSourceType() != null) {
            super.subscribe(mapper.getContext().getSourceType().getModel());
        }
        
        if (mapper.getContext().getTargetType() != null) {
            super.subscribe(mapper.getContext().getTargetType().getModel());
        }
    }

    protected void onModelChanged() {
        if (!checkErrors()) {
            return;
        }
        super.reloadTree(getMapper().getMapperViewManager().getDestView().getTree());
        super.reloadTree(getMapper().getMapperViewManager().getSourceView().getTree());

        getMapper().getBuilder().updateDiagram();
    }
}
