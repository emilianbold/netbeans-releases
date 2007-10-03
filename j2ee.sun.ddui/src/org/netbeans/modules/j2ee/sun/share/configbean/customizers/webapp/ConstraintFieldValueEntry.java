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
 * ConstraintFieldValueEntry.java
 *
 * Created on December 12, 2003, 6:28 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.List;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.ConstraintField;

import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanListMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;

/**
 *
 * @author  Peter Williams
 */
public class ConstraintFieldValueEntry extends GenericTableModel.TableEntry {

	private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N
	
	public ConstraintFieldValueEntry() {
		super(ConstraintField.CONSTRAINT_FIELD_VALUE, 
			webappBundle.getString("LBL_ConstraintFieldValues"));	// NOI18N
	}
	
	/**	The parent is the ConstraintField being edited.  The expected return
	 *  is a ConstraintField that has the array of ConstraintFieldValues.
	 *  We can't just return the parent because if it gets changed and the user
	 *  hits cancel, the changes will still be commited (bad!).
	 *
	 *  @param parent The real ConstraintField that provides initialization data
	 *     for the returned bean.
	 *  @return A new ConstraintField that has any existing data saved into it,
	 *     but can be edited and changed at will independent of the parent.
	 */
	public Object getEntry(CommonDDBean parent) {
		return new BeanListMapping((CommonDDBean) parent.clone(), getPropertyName());
	}

	/** Value is type ConstraintField but contains only an array of 
	 *  ConstraintFieldValues so we merge this into the parent ConstraintField
	 *  which already contains the other stuff.
	 *
	 *  @param parent The real ConstraintField that is to be saved (merged) into.
	 *  @param value The temporary ConstraintField that has been used for storage
	 *     during editing.
	 */
	public void setEntry(CommonDDBean parent, Object value) {
		parent.merge(((BeanListMapping) value).getBean(), CommonDDBean.MERGE_UPDATE);
	}
	
	public Object getEntry(CommonDDBean parent, int row) {
		throw new UnsupportedOperationException();
	}
	
	public void setEntry(CommonDDBean parent, int row, Object value) {
		throw new UnsupportedOperationException();
	}
	
}
