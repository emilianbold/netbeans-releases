package org.netbeans.modules.xml.xam;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Document;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.locator.api.ModelSource;
import org.netbeans.modules.xml.xam.locator.api.DepResolverException;
import org.netbeans.modules.xml.xam.locator.api.DependencyResolver;
import org.netbeans.modules.xml.xam.xdm.*;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class TestModel extends AbstractXDMModel<TestComponent> implements DocumentModel<TestComponent> {
    TestComponent testRoot;
    
    /** Creates a new instance of TestModel */
    public TestModel(javax.swing.text.Document doc) {
        super(doc);
        try {
            super.sync();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TestComponent createRootComponent(org.w3c.dom.Element root) {
        assert(root.getLocalName().equals("test"));
        testRoot = new TestComponent(this, root);
        return testRoot;
    }

    public TestComponent createComponent(TestComponent parent, org.w3c.dom.Element element) {
        TestComponent child = null;
        if (element.getTagName().equals("a")) {
            child = new TestComponent.A(this, element);
        } else if (element.getTagName().equals("b")) {
            child = new TestComponent.B(this, element);
        } else if (element.getTagName().equals("c")) {
            child = new TestComponent.C(this, element);
        } else if (element.getTagName().equals("d")) {
            child = new TestComponent.D(this, element);
        }
        return child;
    }

    public TestComponent getRootComponent() {
        return testRoot;
    }

    protected ComponentUpdater<TestComponent> getComponentUpdater() {
        return new TestComponentUpdater();
    }

    protected ComponentFinder<TestComponent> getComponentFinder() {
        return new TestComponentFinder();
    }

    public ModelSource getModelSource() {
        return new ModelSource() {
            public void setModelSourceModelFileObject(FileObject modelFileObject) {
            }

            public void setReadOnly() {
            }

            public boolean isReadOnly() {
                return false;
            }

            public DependencyResolver getResolver() throws DepResolverException {
                return null;
            }

            public FileObject getFileObject() {
                return null;
            }

            public Document getDocument() throws IOException {
                return TestModel.this.getBaseDocument();
            }
            
        };
    }
    
    private static Set<QName> qnames = null;
    public Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            qnames.add(TestComponent.A.QNAME);
            qnames.add(TestComponent.B.QNAME);
            qnames.add(TestComponent.C.QNAME);
            qnames.add(TestComponent.D.QNAME);
            qnames.add(new QName("test"));
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
    
    
}
