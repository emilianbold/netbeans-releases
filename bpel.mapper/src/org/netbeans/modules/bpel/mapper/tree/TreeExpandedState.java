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
package org.netbeans.modules.bpel.mapper.tree;

import org.netbeans.modules.soa.mappercore.Mapper;

/**
 *
 * @author Alexey
 * @author Vitaly Bychkov
 */
public interface TreeExpandedState {

    /**
     * Mapper should be updated in case if it was rebuilted
     * @param mapper - new mapper value
     */
    void setMapper(Mapper mapper);

    /**
     * Store Expanded state
     */
    void save();

    /**
     * Restore expanded state
     */
    void restore();

    /**
     * Step is a one part of treePath, allows to store and restore tree path for 
     * different tree instances but with the same values
     *
     */
    public static class Step {

        protected String name;
        protected int index;

        public Step(String name, int index) {
            this.name = name;
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return "Name: " + name + " Index: " + index;
        }
    }
}
