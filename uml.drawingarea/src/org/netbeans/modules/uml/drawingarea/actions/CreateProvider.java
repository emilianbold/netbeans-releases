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
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Point;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.spi.palette.PaletteController;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jyothi
 */
public class CreateProvider  implements SelectProvider {
    
    PaletteController paletteController;
    TopComponent uiTopComponent ;
    private Lookup.Template template = new Lookup.Template(PaletteController.class);
    private Lookup.Result<PaletteController> result;
    
    public CreateProvider() {
        super();
    }
    
    private TopComponent getTopComponent() {
        TopComponent tc = WindowManager.getDefault().findTopComponent("UMLDiagramTopComponent");
        return tc;
    }
    
    public boolean isAimingAllowed(Widget widget, Point localLocation, boolean invertSelection) {
        return false;
    }
    
    public boolean isSelectionAllowed(Widget widget, Point localLocation, boolean invertSelection) {
        return true;
    }
    
    public void select(Widget widget, Point localLocation, boolean invertSelection) {
        System.err.println(" !! CreateProvider: select ");
//        uiTopComponent = getTopComponent();
//        if (uiTopComponent != null) {
//            result = uiTopComponent.getLookup().lookup(template);
//            Collection c = result.allInstances();
//            System.err.println(" !!!! new lookup contains PaletteController "+ c.size());
//            if (c.size() > 0) {
//                paletteController = (PaletteController) c.iterator().next();
//            }
//        }
//        else
//            System.err.println(" uiTopComponent is null..");
//        DesignView scene = (DesignView) widget.getScene();
//        
//        boolean addNew=false;
//        
//        Lookup selItem=paletteController.getSelectedItem();
//        if(null != selItem) {
//            System.err.println(" selItem =  "+selItem);
//            ActiveEditorDrop selNode=(ActiveEditorDrop)selItem.lookup(ActiveEditorDrop.class);
//            if(selNode instanceof ClassPaletteItem) {
//                addNew=true;
//            }
//        }
//        
//        if(addNew)//some proper item in palette selected
//        {
//            System.err.println(" .... Drop a widget here... ");
//        }
//        
//        count++;
//        scene.addNode(String.valueOf(count)).setPreferredLocation(widget.convertLocalToScene(localLocation));
    }
    
    static int count = 0;
}
