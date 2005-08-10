/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.junit.wizards.Utils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.Line;

/**
 *
 * @author Marian Petras
 */
final class CallstackFrameNode extends AbstractNode {
    
    /** */
    private final String frameInfo;
    
    /** Creates a new instance of CallstackFrameNode */
    public CallstackFrameNode(final String frameInfo) {
        super(Children.LEAF);
        setDisplayName("at " + frameInfo);                              //NOI18N
        setIconBaseWithExtension(
                "org/netbeans/modules/junit/output/res/empty.gif");     //NOI18N

        this.frameInfo = frameInfo;
    }
    
    /**
     */
    public Action getPreferredAction() {
        return new JumpAction();
    }
    
    /**
     *
     */
    private final class JumpAction extends AbstractAction {
        
        /**
         */
        private JumpAction() {
            super();
        };
        
        /**
         */
        public void actionPerformed(ActionEvent e) {
            Report report = getReport();
            ClassPath srcClassPath = getSrcClassPath(report.antScript);
            
            final int[] lineNumStorage = new int[1];
            FileObject file = getFile(frameInfo, lineNumStorage, srcClassPath);
            openFile(file, lineNumStorage[0]);
        }
        
        /**
         */
        private Report getReport() {
            Node node = CallstackFrameNode.this;
            Node parentNode = node.getParentNode();
            while (parentNode != null) {
                node = parentNode;
                parentNode = node.getParentNode();
            }
            return ((ReportNode) node).report;
        }
        
        /**
         */
        private ClassPath getSrcClassPath(final File file) {
            FileObject scriptFileObj = FileUtil.toFileObject(
                                                FileUtil.normalizeFile(file));
            Project project = FileOwnerQuery.getOwner(scriptFileObj);
            SourceGroup[] srcGroups = new Utils(project).getJavaSourceGroups();

            final FileObject[] srcRoots = new FileObject[srcGroups.length];
            for (int i = 0; i < srcRoots.length; i++) {
                srcRoots[i] = srcGroups[i].getRootFolder();
            }

            return ClassPathSupport.createClassPath(srcRoots);
        }
        
        /**
         * Returns FileObject corresponding to the given callstack line.
         *
         * @param  callstackLine  string representation of a callstack window
         *                        returned by the JUnit framework
         */
        private FileObject getFile(final String callstackLine,
                                   final int[] lineNumStorage,
                                   final ClassPath classPath) {

            /* Get the part before brackets (if any brackets present): */
            int bracketIndex = callstackLine.indexOf('(');
            String beforeBrackets = (bracketIndex == -1)
                                    ? callstackLine
                                    : callstackLine.substring(0, bracketIndex)
                                      .trim();
            String inBrackets = (bracketIndex == -1)
                                ? (String) null
                                : callstackLine.substring(
                                        bracketIndex + 1,
                                        callstackLine.lastIndexOf(')'));

            /* Get the method name and the class name: */
            int lastDotIndex = beforeBrackets.lastIndexOf('.');
            String clsName = beforeBrackets.substring(0, lastDotIndex);
            String methodName = beforeBrackets.substring(lastDotIndex + 1);

            /* Get the file name and line number: */
            String fileName = null;
            int lineNum = -1;
            if (inBrackets != null) {
                // RegexpUtils.getInstance() retns instance from ResultPanelTree
                if (RegexpUtils.getInstance().getLocationInFilePattern()
                        .matcher(inBrackets).matches()) {
                    int ddotIndex = inBrackets.lastIndexOf(':'); //srch from end
                    if (ddotIndex == -1) {
                        fileName = inBrackets;
                    } else {
                        fileName = inBrackets.substring(0, ddotIndex);
                        try {
                            lineNum = Integer.parseInt(
                                           inBrackets.substring(ddotIndex + 1));
                            if (lineNum <= 0) {
                                lineNum = 1;
                            }
                        } catch (NumberFormatException ex) {
                            /* should never happen as it passed the regexp */
                            assert false;
                        }
                    }
                }
            }

            /* Find the file: */
            FileObject file;
            String thePath;

            //PENDING - Once 'thePath' is found for a given <clsName, fileName>
            //          pair, it could be cached for further uses
            //          (during a single AntSession).

            String clsNameSlash = clsName.replace('.', '/');
            String slashName, ending;
            int lastSlashIndex;

            if (fileName == null) {
                lastSlashIndex = clsNameSlash.length();
                slashName = clsNameSlash;
                ending = ".java";                                       //NOI18N
            } else {
                lastSlashIndex = clsNameSlash.lastIndexOf('/');
                slashName = (lastSlashIndex != -1)
                            ? clsNameSlash.substring(0, lastSlashIndex)
                            : clsNameSlash;
                ending = '/' + fileName;
            }
            file = classPath.findResource(thePath = (slashName + ending));
            while ((file == null) && (lastSlashIndex != -1)) {
                slashName = slashName.substring(0, lastSlashIndex);
                file = classPath.findResource(thePath = (slashName + ending));
                if (file == null) {
                    lastSlashIndex = slashName.lastIndexOf(
                                                    '/', lastSlashIndex - 1);
                }
            }
            if ((file == null) && (fileName != null)) {
                file = classPath.findResource(thePath = fileName);
            }

            /* Return the file (or null if no matching file was found): */
            if (file == null) {
                lineNum = -1;
            }
            lineNumStorage[0] = lineNum;
            return file;
        }
        
        /**
         */
        private void openFile(FileObject file, int lineNum) {
            
            /*
             * Most of the following code was copied from the Ant module, method
             * org.apache.tools.ant.module.run.Hyperlink.outputLineAction(...).
             */
            
            if (file == null) {
                java.awt.Toolkit.getDefaultToolkit().beep();
                return;
            }
            
            try {
                DataObject dob = DataObject.find(file);
                EditorCookie ed = (EditorCookie)
                                  dob.getCookie(EditorCookie.class);
                if (ed != null && /* not true e.g. for *_ja.properties */
                                  file == dob.getPrimaryFile()) {
                    if (lineNum == -1) {
                        // OK, just open it.
                        ed.open();
                    } else {
                        ed.openDocument();//XXX getLineSet doesn't do it for you
                        try {
                            Line l = ed.getLineSet().getOriginal(lineNum - 1);
                            if (!l.isDeleted()) {
                                l.show(Line.SHOW_GOTO);
                            }
                        } catch (IndexOutOfBoundsException ioobe) {
                            // Probably harmless. Bogus line number.
                            ed.open();
                        }
                    }
                } else {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                }
            } catch (DataObjectNotFoundException ex1) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, ex1);
            } catch (IOException ex2) {
                // XXX see above, should not be necessary to call openDocument
                // at all
                ErrorManager.getDefault().notify(ErrorManager.WARNING, ex2);
            }
        }
        
    }
    
}
