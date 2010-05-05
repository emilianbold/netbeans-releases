/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.terminal.example.control;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.openide.windows.IOSelect;
import org.openide.windows.InputOutput;

import org.netbeans.lib.terminalemulator.ActiveRegion;
import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.lib.terminalemulator.ActiveTermListener;
import org.netbeans.lib.terminalemulator.Coord;

import org.netbeans.modules.terminal.api.IOConnect;
import org.netbeans.modules.terminal.api.IOEmulation;
import org.netbeans.terminal.example.Config;


/**
 *
 * @author ivan
 */
public class ControlView {
    private final ActiveTerm term;

    private static class VAction {
	public final String cmd;
	public final InputOutput io;

	public VAction(String cmd) {
	    this.cmd = cmd;
	    this.io = null;
	}

	public VAction(String cmd, InputOutput io) {
	    this.cmd = cmd;
	    this.io = io;
	}
    }
    public ControlView(ActiveTerm term) {
	this.term = term;

	term.setBackground(Color.white);
	term.setScrollOnOutput(false);
	term.setTrackCursor(false);

	term.setActionListener(new ActiveTermListener() {

	    public void action(ActiveRegion r, InputEvent e) {
		VAction action = (VAction) r.getUserObject();
		if (action == null)
		    return;

		Set<IOSelect.AdditionalOperation> extraOps = Collections.emptySet();

		if (action.cmd.equals("refresh"))
		    ;
		else if (action.cmd.equals("closeInputOutput"))
		    action.io.closeInputOutput();
		else if (action.cmd.equals("select"))
		    action.io.select();
		else if (action.cmd.equals("select(<empty>)")) {
		    IOSelect.select(action.io, extraOps);
		}
		else if (action.cmd.equals("select(OPEN)")) {
		    extraOps = EnumSet.of(IOSelect.AdditionalOperation.OPEN);
		    IOSelect.select(action.io, extraOps);
		}
		else if (action.cmd.equals("select(OPEN+REQ_VISIBLE)")) {
		    extraOps = EnumSet.of(IOSelect.AdditionalOperation.OPEN,
		                          IOSelect.AdditionalOperation.REQUEST_VISIBLE);
		    IOSelect.select(action.io, extraOps);
		}
		else if (action.cmd.equals("select(OPEN+REQ_VISIBLE+REQ_ACTIVE)")) {
		    extraOps = EnumSet.of(IOSelect.AdditionalOperation.OPEN,
		                          IOSelect.AdditionalOperation.REQUEST_VISIBLE,
					  IOSelect.AdditionalOperation.REQUEST_ACTIVE);
		    IOSelect.select(action.io, extraOps);
		}

		refresh();
	    }
	});
    }

    ActiveRegion beginRegion() {
	boolean hyperlink = true;
	ActiveRegion region = term.beginRegion(hyperlink);
	region.setFeedbackEnabled(true);
	return region;
    }

    void endRegion() {
	term.endRegion();
    }

    public void clear() {
        term.regionManager().reset();
        term.setText("");
    }

    void begin() {
	term.setAnchored(true);
    }

    void end() {
	term.appendText("", true);	// true -> repaint
    }

    void printf(String format, Object ... args) {
	String str = String.format(format, args);
	term.appendText(str, false);	// false -> don't repaint
    }

    public void tabTo(int target_column) {
        Coord cursor = term.getCursorCoord();
        if (target_column == cursor.col)
            return;
        if (cursor.col > target_column) {
            // target is behind us, start a new line
            term.appendText("\n", false);
            cursor = term.getCursorCoord();
        }

        while (cursor.col < target_column) {
            term.appendText(" ", false);
            cursor = term.getCursorCoord();
        }
    }

    private String yesNo(boolean v) {
	if (v)
	    return "";
	else
	    return "no-";
    }

    private void printAction(String cmd, InputOutput io) {
	ActiveRegion r = beginRegion();
	r.setUserObject(new VAction(cmd, io));
	printf(cmd);
	endRegion();
    }

    private void printIO(ControlModel.IOInfo ii) {
	Config config = ii.config;
	InputOutput io = ii.io;

	printf("'%s'", ii.name);
	printf("\n");
	tabTo(8);

	printf("iop %-7s", config.getIOProvider());
	printf(" container %-7s", config.getContainerProvider());
	printf(" shuttle %-8s", config.getIOShuttling());
	printf(" %-6s", config.getContainerStyle());
	printf(" %srestartable", yesNo(config.isRestartable()));
	printf(" %shupOnClose", yesNo(config.isHUPOnClose()));
	printf(" %skeep", yesNo(config.isKeep()));
	printf("\n");

	tabTo(8);
	printf("%sclosed", yesNo(io.isClosed()));
	printf(" ");
	printAction("closeInputOutput", io);
	printf(" ");
	printAction("select", io);
	printf("\n");

	tabTo(8);
	if (IOConnect.isSupported(io)) {
	    printf("%sconnecetd", yesNo(IOConnect.isConnected(io)));
	} else {
	    printf("IOConnect not supported");
	}
	printf("\n");

	tabTo(8);
	if (IOEmulation.isSupported(io)) {
	    printf("emulation %-8s  %sdisciplined",
		    IOEmulation.getEmulation(io),
		    yesNo(IOEmulation.isDisciplined(io)));
	} else {
	    printf("IOEmulation not supported");
	}
	printf("\n");

	tabTo(8);
	if (IOSelect.isSupported(io)) {
	    printf("IOSelect: ");
	    printAction("select(<empty>)", io);
	    printf(" ");
	    printAction("select(OPEN)", io);
	    printf(" ");
	    printAction("select(OPEN+REQ_VISIBLE)", io);
	    printf(" ");
	    printAction("select(OPEN+REQ_VISIBLE+REQ_ACTIVE)", io);
	} else {
	    printf("IOSelect not supported");
	}
	printf("\n");
    }

    public void refresh() {
	clear();
	begin();

	printAction("refresh", null);
	printf("\n");

	// Print all open IO's
	for (ControlModel.IOInfo ii : ControlModel.list()) {
	    if (! ii.io.isClosed())
		printIO(ii);
	}

	printf("------------------ closed ----------------\n");
	// Print all closed IO's
	for (ControlModel.IOInfo ii : ControlModel.list()) {
	    if (ii.io.isClosed())
		printIO(ii);
	}

	end();
    }
}
