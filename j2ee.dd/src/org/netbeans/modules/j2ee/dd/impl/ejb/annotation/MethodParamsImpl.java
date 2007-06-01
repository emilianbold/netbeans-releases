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
import org.netbeans.modules.j2ee.dd.api.ejb.MethodParams;

public class MethodParamsImpl implements MethodParams {

    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setMethodParam(int index, String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getMethodParam(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMethodParam(String[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] getMethodParam() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeMethodParam() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addMethodParam(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeMethodParam(String value) {
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

