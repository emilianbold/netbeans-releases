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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;


/**
 *
 * @author Peter Williams
 */
public class DDBinding implements Comparable<DDBinding> {

    protected final BeanResolver resolver;
    protected final CommonDDBean sunBean;
    protected final org.netbeans.modules.j2ee.dd.api.common.CommonDDBean ddBean;
    protected final org.netbeans.modules.j2ee.dd.api.common.CommonDDBean annotatedBean;
    protected boolean virtual;
    protected Map<String, String> propertyMap;

    public DDBinding(BeanResolver resolver, CommonDDBean sunBean,
            org.netbeans.modules.j2ee.dd.api.common.CommonDDBean ddBean,
            org.netbeans.modules.j2ee.dd.api.common.CommonDDBean annotatedBean) {
        this(resolver, sunBean, ddBean, annotatedBean, false);
    }

    public DDBinding(BeanResolver resolver, CommonDDBean sunBean,
            org.netbeans.modules.j2ee.dd.api.common.CommonDDBean ddBean,
            org.netbeans.modules.j2ee.dd.api.common.CommonDDBean annotatedBean,
            boolean virtual) {
        this.resolver = resolver;
        this.sunBean = sunBean;
        this.ddBean = ddBean;
        this.annotatedBean = annotatedBean;
        this.virtual = virtual;
        this.propertyMap = new HashMap<String, String>();
    }

    public String getBeanName() {
        return resolver.getBeanName(sunBean);
    }
    
    public String getBindingName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean ddBean) {
        return resolver.getBeanName(ddBean);
    }
    
    public String getBindingName() {
        return ddBean != null ? getBindingName(ddBean) : (annotatedBean != null ? getBindingName(annotatedBean) : "");
    }
    
    public CommonDDBean getSunBean() {
        return sunBean;
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean getStandardBean() {
        return ddBean;
    }
    
    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean getAnnotatedBean() {
        return annotatedBean;
    }
    
    public String getProperty(String propertyName) {
        return propertyMap.get(propertyName);
    }
    
    public boolean isBound() {
        return ddBean != null || annotatedBean != null;
    }

    public boolean isAnnotated() {
        return annotatedBean != null;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public void clearVirtual() {
        virtual = false;
    }

    public int compareTo(DDBinding other) {
        return getBeanName().compareTo(other.getBeanName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DDBinding other = (DDBinding) obj;
        if (this.sunBean != other.sunBean && (this.sunBean == null || !this.sunBean.equals(other.sunBean))) {
            return false;
        }
        if (this.ddBean != other.ddBean && (this.ddBean == null || !this.ddBean.equals(other.ddBean))) {
            return false;
        }
        if (this.annotatedBean != other.annotatedBean && (this.annotatedBean == null || !this.annotatedBean.equals(other.annotatedBean))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.sunBean != null ? this.sunBean.hashCode() : 0);
        hash = 31 * hash + (this.ddBean != null ? this.ddBean.hashCode() : 0);
        hash = 31 * hash + (this.annotatedBean != null ? this.annotatedBean.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(128);
        builder.append("Sun DD: ");
        builder.append(sunBean != null ? getBeanName() : "(null)");
        builder.append(", Standard DD: ");
        builder.append(ddBean != null ? getBindingName(ddBean) : "(null)");
        builder.append(", Annotation: ");
        builder.append(annotatedBean != null ? getBindingName(annotatedBean) : "(null)");
        return builder.toString();
    }
    
}
