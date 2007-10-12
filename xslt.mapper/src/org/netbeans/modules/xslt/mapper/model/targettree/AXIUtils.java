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
package org.netbeans.modules.xslt.mapper.model.targettree;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils.PathItem;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.AttributeValueTemplate;
import org.netbeans.modules.xslt.model.Instruction;
import org.netbeans.modules.xslt.model.Template;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.netbeans.modules.xslt.model.XslVisitorAdapter;

/**
 *
 * @author Alexey
 */
public class AXIUtils {

    /**
     * Checks if XSL component this node represents creates an element in output tree of given schema type
     * @returns true if types are the same
     **/
    public static boolean isSameSchemaType(XslComponent xslc, AXIComponent axic) {
        TypeCheckVisitor visitor = new TypeCheckVisitor(axic);
        xslc.accept(visitor);
        return visitor.isMatching();
    }

    public static class TypeCheckVisitor extends XslVisitorAdapter {

        private AXIComponent axic;
        private boolean isMatching = false;

        public TypeCheckVisitor(AXIComponent axic) {
            this.axic = axic;
        }

        public boolean isMatching() {
            return isMatching;
        }

        public void visit(org.netbeans.modules.xslt.model.Attribute attribute) {
            if (axic instanceof org.netbeans.modules.xml.axi.Attribute) {
                AttributeValueTemplate atv = attribute.getName();
                if (atv != null) {
                    isMatching = compareName(atv.getQName());
                }
            }
        }

        public void visit(org.netbeans.modules.xslt.model.Element element) {
            if (axic instanceof org.netbeans.modules.xml.axi.Element) {
                AttributeValueTemplate atv = element.getName();
                if (atv != null) {
                    isMatching = compareName(atv.getQName());
                }
            }
        }

        public void visit(org.netbeans.modules.xslt.model.LiteralResultElement element) {
            if (axic instanceof org.netbeans.modules.xml.axi.Element) {
                QName qname = element.getQName();
                isMatching = compareName(qname);
            }
        }

        private boolean compareName(QName qname) {
            if (qname == null) {
                return false;
            }
            //
            if (AxiomUtils.isUnqualified(axic)) {
                return qname.getLocalPart().equals(((AXIType) axic).getName());
            } else {
                return qname.getLocalPart().equals(((AXIType) axic).getName()) && qname.getNamespaceURI().equals(axic.getTargetNamespace());
            }
        }
    }

    /**
     * Call visitor for all children of type Attribute and Element
     **/
    public static abstract class ElementVisitor {

        public abstract void visit(AXIComponent component);

        public void visitSubelements(AXIComponent axic) {
            
            if (axic instanceof Element) {
                visitSubelements((Element) axic);
            } else if (axic instanceof AXIDocument) {
                visitSubelements((AXIDocument) axic);
            }
        }

        protected void visitSubelements(Element element) {
            for (AbstractAttribute a : element.getAttributes()) {
                if (a instanceof Attribute) {
                    a = (Attribute) getReferent(a);
                    visit(a);
                }
            }

            for (AbstractElement e : element.getChildElements()) {
                if (e instanceof Element) {
                    e = (Element) getReferent(e);
                    visit(e);
                }
            }
        }

        protected void visitSubelements(AXIDocument doc) {

            for (AbstractElement e : doc.getChildElements()) {
                if (e instanceof Element) {
                    visit(e);
                }
            }



        }
    }

    public static List<AXIComponent> getChildTypes(AXIComponent axic) {

        final List<AXIComponent> result = new ArrayList<AXIComponent>();

        if (axic != null) {

            new AXIUtils.ElementVisitor() {

                public void visit(AXIComponent c) {
                    
                    c = getReferent(c);
                    
                    result.add(c);
                }
            }.visitSubelements(axic);
        }

        return result;

    }

    /**
     * Prepares XPath for the specified Schema node.
     */
    public static List<PathItem> prepareSimpleXPath(final SchemaNode schemaNode) {
        //
        // Collects Path Items first
        ArrayList<PathItem> path = new ArrayList<PathItem>();
        TreeNode currNode = schemaNode;
        SchemaNode lastProcessedSchemaNode = null;
        while (currNode != null && currNode instanceof SchemaNode) {
            lastProcessedSchemaNode = (SchemaNode) currNode;
            if (currNode instanceof PredicatedSchemaNode) {
                PredicatedSchemaNode psn = (PredicatedSchemaNode) currNode;
                String pred = psn.getPredicatedAxiComp().getPredicatesText();
                AxiomUtils.processNode(lastProcessedSchemaNode.getType(), pred, path);
            } else {
                AxiomUtils.processNode(lastProcessedSchemaNode.getType(), null, path);
            }
            //
            currNode = currNode.getParent();
        }
        //
        // Add parent elements to ensure that the XPath would be absolute
        if (lastProcessedSchemaNode != null) {
            AXIComponent axiComponent = lastProcessedSchemaNode.getType();
            if (axiComponent != null) {
                AXIComponent parentAxiComponent = axiComponent.getParent();
                while (true) {
                    if (parentAxiComponent == null) {
                        break;
                    }
                    //
                    AxiomUtils.processNode(parentAxiComponent, null, path);
                    //
                    parentAxiComponent = parentAxiComponent.getParent();
                }
            }
        }
        //
        return path;
    }

    public static AXIComponent getType(XslComponent xslc, XsltMapper mapper) {
        if (xslc == null) {
            return null;
        }
        XslComponent xsl_parent = xslc.getParent();

        if (xslc instanceof org.netbeans.modules.xslt.model.Element || xslc instanceof org.netbeans.modules.xslt.model.Attribute || xslc instanceof org.netbeans.modules.xslt.model.LiteralResultElement) {
            AXIComponent axi_parent = getType(xsl_parent, mapper);
            if (axi_parent != null) {
                for (AXIComponent type : axi_parent.getChildElements()) {
                    if (type == null || type.getPeer() == null || type.getPeer().getModel() == null) {
                        continue;
                    }
                    
                    type = getReferent(type); 
                    
                    if (AXIUtils.isSameSchemaType(xslc, type)) {
                        return type;
                    }
                }
            }
        } else if (xslc instanceof org.netbeans.modules.xslt.model.Template) { //no declaration nodes fond downtree
            AXIComponent targetType = mapper.getContext().getTargetType();
            return targetType != null ? targetType.getModel().getRoot() : null;
        } else if (xsl_parent != null) {
            return getType(xsl_parent, mapper);
        }
        return null;
    }

    public static AXIComponent getReferent(AXIComponent type) {
        if (type instanceof Element) {
            Element e = (Element) type;
            if (e.isReference()) {
                type = e.getReferent();
            }
        }
        if (type instanceof Attribute) {
            Attribute a = (Attribute) type;
            if (a.isReference()) {
                type = a.getReferent();
            }
        }
        return type;

    }
}
