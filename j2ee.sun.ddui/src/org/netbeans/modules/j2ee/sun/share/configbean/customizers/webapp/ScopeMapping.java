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
/*
 * ScopeMapping.java
 *
 * Created on February 4, 2004, 10:45 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping;

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
	
	/** resource bundle */
	static final ResourceBundle webappBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N

	// http scopes
	private static final TextMapping scopeBlank =
		new TextMapping("", ""); // NOI18N
	private static final TextMapping scopeContextAttribute = 
		new TextMapping("context.attribute", webappBundle.getString("SCOPE_CONTEXT_ATTRIBUTE"));	// NOI18N
	private static final TextMapping scopeRequestHeader = 
		new TextMapping("request.header", webappBundle.getString("SCOPE_REQUEST_HEADER"));		// NOI18N
	private static final TextMapping scopeRequestParameter = 
		new TextMapping("request.parameter", webappBundle.getString("SCOPE_REQUEST_PARAMETER"));	// NOI18N
	private static final TextMapping scopeRequestCookie = 
		new TextMapping("request.cookie", webappBundle.getString("SCOPE_REQUEST_COOKIE"));		// NOI18N
	private static final TextMapping scopeRequestAttribute = 
		new TextMapping("request.attribute", webappBundle.getString("SCOPE_REQUEST_ATTRIBUTE"));	// NOI18N
	private static final TextMapping scopeSessionAttribute = 
		new TextMapping("session.attribute", webappBundle.getString("SCOPE_SESSION_ATTRIBUTE"));	// NOI18N
	private static final TextMapping scopeSessionId = 
		new TextMapping("session.id", webappBundle.getString("SCOPE_SESSION_ID"));				// NOI18N
	
	// match expressions
	private static final TextMapping matchEquals = 
		new TextMapping("equals", webappBundle.getString("EXPR_EQUALS"));				// NOI18N
	private static final TextMapping matchGreater = 
		new TextMapping("greater", webappBundle.getString("EXPR_GREATER"));			// NOI18N
	private static final TextMapping matchLesser = 
		new TextMapping("lesser", webappBundle.getString("EXPR_LESSER"));				// NOI18N
	private static final TextMapping matchNotEquals = 
		new TextMapping("not-equals", webappBundle.getString("EXPR_NOT_EQUALS"));		// NOI18N
	private static final TextMapping matchInRange = 
		new TextMapping("in-range", webappBundle.getString("EXPR_IN_RANGE"));			// NOI18N
	
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
