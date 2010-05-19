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
package org.netbeans.modules.edm.editor.graph.jgo;

import org.netbeans.modules.edm.editor.graph.jgo.IGraphFieldNode;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphNode;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorField;

import com.nwoods.jgo.JGoText;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class OperatorGraphFieldNode extends BasicCellArea.Highlightable implements IGraphFieldNode {

    private String name;

    /**
     * Creates a new instance of OperatorGraphFieldNode with the given type and operator
     * field information, using default text alignment.
     * 
     * @param type field type
     * @param field contains field information
     */
    public OperatorGraphFieldNode(int type, IOperatorField field) {
        this(type, field, JGoText.ALIGN_LEFT);
    }

    public OperatorGraphFieldNode(int type, String text) {
        super(type, text);
        
        this.drawBoundingRect(true);
    }

    /**
     * Creates a new instance of OperatorGraphFieldNode with the given type and operator
     * field information, using the given text alignment.
     * 
     * @param type field type
     * @param field contains field information
     * @param textAlignment desired text alignment, one of JGoText.ALIGN_LEFT,
     *        JGoText.ALIGN_RIGHT, or JGoText.ALIGN_CENTER
     */
    public OperatorGraphFieldNode(int type, IOperatorField field, int textAlignment) {
        this(type, field.getDisplayName());

        this.name = field.getName();
        this.setToolTipText(field.getToolTip());

        this.setTextEditable(field.isEditable());
        this.setTextAlignment(textAlignment);
    }

    /**
     * Gets the data object stored in this field node
     * 
     * @return data object stored in the field node
     */
    public Object getDataObject() {
        return null;
    }

    /**
     * Sets the data object in this field node
     * 
     * @param obj data object
     */
    public void setObject(Object obj) {

    }

    /**
     * Gets the graph node. Normally this is parent which contain this field node.
     * 
     * @return graph node
     */
    public IGraphNode getGraphNode() {
        return null;
    }

    /**
     * Gets name of this field
     * 
     * @return field name
     */
    public String getName() {
        return this.name;
    }

    /**
     * sets name of this field
     * 
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }
}

