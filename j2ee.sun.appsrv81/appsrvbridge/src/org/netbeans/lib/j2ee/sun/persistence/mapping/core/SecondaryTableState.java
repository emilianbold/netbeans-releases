/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */


/*
 * SecondaryTableState.java
 *
 * Created on June 4, 2001, 4:40 PM
 */

package org.netbeans.lib.j2ee.sun.persistence.mapping.core;

import java.util.ArrayList;

import org.netbeans.modules.dbschema.*;

import com.sun.jdo.api.persistence.model.Model;

/**
 * This state object manages a secondary table which includes the table 
 * and reference key (column pair) information.  This state object is 
 * "owned" by its enclosing TableState.
 *
 * @author Rochelle Raccah
 * @version %I%
 */
public class SecondaryTableState extends AbstractState
	implements PairHolderState, Cloneable
{
	private TableState _tableState;	// backpointer to enclosing holder
	private PairState _pairState;
	private String _tableName;

	public SecondaryTableState (Model model, TableState declaringTable, 
		String tableName) 
	{
		super(model);
		_tableState = declaringTable;

		if (_tableState != null)
			setMappingClassElement(_tableState.getMappingClassElement());

		_tableName = tableName;
		_pairState = new PairState(model, this, new ArrayList());
	}

	// cloning rules: do super.clone and accept the defaults for everything
	// except collections, other state objects, and collections of other
	// state objects.  For other state objects which are not backpointers, 
	// clone them.  For collections of other state objects which are not 
	// backpointers, create a new collection and clone the elements.
	// For collections of other objects, clone the collection.
	public Object clone()
	{
		SecondaryTableState clonedState = (SecondaryTableState)super.clone();

		clonedState._pairState = (PairState)getPairState().clone();

		return clonedState;
	}

	public String getTableName () { return _tableName; }

	public PairState getPairState () { return _pairState; }

    public String getDebugInfo()
    {
        StringBuffer lDump = new StringBuffer();
        
        lDump.append("Secondary Table is : \n");
        lDump.append("\t" + getTableName() + "\n");
        lDump.append(getPairState().getDebugInfo());
        
        return lDump.toString();
    }

    //================== PairHolderState implementation =======================

    public TableState getTableState () { return _tableState; }
}
