/*
 * ExtendedStandardMBeanIterator.java
 *
 * Created on January 8, 2007, 4:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.jmx.mbeanwizard;

import org.netbeans.modules.jmx.WizardConstants;

/**
 *
 * @author jfdenise
 */
public class ExtendedStandardMBeanIterator extends DynamicMBeanIterator {

    protected String getGeneratedMBeanType() {
        return WizardConstants.MBEAN_EXTENDED;
    }
   
    public static ExtendedStandardMBeanIterator createExtendedStandardMBeanIterator() {
        return new ExtendedStandardMBeanIterator();
    }
}
