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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import org.netbeans.modules.docker.DockerImage;
import org.netbeans.modules.docker.DockerContainer;
import org.netbeans.modules.docker.DockerTag;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.swing.SwingUtilities;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.docker.ContainerInfo;
import org.netbeans.modules.docker.ContainerStatus;
import org.netbeans.modules.docker.DockerInstance;
import org.netbeans.modules.docker.DockerUtils;
import org.netbeans.modules.docker.DockerHubImage;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class DockerRemote {

    private static final Logger LOGGER = Logger.getLogger(DockerRemote.class.getName());

    private static final Set<Integer> START_STOP_CONTAINER_CODES = new HashSet<>();

    private static final Set<Integer> REMOVE_CONTAINER_CODES = new HashSet<>();

    private static final Set<Integer> REMOVE_IMAGE_CODES = new HashSet<>();

    static {
        Collections.addAll(START_STOP_CONTAINER_CODES, HttpURLConnection.HTTP_NO_CONTENT, HttpURLConnection.HTTP_NOT_MODIFIED);
        Collections.addAll(REMOVE_CONTAINER_CODES, HttpURLConnection.HTTP_NO_CONTENT, HttpURLConnection.HTTP_NOT_FOUND);
        Collections.addAll(REMOVE_IMAGE_CODES, HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NOT_FOUND);
    }

    private final RequestProcessor requestProcessor = new RequestProcessor(DockerRemote.class);

    private final DockerInstance instance;
    
    private final boolean emitEvents;

    public DockerRemote(DockerInstance instance) {
        this(instance, true);
    }

    public DockerRemote(DockerInstance instance, boolean emitEvents) {
        this.instance = instance;
        this.emitEvents = emitEvents;
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
            LOGGER.log(Level.INFO, null, ex);
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
            LOGGER.log(Level.INFO, null, ex);
        }
        return Collections.emptyList();
    }

    public List<DockerHubImage> search(String searchTerm) {
        // the api does not allow this TAG and DIGEST separator characters
        if (searchTerm.contains(":") || searchTerm.contains("@")) { // NOI18N
            return Collections.emptyList();
        }

        try {
            JSONArray value = (JSONArray) doGetRequest(instance.getUrl(),
                    "/images/search?term=" + HttpUtils.encodeParameter(searchTerm), Collections.singleton(HttpURLConnection.HTTP_OK));
            List<DockerHubImage> ret = new ArrayList<>(value.size());
            for (Object o : value) {
                JSONObject json = (JSONObject) o;
                String name = (String) json.get("name");
                String description = (String) json.get("description");
                long stars = (long) json.getOrDefault("star_count", 0);
                boolean official = (boolean) json.getOrDefault("is_official", false);
                boolean automated = (boolean) json.getOrDefault("is_automated", false);
                ret.add(new DockerHubImage(name, description, stars, official, automated));
            }
            return ret;
        } catch (DockerException | UnsupportedEncodingException ex) {
            LOGGER.log(Level.INFO, null, ex);
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

    public DockerImage commit(DockerContainer container, String repository, String tag,
            String author, String message, boolean pause) throws DockerException {

        if (repository == null && tag != null) {
            throw new IllegalArgumentException("Repository can't be empty when using tag");
        }

        try {
            StringBuilder action = new StringBuilder("/commit");
            action.append("?");
            action.append("container=").append(container.getId());
            if (repository != null) {
                action.append("&repo=").append(HttpUtils.encodeParameter(repository));
                if (tag != null) {
                    action.append("&tag=").append(HttpUtils.encodeParameter(tag));
                }
            }
            if (author != null) {
                action.append("&author=").append(HttpUtils.encodeParameter(author));
            }
            if (message != null) {
                action.append("&comment=").append(HttpUtils.encodeParameter(message));
            }
            if (!pause) {
                action.append("&pause=0");
            }

            JSONObject value = (JSONObject) doPostRequest(instance.getUrl(), action.toString(), null,
                    true, Collections.singleton(HttpURLConnection.HTTP_CREATED));

            String id = (String) value.get("Id");

            // XXX we send it as older API does not have the commit event
            if (emitEvents) {
                instance.getEventBus().sendEvent(
                        new DockerEvent(instance, DockerEvent.Status.COMMIT,
                                id, container.getId(), System.currentTimeMillis() / 1000));
            }

            // FIXME image size and time parameters
            return new DockerImage(instance, Collections.singletonList(tag), (String) value.get("Id"),
                    System.currentTimeMillis() / 1000, 0, 0);

        } catch (UnsupportedEncodingException ex) {
            throw new DockerException(ex);
        }
    }

    public ContainerInfo getInfo(DockerContainer container) throws DockerException {
        JSONObject value = (JSONObject) doGetRequest(instance.getUrl(),
                "/containers/" + container.getId() + "/json", Collections.singleton(HttpURLConnection.HTTP_OK));
        boolean tty = false;
        boolean openStdin = false;
        JSONObject config = (JSONObject) value.get("Config");
        if (config != null) {
            tty = (boolean) config.getOrDefault("Tty", false);
            openStdin = (boolean) config.getOrDefault("OpenStdin", false);
        }
        return new ContainerInfo(openStdin, tty);
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

        // XXX to be precise we should emit DELETE event if we
        // delete the last image, but for our purpose this is enough
        if (emitEvents) {
            instance.getEventBus().sendEvent(
                    new DockerEvent(instance, DockerEvent.Status.UNTAG,
                            tag.getId(), null, System.currentTimeMillis() / 1000));
        }
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

    public StreamResult attach(DockerContainer container, boolean stdin, boolean logs) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        ContainerInfo info = getInfo(container);
        Socket s = null;
        try {
            URL url = createURL(instance.getUrl(), null);
            s = new Socket(url.getHost(), url.getPort());

            OutputStream os = s.getOutputStream();
            os.write(("POST /containers/" + container.getId()
                    + "/attach?logs=" + (logs ? 1 : 0)
                    + "&stream=1&stdout=1&stdin="+ (stdin ? 1 : 0)
                    + "&stderr=1 HTTP/1.1\r\n\r\n").getBytes("ISO-8859-1"));
            os.flush();

            InputStream is = s.getInputStream();
            Pair<Integer, String> response = HttpUtils.readResponse(is);
            int responseCode = response.first();
            if (responseCode != 101 && responseCode != HttpURLConnection.HTTP_OK) {
                throw new DockerRemoteException(responseCode, response.second());
            }

            boolean chunked = HttpUtils.isChunked(HttpUtils.parseHeaders(is));
            if (chunked) {
                is = new ChunkedInputStream(is);
            }

            if (info.isTty()) {
                return new DirectStreamResult(s, is);
            } else {
                return new MuxedStreamResult(s, is);
            }
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

    // this call is BLOCKING
    public void pull(String imageName, StatusEvent.Listener listener, ConnectionListener connectionListener) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            URL httpUrl = createURL(instance.getUrl(), "/images/create?fromImage=" + HttpUtils.encodeParameter(imageName));
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            try {
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new DockerRemoteException(conn.getResponseCode(), conn.getResponseMessage());
                }

                if (connectionListener != null) {
                    connectionListener.onConnect(conn);
                }

                JSONParser parser = new JSONParser();
                try (InputStreamReader r = new InputStreamReader(
                        conn.getInputStream(), "UTF-8")) { // NOI18N
                    String line;
                    while ((line = readEventObject(r)) != null) {
                        JSONObject o = (JSONObject) parser.parse(line);
                        boolean error = false;
                        String id = (String) o.get("id");
                        String status = (String) o.get("status");
                        if (status == null) {
                            status = (String) o.get("error");
                            error = status != null;
                        }
                        if (status == null) {
                            LOGGER.log(Level.INFO, "Unknown event {0}", o);
                            continue;
                        }

                        String progress = (String) o.get("progress");
                        StatusEvent.Progress detail = null;
                        JSONObject detailObj = (JSONObject) o.get("progressDetail");
                        if (detailObj != null) {
                            long current = ((Number) detailObj.getOrDefault("current", 1)).longValue();
                            long total = ((Number) detailObj.getOrDefault("total", 1)).longValue();
                            detail = new StatusEvent.Progress(current, total);
                        }
                        listener.onEvent(new StatusEvent(instance, id, status, progress, error, detail));
                        parser.reset();
                    }
                } catch (ParseException ex) {
                    throw new DockerException(ex);
                }
            } finally {
                conn.disconnect();
                if (connectionListener != null) {
                    connectionListener.onDisconnect();
                }
            }
        } catch (MalformedURLException e) {
            throw new DockerException(e);
        } catch (IOException e) {
            throw new DockerException(e);
       }
    }

    // this call is BLOCKING
    public DockerImage build(@NonNull File buildContext, @NullAllowed File dockerfile,
            BuildEvent.Listener listener) throws DockerException {

        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        if (!buildContext.isDirectory()) {
            throw new IllegalArgumentException("Build context has to be a directory");
        }
        if (dockerfile != null && !dockerfile.isFile()) {
            throw new IllegalArgumentException("Dockerfile has to be a file");
        }

        String dockerfileName = null;
        if (dockerfile != null) {
            dockerfileName = dockerfile.getName();
        }

        Socket s = null;
        try {
            URL url = createURL(instance.getUrl(), null);
            s = new Socket(url.getHost(), url.getPort());

            StringBuilder request = new StringBuilder();
            request.append("POST /build");
            if (dockerfileName != null) {
                request.append("?dockerfile=").append(HttpUtils.encodeParameter(dockerfileName));
            }
            request.append(" HTTP/1.1\r\n");
            request.append("Transfer-Encoding: chunked\r\n");
            request.append("Content-Type: application/tar\r\n");
            request.append("Content-Encoding: gzip\r\n\r\n");

            OutputStream os = s.getOutputStream();
            os.write(request.toString().getBytes("ISO-8859-1"));
            os.flush();

            ChunkedOutputStream cos = new ChunkedOutputStream(new BufferedOutputStream(os));
            try {
                GZIPOutputStream gzos = new GZIPOutputStream(cos);
                try {
                    ArchiveOutputStream aos = new ArchiveStreamFactory().createArchiveOutputStream(
                            ArchiveStreamFactory.TAR, gzos);
                    try {
                        // FIXME exclude dockerignored files
                        FileObject context = FileUtil.toFileObject(FileUtil.normalizeFile(buildContext));
                        for (Enumeration<? extends FileObject> e = context.getChildren(true); e.hasMoreElements();) {
                            FileObject child = e.nextElement();
                            if (child.isFolder()) {
                                continue;
                            }
                            TarArchiveEntry entry = new TarArchiveEntry(FileUtil.toFile(child),
                                    FileUtil.getRelativePath(context, child));
                            aos.putArchiveEntry(entry);
                            try (InputStream is = new BufferedInputStream(child.getInputStream())) {
                                FileUtil.copy(is, aos);
                            }
                            aos.closeArchiveEntry();
                        }
                    } finally {
                        aos.finish();
                        aos.flush();
                    }
                } finally {
                    gzos.finish();
                    gzos.flush();
                }
            } finally {
                cos.finish();
                cos.flush();
            }

            InputStream is = s.getInputStream();
            Pair<Integer, String> response = HttpUtils.readResponse(is);
            int responseCode = response.first();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new DockerRemoteException(responseCode, response.second());
            }

            boolean chunked = HttpUtils.isChunked(HttpUtils.parseHeaders(is));
            if (chunked) {
                is = new ChunkedInputStream(is);
            }

            JSONParser parser = new JSONParser();
            try (InputStreamReader r = new InputStreamReader(
                    is, "UTF-8")) { // NOI18N
                String line;
                while ((line = readEventObject(r)) != null) {
                    JSONObject o = (JSONObject) parser.parse(line);
                    String stream = (String) o.get("stream");
                    if (stream != null) {
                        listener.onEvent(new BuildEvent(instance, stream, false, null));
                    } else {
                        String error = (String) o.get("error");
                        if (error != null) {
                            BuildEvent.Error detail = null;
                            JSONObject detailObj = (JSONObject) o.get("errorDetail");
                            if (detailObj != null) {
                                long code = ((Number) detailObj.getOrDefault("code", "0")).longValue();
                                String mesage = (String) detailObj.get("message");
                                detail = new BuildEvent.Error(code, mesage);
                            }
                            listener.onEvent(new BuildEvent(instance, error, true, detail));
                        } else {
                            LOGGER.log(Level.INFO, "Unknown event {0}", o);    
                        }
                    }
                    parser.reset();
                }
            } catch (ParseException ex) {
                throw new DockerException(ex);
            }
            return null;
        } catch (MalformedURLException e) {
            closeSocket(s);
            throw new DockerException(e);
        } catch (ArchiveException e) {
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

    // this call is BLOCKING
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

                if (connectionListener != null) {
                    connectionListener.onConnect(conn);
                }

                JSONParser parser = new JSONParser();
                try (InputStreamReader r = new InputStreamReader(
                        conn.getInputStream(), "UTF-8")) { // NOI18N
                    String line;
                    while ((line = readEventObject(r)) != null) {
                        JSONObject o = (JSONObject) parser.parse(line);
                        DockerEvent.Status status = DockerEvent.Status.parse((String) o.get("status"));
                        String id = (String) o.get("id");
                        String from = (String) o.get("from");
                        long time = (Long) o.get("time");
                        if (status == null) {
                            LOGGER.log(Level.INFO, "Unknown event {0}", o);
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
                if (connectionListener != null) {
                    connectionListener.onDisconnect();
                }
            }
        } catch (MalformedURLException e) {
            throw new DockerException(e);
        } catch (IOException e) {
            throw new DockerException(e);
       }
    }

    public LogResult logs(DockerContainer container) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        ContainerInfo info = getInfo(container);
        Socket s = null;
        try {
            URL url = createURL(instance.getUrl(), null);
            s = new Socket(url.getHost(), url.getPort());

            OutputStream os = s.getOutputStream();
            os.write(("GET /containers/" + container.getId()
                    + "/logs?stderr=1&stdout=1&timestamps=1&follow=1 HTTP/1.1\r\n\r\n").getBytes("ISO-8859-1"));
            os.flush();

            InputStream is = s.getInputStream();
            Pair<Integer, String> response = HttpUtils.readResponse(is);
            int responseCode = response.first();
            if (responseCode != 101 && responseCode != HttpURLConnection.HTTP_OK) {
                throw new DockerRemoteException(responseCode, response.second());
            }

            Map<String, String> headers = HttpUtils.parseHeaders(is);
            boolean chunked = HttpUtils.isChunked(headers);
            if (chunked) {
                is = new ChunkedInputStream(is);
            }

            StreamItem.Fetcher fetcher;
            Integer length = HttpUtils.getLength(headers);
            // if there was no log it may return just standard reply with content length 0
            if (length != null && length == 0) {
                assert !chunked;
                LOGGER.log(Level.INFO, "Empty logs");
                fetcher = new StreamItem.Fetcher() {
                    @Override
                    public StreamItem fetch() {
                        return null;
                    }
                };
            } else if (info.isTty()) {
                fetcher = new DirectFetcher(is);
            } else {
                fetcher = new Demuxer(is);
            }
            return new LogResult(s, fetcher);
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

    public Pair<DockerContainer, StreamResult> run(JSONObject configuration) throws DockerException {
        Socket s = null;
        try {
            URL url = createURL(instance.getUrl(), null);
            s = new Socket(url.getHost(), url.getPort());

            byte[] data = configuration.toJSONString().getBytes("UTF-8");

            OutputStream os = s.getOutputStream();
            os.write(("POST /containers/create HTTP/1.1\r\n"
                    + "Content-Type: application/json\r\n"
                    + "Content-Length: " + data.length + "\r\n\r\n").getBytes("ISO-8859-1"));
            os.write(data);
            os.flush();

            InputStream is = s.getInputStream();
            Pair<Integer, String> response = HttpUtils.readResponse(is);
            if (response.first() != HttpURLConnection.HTTP_CREATED) {
                throw new DockerRemoteException(response.first(), response.second());
            }

            Map<String, String> headers = HttpUtils.parseHeaders(is);
            Integer length = HttpUtils.getLength(headers);
            assert length != null;

            boolean chunked = HttpUtils.isChunked(headers);
            if (chunked) {
                is = new ChunkedInputStream(is);
            }

            JSONObject value;
            try {
                JSONParser parser = new JSONParser();
                value = (JSONObject) parser.parse(HttpUtils.readContent(is, length, "UTF-8"));
            } catch (ParseException ex) {
                throw new DockerException(ex);
            }

            String id = (String) value.get("Id");
            DockerContainer container = instance.getContainerFactory().create(id,
                    (String) configuration.get("Image"), ContainerStatus.STOPPED);
            StreamResult r = attach(container, true, true);

            os.write(("POST /containers/" + id + "/start HTTP/1.1\r\n\r\n").getBytes("ISO-8859-1"));
            os.flush();

            response = HttpUtils.readResponse(is);
            if (response.first() != HttpURLConnection.HTTP_NO_CONTENT) {
                throw new DockerRemoteException(response.first(), response.second());
            }

            return Pair.of(container, r);
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

    private static URL createURL(@NonNull String url, @NullAllowed String action) throws MalformedURLException, UnsupportedEncodingException {
        // FIXME optimize
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

    public static class LogResult implements Closeable {

        private final Socket s;

        private final StreamItem.Fetcher fetcher;

        public LogResult(Socket s, StreamItem.Fetcher fetcher) {
            this.s = s;
            this.fetcher = fetcher;
        }

        public StreamItem.Fetcher getFetcher() {
            return fetcher;
        }

        @Override
        public void close() throws IOException {
            s.close();
        }
    }

    private static class DirectFetcher implements StreamItem.Fetcher {

        private final InputStream is;

        private final byte[] buffer = new byte[1024];

        public DirectFetcher(InputStream is) {
            this.is = is;
        }

        @Override
        public StreamItem fetch() {
            try {
                int count = is.read(buffer);
                if (count < 0) {
                    return null;
                }
                return new StreamItem(ByteBuffer.wrap(buffer, 0, count), false);
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
                return null;
            }
        }

    }
    public static interface ConnectionListener {

        void onConnect(HttpURLConnection connection);

        void onDisconnect();

    }
}
