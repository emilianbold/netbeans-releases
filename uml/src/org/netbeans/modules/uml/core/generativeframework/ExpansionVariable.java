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


package org.netbeans.modules.uml.core.generativeframework;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 */
public class ExpansionVariable implements IExpansionVariable {
    public static final int EOK_NONE    = 0;
    public static final int EOK_AND     = 1;
    public static final int EOK_OR      = 2;
    
	protected IVariableExpander m_Expander = null;
    protected IExpansionVariable m_ExpansionVar = null;
    
	/**
	* The is the XML DOM node that represents this element.
	*/
    protected Node m_Node = null;
    protected String m_Results = null;
    protected ETList<Node> m_ResultNodes = null;
    protected boolean m_BoolResultCalculated = false;
    protected boolean m_IsTrue = false;

	public String getName() {
		return getValue( "name" );
	}

	public void setName(String value) {
		setValue( "name", value );
	}
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IExpansionVariable#setOperator(int)
     */
    public void setOperator(int operator)
    {
        setValue("operator", operator == EOK_AND? "and" : "or");
    }
    
	/* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IExpansionVariable#getOperator()
     */
    public int getOperator()
    {
        // Boolean variables default to an 'OR' operator if not set.
        return "and".equals(getValue("operator"))? EOK_AND : EOK_OR;
    }

	public String getIDLookup() {
		return getValue( "idLookup" );
	}

	public void setIDLookup(String value) {
		setValue( "idLookup", value );
	}

	public String getExpansionName() {
		return getValue( "expand" );
	}

	public void setExpansionName(String value) {
		setValue( "expand", value );
	}

	public String getQuery() {
		return getValue( "query" );
	}

	public void setQuery(String value) {
		setValue( "query", value );
	}

	public String getMethodGet() {
		return getValue( "methodGet" );
	}

	public void setMethodGet(String value) {
		setValue( "methodGet", value );
	}

	/**
	 *
	 * Checks the Query property to see if it begins with an '@'. If it does,
	 * it is assumed that the results of the query produce an IXMLDOMAttribute.
	 *
	 * @param pVal[out]  - true if the results of this variables expansion
	 *                     result in a IXMLDOMAttribute, else false if
	 *                     it results in elements.
	 *
	 * @return HRESULT
	 *
	 */
	public boolean getIsAttributeResult() {
		return isAttributeResult();
	}

	public IVariableExpander getExecutionContext() {
		return m_Expander;
	}

	public void setExecutionContext(IVariableExpander value) {
		m_Expander = value;
	}

	public String getXSLFilter() {
		return getValue( "xslFilter" );
	}

	public void setXSLFilter(String value) {
		setValue( "xslFilter", value );
	}

	public IExpansionVariable getExpansionVariable() {
		return m_ExpansionVar;
	}

	public void setExpansionVariable(IExpansionVariable value) {
		m_ExpansionVar = value;
	}

	public Node getNode() {
		return m_Node;
	}

	public void setNode(Node value) {
		m_Node = value;
	}

	/**
	 *
	 * Expands this variable, returning the results.
	 *
	 * @param context[in]   The context from which to expand from.
	 * @param results[out]  The results of the expansion
	 *
	 * @return HRESULT
	 *
	 */
	public String expand(Node context)
    {
		String result = null;
		if (!isNodeDeleted(context))
		{
            validate();
            
			int kind = VariableKind.VK_NONE;
			kind = getKind();
			if (kind == VariableKind.VK_NODE_NAME)
			{
				m_Results = context.getName();
			}
			else
			{
				if (kind == VariableKind.VK_ATTRIBUTE)
				{
					// The query is simply pointing us at the value of a particular
					// xml attribute
					m_Results = retrieveAttributeResult(context);
				}
				else
				{
					String query = getQuery();
					if (query != null && query.length() > 0)
					{
						if (isAttributeResult())
						{
							// We could have a varType that isn't VK_ATTRIBUTE, but
							// whose query actually is an attribute query
							m_Results = retrieveAttributeResult(context);
						}
                        else if (isPreferenceQuery())
                        {
                            m_Results = retrievePreferenceResult(query);
                        }
						else
						{
							retrieveElementResults(context, query);
						}
					}
					else
					{
						String idLookup = getIDLookup();
						if (idLookup != null && idLookup.length() > 0)
						{
							retrieveIDResults(context, idLookup);
						}
						else
						{
							String methodGet = getMethodGet();
							if (methodGet != null && methodGet.length() > 0)
							{
                                try
                                {
                                    retrieveGetResults(context, methodGet);
                                }
                                catch (NoSuchMethodException e)
                                {
                                    e.printStackTrace();
                                }
							}
						}
					}
				}
				// Now check to see if there is an expansion variable to be used
				if (useExpansionVar())
				{
					String expansionVar = getExpansionName();
					if (expansionVar != null && expansionVar.length() > 0)
					{
						m_Results = resolveExpansionVar(context, expansionVar);
					}
				}
			}
			String preResult = null;
			if (m_Results != null && m_Results.length() > 0)
			{
				// Check to see if there is a ReplaceFilter installed. If so,
				// process it.
				String replaceFilter = getReplaceFilter();
				if (replaceFilter != null && replaceFilter.length() > 0)
				{
					m_Results = processReplaceFilter(replaceFilter, m_Results);
				}
				preResult = m_Results;
			}
			
			// If this var is of type VK_BOOLEAN, then
			// the actual results will be "true" or "false", dependent
			// on the results gathered so far
			if (isBooleanVar())
			{
				preResult = getBooleanResult(preResult);
			}
			if (preResult != null && preResult.length() > 0)
			{
				preResult = filterResults(preResult, null);
				result = preResult;
			}
		}
		return result;
	}
    
    public String expand(IElement context)
    {
        return context != null? expand(context.getNode()) : null;
    }

    protected boolean isPreferenceQuery()
    {
        return getKind() == VariableKind.VK_PREFERENCE;
    }
    
    protected String retrievePreferenceResult(String query)
    {
        if (query != null)
        {
            ICoreProduct product = ProductRetriever.retrieveProduct();
            IPreferenceManager2 prefMan = product.getPreferenceManager();
            if (prefMan != null)
                return prefMan.getPreferenceValue(query);
        }
        return null;
    }
    
	/**
     * Attempts to filter the toFilter value with an appropriate value found
     * in the ValueFilter property.
     * 
	 * @param toFilter The String to be filtered.
	 * @param filter   The String that contains the various values to be used
     *                 as the filter.
	 * @return The filtered String.
	 */
	protected String filterResults(String toFilter, String filter)
    {
        if (toFilter == null || toFilter.length() == 0 || 
                (filter == null && (filter = getValueFilter()) == null) || 
                filter.length() == 0)
            return toFilter;
        
        ETList<String> tokens = StringUtilities.splitOnDelimiter(filter, ",");
        for (Iterator<String> iter = tokens.iterator(); iter.hasNext(); )
        {
            String token = iter.next();
            // Split the filter pair
            ETList<String> pair = StringUtilities.splitOnDelimiter(token, "=");
            
            if (pair.size() == 2)
            {
                // The first token of the pair is what we should look for in the
                // string to be filtered, and the second token is what we should
                // replace the first with, assuming we find the first.
                String toFind = pair.get(0).trim(),
                       toRepl = pair.get(1).trim();
                if (toFind.length() > 0 && toFind.equals(toFilter))
                {
                    toFilter = toRepl;
                    break;
                }
            }
        }
		return toFilter;
	}

	/**
	 * @param replaceFilter
	 * @param m_Results
	 */
	protected String processReplaceFilter(String replaceFilter, String results) 
    {
		if (replaceFilter != null && results != null)
        {
            ETList<String> tokens = 
                StringUtilities.splitOnDelimiter(replaceFilter, "=");
            if (tokens.size() == 2)
            {
                String replace = tokens.get(0),
                       with    = tokens.get(1);
                results = 
                    StringUtilities.replaceAllSubstrings(
                        results, replace, with);
            }
        }
        return results;
	}

    /**
     *
     * Fires the get property specified in the passed in methodGet parameter
     *
     * @param context      The node that will be queried
     * @param methodGet    Name of the property to perform a Get call on
     * @return The property value.
     */
	protected Object retrieveGetResults(Node context, String methodGet)
            throws NoSuchMethodException
    {
        if (methodGet == null || methodGet.length() == 0)
            throw new IllegalArgumentException("Property name cannot be empty");

        IElement element = 
            new TypedFactoryRetriever<IElement>().createTypeAndFill(context);
        if (element != null)
        {
            String method = "get" + methodGet.substring(0, 1).toUpperCase()
                            + methodGet.substring(1);
            Method m = element.getClass().getMethod(method, (Class[])null);
            try 
            {
                Object res = m.invoke(element, (Object[])null);
                
                if (res instanceof IVersionableElement)
                {
                    addToResultNodes((IVersionableElement) res);
                }
                else if (res != null)
                {
                    if (m_Results != null)
                        m_Results += res;
                    else
                        m_Results = res.toString();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
	}

	/**
	 * @param context
	 * @param query
	 */
	protected void retrieveElementResults(Node context, String query) {
        List nodes = context.selectNodes(query);
        if (nodes.size() > 0)
        {
            if (isNodeValue())
            {
                Node first = (Node) nodes.get(0);
                if (first instanceof Element)
                {    
                    Element el = (Element) first;
                    if (el.nodeCount() > 0)
                        m_Results = el.node(0).getText();
                }
            }
            else
            {
                addToResultNodes( nodes );
            }
        }
	}

    protected void addToResultNodes(IVersionableElement ver)
    {
        if (ver != null)
            addToResultNodes(ver.getNode());
    }
    
	/**
     * @param nodes
     */
    protected void addToResultNodes(List nodes)
    {
        for (Iterator iter = nodes.iterator(); iter.hasNext(); )
        {
            Node element = (Node) iter.next();
            addToResultNodes(element);
        }
    }
    
    /**
     * @param element
     */
    protected void addToResultNodes(Node node)
    {
        ETList<Node> nodes = getResultNodes();
        if (node != null && !isNodeDeleted(node))
            nodes.add(node);
    }

    /**
	 *
	 * Determines whether or not the Query property results in the
	 * return of an XML attribute whose value is what the expansion
	 * variable is interested in.
	 *
	 * @return true if an xml attribute, else false
	 *
	 */
	protected boolean isAttributeResult()
	{
		boolean isAttResult = false;
		String query = getQuery();
		if (query != null && query.startsWith("@"))
		{
			isAttResult = true;
		}
		return isAttResult;
	}

	/**
	 *
	 * Retrieves the results found in the attribute query this expansion var
	 * represents
	 *
	 * @param context[in]   The context that we are querying against
	 * @param results[out]  The results of the query
	 *
	 * @return HRESULT
	 * @warning It is assumed that the Query property results in an xml attribute
	 *          being returned. Calling get_IsAttributeResult() can determine if that
	 *          is the case before calling this method.
	 *
	 */
	protected String retrieveAttributeResult(Node context)
	{
		String query = getQuery();
		return retrieveAttributeResult(context, query);
	}

	/**
	 *
	 * Retrieves the results found in the passed in query this expansion var
	 * represents
	 *
	 * @param context[in]   The context that we are querying against
	 * @param query[in]     The query to perform on context
	 * @param results[out]  The results of the query
	 *
	 * @return HRESULT
	 *
	 */
	protected String retrieveAttributeResult(Node context, String query)
	{
		String result = null;
		if (query != null && query.length() > 0)
		{
			String valueFilter = getValueFilter();
			
			//since query starts with @, we need to remove it to get the 
			//attribute name - so substring(1).
			result = XMLManip.getAttributeValue(context, query.substring(1));
			
			// Check to see if we have a value filter applied
			if (valueFilter != null && valueFilter.length()>0)
			{
				result = filterResults(result, valueFilter);
			}
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IExpansionVariable#getResultNodes()
	 */
	public ETList<Node> getResultNodes() {
        if (m_ResultNodes == null)
            m_ResultNodes = new ETArrayList<Node>();
		return m_ResultNodes;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IExpansionVariable#setResultNodes(org.dom4j.Node[])
	 */
	public void setResultNodes(ETList<Node> value) {
        m_ResultNodes = value;
	}

	/**
	 *
	 * The kind of this variable. See the documentation in ExpansionVar.etc
	 * in the config directory for details.
	 *
	 * @param pVal[out] The current type
	 *
	 * @return HRESULT
	 *
	 */
	public int getKind() {
		int retInt = VariableKind.VK_NONE;
		String value = getValue("varType");
		if (value == null || value.length() == 0)
		{
			if (isAttributeResult())
			{
				retInt = VariableKind.VK_ATTRIBUTE;
			}
			else
			{
				String query = getQuery();
				if (query != null && query.length() > 0)
				{
					retInt = VariableKind.VK_NODES;
				}
			}
		}
		else
		{
			retInt = translateKind(value);
		}
		return retInt;
	}

	/**
	 *
	 * Sets the kin of this variable
	 *
	 * @param newVal[in] The new kind
	 *
	 * @return HRESULT
	 *
	 */
	public void setKind(int value) {
		setValue( "varType", translateKind( value ));
	}

	public String getTypeFilter() {
		return getValue( "type" );
	}

	public void setTypeFilter(String value) {
		setValue("type", value);
	}

	public String getResults() {
		return m_Results;
	}

	public void setResults(String value) {
		m_Results = value;
	}

	public String getValueFilter() {
		return getValue( "valueFilter" );
	}

	public void setValueFilter(String value) {
		setValue( "valueFilter", value );
	}

	public String getReplaceFilter() {
		return getValue( "replaceFilter" );
	}

	public void setReplaceFilter(String value) {
		setValue( "replaceFilter", value );
	}

	public String getOverrideName() {
		return getValue( "overrides" );
	}

	public void setOverrideName(String value) {
		setValue( "overrides", value );
	}

	/**
	 *
	 * Retrieves the value used to determine whether or not this expansion var
	 * expands to "true" or "false". Used only in conjunction with a var that
	 * is of type VK_BOOLEAN
	 *
	 * @param pVal[out] The value
	 *
	 * @return HRESULT
	 *
	 */
	public String getTrueValue() {
		return getValue( "trueValue" );
	}

	/**
	 *
	 * Sets the value used to determine whether or not this expansion var
	 * expands to "true" or "false". Used only in conjunction with a var that
	 * is of type VK_BOOLEAN
	 *
	 * @param pVal[in] The "true" value
	 *
	 * @return HRESULT
	 *
	 */
	public void setTrueValue(String value) {
		setValue( "trueValue", value );
	}

	/**
	 *
	 * Useful only when this expansion variable is a Boolean variable, the routine
	 * checks to see if this variable expanded into a true or false result
	 *
	 * @param pVal[out]  True if this var expanded to a true result, else false
	 *
	 * @return HRESULT
	 *
	 */
	public boolean getIsTrue() {
		return m_IsTrue;
	}

	public void setIsTrue(boolean value) {
		m_IsTrue = value;
	}
	
	/**
	 *
	 * Retrieves a particular value of one of the xml attributes on the
	 * expansion variable.
	 *
	 * @param attrName[in]  Name of the xml attribute to query.
	 * @param value[out]    The value of that attribute
	 *
	 * @return HRESULT
	 *
	 */
	protected String getValue(String name)
	{
		String value = null;
		if (m_Node != null)
		{
			value = XMLManip.getAttributeValue(m_Node, name);
		}
		return value;
	}

	/**
	 *
	 * Sets the value of the passed in xml attribute on this node.
	 *
	 * @param attrName[in] Name of the xml attribute to set
	 * @param newValue[in]  The new value of that attribute
	 *
	 * @return HRESULT
	 *
	 */
	protected void setValue(String name, String value)
	{
		if (m_Node != null)
		{
			XMLManip.setAttributeValue(m_Node, name, value);
		}
	}
	
	/**
	 *
	 * Performs the query behind the IDLookup query.
	 *
	 * @param context[in]   The element to perform the query on
	 * @param query[in]     The query to perform
	 *
	 * @return HRESULT
	 *
	 */
	protected void retrieveIDResults(Node context, String query)
	{
		// Check to see if this idLookup has more than one id to resolve
		if (query != null)
		{
			int pos = query.indexOf(' ');
			if (pos >= 0)
			{
				followLookups(context, query.split(" "));
			}
			else
			{
				resolveIDLookup(context, query);
			}
		}
	}
	
	/**
	 * @param context
	 * @param query
	 */
	protected void resolveIDLookup(Node context, String query) {
        String id = retrieveAttributeResult(context, query);
        if (id != null && id.length() > 0)
        {
            ETList<String> tokens = StringUtilities.splitOnDelimiter(id, " ");
            int count = tokens.size();
            for (int i = 0; i < count; ++i)
            {
                String tid = tokens.get(i);
                Node foundNode = UMLXMLManip.findElementByID(context, tid);
                if (foundNode != null)
                    filterResultNode(foundNode);
            }
        }
	}
    
    protected boolean filterResultNode(Node node)
    {
        String typeFilter = getTypeFilter();
        String nodeName   = ((Element) node).getQualifiedName();
        if (typeFilter == null || typeFilter.length() == 0 
                || nodeName.equals(typeFilter))
        {
            addToResultNodes(node);
            return true;
        }
        return false;
    }

	/**
	 * @param context
	 * @param strings
	 */
	protected void followLookups(Node context, String[] strings) 
	{
		if (context != null && strings != null)
		{
			int count = strings.length;  
			if (count > 0)
			{
				String str = strings[0];
				String id = retrieveAttributeResult(context, str);
				if (id != null && id.length() > 0)
				{
					Node foundNode = UMLXMLManip.findElementByID(context, id);
					if (foundNode != null)
					{
						String[] newStrings = new String[count-1];
						for (int y = 0; y < count-1; y++)
						{
							newStrings[y] = strings[y+1];
						}
						followLookups(foundNode, newStrings);
					}
				}
			}
			else
			{
				filterResultNode(context);
			}
		}
	}

	protected String resolveExpansionVar(Node context, String name)
	{
		String expansionName = name;
        String result = null;
        
		if (name == null)
		{
			expansionName = getExpansionName();
		}
		if (expansionName != null && expansionName.length() > 0)
		{
            ETList<String> expandNames = 
                StringUtilities.splitOnDelimiter(expansionName, " ");
			boolean isBool = isBooleanVar();

			int operatorKind = getOperator();
            for (Iterator<String> iter = expandNames.iterator(); 
                    iter.hasNext(); )
            {
                String varS = iter.next();
                IExpansionVariable var = retrieveVarNode(varS);

                if (m_ResultNodes != null)
                {
                    if (var == null) continue;
                    int kind = var.getKind();
                    if (kind != VariableKind.VK_NODES)
                    {
                        int nodeCount = m_ResultNodes.size();
                        if (nodeCount > 0)
                        {
                            Node node = m_ResultNodes.get(nodeCount - 1);
                            if (node != null)
                                result = resolveExpansionVar(var, node);
                        }
                    }
                    else
                    {
                        result = resolveExpansionVar(var, context);
                    }
                }
                else if (context != null && var != null)
                {
                    result = resolveExpansionVar(var, context);
                }
                
                if (isBool)
                {
                    // If this variable is boolean, then we are looking for the
                    // first expansion that is true. Once we find it, bail
                    String boolResult = getBooleanResult(result);
                    boolean isTrue = Boolean.valueOf(boolResult).booleanValue();
                    
                    if (isTrue && operatorKind == EOK_OR)
                        result = boolResult;
                    else if (!isTrue && operatorKind == EOK_AND)
                    {
                        // No need to continue evaluating vars...
                        m_BoolResultCalculated = true;
                        m_IsTrue = false;
                        break;
                    }
                    else
                    {
                        // If we are looping over several boolean expansion
                        // variables, we want to make sure that we will
                        // calculate the boolean result (when
                        // GetBooleanResult is called).  So, we clear this
                        // flag.

                        m_BoolResultCalculated = false;
                    }
                }
            }
		}
        return result;
	}
	
	/**
	 * @param string
	 * @param n
	 * @return
	 */
	protected String resolveExpansionVar(IExpansionVariable var, Node context) {
        String varResults = var.expand(context);
        if (varResults == null || varResults.length() == 0)
        {
            ETList<Node> nodes = var.getResultNodes();
            if (nodes != null && nodes.size() > 0)
            {
                getResultNodes().addAll(nodes);
            }
        }
        return varResults;
	}

	/**
	 *
	 * Determines whether or not a query needs to have the nodeValue property performed on the node
	 * returned. This would be true in the case of the documentation field, as this is a text node
	 *
	 * @return true if the query returns an element that needs to be queried for its text value,
	 *         else false.
	 *
	 */
	protected boolean isNodeValue()
	{
	   boolean result = false;

	   int kind = VariableKind.VK_NONE;
   
	   kind = getKind();
	   if( kind == VariableKind.VK_TEXT_VALUE )
	   {
		  result = true;
	   }
	   return result;
	}
	
	/**
	 *
	 * Retrieves the expansion variable with the passed in name.
	 *
	 * @param varName[in]      Name of the expansion variable to retrieve
	 * @param newVar[out]      The new variable, else 0 if not found
	 *
	 * @return HRESULT
	 *
	 */
	protected IExpansionVariable retrieveVarNode(String varName)
	{
		IExpansionVariable newVar = null;
		if (m_Expander != null)
		{
			Node varNode = m_Expander.retrieveVarNode(varName);
			if (varNode != null)
			{
				ITemplateManager tMan = m_Expander.getManager();
				if (tMan != null)
				{
					IVariableFactory fact = tMan.getFactory();
					if (fact != null)
					{
						newVar = fact.createVariable(varNode);
					}
				}
			}
		}
		return newVar;
	}
	
	/**
	 *
	 * Translates the passed in value into a VariableKind enum value
	 *
	 * @param val[in] The text to convert
	 *
	 * @return The enum representation of the text value, else VK_NONE
	 *
	 */
	protected int translateKind( String val )
	{
	   int kind = VariableKind.VK_NONE;

	   if( val.length() > 0 )
	   {
		  if( val.equals("attr" ))
		  {
			 kind = VariableKind.VK_ATTRIBUTE;
		  }
		  else if( val.equals("nodes" ))
		  {
			 kind = VariableKind.VK_NODES;
		  }
		  else if( val.equals( "text" ))
		  {
			 kind = VariableKind.VK_TEXT_VALUE;
		  }
		  else if( val.equals("nodeName" ))
		  {
			 kind = VariableKind.VK_NODE_NAME;
		  }
		  else if( val.equals( "boolean" ))
		  {
			 kind = VariableKind.VK_BOOLEAN;
		  }
	   }
	   return kind;
	}

	/**
	 *
	 * Translates the VariableKind enum value coming in into
	 * its text representation
	 *
	 * @param newVal[in] The enum value to translate
	 *
	 * @return The text equivalent
	 *
	 */
	protected String translateKind( int newVal )
	{
	   String val = null;

	   if ( newVal == VariableKind.VK_ATTRIBUTE)
	   {
			val = "attr";
	   }
	   else if ( newVal == VariableKind.VK_NODES)
	   {
			val = "nodes";
	   }
	   else if ( newVal == VariableKind.VK_TEXT_VALUE)
	   {
			val = "text";
	   }
	   else if ( newVal == VariableKind.VK_NODE_NAME)
	   {
			val = "nodeName";
	   }
	   else if ( newVal == VariableKind.VK_BOOLEAN)
	   {
			val = "boolean";
	   }
	   return val;
	}
	
	/**
	 *
	 * Determines whether or not this var is a boolean var
	 *
	 * @return true if it is a boolean, else false
	 *
	 */
	protected boolean isBooleanVar()
	{
	   boolean isBoolean = false;

	   int  kind = getKind( );

	   if( kind == VariableKind.VK_BOOLEAN )
	   {
		  isBoolean = true;
	   }
	   return isBoolean;
	}
	
	/**
	 * Determines whether or not the node passed in is in a deleted
	 * state
	 *
	 * @param context[in] The node to check
	 * @return true if the node is deleted else false
	 */
	protected boolean isNodeDeleted(Node context)
	{
	   boolean isDeleted = false;
	   if( context != null)
	   {
	   	  String deleted = XMLManip.getAttributeValue( context, "isDeleted");
          
	   	  if ("true".equals(deleted))
	   	  {
	   	  	isDeleted = true;
	   	  }
	   }
	   return isDeleted;
	}
	
	/**
	 * Determines if it is ok to use the Expansion Var, if available
	 *
	 * @return true or false
	 */


	protected boolean useExpansionVar()
	{
	   boolean use = true;

	   long numNodes = 0;
	   if( m_ResultNodes != null)
	   {
		  numNodes = m_ResultNodes.size();
	   }
	   if( numNodes == 0 )
	   {
		  // If we don't have any result nodes, then the
		  // only time we should be using the Expansion Var
		  // is if that is the only type of query there is.
		  // This is an aliasing feature in the ExpansionVariable 
		  // syntax.

		  String query = getQuery();
		  String idLookup = getIDLookup();

		  if ( ((query == null) || (query.length() == 0)) && ((idLookup == null) || (idLookup.length() == 0)) )
		  {
		  	use = true;
		  }
		  else
		  {
		  	use = false;
		  }
	   }
	   return use;
	}
	
	/**
	 *
	 * Determines whether or not the results of this variables expansions
	 * have resulted in a "true" or "false" result.
	 *
	 * @param curResult[in] The result we are checking to see if it is a boolean result
	 * @param result[out]   Either "true" or "false", depending on curResult or m_ResultNodes
	 *
	 * @return HRESULT
	 *
	 */
	protected String getBooleanResult(String curVal)
	{
		String result = "false";
		if (m_BoolResultCalculated)
		{
			result = getIsTrue() ? "true" : "false";
		}
		else
		{
			// We only want to calculate this result once.
			m_BoolResultCalculated = true;
			
            boolean testVarLength = false;
            String valueType = null;
            if (m_Node != null)
            {
                valueType = XMLManip.getAttributeValue(m_Node, "trueValueType");
                if ("length".equals(valueType))
                    testVarLength = true;
            }
            
            String expansionResults = curVal;
            
            String trueValue = getTrueValue();
            boolean continueTests = true;
            // tempHR will be S_OK if the trueValue xml attribute exists. S_FALSE
            // will be returned if it doesn't exist. This is important in order
            // to determine whether or not a "" is a true value or not
            if (((Element) m_Node).attribute("trueValue") != null)
            {
                continueTests = false;
                if (trueValue.equals(expansionResults))
                    result = "true";
            }
            else if ("false".equals(curVal))
            {
                // If the value coming in is set to "false" and the TrueValue property
                // is not set to false ( for the double negative ), we're done
                if (!testVarLength)
                    continueTests = false;
            }
            
            if (expansionResults != null && expansionResults.length() > 0
                    && continueTests)
            {
                result = "true";
                continueTests = false;
            }
            
            if (m_ResultNodes != null && continueTests)
            {    
                // Check to see if the 'trueValueType' attr is set to "length". If it is,
                // then we don't want to use m_ResultNodes in the equation
                if (!testVarLength)
                {
                    if (m_ResultNodes.size() > 0)
                        result = "true";
                }
            }
            
            setIsTrue("true".equals(result));
		}
		return result;
	}
    
    protected void validate()
    {
        if (m_Node == null)
        {
            throw new IllegalStateException("Not initialized");
        }
    }
    
    protected String expandTemplateWithNode(String fileName, Node context)
    {
        validate();
        
        String result = null;
        if (fileName != null && fileName.length() > 0)
        {
            IVariableExpander expander = getExecutionContext();
            if (expander != null)
            {
                ITemplateManager manager = expander.getManager();
                if (manager != null)
                {
                    result = manager.expandTemplateWithNode(fileName, context);
                    IVariableExpander expanderUsed = 
                            manager.getVariableExpander();
                    if (this instanceof ICompoundVariable)
                    {
                        ((ICompoundVariable) this).setExecutionContext(
                                                        expanderUsed);
                    }
                }
            }
        }
        return result;
    }
}
