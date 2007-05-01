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
 * Defines methods required for a target column representation.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public interface TargetColumn extends SQLDBColumn, Cloneable, Comparable {

    /**
     * Gets associated input SQLObject, if any, for this column.
     * 
     * @return input SQLObject, or null if none was set
     */
    public SQLObject getValue();

    /**
     * Sets associated SQLObject as input to this column.
     * 
     * @param newInput new input SQLObject; may be null
     */
    public void setValue(SQLObject newInput);

}

