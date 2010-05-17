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
 * LayoutSupport.java
 * 
 * Created on 17.06.2007, 17:41:57
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.drawingarea.actions;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.drawingarea.UMLDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.openide.windows.TopComponent;

/**
 * It calls perfome method on action provider, ususally used to move children widgets after scene is validated
 * Adds itself to listen scene and removes itself too after action processed
 * @author sp153251
 */
public class AfterValidationExecutor  implements Scene.SceneListener{

    ActionProvider provider;
    private int counter;
    private int afterValidationNumber;
    private Scene scene;

    public AfterValidationExecutor(ActionProvider provider,Scene scene,int afterValidationNumber) {
        this.provider=provider;
        this.afterValidationNumber=afterValidationNumber;
        this.scene=scene;
        counter=0;
        scene.addSceneListener(this);
    }
    public AfterValidationExecutor(ActionProvider provider,Scene scene) {
        this(provider,scene,1);
    }

    public void sceneRepaint() {
       
    }

    public void sceneValidating() {
     
    }

    public void sceneValidated() {
        counter++;
       if(counter==afterValidationNumber)
       {
           try
           {
                provider.perfomeAction();               
           }
           finally
           {
                scene.removeSceneListener(this);
           }
       }
       else if(counter>afterValidationNumber)
       {
           scene.removeSceneListener(this);
       }
    }

}
