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


package org.netbeans.modules.iep.editor.designer;

import com.nwoods.jgo.DomDoc;
import com.nwoods.jgo.DomElement;
import com.nwoods.jgo.DomNode;
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoText;

/**
 * SimpleNodeLabel is just a JGoText that treats a single click as a command
 * to start editing the text of the label.
 */
public class SimpleNodeLabel extends JGoText {
   /** Create an empty label for a SimpleNode.  Call initialize() before using. */
    public SimpleNodeLabel() {
        super();
    }
    
    /** Create a label containing the given text for a SimpleNode */
    public SimpleNodeLabel(String text, JGoArea parent) {
        super(text);
        initialize(text, parent);
    }
    
    public void initialize(String text, JGoArea parent) {
        setSelectable(false);
        setDraggable(false);
        setResizable(false);
        setVisible(true);
        setEditable(false);
        setEditOnSingleClick(false);
        setTransparent(true);
        setAlignment(JGoText.ALIGN_CENTER);
        setTopLeft(parent.getLeft(), parent.getTop());
        parent.addObjectAtTail(this);
        // Same font as org.netbeans.modules.iep.editor.tcg.palette.PaletteLabel
        setFaceName("Tahoma");
        setFontSize(10);
        setBold(false);
        setItalic(false);
    }
    
    public void SVGWriteObject(DomDoc svgDoc, DomElement jGoElementGroup) {
        // Add <SimpleNodeLabel> element
        if (svgDoc.JGoXMLOutputEnabled()) {
            svgDoc.createJGoClassElement("org.netbeans.modules.iep.editor.designer.SimpleNodeLabel", jGoElementGroup);
        }
        
        // Have superclass add to the JGoObject group
        super.SVGWriteObject(svgDoc, jGoElementGroup);
    }
    
    public DomNode SVGReadObject(DomDoc svgDoc, JGoDocument jGoDoc, 
        DomElement svgElement, DomElement jGoChildElement)
    {
        if (jGoChildElement != null) {
            // This is a <SimpleNodeLabel> element
            super.SVGReadObject(svgDoc, jGoDoc, svgElement, jGoChildElement.getNextSiblingElement());
        }
        return svgElement.getNextSibling();
    }
    

}
