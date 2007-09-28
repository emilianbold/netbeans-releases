/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
