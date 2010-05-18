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

/*
 * ModelEventMediator.java
 *
 * Created on September 15, 2006, 8:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.axi.AXIComponent;

/**
 *
 * @author girix
 */
public abstract class ModelEventMediator implements PropertyChangeListener{
    
    ABEBaseDropPanel uiPeer;
    AXIComponent modelPeer;
    /** Creates a new instance of ModelEventMediator */
    protected ModelEventMediator(ABEBaseDropPanel uiPeer, AXIComponent modelPeer) {
        this.uiPeer = uiPeer;
        this.modelPeer = modelPeer;
        
        uiPeer.addPropertyChangeListener(ABEBaseDropPanel.PROP_COMPONENT_REMOVED,
                new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(ABEBaseDropPanel.PROP_COMPONENT_REMOVED))
                    cleanUp();
            }
        });
    }
    
    public abstract void _propertyChange(PropertyChangeEvent pce);
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(hasPathToTC()){
            //if the UI peer has the top frame then only despatch the event
            _propertyChange(evt);
        }else{
            //Else remove myself
            cleanUp();
        }
    }
    
    protected boolean hasPathToTC() {
        if(uiPeer != null)
            return (SwingUtilities.getAncestorOfClass(InstanceDesignerPanel.class, uiPeer) != null);
        else{
            modelPeer = null;
            return false;
        }
    }
    
    protected void cleanUp() {
        if(uiPeer != null &&
           uiPeer.getContext() != null &&
           !uiPeer.getContext().isShutdown() &&
           uiPeer instanceof ElementsContainerPanel)
            return;
        
        if(modelPeer != null) {
            if(modelPeer.getModel() != null)
                modelPeer.removePropertyChangeListener(this);
        }
        modelPeer = null;
        uiPeer = null;
    }
    
}
