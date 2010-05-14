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
package org.netbeans.modules.bpel.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Variable;

import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentDefinition;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SequenceDefinition;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

import org.netbeans.modules.xml.xpath.AbstractXPathModelHelper;
import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.XPathException;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xml.xpath.XPathModel;
import org.netbeans.modules.xml.xpath.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.XPathVariableReference;
import org.netbeans.modules.xml.xpath.visitor.AbstractXPathVisitor;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.06.30
 */
final class XPath extends AbstractXPathVisitor {

    XPath(List<Component> usage, Named target, String oldName) {
        myOldName = oldName;
        myTarget = target;
        myUsage = usage;
    }

    void find(String content, Component component) {
//out();
//out("[XPATH] find: '" + content + "' " + component);
        doAction(content, component, false);
//out();
//out();
    }

    String rename(String content, Component component) {
//out();
//out("[XPATH] rename");
        return doAction(content, component, true);
    }

    private String doAction(String content, Component component, boolean doRename) {
        if (content == null || content.length() == 0) {
            return content;
        }
//out();
//out();
//out("[XPATH] do action: '" + content + "' " + component);
        XPathModel model = AbstractXPathModelHelper.getInstance().newXPathModel();
        myVisitedComplexType = new ArrayList<ComplexType>();
        myExpressions = new ArrayList<XPathExpression>();
        myComponent = component;
        myDoRename = doRename;

        try {
            XPathExpression expression = model.parseExpression(content);
            expression.accept(this);
//out();
//out();
            if (myDoRename) {
//out("  do rename: " + myTarget.getName());
                rename(myTarget.getName());
//out("  new content: " + expression.getExpressionString());
            }
//out("  new content: " + expression.getExpressionString());
            return expression.getExpressionString();
        }
        catch (XPathException e) {
//out("  !!!! exception: " + e.getMessage());
            return content;
        }
    }

    private void rename(String newName) {
//out();
//out("----------------------------------");
//out();
//out("To rename: " + newName + " " + myExpressions.size());

        for (XPathExpression expression : myExpressions) {
//out("See: " + expression);
            if (expression instanceof LocationStep) {
                LocationStep step = (LocationStep) expression;
                step.setNodeTest(new StepNodeNameTest(createName(step.getString(), newName)));
            }
            else if (expression instanceof XPathVariableReference) {
                XPathVariableReference reference = (XPathVariableReference) expression;
                reference.setVariableName(createName(reference.getVariableName(), newName));
            }
        }
//out();
//out("----------------------------------");
//out();
    }

    private String createName(String oldName, String newName) {
        int k = oldName.indexOf(":"); // NOI18N

        if (k == -1) {
            return newName;
        }
        return oldName.substring(0, k + 1) + newName;
    }

    private String createName(QName qName, String part) {
        String name = qName.getLocalPart();
        int k = name.indexOf("."); // NOI18N

        if (k == -1) {
            return name;
        }
        return name.substring(0, k + 1) + part;
    }

    @Override
    public void visit(XPathExpressionPath expressionPath) {
//out();
//out("EXPRESION: " + expressionPath);
        XPathExpression rootExpression = expressionPath.getRootExpression();
        myVariable = null;
        myVariableReference = null;

        if (!expressionPath.equals(rootExpression)) {
//out("     root: " + rootExpression);
            rootExpression.accept(this);
        }
        if (myVariable == null) {
//out("Variable is not found");
            return;
        }
//out("VARIABLE: " + myVariable.getName());
//out("    PART: " + myPartName);

        if (myVariable == myTarget) {
//out("  variable is target: " + myVariable.getName());
            return;
        }
        LocationStep[] locations = expressionPath.getSteps();
        List<LocationStep> steps = new ArrayList<LocationStep>();
        steps.add(null); // first step is fake

        if (locations == null) {
            return;
        }
        for (LocationStep location : locations) {
            steps.add(location);
        }
        visit(steps);
        visit(locations);
    }

    private void visit(LocationStep[] locations) {
        if (locations == null) {
            return;
        }
        for (LocationStep location : locations) {
            location.accept(this);
        }
    }

    private void visit(List<LocationStep> steps) {
//visit("VISIT STEPS: " + steps);
        visitReference(myVariable.getMessageType(), createList(steps), ""); // NOI18N
        visitReference(myVariable.getElement(), createList(steps), ""); // NOI18N
        visitReference(myVariable.getType(), createList(steps), ""); // NOI18N
    }

    private List<LocationStep> createList(List<LocationStep> steps) {
        List<LocationStep> list = new ArrayList<LocationStep>();

        for (LocationStep step : steps) {
            list.add(step);
        }
        return list;
    }

    private void visitPart(Part part, List<LocationStep> steps) {
        visitReference(part.getElement(), createList(steps), ""); // NOI18N
        visitReference(part.getType(), createList(steps), ""); // NOI18N
    }

    private void visitReference(NamedComponentReference reference, List<LocationStep> steps, String indent) {
        if (reference != null) {
            visitComponent(reference.get(), steps, indent);
        }
    }

    private void visitComponent(Object object, List<LocationStep> steps, String indent) {
        if (object instanceof ComplexType) {
//out("  visit complex");
            visitComplexType((ComplexType) object, steps, indent);
        }
        else if (object instanceof SimpleType) {
//out("  visit simple");
            visitSimpleType((SimpleType) object, steps, indent);
        }
        else if (object instanceof Element) {
            visitElement((Element) object, steps, indent);
        }
        else if (object instanceof Message) {
            visitMessage((Message) object, steps, indent);
        }
        else {
            if (object == null) {
                return;
            }
//out(indent + " unknown !!!: " + object);
        }
    }

    private void visitMessage(Message message, List<LocationStep> steps, String indent) {
        for (Part part : message.getParts()) {
//out();
//out("  see part: " + part.getName() + "  " + part.getClass().getName());
//out("    myPart: " + myPartName);
//out("  myTarget: " + myTarget.getName());

            if (part == myTarget && checkName()) {
                addItem();
//out("add item after: " + myDoRename);

                if (myDoRename) {
//out("ADD to expressions: " + myVariableReference);
                    myExpressions.add(myVariableReference);
                }
            }
            if (myPartName.equals(part.getName())) {
//out("==== PART: " + myPartName);
//out();
                visitPart(part, createList(steps));
            }
        }
    }

    private boolean checkName() {
        if (myDoRename) {
            return myPartName.equals(myOldName);
        }
        else {
            return myPartName.equals(myTarget.getName());
        }
    }

    private void visitElement(Element element, List<LocationStep> steps, String indent) {
//out(indent + "ELEMENT: " + Util.getName(element));
        if (checkUsages(element, steps, false)) {
            return;
        }
        myTypeReference = null;
        element.accept(new DeepSchemaVisitor() {

            @Override
            public void visit(ComplexExtension extension) {
                myTypeReference = extension.getBase();
            }
        });
        visitReference(myTypeReference, steps, indent + INDENT);

        if (element instanceof TypeContainer) {
            TypeContainer container = (TypeContainer) element;
            visitReference(container.getType(), createList(steps), indent + INDENT);
            visitComponent(container.getInlineType(), createList(steps), indent + INDENT);
        }
    }

    private void visitComplexType(ComplexType type, List<LocationStep> steps, String indent) {
//out(indent + "COMPLEX.TYPE: " + Util.getName(type));
        if (myVisitedComplexType.contains(type)) {
            return;
        }
        myVisitedComplexType.add(type);

        if (checkUsages(type, steps, true)) {
            return;
        }
        ComplexTypeDefinition definition = type.getDefinition();

        if (definition instanceof Sequence) {
            visitSequence((Sequence) definition, steps, indent + INDENT);
        }
        else if (definition instanceof Choice) {
            visitChoice((Choice) definition, steps, indent);
        }
        else if (definition instanceof ComplexContent) {
            visitComplexContent((ComplexContent) definition, steps, indent);
        }
//      else {
//out(indent + "unknown !!: " + definition);
//     }
        Collection<LocalAttribute> attributes = type.getLocalAttributes();

        for (LocalAttribute attribute : attributes) {
            visitAttribute(attribute, steps, indent);
        }
    }

    private void visitAttribute(LocalAttribute attribute, List<LocationStep> steps, String indent) {
//out(indent + "ATTRIBUTE: " + Util.getName(attribute));
        checkUsages(attribute, steps, true);
    }

    private void visitComplexContent(ComplexContent content, List<LocationStep> steps, String indent) {
        ComplexContentDefinition definition = content.getLocalDefinition();

        if (definition instanceof ComplexExtension) {
            visitReference(((ComplexExtension) definition).getBase(), steps, indent);
        }
    }

    private void visitChoice(Choice choice, List<LocationStep> steps, String indent) {
//out(indent + " [choice] ===================");
        for (Choice _choice : choice.getChoices()) {
            visitChoice(_choice, createList(steps), indent + INDENT);
        }
        for (Sequence sequence : choice.getSequences()) {
            visitSequence(sequence, createList(steps), indent + INDENT);
        }
        for (LocalElement element : choice.getLocalElements()) {
            visitElement(element, createList(steps), indent + INDENT);
        }
        for (ElementReference reference : choice.getElementReferences()) {
            visitReference(reference.getRef(), createList(steps), indent + INDENT);
        }
//out(indent + " [===========================");
    }

    private void visitSequence(Sequence sequence, List<LocationStep> steps, String indent) {
//out(indent + " [sequnce] ==================");
        List<SequenceDefinition> content = sequence.getContent();

        if (content == null) {
            return;
        }
        for (SequenceDefinition definition : content) {
////out(indent + "      see: " + Util.getName(definition));
            if (definition instanceof Element) {
                visitElement((Element) definition, createList(steps), indent + INDENT);
            }
            else if (definition instanceof ComplexType) {
                visitComplexType((ComplexType) definition, createList(steps), indent + INDENT);
            }
            else if (definition instanceof SimpleType) {
                visitSimpleType((SimpleType) definition, createList(steps), indent + INDENT);
            }
            else if (definition instanceof ElementReference) {
                visitReference(((ElementReference) definition).getRef(), createList(steps), indent + INDENT);
            }
            else {
//out(indent + "    error !!!: " + definition);
                return;
            }
        }
//out(indent + " [===========================");
    }

    private void visitSimpleType(SimpleType type, List<LocationStep> steps, String indent) {
//out(indent + "SIMPLE.TYPE: " + Util.getName(type));
        checkUsages(type, steps, true);
    }

    @Override
    public void visit(XPathVariableReference reference) {
        QName qName = reference.getVariableName();
//out("VAR REFER: " + qName);
        String name = qName.getLocalPart();
        String part = ""; // NOI18N
        int k = name.indexOf("."); // NOI18N

        if (k != -1) {
            part = name.substring(k + 1);
            name = name.substring(0, k);
        }
        if (!(myComponent instanceof BpelEntity)) {
            return;
        }
        Variable[] variables = ((BpelModel) ((BpelEntity) myComponent).getModel()).getProcess().getVariableContainer().getVariables();

        if (variables == null) {
            return;
        }
        for (Variable variable : variables) {
//out("  see: " + variable.getName());

            if (variable.getName().equals(name)) {
//out("   this.");
                myVariable = variable;
                myVariableReference = reference;
                myPartName = part;

                if (myVariable == myTarget) {
//out("  add variable: " + myVariable.getName());
                    addItem();
                }
                break;
            }
        }
    }

    @Override
    public void visit(LocationStep locationStep) {
//out("=== LOCATION STEP: " + locationStep);
        XPathPredicateExpression[] predicates = locationStep.getPredicates();
//out("  predicates: " + predicates);

        if (predicates == null) {
            return;
        }
        for (XPathPredicateExpression predicate : predicates) {
            predicate.accept(this);
        }
    }

    @Override
    public void visit(XPathCoreFunction coreFunction) {
//out("CORE FUNC: " + coreFunction);
        visitChildren(coreFunction);
    }

    @Override
    public void visit(XPathCoreOperation coreOperation) {
//out("CORE OPER: " + coreOperation);
        visitChildren(coreOperation);
    }

    @Override
    public void visit(XPathExtensionFunction extensionFunction) {
//out("EXT  FUNC: " + extensionFunction);
        visitChildren(extensionFunction);
    }

    @Override
    public void visit(XPathLocationPath locationPath) {
//out("LOCAL PATH: " + locationPath);
        visit(locationPath.getSteps());
    }

    private boolean checkUsages(Component component, List<LocationStep> steps, boolean nextStep) {
        if (component == null) {
            return false;
        }
//out();
//out("  chk compnt: "+ Util.getName(component));
//out("      target: "+ myTarget.getName());
//out("        step: "+ (steps.size() == 0 ? null : steps.get(0)));

        if (steps.size() == 0) {
//out("        size: 0");
//out("      return: false");
//out();
            return false;
        }
        LocationStep step = steps.get(0);

        if (myTarget.equals(component)) {
//out();
//out("name: " + myOldName);
//out("step: " + step.getString());
            if (step != null && equalsIgnorePrefixAndAmpersand(myOldName, step.getString())) {
                addItem();

                if (myDoRename) {
                    myExpressions.add(step);
//out("!! ==== STEP: " + step.getString());
                }
//out("      return: true");
//out();
                return true;
            }
        }
        else {
            if (nextStep) {
//out("      remove: " + step);
                steps.remove(0);
            }
        }
//out("      return: false");
//out();
        return false;
    }

    private boolean equalsIgnorePrefixAndAmpersand(String name, String step) {
        if (name == null || step == null) {
            return false;
        }
        if (step.startsWith("@")) { // NOI18N
            step = step.substring(1);
        }
        int k = step.indexOf(":"); // NOI18N

        if (k == -1) {
            return name.equals(step);
        }
        return name.equals(step.substring(k + 1));
    }

    private void addItem() {
        if (myUsage != null) {
            myUsage.add(myComponent);
//out("!! ===== ADD: " + Util.getName(myComponent));
//dump();
        }
    }

    private Named myTarget;
    private String myOldName;
    private String myPartName;
    private boolean myDoRename;
    private Variable myVariable;
    private Component myComponent;
    private List<Component> myUsage;
    private List<XPathExpression> myExpressions;
    private List<ComplexType> myVisitedComplexType;
    private XPathVariableReference myVariableReference;
    private NamedComponentReference<GlobalType> myTypeReference;
    private static final String INDENT = "  "; // NOI18N
}
