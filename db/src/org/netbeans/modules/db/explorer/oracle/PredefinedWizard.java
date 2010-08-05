/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.explorer.oracle;

import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.dlg.ConnectionDialogMediator;
import org.netbeans.modules.db.explorer.dlg.SchemaPanel;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;

/**
 *
 * @author Jiri Rechtacek
 */
public class PredefinedWizard extends ConnectionDialogMediator implements WizardDescriptor.Iterator<PredefinedWizard> {
    private String driverLocation;

    private PredefinedWizard(Type type) {
        this.type = type;
    }

    @Override
    protected boolean retrieveSchemas(SchemaPanel schemaPanel, DatabaseConnection dbcon, String defaultSchema) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public enum Type {
        ORACLE,
        MYSQL
    }
    
    private WizardDescriptor.Panel<PredefinedWizard>[] panels;
    private Type type;
    private int index;
    private LookingForDriverPanel driverPanel;
    private boolean found = false;
    
    public static void showWizard(PredefinedConnectionProvider.PredefinedConnection conn) {
        if (conn instanceof PredefinedConnectionProvider.OracleConnection) {
            PredefinedWizard wiz = new PredefinedWizard(Type.ORACLE);
            wiz.openWizard();
        } else if (conn instanceof PredefinedConnectionProvider.MySQLConnection) {
            PredefinedWizard wiz = new PredefinedWizard(Type.MYSQL);
            wiz.openWizard();
        } else {
            assert false : "No PredefinedConnection found in lookup.";
        }
    }
    
    private void openWizard() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(this, this);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Your wizard dialog title here");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            // do something
        }
    }
    
    public static interface Panel extends WizardDescriptor.Panel<PredefinedWizard>{}
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel<PredefinedWizard>[] getPanels() {
        if (panels == null) {
            driverPanel = new LookingForDriverPanel(type);
            panels = new Panel[] {
                driverPanel,
                new ConnectionPanel(),
                new ChoosingSchemaPanel(),
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
                    // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
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
    
    @Override
    public WizardDescriptor.Panel<PredefinedWizard> current() {
        // init panels first
        getPanels();
        if (driverPanel.getDriverLocation() != null && ! found) {
            found = true;
            index++;
            setDriverLocation(driverPanel.getDriverLocation());
        }
                
        return getPanels()[index];
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    void setDriverLocation(String location) {
        this.driverLocation = location;
    }
    
    String getDriverLocation() {
        return driverLocation;
    }

}
