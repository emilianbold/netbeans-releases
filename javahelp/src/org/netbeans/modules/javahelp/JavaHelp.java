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

package org.netbeans.modules.javahelp;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import javax.swing.*;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.JHelp;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import org.openide.ErrorManager;
import org.openide.util.*;
import org.openide.windows.WindowManager;

// [PENDING] should event dispatch thread be used thruout?

/** Help implementation using the JavaHelp 1.x system.
* @author Jesse Glick, Richard Gregor
*/
public final class JavaHelp extends AbstractHelp implements AWTEventListener {

    /** Make a JavaHelp implementation of the Help.Impl interface.
     *Or, use {@link #getDefaultJavaHelp}.
     */
    public JavaHelp() {
        Installer.err.log("JavaHelp created");
        if (!isModalExcludedSupported()) {
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.WINDOW_EVENT_MASK);
        }
    }
    void deactivate() {
        if (!isModalExcludedSupported()) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        }
    }

    // [PENDING] hold help sets weakly? softly? try to conserve memory...
    /** The master help set.
     */
    private HelpSet master = null;
    /** map from help sets to (soft refs to) components showing them */
    private Map availableJHelps = new HashMap(); // Map<HelpSet,Reference<JHelp>>
    /** viewer (may be invisible) showing help normally; null until first used; if invisible, is empty */
    private JFrame frameViewer = null;
    /** viewer showing help parented to current modal dialog; initially null */
    private JDialog dialogViewer = null;
    /** whether user explicitly closed dialog viewer.
     * true - frame viewer was initially open, then reparented to dialog viewer,
     * then user closes main dialog and we ought to reparent to frame viewer
     * false - frame viewer not initially open anyway, or it was but the user
     * explicitly closed it as a dialog viewer, we should leave it closed
     */
    private boolean reparentToFrameLater = false;
    /** the modal dialog(s) currently in effect */
    private Stack currentModalDialogs = new Stack(); // Stack<Dialog>
    /** modal dialogs stack has been used successfully */
    private boolean currentModalDialogsReady = false;
    /** last-displayed JHelp */
    private JHelp lastJH = null;
    
    /** progress of merging help sets; max is # of sets to merge */
    private static final BoundedRangeModel mergeModel = new DefaultBoundedRangeModel(0, 0, 0, 0);
    
    private ProgressHandle progressHandle = null;

    /** Get the master help set that others will be merged into.
     * @return the master help set
     */
    private synchronized HelpSet getMaster() {
        if (master == null) {
            ClassLoader loader = JavaHelp.class.getClassLoader();
            try {
                master = new HelpSet(loader, new URL("nbresloc:/org/netbeans/modules/javahelp/resources/masterHelpSet.xml")); // NOI18N
                Collection sets = getHelpSets();
                List toMerge = new ArrayList(Math.min(1, sets.size()));
                Iterator it = sets.iterator();
                while (it.hasNext()) {
                    HelpSet hs = (HelpSet) it.next();
                    if (shouldMerge(hs)) {
                        toMerge.add(hs);
                    }
                }
                mergeModel.setValue(0);
                mergeModel.setMaximum(toMerge.size());
                it = toMerge.iterator();
                while (it.hasNext()) {
                    HelpSet hs = (HelpSet) it.next();
                    master.add(hs);
                    mergeModel.setValue(mergeModel.getValue() + 1);
                }
            } catch (HelpSetException hse) {
                Installer.err.notify(hse);
                master = new HelpSet();
            } catch (MalformedURLException mfue) {
                mfue.printStackTrace();
                throw new IllegalStateException();
            }
        }
        return master;
    }
    
    /** Called when set of helpsets changes.
     * Here, clear the master helpset, since it may
     * need to have different contents (or a different
     * order of contents) when next viewed.
     */    
    protected void helpSetsChanged() {
        synchronized (this) {
            // XXX might be better to incrementally add/remove helpsets?
            // Unfortunately the JavaHelp API does not provide a way to
            // insert them except in last position, which prevents smart
            // navigator ordering.
            master = null;
        }
        mergeModel.setValue(0);
        mergeModel.setMaximum(0);
        super.helpSetsChanged();
    }
    
    private Dialog currentModalDialog() {
        if (currentModalDialogs.empty()) {
            Window w = HelpAction.WindowActivatedDetector.getCurrentActivatedWindow();
            if (!currentModalDialogsReady && (w instanceof Dialog) &&
                    !(w instanceof ProgressDialog) && w != dialogViewer && ((Dialog)w).isModal()) {
                // #21286. A modal dialog was opened before JavaHelp was even created.
                Installer.err.log("Early-opened modal dialog: " + w.getName() + " [" + ((Dialog)w).getTitle() + "]");
                return (Dialog)w;
            } else {
                return null;
            }
        } else {
            return (Dialog)currentModalDialogs.peek();
        }
    }
    
    private void ensureFrameViewer() {
        Installer.err.log("ensureFrameViewer");
        if (frameViewer == null) {
            Installer.err.log("\tcreating new");
            frameViewer = new JFrame();
            frameViewer.setIconImage(Utilities.loadImage("org/netbeans/modules/javahelp/resources/help.gif")); // NOI18N
            frameViewer.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavaHelp.class, "ACSD_JavaHelp_viewer"));
            
            if (isModalExcludedSupported()) {
                setModalExcluded(frameViewer);
                frameViewer.getRootPane().putClientProperty("netbeans.helpframe", Boolean.TRUE); // NOI18N
            }
        }
    }
    private void ensureDialogViewer() {
        Installer.err.log("ensureDialogViewer");
        Dialog parent = currentModalDialog();
        if (dialogViewer != null && dialogViewer.getOwner() != parent) {
            Installer.err.log("\tdisposing old");
            dialogViewer.setVisible(false);
            dialogViewer.dispose();
            dialogViewer = null;
        }
        if (dialogViewer == null) {
            Installer.err.log("\tcreating new");
            dialogViewer = new JDialog(parent);
            dialogViewer.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavaHelp.class, "ACSD_JavaHelp_viewer"));
        }
    }
    private void displayHelpInFrame(JHelp jh) {
        Installer.err.log("displayHelpInFrame");
        if (jh == null) jh = lastJH;
        if (jh == null) throw new IllegalStateException();
        boolean newFrameViewer = (frameViewer == null);
        ensureFrameViewer();
        if (dialogViewer != null) {
            Installer.err.log("\tdisposing old dialog");
            dialogViewer.setVisible(false);
            dialogViewer.getContentPane().removeAll();
            dialogViewer.dispose();
            dialogViewer = null;
        }
        if (frameViewer.getContentPane().getComponentCount() > 0 &&
                frameViewer.getContentPane().getComponent(0) != jh) {
            Installer.err.log("\treplacing content");
            frameViewer.getContentPane().removeAll();
        }
        if (frameViewer.getContentPane().getComponentCount() == 0) {
            Installer.err.log("\tadding content");
            frameViewer.getContentPane().add(jh, BorderLayout.CENTER);
            frameViewer.setTitle(jh.getModel().getHelpSet().getTitle());
            frameViewer.pack();
        }
        if (newFrameViewer) {
            // #22445: only do this stuff once when frame is made.
            // After that we need to remember the size and position.
            Dimension screenSize = Utilities.getUsableScreenBounds().getSize();
            Dimension frameSize = frameViewer.getSize();
            // #11018: have mercy on little screens
            if (frameSize.width > screenSize.width) {
                frameSize.width = screenSize.width;
                frameViewer.setSize(frameSize);
            }
            if (frameSize.height > screenSize.height) {
                frameSize.height = screenSize.height;
                frameViewer.setSize(frameSize);
            }
            // Now center it.
            frameViewer.setBounds(Utilities.findCenterBounds(frameSize));
        }

        frameViewer.setState(Frame.NORMAL);
        if (frameViewer.isVisible()) {
            frameViewer.repaint();
            frameViewer.toFront(); // #20048
            Installer.err.log("\talready visible, just repainting");
        } else {
            frameViewer.setVisible(true);
        }
        //#29417: This call of requestFocus causes lost focus when Help window
        //is reopened => removed.
        //frameViewer.requestFocus();
        lastJH = jh;
    }
    private void displayHelpInDialog(JHelp jh) {
        Installer.err.log("displayHelpInDialog");
        if (jh == null) jh = lastJH;
        if (jh == null) throw new IllegalStateException();
        ensureDialogViewer();
        Rectangle bounds = null;
        if (frameViewer != null) {
            Installer.err.log("\thiding old frame viewer");
            if (frameViewer.isVisible()) {
                bounds = frameViewer.getBounds();
                frameViewer.setVisible(false);
            }
            frameViewer.getContentPane().removeAll();
        }
        if (dialogViewer.getContentPane().getComponentCount() > 0 &&
                dialogViewer.getContentPane().getComponent(0) != jh) {
            Installer.err.log("\tchanging content");
            dialogViewer.getContentPane().removeAll();
        }
        if (dialogViewer.getContentPane().getComponentCount() == 0) {
            Installer.err.log("\tadding content");
            dialogViewer.getContentPane().add(jh, BorderLayout.CENTER);
            dialogViewer.setTitle(jh.getModel().getHelpSet().getTitle());
            dialogViewer.pack();
        }
        if (bounds != null) {
            Installer.err.log("\tcopying bounds from frame viewer: " + bounds);
            dialogViewer.setBounds(bounds);
        }
        rearrange(currentModalDialog());
        if (dialogViewer.isVisible()) {
            Installer.err.log("\talready visible, just repainting");
            dialogViewer.repaint();
        } else {
            dialogViewer.show();
        }
        lastJH = jh;
    }
    /*
    private void closeFrameViewer() {
        if (frameViewer == null || !frameViewer.isVisible()) throw new IllegalStateException();
        Installer.err.log("Closing frame viewer");
        frameViewer.setVisible(false);
        frameViewer.getContentPane().removeAll();
    }
    private void closeDialogViewer() {
        if (dialogViewer == null || !dialogViewer.isVisible()) throw new IllegalStateException();
        Installer.err.log("Closing dialog viewer");
        dialogViewer.setVisible(false);
        dialogViewer.getContentPane().removeAll();
        dialogViewer.dispose();
        dialogViewer = null;
    }
     */
    
    /** Show some help.
     *This is the basic call which should be used externally
     *and is the result of {@link TopManager#showHelp}.
     *Handles null contexts, missing or null help IDs, and null URLs.
     *If there is any problem, shows the master set
     *instead, or it may also create a new help window.
     *Works correctly if invoked while a modal dialog is open--creates a new modal
     *dialog with the help. Else creates a frame to view the help in.
     * @param ctx the help context to display
     * @param showmaster whether to show the master help set or not
     */
    public void showHelp(HelpCtx ctx, final boolean showmaster) {
        final HelpCtx ctx2 = (ctx != null) ? ctx : HelpCtx.DEFAULT_HELP;
        if (!SwingUtilities.isEventDispatchThread()) {
            Installer.err.log("showHelp later...");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showHelp(ctx2, showmaster);
                }
            });
            return;
        }
        Installer.err.log("showing help: " + ctx2);
        final HelpSet[] hs_ = new HelpSet[1];
        Runnable run = new Runnable() {
            public void run() {
                String id = ctx2.getHelpID();
                if (showmaster || ctx2.equals(HelpCtx.DEFAULT_HELP) || id == null) {
                    Installer.err.log("getting master...");
                    hs_[0] = getMaster();
                    Installer.err.log("getting master...done");
                }
                if (hs_[0] == null ||
                        /* #22670: if ID in hidden helpset, use that HS, even if showmaster */
                        (id != null && !hs_[0].getCombinedMap().isValidID(id, hs_[0]))) {
                    Installer.err.log("finding help set for " + id + "...");
                    hs_[0] = findHelpSetForID(id);
                    Installer.err.log("finding help set for " + id + "...done");
                }
            }
        };
        if (master == null) {
            // Computation required. Show the progress dialog and do the computation
            // in a separate thread. When finished, the progress dialog will hide
            // itself and control will return to event thread.
            Installer.err.log("showing progress dialog...");
            progressHandle = ProgressHandleFactory.createHandle("");
            createProgressDialog(run, currentModalDialog()).show();
            progressHandle.finish();
            Installer.err.log("dialog done.");
        } else {
            // Nothing much to do, run it synchronously in event thread.
            run.run();
        }
        HelpSet hs = hs_[0];
        if (hs == null) {
            // Interrupted dialog?
            return;
        }
        JHelp jh = createJHelp(hs);
        if (jh == null) throw new IllegalStateException();

        if (isModalExcludedSupported()) {
            displayHelpInFrame(jh);
        } else {
            if (currentModalDialog() == null) {
                Installer.err.log("showing as non-dialog");
                displayHelpInFrame(jh);
            } else {
                Installer.err.log("showing as dialog");
                displayHelpInDialog(jh);
            }
        }
        displayInJHelp(jh, ctx2.getHelpID(), ctx2.getHelp());
    }

    /** Handle modal dialogs opening and closing. Note reparentToFrameLater state = rTFL.
     * Cases:
     * 1. No viewer open. Dialog opened. Push it on stack. rTFL = false.
     * 2. No viewer open, !rTFL. Top dialog closed. Pop it.
     * 3. No viewer open, rTFL. Only top dialog closed. Pop it. Create frame viewer.
     * 4. No viewer open, rTFL. Some top dialog closed. Pop it. Create dialog viewer.
     * 5. Frame viewer open. Dialog opened. Push it. Close frame viewer. Create dialog viewer. rTFL = true.
     * 6. Dialog viewer open. Dialog opened. Push it. Reparent dialog viewer.
     * 7. Dialog viewer open. Viewer closed. rTFL = false.
     * It cannot happen that the dialog viewer is still open when the top dialog has been
     * closed, as AWT will automatically close the dialog viewer first. However in this case
     * it sends only CLOSED for the dialog viewer. If the user closes it, CLOSING is sent at
     * that time, and CLOSED also later when the main dialog is closed.
     */
    public void eventDispatched(AWTEvent awtev) {
        WindowEvent ev = (WindowEvent)awtev;
        int type = ev.getID();
        Window w = ev.getWindow();
        if (type == WindowEvent.WINDOW_CLOSING && w == dialogViewer) {
            Installer.err.log("7. Dialog viewer open. Viewer closed. rTFL = false.");
            reparentToFrameLater = false;
        }
        if (type != WindowEvent.WINDOW_CLOSED && type != WindowEvent.WINDOW_OPENED) {
            //Installer.err.log("uninteresting window event: " + ev);
            return;
        }
        if (w instanceof Dialog) {
            Dialog d = (Dialog)w;
            String dlgClass = d.getClass().getName();
            if ((d.isModal() && !(d instanceof ProgressDialog)) || d == dialogViewer) {
                //#40950: Print and Page Setup dialogs was not displayed from java help window.
                if ("sun.awt.windows.WPageDialog".equals(dlgClass) || // NOI18N
                    "sun.awt.windows.WPrintDialog".equals(dlgClass) || // NOI18N
                    "sun.print.ServiceDialog".equals(dlgClass) ||
                    "apple.awt.CPrinterJobDialog".equals(dlgClass) ||
                    "apple.awt.CPrinterPageDialog".equals(dlgClass)) { // NOI18N
                    //It is the print or print settings dialog for javahelp, do nothing
                    return;
                }
                
                //#47150: Race condition in toolkit if two dialogs are shown in a row
                if (d instanceof JDialog) {
                    if ("true".equals(((JDialog)d).getRootPane().getClientProperty("javahelp.ignore.modality"))) { //NOI18N
                        return;
                    }
                }
                
                if (Installer.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    Installer.err.log("modal (or viewer) dialog event: " + ev + " [" + d.getTitle() + "]");
                }
                if (type == WindowEvent.WINDOW_CLOSED) {
                    if (d == dialogViewer) {
                        // ignore, expected
                    } else if (d == currentModalDialog()) {
                        if (!currentModalDialogs.isEmpty()) {
                            currentModalDialogs.pop();
                            currentModalDialogsReady = true;
                        } else {
                            Installer.err.notify(ErrorManager.INFORMATIONAL, new IllegalStateException("Please see IZ #24993")); // NOI18N
                        }
                        showDialogStack();
                        if ((frameViewer == null || !frameViewer.isVisible() ||
                             /* 14393 */frameViewer.getState() == Frame.ICONIFIED) &&
                            (dialogViewer == null || !dialogViewer.isVisible())) {
                            if (!reparentToFrameLater) {
                                Installer.err.log("2. No viewer open, !rTFL. Top dialog closed. Pop it.");
                            } else if (currentModalDialog() == null) {
                                Installer.err.log("3. No viewer open, rTFL. Only top dialog closed. Pop it. Create frame viewer.");
                                //#47150 - reusing the old frame viewer can cause
                                //re-showing the frame viewer to re-show the dialog
                                if (frameViewer != null) {
                                    frameViewer.dispose();
                                    frameViewer = null;
                                }
                                displayHelpInFrame(null);
                            } else {
                                Installer.err.log("4. No viewer open, rTFL. Some top dialog closed. Pop it. Create dialog viewer.");
                                displayHelpInDialog(null);
                            }
                        } else if (dialogViewer != null && dialogViewer.isVisible()) {
                            Installer.err.log(ErrorManager.WARNING, "WARNING - dialogViewer should not still be open"); // NOI18N
                        } else {
                            Installer.err.log(ErrorManager.WARNING, "WARNING - frameViewer visible when a dialog was closing"); // NOI18N
                        }
                    } else {
                        Installer.err.log("some random modal dialog closed: " + d.getName() + " [" + d.getTitle() + "]");
                    }
                } else {
                    // WINDOW_OPENED
                    if (d != dialogViewer) {
                        currentModalDialogs.push(d);
                        showDialogStack();
                        if ((frameViewer == null || !frameViewer.isVisible() ||
                             /* 14393 */frameViewer.getState() == Frame.ICONIFIED) &&
                            (dialogViewer == null || !dialogViewer.isVisible())) {
                            Installer.err.log("1. No viewer open. Dialog opened. Push it on stack. rTFL = false.");
                            reparentToFrameLater = false;
                        } else if (frameViewer != null && frameViewer.isVisible()) {
                            Installer.err.log("5. Frame viewer open. Dialog opened. Push it. Close frame viewer. Create dialog viewer. rTFL = true.");
                            displayHelpInDialog(null);
                            reparentToFrameLater = true;
                        } else if (dialogViewer != null && dialogViewer.isVisible()) {
                            Installer.err.log("6. Dialog viewer open. Dialog opened. Push it. Reparent dialog viewer.");
                            displayHelpInDialog(null);
                        } else {
                            Installer.err.log(ErrorManager.WARNING, "WARNING - logic error"); // NOI18N
                        }
                    } else {
                        // dialog viewer opened, fine
                    }
                }
            } else {
                //Installer.err.log("nonmodal dialog event: " + ev);
            }
        } else {
            //Installer.err.log("frame event: " + ev);
        }
    }
    private void showDialogStack() {
        if (Installer.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            StringBuffer buf = new StringBuffer("new modal dialog stack: ["); // NOI18N
            boolean first = true;
            Iterator it = currentModalDialogs.iterator();
            while (it.hasNext()) {
                if (first) {
                    first = false;
                } else {
                    buf.append(", "); // NOI18N
                }
                buf.append(((Dialog)it.next()).getTitle());
            }
            buf.append("]"); // NOI18N
            Installer.err.log(buf.toString());
        }
    }
    
    /** If needed, visually rearrange dialogViewer and dlg on screen.
     * If they overlap, try to make them not overlap.
     * @param dlg the visible modal dialog
     */
    private void rearrange(Dialog dlg) {
        Rectangle r1 = dlg.getBounds();
        Rectangle r2 = dialogViewer.getBounds();
        if (r1.intersects(r2)) {
            Installer.err.log("modal dialog and dialog viewer overlap");
            Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
            int xExtra = s.width - r1.width - r2.width;
            int yExtra = s.height - r1.height - r2.height;
            if(xExtra >= yExtra){
                //compare y axes of r1 and r2 to know how to relocate them - horizontal relocation
                int r1Yaxis = r1.x + (r1.width/2);
                int r2Yaxis = r2.x + (r2.width/2);
                if(r1Yaxis <= r2Yaxis) {
                    Installer.err.log(" send help to the right");
                    if((r1.x + r1.width + r2.width) <= s.width) {
                        Installer.err.log("there is enough place fo help");
                        r2.x = r1.x + r1.width;
                    } else {
                        Installer.err.log("there is not enough place");
                        if((r1.width + r2.width) < s.width) {
                            Installer.err.log("relocate both");
                            r2.x = s.width - r2.width;
                            r1.x = r2.x - r1.width;
                        } else {
                            Installer.err.log("relocate both and resize help");
                            r1.x = 0;
                            r2.x = r1.width;
                            r2.width = s.width - r1.width;
                        }
                    }
                } else {
                    Installer.err.log("send help to the left");
                    if((r1.x - r2.width) > 0) {
                        Installer.err.log("there is enough place for help");
                        r2.x = r1.x - r2.width;
                    } else {
                        Installer.err.log("there is not enough place");
                        if((r1.width + r2.width) < s.width){
                            Installer.err.log("relocate both");
                            r2.x = 0;
                            r1.x = r2.width;
                        } else {
                            Installer.err.log("relocate both and resize help");
                            r1.x = s.width - r1.width;
                            r2.x = 0;
                            r2.width = r1.x;
                        }
                    }
                }
            } else {
                //compare x axes of r1 and r2 to know how to relocate them
                int r1Xaxis = r1.y + (r1.height/2);
                int r2Xaxis = r2.y + (r2.height/2);
                if(r1Xaxis <= r2Xaxis) {
                    Installer.err.log(" send help to the bottom");
                    if((r1.y + r1.height + r2.height) <= s.height) {
                        Installer.err.log("there is enough place fo help");
                        r2.y = r1.y + r1.height;
                    } else {
                        Installer.err.log("there is not enough place");
                        if((r1.height + r2.height) < s.height) {
                            Installer.err.log("relocate both");
                            r2.y = s.height - r2.height;
                            r1.y = r2.y - r1.height;
                        } else {
                            Installer.err.log("relocate both and resize help");
                            r1.y = 0;
                            r2.y = r1.height;
                            r2.height = s.height - r1.height;
                        }
                    }
                } else {
                    Installer.err.log("send help to the top");
                    if((r1.y - r2.height) > 0){
                        Installer.err.log("there is enough place for help");
                        r2.y = r1.y - r2.height;
                    } else {
                        Installer.err.log("there is not enough place");
                        if((r1.height + r2.height) < s.height) {
                            Installer.err.log("relocate both");
                            r2.y = 0;
                            r1.y = r2.height;
                        } else {
                            Installer.err.log("relocate both and resize help");
                            r1.y = s.height - r1.height;
                            r2.y = 0;  //or with -1
                            r2.height = r1.y;
                        }
                    }
                }
            }
            dlg.setBounds(r1);
            dialogViewer.setBounds(r2);
        }
    }
    
    /** Make a dialog showing progress of parsing & merging help sets.
     * Show it; when the runnable is done, it will hide itself.
     * @param run something to do while it is showing
     * @param parent dialog (may be null)
     * @return a new progress dialog
     */
    private JDialog createProgressDialog(Runnable run, Dialog parent) {
        return (parent == null) ?
        new ProgressDialog(run, WindowManager.getDefault().getMainWindow()) :
            new ProgressDialog(run, parent);
    }
    
    private final class ProgressDialog extends JDialog implements TaskListener, Runnable {
        private Runnable run;
        public ProgressDialog(Runnable run, Dialog parent) {
            super(parent, NbBundle.getMessage(JavaHelp.class, "TITLE_loading_help_sets"), true);
            init(run);
        }
        public ProgressDialog(Runnable run, Frame parent) {
            super(parent, NbBundle.getMessage(JavaHelp.class, "TITLE_loading_help_sets"), true);
            init(run);
        }
        private void init(Runnable run) {
            this.run = run;
            JComponent c = ProgressHandleFactory.createProgressComponent(progressHandle);
            c.setPreferredSize(new Dimension(3 * c.getPreferredSize().width, 3 * c.getPreferredSize().height));
            c.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            getContentPane().add(c);
            progressHandle.start();
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavaHelp.class, "ACSD_Loading_Dialog"));  //NOI18N
            pack();
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension me = getSize();
            setLocation((screen.width - me.width) / 2, (screen.height - me.height) / 2);
        }
        public void show() {
            if (run != null) {
                Installer.err.log("posting request from progress dialog...");
                getHelpLoader().post(run).addTaskListener(this);
                run = null;
            }
            super.show();
        }
        public void taskFinished(Task task) {
            Installer.err.log("posting request from progress dialog...request finished.");
            SwingUtilities.invokeLater(this);
        }
        public void run() {
            hide();
            dispose();
        }
    }
    
    private static RequestProcessor helpLoader = null;
    private static RequestProcessor getHelpLoader() {
        if (helpLoader == null) {
            helpLoader = new RequestProcessor("org.netbeans.modules.javahelp.JavaHelp"); // NOI18N
        }
        return helpLoader;
    }

    /** Find the proper help set for an ID.
    * @param id the ID to look up (may be null)
    * @return the proper help set (master if not otherwise found)
    */
    private HelpSet findHelpSetForID(String id) {
        if (id != null) {
            Iterator it = getHelpSets().iterator();
            while (it.hasNext()) {
                HelpSet hs = (HelpSet)it.next();
                if (hs.getCombinedMap().isValidID(id, hs))
                    return hs;
            }
            warnBadID(id);
        }
        return getMaster();
    }
    
    public Boolean isValidID(String id, boolean force) {
        if (force || helpSetsReady()) {
            Iterator it = getHelpSets().iterator();
            if (MASTER_ID.equals(id)) {
                if (it.hasNext()) {
                    Installer.err.log("master id, and >=1 help set");
                    return Boolean.TRUE;
                } else {
                    Installer.err.log("master id, and 0 help sets");
                    return Boolean.FALSE;
                }
            } else {
                // Regular ID.
                while (it.hasNext()) {
                    HelpSet hs = (HelpSet)it.next();
                    if (hs.getCombinedMap().isValidID(id, hs)) {
                        Installer.err.log("found normal valid id " + id + " in " + hs.getTitle());
                        return Boolean.TRUE;
                    }
                }
                Installer.err.log("did not find id " + id);
                return Boolean.FALSE;
            }
        } else {
            Installer.err.log("not checking " + id + " specifically");
            return null;
        }
    }

    /** Warn that an ID was not found in any help set.
    * @param id the help ID
    */
    private static void warnBadID(String id) {
        // PLEASE DO NOT COMMENT OUT...localized warning
        Installer.err.log(ErrorManager.WARNING, NbBundle.getMessage(JavaHelp.class, "MSG_jh_id_not_found", id));
    }

    /** Display something in a JHelp.
     *Handles {@link #MASTER_ID}, as well as help IDs
     *that were not found in any help set, various exceptions, etc.
     * @param jh the help component
     * @param helpID a help ID string to display, may be <CODE>null</CODE>
     * @param url a URL to display, may be <CODE>null</CODE>; lower priority than the help ID
     */
    private synchronized void displayInJHelp(JHelp jh, String helpID, URL url) {
        if (jh == null) throw new NullPointerException();
        if (jh.getModel() == null) throw new IllegalArgumentException();
        Installer.err.log("displayInJHelp: " + helpID + " " + url);
        try {
            if (helpID != null && ! helpID.equals(MASTER_ID)) {
                HelpSet hs = jh.getModel().getHelpSet();
                if (hs.getCombinedMap().isValidID(helpID, hs)) {
                    jh.setCurrentID(helpID);
                } else {
                    warnBadID(helpID);
                }
            } else if (url != null) {
                jh.setCurrentURL(url);
            }
        } catch (RuntimeException e) {
            Installer.err.notify(e);
        }
    }

    /** Create &amp; return a JHelp with the supplied help set.
     * In the case of the master help, will show the home page for
     * the distinguished help set if there is exactly one such,
     * or in the case of exactly one home page, will show that.
     * Caches the result and the result may be a reused JHelp.
     * @return the new JHelp
     * @param hs the help set to show
     */
    private JHelp createJHelp(HelpSet hs) {
        if (hs == null) throw new NullPointerException();
        JHelp jh;
        synchronized (availableJHelps) {
            Reference r = (Reference) availableJHelps.get(hs);
            if (r != null) {
                jh = (JHelp) r.get();
                if (jh != null) {
                    return jh;
                }
            }
        }
        String title = null; // for debugging purposes
        try {
            title = hs.getTitle();
            jh = new JHelp(hs);
        } catch (RuntimeException e) {
            if (title != null) {
                Installer.err.annotate(e, ErrorManager.UNKNOWN, "While trying to display: " + title, null, null, null); // NOI18N
            }
            Installer.err.notify(e);
            return new JHelp();
        }
        synchronized (availableJHelps) {
            availableJHelps.put(hs, new SoftReference(jh));
        }
        try {
            javax.help.Map.ID home = hs.getHomeID();
            if (home != null) {
                jh.setCurrentID(home);
            }
        } catch (Exception e) {
            Installer.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        return jh;
    }

    // XXX(ttran) see JDK bug 5092094 for details
    
    private static int modalExcludedSupported = -1;
    
    private static boolean isModalExcludedSupported() {
        if (modalExcludedSupported == -1) {
            modalExcludedSupported = 0;
            
            try {
                Class clazz = Class.forName("sun.awt.SunToolkit"); // NOI18N
                Method m = clazz.getMethod("isModalExcludedSupported", null); // NOI18N
                Boolean b = (Boolean) m.invoke(null, null);
                modalExcludedSupported = b.booleanValue() ? 1 : 0;
                Installer.err.log("isModalExcludedSupported = " + modalExcludedSupported); // NOI18N
            } catch (ThreadDeath ex) {
                throw ex;
            } catch (Throwable ex) {
                Installer.err.log("isModalExcludedSupported() failed  " + ex); // NOI18N
            }
        }
        
        return modalExcludedSupported == 1;
    }

    private static void setModalExcluded(Window window) {
        if (modalExcludedSupported == 0)
            return;
        
        try {
            Class clazz = Class.forName("sun.awt.SunToolkit"); // NOI18N
            Method m = clazz.getMethod("setModalExcluded", new Class [] { Window.class }); // NOI18N
            m.invoke(null, new Object[] { window });
        } catch (ThreadDeath ex) {
            throw ex;
        } catch (Throwable ex) {
            Installer.err.log("setModalExcluded(Window) failed  " + ex); // NOI18N
            modalExcludedSupported = 0;
        }
    }
}
