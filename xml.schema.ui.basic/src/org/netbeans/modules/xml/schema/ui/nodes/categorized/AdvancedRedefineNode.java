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

import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.NewTypesFactory;
import org.netbeans.modules.xml.schema.ui.nodes.RefreshableChildren;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.RedefineCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.schema.RedefineNode;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;
import org.openide.nodes.Children;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class AdvancedRedefineNode extends RedefineNode {

    /**
     *
     *
     */
    public AdvancedRedefineNode(SchemaUIContext context,
            SchemaComponentReference<Redefine> reference,
            Children children) {
        super(context,reference,children);
    }

    @Override
    protected NewTypesFactory getNewTypesFactory() {
        return new AdvancedNewTypesFactory();
    }

    @Override
    public boolean hasCustomizer() {
        return isEditable();
    }

    @Override
    public CustomizerProvider getCustomizerProvider() {
        return new CustomizerProvider() {
            public Customizer getCustomizer() {
                return new RedefineCustomizer(getReference().get());
            }
        };
    }

    @Override
    public void valueChanged(ComponentEvent evt) {
        super.valueChanged(evt);
        if (isValid() && evt.getSource() == getReference().get()) {
            ((RefreshableChildren) getChildren()).refreshChildren();
        }
    }
}
