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

import java.io.File;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.junit.wizards.Utils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras
 */
final class OutputUtils {
    
    private OutputUtils() {
    }
    
    /**
     */
    static void openCallstackFrame(Node node,
                                   String frameInfo) {
        Node reportNode = getRootNode(node);
        Report report = ((ReportNode) reportNode).report;
        ClassPath srcClassPath = getSrcClassPath(report.antScript);

        final int[] lineNumStorage = new int[1];
        FileObject file = getFile(frameInfo, lineNumStorage, srcClassPath);
        Utils.openFile(file, lineNumStorage[0]);
    }
        
    /**
     */
    private static Node getRootNode(Node node) {
        Node parentNode = node.getParentNode();
        while (parentNode != null) {
            node = parentNode;
            parentNode = node.getParentNode();
        }
        return node;
    }
    
    /**
     */
    private static ClassPath getSrcClassPath(final File file) {
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
    private static FileObject getFile(final String callstackLine,
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
            ending = ".java";                                           //NOI18N
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

}
