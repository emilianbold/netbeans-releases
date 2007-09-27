/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.iep.model.lib;

import java.io.Serializable;

/**
 * Interface TcgPropertyType specifies metadata for TcgProperty
 *
 * @author Bing Lu
 *
 * @since May 1, 2002
 */
public interface TcgPropertyType extends Serializable {
    /**
     * Gets the name attribute of the TcgPropertyType object
     *
     * @return The name value
     */
    public String getName();

    /**
     * Gets the title attribute of the TcgPropertyType object
     *
     * @return The title value
     */
    public String getTitle();

    /**
     * Gets the description attribute of the TcgPropertyType object
     *
     * @return The description value
     */
    public String getDescription();

    /**
     * Gets the defaultValue attribute of the TcgPropertyType object
     *
     * @return The defaultValue value
     */
    public Object getDefaultValue();

    public String getDefaultValueAsString();
    
    /**
     * Gets the editorName attribute of the TcgPropertyType object
     *
     * @return The editorName value
     */
    public String getEditorName();

    /**
     * Gets the multiple attribute of the TcgPropertyType object
     *
     * @return The multiple value
     */
    public boolean isMultiple();


    /**
     * Gets the readable attribute of the TcgPropertyType object
     *
     * @return The readable value
     */
    public boolean isReadable();

    /**
     * Gets the writable attribute of the TcgPropertyType object
     *
     * @return The writable value
     */
    public boolean isWritable();

    /**
     * Gets the mappableL attribute of the TcgPropertyType object
     *
     * @return The mappable value
     */
    public boolean isMappableL();

    /**
     * Gets the mappableR attribute of the TcgPropertyType object
     *
     * @return The mappable value
     */
    public boolean isMappableR();    
    
    /**
     * Gets the executable attribute of the TcgPropertyType object
     *
     * @return The executable value
     */
    public boolean isExecutable();

    /**
     * Gets the access attribute of the TcgPropertyType object
     *
     * @return The access value
     */
    public String getAccess();

    /**
     * Gets the access attribute of the TcgPropertyType object
     *
     * @return The access value
     */
    public boolean hasAccess(String access); 
    
    /**
     * Gets the rendererName attribute of the TcgPropertyType object
     *
     * @return The rendererName value
     */
    public String getRendererName();

    /**
     * Gets the required attribute of the TcgPropertyType object
     *
     * @return The required value
     */
    public boolean isRequired();

    /**
     * Gets the type attribute of the TcgPropertyType object
     *
     * @return The type value
     */
    public TcgType getType();

    /**
     * Creates an instance of TcgProperty using this TcgPropertyType
     *
     * @param parentTcgComponent the containing TcgComponent of the new property
     *
     * @return an instance of TcgProperty with this TcgPropertyType as its
     *         attribute
     */
    public TcgProperty newTcgProperty(TcgComponent parentTcgComponent);
    
    public String getScript();

    public String getCategory();
    
    public boolean isTransient();
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
