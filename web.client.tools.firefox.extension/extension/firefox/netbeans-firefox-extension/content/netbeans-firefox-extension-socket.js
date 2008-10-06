/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Sandip V. Chitale (sandipchitale@netbeans.org)
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

(function() {
    this.createSocket = function(host, port, listener) {
        var socket = new Object();

        var transportService = NetBeans.Utils.CCSV(
        NetBeans.Constants.SocketTransportServiceCID,
        NetBeans.Constants.SocketTransportServiceIF);

        var transport = transportService.createTransport(null,0,host,port,null);
        transport.setTimeout(
        NetBeans.Constants.SocketTransportIF.TIMEOUT_CONNECT, 20/*seconds*/);

        // Output stream
        var outstream = transport.openOutputStream(
        NetBeans.Constants.TransportIF.OPEN_BLOCKING,0,0);

        // Input stream
        var stream = transport.openInputStream(0,0,0);

        // Wrap in a scriptable input stream
        var instream = NetBeans.Utils.CCIN(
        NetBeans.Constants.BinaryInputStreamCID,
        NetBeans.Constants.BinaryInputStreamIF);
        instream.setInputStream(stream);

        socket.transport = transport;
        socket.output = outstream;
        socket.input = stream;
        socket.consoleService = NetBeans.Logger.getLogger();

        socket.close = function() {
            if (this.closed) {
                return;
            }

            this.closed = true;
            this.input.close();
            this.output.close();
        };

        socket.send = function(data) {
            if( this.closed ) {
                return;
            }

            if (data instanceof XML) {
                data = NetBeans.Utils.convertUnicodeToUTF8('<?xml version="1.0" ?>' + data.toXMLString());
            } else {
                // DBGP expects only XML messages back from the debugger
                return;
            }

            var outputData = data.length +
                NetBeans.Constants.NULL_TERMINATOR +
                data +
                NetBeans.Constants.NULL_TERMINATOR;

            // Components.utils.reportError("Sending : " + data);
            this.output.write(outputData, outputData.length);

            this.output.flush();
            socket.sentFlag = true;
        };

        var inputStreamPump = {
            astream: stream.QueryInterface(NetBeans.Constants.AsyncInputStreamIF),
            eventQueue: null,
            state: 0,
            data: "",

            startInThread: function() {
                if (NetBeans.Utils.isFF2()) {
                    var eqService = NetBeans.Utils.CCSV(
                    NetBeans.Constants.EventQueueServiceCID,
                    NetBeans.Constants.EventQueueServiceIF);
                    this.eventQueue = eqService.getSpecialEventQueue(
                    NetBeans.Constants.EventQueueServiceIF.CURRENT_THREAD_EVENT_QUEUE);
                } else {
                    this.eventQueue = NetBeans.Utils.CCSV(                
                    NetBeans.Constants.ThreadManagerServiceCID,
                    NetBeans.Constants.ThreadManagerService).currentThread;
                }

                this.astream.asyncWait(this, 0, 0, this.eventQueue);
            },

            stopInThread: function() {
                /* hack to clear socket stream callback */
                this.astream.asyncWait(null, 0, 0, null);
            },

            onStartRequest: function() {},

            onStopRequest: function() {
                socket.close();
                listener.onDBGPClose();
            },

            onDataAvailable: function(count) {
                // Read command
                this.data += instream.readBytes(count);
                // If we got something
                while (this.data.length > 0) {
                    // Try to locate the null terminator
                    var end_pt = this.data.indexOf(NetBeans.Constants.NULL_TERMINATOR);
                    // Not found
                    if (end_pt < 0) {
                        // log and return
                        break;
                    }
                    // found it
                    if (end_pt > 0) {
                        // extract the command
                        var command = this.data.substr(0, end_pt);
                        // send the command to the Debugger
                        listener.onDBGPCommand(NetBeans.Utils.convertUTF8ToUnicode(command));
                    }
                    // keep the remaining input
                    this.data = this.data.substr(end_pt + 1);
                }
            },

            // Implementation of nsIInputStreamCallback
            onInputStreamReady: function(astream) {
                var state = -1;
                while( state != this.state ) {
                    state = this.state;
                    try {
                        var count = astream.available();
                        if ( state == 1 ) {
                            if ( count > 0 ) {
                                this.onDataAvailable(count);
                            }
                        } else if ( state == 0 ) {
                            this.onStartRequest();
                            ++this.state;
                        }
                        astream.asyncWait(this, 0, 0, this.eventQueue);
                    } catch(exc) {
                        // TODO Use Components.results.NS_BASE_STREAM_CLOSED
                        if ( exc.result != 0x80470002) {
                            try {
                                NetBeans.Logger.logMessage("Unexpected socket failure:");
                                NetBeans.Logger.logException(exc);
                            } catch (exc) {
                                if (socket && socket.consoleService && socket.consoleService.logStringMessage) {
                                    socket.consoleService.logStringMessage("Unexpected socket failure:");
                                    socket.consoleService.logStringMessage(exc.toString());
                                    if (exc.stack) {
                                        socket.consoleService.logStringMessage(exc.stack);
                                    }
                                }
                            }
                            astream.closeWithStatus(exc.result);
                        }
                        
                        this.onStopRequest();
                        return;
                    }
                }
            },

            // Indicate that this implements nsIInputStreamCallback
            QueryInterface: function(iid) {
                if (iid.equals(NetBeans.Constants.InputStreamCallbackIF) ||
                    iid.equals(NetBeans.Constants.SupportsIF)) {
                    return this;
                }
                throw NetBeans.Constants.NS_ERROR_NO_INTERFACE;
            }
        };

        // Start reading from the socket.
        socket.startProcessing = function() {
            try {
                inputStreamPump.startInThread();
            } catch(exc) {
                NetBeans.Logger.logException(exc);
            }
        };

        // Stop reading from the socket.
        socket.stopProcessing = function() {
            try {
                inputStreamPump.stopInThread();
            } catch(exc) {
                NetBeans.Logger.logException(exc);
            }
        };

        // Socket is now prepared and connected. Now start reading from the socket
        // on the special eventQueue
        socket.startProcessing();

        return socket;
    }
}).apply(NetBeans.SocketUtils);
