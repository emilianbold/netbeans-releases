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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;

/**
 *
 * @author Peter Williams
 */
public interface BeanResolver {
    
    // for sun beans
    
    public CommonDDBean createBean();
    
    public String getBeanName(CommonDDBean sunBean);
    
    public void setBeanName(CommonDDBean sunBean, String name);
    
    public String getSunBeanNameProperty();
    
    // for standard beans
    
    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean);
    
    public String getStandardBeanNameProperty();
    
}
