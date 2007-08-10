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

import org.netbeans.modules.uml.common.generics.ETPairT;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface IMultiplicityRange extends IElement
{

    /**
     * prop value when collection type is set to "As Array"
     */
    public static  final String AS_ARRAY = "[]";

    /**
     * Gets the lower bound for this range.
     * @return The lower range
     */
    public String getLower();
    
    /**
     * Sets the lower bound for this range.
     * @param val The lower range
     */
    public void setLower(String val);
    
    /**
     * Gets the upper bound for this range. If upper is not specified
     * ( i.e., -1 ), then the range includes the lower bound and all integers 
     * greater than the lower bound.
     * @return The upper range value.
     */
    public String getUpper();
    
    /**
     * Sets the upper bound for this range. If upper is not specified
     * ( i.e., -1 ), then the range includes the lower bound and all integers 
     * greater than the lower bound.
     * @param val The upper range value.
     */
    public void setUpper(String val);
    
    /**
     * A convenience function used to get the upper and lower bounds in one call.
     * @return The lower and the upper range values.
     */
    public ETPairT<String, String> getRange();
    
    /**
     * A convenience function used to set the upper and lower bounds in one call.
     * @param lower the lower range
     * @param upper the upper range
     */
    public void setRange(String lower, String upper);
    
    /**
     * Returns the parent IMultiplicity object
     * @return the multiplicity object.
     */
    public IMultiplicity getParentMultiplicity();
    
    /**
     * Returns the string represenation of a multiplicity range. The 
     * representation will be formated like 
     * 
     * if there is no lower or upper value: *
     * if lower == null and upper != null : upper
     * if lower != null and upper == null : lower
     * if lower != null and upper != null : lower..upper
     * if lower.equals(upper)             : lower
     * 
     * @return the string presenetation.
     */
    public String getRangeAsString();
    
    /**
     * Sometimes a user will want the code to be generated as collection not
     * an array.  The collectionType property allows a collection type to be
     * specified.
     * 
     * @return The type of collection to use. If no collection type is specified
     *         "As Array" (or the localized form) wil be returned.
     */
    public String getCollectionType();
    
    /**
     * Sometimes a user will want the code to be generated as collection not
     * an array.  The collectionType property allows a collection type to be
     * specified.
     * 
     * @param useDefault If true default collection type (or as array) 
     *                   will be returned when no collection is 
     *                   specified.  If false, an empty string will be returned.
     * @return The type of collection to use. Localized for "synthetic" values
     *         like "As Array"
     */
    public String getCollectionType(boolean useDefault);
    
    /**
     * Sometimes a user will want the code to be generated as collection not
     * an array.  The collectionType property allows a collection type to be
     * specified.
     * 
     * @param useDefault If true default collection type (or array) 
     *                   will be returned when no collection is 
     *                   specified.  If false, an empty string will be returned.
     * @return The type of collection to use. Non-localized.
     */
    public String getCollectionTypeValue(boolean useDefault);
    
    /**
     * Sets the collection type to use when generating code.  An empty string 
     * means that an array should be used.
     * 
     * @param type The collection type.  
     */
    public void setCollectionType(String type);
}
