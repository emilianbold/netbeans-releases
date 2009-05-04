/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.terminal.api;

/**
 * Callback for when a hyperlink in a Terminal is clicked.
 * <p>
 * A hyperlink can be created by outputting a sequence like this:
 * <br>
 * <b>ESC</b>]10;<i>clientData</i>;<i>text</i><b>BEL</b>
 * @author ivan
 */
public interface HyperlinkListener {
    public void action(String clientData);
}
