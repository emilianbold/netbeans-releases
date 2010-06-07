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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.encoder.coco.ui.CocoEncodingConst;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/**
 * The wizard iterator for retrieving external COBOL Copybooks into current
 * project.
 * 
 * @author Jun Xu
 */
public class RetrieveCopybookWizardIterator implements TemplateWizard.Iterator {

    private WizardDescriptor.Panel[] mPanels;
    private int mIndex;
    
    public RetrieveCopybookWizardIterator() {
    }

    /**
     * Instantiates the template using information provided by the wizard.
     * If instantiation fails then wizard remains open to enable correct values.
     * @param wiz - the wizard.
     * @return set of data objects that have been created (should contain
     * at least one).
     * @throws java.io.IOException - if the instantiation fails.
     */
    public Set<DataObject> instantiate(TemplateWizard wiz)
            throws IOException {
        try {
            PropertyValue sourceType =
                    (PropertyValue) wiz.getProperty(PropertyKey.SOURCE_TYPE);
            String sourceLocation =
                    (String) wiz.getProperty(PropertyKey.SOURCE_LOCATION);
            //File targetFolder = new File(
            //        (String) wiz.getProperty(PropertyKey.TARGET_FOLDER));
            boolean overwrite = (Boolean) wiz.getProperty(PropertyKey.OVERWRITE_EXIST);
            FileObject dir = Templates.getTargetFolder(wiz);
            String targetFileName;
            InputStream inStream;
            if (PropertyValue.FROM_URL.equals(sourceType)) {
                URL sourceURL = new URL(sourceLocation);
                targetFileName = new File(sourceURL.getPath()).getName();
                inStream = sourceURL.openStream();
            } else if (PropertyValue.FROM_FILE.equals(sourceType)) {
                File sourceFile = new File(sourceLocation);
                targetFileName = sourceFile.getName();
                inStream = new FileInputStream(sourceFile);
            } else {
                throw new IllegalArgumentException(
                        "Unknown source type: " + sourceType);
            }
            FileObject fObj = FileObjectUtil.createFileObject(dir,
                    targetFileName, CocoEncodingConst.DEFAULT_COBOL_EXT, overwrite);

            importOneFile(inStream, FileUtil.toFile(fObj));
            inStream.close();
            DataObject dObj = DataObject.find(fObj);
            if (dObj != null) {
                // open it in editor
                EditCookie edit = (EditCookie) dObj.getCookie(EditCookie.class);
                if (edit != null) {
                    edit.edit();
                }
            }
            Set<DataObject> dObjs = new HashSet<DataObject>(1);
            dObjs.add(dObj);
            return dObjs;
        } catch (IOException e) {
            throw new IOException("IOException: " + e.toString());
        }
    }

    public void initialize(TemplateWizard wiz) {
        if (mPanels == null) {
            mPanels = new WizardDescriptor.Panel[]{
                new RetrieveCopybookWizardPanel()};
        }
        String steps[] = createSteps(wiz);
        // int delta = steps.length - mPanels.length;
        for (int i = 0; i < mPanels.length; i++) {
            if (mPanels[i].getComponent() instanceof JComponent) {
                JComponent jc = (JComponent) mPanels[i].getComponent();
                //How does this index match the steps? Somehow it just works.
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
        mIndex = 0;
        wiz.putProperty(PropertyKey.CURRENT_PROJECT, Templates.getProject(wiz));
        wiz.putProperty(PropertyKey.CURRENT_FOLDER, Templates.getTargetFolder(wiz));
    }

    public void uninitialize(TemplateWizard wiz) {
        mPanels = null;
    }

    @SuppressWarnings("unchecked")
    public Panel<WizardDescriptor> current() {
        return mPanels[mIndex];
    }

    public String name() {
        return mIndex + 1 + ". from " + mPanels.length;
    }

    public boolean hasNext() {
        return mIndex < mPanels.length - 1;
    }

    public boolean hasPrevious() {
        return mIndex > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        mIndex++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        mIndex--;
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }
    
    /*
     * This code is a bit odd to be here.  If someone does not know how wizard
     * framework works internally, how would he/she write the following code?
     * And why the last one of the beforeSteps is always "..."?
     */
    private String[] createSteps(WizardDescriptor wizard) {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty("WizardPanel_contentData");
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        
        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }
        
        String[] res = new String[beforeSteps.length - 1 + mPanels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < beforeSteps.length - 1) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = mPanels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }

    /**
     * Imports one file from the source InputStream into targetFile.
     * @param in source InputStream object.
     * @param targetFile target File.
     * @throws java.io.IOException --
     */
    private void importOneFile(InputStream in, File targetFile)
            throws IOException {
        byte[] buf = new byte[1024];
        int numOfBytesActuallyRead;
        OutputStream out = new FileOutputStream(targetFile);
        while ((numOfBytesActuallyRead = in.read(buf)) >= 0) {
            out.write(buf, 0, numOfBytesActuallyRead);
        }
        out.flush();
        out.close();
    }
}
