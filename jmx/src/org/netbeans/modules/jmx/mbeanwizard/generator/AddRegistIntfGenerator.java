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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard.generator;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.jmx.JavaModelHelper;
import org.openide.filesystems.FileObject;

/**
 *
 *  MBeanRegistration interface implementation code generator class.
 */
public class AddRegistIntfGenerator
{   
    /**
     * Entry point to generate Mbean registration code in mbean class.
     * @param mbeanClass <CODE>JavaClass</CODE> the MBean class to update
     * @param keepRefSelected <CODE>boolean</CODE> keep preRegister method 
     * parameters is selected
     * @param mbeanRes <CODE>Resource</CODE> represents MBean class
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     */
    public void update(FileObject fo, boolean keepRefSelected)
           throws java.io.IOException, Exception
    {
        JavaSource js = JavaModelHelper.getSource(fo);
       JavaModelHelper.updateMBeanWithRegistration(js, keepRefSelected);
    }
    
}
