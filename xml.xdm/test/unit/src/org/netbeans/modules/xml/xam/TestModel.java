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
            qnames.add(TestComponent.Err.QNAME);
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

    public TestComponent.Err createErr(TestComponent parent) {
        QName q = TestComponent.Err.QNAME;
        Element e = this.getDocument().createElementNS(q.getNamespaceURI(), q.getLocalPart());
        return (TestComponent.Err) TestComponent.createComponent(this, parent, e);
    }
}
