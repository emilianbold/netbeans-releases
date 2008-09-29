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
/*
 * EjbNode.java
 *
 * Created on April 30, 2004, 3:46 PM
 */

package org.netbeans.modules.visualweb.ejb.nodes;

import org.netbeans.modules.visualweb.ejb.actions.AddEjbGroupAction;
import org.netbeans.modules.visualweb.ejb.actions.ExportAllEjbDataSourcesAction;
import org.netbeans.modules.visualweb.ejb.actions.ImportEjbDataSourceAction;
import java.awt.Image;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * This is the parent node for the Enterprise Java Beans data source.
 * The only valid action on this node is to add a new ejb group 
 * which is handled by the AddEjbGroupAction.java
 *
 * @author cao
 */

public class EjbRootNode extends AbstractNode {
    
    public EjbRootNode() {
        super( new EjbRootNodeChildren() );
        
        // Set FeatureDescriptor stuff:
        setName( NbBundle.getMessage(EjbRootNode.class, "ENTERPRISE_JAVA_BEANS") );
        setDisplayName( NbBundle.getMessage(EjbRootNode.class, "ENTERPRISE_JAVA_BEANS") );
        setShortDescription( NbBundle.getMessage(EjbRootNode.class, "ENTERPRISE_JAVA_BEANS_SHORT_DESC") );
    }
    
    public Image getIcon(int type){
        return ImageUtilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/ejb_modul_project.png");
    }
    
    public Image getOpenedIcon(int type){
        return ImageUtilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/ejb_modul_project.png");
    }
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get( AddEjbGroupAction.class ),
            SystemAction.get( ImportEjbDataSourceAction.class ),
            SystemAction.get( ExportAllEjbDataSourcesAction.class )
        };
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(AddEjbGroupAction.class);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("projrave_ui_elements_server_nav_ejb_node");
    }
    
    protected EjbRootNodeChildren getEjbRootNodeChildren() {
        return (EjbRootNodeChildren)getChildren();
    }
}
