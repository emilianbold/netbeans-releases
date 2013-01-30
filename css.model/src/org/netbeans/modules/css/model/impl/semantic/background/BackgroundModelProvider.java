/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.model.impl.semantic.background;

import java.util.Collection;
import org.netbeans.modules.css.lib.api.properties.Node;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.css.model.api.semantic.ModelProvider;
import org.netbeans.modules.css.model.api.semantic.background.BackgroundModel;
import org.netbeans.modules.css.model.impl.semantic.Element;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service=ModelProvider.class)
public class BackgroundModelProvider implements ModelProvider<BackgroundModel> {

    @Override
    public Class getModelClass() {
        return BackgroundModel.class;
    }

    @Override
    public BackgroundModel createModel(ResolvedProperty resolvedProperty) {
        BackgroundModelI model = new BackgroundModelI();
        Node node = resolvedProperty.getParseTree();
          switch(Element.forNode(node)) {
            case background:
                model.addBackgrounds(new BackgroundProperty(node).getBackgrounds());
                break;
            case background_position:
                model.addBackgrounds(new BackgroundPositionProperty(node).getBackgrounds());
                break;
            case background_color:
                model.addBackground(new BackgroundColorProperty(node).getBackground());
                break;
            case background_image:
                model.addBackgrounds(new BackgroundImageProperty(node).getBackgrounds());
                break;
            case background_repeat:
                model.addBackgrounds(new BackgroundRepeatProperty(node).getBackgrounds());
                break;
            case background_attchment:
                model.addBackgrounds(new BackgroundAttachmentProperty(node).getBackgrounds());
                break;
            case background_clip:
                model.addBackgrounds(new BackgroundClipProperty(node).getBackgrounds());
                break;
            case background_origin:
                model.addBackgrounds(new BackgroundOriginProperty(node).getBackgrounds());
                break;
            case background_size:
                model.addBackgrounds(new BackgroundSizeProperty(node).getBackgrounds());
                break;
                
            default:
                return null;
        }
        return model;
    }

    @Override
    public BackgroundModel createModel(Collection<ResolvedProperty> resolvedProperty) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
