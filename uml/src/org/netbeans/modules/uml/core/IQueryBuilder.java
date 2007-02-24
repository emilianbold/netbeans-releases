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


package org.netbeans.modules.uml.core;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * @author sumitabhk
 *
 */
public interface IQueryBuilder
{
	//sets Location of the QuerySchemas.etc file, or another file similar.
	public void setSchemaLocation(String pVal);
	
	//gets Location of the QuerySchemas.etc file, or another file similar.
	public String getSchemaLocation();
	
	//Process the results of the schemas defined in the file pointed to in the SchemaLocation property.
	public Document generateResults(Node node);
	
	//sets the ID of the Project that the builder is building against.
	public void setProjectId(String pVal);
	
	//gets the ID of the Project that the builder is building against.
	public String getProjectId();
	
	//gets the The prog id of the default updater.
	public String getDefaultUpdaterProgId();
}


