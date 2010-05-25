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

package org.netbeans.modules.encoder.coco.ui;

import java.util.List;
import org.netbeans.modules.encoder.ui.basic.ModelUtils;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.basic.spi.AppInfoProvider;
import org.openide.nodes.Node;

/**
 * <appinfo> provider that implements the COBOL Copybook encoder plug-in over
 * the XSD editor.
 *
 * @author Jun Xu
 */
public class CocoEncoderAppInfoProvider extends AppInfoProvider {
    
    public boolean isActive(SchemaModel schemaModel) {
        return ModelUtils.isEncodedWith(schemaModel, CocoEncodingConst.STYLE);
    }

    public Node getNode(List<Node> nodes) {
        return null;
    }
}
