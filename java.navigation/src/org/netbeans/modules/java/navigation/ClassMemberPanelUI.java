/*
 * ClassMemberPanelUi.java
 *
 * Created on November 8, 2006, 4:03 PM
 */

package org.netbeans.modules.java.navigation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.TreePath;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.modules.java.navigation.ElementNode.Description;
import org.netbeans.modules.java.navigation.actions.FilterSubmenuAction;
import org.netbeans.modules.java.navigation.actions.SortActionSupport.SortByNameAction;
import org.netbeans.modules.java.navigation.actions.SortActionSupport.SortBySourceAction;
import org.netbeans.modules.java.navigation.base.FiltersManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.netbeans.modules.java.navigation.base.TapPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author  phrebejk
 */
public class ClassMemberPanelUI extends javax.swing.JPanel
        implements ExplorerManager.Provider, FiltersManager.FilterChangeListener, PropertyChangeListener {
    
    private ExplorerManager manager = new ExplorerManager();
    private MyBeanTreeView elementView;
    private TapPanel filtersPanel;
    private InstanceContent selectedNodes = new InstanceContent();
    private Lookup lookup = new AbstractLookup(selectedNodes);
    private ClassMemberFilters filters;
    
    private Action[] actions; // General actions for the panel
    
    private static final Rectangle ZERO = new Rectangle(0,0,1,1);

    private long lastShowWaitNodeTime = -1;
    private static final Logger PERF_LOG = Logger.getLogger(ClassMemberPanelUI.class.getName() + ".perf"); //NOI18N
    
    /** Creates new form ClassMemberPanelUi */
    public ClassMemberPanelUI() {
                      
        initComponents();
        manager.addPropertyChangeListener(this);
        
        // Tree view of the elements
        elementView = createBeanTreeView();        
        add(elementView, BorderLayout.CENTER);
               
        // filters
        filtersPanel = new TapPanel();
        filtersPanel.setOrientation(TapPanel.DOWN);
        // tooltip
        KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        String keyText = Utilities.keyToString(toggleKey);
        filtersPanel.setToolTipText(NbBundle.getMessage(ClassMemberPanelUI.class, "TIP_TapPanel", keyText)); //NOI18N
        
        filters = new ClassMemberFilters( this );
        filters.getInstance().hookChangeListener(this);
        JComponent buttons = filters.getComponent();
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        filtersPanel.add(buttons);
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            filtersPanel.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        
        actions = new Action[] {            
            new SortByNameAction( filters ),
            new SortBySourceAction( filters ),
            null,
            new FilterSubmenuAction(filters.getInstance())            
        };
        
        add(filtersPanel, BorderLayout.SOUTH);
        
        manager.setRootContext(ElementNode.getWaitNode());
        
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean result = super.requestFocusInWindow();
        elementView.requestFocusInWindow();
        return result;
    }
    
    public org.openide.util.Lookup getLookup() {
        // XXX Check for chenge of FileObject
        return lookup;
    }
    
    public org.netbeans.modules.java.navigation.ElementScanningTask getTask() {
        
        return new ElementScanningTask(this);
        
    }
    
    
    public void showWaitNode() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               elementView.setRootVisible(true);
               manager.setRootContext(ElementNode.getWaitNode());
               lastShowWaitNodeTime = System.currentTimeMillis();
            } 
        });
    }
    
    public void selectElementNode( ElementHandle<Element> eh ) {
        ElementNode root = getRootNode();
        if ( root == null ) {
            return;
        }
        ElementNode node = root.getNodeForElement(eh);
        try {
            manager.setSelectedNodes(new Node[]{ node == null ? getRootNode() : node });
        } catch (PropertyVetoException propertyVetoException) {
            Exceptions.printStackTrace(propertyVetoException);
        }
    }

    public void refresh( final Description description ) {
        
        final ElementNode rootNode = getRootNode();
        
        if ( rootNode != null && rootNode.getDescritption().fileObject.equals( description.fileObject) ) {
            // update
            //System.out.println("UPDATE ======" + description.fileObject.getName() );
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    rootNode.updateRecursively( description );
                }
            } );            
        } 
        else {
            //System.out.println("REFRES =====" + description.fileObject.getName() );
            // New fileobject => refresh completely
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    elementView.setRootVisible(false);        
                    manager.setRootContext(new ElementNode( description ) );
                    boolean scrollOnExpand = getScrollOnExpand();
                    setScrollOnExpand( false );
                    elementView.expandAll();
                    setScrollOnExpand( scrollOnExpand );

                    if (PERF_LOG.isLoggable(Level.FINE)) {
                        final long tm2 = System.currentTimeMillis();
                        final long tm1 = lastShowWaitNodeTime;
                        if (tm1 != -1) {
                            lastShowWaitNodeTime = -1;
                            PERF_LOG.log(Level.FINE,
                                String.format("ClassMemberPanelUI refresh took: %d ms", (tm2 - tm1)),
                                new Object[] { description.getFileObject().getName(), (tm2 - tm1) });
                        }
                    }
                }
            } );
            
        }
    }
    
    public void sort() {
        ElementNode root = getRootNode();
        if( null != root )
            root.refreshRecursively();
    }
    
    public ClassMemberFilters getFilters() {
        return filters;
    }
    
    public void expandNode( Node n ) {
        elementView.expandNode(n);
    }
    
    public Action[] getActions() {
        return actions;
    }
    
    public FileObject getFileObject() {
        final ElementNode root = getRootNode();
        if (root != null) {
            return root.getDescritption().fileObject;
        }
        else {
            return null;
        }        
    }
    
    // FilterChangeListener ----------------------------------------------------
    
    public void filterStateChanged(ChangeEvent e) {
        ElementNode root = getRootNode();
        
        if ( root != null ) {
            root.refreshRecursively();
        }
    }
    
    boolean getScrollOnExpand() {
        return null == elementView ? true : elementView.getScrollOnExpand();
    }
    
    void setScrollOnExpand( boolean scroll ) {
        if( null != elementView )
            elementView.setScrollOnExpand( scroll );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    // Private methods ---------------------------------------------------------
    
    private ElementNode getRootNode() {
        
        Node n = manager.getRootContext();
        if ( n instanceof ElementNode ) {
            return (ElementNode)n;
        }
        else {
            return null;
        }
    }
    
    private MyBeanTreeView createBeanTreeView() {
        return new MyBeanTreeView();
    }
    
    
    // ExplorerManager.Provider imlementation ----------------------------------
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    protected ElementJavadoc getJavaDocFor( ElementNode node ) {
        ElementNode root = getRootNode();
        if ( root == null ) {
            return null;
        }
        
        ElementHandle<? extends Element> eh = node.getDescritption().elementHandle;

        final JavaSource js = JavaSource.forFileObject( root.getDescritption().fileObject );
        if (js == null) {
            return null;
        }
        JavaDocCalculator calculator = new JavaDocCalculator( eh );
        final CompilationInfo[] ci = new CompilationInfo[1];
        
        try {
            js.runUserActionTask( calculator, true );
        } catch( IOException ioE ) {
            Exceptions.printStackTrace( ioE );
            return null;
        }
        
        return calculator.doc;
        
    }
    
    private static class JavaDocCalculator implements Task<CompilationController> {

        private ElementHandle<? extends Element> handle;
        private ElementJavadoc doc;
        
        public JavaDocCalculator( ElementHandle<? extends Element> handle ) {
            this.handle = handle;
        }


        public void run(CompilationController cc) throws Exception {
            cc.toPhase( JavaSource.Phase.UP_TO_DATE );
            
            Element e = handle.resolve( cc );
            doc = ElementJavadoc.create( cc, e );
        }
    };
        
    private class MyBeanTreeView extends BeanTreeView implements ToolTipManagerEx.ToolTipProvider {
        
        public MyBeanTreeView() {
            new ToolTipManagerEx( this );
            setUseSubstringInQuickSearch(true);
        }
        
        public boolean getScrollOnExpand() {
            return tree.getScrollsOnExpand();
}
        
        public void setScrollOnExpand( boolean scroll ) {
            this.tree.setScrollsOnExpand( scroll );
        }
        
        public JComponent getComponent() {
            return tree;
        }

        public String getToolTipText(Point loc) {
            ElementJavadoc doc = getDocumentation(loc);
            return null == doc ? null : doc.getText();
        }
        
        private ElementJavadoc getDocumentation( Point loc ) {
            TreePath path = tree.getPathForLocation( loc.x, loc.y );
            if( null == path )
                return null;
            Node node = Visualizer.findNode( path.getLastPathComponent() );
            if( node instanceof ElementNode ) {
                return getJavaDocFor( (ElementNode)node );
            }
            return null;
        }

        public Rectangle getToolTipSourceBounds(Point loc) {
            ElementNode root = getRootNode();
            if ( root == null ) {
                return null;
            }
            TreePath path = tree.getPathForLocation( loc.x, loc.y );
            return null == path ? null : tree.getPathBounds( path );
        }
        
        public Point getToolTipLocation( Point mouseLocation, Dimension tipSize ) {
            Point screenLocation = getLocationOnScreen();
            Rectangle sBounds = getGraphicsConfiguration().getBounds();
            Dimension compSize = getSize();
            Point res = new Point();
            Rectangle tooltipSrcRect = getToolTipSourceBounds( mouseLocation );
            //May be null, prevent the NPE, nothing will be shown anyway.
            if (tooltipSrcRect == null) {
                tooltipSrcRect = new Rectangle();
            }

            Point viewPosition = getViewport().getViewPosition();
            screenLocation.x -= viewPosition.x;
            screenLocation.y -= viewPosition.y;
            
            //first try bottom right
            res.x = screenLocation.x + compSize.width;
            res.y = screenLocation.y + tooltipSrcRect.y+tooltipSrcRect.height;

            if( res.x + tipSize.width <= sBounds.x+sBounds.width
                    && res.y + tipSize.height <= sBounds.y+sBounds.height ) {
                return res;
            }

            //upper right
            res.x = screenLocation.x + compSize.width;
            res.y = screenLocation.y + tooltipSrcRect.y - tipSize.height;

            if( res.x + tipSize.width <= sBounds.x+sBounds.width
                    && res.y >= sBounds.y ) {
                return res;
            }

            //lower left
            res.x = screenLocation.x - tipSize.width;
            res.y = screenLocation.y + tooltipSrcRect.y;

            if( res.x >= sBounds.x
                    && res.y + tipSize.height <= sBounds.y+sBounds.height ) {
                return res;
            }

            //upper left
            res.x = screenLocation.x - tipSize.width;
            res.y = screenLocation.y + tooltipSrcRect.y + tooltipSrcRect.height - tipSize.height;

            if( res.x >= sBounds.x && res.y >= sBounds.y ) {
                return res;
            }

            //give up (who's got such a small display anyway?)
            res.x = screenLocation.x + tooltipSrcRect.x;
            if( sBounds.y + sBounds.height - (screenLocation.y + tooltipSrcRect.y + tooltipSrcRect.height) 
                > screenLocation.y + tooltipSrcRect.y - sBounds.y ) {
                res.y = screenLocation.y + tooltipSrcRect.y + tooltipSrcRect.height;
            } else {
                res.y = screenLocation.y + tooltipSrcRect.y - tipSize.height;
            }

            return res;
        }

        public void invokeUserAction(final MouseEvent me) {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    if( null != me ) {
                        ElementJavadoc doc = getDocumentation( me.getPoint() );
                        JavadocTopComponent tc = JavadocTopComponent.findInstance();
                        if( null != tc ) {
                            tc.open();
                            tc.setJavadoc( doc );
                            tc.requestActive();
                        }
                    }
                }
            });
        }

        //#123940 start
        private boolean inHierarchy;
        private boolean doExpandAll;
        
        @Override
        public void addNotify() {
            super.addNotify();
            
            inHierarchy = true;
            
            if (doExpandAll) {
                super.expandAll();
                doExpandAll = false;
            }
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            
            inHierarchy = false;
        }

        @Override
        public void expandAll() {
            super.expandAll();
            
            if (!inHierarchy) {
                doExpandAll = true;
            }
        }
        //#123940 end
        
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            for (Node n:(Node[])evt.getOldValue()) {
                selectedNodes.remove(n);
            }
            for (Node n:(Node[])evt.getNewValue()) {
                selectedNodes.add(n);
            }
        }
    }
}
