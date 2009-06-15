/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.javacard.shell;

import java.awt.Dimension;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Anki R. Nelaturu
 */
public class NoWrapTextPane extends JTextPane {

    public NoWrapTextPane(StyledDocument doc) {
        super(doc);
    }

    public NoWrapTextPane() {
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public void setSize(Dimension d) {
        if (d.width < getParent().getSize().width) {
            d.width = getParent().getSize().width;
        }
        super.setSize(d);
    }
}
