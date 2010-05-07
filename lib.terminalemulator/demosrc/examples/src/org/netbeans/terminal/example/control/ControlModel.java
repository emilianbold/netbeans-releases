/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.terminal.example.control;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.terminal.example.Config;
import org.openide.windows.InputOutput;

/**
 *
 * @author ivan
 */
public class ControlModel {

    public static class IOInfo {
	public final InputOutput io;
	public final Config config;
	public final String name;

	public IOInfo(InputOutput io, Config config, String name) {
	    this.io = io;
	    this.config = config;
	    this.name = name;
	}
    }

    private static final List<IOInfo> list = new ArrayList<IOInfo>();
    private static ControlView view;

    public static void setView(ControlView newView) {
	view = newView;
	view.refresh();
    }

    public static void add(InputOutput io, Config config, String name) {
	IOInfo ii = new IOInfo(io, config, name);
	list.add(ii);
	view.refresh();
    }

    public static List<IOInfo> list() {
	return list;
    }
}
