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
package com.sun.rave.web.ui.util;

import com.sun.rave.web.ui.component.util.descriptors.LayoutElement;
import com.sun.rave.web.ui.component.util.descriptors.LayoutComponent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Stack;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;


/**
 *  <p>	VariableResolver is used to parse expressions of the format.</p>
 *
 *  <p>	<dd>$&lt;type$gt;{&lt;key&gt;}</dd></p>
 *
 *  <p>	&lt;type&gt; refers to a registerd {@link VariableResolver.DataSource},
 *	custom {@link VariableResolver.DataSource}s can be registered via:
 *	{@link #setDataSource(String key,
 *	    VariableResolver.DataSource dataSource)}.
 *	However, there are many built-in {@link VariableResolver.DataSource}
 *	types that are pre-registered.</p>
 *
 *  <p>	Below are the pre-registered types: </p>
 *
 *  <ul><li>{@link #ATTRIBUTE} -- {@link AttributeDataSource}</li>
 *	<li>{@link #BOOLEAN} -- {@link BooleanDataSource}</li>
 *	<li>{@link #BROWSER} -- {@link BrowserDataSource}</li>
 *	<li>{@link #CONSTANT} -- {@link ConstantDataSource}</li>
 *	<li>{@link #ESCAPE} -- {@link EscapeDataSource}</li>
 *	<li>{@link #HAS_FACET} -- {@link HasFacetDataSource}</li>
 *	<li>{@link #HAS_PROPERTY} -- {@link HasPropertyDataSource}</li>
 *	<li>{@link #INT} -- {@link IntDataSource}</li>
 *	<li>{@link #METHOD_BINDING} -- {@link MethodBindingDataSource}</li>
 *	<li>{@link #PROPERTY} -- {@link PropertyDataSource}</li>
 *	<li>{@link #REQUEST_PARAMETER} --
 *		{@link RequestParameterDataSource}</li>
 *	<li>{@link #RESOURCE} -- {@link ResourceBundleDataSource}</li>
 *	<li>{@link #SESSION} -- {@link SessionDataSource}</li>
 *	<li>{@link #STYLE} -- {@link StyleDataSource}</li>
 *	<li>{@link #THEME_JS} -- {@link ThemeJavaScriptDataSource}</li>
 *	<li>{@link #THIS} -- {@link ThisDataSource}</li></ul>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class VariableResolver {

    /**
     *	<p> This method will substitute variables into the given String, or
     *	    return the variable if the substitution is the whole String.  This
     *	    method looks for the LAST occurance of startToken in the given
     *	    String.  It then searches from that location (if found) to the
     *	    first occurance of typeDelim.  The value inbetween is used as the
     *	    type of substitution to perform (i.e. request attribute, session,
     *	    etc.).  It next looks for the next occurance of endToken.  The
     *	    value inbetween is used as the key passed to the
     *	    {@link VariableResolver.DataSource} specified by the type.  The
     *	    String value from the {@link VariableResolver.DataSource} replaces
     *	    the portion of the String from the startToken to the endToken.  If
     *	    this is the entire String, the Object is returned instead of the
     *	    String value.  This process is repeated until no more
     *	    substitutions are *  needed.</p>
     *
     *	<p> This algorithm will accomodate nested variables (e.g. "${A{$x}}").
     *	    It also allows the replacement value itself to contain variables.
     *	    Care should be taken to ensure that the replacement String included
     *	    does not directly or indirectly refer to itself -- this will cause
     *	    an infinite loop.</p>
     *
     *	<p> There is one special case where the string to be evaluated begins
     *	    with the startToken and ends with the endToken.  In this case,
     *	    string substitution is NOT performed.  Instead the value of the
     *	    request attribute is returned.</p>
     *
     *	@param	ctx		The FacesContext
     *	@param	desc		The closest LayoutElement to this string
     *	@param	component	The assoicated UIComponent
     *	@param	string		The string to be evaluated.
     *	@param	startToken	Marks the beginning		    "$"
     *	@param	typeDelim	Marks separation of type/variable   "{"
     *	@param	endToken	Marks the end of the variable	    "}"
     *
     *	@return The new string with substitutions, or the specified request
     *		attribute value.
     */
    public static Object resolveVariables(FacesContext ctx,
	    LayoutElement desc, UIComponent component, String string,
	    String startToken, String typeDelim, String endToken) {

	int stringLen = string.length();
	int delimIndex;
	int endIndex;
	int parenSemi;
	int startTokenLen = startToken.length();
	int delimLen = typeDelim.length();
	int endTokenLen = endToken.length();
	boolean expressionIsWholeString = false;
	char firstEndChar = SUB_END.charAt(0);
	char firstDelimChar = SUB_TYPE_DELIM.charAt(0);
	char currChar;
	String type;
	Object variable;

	for (int startIndex = string.lastIndexOf(startToken); startIndex != -1;
		 startIndex = string.lastIndexOf(startToken, startIndex - 1)) {

	    // Find first typeDelim
	    delimIndex = string.indexOf(typeDelim, startIndex + startTokenLen);
	    if (delimIndex == -1) {
		continue;
	    }

	    // Next find the end token
	    parenSemi = 0;
	    endIndex = -1;
	    // Iterate through the string looking for the matching end
	    for (int curr = delimIndex + delimLen; curr < stringLen; ) {
		// Get the next char...
		currChar = string.charAt(curr);
		if ((currChar == firstDelimChar) && typeDelim.equals(
			    string.substring(curr, curr + delimLen))) {
		    // Found the start of another... inc the semi
		    parenSemi++;
		    curr += delimLen;
		    continue;
		}
		if ((currChar == firstEndChar) && endToken.equals(
			    string.substring(curr, curr + endTokenLen))) {
		    parenSemi--;
		    if (parenSemi < 0) {
			// Found the right one!
			endIndex = curr;
			break;
		    }
		    // Found one, but this isn't the right one
		    curr += endTokenLen;
		    continue;
		}
		curr++;
	    }
	    if (endIndex == -1) {
		// We didn't find a matching end...
		continue;
	    }

/*
	    // Next find end token
	    endIndex = string.indexOf(endToken, delimIndex+delimLen);
	    matchingIndex = string.lastIndexOf(typeDelim, endIndex);
	    while ((endIndex != -1) && (matchingIndex != delimIndex)) {
		// We found a endToken, but not the matching one...keep looking
		endIndex = string.indexOf(endToken, endIndex+endTokenLen);
		matchingIndex = string.lastIndexOf(typeDelim,
			matchingIndex-delimLen);
	    }
	    if ((endIndex == -1) || (matchingIndex == -1)) {
		continue;
	    }
*/

	    // Handle special case where string starts with startToken and ends
	    // with endToken (and no replacements inbetween).  This is special
	    // because we don't want to convert the attribute to a string, we
	    // want to return it (this allows Object types).
	    if ((startIndex == 0) && (endIndex == string.lastIndexOf(endToken))
		    && (string.endsWith(endToken))) {
		// This is the special case...
		expressionIsWholeString = true;
	    }

	    // Pull off the type...
	    type = string.substring(startIndex + startTokenLen, delimIndex);
	    DataSource ds = (DataSource) dataSourceMap.get(type);
	    if (ds == null) {
		throw new IllegalArgumentException("Invalid type '" + type
			+ "' in attribute value: '" + string + "'.");
	    }

	    // Pull off the variable...
	    variable = string.substring(delimIndex + delimLen, endIndex);

	    // Get the value...
	    variable = ds.getValue(ctx, desc, component, (String) variable);
	    if (expressionIsWholeString) {
		return variable;
	    }

	    // Make new string
	    string = string.substring(0, startIndex) +	// Before replacement
		     ((variable == null) ? "" : variable.toString())
		     + string.substring(endIndex + endTokenLen); // After
	    stringLen = string.length();
	}

	// Return the string
	return string;
    }

    /**
     *	This method replaces the ${..} variables with their values.  It will
     *	only do this for Strings and List's that contain Strings.
     *
     *	@param	desc	    The <code>LayoutElement</code> descriptor
     *	@param	component   The <code>UIComponent</code>
     *	@param	value	    The value to resolve
     *
     *	@return The result
     */
    public static Object resolveVariables(LayoutElement desc,
	    UIComponent component, Object value) {
	if (value == null) {
	    return null;
	}
	return VariableResolver.resolveVariables(
	    FacesContext.getCurrentInstance(), desc, component, value);
    }

    /**
     *	This method replaces the ${..} variables with their attribute values.
     *	It will only do this for Strings and List's that contain Strings.
     *
     *	@param	ctx	    The <code>FacesContext</code>
     *	@param	desc	    The <code>LayoutElement</code> descriptor
     *	@param	component   The <code>UIComponent</code>
     *	@param	value	    The value to resolve
     *
     *	@return	The result
     */
    public static Object resolveVariables(FacesContext ctx, LayoutElement desc,
	    UIComponent component, Object value) {
	if (value == null) {
	    return null;
	}
	if (value instanceof String) {
	    value = VariableResolver.resolveVariables(
		ctx,
		desc,
		component,
		(String) value,
		VariableResolver.SUB_START,
		VariableResolver.SUB_TYPE_DELIM,
		VariableResolver.SUB_END);
	} else if (value instanceof List) {
	    // Create a new List b/c invalid to change shared List
	    List list = ((List) value);
	    int size = list.size();
	    List newList = new ArrayList(size);
	    Iterator it = list.iterator();
	    while (it.hasNext()) {
		newList.add(VariableResolver.resolveVariables(
			    ctx, desc, component, it.next()));
	    }
	    return newList;
	}
	return value;
    }

    /**
     *	<p> This method looks up the requested
     *	    {@link VariableResolver.DataSource} by the given key.
     *
     *	@param	key	The key identifying the desired
     *			{@link VariableResolver.DataSource}
     *
     *	@return	    The requested {@link VariableResolver.DataSource}
     */
    public static VariableResolver.DataSource getDataSource(String key) {
	return (VariableResolver.DataSource) dataSourceMap.get(key);
    }

    /**
     *	<p> This method sets the given {@link VariableResolver.DataSource} to
     *	    be used for $[type]{...} when key matches type.</p>
     *
     *	@param	key		The key identifying the
     *				{@link VariableResolver.DataSource}
     *	@param	dataSource	The {@link VariableResolver.DataSource}
     */
    public static void setDataSource(String key,
	    VariableResolver.DataSource dataSource) {
	dataSourceMap.put(key, dataSource);
    }

    /**
     *	<p> This interface defines a String substitution data source.  This
     *	    is used to retrieve values when a $&lt;type&gt;{&lt;data&gt;} is
     *	    encountered within a parameter value.</p>
     *
     *	<p> Implementations of this interface may register themselves
     *	    statically to extend the capabilities of the ${} substitution
     *	    mechanism.</p>
     */
    public interface DataSource {
	/**
	 *  <p>	This method should return the resolved value based on the
	 *	given key and contextual information.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key);
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} provides access to
     *	    HttpRequest attributes.  It uses the data portion of the
     *	    substitution String as a key to the HttpRequest attribute Map.</p>
     */
    public static class AttributeDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    return ctx.getExternalContext().getRequestMap().get(key);
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} provides access to
     *	    PageSession attributes.  It uses the data portion of the
     *	    substitution String as a key to the PageSession attribute Map.</p>
    public static class PageSessionDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    while (desc.getParent() != null) {
		desc = desc.getParent();
	    }
	    return ((ViewBean)desc.getView(ctx)).getPageSessionAttribute(key);
	}
    }
    */

    /**
     *	<p> This {@link VariableResolver.DataSource} provides access to
     *	    HttpRequest Parameters.  It uses the data portion of the
     *	    substitution String as a key to the HttpRequest Parameter Map.</p>
     */
    public static class RequestParameterDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    return ctx.getExternalContext().getRequestParameterMap().get(key);
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} provides access to
     *	    UIComponent Properties.  It uses the data portion of the
     *	    substitution String as a key to the UIComponent's properties via
     *	    the attribute Map.  If the property is null, it will attempt to
     *	    look at the parent's properties.</p>
     */
    public static class PropertyDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {

	    // Check to see if we should walk up the tree or not
	    int idx = key.indexOf(',');
	    boolean walk = false;
	    if (idx > 0) {
		walk = Boolean.valueOf(key.substring(idx+1)).booleanValue();
		key = key.substring(0, idx);
	    }

	    Object value = component.getAttributes().get(key);
	    while (walk && (value == null) && (component.getParent() != null)) {
		component = component.getParent();
		value = component.getAttributes().get(key);
	    }
/*
	    if (LogUtil.finestEnabled()) {
		// Trace information
		LogUtil.finest(this, "RESOLVING ('" + key + "') for ('"
			+ component.getId() + "'): '" + value + "'");
	    }
*/
	    return value;
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} tests if the given
     *	    property exists on the UIComponent.  It uses the data portion of
     *	    the substitution String as a key to the UIComponent's properties
     *	    via the attribute Map.</p>
     */
    public static class HasPropertyDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    boolean hasKey = component.getAttributes().containsKey(key);
	    if (!hasKey) {
		// Check the getter... JSF sucks when wrt attrs vs. props
		if (component.getAttributes().get(key) != null) {
		    hasKey = true;
		}
	    }
	    if (!hasKey && (desc instanceof LayoutComponent)) {
		// In some cases, the component is a TemplateComponent child
		return getValue(
		    ctx, desc.getParent(), component.getParent(), key);
	    }
	    return Boolean.valueOf(hasKey);
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} tests if the given facet
     *	    exists on the UIComponent.  It uses the data portion of the
     *	    substitution String as a key to the UIComponent's facets.</p>
     */
    public static class HasFacetDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    boolean hasFacet = component.getFacets().containsKey(key);
	    if (!hasFacet && (desc instanceof LayoutComponent)) {
		// In some cases, the component is a TemplateComponent child
		return getValue(
		    ctx, desc.getParent(), component.getParent(), key);
	    }
	    return Boolean.valueOf(hasFacet);
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} simply returns the key
     *	    that it is given.  This is useful for supplying ${}'s around the
     *	    string you wish to mark as a string.  If not used, characters such
     *	    as '=' will be interpretted as a separator causing your string to
     *	    be split -- which can be very undesirable. Mostly useful in "if"
     *	    statements.</p>
     */
    public static class EscapeDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    return key;
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} converts the given
     *	    <code>key</code> to a <code>Boolean</code>.  This is needed because
     *	    JSF does not do this for you.  When you call
     *	    <code>UIComponent.getAttributes().put(key, value)</code>,
     *	    <code>value</code> is expected to be the correct type.  Often
     *	    <code>Boolean</code> types are needed.  This
     *	    {@link VariableResolver.DataSource} provides a means to supply a
     *	    <code>Boolean</code> value.</p>
     */
    public static class BooleanDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    return Boolean.valueOf(key);
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} uses the given
     *	    <code>key</code> to check various properties of the browser that
     *	    submitted the request.  The valid keys are:</p>
     *
     *	<ul><li>getUserAgent</li>
     *	    <li>getUserAgentMajor</li>
     *	    <li>isIe</li>
     *	    <li>isNav</li>
     *	    <li>isGecko</li>
     *	    <li>isSun</li>
     *	    <li>isWin</li>
     *	    <li>isIe5up</li>
     *	    <li>isIe6up</li>
     *	    <li>isNav7up</li>
     *	    <li>isIe6</li>
     *	    <li>isIe5</li>
     *	    <li>isIe4</li>
     *	    <li>isIe3</li>
     *	    <li>isNav70</li>
     *	    <li>isNav7</li>
     *	    <li>isNav6up</li>
     *	    <li>isNav6</li>
     *	    <li>isNav4up</li>
     *	    <li>isNav4</li></ul>
     *
     *	@see ClientSniffer
     */
    public static class BrowserDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    DataSource ds = (DataSource) innerDataSources.get(key);
	    if (ds == null) {
		throw new IllegalArgumentException("'" + key
			+ "' is not a valid key for BrowserDataSource!");
	    }
	    return ds.getValue(ctx, desc, component, key);
	}

	/**
	 *  <p>	For efficiency, we will implement the branching as a Map of
	 *	DataSources.</p>
	 */
	private static Map innerDataSources = new HashMap();

	static {
	    innerDataSources.put("getUserAgent", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return ClientSniffer.getInstance(ctx).getUserAgent();
		    }
		});
	    innerDataSources.put("getUserAgentMajor", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return new Integer(
			    ClientSniffer.getInstance(ctx).getUserAgentMajor());
		    }
		});
	    innerDataSources.put("isIe", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isIe());
		    }
		});
	    innerDataSources.put("isNav", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isNav());
		    }
		});
	    innerDataSources.put("isGecko", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isGecko());
		    }
		});
	    innerDataSources.put("isSun", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isSun());
		    }
		});
	    innerDataSources.put("isWin", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isWin());
		    }
		});
	    innerDataSources.put("isIe5up", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isIe5up());
		    }
		});
	    innerDataSources.put("isIe6up", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isIe6up());
		    }
		});
	    innerDataSources.put("isNav7up", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isNav7up());
		    }
		});
	    innerDataSources.put("isIe6", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isIe6());
		    }
		});
	    innerDataSources.put("isIe5", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isIe5());
		    }
		});
	    innerDataSources.put("isIe4", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isIe4());
		    }
		});
	    innerDataSources.put("isIe3", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isIe3());
		    }
		});
	    innerDataSources.put("isNav70", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isNav70());
		    }
		});
	    innerDataSources.put("isNav7", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isNav7());
		    }
		});
	    innerDataSources.put("isNav6up", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isNav6up());
		    }
		});
	    innerDataSources.put("isNav6", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isNav6());
		    }
		});
	    innerDataSources.put("isNav4up", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isNav4up());
		    }
		});
	    innerDataSources.put("isNav4", new DataSource() {
		    public Object getValue(FacesContext ctx, LayoutElement desc,
			    UIComponent component, String key) {
			return Boolean.valueOf(
			    ClientSniffer.getInstance(ctx).isNav4());
		    }
		});
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} converts the given
     *	    <code>key</code> to an <code>Integer</code>.  This is needed
     *	    because JSF does not do this for you.  When you call
     *	    <code>UIComponent.getAttributes().put(key, value)</code>,
     *	    <code>value</code> is expected to be the correct type.  Often
     *	    <code>Integer</code> types are needed.  This
     *	    {@link VariableResolver.DataSource} provides a means to supply an
     *	    <code>Integer</code> value.</p>
     */
    public static class IntDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    return Integer.valueOf(key);
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} allows access to constants
     *	    in java classes.  It expects the key to be a fully qualified Java
     *	    classname plus the variable name. Example:</p>
     *
     *	<p> $constant{java.lang.Integer.MAX_VALUE} </p>
     */
    public static class ConstantDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    // First check to see if we've already found the value before.
	    Object value = constantMap.get(key);
	    if (value == null) {
		// Not found, lets resolve it, duplicate the old Map to avoid
		// sync problems
		Map map = new HashMap(constantMap);
		value = resolveValue(map, key);

		// Replace the shared Map w/ this new one.
		constantMap = map;
	    }
	    return value;
	}

	/**
	 *  <p>	This method resolves key.  Key is expected to be in the
	 *	format:</p>
	 *
	 *  <p>	some.package.Class.STATIC_VARIBLE</p>
	 *
	 *  <p>	This method will first resolve Class. It will then walk
	 *	through all its variables adding each static final variable to
	 *	the Map.</p>
	 *
	 *  @param  map	    The map to add variables to
	 *  @param  key	    The fully qualified CONSTANT name
	 *
	 *  @return The value of the CONSTANT, or null if not found
	 */
	private Object resolveValue(Map map, String key) {
	    int lastDot = key.lastIndexOf('.');
	    if (lastDot == -1) {
		throw new IllegalArgumentException("Unable to resolve '" + key
		    + "' in $constant{" + key + "}.  '" + key + "' must be a "
		    + "fully qualified classname plus the constant name.");
	    }

	    // Get the classname / constant name
	    String className = key.substring(0, lastDot);

	    // Add all constants to the Map
	    try {
		addConstants(map, Class.forName(className));
	    } catch (ClassNotFoundException ex) {
		RuntimeException iae = new IllegalArgumentException("'"
		    + className + "' was not found!  This must be a valid "
		    + "classname.  This was found in expression $constant{"
		    + key + "}.");
		iae.initCause(ex);
		throw iae;
	    }

	    // The constant hopefully is in the Map now, null if not
	    return map.get(key);
	}

	/**
	 *  This method adds all constants in the given class to the Map.  The
	 *  Map key will be the fully qualified class name, plus a '.', plus
	 *  the constant name.
	 *
	 *  @param  map	<code>Map</code> to store <code>cls</code>
	 *  @param  cls	The <code>Class</code> to store in <code>map</code>
	 */
	private void addConstants(Map map, Class cls) {
	    // Get the class name
	    String className = cls.getName();

	    // Get the fields
	    Field fields[] = cls.getFields();

	    // Add the static final fields to the Map
	    Field field = null;
	    for (int count = 0; count < fields.length; count++) {
		field = fields[count];
		if (Modifier.isStatic(field.getModifiers())
			&& Modifier.isFinal(field.getModifiers())) {
		    try {
			map.put(className + '.' + field.getName(),
				field.get(null));
		    } catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		    }
		}
	    }
	}

	/**
	 *  This embedded Map caches constant value lookups.  It is static and
	 *  is shared by all users.
	 */
	private static Map constantMap = new HashMap();
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} creates a MethodBinding
     *	    from the supplied key.  Example:</p>
     *
     *	<p> $methodBinding{#{bean.bundleKey}}</p>
     */
    public static class MethodBindingDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    return ctx.getApplication().createMethodBinding(key, actionArgs);
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} allows access to resource
     *	    bundle keys.  It expects the key to be a resource bundle key plus a
     *	    '.' then the actual resouce bundle key Example:</p>
     *
     *	<p> $resource{bundleID.bundleKey} </p>
     *
     *	<p> The bundleID should not contain '.' characters.  The bundleKey
     *	    may.</p>
     */
    public static class ResourceBundleDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    // Get the Request attribute key
	    int separator = key.indexOf(".");
	    if (separator == -1) {
		throw new IllegalArgumentException("'" + key
		    + "' is not in format: \"[bundleID].[bundleKey]\"!");
	    }
	    String value = key.substring(0, separator);

	    // Get the Resource Bundle
	    ResourceBundle bundle = (ResourceBundle) ctx.getExternalContext().
		    getRequestMap().get(value);

	    // Make sure we have the bundle
	    if (bundle == null) {
		// Should we throw an exception?  For now just return the key
		return key;
	    }

	    // Return the result of the ResouceBundle lookup
	    value = bundle.getString(key.substring(separator + 1));
	    if (value == null) {
		value = key;
	    }

	    return value;
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} provides access to
     *	    HttpSession attributes.  It uses the data portion of the
     *	    substitution String as a key to the HttpSession Map.</p>
     */
    public static class SessionDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    return ctx.getExternalContext().getSessionMap().get(key);
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} retrieves style classes
     *	    from the current {@link com.sun.rave.web.ui.theme.Theme}.  The data
     *	    portion of the substitution String as the
     *	    {@link com.sun.rave.web.ui.theme.Theme} key.</p>
     */
    public static class StyleDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    return ThemeUtilities.getTheme(ctx).getStyleClass(key);
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} retrieves Theme messages
     *	    from the {@link com.sun.rave.web.ui.theme.Theme}.  The data portion of
     *	    the substitution String is the {@link com.sun.rave.web.ui.theme.Theme}
     *	    message key.</p>
     */
    public static class ThemeDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    return ThemeUtilities.getTheme(ctx).getMessage(key);
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} retrieves the path to a
     *	    JavaScript file from the {@link com.sun.rave.web.ui.theme.Theme}.  The
     *	    data portion of the substitution String is the
     *	    {@link com.sun.rave.web.ui.theme.Theme} key for the JavaScript file.</p>
     */
    public static class ThemeJavaScriptDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  component	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    return ThemeUtilities.getTheme(ctx).getPathToJSFile(key);
	}
    }

    /**
     *	<p> This {@link VariableResolver.DataSource} provides access to
     *	    DisplayField values.  It uses the data portion of the substitution
     *	    String as the DisplayField name to find.  This is a non-qualified
     *	    DisplayField name.  It will walk up the View tree starting at the
     *	    View object cooresponding to the LayoutElement which contained this
     *	    expression.  At each ContainerView, it will look for a child with
     *	    a matching name.</p>
    public static class DisplayFieldDataSource implements DataSource {
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent component, String key) {
	    while (desc != null) {
		View view = desc.getView(ctx);
		if (view instanceof ContainerView) {
		    View child = null;
//FIXME: use a better way to find if 'key' is a child of 'view'
		    try {
			child = (((ContainerView)(view)).getChild(key));
		    } catch (Exception ex) {
		    }
		    if (child != null) {
			return ((ContainerView) view).getDisplayFieldValue(key);
		    }
		}
		desc = desc.getParent();
	    }
	    return null;
	}
    }
    */

    /**
     *	<p> This class provides an implementation for the syntax $this{xyz}
     *	    where xyz can be any of the following.</p>
     *
     *	<ul><li>component -- Current <code>UIComponent</code></li>
     *	    <li>clientId -- Current <code>UIComponent</code>'s client id</li>
     *	    <li>id -- Current <code>UIComponent</code>'s id</li>
     *	    <li>layoutElement -- Current {@link LayoutElement}</li>
     *	    <li>parent -- Parent <code>UIComponent</code></li>
     *	    <li>parentId -- Parent <code>UIComponent</code>'s client id</li>
     *	    <li>parentLayoutElement -- Parent {@link LayoutElement}</li>
     *	    <li>namingContainer -- Nearest <code>NamingContainer</code></li>
     *	    <li>valueBinding -- <code>ValueBinding</code> representing the
     *		<code>UIComponent</code></li>
     *	</ul>
     */
    public static class ThisDataSource implements DataSource {
	/**
	 *  <p>	See class JavaDoc.</p>
	 *
	 *  @param  ctx		The <code>FacesContext</code>
	 *  @param  desc	The <code>LayoutElement</code>
	 *  @param  comp	The <code>UIComponent</code>
	 *  @param  key		The key used to obtain information from this
	 *			<code>DataSource</code>.
	 *
	 *  @return The value resolved from key.
	 */
	public Object getValue(FacesContext ctx, LayoutElement desc,
		UIComponent comp, String key) {
	    Object value = null;

	    if ((key.equalsIgnoreCase(CLIENT_ID)) || (key.length() == 0)) {
		value = comp.getClientId(ctx);
	    } else if (key.equalsIgnoreCase(ID)) {
		value = comp.getId();
	    } else if (key.equalsIgnoreCase(COMPONENT)) {
		value = comp;
	    } else if (key.equalsIgnoreCase(LAYOUT_ELEMENT)) {
		value = desc;
	    } else if (key.equalsIgnoreCase(PARENT_ID)) {
		value = comp.getParent().getId();
	    } else if (key.equalsIgnoreCase(PARENT_CLIENT_ID)) {
		value = comp.getParent().getClientId(ctx);
	    } else if (key.equalsIgnoreCase(PARENT)) {
		value = comp.getParent();
	    } else if (key.equalsIgnoreCase(PARENT_LAYOUT_ELEMENT)) {
		value = desc.getParent();
	    } else if (key.equalsIgnoreCase(NAMING_CONTAINER)) {
		for (value = comp.getParent(); value != null;
			value = ((UIComponent) value).getParent()) {
		    if (value instanceof NamingContainer) {
			break;
		    }
		}
	    } else if (key.equalsIgnoreCase(VALUE_BINDING)) {
		// Walk backward up the tree generate the path
		Stack stack = new Stack();
		String id = null;
		// FIXME: b/c of a bug, the old behavior actually returned the
		// FIXME: parent component... the next line is here to persist
		// FIXME: this behavior b/c some code depends on this, fix this
		// FIXME: when you have a chance.
		comp = comp.getParent();
		while ((comp != null) && !(comp instanceof UIViewRoot)) {
		    id = comp.getId();
		    if (id == null) {
			// Generate an id based on the clientId
			id = comp.getClientId(ctx);
			id = id.substring(id.lastIndexOf(
			    NamingContainer.SEPARATOR_CHAR)+1);
		    }
		    stack.push(id);
		    comp = comp.getParent();
		}
		StringBuffer buf = new StringBuffer();
		buf.append("view");
		while (!stack.empty()) {
		    buf.append("."+stack.pop());
		}
		value = buf.toString();
	    } else {
		throw new IllegalArgumentException("'" + key
		    + "' is not valid in $this{" + key + "}.");
	    }

	    return value;
	}

	/**
	 *  <p> Defines "component" in $this{component}.  Returns the
	 *	UIComponent object.</p>
	 */
	public static final String COMPONENT		= "component";

	/**
	 *  <p> Defines "clientId" in $this{clientId}.  Returns
	 *	the String representing the client id for the UIComponent.</p>
	 */
	public static final String CLIENT_ID		= "clientId";

	/**
	 *  <p> Defines "id" in $this{id}.  Returns the String representing
	 *	the id for the UIComponent.</p>
	 */
	public static final String ID			= "id";

	/**
	 *  <p> Defines "layoutElement" in $this{layoutElement}.  Returns
	 *	the LayoutElement.</p>
	 */
	public static final String LAYOUT_ELEMENT	= "layoutElement";

	/**
	 *  <p> Defines "parent" in $this{parent}.  Returns the
	 *	parent UIComponent object.</p>
	 */
	public static final String PARENT		= "parent";

	/**
	 *  <p> Defines "parentId" in $this{parentId}.  Returns the
	 *	parent UIComponent object's Id.</p>
	 */
	public static final String PARENT_ID		= "parentId";

	/**
	 *  <p> Defines "parentClientId" in $this{parentClientId}.  Returns the
	 *	parent UIComponent object's client Id.</p>
	 */
	public static final String PARENT_CLIENT_ID	= "parentClientId";

	/**
	 *  <p> Defines "parentLayoutElement" in $this{parentLayoutElement}.
	 *	Returns the parent LayoutElement.</p>
	 */
	public static final String PARENT_LAYOUT_ELEMENT =
		"parentLayoutElement";

	/**
	 *  <p> Defines "namingContainer" in $this{namingContainer}.  Returns
	 *	the nearest naming container object (i.e. the form).</p>
	 */
	public static final String NAMING_CONTAINER	= "namingContainer";

	/**
	 *  <p> Defines "valueBinding" in $this{valueBinding}.  Returns
	 *	a <code>ValueBinding</code> to this UIComponent.</p>
	 */
	public static final String VALUE_BINDING	= "valueBinding";
    }

    /**
     *	The main function for this class provides some simple test cases.
     *
     *	@param	args	The commandline arguments.
     */
    public static void main(String args[]) {
	String test = null;
	String good = null;

	test = "" + VariableResolver.resolveVariables(null, null, null,
	    "$escape($escape(LayoutElement))", "$", "(", ")");
	good = "LayoutElement";
	System.out.println("Expected Result: '" + good + "'");
	System.out.println("         Result: '" + test + "'");
	if (!test.equals(good)) {
	    System.out.println("FAILED!!!!");
	}

	test = "" + VariableResolver.resolveVariables(null, null, null,
	    "$escape($escape(EEPersistenceManager))", "$", "(", ")");
	good = "EEPersistenceManager";
	System.out.println("Expected Result: '" + good + "'");
	System.out.println("         Result: '" + test + "'");
	if (!test.equals(good)) {
	    System.out.println("FAILED!!!!");
	}

	test = "" + VariableResolver.resolveVariables(null, null, null,
	    "$es$cape$escape(EEPersistenceManager))", "$", "(", ")");
	good = "$es$capeEEPersistenceManager)";
	System.out.println("Expected Result: '" + good + "'");
	System.out.println("         Result: '" + test + "'");
	if (!test.equals(good)) {
	    System.out.println("FAILED!!!!");
	}

	test = "" + VariableResolver.resolveVariables(null, null, null,
	    "$escape($escapeEEP$ersistenceManager))", "$", "(", ")");
	good = "$escapeEEP$ersistenceManager)";
	System.out.println("Expected Result: '" + good + "'");
	System.out.println("         Result: '" + test + "'");
	if (!test.equals(good)) {
	    System.out.println("FAILED!!!!");
	}

	test = "" + VariableResolver.resolveVariables(null, null, null,
	    "$escape($escape(EEPersistenceManager)))", "$", "(", ")");
	good = "EEPersistenceManager)";
	System.out.println("Expected Result: '" + good + "'");
	System.out.println("         Result: '" + test + "'");
	if (!test.equals(good)) {
	    System.out.println("FAILED!!!!");
	}

	test = "" + VariableResolver.resolveVariables(null, null, null,
	    "$escape($escape(EEPersistenceManager())", "$", "(", ")");
	good = "$escape(EEPersistenceManager()";
	System.out.println("Expected Result: '" + good + "'");
	System.out.println("         Result: '" + test + "'");
	if (!test.equals(good)) {
	    System.out.println("FAILED!!!!");
	}

	test = "" + VariableResolver.resolveVariables(null, null, null,
	    "$escape($escape($escape(EEPersistenceManager()))==$escape("
	    + "EEPersistenceManager()))", "$", "(", ")");
	good = "EEPersistenceManager()==EEPersistenceManager()";
	System.out.println("Expected Result: '" + good + "'");
	System.out.println("         Result: '" + test + "'");
	if (!test.equals(good)) {
	    System.out.println("FAILED!!!!");
	}

	test = "" + VariableResolver.resolveVariables(null, null, null,
	    "$escape($escape($escape(EEPersistenceManager()))==$escape("
	    + "EEPersistenceManager()))", "$", "(", ")");
	good = "EEPersistenceManager()==EEPersistenceManager()";
	System.out.println("Expected Result: '" + good + "'");
	System.out.println("         Result: '" + test + "'");
	if (!test.equals(good)) {
	    System.out.println("FAILED!!!!");
	}

	/*
	for (int x = 0; x < 100000; x++) {
	    System.out.println("" + VariableResolver.resolveVariables(
		null, null, null,
		"$escape($escape(EEPers" + x + "istenceManager()))==$escape("
		+ "EEPersistenceManager())", "$", "(", ")"));
	}
	*/
    }


    /**
     *	<p> Contains the {@link VariableResolver.DataSource}'s for
     *	    $&lt;type&gt;{&lt;variable&gt;} syntax.</p>
     */
    private static Map dataSourceMap			= new HashMap();

    /**
     *	<p> Defines "attribute" in $attribute{...}.  This allows you to
     *	    retrieve an HttpRequest attribute.</p>
     */
    public static final String	    ATTRIBUTE		= "attribute";

    /**
     *	<p> Defines "pageSession" in $pageSession{...}.  This allows you to
     *	    retrieve a PageSession attribute.</p>
    public static final String	    PAGE_SESSION	= "pageSession";
    */

    /**
     *	<p> Defines "property" in $property{...}.  This allows you to
     *	    retrieve a property from the UIComponent.</p>
     */
    public static final String	    PROPERTY		= "property";

    /**
     *	<p> Defines "hasProperty" in $hasProperty{...}.  This allows you to
     *	    see if a property from the UIComponent exists.</p>
     */
    public static final String	    HAS_PROPERTY	= "hasProperty";

    /**
     *	<p> Defines "hasFacet" in $hasFacet{...}.  This allows you to
     *	    see if a facet from the UIComponent exists.</p>
     */
    public static final String	    HAS_FACET		= "hasFacet";

    /**
     *	<p> Defines "session" in $session{...}.  This allows you to retrieve
     *	    an HttpSession attribute.
     */
    public static final String	    SESSION		= "session";

    /**
     *	<p> Defines "style" in $style{...}.  This allows you to retrieve
     *	    a style classes from the current {@link com.sun.rave.web.ui.theme.Theme}.
     */
    public static final String	    STYLE		= "style";

    /**
     *	<p> Defines "theme" in $theme{...}.  This allows you to
     *	    retrieve a Theme message from the current
     *	    {@link com.sun.rave.web.ui.theme.Theme}.
     */
    public static final String	    THEME		= "theme";

    /**
     *	<p> Defines "themeScript" in $themeScript{...}.  This allows you to
     *	    retrieve a JavaScript classes from the current
     *	    {@link com.sun.rave.web.ui.theme.Theme}.
     */
    public static final String	    THEME_JS		= "themeScript";

    /**
     *	<p> Defines "requestParameter" in $requestParameter{...}.  This allows
     *	    you to retrieve a HttpRequest parameter (QUERY_STRING
     *	    parameter).</p>
     */
    public static final String	    REQUEST_PARAMETER	= "requestParameter";

    /**
     *	<p> Defines "display" in $display{...}.  This allows you to retrive
     *	    a DisplayField value.</p>
    public static final String	    DISPLAY             = "display";
     */

    /**
     *	<p> Defines "this" in $this{...}.  This allows you to retrieve a
     *	    number of different objects related to the relative placement of
     *	    this expression.</p>
     *
     *	@see ThisDataSource
     */
    public static final String	    THIS		= "this";

    /**
     *	<p> Defines "escape" in $escape{...}.  This allows some reserved
     *	    characters to be escaped in "if" attributes.  Such as '=' or
     *	    '|'.</p>
     */
    public static final String	    ESCAPE		= "escape";

    /**
     *	<p> Defines "boolean" in $boolean{...}.  This converts the given
     *	    String to a Boolean.</p>
     */
    public static final String	    BOOLEAN		= "boolean";

    /**
     *	<p> Defines "browser" in $browser{...}.  This checks properties of the
     *	    browser that sent the request.</p>
     */
    public static final String	    BROWSER		= "browser";

    /**
     *	<p> Defines "int" in $int{...}.  This converts the given String to an
     *	    Integer.</p>
     */
    public static final String	    INT			= "int";

    /**
     *	<p> Defines "methodBinding" in $methodBinding{...}.  This allows
     *	    MethodBindings in to be created.</p>
     */
    public static final String	    METHOD_BINDING	= "methodBinding";

    /**
     *	<p> Defines "constant" in $constant{...}.  This allows constants
     *	    in java classes to be accessed.</p>
     */
    public static final String	    CONSTANT		= "constant";

    /**
     *	<p> Defines "resource" in $resource{...}.  This allows resource
     *	    to be accessed.</p>
     */
    public static final String	    RESOURCE		= "resource";


    // Static initializer to setup DataSources
    static {
	AttributeDataSource attrDS = new AttributeDataSource();
	dataSourceMap.put(ATTRIBUTE, attrDS);
	dataSourceMap.put("", attrDS);
//	dataSourceMap.put(PAGE_SESSION, new PageSessionDataSource());
	dataSourceMap.put(PROPERTY, new PropertyDataSource());
	dataSourceMap.put(HAS_PROPERTY, new HasPropertyDataSource());
	dataSourceMap.put(HAS_FACET, new HasFacetDataSource());
	dataSourceMap.put(SESSION, new SessionDataSource());
	dataSourceMap.put(STYLE, new StyleDataSource());
	dataSourceMap.put(THEME, new ThemeDataSource());
	dataSourceMap.put(THEME_JS, new ThemeJavaScriptDataSource());
	dataSourceMap.put(REQUEST_PARAMETER, new RequestParameterDataSource());
//	dataSourceMap.put(DISPLAY, new DisplayFieldDataSource());
	dataSourceMap.put(THIS, new ThisDataSource());
	dataSourceMap.put(ESCAPE, new EscapeDataSource());
	dataSourceMap.put(INT, new IntDataSource());
	dataSourceMap.put(BOOLEAN, new BooleanDataSource());
	dataSourceMap.put(BROWSER, new BrowserDataSource());
	dataSourceMap.put(CONSTANT, new ConstantDataSource());
	dataSourceMap.put(RESOURCE, new ResourceBundleDataSource());
	dataSourceMap.put(METHOD_BINDING, new MethodBindingDataSource());
    }

    /**
     *	Constant defining the arguments required for a Action MethodBinding.
     */
    private static Class actionArgs[] = { ActionEvent.class };

    /**
     *	The '$' character marks the beginning of a substituion in a String.
     */
    public static final String SUB_START	= "$";


    /**
     *	The '(' character marks the beginning of the data content of a String
     *	substitution.
     */
    public static final String SUB_TYPE_DELIM	= "{";


    /**
     *	The ')' character marks the end of the data content for a String
     *	substitution.
     */
    public static final String SUB_END		= "}";
}
