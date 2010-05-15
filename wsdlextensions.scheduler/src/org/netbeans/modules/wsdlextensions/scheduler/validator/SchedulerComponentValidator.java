/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.wsdlextensions.scheduler.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import java.util.TimeZone;
import org.netbeans.modules.wsdlextensions.scheduler.configeditor.CronTriggerPanel;
import org.netbeans.modules.wsdlextensions.scheduler.configeditor.HybridTriggerPanel;
import org.netbeans.modules.wsdlextensions.scheduler.configeditor.SchedulerArgumentException;
import org.netbeans.modules.wsdlextensions.scheduler.model.CronTrigger;
import org.netbeans.modules.wsdlextensions.scheduler.model.HybridTrigger;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerActivePeriod;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerBinding;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerComponent;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerConstants;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerOperation;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerTrigger;
import org.netbeans.modules.wsdlextensions.scheduler.model.SimpleTrigger;
import org.netbeans.modules.wsdlextensions.scheduler.utils.Utils;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;

/**
 * This class enables semantic validations for
 * File WSDL documents.
 *
 * @author sweng
 */
public class SchedulerComponentValidator
        implements Validator, SchedulerComponent.Visitor, SchedulerConstants {

    private Validation mValidation;
    private ValidationType mValidationType;
    private Collection<ResultItem> results = new ArrayList<ResultItem>();
    private boolean triggerFound = false;
    private StringBuilder triggerNames = null;
    private SimpleDateFormat dateFormat = null;
    
    @SuppressWarnings("unchecked")
    public static final ValidationResult EMPTY_RESULT = 
        new ValidationResult( Collections.EMPTY_SET, 
                Collections.EMPTY_SET);
    
    public SchedulerComponentValidator() {}
    
    /**
     * Returns name of this validation service.
     */
    public String getName() {
        return getClass().getName();
    }
    
    /**
     * Validates given model.
     *
     * @param model model to validate.
     * @param validation reference to the validation context.
     * @param validationType the type of validation to perform
     * @return ValidationResult.
     */
    public ValidationResult validate(Model model, Validation validation,
            ValidationType validationType) {
        results.clear();
        mValidation = validation;
        mValidationType = validationType;
        triggerFound = false;
        triggerNames = new StringBuilder();
        
        HashSet<Model> models = new HashSet<Model>();
        models.add(model);
        ValidationResult validationResult =
                new ValidationResult(results, models);
        
        if (!(model instanceof WSDLModel)) {
            // Clear out our state
            mValidation = null;
            mValidationType = null;
            triggerFound = false;
            dateFormat = null;
            return validationResult;
        }
        
        // Traverse the model
        WSDLModel wsdlModel = (WSDLModel)model;

        if (model.getState() == State.NOT_WELL_FORMED) {
            return EMPTY_RESULT;
        }

        Definitions defs = wsdlModel.getDefinitions();
        Collection<Binding> bindings = defs.getBindings();

        for (Binding binding : bindings) {
            if ((binding.getType() == null)
                    || (binding.getType().get() == null)) {
                continue;
            }

            List<SchedulerBinding> schedulerBindings = binding
                    .getExtensibilityElements(SchedulerBinding.class);

            if (Utils.isEmpty(schedulerBindings)) {
                continue;
            }

            if (schedulerBindings.size() != 1) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, binding,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.binding.oneBindingOnly")));    //NOI18N
            } else {
                SchedulerBinding schedulerBinding = schedulerBindings.get(0);
                schedulerBinding.accept(this);
            }

            boolean foundSchedulerOp = false;
            int count = 0;
            for (BindingOperation bindingOp
                    : binding.getBindingOperations()) {
                List<SchedulerOperation> schedulerOps = bindingOp
                        .getExtensibilityElements(SchedulerOperation.class);

                for (SchedulerOperation schedulerOp : schedulerOps) {
                    schedulerOp.accept(this);
                }

                if (schedulerOps.size() > 0) {
                    foundSchedulerOp = true;
                    BindingInput bindingInput = bindingOp.getBindingInput();
                    if (bindingInput != null) {
                        List<SchedulerTrigger> triggers = bindingInput
                                .getExtensibilityElements(
                                        SchedulerTrigger.class);
                        if (!Utils.isEmpty(triggers)) {
                            for (SchedulerTrigger st
                                    : bindingInput.getExtensibilityElements(
                                            SchedulerTrigger.class)) {
                                st.accept(this);
                            }
                        }
                    }
                }
            }
            
            if (!triggerFound) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, binding,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.trigger.noneEnabled",  //NOI18N
                    binding.getName())));
            }
            // validating: sched:binding found but no sched:operation is defined
            if (!foundSchedulerOp ) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, binding,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.operation.noneFound",  //NOI18N
                    binding.getName())));
            }
            // validating: found sched:operation but no sched:binding is defined
            if ((schedulerBindings.size() == 0) && foundSchedulerOp) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, binding,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.binding.noneFound",//NOI18N
                        binding.getName())));
            }
        }

        for (Service service : defs.getServices()) {
            for (Port port : service.getPorts()) {
                if (port.getBinding() != null) {
                    Binding binding = port.getBinding().get();
                    if (binding != null) {
                        int numRelatedSchedulerBindings =
                                binding.getExtensibilityElements(
                                        SchedulerBinding.class).size();
                        List<SchedulerActivePeriod> activePeriods =
                                port.getExtensibilityElements(
                                        SchedulerActivePeriod.class);
                        if (numRelatedSchedulerBindings > 0) {
                            if (activePeriods.size() == 0) {
                            results.add(new Validator.ResultItem(this,
                                Validator.ResultType.ERROR, port,
                                NbBundle.getMessage(
                                    SchedulerComponentValidator.class,
                                    "SchedulerComponentValidator.port.noActivePeriod",  //NOI18N
                                    port.getName())));
                            } else if (activePeriods.size() > 1) {
                                results.add(new Validator.ResultItem(this,
                                    Validator.ResultType.ERROR, port,
                                    NbBundle.getMessage(
                                        SchedulerComponentValidator.class,
                                        "SchedulerComponentValidator.activePeriod.tooMany", //NOI18N
                                        port.getName())));
                            }
                        }
                        for (SchedulerActivePeriod ap : activePeriods) {
                            ap.accept(this);
                        }
                    }
                }
            }
        }
        // Clear out our state
        mValidation = null;
        mValidationType = null;
        
        return validationResult;
    }

    public void visit(SchedulerBinding target) {
        if (Utils.isEmpty(target.getGroup())) {
            results.add(new Validator.ResultItem(this,
                Validator.ResultType.ERROR, target,
                NbBundle.getMessage(SchedulerComponentValidator.class,
                    "SchedulerComponentValidator.schedBinding.noGroup", //NOI18N
                    ((Binding) target.getParent()).getName())));
        }
        if (Utils.isEmpty(target.getDateFormat())) {
            results.add(new Validator.ResultItem(this,
                Validator.ResultType.WARNING, target,
                NbBundle.getMessage(SchedulerComponentValidator.class,
                    "SchedulerComponentValidator.schedBinding.noDateFormat",    //NOI18N
                    ((Binding) target.getParent()).getName())));
        } else {
            try {
                dateFormat = new SimpleDateFormat(target.getDateFormat());
            } catch (IllegalArgumentException iae) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, target,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.schedBinding.badDateFormat",   //NOI18N
                        target.getDateFormat(),
                        ((Binding) target.getParent()).getName())));
            }
        }
    }

    public void visit(SchedulerOperation target) {
        if (Utils.isEmpty(target.getMode())) {
            results.add(new Validator.ResultItem(this,
                Validator.ResultType.ERROR, target,
                NbBundle.getMessage(SchedulerComponentValidator.class,
                    "SchedulerComponentValidator.schedOperation.noMode",//NOI18N
                    ((BindingOperation) target.getParent()).getName())));
        } else if (OperationMode.toEnum(target.getMode()) == null) {
             results.add(new Validator.ResultItem(this,
                Validator.ResultType.ERROR, target,
                NbBundle.getMessage(SchedulerComponentValidator.class,
                    "SchedulerComponentValidator.schedOperation.badMode",   //NOI18N
                    target.getMode(),
                    ((BindingOperation) target.getParent()).getName())));
        }
    }

    public void visit(SchedulerTrigger target) {
        int startNumResults = results.size();
        if (Utils.isEmpty(target.getName())) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, target,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.trigger.noName",   //NOI18N
                        ((BindingInput) target.getParent()).getName())));
        } else if (triggerNames.indexOf(target.getName()) != -1) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, target,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.trigger.duplicateName",    //NOI18N
                        target.getName(),
                        ((BindingInput) target.getParent()).getName())));
        } else {
            triggerNames.append(target.getName()).append('|');          //NOI18N
        }
        
        if (Utils.isEmpty(target.getType())) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, target,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.trigger.noType",   //NOI18N
                        ((BindingInput) target.getParent()).getName())));
        } else if (TriggerType.toEnum(target.getType()) == null) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, target,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.trigger.badType",  //NOI18N
                        target.getType(),
                        ((BindingInput) target.getParent()).getName())));
        }
        
        if (target instanceof SimpleTrigger) {
            SimpleTrigger st = (SimpleTrigger) target;
            
            // Runtime interprets empty repeat as indefinite
            if (!Utils.isEmpty(st.getRepeat())) {
                if (!INDEFINITE_VAL.equalsIgnoreCase(st.getRepeat())) {
                    boolean bad = false;
                    try {
                        int repeat = Integer.parseInt(st.getRepeat());
                        if (repeat < 0) {
                            bad = true;
                        }
                    } catch (NumberFormatException nfe) {
                        bad = true;
                    }
                    if (bad) {
                        results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR, st,
                            NbBundle.getMessage(
                                SchedulerComponentValidator.class,
                                "SchedulerComponentValidator.trigger.badRepeat",//NOI18N
                                st.getRepeat(), ((BindingInput) st.getParent())
                                    .getName(), st.getType())));
                    }
                }
            }
            
            if (Utils.isEmpty(st.getIntervalAsString())) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, st,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.trigger.noInterval",       //NOI18N
                        ((BindingInput) st.getParent()).getName(),
                        st.getType())));
            } else {
                boolean bad = false;
                try {
                    long interval = Long.parseLong(st.getIntervalAsString());
                    if (interval <= 0L) {
                        bad = true;
                    }
                } catch (NumberFormatException nfe) {
                    bad = true;
                }
                if (bad) {
                    results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR, st,
                        NbBundle.getMessage(
                            SchedulerComponentValidator.class,
                            "SchedulerComponentValidator.trigger.badInterval",  //NOI18N
                            st.getIntervalAsString(), ((BindingInput) st
                                .getParent()).getName(), st.getType())));
                }
            }
        } else if (target instanceof CronTrigger) {
            CronTrigger ct = (CronTrigger) target;
            if (Utils.isEmpty(ct.getCronExpression())) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, ct,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.trigger.noCronExpr",//NOI18N
                        ((BindingInput) ct.getParent()).getName(),
                        ct.getType())));
            } else {
                try {
                    CronTriggerPanel.parseCronExpression(ct.getCronExpression(),
                            null);
                } catch (SchedulerArgumentException sae) {
                    String errMsg = sae.getMessage();
                    results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR, ct,
                        NbBundle.getMessage(SchedulerComponentValidator.class,
                            "SchedulerComponentValidator.trigger.badCronExpr",//NOI18N
                            ((BindingInput) ct.getParent()).getName(),
                            (Utils.isEmpty(errMsg) ? "" : ": " + errMsg),//NOI18N
                            ct.getType())));
                }
            }
        } else if (target instanceof HybridTrigger) {
            HybridTrigger ht = (HybridTrigger) target;
            if (Utils.isEmpty(ht.getCronExpression())) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, ht,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.trigger.noCronExpr",//NOI18N
                        ((BindingInput) ht.getParent()).getName(),
                        ht.getType())));
            } else {
                try {
                    HybridTriggerPanel.parseCronExpression(ht.getCronExpression(),
                            null);
                } catch (SchedulerArgumentException sae) {
                    String errMsg = sae.getMessage();
                    results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR, ht,
                        NbBundle.getMessage(SchedulerComponentValidator.class,
                            "SchedulerComponentValidator.trigger.badCronExpr",//NOI18N
                            ((BindingInput) ht.getParent()).getName(),
                            (Utils.isEmpty(errMsg) ? "" : ": " + errMsg),//NOI18N
                            ht.getType())));
                }
            }
            
            if (Utils.isEmpty(ht.getDurationAsString())) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, ht,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.trigger.noDuration",
                        ((BindingInput) ht.getParent()).getName())));   //NOI18N
            } else {
                boolean bad = false;
                try {
                    long duration = Long.parseLong(ht.getDurationAsString());
                    if (duration <= 0L) {
                        bad = true;
                    }
                } catch (NumberFormatException nfe) {
                    bad = true;
                }
                if (bad) {
                    results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR, ht,
                        NbBundle.getMessage(
                            SchedulerComponentValidator.class,
                            "SchedulerComponentValidator.trigger.badDuration",  //NOI18N
                            ht.getDurationAsString(), ((BindingInput) ht
                                .getParent()).getName())));
                }
            }
            
            // Runtime interprets empty repeat as indefinite
            if (!Utils.isEmpty(ht.getRepeat())) {
                if (!INDEFINITE_VAL.equalsIgnoreCase(ht.getRepeat())) {
                    boolean bad = false;
                    try {
                        int repeat = Integer.parseInt(ht.getRepeat());
                        if (repeat < 0) {
                            bad = true;
                        }
                    } catch (NumberFormatException nfe) {
                        bad = true;
                    }
                    if (bad) {
                        results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR, ht,
                            NbBundle.getMessage(
                                SchedulerComponentValidator.class,
                                "SchedulerComponentValidator.trigger.badRepeat",//NOI18N
                                ht.getRepeat(), ((BindingInput) ht.getParent())
                                    .getName(), ht.getType())));
                    }
                }
            }
            
            if (Utils.isEmpty(ht.getIntervalAsString())) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, ht,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.trigger.noInterval",       //NOI18N
                        ((BindingInput) ht.getParent()).getName(),
                        ht.getType())));
            } else {
                boolean bad = false;
                try {
                    long interval = Long.parseLong(ht.getIntervalAsString());
                    if (interval <= 0L) {
                        bad = true;
                    }
                } catch (NumberFormatException nfe) {
                    bad = true;
                }
                if (bad) {
                    results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR, ht,
                        NbBundle.getMessage(
                            SchedulerComponentValidator.class,
                            "SchedulerComponentValidator.trigger.badInterval",  //NOI18N
                            ht.getIntervalAsString(), ((BindingInput) ht
                                .getParent()).getName(), ht.getType())));
                }
            }
        }
            
        if (Utils.isEmpty(target.getMessage())) {
            results.add(new Validator.ResultItem(this,
                Validator.ResultType.ERROR, target,
                NbBundle.getMessage(SchedulerComponentValidator.class,
                    "SchedulerComponentValidator.trigger.noMessage",    //NOI18N
                    ((BindingInput) target.getParent()).getName())));
        }
        
        if (results.size() == startNumResults) {
            triggerFound = triggerFound || target.isEnabled();
        }
    }

    public void visit(SchedulerActivePeriod target) {
        Date startDate = new Date();
        Date endDate = new Date(Long.MAX_VALUE);
        
        if (!Utils.isEmpty(target.getStarting())) {
            if (!NOW_VAL.equalsIgnoreCase(target.getStarting())) {
                if (dateFormat != null) {
                    try {
                        startDate = dateFormat.parse(target.getStarting());
                    } catch (ParseException ex) {
                        results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR, target,
                            NbBundle.getMessage(SchedulerComponentValidator.class,
                                "SchedulerComponentValidator.activePeriod.badStart",    //NOI18N
                                target.getStarting(),
                                ((Port) target.getParent()).getName())));   //NOI18N
                        startDate = null;
                    }
                }
            }
        }
        
        if (!Utils.isEmpty(target.getEnding())) {
            if (!NEVER_VAL.equalsIgnoreCase(target.getEnding())) {
                if (dateFormat != null) {
                    try {
                        endDate = dateFormat.parse(target.getEnding());
                    } catch (ParseException ex) {
                        results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR, target,
                            NbBundle.getMessage(SchedulerComponentValidator.class,
                                "SchedulerComponentValidator.activePeriod.badEnd",  //NOI18N
                                target.getEnding(),
                                ((Port) target.getParent()).getName())));   //NOI18N
                        endDate = null;
                    }
                }
            }
        }
        
        if ((startDate != null) && (endDate != null)) {
            if (startDate.compareTo(endDate) > 0) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, target,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.activePeriod.endBeforeStart",  //NOI18N
                        new Object[] {target.getEnding(), target.getStarting(),
                            ((Port) target.getParent()).getName()})));  //NOI18N
            }
        }
        
        if (!Utils.isEmpty(target.getTimeZone())) {
            boolean found = false;
            String timezone = target.getTimeZone();
            for (String tz : TimeZone.getAvailableIDs()) {
                if (timezone.equals(tz)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR, target,
                    NbBundle.getMessage(SchedulerComponentValidator.class,
                        "SchedulerComponentValidator.activePeriod.badTimezone",//NOI18N
                        ((Port) target.getParent()).getName(),
                        timezone)));
            }
        }
    }
}
