package org.netbeans.modules.apisupport.project.ui.wizard.glf;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicVisualPanel;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public final class GLFTemplateVisualPanel2 extends BasicVisualPanel {

    private GLFTemplateWizardPanel2 wizardPanel;
    private CreatedModifiedFiles cmf;
    private boolean initialized;

    /** Creates new form GLFTemplateVisualPanel2 */
    public GLFTemplateVisualPanel2(final GLFTemplateWizardPanel2 wizardPanel) {
        super(wizardPanel.getIterator().getWizardDescriptor());
        this.wizardPanel = wizardPanel;
        initComponents();
        wizardPanel.getIterator().getWizardDescriptor().putProperty("NewFileWizard_Title", // NOI18N
                NbBundle.getMessage(GLFTemplateVisualPanel2.class, "LBL_GLFWizardTitle"));

        // Creating 1st entry takes couple of seconds, we better do it in background
        String msg = NbBundle.getMessage(GLFTemplateVisualPanel2.class, "LBL_PleaseWait");
        createdFiles.setText(msg);
        modifiedFiles.setText(msg);
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                try {
                    cmf = new CreatedModifiedFiles(wizardPanel.getIterator().getProject());
                    cmf.add(cmf.createLayerEntry(
                            "Editors/x1/x2/language.nbs", // NOI18N
                            CreatedModifiedFiles.getTemplate("NBSTemplate.nbs"), Collections.<String, String>emptyMap(), null, null));
                } finally {
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            initialized = true;
                            createdFiles.setText("");
                            modifiedFiles.setText("");
                            update();
                        }
                    });
                }
            }
        });

        DocumentListener documentListener = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void changedUpdate(DocumentEvent e) {
                update();
            }
        };
        tfExtensions.getDocument().addDocumentListener(documentListener);
        tfMimeType.getDocument().addDocumentListener(documentListener);
        update();
    }
    private static final Pattern MIME_PATTERN = Pattern.compile("[\\w.]+(?:[+-][\\w.]+)?/[\\w.]+(?:[+-][\\w.]+)?");  // NOI18N
    private static final Pattern EXT_PATTERN = Pattern.compile("(\\w+\\s*)+");  // NOI18N

    void update() {
        if (getMimeType().trim().length() == 0) {
            setInfo(NbBundle.getMessage(GLFTemplateVisualPanel2.class, "MSG_Empty_Mime_Type"), false);
            return;
        } else if (!MIME_PATTERN.matcher(getMimeType().trim()).matches()) {    // reasonable mime type check
            setError(NbBundle.getMessage(GLFTemplateVisualPanel2.class, "CTL_Invalid_Mime_Type"));
            return;
        }
        
        if (getExtensions().trim().length() == 0) {
            setInfo(NbBundle.getMessage(GLFTemplateVisualPanel2.class, "MSG_Empty_Extensions"), false);
            return;
        } else if (!EXT_PATTERN.matcher(getExtensions().trim()).matches()) {
            setError(NbBundle.getMessage(GLFTemplateVisualPanel2.class, "CTL_Invalid_Extensions"));
            return;
        }
        
        if (!initialized) {
            markInvalid();
            return;
        }
        markValid();

        String mimeType = getMimeType();
        String extensions = getExtensions();

        int i = mimeType.indexOf('/');
        String mimeType1 = mimeType.substring(0, i);
        String mimeType2 = mimeType.substring(i + 1);

        cmf = new CreatedModifiedFiles(wizardPanel.getIterator().getProject());

        cmf.add(cmf.createLayerEntry(
                "Editors/" + mimeType1 + "/" + mimeType2 + "/" + "language.nbs", // NOI18N
                CreatedModifiedFiles.getTemplate("NBSTemplate.nbs"), Collections.<String, String>emptyMap(), null, null));

        cmf.add(cmf.createLayerEntry(
                "Navigator/Panels/" + mimeType1 + "/" + mimeType2 + "/" + "org-netbeans-modules-languages-features-LanguagesNavigator.instance", // NOI18N
                null, null, null, null));

        Map<String, String> toks = new HashMap<String, String>();
        toks.put("mime", mimeType);
        StringBuilder b = new StringBuilder();
        for (String ext : extensions.split(" ")) {
            b.append("        <ext name=\"").append(ext).append("\"/>\n");
        }
        toks.put("extensions", b.toString());
        cmf.add(cmf.createLayerEntry(
                "Services/MIMEResolver/" + mimeType1 + "-" + mimeType2 + "-mime-resolver.xml", // NOI18N
                CreatedModifiedFiles.getTemplate("nbsresolver.xml"), toks, null, null));

        createdFiles.setText(UIUtil.generateTextAreaContent(cmf.getCreatedPaths()));
        modifiedFiles.setText(UIUtil.generateTextAreaContent(cmf.getModifiedPaths()));
    }

    public 
    @Override
    String getName() {
        return NbBundle.getMessage(GLFTemplateVisualPanel2.class, "CTL_Step2");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modifiedFiles = new javax.swing.JTextArea();
        lMimeType = new javax.swing.JLabel();
        tfMimeType = new javax.swing.JTextField();
        lExtensions = new javax.swing.JLabel();
        tfExtensions = new javax.swing.JTextField();
        createdFiles = new javax.swing.JTextArea();
        extensionsHint = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        mimeTypeHint = new javax.swing.JLabel();

        modifiedFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        modifiedFiles.setColumns(20);
        modifiedFiles.setEditable(false);
        modifiedFiles.setRows(5);
        modifiedFiles.setBorder(null);

        lMimeType.setLabelFor(tfMimeType);
        org.openide.awt.Mnemonics.setLocalizedText(lMimeType, org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "CTL_Mime_Type")); // NOI18N

        lExtensions.setLabelFor(tfExtensions);
        org.openide.awt.Mnemonics.setLocalizedText(lExtensions, org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "CTL_Extensions")); // NOI18N

        createdFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFiles.setColumns(20);
        createdFiles.setEditable(false);
        createdFiles.setRows(5);
        createdFiles.setBorder(null);

        org.openide.awt.Mnemonics.setLocalizedText(extensionsHint, org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "CTL_Extensions_Comment")); // NOI18N

        jLabel1.setLabelFor(createdFiles);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "GLFTemplateVisualPanel2.jLabel1.text")); // NOI18N

        jLabel2.setLabelFor(modifiedFiles);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "GLFTemplateVisualPanel2.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(mimeTypeHint, org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "LBL_mimeTypeHint")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lMimeType)
                    .add(lExtensions)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, modifiedFiles, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                    .add(extensionsHint)
                    .add(createdFiles, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                    .add(tfExtensions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, tfMimeType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                    .add(mimeTypeHint, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lMimeType)
                    .add(tfMimeType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mimeTypeHint)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lExtensions)
                    .add(tfExtensions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(extensionsHint)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(createdFiles, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(modifiedFiles, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                    .add(jLabel2))
                .addContainerGap())
        );

        modifiedFiles.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GLFTemplateVisualPanel2.class, "GLFTemplateVisualPanel2.modifiedFiles.AccessibleContext.accessibleDescription")); // NOI18N
        tfMimeType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "GLFTemplateVisualPanel2.tfMimeType.AccessibleContext.accessibleDescription")); // NOI18N
        tfExtensions.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "GLFTemplateVisualPanel2.tfExtensions.AccessibleContext.accessibleDescription")); // NOI18N
        createdFiles.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GLFTemplateVisualPanel2.class, "GLFTemplateVisualPanel2.createdFiles.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GLFTemplateVisualPanel2.class, "GLFTemplateVisualPanel2.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea createdFiles;
    private javax.swing.JLabel extensionsHint;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lExtensions;
    private javax.swing.JLabel lMimeType;
    private javax.swing.JLabel mimeTypeHint;
    private javax.swing.JTextArea modifiedFiles;
    private javax.swing.JTextField tfExtensions;
    private javax.swing.JTextField tfMimeType;
    // End of variables declaration//GEN-END:variables
    String getMimeType() {
        return tfMimeType.getText();
    }

    String getExtensions() {
        return tfExtensions.getText();
    }

    public CreatedModifiedFiles getCreatedModifiedFiles() {
        return wizardPanel.isValid() ? cmf : null;  // so that dummy entry is not returned
    }
}

