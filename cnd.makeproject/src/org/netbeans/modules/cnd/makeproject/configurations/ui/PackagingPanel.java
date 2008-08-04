/*
 * PackagingPanel.java
 *
 * Created on June 20, 2008, 4:19 PM
 */

package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.packaging.InfoElement;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  thp
 */
public class PackagingPanel extends javax.swing.JPanel implements HelpCtx.Provider, PropertyChangeListener  {
    PackagingConfiguration packagingConfiguration;
    private PropertyEditorSupport editor;
    private MakeConfiguration conf;
    private PackagingInfoOuterPanel packagingInfoOuterPanel = null;
    private PackagingInfoPanel packagingInfoPanel = null;
    private PackagingFilesOuterPanel packagingFilesOuterPanel = null;
    private PackagingFilesPanel packagingFilesPanel = null;
    
    /** Creates new form PackagingPanel */
    public PackagingPanel(PackagingConfiguration packagingConfiguration, PropertyEditorSupport editor, PropertyEnv env, MakeConfiguration conf) {
        initComponents();
        
        this.packagingConfiguration = packagingConfiguration;
        this.editor = editor;
        this.conf = conf;
        
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);
        
        // Init default values
        if (!packagingConfiguration.getHeader().getModified()) {
            String defArch = getString("DefaultArchictureValue");
            if (conf.getPlatform().getValue() == Platform.PLATFORM_SOLARIS_INTEL) {
                defArch = "i386"; // NOI18N
            }
            else if (conf.getPlatform().getValue() == Platform.PLATFORM_SOLARIS_SPARC) {
                defArch = "sparc"; // NOI18N
            }
            List<InfoElement> headerList = packagingConfiguration.getHeader().getValue();
            headerList.add(new InfoElement("PKG", "MyPackage", true)); // NOI18N
            headerList.add(new InfoElement("NAME", "Package description ...", true)); // NOI18N
            headerList.add(new InfoElement("ARCH", defArch, true)); // NOI18N
            headerList.add(new InfoElement("CATEGORY", "application", true)); // NOI18N
            headerList.add(new InfoElement("VERSION", "1.0", true)); // NOI18N
            headerList.add(new InfoElement("BASEDIR", "/opt", false)); // NOI18N
            headerList.add(new InfoElement("PSTAMP", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()), false)); // NOI18N
            headerList.add(new InfoElement("CLASSES", "none", false)); // NOI18N

            packagingConfiguration.getHeader().setDirty(true);
        }
        
        // Add tabs
        packagingInfoOuterPanel = new PackagingInfoOuterPanel(packagingInfoPanel = new PackagingInfoPanel(packagingConfiguration.getHeader().getValue(), conf.getBaseDir()));
        if (packagingConfiguration.getType().getValue() == PackagingConfiguration.TYPE_SVR4_PACKAGE) {
            packagingFilesPanel = new PackagingFilesPanel(packagingConfiguration.getFiles().getValue(), conf.getBaseDir());
        }
        else if (packagingConfiguration.getType().getValue() == PackagingConfiguration.TYPE_TAR) {
            packagingFilesPanel = new PackagingFiles4Panel(packagingConfiguration.getFiles().getValue(), conf.getBaseDir());
        }
        else {
            packagingFilesPanel = new PackagingFiles4Panel(packagingConfiguration.getFiles().getValue(), conf.getBaseDir());
        }
        packagingFilesOuterPanel = new PackagingFilesOuterPanel(packagingFilesPanel, packagingConfiguration);
        
        tabbedPane.addTab(getString("InfoPanelText"), packagingInfoOuterPanel);
        tabbedPane.addTab(getString("FilePanelText"), packagingFilesOuterPanel);
            
        if (packagingConfiguration.getType().getValue() == PackagingConfiguration.TYPE_ZIP || packagingConfiguration.getType().getValue() == PackagingConfiguration.TYPE_TAR) {
            // Add tabs
            tabbedPane.setEnabledAt(0,false);
            tabbedPane.setEnabledAt(1,true);
            tabbedPane.setSelectedIndex(1);
        }
        else if (packagingConfiguration.getType().getValue() == PackagingConfiguration.TYPE_SVR4_PACKAGE) {
            // Add tabs
            tabbedPane.setEnabledAt(0,true);
            tabbedPane.setEnabledAt(1,true);
            tabbedPane.setSelectedIndex(0);
        }
        else {
            assert false;
        }
    }

        
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }
    
    private Object getPropertyValue() throws IllegalStateException {
        Vector v;
        
        v = packagingInfoPanel.getListData();
        packagingConfiguration.getHeader().setValue(new ArrayList(v));
        
        v = packagingFilesPanel.getListData();
        packagingConfiguration.getFiles().setValue(new ArrayList(v));
        
	return packagingConfiguration;
        
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("Libraries"); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        innerPanel = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();

        setPreferredSize(new java.awt.Dimension(1000, 500));
        setLayout(new java.awt.GridBagLayout());

        innerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        innerPanel.add(tabbedPane, gridBagConstraints);
        tabbedPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PackagingPanel.class, "PackagingPanel.tabbedPane.AccessibleContext.accessibleName")); // NOI18N
        tabbedPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PackagingPanel.class, "PackagingPanel.tabbedPane.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(innerPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel innerPanel;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(PackagingPanel.class);
	}
	return bundle.getString(s);
    }
}
