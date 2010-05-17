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

package org.netbeans.modules.xml.xpath;

import org.netbeans.modules.xml.xpath.function.core.visitor.XPathCoreFunctionVisitor;

/**
 * Represents a core XPath function.
 * 
 * @author Enrico Lelina
 * @version 
 */
public interface XPathCoreFunction extends XPathOperationOrFuntion {
            
    /** Function code: last */
    public static final int FUNC_LAST = 1;
    
    /** Function code: position */
    public static final int FUNC_POSITION = 2;
    
    /** Function code: count */
    public static final int FUNC_COUNT = 3;
    
    /** Function code: id */
    public static final int FUNC_ID = 4;
    
    /** Function code: local-name */
    public static final int FUNC_LOCAL_NAME = 5;
    
    /** Function code: namespace-uri */
    public static final int FUNC_NAMESPACE_URI = 6;
    
    /** Function code: name */
    public static final int FUNC_NAME = 7;
    
    /** Function code: string */
    public static final int FUNC_STRING = 8;
    
    /** Function code: concat */
    public static final int FUNC_CONCAT = 9;
    
    /** Function code: starts-with */
    public static final int FUNC_STARTS_WITH = 10;
    
    /** Function code: contains */
    public static final int FUNC_CONTAINS = 11;
    
    /** Function code: substring-before */
    public static final int FUNC_SUBSTRING_BEFORE = 12;
    
    /** Function code: substring-after */
    public static final int FUNC_SUBSTRING_AFTER = 13;
    
    /** Function code: substring */
    public static final int FUNC_SUBSTRING = 14;
    
    /** Function code: string-length */
    public static final int FUNC_STRING_LENGTH = 15;
    
    /** Function code: normalize-space */
    public static final int FUNC_NORMALIZE_SPACE = 16;
    
    /** Function code: translate */
    public static final int FUNC_TRANSLATE = 17;
    
    /** Function code: boolean */
    public static final int FUNC_BOOLEAN = 18;
    
    /** Function code: not */
    public static final int FUNC_NOT = 19;
    
    /** Function code: true */
    public static final int FUNC_TRUE = 20;
    
    /** Function code: false */
    public static final int FUNC_FALSE = 21;
    
    /** Function code: lang */
    public static final int FUNC_LANG = 22;
    
    /** Function code: number */
    public static final int FUNC_NUMBER = 23;
    
    /** Function code: sum */
    public static final int FUNC_SUM = 24;
    
    /** Function code: floor */
    public static final int FUNC_FLOOR = 25;
    
    /** Function code: ceiling */
    public static final int FUNC_CEILING = 26;
    
    /** Function code: round */
    public static final int FUNC_ROUND = 27;
    
    /** Function code: null */
    public static final int FUNC_NULL = 28;
    
    /** Function code: key */
    public static final int FUNC_KEY = 29;
    
    /** Function code: format-number */
    public static final int FUNC_FORMAT_NUMBER = 30;
    
    /** Function code: exists */
    public static final int FUNC_EXISTS = 31;
    
    
    /**
     * Gets the function code.
     * @return the function code
     */
    int getFunction();
    
    
    /**
     * Sets the function code.
     * @param function the function code
     */
    void setFunction(int function);
    
    
    void accept(XPathCoreFunctionVisitor visitor);
}
