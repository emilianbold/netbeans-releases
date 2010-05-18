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

package org.netbeans.modules.mobility.svgcore.view.source;

import org.netbeans.modules.mobility.svgcore.palette.SVGPaletteFactory;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.netbeans.modules.mobility.svgcore.view.svg.SelectionCookie;
import org.netbeans.modules.xml.multiview.XmlMultiViewElement;
import org.netbeans.spi.palette.PaletteController;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Pavel Benes
 */
public class SVGSourceMultiViewElement extends XmlMultiViewElement {
    private static final long serialVersionUID = 7525761714575627761L;        
    private static final Logger LOG = Logger.getLogger(SVGSourceMultiViewElement.class.getName());
    
    /** Creates a new instance of SVGXmlMultiViewElement */
    public SVGSourceMultiViewElement( SVGDataObject dObj) {
        super(dObj);
    }    
    
    @Override
    public Lookup getLookup() {
        try {
            PaletteController pc = SVGPaletteFactory.getPalette();

            return new ProxyLookup(new org.openide.util.Lookup[] {                
                dObj.getNodeDelegate().getLookup(),
                Lookups.singleton(pc),
                Lookups.singleton( new SelectionCookie() {
                    public void updateSelection(SVGDataObject doj, String id, int startOff, boolean doubleClick) {
                        selectElement(doj, startOff, doubleClick);
                    }
                })
            });
        } catch( IOException e) {
            SceneManager.error("Lookup creation failed", e); //NOI18N
            return super.getLookup();
        }
    }    

    @Override
    public void componentHidden() {
        ((SVGDataObject) dObj).setMultiViewElement(null);
        super.componentHidden();
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
        SVGFileModel svgModel = ((SVGDataObject) dObj).getModel();
        if (svgModel.getModel() != null){
            svgModel.attachToOpenedDocument();
        } else {
            LOG.log(Level.WARNING, "Can not attachToOpenedDocument. document model is not loaded.");
        }
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        dObj.setLastOpenView(SVGDataObject.XML_VIEW_INDEX);
        ((SVGDataObject) dObj).setMultiViewElement(this);
    }

    public static void selectElement( final SVGDataObject svgDoj, int startOffset, final boolean requestFocus) {
        if ( startOffset != -1) {
            selectPosition(svgDoj, startOffset, requestFocus);
        }
    }
    
    public static void selectPosition( final SVGDataObject svgDoj, final int position, final boolean requestFocus) {
        openFileInEditor(svgDoj);

        SwingUtilities.invokeLater( new Runnable() {
            @SuppressWarnings({"deprecation"})
            public void run() {
                EditorCookie ed = svgDoj.getCookie(EditorCookie.class);
                try {
                    if (ed != null) {
                        ed.openDocument();
                        JEditorPane [] opened = ed.getOpenedPanes();
                        if ( opened != null && opened.length > 0) {
                            final JEditorPane  pane = opened[0];
                            pane.setSelectionStart(position);
                            pane.setSelectionEnd(position);

                            if ( requestFocus) {
                                TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass( TopComponent.class, pane);
                                if (tc != null) {
                                    tc.requestActive();
                                    // the requestActive itself does not work
                                    tc.requestFocus();
                                }
                            }
                        }
                    }            
                } catch( Exception e) {
                    SceneManager.error("Select in editor failed.", e); //NOI18N
                }
            }            
        });        
    }    
        
    private static boolean openFileInEditor(SVGDataObject svgDoj) {
        EditCookie ck = svgDoj.getCookie(EditCookie.class);
        
        if (ck != null) {
            ck.edit();
            return true;
        }

        OpenCookie oc = svgDoj.getCookie(OpenCookie.class);
        if (oc != null) {
            oc.open();
            return true;
        }
        return false;
    }    
}
