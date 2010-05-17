/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.palette.SVGPaletteItemDataObject;
import org.openide.text.ActiveEditorDrop;

/**
 *
 * @author akorostelev
 */
public abstract class SVGComponentDrop implements  ActiveEditorDrop{

    protected static final String PATTERN = "%%";//NOI18N
    private static final String X_COORDINATE_PATTERN = PATTERN + "COORDINATE_X" + PATTERN;//NOI18N
    private static final String Y_COORDINATE_PATTERN = PATTERN + "COORDINATE_Y" + PATTERN;//NOI18N

    /**
     * Should implement real adding component snippet into SVG specified as SVGDataObject
     * @param svgDataObject SVGDataObject to add component snippet to
     * @return
     */
    protected abstract boolean doTransfer(SVGDataObject svgDataObject);

    /**
     * Should implement real adding component snippet into SVG opened in
     * specified JTextComponent
     * @param target JTextComponent with svg to which component snippet should be added
     * @return
     */
    protected abstract boolean doTransfer(JTextComponent target);

    /**
     * Is used to create SVGComponentDrop instance for editor-palette-items
     * with snipped specificed in <body> tag. In this case Snippet string is
     * already known at the moment of SVGComponentDrop creation.
     * @param snippet String with snippet text
     * @return SVGComponentDrop instance
     */
    public static SVGComponentDrop getDefault(String snippet){
        return new Default(snippet);
    }
    
    protected static String loadSnippetString(Class clazz, String relatedSnippetPath) throws IOException{
        InputStream is = clazz.getResourceAsStream(relatedSnippetPath);
        assert is != null : relatedSnippetPath + " resource Input Stream is null";//NOI18N
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }

    protected static void insertToTextComponent( final String text, final JTextComponent target) {
        SVGPaletteItemDataObject.insertToTextComponent(text, target);
    }
    
    public boolean handleTransfer(SVGDataObject svgDataObject, float[] point) {
        if (svgDataObject == null){
            SceneManager.log(Level.INFO, "SVGDataObject not found."); //NOI18N
            return false;
        }
        mySvgDataObject = svgDataObject;
        myPoint = point;
        return doTransfer(svgDataObject);
    }

    public boolean handleTransfer(JTextComponent target) {
        //SVGDataObject svgDO = SVGDataObject.getActiveDataObject(target);
        myPoint = new float[]{0, 0};
        return doTransfer(target);
    }

    protected float[] getDropPoint(){
        return myPoint;
    }
    
    protected void setSelection(String id){
        if (mySvgDataObject != null){
            mySvgDataObject.getSceneManager().setSelection(id, true);
        }
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
        protected boolean doTransfer(SVGDataObject svgDataObject) {
            try {
                if (mySnippet != null) {
                    String text = replaceCoordinates(mySnippet);
                    String id = svgDataObject.getModel().mergeImage(text, false);
                    setSelection(id);
                }
                return true;
            } catch (Exception ex) {
                SceneManager.error("Error during image merge", ex); //NOI18N
            }
            return false;
        }

        @Override
        protected boolean doTransfer(JTextComponent target) {
            if (mySnippet != null) {
                String text = replaceCoordinates(mySnippet);
                insertToTextComponent(text, target);
            }
            return true;
        }

        private String mySnippet;

    }
    
    private SVGDataObject mySvgDataObject;
    private float[] myPoint;

}
