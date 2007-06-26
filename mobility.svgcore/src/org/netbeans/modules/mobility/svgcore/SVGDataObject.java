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
package org.netbeans.modules.mobility.svgcore;

import java.awt.Image;
import java.io.IOException;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.netbeans.modules.mobility.svgcore.view.svg.SVGViewMultiViewElement;
import org.netbeans.modules.mobility.svgcore.view.source.SVGSourceMultiViewElement;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Pavel Benes
 */
@SuppressWarnings({"unchecked"})
public class SVGDataObject extends XmlMultiViewDataObject {
    public static final int XML_VIEW_INDEX      = 0;
    public static final int SVG_VIEW_INDEX      = 1;
   
    public static final String PROP_SVG_VIEW_CHANGED = "svg_view_changed";
    
    private static final Image SVGFILE_ICON = org.openide.util.Utilities.loadImage ("org/netbeans/modules/mobility/svgcore/resources/svg.png"); // NOI18N        

    private transient final SVGFileModel model;
   
    private static class VisualView extends DesignMultiViewDesc {
        private static final long serialVersionUID = 7526471457562776148L;        

        VisualView(SVGDataObject dObj) {
           super(dObj, NbBundle.getMessage(SVGDataObject.class, "LBL_MULVIEW_TITLE_VIEW"));
        }
        
        public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
            SVGDataObject dObj = (SVGDataObject)getDataObject();
            return new SVGViewMultiViewElement(dObj, SVG_VIEW_INDEX);
        }
        
        public java.awt.Image getIcon() {
            return SVGFILE_ICON;         
        }
        
        public String preferredID() {
            return "multiview_svgview";
        }

        public int getPersistenceType() {
            //return TopComponent.PERSISTENCE_ONLY_OPENED;
            return TopComponent.PERSISTENCE_NEVER;
        }        
    }

    private static class XMLTextView extends DesignMultiViewDesc { 
        private static final long serialVersionUID = 7526471457562776147L;        
        
        XMLTextView(SVGDataObject dObj) {
            super( dObj, NbBundle.getMessage(SVGDataObject.class, "LBL_MULVIEW_TITLE_SOURCE"));
        }
        
        public MultiViewElement createElement() {
            return new SVGSourceMultiViewElement( (SVGDataObject) getDataObject(), XML_VIEW_INDEX);
        }
        
        public java.awt.Image getIcon() {
            return ((SVGDataObject) getDataObject()).getXmlViewIcon();
        }
        
        public String preferredID() {
            return "multiview_xml"; //NOI18N
        }
        
        public int getPersistenceType() {
            //return TopComponent.PERSISTENCE_ONLY_OPENED;
            return TopComponent.PERSISTENCE_NEVER;

        }        
    }
   
    public SVGDataObject(FileObject pf, SVGDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        //System.out.println("> SVGDataObject()");
        org.xml.sax.InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
        XmlMultiViewEditorSupport edSup = getEditorSupport();
        edSup.setSuppressXmlView(true);
        setLastOpenView( SVG_VIEW_INDEX);        
        //call the method getMultiViewDescriptions() to 
        //recalculate the xmlMultiViewIndex member after default
        //XML view has been suppressed.
        edSup.getMultiViewDescriptions();
        model = new SVGFileModel(edSup);
        //System.out.println("< SVGDataObject()");
    }
    
    public TopComponent getMTVC() {
        return getEditorSupport().getMVTC();
    }
    
    public SVGFileModel getModel() {
        return model;
    }    
  
    protected DesignMultiViewDesc[] getMultiViewDesc() {
        return new DesignMultiViewDesc[]{ 
            new XMLTextView(this),
            new VisualView(this) 
        };
    }
        
    protected Node createNodeDelegate() {
        return new SVGDataNode(this);
    }
        
    protected String getPrefixMark() {
        return null;
    }
}
