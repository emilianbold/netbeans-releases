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

package threaddemo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.*;

/**
 * Displays information about any long pauses in the AWT thread.
 * @author Jesse Glick
 */
final class Monitor extends JPanel implements ActionListener {
    
    private static final int DELAY = 100; // msec between pings
    private static final long GRACE = 500; // msec permitted without access
    
    private long last;
    private final Timer t;
    private final JLabel blockageLabel;
    private final JLabel heapLabel;
    private static final NumberFormat format = new DecimalFormat("0.00");
    private int tick = 0;
    
    public Monitor() {
        t = new Timer(DELAY, this);
        t.setRepeats(true);
        last = System.currentTimeMillis();
        blockageLabel = new JLabel("No blockages longer than " + GRACE + "msec");
        heapLabel = new JLabel("Heap: XXXXXXXXXXXXXXXXX");
        add(blockageLabel);
        add(heapLabel);
        t.start();
    }
    
    public void actionPerformed(ActionEvent e) {
        if (tick++ % 10 == 0) {
            heapLabel.setText("Heap: " + format.format(heapUsedMega()) + "Mb");
        }
        long now = System.currentTimeMillis();
        long block = now - last;
        //System.err.println("timer: block=" + block);
        if (block > GRACE) {
            System.err.println("AWT blocked for " + block + "msec");
            blockageLabel.setText("Last big blockage for " + block + "msec");
            paintImmediately(0, 0, getWidth(), getHeight());
        }
        last = now;
    }
    
    private static float heapUsedMega() {
        Runtime r = Runtime.getRuntime();
        return (r.totalMemory() - r.freeMemory()) / 1024.0f / 1024.0f;
    }
    
}
