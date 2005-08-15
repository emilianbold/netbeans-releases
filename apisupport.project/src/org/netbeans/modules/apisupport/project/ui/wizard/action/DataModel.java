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

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * Data model used across the <em>New Action Wizard</em>.
 */
final class DataModel extends BasicWizardIterator.BasicDataModel {
    
    private CreatedModifiedFiles cmf;
    
    // first panel data (Action Type)
    private boolean alwaysEnabled;
    
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
    
    // third panel data (Name, Icon, and Location)
    private String className;
    private String displayName;
    private String origIconPath;
    private String packageName;
    
    // XXX such constans should be probably on more proper place (or generated
    // by mehtods in CreatedModifiedFiles automatically)
    private static final String STRING_VALUE = "stringvalue"; // NOI18N
    
    DataModel(WizardDescriptor wiz) {
        super(wiz);
    }
    
    private void regenerate() {
        String fqClassName = packageName + '.' + className; // NOI18N
        String dashedPkgName = packageName.replace('.', '-'); // NOI18N
        String dashedFqClassName = dashedPkgName + "-" + className; // NOI18N
        String shadow = dashedFqClassName + ".shadow"; // NOI18N
        
        cmf = new CreatedModifiedFiles(getProject());
        
        // Create CallableSystemAction from template
        String actionPath = getDefaultPackagePath(className + ".java"); // NOI18N
        // XXX use nbresloc URL protocol rather than NewActionIterator.class.getResource(...):
        URL template = NewActionIterator.class.getResource("callableSystemAction.javx"); // NOI18N
        Map replaceTokens = new HashMap();
        replaceTokens.put("@@CLASS_NAME@@", className); // NOI18N
        replaceTokens.put("@@PACKAGE_NAME@@", packageName); // NOI18N
        replaceTokens.put("@@DISPLAY_NAME@@", displayName); // NOI18N
        
        if (origIconPath != null) {
            FileObject origIconFO = FileUtil.toFileObject(new File(origIconPath));
            String relativeIconPath = null;
            if (!FileUtil.isParentOf(getProject().getSourceDirectory(), origIconFO)) {
                String iconPath = getDefaultPackagePath(origIconFO.getNameExt());
                try {
                    cmf.add(cmf.createFile(iconPath, origIconFO.getURL()));
                    relativeIconPath = packageName.replace('.', '/') + '/' + origIconFO.getNameExt();
                } catch (FileStateInvalidException exc) {
                    Util.err.notify(exc);
                    relativeIconPath = "null"; // NOI18N
                }
            } else {
                relativeIconPath = FileUtil.getRelativePath(getProject().getSourceDirectory(), origIconFO);
            }
            replaceTokens.put("@@ICON_RESOURCE@@", '"' + relativeIconPath + '"'); // NOI18N
        } else {
            replaceTokens.put("@@ICON_RESOURCE@@", "null"); // NOI18N
        }
        cmf.add(cmf.createFileWithSubstitutions(actionPath, template, replaceTokens));
        
        // add layer entry about the action
        String instanceFullPath = "Actions/" + category + "/" // NOI18N
                + dashedFqClassName + ".instance"; // NOI18N
        cmf.add(cmf.createLayerEntry(instanceFullPath, null, null, null, null, null, null));
        cmf.add(cmf.createLayerAttribute(instanceFullPath, "instanceClass", STRING_VALUE, fqClassName)); // NOI18N
        
        // add dependency on util to project.xml
        cmf.add(cmf.addModuleDependency("org.openide.util", -1, null, true)); // NOI18N
        
        // create layer entry for global menu item
        if (globalMenuItemEnabled) {
            String parentPath = getGMIParentMenuPath();
            if (gmiSeparatorBefore) {
                String sepName = dashedPkgName + "-separatorBefore.instance"; // NOI18N
                DataModel.generateSeparator(cmf, parentPath, sepName);
                generateOrder(parentPath, gmiPosition.getBefore(), sepName);
                generateOrder(parentPath, sepName, shadow);
            } else {
                generateOrder(parentPath, gmiPosition.getBefore(), shadow);
            }
            generateShadow(parentPath + "/" + shadow, instanceFullPath); // NOI18N
            generateOrder(parentPath, shadow, gmiPosition.getAfter());
            if (gmiSeparatorAfter) {
                String sepName = dashedPkgName + "-separatorAfter.instance"; // NOI18N
                DataModel.generateSeparator(cmf, parentPath, sepName);
                generateOrder(parentPath, shadow, sepName);
                generateOrder(parentPath, sepName, gmiPosition.getAfter());
            } else {
                generateOrder(parentPath, shadow, gmiPosition.getAfter());
            }
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
    }
    
    private String getDefaultPackagePath(String fileName) {
        StringBuffer sb = new StringBuffer();
        sb.append(getProject().getSourceDirectoryPath()).
                append("/"). // NOI18N
                append(packageName.replace('.','/')). // NOI18N
                append("/"). // NOI18N
                append(fileName);
        
        return sb.toString();
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
    
    private void generateShadow(final String itemPath, final String origInstance) {
        cmf.add(cmf.createLayerEntry(itemPath, null, null, null, null, null, null));
        cmf.add(cmf.createLayerAttribute(itemPath, "originalFile", STRING_VALUE, origInstance)); // NOI18N
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
    
    void setPackageName(String pkg) {
        reset();
        this.packageName = pkg;
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
    
    // XXX candidate for CreatedModifiedFiles?
    private static void generateSeparator(final CreatedModifiedFiles cmf,
            final String parentPath, final String sepName) {
        String sepPath = parentPath + "/" + sepName; // NOI18N
        cmf.add(cmf.createLayerEntry(sepPath,
                null, null, null, null, null, null));
        cmf.add(cmf.createLayerAttribute(sepPath, "instanceClass", // NOI18N
                STRING_VALUE, "javax.swing.JSeparator")); // NOI18N
    }
    
}
