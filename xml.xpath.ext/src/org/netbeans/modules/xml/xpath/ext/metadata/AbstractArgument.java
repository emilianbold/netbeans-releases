/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xpath.ext.metadata;

/**
 * Base interface for differen kind of arguments (parameters) of a function 
 * or an operation. 
 * Actual arguments sould not implement this interface directly. 
 * They have to implement an inherited interfaces. 
 * Now there are two such interfaces: ArgumentDescriptor and ArgumentGroup.
 * 
 * @author nk160297
 */
public interface AbstractArgument {

    /**
     * Returns a text which can be shown as a tooltip. Can return null.
     * @return
     */
    String getDescription();
    
    /**
     * Returns a flag, which indicates if the argument is mandatory or optional. 
     * 
     * ATTENTION! If the argument is mandatory and repeated simultaneously, 
     * then it means that at least one argument in the repeated set is mandatory.
     * It means the same as if two consecutive arguments are declared 
     *  - one mandatory and single
     *  - another optional and repeated
     * 
     * @return
     */
    boolean isMandatory();
    
    /**
     * Returns a flag which indicates if the argument is always single or 
     * can be repeated one by one several times. A set of repeated arguments 
     * have only one descriptor in a Metadata object. 
     * 
     * @see org.netbeans.modules.xml.xpath.ext.metadata.GeneralFunctionMetadata#getArgumentDescriptors
     * @return
     */
    boolean isRepeated();
    
    /**
     * Returns minimum count of repeated arguments with the same type. 
     * The value has to be = 0 if an argument is optional. 
     * 
     * @return
     */
    int getMinOccurs();

    /**
     * Returns maximum count of repeated arguments with the same type. 
     * The value has to be > 1 if an argument is repeated. 
     * 
     * @return
     */
    int getMaxOccurs();
    
}
