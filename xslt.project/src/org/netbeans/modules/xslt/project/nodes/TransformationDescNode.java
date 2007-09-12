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
package org.netbeans.modules.xslt.project.nodes;

import java.awt.Image;
import javax.xml.namespace.QName;
import org.netbeans.modules.xslt.project.XsltproConstants;
import org.netbeans.modules.xslt.tmap.model.xsltmap.TransformationDesc;
import org.netbeans.modules.xslt.tmap.model.xsltmap.TransformationDescType;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TransformationDescNode extends AbstractNode {
    private TransformationDesc tDesc;
    private static String INPUT_ICON = "org/netbeans/modules/xslt/project/resources/inputTransformationDesc.png"; // NOI18N
    private static String OUTPUT_ICON = "org/netbeans/modules/xslt/project/resources/outputTransformationDesc.png"; // NOI18N
    private static final String DOT = "."; // NOI18N
    private static final String EMPTY_STRING = "";// NOI18N
            
    public TransformationDescNode(TransformationDesc tDesc) {
        super(Children.LEAF);
        this.tDesc = tDesc;
        // set nodeDescription property which is shown in property sheet help region
        setValue("nodeDescription", "");
    }

    public TransformationDescNode(Lookup lookup) {
        super(Children.LEAF, lookup);
    }

    public String getDisplayName() {
        return getName();
    }
    
    public String getName() {
        String roleName = tDesc.getRoleName();
        String fileName = tDesc.getFile();
        fileName = fileName == null ? " [empty file name] " : fileName;
        roleName = roleName == null ? "" : roleName;
        
        QName portTypeQname = QName.valueOf(tDesc.getPortType());
        String portType = portTypeQname == null 
                ? null 
                : portTypeQname.getLocalPart();
        
        portType = portType == null ? EMPTY_STRING : portType;
        
        String operation = tDesc.getOperation();
        operation = operation == null ? EMPTY_STRING : DOT+operation;
        
        return portType+operation;
    }
    
    public String getHtmlDisplayName() {
        String name = getName();
        QName messageTypeQname = QName.valueOf(tDesc.getMessageType());
        String messageType = messageTypeQname == null 
                ? null 
                : messageTypeQname.getLocalPart();
        

        messageType = messageType == null ? null : XsltproConstants.WHITESPACE+
                NbBundle.getMessage(
                    TransformationDescNode.class, 
                    "LBL_TRANSFORM_DESC_MESSAGE", // NOI18N
                    messageType);
        
        return Util.getGrayString(name, messageType);
    }

    public String getShortDescription() {
        String roleName = tDesc.getRoleName();
        String fileName = tDesc.getFile();
        fileName = fileName == null ? " [empty file name] " : fileName;
        roleName = roleName == null ? "" : roleName;
        return "<html> <b>role</b> = "+roleName + "<br> <b>file</b> ="+ fileName+"</html>";
        
    }

    public Image getIcon(int type) {
        // TODO m
        return Utilities.loadImage(TransformationDescType.OUTPUT.equals(tDesc.getType()) ?
                OUTPUT_ICON 
                : INPUT_ICON);
    }

    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    public boolean canCopy() {
        return false;
    }

    public boolean canCut() {
        return false;
    }

    public boolean canDestroy() {
        return false;
    }

    public boolean canRename() {
        return false;
    }
    
}
