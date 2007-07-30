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
