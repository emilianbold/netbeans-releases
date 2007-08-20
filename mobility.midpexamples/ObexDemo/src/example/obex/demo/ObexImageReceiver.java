/*
 *
 * Copyright (c) 2007, Sun Microsystems, Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package example.obex.demo;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;

import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import javax.obex.ServerRequestHandler;
import javax.obex.SessionNotifier;


/**
 * @version
 */
final class ObexImageReceiver implements Runnable {
    /** Shows if debug prints should be done. */
    private static final boolean DEBUG = false;

    /** DEBUG only: keeps the class name for debug. */
    private static final String cn = "ObexImageReceiver";

    /** List of supported states. */
    private static final byte NONE = 0x00;
    private static final byte OPENED = 0x01;
    private static final byte CONNECTED = 0x02;
    private static final byte STARTED = 0x03;
    private static final byte STOPED = 0x4;
    private static final byte CLOSED = 0x5;

    /** Max quantity of processing senders. */
    private static final int MAX_PROCESSING_LENGTH = 2;

    /** Current state of receiver. */
    private byte current_state = NONE;

    /** Handler to process OBEX operation. */
    private Handler handler = new Handler();

    /** Indicate the length of connection request queue. */
    private int processingLength = 0;

    /** Session to receive an image. */
    private SessionNotifier session = null;

    /** Put Operation of image downloading process. */
    private Operation operation = null;

    /** Input stream of OBEX. */
    private InputStream inputStream = null;

    /** Reference to GUI part of sender */
    private GUIImageReceiver gui = null;

    ObexImageReceiver(GUIImageReceiver gui) {
        this.gui = gui;
    }

    /**
     * Used to accept connection and download an image.
     */
    public void run() {
        try {
            // synchronized to prevent loss of opened session
            synchronized (this) {
                //String url = "tcpobex://:5010";
                String url = "irdaobex://localhost.0010;ias=ImageExchange";
                session = (SessionNotifier)Connector.open(url);
                current_state = OPENED;
            }

            while (current_state != CLOSED) {
                session.acceptAndOpen(handler);
                preventOverloading();
            }
        } catch (IOException ioe) {
            synchronized (this) {
                if (current_state != CLOSED) {
                    ioe.printStackTrace();
                }
            }
        }

        synchronized (this) {
            if (current_state != CLOSED) {
                current_state = CLOSED;
                gui.canNotConnectMessage();
            }
        }

        closeAll(true);
    }

    /**
     * Prevents connection more than MAX_PROCESSING_LENGTH senders.
     */
    private void preventOverloading() {
        synchronized (handler) {
            while (processingLength >= MAX_PROCESSING_LENGTH) {
                processingLength++;

                try {
                    handler.wait();
                } catch (InterruptedException ie) {
                    // don't wait then
                }

                processingLength--;

                if (processingLength > 0) {
                    handler.notify();
                }
            }
        }
    }

    /**
     * Starts synchronized process of image receiving.
     */
    private void startSynchReceiving(Operation operation)
        throws IOException {
        synchronized (handler) {
            processingLength++;

            if (processingLength > 1) {
                try {
                    handler.wait();
                } catch (InterruptedException ie) {
                    // don't wait then
                }
            }
        }

        synchronized (this) {
            if (current_state == CLOSED) {
                throw new IOException();
            }

            this.operation = operation;
            current_state = CONNECTED;
        }
    }

    /**
     * Stops synchronized process of image receiving.
     */
    private void stopSynchReceiving() {
        synchronized (handler) {
            processingLength--;

            if (processingLength > 0) {
                handler.notify();
            }
        }
    }

    /**
     * Tries to receive.
     */
    private int imageReceive(Operation operation) {
        HeaderSet headers = null;
        String imageName = null;
        int imageLength = 0;
        int responseCode;

        try {
            startSynchReceiving(operation);

            // download information about image
            headers = operation.getReceivedHeaders();
            imageName = (String)headers.getHeader(HeaderSet.NAME);
            imageLength = (int)((Long)headers.getHeader(HeaderSet.LENGTH)).longValue();

            // ask permission to download image
            if (gui.askPermission(imageName, imageLength)) {
                // download and show image
                gui.showImage(downloadImage(imageLength));
                responseCode = ResponseCodes.OBEX_HTTP_OK;
            } else {
                responseCode = ResponseCodes.OBEX_HTTP_FORBIDDEN;
            }
        } catch (IOException ioe) {
            if (current_state == STOPED) {
                gui.showWaiting();
                closeAll(false);
                responseCode = ResponseCodes.OBEX_HTTP_RESET;
            } else if (current_state == CONNECTED) {
                gui.canNotConnectMessage();
                closeAll(false);
                responseCode = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
            } else if (current_state == STARTED) {
                gui.stopMessage();
                gui.showWaiting();
                closeAll(false);
                responseCode = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
            } else { // CLOSED, OPENED ?, NONE ???

                synchronized (this) {
                    current_state = CLOSED;
                }

                closeAll(true);
                responseCode = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
            }
        }

        stopSynchReceiving();

        return responseCode;
    }

    /** Download image. */
    private byte[] downloadImage(int imageLength) throws IOException {
        byte[] imageData = new byte[imageLength];
        int position = 0;
        int length = 0;
        checkStopSignal();
        gui.showProgress(imageLength);

        synchronized (this) {
            current_state = STARTED;
            inputStream = operation.openInputStream();
        }

        while (position < imageLength) {
            InputStream inputStream = this.inputStream;

            if (inputStream == null) {
                throw new IOException();
            }

            checkStopSignal();

            if (position < (imageLength - 256)) {
                length = inputStream.read(imageData, position, 256);
            } else {
                length = inputStream.read(imageData, position, imageLength - position);
            }

            if (length < 0) {
                throw new IOException();
            }

            position += length;
            gui.updateProgress(position);
        }

        synchronized (this) {
            current_state = CLOSED;
        }

        closeAll(true);

        return imageData;
    }

    /** Throws IOException if current_state is STOPED. */
    private void checkStopSignal() throws IOException {
        synchronized (this) {
            if ((current_state == STOPED) || (current_state == CLOSED)) {
                throw new IOException();
            }
        }
    }

    /** Stops downloading process. */
    void stop(boolean stopSession) {
        synchronized (this) {
            if (stopSession) {
                current_state = CLOSED;
            } else {
                current_state = STOPED;

                synchronized (handler) {
                    handler.notifyAll();
                }
            }

            closeAll(stopSession);
        }
    }

    /** Closes all connections. */
    private void closeAll(boolean closeSession) {
        synchronized (this) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                }

                inputStream = null;
            }

            if (operation != null) {
                try {
                    operation.close();
                } catch (IOException ioe) {
                }

                operation = null;
            }

            if ((session != null) && (closeSession)) {
                if (DEBUG) {
                    System.out.println(cn + ".closeAll: closing session");
                }

                try {
                    session.close();
                } catch (IOException ioe) {
                    if (DEBUG) {
                        System.out.println(cn + ".closeAll: got exc: " + ioe);
                    }
                }

                if (DEBUG) {
                    System.out.println(cn + ".closeAll: session is closed");
                }

                session = null;
            }
        }
    }

    class Handler extends ServerRequestHandler {
        public Handler() {
        }

        public int onPut(Operation operation) {
            return imageReceive(operation);
        }
    }
}
