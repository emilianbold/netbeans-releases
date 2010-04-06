/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.validation.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.util.XmlUtil;
import org.w3c.dom.Attr;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.03
 */
public abstract class Validator implements org.netbeans.modules.xml.xam.spi.Validator {

    public abstract ValidationResult validate(Model model, Validation validation, ValidationType type);

    protected void init() {}

    protected final String getDisplayName() {
        String name = getName();
        StringBuilder spaces = new StringBuilder();

        for (int i = name.length(); i < MAX_LEN; i++) {
            spaces.append(" "); // NOI18N
        }
        return "Validator: " + name + spaces; // NOI18N
    }

    public String getName() {
        return getClass().getName();
    }

    protected final ValidationResult createValidationResult(Model model) {
        return new ValidationResult(getValidationResult(), Collections.singleton(model));
    }

    protected final Set<ResultItem> getValidationResult() {
        return myValidationResult;
    }

    protected final void validate(Model model) {
        myValidation.validate(model, myType);
    }

    protected final void init(Validation validation, ValidationType type) {
        myType = type;
        myValidation = validation;
        myValidationResult = new HashSet<ResultItem>();
        init();
    }

    protected final void addError(String key, Component component) {
//out("add error: " + key + " " + component);
        addMessage(i18n(getClass(), key), ResultType.ERROR, component);
    }

    protected final void addError(String key, Component component, String param) {
        addMessage(i18n(getClass(), key, param), ResultType.ERROR, component);
    }

    protected final void addError(String key, Component component, String param1, String param2) {
//out("add error: " + key + " " + param1 + " " + param2);
        addMessage(i18n(getClass(), key, param1, param2), ResultType.ERROR, component);
    }

    protected final void addError(String key, Component component, String param1, String param2, String param3) {
        addMessage(i18n(getClass(), key, param1, param2, param3), ResultType.ERROR, component);
    }

    protected final void addWarning(String key, Component component) {
        addMessage(i18n(getClass(), key), ResultType.WARNING, component);
    }

    protected final void addWarning(String key, Component component, String param) {
        addMessage(i18n(getClass(), key, param), ResultType.WARNING, component);
    }

    protected final void addWarning(String key, Component component, String param1, String param2) {
        addMessage(i18n(getClass(), key, param1, param2), ResultType.WARNING, component);
    }

    protected final void addWarning(String key, Component component, String param1, String param2, String param3) {
        addMessage(i18n(getClass(), key, param1, param2, param3), ResultType.WARNING, component);
    }

    protected final void addMessage(String message, ResultType type, Component component) {
        addQuickFixable(component, type, message, null);
    }

    protected final void checkReference(Object object, Reference<? extends Component> reference) {
//out("reference: " + reference);
        if (!(object instanceof Component)) {
            return;
        }
        if (reference != null && !reference.isBroken()) {
            return;
        }
        String name;
        Component component = (Component) object;

        if (component instanceof DocumentComponent) {
            name = ((DocumentComponent) component).getPeer().getLocalName();
        } else {
            name = ""; // NOI18N
        }
        addMessage(i18n(Validator.class, "FIX_Reference", name), ResultType.ERROR, component); // NOI18N
    }

    protected final void addQuickFix(String key, Component component, String param1, String param2, QuickFix quickFix) {
        addQuickFixable(component, ResultType.ERROR, i18n(getClass(), key, param1, param2), quickFix);
    }

    private void addQuickFixable(Component component, ResultType type, String message, QuickFix quickFix) {
        myValidationResult.add(new QuickFixable(this, type, component, message, quickFix));
    }

    protected final boolean isComplete() {
        return myType == ValidationType.COMPLETE;
    }

    protected final String getTypeName(Component component) {
        if (component == null) {
            return "n/a"; // NOI18N
        }
        if (component instanceof Named) {
            return ((Named) component).getName();
        }
        return component.toString();
    }

    protected final String getName(Object component) {
        return XmlUtil.getComponentName(component);
    }

    protected final Component getType(Part part) {
        NamedComponentReference<GlobalType> ref1 = part.getType();

        if (ref1 != null) {
            GlobalType type = ref1.get();

            if (type != null) {
                return checkTypeOfElement(type);
            }
        }
        NamedComponentReference<GlobalElement> ref2 = part.getElement();

        if (ref2 != null) {
            GlobalElement element = ref2.get();

            if (element != null) {
                return checkTypeOfElement(element);
            }
        }
        return null;
    }

    protected final Component checkTypeOfElement(Component component) {
//out();
//out("CHECK TYPE: " + component);
        Component type = getTypeOfElement(component);

        if (type != null) {
            return type;
        }
        return component;
    }

    protected final Component getTypeOfElement(Component component) {
//out();
//out("GET TYPE: " + component);
        if (component instanceof TypeContainer) {
//out("1");
            TypeContainer typeContainer = (TypeContainer) component;
            NamedComponentReference<? extends GlobalType> ref = typeContainer.getType();

            if (ref != null && ref.get() != null) {
                return ref.get();
            }
            LocalType inline = typeContainer.getInlineType();

            if (inline != null) {
                return inline;
            }
        }
//out("3");
        if (component instanceof DocumentComponent && component instanceof SchemaComponent) {
            DocumentComponent document = (DocumentComponent) component;
            Attr attr = document.getPeer().getAttributeNode("type"); // NOI18N
            if (attr != null) {
                String typeName = attr.getValue();
                typeName = removePrefix(typeName);
                return findType(typeName, (SchemaComponent) component);
            }
        }
        return null;
    }

    private GlobalType findType(String typeName, SchemaComponent component) {
//out("= findType: " + typeName);
        if (typeName == null || typeName.equals("")) { // NOI18N
            return null;
        }
        SchemaModel model = component.getModel();
        Collection<Schema> schemas = model.findSchemas("http://www.w3.org/2001/XMLSchema"); // NOI18N
        GlobalType type = null;

        for (Schema schema : schemas) {
            type = findType(typeName, schema);

            if (type != null) {
                return type;
            }
        }
        Schema schema = model.getSchema();

        if (schema == null) {
            return null;
        }
        return findType(typeName, schema);
    }

    private GlobalType findType(final String typeName, Schema schema) {
//out();
//out("= in schema: " + schema.getTargetNamespace());
        myGlobalType = null;

        schema.accept(new DeepSchemaVisitor() {

            @Override
            public void visit(GlobalSimpleType type) {
//out("  see GLOBAL Simple TYPE : " + type.getName());
                if (typeName.equals(type.getName())) {
//out("!!!=== FOUND GLOBAL Simple TYPE ==== : " + type.getName());
                    myGlobalType = type;
                }
            }

            @Override
            public void visit(GlobalComplexType type) {
//out(" see GLOBAL Complex TYPE : " + type.getName());
                if (typeName.equals(type.getName())) {
//out("!!!=== FOUND GLOBAL Complex TYPE ==== : " + type.getName());
                    myGlobalType = type;
                }
            }
        });

        return myGlobalType;
    }

    private String removePrefix(String value) {
        if (value == null) {
            return null;
        }
        int k = value.indexOf(":");

        if (k == -1) {
            return value;
        }
        return value.substring(k + 1);
    }

    private void out() {
        System.out.println();
    }

    private void out(Object object) {
        System.out.println("*** " + object); // NOI18N
    }

    private Validation myValidation;
    private ValidationType myType;
    private GlobalType myGlobalType;
    private Set<ResultItem> myValidationResult;
    private static final int MAX_LEN = 57;
}
