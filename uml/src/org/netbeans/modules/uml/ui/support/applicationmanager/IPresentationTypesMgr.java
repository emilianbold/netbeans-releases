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


package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

public interface IPresentationTypesMgr
{
	/**
	 * Creates the default presentation types file.
	*/
	public void createDefaultXMLFile();

	/**
	 * Validates the file.
	*/
	public boolean validateFile();

	/**
	 * Returns the initialization string for a specific button/diagram pair.
	*/
	public String getButtonInitString( String sButtonName,
                                     /* DiagramKind */ int nDiagramKind );

	/**
	 * Returns the initialization string that should be used when wishing to create a presentation object
	*/
	public String getMetaTypeInitString( String sMetaType, /* DiagramKind */ int nDiagramKind );

	/**
	 * Returns the initialization string that should be used when wishing to create a presentation object
	*/
	public String getMetaTypeInitString( IElement pElement, /* DiagramKind */ int nDiagramKind );

	/**
	 * For a particular initialization string/diagram pair this returns the 
    * details
    * 
    * @param The initialization String.
    * @param The type of the diagram.  The value must be one of the 
    *        IDiagramKind values.
    * @return The details of the item.
	 */
	public PresentationTypeDetails getInitStringDetails( String sInitString, 
                                                   /* IDiagramKind */ int nDiagramKind);

	/**
	 * Returns the version of the presentation file.
	*/
	public String getVersion();

	/**
	 * If the default file is created, this the version of that file
	*/
	public String getPresentationTypesMgrVersion(  );

	/**
	 * Returns the owner element type of an artifact element, e.g. 'Attribute' and 'Operation' would typically return 'Class'.
	*/
	public String getOwnerMetaType( String sElementType );

	/**
	 * Is this a valid drawengine on this diagram type?
	*/
	public boolean isValidDrawEngine( /* DiagramKind */ int nDiagramKind, String sDrawEngineID );

	/**
	 * Returns the default description for a label view
	*/
	public String getDefaultLabelView();

	/**
	 * Returns the default description for a connector view
	*/
	public String getDefaultConnectorView();

	/**
	 * Returns the metatype for the various types of edges
	*/
	public String getPresentationElementMetaType( String sElementType, String sInitializationString );

}
