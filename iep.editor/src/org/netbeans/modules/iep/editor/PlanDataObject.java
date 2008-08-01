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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.editor;


import java.io.IOException;

import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.HelpCtx;
import org.xml.sax.InputSource;
import org.netbeans.modules.iep.editor.designer.cookies.IEPSaveCookie;
import org.netbeans.modules.iep.editor.designer.cookies.IEPValidateXMLCookie;
import org.netbeans.modules.iep.editor.designer.cookies.PlanReportCookie;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;

/**
 * Represents a Plan file.
 *
 * @author  Bing Lu
 */
public class PlanDataObject extends MultiDataObject {
    public PlanDataObject(FileObject fObj, MultiFileLoader loader)
        throws DataObjectExistsException {
        super(fObj, loader);
        
        CookieSet set = getCookieSet();

        editorSupport = new PlanEditorSupport(this);
        // editor support defines MIME type understood by EditorKits registry
        set.add(editorSupport);
        
//      Add check and validate cookies
        InputSource is = DataObjectAdapters.inputSource(this);
        set.add(new CheckXMLSupport(is));
        set.add(new PlanMultiviewSupport(this));
        //add validate action here
        set.add(new IEPValidateXMLCookie(this));
        
        set.add(new PlanReportCookie(this));
        
        SaveCookie saveCookie = set.getCookie(SaveCookie.class);
        
        // add support for viewing a prompt in the IDE
        // getCookieSet().add(new PlanOpenSupport(getPrimaryEntry()));
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you add context help, change to:
        // return new HelpCtx (MyDataObject.class);
    }

    protected Node createNodeDelegate() {
        return new PlanNode(this);
    }

    public void addCookie(Cookie cookie) {
        getCookieSet().add(cookie);
    }

    public void removeCookie(Cookie cookie) {
        getCookieSet().remove(cookie);
    }

    public PlanEditorSupport getPlanEditorSupport() {
        return editorSupport;
    }

    @Override
    public void setModified(boolean modified) {
        super.setModified(modified);
        if (modified) {
            getCookieSet().add(getSaveCookie());
        } else {
            getCookieSet().remove(getSaveCookie());
        }
    }

    private SaveCookie getSaveCookie() {
        if(mSaveCookie == null && getPlanEditorSupport() != null) {
            mSaveCookie = new IEPSaveCookie(getPlanEditorSupport());
        }
        
        return mSaveCookie;
    }

    private transient IEPSaveCookie mSaveCookie;
    
    private transient PlanEditorSupport editorSupport;

    public static final String IEP_ICON_BASE_WITH_EXT = "org/netbeans/modules/iep/editor/eventProcess.png";
 
    private static final long serialVersionUID = 6338889116068357651L;
}