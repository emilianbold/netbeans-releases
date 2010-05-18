/*****************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
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
 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.command;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;

import java.io.UnsupportedEncodingException;

/**
 * A class that provides common functionality for many of the CVS command
 * that send similar sequences of requests.
 * @author  Robert Greig
 */
public abstract class BuildableCommand extends Command {

    /**
     * An implementation of Builder interface that constructs a FileContainerInfo object from
     * the server's output..
     */
    protected Builder builder;

    private final StringBuffer taggedLineBuffer = new StringBuffer();

    /**
     * A boolean value indicating if the user has used the setBuilder() method.
     */
    private boolean builderSet;

    /**
     * Execute a command. This implementation sends a Root request, followed
     * by as many Directory and Entry requests as is required by the recurse
     * setting and the file arguments that have been set. Subclasses should
     * call this first, and tag on the end of the requests list any further
     * requests and, finally, the actually request that does the command (e.g.
     * <pre>update</pre>, <pre>status</pre> etc.)
     * @param client the client services object that provides any necessary
     * services to this command, including the ability to actually process
     * all the requests
     * @throws CommandException if an error occurs executing the command
     */
    public void execute(ClientServices client, EventManager eventManager)
            throws CommandException, AuthenticationException {
        super.execute(client, eventManager);

        if (builder == null && !isBuilderSet()) {
            builder = createBuilder(eventManager);
        }
    }

    /**
     * Method that is called while the command is being executed.
     * Descendants can override this method to return a Builder instance
     * that will parse the server's output and create data structures.
     */
    public Builder createBuilder(EventManager eventManager) {
        return null;
    }

    public void messageSent(BinaryMessageEvent e) {
        super.messageSent(e);

        if (builder == null) {
            return;
        }

        if (builder instanceof BinaryBuilder) {   // XXX assert it?
            BinaryBuilder binaryBuilder = (BinaryBuilder) builder;
            binaryBuilder.parseBytes(e.getMessage(), e.getMessageLength());
        }
    }

    public void messageSent(MessageEvent e) {
        super.messageSent(e);
        if (builder == null) {
            return;
        }

        if (e instanceof EnhancedMessageEvent) {
            EnhancedMessageEvent eEvent = (EnhancedMessageEvent)e;
            builder.parseEnhancedMessage(eEvent.getKey(), eEvent.getValue());
            return;
        }

        if (e.isTagged()) {
            String message = MessageEvent.parseTaggedMessage(taggedLineBuffer, e.getMessage());
            if (message != null) {
                builder.parseLine(message, false);
                taggedLineBuffer.setLength(0);
            }
        }
        else {
            if (taggedLineBuffer.length() > 0) {
                builder.parseLine(taggedLineBuffer.toString(), false);
                taggedLineBuffer.setLength(0);
            }
            //#67337 do not interpret piped data using platform default encoding
            // UTF-8 causes problems as raw data (non UTf-8) can contain confusing sequences
            // use safe encoding that does not interpret byte sequences
            if (builder instanceof PipedFilesBuilder && e.isError() == false) {
                try {
                    String iso88591 = new String(e.getRawData(), "ISO-8859-1");
                    builder.parseLine(iso88591, e.isError());
                } catch (UnsupportedEncodingException e1) {
                    assert false;
                }
            } else {
                builder.parseLine(e.getMessage(), e.isError());
            }
        }
    }

    /**
     * Returns whether the builder is set.
     */
    protected boolean isBuilderSet() {
        return builderSet;
    }

    /**
     * Used for setting user-defined builder.
     * Can be also set null, in that case the builder mechanism is not used at
     * all.
     */
    public void setBuilder(Builder builder) {
        this.builder = builder;
        builderSet = true;
    }

    /**
     * Called when server responses with "ok" or "error", (when the command finishes).
     */
    public void commandTerminated(TerminationEvent e) {
        if (builder == null) {
            return;
        }

        if (taggedLineBuffer.length() > 0) {
            builder.parseLine(taggedLineBuffer.toString(), false);
            taggedLineBuffer.setLength(0);
        }
        builder.outputDone();
    }
}
