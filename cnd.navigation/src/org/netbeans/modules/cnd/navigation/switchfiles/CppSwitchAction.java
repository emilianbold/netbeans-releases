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

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.JumpList;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
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
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

public final class CppSwitchAction extends BaseAction {

    private static final String actionName = "cpp-switch-header-source"; // NOI18N
    private static final String ICON = "org/netbeans/modules/cnd/navigation/resources/header_source_icon.png"; // NOI18N
    private static CppSwitchAction instance;

    public static synchronized CppSwitchAction getInstance() {
        if (instance == null) {
            instance = new CppSwitchAction();
        }
        return instance;
    }

    public CppSwitchAction() {
        super(actionName);
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        putValue(BaseAction.ICON_RESOURCE_PROPERTY, ICON);
    }

    public void actionPerformed(ActionEvent evt, JTextComponent txt) {
        final Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();

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

    public @Override String getPopupMenuText(JTextComponent target) {
        String trimmedNameKey = "goto-cpp-switch-file"; //NOI18N
        switch (getTargetNodeKind(TopComponent.getRegistry().getActivatedNodes())) {
            case HEADER:
                trimmedNameKey = "goto-cpp-header-file"; //NOI18N
                break;
            case SOURCE:
                trimmedNameKey = "goto-cpp-source-file"; //NOI18N
                break;
        }
        return getMessage(trimmedNameKey);
    }

    protected @Override Object getDefaultShortDescription() {
        return getMessage("goto-cpp-switch-file"); //NOI18N
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
            if (h.getIncludeFile() != null && name.equals(h.getIncludeFile().getAbsolutePath())) {
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
        String name = getName(header.getAbsolutePath());

        Collection<CsmFile> includers = CsmIncludeHierarchyResolver.getDefault().getFiles(header);

        for (CsmFile f : includers) {
            if (getName(f.getAbsolutePath()).equals(name)) {
                // we found source file with the same name
                // as header and with dependency to it. Best shot.
                return f;
            }
        }

        // look for random namesake
        for (CsmFile f : header.getProject().getSourceFiles()) {
            if (getName(f.getAbsolutePath()).equals(name)) {
                return f;
            }
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

    // Utility
    
    private static String getName(String path) {
        int idxSlash = path.lastIndexOf(File.separatorChar);
        String name = path.substring(idxSlash == -1 ? 0 : idxSlash + 1);
        int idxDot = name.lastIndexOf('.');
        return name.substring(0, idxDot == -1 ? name.length() : idxDot);
    }

    private static String trimExtension(String path) {
        // I hardly believe we can meet file w/o extension here but lets play safe
        int idxDot = path.lastIndexOf('.');
        return idxDot == -1 ? path : path.substring(0, idxDot);
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(CppSwitchAction.class, key);
    }
}