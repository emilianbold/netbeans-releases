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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.websphere6.dd.loaders.webbnd;


import org.netbeans.modules.j2ee.websphere6.dd.beans.EjbRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ResEnvRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ResRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.CommonRef;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebBnd;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.WSReferenceBindingsPanel;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.WSWebBndAttributesPanel;
import org.netbeans.modules.xml.multiview.ui.*;

/**
 *
 * 
 */
public class PanelFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    private WSWebBndDataObject dObj;
    ToolBarDesignEditor editor;
    
    /** Creates a new instance of ServletPanelFactory */
    PanelFactory(ToolBarDesignEditor editor, WSWebBndDataObject dObj) {
        this.dObj=dObj;
        this.editor=editor;
    }
    
    public SectionInnerPanel createInnerPanel(Object key) {
        SectionView pv=(SectionView)editor.getContentView();
        if (key instanceof WSWebBnd){            
            return new WSWebBndAttributesPanel(pv, dObj, (WSWebBnd)key);
        } else if (key instanceof CommonRef){
            return new WSReferenceBindingsPanel(pv,dObj,(CommonRef)key);
        } 
        return null;
    }
}
