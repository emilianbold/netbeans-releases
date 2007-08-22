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

package org.netbeans.modules.visualweb.webui.jsf.defaulttheme;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.project.jsf.services.RefreshService;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;
import org.netbeans.api.project.Project;
import org.openide.util.Utilities;

/**
 * ThemesFolderNode displays the available themes and badges the current theme node
 *
 * @author Po-Ting Wu, Mark Dey, Winston Prakash
 */
final class ThemesFolderNode extends AbstractNode {
    static final String WoodStock_ThemeVersion = "4.0"; // NOI18N
    static final RequestProcessor rp = new RequestProcessor();
    private final String displayName;
    private final Action[] themesNodeActions;
    private final Project project;

    /**
     * Creates new LibrariesNode named displayName displaying classPathProperty classpath
     * and optionaly Java platform.
     * @param project {@link Project} used for reading and updating project's metadata
     */
    ThemesFolderNode(Project project) {
        super(new ThemesChildren(project));
        this.displayName = NbBundle.getMessage(ThemesFolderNode.class, "CTL_ThemesNode");
        this.themesNodeActions = new Action[]{
            /* TODO: Themes folder actions */
        };
        this.project = project;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getName() {
        return this.getDisplayName();
    }

    public Action[] getActions(boolean context) {
        return this.themesNodeActions;
    }

    public Image getIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/visualweb/webui/jsf/defaulttheme/resources/JSF-themesFolder.png"); // NOI18N;
    }

    public Image getOpenedIcon(int type) {
        // TODO: need graphic for opened folder icon
        return Utilities.loadImage("org/netbeans/modules/visualweb/webui/jsf/defaulttheme/resources/JSF-themesFolder.png"); // NOI18N;
    }

    public boolean canCopy() {
        return false;
    }

    //Static Action Factory Methods
    public static Action createSetAsCurrentThemeAction(Library theme, Project project) {
        return new SetAsCurrentThemeAction(theme, project);
    }

    //Static inner classes
    private static class ThemesChildren extends Children.Keys implements PropertyChangeListener {

        private final Project project;

        ThemesChildren(Project project) {
            this.project = project;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            this.setKeys(getKeys());
        }

        protected void addNotify() {
            LibraryManager.getDefault().addPropertyChangeListener(this);
            this.setKeys(getKeys());
        }

        protected void removeNotify() {
            LibraryManager.getDefault().removePropertyChangeListener(this);
            this.setKeys(Collections.EMPTY_SET);
        }

        protected Node[] createNodes(Object obj) {
            String version = getThemeLibraryVersion((Library) obj);

            if (WoodStock_ThemeVersion.equals(version)) {
                Node n = new ThemeNode((Library) obj, project, version);
                return new Node[]{n};
            } else {
                return new Node[]{};
            }
        }

        private List getKeys() {
            List themesList = new /*Library*/ ArrayList();
            Library[] libraries = LibraryManager.getDefault().getLibraries();
            for (int i = 0; i < libraries.length; i++) {
                Library lib = libraries[i];
                // XXX if (ThemeLibraryDefinition.LIBRARY_NAME.equals(lib.getType())) {
                if ("theme".equals(lib.getType())) {
                    themesList.add(lib);
                }
            }

            return themesList;
        }
    }

    private static class ThemeNode extends AbstractNode implements PropertyChangeListener {

        Project project;
        Library theme;
        Action setCurrentThemeAction = null;
        String version;

        public ThemeNode(Library theme, Project project, String version) {
            super(Children.LEAF);
            this.setDisplayName(theme.getDisplayName());
            this.project = project;
            this.theme = theme;
            this.version = version;

            updateToolTip();
            updateIcon();

            JsfProjectUtils.addProjectPropertyListener(project, this);

            // #6311905
            theme.addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(this, theme));
        }

        public void destroy() throws IOException {
            JsfProjectUtils.removeProjectPropertyListener(project, this);
            super.destroy();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(JsfProjectConstants.PROP_CURRENT_THEME)) {
                this.fireIconChange();
            } else if (Library.PROP_CONTENT.equals(evt.getPropertyName())) {
                version = getThemeLibraryVersion(theme);
                updateToolTip();
                updateIcon();
            }
        }

        public Action[] getActions(boolean context) {
            return new Action[]{getAction()};
        }

        public Action getAction() {
            if (setCurrentThemeAction == null) {
                setCurrentThemeAction = ThemesFolderNode.createSetAsCurrentThemeAction(theme, project);
            }
            return setCurrentThemeAction;
        }

        public Image getIcon(int type) {
            Image baseImage = Utilities.loadImage("org/netbeans/modules/visualweb/webui/jsf/defaulttheme/resources/JSF-theme.png"); // NOI18N
            String currentTheme = JsfProjectUtils.getProjectProperty(project, JsfProjectConstants.PROP_CURRENT_THEME);
            if (currentTheme != null && currentTheme.equals(theme.getName())) {
                Image currentThemeBadge = Utilities.loadImage("org/netbeans/modules/visualweb/webui/jsf/defaulttheme/resources/JSF-currentThemeBadge.png"); // NOI18N
                baseImage = Utilities.mergeImages(baseImage, currentThemeBadge, baseImage.getWidth(null), baseImage.getHeight(null) - currentThemeBadge.getHeight(null) + 1);
                getAction().setEnabled(false);
            } else {
                if (version != null) {
                    getAction().setEnabled(true);
                } else {
                    Image errorBadge = Utilities.loadImage("org/netbeans/modules/visualweb/webui/jsf/defaulttheme/resources/JSF-error-badge.gif"); // NOI18N
                    baseImage = Utilities.mergeImages(baseImage, errorBadge, baseImage.getWidth(null), baseImage.getHeight(null) - errorBadge.getHeight(null) + 1);

                    getAction().setEnabled(false);
                }
            }

            return baseImage;
        }

        private void updateToolTip() {
            String toolTip;
            if (version != null) {
                toolTip = NbBundle.getMessage(ThemesFolderNode.class, "LBL_ThemeLibraryDescription");
            } else {
                toolTip = NbBundle.getMessage(ThemesFolderNode.class, "LBL_ThemeLibraryDescription_Invalid");
            }

            setShortDescription(toolTip);
        }

        private void updateIcon() {
            // XXX If the getter (getIcon) is not overriden(incorrect), then here the setter would be called with the appropriate icon.
            fireIconChange();
        }
    }

    private static class SetAsCurrentThemeAction extends AbstractAction {

        private final Project project;
        private final Library theme;

        public SetAsCurrentThemeAction(Library theme, Project project) {
            super(NbBundle.getMessage(ThemesFolderNode.class, "LBL_SetAsCurrentTheme_Action"));
            this.project = project;
            this.theme = theme;
        }

        public void actionPerformed(ActionEvent e) {
            // TODO: set the project property to point at the current theme using
            // the library name as the identifier. Add a new library reference to the project
            // for the new theme.
            // Is there any other config file that needs to be updated with the current theme?
            // How do we handle multiple themes per project? In that case, we need lib refs for
            // all "available" themes (i.e. those that are bundled with the project) but then
            // we definitely need some way to communicate the current theme to the designtime
            // and runtime components.
            // Remove the old theme's library reference
            String oldTheme = JsfProjectUtils.getProjectProperty(project, JsfProjectConstants.PROP_CURRENT_THEME);
            if (oldTheme == null) {
                // TODO: Should scan all librefs for any theme libraries and remove them
                oldTheme = "theme-default"; // NOI18N
            }
            Library oldThemeLibrary = LibraryManager.getDefault().getLibrary(oldTheme);
            if (oldThemeLibrary != null) {
                try {
                    JsfProjectUtils.removeLibraryReferences(project, new Library[]{oldThemeLibrary});
                    JsfProjectUtils.removeLocalizedTheme(project, oldTheme);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
            JsfProjectUtils.putProjectProperty(project, JsfProjectConstants.PROP_CURRENT_THEME, theme.getName());
            try {
                JsfProjectUtils.addLibraryReferences(project, new Library[]{theme});
                JsfProjectUtils.addLocalizedTheme(project, theme.getName());
                String msg = NbBundle.getMessage(ThemesFolderNode.class, Utilities.isWindows() ? "MSG_ThemeChangeRestartIDE" : "MSG_ThemeChangeRebuild");
                DialogDescriptor nderr = new DialogDescriptor(msg, NbBundle.getMessage(NotifyDescriptor.class, "NTF_InformationTitle"), true, new Object[]{NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx("projrave_ui_elements_dialogs_theme_switch_info_db"), null);
                DialogDisplayer.getDefault().notify(nderr);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }

            // Refresh pages in the project
            RefreshService service = RefreshService.getDefault();
            if (service != null) {
                service.refresh(project);
            }
        }
    }

    private static String getThemeLibraryVersion(Library library) {
        // XXX It is a question whether to look for "classpath" or "runtime" or one of those.
        List contents = library.getContent("classpath"); // TEMP
        for (java.util.Iterator it = contents.iterator(); it.hasNext();) {
            Object content = it.next();
            if (content instanceof java.net.URL) {
                java.net.URL url = (java.net.URL) content;
                try {
                    url = extractFileUrlFromJarContentUrl(url);

                    FileObject fo = org.openide.filesystems.URLMapper.findFileObject(url);
                    if (fo != null) {
                        File file = FileUtil.toFile(fo);
                        if (file != null) {
                            return getThemeJarFileVersion(new java.util.jar.JarFile(file));
                        }
                    } else {
                        ErrorManager.getDefault().log("Cannot find file object for " + url.toString());
                    }
                } catch (java.net.MalformedURLException mue) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mue);
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                }
            }
        }
        return null;
    }

    // XXX This shouldn't be needed if the URL is kept in the file form.
    private static java.net.URL extractFileUrlFromJarContentUrl(java.net.URL jarContentUrl) throws java.net.MalformedURLException {
        // XXX Horrible way hot to extract the file protocol from inside the jar one, is there a nicer way?
        // If there is none, isn't it better to keep it in the lib as file protocol, and wrap it to jar
        // only when needed. Otherwise this is forced, and that is very ugly and error-prone.
        String externalForm = jarContentUrl.toExternalForm();
        String newExternalForm = externalForm.replaceFirst("jar:", ""); // NOI18N
        newExternalForm = newExternalForm.replaceFirst("!/", ""); // NOI18N
        return new java.net.URL(newExternalForm);
    }

    private static String getThemeJarFileVersion(java.util.jar.JarFile jarFile) {
        if (jarFile == null) {
            return null;
        }

        try {
            Manifest mf = jarFile.getManifest();
            if (mf != null) {
                Map<String, Attributes> entries = mf.getEntries();
                Iterator<Attributes> iter = entries.values().iterator();
                while (iter.hasNext()) {
                    Attributes attrs = iter.next();
                    String version = attrs.getValue("X-SJWUIC-Theme-Version"); // NOI18N
                    if (WoodStock_ThemeVersion.equals(version)) {
                        return version;
                    }
                }
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }

        return null;
    }
}