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

package org.netbeans.modules.wsdlextensions.scheduler.model.impl;

import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;

import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerConstants;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

/**
 ** @author sunsoabi_edwong
*/
public class SchedulerElementFactoryProvider implements SchedulerConstants {
    
    public static class BindingFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(SCHED_BINDING_QNAME);
        }
        
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SchedulerBindingImpl(context.getModel(), element);
        }
    }

    public static class OperationFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(SCHED_OPERATION_QNAME);
        }
        
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SchedulerOperationImpl(context.getModel(), element);
        }
    }

    public static class TriggerFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SCHED_TRIGGER_QNAME);
        }
        
        public WSDLComponent create(WSDLComponent context, Element element) {
            TriggerType type = TriggerType.toEnum(element.getAttribute(
                    SchedulerAttribute.TYPE.getName()));

            WSDLComponent result = null;
            if (type != null) {
                switch (type) {
                case SIMPLE:
                    result = new SimpleTriggerImpl(context.getModel(), element);
                    break;
                case CRON:
                    result = new CronTriggerImpl(context.getModel(), element);
                    break;
                case HYBRID:
                    result = new HybridTriggerImpl(context.getModel(), element);
                    break;
                default:
                    result = null;
                    break;
                }
            }
            
            if (null == result) {
                result = new SchedulerTriggerImpl(context.getModel(), element);
            }

            return result;
        }
    }

    public static class ActivePeriodFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SCHED_ACTIVEPERIOD_QNAME);
        }
        
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SchedulerActivePeriodImpl(context.getModel(), element);
        }
    }
}
