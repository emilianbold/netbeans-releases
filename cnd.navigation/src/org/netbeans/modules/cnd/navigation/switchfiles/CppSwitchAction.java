/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
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

package org.netbeans.modules.cnd.navigation.switchfiles;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.JumpList;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.loaders.CCDataLoader;
import org.netbeans.modules.cnd.loaders.CCDataObject;
import org.netbeans.modules.cnd.loaders.CDataLoader;
import org.netbeans.modules.cnd.loaders.CDataObject;
import org.netbeans.modules.cnd.loaders.HDataLoader;
import org.netbeans.modules.cnd.loaders.HDataObject;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.ExtensionList;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;

public final class CppSwitchAction extends CookieAction {

    protected void performAction(Node[] activatedNodes) {
        // fast search
        FileObject res = findToggleFile(activatedNodes);
        if (res != null) {
            doToggle(res);
        } else {
            CsmFile target = getTarget(activatedNodes);
            if (target != null) {
                DataObject dob = CsmUtilities.getDataObject(target);
                doToggle(dob);
            } else {
                StatusDisplayer.getDefault().setStatusText(getMessage("cpp-switch-file-not-found")); // NOI18N
            }
        } 
    }

    private CsmFile getTarget(Node[] activatedNodes) {
        CsmFile f = CsmUtilities.getCsmFile(activatedNodes[0], false);
        CsmFile target = null;
        if (f != null) {
            NodeKind nk = getTargetNodeKind(activatedNodes);
            if (nk == NodeKind.SOURCE) {
                target = findSource(f);
            } else if (nk == NodeKind.HEADER) {
                target = findHeader(f);
            }
        }
        return target;
    }

    public String getName() {
        String trimmedNameKey = "goto-cpp-switch-file"; //NOI18N
        String fullNameKey = "CTL_CppSwitchAction"; //NOI18N
        switch (getTargetNodeKind(getActivatedNodes())) {
            case HEADER:
                trimmedNameKey = "goto-cpp-header-file"; //NOI18N
                break;
            case SOURCE:
                trimmedNameKey = "goto-cpp-source-file"; //NOI18N
                break;
        }
        String trimmedName = getMessage(trimmedNameKey);
        putValue(ExtKit.TRIMMED_TEXT, trimmedName);
        putValue(BaseAction.POPUP_MENU_TEXT, trimmedName);
        return getMessage(fullNameKey);
    }

    private enum NodeKind {

        HEADER, SOURCE, UNKNOWN
    }

    private static NodeKind getTargetNodeKind(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            if (activatedNodes[0].getLookup().lookup(HDataObject.class) != null) {
                return NodeKind.SOURCE;
            } else if (activatedNodes[0].getLookup().lookup(CCDataObject.class) != null || activatedNodes[0].getLookup().lookup(CDataObject.class) != null) {
                return NodeKind.HEADER;
            }
        }
        return NodeKind.UNKNOWN;
    }

    private CsmFile findHeader(CsmFile source) {
        String name = getName(source.getAbsolutePath());
        // first look at the list of includes
        for (CsmInclude h : source.getIncludes()) {
            if (name.equals(h.getIncludeFile().getAbsolutePath())) {
                return h.getIncludeFile();
            }
        }

        String path = trimExtension(source.getAbsolutePath());
        CsmFile namesake = null;
        for (CsmFile f : source.getProject().getHeaderFiles()) {
            if (getName(f.getAbsolutePath()).equals(name)) {
                if (path.equals(trimExtension(f.getAbsolutePath()))) {
                    // we got namesake in the same directory. Best hit.
                    // TODO: actually this is pretty common issue, should we
                    // make a special check for such files?
                    return f;
                }
                if (namesake == null) {
                    // we don't care which namesake to take if it's not in
                    // the same directory
                    namesake = f;
                }
            }
        }
        return namesake;
    }

    private CsmFile findSource(CsmFile header) {
        List<CsmFile> namesakes = new ArrayList<CsmFile>();
        String name = getName(header.getAbsolutePath());

        for (CsmFile f : header.getProject().getSourceFiles()) {
            if (getName(f.getAbsolutePath()).equals(name)) {
                for (CsmInclude h : f.getIncludes()) {
                    if (h.getIncludeFile() == header) {
                        // we found source file with the same name
                        // as header and with dependency to it. Best shot.
                        return f;
                    }
                }
                namesakes.add(f);
            }
        }

        if (namesakes.size() > 0) {
            // best namesake is one within same folder
            String path = trimExtension(header.getAbsolutePath());
            for (CsmFile f : namesakes) {
                if (path.equals(trimExtension(f.getAbsolutePath()))) {
                    return f;
                }
            }
            // no clear winner
            return namesakes.get(0);
        }

        return null;
    }

    private void doToggle(final DataObject toggled) {
        // check if the data object has possibility to be opened in editor
        final OpenCookie oc = toggled.getCookie(OpenCookie.class);
        if (oc != null) {
            // remember current caret position
            JTextComponent textComponent = EditorRegistry.lastFocusedComponent();
            JumpList.checkAddEntry(textComponent);
            // try to open ASAP, but better not in EQ
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    // open component
                    oc.open();
                }
            }, 0, Thread.MAX_PRIORITY);
        }
    }

    private void doToggle(FileObject fo) {
        assert (fo != null);
        try {
            // find a data object for the input file object
            DataObject toggled = DataObject.find(fo);
            if (toggled != null) {
                doToggle(toggled);
            }
        } catch (DataObjectNotFoundException ex) {
            // may be error message?
        }
    }

    private FileObject findToggleFile(final Node[] activatedNodes) {
        FileObject res = null;
        // check whether current file is C++ Source file
        DataObject dob = activatedNodes[0].getLookup().lookup(CCDataObject.class);
        if (dob == null) {
            // check whether current file is C Source file
            dob = activatedNodes[0].getLookup().lookup(CDataObject.class);
        }
        if (dob != null) {
            // it was Source file, find Header
            res = findBrother(dob, getSuffices(HDataLoader.getInstance().getExtensions()));
        } else {
            // check whether current file is Header file
            dob = activatedNodes[0].getLookup().lookup(HDataObject.class);
            if (dob != null) {
                // try to find C++ Source file
                res = findBrother(dob, getSuffices(CCDataLoader.getInstance().getExtensions()));
                if (res == null) {
                    // try to find C Source file
                    res = findBrother(dob, getSuffices(CDataLoader.getInstance().getExtensions()));
                }
            }
        }
        return res;
    }

    private FileObject findBrother(DataObject dob, String[] ext) {
        assert (dob != null);
        assert (dob.getPrimaryFile() != null);
        // get a file object associated with the data object
        FileObject fo = dob.getPrimaryFile();
        if (ext != null && ext.length > 0) {
            // try to find a file with the same name and one of passed extensions
            for (int i = 0; i < ext.length; i++) {
                // use FileUtilities to find brother of the file object
                FileObject res = FileUtil.findBrother(fo, ext[i]);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    private static String[] getSuffices(ExtensionList list) {
        List<String> suffixes = new ArrayList<String>();
        for (Enumeration e = list.extensions(); e != null && e.hasMoreElements();) {
            String ex = (String) e.nextElement();
            suffixes.add(ex);
        }
        return suffixes.toArray(new String[suffixes.size()]);
    }
    
//    @Override
//    public boolean isEnabled() {
//        boolean enabled = false;
//        if (super.isEnabled()) {
//            enabled = getTarget(getActivatedNodes()) != null;
//        }
//        return enabled;
//    }
    
    // Utility

    private static String getName(String path) {
        int idxSlash = path.lastIndexOf(File.separatorChar);
        String name = path.substring(idxSlash == -1 ? 0 : idxSlash + 1);
        int idxDot = name.lastIndexOf(".");
        return name.substring(0, idxDot == -1 ? name.length() : idxDot);
    }

    private static String trimExtension(String path) {
        // I hardly believe we can meet file w/o extension here but lets play safe
        int idxDot = path.lastIndexOf(".");
        return idxDot == -1 ? path : path.substring(0, idxDot);
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(CppSwitchAction.class, key);
    }
    
//    private static Collection<CsmProject> geProjectsScope(CsmFile file) {
//        Collection<CsmProject> projects = CsmModelAccessor.getModel().projects();
//        for (CsmProject prj : CsmModelAccessor.getModel().projects()) {
//            projects.addAll(prj.getLibraries());
//        }
//        return projects;
//    }
    
    // System

    protected Class[] cookieClasses() {
        return new Class[]{HDataObject.class, CDataObject.class, CCDataObject.class};
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected @Override boolean asynchronous() {
        return false;
    }
    
    public @Override String iconResource() {
        return "org/netbeans/modules/cnd/navigation/resources/header_source_icon.png"; //NOI18N
    }
    
}