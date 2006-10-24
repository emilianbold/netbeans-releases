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

package org.netbeans.modules.java.guards;

/** Class for holding information about the one special (guarded)
* comment. It is created by GuardedReader and used by
* JavaEditor to creating the guarded sections.
*/
public final class SectionDescriptor {
    /** Type - one of T_XXX constant */
    private GuardTag type;

    /** Name of the section comment */
    private String name;

    /** offset of the begin */
    private int begin;

    /** offset of the end */
    private int end;

    /** Simple constructor */
    public SectionDescriptor(GuardTag type) {
        this.type = type;
        name = null;
        begin = 0;
        end = 0;
    }
    
    public SectionDescriptor(GuardTag type, String name, int begin, int end) {
        this.type = type;
        this.name = name;
        this.begin = begin;
        this.end = end;
    }

    /** offset of the begin */
    public int getBegin() {
        return begin;
    }

    /** Name of the section comment */
    public String getName() {
        return name;
    }

    /** offset of the end */
    public int getEnd() {
        return end;
    }

    public GuardTag getType() {
        return type;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setName(String name) {
        this.name = name;
    }


}
