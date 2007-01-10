/*
 * MXBeanIterator.java
 *
 * Created on January 8, 2007, 3:59 PM
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
public class MXBeanIterator extends MBeanIterator {
    
    /**
     * Method called with the menu new->file->Standard MBean which provides
     * an instance of an iterator
     * @return JMXMBeanIterator an iterator
     */
    public static MXBeanIterator createMXBeanIterator() {
         return new MXBeanIterator();
    }
    
    public String getGeneratedMBeanType() {
        return WizardConstants.MXBEAN;
    }
}
