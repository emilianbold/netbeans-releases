/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xsl;

import org.xml.sax.InputSource;
import javax.xml.transform.Source;

import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;

import org.netbeans.spi.xml.cookies.*;

import org.netbeans.modules.xml.core.XMLDataObjectLook;
import org.netbeans.modules.xml.core.text.TextEditorSupport;
import org.netbeans.modules.xml.core.sync.*;
import org.netbeans.modules.xml.core.cookies.*;
import org.netbeans.modules.xsl.cookies.ValidateXSLSupport;

/**
 * XSL owner.
 *
 * @author Libor Kramolis
 * @author asgeir@dimonsoftware.com
 */
public final class XSLDataObject extends MultiDataObject implements XMLDataObjectLook {
    /** Serial Version UID */
    private static final long serialVersionUID = -3523066651187749549L;

    /** XSLT Mime Type. */
    public static final String MIME_TYPE = "application/xslt+xml"; // NOI18N
    
    private static final String XSL_ICON_BASE =
        "org/netbeans/modules/xsl/resources/xslObject"; // NOI18N
    
    private transient final DataObjectCookieManager cookieManager;

    private transient Synchronizator synchronizator;
    
    
    //
    // init
    //

    public XSLDataObject(final FileObject obj, final UniFileLoader loader) throws DataObjectExistsException {
        super (obj, loader);

        CookieSet set = getCookieSet();
        cookieManager = new DataObjectCookieManager (this, set);
        set.add (cookieManager);
    
        // add check and validate cookies
        InputSource is = DataObjectAdapters.inputSource (this);
        set.add(new CheckXMLSupport (is));
        set.add(new ValidateXSLSupport (is));

        // add TransformableCookie
        Source source = DataObjectAdapters.source (this);
        set.add (new TransformableSupport (source));

        // add Scenario support
        set.add (new ScenarioSupport (this));    

        // editor support defines MIME type understood by EditorKits registry         
        TextEditorSupport.TextEditorSupportFactory editorFactory =
            new TextEditorSupport.TextEditorSupportFactory (this, org.netbeans.modules.xml.core.XMLDataObject.MIME_TYPE);
        editorFactory.registerCookies (set);
		
    }


    /**
     */
    protected Node createNodeDelegate () {
        return new XSLDataNode (this);
    }

    
    /**
     */
    public HelpCtx getHelpCtx() {
        //return new HelpCtx (XSLDataObject.class);
        return HelpCtx.DEFAULT_HELP;
    }
    
    // XMLDataObjectLook to be deprecated ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public DataObjectCookieManager getCookieManager() {
        return cookieManager;
    }

    public synchronized Synchronizator getSyncInterface() {
        if (synchronizator == null) {
            synchronizator = new DataObjectSyncSupport (XSLDataObject.this);
        }
        return synchronizator;
    }
    
    /**
     * Redefine icon and help.
     */
    private static class XSLDataNode extends DataNode {

        /** Create new XSLDataNode. */
        public XSLDataNode (XSLDataObject obj) {
            super (obj, Children.LEAF);
            setIconBase (XSL_ICON_BASE);
            setShortDescription(Util.THIS.getString("PROP_XSLDataNode_desc"));
        }

        /**
         */
        public HelpCtx getHelpCtx() {
            //return new HelpCtx (XSLDataObject.class);
            return HelpCtx.DEFAULT_HELP;
        }
        
    }

}
