/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.core.compatibility.converter;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.modules.bpel.core.compatibility.CheckCompatibilityAction;
import org.netbeans.modules.bpel.core.compatibility.TransformUtil;

/**
 * Implements stand-alone application which allows either to remove obsolete
 * extensions from BPEL file(-s) or convert them to a correct valid forms.
 *
 * @author Alex Petrov
 */
public class ConverterObsoleteBPELExtensions {
    private static final String BPEL_EXT = "bpel"; // NOI18N

    public static void main(String[] args) {
        if ((args == null) || (args.length < 1) ||
            (args[0].equals("?")) ||
            (args[0].equals("-?")) ||
            (args[0].equals("help")) ||
            (args[0].equals("-help"))) {
            displayHelpInfo();
            return;
        }

        ConverterObsoleteBPELExtensions converter =
            new ConverterObsoleteBPELExtensions();

        try {
            List<String> bpelFiles = converter.createBpelFileList(args[0]);

            if (bpelFiles == null) return;

            if (bpelFiles.isEmpty()) {
                System.out.println();
                System.out.println("No BPEL files have been found.");
                System.out.println();
                return;
            }

            FileViewer viewer = new FileViewer(converter, bpelFiles);
            viewer.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> createBpelFileList(String strFileName) throws FileNotFoundException {
        List<String> bpelFiles = new ArrayList<String>();

        File file = new File(strFileName);
        if (! file.exists()) {
            throw new FileNotFoundException("Directory or file [" + strFileName +
                "] does not exist");
        }
        if (file.isFile()) {
            bpelFiles.add(file.getAbsolutePath());
        } else if (file.isDirectory()) {
            getBpelFilesFromDirectory(bpelFiles, file);
        }

        Collections.sort(bpelFiles);
        displayBpelFiles(bpelFiles);

        return bpelFiles;
    }

    private static void getBpelFilesFromDirectory(List<String> bpelFiles, File file) {
        if (bpelFiles == null) {
            bpelFiles = new ArrayList<String>();
        }
        if (file == null) return;

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (File childFile : children) {
                getBpelFilesFromDirectory(bpelFiles, childFile);
            }
        } else if (isBPELFile(file)) {
            bpelFiles.add(file.getAbsolutePath());
        }
    }

    private static boolean isBPELFile(File file) {
        if ((file == null) || (! file.isFile())) return false;
        
        return file.getAbsolutePath().endsWith("." + BPEL_EXT);
    }

    private static void displayBpelFiles(List<String> bpelFiles) {
        if ((bpelFiles == null) || (bpelFiles.isEmpty())) return;

        System.out.println();
        System.out.println("List of BPEL files:");
        System.out.println("===================");
        for (String filePath : bpelFiles) {
            System.out.println("    " + filePath);
        }
        System.out.println();
    }

    private static void displayHelpInfo() {
        System.out.println();
        System.out.println("\n" +
            "Usage: \n" +
            "java -cp org-netbeans-modules-bpel-core.jar" + File.pathSeparator + "\n" +
            "         ../../ide12/modules/org-netbeans-api-xml.jar" + File.pathSeparator + "\n" +
            "         ../../platform11/modules/org-openide-nodes.jar" + File.pathSeparator + "\n" +
            "         ../../platform11/lib/org-openide-util.jar " + "\n" +
            "org.netbeans.modules.bpel.core.compatibility.converter.ConverterObsoleteBPELExtensions <BPEL file> or <BPEL folder>" +
            "\n\n" +
            "Examples: java ... ConverterObsoleteBPELExtensions MyBPELProcess.bpel\n" +
            "          java ... ConverterObsoleteBPELExtensions /MyProjects/MyBPEL/MyBPELProcess.bpel\n" +
            "          java ... ConverterObsoleteBPELExtensions /MyProjects/MyBPEL\n" +
            "          java ... ConverterObsoleteBPELExtensions /MyProjects\n" +
            "-? -help  print this help message"
        );
        System.out.println();
    }
}

class FileViewer extends WindowAdapter {
    private static final double MAIN_APP_FRAME_SIZE_PERCENT = 0.8;

    private ConverterObsoleteBPELExtensions converter;
    private List<String> bpelFiles;
    private JFrame mainFrame;
    private JTree treeFile;
    private JTextArea statusArea;
    private JButton btnConvertBpelFile, btnRemoveObsoleteExt;

    public FileViewer(ConverterObsoleteBPELExtensions converter, List<String> bpelFiles) {
        this.converter = converter;
        this.bpelFiles = bpelFiles;
    }

    public void show() {
        if ((mainFrame != null) && (mainFrame.isVisible())) return;

        mainFrame = new JFrame("Viewer of BPEL Files");
        mainFrame.addWindowListener(this);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setMainFrameSizeLocation();
        addMainFrameComponents();

        treeFile.clearSelection();

        btnConvertBpelFile.setEnabled(false);
        ((AbstractConverterAction) btnConvertBpelFile.getAction()).setTreeFile(treeFile);
        ((AbstractConverterAction) btnConvertBpelFile.getAction()).setStatusArea(statusArea);

        btnRemoveObsoleteExt.setEnabled(false);
        ((AbstractConverterAction) btnRemoveObsoleteExt.getAction()).setTreeFile(treeFile);
        ((AbstractConverterAction) btnRemoveObsoleteExt.getAction()).setStatusArea(statusArea);

        statusArea.setText("");

        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private void addMainFrameComponents() {
        Container contentPane = mainFrame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        contentPane.add(Box.createVerticalStrut(3));
        contentPane.add(getTreeFilePane());

        contentPane.add(Box.createVerticalStrut(5));
        contentPane.add(getButtonPane());

        contentPane.add(Box.createVerticalStrut(5));
        contentPane.add(getStatusPane());
    }

    private JPanel getTreeFilePane() {
        treeFile = new JTree(new FileNode("/"));
        makeFileTree();

        final DefaultTreeSelectionModel selectionModel =
            (DefaultTreeSelectionModel) treeFile.getSelectionModel();
        selectionModel.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                int selectedNodeCount = treeFile.getSelectionCount();

                TreePath[] selectedPaths = selectionModel.getSelectionPaths();
                if (selectedPaths != null) {
                    for (TreePath treePath : selectedPaths) {
                        FileNode fileNode = (FileNode) treePath.getLastPathComponent();
                        if ((fileNode != null) && (fileNode.getAllowsChildren())) {
                            selectionModel.removeSelectionPath(treePath);
                            --selectedNodeCount;
                        }
                    }
                }
                btnConvertBpelFile.setEnabled(selectedNodeCount > 0);
                btnRemoveObsoleteExt.setEnabled(selectedNodeCount > 0);
            }
        });

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.setBorder(new TitledBorder("BPEL Files"));

        JScrollPane scrollPane = new JScrollPane(treeFile,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        pane.add(scrollPane);

        return pane;
    }

    private void makeFileTree() {
        DefaultTreeModel treeModel = (DefaultTreeModel) treeFile.getModel();
        FileNode rootNode = (FileNode) treeModel.getRoot();

        String commonDirPath = getShortestCommonDirectoryPath(bpelFiles);

        rootNode.setUserObject(commonDirPath == null ? "/" : commonDirPath);
        rootNode.setNodeName(commonDirPath);
        treeModel.nodeChanged(rootNode);

        for (String bpelFilePath : bpelFiles) {
            addFileNode(treeModel, rootNode, bpelFilePath);
        }

        Util.expandAllTreeNodes(treeFile, rootNode);
        treeFile.setRootVisible(bpelFiles.size() > 1);
    }

    private void addFileNode(DefaultTreeModel treeModel,
        FileNode parentNode, String bpelFilePath) {
        if ((parentNode == null) || (bpelFilePath == null) || (bpelFilePath.length() < 1)) {
            return;
        }

        String parentPath = (String) parentNode.getUserObject();
        if ((parentPath == null) || (parentPath.length() < 1)) return;

        parentPath = parentPath.replace("\\", "/");
        bpelFilePath = bpelFilePath.replace("\\", "/");

        if (! bpelFilePath.startsWith(parentPath)) return;

        int startParsingPos = parentPath.length();
        if (bpelFilePath.charAt(startParsingPos) == '/') ++startParsingPos;

        int endParsingPos = bpelFilePath.indexOf("/", startParsingPos);
        if (endParsingPos < 0) {
            endParsingPos = bpelFilePath.length();
        }

        //System.out.println("bpelFilePath = " + bpelFilePath);
        //System.out.println("parentPath = " + parentPath);
        //System.out.println("startParsingPos = " + startParsingPos);
        //System.out.println("endParsingPos = " + endParsingPos);

        String nodeFilePath = bpelFilePath.substring(0, endParsingPos),
               nodeName = bpelFilePath.substring(startParsingPos, endParsingPos);

        //System.out.println("nodeFilePath = " + nodeFilePath);
        //System.out.println("nodeName = " + nodeName);

        FileNode fileNode = parentNode.getChildByUserObject(nodeFilePath);
        if (fileNode == null) {
            fileNode = new FileNode(nodeFilePath);
            fileNode.setNodeName(nodeName);

            parentNode.add(fileNode);
            treeModel.nodesWereInserted(parentNode, new int[] {parentNode.getChildCount() - 1});
        }

        if (! nodeFilePath.equals(bpelFilePath)) {
            addFileNode(treeModel, fileNode, bpelFilePath);
        }
    }

    private String getShortestCommonDirectoryPath(List<String> bpelFiles) {
        SortedSet<String> dirPaths = new TreeSet<String>();
        for (String bpelFilePath : bpelFiles) {
            String dirPath = Util.getDirectoryName(bpelFilePath);
            dirPaths.add(dirPath);
        }
        if (dirPaths.isEmpty()) return null;

        List<String> paths = new ArrayList<String>(dirPaths);
        String shortestPath = paths.get(0);
        for (String path : paths) {
            if (path.length() < shortestPath.length()) {
                shortestPath = path;
            }
        }
        return shortestPath;
    }

    private JPanel getButtonPane() {
        JPanel pane = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));

        btnConvertBpelFile = new JButton(new ConvertObsoleteExtensionsAction());
        btnRemoveObsoleteExt = new JButton(new RemoveObsoleteExtensionsAction());

        pane.add(btnConvertBpelFile);

        pane.add(Box.createHorizontalStrut(20));
        pane.add(btnRemoveObsoleteExt);

        return pane;
    }

    private JPanel getStatusPane() {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.setBorder(new TitledBorder("Status Area"));

        statusArea = new JTextArea();

        JScrollPane scrollPane = new JScrollPane(statusArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.add(scrollPane);

        return pane;
    }

    private void setMainFrameSizeLocation() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int frameWidth  = (int) (screenSize.width * MAIN_APP_FRAME_SIZE_PERCENT),
            frameHeight = (int) (screenSize.height * MAIN_APP_FRAME_SIZE_PERCENT),
            frameX = (screenSize.width - frameWidth) >> 1,
            frameY = (screenSize.height - frameHeight) >> 1;

        mainFrame.setPreferredSize(new Dimension(frameWidth, frameHeight));
        mainFrame.setLocation(frameX, frameY);
    }
    
    public void exit() {
        WindowEvent
            windowClosingEvent = new WindowEvent(mainFrame,
                WindowEvent.WINDOW_CLOSING),
            windowClosedEvent = new WindowEvent(mainFrame,
                WindowEvent.WINDOW_CLOSED);
        windowClosing(windowClosingEvent);
        windowClosed(windowClosedEvent);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        mainFrame.setVisible(false);
        mainFrame.dispose();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        if (mainFrame.isVisible()) return;

        super.windowClosed(e);
        mainFrame = null;
        
        System.exit(0);
    }
}

class FileNode extends DefaultMutableTreeNode {
    private String nodeName;

    public FileNode(Object userObject) {
        super();
        setUserObject(userObject);
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    private boolean isDirectory() {
        if ((userObject instanceof String) && (((String) userObject).length() > 0)) {
            String filePath = (String) userObject;
            File file = new File(filePath);
            return file.isDirectory();
        }
        return false;
    }

    public FileNode getChildByUserObject(String childUserObject) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            FileNode childNode = (FileNode) getChildAt(i);
            if (childNode == null) continue;

            if ((childNode.userObject == null) && (childUserObject == null)) return childNode;

            if ((childNode.userObject instanceof String) &&
                (childNode.userObject.equals(childUserObject))) {
                return childNode;
            }
        }
        return null;
    }

    @Override
    public void setUserObject(Object userObject) {
        super.setUserObject(userObject);
        if (isDirectory()) { // is a directory
            setAllowsChildren(true);
        } else {
            setAllowsChildren(false);
        }
    }

    @Override
    public boolean isLeaf() {
        return (! getAllowsChildren());
    }

    @Override
    public String toString() {
        if (nodeName != null) return nodeName;
        if (userObject == null) return "";

        String filePath = (String) userObject;
        File file = new File(filePath);
        return (file.isFile() ? file.getName() : filePath);
    }
}

class Util {
    public static String getDirectoryName(String dirPath) {
        if (dirPath == null) {
            return null;
        }
        try {
            dirPath = dirPath.replace("\\", "/");
            
            if (dirPath.equals("/")) return dirPath;

            if (dirPath.endsWith("/")) {
                dirPath = dirPath.substring(0, dirPath.length() - 1);
            }

            int pos = dirPath.lastIndexOf("/");
            if ((pos < 0) || (dirPath.equals("/"))) {
                return dirPath;
            } else {
                return dirPath.substring(0, pos);
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void expandAllTreeNodes(JTree tree, FileNode fileNode) {
        if ((tree == null) || (fileNode == null)) return;

        tree.expandPath(new TreePath(fileNode.getPath()));
        
        int childCount = fileNode.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            FileNode childNode = (FileNode) fileNode.getChildAt(i);
            if (! childNode.isLeaf()) {
                expandAllTreeNodes(tree, childNode);
            }
        }
    }
}

abstract class AbstractConverterAction extends AbstractAction {
    protected String xslTransformationFileName;
    protected JTree treeFile;
    protected JTextArea statusArea;

    public AbstractConverterAction(String name, String xslTransformationFileName) {
        super(name);
        this.xslTransformationFileName = xslTransformationFileName;
    }

    public void actionPerformed(ActionEvent e) {
        if (treeFile == null) return;

        DefaultTreeSelectionModel selectionModel =
            (DefaultTreeSelectionModel) treeFile.getSelectionModel();
        if (selectionModel == null) return;

        TreePath[] treePaths = selectionModel.getSelectionPaths();
        if (treePaths == null) return;

        List<FileNode> handledFileNodes = new ArrayList<FileNode>();
        for (TreePath treePath : treePaths) {
            FileNode fileNode = (FileNode) treePath.getLastPathComponent();
            if ((fileNode == null) || (fileNode.getAllowsChildren()) ||
                (fileNode.getUserObject() == null)) {
                continue;
            }
            try {
                transformBpelFile((String) fileNode.getUserObject());
                treeFile.removeSelectionPath(treePath);
                handledFileNodes.add(fileNode);
            } catch (Throwable ex) {
                ex.printStackTrace();
                addStatusMessage(ex.getClass().getName() + ": " + ex.getMessage());
            }
        }
        
        try {
            treeFile.clearSelection();
            for (FileNode fileNode : handledFileNodes) {
                ((DefaultTreeModel) treeFile.getModel()).removeNodeFromParent(fileNode);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            addStatusMessage(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    private void addStatusMessage(String message) {
        if ((statusArea == null) || (message == null) || (message.length() < 1)) {
            return;
        }
        String currentText = statusArea.getText();
        statusArea.setText(currentText + (currentText.length() < 1 ? "" : "\n") + message);
    }

    private void transformBpelFile(String bpelFilePath) throws Throwable {
        if ((bpelFilePath == null) || (bpelFilePath.length() < 1)) return;

        String backupFilePath = copyBpelFile(bpelFilePath);
        if (backupFilePath != null) {
            convertBpelFile(bpelFilePath, backupFilePath);
        }
    }

    private String copyBpelFile(String filePath) {
        if ((filePath == null) || (filePath.trim().length() < 1)) {
            return null;
        }

        File bpelFile = new File(filePath);
        File backupFile = null;

        if (!bpelFile.exists()) return null;

        int i = 0;
        while (true) {
            backupFile = new File(filePath + "-" + (++i) + ".bak");
            if (! backupFile.exists()) {
                break;
            }
        }

        if (backupFile == null) return null;
        //System.out.println("newFile = [" + newFile.getAbsolutePath() + "]");

        BufferedReader reader = null;
        PrintWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(bpelFile));
            writer = new PrintWriter(backupFile);

            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                writer.println(line);
            }
            return backupFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            addStatusMessage(e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                addStatusMessage(ex.getMessage());
            }

            writer.flush();
            writer.close();
        }
        return null;
    }

    private void convertBpelFile(String originalFilePath, String backupFilePath) throws Throwable {
        if ((xslTransformationFileName == null) || (originalFilePath == null) ||
            (backupFilePath == null)) return;

        InputStream xslFileInputStream = null, bpelInputStream = null;
        OutputStream transformationOutputStream = null;

        try {
            xslFileInputStream = CheckCompatibilityAction.class.getResourceAsStream(
                xslTransformationFileName);
            bpelInputStream = new FileInputStream(backupFilePath);

            transformationOutputStream = new FileOutputStream(originalFilePath);

            StreamSource xslSource = new StreamSource(xslFileInputStream),
                         bpelSource = new StreamSource(bpelInputStream);
            StreamResult transformationStreamResult = new StreamResult(
                transformationOutputStream);

            TransformUtil.transform(bpelSource, null, xslSource, transformationStreamResult,
                null);

            addStatusMessage("The BPEL file [" + originalFilePath + "] has been converted properly.\n" +
                "A new backup copy of this BPEL file was created before conversion: [" + backupFilePath + "].\n");
        } finally {
            if (xslFileInputStream != null) {
                try {xslFileInputStream.close();} catch (Exception e) {e.printStackTrace();}
            }
            if (bpelInputStream != null) {
                try {bpelInputStream.close();} catch (Exception e) {e.printStackTrace();}
            }
            if (transformationOutputStream != null) {
                try {transformationOutputStream.close();} catch (Exception e) {e.printStackTrace();}
            }
        }
    }

    public void setTreeFile(JTree treeFile) {
        this.treeFile = treeFile;
    }

    public void setStatusArea(JTextArea statusArea) {
        this.statusArea = statusArea;
    }
}

class ConvertObsoleteExtensionsAction extends AbstractConverterAction {
    public ConvertObsoleteExtensionsAction() {
        super("Convert selected files",
            CheckCompatibilityAction.XSL_FILE_NAME_BPEL_CONVERSION);
    }

}

class RemoveObsoleteExtensionsAction extends AbstractConverterAction {
    public RemoveObsoleteExtensionsAction() {
        super("Remove obsolete extensions from selected files",
            CheckCompatibilityAction.XSL_FILE_NAME_DELETE_OLD_EXT);
    }
}