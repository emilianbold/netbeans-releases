/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.terminal.ioprovider;

import javax.swing.Action;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * An implementation of {@link IOProvider} based on
 * {@link org.netbeans.lib.terminalemulator.Term}.
 * Lookup id is "Terminal".
 * <p>
 * <pre>
	IOProvider iop = IOProvider.get("Terminal");
        if (iop == null)
            iop = IOProvider.getDefault();
 * </pre>
 * @author ivan
 */

@ServiceProvider(service = IOProvider.class, position=100)

public final class TerminalIOProvider extends IOProvider {
    @Override
    public String getName() {
        return "Terminal";      // NOI18N
    }

    @Override
    public InputOutput getIO(String name, Action[] additionalActions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputOutput getIO(String name, boolean newIO) {
        IOContainer ioContainer = null;
	ioContainer = IOContainer.getDefault();
        return new TerminalInputOutput(name, null, ioContainer);

    }

    @Override
    public InputOutput getIO(String name, Action[] actions, IOContainer ioContainer) {
        if (ioContainer == null)
            ioContainer = IOContainer.getDefault();
        return new TerminalInputOutput(name, actions, ioContainer);
    }

    /**
     * This operation is not supported because standard Netbeans output are
     * is not Term based.
     * @return nothing. Always throws UnsupportedOperationException.
     */
    @Override
    public OutputWriter getStdOut() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
