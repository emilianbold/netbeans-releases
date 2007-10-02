/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.refactoring.ruby.ui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import org.netbeans.api.gsfpath.classpath.ClassPath;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.ruby.RetoucheUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.awt.Mnemonics;
import org.openide.explorer.view.NodeRenderer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * @author Jan Becicka
 */
public class MoveClassesUI implements RefactoringUI, RefactoringUIBypass {
    
    private List<FileObject> resources;
    private Set<FileObject> javaObjects;
    private MovePanel panel;
    private MoveRefactoring refactoring;
    private String targetPkgName = "";
    private boolean disable;
    private FileObject targetFolder;
    private PasteType pasteType;
    
    static final String getString(String key) {
        return NbBundle.getMessage(MoveClassUI.class, key);
    }
    
    public MoveClassesUI(Set<FileObject> javaObjects) {
        this(javaObjects, null, null);
    }

    public MoveClassesUI(Set<FileObject> javaObjects, FileObject targetFolder, PasteType paste) {
        this.disable = targetFolder != null;
        this.targetFolder = targetFolder;
        this.javaObjects=javaObjects;
        this.pasteType = paste;
        if (!disable) {
            resources = new ArrayList<FileObject>(javaObjects);
        }
    }
    
    public String getName() {
        return getString ("LBL_MoveClasses");
    }
     
    public String getDescription() {
        return getName();
    }
    
    public boolean isQuery() {
        return false;
    }
        
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            String pkgName = null;
            if (targetFolder != null) {
                ClassPath cp = ClassPath.getClassPath(targetFolder, ClassPath.SOURCE);
                if (cp != null)
                    pkgName = cp.getResourceName(targetFolder, '.', false);
            }
            panel = new MovePanel (parent, 
                    pkgName != null ? pkgName : getDOPackageName((javaObjects.iterator().next()).getParent()),
                    getString("LBL_MoveClassesHeadline")
            );
        }
        return panel;
    }
    
    private static String getDOPackageName(FileObject f) {
        return ClassPath.getClassPath(f, ClassPath.SOURCE).getResourceName(f, '.', false);
    }

    private String packageName () {
        return targetPkgName.trim().length() == 0 ? getString ("LBL_DefaultPackage") : targetPkgName.trim ();
    }
    
    private Problem setParameters(boolean checkOnly) {
        if (panel==null) 
            return null;
        URL url = URLMapper.findURL(panel.getRootFolder(), URLMapper.EXTERNAL);
        try {
            refactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm() + URLEncoder.encode(panel.getPackageName().replace('.','/')))));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        if (checkOnly) {
            return refactoring.fastCheckParameters();
        } else {
            return refactoring.checkParameters();
        }
    }
    
    public Problem checkParameters() {
        return setParameters(true);
    }
    
    public Problem setParameters() {
        return setParameters(false);
    }
    
    public AbstractRefactoring getRefactoring() {
        if (refactoring == null) {
            if (disable) {
                refactoring = new MoveRefactoring(Lookups.fixed(javaObjects.toArray()));
                refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(javaObjects.toArray(new FileObject[javaObjects.size()])));
            } else {
                refactoring = new MoveRefactoring (Lookups.fixed(resources.toArray()));
                refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(resources.toArray(new FileObject[resources.size()])));
            }
        }
        return refactoring;
    }

    private final Vector<Node> getNodes() {
        Vector<Node> result = new Vector<Node>(javaObjects.size());
        for(Iterator i = javaObjects.iterator(); i.hasNext();) {
            DataObject d = null;
            try {
                d = DataObject.find((FileObject) i.next());
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }
            result.add(d.getNodeDelegate());
        }
        return result;
    }
 
    public boolean hasParameters() {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(MoveClassesUI.class);
    }

    public boolean isRefactoringBypassRequired() {
        return !panel.isUpdateReferences();
    }
    public void doRefactoringBypass() throws IOException {
        pasteType.paste();
    }

    // MovePanel ...............................................................
    class MovePanel extends MoveClassPanel {
        public MovePanel (final ChangeListener parent, String startPackage, String headLine) {
            super(parent, startPackage, headLine, targetFolder != null ? targetFolder : javaObjects.iterator().next() );
            setCombosEnabled(!disable);
            JList list = new JList(getNodes());
            list.setCellRenderer(new NodeRenderer()); 
            list.setVisibleRowCount(5);
            JScrollPane pane = new JScrollPane(list);
            bottomPanel.setBorder(new EmptyBorder(8,0,0,0));
            bottomPanel.setLayout(new BorderLayout());
            bottomPanel.add(pane, BorderLayout.CENTER);
            JLabel listOf = new JLabel();
            Mnemonics.setLocalizedText(listOf, NbBundle.getMessage(MoveClassesUI.class, "LBL_ListOfClasses"));
            bottomPanel.add(listOf, BorderLayout.NORTH);
        }
    }
}
