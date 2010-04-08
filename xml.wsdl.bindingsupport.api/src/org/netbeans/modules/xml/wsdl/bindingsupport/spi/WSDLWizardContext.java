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

import java.beans.PropertyChangeListener;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.WizardDescriptor;

/**
 * Provides the context implementation for implementors to use for customizing wsdl wizard
 * 
 * @author skini
 */
public interface WSDLWizardContext {

    public final static String PROP_HAS_NEXT = "hasNext";
    public final static String PROP_STEPS_CHANGED = "STEPS_CHANGED";
    /**
     * The wsdl model being used by the wizard.
     * 
     * @return WSDLModel
     */
    public WSDLModel getWSDLModel();
    
    /**
     * Returns the current WSDLExtensionIterator being used.
     * 
     * @return WSDLExtensionIterator
     * 
     */
    public WSDLWizardExtensionIterator getWSDLExtensionIterator();

    /**
     * Used to control the Next button.
     * In case the panel decides to disable/enable the next button, 
     * set true to enable.
     * 
     * @param hasNext true to enable, false to disable
     */
    public void setHasNext(boolean hasNext);

    /**
     * Get the current step index in the main wsdl wizard iterator.
     * 
     * @return current index
     */
    Integer getStepIndex();

    
    /**
     * Gets the current steps shown in the main wsdl wizard iterator.
     * 
     * @return steps
     */
    String[] getSteps();

    
    /**
     * Returns the current hasNext value of the main wsdl wizard iterator.
     * @return true if next is enabled else false.
     */
    boolean hasNext();

        
    /**
     * Returns the current hasPrevious value of the main wsdl wizard iterator.
     * @return true if next is enabled else false.
     */
    boolean hasPrevious();
    
    
    /**
     * Add propertychange listener if you want to listen to events in main wsdl wizard iterator.
     * 
     * @param l
     */
    void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Remove propertychange listener.
     * 
     * @param l
     */
    void removePropertyChangeListener(PropertyChangeListener l);
    
    
    /**
     * Fire property change events, events recognized are PROP_STEPS_CHANGED and PROP_HAS_NEXT.
     * 
     * @param propertyName
     * @param o
     * @param n
     */
    void firePropertyChange(String propertyName, Object o, Object n);

    WizardDescriptor getWizardDescriptor(); 
}
