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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.encoder.coco.ui.wizard;

import com.sun.encoder.coco.xsdbuilder.CocoXsdBuilder;
import com.sun.encoder.coco.xsdbuilder.CocoXsdBuilderException;
import com.sun.encoder.coco.xsdbuilder.CocoXsdBuilderSpec;
import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.modules.encoder.coco.ui.CocoEncodingConst;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

public final class GenerateXSDWizardAction extends NodeAction {
    
    private WizardDescriptor.Panel[] panels;
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new GenerateXSDWizardPanel()
            };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }
    
    public String getName() {
        return "Generate Encoder Definition XSD...";
    }
    
    @Override
    public String iconResource() {
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }

    @SuppressWarnings({"unchecked"})
    protected void performAction(Node[] activatedNodes) {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Generate Encoder Definition XSD");
        DataObject dobj = activatedNodes[0].getCookie(DataObject.class);
        Set<FileObject> files = dobj.files();
        FileObject fileObj = files.iterator().next();
        File cpyFile = new File(FileUtil.toFile(fileObj).getPath());
        wizardDescriptor.putProperty(PropertyKey.CURRENT_FOLDER, fileObj.getParent());
        wizardDescriptor.putProperty(PropertyKey.CURRENT_FILE_NAME, cpyFile.getName());
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled =
            wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            boolean overwrite =
                (Boolean) wizardDescriptor.getProperty(PropertyKey.OVERWRITE_EXIST);
            FileObject xsdFileObj = null;
            try {
                xsdFileObj = FileObjectUtil.createFileObject(
                        fileObj.getParent(), FileObjectUtil.getBaseName(cpyFile.getName()), CocoEncodingConst.XSD_EXT, overwrite);
                String copybookCodePage =
                    (String) wizardDescriptor.getProperty(PropertyKey.COPYBOOK_CODEPAGE);
                String displayCodePage =
                    (String) wizardDescriptor.getProperty(PropertyKey.DISPLAY_CODEPAGE);
                String display1CodePage =
                    (String) wizardDescriptor.getProperty(PropertyKey.DISPLAY1_CODEPAGE);
                String predecodeCoding =
                    (String) wizardDescriptor.getProperty(PropertyKey.PREDECODE_CODING);
                String postencodeCoding =
                    (String) wizardDescriptor.getProperty(PropertyKey.POSTENCODE_CODING);
                boolean checkReservedWords =
                    (Boolean) wizardDescriptor.getProperty(PropertyKey.CHECK_RESERVED_WORDS);
                boolean ignoreCol72Beyond =
                    (Boolean) wizardDescriptor.getProperty(PropertyKey.IGNORE_72_COL_BEYOND);
                String targetNamespace = (String) wizardDescriptor.getProperty(PropertyKey.TARGET_NAMESPACE);
                CocoXsdBuilderSpec spec = new CocoXsdBuilderSpec();
                spec.setCopybookLocation(cpyFile.getAbsolutePath());
                spec.setXsdLocation(FileUtil.toFile(xsdFileObj).getAbsolutePath());
                spec.setTargetNamespace(targetNamespace);
                spec.setCopybookCharEncoding(copybookCodePage);
                spec.setDisplayCharEncoding(displayCodePage);
                spec.setDisplay1CharEncoding(display1CodePage);
                spec.setPreDecodeCharCoding(predecodeCoding);
                spec.setPostEncodeCharCoding(postencodeCoding);
                spec.setCheckNamesForReservedWords(checkReservedWords);
                spec.setIgnoreContentBeyondCol72(ignoreCol72Beyond);
                CocoXsdBuilder builder = new CocoXsdBuilder(spec);
                builder.buildXsd();
                DataObject dObj =
                        DataLoaderPool.getDefault().findDataObject(xsdFileObj);
                if (dObj != null) {
                    // open it in editor
                    EditCookie edit = (EditCookie) dObj.getCookie(EditCookie.class);
                    if (edit != null) {
                        edit.edit();
                    }
                }
            } catch (CocoXsdBuilderException ex) {
                NotifyDescriptor d = new NotifyDescriptor.Message(ex.toString(),
                         NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                if (xsdFileObj != null) {
                    try {
                        xsdFileObj.delete();
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
                    }
                }
            } catch (IOException ex) {
                NotifyDescriptor d = new NotifyDescriptor.Message(ex.toString(),
                         NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                if (xsdFileObj != null) {
                    try {
                        xsdFileObj.delete();
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
                    }
                }
            }
        }
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
}

