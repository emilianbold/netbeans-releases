/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.terminal.example.listenio;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import org.netbeans.terminal.example.TerminalIOProviderSupport;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Demonstrate ...
 * <ul>
 * <li>Internal output.
 * <li>IOProvider style hyperlinks.
 * <li>Closing via API.
 * </ul>
 * @author ivan
 */
public final class ListenIOAction implements ActionListener {

    private OutputWriter ow;

    private final OutputListener outputListener = new OutputListener() {

        public void outputLineSelected(OutputEvent ev) {
            ow.println("Got outputLineSelected()");
            ow.println(String.format("contents: '%s'", ev.getLine()));
        }

        public void outputLineAction(OutputEvent ev) {
            ow.println("Got outputLineAction()");
            ow.println(String.format("contents: '%s'", ev.getLine()));

            if ("Close me".equals(ev.getLine()))
                ev.getInputOutput().closeInputOutput();
        }

        public void outputLineCleared(OutputEvent ev) {
            ow.println("Got outputLineEvent()");
            ow.println(String.format("contents: '%s'", ev.getLine()));
        }
    };

    public void actionPerformed(ActionEvent e) {
        // Get a Term-based IOPRovider
        IOProvider iop = TerminalIOProviderSupport.getIOProvider();

        InputOutput io = iop.getIO("TermIOProvider hyperlinks", true);

        // Adds a line discipline so newlines etc work correctly
        TerminalIOProviderSupport.setInternal(io, true);

        ow = io.getOut();

        try {
            // print some stuff
            ow.println("Hello");

            // print some stuff with hyperlinks
            ow.println("Press me", outputListener);
            ow.println("Press me too", outputListener);
            ow.println("Close me", outputListener);

            ow.println("Goodbye");

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
