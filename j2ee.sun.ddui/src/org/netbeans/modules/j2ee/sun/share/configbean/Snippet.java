/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.sun.share.configbean;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;


/** This class represents holds the segments of deployment descriptor files that
 * a DConfigBean represents to the user.
 * @author vkraemer
 */
interface Snippet {
	
	/** Creates a fragment of a schema2beans graph for the DConfigBean this 
	 *  snippet is associated with.  Returns the bean representing this fragment
	 *
	 * This method is called by Base.addToGraphs().  
	 * @return The element bean for the deployment descriptor branch.
	 */
	public CommonDDBean getDDSnippet();
    
	public org.netbeans.modules.schema2beans.BaseBean getCmpDDSnippet();
	
	/** Returns true if getDDSnippet will construct and return a basebean
	 *  with data in it to be saved.  (Some DConfigBeans might not require
	 *  saving.  Those will have this method return false).
	 *
	 * @return true if getDDSnippet() will return a non-null bean, false otherwise
	 */
	public boolean hasDDSnippet();

	/** Return the name of the file that this snippet belongs in.  
	 *
	 * This method is called by Base.addToGraphs().
	 * @return the name of a s1as specific deployment descriptor file.
	 */	
	public String getFileName();
	
	/** Merge this snippet into a schema2beans object.  See also getPropertyName()
	 *  which allows some adjustment to the default implementation of this algorithm
	 *
	 *  @param parentDD This schema2beans object will be the immediate parent in
	 *  the graph of the snippet created by this object.
	 *  @return the schema2beans object representing the snippet created by this
	 *  object.
	 */
	public CommonDDBean mergeIntoRovingDD(CommonDDBean parentDD);
	
	
	/** Merge this snippet into a schema2beans object.
	 *
	 *  @param rootDD This schema2beans object is the root of the beangraph
	 *  that this snippet needs to be merged into.
	 *  @return the schema2beans object representing the snippet created by this
	 *  object.
	 */
	public CommonDDBean mergeIntoRootDD(CommonDDBean rootDD);
	
	/** This method is provided to allow some customization to the merge algorithms
	 *  above.
	 *
	 *  @return the schema2beans property name corresponding to the schema2beans
	 *  object created that represents this snippet.  E.g. the property name
	 *  of an Ejb snippet would be EnterpriseBeans.EJB.
	 */
	public String getPropertyName();
	
}
