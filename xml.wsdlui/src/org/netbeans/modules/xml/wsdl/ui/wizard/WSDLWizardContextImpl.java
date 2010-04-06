/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xml.wsdl.ui.wizard;

import org.netbeans.modules.xml.wsdl.ui.wizard.common.WSDLWizardIterator;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardExtensionIterator;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.loaders.TemplateWizard;
import org.openide.WizardDescriptor;

/**
 *
 * @author skini
 */
public class WSDLWizardContextImpl implements WSDLWizardContext {
    private WSDLWizardIterator iterator;
    private TemplateWizard wiz;
    
    private WSDLWizardExtensionIterator extensionIterator;
    
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private boolean hasNext = true;
    private List<String> steps = new ArrayList<String>();
    private String[] currentSteps;
    private List<String> virtualSteps = new ArrayList<String>();
    private WSDLModel wsdlModel;
    
    public WSDLWizardContextImpl(WSDLWizardIterator iterator, TemplateWizard wiz) {
        this.iterator = iterator;
        this.wiz = wiz;
        
    }
    

    public Integer getStepIndex() {
        return iterator.getCurrentStepIndex();
    }

    public String[] getSteps() {
        return currentSteps;
    }

    public boolean hasNext() {
        return hasNext && extensionIterator != null ? extensionIterator.hasNext() : false;
    }

    public boolean hasPrevious() {
        return true;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
        
    }
    
    public void firePropertyChange(String propertyName, Object o, Object n) {
        propertyChangeSupport.firePropertyChange(propertyName, o, n);
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
        firePropertyChange("hasNext", !hasNext, hasNext);
    }
    
    public void stepsChanged() {
            if (extensionIterator == null) {
            currentSteps = steps.toArray(new String[steps.size()]);
        } else {
            String[] extensionSteps = extensionIterator.getSteps();

            if (extensionSteps == null) {
                currentSteps = steps.toArray(new String[steps.size()]);
            } else {
                currentSteps = new String[steps.size() + extensionSteps.length];
                int j = 0;
                for (int i = 0; i < steps.size(); i++) {
                    currentSteps[i] = steps.get(i);
                    j = i;
                }
                j++;
                for (int i = 0; i < extensionSteps.length; i++, j++) {
                    currentSteps[j] = extensionSteps[i];
                }
            }
        }
        synchVirtualSteps();
        firePropertyChange("STEPS_CHANGED", null, currentSteps);
    }
    
    public void setWSDLExtensionIterator(WSDLWizardExtensionIterator iterator) {
        extensionIterator = iterator;
        stepsChanged();
    }
    
    public WSDLWizardExtensionIterator getWSDLExtensionIterator() {
        return extensionIterator;
    }
    
    public void setInitialSteps(String[] steps) {
        this.steps = Arrays.asList(steps);
        stepsChanged();
    }

    public WSDLModel getWSDLModel() {
        return wsdlModel;
    }

    public void setWSDLModel(WSDLModel wsdlModel) {
        this.wsdlModel = wsdlModel;
    }
    
    public WizardDescriptor getWizardDescriptor() {
	return (WizardDescriptor) wiz;
    }

    public void addStep(ArrayList<String> virtualSteps) {
        this.virtualSteps = virtualSteps;
    }

    
    private void synchVirtualSteps() {
        if ( virtualSteps != null) {
            String[] currSteps = currentSteps;
            int oldLen = currSteps.length;
            int newLen = oldLen+virtualSteps.size();
            currentSteps = new String[newLen];
            System.arraycopy(currSteps, 0, currentSteps, 0, oldLen);
            System.arraycopy(virtualSteps.toArray(new String[0]), 0,currentSteps, oldLen, virtualSteps.size());
            firePropertyChange("STEPS_CHANGED", null, currentSteps);
        }

        
    }

}
