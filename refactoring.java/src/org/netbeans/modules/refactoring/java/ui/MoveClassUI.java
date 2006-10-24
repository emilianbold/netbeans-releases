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

import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

public class MoveClassUI implements RefactoringUI, RefactoringUIBypass {
    
    private DataObject javaObject;    
    private FileObject resource;
    private TypeElement mainSourceClass;
    private MoveClassPanel panel;
    private MoveRefactoring refactoring;
    private String targetPkgName = "";
    private boolean disable;
    private TypeElement clazz = null;
    private FileObject targetFolder;
    private PasteType pasteType;
    
    static final String getString(String key) {
        return NbBundle.getMessage(MoveClassUI.class, key);
    }
    
    public MoveClassUI (TypeElement sourceClass) {
        DataObject ob = null;
        try {
            ob = DataObject.find(resource);
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
        if (ob instanceof DataObject)
            javaObject = (DataObject) ob;
        clazz = sourceClass;
        findMainClass ();
    }
    
    public MoveClassUI (DataObject javaObject) {
        this(javaObject, null, null);
    }
    
    public MoveClassUI (DataObject javaObject, FileObject targetFolder, PasteType pasteType) {
        this.disable = targetFolder != null ;
        this.targetFolder = targetFolder;
        this.javaObject = javaObject;
        this.pasteType = pasteType;
        resource = javaObject.getPrimaryFile();
        findMainClass ();
    }
    
    public String getName() {
        return getString ("LBL_MoveClass");
    }
     
    public String getDescription() {
        if (mainSourceClass == null) {
            return new MessageFormat(getString("DSC_MoveClass")).format(
                    new Object[] {javaObject.getName(), packageName()}
            );
        } else {
            return new MessageFormat(getString("DSC_MoveClass")).format(
                    new Object[] {mainSourceClass.getSimpleName(), packageName()}
            );
        }
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
            
            panel = new MoveClassPanel (parent, pkgName != null ? pkgName: getResPackageName(mainSourceClass), 
                    new MessageFormat(getString("LBL_MoveClassNamed")).format (
                    new Object[] {mainSourceClass==null?"":mainSourceClass.getSimpleName()}
                ),
                targetFolder != null ? targetFolder : (javaObject != null ? javaObject.getPrimaryFile(): null)
            );
            panel.setCombosEnabled(!disable);
        }
        return panel;
    }
    
    private static String getResPackageName(TypeElement cl) {
        if (cl==null)
            return ""; 
        return cl.getQualifiedName().toString();
    }

    private void findMainClass () {
//        mainSourceClass = null;
//        java.util.List list = resource.getClassifiers();
//        int size = list.size ();
//        if (size == 0) {
//            return;
//        }
//        if (size == 1) {
//            mainSourceClass = (JavaClass) list.get (0);
//            return;
//        }
//                
//        String resName = resource.getName();
//        int index_1 = resName.lastIndexOf ('/');
//        int index_2 = resName.indexOf ('.');
//        String mainClassName = null;
//        if (index_2 > -1) {
//            mainClassName = resName.substring (index_1 + 1, index_2);
//        }
//        
//        Iterator iter = list.iterator ();        
//        for (int x = 0; x < size; x++) {
//            JavaClass jc = (JavaClass) iter.next ();
//            if ((mainClassName != null) && mainClassName.equals (jc.getName ())) {
//                mainSourceClass = jc;
//                return;
//            }
//            if ((jc.getModifiers() & Modifier.PUBLIC) > 0)
//                mainSourceClass = jc;
//        }
//        if (mainSourceClass == null) {
//            mainSourceClass = (JavaClass) list.get (0);
//        }        
    }
    
    private String packageName () {
        return targetPkgName.trim().length() == 0 ? getString ("LBL_DefaultPackage") : targetPkgName.trim ();
    }
    
    private Problem setParameters(boolean checkOnly) {
        if (panel==null)
            return null;
        targetPkgName = panel.getPackageName ();

        URL url = URLMapper.findURL(panel.getRootFolder(), URLMapper.EXTERNAL);
        try {
            refactoring.setTarget(new URL(url.toExternalForm() + "/" + panel.getPackageName().replace('.','/')));
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
            List list = new LinkedList();
            list.add(resource);
            if (clazz != null) {
                refactoring = new MoveRefactoring (new Object[]{clazz});
            } else {
                refactoring = new MoveRefactoring (list.toArray());
            }
        }
        return refactoring;
    }
    
    public boolean hasParameters() {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(MoveClassUI.class);
    }

    public boolean isRefactoringBypassRequired() {
        return !panel.isUpdateReferences();
    }
    public void doRefactoringBypass() throws IOException {
        pasteType.paste();
    }
}
