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
package org.netbeans.modules.refactoring.java.ui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.awt.Mnemonics;
import org.openide.explorer.view.NodeRenderer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
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
            resources = new ArrayList(javaObjects);
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
                    pkgName != null ? pkgName : getDOPackageName(((FileObject)javaObjects.iterator().next()).getParent()),
                    getString("LBL_MoveClassesHeadline")
            );
        }
        return panel;
    }
    
//    private static String getResPackageName(Resource res) {
//        String name = res.getName();
//        if ( name.indexOf('/') == -1 )
//            return "";
//        return name.substring(0, name.lastIndexOf('/')).replace('/','.');
//    }
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
            refactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm() + URLEncoder.encode(panel.getPackageName().replace('.','/'), "utf-8"))));
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
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

    private final Vector getNodes() {
        Vector<Node> result = new Vector(javaObjects.size());
        LinkedList<FileObject> q = new LinkedList<FileObject>(javaObjects);
        while (!q.isEmpty()) {
            FileObject f = q.removeFirst();
            if (!VisibilityQuery.getDefault().isVisible(f))
                continue;
            DataObject d = null;
            try {
                d = DataObject.find(f);
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }
            if (d instanceof DataFolder) {
                for (DataObject o:((DataFolder) d).getChildren()) {
                    q.addLast(o.getPrimaryFile());
                }
            } else {
                result.add(d.getNodeDelegate());
            }
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
            super(parent, startPackage, headLine, targetFolder != null ? targetFolder : (FileObject) javaObjects.iterator().next() );
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
