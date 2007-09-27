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
import java.util.List;
import javax.swing.ImageIcon;

/**
 * Interface TcgComponentType serves as metadata for TcgComponent. For example,
 * Tag_Font, Tag_Text, etc...
 *
 * @author Bing Lu
 *
 * @see TcgComponent
 * @see TcgComponentImpl
 * @see TcgComponentTypeImpl
 * @since April 30, 2002
 */
public interface TcgComponentType extends Serializable {
    /**
     * Gets the allowsChildren attribute of the TcgComponentType object
     *
     * @return The allowsChildren value
     */
    public boolean getAllowsChildren();

    /**
     * Gets the codeType attribute of the TcgComponentType object
     *
     * @param codeTypeName Description of the Parameter
     *
     * @return The codeType value
     */
    public TcgCodeType getCodeType(String codeTypeName);

    /**
     * Gets the number of TcgCodeType's contained in this object
     *
     * @return the number of TcgCodeType's contained in this object
     */
    public int getCodeTypeCount();

    /**
     * Gets the codeTypeList attribute of the TcgComponentType object
     *
     * @return The codeTypeList value
     */
    public java.util.List getCodeTypeList();

    /**
     * Gets the named TcgComponentType from this object
     *
     * @param componentTypeName the name of the TcgComponentType
     *
     * @return The named TcgComponentType from this object
     */
    public TcgComponentType getComponentType(String componentTypeName);

    /**
     * Gets the number of TcgComponentType's directly contained in this object
     *
     * @return the number of TcgComponentType's directly contained in this object
     */
    public int getComponentTypeCount();

    /**
     * Gets the list of all TcgComponentType of this object
     *
     * @return the TcgComponentType list of the object
     */
    public List getComponentTypeList();
  
    public ImageIcon getIcon();

    /**
     * Gets the name attribute of the TcgComponentType object
     *
     * @return The name value
     */
    public String getName();

    /**
     * Gets the path attribute of the TcgComponentType object
     *
     * @return The path value
     */
    public String getPath();

    /**
     * Gets the propertyType from this TcgComponentType object given the
     * property type name
     *
     * @param propertyTypeName Description of the Parameter
     *
     * @return The propertyType value
     */
    public TcgPropertyType getPropertyType(String propertyTypeName);

    /**
     * @param propertyTypeName Description of the Parameter
     *
     * @return true if this TcgComponentType object has a property type with 
     * the given name
     */
    public boolean hasPropertyType(String propertyTypeName);

    /**
     * Gets the number of TcgPropertyType's contained in this object
     *
     * @return the number of TcgPropertyType's contained in this object
     */
    public int getPropertyTypeCount();

    /**
     * Gets the list of TcgPropertyType associated with this TcgComponentType
     * object
     *
     * @return The propertyTypeList value
     */
    public java.util.List getPropertyTypeList();

    /**
     * Gets the title attribute of the TcgComponentType object
     *
     * @return The title value
     */
    public String getTitle();

    /**
     * Gets the description attribute of the TcgComponent object
     *
     * @return The description value
     */
    public String getDescription();

    /**
     * Gets the treeNode attribute of the TcgComponentType object
     *
     * @return The treeNode value
     */
    public ListMapTreeNode getTreeNode();

    /**
     * Gets the visible attribute of the TcgComponentType object
     *
     * @return The visible value
     */
    public boolean isVisible();

    /**
     * Duplicate the given TcgComponent instance.
     *
     * @param componentName String component name for the duplicate instance
     * @param original The TcgComponent to clone
     *
     * @return Newle duplicated TcgComponent object
     */
    public TcgComponent duplicate(String componentName, TcgComponent original);

    /**
     * Duplicate the TcgComponentType tree rooted at this TcgComponentType
     * instance, and allows change to name, title, path, iconName, and visibility
     *
     * @return DOCUMENT ME!
     */
    public TcgComponentType duplicate(String name, String path, String title, String description, 
        ImageIcon icon, boolean allowsChildren, boolean visible, 
        List xtraTcgCodeTypeList, List xtraPropTypeList, List xtraChildTypeList,
        TcgComponentValidator validator);

    /**
     * Duplicate this TcgComponentType instance.
     *
     * @return DOCUMENT ME!
     */
    public TcgComponentType duplicate();

    /**
     * Creates a TcgComponent instance with the given component name having
     * this TcgComponentType as its attribute
     *
     * @param componentName String component name
     * @param componentTitle Description of the Parameter
     *
     * @return Newly created TcgComponent object
     */
    public TcgComponent newTcgComponent(String componentName,
                                        String componentTitle);

    public TcgComponent newShallowComponent( String compName, String componentType);
    
    public TcgComponentValidator getValidator();
}
