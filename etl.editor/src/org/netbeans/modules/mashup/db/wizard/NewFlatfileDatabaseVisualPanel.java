package org.netbeans.modules.mashup.db.wizard;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JPanel;
import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.netbeans.modules.mashup.db.ui.AxionDBConfiguration;
import org.netbeans.modules.mashup.tables.wizard.MashupTableWizardIterator;

public final class NewFlatfileDatabaseVisualPanel extends JPanel {

    class NameFieldKeyAdapter extends KeyAdapter {

        /**
         * Overrides default implementation to notify listeners of new flat file database
         * definition name value in associated textfield.
         *
         * @param e KeyEvent to be handled
         */
        @Override
        public void keyReleased(KeyEvent e) {
            checkDBName();
            NewFlatfileDatabaseVisualPanel.this.owner.fireChangeEvent();
        }
    }
    private boolean canProceed = false;
    private NewFlatfileDatabaseWizardPanel owner;

    /**
     * Creates new form NewFlatfileDatabaseVisualPanel
     */
    public NewFlatfileDatabaseVisualPanel() {
        initComponents();
        errorMsg.setForeground(Color.RED);
        dbLoc.setForeground(Color.BLUE);
        driverClass.setForeground(Color.BLUE);
        dbName.setText("");
        errorMsg.setText("");
        dbName.addKeyListener(new NameFieldKeyAdapter());
    }

    public NewFlatfileDatabaseVisualPanel(NewFlatfileDatabaseWizardPanel panel) {
        this();
        this.owner = panel;
    }

    @Override
    public String getName() {
        return "Create Mashup Database";
    }

    public void clearText() {
        dbName.setText("");
    }

    public boolean canProceed() {
        checkDBName();
        return this.canProceed;
    }

    private String getDefaultWorkingFolder() {
        File conf = AxionDBConfiguration.getConfigFile();
        Properties prop = new Properties();
        try {
            FileInputStream in = new FileInputStream(conf);
            prop.load(in);
        } catch (FileNotFoundException ex) {
        //ignore
        } catch (IOException ex) {
        //ignore
        }
        String defaultDir = prop.getProperty(AxionDBConfiguration.PROP_DB_LOC);
        defaultDir = defaultDir.replace('/', '\\');
        if (!defaultDir.endsWith("\\")) {
            defaultDir = defaultDir + "\\";
        }
        return defaultDir;
    }

    public String getDBName() {
        return dbName.getText().trim();
    }

    public void setDBName(String name) {
        dbName.setText(name.trim());
    }

    public void setErrorMsg(String msg) {
        errorMsg.setText(msg);
    }

    public String getErrorMsg() {
        return errorMsg.getText();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        dbName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        dbLoc = new javax.swing.JLabel();
        driver = new javax.swing.JLabel();
        driverClass = new javax.swing.JLabel();
        errorMsg = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(10000, 4000));
        setPreferredSize(new java.awt.Dimension(10, 4));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Database Name");

        dbName.setToolTipText("Database name should start with an alphabet.");
        dbName.addKeyListener(new NameFieldKeyAdapter());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "Location");

        org.openide.awt.Mnemonics.setLocalizedText(driver, "Driver Class");

        org.openide.awt.Mnemonics.setLocalizedText(driverClass, "org.axiondb.jdbc.AxionDriver");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).add(jLabel1).add(layout.createSequentialGroup().add(driver).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(driverClass).addContainerGap()).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(dbName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 161, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(190, 190, 190)).add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(org.jdesktop.layout.GroupLayout.LEADING, dbLoc, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE).add(errorMsg, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)).addContainerGap())))));
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(40, 40, 40).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(dbName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(errorMsg, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(14, 14, 14).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(dbLoc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(32, 32, 32).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(driver).add(driverClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    }

    private void checkDBName() {
        String name = dbName.getText().trim();
        String location = null;

        if (MashupTableWizardIterator.IS_PROJECT_CALL) {
            location = ETLEditorSupport.PRJ_PATH + "\\nbproject\\private\\databases";
            dbLoc.setText("${project.home}" + "\\nbproject\\private\\databases");
        } else {
            location = getDefaultWorkingFolder();
            dbLoc.setText(location);
        }

        File f = new File(location + "\\" + name);
        char[] ch = name.toCharArray();
        if (ch.length != 0) {
            if (f.exists()) {
                errorMsg.setText("Database '" + name + "' already exists.");
                canProceed = false;
            } else if (Character.isDigit(ch[0])) {
                errorMsg.setText("Database name should start with an alphabet.");
                canProceed = false;
            } else {
                errorMsg.setText("");
                canProceed = true;
            }
        } else {
            errorMsg.setText("");
            canProceed = false;
        }
    }
    private javax.swing.JLabel dbLoc;
    private javax.swing.JTextField dbName;
    private javax.swing.JLabel driver;
    private javax.swing.JLabel driverClass;
    private javax.swing.JLabel errorMsg;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
}
