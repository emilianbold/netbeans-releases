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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.RefreshableChildren;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.ImportCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.schema.ImportNode;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;
import org.openide.nodes.Children;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class AdvancedImportNode extends ImportNode {

    /**
     *
     *
     */
    public AdvancedImportNode(SchemaUIContext context,
            SchemaComponentReference<Import> reference,
            Children children) {
        super(context,reference,children);
    }

    @Override
    public boolean hasCustomizer() {
        return isEditable();
    }

    @Override
    public CustomizerProvider getCustomizerProvider() {
        return new CustomizerProvider() {

            public Customizer getCustomizer() {
                return new ImportCustomizer(getReference().get());
            }
        };
    }

    @Override
    public void valueChanged(ComponentEvent evt) {
        super.valueChanged(evt);
        if(evt.getSource()==getReference().get()) {
            ((RefreshableChildren) getChildren()).refreshChildren();
        }
    }
    
    protected void cleanup() {         
         //after deleting the node, we need to delete the prefix attribute from the xsd element
         Map<String, String> prefixes = getContext().getModel().getSchema().getPrefixes();
         Import _import = getReference().get();
         String ns = _import.getNamespace();
         Set<String> keys = prefixes.keySet();
         for(String key: keys) {
             if(prefixes.get(key).equals(ns) ) {
                 getContext().getModel().getSchema().removePrefix(key);
             }
         }             
     }
}
