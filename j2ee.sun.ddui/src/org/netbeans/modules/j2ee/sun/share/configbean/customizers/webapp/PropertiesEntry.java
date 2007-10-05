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
/*
 * PropertiesEntry.java
 *
 * Created on December 12, 2003, 6:28 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.List;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty;

import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ListMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.openide.util.NbBundle;


/**
 *
 * @author  Peter Williams
 */
public class PropertiesEntry extends GenericTableModel.TableEntry {
	
	public PropertiesEntry() {
		this(SunWebApp.PROPERTY, NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle").getString("LBL_Properties"));	// NOI18N
	}
	
	public PropertiesEntry(String propertyName, String captionName) {
		super(propertyName, captionName);
	}

	public Object getEntry(CommonDDBean parent) {
		// FIXME this can be made more concise - spread out for debugging.
		ListMapping listMap = null;
		Object obj = parent.getValues(propertyName);
		if(obj != null) {
			WebProperty [] webProps = (WebProperty []) obj;
			List properties = Utils.arrayToList(webProps);
			listMap = new ListMapping(properties);
		}
		
		return listMap;
	}

	public void setEntry(CommonDDBean parent, Object value) {
		List list = ((ListMapping) value).getList();
		WebProperty [] webProps = (WebProperty []) 
			Utils.listToArray(list, WebProperty.class);
		parent.setValue(propertyName, webProps);
	}
	
	public Object getEntry(CommonDDBean parent, int row) {
		throw new UnsupportedOperationException();
	}	
	
	public void setEntry(CommonDDBean parent, int row, Object value) {
		throw new UnsupportedOperationException();
	}
}
