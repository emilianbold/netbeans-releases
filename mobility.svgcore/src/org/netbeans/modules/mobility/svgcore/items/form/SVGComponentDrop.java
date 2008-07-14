/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mobility.svgcore.items.form;

import java.awt.Point;
import java.util.logging.Level;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.openide.text.ActiveEditorDrop;

/**
 *
 * @author avk
 */
public abstract class SVGComponentDrop implements  ActiveEditorDrop{

    protected static final String PATTERN = "%%";//NOI18N
    private static final String X_COORDINATE_PATTERN = PATTERN + "COORDINATE_X" + PATTERN;//NOI18N
    private static final String Y_COORDINATE_PATTERN = PATTERN + "COORDINATE_Y" + PATTERN;//NOI18N
    
    protected abstract boolean doTransfer();
    
    public static SVGComponentDrop getDefault(String snippet){
        return new Default(snippet);
    }
    
    public boolean handleTransfer(SVGDataObject svgDataObject, float[] point) {
        if (svgDataObject == null){
            SceneManager.log(Level.INFO, "SVGDataObject not found."); //NOI18N
            return false;
        }
        mySvgDataObject = svgDataObject;
        myPoint = point;
        return doTransfer();
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        return false;
    }
    
    protected void setSelection(String id){
        if (getSVGDataObject() != null){
            getSVGDataObject().getSceneManager().setSelection(id, true);
        }
    }
    
    protected SVGDataObject getSVGDataObject(){
        return mySvgDataObject;
    }
    
    protected String replaceCoordinates(String text){
        return text.replace(X_COORDINATE_PATTERN, String.valueOf(myPoint[0]))
                .replace(Y_COORDINATE_PATTERN, String.valueOf(myPoint[1]));
    }

    private static class Default extends SVGComponentDrop{

        public Default(String snippet) {
            mySnippet = snippet;
        }
        
        @Override
        protected boolean doTransfer() {
            try {
                if (mySnippet != null) {
                    String text = replaceCoordinates(mySnippet);
                    String id = getSVGDataObject().getModel().mergeImage(text, false);
                    setSelection(id);
                }
                return true;
            } catch (Exception ex) {
                SceneManager.error("Error during image merge", ex); //NOI18N
            }
            return false;
        }

        private String mySnippet;
    }
    
    private SVGDataObject mySvgDataObject;
    private float[] myPoint;
}
