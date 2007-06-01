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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.impl.common.annotation;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.common.InjectionTarget;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

/**
 * Default implemetation of {@link MessageDestinationRef}.
 * For getting all {@link MessageDestinationRef}s use one of
 * {@link CommonAnnotationHelper#getMessageDestinationRefs CommonAnnotationHelper.getMessageDestinationRefs()} methods.
 * <p>
 * For more info see {@link ResourceImpl}.
 * @author Tomas Mysik
 */
public class MessageDestinationRefImpl implements MessageDestinationRef {
    
    private final ResourceImpl resource;
    
    /**
     * Create a new instance of MessageDestinationRefImpl.
     * @param resource {@link javax.annotation.Resource @Resource} implementation.
     */
    public MessageDestinationRefImpl(ResourceImpl resource) {
        this.resource = resource;
    }
    
    // <editor-fold desc="Model implementation">
    public String getMessageDestinationRefName() {
        return resource.getName();
    }

    public String getMessageDestinationType() {
        return resource.getType();
    }

    public String getMappedName() throws VersionNotSupportedException {
        return resource.getMappedName();
    }

    public String getDefaultDescription() {
        return resource.getDescription();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">
    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestinationRefName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestinationType(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestinationUsage(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getMessageDestinationUsage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestinationLink(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getMessageDestinationLink() {
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
