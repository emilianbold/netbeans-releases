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
/*
 * Created on Mar 10, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.netbeans.modules.visualweb.insync;

/**
 * Allow Model's and other objects to be notified of state changes on a SourceUnit.  This provides the ability of a SourceUnit to belong to more than one owner.
 */
public interface SourceUnitListener {

    /**
     * unit has just been saved.
     * @param unit
     */
    public void sourceUnitSaved(SourceUnit unit);

    /**
     * unit's document has just been modified somehow.
     * @param unit
     */
    public void sourceUnitSourceDirtied(SourceUnit unit);

    /**
     * unit's model has just been modified somehow.
     * @param unit
     */
    public void sourceUnitModelDirtied(SourceUnit unit);

}
