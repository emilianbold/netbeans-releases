/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * TransactionView.java
 *
 *
 * Created: Wed Feb  2 15:42:32 2000
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionListener;    
import java.awt.event.ActionEvent;    
import java.beans.PropertyChangeListener;    
import java.beans.PropertyChangeEvent;
import java.io.ObjectStreamException;
import java.io.Serializable;
import javax.swing.Icon;     
import javax.swing.ImageIcon;     
import javax.swing.JFrame;     
import javax.swing.JLabel;     
import javax.swing.JPanel;     
import javax.swing.JScrollPane;      
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;    
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;     
import javax.swing.border.EmptyBorder;     
import javax.swing.border.EtchedBorder;     
import javax.swing.event.ChangeListener;    
import javax.swing.event.ChangeEvent;    

import org.openide.awt.ToolbarButton;
import org.openide.awt.ToolbarToggleButton;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children.SortedArray;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.web.monitor.data.DataRecord;
import org.netbeans.modules.web.monitor.data.MonitorData;

/**
 * Update title does not work like it should. Maybe there is a getName
 * method for this that I can override.
 */
class TransactionView extends ExplorerPanel implements
				     PropertyChangeListener, ChangeListener {

    // Handles all the files etc. 
    private transient static TransactionView instance = null; 
    private transient static Controller controller = null;

    // Misc
    private transient JLabel transactionTitle = null;
    private transient ToolbarToggleButton timeAButton, 	timeDButton,
	alphaButton, browserCookieButton, savedCookieButton; 

    // Sizing and stuff...
    private transient  Dimension logD = new Dimension(250, 400);
    private transient  Dimension dataD = new Dimension(500, 400);
    private transient  Dimension tabD = new Dimension(500,472);
    
    // Are we debugging?
    private transient  final static boolean debug = false;

    // Display stuff 
    private transient static ExplorerManager mgr = null;
    private transient JPanel logPanel = null; 
    private transient JPanel dataPanel = null; 
    private transient JSplitPane splitPanel = null; 
    private transient double dividerRatio = .35;
    private transient BeanTreeView tree = null;
    private transient AbstractNode selected = null;

    private transient RequestDisplay requestDisplay = null;
    private transient CookieDisplay  cookieDisplay = null;
    private transient SessionDisplay sessionDisplay = null;
    private transient ContextDisplay contextDisplay = null;
    private transient ClientDisplay  clientDisplay = null;
    private transient HeaderDisplay  headerDisplay = null;

    private transient EditPanel editPanel = null;

    // Handle resizing for larger fonts
    boolean fontChanged = true;

    // Data display tables 
    private int displayType = 0;

    // Button icons

    static protected Icon updateIcon;
    static protected Icon a2zIcon;
    static protected Icon timesortAIcon;
    static protected Icon timesortDIcon;
    static protected Icon timestampIcon;
    static protected Icon browserCookieIcon;
    static protected Icon savedCookieIcon;
    static protected ImageIcon frameIcon;
   
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

	    browserCookieIcon = 
	    new ImageIcon(TransactionView.class.getResource
			  ("/org/netbeans/modules/web/monitor/client/icons/browsercookie.gif")); // NOI18N


	    savedCookieIcon = 
	    new ImageIcon(TransactionView.class.getResource
			  ("/org/netbeans/modules/web/monitor/client/icons/savedcookie.gif")); // NOI18N

	    frameIcon =
	    new ImageIcon(TransactionView.class.getResource
            ("/org/netbeans/modules/web/monitor/client/icons/menuitem.gif")); // NOI18N

	} catch(Throwable t) {
	    t.printStackTrace();
	} 
    }

    public HelpCtx getHelpCtx() {
	String helpID = NbBundle.getBundle(TransactionView.class).getString("MON_Transaction_View_F1_Help_ID"); // NOI18N
	return new HelpCtx( helpID );
    }

    /**
     * Creates the display and the nodes that are present all the
     * time. Because all this is done at startup, we don't actually
     * retrieve any data until the Monitor is opened.
     */
    private TransactionView() {
        setIcon(frameIcon.getImage());
	controller = Controller.getInstance();
	initialize();
	DisplayAction.setTransactionView(this);
	SaveAction.setTransactionView(this);
	EditReplayAction.setTransactionView(this);
	DeleteAction.setTransactionView(this);
	this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(TransactionView.class).getString("ACS_MON_monitorDesc"));
	this.getAccessibleContext().setAccessibleName(NbBundle.getBundle(TransactionView.class).getString("ACS_MON_monitorName"));

	if (debug) log ("Calling opentransactions from constructor"); // NOI18N
    }

    static TransactionView getInstance() { 
	if(instance == null) 
	    instance = new TransactionView(); 
	return instance; 
    }

    private void initialize() {

	mgr = getExplorerManager();
	mgr.addPropertyChangeListener(this);
	mgr.setRootContext(controller.getRoot());

	tree = new BeanTreeView();
	tree.setDefaultActionAllowed(true);
	tree.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(TransactionView.class).getString("ACS_MON_treeName"));
	tree.getAccessibleContext().setAccessibleName(NbBundle.getBundle(TransactionView.class).getString("ACS_MON_treeDesc"));

	createLogPanel(); 
	createDataPanel(); 
	splitPanel = 
	    new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logPanel, dataPanel); 
	splitPanel.setDividerLocation((int)(logD.getWidth()));
	splitPanel.setResizeWeight(dividerRatio);
	splitPanel.setDividerSize(1); 
	splitPanel.setOneTouchExpandable(true); 
	this.add(splitPanel);
	setName(NbBundle.getBundle(TransactionView.class).getString("MON_Title"));
    }
    
    /**
     * Open the transaction nodes (i.e. first level children of the root).
     */
    void openTransactionNodes() {

	// Post the request for later in case there are timing issues
	// going on here. 

	OpenTransactionNodesRequest req = new
	    OpenTransactionNodesRequest();
	
	if(debug) 
	    log("OpenTransactionNodesRequest:: " +  // NOI18N
			       "posting request..."); // NOI18N
				     
	RequestProcessor.Task t = 
	    RequestProcessor.postRequest(req, 500); // wait a sec...
    }

    class OpenTransactionNodesRequest implements Runnable {
	
	public void run() {
	    if(debug) 
		log("OpenTransactionNodesRequest:: " + // NOI18N
				   "running..."); // NOI18N
	    openTransactionNodes();
	}

	void openTransactionNodes() {
	    if (debug) 
		log("TransactionView::openTransactionNodes"); // NOI18N
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
		log("TransactionView::openTransactionNodes. currCHNodes.length = " + numCN); // NOI18N
	    if (numCN > 0) {
		int selectThisOne = 0;
		if (timeAButton.isSelected()) {
		    selectThisOne = numCN - 1;
		}
		if(debug) log("TransactionView::openTransactionNodes. selecting node " + currChNodes[selectThisOne] + "("+selectThisOne+")"); // NOI18N
		selectNode(currChNodes[selectThisOne]);
	    } else {
		Children savedCh = sn.getChildren();
		Node [] savedChNodes = savedCh.getNodes();
		int numSN = savedChNodes.length;
		if(debug) log("TransactionView::openTransactionNodes. savedChNodes.length = " + numSN); // NOI18N
		if (numSN > 0) {
		    selectNode(savedChNodes[0]);
		}
	    }
	}
    }

    void selectNode(Node n) {

	try {
	    mgr.setSelectedNodes(new Node[] {n});
	    
	} catch (Exception exc) {
	    if (debug) {
		log("TransactionView::caught exception selecting node. " + exc); // NOI18N
		exc.printStackTrace();
	    }
	} // safely ignored
    }
    
    /**
     * Loads the transactions into the monitor on opening. */
    private boolean openedOnceAlready = false;
    public void open() {
	if(debug) log("::open()"); //NOI18N
	super.open();
	//setName(NbBundle.getBundle(TransactionView.class).getString("MON_Title"));	
	if (!openedOnceAlready) {
	    openedOnceAlready = true;
	    controller.getTransactions();
	    openTransactionNodes();
	    //this.revalidate(); 
	    //this.repaint(); 
	}
	//PENDING ...
	controller.checkServer(false);
        requestFocus();
    }

    protected void updateTitle() {
	setName(NbBundle.getBundle(TransactionView.class).getString("MON_Title"));	
    }
    
    /**
     * Invoked by IDE when trying to close monitor. */
    public boolean canClose(Workspace w, boolean last) {
	return true;
    }

    /**
     * Do not serialize this component, substitute null instead.
     */
    public Object writeReplace() throws ObjectStreamException {
        return new ResolvableHelper();
    }


    /**
     * Invoked at startup, creates the display GUI.
     */
    private void createLogPanel() {

	JLabel title =
	    new JLabel(NbBundle.getBundle(TransactionView.class).getString("MON_Transactions_27"), SwingConstants.CENTER);
	title.setBorder (new EtchedBorder (EtchedBorder.LOWERED));

	JToolBar buttonPanel = new JToolBar();
	buttonPanel.setBorder
	    (new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED),
				new EmptyBorder (4, 4, 4, 4)
				    ));
	buttonPanel.setFloatable (false);

	ToolbarButton updateButton = new ToolbarButton(updateIcon);
	updateButton.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("MON_Reload_all_17"));
	updateButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    controller.getTransactions();
		}});

	timeAButton = new ToolbarToggleButton(timesortAIcon, false);
	timeAButton.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("MON_Order_transactions_15"));

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
	timeDButton.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("MON_Order_transactions_16"));
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
	alphaButton.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("MON_Order_transactions_14"));
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


	// Do we use the browser's cookie or the saved cookie? 
	browserCookieButton = new ToolbarToggleButton(browserCookieIcon, true);
	browserCookieButton.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("MON_Browser_cookie"));
	browserCookieButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    browserCookieButton.setSelected(true);
		    savedCookieButton.setSelected(false);
		    controller.setUseBrowserCookie(true); 

		}});

	savedCookieButton = new ToolbarToggleButton(savedCookieIcon, false);
	savedCookieButton.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("MON_Saved_cookie"));
	savedCookieButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    browserCookieButton.setSelected(false);
		    savedCookieButton.setSelected(true);
		    controller.setUseBrowserCookie(false); 
		}});


	ToolbarToggleButton timestampButton = new
	    ToolbarToggleButton(timestampIcon,
				TransactionNode.showTimeStamp());
	timestampButton.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("MON_Show_time_25"));
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
	buttonPanel.add(browserCookieButton);
	buttonPanel.add(savedCookieButton);
	//browserCookieButton.setSelected(true);
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

	logPanel.add(title, "North"); // NOI18N

	JPanel p = new JPanel (new BorderLayout ());
	//p.setBorder (new EtchedBorder (EtchedBorder.LOWERED));
	p.add(BorderLayout.NORTH, buttonPanel);
	p.add(BorderLayout.CENTER, tree);
	logPanel.add(BorderLayout.CENTER, p);
	logPanel.setPreferredSize(logD);		  
    }


    /**
     * Invoked at startup, creates the display GUI.
     */
    private void createDataPanel() {

	transactionTitle =
	    new JLabel(NbBundle.getBundle(TransactionView.class).getString("MON_Transaction_data_26"), SwingConstants.CENTER);
	transactionTitle.setBorder (new EtchedBorder (EtchedBorder.LOWERED));

	JTabbedPane jtp = new JTabbedPane();
        jtp.getAccessibleContext().setAccessibleName(NbBundle.getBundle(TransactionView.class).getString("ACS_MON_Transaction_dataName"));
        jtp.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(TransactionView.class).getString("ACS_MON_Transaction_dataDesc"));

	jtp.setPreferredSize(tabD);
	jtp.setMaximumSize(tabD);

	requestDisplay = new RequestDisplay(); 
	JScrollPane p = new JScrollPane(requestDisplay);
	jtp.addTab(NbBundle.getBundle(TransactionView.class).getString("MON_Request_19"), p);


	cookieDisplay = new CookieDisplay(); 
	p = new JScrollPane(cookieDisplay);
	jtp.addTab(NbBundle.getBundle(TransactionView.class).getString("MON_Cookies_4"), p);

	sessionDisplay = new SessionDisplay(); 
	p = new JScrollPane(sessionDisplay);
	jtp.addTab(NbBundle.getBundle(TransactionView.class).getString("MON_Session_24"), p); 

	contextDisplay = new ContextDisplay(); 
	p = new JScrollPane(contextDisplay);
	jtp.addTab(NbBundle.getBundle(TransactionView.class).getString("MON_Context_23"), p);

	clientDisplay = new ClientDisplay(); 
	p = new JScrollPane(clientDisplay);
	jtp.addTab(NbBundle.getBundle(TransactionView.class).getString("MON_Client_Server"), p);

	headerDisplay = new HeaderDisplay(); 
	p = new JScrollPane(headerDisplay);
	jtp.addTab(NbBundle.getBundle(TransactionView.class).getString("MON_Header_19"), p);

	jtp.addChangeListener(this);

	dataPanel = new JPanel();
	dataPanel.setLayout(new BorderLayout());
	dataPanel.add(transactionTitle, "North"); //NOI18N
	dataPanel.add(BorderLayout.CENTER, jtp);
	dataPanel.setPreferredSize(dataD);
    }


    /**
     * Invoked by DisplayAction. Displays monitor data for the selected
     * node. 
     * PENDING - register this as a listener for the display action
     */
    void displayTransaction(Node node) {
	if(debug) log("Displaying a transaction. Node: "  + (node == null ? "null" : node.getName())); //NOI18N
	if (node == null)
	    return;

	if(node instanceof TransactionNode || 
	   node instanceof NestedNode) {
	    try {
		selected = (AbstractNode)node;
	    } 
	    catch (ClassCastException ex) {
		selected = null;
		selectNode(null);
	    }
	}
	else {
	    selected = null;
	    selectNode(null);
	}
	
	if(debug) log("Set the selected node to\n" + // NOI18N
					 (selected == null ? "null" : selected.toString())); // NOI18N
	showData(); 
	if(debug) log("Finished displayTransaction())"); // NOI18N
    }

    void saveTransaction(Node[] nodes) {
	if(debug) log("In saveTransaction())"); // NOI18N
	if((nodes == null) || (nodes.length == 0)) return;
	controller.saveTransaction(nodes);
	selected = null;
	selectNode(null);
	showData(); 
	if(debug) log("Finished saveTransaction())"); // NOI18N
    }
    
    /**
     * Invoked by EditReplayAction. 
     */
    void editTransaction(Node node) {
	if(debug) log("Editing a transaction"); //NOI18N
	// Exit if the internal server is not running - the user
	// should start it before they do this. 
	if(!controller.checkServer(true)) return;
	selected = (TransactionNode)node;
	if(debug) log("Set the selected node to\n" + // NOI18N
					 selected.toString()); 
	editData(); 
	if(debug) log("Finished editTransaction())"); // NOI18N
    }


    /**
     * Listens to events from the tab pane, displays different
     * categories of data accordingly. 
     */
    public void stateChanged(ChangeEvent e) {

	setName(NbBundle.getBundle(TransactionView.class).getString("MON_Title"));

	JTabbedPane p = (JTabbedPane)e.getSource();
	displayType = p.getSelectedIndex();
	showData();
    }
    

    void showData() {
	 
	if(selected == null) {
	    // PENDING
	    if(debug) 
		log("No selected node, why is this?"); // NOI18N
	    if(debug) log("  Probably because user selected a non-transaction node (i.e. one of the folders. So we clear the display."); // NOI18N
	}
	
	if(debug) log("Now in showData()"); // NOI18N
	    
	DataRecord dr = null;	    
	try {
	    if (selected != null) {
		dr = controller.getDataRecord(selected);
	    }
	}
	catch(Exception ex) {
	    if(debug) log(ex.getMessage());
	    ex.printStackTrace();
	}
	
	if(debug) {
	    log("Got this far"); // NOI18N
	    log("displayType:" + String.valueOf(displayType)); // NOI18N
	}
	
	
	if (displayType == 0)
	    requestDisplay.setData(dr);
	else if (displayType == 1)
	    cookieDisplay.setData(dr);
	else if (displayType == 2)
	    sessionDisplay.setData(dr);
	else if (displayType == 3)
	    contextDisplay.setData(dr);
	else if (displayType == 4)
	    clientDisplay.setData(dr);
	else if (displayType == 5)
	    headerDisplay.setData(dr);

	this.repaint();
	
	if(debug) log("Finished showData()"); // NOI18N
    }


    void editData() {

	if(selected == null) {
	    if(debug) 
		log("No selected node, why is this?"); // NOI18N 
	    return;
	}
	
	if(!(selected instanceof TransactionNode)) return;
		
	if(debug) log("Now in editData()"); // NOI18N
	    
	MonitorData md = null;	    
	try {
	    // We retrieve the data from the file system, not from the 
	    // cache
	    md = controller.getMonitorData((TransactionNode)selected, 
					   false,  // get from file
					   false); // and don't cache
	}
	catch(Exception ex) {
	    if(debug) log(ex.getMessage());
	    ex.printStackTrace();
	}
	
	if(debug) {
	    log("Got this far"); // NOI18N
	    log(md.dumpBeanNode()); 
	    log("displayType:" + // NOI18N
			       String.valueOf(displayType));
	}
	
	// Bring up the dialog. 
	if (editPanel == null) {
	    editPanel = new EditPanel(md);
	}

	editPanel.setData(md);
	editPanel.showDialog();

	if(debug) log("Finished editData()"); // NOI18N
    }

    /**
     * Display the data for a node if it's selected. This should
     * probably be done by checking if you can get the DisplayAction
     * from the Node, and then calling it if it's enabled.
     */
    public void propertyChange(PropertyChangeEvent evt) {

	setName(NbBundle.getBundle(TransactionView.class).getString("MON_Title"));
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
			log(e.getMessage());
			e.printStackTrace();
		    }
		    selected = null;
		    if(debug) 
			log("Set the selected node to null"); // NOI18N
		    showData();
		    return;
		}
	    }
	}
	if(debug) log("Finished propertyChange()"); // NOI18N
    }

    /**
     * Blanks out the displays - this is used by the delete actions
     */
    void blank() {
	selected = null;
	selectNode(null);
	showData(); 
    }

    /** 
     * When paint is first invoked, we set the rowheight based on the
     * size of the font. */
    public void paint(Graphics g) {
	if(fontChanged) {
	    super.paint(g);
	    return; 
	}

	FontMetrics fm = g.getFontMetrics(getFont());
	fontChanged = false;
	
	double logWidth = fm.stringWidth(NbBundle.getBundle(TransactionView.class).getString("MON_Transactions_27")) * 1.1; 

	if(logWidth > logD.getWidth()) { 
	    double factor = logWidth/logD.getWidth(); 
	    logD.setSize(logWidth, factor * logD.getHeight());


	    dataD.setSize(factor * dataD.getWidth(), 
			  factor * dataD.getHeight()); 
	}

	logPanel.setPreferredSize(logD);
	dataPanel.setPreferredSize(dataD);
	splitPanel.resetToPreferredSizes(); 
	splitPanel.setDividerLocation((int)(logD.getWidth()));

	try { 
	    Container o = (Container)this.getParent(); 
	    while(true) { 
		if(o instanceof JFrame) { 
		    JFrame parent = (JFrame)o; 
		    parent.pack(); 
		    break; 
		} 
		o = o.getParent(); 
	    } 
	}
	catch(Throwable t) {
	    // Do nothing, we can't resize the component
	    // invalidate on this component does not work. 
	}
	//super.paint(g);
	return;
    }

    private void log(String s) {
	System.out.println("TransactionView::" + s); //NOI18N
    }


    public static final class ResolvableHelper implements Serializable {
        static final long serialVersionUID = 1234546018839457544L;
        Object readResolve() {
	    Controller.getInstance().getTransactions();
            return TransactionView.getInstance(); 
        }
    }
}
