/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.scheduler.utils;

import java.util.List;
import org.netbeans.modules.wsdlextensions.scheduler.model.CronTrigger;
import org.netbeans.modules.wsdlextensions.scheduler.model.HybridTrigger;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerActivePeriod;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerBinding;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerConstants;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerModel;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerModel.TriggerDetail;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerOperation;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerTrigger;
import org.netbeans.modules.wsdlextensions.scheduler.model.SimpleTrigger;
import org.netbeans.modules.wsdlextensions.scheduler.model.impl.CronTriggerImpl;
import org.netbeans.modules.wsdlextensions.scheduler.model.impl.HybridTriggerImpl;
import org.netbeans.modules.wsdlextensions.scheduler.model.impl.SchedulerActivePeriodImpl;
import org.netbeans.modules.wsdlextensions.scheduler.model.impl.SchedulerBindingImpl;
import org.netbeans.modules.wsdlextensions.scheduler.model.impl.SchedulerModelImpl;
import org.netbeans.modules.wsdlextensions.scheduler.model.impl.SchedulerOperationImpl;
import org.netbeans.modules.wsdlextensions.scheduler.model.impl.SimpleTriggerImpl;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author sunsoabi_edwong
 */
public class ModelUtils implements SchedulerConstants {
    
    public static SchedulerModel parseWSDLModel(WSDLModel wsdlModel, Port port,
            Operation operation) {
        SchedulerBinding schedBinding = null;
        SchedulerOperation schedOperation = null;
        BindingInput bindingInput = null;
        SchedulerActivePeriod activePeriod = null;
        
        Binding binding = port.getBinding().get();
        if ((null == binding)
                || Utils.isEmpty(binding.getBindingOperations())) {
            return null;
        }
        List<SchedulerBinding> schedBindings =
                binding.getExtensibilityElements(SchedulerBinding.class);
        if (Utils.isEmpty(schedBindings)) {
            return null;
        }
        schedBinding = schedBindings.get(0);
        
        for (BindingOperation bop : binding.getBindingOperations()) {
            if (operation.equals(bop.getOperation().get())) {
                List<SchedulerOperation> schedOperations = bop
                        .getExtensibilityElements(SchedulerOperation.class);
                if (Utils.isEmpty(schedOperations)) {
                    return null;
                }
                schedOperation = schedOperations.get(0);
                
                bindingInput = bop.getBindingInput();
                if (null == bindingInput) {
                    return null;
                }
                break;
            }
        }
        if ((null == schedOperation) || (null == bindingInput)) {
            return null;
        }
        
        List<SchedulerActivePeriod> activePeriods = port
                .getExtensibilityElements(SchedulerActivePeriod.class);
        if (Utils.isEmpty(activePeriods)) {
            return null;
        }
        activePeriod = activePeriods.get(0);
        
        SchedulerModel schedModel = new SchedulerModelImpl();
        
        schedModel.setGroup(schedBinding.getGroup());
        schedModel.setDateFormat(schedBinding.getDateFormat());
        
        schedModel.setMode(schedOperation.getMode());
        
        schedModel.setStart(activePeriod.getStarting());
        schedModel.setEnd(activePeriod.getEnding());
        
        schedModel.setTimeZone(activePeriod.getTimeZone());
        
        List<SchedulerTrigger> schedTriggers = bindingInput
                .getExtensibilityElements(SchedulerTrigger.class);
        if (!Utils.isEmpty(schedTriggers)) {
            for (SchedulerTrigger st : schedTriggers) {
                TriggerDetail td = schedModel.createTriggerDetail();
                schedModel.addTrigger(td);
                
                td.setName(st.getName());
                td.setType(st.getType());
                td.setEnabled(st.isEnabled());
                td.setDescription(st.getDescription());
                
                if (st instanceof SimpleTrigger) {
                    SimpleTrigger s = (SimpleTrigger) st;
                    td.setRepeat(s.getRepeat());
                    td.setInterval(s.getInterval());
                    td.setMessage(s.getMessage());
                } else if (st instanceof CronTrigger) {
                    CronTrigger c = (CronTrigger) st;
                    td.setCronExpression(c.getCronExpression());
                    td.setMessage(c.getMessage());
                } else if (st instanceof HybridTrigger) {
                    HybridTrigger ht = (HybridTrigger) st;
                    td.setCronExpression(ht.getCronExpression());
                    td.setDuration(ht.getDuration());
                    td.setRepeat(ht.getRepeat());
                    td.setInterval(ht.getInterval());
                    td.setMessage(ht.getMessage());
                }
            }
        }
        
        return schedModel;
    }
    
    private static WSDLComponent[] findPortOperation(WSDLModel wsdlModel) {
        if ((wsdlModel.getDefinitions() != null)
                && (wsdlModel.getDefinitions().getServices() != null)) {
            for (Service s : wsdlModel.getDefinitions().getServices()) {
                if (s.getPorts() == null) {
                    continue;
                }
                for (Port p : s.getPorts()) {
                    if (p.getBinding() == null) {
                        continue;
                    }
                    Binding binding = p.getBinding().get();
                    if (null == binding) {
                        continue;
                    }
                    PortType pt = binding.getType().get();
                    if (null == pt) {
                        continue;
                    }
                    for (Operation op : pt.getOperations()) {
                        if (null == op.getInput()) {
                            continue;
                        }
                        if (op.getOutput() != null) {
                            continue;
                        }
                        
                        return new WSDLComponent[] {p, op};
                    }
                }
            }
        }
        
        return null;
    }

    public static SchedulerModel parseWSDLModel(WSDLModel wsdlModel) {
        SchedulerModel schedModel = null;
        WSDLComponent[] portOperation = findPortOperation(wsdlModel);
        if (portOperation != null) {
            schedModel = parseWSDLModel(wsdlModel, (Port) portOperation[0],
                    (Operation) portOperation[1]);
        }
        return schedModel;
    }
        
    public static Binding commitWSDLModel(WSDLModel wsdlModel,
            SchedulerModel schedModel, Port port, Operation operation) {
        SchedulerBinding schedBinding = null;
        BindingOperation bindingOperation = null;
        SchedulerOperation schedOperation = null;
        BindingInput bindingInput = null;
        SchedulerActivePeriod activePeriod = null;
        
        Binding binding = port.getBinding().get();
        if ((null == binding)
                || Utils.isEmpty(binding.getBindingOperations())) {
            return null;
        }
        List<SchedulerBinding> schedBindings =
                binding.getExtensibilityElements(SchedulerBinding.class);
        if (!Utils.isEmpty(schedBindings)) {
            schedBinding = schedBindings.get(0);
        }
        
        for (BindingOperation bop : binding.getBindingOperations()) {
            if (operation.equals(bop.getOperation().get())) {
                bindingOperation = bop;
                
                List<SchedulerOperation> schedOperations = bindingOperation
                        .getExtensibilityElements(SchedulerOperation.class);
                if (!Utils.isEmpty(schedOperations)) {
                    schedOperation = schedOperations.get(0);
                }
                
                bindingInput = bindingOperation.getBindingInput();
                break;
            }
        }
        if (null == bindingInput) {
            return null;
        }
        
        List<SchedulerActivePeriod> activePeriods = port
                .getExtensibilityElements(SchedulerActivePeriod.class);
        if (!Utils.isEmpty(activePeriods)) {
            activePeriod = activePeriods.get(0);
        }
        
        if (!wsdlModel.isIntransaction()) {
            wsdlModel.startTransaction();
        }
        
        if (null == schedBinding) {
            schedBinding = new SchedulerBindingImpl(wsdlModel);
            binding.addExtensibilityElement(schedBinding);
        }
        if (null == schedOperation) {
            schedOperation = new SchedulerOperationImpl(wsdlModel);
            bindingOperation.addExtensibilityElement(schedOperation);
        }
        if (null == activePeriod) {
            activePeriod = new SchedulerActivePeriodImpl(wsdlModel);
            port.addExtensibilityElement(activePeriod);
        }
        
        schedBinding.setGroup(schedModel.getGroup());
        schedBinding.setDateFormat(schedModel.getDateFormat());
        
        schedOperation.setMode(schedModel.getMode());
        
        activePeriod.setStarting(schedModel.getStart());
        activePeriod.setEnding(schedModel.getEnd());
        
        activePeriod.setTimeZone(schedModel.getTimeZone());
        
        List<SchedulerTrigger> schedTriggers = bindingInput
                .getExtensibilityElements(SchedulerTrigger.class);
        if (!Utils.isEmpty(schedTriggers)) {
            for (SchedulerTrigger st : schedTriggers) {
                bindingInput.removeExtensibilityElement(st);
            }
        }
        for (TriggerDetail td : schedModel.getTriggers()) {
            if (TriggerType.SIMPLE.getProgName().equals(td.getType())) {
                SimpleTrigger s = new SimpleTriggerImpl(wsdlModel);
                bindingInput.addExtensibilityElement(s);

                s.setName(td.getName());
                s.setType(td.getType());
                s.setEnabled(td.isEnabled());
                s.setDescription(td.getDescription());

                s.setRepeat(td.getRepeat());
                s.setInterval(td.getInterval());
                s.setMessage(td.getMessage());
            } else if (TriggerType.CRON.getProgName().equals(td.getType())) {
                CronTrigger c = new CronTriggerImpl(wsdlModel);
                bindingInput.addExtensibilityElement(c);
                
                c.setName(td.getName());
                c.setType(td.getType());
                c.setEnabled(td.isEnabled());
                c.setDescription(td.getDescription());
                
                c.setCronExpression(td.getCronExpression());
                c.setMessage(td.getMessage());
            } else if (TriggerType.HYBRID.getProgName().equals(td.getType())) {
                HybridTrigger h = new HybridTriggerImpl(wsdlModel);
                bindingInput.addExtensibilityElement(h);
                
                h.setName(td.getName());
                h.setType(td.getType());
                h.setEnabled(td.isEnabled());
                h.setDescription(td.getDescription());
                
                h.setCronExpression(td.getCronExpression());
                h.setDuration(td.getDuration());
                h.setRepeat(td.getRepeat());
                h.setInterval(td.getInterval());
                h.setMessage(td.getMessage());
            }
        }
        
        return binding;
    }
    
    public static Binding commitWSDLModel(WSDLModel wsdlModel,
            SchedulerModel schedulerModel) {
        Binding binding = null;
        WSDLComponent[] portOperation = findPortOperation(wsdlModel);
        if (portOperation != null) {
            binding = commitWSDLModel(wsdlModel, schedulerModel,
                    (Port) portOperation[0], (Operation) portOperation[1]);
        }
        return binding;
    }
}
