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

package  org.netbeans.modules.cnd.editor.parser;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.event.KeyAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.UIManager;

import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.netbeans.modules.cnd.loaders.CppEditorSupport;

import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.explorer.view.NodeListModel;
import org.openide.explorer.view.ChoiceView;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.NodeRenderer;
import org.openide.explorer.view.Visualizer;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.windows.TopComponent;

import org.netbeans.modules.cnd.loaders.FortranDataObject;


/**
 * Loosly based on java/src/org/netbeans/modules/java/ui/NavigationView.java
 */
public class NavigationView extends ChoiceView {
    /**
     * Selection manager for keyboard events.
     */
    SelectionManager        selManager;
    
    /**
     * current source file object and editor support
     */
    private DataObject sourceObject = null;
    private CppEditorSupport cppEditorSupport = null;
    
    /**
     * Index/linenumber table - used to quickly determine context of current cursor position
     */
    ArrayList indexLineNumber = null;

    /**
     * Auto parse timers
     */
    private Timer checkModifiedTimer = null;
    private long lastModified = 0;
    
    private Timer checkCursorTimer = null;
    private int lastCursorPos = -1;
    private int lastCursorPosWhenChecked = 0;

    /**
     * Clusters: value indicate sorting order in drop-down combobox list
     */
    // Common to all language
    private static int CLUSTER_FILE = 0;
    // Fortran
    private static int CLUSTER_FORTRAN_PROGRAM = 1;
    private static int CLUSTER_FORTRAN_LABEL = 2;
    private static int CLUSTER_FORTRAN_TYPES = 3;
    private static int CLUSTER_FORTRAN_OTHER = 4;
    private static int CLUSTER_FORTRAN_VAR = 5;
    private static int CLUSTER_FORTRAN_FUNCSUB = 6;
    private static int CLUSTER_FORTRAN_MODULES = 7;
    private static int CLUSTER_FORTRAN_BLOCK_DATA = 8;
    /**
     * Associated ExplorerManager.
     */
    ExplorerManager manager;
    
    /**
     * TopComponent to listen to, or null if we should listen to
     * TopComponent.Registry.
     */
    TopComponent topComponent = null;
    
    /**
     * Bundle
     */
    static ResourceBundle bundle = null;
    
    /**
     * Constructor
     */
    public NavigationView() {
        initComponents();
    }
    
    /**
     * Initialization
     */
    private void initComponents() {
        ((NodeListModel)getModel()).setDepth(8);
        setKeySelectionManager(selManager = new SelectionManager());
        this.getAccessibleContext().setAccessibleDescription(getString("ACSD_NavigationView"));
        
        // Anyone ever heard of this property ? Me not... until I looked through Swing sources.
        this.putClientProperty("JComboBox.lightweightKeyboardNavigation", "Lightweight"); // NOI18N
        
        if (CppSettings.getDefault().getParsingDelay() > 0) {
            checkModifiedTimer = new Timer(CppSettings.getDefault().getParsingDelay(), new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    checkModified();
                }
            });
            checkCursorTimer = new Timer(250, new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    checkCursor();
                }
            });
        }
	setRenderer(new MyNodeRenderer());
    }

    /**
     * Using own own node renderer to be able to indent nodes under classes and namespaces. Usually it is done
     * automatically based on the node position in the tree, but we use a flat list structure so it has to be done this way
     * FIXUP: perhaps use a real tree structure to avoid this, but it is more exprensive to create ????
     */
    class MyNodeRenderer extends NodeRenderer {
	Border focusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
	Border emptyBorder = new EmptyBorder(1, 1, 1, 1);

	public java.awt.Component getListCellRendererComponent (
	javax.swing.JList list,
	Object value,            // value to display
	int index,               // cell index
	boolean isSelected,      // is the cell selected
	boolean cellHasFocus)    // the list and the cell have the focus
	{
	    javax.swing.JLabel label = (javax.swing.JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    if (index < 0) {
		return label;
	    }
            Border border = (cellHasFocus) ? focusBorder : emptyBorder;
	    ViewNode vn = (ViewNode)Visualizer.findNode(getItemAt(index));
            if (vn.getScopeLevel() > 0) {
                border = new CompoundBorder(new EmptyBorder (0, label.getIcon().getIconWidth() * vn.getScopeLevel(), 0, 0), border);
            }
            label.setBorder(border);
	    return label;
	}
    }
    

    /**
     * Check position of cursor and update combobox selection with current context (nearest name in list)
     */
    private void checkCursor() {
        if (sourceObject == null)
            return;
        if (isPopupVisible())
            return;
        if (CppSettings.getDefault().getParsingDelay() <= 0) 
	    return;
	if (checkCursorTimer != null)
	    checkCursorTimer.stop();
	JEditorPane jEditorPane = findCurrentJEditorPane();
	if (jEditorPane != null) {
            Caret caret = jEditorPane.getCaret();
            if (caret.getDot() != lastCursorPos && caret.getDot() == lastCursorPosWhenChecked) {
                //long before = System.currentTimeMillis();
                lastCursorPos = caret.getDot();
		lastCursorPosWhenChecked = caret.getDot();
                javax.swing.text.Document doc = jEditorPane.getDocument();
                int caretLineNo = 1;
                try {
                    caretLineNo = Utilities.getLineOffset((org.netbeans.editor.BaseDocument)doc, caret.getDot()) + 1;
                }
                catch (Exception e) {
		    // do nothing. Line 1 is the best we can do...
                }
                // Find nearest Line
                int bestIndex = 0;

		int index = Collections.binarySearch(indexLineNumber, new IndexLineNumber(0, caretLineNo), new IndexLineNumberComparator());
		if (index < 0) {
		    // exact line not found, but insersion index (-1) returned instead
		    index = -index-2;
		}

		bestIndex = ((IndexLineNumber)indexLineNumber.get(index)).getIndex();
                setSelectedIndex(bestIndex);
                //long after = System.currentTimeMillis();
                //System.out.println("checkCursor " + sourceObject.getName() + ": " + (after - before) + " msec.");
            }
	    lastCursorPosWhenChecked = caret.getDot();
        }
	if (checkCursorTimer != null)
	    checkCursorTimer.restart();
    }
    
    /**
     * Check if buffer has been modified (since last time it was checked). If modified,
     * save buffer in tmp file and re-parse it.
     */
    private void checkModified() {
        if (sourceObject == null)
            return;
        if (isPopupVisible())
            return;
        stopTimers();
	JEditorPane jEditorPane = findCurrentJEditorPane();
	if (jEditorPane != null) {
            updateNodesIfModified(getCppEditorSupport(), sourceObject, jEditorPane);
        }
        restartTimers();
    }
    
    private void updateNodesIfModified(CppEditorSupport cppEditorSupport, DataObject sourceObject, JEditorPane jEditorPane) {
        File tmpFile = null;
        
        if (cppEditorSupport.getLastModified() <= lastModified) {
            // No need to update
            return;
        }

	long timeSinceLastModification = System.currentTimeMillis() - cppEditorSupport.getLastModified();
	if (timeSinceLastModification < CppSettings.getDefault().getParsingDelay())
	    return;

        lastModified = cppEditorSupport.getLastModified();
        lastCursorPos = -1;
	lastCursorPosWhenChecked = 0;
        
        //long before = System.currentTimeMillis();
        // Create tmp file with current content...
        try {
            tmpFile = File.createTempFile(sourceObject.getPrimaryFile().getName() + "__", "." + sourceObject.getPrimaryFile().getExt()); // NOI18N
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(tmpFile)));
            out.print(jEditorPane.getText());
            out.flush();
            out.close();
        }
        catch (Exception e) {
	    ErrorManager.getDefault().annotate(e, "Problems creating tmp file for " + sourceObject.getPrimaryFile().getPath()); // NOI18N
	    ErrorManager.getDefault().notify(e);
	    tmpFile.delete();
	    return;
        }
        updateChildren(tmpFile.getPath());
        //long after = System.currentTimeMillis();
        //System.out.println("updateNodesIfModified " + sourceObject.getName() + ": " + (after - before) + " msec.");
        //java.awt.Toolkit.getDefaultToolkit().beep();
        tmpFile.delete();
    }
    
    /*
     * Called when view is opened
     */
    public void addNotify() {
        super.addNotify();
        topComponent = findParentTopComponent();
        setRoot(getActivatedNode());
        restartTimers();
    }
    
    /**
     * Called when view is closed
     */
    public void removeNotify() {
        stopTimers();
        super.removeNotify();
    }
    
    /**
     * Restart all timers
     */
    private void restartTimers() {
        if (checkModifiedTimer != null)
            checkModifiedTimer.restart();
        if (checkCursorTimer != null)
            checkCursorTimer.restart();
    }
    
    /**
     * Stop all timers
     */
    private void stopTimers() {
        if (checkModifiedTimer != null)
            checkModifiedTimer.stop();
        if (checkCursorTimer != null)
            checkCursorTimer.stop();
    }
    
    private ExplorerManager getExplorerManager() {
        if (manager == null) {
            manager = ExplorerManager.find(this);
        }
        return manager;
    }
    
    private TopComponent findParentTopComponent() {
        java.awt.Component p;
        
        for (p = getParent(); p != null && !(p instanceof TopComponent); p = p.getParent())
            ;
        return (TopComponent)p;
    }
    
    public void setPopupVisible(boolean show) {
        boolean wasVisible = isPopupVisible() && isVisible();
        super.setPopupVisible(show);
        if (!show && wasVisible) {
            choiceItemSelected(getExplorerManager().getSelectedNodes());
        }
    }
    
    /**
     * Finds activated node
     */
    private Node getActivatedNode() {
        Node[] actNodes;
        
        if (topComponent == null) {
            actNodes = TopComponent.getRegistry().getActivatedNodes();
        } else {
            actNodes = topComponent.getActivatedNodes();
        }
        if (actNodes == null || actNodes.length == 0)
            return null;
        else
            return actNodes[0];
    }
    
    
    /**
     * Sets the root node in the combobox view according to active node
     */
    private void setRoot(Node activeNode) {
        if (activeNode == null) {
            return;
        }
        
        DataObject dataObject = activeNode.getCookie(DataObject.class);
        if (dataObject == null)
            return; // Should not happen...
        if (!(dataObject instanceof FortranDataObject)) {
            return; // Should not happen...
        }
        
        if (sourceObject == null || sourceObject != dataObject) {
            sourceObject = dataObject;
	    cppEditorSupport = null;
            File file = FileUtil.toFile(sourceObject.getPrimaryFile());
            if (file != null) {
                updateChildren(file.getPath());
            } else {
                sourceObject = null;
            }
        }
    }
    
    /**
     * Removes 'old' children, reparse file, and inserts 'new' children in root node
     */
    private void updateChildren(String sname) {
        // Remove 'old' nodes
	removeComboboxChildren();
        
        // parse file using ctags and create new nodes...
	MyCtagsTokenListener myCtagsTokenListener = new MyCtagsTokenListener();
        if (CppSettings.getDefault().getParsingDelay() > 0) {
	    CtagsParser ctagsParser = new CtagsParser(sname);
	    ctagsParser.setCtagsTokenListener(myCtagsTokenListener);
	    
	    try {
		ctagsParser.parse();
	    }
	    catch (Exception e) {
		ErrorManager.getDefault().notify(e);
		// OK to continue...
	    }
	}
	addComboboxChildren(myCtagsTokenListener.getNodes());
	indexLineNumber = myCtagsTokenListener.getIndexLineNumbers();
    }
    
    /**
     * Implements combobox list with index and source file line number.
     * Also implements Comparator to be used when sorting and binary search lookup
     */
    class IndexLineNumber {
	private int index;
	private int lineNumber;

	public IndexLineNumber(int index, int lineNumber) {
	    this.index = index;
	    this.lineNumber = lineNumber;
	}

	public int getIndex() {
	    return index;
	}

	public int getLineNumber() {
	    return lineNumber;
	}
    }

    class IndexLineNumberComparator implements Comparator {
	public int compare(Object o1, Object o2) {
	    IndexLineNumber iln1 = (IndexLineNumber)o1;
	    IndexLineNumber iln2 = (IndexLineNumber)o2;

	    if (iln1.getLineNumber() < iln2.getLineNumber())
		return -1;
	    else if (iln1.getLineNumber() > iln2.getLineNumber())
		return 1;
	    else
		return 0;
	}
    }

    class NodesComparator implements Comparator {
	public int compare(Object o1, Object o2) {
	    ViewNode iln1 = (ViewNode)o1;
	    ViewNode iln2 = (ViewNode)o2;

	    return iln1.getSortName().compareTo(iln2.getSortName());
	}
    }


    /**
     * Listener that will receive tokens (name/linenumber pairs) from parser
     */
    class MyCtagsTokenListener implements CtagsTokenListener {
        private ArrayList nodes = null;
	private ArrayList lineNumberIndex = null;
	//private int index = 0;

        MyCtagsTokenListener() {
            nodes = new ArrayList(5);
            nodes.add(new SourceFileNode(sourceObject, getString("NAME_StartOfFile"), 1, ' ', null, 0, CLUSTER_FILE));
            //lineNumberIndex = new ArrayList(5);
	    //lineNumberIndex.add(new IndexLineNumber(index++, 1));
        }

	private int findFortranScopeCluster(String scope, int scopeKind) {
	    int cluster = 0;
	    if (scope != null) {
		if (scopeKind == CtagsTokenEvent.SCOPE_MODULE)
		    cluster = CLUSTER_FORTRAN_MODULES;
		else if (scopeKind == CtagsTokenEvent.SCOPE_TYPE)
		    cluster = CLUSTER_FORTRAN_TYPES;
		else if (scopeKind == CtagsTokenEvent.SCOPE_SUBROUTINE)
		    cluster = CLUSTER_FORTRAN_FUNCSUB;
		else if (scopeKind == CtagsTokenEvent.SCOPE_BLOCK_DATA)
		    cluster = CLUSTER_FORTRAN_BLOCK_DATA;
		else {
		    // Error
		    System.err.println("Illegal scopeKind " + scopeKind); // NOI18N
		}
	    }
	    return cluster;
	}
        
	private Vector scopeList = new Vector(0);
	private boolean checkInScopeList(String scope) {
	    if (scope != null) {
		for (Enumeration e = scopeList.elements(); e.hasMoreElements(); ) {
		    String s = ((String)e.nextElement());
		    if (s.equals(scope))
			return true;
		}
	    }
	    return false;
	}

        public void gotToken(CtagsTokenEvent ctagsTokenEvent) {
	    ViewNode node;

	    String name = ctagsTokenEvent.getToken();
	    int lineno = ctagsTokenEvent.getLineNo();
	    char kind = ctagsTokenEvent.getKind();
	    String scope = ctagsTokenEvent.getScope();
	    int scopeKind = ctagsTokenEvent.getScopeKind();

	    if (sourceObject instanceof FortranDataObject) {
		int scopeCluster = findFortranScopeCluster(scope, scopeKind);
		switch (ctagsTokenEvent.getKind()) {
		case 'l':   // labels
		    node = new LabelNode(sourceObject, name, lineno, kind, scope, scopeCluster,  CLUSTER_FORTRAN_LABEL);
		    break;
		case 'f':   // functions
		case 's':   // subroutines
		    node = new FuncSubNode(sourceObject, name, lineno, kind, scope, scopeCluster,  CLUSTER_FORTRAN_FUNCSUB);
		    break;
		case 'v':   // module variables
		case 'L':   // local and common block variables
		    node = new VarNode(sourceObject, name, lineno, kind, scope, scopeCluster,  CLUSTER_FORTRAN_VAR);
		    break;
		case 'm':   // modules
		    node = new ModulesNode(sourceObject, name, lineno, kind, scope, scopeCluster, CLUSTER_FORTRAN_MODULES);
		    break;
		case 't':   // derived types
		    node = new TypesNode(sourceObject, name, lineno, kind, scope, scopeCluster,  CLUSTER_FORTRAN_TYPES);
		    break;
		case 'p':   // programs
		    node = new ProgramNode(sourceObject, name, lineno, kind, scope, scopeCluster,  CLUSTER_FORTRAN_PROGRAM);
		    break;
		case 'b':   // block data
		    node = new BlockDataNode(sourceObject, name, lineno, kind, scope, scopeCluster,  CLUSTER_FORTRAN_BLOCK_DATA);
		    break;
		case 'c':   // common blocks
		case 'e':   // entry points
		case 'i':   // interfaces
		case 'k':   // type components
		case 'n':   // namelists
		default:
		    node = new OtherNode(sourceObject, name, lineno, kind, scope, scopeCluster, CLUSTER_FORTRAN_OTHER);
		    break;
		}
		nodes.add(node);
	    }
        }
        
        ViewNode[] getNodes() {
	    Collections.sort(nodes, new NodesComparator());
	    return (ViewNode[])nodes.toArray(new ViewNode[nodes.size()]);
        }

	private ArrayList getIndexLineNumbers() {
            lineNumberIndex = new ArrayList(5);
	    for (int i = 0; i < nodes.size(); i++) {
		lineNumberIndex.add(new IndexLineNumber(i, ((ViewNode)nodes.get(i)).getLineNo()));
	    }
	    Collections.sort(lineNumberIndex, new IndexLineNumberComparator());
	    return lineNumberIndex;
	}
    }
    
    /**
     * Called when item selected in combo box
     */
    private void choiceItemSelected(Node[] sel) {
        if (sel == null || sel.length == 0)
            sel = getExplorerManager().getSelectedNodes();
        // Do nothing if nothing is selected (it is possible ?)
        if (sel == null || sel.length == 0)
            return;
        
        Node n = sel[0];
        if (n instanceof ViewNode) {
            ViewNode sen = (ViewNode)n;
            //sen.goToLine(); // This go to correct line, but does it first pane!!!

	    // Find actual pane and do it there...
	    JEditorPane currentJEditorPane = findCurrentJEditorPane();
	    if (currentJEditorPane != null) {
		sen.goToOffset(currentJEditorPane);
	    }
        }
    }

    /**
     * Find the active (Current) editor pane. There is more than one pane if the view is cloned.
     */
    private JEditorPane findCurrentJEditorPane() {
	JEditorPane currentJEditorPane = null;
	JEditorPane[] jEditorPanes = getCppEditorSupport().getOpenedPanes();
	if (jEditorPanes == null)
	    return null;
	if (jEditorPanes.length == 1) { // FIXUP: sometimes NPE here when starting ide and file already open
	    currentJEditorPane = jEditorPanes[0];
	}
	else {
	    if (jEditorPanes != null && topComponent != null) {
		for (int i = 0; i < jEditorPanes.length; i++) {
		    if (topComponent.isAncestorOf(jEditorPanes[i])) {
			currentJEditorPane = jEditorPanes[i];
			break;
		    }
		}
	    }
	}
	return currentJEditorPane;
    }
    
    /**
     * Implements keyboard navigation in combo box drop down list
     */
    private class SelectionManager extends KeyAdapter implements JComboBox.KeySelectionManager {
        public int selectionForKey(char key, ComboBoxModel comboBoxModel) {
            Node[] children = getComboboxChildren();
            boolean gotOne = false;
            int index = 0;
            if (getSelectedIndex() >= 0) {
                // Start with selected index
                index = getSelectedIndex() + 1;
                if (index >= children.length)
                    index = 1; // Start over
            }
            else {
                // skip 1st node
                index = 1;
            }
            
	    int startIndex = index;
            while (!gotOne && index < children.length) {
                ViewNode n = (ViewNode)children[index];
                if (key == n.getName().charAt(0) || key == n.getDisplayName().charAt(0)) {
                    gotOne = true;
                    break;
                }
                index++;
		if (index == startIndex) {
		    gotOne = false; // back to where we started; didn't find a match
		    break;
		}
		if (index == children.length) {
		    index = 1; // start over
		}
            }
            
            if (!gotOne)
                index =  -1;
            
            return index;
        }
    }

    private CppEditorSupport getCppEditorSupport() {
	if (cppEditorSupport == null) {
	    cppEditorSupport = (CppEditorSupport)sourceObject.getCookie(CppEditorSupport.class);
	}
	return cppEditorSupport;
    }


    /**
     * Methods to handle (and cache) root node and it's children. "getChildren().getNodes()" is expensive, and
     * that's why we are caching it here.
     */
    private AbstractNode comboboxRootNode = null;
    private Node[] comboboxChildren = null;

    private AbstractNode newRootNode() {
        AbstractNode rn = new AbstractNode(new Children.Array());
        rn.setIconBaseWithExtension("org/netbeans/modules/java/resources/class.gif"); // NOI18N
        rn.setDisplayName(getString("NAME_NothingSelected")); // NOI18N
        return rn;
    }

    private AbstractNode getComboboxRootNode() {
	if (comboboxRootNode == null) {
	    comboboxRootNode = newRootNode();
	    getExplorerManager().setRootContext(comboboxRootNode);
	    comboboxChildren = null;
	}
	return comboboxRootNode;
    }

    private Node[] getComboboxChildren() {
	if (comboboxChildren == null) {
	    comboboxChildren = getComboboxRootNode().getChildren().getNodes();
	}
	return comboboxChildren;
    }

    private void removeComboboxChildren() {
        getComboboxRootNode().getChildren().remove(getComboboxRootNode().getChildren().getNodes());
	comboboxChildren = null;
    }
    
    private void addComboboxChildren(Node[] c) {
        getComboboxRootNode().getChildren().add(c);
	comboboxChildren = c;
    }


    /**
     * Test whether we are running on Linux
     */
    private static boolean isLinuxTestDone = false;
    private static boolean isLinux = false;
    private static boolean isLinux() {
	if (!isLinuxTestDone) {
	    isLinux = (System.getProperty("os.name", "").toLowerCase().indexOf("linux") >= 0);
	    isLinuxTestDone = true;
	}
	return isLinux;
    }

    
    /**
     * I18N
     */
    static String getString(String key) {
        if (bundle == null)
            bundle = org.openide.util.NbBundle.getBundle(NavigationView.class);
        return bundle.getString(key);
    }
}
