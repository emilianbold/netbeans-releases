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


/*
 * File       : VBParameterStateHandler.java
 * Created on : Dec 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class VBParameterStateHandler extends ParameterStateHandler
{

    public VBParameterStateHandler(String language)
    {
        super(language);
    }

    public VBParameterStateHandler(String language, String direction)
    {
        super(language, direction, "Parameter");
    }

    public VBParameterStateHandler(
        String language,
        String direction,
        String stateName)
    {
        super(language, direction, stateName);
    }
    
    public void initialize()
    {
        super.initialize();
        setNodeAttribute("type", "Variant");
    }
    
    public void processToken(ITokenDescriptor pToken, String language)
    {
        if(pToken == null) return;
        
        super.processToken(pToken, language);
        
        String tokenType = pToken.getType();
        if("Modifier".equals(tokenType))
        {
            String value = pToken.getValue();
            if(value != null)
            {
                if(isModifierSame("Optional", language, value) == true)
                {
                    setNodeAttribute("isOptional", "true");
                }
                else if(isModifierSame("ByRef", language, value) == true)
                {
                    setNodeAttribute("isByRef", "true");
                }
                else if(isModifierSame("ByVal", language, value) == true)
                {
                    setNodeAttribute("isByVal", "true");
                }
                else if(isModifierSame("AddressOf", language, value) == true)
                {
                    setNodeAttribute("isAddressOf", "true");
                }
            }
        }
    }
    
    protected void handleName(ITokenDescriptor pToken) 
    {
        if(pToken == null) return;
        
        handleName(pToken);

        String value = pToken.getValue();

        updateByTypeSpecifier(value);
    }

    protected void updateByTypeSpecifier(String typeName) 
    {
        if(typeName != null)
        {
            String value = null;
            String lastChar = typeName.substring(typeName.length() - 1);
            if("%".equals(lastChar))
            {
                value = "Integer";
            }
            else if("!".equals(lastChar))
            {
                value = "Single";
            }
            else if("$".equals(lastChar))
            {
                value = "String";
            }
            else if("@".equals(lastChar))
            {
               value = "Decimal";
            }
            else if("#".equals(lastChar))
            {
               value = "Double";
            }
            else if("&".equals(lastChar))
            {
               value = "Long";
            }

            if(value != null)
            {
                setNodeAttribute("type", value);
            }
        }
    }



}
