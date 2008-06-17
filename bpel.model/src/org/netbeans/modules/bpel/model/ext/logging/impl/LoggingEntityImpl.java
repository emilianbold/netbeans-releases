/*
 * AlertImpl.java
 * 
 * Created on Sep 22, 2007, 10:21:17 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.model.ext.logging.impl;

import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.ext.logging.xam.LoggingElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.bpel.model.impl.ExtensibleElementsImpl;
import org.w3c.dom.Element;

/**
 *
 * @author zgursky
 */
public abstract class LoggingEntityImpl extends ExtensibleElementsImpl implements ExtensionEntity {
    
    private LoggingEntityFactory mFactory;
    
    
    LoggingEntityImpl(LoggingEntityFactory factory, BpelModelImpl model, Element e ) {
        super(model, e);
        mFactory = factory;
    }

    LoggingEntityImpl(LoggingEntityFactory factory, BpelBuilderImpl builder, LoggingElements loggingElements) {
        super(builder.getModel(), 
                builder.getModel().getDocument().createElementNS(
                loggingElements.getNamespace(), loggingElements.getName()));
        mFactory = factory;
    }

    public void accept(BpelModelVisitor visitor) {
        visitor.visit(this);
    }
    
    public LoggingEntityFactory getFactory() {
        return mFactory;
    }
    
    public boolean canExtend(ExtensibleElements extensible) {
        if (getFactory().canExtend(extensible, getElementType())) {
            return true;
        } else {
            return false;
        }
    }
    
}
