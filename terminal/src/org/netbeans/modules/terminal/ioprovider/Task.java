/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.terminal.ioprovider;

import java.io.CharConversionException;
import javax.swing.Action;

import javax.swing.SwingUtilities;

import org.openide.windows.IOContainer;
import org.openide.xml.XMLUtil;

/**
 * Perform a Task on the EDT.
 * @author ivan
 */

/* package */ abstract class Task {

    private final IOContainer container;
    private final Terminal terminal;

    /**
     * Schedule this task to be performed on the EDT, or perform it now.
     */
    public final void dispatch() {
	if (SwingUtilities.isEventDispatchThread())
	    perform();
	else
	    SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run() {
		    perform();
		}
	    });
    }

    protected abstract void perform();

    protected Task(IOContainer container, Terminal terminal) {
	this.container = container;
	this.terminal = terminal;
    }

    protected final IOContainer container() {
	return container;
    }

    protected final Terminal terminal() {
	return terminal;
    }


    static class Add extends Task {

	public Add(IOContainer container, Terminal terminal) {
	    super(container, terminal);
	}

	@Override
	public void perform() {
	    container().add(terminal(), terminal().callBacks());
	    container().setToolbarActions(terminal(), terminal().getActions());
	    terminal().setVisibleInContainer(true);
	    /* OLD bug #181064
	    container().open();
	    container().requestActive();
	     */
	    if (terminal().name() != null)
		terminal().setTitle(terminal().name());
	}
    }

    static class Select extends Task {

	public Select(IOContainer container, Terminal terminal) {
	    super(container, terminal);
	}

	@Override
	public void perform() {
	    if (!terminal().isVisibleInContainer()) {
		container().add(terminal(), terminal().callBacks());
		container().setToolbarActions(terminal(), terminal().getActions());
		terminal().setVisibleInContainer(true);
	    }
	    container().select(terminal());
	}
    }

    static class DeSelect extends Task {

	public DeSelect(IOContainer container, Terminal terminal) {
	    super(container, terminal);
	}

	@Override
	public void perform() {
	    container().setToolbarActions(terminal(), new Action[0]);
	    container().remove(terminal());
	}
    }

    static class StrongClose extends Task {

	public StrongClose(IOContainer container, Terminal terminal) {
	    super(container, terminal);
	}

	@Override
	public void perform() {
	    terminal().close();
	    terminal().dispose();
	}
    }

    static class UpdateName extends Task {

	public UpdateName(IOContainer container, Terminal terminal) {
	    super(container, terminal);
	}

	@Override
	public void perform() {
	    String newTitle = terminal().getTitle();
	    if (terminal().isConnected()) {
		String escaped;
		try {
		    escaped = XMLUtil.toAttributeValue(newTitle);
		} catch (CharConversionException ex) {
		    escaped = newTitle;
		}

		newTitle = "<html><b>" + escaped + "</b></html>";	// NOI18N
	    }
	    container().setTitle(terminal(), newTitle);
	}
    }
}
