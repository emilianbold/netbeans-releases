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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.kenai.ui.dashboard.ColorManager;
import org.openide.util.Cancellable;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Node which creates its renderer component asynchronously.
 *
 * @param <T> The type of data displayed in this Node.
 *
 * @author S. Aubrecht
 */
public abstract class AsynchronousLeafNode<T> extends LeafNode {

    private JComponent inner = null;
    private JPanel panel;
    private JLabel lblTitle;
    private ProgressLabel lblLoading;
    private JLabel lblError;
    private boolean loaded = false;
    private Loader loader;
    private final Object LOCK = new Object();

    /**
     * C'tor
     * @param parent Node parent or null.
     * @param title Title to show in node's renderer while its actual content
     * is getting created, can be null.
     */
    public AsynchronousLeafNode( TreeListNode parent, String title ) {
        super( parent );
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        lblTitle = new TreeLabel(title);
        lblLoading = createProgressLabel(NbBundle.getMessage(AsynchronousLeafNode.class, "LBL_LoadingInProgress")); //NOI18N
        lblLoading.setForeground(ColorManager.getDefault().getDisabledColor());
        lblError = new TreeLabel(NbBundle.getMessage(AsynchronousLeafNode.class, "LBL_NotResponding")); //NOI18N
        lblError.setForeground(ColorManager.getDefault().getErrorColor());
        Image img = ImageUtilities.loadImage("org/netbeans/modules/kenai/ui/resources/error.png"); //NOI18N
        lblError.setIcon( new ImageIcon(img) );

        panel.add( lblTitle, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0) );
        panel.add( lblLoading, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0) );
        panel.add( lblError, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0) );
        panel.add( new JLabel(), new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0) );
    }

    @Override
    protected final JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus) {
        synchronized( LOCK ) {
            if( null != inner ) {
                configure( inner, foreground, background, isSelected, hasFocus );
            } else {
                if( !loaded ) {
                    if( null == loader ) {
                        startLoading();
                    }
                }
                if( isSelected ) {
                    lblLoading.setForeground(foreground);
                    lblError.setForeground(foreground);
                } else {
                    lblLoading.setForeground(ColorManager.getDefault().getDisabledColor());
                    lblError.setForeground(ColorManager.getDefault().getErrorColor());
                }
                lblTitle.setForeground(foreground);
            }
        }
        return panel;
    }

    /**
     * Configure renderer component's colors.
     * @param component Component return from createComponent() call.
     * @param foreground
     * @param background
     * @param isSelected
     * @param hasFocus
     */
    protected abstract void configure( JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus);

    /**
     * Creates node's renderer component.  The method is always invoked from AWT thread.
     * @return Renderer component, never null.
     */
    protected abstract JComponent createComponent( T data );

    /**
     * Retrieve data to display in this node. The method is called outside AWT
     * thread and can block indefinetely.
     * @return Node's data.
     */
    protected abstract T load();

    /**
     * Invoke this method to recreate node's renderer component.
     */
    protected final void refresh() {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                synchronized( LOCK ) {
                    loaded = false;
                    if( null != inner )
                        panel.remove(inner);
                    inner = null;
                    startLoading();
                }
            }
        });
    }

    private void startLoading() {
        synchronized( LOCK ) {
            loaded = false;
            lblLoading.setVisible(true);
            lblError.setVisible(false);
        }
        if( null != loader )
            loader.cancel();
        loader = new Loader();
        post(loader);
    }

    private void timedout() {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                synchronized( LOCK ) {
                    lblError.setVisible(true);
                    lblLoading.setVisible(false);
                    loaded = true;
                    loader = null;
                }
                fireContentChanged();
            }
        });
    }

    private void loaded( final T data ) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized( LOCK ) {
                    JComponent c = createComponent(data);
                    loaded = true;
                    if( null == c ) {
                        lblLoading.setVisible(false);
                        lblError.setVisible(true);
                    } else {
                        lblLoading.setVisible(false);
                        lblError.setVisible(false);
                        if( null != inner )
                            panel.remove(inner);
                        inner = c;
                        panel.remove(lblTitle);
                        panel.add( inner, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0) );
                    }
                    panel.invalidate();
                    panel.revalidate();
                    panel.repaint();
                    loader = null;
                }
                fireContentChanged();
            }
        });
    }

    private class Loader implements Runnable, Cancellable {

        private boolean cancelled = false;
        private Thread t = null;

        public void run() {
            final Object[] res = new Object[1];
            Runnable r = new Runnable() {
                public void run() {
                    res[0] = load();
                }
            };
            t = new Thread( r );
            t.start();
            try {
                t.join( TreeListNode.TIMEOUT_INTERVAL_MILLIS );
                if( null == res[0] ) {
                    timedout();
                    return;
                }
                if( cancelled )
                    return;

                loaded((T)res[0]);
            } catch( InterruptedException iE ) {
                if( !cancelled )
                    timedout();
            }
        }

        public boolean cancel() {
            cancelled = true;
            if( null != t ) {
                t.interrupt();
            }
            return true;
        }
    }
}

