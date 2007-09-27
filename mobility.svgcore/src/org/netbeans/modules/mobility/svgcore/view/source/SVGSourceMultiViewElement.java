/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */   

package org.netbeans.modules.mobility.svgcore.view.source;

import org.netbeans.modules.mobility.svgcore.palette.SVGPaletteFactory;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
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
    
    /** Creates a new instance of SVGXmlMultiViewElement */
    public SVGSourceMultiViewElement( SVGDataObject dObj) {
        super(dObj);
    }    
    
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

    public void componentHidden() {
        ((SVGDataObject) dObj).setMultiViewElement(null);
        super.componentHidden();
    }
    
    public void componentOpened() {
        super.componentOpened();
        ((SVGDataObject) dObj).getModel().attachToOpenedDocument();
    }

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
