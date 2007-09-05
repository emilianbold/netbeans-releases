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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.highlight.error.includes;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class ErrorIncludeDialog extends JPanel implements CsmModelListener {
    private static final boolean TRACE_ERROR_STATISTIC = Boolean.getBoolean("cnd.highlight.trace.statistic"); // NOI18N
    private CsmProject baseProject;
    private Dialog parent;

    public ErrorIncludeDialog(Set<CsmFile> files) {
        List<CsmInclude> includes = new ArrayList<CsmInclude>();
        for(CsmFile file:files){
            boolean hasFailed = false;
            for(CsmInclude incl : file.getIncludes()){
                if (incl.getIncludeFile() == null){
                    includes.add(incl);
                    hasFailed = true;
                }
            }
            if (baseProject == null){
                baseProject = file.getProject();
            }
            if (!hasFailed && TRACE_ERROR_STATISTIC) {
                System.out.println("File marked as failed does not contain failed directives:"); // NOI18N
                System.out.println("  "+file.getAbsolutePath()); // NOI18N
            }
        }
        if (baseProject != null && TRACE_ERROR_STATISTIC) {
            checkHighlightModel(files);
        }

        createComponents(includes);
        setPreferredSize(new Dimension(500, 240));
        setMinimumSize(new Dimension(320, 240));
        addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                if (e.getChangeFlags() == HierarchyEvent.SHOWING_CHANGED) {
                    if (!e.getChanged().isVisible()){
                        leftList.setModel(new DefaultListModel());
                        rightList.setModel(new DefaultListModel());
                        model = null;
                        baseProject = null;
                        parent = null;
                        if (searchBase != null) {
                            searchBase.clear();
                        }
                        CsmModelAccessor.getModel().removeModelListener(ErrorIncludeDialog.this);
                    }
                }
            }
        });
    }

    public void projectOpened(CsmProject project) {
    }

    public void projectClosed(CsmProject project) {
        if (project == baseProject) {
            if (parent !=  null) {
                parent.setVisible(false);
            }
        }
    }

    public void modelChanged(CsmChangeEvent e) {
    }
    
    public static void showErrorIncludeDialog(Set<CsmFile> files) {
        ErrorIncludeDialog errors = new ErrorIncludeDialog(files);
        DialogDescriptor descriptor = new DialogDescriptor(errors, i18n("ErrorIncludeDialog_Title"), // NOI18N
                false, new Object[]{DialogDescriptor.CLOSED_OPTION}, DialogDescriptor.CLOSED_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setVisible(true);
        errors.parent = dlg;
        CsmModelAccessor.getModel().addModelListener(errors);
    }
    
    private void createComponents(List<CsmInclude> includes) {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(createIncludesPane(), c);
        getAccessibleContext().setAccessibleName(i18n("ErrorIncludeDialog_AccessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(i18n("ErrorIncludeDialog_AccessibleDescription")); // NOI18N
        model = new ErrorIncludesModel(includes);
        leftList.setModel(model);
        addListeners();
        if (TRACE_ERROR_STATISTIC) {
             printStatistic();
        }
    }

    private void checkHighlightModel(Set<CsmFile> files){
        for(Object f : baseProject.getSourceFiles()){
            CsmFile file = (CsmFile) f;
            boolean failed = false;
            for (CsmInclude directive : file.getIncludes()){
                if (directive.getIncludeFile()==null){
                    failed = true;
                }
            }
            if (failed){
                if (!files.contains(file)) {
                    System.out.println("Project source file is failed and not found in highlight"); // NOI18N
                    System.out.println("  "+file.getAbsolutePath()); // NOI18N
                }
            } else {
                if (files.contains(file)) {
                    System.out.println("Project source file is not failed and found in highlight"); // NOI18N
                    System.out.println("  "+file.getAbsolutePath()); // NOI18N
                }
            }
        }
        for(Object f :baseProject.getHeaderFiles()){
            CsmFile file = (CsmFile) f;
            boolean failed = false;
            for (CsmInclude directive : file.getIncludes()){
                if (directive.getIncludeFile()==null){
                    failed = true;
                }
            }
            if (failed){
                if (!files.contains(file)) {
                    System.out.println("Project header file is failed and not found in highlight"); // NOI18N
                    System.out.println("  "+file.getAbsolutePath()); // NOI18N
                }
            } else {
                if (files.contains(file)) {
                    System.out.println("Project header file is not failed and found in highlight"); // NOI18N
                    System.out.println("  "+file.getAbsolutePath()); // NOI18N
                }
            }
        }
    }
    
    private void printStatistic(){
        if (baseProject != null){
            int files = 0;
            int directives = 0;
            int failedDirectives = 0;
            int failedFiles = 0;
            for(Object f : baseProject.getSourceFiles()){
                CsmFile file = (CsmFile) f;
                files++;
                boolean failed = false;
                for (CsmInclude directive : file.getIncludes()){
                    if (directive.getIncludeFile()==null){
                        failedDirectives++;
                        failed = true;
                    }
                    directives++;
                }
                if (failed){
                    failedFiles++;
                }
            }
            for(Object f :baseProject.getHeaderFiles()){
                CsmFile file = (CsmFile) f;
                files++;
                boolean failed = false;
                for (CsmInclude directive : file.getIncludes()){
                    if (directive.getIncludeFile()==null){
                        failedDirectives++;
                        failed = true;
                    }
                    directives++;
                }
                if (failed){
                    failedFiles++;
                }
            }
            System.out.println("*Model #includes statistic*"); // NOI18N
            System.out.println("  Amount of #includes:"+directives); // NOI18N
            System.out.println("  Failed    #includes:"+failedDirectives); // NOI18N
            System.out.println("  Amount     of files:"+files); // NOI18N
            System.out.println("  Failed        files:"+failedFiles); // NOI18N
            if (directives>0) {
                double metric = 100.0 * (directives-failedDirectives) / directives;
                System.out.println("  Resolve #include Accuracy:"+metric+"%"); // NOI18N
                metric = 100.0 * (files-failedFiles) / files;
                System.out.println("  File-based       Accuracy:"+metric+"%"); // NOI18N
            }
            Object o = baseProject.getPlatformProject();
            if (o instanceof NativeProject){
                NativeProject nativeProject = (NativeProject) o;
                files = nativeProject.getAllHeaderFiles().size()+nativeProject.getAllSourceFiles().size();
                System.out.println("*Details for project statistic*"); // NOI18N
                System.out.println("  Amount of native project files:"+files); // NOI18N
                System.out.println("  Failed   highlight   #includes:"+model.getFailedIncludesSize()); // NOI18N
                System.out.println("  Failed   highlight      files:"+model.getFailedFilesSize()); // NOI18N
            }
        }
    }
    
    private ErrorIncludesModel model;
    private JList leftList;
    private JList rightList;
    private JTextArea guessList;
    private Map<String, List<String>> searchBase;
    private JComponent createIncludesPane(/*List<CsmInclude> includes*/) {
        leftList = new JList();
        leftList.setBorder(BorderFactory.createEmptyBorder());
        leftList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        rightList = new JList();
        rightList.setBorder(BorderFactory.createEmptyBorder());
        rightList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        guessList = new JTextArea();
        guessList.setEditable(false);
        
        JSplitPane pane = new JSplitPane();
        pane.setDividerLocation(0.35);
        pane.setResizeWeight(0.35);
        pane.setOneTouchExpandable(true);
        
        JPanel p;
        JLabel l;
        GridBagConstraints c;
        
        JScrollPane leftScroller = new JScrollPane(leftList);
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        l = new JLabel();
        l.setLabelFor(leftList);
        Mnemonics.setLocalizedText(l, i18n("ErrorIncludeDialog_TitleInclides")); // NOI18N
        l.setToolTipText(i18n("ErrorIncludeDialog_AccessibleNameInclides")); // NOI18N
        l.getAccessibleContext().setAccessibleName(i18n("ErrorIncludeDialog_AccessibleNameInclides")); // NOI18N
        l.getAccessibleContext().setAccessibleDescription(i18n("ErrorIncludeDialog_AccessibleDescriptionInclides")); // NOI18N
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.insets = new Insets(5, 6, 5, 5);
        p.add(l, c);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(1, 6, 5, 5);
        p.add(leftScroller, c);
        pane.setLeftComponent(p);
        
        JSplitPane vertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        vertical.setDividerLocation(0.65);
        vertical.setResizeWeight(0.65);
        vertical.setOneTouchExpandable(true);
        
        JScrollPane rightTopScroller = new JScrollPane(rightList);
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        l = new JLabel();
        l.setLabelFor(rightList);
        Mnemonics.setLocalizedText(l, i18n("ErrorIncludeDialog_TitleFiles"));// NOI18N
        l.setToolTipText(i18n("ErrorIncludeDialog_AccessibleNameFiles")); // NOI18N
        l.getAccessibleContext().setAccessibleName(i18n("ErrorIncludeDialog_AccessibleNameFiles")); // NOI18N
        l.getAccessibleContext().setAccessibleDescription(i18n("ErrorIncludeDialog_AccessibleDescriptionFiles")); // NOI18N
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.insets = new Insets(5, 6, 5, 5);
        p.add(l, c);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(1, 6, 5, 5);
        p.add(rightTopScroller, c);
        vertical.setTopComponent(p);
        
        JScrollPane rightBottomScroller = new JScrollPane(guessList);
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        l = new JLabel();
        l.setLabelFor(guessList);
        Mnemonics.setLocalizedText(l, i18n("ErrorIncludeDialog_TitleGuess"));// NOI18N
        l.setToolTipText(i18n("ErrorIncludeDialog_AccessibleNameGuess")); // NOI18N
        l.getAccessibleContext().setAccessibleName(i18n("ErrorIncludeDialog_AccessibleNameGuess")); // NOI18N
        l.getAccessibleContext().setAccessibleDescription(i18n("ErrorIncludeDialog_AccessibleDescriptionGuess")); // NOI18N
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.insets = new Insets(5, 6, 5, 5);
        p.add(l, c);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(1, 6, 5, 5);
        p.add(rightBottomScroller, c);
        vertical.setBottomComponent(p);
        
        pane.setRightComponent(vertical);
        
        return pane;
    }
    
    private void addListeners(){
        leftList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selected = leftList.getSelectedIndex();
                    if (selected >=0){
                        List<CsmInclude> files;
                        if (baseProject != null && baseProject.isValid()) {
                            files = model.getElementList(selected);
                        } else {
                            files = Collections.<CsmInclude>emptyList();
                        }
                        ErrorFilesModel m = new ErrorFilesModel(files);
                        rightList.setModel(m);
                        if (files.size()>0) {
                            rightList.setSelectedIndex(0);
                            rightList.invalidate();
                            rightList.repaint();
                            CsmInclude incl = m.getElementInclude(0);
                            guess(incl, (String)model.getElementAt(selected));
                        } else {
                            guessList.setText("");
                        }
                    }
                }
            }
        });
        leftList.setSelectedIndex(0);
        
        rightList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selected = rightList.getSelectedIndex();
                    if (selected >=0 && baseProject != null && baseProject.isValid()){
                        ErrorFilesModel m = (ErrorFilesModel)rightList.getModel();
                        CsmInclude incl = m.getElementInclude(selected);
                        guess(incl, (String)model.getElementAt(leftList.getSelectedIndex()));
                    } else {
                        guessList.setText("");
                    }
                }
            }
        });
        
        leftList.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                int selected = rightList.getSelectedIndex();
                if (e.getClickCount()==2 && selected >= 0){
                    openElement(selected);
                }
            }
        });
        
        leftList.addKeyListener(new KeyListener(){
            public void keyTyped(KeyEvent e) {
            }
            public void keyPressed(KeyEvent e) {
            }
            public void keyReleased(KeyEvent e) {
                int selected = rightList.getSelectedIndex();
                if (e.getModifiers()==0 && e.getKeyCode()==KeyEvent.VK_SPACE && selected >= 0){
                    openElement(selected);
                }
            }
        });
        
        rightList.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                int selected = rightList.getSelectedIndex();
                if (e.getClickCount()==2 && selected >= 0){
                    openElement(selected);
                }
            }
        });
        
        rightList.addKeyListener(new KeyListener(){
            public void keyTyped(KeyEvent e) {
            }
            public void keyPressed(KeyEvent e) {
            }
            public void keyReleased(KeyEvent e) {
                int selected = rightList.getSelectedIndex();
                if (e.getModifiers()==0 && e.getKeyCode()==KeyEvent.VK_SPACE && selected >= 0){
                    openElement(selected);
                }
            }
        });
    }
    
    private void openElement(int selected){
        if (baseProject != null && baseProject.isValid()) {
            ErrorFilesModel m = (ErrorFilesModel)rightList.getModel();
            final CsmInclude incl = m.getElementInclude(selected);
            if (CsmKindUtilities.isOffsetable(incl)) {
                CsmUtilities.openSource((CsmOffsetable)incl);
            }
        }
    }
    
    private void guess(CsmInclude incl, String found){
        StringBuilder buf = new StringBuilder();
        if (searchBase == null){
            searchBase = search(incl);
        }
        found = found.replace("<",""); // NOI18N
        found = found.replace(">",""); // NOI18N
        found = found.replace("\"",""); // NOI18N
        found = found.replace("\\","/"); // NOI18N
        if(found.indexOf('/')>=0){
            found = found.substring(found.lastIndexOf('/')+1);
        }
        List result = (List)searchBase.get(found);
        if (result != null){
            for (Iterator it = result.iterator(); it.hasNext();) {
                String elem = (String) it.next();
                buf.append(elem+"\n"); // NOI18N
            }
        }
        guessList.setText(buf.toString());
        getObjectFile(found, incl.getContainingFile().getAbsolutePath());
    }
    
    private void getObjectFile(String searchFor, String in){
        String source = in.replace("<",""); // NOI18N
        source = source.replace(">",""); // NOI18N
        source = source.replace("\"",""); // NOI18N
        source = source.replace("\\","/"); // NOI18N
        if(source.indexOf('/')>=0){
            source = source.substring(source.lastIndexOf('/')+1);
        }
        if (source.lastIndexOf('.')>0){
            source = source.substring(0,source.lastIndexOf('.'))+".o";  // NOI18N
            List result = (List)searchBase.get(source);
            if (result != null){
                StringBuilder buf = new StringBuilder();
                for (Iterator it = result.iterator(); it.hasNext();) {
                    String elem = (String) it.next();
                    buf.append(elem+"\n"); // NOI18N
                    String path = trace(searchFor, elem, in);
                    if (path != null){
                        buf.append(path+"\n"); // NOI18N
                    }
                }
                guessList.setText(guessList.getText()+buf.toString());
            }
        }
    }
    
    private String trace(String found, String objFileName, String unit){
        Dwarf dump = null;
        try {
            dump = new Dwarf(objFileName);
            List <CompilationUnit> units = dump.getCompilationUnits();
            if (units.size()>0){
                CompilationUnit cu = units.get(0);
                String fullName = getRightName(cu.getSourceFileAbsolutePath());
                if (unit.equals(fullName)){
                    List<String> includes = cu.getStatementList().getPathsForFile(found);
                    if (includes.size()>0){
                        String path = getRightName(cu.getCompilationDir());
                        String message = i18n("HeaderFromBinary");  // NOI18N
                        return MessageFormat.format(message, new Object[]{
                            path,fullName,includes.get(0)
                        });
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            // Skip exception
        } catch (WrongFileFormatException ex) {
            // Skip exception
        } catch (IOException ex) {
            // Skip exception
            //ex.printStackTrace();
        } catch (Exception ex) {
            // Skip exception
            //ex.printStackTrace();
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        return null;
    }

    private static final String CYG_DRIVE_UNIX = "/cygdrive/"; // NOI18N
    private static final String CYG_DRIVE_WIN = "\\cygdrive\\"; // NOI18N
    private String fixFileName(String fileName) {
        if (fileName != null && Utilities.isWindows()) {
            if (fileName.startsWith(CYG_DRIVE_UNIX)) {
                fileName = fileName.substring(CYG_DRIVE_UNIX.length()); // NOI18N
                fileName = "" + Character.toUpperCase(fileName.charAt(0)) + ':' + fileName.substring(1); // NOI18N
            } else {
                int i = fileName.indexOf(CYG_DRIVE_WIN);
                if (i > 0) {
                    fileName = fileName.substring(i+CYG_DRIVE_UNIX.length());
                    fileName = "" + Character.toUpperCase(fileName.charAt(0)) + ':' + fileName.substring(1); // NOI18N
                }
            }
            fileName = fileName.replace('/', '\\');
        }
        return fileName;
    }
    
    private String getRightName(String fullName){
        File file = new File(fullName);
        fullName = FileUtil.normalizeFile(file).getAbsolutePath();
        fullName = fixFileName(fullName);
        return fullName;
    }

    private Map<String,List<String>> search(CsmInclude include){
        CsmProject prj = include.getContainingFile().getProject();
        HashSet<String> set = new HashSet<String>();
        for (Iterator it = prj.getSourceFiles().iterator(); it.hasNext();){
            CsmFile file = (CsmFile)it.next();
            File f = new File(file.getAbsolutePath());
            set.add(f.getParentFile().getAbsolutePath());
        }
        for (Iterator it = prj.getHeaderFiles().iterator(); it.hasNext();){
            CsmFile file = (CsmFile)it.next();
            File f = new File(file.getAbsolutePath());
            set.add(f.getParentFile().getAbsolutePath());
        }
        ArrayList<String> list = new ArrayList<String>(set);
        for (Iterator<String> it = list.iterator(); it.hasNext();){
            File f = new File(it.next());
            gatherSubFolders(f, set);
        }
        HashMap<String,List<String>> map = new HashMap<String,List<String>>();
        for (Iterator it = set.iterator(); it.hasNext();){
            File d = new File((String)it.next());
            if (d.isDirectory()){
                File[] ff = d.listFiles();
                for (int i = 0; i < ff.length; i++) {
                    if (ff[i].isFile()) {
                        List<String> l = map.get(ff[i].getName());
                        if (l==null){
                            l = new ArrayList<String>();
                            map.put(ff[i].getName(),l);
                        }
                        l.add(ff[i].getAbsolutePath());
                    }
                }
            }
        }
        return map;
    }
    
    private void gatherSubFolders(File d, HashSet<String> set){
        if (d.isDirectory()){
            String path = d.getAbsolutePath();
            if (path.endsWith("/SCCS") || path.endsWith("/CVS")) {  // NOI18N
                return;
            }
            if (!set.contains(path)){
                set.add(d.getAbsolutePath());
                File[] ff = d.listFiles();
                for (int i = 0; i < ff.length; i++) {
                    gatherSubFolders(ff[i], set);
                }
            }
        }
    }
    
    private static String i18n(String id) {
        return NbBundle.getMessage(ErrorIncludeDialog.class,id);
    }

}
