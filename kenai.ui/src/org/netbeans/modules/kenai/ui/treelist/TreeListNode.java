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

package org.netbeans.modules.kenai.ui.treelist;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.jdesktop.swingx.painter.BusyPainter;
import org.jdesktop.swingx.painter.PainterIcon;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * Representation of a single row in TreeList.<br>
 * If the node is expandable then its children are created asynchronously in a
 * separate thread to avoid blocking of AWT queue.<br>
 * Each node provides its own renderer component.
 *
 * @author S. Aubrecht
 */
public abstract class TreeListNode {

    /**
     * Time in milliseconds to wait for children creation to finish. When the interval
     * elapses then node's renderer shows an error message.
     */
    public static final long TIMEOUT_INTERVAL_MILLIS =
            NbPreferences.forModule(TreeListNode.class).getInt("node.expand.timeoutmillis", 5 * 60 * 1000); //NOI18N

    private final boolean expandable;
    private final TreeListNode parent;
    private TreeListListener listener;
    private boolean expanded = false;
    private ArrayList<TreeListNode> children = null;
    private final Object LOCK = new Object();

    private RendererPanel renderer;

    private ChildrenLoader loader;

    private static RequestProcessor rp = new RequestProcessor("Asynchronous Tree List Node", 5); // NOI18N

    protected static void post(Runnable run) {
        rp.post(run);
    }

    /**
     * C'tor
     * @param expandable True if the node provides some children
     * @param parent Node's parent or null if this node is root.
     */
    public TreeListNode( boolean expandable, TreeListNode parent ) {
        this.expandable = expandable;
        this.parent = parent;
    }

    public final boolean isExpandable() {
        return expandable;
    }

    public final TreeListNode getParent() {
        return parent;
    }

    public final List<TreeListNode> getChildren() {
        synchronized( LOCK ) {
            if( null == children )
                return Collections.emptyList();
            return new ArrayList<TreeListNode>(children);
        }
    }

    /**
     * @return Actions for popup menu, or null to disable popup menu.
     */
    public Action[] getPopupActions() {
        return null;
    }

    /**
     * This method is called outside AWT thread and may block indefinetely.
     * The list of children is cached until the call of refreshChildren() method.
     * @return Node's children or an empty list if no children are available,
     * never returns null.
     */
    protected abstract List<TreeListNode> createChildren();

    /**
     * Invoke this method when node's children must be reloaded.
     */
    protected final void refreshChildren() {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                synchronized( LOCK ) {
                    if( null != children ) {
                        for( TreeListNode node : children )
                            node.dispose();
                        children = null;
                        if( null != listener )
                            listener.childrenRemoved(TreeListNode.this);
                    }

                    if( expanded ) {
                        startLoadingChildren();
                    }
                }
            }
        });
    }
    
    final JComponent getRenderer( Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowHeight ) {
        RendererPanel res = null;
        synchronized( this ) {
            if( null == renderer ) {
                renderer = new RendererPanel( this );
            }
            res = renderer;
        }

        res.configure(foreground, background, isSelected, hasFocus, getNestingDepth(), rowHeight);

        return res;
    }

    /**
     * Creates component that will render this node in TreeList. The component
     * will be wrapped in another component to add proper background, border and expansion button.
     * @param foreground
     * @param background
     * @param isSelected
     * @param hasFocus
     * @return Component to render this node.
     */
    protected abstract JComponent getComponent( Color foreground, Color background, boolean isSelected, boolean hasFocus );

    /**
     * @return Action to invoke when Enter key is pressed on selected node in TreeList.
     */
    protected Action getDefaultAction() {
        return null;
    }

    /**
     * Notification that the loading of this node's children has started.
     * The method may get called several times without corresponding childrenLoadingFinished()
     * or childrenLoadingTimedout() calls as the loading thread may get cancelled.
     */
    protected void childrenLoadingStarted() {
    }

    /**
     * Notification that the loading of this node's children is finished.
     */
    protected void childrenLoadingFinished() {
    }

    /**
     * Notification that the loading of this node's children has timed out.
     */
    protected void childrenLoadingTimedout() {
    }

    final void setListener( TreeListListener listener ) {
        this.listener = listener;
    }

    /**
     * Invoked when the node is removed from the model. All listeners should be
     * removed here.
     * Always call super implementation to ensure that children node's (if any)
     * get disposed properly as well.
     */
    protected void dispose() {
        synchronized( LOCK ) {
            this.listener = null;
            if( null != children ) {
                for( TreeListNode node : children )
                    node.dispose();
            }
        }
    }

    final boolean isDescendantOf(TreeListNode grandParent) {
        if( null == parent )
            return false;
        if( parent.equals( grandParent ) )
            return true;
        return parent.isDescendantOf(grandParent);
    }

    final boolean isExpanded() {
        return expanded && isExpandable();
    }

    public final void setExpanded( boolean expanded ) {
        if( !isExpandable() )
            throw new IllegalStateException();
        if( this.expanded == expanded )
            return;
        this.expanded = expanded;
        if( null != listener ) {
            if( this.expanded ) {
                boolean childrenLoaded = true;
                synchronized( LOCK ) {
                    if( null == children ) {
                        childrenLoaded = false;
                        startLoadingChildren();
                    }
                }
                if( childrenLoaded )
                    listener.childrenAdded(this);
            } else {
                synchronized( LOCK ) {
                    if( null != loader ) {
                        loader.cancel();
                        childrenLoadingFinished();
                    }
                }
                listener.childrenRemoved(this);
            }
        }
    }

    final protected void fireContentChanged() {
        synchronized( this ) {
            renderer = null;
        }
        if( null != listener )
            listener.contentChanged(this);
    }

    final protected ProgressLabel createProgressLabel() {
        return createProgressLabel(NbBundle.getMessage(TreeListNode.class, "LBL_LoadingInProgress")); //NOI18N
    }

    final protected ProgressLabel createProgressLabel( String text ) {
        return new ProgressLabel(text, this);
    }

    final int getNestingDepth() {
        if( null == getParent() )
            return 0;
        return getParent().getNestingDepth() + 1;
    }

    private void startLoadingChildren() {
        childrenLoadingStarted();
        if( null != loader )
            loader.cancel();
        loader = new ChildrenLoader();
        post(loader);
    }

    private class ChildrenLoader implements Runnable, Cancellable {

        private boolean cancelled = false;
        private Thread t = null;

        public void run() {
            final List<TreeListNode> res[] = new ArrayList[1];
            Runnable r = new Runnable() {
                public void run() {
                    res[0] = createChildren();
                }
            };
            t = new Thread( r );
            t.start();
            try {
                t.join( TIMEOUT_INTERVAL_MILLIS );
            } catch( InterruptedException iE ) {
                //ignore
            }
            
            if( cancelled )
                return;
            
            if( null == res[0] ) {
                childrenLoadingTimedout();
                return;
            }

            synchronized( LOCK ) {
                children = new ArrayList<TreeListNode>(res[0]);
            }
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    if( null != listener ) {
                        listener.childrenAdded(TreeListNode.this);
                    }
                }
            });
            childrenLoadingFinished();
        }

        public boolean cancel() {
            cancelled = true;
            if( null != t ) {
                t.interrupt();
            }
            return true;
        }
    }

    public static final class ProgressLabel extends TreeLabel {
        private int frame = 0;
        private Timer t;
        final BusyPainter painter;
        private final Reference<TreeListNode> ref;

        public ProgressLabel( String text, TreeListNode nd ) {
            super( text );
            ref = new WeakReference <TreeListNode> (nd);
            painter = new BusyPainter(16);
            PainterIcon icon = new PainterIcon(new Dimension(16, 16));
            icon.setPainter(painter);
            setIcon(icon );
            t = new Timer(100, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //#183004 - The timer is never explicitly stopped (no
                    //guarantee that setVisible(false) will ever be called
                    //again.  This way, if the node it was rendering becomes
                    //unreferenced, this label can be collected.  Since it is
                    //no longer an inner class, although it may continue
                    //running on a timer, it will not hold a reference to the
                    //owning node
                    TreeListNode nd = ref.get();
                    if (nd == null) {
                        t.stop();
                        Container p = getParent();
                        if (p != null) {
                            p.remove(ProgressLabel.this);
                        }
                        return;
                    } else {
                        frame = (frame+1)%painter.getPoints();
                        painter.setFrame(frame);
                        ProgressLabel.this.repaint();
                        nd.fireContentChanged();
                    }
                }
            });
            t.setRepeats(true);
            super.setVisible(false);
        }

        @Override
        public void setVisible( boolean visible ) {
            boolean old = isVisible();
            super.setVisible(visible);
            if( old != visible ) {
                if( visible )
                    t.start();
                else {
                    t.stop();
                }
            }
        }

        /**
         * Stop the timer.  Make sure to call this method if you do not
         * explicitly call setVisible(false) on this label.  Otherwise, its
         * timer will keep running and it will be referenced forever.
         */
        public void stop() {
            t.stop();
        }

        //The usual cell-renderer performance overrides
        public void repaint() {
            //do nothing
        }

        @Override
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            //do nothing
        }

        @Override
        public void validate() {
            //do nothing
        }

        @Override
        public void invalidate() {
            //do nothing
        }
    }
}
