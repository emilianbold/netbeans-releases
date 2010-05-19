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
 * License. When distributing the software, include this License Header
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
package org.netbeans.modules.bpel.validation.wsdl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.visitor.DefaultVisitor;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.bpel.validation.core.WsdlValidator;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.02.15
 */
public final class Validator extends WsdlValidator {

    @Override
    protected WSDLVisitor getVisitor() {
        return new DefaultVisitor() {

            @Override
            public void visit(Definitions definitions) {
//out();
//out("definitions: " + definitions);
                List<PropertyAlias> aliases = new ArrayList<PropertyAlias>();
                collectPropertyAliases(definitions, aliases);
//out("aliases: " + aliases.size());

                // # 120419
                for (int i = 0; i < aliases.size(); i++) {
                    for (int j = i + 1; j < aliases.size(); j++) {
                        if (theSame(aliases.get(i), aliases.get(j))) {
//out("the same");
                            addError("FIX_Identical_Property_Aliases", aliases.get(i)); // NOI18N
                            addError("FIX_Identical_Property_Aliases", aliases.get(j)); // NOI18N
                        }
                    }
                }
                // # 125954
                visitChildren(definitions);
            }

            private void visitChildren(Definitions definitions) {
                Collection<ExtensibilityElement> collection = definitions.getChildren(ExtensibilityElement.class);
//out("collection: " + elements);

                for (ExtensibilityElement element : collection) {
                    visitExtensibility(element);
                }
            }

            private boolean theSame(PropertyAlias alias1, PropertyAlias alias2) {
                if (alias1 == null || alias2 == null) {
                    return false;
                }
                return getProperty(alias1) == getProperty(alias2) &&
                        getMessage(alias1) == getMessage(alias2) &&
                        alias1.getPart() == alias2.getPart() &&
                        alias1.getQuery() == alias2.getQuery() &&
                        getGlobalType(alias1) == getGlobalType(alias2) &&
                        getGlobalElement(alias1) == getGlobalElement(alias2);
            }

            private CorrelationProperty getProperty(PropertyAlias alias) {
                NamedComponentReference<CorrelationProperty> ref = alias.getPropertyName();

                if (ref == null) {
                    return null;
                }
                return ref.get();
            }

            private Message getMessage(PropertyAlias alias) {
                NamedComponentReference<Message> ref = alias.getMessageType();

                if (ref == null) {
                    return null;
                }
                return ref.get();
            }

            private GlobalType getGlobalType(PropertyAlias alias) {
                NamedComponentReference<GlobalType> ref = alias.getType();

                if (ref == null) {
                    return null;
                }
                return ref.get();
            }

            private GlobalElement getGlobalElement(PropertyAlias alias) {
                NamedComponentReference<GlobalElement> ref = alias.getElement();

                if (ref == null) {
                    return null;
                }
                return ref.get();
            }

            private void collectPropertyAliases(WSDLComponent component, List<PropertyAlias> aliases) {
                if (component instanceof PropertyAlias) {
                    aliases.add((PropertyAlias) component);
                }
                List<WSDLComponent> children = component.getChildren();

                for (WSDLComponent child : children) {
                    collectPropertyAliases(child, aliases);
                }
            }

            private void visitExtensibility(ExtensibilityElement element) {
//out("EXT VISIT: " + element);
                if (!(element instanceof PropertyAlias)) {
                    return;
                }
                // # 90324
                PropertyAlias alias = (PropertyAlias) element;
//out();
//out("PROPERTY ALIAS: " + alias);
//out("Query: " + alias.getQuery());

                if (alias.getQuery() != null) {
//out("1");
                    return;
                }
                // # 125954
                DocumentComponent wrongQuery = getWrongQuery(alias);

                if (wrongQuery != null) {
//out("2");
                    addError("FIX_QUERY_PREFIX", alias); // NOI18N
                    return;
                }
                // property
                NamedComponentReference<CorrelationProperty> ref1 = alias.getPropertyName();

                if (ref1 == null) {
//out("3");
                    return;
                }
                CorrelationProperty property = ref1.get();
//out();
//out("property: " + getName(property));

                if (property == null) {
                    return;
                }
                Component propertyType = getType(property);
//out("!! propertyType: " + getName(propertyType));

                if (propertyType == null) {
                    return;
                }
                // message
                NamedComponentReference<Message> ref2 = alias.getMessageType();

                if (ref2 == null) {
                    return;
                }
                Message message = ref2.get();
//out("message: " + getName(message));

                if (message == null) {
                    return;
                }
                // part
                String partName = alias.getPart();
//out("partName: " + partName);

                if (partName == null) {
                    return;
                }
                Collection<Part> parts = message.getParts();

                if (parts == null) {
                    return;
                }
                Part aliasPart = null;

                for (Part part : parts) {
                    if (partName.equals(part.getName())) {
                        aliasPart = part;
                        break;
                    }
                }
//out("aliasPart: " + getName(aliasPart));
                if (aliasPart == null) {
                    return;
                }
                // type
                Component aliasType = getType(aliasPart);
//out("!! aliasType: " + getName(aliasType));

                if (aliasType == null) {
                    return;
                }
                // check
                if (XAMUtils.getBasedSimpleType(aliasType) != XAMUtils.getBasedSimpleType(propertyType)) {
                    addWarning("FIX_TYPE_IN_PROPERTY_ALIAS", alias, getTypeName(propertyType), getTypeName(aliasType)); // NOI18N
//out("WARNING: " + getTypeName(aliasType) + " "  + getTypeName(propertyType));
                }
//out();
            }
        };
    }

    private Component getType(CorrelationProperty property) {
        NamedComponentReference<GlobalType> ref1 = property.getType();

        if (ref1 != null) {
            GlobalType type = ref1.get();

            if (type != null) {
                return checkTypeOfElement(type);
            }
        }
        NamedComponentReference<GlobalElement> ref2 = property.getElement();

        if (ref2 != null) {
            GlobalElement element = ref2.get();

            if (element != null) {
                return checkTypeOfElement(element);
            }
        }
        return null;
    }

    private DocumentComponent getWrongQuery(PropertyAlias alias) {
        List<WSDLComponent> children = alias.getChildren();

        for (WSDLComponent child : children) {
//out("  child: " + child);
            if (!(child instanceof DocumentComponent)) {
                continue;
            }
            DocumentComponent document = (DocumentComponent) child;
            String tag = document.getPeer().getTagName();
//out("   tag: " + tag);
            if (tag.endsWith("query")) { // NOI18N
                return document;
            }
        }
        return null;
    }
}
