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

package org.netbeans.modules.xml.wsdl.bindingsupport.spi;

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;

/**
 * Interface for implementing wsdl wizard extension iterator.
 * 
 * @author skini
 */
public abstract class WSDLWizardExtensionIterator implements WizardDescriptor.Iterator {
    private WSDLWizardContext context;

    public WSDLWizardExtensionIterator(WSDLWizardContext context) {
        this.context = context;
    }
   
    /**
     * Called when the template is changed in the wizard name and location panel.
     * Use this method to initialize panels, and steps.
     * 
     * @param templateName
     */
    public abstract void setTemplateName(String templateName);
    
    /**
     * Called when Finish is clicked on the wsdl wizard. Commit all values collected into the WSDLModel.
     * 
     * @return true if successful.
     */
    public abstract boolean commit();
    
    
    /**
     * Can be used to cleanup long living objects, like connections etc.
     */
    public void cleanup() {
        
    }
    
    public WSDLWizardContext getWSDLWizardContext() {
        return context;
    }

    public abstract WSDLWizardDescriptorPanel current();

    


    /**
     * Return all the step names to be shown on the left hand side of the wizard.
     * 
     * @return array of step names
     */
    public abstract String[] getSteps();

    
    /**
     * Return true if there is a next step.
     * @return true if next should be enabled.
     */
    public abstract boolean hasNext();

    
     /**
     * Return true if there is a previous step. This generally should return true.
     * @return true if previous should be enabled.
     */
    public abstract boolean hasPrevious();

    /**
     * Callback so that the extension iterator know that next button is clicked.
     * Can be used to increment the counter, or some other logic to calculate the next step.
     */
    public abstract void nextPanel();

    /**
     * Callback so that extension iterator knows that previous button was clicked.
     * 
     */
    public abstract void previousPanel();

    //Following methods are there for satisfying the WizardDesciptor.Iterator interfaces.
    //Need not be implemented.
    /**
     * Not used.
     * @return
     */
    public final String name() {
        return "";
    }
    /**
     * Not used
     * @param l
     */
    public void addChangeListener(ChangeListener l) {
        
    }

    /**
     * Not used.
     * @param l
     */
    public void removeChangeListener(ChangeListener l) {
        
    }

}
