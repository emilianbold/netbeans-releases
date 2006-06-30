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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.api.common;
/**
 * Ability to create a new CommonDDBean objects.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 *
 * @author Milan Kuchtiak
 */
public interface CreateCapability {
    /**
     * An empty (not bound to bean graph) CommonDDBean object is created corresponding to beanName,
     * regardless the servlet spec. version
     * @param beanName bean name e.g. "Servlet"
     * @return CommonDDBean object corresponding to beanName value
     */
    public CommonDDBean createBean(String beanName) throws ClassNotFoundException ;
    /**
     * An empty bean is created corresponding to beanName regardless the srvlet spec. version.
     * The bean is nested under the actual bean.<br>
     * There is an array of properties that will be initialized.
     * The method is useful for DD elements containing
     * single properties like Servlet, Taglib, ResourceRef etc.<br>
     * Usage<pre>
...
Servlet servlet = (Servlet)webApp.addBean("Servlet",new String[]{"ServletName","ServletClass"},
                                          new Object[]{"TestServlet","mypackage.TestServlet"},"ServletName");
servlet.addBean("InitParam",new String[]{"ParamName","ParamValue"},
                new Object[]{"car","Volvo"},"ParamName");
...
     *</pre>
     * @param beanName bean name
     * @param propertyNames array of properties that should be initialized
     * @param propertyValues array of initialization values, usually strings
     * @param keyProperty the property name that is checked in order to evoid the duplicity, e.g. ServletName.<br>
     * <b>keyProperty should be included to propertyNames array.</b>
     * @return CommonDDBean object that has been nested inside the current element (CommonDDBean object).
     * @exception ClassNotFoundException thrown when the class for beanName cannot be found under the current DD element
     * @exception NameAlreadyUsedException thrown when object with keyProperty value already exists.
     */   
    public CommonDDBean addBean (String beanName, String[] propertyNames, Object [] propertyValues, String keyProperty)
        throws ClassNotFoundException, NameAlreadyUsedException;
    /**
     * An empty bean is created corresponding to beanName, regardless the servlet spec. version.
     * The bean is included under the actual bean. The method is useful for elements containing only
     * non-single properties like WelcomeFileList, JspConfig etc.
     *<pre>
...
JspConfig config = webApp.addBean("JspConfig");
jspConfig.addBean("JspPropertyGroup",new String[]{"UrlPattern","IncludePrelude","IncludeCoda"},
                  new String[]{"*.jsp","/jsp/prelude.html","/jsp/coda.html"},null);
...
     *</pre>
     * @param beanName bean name e.g. "JspConfig"
     * @return CommonDDBean object corresponding to beanName value
     * @exception ClassNotFoundException thrown when the class for beanName cannot be found under the current DD element
     */
    public CommonDDBean addBean (String beanName) throws ClassNotFoundException;
}
