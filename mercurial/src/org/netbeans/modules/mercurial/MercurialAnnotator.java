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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mercurial;

import org.netbeans.modules.mercurial.ui.clone.CloneAction;
import org.netbeans.modules.mercurial.ui.clone.CloneExternalAction;
import org.netbeans.modules.mercurial.ui.create.CreateAction;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.api.project.Project;
import org.openide.util.Utilities;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import javax.swing.*;
import java.awt.Image;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.lang.reflect.Field;
import java.lang.Exception;
import org.netbeans.modules.mercurial.ui.annotate.AnnotateAction;
import org.netbeans.modules.mercurial.ui.commit.CommitAction;
import org.netbeans.modules.mercurial.ui.diff.DiffAction;
import org.netbeans.modules.mercurial.ui.diff.ExportDiffAction;
import org.netbeans.modules.mercurial.ui.diff.ImportDiffAction;
import org.netbeans.modules.mercurial.ui.ignore.IgnoreAction;
import org.netbeans.modules.mercurial.ui.log.LogAction;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.netbeans.modules.mercurial.ui.properties.PropertiesAction;
import org.netbeans.modules.mercurial.ui.pull.PullAction;
import org.netbeans.modules.mercurial.ui.pull.PullOtherAction;
import org.netbeans.modules.mercurial.ui.push.PushAction;
import org.netbeans.modules.mercurial.ui.rollback.RollbackAction;
import org.netbeans.modules.mercurial.ui.update.RevertModificationsAction;
import org.netbeans.modules.mercurial.ui.status.StatusAction;
import org.netbeans.modules.mercurial.ui.update.ConflictResolvedAction;
import org.netbeans.modules.mercurial.ui.update.ResolveConflictsAction;
import org.netbeans.modules.mercurial.ui.update.UpdateAction;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Responsible for coloring file labels and file icons in the IDE and providing IDE with menu items.
 *
 * @author Maros Sandor
 */
public class MercurialAnnotator extends VCSAnnotator {
    
    private static final int INITIAL_ACTION_ARRAY_LENGTH = 25;
    private static MessageFormat uptodateFormat = getFormat("uptodateFormat");  // NOI18N
    private static MessageFormat newLocallyFormat = getFormat("newLocallyFormat");  // NOI18N
    private static MessageFormat addedLocallyFormat = getFormat("addedLocallyFormat"); // NOI18N
    private static MessageFormat modifiedLocallyFormat = getFormat("modifiedLocallyFormat"); // NOI18N
    private static MessageFormat removedLocallyFormat = getFormat("removedLocallyFormat"); // NOI18N
    private static MessageFormat deletedLocallyFormat = getFormat("deletedLocallyFormat"); // NOI18N
    private static MessageFormat excludedFormat = getFormat("excludedFormat"); // NOI18N
    private static MessageFormat conflictFormat = getFormat("conflictFormat"); // NOI18N
    
    private static final int STATUS_TEXT_ANNOTABLE = 
            FileInformation.STATUS_NOTVERSIONED_EXCLUDED |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | 
            FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_CONFLICT |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    private static final Pattern lessThan = Pattern.compile("<");  // NOI18N
    
    private static final int STATUS_BADGEABLE = 
            FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY;

    public static String ANNOTATION_REVISION    = "revision";
    public static String ANNOTATION_STATUS      = "status";
    public static String ANNOTATION_FOLDER      = "folder";

    public static String[] LABELS = new String[] {ANNOTATION_REVISION, ANNOTATION_STATUS, ANNOTATION_FOLDER};

    private FileStatusCache cache;
    private MessageFormat format;
    private String emptyFormat;
    private Boolean needRevisionForFormat;
    
    public MercurialAnnotator() {
        cache = Mercurial.getInstance().getFileStatusCache();
        initDefaults();
    }
    
    private void initDefaults() {
        Field [] fields = MercurialAnnotator.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            if (name.endsWith("Format")) {  // NOI18N
                initDefaultColor(name.substring(0, name.length() - 6));
            }
        }
        refresh();
    }

    public void refresh() {
        String string = HgModuleConfig.getDefault().getAnnotationFormat(); //System.getProperty("netbeans.experimental.svn.ui.statusLabelFormat");  // NOI18N
        if (string != null && !string.trim().equals("")) {
            if (string.indexOf("\\{revision\\}") != -1 ) {
                needRevisionForFormat = true;
            } else {
                needRevisionForFormat = false;
            }
            string = string.replaceAll("\\{revision\\}",  "\\{0\\}");           // NOI18N
            string = string.replaceAll("\\{status\\}",    "\\{1\\}");           // NOI18N
            string = string.replaceAll("\\{folder\\}",    "\\{2\\}");           // NOI18N
            format = new MessageFormat(string);
            emptyFormat = format.format(new String[] {"", "", ""} , new StringBuffer(), null).toString().trim();
        }
    }

    private void initDefaultColor(String name) {
        String color = System.getProperty("hg.color." + name);  // NOI18N
        if (color == null) return;
        setAnnotationColor(name, color);
    }


    /**
     * Changes annotation color of files.
     *
     * @param name name of the color to change. Can be one of:
     * newLocally, addedLocally, modifiedLocally, removedLocally, deletedLocally, newInRepository, modifiedInRepository,
     * removedInRepository, conflict, mergeable, excluded.
     * @param colorString new color in the format: 4455AA (RGB hexadecimal)
     */
    private void setAnnotationColor(String name, String colorString) {
        try {
            Field field = MercurialAnnotator.class.getDeclaredField(name + "Format");  // NOI18N
            MessageFormat format = new MessageFormat("<font color=\"" + colorString + "\">{0}</font><font color=\"#999999\">{1}</font>");  // NOI18N
            field.set(null, format);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid color name");  // NOI18N
        }
    }

    private static MessageFormat getFormat(String key) {
        String format = NbBundle.getMessage(MercurialAnnotator.class, key);
        return new MessageFormat(format);
    }
    
    public String annotateName(String name, VCSContext context) {
        int includeStatus = FileInformation.STATUS_VERSIONED_UPTODATE | FileInformation.STATUS_LOCAL_CHANGE | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
        
        FileInformation mostImportantInfo = null;
        File mostImportantFile = null;
        boolean folderAnnotation = false;
        
        for (File file : context.getRootFiles()) {
            FileInformation info = cache.getStatus(file);
            int status = info.getStatus();
            if ((status & includeStatus) == 0) continue;
            
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
                mostImportantFile = file;
                folderAnnotation = file.isDirectory();
            }
        }
        
        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !Utils.shareCommonDataObject(context.getRootFiles().toArray(new File[context.getRootFiles().size()]));
        }
        
        if (mostImportantInfo == null) return null;
        return folderAnnotation ?
            annotateFolderNameHtml(name, mostImportantInfo, mostImportantFile) :
            annotateNameHtml(name, mostImportantInfo, mostImportantFile);
    }
    
    public Image annotateIcon(Image icon, VCSContext context) {
        boolean folderAnnotation = false;
        for (File file : context.getRootFiles()) {
            if (file.isDirectory()) {
                folderAnnotation = true;
                break;
            }
        }
        
        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !Utils.shareCommonDataObject(context.getRootFiles().toArray(new File[context.getRootFiles().size()]));
        }
        
        if (folderAnnotation == false) {
            return null;
        }
        
        boolean isVersioned = false;
        for (Iterator i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = (File) i.next();
            if ((cache.getStatus(file).getStatus() & STATUS_BADGEABLE) != 0) {
                isVersioned = true;
                break;
            }
        }
        if (!isVersioned) return null;
        
        boolean allExcluded = true;
        boolean modified = false;
        
        Map map = cache.getAllModifiedFiles();
        Map modifiedFiles = new HashMap();
        for (Iterator i = map.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = (FileInformation) map.get(file);
            if ((info.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0) modifiedFiles.put(file, info);
        }
        
        for (Iterator i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = (File) i.next();
            if (VersioningSupport.isFlat(file)) {
                for (Iterator j = modifiedFiles.keySet().iterator(); j.hasNext();) {
                    File mf = (File) j.next();
                    if (mf.getParentFile().equals(file)) {
                        FileInformation info = (FileInformation) modifiedFiles.get(mf);
                        if (info.isDirectory()) continue;
                        int status = info.getStatus();
                        if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                            Image badge = Utilities.loadImage("org/netbeans/modules/mercurial/resources/icons/conflicts-badge.png", true);  // NOI18N
                            return Utilities.mergeImages(icon, badge, 16, 9);
                        }
                        modified = true;
                        allExcluded &= isExcludedFromCommit(mf.getAbsolutePath());
                    }
                }
            } else {
                for (Iterator j = modifiedFiles.keySet().iterator(); j.hasNext();) {
                    File mf = (File) j.next();
                    if (Utils.isAncestorOrEqual(file, mf)) {
                        FileInformation info = (FileInformation) modifiedFiles.get(mf);
                        int status = info.getStatus();
                        if ((status == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY || status == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) && file.equals(mf)) {
                            continue;
                        }
                        if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                            Image badge = Utilities.loadImage("org/netbeans/modules/mercurial/resources/icons/conflicts-badge.png", true); // NOI18N
                            return Utilities.mergeImages(icon, badge, 16, 9);
                        }
                        modified = true;
                        allExcluded &= isExcludedFromCommit(mf.getAbsolutePath());
                    }
                }
            }
        }
        
        if (modified && !allExcluded) {
            Image badge = Utilities.loadImage("org/netbeans/modules/mercurial/resources/icons/modified-badge.png", true); // NOI18N
            return Utilities.mergeImages(icon, badge, 16, 9);
        } else {
            return null;
        }
    }
    
    public Action[] getActions(VCSContext ctx, VCSAnnotator.ActionDestination destination) {
        // TODO: get resource strings for all actions:
        ResourceBundle loc = NbBundle.getBundle(MercurialAnnotator.class);
        Node [] nodes = ctx.getElements().lookupAll(Node.class).toArray(new Node[0]);
        File [] files = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);
        File root = HgUtils.getRootFile(ctx);
        boolean noneVersioned = isNothingVersioned(files);
        boolean onlyFolders = onlyFolders(files);
        boolean onlyProjects = onlyProjects(nodes);

        boolean goodVersion = Mercurial.getInstance().isGoodVersion();

        List<Action> actions = new ArrayList<Action>(INITIAL_ACTION_ARRAY_LENGTH);
        if (goodVersion && destination == VCSAnnotator.ActionDestination.MainMenu) {
            actions.add(new CreateAction(loc.getString("CTL_PopupMenuItem_Create"), ctx));
            actions.add(null);
            actions.add(new StatusAction(loc.getString("CTL_PopupMenuItem_Status"), ctx));
            actions.add(new DiffAction(loc.getString("CTL_PopupMenuItem_Diff"), ctx));
            actions.add(new UpdateAction(loc.getString("CTL_PopupMenuItem_Update"), ctx));
            actions.add(new CommitAction(loc.getString("CTL_PopupMenuItem_Commit"), ctx));
            actions.add(null);
            actions.add(new ExportDiffAction(loc.getString("CTL_PopupMenuItem_ExportDiff"), ctx));
            actions.add(new ImportDiffAction(loc.getString("CTL_PopupMenuItem_ImportDiff"), ctx));

            actions.add(null);
            if (root != null) {
                actions.add(new CloneAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_CloneLocal", 
                        root.getName()), ctx));
            }
            actions.add(new CloneExternalAction(loc.getString("CTL_PopupMenuItem_CloneOther"), ctx));            
            actions.add(new PushAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_PushLocal"), ctx));
            actions.add(new PullAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_PullLocal"), ctx));
            actions.add(new PullOtherAction(loc.getString("CTL_PopupMenuItem_PullOther"), ctx));
            actions.add(new MergeAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_Merge"), ctx));
            actions.add(null);
            AnnotateAction tempA = new AnnotateAction(loc.getString("CTL_PopupMenuItem_ShowAnnotations"), ctx);
            if (tempA.visible(nodes)) {
                tempA = new AnnotateAction(loc.getString("CTL_PopupMenuItem_HideAnnotations"), ctx);
            }
            actions.add(tempA);
            actions.add(new LogAction(loc.getString("CTL_PopupMenuItem_Log"), ctx));
            // TODO: actions.add(new ViewAction(loc.getString("CTL_PopupMenuItem_View"), ctx));
            actions.add(null);
            actions.add(new RollbackAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_Rollback"), ctx));
            actions.add(new RevertModificationsAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_Revert"), ctx));
            actions.add(new ResolveConflictsAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_Resolve"), ctx));
            IgnoreAction tempIA = new IgnoreAction(loc.getString("CTL_PopupMenuItem_Ignore"), ctx);
            if (files.length > 0 && tempIA.getActionStatus(files) == IgnoreAction.UNIGNORING) {
                tempIA = new IgnoreAction(loc.getString("CTL_PopupMenuItem_Unignore"), ctx);
            }
            actions.add(tempIA);
            actions.add(null);
            actions.add(new PropertiesAction(loc.getString("CTL_PopupMenuItem_Properties"), ctx));
        } else if (goodVersion) {
            if (noneVersioned){
                actions.add(new CreateAction(loc.getString("CTL_PopupMenuItem_Create"), ctx));
            }else{
                actions.add(new StatusAction(loc.getString("CTL_PopupMenuItem_Status"), ctx));
                actions.add(new DiffAction(loc.getString("CTL_PopupMenuItem_Diff"), ctx));
                actions.add(new UpdateAction(loc.getString("CTL_PopupMenuItem_Update"), ctx));
                actions.add(new CommitAction(loc.getString("CTL_PopupMenuItem_Commit"), ctx));
                actions.add(null);
                if (root != null) {
                    actions.add(new CloneAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_CloneLocal", 
                            root.getName()), ctx));
                }

                actions.add(new PushAction(NbBundle.getMessage(MercurialAnnotator.class, 
                        "CTL_PopupMenuItem_PushLocal"), ctx));
                actions.add(new PullAction(NbBundle.getMessage(MercurialAnnotator.class, 
                        "CTL_PopupMenuItem_PullLocal"), ctx));
                actions.add(new MergeAction(NbBundle.getMessage(MercurialAnnotator.class, 
                        "CTL_PopupMenuItem_Merge"), ctx));
                actions.add(null);                

                if (!onlyFolders) {
                    AnnotateAction tempA = new AnnotateAction(loc.getString("CTL_PopupMenuItem_ShowAnnotations"), ctx); 
                    if (tempA.visible(nodes)) {
                        tempA = new AnnotateAction(loc.getString("CTL_PopupMenuItem_HideAnnotations"), ctx); 
                    }
                    actions.add(tempA);
                }
                actions.add(new LogAction(loc.getString("CTL_PopupMenuItem_Log"), ctx));
                // TODO: actions.add(new ViewAction(loc.getString("CTL_PopupMenuItem_View"), ctx));
                actions.add(null);
                actions.add(new RollbackAction(NbBundle.getMessage(MercurialAnnotator.class,
                        "CTL_PopupMenuItem_Rollback"), ctx));
                actions.add(new RevertModificationsAction(NbBundle.getMessage(MercurialAnnotator.class,
                        "CTL_PopupMenuItem_Revert"), ctx));
                actions.add(new ResolveConflictsAction(NbBundle.getMessage(MercurialAnnotator.class,
                        "CTL_PopupMenuItem_Resolve"), ctx));
                if (!onlyProjects) {
                    actions.add(new ConflictResolvedAction(NbBundle.getMessage(MercurialAnnotator.class,
                        "CTL_PopupMenuItem_MarkResolved"), ctx));
                    
                    IgnoreAction tempIA = new IgnoreAction(loc.getString("CTL_PopupMenuItem_Ignore"), ctx); 
                    if (files.length > 0 && tempIA.getActionStatus(files) == IgnoreAction.UNIGNORING) {
                        tempIA = new IgnoreAction(loc.getString("CTL_PopupMenuItem_Unignore"), ctx); 
                    }
                    actions.add(tempIA);
                }
                actions.add(null);
                actions.add(new PropertiesAction(loc.getString("CTL_PopupMenuItem_Properties"), ctx));
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }
    
    /**
     * Applies custom format.
     */
    private String formatAnnotation(FileInformation info, File file) {
        String statusString = "";  // NOI18N
        int status = info.getStatus();
        if (status != FileInformation.STATUS_VERSIONED_UPTODATE) {
            statusString = info.getShortStatusText();
        }

        String revisionString = "";     // NOI18N
        String binaryString = "";       // NOI18N

        if (needRevisionForFormat) {
            try {
                File repository = Mercurial.getInstance().getTopmostManagedParent(file);
                String revStr = HgCommand.getLastRevision(repository, file);
                if (revStr != null) {
                    revisionString = revStr;
                }
            } catch (HgException ex) {
                NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                DialogDisplayer.getDefault().notifyLater(e);
            }
        }


        //String stickyString = SvnUtils.getCopy(file);
        String stickyString = null;
        if (stickyString == null) {
            stickyString = ""; // NOI18N
        }

        Object[] arguments = new Object[] {
            revisionString,
            statusString,
            stickyString,
        };

        String annotation = format.format(arguments, new StringBuffer(), null).toString().trim();
        if(annotation.equals(emptyFormat)) {
            return "";
        } else {
            return " " + annotation;
        }
    }

    public String annotateNameHtml(File file, FileInformation info) {
        return annotateNameHtml(file.getName(), info, file);
    }

    public String annotateNameHtml(String name, FileInformation mostImportantInfo, File mostImportantFile) {
        // Hg: The codes used to show the status of files are:
        // M = modified
        // A = added
        // R = removed
        // C = clean
        // ! = deleted, but still tracked
        // ? = not tracked
        // I = ignored (not shown by default)
        
        name = htmlEncode(name);
        
        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        int status = mostImportantInfo.getStatus();
        
        if (annotationsVisible && mostImportantFile != null && (status & STATUS_TEXT_ANNOTABLE) != 0) {
            if (format != null) {
                textAnnotation = formatAnnotation(mostImportantInfo, mostImportantFile);
            } else {
                //String sticky = SvnUtils.getCopy(mostImportantFile);
                String sticky = null;
                if (status == FileInformation.STATUS_VERSIONED_UPTODATE && sticky == null) {
                    textAnnotation = "";  // NOI18N
                } else if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
                    textAnnotation = " [" + sticky + "]"; // NOI18N
                } else if (sticky == null) {
                    String statusText = mostImportantInfo.getShortStatusText();
                    if(!statusText.equals("")) {
                        textAnnotation = " [" + mostImportantInfo.getShortStatusText() + "]"; // NOI18N
                    } else {
                        textAnnotation = ""; // NOI18N
                    }
                } else {
                    textAnnotation = " [" + mostImportantInfo.getShortStatusText() + "; " + sticky + "]"; // NOI18N
                }
            }
        } else {
            textAnnotation = ""; // NOI18N
        }

        if (textAnnotation.length() > 0) {
            textAnnotation = NbBundle.getMessage(MercurialAnnotator.class, "textAnnotation", textAnnotation);
        }

        if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return excludedFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return deletedLocallyFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return removedLocallyFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return newLocallyFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return addedLocallyFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return modifiedLocallyFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return uptodateFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return conflictFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return name;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            return name;
        } else {
            throw new IllegalArgumentException("Uncomparable status: " + status); // NOI18N
        }
    }
    
    private String htmlEncode(String name) {
        if (name.indexOf('<') == -1) return name;
        return lessThan.matcher(name).replaceAll("&lt;"); // NOI18N
    }
    
    private String annotateFolderNameHtml(String name, FileInformation mostImportantInfo, File mostImportantFile) {
        name = htmlEncode(name);
        if (mostImportantInfo.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED){
            return excludedFormat.format(new Object [] { name, ""});
        }
        return uptodateFormat.format(new Object [] { name, "" });
    }
    
    private boolean isMoreImportant(FileInformation a, FileInformation b) {
        if (b == null) return true;
        if (a == null) return false;
        return getComparableStatus(a.getStatus()) < getComparableStatus(b.getStatus());
    }
    
    /**
     * Gets integer status that can be used in comparators. The more important the status is for the user,
     * the lower value it has. Conflict is 0, unknown status is 100.
     *
     * @return status constant suitable for 'by importance' comparators
     */
    public static int getComparableStatus(int status) {
        if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return 0;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MERGE)) {
            return 1;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return 10;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return 11;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return 12;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return 13;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return 14;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return 30;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return 31;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return 32;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return 50;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return 100;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return 101;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            return 102;
        } else {
            throw new IllegalArgumentException("Uncomparable status: " + status); // NOI18N
        }
    }
    
    private boolean isExcludedFromCommit(String absolutePath) {
        return false;
    }
    
    private boolean isNothingVersioned(File[] files) {
        for (File file : files) {
            if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) != 0) return false;
        }
        return true;
    }
    
    private static boolean onlyProjects(Node[] nodes) {
        if (nodes == null) return false;
        for (Node node : nodes) {
            if (node.getLookup().lookup(Project.class) == null) return false;
        }
        return true;
    }
    
    private boolean onlyFolders(File[] files) {
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) return false;
            if (!files[i].exists() && !cache.getStatus(files[i]).isDirectory()) return false;
        }
        return true;
    }
    
}
