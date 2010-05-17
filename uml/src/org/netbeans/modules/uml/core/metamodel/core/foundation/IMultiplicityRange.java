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
