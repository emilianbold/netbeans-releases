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
 * ScopeMapping.java
 *
 * Created on February 4, 2004, 10:45 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping;
import org.openide.util.NbBundle;


/**
 *
 * @author Peter Williams
 */
public final class ScopeMapping {
	
	/** Creates a new instance of ScopeMapping */
	private ScopeMapping() {
		// No instances of this class.  It's a holder for a static list of
		// TextMapping objects that represent various request scopes.
	}
	
	// http scopes
	private static final TextMapping scopeBlank =
		new TextMapping("", ""); // NOI18N
	private static final TextMapping scopeContextAttribute = 
		new TextMapping("context.attribute", NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle").getString("SCOPE_CONTEXT_ATTRIBUTE"));	// NOI18N
	private static final TextMapping scopeRequestHeader = 
		new TextMapping("request.header", NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle").getString("SCOPE_REQUEST_HEADER"));		// NOI18N
	private static final TextMapping scopeRequestParameter = 
		new TextMapping("request.parameter", NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle").getString("SCOPE_REQUEST_PARAMETER"));	// NOI18N
	private static final TextMapping scopeRequestCookie = 
		new TextMapping("request.cookie", NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle").getString("SCOPE_REQUEST_COOKIE"));		// NOI18N
	private static final TextMapping scopeRequestAttribute = 
		new TextMapping("request.attribute", NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle").getString("SCOPE_REQUEST_ATTRIBUTE"));	// NOI18N
	private static final TextMapping scopeSessionAttribute = 
		new TextMapping("session.attribute", NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle").getString("SCOPE_SESSION_ATTRIBUTE"));	// NOI18N
	private static final TextMapping scopeSessionId = 
		new TextMapping("session.id", NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle").getString("SCOPE_SESSION_ID"));				// NOI18N
	
	// match expressions
	private static final TextMapping matchEquals = 
		new TextMapping("equals", NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle").getString("EXPR_EQUALS"));				// NOI18N
	private static final TextMapping matchGreater = 
		new TextMapping("greater", NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle").getString("EXPR_GREATER"));			// NOI18N
	private static final TextMapping matchLesser = 
		new TextMapping("lesser", NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle").getString("EXPR_LESSER"));				// NOI18N
	private static final TextMapping matchNotEquals = 
		new TextMapping("not-equals", NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle").getString("EXPR_NOT_EQUALS"));		// NOI18N
	private static final TextMapping matchInRange = 
		new TextMapping("in-range", NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle").getString("EXPR_IN_RANGE"));			// NOI18N
	
	/** xml <--> ui mapping for timeout scopes combo box */
	private static final TextMapping [] scopeTypes = {
		scopeBlank, 
		scopeContextAttribute, 
		scopeRequestHeader, scopeRequestParameter, scopeRequestCookie, scopeRequestAttribute, 
		scopeSessionAttribute,
	};
	
	/** xml <--> ui mapping for refresh field scopes combo box */
	private static final TextMapping [] keyScopeTypes = {
		scopeBlank, 
		scopeContextAttribute, 
		scopeRequestHeader, scopeRequestParameter, scopeRequestCookie,
		scopeSessionId, scopeSessionAttribute,
	};

	/** xml <--> ui mapping for match expresssions combo box */
	private static final TextMapping [] matchExpressions = {
		matchEquals, matchGreater, matchLesser, matchNotEquals, matchInRange, 
	};	

	/** Retrieves array associated with %scope ENTITY type from sun-web.dtd
	 *  @return Array of TextMappings for the %scope field
	 */
	public static TextMapping [] getScopeMappings() {
		return scopeTypes;
	}
	
	/** Retrieves array associated with %keyscope ENTITY type from sun-web.dtd
	 *  @return Array of TextMappings for the %keyscope field
	 */
	public static TextMapping [] getKeyScopeMappings() {
		return keyScopeTypes;
	}
	
	/** Retrieves array associated with %expr ENTITY type from sun-web.dtd
	 *  @return Array of TextMappings for the %expr field
	 */
	public static TextMapping [] getMatchExpressionMappings() {
		return matchExpressions;
	}
	
}
