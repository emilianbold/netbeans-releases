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


package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import java.awt.event.MouseEvent;
import java.util.List;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import com.tomsawyer.editor.graphics.TSEGraphics;

public interface IListCompartment extends ISimpleListCompartment
{
	/**
	 * Deletes all selected compartments from this list.  Optionally displays a verification messagebox.
	*/
	public void deleteSelectedCompartments( boolean bPrompt );

	/**
	 * Returns a collection of selected compartments.
	*/
	public ETList < ICompartment > getSelectedCompartments();

	/**
	 * Adds this list's selected compartments to the passed in list.
	*/
	public void getSelectedCompartments2( ETList < ICompartment > pCompartments );

	/**
	 * Selects the compartments in a collection.
	*/
	public void setSelectedCompartments( ETList < ICompartment > pCompartments );

	/**
	 * Does this list compartment have selected compartments.
	*/
	public boolean getHasSelectedCompartments();

	/**
	 * Moves selection up to the previous visible compartment.
	*/
	public boolean lineUp();

	/**
	 * Moves selection down to the next visible compartment.
	*/
	public boolean lineDown();

	/**
	 * Retrieves a list of all visible compartments contained by this list compartment.
	*/
	public ETList < ICompartment > getVisibleCompartments();

	/**
	 * Indicates whether this compartment should be removed from its drawengine if it is empty of compartment elements.
	*/
	public boolean getDeleteIfEmpty();

	/**
	 * Indicates whether this compartment should be removed from its drawengine if it is empty of compartment elements.
	*/
	public void setDeleteIfEmpty( boolean value );

//	public String getStaticText();

//	public void setName(String string);
	
	public IETSize getMaxSize();

}
