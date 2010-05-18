/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.bpel.validation.xpath;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openide.filesystems.FileObject;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathModelHelper;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitorAdapter;

import org.netbeans.modules.xml.time.Duration;
import org.netbeans.modules.xml.time.TimeUtil;
import org.netbeans.modules.bpel.validation.core.BpelValidator;

import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.Condition;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.DeadlineExpression;
import org.netbeans.modules.bpel.model.api.DurationExpression;
import org.netbeans.modules.bpel.model.api.FinalCounterValue;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.RepeatEvery;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.bpel.model.api.PartReference;
import org.netbeans.modules.bpel.model.api.support.PathValidationContext;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.bpel.model.api.support.ValidationVisitor;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.BpelExternalModelResolver;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitorAdaptor;
import org.netbeans.modules.xml.xpath.ext.schema.TypeInheritanceUtil;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;
import org.netbeans.modules.soa.ui.SoaUtil;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.02.08
 */
public final class Validator extends BpelValidator implements ValidationVisitor {

    public static boolean doForceXPathValidation;

    public Set<ResultItem> getResultItems() {
        return getValidationResult();
    }

    @Override
    protected SimpleBpelModelVisitor getVisitor() {
        return new SimpleBpelModelVisitorAdaptor() {

            @Override
            public void visit(Copy copy) {
                //   # 158235              # 168169
                if ( !doForceXPathValidation && !isComplete()) {
                    return;
                }
//out("CHECK XPATH COPY");
//out();
//out("Assign: " + ((Named) copy.getParent()).getName());
                Component fromType = checkTypeOfElement(getType(copy.getFrom()));
//out("FROM: " + fromType);
                Component toType = checkTypeOfElement(getType(copy.getTo()));
//out("  TO: " + toType);

                if ( !(fromType instanceof Named) || !(toType instanceof Named)) {
                    return;
                }
                String fromName = ((Named) fromType).getName();
//out("   from name: " + fromName);
                String toName = ((Named) toType).getName();
//out("     to name: " + toName);

                if (fromName == null || toName == null) {
                    return;
                }
                if (fromName.equals(toName)) {
                    return;
                }
                if (fromName.equals("anyType") || toName.equals("anyType")) { // NOI18N
                    return;
                }
                // # 135489
                if (fromName.startsWith("nonNegative") && toName.startsWith("negative")) { // NOI18N
                    addError("FIX_NonNegative_Negative_Copy", copy); // NOI18N
                    return;
                }
                if (fromName.startsWith("positive") && toName.startsWith("negative")) { // NOI18N
                    addError("FIX_Positive_Negative_Copy", copy); // NOI18N
                    return;
                }
                if (fromName.startsWith("positive") && toName.startsWith("nonPositive")) { // NOI18N
                    addError("FIX_Positive_NonPositive_Copy", copy); // NOI18N
                    return;
                }
                // # 135489
                if (fromName.startsWith("negative") && toName.startsWith("nonNegative")) { // NOI18N
                    addError("FIX_Negative_NonNegative_Copy", copy); // NOI18N
                    return;
                }
                if (fromName.startsWith("negative") && toName.startsWith("positive")) { // NOI18N
                    addError("FIX_Negative_Positive_Copy", copy); // NOI18N
                    return;
                }
                if (fromName.startsWith("nonPositive") && toName.startsWith("positive")) { // NOI18N
                    addError("FIX_NonPositive_Positive_Copy", copy); // NOI18N
                    return;
                }
                if (fromName.startsWith("nonPositive") && toName.startsWith("nonNegative")) { // NOI18N
                    addWarning("FIX_NonPositive_NonNegative_Copy", copy); // NOI18N
                    return;
                }
                if (fromName.startsWith("nonNegative") && toName.startsWith("nonPositive")) { // NOI18N
                    addWarning("FIX_NonNegative_NonPositive_Copy", copy); // NOI18N
                    return;
                }
                // # 135489
                if (isNumeric(fromName) && isNumeric(toName)) {
                    return;
                }
                Component fType = XAMUtils.getBasedSimpleType(fromType);
                Component tType = XAMUtils.getBasedSimpleType(toType);

                if (fType == tType) {
                    return;
                }
                if (fType instanceof GlobalType && tType instanceof GlobalType) {
                    if (TypeInheritanceUtil.areTypesDerived((GlobalType) tType, (GlobalType) fType, getDerivations(copy))) {
                        // Types are derived
                        return;
                    }
                }
                String fTypeName = getTypeName(fType);
                String tTypeName = getTypeName(tType);
//out("  from based: " + fTypeName);
//out("    to based: " + tTypeName);

                if (fTypeName.equals("string") && tTypeName.equals("time")) { // NOI18N
                    // # 135079
                    addWarning("FIX_Time_in_copy", copy); // NOI18N
                }
                else {
                    addWarning("FIX_Type_in_copy", copy, fTypeName, tTypeName); // NOI18N
                }
            }

            private boolean isNumeric(String value) {
                return
                    value.equals("byte") || // NOI18N
                    value.equals("decimal") || // NOI18N
                    value.equals("double") || // NOI18N
                    value.equals("float") || // NOI18N
                    value.equals("int") || // NOI18N
                    value.equals("integer") || // NOI18N
                    value.equals("long") || // NOI18N
                    value.equals("negativeInteger") || // NOI18N
                    value.equals("nonNegativeInteger") || // NOI18N
                    value.equals("nonPositiveInteger") || // NOI18N
                    value.equals("positiveInteger") || // NOI18N
                    value.equals("short") || // NOI18N
                    value.equals("unsignedByte") || // NOI18N
                    value.equals("unsignedInt") || // NOI18N
                    value.equals("unsignedLong") || // NOI18N
                    value.equals("unsignedShort"); // NOI18N
            }

            @Override
            public void visit(To to) {
                // # 125525
                checkPartnerLink(to);
                // # 131658
                checkVariable(to);
                // # 162134
                checkXPath(to);
            }
            
            @Override
            public void visit(From from) {
                checkXPath(from);
            }

            private void checkPartnerLink(To to) {
                BpelReference<PartnerLink> ref = to.getPartnerLink();

                if (ref == null) {
                    return;
                }
                PartnerLink partnerLink = ref.get();

                if (partnerLink == null) {
                    return;
                }
                WSDLReference<Role> ref1 = partnerLink.getPartnerRole();

                if (ref1 == null || ref1.get() == null) {
                    addError("FIX_To_PartnerLink", to); // NOI18N
                }
            }

            private void checkVariable(To to) {
                String value = to.getContent();
//out();
//out("to: " + value);
//out();
                if (value == null) {
                    return;
                }
                value = value.trim();

                if (value.length() == 0) {
                    return;
                }
                if (!value.startsWith("$")) { // NOI18N
                    addError("FIX_SA00033", to, value); // NOI18N
                }
            }

            private Component getType(From from) {
//out();
//out("get type: " + from);
                if (from == null) {
                    return null;
                }
                Component variableType = getVariableReferenceType(from);
//out("  var: " + variableType);

                if (variableType != null) {
                    Component partType = getPartType(from);

                    if (partType == null) {
                        return variableType;
                    } else {
                        return partType;
                    }
                }
//out("  see xpath FROM: " + getXPathType(from));
                return getXPathType(from);
            }

            private Component getType(To to) {
                if (to == null) {
                    return null;
                }
                Component variableType = getVariableReferenceType(to);

                if (variableType != null) {
                    Component partType = getPartType(to);

                    if (partType == null) {
                        return variableType;
                    } else {
                        return partType;
                    }
                }
                return getXPathType(to);
            }

            private Component getVariableReferenceType(VariableReference reference) {
                BpelReference<VariableDeclaration> ref = reference.getVariable();

                if (ref == null) {
                    return null;
                }
                return getVariableDeclarationType(ref.get());
            }

            private SchemaComponent getPartType(PartReference reference) {
//out("get part type");
                WSDLReference<Part> ref = reference.getPart();

                if (ref == null) {
                    return null;
                }
                return getPartType(ref.get());
            }

            private SchemaComponent getPartType(Part part) {
                if (part == null) {
                    return null;
                }
                // element
                NamedComponentReference<GlobalElement> elementRef = part.getElement();

                if (elementRef != null) {
                    GlobalElement element = elementRef.get();

                    if (element != null) {
                        return element;
                    }
                }
                // type
                NamedComponentReference<GlobalType> typeRef = part.getType();

                if (typeRef != null) {
                    GlobalType type = typeRef.get();

                    if (type != null) {
                        return type;
                    }
                }
                return null;
            }

            @Override
            public void visit(BooleanExpr bool) {
                checkXPath(bool);
            }

            @Override
            public void visit(Branches branches) {
                checkXPath(branches);
            }

            @Override
            public void visit(Condition condition) {
                checkXPath(condition);
            }

            @Override
            public void visit(DeadlineExpression deadline) {
                checkXPath(deadline);
                checkDeadline(deadline);
            }

            @Override
            public void visit(FinalCounterValue counter) {
                checkXPath(counter);
            }

            @Override
            public void visit(For _for) {
                checkXPath(_for);
                checkDuration(_for);
            }

            @Override
            public void visit(RepeatEvery repeatEvery) {
                checkXPath(repeatEvery);
                checkDuration(repeatEvery);
                // # 117688
                checkNegative(repeatEvery);
            }

            private void checkNegative(RepeatEvery repeatEvery) {
                String value = repeatEvery.getContent();

                try {
                    Duration duration = TimeUtil.parseDuration(value, true);

                    if (duration == null) {
                        return;
                    }
                    if (duration.isNegative() || duration.isZero()) {
                        addError("FIX_Negative_RepeatEvery", repeatEvery); // NOI18N
                    }
                } catch (IllegalArgumentException e) {
                    return;
                }
            }

            @Override
            public void visit(Query query) {
                checkXPath(query);
            }

            @Override
            public void visit(StartCounterValue counter) {
                checkXPath(counter);
            }

            private SchemaComponent getXPathType(ContentElement content) {
                return Utils.getXPathExpressionType(content, new PathValidationContext(Validator.this, null, content));
            }

            private void checkXPath(ContentElement content) {
                //   # 158235                   # 168169
                if ( !doForceXPathValidation && !isComplete()) {
                    return;
                }
                checkDoXslTransform(content);
//out("CHECK XPATH: " + content.getContent());
                Utils.checkXPathExpression(content, new PathValidationContext(Validator.this, Validator.this, content));
            }

            // # 173412
            private void checkDeadline(DeadlineExpression deadline) {
                String value = deadline.getContent();

                try {
                    TimeUtil.parseDeadline(value, true);
                }
                catch (IllegalArgumentException e) {
                    addError("FIX_Deadline", deadline, e.getMessage()); // NOI18N
                }
            }

            // # 117689
            private void checkDuration(DurationExpression duration) {
                String value = duration.getContent();

                try {
                    TimeUtil.parseDuration(value, true);
                }
                catch (IllegalArgumentException e) {
                    addError("FIX_Duration", duration, e.getMessage()); // NOI18N
                }
            }

            // # 163856
            private void checkDoXslTransform(ContentElement element) {
                String content = element.getContent();

                if (content == null || content.length() == 0) {
                    return;
                }
                XPathExpression expression;

                try {
//out("XPATH MODEL: " + content);
                    XPathModel model = XPathModelHelper.getInstance().newXPathModel();
                    expression = model.parseExpression(content);
                }
                catch (XPathException e) {
                    return;
                }
                final BpelEntity entity = (BpelEntity) element;

                expression.accept(new XPathVisitorAdapter() {
                    public void visit(XPathExtensionFunction function) {
//out("see function: " + function);
                        String name = function.getName().getLocalPart();

                        if ( !DO_XSL_TRANSFORM.equals(name)) {
                            return;
                        }
//out("            : " + name);
                        List<XPathExpression> children = function.getChildren();
//out("            : " + children.size());

                        // # 175478
                        if (children.size() % 2 != 0) {
                            addError("FIX_SA00040", entity); // NOI18N
                            return;
                        }
//out();
//out("            : " + name);
                        XPathExpression child = children.get(0);
//out("            : " + child.getClass().getName());
                        if ( !(child instanceof XPathStringLiteral)) {
                            addError("FIX_doXslTransform_Parameter_String", entity); // NOI18N
                            return;
                        }
                        String parameter = ((XPathStringLiteral) child).getValue();
//out("            : " + parameter);
                        if (parameter == null) {
                            addError("FIX_doXslTransform_Parameter_Must_Be", entity); // NOI18N
                            return;
                        }
                        if ( !parameter.startsWith(URN_STYLESHEETS)) {
                            Validator.this.addError("FIX_doXslTransform_Parameter_Format", entity); // NOI18N
                            return;
                        }
                        String file = parameter.substring(URN_STYLESHEETS.length());

                        if (file == null) {
                            Validator.this.addError("FIX_doXslTransform_Parameter_File", entity, file); // NOI18N
                            return;
                        }
                        FileObject bpel = SoaUtil.getFileObjectByModel(entity.getModel());

                        if (bpel == null) {
                            return;
                        }
//System.out.println("bpel: " + f);
                        FileObject xsl = bpel.getParent().getFileObject(file);

                        if (xsl == null) {
                            Validator.this.addError("FIX_doXslTransform_Parameter_File", entity, file); // NOI18N
                            return;
                        }
                    }
                });
            }
        };
    }

    private Map<GlobalType, GlobalType> getDerivations(BpelEntity entity) {
        if (myDerivations != null) {
            return myDerivations;
        }
        if (entity == null) {
            return null;
        }
        ExternalModelResolver resolver = new BpelExternalModelResolver(entity.getBpelModel());
        myDerivations = TypeInheritanceUtil.populateDerivationMap(resolver, false);

        return myDerivations;
    }

    private Map<GlobalType, GlobalType> myDerivations;

    private static final String URN_STYLESHEETS = "urn:stylesheets:"; // NOI18N
    private static final String DO_XSL_TRANSFORM = "doXslTransform"; // NOI18N
}
