/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.terminal.ioprovider;

import javax.swing.Action;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * An implementation of {@link IOProvider} based on
 * {@link org.netbeans.lib.terminalemulator.Term}.
 * <p>
 * This class is public to act as a signature for distinguishing us
 * from other IOProvider implementations:
 * <pre>
        IOProvider iop = null;
 *
        Lookup lookup = Lookup.getDefault();
 *
        Collection<? extends IOProvider> ioProviders = lookup.lookupAll(IOProvider.class);
 *
        for (IOProvider iopCandidate : ioProviders) {
            if (iopCandidate instanceof TerminalIOProvider)
                iop = iopCandidate;
        }
        if (iop == null)
            iop = IOProvider.getDefault();
        return iop;
 * </pre>
 * @author ivan
 */


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
        if (true)
            ioContainer = IOContainer.getDefault();
        return new TerminalInputOutput(name, ioContainer);

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
