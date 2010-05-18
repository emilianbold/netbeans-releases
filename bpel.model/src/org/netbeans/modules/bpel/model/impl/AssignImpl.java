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
package org.netbeans.modules.bpel.model.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.AssignChild;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.ExtensionAssignOperation;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.ext.Extensions;
import org.netbeans.modules.bpel.model.ext.js.api.Expression;
import org.netbeans.modules.bpel.model.ext.js.api.ExpressionLanguage;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;

// TODO ref
public class AssignImpl extends ActivityImpl implements Assign {

    private static Logger LOGGER = Logger.getLogger(AssignImpl.class.getName());

    public AssignImpl(BpelBuilderImpl builder, boolean isJavaScript) {
        super(builder, BpelElements.ASSIGN.getName());

        if ( !isJavaScript) {
            return;
        }
        // JavaScript
        try {
            setName(getUniqueName(builder.getModel()));
        } catch (VetoException e) {
            e.printStackTrace();
        }
        createEmptyJavaScript();
    }

    public AssignImpl(BpelModelImpl model, Element e) {
        super(model, e);
    }

    public AssignImpl(BpelBuilderImpl builder, String name) {
        super(builder, name);
    }

    public String getJavaScript() {
        BpelModel bpelModel = getBpelModel();
        boolean wasInTransaction = bpelModel.isIntransaction();

        if (!wasInTransaction) {
            bpelModel.startTransaction();
        }
        try {
            Expression expression = getExpression();

            if (expression == null) {
                return null;
            }
            return expression.getCDataContent();
        }
        finally {
            if (!wasInTransaction) {
                bpelModel.endTransaction();
            }
        }
//      Element expression = getExpression();
//
//      if (expression == null) {
//          return null;
//      }
//      CDATASection cdata = getCDATASection(expression.getChildNodes());
//
//      if (cdata == null) {
//          return null;
//      }
//      return cdata.getData();
    }

    public void setJavaScript(String value) {
        BpelModel bpelModel = getBpelModel();
        boolean wasInTransaction = bpelModel.isIntransaction();
  
        if (!wasInTransaction) {
            bpelModel.startTransaction();
        }
        try {
            Expression expr = getExpression();

            if (expr == null) {
                ExtensionAssignOperation extAssignOp = null;
                List<ExtensionAssignOperation> extAssignOps = getChildren(ExtensionAssignOperation.class);
                boolean extAssignOpExist = !(extAssignOps.size() < 1);

                if ( !extAssignOpExist) {
                    extAssignOp = getBpelModel().getBuilder().createExtensionAssignOperation();
//                  addAssignChild(extAssignOp);
//                  extAssignOps = getChildren(ExtensionAssignOperation.class);
//                  assert extAssignOps != null && extAssignOps.size() > 0;
                }
//              extAssignOp = extAssignOps.get(0);
                extAssignOp.getNamespaceContext().addNamespace(Extensions.SUN_JS_EXT_URI);
                expr = getBpelModel().getBuilder().createExtensionEntity(Expression.class);
                extAssignOp.addExtensionEntity(Expression.class, expr);
                List<Expression> exprs = extAssignOp.getChildren(Expression.class);
                assert exprs != null && exprs.size() > 0;
                expr = exprs.get(0);

                if (!extAssignOpExist) {
                    addAssignChild(extAssignOp);
                }
                expr = getExpression();
            }
            assert expr != null;
            expr.setCDataContent(value);
        } catch (VetoException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (InvalidNamespaceException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } finally {
            if (!wasInTransaction) {
                bpelModel.endTransaction();
            }
        }
//      Element expression = getExpression();
//
//      if (expression == null) {
//          return;
//      }
//      Document document = getBpelModel().getProcess().getPeer().getOwnerDocument();
//      CDATASection cdata = getCDATASection(expression.getChildNodes());
//      CDATASection newCdata = document.createCDATASection(""); // NOI18N
//      newCdata.setData(value);
//
//      if (cdata == null) {
//          getModel().getAccess().appendChild(expression, newCdata, this);
//      }
//      else {
//          getModel().getAccess().replaceChild(expression, cdata, newCdata, this);
//      }
//      getModel().getAccess().flush();
//      Element expression = getExpression();
//
//      if (expression == null) {
//          return;
//      }
//      Document document = getBpelModel().getProcess().getPeer().getOwnerDocument();
//      CDATASection cdata = getCDATASection(expression.getChildNodes());
//      CDATASection newCdata = document.createCDATASection(""); // NOI18N
//      newCdata.setData(value);
//
//      if (cdata == null) {
//          getModel().getAccess().appendChild(expression, newCdata, this);
//      }
//      else {
//          getModel().getAccess().replaceChild(expression, cdata, newCdata, this);
//      }
//      getModel().getAccess().flush();
    }

    public String getInput() {
        BpelModel bpelModel = getBpelModel();
        boolean wasInTransaction = bpelModel.isIntransaction();

        if (!wasInTransaction) {
            bpelModel.startTransaction();
        }
        try {
            Expression expr = getExpression();

            if (expr == null) {
                return null;
            }
            return expr.getInputVariablesList();

        }
        finally {
            if (!wasInTransaction) {
                bpelModel.endTransaction();
            }
        }
//      Element expression = getExpression();
//
//      if (expression == null) {
//          return null;
//      }
//      return expression.getAttribute("inputVars"); // NOI18N
    }

    public void setInput(String value) {
        BpelModel bpelModel = getBpelModel();
        boolean wasInTransaction = bpelModel.isIntransaction();

        if (!wasInTransaction) {
            bpelModel.startTransaction();
        }
        try {
            Expression expr = getExpression();

            if (expr != null) {
                expr.setInputVariablesList(value);
            }
        } catch (VetoException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        finally {
            if (!wasInTransaction) {
                bpelModel.endTransaction();
            }
        }
//      Element expression = getExpression();
//
//      if (expression == null) {
//          return;
//      }
//      getModel().getAccess().setAttribute(expression, "inputVars", value, this); // NOI18N
//      getModel().getAccess().flush();
    }

    public String getOutput() {
        BpelModel bpelModel = getBpelModel();
        boolean wasInTransaction = bpelModel.isIntransaction();

        if (!wasInTransaction) {
            bpelModel.startTransaction();
        }
        try {
            Expression expr = getExpression();

            if (expr == null) {
                return null;
            }
            return expr.getOutputVariablesList();
        }
        finally {
            if (!wasInTransaction) {
                bpelModel.endTransaction();
            }
        }
//      Element expression = getExpression();
//
//      if (expression == null) {
//          return null;
//      }
//      return expression.getAttribute("outputVars"); // NOI18N
    }

    public void setOutput(String value) {
        BpelModel bpelModel = getBpelModel();
        boolean wasInTransaction = bpelModel.isIntransaction();

        if (!wasInTransaction) {
            bpelModel.startTransaction();
        }
        try {
            Expression expr = getExpression();

            if (expr != null) {
                expr.setOutputVariablesList(value);
            }
        } catch (VetoException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        finally {
            if (!wasInTransaction) {
                bpelModel.endTransaction();
            }
        }
//      Element expression = getExpression();
//
//      if (expression == null) {
//          return;
//      }
//      getModel().getAccess().setAttribute(expression, "outputVars", value, this); // NOI18N
//      getModel().getAccess().flush();
    }

    public boolean isJavaScript() {
//System.out.println("is JavaScript: " + getName() + ": " + (getExpression() != null));
        return getExpression() != null;
    }

    private Expression getExpression() {
        BpelModel bpelModel = getBpelModel();
        boolean wasInTransaction = bpelModel.isIntransaction();

        if (!wasInTransaction) {
            bpelModel.startTransaction();
        }
        try {
            List<ExtensionAssignOperation> extAssignOps = getChildren(ExtensionAssignOperation.class);
            assert extAssignOps != null; // regarding to the impl it is never been null but empty list

            if (extAssignOps.size() != 1) {
                return null;
            }
            ExtensionAssignOperation extAssignOp = extAssignOps.get(0);
            List<Expression> exprs = extAssignOp.getChildren(Expression.class);
            return exprs == null || exprs.size() < 1 ? null : exprs.get(0);
        }
        finally {
            if (!wasInTransaction) {
                bpelModel.endTransaction();
            }
        }
//      Element extAssignOpEl = extAssignOp.getPeer();
//      if (extAssignOpEl == null) {
//          return null;
//      }
//
////System.out.println("is JavaScript: " + EXTENSION_ASSIGN_OPERATION.equals(extension.getTagName()));
//       List<Element> children = getElementNodes(extAssignOpEl.getChildNodes());
////System.out.println("  size: " + children.size());
//
//      if (children.size() != 1) {
//          return null;
//      }
//      Element expression = children.get(0);
//
//      if (expression == null) {
//          return null;
//      }
////System.out.println("is JavaScript: " + EXPRESSION.equals(expression.getTagName()));
////System.out.println("          tag: " + expression.getTagName());
//      if ( !EXPRESSION.equals(expression.getTagName()) && !SUNXD_EXPRESSION.equals(expression.getTagName())) {
//          return null;
//      }
//      return expression;
    }

    private List<Element> getElementNodes(NodeList nodes) {
        List<Element> elements = new ArrayList<Element>();

        if (nodes == null) {
            return elements;
        }
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node instanceof Element) {
                elements.add((Element) node);
            }
        }
        return elements;
    }
/* todo r
    private CDATASection getCDATASection(NodeList nodes) {
        if (nodes == null) {
            return null;
        }
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node instanceof CDATASection) {
                return (CDATASection) node;
            }
        }
        return null;
    }
*/
    // todo remove out of here and use model transaction
    private void createEmptyJavaScript() {
        BpelModel bpelModel = getBpelModel();
        boolean wasInTransaction = bpelModel.isIntransaction();
  
        if ( !wasInTransaction) {
            bpelModel.startTransaction();
        }
        try {
            Expression expr = getExpression();

            if (expr == null) {
                ExtensionAssignOperation extAssignOp = null;
                List<ExtensionAssignOperation> extAssignOps = getChildren(ExtensionAssignOperation.class);
                boolean extAssignOpExist = !(extAssignOps.size() < 1);

                if (!extAssignOpExist) {
                    extAssignOp = getBpelModel().getBuilder().createExtensionAssignOperation();
//                  addAssignChild(extAssignOp);
//                  extAssignOps = getChildren(ExtensionAssignOperation.class);
//                  assert extAssignOps != null && extAssignOps.size() > 0;
                }
//              extAssignOp = extAssignOps.get(0);
                extAssignOp.getNamespaceContext().addNamespace(Extensions.SUN_JS_EXT_URI);
                expr = getBpelModel().getBuilder().createExtensionEntity(Expression.class);
                extAssignOp.addExtensionEntity(Expression.class, expr);

                if (!extAssignOpExist) {
                    addAssignChild(extAssignOp);
                }
                expr = getExpression();
            } else {
                // do nothing
                return;
            }
            assert expr != null;
            expr.setCDataContent("");
            expr.setInputVariablesList("");
            expr.setOutputVariablesList("");
            expr.setExpressionLanguage(ExpressionLanguage.JAVA_SCRIPT);
        } catch (VetoException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (InvalidNamespaceException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } finally {
            if (!wasInTransaction) {
                bpelModel.endTransaction();
            }
        }
//      ExtensionAssignOperation extAssignOp = getBpelModel().getBuilder().createExtensionAssignOperation();
//      Element extAssignOpEl = extAssignOp.getPeer();
//      Document document = getBpelModel().getProcess().getPeer().getOwnerDocument();
//
//      try {
//          Element expression = document.createElement("Expression"); // NOI18N
//          CDATASection cdata = document.createCDATASection("<![CDATA[]]>"); // NOI18N
//
//          extAssignOpEl.appendChild(expression);
//          expression.appendChild(cdata);
//
//          expression.setAttribute("xmlns", "http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/DataHandling"); // NOI18N
//          expression.setAttribute("expressionLanguage", "urn:sun:bpel:JavaScript"); // NOI18N
//          expression.setAttribute("inputVars", ""); // NOI18N
//          expression.setAttribute("outputVars", ""); // NOI18N
//      }
//      catch (DOMException e) {
//          e.printStackTrace();
//      }
//      addAssignChild(extAssignOp);
    }

    private String getUniqueName(BpelModel model) {
        List<String> javaScriptNames = new ArrayList<String>();
        collectNames(model.getProcess(), javaScriptNames);
        return getUniqueName(javaScriptNames);
    }

    private void collectNames(BpelEntity entity, List<String> names) {
        if (entity == null) {
            return;
        }
        if (entity instanceof NamedElement) {
            String name = ((NamedElement) entity).getName();

            if (name != null && name.startsWith(PATTERN)) {
                names.add(name);
            }
        }
        List<BpelEntity> children = entity.getChildren();

        for (BpelEntity child : children) {
            collectNames(child, names);
        }
    }

    private String getUniqueName(List<String> names) {
        int suffix = 1;
        String name;

        while (true) {
            name = PATTERN + suffix;

            if (!names.contains(name)) {
                return name;
            }
            suffix++;
        }
    }

    public AssignChild[] getAssignChildren() {
        readLock();
        try {
            List<AssignChild> list = getChildren(AssignChild.class);
            return list.toArray(new AssignChild[list.size()]);
        } finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#getAssignChild(int)
     */
    public AssignChild getAssignChild(int i) {
        return getChild(AssignChild.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#removeAssignChild(int)
     */
    public void removeAssignChild(int i) {
        removeChild(Copy.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#setAssignChild(org.netbeans.modules.soa.model.bpel20.api.AssignChild, int)
     */
    public void setAssignChild(AssignChild child, int i) {
        setChildAtIndex(child, AssignChild.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#addAssignChild(org.netbeans.modules.soa.model.bpel20.api.AssignChild)
     */
    public void addAssignChild(AssignChild child) {
        addChildBefore(child, AssignChild.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#insertAssignChild(org.netbeans.modules.soa.model.bpel20.api.AssignChild, int)
     */
    public void insertAssignChild(AssignChild child, int i) {
        insertAtIndex(child, AssignChild.class, i);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#setAssignChildren(org.netbeans.modules.soa.model.bpel20.api.AssignChild[])
     */
    public void setAssignChildren(AssignChild[] children) {
        setArrayBefore(children, AssignChild.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#sizeOfAssignChildren()
     */
    public int sizeOfAssignChildren() {
        readLock();
        try {
            return getChildren(AssignChild.class).size();
        } finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Assign.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#getValidate()
     */
    public TBoolean getValidate() {
        return getBooleanAttribute(BpelAttributes.VALIDATE);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#setValidate(org.netbeans.modules.soa.model.bpel20.api.support.TBoolean)
     */
    public void setValidate(TBoolean value) {
        setBpelAttribute(BpelAttributes.VALIDATE, value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Assign#removeValidate()
     */
    public void removeValidate() {
        removeAttribute(BpelAttributes.VALIDATE);
    }

    public void accept(BpelModelVisitor visitor) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelContainerImpl#getChildType(T)
     */
    @Override
    protected <T extends BpelEntity> Class<? extends BpelEntity> getChildType(T entity) {
        if (entity instanceof AssignChild) {
            return AssignChild.class;
        }
        return super.getChildType(entity);
    }

    @Override
    protected BpelEntity create(Element element) {
        if (BpelElements.COPY.getName().equals(element.getLocalName())) {
            return new CopyImpl(getModel(), element);
        } else if (BpelElements.EXTENSIBLE_ASSIGN.getName().
                equals(element.getLocalName())) {
            return new ExtensibleAssignImpl(getModel(), element);
        } else if (BpelElements.EXTENSION_ASSIGN_OPERATION.getName().
                equals(element.getLocalName())) {
            return new ExtensionAssignOperationImpl(getModel(), element);
        }
        return super.create(element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if (myAttributes.get() == null) {
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[attr.length + 1];
            System.arraycopy(attr, 0, ret, 1, attr.length);
            ret[ 0] = BpelAttributes.VALIDATE;
            myAttributes.compareAndSet(null, ret);
        }
        return myAttributes.get();
    }
    private static final String PATTERN = "JavaScript"; // NOI18N
    private static final String EXPRESSION = "Expression"; // NOI18N
    private static final String SUNXD_EXPRESSION = "sunxd:Expression"; // NOI18N
    private static final String EXTENSION_ASSIGN_OPERATION = "extensionAssignOperation"; // NOI18N
    private static AtomicReference<Attribute[]> myAttributes = new AtomicReference<Attribute[]>();
}
