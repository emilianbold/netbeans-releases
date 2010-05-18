/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.xml.schema.actions;

import java.io.IOException;
import org.netbeans.modules.xml.schema.SchemaDataObject;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.ErrorManager;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Jeri Lockhart
 */
public class SchemaSourceViewOpenAction extends OpenAction{
    private static final long serialVersionUID = 1L;

    /**
     * SchemaSourceViewOpenAction is like OpenAction
     *  but also opens the source view to the line
     *  of the schema component
     *  The name of the action is Edit, not Open
     *
     *  See SchemaViewOpenAction.java for the action named "Open"
     *
     *
     */
    public String getName() {
        return NbBundle.getMessage(SchemaSourceViewOpenAction.class, "Edit");
    }

    protected void performAction(Node[] node) {
        if (node == null || node[0] == null){
            return;
        }
        SchemaDataObject sdo = node[0].getLookup().lookup(SchemaDataObject.class);
		if(sdo!=null)
		{
			ViewComponentCookie svc = sdo.getCookie(
					ViewComponentCookie.class);
			if(svc!=null)
			{
				try
				{
					svc.view(ViewComponentCookie.View.SOURCE,
							sdo.getSchemaEditorSupport().getModel().getSchema());
					return;
				}
				catch (IOException ex)
				{
					ErrorManager.getDefault().notify(ex);
				}
			}
		}
		// default to open cookie
        OpenCookie oc = node[0].getCookie(OpenCookie.class);
        if (oc != null){
            oc.open();
        }
    }
}
