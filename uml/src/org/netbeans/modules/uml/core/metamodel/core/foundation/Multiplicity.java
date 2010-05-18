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


package org.netbeans.modules.uml.core.metamodel.core.foundation;


//import org.apache.xpath.XPathAPI;
import org.dom4j.Document;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class Multiplicity extends Element implements IMultiplicity, ITransitionElement
{
    private ITransitionElement m_TransitionElement = null;
    
    /**
     *
     */
    public Multiplicity()
    {
        super();
        m_TransitionElement = new TransitionElement();
    }
    
    /**
     * Sets the lower bounds of this multiplicity, so it becomes [LowerBounds..*].  If ranges
     * currently exist they are deleted and a new one is created.
     *
     * @param sLowerBounds[in]
     *
     * @result S_OK
     */
    public void setRange(String bounds)
    {
        // Remove all the ranges.
        removeAllRanges();
        
        if (bounds == null || bounds.length() == 0)
        {
            // No ranges, set to *
            IMultiplicityRange newRange = createRange();
            if (newRange != null)
            {
                newRange.setLower("*");
                newRange.setUpper("");
            }
        }
        else
        {
            // Now create the new range
            IMultiplicityRange newRange = createRange();
            if (newRange != null)
            {
                newRange.setLower(bounds);
                newRange.setUpper("");
                addRange(newRange);
            }
        }
    }
    
    /**
     * Sets the bounds of this multiplicity, so it becomes [LowerBounds..UpperBounds].
     * If ranges currently exist they are deleted and a new one is created.
     *
     * @param sLowerBounds[in]
     * @param sUpperBounds[in]
     *
     * @result S_OK
     */
    public void setRange2(String lower, String upper)
    {
        // Remove all the ranges.
        removeAllRanges();
        
        if (lower.length() == 0 || lower.equals("*"))
        {
            // No range, set to *
            IMultiplicityRange newRange = createRange();
            if (newRange != null)
            {
                newRange.setLower("*");
                newRange.setUpper("");
            }
        }
        else
        {
            // Now create the new range
            IMultiplicityRange newRange = createRange();
            if (newRange != null)
            {
                newRange.setLower(lower);
                if (!lower.equals("*"))
                {
                    newRange.setUpper(upper);
                }
                addRange(newRange);
            }
        }
    }
    
    /**
     * Get the range(s) of this multiplicity accoring to a well formed string
     * [LowerBounds..Upperbounds, LowerBounds2..Upperbounds2,...].
     *
     * @param sRangeString[out] A well formed multiplicity string
     * @param bIncludeBrackets[in] TRUE to include the brackets [...]
     *
     * @result S_OK
     */
    public String getRangeAsString(boolean includeBrackets)
    {
        String retStr = null;
        ETList<IMultiplicityRange> ranges = getRanges();
        if (ranges != null)
        {
            String lower = "";
            String upper = "";
            String value = "";
            int count = ranges.size();
            for (int i=0; i<count; i++)
            {
                IMultiplicityRange range = ranges.get(i);
                lower = range.getLower();
                upper = range.getUpper();
                
                if (value.length() > 0)
                {
                    value += "," ;
                }
                
                if (lower.length() == 0)
                {
                    if(upper.length() == 0)
                    {
                        value += "*";
                    }
                    else
                    {
                        value += upper;
                    }
                }
                else if (upper.length() == 0)
                {
                    value += lower;
                }
                else if (!lower.equals(upper))
                {
                    value += lower;
                    value += "..";
                    if (upper.length() > 0)
                    {
                        value += upper;
                    }
                }
                else
                {
                    // We have 1..1, show it as just 1
                    value += lower;
                }
            }
            if (value.length() > 0)
            {
                if (includeBrackets)
                {
                    retStr = "[" + value + "]";
                }
                else
                {
                    retStr = value;
                }
            }
        }
        return (retStr != null) ? retStr : "";
    }
    
    /**
     * Adds a MultiplicityRange to this element.
     *
     * @param range[out]
     *
     * @result HRESULT
     */
    public void addRange(IMultiplicityRange range)
    {
        boolean proceed = true;
        IMultiplicityListener listener = retrieveListener(this);
        if (listener != null)
        {
            proceed = listener.onPreRangeAdded(this, range);
        }
        if (proceed)
        {
            addChild("UML:Multiplicity.range", "UML:Multiplicity.range", range);
            if (listener != null)
            {
                listener.onRangeAdded(this, range);
            }
        }
        else
        {
            //cancel the events
        }
    }
    
    private IMultiplicityListener retrieveListener(IMultiplicity mult)
    {
        IMultiplicityListener mList = null;
        IElement owner = getOwner();
        if (owner instanceof IMultiplicityListener)
        {
            mList = (IMultiplicityListener)owner;
        }
        return mList;
    }
    
    /**
     *
     * Inserts a new range object into this multiplicities collection of ranges.
     *
     * @param existingRange[in]   A current member of the range collection. If 0, newRange is appended
     *                            to the end of the list.
     * @param newRange[in]        The range to add.
     *
     * @return HRESULT
     *
     */
    public void insertRange(IMultiplicityRange existingRange, IMultiplicityRange newRange)
    {
        try
        {
            org.dom4j.Node node = XMLManip.selectSingleNode(m_Node, "UML:Multiplicity.range");
            if (node != null && node.getNodeType() == org.dom4j.Node.ELEMENT_NODE)
            {
                org.dom4j.Element owner = (org.dom4j.Element)node;
                insertNode(owner, existingRange, newRange);
            }
        }
        catch (Exception e)
        {
        }
    }
    
    /**
     * Removes a range from this element.
     *
     * @param range[out]
     *
     * @result S_OK
     */
    public void removeRange(IMultiplicityRange range)
    {
        boolean proceed = true;
        IMultiplicityListener listener = retrieveListener(this);
        if (listener != null)
        {
            proceed = listener.onPreRangeRemoved(this, range);
        }
        if (proceed)
        {
            UMLXMLManip.removeChild(m_Node, range);
            if (listener != null)
            {
                listener.onRangeRemoved(this, range);
            }
        }
        else
        {
            //cancel the events
        }
    }
    
    /**
     * Removes all the ranges from the element.
     *
     * @result S_OK
     */
    public void removeAllRanges()
    {
        ETList<IMultiplicityRange> ranges = getRanges();
        if (ranges != null && ranges.size() > 0)
        {
            int count = ranges.size();
            for (int i=count-1; i>=0; i--)
            {
                IMultiplicityRange range = ranges.get(i);
                removeRange(range);
            }
        }
    }
    
    /**
     * Retrieves the set of MultiplicityRanges for this element.
     *
     * @param ranges[out]
     *
     * @result HRESULT
     */
    public ETList<IMultiplicityRange> getRanges()
    {
        IMultiplicityRange dummy = null;
        return retrieveElementCollection(dummy, "UML:Multiplicity.range/*", IMultiplicityRange.class);
    }
    
    /**
     * Gets the ordered flag.  For a multiplicity that permits multiple
     * values, this attribute specifies whether the values are sequentially
     * ordered.
     *
     * @param pVal[out]
     *
     * @result HRESULT
     */
    public boolean getIsOrdered()
    {
        return getBooleanAttributeValue( "isOrdered", true );
    }
    
    /**
     * Sets the ordered flag.  For a multiplicity that permits multiple
     * values, this attribute specifies whether the values are sequentially
     * ordered.
     *
     * @param newVal[in]
     *
     * @result HRESULT
     */
    public void setIsOrdered(boolean val)
    {
        boolean proceed = true;
        IMultiplicity mult = null;
        IMultiplicityListener listener = retrieveListener(mult);
        if (listener != null)
        {
            proceed = listener.onPreOrderModified(mult, val);
        }
        if (proceed)
        {
            setBooleanAttributeValue("isOrdered", val);
            if (listener != null)
            {
                listener.onOrderModified(mult);
            }
        }
        else
        {
            //cancel the events
        }
    }
    
    /**
     * Description.
     *
     * @param pVal[out]
     *
     * @result HRESULT
     */
    public long getRangeCount()
    {
        return UMLXMLManip.queryCount(m_Node, "UML:Multiplicity.range/*", false);
    }
    
    /**
     *
     * Creates a new MultiplicityRange. The range is NOT automatically added to this
     * Multiplicity.
     *
     * @param pRange[out] The created range
     *
     * @return HRESULT
     *
     */
    public IMultiplicityRange createRange()
    {
        TypedFactoryRetriever < IMultiplicityRange > ret = new TypedFactoryRetriever < IMultiplicityRange >();
        return ret.createType("MultiplicityRange");
    }
    
    /**
     * Sets the range(s) of this multiplicity accoring to a well formed string
     * [LowerBounds..Upperbounds, LowerBounds2..Upperbounds2,...].If ranges currently
     * exist they are deleted and a new ones are created.
     *
     * @param sWellFormedRangeString
     *
     * @result S_OK
     */
    public void setRangeThroughString(String wellFormedStr)
    {
        removeAllRanges();
        
        if (wellFormedStr.length() > 0)
        {
            String formatStr = wellFormedStr;
            int firstBr = formatStr.indexOf("[");
            int lastBr = formatStr.lastIndexOf("]");
            String multiplicities = "";
            
            if (firstBr >= 0 && lastBr >= 0)
            {
                // Extract out the parameter list inside the parens
                multiplicities = formatStr.substring(firstBr+1, lastBr);
            }
            else
            {
                multiplicities = formatStr;
            }
            
            if (multiplicities.length() > 0)
            {
                // Multiplicity ranges can look like [ 1..2, 4..5 ]
                String[] strs = multiplicities.split(",");
                
                // Create the multiplicities
                if (strs != null && strs.length>0)
                {
                    for (int i=0; i<strs.length; i++)
                    {
                        // Now split on . to find the first and last word.  That's the first and last part of the range
                        String str = strs[i];
                        addMultiplicity(str);
                    }
                }
            }
        }
    }
    
    /**
     * Used by the edit control to get the range(s) of this multiplicity according to a
     * well formed string [LowerBounds..Upperbounds, LowerBounds2..Upperbounds2,...].
     *
     * @param sRangeString[out] A well formed multiplicity string
     */
    public String getRangeAsString()
    {
        return getRangeAsString(false);
    }
    
    /**
     *
     * Retrieves the owning element of this Multiplicity
     *
     * @param owner[out] The owner
     *
     * @return HRESULT
     *
     */
    public IElement getOwner()
    {
        IElement owner = null;
        
        // Need to retrieve the node that owns the Multiplicity. You have
        // to go get the grandparent node for this
        Node parent = m_Node.getParent();
        if (parent != null)
        {
            Node grandparent = parent.getParent();
            if (grandparent != null)
            {
                FactoryRetriever fact = FactoryRetriever.instance();
                String name = retrieveSimpleName(grandparent);
                Object obj = fact.createTypeAndFill(name, grandparent);
                if (obj != null && obj instanceof IElement)
                {
                    owner = (IElement)obj;
                }
            }
        }
        return owner;
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
    public void establishNodePresence( Document doc, Node parent )
    {
        buildNodePresence( "UML:Multiplicity", doc, parent );
    }
    
    /**
     * Given 1..2 we set the multiplicity range at index 'index' into the pMultiplicityRanges list.
     */
    protected void addMultiplicity(String multStr)
    {
        ETList<IMultiplicityRange> ranges = getRanges();
        boolean bFoundEllipses = false;
        int pos = multStr.indexOf("..");
        if (pos >= 0)
        {
            bFoundEllipses = true;
        }
        
        if (multStr.length() >0 && ranges != null)
        {
            // Now create the new range
            String searchStr = "..";
            String replaceStr = " ";
            String modifiedMultStr = multStr;
            
            // Replace the .. with spaces
            modifiedMultStr = StringUtilities.replaceSubString(modifiedMultStr, searchStr, replaceStr);
            
            // Replace the brackets in case we got some.
            searchStr = "]";
            modifiedMultStr = StringUtilities.replaceSubString(modifiedMultStr, searchStr, replaceStr);
            
            //find the upper and lower ranges
            String upperBound = "";
            String lowerBound = "";
            if (bFoundEllipses)
            {
                String[] strs = modifiedMultStr.split(" ");
                if (strs != null && strs.length >= 2)
                {
                    lowerBound = strs[0];
                    upperBound = strs[1];
                }
            }
            else
            {
                lowerBound = upperBound = modifiedMultStr.trim();
            }
            
            IMultiplicityRange newRange = createRange();
            if (newRange != null)
            {
                if (lowerBound.length() == 0 || lowerBound.equals("*"))
                {
                    // Range is *
                    newRange.setLower("*");
                    newRange.setUpper("");
                }
                else
                {
                    newRange.setLower(lowerBound);
                    if (upperBound.length() == 0)
                    {
                        // We have a range like "1".  set the upper range to ""
                        newRange.setUpper("");
                    }
                    else if (upperBound.equals("*"))
                    {
                        // Range is sLowerText..*.
                        newRange.setUpper("*");
                    }
                    else
                    {
                        newRange.setUpper(upperBound);
                    }
                }
                addRange(newRange);
            }
        }
    }
    
    public IVersionableElement performDuplication()
    {
        IVersionableElement dup = super.performDuplication();
        if (dup != null)
        {
            IMultiplicity mult = (IMultiplicity)dup;
            ETList<IMultiplicityRange> ranges = mult.getRanges();
            if (ranges != null)
            {
                int count = ranges.size();
                for (int i=0; i<count; i++)
                {
                    IMultiplicityRange range = ranges.get(i);
                    IVersionableElement ver = range.duplicate();
                    if (ver != null && ver instanceof IMultiplicityRange)
                    {
                        IMultiplicityRange dupRange = (IMultiplicityRange)ver;
                        replaceIds(mult, dupRange);
                    }
                }
            }
            replaceIds(mult, mult);
        }
        return dup;
    }
    
    /**
     * Override from TransitionElementImpl.  Eventually MultiplicityImpl should
     * not derive from TransitionElementImpl, rather we need to create an
     * IMultiplicityTransitionElement
     */
    public IElement getFutureOwner()
    {
        IElement retEle = m_TransitionElement.getFutureOwner();
        if (retEle == null)
        {
            // We have probably already been connected, so get our current owner
            retEle = getOwner();
        }
        return retEle;
    }
    
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.core.foundation.ITransitionElement#setFutureOwner(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
         */
    public void setFutureOwner(IElement value)
    {
        m_TransitionElement.setFutureOwner(value);
    }
}


