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

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.deploy.model.DDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
/**
 *
 * @author  vkraemer
 */
public class ToolSideError extends BaseRoot {

	/** Holds value of property message. */
	private String message;
	
	/** Creates a new instance of ToolSideError */
	public ToolSideError(String message) { // DDBean ddbean, Base parent) {
		this.message = message;
	}

	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	Collection getSnippets() {
		Collection snippets = new ArrayList();
		return snippets;
	}

    protected ConfigParser getParser() {
        return null;
    }
    
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		return false;
	}
	
	/** Getter for property message.
	 * @return Value of property message.
	 *
	 */
	public String getMessage() {
		return message; // "there is a tool side error"; //this.message;
	}
	
	/** Getter for property ddbeanXpath.
	 * @return Value of property ddbeanXpath.
	 *
	 */
	public String getDdbeanXpath() {
		return getDDBean().getXpath(); // this.ddbeanXpath;
	}
	
	/** Getter for property ddbeanText.
	 * @return Value of property ddbeanText.
	 *
	 */
	public String getDdbeanText() {
		return getDDBean().getText(); // this.ddbeanText;
	}
    
    public String generateDocType(ASDDVersion version) {
        return "";
    }

    public String getHelpId() {
        return "AS_CFG_ToolSideError";                              //NOI18N
    }

}
