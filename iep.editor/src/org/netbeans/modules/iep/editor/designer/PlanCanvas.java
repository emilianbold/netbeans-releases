package org.netbeans.modules.iep.editor.designer;

import com.nwoods.jgo.JGoCopyEnvironment;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.Clipboard;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.TopComponent;

import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoDocumentEvent;
import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoView;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoObjectSimpleCollection;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoSelection;
import com.nwoods.jgo.JGoViewEvent;
import com.nwoods.jgo.JGoViewListener;

import org.netbeans.modules.iep.editor.designer.actions.CopyAction;
import org.netbeans.modules.iep.editor.designer.actions.CutAction;
import org.netbeans.modules.iep.editor.designer.actions.DeleteAction;
import org.netbeans.modules.iep.editor.designer.actions.IEPComponentTransferable;
import org.netbeans.modules.iep.editor.designer.actions.OperatorPlanCanvasAction;
import org.netbeans.modules.iep.editor.designer.actions.PasteAction;

import org.netbeans.modules.iep.editor.designer.nodes.PlanNode;
import org.netbeans.modules.iep.editor.model.NameGenerator;
import org.netbeans.modules.iep.editor.palette.IepPaletteFactory;
import org.netbeans.modules.tbls.editor.palette.TcgActiveEditorDrop;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodeProperty;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodePropertyCustomizerDialogManager;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodeView;
import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.Documentation;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.LinkComponent;
import org.netbeans.modules.iep.model.LinkComponentContainer;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.tbls.model.ImageUtil;
import org.netbeans.modules.tbls.model.TcgComponentType;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.spi.palette.PaletteController;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.netbeans.api.javahelp.Help;
import org.netbeans.modules.iep.model.share.SharedConstants;

public class PlanCanvas extends JGoView implements JGoViewListener, GuiConstants, TcgComponentNodeView, SharedConstants {

    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(PlanCanvas.class.getName());    // there's just a single JPopupMenu, shared by all instances of this class,
    // that gets initialized in the app initialization
    private JPopupMenu mPopupMenu = new JPopupMenu();
    private List<Integer> mAccList = new ArrayList<Integer>();
    private Point mLastPopupPoint = null;
    protected Map mMsgListenerTable = new HashMap();
    private DocumentationControl docControl = null;
    private boolean showingDocumentaion = false;
    private IEPModel mModel;
    private ImageIcon mNoDropCursor = ImageUtil.getImageIcon("cursorsnone.gif");
    private Cursor mDefaultCursor = null;
    private JGoPort mTempPortForLink;
    private Link mTempLink;
    private Cursor mNoLinkCursor;
    private EntityNode mLastEntityNode = null;
    private PlanCanvasFocusHandler mCanvasFocusHandler;
    private boolean mIsLinking = false;
    private IEPModelListener mModelListener = null;

    /**
     * The sizing of the JGoText instance, if not explicitly set by calling setBoundingRect()
     * or setSize() or setHeight() or the like, is continually determined by the text string
     * and the font size and other attributes, if isAutoResize() is true. However, the text
     * string cannot be measured unless a Graphics[2D] instance is available. Since
     * Component.getGraphics() returns null if it has not yet become visible, JGoText cannot
     * be sized automatically until after the view becomes visible. Thus if you care about
     * the size of a JGoText but want it to be smart and not have to set it explicitly,
     * you should wait to add the JGoText object to the document until after calling
     * JGoView.setVisible(true). For example, it's common to initialize JGo documents
     * in the init() method of applets, rather than in the constructor.
     */
    void setDoc(PdModel doc) {
        setDocument(doc);
    //ritdoc.setDesigner(mDesigner);
    //ritmDesigner.showPropertyPane(mDesigner.getPlan(), this);
    }
    // convenience method--the return value is a PdModel instead
    // of a JGoDocument
    public PdModel getDoc() {
        return (PdModel) getDocument();
    }

    void help() {
//        try {
//            boolean valid = true;
//            final StringBuffer helpMsg = new StringBuffer();
//            JGoSelection sel = getSelection();
//            if (sel.isEmpty()) {
//                return;
//            }
//            
//            Plan plan = getDoc().getPlan();
//            InputOutput io = IOProvider.getDefault().getIO(plan.getFullName(), false);
//            io.select();
//            OutputWriter out = io.getOut();
//            
//            JGoListPosition pos = sel.getFirstObjectPos();
//            while (pos != null) {
//                JGoObject obj = sel.getObjectAtPos(pos);
//                pos = sel.getNextObjectPos(pos);
//                
//                if (!obj.isTopLevel()) {
//                    continue;
//                }
//                if (obj instanceof EntityNode) {
//                    EntityNode node = (EntityNode)obj;
//                    TcgComponent component = node.getComponent();
//                    TcgComponentValidationReport r = component.validate();
//                    if (!r.isOK()) {
//                        valid = false;
//                        out.println(node.getLabelString() + ": ");
//                        List msgList = r.getMessageList();
//                        for (int i = 0, I = msgList.size(); i < I; i++) {
//                            TcgComponentValidationMsg msg = (TcgComponentValidationMsg)msgList.get(i);
//                            String type = msg.getType().equals(VALIDATION_ERROR_KEY)? "error" : "warning";
//                            out.println("\t" + type + ": " + msg.getText());
//                        }
//                    }
//                }
//            }
//            if (valid) {
//                helpMsg.append(NbBundle.getMessage(PlanCanvas.class,"PdCanvas.No_error_found"));
//            }
//            out.println(helpMsg.toString());
//            out.flush();
//        } catch (Exception e) {
//            //e.printStackTrace();
//            mLog.warning("Exception :" + e.getMessage());
//        }
    }
    /*
    void validatePlan() {
    Plan plan = getDoc().getPlan();
    InputOutput io = IOProvider.getDefault().getIO(plan.getFullName(), false);
    io.select();
    OutputWriter out = io.getOut();
    try {
    out.reset();
    } catch (Exception e) {
    e.printStackTrace();
    }
    PdModel doc = getDoc();
    int errorCnt = 0;
    int warningCnt = 0;
    // To include plan level valiation error reporting.
    TcgComponentValidationReport planValidationReport = plan.validate();
    if(!planValidationReport.isOK()) {
    try {
    out.println(plan.getFullName() + ": ", null);
    List msgList = planValidationReport.getMessageList();
    for (int i = 0, I = msgList.size(); i < I; i++) {
    TcgComponentValidationMsg msg = (TcgComponentValidationMsg)msgList.get(i);
    String type = "";
    if (msg.getType().equals(VALIDATION_ERROR_KEY)) {
    type = "error";
    errorCnt++;
    } else {
    type = "warning";
    warningCnt++;
    }
    out.println("\t" + type + ": " + msg.getText(), null);
    }
    } catch(Exception e) {
    mLog.log(Level.WARNING,e.getMessage(),e);
    }
    }
    
    JGoListPosition pos = doc.getFirstObjectPos();
    try {
    while (pos != null) {
    JGoObject obj = doc.getObjectAtPos(pos);
    pos = doc.getNextObjectPos(pos);
    if (obj instanceof EntityNode) {
    EntityNode node = (EntityNode)obj;
    TcgComponent component = node.getComponent();
    TcgComponentValidationReport r = component.validate();
    if (!r.isOK()) {
    PdMessageListener msgListener = (PdMessageListener)mMsgListenerTable.get(node);
    if (msgListener == null) {
    msgListener = new PdMessageListener(this, node);
    mMsgListenerTable.put(node, msgListener);
    }
    out.println(node.getLabelString() + ": ");
    List msgList = r.getMessageList();
    for (int i = 0, I = msgList.size(); i < I; i++) {
    TcgComponentValidationMsg msg = (TcgComponentValidationMsg)msgList.get(i);
    String type = "";
    if (msg.getType().equals(VALIDATION_ERROR_KEY)) {
    type = "error";
    errorCnt++;
    } else {
    type = "warning";
    warningCnt++;
    }
    out.println("\t" + type + ": " + msg.getText(), msgListener);
    }
    }
    }
    }
    } catch (Exception e) {
    e.printStackTrace();
    }
    if (errorCnt == 0 && warningCnt == 0) {
    out.println(NbBundle.getMessage(PdCanvas.class,"PdCanvas.event_process_validates_succesfully"));
    }
    out.flush();
    }
     */
    // toggle the grid appearance
    void showGrid() {
        int style = getGridStyle();
        if (style == JGoView.GridInvisible) {
            style = JGoView.GridDot;
            setGridPen(JGoPen.black);
            setSnapMove(JGoView.SnapJump);
        } else {
            style = JGoView.GridInvisible;
            setSnapMove(JGoView.NoSnap);
        }
        setGridStyle(style);
    }

    void zoomIn() {
        double newscale = Math.rint(getScale() / 0.9f * 100f) / 100f;
        setScale(newscale);
    }

    void zoomOut() {
        double newscale = Math.rint(getScale() * 0.9f * 100f) / 100f;
        setScale(newscale);
    }

    void zoomNormal() {
        setScale(1.0d);
    }

    void zoomToFit() {
        double newscale = 1;
        if (!getDocument().isEmpty()) {
            double extentWidth = getExtentSize().width;
            double printWidth = getPrintDocumentSize().width;
            double extentHeight = getExtentSize().height;
            double printHeight = getPrintDocumentSize().height;
            newscale = Math.min((extentWidth / printWidth), (extentHeight / printHeight));
        }
        if (newscale > 2) {
            newscale = 1;
        }
        newscale *= getScale();
        setScale(newscale);
        setViewPosition(0, 0);
    }
    // Behavior
    public PlanCanvas() {
        super();
    }

    public PlanCanvas(IEPModel model) {
        this.mModel = model;
        this.mModelListener = new IEPModelListener();
        this.mModel.addComponentListener(this.mModelListener);
        this.setDocument(new PdModel(model));
        init();

        // vlv: print
        getCanvas().putClientProperty("print.printable", Boolean.TRUE); // NOI18N

//        mPopupMenu.add(new IEPTemplateAction(this.mModel.getModelSource().getLookup().lookup(PlanDataObject.class)));
        buildOperatorPopupMenu();
    }

    public void cleanup() {
        if (this.mModel != null && this.mModelListener != null) {
            this.mModel.removeComponentListener(this.mModelListener);
        }
    }
    // handle DELETE, HOME, and arrow keys as well as the page up/down keys
    public void onKeyEvent(KeyEvent evt) {
        PdModel doc = getDoc();
        int t = evt.getKeyCode();
        if (t == KeyEvent.VK_DELETE) {
            if (doc.isModifiable()) {
                doc.startTransaction();
                deleteSelection();
                doc.endTransaction("deleteByKey");
            }
        } else if (t == KeyEvent.VK_HOME) {
            setViewPosition(0, 0);
        } else if (t == KeyEvent.VK_RIGHT) {
            if (doc.isModifiable()) {
                doMoveSelection(0, getGridWidth(), 0, EventMouseUp);
            }
        } else if (t == KeyEvent.VK_LEFT) {
            if (doc.isModifiable()) {
                doMoveSelection(0, -getGridWidth(), 0, EventMouseUp);
            }
        } else if (t == KeyEvent.VK_DOWN) {
            if (doc.isModifiable()) {
                doMoveSelection(0, 0, getGridHeight(), EventMouseUp);
            }
        } else if (t == KeyEvent.VK_UP) {
            if (doc.isModifiable()) {
                doMoveSelection(0, 0, -getGridHeight(), EventMouseUp);
            }
        } else if (t == KeyEvent.VK_ESCAPE) {
            getSelection().clearSelection();
        } else if (Character.isLetter(evt.getKeyChar())) {
            if (!selectNextNode(evt.getKeyChar())) {
                Toolkit.getDefaultToolkit().beep();
            }
        } else if (t == KeyEvent.VK_F1) {
            JGoObject obj = getSelection().getPrimarySelection();
            if (obj != null && obj instanceof EntityNode) {
                EntityNode node = (EntityNode) obj;
                OperatorComponent comp = node.getModelComponent();
                final String helpID = comp.getString(PROP_HELP_ID);

                final Help help = Lookup.getDefault().lookup(Help.class);
                if (help != null) {
                    Runnable r = new Runnable() {

                        public void run() {
                            help.showHelp(new HelpCtx(helpID));
                        }
                    };

                    SwingUtilities.invokeLater(r);

                } else {
                    super.onKeyEvent(evt);
                }
            }

        } else if (t == KeyEvent.VK_TAB) {
            //allow tabbing on operator starting from left to right and going
            //downward
            mCanvasFocusHandler.moveFocusToNextOperator();
        } else {
            super.onKeyEvent(evt);
        }
    }
    private EntityNode lastSelectedNode = null;

    public void mouseSelect(EntityNode node) {
        selectObject(node);
        lastSelectedNode = node;
        node.mouseSelect(this);
    }

    public boolean doMouseDblClick(int modifiers, java.awt.Point dc, java.awt.Point vc) {
        boolean selectableOnly = true;
        JGoObject obj = pickDocObject(dc, selectableOnly);
        if (obj instanceof EntityNode &&
                getCurrentMouseEvent() != null) {
            mouseSelect((EntityNode) obj);
            doPopupCutomizer((EntityNode) obj);
            return true;
        }
        return super.doMouseDblClick(modifiers, dc, vc);
    }

    private void doPopupCutomizer(EntityNode node) {
        OperatorComponent comp = (OperatorComponent) node.getModelComponent();
        TcgComponentType compType = comp.getComponentType();
        if (!compType.hasPropertyType(PROP_PROPERTY_EDITOR)) {
            return;
        }
        try {
            //TcgComponentNode n = new TcgComponentNode(comp, mModel, node);
            TcgComponentNodeProperty p = TcgComponentNodeProperty.newCustomPropertyEditorInstance(comp, mModel);
            TcgComponentNodePropertyCustomizerDialogManager.showDialog(p);
        } catch (Exception e) {
        }

    /*TcgComponent comp = node.getComponent();
    if (!comp.hasProperty(PROPERTY_EDITOR_KEY)) {
    return;
    }
    try {
    TcgComponentNode n = new TcgComponentNode(comp, mDesigner.getPlan(), node);
    TcgComponentNodeProperty p = TcgComponentNodeProperty.newInstance(PROPERTY_EDITOR_KEY, n);
    TcgComponentNodePropertyCustomizerDialogManager.showDialog(p);
    } catch (Exception e) {
    }*/
    }

    public boolean doMouseDown(int modifiers, Point dc, Point vc) {
        boolean selectableOnly = true;
        mTempLink = null;
        mIsLinking = false;
        mTempPortForLink = null;

        JGoObject obj = pickDocObject(dc, selectableOnly);
        if (obj == null) {
            // Canvas is selected instead of things on the canvas
            // Show the properties of Plan
            //ritmDesigner.showPropertyPane(mDesigner.getPlan(), this);
            refreshProperties();
            return super.doMouseDown(modifiers, dc, vc);
        }
        // otherwise implement the default behavior
        return super.doMouseDown(modifiers, dc, vc);


    }

    @Override
    public boolean doMouseUp(int modifiers, Point dc, Point vc) {
        setCursor(mDefaultCursor);
        mTempLink = null;
        mIsLinking = false;
        mTempPortForLink = null;

        //when mouse is up after moving selection
        //then we want to update x,y in the model
        if (getState() == MouseStateMove) {
            updateSelectedEntityNodeLocation();
        }

        return super.doMouseUp(modifiers, dc, vc);
    }

    @Override
    protected void onMouseReleased(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            mLastPopupPoint = evt.getPoint();
            mPopupMenu.show(this, evt.getX(), evt.getY());

        }
        super.onMouseReleased(evt);
    }

    @Override
    public void doMoveSelection(int modifiers, int offsetx, int offsety, int event) {
        super.doMoveSelection(modifiers, offsetx, offsety, event);
    }

    public void updateOnAutoLayout(PdModel doc) {
        JGoListPosition pos = doc.getFirstObjectPos();
        if (pos != null) {
            OperatorComponent opComp = null;
            EntityNode node = null;
            // do it all in a single transaction so that the Undo Manager 
            // can undo the changes as part of one undo action.
            mModel.startTransaction();
            while (pos != null) {
                JGoObject obj = doc.getObjectAtPos(pos);
                if (obj instanceof EntityNode) {
                    node = (EntityNode) obj;
                    opComp = node.getModelComponent();
                    int x = node.getLocation().x;
                    int y = node.getLocation().y;
                    opComp.setInt(PROP_X, x);
                    opComp.setInt(PROP_Y, y);
                }
                // only consider top-level objects
                pos = doc.getNextObjectPosAtTop(pos);
            }
            mModel.endTransaction();
        }
    }

    private void updateSelectedEntityNodeLocation() {
        JGoSelection selection = getSelection();
        if (selection != null) {
            JGoListPosition pos = selection.getFirstObjectPos();
            while (pos != null) {
                JGoObject obj = selection.getObjectAtPos(pos);
                if (obj instanceof EntityNode) {
                    EntityNode node = (EntityNode) obj;
                    updateLocation(node);
                }
                pos = selection.getNextObjectPos(pos);
            }
        }
    }

    private void updateLocation(final EntityNode node) {

        //IOProvider.getDefault().getStdOut().println("************updateLocation :" + node.getLabelString() + " loca :"+ node.getLocation());

        Runnable r = new Runnable() {

            public void run() {
                OperatorComponent opComp = node.getModelComponent();
                int x = node.getLocation().x;
                int y = node.getLocation().y;
                int compx = opComp.getInt(PROP_X);
                int compy = opComp.getInt(PROP_Y);
                if (x != compx || y != compy) {
                    IEPModel model = opComp.getModel();
                    model.startTransaction();
                    opComp.setInt(PROP_X, x);
                    opComp.setInt(PROP_Y, y);
                    model.endTransaction();
                }
            }
        };

        SwingUtilities.invokeLater(r);

    }
    /*
     * Event that is notified when the underlying JGoDocument is changed. 
     * (non-Javadoc)
     * @see com.nwoods.jgo.JGoView#documentChanged(com.nwoods.jgo.JGoDocumentEvent)
     */

    public void documentChanged(JGoDocumentEvent evt) {
        switch (evt.getHint()) {
            case JGoDocumentEvent.CHANGED:
                Object o = evt.getObject(); // Source Object
                if (o != null && o instanceof SimpleNodeLabel) {
                    SimpleNodeLabel label = (SimpleNodeLabel) o;
                    String value = label.getText();
                    // set the new value on the backend model.
                    JGoArea area = label.getParent();
                    if (area != null && area instanceof EntityNode) {
                        EntityNode node = (EntityNode) area;
                        OperatorComponent comp = node.getModelComponent();
                        Property mProperty = comp.getProperty(PROP_NAME);
                        if (!mProperty.getValue().equals(value)) {
                            mProperty.getModel().startTransaction();
                            mProperty.setValue(value);
                            mProperty.getModel().endTransaction();
                        }
                        // refresh the node properties irrespective of model change
                        // as this user could have undone the change before commit (pressed enter).
                        node.refreshProperties();
                    }
                }
                break;
            case JGoDocumentEvent.REMOVED:
                repaint();
                break;
        }
        //ritmDesigner.setDirty();
        super.documentChanged(evt);
    }
    // implement JGoViewListener
    // just need to keep the actions enabled appropriately
    // depending on the selection
    public void viewChanged(JGoViewEvent e) {
        // if the selection changed, maybe some commands need to
        // be disabled or re-enabled
        switch (e.getHint()) {
            case JGoViewEvent.UPDATE_ALL:
            case JGoViewEvent.SELECTION_GAINED:
            case JGoViewEvent.SELECTION_LOST:
            case JGoViewEvent.SCALE_CHANGED:
                //ritPdAction.updateAllActions(getDesigner());
                break;
        }
    }
    // Override JGoView's newLink to force the creation of a Link
    // instead of JGoLink
    // let PdModel do the work
    public void newLink(JGoPort from, JGoPort to) {
        //ritgetDoc().startTransaction();

        //check if link already exist if so do not create a link
        if (!getDoc().isLinkExists(from, to)) {
            getDoc().newModelLink(from, to);
        }

    //ritfireUpdate(JGoViewEvent.LINK_CREATED, 0, l);
    //ritgetDoc().endTransaction("newLink");

    // In case the toNode was selected just before the new link is created,
    // refresh property pane to show the new property values
    //ritmDesigner.refreshPropertyPane();
    }

    public boolean matchesNodeLabel(EntityNode node, char c) {
        if (node == null) {
            return false;
        }
        String name = node.getText();
        return (name.length() > 0 && Character.toUpperCase(name.charAt(0)) == c);
    }

    public boolean selectNextNode(char c) {
        c = Character.toUpperCase(c);

        JGoDocument doc = getDocument();

        EntityNode startnode = null;
        JGoObject obj = getSelection().getPrimarySelection();
        if (obj != null && obj instanceof EntityNode) {
            startnode = (EntityNode) obj;
        }

        JGoListPosition startpos = null;
        if (startnode != null) {
            startpos = doc.findObject(startnode);
        }

        JGoListPosition pos = startpos;
        if (pos != null) {
            pos = doc.getNextObjectPosAtTop(pos);
        }

        while (pos != null) {
            obj = doc.getObjectAtPos(pos);
            pos = doc.getNextObjectPosAtTop(pos);

            if (obj instanceof EntityNode) {
                EntityNode pn = (EntityNode) obj;
                if (matchesNodeLabel(pn, c)) {
                    mouseSelect(pn);
                    scrollRectToVisible(pn.getBoundingRect());
                    return true;
                }
            }
        }
        pos = doc.getFirstObjectPos();
        while (pos != null && pos != startpos) {
            obj = doc.getObjectAtPos(pos);
            pos = doc.getNextObjectPosAtTop(pos);

            if (obj instanceof EntityNode) {
                EntityNode pn = (EntityNode) obj;
                if (matchesNodeLabel(pn, c)) {
                    mouseSelect(pn);
                    scrollRectToVisible(pn.getBoundingRect());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean validLink(JGoPort from, JGoPort to) {
        JGoObject fromObj = from.getParentNode();
        JGoObject toObj = to.getParentNode();
        if (!(fromObj instanceof EntityNode) || !(toObj instanceof EntityNode)) {
            return false;
        }

        if (fromObj.equals(toObj)) {
            return false;
        }

        EntityNode fromNode = (EntityNode) fromObj;
        EntityNode toNode = (EntityNode) toObj;

        if (fromNode.getOutputType().equals(IO_TYPE_TABLE)) {
            if (toNode.getStaticInputCount() >= toNode.getStaticInputMaxCount()) {
                return false;
            }
        } else if (fromNode.getOutputType().equals(IO_TYPE_RELATION) && toNode.isRelationInputStatic()) {
            if (toNode.getStaticInputCount() >= toNode.getStaticInputMaxCount()) {
                return false;
            }
        } else {
            // Check input count
            if (toNode.getInputCount() >= toNode.getInputMaxCount()) {
                return false;
            }
            // Check type compatibility
            if (!toNode.getInputType().equals(fromNode.getOutputType())) {
                return false;
            }
        }


        // Set a flag in the node being linked from
        boolean bReturn = true;
        int nFlags = fromNode.getFlags();
        fromNode.setFlags(fromNode.getFlags() | 0x10000);

        // Recursively traverse nodes starting from node being linked to
        // looking for the flag.  If found, we have a circular path.
        if (toObj instanceof EntityNode) {
            bReturn = !toNode.downstreamNodeContainsFlag(0x10000);
        }
        fromNode.setFlags(fromNode.getFlags() & ~(0x10000));
        boolean result = bReturn && super.validLink(from, to);
        return result;
    }
    //==================DnD Begin==================================
    private static boolean isDragAcceptable(DropTargetDragEvent e) {
        DataFlavor[] dfs = e.getCurrentDataFlavors();
        if ((dfs.length == 0) || e.isDataFlavorSupported(PaletteController.ITEM_DATA_FLAVOR)) {
            return true;
        }
        return false;
    }

    public void dragEnter(DropTargetDragEvent e) {
        if (!isDragAcceptable(e)) {
            super.dragEnter(e);
            return;
        }
        return;
    }

    public void dragOver(DropTargetDragEvent e) {
        if (!isDragAcceptable(e)) {
            super.dragOver(e);
            return;
        }
        return;
    }

    public void drop(DropTargetDropEvent e) {
        Transferable transferable = e.getTransferable();
        if (transferable.isDataFlavorSupported(PaletteController.ITEM_DATA_FLAVOR)) {
            try {
                Point vc = e.getLocation();
                Point dc = viewToDocCoords(vc);

                Lookup itemLookup = (Lookup) transferable.getTransferData(PaletteController.ITEM_DATA_FLAVOR);
                TcgActiveEditorDrop iaed = (TcgActiveEditorDrop) itemLookup.lookup(ActiveEditorDrop.class);
                handleAddNewOperator(iaed, dc);
            } catch (Exception ex) {
                mLog.log(Level.SEVERE, "drop failed.", ex);
            }

            return;
        }

        super.drop(e);
    }

    public void handleAddNewOperatorByPopup(TcgActiveEditorDrop iaed) {
        handleAddNewOperator(iaed, this.mLastPopupPoint);
    }

    private void handleAddNewOperator(TcgActiveEditorDrop iaed, Point p) {
        try {
            String ctPath = iaed.getPath();
            mLog.info("drop ctPath: " + ctPath);
            OperatorComponent operator = mModel.getFactory().createOperator(mModel, ctPath);
            OperatorComponentContainer opContainer = this.mModel.getPlanComponent().getOperatorComponentContainer();

            this.mModel.startTransaction();
            String name = NameGenerator.generateNewName(opContainer, operator.getComponentType());
            operator.setString(PROP_NAME, name);

            String id = NameGenerator.generateId(opContainer, "o");
            operator.setString(PROP_ID, id);
            operator.setName(id);
            operator.setTitle(id);

            operator.setInt(PROP_X, p.x);
            operator.setInt(PROP_Y, p.y);


            opContainer.addChildComponent(operator);

            this.mModel.endTransaction();

            //get this canvas into focus
            this.requestFocusInWindow();

        /*rit
        TcgComponentType type = TcgModelManager.getTcgComponentType(ctPath);
        Plan plan = mDesigner.getPlan();
        TcgComponent comp = plan.addNewOperator(type);
        Point vc = e.getLocation();
        Point dc = viewToDocCoords(vc);
        EntityNode node = new EntityNode(plan, comp, dc);
        PdModel model = getDoc();
        model.startTransaction();
        model.addObjectAtTail(node);
        fireUpdate(JGoViewEvent.EXTERNAL_OBJECTS_DROPPED, 0, null);
        model.endTransaction("newEntityNode");
         */
        } catch (Exception ex) {
            mLog.log(Level.SEVERE, "drop failed.", ex);
        }
    }

    public void dropActionChanged(DropTargetDragEvent e) {
        if (!isDragAcceptable(e)) {
            super.dropActionChanged(e);
            return;
        }
        return;
    }
    //==================DnD End ===================================
    //==================Cut,Copy,and Paste Begin===========================
    /**
     * Copy JGoObjects from the clipboard into this document.
     *
     * @param clipboard the clipboard supporting the standard JGoDocument
     *        DataFlavor containing objects to be copied to this document
     *        using the default copying method.
     */
    public JGoCopyEnvironment pasteFromClipboard(Clipboard clipboard) {
        Transferable contents = clipboard.getContents(this);
        DataFlavor jgoflavor = IEPComponentTransferable.NODE_DATA_FLAVOR;//JGoDocument.getStandardDataFlavor();
        if ((contents != null) && contents.isDataFlavorSupported(jgoflavor)) {
            try {
                PdModel doc = getDoc();
                if (doc != null) {
                    // copy objects from selection into this document
                    JGoObjectSimpleCollection sel =
                            (JGoObjectSimpleCollection) contents.getTransferData(jgoflavor);
                    if (doc.getDefaultLayer().isModifiable()) {
                        return doc.islandCopyFromCollection(sel, new Point(5, 5));
                    } else {
                        return null;
                    }
                }
            } catch (Exception e) {
                mLog.log(Level.SEVERE, "pasteFromClipbaord failed.", e);
            }
        }
        return null;
    }
    //==================Cut,Copy,and Paste End===========================
    public void deleteSelection() {
        //rit
    }
    // ================= Support for Testtools Begin============================
    public EntityNode findNodeByLabel(String label) {
        PdModel doc = getDoc();
        JGoListPosition pos = doc.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = doc.getObjectAtPos(pos);
            pos = doc.getNextObjectPos(pos);
            if (obj instanceof EntityNode) {
                EntityNode node = (EntityNode) obj;
                String name = node.getText();
                if (name.equals(label)) {
                    return node;
                }
            }
        }
        return null;
    }

    public Link findLink(String fromNodeLabel, String toNodeLabel) {
        PdModel doc = getDoc();
        JGoListPosition pos = doc.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = doc.getObjectAtPos(pos);
            pos = doc.getNextObjectPos(pos);
            if (obj instanceof Link) {
                Link link = (Link) obj;
                EntityNode fromNode = link.getFromNode();
                EntityNode toNode = link.getToNode();
                if (fromNode.getText().equals(fromNodeLabel) && toNode.getText().equals(toNodeLabel)) {
                    return link;
                }
            }
        }
        return null;
    }

    public boolean isNodeSelected(String label) {
        JGoSelection sel = getSelection();
        if (sel.isEmpty()) {
            return false;
        }
        JGoListPosition pos = sel.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = sel.getObjectAtPos(pos);
            pos = sel.getNextObjectPos(pos);
            if (obj instanceof EntityNode) {
                EntityNode node = (EntityNode) obj;
                if (node.getText().equals(label)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isLinkSelected(String fromNodeLabel, String toNodeLabel) {
        JGoSelection sel = getSelection();
        if (sel.isEmpty()) {
            return false;
        }
        JGoListPosition pos = sel.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = sel.getObjectAtPos(pos);
            pos = sel.getNextObjectPos(pos);
            if (obj instanceof Link) {
                Link link = (Link) obj;
                EntityNode fromNode = link.getFromNode();
                EntityNode toNode = link.getToNode();
                if (fromNode.getText().equals(fromNodeLabel) && toNode.getText().equals(toNodeLabel)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getContainerCount(Point p) {
        int cnt = 0;
        PdModel doc = getDoc();
        JGoListPosition pos = doc.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = doc.getObjectAtPos(pos);
            pos = doc.getNextObjectPos(pos);
            if (obj instanceof Link || obj instanceof EntityNode) {
                if (obj.isPointInObj(p)) {
                    cnt++;
                }
            }
        }
        return cnt;
    }

    public boolean isOrthogonalFlows() {
        return getDoc().isOrthogonalFlows();
    }
    // TcgCoomponentNodeView
    public void updateTcgComponentNodeView() {
    }

    private void init() {
        ActionMap map = this.getActionMap();
        map.put(DefaultEditorKit.cutAction, new CutAction(this));
        map.put(DefaultEditorKit.copyAction, new CopyAction(this));
        map.put(DefaultEditorKit.pasteAction, new PasteAction(this));
        map.put(DeleteAction.DELETE_NAME, new DeleteAction(this, mModel));
        InputMap inputMap = this.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK), DefaultEditorKit.cutAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK), DefaultEditorKit.copyAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK), DefaultEditorKit.pasteAction);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DeleteAction.DELETE_NAME);


        PlanComponent planComponent = this.mModel.getPlanComponent();
        OperatorComponentContainer opContainer = planComponent.getOperatorComponentContainer();
        if (opContainer != null) {
            PdModel model = getDoc();
            model.setIsReloading(true);

            List<OperatorComponent> operators = opContainer.getAllOperatorComponent();
            //restore operators

            Iterator<OperatorComponent> it = operators.iterator();

            while (it.hasNext()) {
                OperatorComponent operator = it.next();
                EntityNode node = new EntityNode(operator);
                model.addObjectAtTail(node);
            }

            //restore links
            LinkComponentContainer linkContainer = planComponent.getLinkComponentContainer();
            List<LinkComponent> links = linkContainer.getAllLinkComponents();

            Iterator<LinkComponent> itLink = links.iterator();
            while (itLink.hasNext()) {
                LinkComponent link = itLink.next();
                restoreLink(link);

            }

            model.setIsReloading(false);
        }


        setDefaultPortGravity(32);
        mDefaultCursor = getCursor();
        mNoLinkCursor = Utilities.createCustomCursor(this, mNoDropCursor.getImage(), "no drop");

        setPrimarySelectionColor(Color.ORANGE);

        mCanvasFocusHandler = new PlanCanvasFocusHandler(this);
    }

    private void restoreLink(LinkComponent link) {
        OperatorComponent fromComponent = link.getFrom();
        OperatorComponent toComponent = link.getTo();
        if (fromComponent != null && toComponent != null) {
            createLink(link, fromComponent, toComponent);
        }

    }

    private void createLink(LinkComponent link) {
        restoreLink(link);
    }

    private void createLink(LinkComponent link, OperatorComponent from, OperatorComponent to) {
        if (findLink(link) != null) {
            return;
        }

        EntityNode fromNode = findNode(from);
        EntityNode toNode = findNode(to);

        if (fromNode != null && toNode != null) {
            JGoPort fromPort = fromNode.getOutputPort();
            JGoPort toPort = toNode.getInputPort();

            if (fromPort != null && toPort != null) {
                PdModel model = getDoc();

                model.createLink(fromPort, toPort, link);

            }
        }
    }

    @Override
    public boolean doMouseMove(int modifiers, Point dc, Point vc) {
        boolean selectableOnly = true;
        JGoObject obj = pickDocObject(dc, selectableOnly);
        JGoObject viewObj = this.pickObject(dc, selectableOnly);
//        IOProvider.getDefault().getStdOut().println("************doc obj :" + obj + " view obj :"+ viewObj);

        JGoPort fromPort = null;

        if (mTempLink != null) {
            fromPort = mTempLink.getFromPort();
            if (fromPort.equals(mTempPortForLink)) {
                fromPort = mTempLink.getToPort();
            }
        }

        //when link is started we higlight invalid nodes once
        //till link creation is stopped
        if (mTempLink != null && !mIsLinking) {
            mIsLinking = true;
            highlightInvalidNodes(fromPort);
        }

        if (obj instanceof EntityNode) {
            mLastEntityNode = (EntityNode) obj;

            if (mTempLink != null) {

//                  check if this fromPort is input or output port.
                //because link can be drawn backward from input of an operator
                //to output of another operator.
                boolean isFromPortInput = false;


                EntityNode parentFromNode = (EntityNode) fromPort.getParentJGoNode();
                if (fromPort.equals(parentFromNode.getInputPort())) {
                    isFromPortInput = true;
                }

                OperatorComponent fromComp = parentFromNode.getModelComponent();
                OperatorComponent toComp = mLastEntityNode.getModelComponent();
                //IOProvider.getDefault().getStdOut().println("from " + fromComponent + " to "+ toComponent);

//                  we always validate link as if it is drawn from 
                //output port of an operator to input port of
                //another operator. if it is other way around
                //then we need to switch from and to since
                //or link validator logic assumes we are connecting
                //from output of an operator to input of another
                //operator
                OperatorComponent fromComponent = fromComp;
                OperatorComponent toComponent = toComp;

                if (isFromPortInput) {
                    fromComponent = toComponent;
                    toComponent = fromComp;
                }


                boolean validLink = OperatorLinkValidator.validateLink(fromComponent, toComponent);
                //if mouse is on a node which is not from node then
                //only we need to provide cursor and invalid port behaviour
                if (!parentFromNode.equals(mLastEntityNode)) {
                    //mLastEntityNode.showInvalidPorts(!validLink);
                    mTempLink.showInvalidLink(!validLink);
                    showInvalidCursor(!validLink);
                }

            }

        } else if (obj instanceof DocumentationNode) {
            showDocumentation(dc, (DocumentationNode) obj);
        } else if (!(viewObj instanceof DocumentationControl)) {
            if (docControl != null) {
                docControl.storeDocumentation();
                this.removeObject(docControl);
                docControl = null;
            }
            showingDocumentaion = false;
        }

        //if mouse is no longer on an entity node
        //and mouse was previously on an entity node
        //then make sure we remove invalid ports
        if (!(obj instanceof EntityNode)) {
            if (mLastEntityNode != null) {
                //mLastEntityNode.showInvalidPorts(false);
                mLastEntityNode = null;
                showInvalidCursor(false);
            }

            if (mTempLink != null) {
                mTempLink.showInvalidLink(false);
            }
        }

        if (mTempLink == null) {
            highlightInvalidNodes(true);
        }

        return super.doMouseMove(modifiers, dc, vc);
    }

    private void showDocumentation(Point p, DocumentationNode node) {
        if (!showingDocumentaion) {
            docControl = new DocumentationControl(this, (EntityNode) node.getParentNode());
            this.addObjectAtTail(docControl);
            docControl.setLocation(p);
            docControl.setSize(300, 150);
        //docControl.setDocumentation("test documentation blah blah");
        }
        showingDocumentaion = true;
    }

    @Override
    protected JGoPort createTemporaryPortForNewLink(JGoPort port, Point dc) {
        mTempPortForLink = super.createTemporaryPortForNewLink(port, dc);
        return mTempPortForLink;
    }

    @Override
    protected JGoLink createTemporaryLinkForNewLink(JGoPort from, JGoPort to) {
        //mTempLink =  super.createTemporaryLinkForNewLink(from, to);
        mTempLink = new Link(from, to);
        return mTempLink;
    }

    @Override
    public boolean startNewLink(JGoPort port, Point dc) {
        boolean result = super.startNewLink(port, dc);
        setCursor(mDefaultCursor);
        return result;
    }

    private void refreshWhenOperatorAdded(OperatorComponentContainer opContainer) {
        List<OperatorComponent> operators = opContainer.getAllOperatorComponent();
        Iterator<OperatorComponent> it = operators.iterator();

        while (it.hasNext()) {
            OperatorComponent oc = it.next();
            boolean isExists = isComponentExists(oc);
            if (!isExists) {
                final EntityNode node = new EntityNode(oc);
                PdModel model = getDoc();
                //model.startTransaction();
                model.addObjectAtTail(node);

                Runnable r = new Runnable() {

                    public void run() {
                        node.refreshProperties();
                        selectObject(node);
                    }
                };

                SwingUtilities.invokeLater(r);


            }
        }

    }

    private void refreshWhenOperatorAdded(final OperatorComponent comp) {
        EntityNode node = findNode(comp);
        if (node != null) {
            node.refresh();
        }
    }

    private void refreshWhenOperatorDeleted(OperatorComponentContainer opContainer) {
        List<OperatorComponent> operators = opContainer.getAllOperatorComponent();
        PdModel model = getDoc();
        List<EntityNode> nodes = new ArrayList<EntityNode>();

        JGoListPosition pos = model.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = model.getObjectAtPos(pos);
            pos = model.getNextObjectPos(pos);

            if (!obj.isTopLevel()) {
                continue;
            }
            if (obj instanceof EntityNode) {
                EntityNode node = (EntityNode) obj;
                Component c = node.getModelComponent();
                if (!operators.contains(c)) {
                    nodes.add(node);


                }
            }
        }


        Iterator<EntityNode> it = nodes.iterator();
        while (it.hasNext()) {
            EntityNode node = it.next();
            model.removeObject(node);
        }

        refreshProperties();

    }

    private void refreshWhenOperatorValueChanged(OperatorComponent opContainer) {
    }

    private void refreshWhenPropertyValueChanged(Property property) {

        if (property.getParent() instanceof OperatorComponent) {
            OperatorComponent comp = (OperatorComponent) property.getParent();
            EntityNode node = findNode(comp);
            if (node != null) {
                node.refreshOperator();
            }
        }

        //handle node update when this operators properties changes
        //in dialog and we need to update downstream nodes.
        String name = property.getName();
        if (PROP_INPUT_ID_LIST.equals(name) || PROP_STATIC_INPUT_ID_LIST.equals(name) || PROP_INPUT_SCHEMA_ID_LIST.equals(name) || PROP_OUTPUT_SCHEMA_ID.equals(name)) {
            Component parentComponent = property.getParentComponent();
            if (parentComponent instanceof OperatorComponent) {
                OperatorComponent oc = (OperatorComponent) parentComponent;
                updateReferingNodes(oc);
            }
        } else {
            IEPComponent parent = property.getParent();

            if (parent instanceof LinkComponent) {
                LinkComponent link = (LinkComponent) parent;
                LinkComponentContainer linkContainer = (LinkComponentContainer) link.getParent();

                //make sure any old invalid links are removed
                removeInvalidLinks(linkContainer);

                if (link.getFrom() != null && link.getTo() != null) {
                    //create link, if not already exists
                    createLink(link);
                }
            }

        }

        updateSelectedNodeProperties();
    }

    private void updateReferingNodes(OperatorComponent opComp) {
        EntityNode node = findNode(opComp);
        if (node != null) {
            node.updateDownstreamNodes();
        }
    }

    private void updateSelectedNodeProperties() {
        JGoSelection sel = getSelection();
        if (sel != null) {
            JGoListPosition pos = sel.getFirstObjectPos();
            if (pos != null) {
                JGoObject obj = sel.getObjectAtPos(pos);
                if (obj != null && obj instanceof EntityNode) {
                    EntityNode node = (EntityNode) obj;

                    //we call this only if selected component
                    //is different than the component whose property
                    //is changed, this avoids a loop
//                  if(!oc.equals(node.getModelComponent())) {
//                      node.updateDownstreamNodes();
                    node.refreshProperties();
//                  }
                }
            }
        }
    }

    private void refreshWhenLinkAdded(LinkComponentContainer linkContainer) {
        List<LinkComponent> linkComponents = linkContainer.getAllLinkComponents();
        Iterator<LinkComponent> it = linkComponents.iterator();

        while (it.hasNext()) {
            LinkComponent lc = it.next();
            boolean isExists = isComponentExists(lc);
            if (!isExists) {
                OperatorComponent fromComp = lc.getFrom();
                OperatorComponent toComp = lc.getTo();
                if (fromComp != null && toComp != null) {
                    createLink(lc, fromComp, toComp);
                }
            }
        }

    }

    private void refreshWhenLinkDeleted(LinkComponentContainer linkContainer) {
        List<LinkComponent> linkComponents = linkContainer.getAllLinkComponents();
        PdModel model = getDoc();
        List<Link> links = new ArrayList<Link>();

        JGoListPosition pos = model.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = model.getObjectAtPos(pos);
            pos = model.getNextObjectPos(pos);

            if (!obj.isTopLevel()) {
                continue;
            }
            if (obj instanceof Link) {
                Link link = (Link) obj;
                LinkComponent c = (LinkComponent) link.getModelComponent();
                if (!linkComponents.contains(c)) {
                    links.add(link);


                }
            }
        }


        Iterator<Link> it = links.iterator();
        while (it.hasNext()) {
            Link link = it.next();
            model.removeObject(link);
        }


    }

    private void removeInvalidLinks(LinkComponentContainer linkContainer) {
        List<LinkComponent> linkComponents = linkContainer.getAllLinkComponents();
        PdModel model = getDoc();
        List<Link> links = new ArrayList<Link>();

        JGoListPosition pos = model.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = model.getObjectAtPos(pos);
            pos = model.getNextObjectPos(pos);

            if (!obj.isTopLevel()) {
                continue;
            }
            if (obj instanceof Link) {
                Link link = (Link) obj;

                EntityNode fromNode = link.getFromNode();
                EntityNode toNode = link.getToNode();

                OperatorComponent fromComp = fromNode.getModelComponent();
                OperatorComponent toComp = toNode.getModelComponent();

                LinkComponent c = (LinkComponent) link.getModelComponent();

                boolean result = true;

                result &= fromComp != null ? fromComp.equals(c.getFrom()) : c.getFrom() == null;
                result &= toComp != null ? toComp.equals(c.getTo()) : c.getTo() == null;

                //check if link's from and to node's operator
                //component matches from and to of the link
                if (!result) {
                    links.add(link);
                }
            }
        }


        Iterator<Link> it = links.iterator();
        while (it.hasNext()) {
            Link link = it.next();
            model.removeObject(link);
        }


    }

    private boolean isComponentExists(Component component) {
        boolean result = false;
        PdModel model = getDoc();
        JGoListPosition pos = model.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = model.getObjectAtPos(pos);
            pos = model.getNextObjectPos(pos);

            if (!obj.isTopLevel()) {
                continue;
            }
            if (obj instanceof CanvasWidget) {
                CanvasWidget node = (CanvasWidget) obj;
                Component c = node.getModelComponent();
                if (c != null && c.equals(component)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    private EntityNode findNode(Component component) {
        EntityNode matchingNode = null;
        PdModel model = getDoc();

        JGoListPosition pos = model.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = model.getObjectAtPos(pos);
            pos = model.getNextObjectPos(pos);

            if (!obj.isTopLevel()) {
                continue;
            }
            if (obj instanceof EntityNode) {
                EntityNode node = (EntityNode) obj;
                Component c = node.getModelComponent();
                if (c != null && c.equals(component)) {
                    matchingNode = node;
                    break;
                }
            }
        }

        return matchingNode;
    }

    private Link findLink(LinkComponent component) {
        Link link = null;
        PdModel model = getDoc();

        JGoListPosition pos = model.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = model.getObjectAtPos(pos);
            pos = model.getNextObjectPos(pos);

            if (!obj.isTopLevel()) {
                continue;
            }
            if (obj instanceof Link) {
                Link l = (Link) obj;
                Component c = l.getModelComponent();
                if (c != null && c.equals(component)) {
                    link = l;
                    break;
                }
            }
        }

        return link;
    }

    class IEPModelListener implements ComponentListener {

        public void childrenAdded(ComponentEvent evt) {
            final Object source = evt.getSource();
            Runnable r = null;
            if (source instanceof OperatorComponentContainer) {
                r = new Runnable() {

                    public void run() {
                        refreshWhenOperatorAdded((OperatorComponentContainer) source);
                    }
                };

            } else if (source instanceof LinkComponentContainer) {
                r = new Runnable() {

                    public void run() {
                        refreshWhenLinkAdded((LinkComponentContainer) source);
                    }
                };

            } else if (source instanceof OperatorComponent) {
                r = new Runnable() {

                    public void run() {
                        refreshWhenOperatorAdded(((OperatorComponent) source));

                    }
                };
            }

            if (r != null) {
                SwingUtilities.invokeLater(r);
            }
        }

        public void childrenDeleted(ComponentEvent evt) {
            final Object source = evt.getSource();
            Runnable r = null;

            if (source instanceof OperatorComponentContainer) {
                r = new Runnable() {

                    public void run() {
                        refreshWhenOperatorDeleted((OperatorComponentContainer) source);
                    }
                };

            } else if (source instanceof LinkComponentContainer) {
                r = new Runnable() {

                    public void run() {
                        refreshWhenLinkDeleted((LinkComponentContainer) source);
                    }
                };

            } else if (source instanceof OperatorComponent) {
                //make sure documentation if deleted then
                //we update in selected node's property sheet

                r = new Runnable() {

                    public void run() {
                        updateSelectedNodeProperties();
                    }
                };

            }

            if (r != null) {
                SwingUtilities.invokeLater(r);
            }

        }

        public void valueChanged(ComponentEvent evt) {
            final Object source = evt.getSource();
            Runnable r = null;

            if (source instanceof OperatorComponent) {
                r = new Runnable() {

                    public void run() {
                        refreshWhenOperatorValueChanged((OperatorComponent) source);
                    }
                };


            } else if (source instanceof LinkComponentContainer) {
            } else if (source instanceof Property) {
                final Property p = (Property) source;

                r = new Runnable() {

                    public void run() {
                        refreshWhenPropertyValueChanged(p);
                    }
                };

            } else if (source instanceof Documentation) {
                r = new Runnable() {

                    public void run() {
                        updateSelectedNodeProperties();
                    }
                };

            }

            if (r != null) {
                SwingUtilities.invokeLater(r);
            }
        }
    }

    public void refreshProperties() {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if (tc != null) {
            DataObject dObj = tc.getLookup().lookup(DataObject.class);
            if (dObj != null) {
                tc.setActivatedNodes(new Node[]{new PlanNode(dObj.getNodeDelegate(), mModel.getPlanComponent())});
            } else {
                //Node node = new TcgComponentNode(mModel.getPlanComponent(), mModel, this);
                tc.setActivatedNodes(new Node[]{});
            }
        }
    }

    public void showInvalidCursor(boolean show) {
        if (show) {
            setCursor(mNoLinkCursor);
        } else {
            setCursor(mDefaultCursor);
        }
    }

    private void highlightInvalidNodes(JGoPort fromPort) {

//        IOProvider.getDefault().getStdOut().println("highlightInvalidNodes");

        EntityNode parentFromNode = (EntityNode) fromPort.getParentJGoNode();
        //check if this fromPort is input or output port.
        //because link can be drawn backward from input of an operator
        //to output of another operator.
        boolean isFromPortInput = false;

        if (fromPort.equals(parentFromNode.getInputPort())) {
            isFromPortInput = true;
        }

        OperatorComponent fromComp = parentFromNode.getModelComponent();
        PdModel model = getDoc();

        JGoListPosition pos = model.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = model.getObjectAtPos(pos);
            pos = model.getNextObjectPos(pos);

            if (!obj.isTopLevel()) {
                continue;
            }
            if (obj instanceof EntityNode) {
                EntityNode node = (EntityNode) obj;
                OperatorComponent toComp = node.getModelComponent();

                //we always validate link as if it is drawn from 
                //output port of an operator to input port of
                //another operator. if it is other way around
                //then we need to switch from and to since
                //or link validator logic assumes we are connecting
                //from output of an operator to input of another
                //operator
                OperatorComponent fromComponent = fromComp;
                OperatorComponent toComponent = toComp;


                if (isFromPortInput) {
                    fromComponent = toComponent;
                    toComponent = fromComp;
                }

                boolean isValid = OperatorLinkValidator.validateLink(fromComponent, toComponent);
                if (isFromPortInput) {
                    //if link is drawn backward from input port of one operator
                    //to output port of another operator (A-->B)
                    //then in this node we always have to show input port as invalid
                    node.showInvalidPorts(true, !isValid);
                } else {
                    //if link is drawn from output port of one operator to input port
                    // of another operator (A<--B)
//                  then in this node we always have to show output port as invalid
                    node.showInvalidPorts(!isValid, true);
                }

            }
        }

    }

    private void highlightInvalidNodes(boolean isValid) {
        PdModel model = getDoc();

        JGoListPosition pos = model.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = model.getObjectAtPos(pos);
            pos = model.getNextObjectPos(pos);

            if (!obj.isTopLevel()) {
                continue;
            }
            if (obj instanceof EntityNode) {
                EntityNode node = (EntityNode) obj;
                node.showInvalidPorts(!isValid, !isValid);
            }
        }

    }

    public static void showInformationDialog(String msg) {
        int msgType = NotifyDescriptor.INFORMATION_MESSAGE;
        NotifyDescriptor d = new NotifyDescriptor.Message(msg, msgType);
        DialogDisplayer.getDefault().notify(d);
    }

    private void buildOperatorPopupMenu() {

        PaletteController pc = IepPaletteFactory.getPalette();
        Lookup l = pc.getRoot();
        if (l != null) {
            Node rootNode = l.lookup(Node.class);
            if (rootNode != null) {
                Children ch = rootNode.getChildren();
                if (ch != null) {
                    Node[] nodes = ch.getNodes();
                    if (nodes != null) {
                        for (int i = 0; i < nodes.length; i++) {
                            Node c = nodes[i];
                            //go through each category
                            buildOperatorCategoryPopupMenu(c);
                        }
                    }

                }
            }
        }
    }

    private void buildOperatorCategoryPopupMenu(Node categoryNode) {
        JMenu categoryItem = new JMenu(categoryNode.getDisplayName());
        categoryItem.getAccessibleContext().setAccessibleName(categoryNode.getDisplayName());
        categoryItem.getAccessibleContext().setAccessibleDescription(categoryNode.getDisplayName());

        mPopupMenu.add(categoryItem);
        Children ch = categoryNode.getChildren();
        if (ch != null) {
            Node[] nodes = ch.getNodes();
            if (nodes != null) {
                for (int i = 0; i < nodes.length; i++) {
                    Node c = nodes[i];
                    OperatorPlanCanvasAction act = new OperatorPlanCanvasAction(c, this);
                    JMenuItem item = new JMenuItem(act);
                    item.getAccessibleContext().setAccessibleName(c.getDisplayName());
                    item.getAccessibleContext().setAccessibleDescription(c.getDisplayName());

//                    int intc = getNextAcceleratorKeyChar();
//                    
//                    
//                    item.setAccelerator(KeyStroke.getKeyStroke(
//                            intc, ActionEvent.ALT_MASK | ActionEvent.CTRL_MASK));
                    categoryItem.add(item);
                }
            }

        }
    }

    private int getNextAcceleratorKeyChar() {

        //starts with 0 to 9 numbers , ignore  0x40 as it is not
        //valid key then goes A to Z
        int accInt = 0x30;

        //26 alphabets

        Iterator<Integer> it = mAccList.iterator();
        while (it.hasNext()) {
            Integer accChar = it.next();
            if (accChar.intValue() == accInt || accInt == 0x40) {
                accInt++;
            }
        }




        mAccList.add(new Integer(accInt));

        return accInt;
    }
}
