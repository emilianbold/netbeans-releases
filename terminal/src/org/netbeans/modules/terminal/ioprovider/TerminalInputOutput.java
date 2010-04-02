/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.terminal.ioprovider;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;

import org.netbeans.lib.terminalemulator.ActiveRegion;
import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.lib.terminalemulator.ActiveTermListener;
import org.netbeans.lib.terminalemulator.Coord;
import org.netbeans.lib.terminalemulator.Extent;
import org.netbeans.lib.terminalemulator.LineDiscipline;
import org.netbeans.lib.terminalemulator.StreamTerm;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.lib.terminalemulator.TermListener;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import org.openide.windows.IOColorLines;
import org.openide.windows.IOColors;
import org.openide.windows.IOContainer;
import org.openide.windows.IOPosition;
import org.openide.windows.IOTab;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

import org.netbeans.modules.terminal.api.IOResizable;
import org.netbeans.modules.terminal.api.IOEmulation;
import org.netbeans.modules.terminal.api.IONotifier;
import org.netbeans.modules.terminal.api.IOTerm;
import org.netbeans.modules.terminal.api.IOVisibility;

import org.netbeans.modules.terminal.api.IOConnect;

/**
 * An implementation of {@link InputOutput} based on
 * {@link org.netbeans.lib.terminalemulator.Term}.
 * <p>
 * This class is public to allow access to the underlying Term.
 * <p>
 * A note on println()'s with OutputListeners:
 * <ul>
 * <li>
 * outputLineAction() works when hyperlinks are clicked.
 * <p>
 * <li>
 * outputLineCleared() didn't make much sense for output2 because output2 had
 * "infinte" history. However, it did make sense when the buffer was cleared.
 * <p>
 * For us issuing Cleared() when the buffer is cleared makes sense but isn't
 * implemented.
 * <br>
 * Issuing Cleared() when a hyperlink scrolls out of the history window
 * also makes sense and is even more work to implement.
 * <li>
 * outputLineSelected() tracked the "caret" in output2. However output2 was
 * "editor" based whereas we're a terminal and a terminals cursor is not
 * a caret ... it doesn't move around that much. (It can move under the
 * control of a program, like vi, but one doesn't generally use hyperlinks
 * in such situations).
 * <p>
 * Term can in principle notify when the cursor is hovering over a hyperlink
 * and perhaps that is the right time to issue Selected().
 * </ul>
 * @author ivan
 */
public final class TerminalInputOutput implements InputOutput, Lookup.Provider {

    private final IOContainer ioContainer;
    private final String name;

    private final Terminal terminal;
    private final StreamTerm term;
    private final TermListener termListener;

    private OutputWriter outputWriter;
    private OutputWriter errWriter;

    // shadow copies in support of IOTab
    private Icon icon;
    private String toolTipText;

    private final Lookup lookup = Lookups.fixed(new MyIOColorLines(),
                                                new MyIOColors(),
                                                new MyIOPosition(),
						new MyIOResizable(),
						new MyIOEmulation(),
						new MyIOTerm(),
                                                new MyIOTab(),
						new MyIOVisibility(),
						new MyIOConnect(),
						new MyIONotifier()
                                                );


    private final Map<Color, Integer> colorMap = new HashMap<Color, Integer>();
    private int allocatedColors = 0;

    private final Map<IOColors.OutputType, Color> typeColorMap =
        new EnumMap<IOColors.OutputType, Color>(IOColors.OutputType.class);

    private int outputColor = 0;

    private PropertyChangeSupport pcs;
    private VetoableChangeSupport vcs;

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    /* package */ PropertyChangeSupport pcs() {
	if (pcs == null)
	    pcs = new PropertyChangeSupport(this);
	return pcs;
    }

    /* package */ VetoableChangeSupport vcs() {
	if (vcs == null)
	    vcs = new VetoableChangeSupport(this);
	return vcs;
    }

    /**
     * Convert a Color to an ANSI Term color index.
     * @param color
     * @return
     */
    private int customColor(Color color) {
        if (color == null)
            return -1;

        if (!colorMap.containsKey(color)) {
            if (allocatedColors >= 8)
                return -1;  // ran out of slots for custom colors
            term().setCustomColor(allocatedColors, color);
            colorMap.put(color, (allocatedColors++)+50);
        }
        int customColor = colorMap.get(color);
        return customColor;
    }

    private void println(CharSequence text, Color color) {
        int customColor = customColor(color);
        if (customColor == -1) {        // ran out of colors
            getOut().println(text);
        } else {
            term().setAttribute(customColor);
            getOut().println(text);
            term().setAttribute(outputColor);
        }
    }

    private void println(CharSequence text, OutputListener listener, boolean important, Color color) {
        if ( !(term instanceof ActiveTerm))
            throw new UnsupportedOperationException("Term is not an ActiveTerm");	// NOI18N

        if (color == null) {
            // If color isn't overriden, use default colors.
            if (listener != null) {
                if (important)
                    color = typeColorMap.get(IOColors.OutputType.HYPERLINK_IMPORTANT);
                else
                    color = typeColorMap.get(IOColors.OutputType.HYPERLINK);
            } else {
                // color = typeColorMap.get(IOColors.OutputType.OUTPUT);
            }
        }

        ActiveTerm at = (ActiveTerm) term;
        if (listener != null) {
            ActiveRegion ar = at.beginRegion(true);
            ar.setUserObject(listener);
            ar.setLink(true);
            println(text, color);
            at.endRegion();
        } else {
            println(text, color);
        }
    }

    private void scrollTo(Coord coord) {
        term.possiblyNormalize(coord);
    }

    private class MyIOColorLines extends IOColorLines {
        @Override
        protected void println(CharSequence text, OutputListener listener, boolean important, Color color) {
            TerminalInputOutput.this.println(text, listener, important, color);
        }
    }

    private class MyIOColors extends IOColors {

        @Override
        protected Color getColor(OutputType type) {
            return typeColorMap.get(type);
        }

        @Override
        protected void setColor(OutputType type, Color color) {
            typeColorMap.put(type, color);
            if (type == OutputType.OUTPUT) {
                outputColor = customColor(color);
                if (outputColor == -1)
                    outputColor = 0;
                term.setAttribute(outputColor);
            }
        }
    }

    private static class MyPosition implements IOPosition.Position {
        private final TerminalInputOutput back;
        private final Coord coord;

        MyPosition(TerminalInputOutput back, Coord coord) {
            this.back = back;
            this.coord = coord;
        }

	@Override
        public void scrollTo() {
            back.scrollTo(coord);
        }
    }

    private class MyIOPosition extends IOPosition {

        @Override
        protected Position currentPosition() {
            return new MyPosition(TerminalInputOutput.this, term.getCursorCoord());
        }
    }

    private class MyIOTab extends IOTab {

        @Override
        protected Icon getIcon() {
            return icon;
        }

        @Override
        protected void setIcon(Icon icon) {
	    TerminalInputOutput.this.icon = icon;
	    ioContainer.setIcon(terminal, icon);
        }

        @Override
        protected String getToolTipText() {
	    return toolTipText;
        }

        @Override
        protected void setToolTipText(String text) {
	    TerminalInputOutput.this.toolTipText = toolTipText;
	    ioContainer.setToolTipText(terminal, toolTipText);
        }
    }

    /* LATER
    private class MyIOColorPrint extends IOColorPrint {

        private final Map<Color, Integer> colorMap = new HashMap<Color, Integer>();
        private int index = 0;

        public MyIOColorPrint() {
            // preset standard colors
            colorMap.put(Color.black, 30);
            colorMap.put(Color.red, 31);
            colorMap.put(Color.green, 32);
            colorMap.put(Color.yellow, 33);
            colorMap.put(Color.blue, 34);
            colorMap.put(Color.magenta, 35);
            colorMap.put(Color.cyan, 36);
            colorMap.put(Color.white, 37);
        }

        private int customColor(Color color) {
            if (!colorMap.containsKey(color)) {
                if (index >= 8)
                    return -1;  // ran out of slots for custom colors
                term().setCustomColor(index, color);
                colorMap.put(color, (index++)+50);
            }
            int customColor = colorMap.get(color);
            return customColor;

        }

        @Override
        protected void print(CharSequence text, Color color) {
            if ( !(term instanceof ActiveTerm))
                throw new UnsupportedOperationException("Term is not an ActiveTerm");

            int customColor = customColor(color);
            if (customColor == -1) {
                outputWriter.print(text);
            } else {
                term().setAttribute(customColor);
                outputWriter.print(text);
                term().setAttribute(0);
            }
        }
    }
    */

    private static final class MyIOResizable extends IOResizable {
    }

    private class MyIOEmulation extends IOEmulation {

	private boolean disciplined = false;

	@Override
	protected String getEmulation() {
	    return term.getEmulation();
	}

	@Override
	protected boolean isDisciplined() {
	    return disciplined;
	}

	@Override
	protected void setDisciplined() {
	    if (this.disciplined)
		return;
	    this.disciplined = true;
	    if (disciplined)
		term.pushStream(new LineDiscipline());
	}
    }

    private class MyIOVisibility extends IOVisibility {

	@Override
	protected void setVisible(boolean visible) {
	    final Task task;
	    if (visible) {
		task = new Task.Select(ioContainer, terminal);
	    } else {
		task = new Task.DeSelect(ioContainer, terminal);
	    }
	    task.dispatch();
	}

	@Override
	protected void setClosable(boolean closable) {
	    terminal.setClosable(closable);
	}

	@Override
	protected boolean isSupported() {
	    return true;
	    // LATER return ioContainer instanceof TerminalContainerImpl;
	    // We really can't do the above.
	    // However after IOVisibilityControl.isClosable() switches to
	    // the push model we'll be able to answer this question more
	    // accurately by asking ioContainer if it has the IOClosability
	    // capability.
	}
    }

    private class MyIOConnect extends IOConnect {

	@Override
	protected boolean isConnected() {
	    return terminal.isConnected();
	}

	@Override
	protected void disconnectAll(Runnable continuation) {
	    // don't use getOut().close() as convenient as that might be
	    // because getOut() will change states and fire properties.
	    terminal.setOutConnected(false);	// also "closes" Err
	    IOTerm.disconnect(TerminalInputOutput.this, continuation);
	}
    }

    private class MyIOTerm extends IOTerm {

	@Override
	protected Term term() {
	    return term;
	}

	@Override
	protected void connect(OutputStream pin, InputStream pout, InputStream perr) {
	    term.connect(pin, pout, perr);
	    terminal.setExtConnected(true);
	}

	@Override
	protected void disconnect(final Runnable continuation) {
	    // Wrap 'continuation' in another one so we can
	    // set the extConnected state at the right time.
	    term.disconnect(new Runnable() {
		@Override
		public void run() {
		    terminal.setExtConnected(false);
		    if (continuation != null)
			continuation.run();
		}
	    });
	}
    }

    private class MyIONotifier extends IONotifier {

	@Override
	protected void addPropertyChangeListener(PropertyChangeListener listener) {
	    pcs().addPropertyChangeListener(listener);
	}

	@Override
	protected void removePropertyChangeListener(PropertyChangeListener listener) {
	    pcs().removePropertyChangeListener(listener);
	}

	@Override
	public void addVetoableChangeListener(VetoableChangeListener listener ) {
	    vcs().addVetoableChangeListener(listener);
	}

	@Override
	public void removeVetoableChangeListener(VetoableChangeListener listener ) {
	    vcs().removeVetoableChangeListener(listener);
	}
    }


    /**
     * Delegate prints and writes to a Term via TermWriter.
     */
    private class TermOutputWriter extends OutputWriter {
	private final Terminal owner;

        TermOutputWriter(Terminal owner) {
            super(term.getOut());
	    this.owner = owner;
        }

        @Override
        public void println(String s, OutputListener l) throws IOException {
            TerminalInputOutput.this.println(s, l, false, null);
        }

        @Override
        public void println(String s, OutputListener l, boolean important) throws IOException {
            TerminalInputOutput.this.println(s, l, important, null);
        }

        @Override
        public void reset() throws IOException {
            term.clearHistory();
        }

	@Override
	public void close() {
	    // Don't really close it
	    // super.close();
	    owner.setOutConnected(false);
	}
    }

    /**
     * Delegate prints and writes to a Term via TermWriter.
     */
    private final class TermErrWriter extends OutputWriter {
	private final Terminal owner;

	TermErrWriter(Terminal owner) {
	    super(term.getOut());
	    this.owner = owner;
	}

	@Override
	public void println(String s, OutputListener l) throws IOException {
	    TerminalInputOutput.this.println(s, l, false, Color.red);
	}

	@Override
	public void println(String s, OutputListener l, boolean important) throws IOException {
	    TerminalInputOutput.this.println(s, l, important, Color.red);
	}

	@Override
	public void println(String x) {
	    TerminalInputOutput.this.println(x, Color.red);
	}

	@Override
	public void reset() throws IOException {
	    // no-op
	}

	@Override
	public void close() {
	    // Don't really close it
	    // super.close();
	    owner.setErrConnected(false);
	}
    }

    private static class TerminalOutputEvent extends OutputEvent {
        private final String text;

        public TerminalOutputEvent(InputOutput io, String text) {
            super(io);
            this.text = text;
        }

        @Override
        public String getLine() {
            return text;
        }
    }

    /**
     * Adapter to forward Term size change events as property changes.
     */
    private class MyTermListener implements TermListener {
	@Override
	public void sizeChanged(Dimension cells, Dimension pixels) {
	    IOResizable.Size size = new IOResizable.Size(cells, pixels);
	    pcs().firePropertyChange(IOResizable.PROP_SIZE, null, size);
	}
    }

    TerminalInputOutput(String name, Action[] actions, IOContainer ioContainer) {
	this.name = name;
        this.ioContainer = ioContainer;

        terminal = new Terminal(ioContainer, this, actions, name);

	Task task = new Task.Add(ioContainer, terminal);
	task.dispatch();

        term = terminal.term();

        if (! (term instanceof ActiveTerm)) {
	    termListener = null;
            return;
	}

	termListener = new MyTermListener();
	term.addListener(termListener);

        ActiveTerm at = (ActiveTerm) term;

        // Set up to convert clicks on active regions, created by OutputWriter.
        // println(), to outputLineAction notifications.
        at.setActionListener(new ActiveTermListener() {
	    @Override
            public void action(ActiveRegion r, InputEvent e) {
                OutputListener ol = (OutputListener) r.getUserObject();
                if (ol == null)
                    return;
                Extent extent = r.getExtent();
                String text = term.textWithin(extent.begin, extent.end);
                OutputEvent oe =
                    new TerminalOutputEvent(TerminalInputOutput.this, text);
                ol.outputLineAction(oe);
            }
        });

        // preset standard colors
        colorMap.put(Color.black, 30);
        colorMap.put(Color.red, 31);
        colorMap.put(Color.green, 32);
        colorMap.put(Color.yellow, 33);
        colorMap.put(Color.blue, 34);
        colorMap.put(Color.magenta, 35);
        colorMap.put(Color.cyan, 36);
        colorMap.put(Color.white, 37);
    }

    void dispose() {
        final ActiveTerm at = (ActiveTerm) term;
        at.setActionListener(null);
	term.removeListener(termListener);
	if (outputWriter != null) {
	    // LATER outputWriter.dispose();
	    outputWriter = null;
	}
	// LATER getIn().eof();
	// LATER focusTaken = null;
    }


    public StreamTerm term() {
        return term;
    }

    Terminal terminal() {
        return terminal;
    }

    String name() {
	return name;
    }


    /**
     * Stream to write to stuff being output by the process destined for the
     * terminal.
     * @return the writer.
     */
    @Override
    public OutputWriter getOut() {
        if (outputWriter == null)
            outputWriter = new TermOutputWriter(terminal);
	terminal.setOutConnected(true);
        return outputWriter;
    }

    /**
     * Stream to read from stuff typed into the terminal destined for the process.
     * @return the reader.
     */
    @Override
    public Reader getIn() {
	return term.getIn();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Output written to this Writer may appear in a different tab (not
     * supported) or different color (easily doable).
     * <p>
     * I'm hesitant to implement this because traditionally separation of
     * stdout and stderr (as done by {@link Process#getErrorStream}) is a dead
     * end. That is why {@link ProcessBuilder}'s redirectErrorStream property is
     * false by default. It is also why
     * {@link org.netbeans.lib.termsupport.TermExecutor#start} will
     * pre-combine stderr and stdout.
     */
    @Override
    public OutputWriter getErr() {
	// workaround for #182063: -  UnsupportedOperationException
	if (errWriter == null) {
	    errWriter = new TermErrWriter(terminal);
	}
	terminal.setErrConnected(true);
	return errWriter;
    }

    @Override
    public void closeInputOutput() {
	if (outputWriter != null)
	    outputWriter.close();
	Task task = new Task.StrongClose(ioContainer, terminal);
	task.dispatch();
    }

    @Override
    public boolean isClosed() {
        return ! terminal.isVisibleInContainer();
    }

    @Override
    public void setOutputVisible(boolean value) {
        // no-op in output2
    }

    @Override
    public void setErrVisible(boolean value) {
        // no-op in output2
    }

    @Override
    public void setInputVisible(boolean value) {
        // no-op
    }

    @Override
    public void select() {
	Task task = new Task.Select(ioContainer, terminal);
	task.dispatch();
    }

    @Override
    public boolean isErrSeparated() {
        return false;
    }

    @Override
    public void setErrSeparated(boolean value) {
        // no-op in output2
    }

    @Override
    public boolean isFocusTaken() {
        return false;
    }

    /**
     * output2 considered this to be a "really bad" operation so we will
     * outright not support it.
     */
    @Override
    public void setFocusTaken(boolean value) {
        throw new UnsupportedOperationException("Not supported yet.");	// NOI18N
    }

    @Deprecated
    @Override
    public Reader flushReader() {
	return term.getIn();
    }
}
