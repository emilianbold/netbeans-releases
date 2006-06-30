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
package org.netbeans.modules.xml.text.syntax.javacc.lib;

/**
 * A token Id that is used by JavaCC syntax colourings.
 * It holds additional information if the token is an error
 * one. In such case Syntax could set supposed token.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class JJTokenID extends org.netbeans.editor.BaseTokenID {

    private boolean error = false;

    public JJTokenID(String name, int id) {
        super(name, id);
    }

    public JJTokenID(String name, int id, boolean error) {
        this(name, id);
        this.error = error;
    }
    
    public final int getID() {
        return getNumericID();
    }

    public final boolean isError() {
        return error;
    }
}
