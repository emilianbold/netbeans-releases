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

package org.netbeans.modules.junit.output;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.lang.ref.WeakReference;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Marian Petras
 */
public class ResultWindow extends TopComponent {
    
    /** unique ID of <code>TopComponent</code> (singleton) */
    private static final String ID = "junit-test-results";              //NOI18N
    /**
     * instance/singleton of this class
     *
     * @see  #getInstance
     */
    private static WeakReference instance = null;
    
    /**
     * Returns a singleton of this class.
     *
     * @return  singleton of this class
     */
    static synchronized ResultWindow getInstance() {
        ResultWindow window;
        window = (ResultWindow) WindowManager.getDefault().findTopComponent(ID);
        if (window == null) {
            window = getDefault();
        }
        return window;
    }
    
    /**
     * Singleton accessor reserved for the window system only.
     * The window system calls this method to create an instance of this
     * <code>TopComponent</code> from a <code>.settings</code> file.
     * <p>
     * <em>This method should not be called anywhere except from the window
     * system's code.</em>
     *
     * @return  singleton - instance of this class
     */
    public static synchronized ResultWindow getDefault() {
        ResultWindow window;
        if ((instance == null)
                || ((window = (ResultWindow) instance.get()) == null)) {
            window = new ResultWindow();
            instance = new WeakReference(window);
        }
        return window;
    }
    
    
    /** */
    //private final JTabbedPane tabbedPanel;
    
    private final String indent = "   ";
    private final String[] indents = new String[] {
        "",
        "   ",
        "      ",
        "         ",
        "            ",
        "               " };
    private int callLevel = 0;
    private void logStart(final String msg) {
        StringBuffer buf = buildIndent();
        buf.append(msg);
        buf.append(" (0x");
        buf.append(Integer.toHexString(System.identityHashCode(this)));
        buf.append(") - START");
        System.out.println(buf.toString());
        callLevel++;
    }
    private void logEnd(final String msg) {
        callLevel--;
        StringBuffer buf = buildIndent();
        buf.append(msg);
        buf.append(" (0x");
        buf.append(Integer.toHexString(System.identityHashCode(this)));
        buf.append(") - END");
        System.out.println(buf.toString());
    }
    private void log(final String msg) {
        StringBuffer buf = buildIndent();
        buf.append("- ");
        buf.append(msg);
        buf.append(" (0x");
        buf.append(Integer.toHexString(System.identityHashCode(this)));
        buf.append(')');
        System.out.println(buf.toString());
    }
    private StringBuffer buildIndent() {
        StringBuffer buf = new StringBuffer(150);
        if (callLevel < indents.length) {
            buf.append(indents[callLevel]);
        } else {
            buf.append(indents[indents.length - 1]);
            final int count = callLevel - (indents.length - 1);
            for (int i = 0; i < count; i++) {
                buf.append(indent);
            }
        }
        return buf;
    }
    
    /** Creates a new instance of ResultWindow */
    public ResultWindow() {
        super();
        setLayout(new BorderLayout());
        //add(tabbedPanel = new JTabbedPane(), BorderLayout.CENTER);
        
        setName(ID);
        setDisplayName(NbBundle.getMessage(ResultView.class,
                                           "TITLE_TEST_RESULTS"));      //NOI18N
    }
    
    /**
     */
    void displayReport(final int index, final Report report, boolean promote) {
        assert EventQueue.isDispatchThread();
        
        final ResultView view = new ResultView(report);
        //tabbedPanel.add(view);
        removeAll();
        add(view, BorderLayout.CENTER);
//        tabbedPanel.add(new javax.swing.JLabel("Tab " + (tabbedPanel.getTabCount() + 1)));
        
        if (promote) {
            open();
            requestVisible();
        }
    }
    
    /**
     */
    void removeView(final int index) {
        //tabbedPanel.remove(index);
    }
    
    /**
     */
    int getViewsCount() {
        //return tabbedPanel.getTabCount();
        return 1;
    }
    
    /**
     */
    private void closeView(final int index) {
        //PENDING
    }
    
    /**
     */
    protected void componentClosed() {
        closeAllViews();
    }
    
    /**
     */
    private void closeAllViews() {
        //logStart("closeAllViews()");
        //tabbedPanel.removeAll();
        //logEnd("closeAllViews()");
    }
    
    /**
     */
    protected String preferredID() {
        return ID;
    }
    
    /**
     */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    /**
     * Resolves to the {@linkplain #getDefault default instance} of this class.
     *
     * This method is necessary for correct functinality of window system's
     * mechanism of persistence of top components.
     */
    private Object readResolve() throws java.io.ObjectStreamException {
        return ResultWindow.getDefault();
    }
    
}
