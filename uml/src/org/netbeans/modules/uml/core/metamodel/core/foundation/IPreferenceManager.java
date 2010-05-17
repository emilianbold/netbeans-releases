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
