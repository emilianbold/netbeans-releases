/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * TransactionView.java
 *
 *
 * Created: Wed Feb  2 15:42:32 2000
 *
 * @author Ana von Klopp Lemon
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import org.netbeans.modules.web.monitor.server.Constants;
import org.netbeans.modules.web.monitor.data.*;
import javax.swing.*;     // widgets
import javax.swing.border.*;     // widgets
import javax.swing.event.*;
import java.awt.Font;
import java.awt.Insets;

import java.net.*;        // url
import java.awt.*;          // layouts, dialog, etc.
import java.awt.event.*;    // Events
import java.io.*;           // I/O
import java.text.*;         // I/O
import java.util.*;         // local GUI

import org.openide.TopManager;
import org.openide.awt.SplittedPanel;
import org.openide.awt.ToolbarButton;
import org.openide.awt.ToolbarToggleButton;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.Children.SortedArray;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.util.NbBundle;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

/**
 * Update title does not work like it should. Maybe there is a getName
 * method for this that I can override.
 */
public class TransactionView extends ExplorerPanel implements
				     PropertyChangeListener, ChangeListener {

    // Handles all the files etc. 
    private static Controller controller = null;
    
    // Misc
    private transient Frame parentFrame = null;
    private transient JLabel transactionTitle = null;
    private transient ToolbarToggleButton timeAButton, timeDButton, alphaButton;

    // Sizing and stuff...
    private transient  Dimension logD = new Dimension(250, 400);
    private transient  Dimension dataD = new Dimension(450, 400);
    private transient  Dimension tabD = new Dimension(450,472);
    
    // Are we debugging?
    private transient  final static boolean debug = false;

    // Display stuff 
    private transient static ExplorerManager mgr = null;
    private transient BeanTreeView tree = null;
    private transient TransactionNode selected = null;

    private transient RequestDisplay requestDisplay = null;
    private transient CookieDisplay  cookieDisplay = null;
    private transient SessionDisplay sessionDisplay = null;
    private transient ServletDisplay servletDisplay = null;
    private transient ClientDisplay  clientDisplay = null;
    private transient HeaderDisplay  headerDisplay = null;

    private transient EditPanel editPanel = null;


    // Data display tables 
    private int displayType = 0;

    ResourceBundle msgs = NbBundle.getBundle(TransactionView.class);
    
    // Button icons

    static protected Icon updateIcon;
    static protected Icon a2zIcon;
    static protected Icon timesortAIcon;
    static protected Icon timesortDIcon;
    static protected Icon timestampIcon;
    static protected ImageIcon frameIcon;

    //
    // Common Insets
    // Insets(top, left, bottom, right)
    public static Insets zeroInsets =       new Insets( 0,  0,  0,  0);
    public static Insets tableInsets =      new Insets( 0, 18, 12, 12);
    public static Insets labelInsets =      new Insets( 0,  6,  0,  0);
    public static Insets buttonInsets =     new Insets( 6,  0,  5,  6);
    public static Insets sortButtonInsets = new Insets( 0, 12,  0,  0);
    public static Insets indentInsets =     new Insets( 0, 18,  0,  0);
    public static Insets topSpacerInsets =  new Insets(12,  0,  0,  0);
    
    static {
		
	try {
	    updateIcon =
	    new ImageIcon(TransactionView.class.getResource
	    ("/org/netbeans/modules/web/monitor/client/icons/update.gif")); // NOI18N

	    a2zIcon =
	    new ImageIcon(TransactionView.class.getResource
	    ("/org/netbeans/modules/web/monitor/client/icons/a2z.gif")); // NOI18N

	    timesortAIcon =
	    new ImageIcon(TransactionView.class.getResource
            ("/org/netbeans/modules/web/monitor/client/icons/timesortA.gif")); // NOI18N

	    timesortDIcon =
	    new ImageIcon(TransactionView.class.getResource
			  ("/org/netbeans/modules/web/monitor/client/icons/timesortB.gif")); // NOI18N

	    timestampIcon =
	    new ImageIcon(TransactionView.class.getResource
			  ("/org/netbeans/modules/web/monitor/client/icons/timestamp.gif")); // NOI18N

	    frameIcon =
	    new ImageIcon(TransactionView.class.getResource
            ("/org/netbeans/modules/web/monitor/client/icons/menuitem.gif")); // NOI18N



	} catch(Throwable t) {
	    t.printStackTrace();
	} 
    }

    public HelpCtx getHelpCtx() {
	String helpID = msgs.getString("MON_Transaction_View_F1_Help_ID"); // NOI18N
	return new HelpCtx( helpID );
    }

    /**
     * Creates the display and the nodes that are present all the
     * time. Because all this is done at startup, we don't actually
     * retrieve any data until the Monitor is opened.
     */
    public TransactionView(Controller c) {
        setIcon(frameIcon.getImage());
	controller = c;
	initialize();
	DisplayAction.setTransactionView(this);
	SaveAction.setTransactionView(this);
	EditReplayAction.setTransactionView(this);
	DeleteAction.setTransactionView(this);
	
	if (debug) 
	    System.out.println
		("Calling opentransactions from constructor"); // NOI18N
    }

    private void initialize() {

	mgr = getExplorerManager();
	mgr.addPropertyChangeListener(this);
	mgr.setRootContext(controller.getRoot());

	tree = new BeanTreeView();
	tree.setDefaultActionAllowed(true);

	SplittedPanel splitPanel = new SplittedPanel();
	splitPanel.setSplitPosition(35);
	splitPanel.setSplitterComponent(new SplittedPanel.EmptySplitter(5));
	splitPanel.setSwapPanesEnabled(false);
	splitPanel.setKeepFirstSame(true);

	splitPanel.add(createLogPanel(), SplittedPanel.ADD_LEFT);
	splitPanel.add(createDataPanel(), SplittedPanel.ADD_RIGHT);

	this.add(splitPanel);

	setName(msgs.getString("MON_Title"));
    }

    /**
     * Open the transaction nodes (i.e. first level children of the root).
     */
    public void openTransactionNodes() {

	// Post the request for later in case there are timing issues
	// going on here. 

	OpenTransactionNodesRequest req = new
	    OpenTransactionNodesRequest();
	
	if(debug) 
	    System.out.println("OpenTransactionNodesRequest:: " +  // NOI18N
			       "posting request..."); // NOI18N
				     
	RequestProcessor.Task t = 
	    RequestProcessor.postRequest(req, 500); // wait a sec...
    }

    class OpenTransactionNodesRequest implements Runnable {
	
	public void run() {
	    if(debug) 
		System.out.println("OpenTransactionNodesRequest:: " + // NOI18N
				   "running..."); // NOI18N
	    openTransactionNodes();
	}

	public void openTransactionNodes() {
	    if (debug) 
		System.out.println("TransactionView::openTransactionNodes"); // NOI18N
	    NavigateNode root = controller.getRoot();
	    Children ch = root.getChildren();
	    Node [] nodes = ch.getNodes();
	    CurrNode cn = (CurrNode)nodes[0];
	    SavedNode sn = (SavedNode)nodes[1];
	    
	    
	    // If there are any current nodes, then select the most
	    // recent (i.e. the last?) one. 

	    Children currCh = cn.getChildren();
	    Node [] currChNodes = currCh.getNodes();
	    int numCN = currChNodes.length;
	    if(debug)
		System.out.println("TransactionView::openTransactionNodes. currCHNodes.length = " + numCN); // NOI18N
	    if (numCN > 0) {
		int selectThisOne = 0;
		if (timeAButton.isSelected()) {
		    selectThisOne = numCN - 1;
		}
		if(debug) System.out.println("TransactionView::openTransactionNodes. selecting node " + currChNodes[selectThisOne] + "("+selectThisOne+")"); // NOI18N
		selectNode(currChNodes[selectThisOne]);
	    } else {
		Children savedCh = sn.getChildren();
		Node [] savedChNodes = savedCh.getNodes();
		int numSN = savedChNodes.length;
		if(debug) System.out.println("TransactionView::openTransactionNodes. savedChNodes.length = " + numSN); // NOI18N
		if (numSN > 0) {
		    selectNode(savedChNodes[0]);
		}
	    }
	}
    }

    public void selectNode(Node n) {

	try {
	    mgr.setSelectedNodes(new Node[] {n});
	    
	} catch (Exception exc) {
	    if (debug) {
		System.out.println("TransactionView::caught exception selecting node. " + exc); // NOI18N
		exc.printStackTrace();
	    }
	} // safely ignored
    }
    
    /**
     * Invoked from IDE when Monitor is opened. */
    private boolean openedOnceAlready = false;
    public void open(Workspace w) {
	super.open(w); 
	setName(msgs.getString("MON_Title"));	
	String name = w.getName();
	if (!openedOnceAlready) {
	    openedOnceAlready = true;
	    if (debug) System.out.println("Calling opentransactions from open(workspace w)"); // NOI18N
	    controller.getTransactions();
	    openTransactionNodes();
	}
	controller.checkServer(false);
        requestFocus();
    }
    

    protected void updateTitle() {
	setName(msgs.getString("MON_Title"));	
    }
    
    /**
     * Invoked from IDE when Monitor is opened. This calls
     * getTransactions() which makes a URL connection to the
     * web server. The effect of super.open() is to invoke the method
     * above (which is not circular). Repeating the code from above
     * causes getTransactions() to be called twice (and hence
     * potentially the dialog to show twice) so I had to take it out
     * from here. */
    public void open() {
	super.open();
        requestFocus();
    }


    /**
     * Invoked by IDE when trying to close monitor. */
    public boolean canClose(Workspace w, boolean last) {
	return true;
    }

    /**
     * Do not serialize this component, substitute null instead.
     */
    public Object writeReplace ()
	throws ObjectStreamException {
	return null;
    }


    /**
     * Invoked at startup, creates the display GUI.
     */
    private JPanel createLogPanel() {

	JPanel logPanel = null;
	JLabel title =
	    new JLabel(msgs.getString("MON_Transactions_27"), SwingConstants.CENTER);
	title.setBorder (new EtchedBorder (EtchedBorder.LOWERED));

	JToolBar buttonPanel = new JToolBar();
	buttonPanel.setBorder
	    (new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED),
				new EmptyBorder (4, 4, 4, 4)
				    ));
	buttonPanel.setFloatable (false);

	ToolbarButton updateButton = new ToolbarButton(updateIcon);
	updateButton.setToolTipText(msgs.getString("MON_Reload_all_17"));
	updateButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    controller.getTransactions();
		}});

	timeAButton = new ToolbarToggleButton(timesortAIcon, false);
	timeAButton.setToolTipText(msgs.getString("MON_Order_transactions_15"));

	timeAButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

		    if(!((ToolbarToggleButton)e.getSource()).isSelected())
			return;
		    else {
			timeDButton.setSelected(false);
			alphaButton.setSelected(false);
			controller.setComparator
			    (controller.new CompTime(false));
		    }
		}});

	timeDButton = new ToolbarToggleButton(timesortDIcon, true);
	timeDButton.setToolTipText(msgs.getString("MON_Order_transactions_16"));
	timeDButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

		    if(!((ToolbarToggleButton)e.getSource()).isSelected())
			return;
		    else {
			timeAButton.setSelected(false);
			alphaButton.setSelected(false);
			controller.setComparator
			    (controller.new CompTime(true));
		    }

		}});

	alphaButton = new ToolbarToggleButton(a2zIcon, false);
	alphaButton.setToolTipText(msgs.getString("MON_Order_transactions_14"));
	alphaButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

		    if(!((ToolbarToggleButton)e.getSource()).isSelected())
			return;
		    else {
			timeAButton.setSelected(false);
			timeDButton.setSelected(false);
			controller.setComparator
			    (controller.new CompAlpha());
		    }

		}});

	ToolbarToggleButton timestampButton = new
	    ToolbarToggleButton(timestampIcon,
				TransactionNode.showTimeStamp());
	timestampButton.setToolTipText(msgs.getString("MON_Show_time_25"));
	timestampButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    TransactionNode.toggleTimeStamp();
		    // PENDING - should find a way to repaint
		    // the tree. tree.repaint() does not work. 
		    controller.updateNodeNames();
		}});

	buttonPanel.add(updateButton);

	JPanel sep = new JPanel() {
		public float getAlignmentX() {
		    return 0;
		}
		public float getAlignmentY() {
		    return 0;
		}
	    };
	sep.setMaximumSize(new Dimension(10, 10));
	buttonPanel.add(sep);
	buttonPanel.add(timeDButton);
	buttonPanel.add(timeAButton);
	buttonPanel.add(alphaButton);
	sep = new JPanel() {
		public float getAlignmentX() {
		    return 0;
		}
		public float getAlignmentY() {
		    return 0;
		}
	    };
	sep.setMaximumSize(new Dimension(10, 10));
	buttonPanel.add(sep);
	buttonPanel.add(timestampButton);


	logPanel = new JPanel();
	logPanel.setLayout(new BorderLayout());
        //logPanel.setBorder(new CompoundBorder
	//(new LineBorder (getBackground ()),
	//new BevelBorder(EtchedBorder.LOWERED)));

	logPanel.setPreferredSize(logD);
	logPanel.setMinimumSize(logD);

	logPanel.add(title, "North"); // NOI18N

	JPanel p = new JPanel (new BorderLayout ());
	//p.setBorder (new EtchedBorder (EtchedBorder.LOWERED));
	p.add(BorderLayout.NORTH, buttonPanel);
	p.add(BorderLayout.CENTER, tree);
	logPanel.add(BorderLayout.CENTER, p);

	return logPanel;

    }


    /**
     * Invoked at startup, creates the display GUI.
     */
    private JPanel createDataPanel() {

	JPanel dataPanel = null;

	transactionTitle =
	    new JLabel(msgs.getString("MON_Transaction_data_26"), SwingConstants.CENTER);
	transactionTitle.setBorder (new EtchedBorder (EtchedBorder.LOWERED));

	JTabbedPane jtp = new JTabbedPane();
	jtp.setPreferredSize(tabD);
	jtp.setMaximumSize(tabD);

	requestDisplay = new RequestDisplay(); 
	JScrollPane p = new JScrollPane(requestDisplay);
	jtp.addTab(msgs.getString("MON_Request_19"), p);


	cookieDisplay = new CookieDisplay(); 
	p = new JScrollPane(cookieDisplay);
	// border for debugging. 
	//p.setViewportBorder(BorderFactory.createEtchedBorder());
	jtp.addTab(msgs.getString("MON_Cookies_4"), p);


	sessionDisplay = new SessionDisplay(); 
	p = new JScrollPane(sessionDisplay);
	jtp.addTab(msgs.getString("MON_Session_24"), p); 

	servletDisplay = new ServletDisplay(); 
	p = new JScrollPane(servletDisplay);
	jtp.addTab(msgs.getString("MON_Servlet_23"), p);

	clientDisplay = new ClientDisplay(); 
	p = new JScrollPane(clientDisplay);
	jtp.addTab(msgs.getString("MON_Client_3"), p);


	headerDisplay = new HeaderDisplay(); 
	p = new JScrollPane(headerDisplay);
	jtp.addTab(msgs.getString("MON_Header_19"), p);

	jtp.addChangeListener(this);

	dataPanel = new JPanel();
	dataPanel.setLayout(new BorderLayout());
	dataPanel.setPreferredSize(dataD);
	dataPanel.setMinimumSize(dataD);

	dataPanel.add(transactionTitle, "North"); //NOI18N
	dataPanel.add(BorderLayout.CENTER, jtp);
	return dataPanel;
    }

    //
    // Routines for creating widgets in centralzied styles.
    //
    /**
     * create a header label that uses bold.
     */
    public static JLabel createHeaderLabel(String label) {
	JLabel jl = new JLabel(label);
	Font labelFont = jl.getFont();
	Font boldFont = labelFont.deriveFont(Font.BOLD);
	jl.setFont(boldFont);
	return jl;
    }
	
    public static JLabel createDataLabel(String label) {
	JLabel jl = new JLabel(label);
	return jl;
    }

    public static Component createSortButtonLabel(String label, final DisplayTable dt) {
	JPanel panel = new JPanel();
	panel.add(createHeaderLabel(label));
	panel.add(createSortButton(dt));
	return panel;
    }
		  




    /**
     * create a toggle-able button that changes the sort-order of a
     * DisplayTable. Showing different buttons (up & down arrow)
     * depending on the state. 
     */
    public static JButton createSortButton(DisplayTable dt) {
	SortButton b = new SortButton(dt); 
	return(JButton)b;
    } 

    public static Component createTopSpacer() {
	/*
	JPanel space = new JPanel();
	space.add(Box.createVerticalStrut(1));
	//debug
	space.setBorder(BorderFactory.createLineBorder(Color.red));
	return space;
	*/
	return Box.createVerticalStrut(1);
    }

    /**
     * Invoked by DisplayAction. Displays monitor data for the selected
     * node. 
     * PENDING - register this as a listener for the display action
     */
    public void displayTransaction(Node node) {
	if(debug) System.out.println("Displaying a transaction. Node: "  + (node == null ? "null" : node.getName())); //NOI18N
	if (node == null)
	    return;

	try {
	    selected = (TransactionNode)node;
	} catch (ClassCastException ex) {
	    selected = null;
	    selectNode(null);
	}
	if(debug) System.out.println("Set the selected node to\n" + // NOI18N
					 (selected == null ? "null" : selected.toString())); // NOI18N
	showData(); 
	if(debug) System.out.println("Finished displayTransaction())"); // NOI18N
    }

    public void saveTransaction(Node[] nodes) {
	if(debug) System.out.println("In saveTransaction())"); // NOI18N
	if((nodes == null) || (nodes.length == 0)) return;
	controller.saveTransaction(nodes);
	selected = null;
	selectNode(null);
	showData(); 
	if(debug) System.out.println("Finished saveTransaction())"); // NOI18N
    }
    
    /**
     * Invoked by EditReplayAction. 
     */
    public void editTransaction(Node node) {
	if(debug) System.out.println("Editing a transaction"); //NOI18N
	// Exit if the internal server is not running - the user
	// should start it before they do this. 
	if(!controller.checkServer(true)) return;
	selected = (TransactionNode)node;
	if(debug) System.out.println("Set the selected node to\n" + // NOI18N
					 selected.toString()); 
	editData(); 
	if(debug) System.out.println("Finished editTransaction())"); // NOI18N
    }


    /**
     * Listens to events from the tab pane, displays different
     * categories of data accordingly. 
     */
    public void stateChanged(ChangeEvent e) {

	setName(msgs.getString("MON_Title"));

	JTabbedPane p = (JTabbedPane)e.getSource();
	displayType = p.getSelectedIndex();
	showData();
    }
    

    void showData() {

	if(selected == null) {
	    // PENDING
	    if(debug) 
		System.out.println("No selected node, why is this?"); // NOI18N
	    if(debug) System.out.println("  Probably because user selected a non-transaction node (i.e. one of the folders. So we clear the display."); // NOI18N
	}
	
	if(debug) System.out.println("Now in showData()"); // NOI18N
	    
	MonitorData md = null;	    
	try {
	    if (selected != null) {
		md = controller.getMonitorData(selected);
	    }
	}
	catch(Exception ex) {
	    if(debug) System.out.println(ex.getMessage());
	    ex.printStackTrace();
	}
	
	if(debug) {
	    System.out.println("Got this far"); // NOI18N
	    System.out.println((md == null?"null md":md.createTransactionNode(true).toString())); // NOI18N
	    System.out.println("displayType:" + String.valueOf(displayType)); // NOI18N
	}
	
	
	if (displayType == 0)
	    requestDisplay.setData(md);
	else if (displayType == 1)
	    cookieDisplay.setData(md);
	else if (displayType == 2)
	    sessionDisplay.setData(md);
	else if (displayType == 3)
	    servletDisplay.setData(md);
	else if (displayType == 4)
	    clientDisplay.setData(md);
	else if (displayType == 5)
	    headerDisplay.setData(md);
	this.repaint();
	
	if(debug) System.out.println("Finished showData()"); // NOI18N
    }


    void editData() {

	if(selected == null) {
	    if(debug) 
		System.out.println("No selected node, why is this?"); // NOI18N 
	    return;
	}
	
	if(debug) System.out.println("Now in editData()"); // NOI18N
	    
	MonitorData md = null;	    
	try {
	    boolean cached = false;
	    md = controller.getMonitorData(selected, cached);
	}
	catch(Exception ex) {
	    if(debug) System.out.println(ex.getMessage());
	    ex.printStackTrace();
	}
	
	if(debug) {
	    System.out.println("Got this far"); // NOI18N
	    System.out.println(md.createTransactionNode(true).toString());
	    System.out.println("displayType:" + // NOI18N
			       String.valueOf(displayType));
	}
	
	// Bring up the dialog. 
	if (editPanel == null) {
	    editPanel = new EditPanel(md);
	}

	editPanel.setData(md);
	editPanel.showDialog();

	if(debug) System.out.println("Finished editData()"); // NOI18N
    }

    /**
     * Display the data for a node if it's selected. This should
     * probably be done by checking if you can get the DisplayAction
     * from the Node, and then calling it if it's enabled.
     */
    public void propertyChange(PropertyChangeEvent evt) {

	setName(msgs.getString("MON_Title"));
	//updateTitle();

	if(evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {

	    if(evt.getNewValue() instanceof Node[]) {
		try {
		    Node[] ns = (Node[])evt.getNewValue();
		    if(ns.length == 1) {
			displayTransaction(ns[0]); 
		    }
		}
		// Do nothing, this was not a proper node
		catch(Exception e) {
		    if(debug) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		    }
		    selected = null;
		    if(debug) 
			System.out.println("Set the selected node to null"); // NOI18N
		    showData();
		    return;
		}
	    }
	}
	if(debug) System.out.println("Finished propertyChange()"); // NOI18N
    }

    /**
     * Blanks out the displays - this is used by the delete actions
     */
    public void blank() {
	selected = null;
	selectNode(null);
	showData(); 
    }

    class ComboBoxRenderer extends JLabel implements ListCellRenderer {
	public ComboBoxRenderer() {
	    setOpaque(true);
	}
	public Component getListCellRendererComponent(JList list,
						      Object o,
						      int index,
						      boolean isSelected,
						      boolean cellHasFocus) {
	    if(isSelected) {
		setBackground(list.getSelectionBackground());
		setForeground(list.getSelectionForeground());
	    }
	    else {
		setBackground(list.getBackground());
		setForeground(list.getForeground());
	    }
	    ImageIcon icon = (ImageIcon)o;
	    setText(icon.getDescription());
	    setIcon(icon);
	    return this;
	}
    }
}
