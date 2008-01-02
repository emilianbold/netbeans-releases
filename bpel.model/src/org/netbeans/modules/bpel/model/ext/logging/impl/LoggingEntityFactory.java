/*
 * TracingEntityFactory.java
 * 
 * Created on Sep 22, 2007, 5:09:09 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.model.ext.logging.impl;

import java.util.Arrays;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.impl.*;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.Compensate;
import org.netbeans.modules.bpel.model.api.CompensateScope;
import org.netbeans.modules.bpel.model.api.Empty;
import org.netbeans.modules.bpel.model.api.Exit;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.ReThrow;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.Validate;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.model.ext.logging.api.Alert;
import org.netbeans.modules.bpel.model.ext.logging.api.Log;
import org.netbeans.modules.bpel.model.ext.logging.api.Trace;
import org.netbeans.modules.bpel.model.ext.logging.xam.LoggingElements;
import org.netbeans.modules.bpel.model.spi.EntityFactory;
import org.w3c.dom.Element;

/**
 *
 * @author zgursky
 */
public class LoggingEntityFactory implements EntityFactory {

    public LoggingEntityFactory() {
    }

    public boolean isApplicable(String namespaceUri) {
        if (Trace.LOGGING_NAMESPACE_URI.equals(namespaceUri)) {
            return true;
        } else {
            return false;
        }
    }

    public Set<QName> getElementQNames() {
        return LoggingElements.allQNames();
    }

    public BpelEntity create(BpelContainer container, Element element) {
        QName elementQName = new QName(
                element.getNamespaceURI(), element.getLocalName());
        if (LoggingElements.TRACE.getQName().equals(elementQName)) {
            return new TraceImpl(this, (BpelModelImpl)container.getBpelModel(), element);
        } else if (LoggingElements.LOG.getQName().equals(elementQName)) {
            return new LogImpl(this, (BpelModelImpl)container.getBpelModel(), element);
        } else if (LoggingElements.ALERT.getQName().equals(elementQName)) {
            return new AlertImpl(this, (BpelModelImpl)container.getBpelModel(), element);
        } else {
            return null;
        }
    }

    public <T extends BpelEntity> T create(BpelBuilderImpl builder, Class<T> clazz) {
        T newEntity = null;
        if (Trace.class.equals(clazz)) {
            newEntity = (T)new TraceImpl(this, builder);
        } else if (Log.class.equals(clazz)) {
            newEntity = (T)new LogImpl(this, builder);
        } else if (Alert.class.equals(clazz)) {
            newEntity = (T)new AlertImpl(this, builder);
        }
        return newEntity;
    }

    public boolean canExtend(ExtensibleElements extensible, Class<? extends BpelEntity> extensionType) {
        if (Trace.class.equals(extensionType)) {
            if (sSupportedParents.contains(extensible.getElementType())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static Set<Class<? extends ExtensibleElements>> sSupportedParents =
            new HashSet<Class<? extends ExtensibleElements>>(Arrays.asList(
                    Catch.class,
                    CatchAll.class,
                    Assign.class,
                    Compensate.class,
                    CompensateScope.class,
                    Empty.class,
                    Exit.class,
                    Flow.class,
                    ForEach.class,
                    If.class,
                    Invoke.class,
                    Pick.class,
                    Receive.class,
                    RepeatUntil.class,
                    Reply.class,
                    ReThrow.class,
                    Scope.class,
                    Sequence.class,
                    Throw.class,
                    Validate.class,
                    Wait.class,
                    While.class)
                    );
}
