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

import org.netbeans.modules.uml.core.support.umlutils.ETList;



public interface IPreferenceManager
{
    public boolean getIsAliasingOn();
    public void setIsAliasingOn(boolean newVal);
    public String getDefaultLanguage(String modeName);
    public void setDefaultLanguage(String modeName, String newVal);
    public String getDefaultModeName();
    public void setDefaultModeName(String newVal);
    public String getHomeLocation();

    /**
     * Retrieves the setting that indicates what should be done when an unknown 
     * classifier is referred to.
     * 
     * @return The name of the type to be created, or <code>null</code> if no
     *         type should be created.
     */
    public String unknownClassifier();

    public void installDefaultModelLibraries(IPackage pack, 
            ETList<IElement> libs);
            
    /**
     * Saves the internal preference information out to the given file.
     * 
     * @param fileName The name of the file to which the preference information
     *                 will be saved.
     */
    public void save(String fileName);
    
    /**
     * Gets the name used when creating new metatypes.
     * 
     * @return The default element name.
     */
    public String getDefaultElementName();
    public void setDefaultElementName(String newVal);
    // TODO:
//    public IDType getIDType();
//    public void setIDType(IDType newVal);
    public String getDefaultProjectName();
    public void setDefaultProjectName(String newVal);
    public void load(String prefFile);
    public ETList<String> retrieveDefaultModelLibraryNames();
    public String getDefaultRoundTripBehavior(String sLanguage, String sBehaviorType);
    public void setDefaultRoundTripBehavior(String sLanguage, String sBehaviorType, String sValue);
    // TODO:
//    public ITaggedValues getExpansionVariables();
    public String retrieveDefaultPreferenceLocation();
}