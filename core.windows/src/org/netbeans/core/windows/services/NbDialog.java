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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.windows.services;

import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.openide.WizardDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

// XXx Before as org.netbeans.core.NbDialog

/** Default implementation of Dialog created from DialogDescriptor.
*
* @author Ian Formanek
*/
final class NbDialog extends NbPresenter {
    static final long serialVersionUID =-4508637164126678997L;

    /** Creates a new Dialog from specified DialogDescriptor
    * @param d The DialogDescriptor to create the dialog from
    * @param owner Owner of this dialog.
    */
    public NbDialog (DialogDescriptor d, Frame owner) {
        super (d, owner, d.isModal ());
        initGlassPane(d);
    }

    /** Creates a new Dialog from specified DialogDescriptor
    * @param d The DialogDescriptor to create the dialog from
    * @param owner Owner of this dialog.
    */
    public NbDialog (DialogDescriptor d, Dialog owner) {
        super (d, owner, d.isModal ());
        initGlassPane(d);
    }

    /** Geter for help.
    */
    @Override
    protected HelpCtx getHelpCtx () {
        return ((DialogDescriptor)descriptor).getHelpCtx ();
    }

    /** Options align.
    */
    @Override
    protected int getOptionsAlign () {
        return ((DialogDescriptor)descriptor).getOptionsAlign ();
    }

    /** Getter for button listener or null
    */
    @Override
    protected ActionListener getButtonListener () {
        return ((DialogDescriptor)descriptor).getButtonListener ();
    }

    /** Closing options.
    */
    @Override
    protected Object[] getClosingOptions () {
        return ((DialogDescriptor)descriptor).getClosingOptions ();
    }

    private GlassPane gp;
    private void initGlassPane( DialogDescriptor dd ) {
        if( !(dd instanceof WizardDescriptor) )
            return;
        WizardDescriptor wd = (WizardDescriptor) dd;
        Object imageName = wd.getProperty("OverlayImageName"); //NOI18N
        if( !(imageName instanceof String) )
            return;
        Image img = ImageUtilities.loadImage(imageName.toString(), true); //NOI18N
        if( null == img )
            return;
        gp = new GlassPane(img);
        setGlassPane( gp );
    }

    private static class GlassPane extends JComponent implements Runnable {
        /** background image */
        Image image;

        /** helper variables for passing image between threads and painting
         * methods */
        Image tempImage;

        /** helper variables for passing image between threads and painting
         * methods */
        Image image2Load;

        /** true if loading of image is in progress, false otherwise */
        boolean loadPending = false;

        /** sync lock for image variables access */
        private final Object IMAGE_LOCK = new Object();

        /** Constrcuts panel with given image on background.
         * @param im background image, null means default image
         */
        public GlassPane(Image im) {
            loadImage(im);
            setLayout(new BorderLayout());
            setOpaque(false);
            super.setVisible(true);
        }

        @Override
        public void setVisible(boolean aFlag) {
            //we want to be always visible
        }

        /** Overriden to paint backround image */
        @Override
        protected void paintComponent(Graphics g) {
            if (image != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                int y = getHeight() - image.getHeight(this) - 20;
                g2d.drawImage(image, 20, y, this);
            } else if (image2Load != null) {
                loadImageInBackground(image2Load);
                image2Load = null;
            }
        }

        private void loadImage(Image im) {
            // check image and just set variable if fully loaded already
            MediaTracker mt = new MediaTracker(this);
            mt.addImage(im, 0);

            if (mt.checkID(0)) {
                image = im;

                if (isShowing()) {
                    repaint();
                }

                return;
            }

            // start loading in background or just mark that loading should
            // start when paint is invoked
            if (isShowing()) {
                loadImageInBackground(im);
            } else {
                synchronized (IMAGE_LOCK) {
                    image = null;
                }

                image2Load = im;
            }
        }

        private void loadImageInBackground(Image image) {
            synchronized (IMAGE_LOCK) {
                tempImage = image;

                // coalesce with previous task if hasn't really started yet
                if (loadPending) {
                    return;
                }

                loadPending = true;
            }

            // 30ms is safety time to ensure code will run asynchronously
            RequestProcessor.getDefault().post(this, 30);
        }

        /** Loads image stored in image2Load variable.
         * Then invokes repaint when image is fully loaded.
         */
        public void run() {
            Image localImage;

            // grab value
            synchronized (IMAGE_LOCK) {
                localImage = tempImage;
                tempImage = null;
                loadPending = false;
            }

            // actually loads image
            ImageIcon localImageIcon = new ImageIcon(localImage);
            boolean shouldRepaint = false;

            synchronized (IMAGE_LOCK) {
                // don't commit results if another loading was started after us
                if (!loadPending) {
                    image = localImageIcon.getImage();

                    // keep repaint call out of sync section
                    shouldRepaint = true;
                }
            }

            if (shouldRepaint) {
                repaint();
            }
        }
    }
}
