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

package org.netbeans.modules.xml.xam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Nam Nguyen
 */
public class TestComponent extends AbstractDocumentComponent<TestComponent> implements NamedReferenceable<TestComponent> {
    public static String NS_URI = "http://www.test.com/TestModel";
    public static String NS2_URI = "http://www.test2.com/TestModel";
    
    public TestComponent(TestModel model, org.w3c.dom.Element e) {
        super(model, e);
    }
    public TestComponent(TestModel model, String name, String ns) {
        this(model, model.getDocument().createElementNS(ns, name));
    }
    public TestComponent(TestModel model, String name, String ns, int index) {
        this(model, name, ns);
        setIndex(index);
    }
    public TestComponent(TestModel model, String name, String ns, int index, String value) {
        this(model, name, ns, index);
        setValue(value);
    }

    @Override
    public String toString() { return getName(); }

    @Override
    public String getName() { return getPeer().getLocalName()+getIndex(); }

    @Override
    public String getNamespaceURI() {
        return super.getNamespaceURI();
    }

    @Override
    protected void populateChildren(List<TestComponent> children) {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            for (int i = 0; i < nl.getLength(); i++) {
                org.w3c.dom.Node n = nl.item(i);
                if (n instanceof Element) {
                    Element e = (Element) n;
                    TestComponent comp = createComponent(getModel(), this, e);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }
    
    static TestComponent createComponent(TestModel model, TestComponent parent, Element e)  {
        String namespace = e.getNamespaceURI();
        if (namespace == null && parent != null) {
            namespace = parent.lookupNamespaceURI(e.getPrefix());
        }
        if (e.getLocalName().equals("a") && NS_URI.equals(namespace)) {
            return new TestComponent.A(model, e);
        } else if (e.getLocalName().equals("a") && NS2_URI.equals(namespace)) {
            return new TestComponent.Aa(model, e);
        } else if (e.getLocalName().equals("b") && NS_URI.equals(namespace)) {
            return new TestComponent.B(model, e);
        } else if (e.getLocalName().equals("c") && NS_URI.equals(namespace)) {
            return new TestComponent.C(model, e);
        } else if (e.getLocalName().equals("d") && NS_URI.equals(namespace)) {
            return new TestComponent.D(model, e);
        } else if (e.getLocalName().equals("e") && NS_URI.equals(namespace)) {
            return new TestComponent.E(model, e);
        } else {
            return null;
            //throw new RuntimeException("unsupported element type "+ e.getNodeName());
        }
    }
    
    public void setValue(String v) { 
        setAttribute(TestAttribute.VALUE.getName(), TestAttribute.VALUE, v); 
    }
    public String getValue() { 
        return getAttribute(TestAttribute.VALUE); 
    }

    public void setIndex(int index) {
        setAttribute(TestAttribute.INDEX.getName(), TestAttribute.INDEX, Integer.valueOf(index));
    }
    public int getIndex() {
        String s = getAttribute(TestAttribute.INDEX);
        return s == null ? -1 : Integer.parseInt(s); 
    }

    @Override
    public void updateReference(Element n) {
        assert (n != null);
        assert n.getLocalName().equals(getQName().getLocalPart());
        super.updateReference(n);
    }
    
//    public QName getQName() { return ROOT_QNAME; }

    @Override
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        if (stringValue == null) return null;
        if (String.class.isAssignableFrom(attr.getType())) {
            return stringValue;
        } else if (Integer.class.isAssignableFrom(attr.getType())) {
            return Integer.valueOf(stringValue);
        }
        assert false : "unsupported type"+attr.getType();
        return stringValue;
    }
    
    public static class A extends TestComponent {
        public static final QName QNAME = new QName(NS_URI, "a");
        public A(TestModel model, int i) {
            super(model, "a", NS_URI, i);
        }

        public A(TestModel model, Element e) {
            super(model, e);
        }

        @Override
        public QName getQName() { return QNAME; }
    }
    public static class Aa extends TestComponent {
        public static final QName QNAME = new QName(NS2_URI, "a");
        public Aa(TestModel model, int i) {
            super(model, "a", NS2_URI, i);
        }
        public Aa(TestModel model, Element e) {
            super(model, e);
        }

        @Override
        public QName getQName() { return QNAME; }
    }
    public static class B extends TestComponent {
        public static final QName QNAME = new QName(NS_URI, "b");
        public B(TestModel model, int i) {
            super(model, "b", NS_URI, i);
        }
        public B(TestModel model, Element e) {
            super(model, e);
        }

        @Override
        public QName getQName() { return QNAME; }
    }
    public static class C extends TestComponent {
        public static final QName QNAME = new QName(NS_URI, "c");
        public C(TestModel model, int i) {
            super(model, "c", NS_URI, i);
        }
        public C(TestModel model, Element e) {
            super(model, e);
        }

        @Override
        public QName getQName() { return QNAME; }
    }
    public static class D extends TestComponent {
        public static final QName QNAME = new QName(NS_URI, "d");
        public D(TestModel model, int i) {
            super(model, "d", NS_URI, i);
        }
        public D(TestModel model, Element e) {
            super(model, e);
        }

        @Override
        public QName getQName() { return QNAME; }
    }
    public static class E extends TestComponent {
        public static final QName QNAME = new QName(NS_URI, "e");
        public E(TestModel model, int i) {
            super(model, "e", NS_URI, i);
        }
        public E(TestModel model, Element e) {
            super(model, e);
        }

        @Override
        public QName getQName() { return QNAME; }

        @Override
        public String getValue() {
            String retValue;
            
            retValue = super.getValue();
            return retValue;
        }

        @Override
        public String getName() {
            return super.getAttribute(TestAttribute.NAME);
        }

        public void setName(String v) {
            setAttribute(TestAttribute.NAME.getName(), TestAttribute.NAME, v);
        }
    }
    
    public static class TestComponentReference<T extends TestComponent> 
            extends AbstractNamedComponentReference<T> {
        public TestComponentReference(Class<T> type, TestComponent parent, String ref) {
            super(type, parent, ref);
        }
        public TestComponentReference(T ref, Class<T> type, TestComponent parent) {
            super(ref, type, parent);
        }

        @Override
        public TestComponent getParent() {
            return (TestComponent) super.getParent();
        }

        @Override
        public String getEffectiveNamespace() {
            if (getReferenced() != null) {
                return getReferenced().getModel().getRootComponent().getTargetNamespace();
            }
            return getParent().getModel().getRootComponent().getTargetNamespace();
        }

        @Override
        public T get() {
            if (getReferenced() == null) {
                String tns = getQName().getNamespaceURI();
                TestComponent root = getParent().getModel().getRootComponent();
                if (tns != null && tns.equals(root.getTargetNamespace())) {
                    setReferenced(getType().cast(new ReferencedFinder().
                            findReferenced(root, getQName().getLocalPart())));
                }
            }
            return getReferenced();
        }
    }

    @Override
    public String getLeadingText(TestComponent child) {
        return super.getLeadingText(child);
    }

    @Override
    public String getTrailingText(TestComponent child) {
        return super.getTrailingText(child);
    }
    
    public void setText(String propName, String value, TestComponent child, final boolean leading) {
        if (leading) {
            setLeadingText(propName, value, child);
        } else {
            setTrailingText(propName, value, child);
        }
    }

    @Override
    public TestModel getModel() {
        return (TestModel) super.getModel();
    }
    
    public void accept(TestVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getTargetNamespace() {
        return getAttribute(TestAttribute.TNS);
    }
    public void setTargetNamespace(String v) {
        setAttribute(TestAttribute.TNS.getName(), TestAttribute.NAME, v);
    }
    
    public <T extends TestComponent> TestComponentReference<T> getRef(Class<T> type) {
        String v = getAttribute(TestAttribute.REF);
        return v == null ? null : new TestComponentReference<T>(type, this, v);
    }
    
    public <T extends TestComponent> void setRef(T referenced, Class<T> type) {
        TestComponentReference<T> ref = new TestComponentReference<T>(referenced, type, this);
        super.setAttribute(TestAttribute.REF.getName(), TestAttribute.REF, ref);
    }
    
    
    public static class ReferencedFinder extends TestVisitor {
        String name;
        TestComponent found;
        
        public ReferencedFinder() {
        }
        public TestComponent findReferenced(TestComponent root, String name) {
            this.name = name;
            root.accept(this);
            return found;
        }

        @Override
        public void visit(TestComponent component) {
            if (name.equals(component.getName())) {
                found = component;
            } else {
                visitChildren(component);
            }
        }

        @Override
        public void visitChildren(TestComponent component) {
            for (TestComponent child : component.getChildren()) {
                child.accept(this);
                if (found != null) {
                    return;
                }
            }
        }
    }
    
    static Collection<Class<? extends TestComponent>> EMPTY = new ArrayList<Class<? extends TestComponent>>();
    public static Collection <Class<? extends TestComponent>> _ANY = new ArrayList<Class<? extends TestComponent>>();
    static { _ANY.add(TestComponent.class); }
    public static Collection <Class<? extends TestComponent>> _A = new ArrayList<Class<? extends TestComponent>>();
    static {  _A.add(A.class); }
    public static Collection <Class<? extends TestComponent>> _B = new ArrayList<Class<? extends TestComponent>>();
    static {  _B.add(B.class); }
    public static Collection <Class<? extends TestComponent>> _C = new ArrayList<Class<? extends TestComponent>>();
    static {  _C.add(C.class); }
    public static Collection <Class<? extends TestComponent>> _D = new ArrayList<Class<? extends TestComponent>>();
    static {  _D.add(D.class); }
    public static Collection <Class<? extends TestComponent>> _AB = new ArrayList<Class<? extends TestComponent>>();
    static {  _AB.add(A.class); _AB.add(B.class); }
    public static Collection <Class<? extends TestComponent>> _BC = new ArrayList<Class<? extends TestComponent>>();
    static {  _BC.add(B.class); _BC.add(C.class); }
    public static Collection <Class<? extends TestComponent>> _AC = new ArrayList<Class<? extends TestComponent>>();
    static {  _AC.add(A.class); _AC.add(C.class); }
    public static Collection <Class<? extends TestComponent>> _ABC = new ArrayList<Class<? extends TestComponent>>();
    static {  _ABC.add(A.class); _ABC.add(B.class); _ABC.add(C.class); }
    public static Collection <Class<? extends TestComponent>> _BAC = new ArrayList<Class<? extends TestComponent>>();
    static {  _BAC.add(B.class); _BAC.add(A.class); _BAC.add(C.class); }
}
