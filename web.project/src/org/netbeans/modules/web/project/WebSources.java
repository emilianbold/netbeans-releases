/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project;

import org.netbeans.spi.project.SourceGroup;
import org.netbeans.modules.web.api.webmodule.SourcesGroupTypes;
import org.netbeans.spi.project.Sources;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.modules.web.project.ui.WebCustomizerProvider;
/** WebSources.java
 *
 * @author  mk115033
 */
public class WebSources implements org.netbeans.spi.project.Sources {
   
    private AntProjectHelper helper;
    
    /** Creates a new instance of WebSources */
    public WebSources(AntProjectHelper helper) {
        this.helper=helper;
    }
    
    public org.netbeans.spi.project.SourceGroup[] getSourceGroups(String str) {
        if (SourcesGroupTypes.TYPE_DOC_ROOT.equals(str)) {
            FileObject fo = helper.resolveFileObject(helper.evaluate(WebProjectProperties.WEB_DOCBASE_DIR));
            return new SourceGroup[] {new WebSources.WebSourceGroup(fo,
                org.openide.util.NbBundle.getMessage(WebCustomizerProvider.class, "LBL_Node_DocBase"))};
        }
        else if (SourcesGroupTypes.TYPE_WEB_INF.equals(str)) {
            FileObject fo = helper.resolveFileObject(helper.evaluate(WebProjectProperties.WEB_DOCBASE_DIR));
            fo = fo.getFileObject(ProjectWebModule.FOLDER_WEB_INF);
            return new SourceGroup[] {new WebSources.WebSourceGroup(fo,fo.getName())};
        }
        else if (Sources.TYPE_GENERIC.equals(str)) {
            FileObject fo = helper.resolveFileObject(helper.evaluate(WebProjectProperties.SOURCE_ROOT));
            return new SourceGroup[] {new WebSources.WebSourceGroup(fo, fo.getName())};
        }
        else if (Sources.TYPE_JAVA.equals(str)) {
            FileObject fo = helper.resolveFileObject(helper.evaluate(WebProjectProperties.SRC_DIR));
            return new SourceGroup[] {new WebSources.WebSourceGroup(fo,
                org.openide.util.NbBundle.getMessage(WebCustomizerProvider.class, "LBL_Node_Sources"))};
        }
        else return null;
    }
    
    private static class WebSourceGroup implements org.netbeans.spi.project.SourceGroup {
        private FileObject folder;
        private String displayName;
        
        WebSourceGroup(FileObject folder, String displayName) {
            this.folder=folder;
            this.displayName=displayName;
        }
        public boolean contains(org.openide.filesystems.FileObject fileObject) throws java.lang.IllegalArgumentException {
            if (fileObject.isFolder()) return true;
            return false;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public org.openide.filesystems.FileObject getRootFolder() {
            return folder;
        }
        
    }
}
