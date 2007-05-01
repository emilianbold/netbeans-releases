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
package org.netbeans.modules.sql.framework.model;

/**
 * This class is part of When. An addendum to SQLCase and always used with SQLCase
 * 
 * @author Sudhendra Seshachala
 * @version $Revision$
 */
public interface SQLWhen extends SQLConnectableObject {

    /** Key constant: condition input */
    public static final String CONDITION = "condition";

    /** Key constant: return output */
    public static final String RETURN = "return";

    /** extraction condition tag */
    public static final String WHEN_CONDITION = "whenCondition";

    public SQLCondition getCondition();

    public int getJdbcType();

    public void setCondition(SQLCondition cond);
}

