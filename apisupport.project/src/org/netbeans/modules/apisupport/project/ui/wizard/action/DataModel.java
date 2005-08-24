/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 * Data model used across the <em>New Action Wizard</em>.
 */
final class DataModel extends BasicWizardIterator.BasicDataModel {
    
    private CreatedModifiedFiles cmf;
    
    // first panel data (Action Type)
    private boolean alwaysEnabled;
    private String[] cookieClasses;
    private boolean multiSelection;
    
    // second panel data (GUI Registration)
    private String category;
    
    // global menu item fields
    private boolean globalMenuItemEnabled;
    private String[] gmiParentMenuPath;
    private Position gmiPosition;
    private boolean gmiSeparatorAfter;
    private boolean gmiSeparatorBefore;
    
    // global toolbar button fields
    private boolean toolbarEnabled;
    private String toolbar;
    private Position toolbarPosition;
    
    // global keyboard shortcut
    private boolean kbShortcutEnabled;
    private String keyStroke;
    
    // file type context menu item
    private boolean ftContextEnabled;
    private String ftContextType;
    private Position ftContextPosition;
    private boolean ftContextSeparatorAfter;
    private boolean ftContextSeparatorBefore;
    
    // editor context menu item
    private boolean edContextEnabled;
    private String edContextType;
    private Position edContextPosition;
    private boolean edContextSeparatorAfter;
    private boolean edContextSeparatorBefore;
    
    // third panel data (Name, Icon, and Location)
    private String className;
    private String displayName;
    private String origIconPath;
    
    DataModel(WizardDescriptor wiz) {
        super(wiz);
    }
    
    private void regenerate() {
        String fqClassName = getPackageName() + '.' + className;
        String dashedPkgName = getPackageName().replace('.', '-');
        String dashedFqClassName = dashedPkgName + '-' + className;
        String shadow = dashedFqClassName + ".shadow"; // NOI18N
        
        cmf = new CreatedModifiedFiles(getProject());
        
        // Create CallableSystemAction from template
        String actionPath = getDefaultPackagePath(className + ".java"); // NOI18N
        // XXX use nbresloc URL protocol rather than DataModel.class.getResource(...):
        URL template = DataModel.class.getResource(alwaysEnabled
                ? "callableSystemAction.javx" : "cookieAction.javx"); // NOI18N
        Map replaceTokens = new HashMap();
        replaceTokens.put("@@CLASS_NAME@@", className); // NOI18N
        replaceTokens.put("@@PACKAGE_NAME@@", getPackageName()); // NOI18N
        replaceTokens.put("@@DISPLAY_NAME@@", displayName); // NOI18N
        replaceTokens.put("@@MODE@@", getSelectionMode()); // NOI18N
        if (!alwaysEnabled) {
            String indent = "            "; // NOI18N
            String newLine = System.getProperty("line.separator"); // NOI18N
            StringBuffer cookieSB = new StringBuffer();
            for (int i = 0; i < cookieClasses.length; i++) {
                cookieSB.append(indent + cookieClasses[i] + ".class"); // NOI18N
                if (i != cookieClasses.length - 1) {
                    cookieSB.append(',' + newLine);
                }
            }
            replaceTokens.put("@@COOKIE_CLASSES_BLOCK@@", cookieSB.toString()); // NOI18N
        }
        cmf.add(cmf.createFileWithSubstitutions(actionPath, template, replaceTokens));

        // Copy action icon
        if (origIconPath != null) {
            String relativeIconPath = copyIconToDefatulPackage(cmf, origIconPath);
            replaceTokens.put("@@ICON_RESOURCE@@", '"' + relativeIconPath + '"'); // NOI18N
        } else {
            replaceTokens.put("@@ICON_RESOURCE@@", "null"); // NOI18N
        }
        
        // add layer entry about the action
        String instanceFullPath = "Actions/" + category + "/" // NOI18N
                + dashedFqClassName + ".instance"; // NOI18N
        cmf.add(cmf.createLayerEntry(instanceFullPath, null, null, null, null));
        cmf.add(cmf.createLayerAttribute(instanceFullPath, "instanceClass", fqClassName)); // NOI18N
        
        // add dependency on util to project.xml
        cmf.add(cmf.addModuleDependency("org.openide.util", -1, null, true)); // NOI18N
        if (!alwaysEnabled) {
            cmf.add(cmf.addModuleDependency("org.openide.nodes", -1, null, true)); // NOI18N
        }
        
        // create layer entry for global menu item
        if (globalMenuItemEnabled) {
            String parentPath = getGMIParentMenuPath();
            generateShadowWithOrderAndSeparator(parentPath, shadow,
                    dashedPkgName, instanceFullPath, gmiSeparatorBefore,
                    gmiSeparatorAfter, gmiPosition);
        }
        
        // create layer entry for toolbar button
        if (toolbarEnabled) {
            String parentPath = "Toolbars/" + toolbar; // NOI18N
            generateOrder(parentPath, toolbarPosition.getBefore(), shadow);
            generateShadow(parentPath + "/" + shadow, instanceFullPath); // NOI18N
            generateOrder(parentPath, shadow, toolbarPosition.getAfter());
        }
        
        // create layer entry for keyboard shortcut
        if (kbShortcutEnabled) {
            String parentPath = "Shortcuts"; // NOI18N
            generateShadow(parentPath + "/" + keyStroke + ".shadow", instanceFullPath); // NOI18N
        }
        
        // create file type context menu item
        if (ftContextEnabled) {
            String parentPath = "Loaders/" + ftContextType + "/Actions"; // NOI18N
            generateShadowWithOrderAndSeparator(parentPath, shadow,
                    dashedPkgName, instanceFullPath, ftContextSeparatorBefore,
                    ftContextSeparatorAfter, ftContextPosition);
        }
        
        // create editor context menu item
        if (edContextEnabled) {
            String parentPath = "Editors/" + edContextType + "/Popup"; // NOI18N
            generateShadowWithOrderAndSeparator(parentPath, shadow,
                    dashedPkgName, instanceFullPath, edContextSeparatorBefore,
                    edContextSeparatorAfter, edContextPosition);
        }
    }
    
    private void generateShadowWithOrderAndSeparator(
            final String parentPath,
            final String shadow,
            final String dashedPkgName,
            final String instanceFullPath,
            final boolean separatorBefore,
            final boolean separatorAfter,
            final Position position) {
        if (separatorBefore) {
            String sepName = dashedPkgName + "-separatorBefore.instance"; // NOI18N
            generateSeparator(parentPath, sepName);
            generateOrder(parentPath, position.getBefore(), sepName);
            generateOrder(parentPath, sepName, shadow);
        } else {
            generateOrder(parentPath, position.getBefore(), shadow);
        }
        generateShadow(parentPath + "/" + shadow, instanceFullPath); // NOI18N
        if (separatorAfter) {
            String sepName = dashedPkgName + "-separatorAfter.instance"; // NOI18N
            generateSeparator(parentPath, sepName);
            generateOrder(parentPath, shadow, sepName);
            generateOrder(parentPath, sepName, position.getAfter());
        } else {
            generateOrder(parentPath, shadow, position.getAfter());
        }
    }
    
    /**
     * Just a helper convenient mehtod for cleaner code. If either
     * <em>before</em> or <em>after</em> is <code>null</code>, nothing will be
     * generated.
     */
    private void generateOrder(String layerPath, String before, String after) {
        if (before != null && after != null) {
            cmf.add(cmf.orderLayerEntry(layerPath, before, after));
        }
    }
    
    /** Checks whether a proposed class exists. */
    boolean classExists() {
        FileObject classFO = getProject().getProjectDirectory().getFileObject(
                getDefaultPackagePath(className + ".java")); // NOI18N
        return classFO != null;
    }
    
    private void generateShadow(final String itemPath, final String origInstance) {
        cmf.add(cmf.createLayerEntry(itemPath, null, null, null, null));
        cmf.add(cmf.createLayerAttribute(itemPath, "originalFile", origInstance)); // NOI18N
    }
    
    CreatedModifiedFiles getCreatedModifiedFiles() {
        if (cmf == null) {
            regenerate();
        }
        return cmf;
    }
    
    private void reset() {
        cmf = null;
    }
    
    void setAlwaysEnabled(boolean alwaysEnabled) {
        this.alwaysEnabled = alwaysEnabled;
    }
    
    boolean isAlwaysEnabled() {
        return alwaysEnabled;
    }
    
    void setCookieClasses(String[] cookieClasses) {
        this.cookieClasses = cookieClasses;
    }
    
    void setMultiSelection(boolean multiSelection) {
        this.multiSelection = multiSelection;
    }
    
    private String getSelectionMode() {
        return multiSelection ? "MODE_ANY" : "MODE_EXACTLY_ONE"; // NOI18N
    }
    
    void setCategory(String category) {
        this.category = category;
    }
    
    void setClassName(String className) {
        reset();
        this.className = className;
    }
    
    void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    void setIcon(String origIconPath) {
        reset();
        this.origIconPath = origIconPath;
    }
    
    public void setPackageName(String pkg) {
        super.setPackageName(pkg);
        reset();
    }
    
    void setGlobalMenuItemEnabled(boolean globalMenuItemEnabled) {
        this.globalMenuItemEnabled = globalMenuItemEnabled;
    }
    
    private String getGMIParentMenuPath() {
       StringBuffer sb = new StringBuffer("Menu"); // NOI18N
        for (int i = 0; i < gmiParentMenuPath.length; i++) {
            sb.append('/' + gmiParentMenuPath[i]);
        }
        return sb.toString();
    }
    
    void setGMIParentMenu(String[] gmiParentMenuPath) {
        this.gmiParentMenuPath = gmiParentMenuPath;
    }
    
    void setGMISeparatorAfter(boolean gmiSeparatorAfter) {
        this.gmiSeparatorAfter = gmiSeparatorAfter;
    }
    
    void setGMISeparatorBefore(boolean gmiSeparatorBefore) {
        this.gmiSeparatorBefore = gmiSeparatorBefore;
    }
    
    void setGMIPosition(Position position) {
        this.gmiPosition = position;
    }
    
    void setToolbarEnabled(boolean toolbarEnabled) {
        this.toolbarEnabled = toolbarEnabled;
    }
    
    void setToolbar(String toolbar) {
        this.toolbar = toolbar;
    }
    
    void setToolbarPosition(Position position) {
        this.toolbarPosition = position;
    }
    
    void setKeyboardShortcutEnabled(boolean kbShortcutEnabled) {
        this.kbShortcutEnabled = kbShortcutEnabled;
    }
    
    void setKeyStroke(String keyStroke) {
        this.keyStroke = keyStroke;
    }
    
    void setFileTypeContextEnabled(boolean contextEnabled) {
        this.ftContextEnabled = contextEnabled;
    }
    
    void setFTContextType(String contextType) {
        this.ftContextType = contextType;
    }
    
    void setFTContextPosition(Position position) {
        this.ftContextPosition = position;
    }
    
    void setFTContextSeparatorAfter(boolean separator) {
        this.ftContextSeparatorAfter = separator;
    }
    
    void setFTContextSeparatorBefore(boolean separator) {
        this.ftContextSeparatorBefore = separator;
    }
    
    void setEditorContextEnabled(boolean contextEnabled) {
        this.edContextEnabled = contextEnabled;
    }
    
    void setEdContextType(String contextType) {
        this.edContextType = contextType;
    }
    
    void setEdContextPosition(Position position) {
        this.edContextPosition = position;
    }
    
    void setEdContextSeparatorAfter(boolean separator) {
        this.edContextSeparatorAfter = separator;
    }
    
    void setEdContextSeparatorBefore(boolean separator) {
        this.edContextSeparatorBefore = separator;
    }
    
    static final class Position {
        
        private String before;
        private String after;
        private String beforeName;
        private String afterName;
        
        Position(String before, String after) {
            this(before, after, null, null);
        }
        
        Position(String before, String after, String beforeName, String afterName) {
            this.before = before;
            this.after = after;
            this.beforeName = beforeName;
            this.afterName = afterName;
        }
        
        String getBefore() {
            return before;
        }
        
        String getAfter() {
            return after;
        }
        
        String getBeforeName() {
            return beforeName;
        }
        
        String getAfterName() {
            return afterName;
        }
    }
    
    private void generateSeparator(final String parentPath, final String sepName) {
        String sepPath = parentPath + "/" + sepName; // NOI18N
        cmf.add(cmf.createLayerEntry(sepPath,
                null, null, null, null));
        cmf.add(cmf.createLayerAttribute(sepPath, "instanceClass", // NOI18N
                "javax.swing.JSeparator")); // NOI18N
    }
    
}

