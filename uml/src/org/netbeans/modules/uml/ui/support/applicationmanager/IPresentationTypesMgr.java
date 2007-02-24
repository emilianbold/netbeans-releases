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
