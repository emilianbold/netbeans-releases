package org.netbeans.modules.xml.xam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author Nam Nguyen
 */
public class TestComponent extends AbstractDocumentComponent<TestComponent> implements NamedReferenceable<TestComponent> {
    public static String NS_URI = "http://www.test.com/TestModel";
    public static String NS2_URI = "http://www.test2.com/TestModel";
    public static QName ROOT_QNAME = new QName(NS_URI, "test");
    
    public TestComponent(TestModel model, org.w3c.dom.Element e) {
        super(model, e);
    }
    public TestComponent(TestModel model, String name) {
        this(model, model.getDocument().createElementNS(NS_URI, name));
    }
    public TestComponent(TestModel model, String name, int index) {
        this(model, name);
        setIndex(index);
    }
    public TestComponent(TestModel model, String name, int index, String value) {
        this(model, name, index);
        setValue(value);
    }
    public String toString() { return getName(); }
    public String getName() { return getPeer().getLocalName()+getIndex(); }
    public String getNamespaceURI() {
        return super.getNamespaceURI();
    }
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

    public void updateReference(Element n) {
        assert (n != null);
        assert n.getLocalName().equals(getQName().getLocalPart());
        super.updateReference(n);
    }
    
    public QName getQName() { return ROOT_QNAME; }
    
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
            super(model, "a", i);
        }
        public A(TestModel model, Element e) {
            super(model, e);
        }
        public QName getQName() { return QNAME; }
    }
    public static class Aa extends TestComponent {
        public static final QName QNAME = new QName(NS2_URI, "a");
        public Aa(TestModel model, int i) {
            super(model, "a", i);
        }
        public Aa(TestModel model, Element e) {
            super(model, e);
        }
        public QName getQName() { return QNAME; }
    }
    public static class B extends TestComponent {
        public static final QName QNAME = new QName(NS_URI, "b");
        public B(TestModel model, int i) {
            super(model, "b", i);
        }
        public B(TestModel model, Element e) {
            super(model, e);
        }
        public QName getQName() { return QNAME; }
    }
    public static class C extends TestComponent {
        public static final QName QNAME = new QName(NS_URI, "c");
        public C(TestModel model, int i) {
            super(model, "c", i);
        }
        public C(TestModel model, Element e) {
            super(model, e);
        }
        public QName getQName() { return QNAME; }
    }
    public static class D extends TestComponent {
        public static final QName QNAME = new QName(NS_URI, "d");
        public D(TestModel model, int i) {
            super(model, "d", i);
        }
        public D(TestModel model, Element e) {
            super(model, e);
        }
        public QName getQName() { return QNAME; }
    }
    public static class E extends TestComponent {
        public static final QName QNAME = new QName(NS_URI, "e");
        public E(TestModel model, int i) {
            super(model, "e", i);
        }
        public E(TestModel model, Element e) {
            super(model, e);
        }
        public QName getQName() { return QNAME; }

        public String getValue() {
            String retValue;
            
            retValue = super.getValue();
            return retValue;
        }
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
        public TestComponent getParent() {
            return (TestComponent) super.getParent();
        }
        public String getEffectiveNamespace() {
            if (getReferenced() != null) {
                return getReferenced().getModel().getRootComponent().getTargetNamespace();
            }
            return getParent().getModel().getRootComponent().getTargetNamespace();
        }

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

    public String getLeadingText(TestComponent child) {
        return super.getLeadingText(child);
    }
    
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
        public void visit(TestComponent component) {
            if (name.equals(component.getName())) {
                found = component;
            } else {
                visitChildren(component);
            }
        }
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
