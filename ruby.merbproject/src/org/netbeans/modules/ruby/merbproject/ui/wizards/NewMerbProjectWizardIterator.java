/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.merbproject.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.merbproject.MerbProjectGenerator;
import org.netbeans.modules.ruby.merbproject.ui.MerbProjectSettings;
import org.netbeans.modules.ruby.rubyproject.Util;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Merb project.
 */
public class NewMerbProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    static enum Type { APPLICATION, EXISTING; }
    
    static final String PROP_NAME_INDEX = "nameIndex";      //NOI18N

    private static final long serialVersionUID = 1L;
    
    private Type type;
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public NewMerbProjectWizardIterator() {
        this(Type.APPLICATION);
    }
    
    NewMerbProjectWizardIterator(Type type) {
        this.type = type;
    }
        
    public static NewMerbProjectWizardIterator existing() {
        return new NewMerbProjectWizardIterator(Type.EXISTING);
    }

    private WizardDescriptor.Panel[] createPanels() {
        WizardDescriptor.Panel[] result;
        switch (this.type) {
            case APPLICATION:
                result = new WizardDescriptor.Panel[] {
                    new PanelConfigureProject(this.type)
                };
                break;
            case EXISTING:
                result = new WizardDescriptor.Panel[] {
                    new PanelConfigureProject(this.type),
                    new PanelSourceFolders.Panel()
                };
                break;
            default:
                throw new IllegalStateException("unknown type: " + type);
        }
        return result;
    }
    
    private String[] createSteps() {
        String[] result;
        switch (this.type) {
            case APPLICATION:
                result = new String[] {
                    NbBundle.getMessage(NewMerbProjectWizardIterator.class,"LAB_ConfigureProject"),
                };
                break;
            case EXISTING:
                result = new String[] {                
                    NbBundle.getMessage(NewMerbProjectWizardIterator.class,"LAB_ConfigureProject"),
                    NbBundle.getMessage(NewMerbProjectWizardIterator.class,"LAB_ConfigureSourceRoots"),
                };
                break;
            default:
                throw new IllegalStateException("unknown type: " + type);
        }
        return result;
    }
    
    
    public Set<FileObject> instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    public Set<FileObject> instantiate(final ProgressHandle handle) throws IOException {
        handle.start(4);
        //handle.progress (NbBundle.getMessage (NewRubyProjectWizardIterator.class, "LBL_NewRubyProjectWizardIterator_WizardProgress_ReadingProperties"));
        Set<FileObject> resultSet = new HashSet<FileObject>();
        File dirF = (File) wiz.getProperty("projdir");        //NOI18N
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        String name = (String) wiz.getProperty("name");        //NOI18N
        handle.progress (NbBundle.getMessage (NewMerbProjectWizardIterator.class, "LBL_NewRubyProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        RubyPlatform platform = (RubyPlatform) wiz.getProperty("platform"); // NOI18N
        if (this.type == Type.EXISTING) {
            File[] sourceFolders = (File[])wiz.getProperty("sourceRoot");        //NOI18N
            File[] testFolders = (File[])wiz.getProperty("testRoot");            //NOI18N
            MerbProjectGenerator.createProject(dirF, name, sourceFolders, testFolders, platform);
            handle.progress(2);
            for (int i=0; i<sourceFolders.length; i++) {
                FileObject srcFo = FileUtil.toFileObject(sourceFolders[i]);
                if (srcFo != null) {
                    resultSet.add (srcFo);
                }
            }
        } else if (type == Type.APPLICATION) {
            String appType = (String) wiz.getProperty("appType"); //NOI18N
            MerbProjectGenerator.createProject(dirF, name, appType, platform);
        }
        FileObject dir = FileUtil.toFileObject(dirF);
        handle.progress (3);

        // Returning FileObject of project diretory. 
        Integer index = (Integer) wiz.getProperty(PROP_NAME_INDEX);
        switch (this.type) {
            case APPLICATION:
                MerbProjectSettings.getDefault().setNewApplicationCount(index.intValue());
                break;
            case EXISTING:
                MerbProjectSettings.getDefault().setNewProjectCount(index.intValue());
                break;
            default:
                throw new IllegalStateException("unknown type: " + type);
        }        
        resultSet.add(dir);
        handle.progress (NbBundle.getMessage (NewMerbProjectWizardIterator.class, "LBL_NewRubyProjectWizardIterator_WizardProgress_PreparingToOpen"), 4);
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder(dirF);    
        }
                        
        return resultSet;
    }
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
        //set the default values of the sourceRoot and the testRoot properties
        this.wiz.putProperty("sourceRoot", new File[0]);    //NOI18N
        this.wiz.putProperty("testRoot", new File[0]);      //NOI18N
    }

    public void uninitialize(WizardDescriptor wiz) {
        if (this.wiz != null) {
            this.wiz.putProperty("projdir", null);           //NOI18N
            this.wiz.putProperty("name", null);          //NOI18N
            this.wiz.putProperty("mainClass", null);         //NOI18N
            this.wiz.putProperty("platform", null);         //NOI18N
            if (this.type == Type.EXISTING) {
                this.wiz.putProperty("sourceRoot", null);    //NOI18N
                this.wiz.putProperty("testRoot", null);      //NOI18N
            }
            this.wiz = null;
            panels = null;
        }
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(NewMerbProjectWizardIterator.class, "LAB_IteratorName"), index + 1, panels.length);
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    static String getPackageName (String displayName) {
        StringBuffer builder = new StringBuffer ();
        boolean firstLetter = true;
        for (int i=0; i< displayName.length(); i++) {
            char c = displayName.charAt(i);            
            if ((!firstLetter && Character.isJavaIdentifierPart (c)) || (firstLetter && Character.isJavaIdentifierStart(c))) {
                firstLetter = false;
                if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                }                    
                builder.append(c);
            }            
        }
        return builder.length() == 0 ? NbBundle.getMessage(NewMerbProjectWizardIterator.class,"TXT_DefaultPackageName") : builder.toString();
    }
    
}
