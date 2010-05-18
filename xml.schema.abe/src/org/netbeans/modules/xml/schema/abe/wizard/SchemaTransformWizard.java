/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.xml.schema.abe.wizard;

import java.awt.Component;
import java.awt.Dialog;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.netbeans.modules.xml.axi.SchemaGeneratorFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * An action on the SchemaDataObject node (SchemaNode)
 * to "Transform" the schema (from one design pattern to another)
 *
 * @author Ayub Khan
 */
public class SchemaTransformWizard implements WizardDescriptor.InstantiatingIterator {
    private static final long serialVersionUID = 1L;
    
    public static final String SINGLE_GLOBAL_ELEMENT_KEY = "singleGlobalElementKey"; //NOI18N
    
    public static final String TYPE_REUSE_KEY = "typeReuseKey"; //NOI18N
    
    public static final String INFERED_DESIGN_PATTERN_KEY = "inferedDesignPatternKey"; //NOI18N
    
    public static final String SELECTED_DESIGN_PATTERN_KEY = "selectedDesignPatternKey"; //NOI18N
    
    public static final String SCHEMA_MODEL_KEY = "schemaModelKey"; //NOI18N
    
    private int index;
    
    private WizardDescriptor.Panel[] panels;
    
    private WizardDescriptor wizard;
    
    private RequestProcessor.Task transformTask;
    
//    private ProgressHandle progressHandle;
    
    private SchemaTransformProgressPanel progressPanel = new SchemaTransformProgressPanel();
    
    private SchemaModel sm;
    
    private String fileName;
    
    private boolean isCancelled;
    
    private boolean finishTransform;
    
    /** Creates a new instance of SchemaTransformWizard */
    public SchemaTransformWizard(final SchemaModel sm) {
        this.sm = sm;
        FileObject fo = (FileObject) sm.getModelSource().getLookup().
                lookup(FileObject.class);
        if(fo != null)
            this.fileName = fo.getNameExt();
    }
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            SchemaGenerator.Pattern inferedPattern = inferSchemaDesignPattern();
            panels = new WizardDescriptor.Panel[] {
                new SchemaTransformPatternSelection(inferedPattern)
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
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));//NOI18n
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);//NOI18n
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);//NOI18n
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);//NOI18n
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);//NOI18n
                }
            }
        }
        return panels;
    }
    
    public SchemaGenerator.Pattern show() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        initialize(wizardDescriptor);
        
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(
                NbBundle.getMessage(SchemaTransformWizard.class,"TITLE_SchemaTransform"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(SchemaTransformWizard.class,"TITLE_SchemaTransform"));
        dialog.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(SchemaTransformWizard.class,"HINT_SchemaTransform"));
        dialog.setVisible(true);
        dialog.toFront();
        
        final SchemaGenerator.Pattern selectedPattern = (SchemaGenerator.Pattern)
        wizard.getProperty(SchemaTransformWizard.SELECTED_DESIGN_PATTERN_KEY);
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        this.isCancelled = cancelled;
        if (!cancelled) {
            transformTask = RequestProcessor.getDefault().create(new Runnable() {
                public void run() {
                    try {
                        SchemaGeneratorFactory.getDefault().transformSchema(
                                sm, selectedPattern);
                    } catch(IOException iox) {
                        ErrorManager.getDefault().notify(iox);
                    } finally {
                        finishTransform = true;
                        finishProgress();
                    }
                }
            });
            transformTask.schedule(50);
            finishTransform = false;
            startProgress(NbBundle.getMessage(SchemaTransformWizard.class,
                    "MSG_SchemaTransform_ProgressMessage", fileName));
        }
        return selectedPattern;
    }
    
    /**
     * Starts associated progress if not yet started. Allows to share
     * progress with execution preparation phase (cache ops).
     *
     * @param details progress detail messag eor null
     */
    private void startProgress(String details) {
        DialogDescriptor d = null;
        
        //keep showing the dialog if user closes the dialog and transform is not finished
        while(!finishTransform) {
            // clear/hide dialog if any
            progressPanel.hideDialog();
            
            d = progressPanel.createDialog(fileName);
            progressPanel.showDialog(fileName);
        }
    }
    
    private void finishProgress() {
        progressPanel.hideDialog();
    }
    
    private SchemaGenerator.Pattern inferSchemaDesignPattern() {
        AXIModel am = AXIModelFactory.getDefault().getModel(sm);
        //inferedPattern = am.getSchemaDesignPattern();
        SchemaGenerator.Pattern inferedPattern =
                SchemaGeneratorFactory.getDefault().inferDesignPattern(am);
        if(inferedPattern == null)
            inferedPattern = SchemaGenerator.DEFAULT_DESIGN_PATTERN;
        return inferedPattern;
    }
    
    private void selectInitialDesignPattern(WizardDescriptor wizard, SchemaGenerator.Pattern p) {
        if(p == SchemaGenerator.Pattern.RUSSIAN_DOLL) {
            wizard.putProperty(SchemaTransformWizard.SINGLE_GLOBAL_ELEMENT_KEY,
                    Boolean.valueOf(true));
            wizard.putProperty(SchemaTransformWizard.TYPE_REUSE_KEY,
                    Boolean.valueOf(false));
        } else if(p == SchemaGenerator.Pattern.VENITIAN_BLIND) {
            wizard.putProperty(SchemaTransformWizard.SINGLE_GLOBAL_ELEMENT_KEY,
                    Boolean.valueOf(true));
            wizard.putProperty(SchemaTransformWizard.TYPE_REUSE_KEY,
                    Boolean.valueOf(true));
        } else if(p == SchemaGenerator.Pattern.SALAMI_SLICE) {
            wizard.putProperty(SchemaTransformWizard.SINGLE_GLOBAL_ELEMENT_KEY,
                    Boolean.valueOf(false));
            wizard.putProperty(SchemaTransformWizard.TYPE_REUSE_KEY,
                    Boolean.valueOf(false));
        } else if(p == SchemaGenerator.Pattern.GARDEN_OF_EDEN) {
            wizard.putProperty(SchemaTransformWizard.SINGLE_GLOBAL_ELEMENT_KEY,
                    Boolean.valueOf(false));
            wizard.putProperty(SchemaTransformWizard.TYPE_REUSE_KEY,
                    Boolean.valueOf(true));
        }
    }
    
    public boolean isCancelled() {
        return isCancelled;
    }
    
    public Set instantiate() throws IOException {
        return Collections.emptySet();
    }
    
    public void initialize(WizardDescriptor wizard) {
        SchemaGenerator.Pattern inferedPattern = inferSchemaDesignPattern();
        
        wizard.putProperty(SchemaTransformWizard.SCHEMA_MODEL_KEY, this.sm);
        wizard.putProperty(SchemaTransformWizard.SELECTED_DESIGN_PATTERN_KEY,
                inferedPattern);
        wizard.putProperty(SchemaTransformWizard.INFERED_DESIGN_PATTERN_KEY,
                inferedPattern);
        selectInitialDesignPattern(wizard, inferedPattern);
        this.wizard = wizard;
    }
    
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }
    
    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }
    
    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }
    
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    public void addChangeListener(ChangeListener l) {}
    
    public void removeChangeListener(ChangeListener l) {}
}
