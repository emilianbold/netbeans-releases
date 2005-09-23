/*
 * InputOutputProviderImpl.java
 *
 * Created on 23. zברם 2005, 8:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.apisupport.project;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import junit.framework.Assert;
import org.netbeans.junit.NbTestCase;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Jaroslav Tulach
 */
public class InputOutputProviderImpl extends org.openide.windows.IOProvider {
	private static NbTestCase running;
	
	/** Creates a new instance of InputOutputProviderImpl */
	public InputOutputProviderImpl() {
	}
	
	public static void registerCase(NbTestCase r) {
		running = r;
	}

	public InputOutput getIO(String name, boolean newIO) {
		Assert.assertNotNull("A test case must be registered", running);
		return new IO(name);
	}

	public OutputWriter getStdOut() {
		Assert.assertNotNull("A test case must be registered", running);
		return new OW();
	}
	
	private static class OW extends OutputWriter {
		public OW() {
			super(new PrintWriter (running.getLog()));
		}
		public OW(String prefix) {
			super(new PrintWriter (running.getLog(prefix)));
		}
		
		public void println(String s, OutputListener l) throws IOException {
			write("println: " + s + " listener: " + l);
		}

		public void reset() throws IOException {
			write("Internal reset");
		}
		
	}
	
	private static class IO implements InputOutput {
		private OW w;
		private boolean closed;
		
		public IO(String n) {
			w = new OW(n);
		}
		
		
		public OutputWriter getOut() {
			return w;
		}

		public Reader getIn() {
			return new StringReader("");
		}

		public OutputWriter getErr() {
			return w;
		}

		public void closeInputOutput() {
			w.write("closeInputOutput");
			closed = true;
		}

		public boolean isClosed() {
			w.write("isClosed");
			return closed;
		}

		public void setOutputVisible(boolean value) {
			w.write("setOutputVisible: " + value);
		}

		public void setErrVisible(boolean value) {
			w.write("setErrVisible: " + value);
		}

		public void setInputVisible(boolean value) {
			w.write("setInputVisible: " + value);
		}

		public void select() {
			w.write("select");
		}

		public boolean isErrSeparated() {
			return false;
		}

		public void setErrSeparated(boolean value) {
		}

		public boolean isFocusTaken() {
			return false;
		}

		public void setFocusTaken(boolean value) {
		}

		public Reader flushReader() {
			return getIn();
		}

	}
}
