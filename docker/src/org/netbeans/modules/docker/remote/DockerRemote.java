/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.remote;

import java.io.BufferedOutputStream;
import org.netbeans.modules.docker.DockerImage;
import org.netbeans.modules.docker.DockerContainer;
import org.netbeans.modules.docker.DockerTag;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.docker.ContainerStatus;
import org.netbeans.modules.docker.DockerInstance;
import org.netbeans.modules.docker.DockerUtils;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public class DockerRemote {

    private static final Logger LOGGER = Logger.getLogger(DockerRemote.class.getName());

    private static final Pattern HTTP_RESPONSE_PATTERN = Pattern.compile("^HTTP/1\\.1 (\\d\\d\\d) (.*)$");

    private static final Set<Integer> START_STOP_CONTAINER_CODES = new HashSet<>();

    private static final Set<Integer> REMOVE_CONTAINER_CODES = new HashSet<>();

    private static final Set<Integer> REMOVE_IMAGE_CODES = new HashSet<>();

    static {
        Collections.addAll(START_STOP_CONTAINER_CODES, HttpURLConnection.HTTP_NO_CONTENT, HttpURLConnection.HTTP_NOT_MODIFIED);
        Collections.addAll(REMOVE_CONTAINER_CODES, HttpURLConnection.HTTP_NO_CONTENT, HttpURLConnection.HTTP_NOT_FOUND);
        Collections.addAll(REMOVE_IMAGE_CODES, HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NOT_FOUND);
    }

    private final DockerInstance instance;

    public DockerRemote(DockerInstance instance) {
        this.instance = instance;
    }

    public List<DockerImage> getImages() {
        try {
            JSONArray value = (JSONArray) doGetRequest(instance.getUrl(),
                    "/images/json", Collections.singleton(HttpURLConnection.HTTP_OK));
            List<DockerImage> ret = new ArrayList<>(value.size());
            for (Object o : value) {
                JSONObject json  = (JSONObject) o;
                JSONArray repoTags = (JSONArray) json.get("RepoTags");
                String id = (String) json.get("Id");
                long created = (long) json.get("Created");
                long size = (long) json.get("Size");
                long virtualSize = (long) json.get("VirtualSize");
                ret.add(new DockerImage(instance, repoTags, id, created, size, virtualSize));
            }
            return ret;
        } catch (DockerException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return Collections.emptyList();
    }

    public List<DockerContainer> getContainers() {
        try {
            JSONArray value = (JSONArray) doGetRequest(instance.getUrl(),
                    "/containers/json?all=1", Collections.singleton(HttpURLConnection.HTTP_OK));
            List<DockerContainer> ret = new ArrayList<>(value.size());
            for (Object o : value) {
                JSONObject json = (JSONObject) o;
                String id = (String) json.get("Id");
                String image = (String) json.get("Image");
                ContainerStatus status = DockerUtils.getContainerStatus((String) json.get("Status"));
                ret.add(instance.getContainerFactory().create(id, image, status));
            }
            return ret;
        } catch (DockerException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return Collections.emptyList();
    }

    public DockerContainer createContainer(JSONObject configuration) throws DockerException {
        try {
            JSONObject value = (JSONObject) doPostRequest(instance.getUrl(), "/containers/create", new ByteArrayInputStream(configuration.toJSONString().getBytes("UTF-8")),
                    true, Collections.singleton(HttpURLConnection.HTTP_CREATED));
            // FIXME image id
            return instance.getContainerFactory().create((String) value.get("Id"),
                    (String) configuration.get("Image"), ContainerStatus.STOPPED);
        } catch (UnsupportedEncodingException ex) {
            throw new DockerException(ex);
        }
    }

    public void start(DockerContainer container) throws DockerException {
        doPostRequest(instance.getUrl(), "/containers/" + container.getId() + "/start", null, false, START_STOP_CONTAINER_CODES);
    }

    public void stop(DockerContainer container) throws DockerException {
        doPostRequest(instance.getUrl(), "/containers/" + container.getId() + "/stop", null, false, START_STOP_CONTAINER_CODES);
    }

    public void pause(DockerContainer container) throws DockerException {
        doPostRequest(instance.getUrl(), "/containers/" + container.getId() + "/pause", null, false,
                Collections.singleton(HttpURLConnection.HTTP_NO_CONTENT));
    }

    public void unpause(DockerContainer container) throws DockerException {
        doPostRequest(instance.getUrl(), "/containers/" + container.getId() + "/unpause", null, false,
                Collections.singleton(HttpURLConnection.HTTP_NO_CONTENT));
    }

    public void remove(DockerTag tag) throws DockerException {
        String id = DockerUtils.getTag(tag);
        JSONArray value = (JSONArray) doDeleteRequest(instance.getUrl(), "/images/" + id, true,
                REMOVE_IMAGE_CODES);
    }

    public void remove(DockerContainer container) throws DockerException {
        doDeleteRequest(instance.getUrl(), "/containers/" + container.getId(), false,
                REMOVE_CONTAINER_CODES);
    }

    public void resizeTerminal(DockerContainer container, int rows, int columns) throws DockerException {
        // formally there should be restart so changes take place
        doPostRequest(instance.getUrl(), "/containers/" + container.getId() + "/resize?h=" + rows + "&w=" + columns,
                null, false, Collections.singleton(HttpURLConnection.HTTP_OK));
    }

    public StreamResult attach(DockerContainer container, boolean logs) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        Socket s = null;
        try {
            URL url = createURL(instance.getUrl(), null);
            s = new Socket(url.getHost(), url.getPort());

            OutputStream os = s.getOutputStream();
            os.write(("POST /containers/" + container.getId()
                    + "/attach?logs=" + (logs ? 1 : 0) + "&stream=1&stdout=1&stdin=1&stderr=1 HTTP/1.1\r\n\r\n").getBytes("ISO-8859-1"));
            os.flush();

            InputStream is = s.getInputStream();
            String response = HttpUtils.readResponseLine(is);
            if (response == null) {
                throw new DockerException("No response from server");
            }
            Matcher m = HTTP_RESPONSE_PATTERN.matcher(response);
            if (!m.matches()) {
                throw new DockerException("Wrong response from server");
            }

            int responseCode = Integer.parseInt(m.group(1));
            if (responseCode != 101 && responseCode != HttpURLConnection.HTTP_OK) {
                throw new DockerRemoteException(responseCode, m.group(2));
            }

            String line;
            do {
                line = HttpUtils.readResponseLine(is);
            } while (line != null && !"".equals(line.trim()));

            return new AttachResult(s);
        } catch (MalformedURLException e) {
            closeSocket(s);
            throw new DockerException(e);
        } catch (IOException e) {
            closeSocket(s);
            throw new DockerException(e);
        } catch (DockerException e) {
            closeSocket(s);
            throw e;
        }
    }

    public void events(Long since, DockerEvent.Listener listener, ConnectionListener connectionListener) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            URL httpUrl = createURL(instance.getUrl(), since != null ? "/events?since=" + since : "/events");
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            try {
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new DockerRemoteException(conn.getResponseCode(), conn.getResponseMessage());
                }

                connectionListener.onConnect(conn);

                JSONParser parser = new JSONParser();
                try (InputStreamReader r = new InputStreamReader(
                        (conn.getInputStream()))) {
                    String line;
                    while ((line = readEventObject(r)) != null) {
                        JSONObject o = (JSONObject) parser.parse(line);
                        DockerEvent.Status status = DockerEvent.Status.parse((String) o.get("status"));
                        String id = (String) o.get("id");
                        String from = (String) o.get("from");
                        long time = (Long) o.get("time");
                        if (status == null) {
                            LOGGER.log(Level.INFO, "Unknown event {0}", o.get("status"));
                        } else {
                            listener.onEvent(new DockerEvent(instance, status, id, from, time));
                        }
                        parser.reset();
                    }
                } catch (ParseException ex) {
                    throw new DockerException(ex);
                }
            } finally {
                conn.disconnect();
            }
        } catch (MalformedURLException e) {
            throw new DockerException(e);
        } catch (IOException e) {
            throw new DockerException(e);
       }
    }

    public StreamResult logs(DockerContainer container) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        Socket s = null;
        try {
            URL url = createURL(instance.getUrl(), null);
            s = new Socket(url.getHost(), url.getPort());

            OutputStream os = s.getOutputStream();
            os.write(("GET /containers/" + container.getId()
                    + "/logs?stderr=1&stdout=1&timestamps=1&follow=1 HTTP/1.1\r\n\r\n").getBytes("ISO-8859-1"));
            os.flush();

            InputStream is = s.getInputStream();
            String response = HttpUtils.readResponseLine(is);
            if (response == null) {
                throw new DockerException("No response from server");
            }
            Matcher m = HTTP_RESPONSE_PATTERN.matcher(response);
            if (!m.matches()) {
                throw new DockerException("Wrong response from server");
            }

            int responseCode = Integer.parseInt(m.group(1));
            if (responseCode != 101 && responseCode != HttpURLConnection.HTTP_OK) {
                throw new DockerRemoteException(responseCode, m.group(2));
            }

            boolean chunked = false;
            String line;
            do {
                line = HttpUtils.readResponseLine(is);
                if (line != null && line.startsWith("Transfer-Encoding") && line.contains("chunked")) {
                    chunked = true;
                }
            } while (line != null && !"".equals(line.trim()));

            return new StreamDemultiplexer(s, chunked);
        } catch (MalformedURLException e) {
            closeSocket(s);
            throw new DockerException(e);
        } catch (IOException e) {
            closeSocket(s);
            throw new DockerException(e);
        } catch (DockerException e) {
            closeSocket(s);
            throw e;
        }
    }

    private static Object doGetRequest(@NonNull String url, @NonNull String action, Set<Integer> okCodes) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            URL httpUrl = createURL(url, action);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            try {
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (!okCodes.contains(conn.getResponseCode())) {
                    throw new DockerRemoteException(conn.getResponseCode(), conn.getResponseMessage());
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())))) {
                    JSONParser parser = new JSONParser();
                    return parser.parse(br);
                } catch (ParseException ex) {
                    throw new DockerException(ex);
                }
            } finally {
                conn.disconnect();
            }

        } catch (MalformedURLException e) {
            throw new DockerException(e);
        } catch (IOException e) {
            throw new DockerException(e);
        }
    }

    private static Object doPostRequest(@NonNull String url, @NonNull String action,
            @NullAllowed InputStream data, boolean output, Set<Integer> okCodes) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            URL httpUrl = createURL(url, action);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            try {
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                if (data != null) {
                    conn.setDoOutput(true);
                    try (OutputStream os = new BufferedOutputStream(conn.getOutputStream())) {
                        FileUtil.copy(data, os);
                    }
                }

                if (!okCodes.contains(conn.getResponseCode())) {
                    throw new DockerRemoteException(conn.getResponseCode(), conn.getResponseMessage());
                }

                if (output) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(
                            (conn.getInputStream())))) {
                        JSONParser parser = new JSONParser();
                        return parser.parse(br);
                    } catch (ParseException ex) {
                        throw new DockerException(ex);
                    }
                } else {
                    return null;
                }
            } finally {
                conn.disconnect();
            }

        } catch (MalformedURLException e) {
            throw new DockerException(e);
        } catch (IOException e) {
            throw new DockerException(e);
        }
    }

    private static Object doDeleteRequest(@NonNull String url, @NonNull String action, boolean output, Set<Integer> okCodes) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            URL httpUrl = createURL(url, action);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            try {
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded" );

                if (!okCodes.contains(conn.getResponseCode())) {
                    throw new DockerRemoteException(conn.getResponseCode(), conn.getResponseMessage());
                }

                if (output) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(
                            (conn.getInputStream())))) {
                        JSONParser parser = new JSONParser();
                        return parser.parse(br);
                    } catch (ParseException ex) {
                        throw new DockerException(ex);
                    }
                } else {
                    return null;
                }
            } finally {
                conn.disconnect();
            }

        } catch (MalformedURLException e) {
            throw new DockerException(e);
        } catch (IOException e) {
            throw new DockerException(e);
        }
    }

    private static URL createURL(@NonNull String url, @NullAllowed String action) throws MalformedURLException {
        String realUrl;
        if (url.startsWith("tcp://")) {
            realUrl = "http://" + url.substring(6);
        } else {
            realUrl = url;
        }
        if (realUrl.endsWith("/")) {
            realUrl = realUrl.substring(0, realUrl.length() - 1);
        }
        if (action != null) {
            realUrl += action;
        }

        return new URL(realUrl);
    }

    private static String readEventObject(Reader is) throws IOException {
        StringWriter sw = new StringWriter();
        int b;
        int balance = -1;
        while ((b = is.read()) != -1) {
            if (balance < 0) {
                if (b == '{') {
                    balance = 1;
                    sw.write(b);
                }
                continue;
            }
            if (b == '{') {
                balance++;
            } else if (b == '}') {
                balance--;
            }
            sw.write(b);
            if (balance == 0) {
                return sw.toString();
            }
        }
        return null;
    }

    private static void closeSocket(Socket s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
    }

    public static class AttachResult implements StreamResult {

        private final Socket s;

        private final OutputStream stdIn;

        private final InputStream stdOut;

        private final InputStream stdErr;

        AttachResult(Socket s) throws IOException {
            this.s = s;
            this.stdIn = s.getOutputStream();
            this.stdOut = s.getInputStream();
            this.stdErr = null;
        }

        public OutputStream getStdIn() {
            return stdIn;
        }

        public InputStream getStdOut() {
            return stdOut;
        }

        public InputStream getStdErr() {
            return stdErr;
        }

        @Override
        public void close() throws IOException {
            s.close();
        }
    }

    public static interface ConnectionListener {

        void onConnect(HttpURLConnection connection);
    }
}
