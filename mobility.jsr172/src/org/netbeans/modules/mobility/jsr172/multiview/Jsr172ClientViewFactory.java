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

/*
 * ClientViewFactory.java
 *
 * Created on July 22, 2005, 2:54 PM
 *
 */
package org.netbeans.modules.mobility.jsr172.multiview;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;

/**
 *
 * @author Michal Skvor
 */
public class Jsr172ClientViewFactory implements InnerPanelFactory {
    
    static final String PROP_PANEL_SERVICES = "services"; //NOI18N
    static final String PROP_PANEL_CLIENT_GENERAL = "clientGeneralInfo"; //NOI18N
    static final String PROP_PANEL_CLIENT_OPTIONS = "clientOptions"; //NOI18N
    
    final private E2EDataObject dataObject;
    ToolBarDesignEditor editor;
    
    private JSR172ServicePanel servicesPanel;
    private ClientGeneralInfoPanel clientGeneralInfoPanel;
    private Jsr172ClientOptionsPanel clientOptionsPanel;
    
    /** Creates a new instance of ClientViewFactory */
    public Jsr172ClientViewFactory( ToolBarDesignEditor editor, E2EDataObject dataObject ) {
        this.editor = editor;
        this.dataObject = dataObject;
    }
    
    public SectionInnerPanel createInnerPanel( final Object key ) {
        final String keyName = (String)key;
        if( PROP_PANEL_SERVICES.equals( keyName )) {
            if( servicesPanel == null ) {
                servicesPanel = new JSR172ServicePanel((SectionView)editor.getContentView(),
                        dataObject, dataObject.getConfiguration());
            }
            return servicesPanel;
        } else if( PROP_PANEL_CLIENT_GENERAL.equals( keyName )) {
            if( clientGeneralInfoPanel == null )
                clientGeneralInfoPanel = new ClientGeneralInfoPanel((SectionView)editor.getContentView(), dataObject );
            return clientGeneralInfoPanel;
        } else if( PROP_PANEL_CLIENT_OPTIONS.equals( keyName )) {
            if( clientOptionsPanel == null )
                clientOptionsPanel = new Jsr172ClientOptionsPanel((SectionView)editor.getContentView(), dataObject );
            return clientOptionsPanel;
        }
        return null;
    }
}
