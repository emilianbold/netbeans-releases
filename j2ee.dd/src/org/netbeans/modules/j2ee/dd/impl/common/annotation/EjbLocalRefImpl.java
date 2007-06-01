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
package org.netbeans.modules.j2ee.dd.impl.common.annotation;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.InjectionTarget;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

/**
 *
 * @author Martin Adamek
 */
public class EjbLocalRefImpl implements EjbLocalRef {

    private final String name;
    private final String beanInterface;
    private final String beanName;
    private final String mappedName;
    private final String description;

    public EjbLocalRefImpl(String name, String beanInterface, String beanName, String mappedName, String description) {
        this.name = name;
        this.beanInterface = beanInterface;
        this.beanName = beanName;
        this.mappedName = mappedName;
        this.description = description;
    }
    
    // <editor-fold desc="Model implementation">

    public String getEjbRefName() {
        return name;
    }

    public String getLocal() {
        return beanInterface;
    }

    public String getEjbLink() {
        return beanName;
    }

    public String getMappedName() throws VersionNotSupportedException {
        return mappedName;
    }

    public String getDefaultDescription() {
        return description;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">
    
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbRefName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbRefType(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getEjbRefType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLocalHome(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLocalHome() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLocal(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbLink(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMappedName(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setInjectionTarget(int index, InjectionTarget valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InjectionTarget getInjectionTarget(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeInjectionTarget() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setInjectionTarget(InjectionTarget[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InjectionTarget[] getInjectionTarget() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addInjectionTarget(InjectionTarget valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeInjectionTarget(InjectionTarget valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InjectionTarget newInjectionTarget() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String description) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAllDescriptions(Map descriptions) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map getAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // </editor-fold>

}
