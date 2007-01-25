/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.builder.dbmodel.impl;

public class ParameterDescriptor extends DBObjectImpl {
    private int ordinalPosition = 0;

    private String paramType = "";

    public String getJavaType() {
        return this.javaType;
    }

    private String javaType = "";

    /**
     * * constructor
     */
    public ParameterDescriptor() {
    }

    /**
     * getter for paramType; *
     * 
     * @return paramType;
     */
    public String getParamType() {
        return this.paramType;
    }

    /**
     * setter for paramType; *
     * 
     * @param paramType parameter type;
     */
    public void setParamType(final String paramType) {
        this.paramType = paramType;
    }

    /**
     * getter for ordinalPosition; *
     * 
     * @return ordinalPosition;
     */
    public int getOrdinalPosition() {
        return this.ordinalPosition;
    }

    /**
     * setter for ordinalPosition; *
     * 
     * @param ordinalPosition position of the parameter;
     */
    public void setOrdinalPosition(final int ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public void setJavaType(final String s) {
        this.javaType = s;
    }

    public void setNumericScale(final int i) {
    }

    public void setSQLType(final String sqlType) {
    }
}
