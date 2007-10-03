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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
