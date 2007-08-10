/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */

package org.netbeans.modules.etl.ui.palette;

import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author nithya
 */
public class CategoryChildren extends Children.Keys {

    private String[] Categories = new String[]{
        "String Operators",
        "Date Operators",
        "Cleansing Operators",
        "Relational Operators",
        "SQL Operators",
        "Function Operators"
        /*"Mashup Operators"*/};

    public CategoryChildren() {
    }

    /**
     * 
     * @param key 
     * @return nodes Node[]
     */
    protected Node[] createNodes(Object key) {
        Category obj = (Category) key;
        return new Node[] { new CategoryNode(obj) };
    }

    protected void addNotify() {
        super.addNotify();
        Category[] objs = new Category[Categories.length];
        for (int i = 0; i < objs.length; i++) {
            Category cat = new Category();
            cat.setName(Categories[i]);
            objs[i] = cat;
        }
        setKeys(objs);
    }

}
