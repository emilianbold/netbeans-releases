/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is Terminal Emulator.
 * The Initial Developer of the Original Software is Sun Microsystems, Inc..
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2001.
 * All Rights Reserved.
 *
 * Contributor(s): Ivan Soleimanipour.
 */

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.netbeans.lib.terminalemulator.*;

/*
 * A simple demonstration of using ActiveTerm for highlighting compiler
 * error output. Most of the machinery in this code is concerened with
 * starting the build and capturing the output unfortunately.
 *	- The Compiler is started in a separate thread so that we quickly
 *	  return from the button action handler.
 *	- We have separate readers for stdout and stderr because alternating
 *	  between them in a loop may cause deadlocks, and using Reader.ready()
 *	  will cause needless CPU looping.
 * 	- Note that since Java provides no facility for merging stdout
 *	  and stderr at the origin that by the time the outputs make it
 *	  through the OS and BufferedReaders buffers all ordering information
 *	  is lost.
 */

public class BuildTool extends JFrame implements ActionListener {

    private ActiveTerm term;
    private JButton b_javac;
    private JTextField t_files;

    private Object sync = new Object();

    /**
     * Create a simple red square glyph image
     */
    private Image glyph(int dim) {
	int xpoints[] = {1, 1, dim-1, dim-1};
	int ypoints[] = {1, dim-1, dim-1, 1};
	Polygon square = new Polygon(xpoints, ypoints, 4);
	Image i = term.getTopLevelAncestor().createImage(dim, dim);
	Graphics g = i.getGraphics();
	g.setColor(Color.white);
	g.fillRect(0, 0, dim, dim);
	g.setColor(Color.red);
	g.fillPolygon(square);
	g.setColor(Color.black);
	g.drawPolygon(square);
	g.dispose();
	return i;
    } 

    private void setup_gui() {

	// Handle quit from WM
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});

	term = new ActiveTerm();
	term.setFont(new Font("Helvetica", Font.PLAIN, 10));

	term.setBackground(Color.white);
	term.setGlyphGutterWidth(20);
	term.setClickToType(false);
	term.setHighlightColor(Color.orange);

	term.setActionListener(new ActiveTermListener() {
	    public void action(ActiveRegion r, InputEvent e) {
		if (r.isLink()) {
		    String text = term.textWithin(r.begin, r.end);
		    JOptionPane.showMessageDialog(term,
			"Get the editor to show " + text,
			"Syntax error",
			JOptionPane.DEFAULT_OPTION);
		}
	    }
	} );

	JToolBar toolbar = new JToolBar();
	toolbar.setMargin(new Insets(5, 5, 5, 5));
	toolbar.setFloatable(false);
	    b_javac = new JButton("Build");
	    b_javac.addActionListener(this);
	    toolbar.add(b_javac);

	    toolbar.addSeparator();

	    t_files = new JTextField();
	    t_files.setColumns(10);
	    t_files.setText("<Enter java file names and press Build>");
	    toolbar.add(t_files);

	getContentPane().add(toolbar, BorderLayout.NORTH);
	getContentPane().add(term, BorderLayout.CENTER);

	pack();
    } 

    public void setup() {
	// Have to do this image creation business
	// after we've become visible

	Dimension glyph_cell_size = term.getGlyphCellSize();
	term.setGlyphImage(48, glyph(glyph_cell_size.height));
    }


    public void actionPerformed(ActionEvent e) {

	if (e.getSource() == b_javac) {
	    String what = t_files.getText();
	    Thread compiler = new Compiler("javac", what);
	    compiler.start();
	}
    }

    class Compiler extends Thread {
	String args;
	String command;
	Compiler(String command, String args) {
	    this.command = command;
	    this.args = args;
	} 
	public void run() {
	    run_compile(command, args);
	}
    }

    private void run_compile(String cmd, String files) {

	term.clearHistory();

	term.setAnchored(true);

	// echo issued command in bold
	String command = cmd + " " + files;
	term.setAttribute(1);	// bold
	term.appendText(command + "\n", true);
	term.setAttribute(0);	// default

	Process proc = null;
	try {
	    proc = Runtime.getRuntime().exec(command, null, null);
	} catch (Exception x) {
	    x.printStackTrace();
	    System.exit(1);
	}


	int nreaders = 0;

	InputStream pout = proc.getInputStream();
	Reader out_R = new Reader(pout, this);
	nreaders++;
	out_R.start();

	InputStream perr = proc.getErrorStream();
	if (perr != null) {
	    Reader err_R = new Reader(perr, this);
	    nreaders++;
	    err_R.start();
	}

	// wait until we've drained all output
	while (nreaders > 0) {
	    try {
		synchronized(sync) {
		    sync.wait();
		}
	    } catch (InterruptedException x) {
		System.out.println("Compiler wait interrupted");
		continue;
	    } 
	    nreaders--;
	} 

	term.endRegion();

	term.setAttribute(1);	// bold
	term.appendText("No more output\n", true);

	// wait until child exits
	try {
	    proc.waitFor();
	} catch (Exception x) {}

	term.appendText("Command done\n", true);
	term.setAttribute(0);	// default
    }


    /**
     * Read and process compiler output.
     * The semantics of the processing are accomplished by the 'sink', us.
     */

    class Reader extends Thread {
	BufferedReader source;
	BuildTool sink;

	Reader(InputStream src, BuildTool sink) {
	    this.source = new BufferedReader(new InputStreamReader(src));
	    this.sink = sink;
	}

	public void run() {
	    while (true) {
		String line = null;
		try {
		    line = source.readLine();
		} catch (IOException x) {
		    x.printStackTrace();
		    sink.done();
		    break;
		} 

		if (line == null) {
		    sink.done();
		    break;
		} else {
		    sink.process_line(line);
		}
	    }
	}
    }

    private ActiveRegion region = null;

    synchronized private void process_line(String line) {
	// System.out.println(line);

	int jx = line.indexOf(".java:");

	if (jx != -1) {
	    // new error message detected
	    term.setGlyph(48, 0);

	    if (region != null)
		term.endRegion();
	    region = term.beginRegion(false);
	    region.setFeedbackEnabled(true);
	    region.setSelectable(true);

	    int cx1 = line.indexOf(":");	// index of first colon
	    int cx2 = line.indexOf(":", cx1+1);	// index of second colon

	    String srcloc = line.substring(0, cx2);
	    ActiveRegion link = term.beginRegion(true);
	    link.setLink(true);
	    link.setFeedbackViaParent(true);
	    term.appendText(srcloc, false);
	    term.endRegion();

	    String rest = line.substring(cx2);
	    term.appendText(rest, true);
	} else {
	    term.appendText(line, true);
	}

	term.putChar((char)10);
	term.putChar((char)13);
    } 


    public void done() {
	// declare that a Reader is done
	synchronized(sync) {
	    sync.notify();
	}
    }

    public static void main(String[] args) {
	BuildTool app = new BuildTool();
	app.setup_gui();
	app.setVisible(true);
	app.setup();
    }
}
