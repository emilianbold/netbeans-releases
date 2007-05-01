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
package org.netbeans.modules.sql.framework.model.utils;

import org.netbeans.modules.sql.framework.model.impl.OperatorXmlInfoModelX;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoModel;
import org.netbeans.modules.sql.framework.ui.graph.impl.OperatorXmlInfoModel;
import org.openide.filesystems.Repository;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class OperatorUtil {

	private static IOperatorXmlInfoModel opModel;

	public static IOperatorXmlInfo findOperatorXmlInfo(String operatorName) {

		/* If opModel is null, instantiate the opModel object to default folder */
		if (opModel == null) {
			setOperatorLayerFolder(null);
		}
		return opModel.findOperatorXmlInfo(operatorName);
	}

	public static void setOperatorLayerFolder(String folderName) {

		org.openide.filesystems.FileObject fo=null;
		try {
			fo = Repository.getDefault().getDefaultFileSystem().findResource(folderName);
		} catch (Exception e) {

		}

		//outside netbeans ENV
		if (fo == null) {
			opModel = OperatorXmlInfoModelX.getInstance(folderName);
		} else {
			opModel = OperatorXmlInfoModel.getInstance(folderName);
		}
	}
}
