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
package org.netbeans.modules.compapp.casaeditor.model.jbi.impl;

import java.io.IOException;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBI;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponentFactory;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class JBIModelImpl extends JBIModel {
    
    private JBIComponentFactory factory;
    private JBI jbi;
    
    /**
     * Creates a new instance of JBIModelImpl
     */
    public JBIModelImpl(ModelSource source) {
        super(source);
        factory = new JBIComponentFactoryImpl(this);
    }
    
    public JBI getRootComponent() {
        return jbi;
    }
    
    public void setJBI(JBI jbi) {
        this.jbi = jbi;
    }

    protected ComponentUpdater<JBIComponent> getComponentUpdater() {
        return new JBISyncUpdateVisitor();
    }

    public JBIComponent createComponent(JBIComponent parent, Element element) {
        return getFactory().create(element, parent);
    }

    public JBI createRootComponent(Element root) {
        JBI jbi = new JBIImpl(this, root);
        setJBI(jbi);
        return jbi;
    }

    public JBIComponentFactory getFactory() {
        return factory;
    }
    
    public Set<QName> getQNames() {
        return JBIQNames.getMappedQNames();
    }
    
    /*
    public synchronized void sync() throws java.io.IOException {
        if (needsSync()) {
            syncStarted();
            boolean syncStartedTransaction = false;
            boolean success = false;
            try {
                startTransaction(); //true, false);  //start pseudo transaction for event firing
                syncStartedTransaction = true;
                setState(getAccess().sync());
                endTransaction();
                success = true;
            } catch (IOException e) {
                setState(State.NOT_WELL_FORMED);
                endTransaction(false); // do want to fire just the state transition event
                throw e;
            } finally {
                if (syncStartedTransaction && isIntransaction()) { //CR: consider separate try/catch
                    try {
                        endTransaction(true); // do not fire events
                    } catch(Exception ex) {
                        //Logger.getLogger(getClass().getName()).log(Level.INFO, "Sync cleanup error.", ex); //NOI18N
                    }
                }

                if (!success && getState() != State.NOT_WELL_FORMED) {
                    setState(State.NOT_SYNCED);
                    refresh(); 
                }
                
                setInSync(true); // false);
                syncCompleted();
            }
        }
    }
    */
}
