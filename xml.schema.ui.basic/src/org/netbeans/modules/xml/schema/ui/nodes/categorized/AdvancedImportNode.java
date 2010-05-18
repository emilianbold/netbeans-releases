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
