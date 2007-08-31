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

package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.diff.builtin.visualizer.TextDiffVisualizer;
import org.netbeans.modules.proxy.Base64Encoder;
import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.openide.windows.TopComponent;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.nodes.Node;
import org.openide.awt.StatusDisplayer;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * Exports diff to file:
 *
 * <ul>
 * <li>for components that implements {@link DiffSetupSource} interface
 * exports actually displayed diff.
 *
 * <li>for DataNodes <b>local</b> differencies between the current
 * working copy and BASE repository version.
 * </ul>
 *  
 * @author Petr Kuzel
 */
public class ExportDiffAction extends AbstractSystemAction {
    
    private static final int enabledForStatus =
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY |
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY |
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    public ExportDiffAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_ExportDiff";  // NOI18N
    }

    /**
     * First look for DiffSetupSource name then for super (context name).
     */
    public String getName() {
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated instanceof DiffSetupSource) {
            String setupName = ((DiffSetupSource)activated).getSetupDisplayName();
            if (setupName != null) {
                return NbBundle.getMessage(this.getClass(), getBaseName(getActivatedNodes()) + "_Context",  // NOI18N
                                            setupName);
            }
        }
        return super.getName();
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    public boolean enable(Node[] nodes) {
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated instanceof DiffSetupSource) {
            return true;
        }
        return  super.enable(nodes) && 
                Lookup.getDefault().lookup(DiffProvider.class) != null;
    }

    public void performCvsAction(final Node[] nodes) {

        // reevaluate fast enablement logic guess

        boolean noop;
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated instanceof DiffSetupSource) {
            noop = ((DiffSetupSource) activated).getSetups().isEmpty();
        } else {
            Context context = getContext(nodes);
            File [] files = DiffExecutor.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE);
            noop = files.length == 0;
        }
        if (noop) {
            NotifyDescriptor msg = new NotifyDescriptor.Message(NbBundle.getMessage(ExportDiffAction.class, "BK3001"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
            return;
        }

        final JFileChooser chooser = new AccessibleJFileChooser(NbBundle.getMessage(ExportDiffAction.class, "ACSD_Export"));
        chooser.setDialogTitle(NbBundle.getMessage(ExportDiffAction.class, "CTL_Export_Title"));
        chooser.setMultiSelectionEnabled(false);
        javax.swing.filechooser.FileFilter[] old = chooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            javax.swing.filechooser.FileFilter fileFilter = old[i];
            chooser.removeChoosableFileFilter(fileFilter);

        }        
        chooser.setCurrentDirectory(new File(CvsModuleConfig.getDefault().getPreferences().get("ExportDiff.saveFolder", System.getProperty("user.home")))); // NOI18N
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.getName().endsWith("diff") || f.getName().endsWith("patch") || f.isDirectory();  // NOI18N
            }
            public String getDescription() {
                return NbBundle.getMessage(ExportDiffAction.class, "BK3002");
            }
        });

        chooser.setDialogType( JFileChooser.SAVE_DIALOG );  // #71861
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(ExportDiffAction.class, "MNE_Export_ExportAction").charAt(0));
        chooser.setApproveButtonText(NbBundle.getMessage(ExportDiffAction.class, "CTL_Export_ExportAction"));
        DialogDescriptor dd = new DialogDescriptor(chooser, NbBundle.getMessage(ExportDiffAction.class, "CTL_Export_Title"));
        dd.setOptions(new Object[0]);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        chooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String state = (String)e.getActionCommand();
                if (state.equals(JFileChooser.APPROVE_SELECTION)) {
                    File destination = chooser.getSelectedFile();
                    String name = destination.getName();
                    boolean requiredExt = false;
                    requiredExt |= name.endsWith(".diff");  // NOI18N
                    requiredExt |= name.endsWith(".dif");   // NOI18N
                    requiredExt |= name.endsWith(".patch"); // NOI18N
                    if (requiredExt == false) {
                        File parent = destination.getParentFile();
                        destination = new File(parent, name + ".patch"); // NOI18N
                    }

                    if (destination.exists()) {
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(ExportDiffAction.class, "BK3005", destination.getAbsolutePath()));
                        nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
                        DialogDisplayer.getDefault().notify(nd);
                        if (nd.getValue().equals(NotifyDescriptor.OK_OPTION) == false) {
                            return;
                        }
                    }

                    CvsModuleConfig.getDefault().getPreferences().put("ExportDiff.saveFolder", destination.getParent());

                    final File out = destination;
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            async(nodes, out);
                        }
                    });
                }
                dialog.dispose();
            }
        });
        dialog.setVisible(true);

    }

    protected boolean asynchronous() {
        return false;
    }
    
    private void async(Node[] nodes, File destination) {
        boolean success = false;
        OutputStream out = null;
        int exportedFiles = 0;
        ExecutorGroup group = new ExecutorGroup(getRunningName(nodes));
        try {

            // prepare setups and common parent - root

            List<Setup> setups;

            TopComponent activated = TopComponent.getRegistry().getActivated();
            if (activated instanceof DiffSetupSource) {
                setups = new ArrayList<Setup>(((DiffSetupSource) activated).getSetups());
                List<File> setupFiles = new ArrayList<File>(setups.size());
                for (Iterator i = setups.iterator(); i.hasNext();) {
                    Setup setup = (Setup) i.next();
                    setupFiles.add(setup.getBaseFile()); 
                }
            } else {
                Context context = getContext(nodes);
                File [] files = DiffExecutor.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE);
                setups = new ArrayList<Setup>(files.length);
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    Setup setup = new Setup(file, Setup.DIFFTYPE_LOCAL);
                    setups.add(setup);
                }
            }

            String sep = System.getProperty("line.separator"); // NOI18N
            out = new BufferedOutputStream(new FileOutputStream(destination));
            // Used by PatchAction as MAGIC to detect right encoding
            out.write(("# This patch file was generated by NetBeans IDE" + sep).getBytes("utf8"));  // NOI18N
            out.write(("# This patch can be applied using context Tools: Apply Diff Patch action on respective folder." + sep).getBytes("utf8"));  // NOI18N
            out.write(("# It uses platform neutral UTF-8 encoding." + sep).getBytes("utf8"));  // NOI18N
            out.write(("# Above lines and this line are ignored by the patching process." + sep).getBytes("utf8"));  // NOI18N


            Collections.sort(setups, new Comparator<Setup>() {
                public int compare(Setup o1, Setup o2) {
                    return o1.getBaseFile().compareTo(o2.getBaseFile());
                }
            });
            Iterator it = setups.iterator();
            int i = 0;
            while (it.hasNext()) {
                Setup setup = (Setup) it.next();
                File file = setup.getBaseFile();
                group.progress(file.getName());

                String relativePath = getIndexPath(file);
                String index = "Index: " + relativePath + sep;   // NOI18N
                    out.write(index.getBytes("utf8")); // NOI18N
                exportDiff(group, setup, relativePath, out);
                i++;
            }

            exportedFiles = i;
            success = true;
        } catch (IOException ex) {
            ErrorManager.getDefault().annotate(ex, NbBundle.getMessage(ExportDiffAction.class, "BK3003"));
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);   // stack trace to log
            ErrorManager.getDefault().notify(ErrorManager.USER, ex);  // message to user
        } finally {
            group.executed();
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException alreadyClsoed) {
                }
            }
            if (success) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ExportDiffAction.class, "BK3004", new Integer(exportedFiles)));
                if (exportedFiles == 0) {
                    destination.delete();
                } else {
                    Utils.openFile(destination);
                }
            } else {
                destination.delete();
            }

        }
    }

    private static String getIndexPath(File file) {
        AdminHandler ah = CvsVersioningSystem.getInstance().getAdminHandler();
        try {
            return ah.getRepositoryForDirectory(file.getParent(), "").substring(1) + "/" + file.getName();
        } catch (IOException e) {
            return null;
        }
    }

    /** Writes contextual diff into given stream.*/
    private void exportDiff(ExecutorGroup group, Setup setup, String relativePath, OutputStream out) throws IOException {
        setup.initSources(group);
        DiffProvider diff = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);

        Reader r1 = null;
        Reader r2 = null;
        Difference[] differences;

        try {
            r1 = setup.getFirstSource().createReader();
            if (r1 == null) r1 = new StringReader("");  // NOI18N
            r2 = setup.getSecondSource().createReader();
            if (r2 == null) r2 = new StringReader("");  // NOI18N
            differences = diff.computeDiff(r1, r2);
        } finally {
            if (r1 != null) try { r1.close(); } catch (Exception e) {}
            if (r2 != null) try { r2.close(); } catch (Exception e) {}
        }

        File file = setup.getBaseFile();
        try {
            InputStream is;
            if (!CvsVersioningSystem.getInstance().isText(file) && differences.length == 0) {
                // assume the file is binary 
                is = new ByteArrayInputStream(exportBinaryFile(file).getBytes("utf8"));  // NOI18N
            } else {
                r1 = setup.getFirstSource().createReader();
                if (r1 == null) r1 = new StringReader(""); // NOI18N
                r2 = setup.getSecondSource().createReader();
                if (r2 == null) r2 = new StringReader(""); // NOI18N
                TextDiffVisualizer.TextDiffInfo info = new TextDiffVisualizer.TextDiffInfo(
                    relativePath + " " + setup.getFirstSource().getTitle(), // NOI18N
                    relativePath + " " + setup.getSecondSource().getTitle(),  // NOI18N
                    null,
                    null,
                    r1,
                    r2,
                    differences
                );
                info.setContextMode(true, 3);
                String diffText = TextDiffVisualizer.differenceToUnifiedDiffText(info);
                is = new ByteArrayInputStream(diffText.getBytes("utf8"));  // NOI18N
            }
            while(true) {
                int i = is.read();
                if (i == -1) break;
                out.write(i);
            }
        } finally {
            if (r1 != null) try { r1.close(); } catch (Exception e) {}
            if (r2 != null) try { r2.close(); } catch (Exception e) {}
        }
    }

    private String exportBinaryFile(File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringBuilder sb = new StringBuilder((int) file.length());
        if (file.canRead()) {
            Utils.copyStreamsCloseAll(baos, new FileInputStream(file));
        }
        sb.append("MIME: application/octet-stream; encoding: Base64; length: " + (file.canRead() ? file.length() : -1)); // NOI18N
        sb.append(System.getProperty("line.separator")); // NOI18N
        sb.append(Base64Encoder.encode(baos.toByteArray(), true));
        sb.append(System.getProperty("line.separator")); // NOI18N
        return sb.toString();
    }
}
