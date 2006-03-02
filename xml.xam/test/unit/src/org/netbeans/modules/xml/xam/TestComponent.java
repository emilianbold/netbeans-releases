package org.netbeans.modules.xml.xam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Nam Nguyen
 */
public class TestComponent extends AbstractComponent<TestComponent, TestModel> {
    public static String NS_URI = "http://www.test.com/TestModel";
    public static QName ROOT_QNAME = new QName("test");
    
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
    protected String getNamespaceURI() {
        return NS_URI;
    }
    public String toString() { return getPeer().getLocalName()+getIndex(); }
    public String getName() { return getPeer().getLocalName(); }
    
    protected void populateChildren(List<TestComponent> children) {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            for (int i = 0; i < nl.getLength(); i++) {
                org.w3c.dom.Node n = nl.item(i);
                if (n instanceof Element) {
                    Element e = (Element) n;
                    TestComponent comp = TestComponent.createComponent(TestModel.class.cast(getModel()), e);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }
    
    static TestComponent createComponent(TestModel model, Element e)  {
        if (e.getLocalName().equals("a")) {
            return new TestComponent.A(model, e);
        } else if (e.getLocalName().equals("b")) {
            return new TestComponent.B(model, e);
        } else if (e.getLocalName().equals("c")) {
            return new TestComponent.C(model, e);
        } else if (e.getLocalName().equals("d")) {
            return new TestComponent.D(model, e);
        } else {
            throw new RuntimeException("unsupported element type "+ e.getNodeName());
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

    public void updateReference(Node n) {
        assert (n != null);
        assert n.getLocalName().equals(getQName().getLocalPart());
        super.updateReference(n);
    }
    
    protected QName getQName() { return ROOT_QNAME; }
    
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
    
    public int findPosition() {
        return 0;
    }

    public TestComponent copy(TestComponent parent) {
        return null;
    }

    public static class A extends TestComponent {
        public static final QName QNAME = new QName("a");
        public A(TestModel model, int i) {
            super(model, "a", i);
        }
        public A(TestModel model, Element e) {
            super(model, e);
        }
        public QName getQName() { return QNAME; }
    }
    
    public static class B extends TestComponent {
        public static final QName QNAME = new QName("b");
        public B(TestModel model, int i) {
            super(model, "b", i);
        }
        public B(TestModel model, Element e) {
            super(model, e);
        }
        public QName getQName() { return QNAME; }
    }
    public static class C extends TestComponent {
        public static final QName QNAME = new QName("c");
        public C(TestModel model, int i) {
            super(model, "c", i);
        }
        public C(TestModel model, Element e) {
            super(model, e);
        }
        public QName getQName() { return QNAME; }
    }
    public static class D extends TestComponent {
        public static final QName QNAME = new QName("d");
        public D(TestModel model, int i) {
            super(model, "d", i);
        }
        public D(TestModel model, Element e) {
            super(model, e);
        }
        public QName getQName() { return QNAME; }
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
