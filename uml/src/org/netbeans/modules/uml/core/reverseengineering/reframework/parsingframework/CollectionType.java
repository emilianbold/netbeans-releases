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

package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

/**
 *
 * @author treyspiva
 */
public class CollectionType
{
    private String name = "";
    private String packageName = "";
    private boolean userDefined = false;
    private boolean defaultType = false;
    
    /** Creates a new instance of CollectionTypes */
    public CollectionType()
    {
    }
    
    /** Creates a new instance of CollectionTypes 
     * @param name The name of the type
     * @param packName  The package of the type
     * @param user is the type a user defined type
     * @param defaultType is the type the default collection type
     */
    public CollectionType(String name, String packName,
                           boolean user, boolean defaultType)
    {
        setDefaultType(defaultType);
        setUserDefined(user);
        setPackageName(packName);
        setName(name);
    }
    
    
    /**
     * Set the default type property.  The default collection type is the 
     * default type to use when generating code for an attribute with 0..* 
     * multiplicity
     * 
     * @param defaultType true if the type is the default type
     */
    public void setDefaultType(boolean defaultType)
    {
        this.defaultType = defaultType;
    }
    
    /**
     * Retrieves if the default type property.  The default collection type is the 
     * default type to use when generating code for an attribute with 0..* 
     * multiplicity
     * 
     * @return true if the type is the default type
     */
    public boolean isDefaultType()
    {
        return defaultType;
    }
    
    /**
     * Sets if the collection type is a user defined type or a type defined by 
     * the language.
     * 
     * @param userDefined true if the type is a user type.
     */
    public void setUserDefined(boolean userDefined)
    {
        this.userDefined = userDefined;
    }
    
    /**
     * Retrieves if the collection type is a user defined type or a type defined by 
     * the language.
     * 
     * @return true if the type is a user type.
     */
    public boolean isUserDefined()
    {
        return userDefined;
    }
    
    /**
     * Sets the package that owns the collection type.
     * 
     * @param packageName the owning package name.
     */
    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }
    
    /**
     * Retrieves the package that owns the collection type.
     * 
     * @return the owning package name.
     */
    public String getPackageName()
    {
        return packageName;
    }
    
    /**
     * Sets the name of the collection type.
     * 
     * @param name the type name
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * Retrieves the name of the collection type.
     * 
     * @return the name of the collection type.
     */
    public String getName()
    {
        return name;
    }
    
    public String getFullName()
    {
        return getPackageName().replace(".", "::") + "::" + getName();
    }
    
    /**
     * Test if two types are the same.  If a String is passed in it tries to 
     * determine if the package and name equals the string value.
     * 
     * @param obj The object to test.
     * @return true if the objects are equal, false otherwise.
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        
        if (getClass() == obj.getClass())
        {
            final CollectionType other = (CollectionType) obj;

            if (this.name != other.name &&
                    (this.name == null || !this.name.equals(other.name)))
            {
                return false;
            }
            
            if (this.packageName != other.packageName &&
                    (this.packageName == null ||
                    !this.packageName.equals(other.packageName)))
            {
                return false;
            }
            
            return true;
        }
        else if(obj instanceof String)
        {
            // Check if we are checking if the name is the same.  There are two
            // possibilities.  First we may have just the name of the class.
            // Second, we could have a fully qualified name.
            
            String wantedName = (String)obj;
            int umlSeperator = wantedName.lastIndexOf(":");
            int javaSeperator = wantedName.lastIndexOf(".");
            
            String collectionName = "";
            if(umlSeperator >= 0)
            {
                // First make sure that the name is in the correct format.
                collectionName = getPackageName().replaceAll(".", "::");
                if(collectionName.charAt(collectionName.length() - 1) != ':')
                {
                    collectionName += "::";
                }
                collectionName += getName();
            }
            else if(javaSeperator >= 0)
            {
                // First make sure that the name is in the correct format.
                collectionName = getPackageName().replaceAll("::", ".");
                if(collectionName.charAt(collectionName.length() - 1) != '.')
                {
                    collectionName += ".";
                }
                collectionName += getName();
            }
            else
            {
                collectionName = getName();
            }
            
            return wantedName.equals(collectionName);
        }
        
        return false;
    }
    
    /**
     * Calculates a hashcode
     * 
     * @return the hashcode value.
     */
    public int hashCode()
    {
        int hash = 5;
        
        hash = 29 * hash + (this.name != null ? this.name.hashCode()
                : 0);
        hash = 29 * hash +
                (this.packageName != null ? this.packageName.hashCode()
                : 0);
        hash = 29 * hash + (this.userDefined ? 1
                : 0);
        hash = 29 * hash + (this.defaultType ? 1
                : 0);
        return hash;
    }
}
