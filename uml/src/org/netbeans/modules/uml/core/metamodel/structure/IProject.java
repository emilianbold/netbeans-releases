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


package org.netbeans.modules.uml.core.metamodel.structure;
import org.netbeans.modules.uml.core.support.IAssociatedProjectSourceRoots;
import org.dom4j.Document;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementDisposal;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.core.workspacemanagement.ITwoPhaseCommit;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;

public interface IProject extends IModel, ITwoPhaseCommit
{
	/**
	 * Retrieves the document that holds the data of the project.
	*/
	public Document getDocument();

	/**
	 * Retrieves the document that holds the data of the project.
	*/
	public void setDocument( Document value );

	/**
	 * Saves this Project to the location specified.
	*/
	public void save( String fileName, boolean remember );

	/**
	 * Sets / Gets the name of the file this project will be saved to.
	*/
	public String getFileName();

	/**
	 * Sets / Gets the name of the file this project will be saved to.
	*/
	public void setFileName( String value );

	/**
	 * Gets the directory where this project will be saved to.
	*/
	public String getBaseDirectory();

	/**
	 * Pulls in the default package imports for this project. This is called by default by the Application.
	*/
	public boolean loadDefaultImports();

	/**
	 * Sets / Gets the name of the mode this project is in.
	*/
	public String getMode();

	/**
	 * Sets / Gets the name of the mode this project is in.
	*/
	public void setMode( String value );

	/**
	 * Sets / Gets the name of the language to use for this project when in Implementation mode.
	*/
	public String getDefaultLanguage();

	/**
	 * Sets / Gets the name of the language to use for this project when in Implementation mode.
	*/
	public void setDefaultLanguage( String value );

	/**
	 * The object responsible for handling the maintenance and purging of deleted items.
	*/
	public IElementDisposal getElementDisposal();

	/**
	 * The object responsible for handling the maintenance and purging of deleted items.
	*/
	public void setElementDisposal( IElementDisposal value );

	/**
	 * The ID that identifies this Project in the SC tool.
	*/
	public String getSourceControlID();

	/**
	 * The ID that identifies this Project in the SC tool.
	*/
	public void setSourceControlID( String value );

	/**
	 * Extracts the passed in element from the Projects document, allowing for Version control.
	*/
	public void extractElement( IElement element );

	/**
	 * The TypeManager associated with this Project.
	*/
	public ITypeManager getTypeManager();

	/**
	 * Gets the language to use for this project when in Implementation mode.
	*/
	public ILanguage getDefaultLanguage2();

	/**
	 * Retrieves the WSProject that corresponds with this IProject.
	*/
	public IWSProject getWSProject();

	/**
	 * Returns the child item dirty flag.
	*/
	public boolean getChildrenDirty();

	/**
	 * Returns the child item dirty flag.
	*/
	public void setChildrenDirty( boolean value );

        
        public boolean getDirty();
	/**
	 * Closes this Project. The Project object should not be used after this method is called
	*/
	public void close();

	/**
	 * Adds an absolute path to an external library to this Project
	*/
	public void addReferencedLibrary( String libLocation );

	/**
	 * Removes the location of a referenced library from this Project's list of libraries.
	*/
	public void removeReferencedLibrary( String libLocation );

	/**
	 * The collection of locations of libraries this Project references.
	*/
	public ETList<String> getReferencedLibraries();

	/**
	 * Sets the Project into more of a reference mode, turning off roundtrip behaviors.
	*/
	public boolean getLibraryState();

	/**
	 * Sets the Project into more of a reference mode, turning off roundtrip behaviors.
	*/
	public void setLibraryState( boolean value );

	/**
	 * Retrieves the referenced library with the passed in location.
	*/
	public IProject getReferencedLibraryProjectByLocation( String refLibLoc );

	/**
	 * Retrieves all the referenced libraries associated with this Project.
	*/
	public ETList<IProject> getReferencedLibraryProjects();

	public void prepareNode();
    
    /**
     * Add an element that must be removed before project is saved.
     */
    public void addRemoveOnSave(IElement element);
	
    /**
     * Sets the source roots that is associated with the project.
     */
    public void setAssociatedProjectSourceRoots(IAssociatedProjectSourceRoots sourceRoots);
    
    /**
     * Retrieve the source roots associated with the project.
     */
    public IAssociatedProjectSourceRoots getAssociatedProjectSourceRoots();
}
