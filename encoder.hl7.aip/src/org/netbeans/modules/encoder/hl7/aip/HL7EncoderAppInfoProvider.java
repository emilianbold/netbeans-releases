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

package org.netbeans.modules.encoder.hl7.aip;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.encoder.ui.basic.InvalidAppInfoException;
import org.netbeans.modules.encoder.ui.basic.ModelUtils;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.ui.basic.spi.AppInfoProvider;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * <appinfo> provider that implements the HL7 encoder plug-in over
 * the XSD editor.
 *
 * @author Jun Xu
 */
public class HL7EncoderAppInfoProvider extends AppInfoProvider {
    
    public boolean isActive(SchemaModel schemaModel) {
        return ModelUtils.isEncodedWith(schemaModel, HL7EncodingConst.STYLE);
    }

    public Node getNode(List<Node> nodes) {
        List<SchemaComponent> listComponents = new ArrayList<SchemaComponent>();
        SchemaComponent comp;
        SchemaComponent prevComp = null;
        for (Node node : nodes) {
            comp = (SchemaComponent) node.getLookup().lookup(SchemaComponent.class);
            if (comp != null && comp != prevComp) {
                listComponents.add(comp);
                prevComp = comp;
            }
        }
        Annotation annotation = 
                (Annotation) nodes.get(nodes.size() - 1).getLookup().lookup(Annotation.class);
        if ((!(annotation.getParent() instanceof Element)
                || annotation.getParent() instanceof ElementReference)
                && !(annotation.getParent() instanceof ComplexType)
                && !(annotation.getParent() instanceof SimpleType)) {
            return null;
        }
        AppInfo encAppinfo = ModelUtils.getEncodingAppinfo(annotation);
        if (encAppinfo == null) {
            return null;
        }
        Lookup lookup = Lookups.singleton(encAppinfo);
        EncodingOption encodingOption;
        try {
            encodingOption = EncodingOption.createFromAppInfo(listComponents);
        } catch (InvalidAppInfoException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
        if (encodingOption == null) {
            return null;
        }
        return new EncodingNode(encodingOption, lookup);
    }
}
