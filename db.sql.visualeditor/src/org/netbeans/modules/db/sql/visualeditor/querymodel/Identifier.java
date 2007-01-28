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
package org.netbeans.modules.db.sql.visualeditor.querymodel;

/**
 * Represents an identifier (schema/table/column name)
 */
public class Identifier {

    // Fields
    private String      _name;
    private boolean     _delimited;


    // Constructors

    // Create an Identifier with delimiter status explicitly specified.
    // Only occurs when the parser has that information
    public Identifier(String name, boolean delimited) {
        _name = name;
        _delimited = delimited;
    }


    // Create an Identifier with delimiter status decided heuristically,
    // depending whether the name contains any special characters
    public Identifier(String name) {
        _name=name;
        _delimited = needsDelimited(name);
    }


    // Accessors

    public String genText() {
        if (_delimited)
            return "\"" + _name + "\"";
        else
            return _name;
    }


    public String getName() {
        return _name;
    }


    /**
     * Returns true if the argument contains any non-word characters, which
     * will require it to be delimited.
     */
    private boolean needsDelimited(String name) {
        String[] split=name.split("\\W");
        return (split.length>1);
    }

}

