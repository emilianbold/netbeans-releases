package org.netbeans.modules.xml.xam;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.ComponentFactory;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.dom.ElementIdentity;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class TestModel extends AbstractDocumentModel<TestComponent> implements DocumentModel<TestComponent> {
    TestComponent testRoot;
    
    /** Creates a new instance of TestModel */
    public TestModel(Document doc) {
        super(createModelSource(doc));
        try {
            super.sync();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        this.addPropertyChangeListener(new FaultGenerator());
    }
    
    protected void setIdentifyingAttributes() {
        ElementIdentity eid = getAccess().getElementIdentity();
        eid.addIdentifier("id");
        eid.addIdentifier("index");
        eid.addIdentifier("name");
        eid.addIdentifier("ref");
    }

    public TestComponent createRootComponent(org.w3c.dom.Element root) {
        if (TestComponent.NS_URI.equals(root.getNamespaceURI()) &&
            "test".equals(root.getLocalName())) {
                testRoot = new TestComponent(this, root);
        } else {
            testRoot = null;
        }
        return testRoot;
    }
    
    public TestComponent createComponent(TestComponent parent, org.w3c.dom.Element element) {
        return TestComponent.createComponent(this, parent, element);
    }
    
    public TestComponent getRootComponent() {
        return testRoot;
    }
    
    private boolean faultInSyncUpdater = false;
    public void injectFaultInSyncUpdater() {
        faultInSyncUpdater = true;
    }
    protected ComponentUpdater<TestComponent> getComponentUpdater() {
        if (faultInSyncUpdater) {
            faultInSyncUpdater = false;
            Object npe = null; npe.getClass();
        }
        return new TestComponentUpdater();
    }
    
    private boolean faultInFindComponent = false;
    public void injectFaultInFindComponent() {
        faultInFindComponent = true;
    }
    public DocumentComponent findComponent(List<Element> pathFromRoot) {
        if (faultInFindComponent) {
            faultInFindComponent = false;
            Object npe = null; 
            npe.getClass();
        }
        return super.findComponent(pathFromRoot);
    }
    
    private boolean faultInEventFiring = false;
    public void injectFaultInEventFiring() {
        faultInEventFiring = true;
    }
    private class FaultGenerator implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (faultInEventFiring) {
                faultInEventFiring = false;
                Object foo = null;  foo.getClass();
            }
        }
    }
    
    public static ModelSource createModelSource(Document doc) {
        Lookup lookup = Lookups.fixed(new Object[] { doc } ); //maybe later a simple catalog
        return new ModelSource(lookup, true);
    }
    
    private static Set<QName> qnames = null;
    public Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            qnames.add(TestComponent.A.QNAME);
            qnames.add(TestComponent.Aa.QNAME);
            qnames.add(TestComponent.B.QNAME);
            qnames.add(TestComponent.C.QNAME);
            qnames.add(TestComponent.D.QNAME);
            qnames.add(TestComponent.E.QNAME);
            qnames.add(new QName(TestComponent.NS_URI, "test"));
        }
        return qnames;
    }
    
    public ComponentFactory<TestComponent> getFactory() {
        return new ComponentFactory<TestComponent>() {
            public TestComponent create(Element child, TestComponent parent) {
                return TestModel.this.createComponent(parent, child);
            }
        };
    }
    
    public TestComponent.A createA(TestComponent parent) {
        QName q = TestComponent.A.QNAME;
        Element e = this.getDocument().createElementNS(q.getNamespaceURI(), q.getLocalPart());
        return (TestComponent.A) TestComponent.createComponent(this, parent, e);
    }
    public TestComponent.Aa createAa(TestComponent parent) {
        QName q = TestComponent.Aa.QNAME;
        Element e = this.getDocument().createElementNS(q.getNamespaceURI(), q.getLocalPart());
        return (TestComponent.Aa) TestComponent.createComponent(this, parent, e);
    }
    public TestComponent.B createB(TestComponent parent) {
        QName q = TestComponent.B.QNAME;
        Element e = this.getDocument().createElementNS(q.getNamespaceURI(), q.getLocalPart());
        return (TestComponent.B) TestComponent.createComponent(this, parent, e);
    }
    public TestComponent.C createC(TestComponent parent) {
        QName q = TestComponent.C.QNAME;
        Element e = this.getDocument().createElementNS(q.getNamespaceURI(), q.getLocalPart());
        return (TestComponent.C) TestComponent.createComponent(this, parent, e);
    }
    public TestComponent.D createD(TestComponent parent) {
        QName q = TestComponent.D.QNAME;
        Element e = this.getDocument().createElementNS(q.getNamespaceURI(), q.getLocalPart());
        return (TestComponent.D) TestComponent.createComponent(this, parent, e);
    }
}
