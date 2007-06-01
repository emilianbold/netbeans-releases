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

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.Method;
import org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission;

public class MethodPermissionImpl implements MethodPermission {

    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setRoleName(int index, String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getRoleName(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setRoleName(String[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] getRoleName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeRoleName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeRoleName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addRoleName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setUnchecked(boolean value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isUnchecked() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMethod(int index, Method value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Method getMethod(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMethod(Method[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Method[] getMethod() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addMethod(Method value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeMethod() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeMethod(Method value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Method newMethod() {
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

}

