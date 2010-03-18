/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.terminal.ioprovider;

import java.util.HashMap;
import java.util.Map;

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

@ServiceProvider(service = IOProvider.class, position=200)

public final class TerminalIOProvider extends IOProvider {

    private static final Map<String, InputOutput> map =
	    new HashMap<String, InputOutput>();

    @Override
    public String getName() {
        return "Terminal";      // NOI18N
    }

    @Override
    public InputOutput getIO(String name, Action[] additionalActions) {
	// FIXUP: to try from CND
	return getIO(name, true, additionalActions, null);
    }

    @Override
    public InputOutput getIO(String name, boolean newIO) {
	return getIO(name, newIO, null, null);
    }

    @Override
    public InputOutput getIO(String name, Action[] actions, IOContainer ioContainer) {
	return getIO(name, true, actions, ioContainer);
    }

    private InputOutput getIO(String name,
	                      boolean newIO,
			      Action[] actions,
			      IOContainer ioContainer) {
	InputOutput io = map.get(name);
	if (io == null || newIO) {
	    if (ioContainer == null)
		ioContainer = IOContainer.getDefault();
	    io = new TerminalInputOutput(name, actions, ioContainer);
	    map.put(name, io);
	}
	return io;
    }

    /**
     * This operation is not supported because standard Netbeans output are
     * is not Term based.
     * @return nothing. Always throws UnsupportedOperationException.
     */
    @Override
    public OutputWriter getStdOut() {
        throw new UnsupportedOperationException("Not supported yet.");	// NOI18N
    }

    static void remove(TerminalInputOutput io) {
	map.remove(io);
    }

}
