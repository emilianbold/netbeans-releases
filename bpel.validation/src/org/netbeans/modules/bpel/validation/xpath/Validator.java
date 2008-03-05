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
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
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

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.Set;

import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.Condition;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.DeadlineExpression;
import org.netbeans.modules.bpel.model.api.ExpressionLanguageSpec;
import org.netbeans.modules.bpel.model.api.FinalCounterValue;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.RepeatEvery;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.bpel.model.api.support.ExNamespaceContext;
import org.netbeans.modules.bpel.model.api.support.XPathModelFactory;
import org.netbeans.modules.bpel.model.impl.references.SchemaReferenceBuilder;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.XPathModelHelper;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;
import org.netbeans.modules.bpel.model.api.PartReference;
import org.netbeans.modules.bpel.model.api.support.PathValidationContext;
import org.netbeans.modules.bpel.model.api.support.BpelXPathNamespaceContext;
import org.netbeans.modules.bpel.model.api.support.BpelVariableResolver;
import org.netbeans.modules.bpel.model.api.support.BpelXpathExtFunctionResolver;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.bpel.validation.core.BpelValidator;
import org.netbeans.modules.bpel.model.api.support.ValidationVisitor;
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.02.08
 */
public final class Validator extends BpelValidator implements ValidationVisitor {

  @Override
  public void visit(Copy copy) {
//out();
    Component fromType = getTypeOfElement(getType(copy.getFrom()));
//out("Assign: " + ((Named) copy.getParent()).getName());
//out("FROM: " + fromType);
    Component toType = getTypeOfElement(getType(copy.getTo()));
//out();
//out("  TO: " + toType);

    if (fromType == null || toType == null) {
      return;
    }
    String fromName = ((Named) fromType).getName();
    String toName = ((Named) toType).getName();

    if (fromName != null && fromName.equals(toName)) {
      return;
    }
    if (fromName.equals("anyURI") || toName.equals("anyURI")) {
      return;
    }
    if (fromType instanceof GlobalSimpleType && toType instanceof GlobalSimpleType) {
      return;
    }
    if (fromType != toType) {
      addError("FIX_TYPE_IN_COPY", copy, getTypeName(fromType), getTypeName(toType));
    }
  }

  private Component getType(From from) {
    if (from == null) {
      return null;
    }
    Component variableType = getVariableType(from);

    if (variableType != null) {
      Component partType = getPartType(from);

      if (partType == null) {
        return variableType;
      }
      else {
        return partType;
      }
    }
    return checkXPathExpression(from);
  }

  private Component getType(To to) {
    if (to == null) {
      return null;
    }
    Component variableType = getVariableType(to);

    if (variableType != null) {
      Component partType = getPartType(to);

      if (partType == null) {
        return variableType;
      }
      else {
        return partType;
      }
    }
    return checkXPathExpression(to);
  }

  private Component getVariableType(VariableReference reference) {
    BpelReference<VariableDeclaration> ref = reference.getVariable();

    if (ref == null) {
      return null;
    }
    VariableDeclaration declaration = ref.get();

    if (declaration == null) {
      return null;
    }
    // message type
    WSDLReference<Message> wsdlRef = declaration.getMessageType();

    if (wsdlRef != null) {
      Message message = wsdlRef.get();

      if (message != null) {
        Collection<Part> parts = message.getParts();

        if (parts == null || parts.size() == 0) {
          return null;
        }
        if (parts.size() == 1) {
          return getPartType(parts.iterator().next());
        }
        return message;
      }
    }
    // element
    SchemaReference<GlobalElement> elementRef = declaration.getElement();

    if (elementRef != null) {
      GlobalElement element = elementRef.get();

      if (element != null) {
        return element;
      }
    }
    // type
    SchemaReference<GlobalType> typeRef = declaration.getType();

    if (typeRef != null) {
      GlobalType type = typeRef.get();

      if (type != null) {
        return type;
      }
    }
    return null;
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
  public void visit(BooleanExpr expr) {
      checkXPathExpression(expr);
  }

  @Override
  public void visit(Branches branches) {
      checkXPathExpression(branches);
  }

  @Override
  public void visit(Condition condition) {
      checkXPathExpression(condition);
  }
  
  @Override
  public void visit(DeadlineExpression expression) {
      checkXPathExpression(expression);
  }
  
  @Override
  public void visit(FinalCounterValue value) {
      checkXPathExpression(value);
  }
  
  @Override
  public void visit(For fo) {
      checkXPathExpression(fo);
  }
  
  @Override
  public void visit(Query query) {
      checkXPathExpression(query);
  }
  
  @Override
  public void visit(RepeatEvery repeatEvery) {
      checkXPathExpression(repeatEvery);
  }
  
  @Override
  public void visit(StartCounterValue value) {
      checkXPathExpression(value);
  }
  
  @Override
  public void visit(OnAlarmEvent event) {
      myValidatedActivity = event;
  }
  
  @Override
  protected void visit(Activity activity) {
      myValidatedActivity = activity;
  }
  
  private SchemaComponent checkXPathExpression(ContentElement element) {
      String content = element.getContent();
      
      if (content == null) {
          return null;
      }
      content = content.trim();

      if (content.length() == 0) {
          return null;
      }
      String expressionLang = null;
      
      if (element instanceof ExpressionLanguageSpec) {
          expressionLang = ((ExpressionLanguageSpec) element).
                  getExpressionLanguage();
      }
      return checkExpression(expressionLang, content, element);
  }
  
  public SchemaComponent checkExpression(String exprLang, String exprText, final ContentElement element) {
      boolean isXPathExpr = exprLang == null || XPathModelFactory.DEFAULT_EXPR_LANGUAGE.equals(exprLang);

      if ( !isXPathExpr) {
          return null;
      }
      XPathModelHelper helper= XPathModelHelper.getInstance();
      XPathModel model = helper.newXPathModel();
      assert myValidatedActivity != null;

      final PathValidationContext context = new PathValidationContext(model, this, this, myValidatedActivity, element);
      model.setValidationContext(context);

      ExNamespaceContext nsContext = ((BpelEntity)element).getNamespaceContext();
      model.setNamespaceContext(new BpelXPathNamespaceContext(nsContext));

      model.setVariableResolver(new BpelVariableResolver(context, myValidatedActivity));
      model.setExtensionFunctionResolver(new BpelXpathExtFunctionResolver());

      model.setExternalModelResolver(new ExternalModelResolver() {
          public Collection<SchemaModel> getModels(String modelNsUri) {
              BpelModel bpelModel = ((BpelEntity)element).getBpelModel();
              return SchemaReferenceBuilder.getSchemaModels(bpelModel, modelNsUri);
          }

          public Collection<SchemaModel> getVisibleModels() {
              context.addResultItem(Validator.ResultType.ERROR, i18n(Validator.class, "ABSOLUTE_PATH_DISALLOWED")); // NOI18N
              return null;
          }

          public boolean isSchemaVisible(String schemaNamespaceUri) {
              return context.isSchemaImported(schemaNamespaceUri);
          }
      });
      // Checks if the expression contains ";". 
      // If it does, then split it to parts and verifies them separately.
      if (XPathModelFactory.isSplitable(exprText)) {
          context.addResultItem(exprText, Validator.ResultType.ERROR, i18n(Validator.class, "INCOMPLETE_XPATH")); // NOI18N
          String[] partsArr = XPathModelFactory.split(exprText);

          for (String anExprText : partsArr) {
              checkSingleExpr(model, anExprText);
          }
          return null;
      } 
      else {
          return checkSingleExpr(model, exprText);
      }
  }

  private SchemaComponent checkSingleExpr(XPathModel model, String exprText) {
      try {
          XPathExpression xpath = model.parseExpression(exprText);
          model.resolveExtReferences(true);
          return model.getLastSchemaComponent();
      } 
      catch (XPathException e) {
          // Nothing to do here because of the validation context 
          // was specified before and it has to be populated 
          // with a set of problems.
          return null;
      }
  }

  private static void out() {
    System.out.println();
  }

  private void out(Object object) {
    System.out.println("*** " + object); // NOI18N
  }

  private BpelEntity myValidatedActivity; 
}
