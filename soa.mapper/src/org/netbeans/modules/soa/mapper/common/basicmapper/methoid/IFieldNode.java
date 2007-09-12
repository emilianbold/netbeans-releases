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

package org.netbeans.modules.soa.mapper.common.basicmapper.methoid;

import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.IMapperNode;

/**
 * <p>
 *
 * Title: A Methoid Field Node </p> <p>
 *
 * Description: Generic interface describe a methoid field node.
 * FuncotidFieldNode is the base interface for all methoid field node to be
 * added to the IMethoidNode. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IFieldNode
     extends IMapperNode {
    /**
     * The property name of the field node name changed.
     */
    public static final String NAME_CHANGED = "FieldNode.Name";

    /**
     * The property name of the field node type changed.
     */
    public static final String TYPE_CHANGED = "FieldNode.Type";

    /**
     * The property name of the field node tooltip changed.
     */
    public static final String TOOLTIP_CHANGED = "FieldNode.ToolTip";

    public static final String STYLE_CHANGED = "FieldNode.Style";
    
    public static final String FIELD_OBJECT_CHANGED = "FieldNode.ObjectChanged";

    public static final String STYLE_TYPE_NORMAL  = "normal";
    public static final String STYLE_TYPE_LITERAL = "literal";
    
    
    /**
     * Return the name of this field.
     *
     * @return   the name of this field.
     */
    public String getName();

    /**
     * Set the name of this methoid field node.
     *
     * @param name  the name of this methoid field node.
     */
    public void setName(String name);
    
    /**
     * Return the type name of this field.
     *
     * @return   the type name of this field.
     */
    public String getTypeName();

    /**
     * Set the type of this methoid field node.
     *
     * @param type  the type of this methoid field node.
     */
    public void setTypeName(String type);

    /**
     * Return the popup tooltip text of this methoid field node.
     *
     * @return   the popup tooltip text of this methoid field node.
     */
    public String getToolTipText();

    /**
     * Set the tooptip text of this methoid field node.
     *
     * @param tooltip  the tooptip text of this methoid field node.
     */
    public void setToolTipText(String tooltip);

    /**
     * Return the field data in another object repersentation.
     *
     * @return   the field data in another object repersentation.
     */
    public Object getFieldObject();
    
    public void setFieldObject(Object fieldObj);

    /**
     * Return true if this field is an input field, false otherwise.
     *
     * @return   true if this field is an input field, false otherwise.
     */
    public boolean isInput();

    /**
     * Return true if this field is an output field, false otherwise.
     *
     * @return   true if this field is an output field, false otherwise.
     */
    public boolean isOutput();

    /**
     * Return the literal name of this methoid field node.
     * The literal name is the display name of the methoid
     * field node while the field node has an in-place literal.
     */
    public String getLiteralName();
    
    /**
     * Returns whether this field node has an in-place literal.
     * This represents an unlinked field node that is 
     * tied to literal expression. 
     */
    public boolean hasInPlaceLiteral();
    
    /**
     * Set the literal name of this methoid field node.
     */
    public void setLiteralName(String name);
        
    /**
     * Set the literal name of this methoid field node.
     * Override the innate literal updater with the specified one.
     */
    public void setLiteralName(String name, ILiteralUpdater literalUpdater);
}
