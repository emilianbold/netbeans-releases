/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * File       : IIfVariable.java
 * Created on : Oct 30, 2003
 * Author     : aztec
 */
package org.netbeans.modules.uml.core.generativeframework;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 */
public interface IIfVariable extends ICompoundVariable
{
    public void addTest(IIfTest pTest);

    public void removeTest(IIfTest pTest);

    public ETList<IIfTest> getTests();

    /**
     * Expands at most one of the tests that make up this variable. The tests
     * are executed in the order they are added.
     *
     * @param context   The context element for the expansions
     * @return The results of this variable
     */
    public String expand(Node context);
    
    public int getKind();
    
    public void setKind(int kind);
}