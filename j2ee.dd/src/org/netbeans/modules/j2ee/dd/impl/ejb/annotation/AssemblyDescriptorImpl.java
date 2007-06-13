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

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.dd.api.ejb.ExcludeList;
import org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonAnnotationHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;

public class AssemblyDescriptorImpl implements AssemblyDescriptor {

    private final AnnotationModelHelper helper;
    
    public AssemblyDescriptorImpl(AnnotationModelHelper helper) {
        this.helper = helper;
    }
    
    public SecurityRole[] getSecurityRole() {
        return CommonAnnotationHelper.getSecurityRoles(helper);
    }
    
    public MessageDestination[] getMessageDestination() throws VersionNotSupportedException {
        return new MessageDestination[0];
    }

    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">

    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public ContainerTransaction[] getContainerTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ContainerTransaction getContainerTransaction(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setContainerTransaction(ContainerTransaction[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setContainerTransaction(int index, ContainerTransaction value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeContainerTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addContainerTransaction(ContainerTransaction value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeContainerTransaction(ContainerTransaction value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ContainerTransaction newContainerTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MethodPermission[] getMethodPermission() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MethodPermission getMethodPermission(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMethodPermission(MethodPermission[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMethodPermission(int index, MethodPermission value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addMethodPermission(MethodPermission value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeMethodPermission() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeMethodPermission(MethodPermission value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MethodPermission newMethodPermission() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecurityRole getSecurityRole(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSecurityRole(SecurityRole[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSecurityRole(int index, SecurityRole value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeSecurityRole() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeSecurityRole(SecurityRole value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addSecurityRole(SecurityRole value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecurityRole newSecurityRole() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setExcludeList(ExcludeList value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ExcludeList getExcludeList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ExcludeList newExcludeList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestination getMessageDestination(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestination(MessageDestination[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestination(int index, MessageDestination value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeMessageDestination() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeMessageDestination(MessageDestination value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addMessageDestination(MessageDestination value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestination newMessageDestination() throws VersionNotSupportedException {
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

    // </editor-fold>

} 

