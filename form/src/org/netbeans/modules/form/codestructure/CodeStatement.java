/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.codestructure;

/**
 * @author Tomas Pavek
 */

public interface CodeStatement extends UsingCodeObject {

    // parent expression for this statement
    public CodeExpression getParentExpression();

    // meta object representing the statement
    // (e.g. Method to be called or Field to be assigned)
    public Object getMetaObject();

    public CodeExpression[] getStatementParameters();

    public String getJavaCodeString(String parentStr, String[] paramsStr);
}
