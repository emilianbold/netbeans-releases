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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import java.util.prefs.Preferences;
import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author sumitabhk
 *
 */
public class MultiplicityRange extends Element implements IMultiplicityRange
{
    
    /**
     *
     */
    public MultiplicityRange()
    {
        super();
    }
    
    /**
     * Gets the lower bound for this range.
     *
     * @result HRESULT
     */
    public String getLower()
    {
        return getAttributeValue("lower");
    }
    
    /**
     * Sets the lower bound for this range.
     *
     * @param val[ini]
     */
    public void setLower(String val)
    {
        if (valueIsValid(val, true))
        {
            boolean proceed = true;
            IMultiplicity mult = this.getParentMultiplicity();
            IMultiplicityListener mList = retrieveListener(mult, this);
            
            if (mList != null)
            {
                proceed = mList.onPreLowerModified(mult, this, val);
            }
            
            if (proceed)
            {
                setAttributeValue("lower", val);
                if (mList != null)
                {
                    mList.onLowerModified(mult, this);
                }
            }
            else
            {
                //cancel the event
            }
        }
    }
    
    /**
     *
     * Checks to see if the cardinality being set is valid, vs. the other cardinality
     *
     * @param str[in]       The string to check
     * @param isLower[in]   true if str represents the lower cardinality, else false
     *                      if it represents the upper cardinality
     *
     * @return true if it is a valid value, else false
     *
     */
    private boolean valueIsValid(String val, boolean isLower)
    {
        boolean isValid = true;
        if (isNumber(val))
        {
            String checkValue = "";
            if (isLower)
            {
                checkValue = getUpper();
            }
            else
            {
                checkValue = getLower();
            }
            
            if (checkValue != null && isNumber(checkValue))
            {
                int value = Integer.valueOf(val).intValue();
                int checkVal = Integer.valueOf(checkValue).intValue();
                
                if (isLower)
                {
                    isValid = value <= checkVal;
                }
                else
                {
                    isValid = value >= checkVal;
                }
            }
        }
        return isValid;
    }
    
    /**
     *
     * Retrieves the necessary interface in order to fire the few methods on the
     * IMultiplicityListener interface
     *
     * @param mult[out] The IMultipliticy interface this range is nested in
     * @param range[out] The interface that wraps this object
     * @param listener[out] The listener interface, else 0
     *
     * @return HRESULT
     *
     */
    private IMultiplicityListener retrieveListener(IMultiplicity mult, MultiplicityRange range)
    {
        IMultiplicityListener listener = null;
        if (m_Node != null)
        {
            // Need to get this node's great great grandparent, as a multiplicity
            // range is always within a multiplicity. It's the grand parent of the
            // Multiplicity element that we need...
            Node parent = m_Node.getParent();
            if (parent != null)
            {
                Node grandParent = parent.getParent();
                if (grandParent != null)
                {
                    FactoryRetriever fact = FactoryRetriever.instance();
                    
                    // grandParent is the multiplicity element
                    Object multObj = fact.createTypeAndFill("Multiplicity", grandParent);
                    if (multObj != null && multObj instanceof IMultiplicity)
                    {
                        mult = (IMultiplicity)multObj;
                    }
                    
                    Node greatGParent = grandParent.getParent();
                    if (greatGParent != null)
                    {
                        Node actual = greatGParent.getParent();
                        if (actual != null)
                        {
                            String name = retrieveSimpleName(actual);
                            Object obj = fact.createTypeAndFill(name, actual);
                            
                            if (obj instanceof IMultiplicityListener)
                            {
                                listener = (IMultiplicityListener)obj;
                            }
                        }
                    }
                }
            }
        }
        return listener;
    }
    
    /**
     * Gets the upper bound for this range.  If upper is not specified (i.e.,
     * -1), then the range includes the lower bound and all integers greater
     * than the lower bound.
     *
     * @result The upper range value.
     */
    public String getUpper()
    {
        return getAttributeValue("upper");
    }
    
    /**
     * Sets the upper bound for this range.  If upper is not specified (i.e.,
     * -1), then the range includes the lower bound and all integers greater
     * than the lower bound.
     *
     * @param val the upper range value.
     */
    public void setUpper(String val)
    {
        if (valueIsValid(val, false))
        {
            boolean proceed = true;
            IMultiplicity mult = this.getParentMultiplicity();
            IMultiplicityListener mList = retrieveListener(mult, this);
            
            if (mList != null)
            {
                proceed = mList.onPreUpperModified(mult, this, val);
            }
            
            if (proceed)
            {
                setAttributeValue("upper", val);
                if (mList != null)
                {
                    mList.onUpperModified(mult, this);
                }
            }
            else
            {
                //cancel the event
            }
        }
    }
    
    /**
     * A convenience function used to get the upper and lower
     * bounds in one call.
     *
     * @param lower[out]
     * @param upper[out]
     *
     * @result S_OK
     */
    public ETPairT < String, String > getRange()
    {
        
        return new ETPairT < String, String > (getLower(), getUpper());
    }
    
    /**
     * A convenience function used to set the upper and lower bounds in one call.
     *
     * @param lower the lower value
     * @param upper the upper value
     *
     * @result S_OK
     */
    public void setRange(String lower, String upper)
    {
        setLower(lower);
        setUpper(upper);
    }
    
    /**
     * Returns the parent IMultiplicity object
     *
     * @result The multiplicity that owns this range.
     */
    public IMultiplicity getParentMultiplicity()
    {
        IMultiplicity pMult = null;
        if (m_Node != null)
        {
            // Need to get this node's great great grandparent, as a multiplicity
            // range is always within a multiplicity. It's the grand parent of the
            // Multiplicity element that we need...
            Node parent = m_Node.getParent();
            if (parent != null)
            {
                Node grandParent = parent.getParent();
                if (grandParent != null)
                {
                    FactoryRetriever fact = FactoryRetriever.instance();
                    
                    // grandParent is the multiplicity element
                    Object mult = fact.createTypeAndFill("Multiplicity", grandParent);
                    if (mult != null && mult instanceof IMultiplicity)
                    {
                        pMult = (IMultiplicity)mult;
                    }
                }
            }
        }
        return pMult;
    }
    
    /**
     *
     * Establishes the appropriate XML elements for this UML type.
     *
     * @param doc[in] The element's document
     * @param parent[in] The element's parent node
     *
     * @return HRESULT
     *
     */
    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence("UML:MultiplicityRange", doc, parent);
    }
    
    /**
     *
     * Determines whether or not the passed in string represents a number or not.
     *
     * @param str[in] The string to check
     *
     * @return HRESULT
     *
     */
    protected boolean isNumber(String str)
    {
        boolean isNum = false;
        if (str != null && str.length() > 0)
        {
            try
            {
                int intVal = Integer.parseInt(str);
                if (intVal >= 0)
                {
                    isNum = true;
                }
            }
            catch (Exception e)
            {
            }
        }
        return isNum;
    }
    
    public String getRangeAsString()
    {
        String retStr = null;
        String lower = getLower();
        String upper = getUpper();
        String value = ""; // NOI18N
        
        if (lower.length() == 0)
        {
            if (upper.length() == 0)
                value += "*"; // NOI18N
            
            else
                value += upper;
        }
        
        else if (upper.length() == 0)
            value += lower;
        
        else if (!lower.equals(upper))
        {
            value += lower;
            value += ".."; // NOI18N
            
            if (upper.length() > 0)
                value += upper;
        }
        
        else
            // We have 1..1, show it as just 1
            value += lower;
        
        if (value.length() > 0)
            retStr = value;
        
        return (retStr != null) ? retStr : ""; // NOI18N
    }
    
    /**
     * Sometimes a user will want the code to be generated as collection not
     * an array.  The collectionType property allows a collection type to be
     * specified.
     * 
     * @return The type of collection to use. 
     */
    public String getCollectionType()
    {
        return getCollectionType(false);
    }
    
    /**
     * Sometimes a user will want the code to be generated as collection not
     * an array.  The collectionType property allows a collection type to be
     * specified.
     *
     * @return The type of collection to use. Localized for "synthetic" values
     *         like "As Array"
     */
    public String getCollectionType(boolean useDefault)
    {
        String retVal = getCollectionTypeValue(useDefault);

        if(retVal != null && retVal.equals(AS_ARRAY)) 
	{
	    retVal = NbBundle.getMessage(MultiplicityRange.class, "LBL_AS_ARRAY");
	}
        return retVal;
    }

    public String getCollectionTypeValue(boolean useDefault)
    {
        String retVal = super.getAttributeValue("collectionType");
        
        Preferences prefs = NbPreferences.forModule (MultiplicityRange.class);
        boolean useCollection = prefs.getBoolean("UML_USE_GENERICS_DEFAULT", true);
        
        if((retVal == null) || (retVal.length() == 0))
        {
	    if(useDefault) {
		if(useCollection == true)
		{
		    String defaultCollection = prefs.get("UML_COLLECTION_OVERRIDE_DEFAULT",
							 "java.util.ArrayList");
		    retVal = defaultCollection.replace(".", "::");
		}
		else 
		{
		    retVal = AS_ARRAY;
		}
	    }
        } 
       return retVal;
    }
    
    /**
     * Sets the collection type to use when generating code.  An empty string 
     * means that an array should be used.
     * 
     * @param type The collection type.  
     */
    public void setCollectionType(String type)
    {
        String asArray = NbBundle.getMessage(MultiplicityRange.class, "LBL_AS_ARRAY");
        if(asArray.equals(type) == false)
        {
            super.setAttributeValue("collectionType", type);
        }
        else
        {
            super.setAttributeValue("collectionType", AS_ARRAY);
        }
        
        IMultiplicity mult = this.getParentMultiplicity();
        IMultiplicityListener mList = retrieveListener(mult, this);
        
        if(mList != null)
        {
            mList.onCollectionTypeModified(mult, this);
        }
    }
    
}
