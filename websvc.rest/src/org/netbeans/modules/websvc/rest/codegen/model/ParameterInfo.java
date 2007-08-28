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
package org.netbeans.modules.websvc.rest.codegen.model;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.wizard.Util;

/**
 *
 * @author Peter Liu
 */
public class ParameterInfo {

    private String name;
    private Class type;
    private Object defaultValue;
    private boolean isQueryParam;

    public ParameterInfo(String name, Class type) {
        this.name = name;
        this.type = type;
        this.defaultValue = null;
        this.isQueryParam = true;
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }

    public void setDefaultValue(Object value) {
        this.defaultValue = value;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean isQueryParam() {
        return isQueryParam;
    }

    public void setIsQueryParam(boolean flag) {
        this.isQueryParam = flag;
    }
}