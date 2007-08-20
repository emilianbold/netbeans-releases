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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;

import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;


final class ObexImageSender implements Runnable {
    /** Shows if debug prints should be done. */
    private static final boolean DEBUG = false;

    /** Indicate that uploading should be stopped */
    private boolean stop = true;

    /** Stream  with image data */
    private InputStream imageSource;

    /** Special stream to load image data to byte array */
    private ByteArrayOutputStream baos;

    /** Session to send an image */
    private ClientSession session;

    /** Put Operation of image uploading process */
    private Operation operation;

    /** Output stream of obex */
    private OutputStream outputStream;

    /** Array with image data */
    private byte[] imageData;

    /** Contains name of uploading image */
    private String imageName;

    /** Reference to GUI part of sender */
    private GUIImageSender gui;

    ObexImageSender(GUIImageSender gui) {
        this.gui = gui;
    }

    void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * Used to send an image
     */
    public void run() {
        boolean loaded = false;
        boolean connected = false;
        stop = false;

        try {
            loadImageData(imageName);
            loaded = true;
            connect();

            if (stop) {
                throw new IOException();
            }

            connected = true;
            uploadImage(imageName);
        } catch (IOException e) {
            if (DEBUG) {
                e.printStackTrace();
            }

            if (!stop) {
                if (connected) {
                    gui.stopMessage();
                } else if (loaded) {
                    gui.notReadyMessage();
                } else {
                    gui.errorMessage();
                }

                closeAll();

                return;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        closeAll();
        gui.showImageList();
    }

    /** load image data to array */
    private void loadImageData(String imageName) throws IOException {
        imageSource = getClass().getResourceAsStream(imageName);

        // read image data and create a byte array
        byte[] buff = new byte[1024];
        baos = new ByteArrayOutputStream(1024);

        while (true) {
            // check stop signal
            if (stop) {
                throw new IOException();
            }

            int length = imageSource.read(buff);

            if (length == -1) {
                break;
            }

            baos.write(buff, 0, length);
        }

        imageData = baos.toByteArray();
    }

    /** Connects with image receiver */
    private void connect() throws IOException {
        //String url = "tcpobex://localhost:5010";
        String url = "irdaobex://discover.0210;ias=ImageExchange";

        // update the Gauge message before connecting
        gui.showProgress("Connecting ...", imageData.length);

        session = (ClientSession)Connector.open(url);

        if (stop) {
            throw new IOException();
        }

        // update the Gauge message after connecting
        gui.showProgress("Uploading Image ...", imageData.length);

        HeaderSet response = session.connect(null);
        int responseCode = response.getResponseCode();

        if (responseCode != ResponseCodes.OBEX_HTTP_OK) {
            throw new IOException();
        }
    }

    /** Uploads image to receiver */
    private void uploadImage(String imageName) throws IOException {
        int position = 0;

        // start put operation
        HeaderSet headers = session.createHeaderSet();
        headers.setHeader(HeaderSet.NAME, imageName);
        headers.setHeader(HeaderSet.LENGTH, new Long(imageData.length));
        operation = session.put(headers);
        outputStream = operation.openOutputStream();

        while (position != imageData.length) {
            OutputStream outputStream = this.outputStream;
            int sendLength =
                ((imageData.length - position) > 256) ? 256 : (imageData.length - position);

            if (outputStream == null) {
                throw new IOException();
            }

            outputStream.write(imageData, position, sendLength);
            position += sendLength;
            gui.updateProgress(position);
        }

        outputStream.close();

        int code = operation.getResponseCode();

        if (code != ResponseCodes.OBEX_HTTP_OK) {
            throw new IOException();
        }
    }

    /** Stops uploading process */
    void stop() {
        stop = true;
    }

    /** Closes all connections */
    private void closeAll() {
        imageData = null;

        if (imageSource != null) {
            try {
                imageSource.close();
            } catch (IOException ioe) {
            }

            imageSource = null;
        }

        if (baos != null) {
            try {
                baos.close();
            } catch (IOException ioe) {
            }

            baos = null;
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException ioe) {
            }

            outputStream = null;
        }

        if (operation != null) {
            try {
                operation.close();
            } catch (IOException ioe) {
            }

            operation = null;
        }

        if (session != null) {
            try {
                session.disconnect(null);
            } catch (IOException ioe) {
            }

            try {
                session.close();
            } catch (IOException ioe) {
            }

            session = null;
        }
    }
}
