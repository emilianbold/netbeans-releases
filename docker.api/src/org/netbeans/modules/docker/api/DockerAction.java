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
package org.netbeans.modules.docker.api;

import org.netbeans.modules.docker.StreamItem;
import org.netbeans.modules.docker.ConnectionListener;
import org.netbeans.modules.docker.DockerRemoteException;
import org.netbeans.modules.docker.FolderUploader;
import org.netbeans.modules.docker.MuxedStreamResult;
import org.netbeans.modules.docker.ChunkedInputStream;
import org.netbeans.modules.docker.tls.ContextProvider;
import org.netbeans.modules.docker.IgnoreFileFilter;
import org.netbeans.modules.docker.HttpUtils;
import org.netbeans.modules.docker.DirectStreamResult;
import org.netbeans.modules.docker.Demuxer;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.swing.SwingUtilities;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.docker.DockerActionAccessor;
import org.netbeans.modules.docker.DockerConfig;
import org.netbeans.modules.docker.DockerUtils;
import org.netbeans.modules.docker.StreamResult;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.io.NullInputStream;
import org.openide.util.io.NullOutputStream;

/**
 *
 * @author Petr Hejl
 */
public class DockerAction {

    public static final String DOCKER_FILE = "Dockerfile"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(DockerAction.class.getName());

    private static final Pattern ID_PATTERN = Pattern.compile(".*([0-9a-f]{12}([0-9a-f]{52})?).*");

    private static final Pattern PORT_PATTERN = Pattern.compile("^(\\d+)/(tcp|udp)$");

    private static final Set<Integer> START_STOP_CONTAINER_CODES = new HashSet<>();

    private static final Set<Integer> REMOVE_CONTAINER_CODES = new HashSet<>();

    private static final Set<Integer> REMOVE_IMAGE_CODES = new HashSet<>();

    static {
        DockerActionAccessor.setDefault(new DockerActionAccessor() {
            @Override
            public void events(DockerAction action, Long since, DockerEvent.Listener listener, ConnectionListener connectionListener) throws DockerException {
                action.events(since, listener, connectionListener);
            }
        });

        Collections.addAll(START_STOP_CONTAINER_CODES, HttpURLConnection.HTTP_NO_CONTENT, HttpURLConnection.HTTP_NOT_MODIFIED);
        Collections.addAll(REMOVE_CONTAINER_CODES, HttpURLConnection.HTTP_NO_CONTENT, HttpURLConnection.HTTP_NOT_FOUND);
        Collections.addAll(REMOVE_IMAGE_CODES, HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NOT_FOUND);
    }

    private final RequestProcessor requestProcessor = new RequestProcessor(DockerAction.class);

    private final DockerInstance instance;

    private final boolean emitEvents;

    public DockerAction(DockerInstance instance) {
        this(instance, true);
    }

    // not needed in the api for now
    private DockerAction(DockerInstance instance, boolean emitEvents) {
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
                String name = null;
                JSONArray names = (JSONArray) json.get("Names");
                if (names != null && !names.isEmpty()) {
                    name = (String) names.get(0);
                }
                DockerContainer.Status status = DockerUtils.getContainerStatus((String) json.get("Status"));
                ret.add(new DockerContainer(instance, id, image, name, status));
            }
            return ret;
        } catch (DockerException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return Collections.emptyList();
    }

    public List<DockerRegistryImage> search(String searchTerm) {
        // the api does not allow this TAG and DIGEST separator characters
        if (searchTerm.contains(":") || searchTerm.contains("@")) { // NOI18N
            return Collections.emptyList();
        }

        try {
            JSONArray value = (JSONArray) doGetRequest(instance.getUrl(),
                    "/images/search?term=" + HttpUtils.encodeParameter(searchTerm), Collections.singleton(HttpURLConnection.HTTP_OK));
            List<DockerRegistryImage> ret = new ArrayList<>(value.size());
            for (Object o : value) {
                JSONObject json = (JSONObject) o;
                String name = (String) json.get("name");
                String description = (String) json.get("description");
                long stars = ((Number) getOrDefault(json, "star_count", 0)).longValue();
                boolean official = (boolean) getOrDefault(json, "is_official", false);
                boolean automated = (boolean) getOrDefault(json, "is_automated", false);
                ret.add(new DockerRegistryImage(name, description, stars, official, automated));
            }
            return ret;
        } catch (DockerException | UnsupportedEncodingException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return Collections.emptyList();
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

            long time = System.currentTimeMillis() / 1000;
            // XXX we send it as older API does not have the commit event
            if (emitEvents) {
                instance.getEventBus().sendEvent(
                        new DockerEvent(instance, DockerEvent.Status.COMMIT,
                                id, container.getId(), time));
            }

            // FIXME image size and time parameters
            return new DockerImage(instance, Collections.singletonList(DockerUtils.getTag(repository, tag)),
                    (String) value.get("Id"), time, 0, 0);

        } catch (UnsupportedEncodingException ex) {
            throw new DockerException(ex);
        }
    }

    public void rename(DockerContainer container, String name) throws DockerException {
        Parameters.notNull("name", name);

        try {
            doPostRequest(instance.getUrl(),
                    "/containers/" + container.getId() + "/rename?name=" + HttpUtils.encodeParameter(name), null,
                    false, Collections.singleton(HttpURLConnection.HTTP_NO_CONTENT));

            long time = System.currentTimeMillis() / 1000;
            // XXX we send it as older API does not have the commit event
            if (emitEvents) {
                instance.getEventBus().sendEvent(
                        new DockerEvent(instance, DockerEvent.Status.RENAME,
                                container.getId(), container.getId(), time));
            }
        } catch (UnsupportedEncodingException ex) {
            throw new DockerException(ex);
        }
    }

    public DockerTag tag(DockerTag source, String repository, String tag, boolean force) throws DockerException {
        if (repository == null) {
            throw new IllegalArgumentException("Repository can't be empty");
        }

        StringBuilder action = new StringBuilder("/images/");
        action.append(source.getId());
        action.append("/tag");
        action.append("?");
        action.append("repo=").append(repository);
        if (force) {
            action.append("&force=1");
        }
        if (tag != null) {
            action.append("&tag=").append(tag);
        }

        doPostRequest(instance.getUrl(), action.toString(), null,
                false, Collections.singleton(HttpURLConnection.HTTP_CREATED));

        String tagResult = DockerUtils.getTag(repository, tag);
        long time = System.currentTimeMillis() / 1000;
        // XXX we send it as older API does not have the commit event
        if (emitEvents) {
            instance.getEventBus().sendEvent(
                    new DockerEvent(instance, DockerEvent.Status.TAG,
                            source.getId(), tagResult, time));
        }

        return new DockerTag(source.getImage(), tagResult);
    }

    public DockerContainerDetail getDetail(DockerContainer container) throws DockerException {
        JSONObject value = (JSONObject) doGetRequest(instance.getUrl(),
                "/containers/" + container.getId() + "/json", Collections.singleton(HttpURLConnection.HTTP_OK));
        boolean tty = false;
        boolean openStdin = false;
        JSONObject config = (JSONObject) value.get("Config");
        if (config != null) {
            tty = (boolean) getOrDefault(config, "Tty", false);
            openStdin = (boolean) getOrDefault(config, "OpenStdin", false);
        }
        return new DockerContainerDetail(openStdin, tty);
    }

    public DockerImageDetail getDetail(DockerImage image) throws DockerException {
        JSONObject value = (JSONObject) doGetRequest(instance.getUrl(),
                "/images/" + image.getId() + "/json", Collections.singleton(HttpURLConnection.HTTP_OK));
        List<ExposedPort> ports = new LinkedList<>();
        JSONObject config = (JSONObject) value.get("Config");
        if (config != null) {
            JSONObject portsObject = (JSONObject) config.get("ExposedPorts");
            if (portsObject != null) {
                for (Object k : portsObject.keySet()) {
                    String portStr = (String) k;
                    Matcher m = PORT_PATTERN.matcher(portStr);
                    if (m.matches()) {
                        int port = Integer.parseInt(m.group(1));
                        ExposedPort.Type type = ExposedPort.Type.valueOf(m.group(2).toUpperCase(Locale.ENGLISH));
                        ports.add(new ExposedPort(port, type));
                    } else {
                        LOGGER.log(Level.FINE, "Unparsable port: {0}", portStr);
                    }
                }
            }
        }
        return new DockerImageDetail(ports);
    }

    public void start(DockerContainer container) throws DockerException {
        doPostRequest(instance.getUrl(), "/containers/" + container.getId() + "/start", null, false, START_STOP_CONTAINER_CODES);

        if (emitEvents) {
            instance.getEventBus().sendEvent(
                    new DockerEvent(instance, DockerEvent.Status.START,
                            container.getId(), container.getImage(), System.currentTimeMillis() / 1000));
        }
    }

    public void stop(DockerContainer container) throws DockerException {
        doPostRequest(instance.getUrl(), "/containers/" + container.getId() + "/stop", null, false, START_STOP_CONTAINER_CODES);

        if (emitEvents) {
            instance.getEventBus().sendEvent(
                    new DockerEvent(instance, DockerEvent.Status.DIE,
                            container.getId(), container.getImage(), System.currentTimeMillis() / 1000));
        }
    }

    public void pause(DockerContainer container) throws DockerException {
        doPostRequest(instance.getUrl(), "/containers/" + container.getId() + "/pause", null, false,
                Collections.singleton(HttpURLConnection.HTTP_NO_CONTENT));

        if (emitEvents) {
            instance.getEventBus().sendEvent(
                    new DockerEvent(instance, DockerEvent.Status.PAUSE,
                            container.getId(), container.getImage(), System.currentTimeMillis() / 1000));
        }
    }

    public void unpause(DockerContainer container) throws DockerException {
        doPostRequest(instance.getUrl(), "/containers/" + container.getId() + "/unpause", null, false,
                Collections.singleton(HttpURLConnection.HTTP_NO_CONTENT));

        if (emitEvents) {
            instance.getEventBus().sendEvent(
                    new DockerEvent(instance, DockerEvent.Status.UNPAUSE,
                            container.getId(), container.getImage(), System.currentTimeMillis() / 1000));
        }
    }

    public void remove(DockerTag tag) throws DockerException {
        String id = getImage(tag);
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

        if (emitEvents) {
            instance.getEventBus().sendEvent(
                    new DockerEvent(instance, DockerEvent.Status.DESTROY,
                            container.getId(), container.getImage(), System.currentTimeMillis() / 1000));
        }
    }

    public void resizeTerminal(DockerContainer container, int rows, int columns) throws DockerException {
        // formally there should be restart so changes take place
        doPostRequest(instance.getUrl(), "/containers/" + container.getId() + "/resize?h=" + rows + "&w=" + columns,
                null, false, Collections.singleton(HttpURLConnection.HTTP_OK));
    }

    public ActionStreamResult attach(DockerContainer container, boolean stdin, boolean logs) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        DockerContainerDetail info = DockerAction.this.getDetail(container);
        Socket s = null;
        try {
            URL url = createURL(instance.getUrl(), null);
            s = createSocket(url);

            OutputStream os = s.getOutputStream();
            os.write(("POST /containers/" + container.getId()
                    + "/attach?logs=" + (logs ? 1 : 0)
                    + "&stream=1&stdout=1&stdin="+ (stdin ? 1 : 0)
                    + "&stderr=1 HTTP/1.1\r\n\r\n").getBytes("ISO-8859-1"));
            os.flush();

            InputStream is = s.getInputStream();
            HttpUtils.Response response = HttpUtils.readResponse(is);
            int responseCode = response.getCode();
            if (responseCode != 101 && responseCode != HttpURLConnection.HTTP_OK) {
                String error = HttpUtils.readContent(is, response);
                throw new DockerRemoteException(responseCode, error != null ? error : response.getMessage());
            }

            if (emitEvents) {
                instance.getEventBus().sendEvent(
                        new DockerEvent(instance, DockerEvent.Status.ATTACH,
                                container.getId(), container.getImage(), System.currentTimeMillis() / 1000));
            }

            Integer length = HttpUtils.getLength(response.getHeaders());
            if (length != null && length <= 0) {
                closeSocket(s);
                return new ActionStreamResult(new EmptyStreamResult(info.isTty()));
            }
            is = HttpUtils.getResponseStream(is, response);

            if (info.isTty()) {
                return new ActionStreamResult(new DirectStreamResult(s, is));
            } else {
                return new ActionStreamResult(new MuxedStreamResult(s, is));
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
    public void pull(String imageName, StatusEvent.Listener listener) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            DockerName parsed = DockerName.parse(imageName);
            URL httpUrl = createURL(instance.getUrl(), "/images/create?fromImage="
                    + HttpUtils.encodeParameter(imageName));
            HttpURLConnection conn = createConnection(httpUrl);
            try {
                conn.setRequestMethod("POST");
                JSONObject auth = createAuthObject(CredentialsManager.getDefault().getCredentials(parsed.getRegistry()));
                if (auth != null) {
                    conn.setRequestProperty("X-Registry-Auth", HttpUtils.encodeBase64(auth.toJSONString()));
                }
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    String error = HttpUtils.readError(conn);
                    throw new DockerRemoteException(conn.getResponseCode(),
                            error != null ? error : conn.getResponseMessage());
                }

                JSONParser parser = new JSONParser();
                try (InputStreamReader r = new InputStreamReader(conn.getInputStream(), HttpUtils.getCharset(conn))) {
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
                            long current = ((Number) getOrDefault(detailObj, "current", 1)).longValue();
                            long total = ((Number) getOrDefault(detailObj, "total", 1)).longValue();
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
            }
        } catch (MalformedURLException e) {
            throw new DockerException(e);
        } catch (IOException e) {
            throw new DockerException(e);
       }
    }

    public void push(DockerTag tag, StatusEvent.Listener listener) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            String name = tag.getTag();
            DockerName parsed = DockerName.parse(name);
            String tagString = parsed.getTag();
            StringBuilder action = new StringBuilder();
            action.append("/images/");
            if (tagString == null) {
                action.append(name);
            } else {
                action.append(name.substring(0, name.length() - tagString.length() - 1));
            }
            action.append("/push");
            if (tagString != null) {
                action.append("?tag=").append(HttpUtils.encodeParameter(tagString));
            }

            URL httpUrl = createURL(instance.getUrl(), action.toString());
            HttpURLConnection conn = createConnection(httpUrl);
            try {
                conn.setRequestMethod("POST");
                JSONObject auth = createAuthObject(CredentialsManager.getDefault().getCredentials(parsed.getRegistry()));
                if (auth != null) {
                    conn.setRequestProperty("X-Registry-Auth", HttpUtils.encodeBase64(auth.toJSONString()));
                }
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    String error = HttpUtils.readError(conn);
                    throw new DockerRemoteException(conn.getResponseCode(),
                            error != null ? error : conn.getResponseMessage());
                }

                JSONParser parser = new JSONParser();
                try (InputStreamReader r = new InputStreamReader(conn.getInputStream(), HttpUtils.getCharset(conn))) {
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
                            long current = ((Number) getOrDefault(detailObj, "current", 1)).longValue();
                            long total = ((Number) getOrDefault(detailObj, "total", 1)).longValue();
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
            }
        } catch (MalformedURLException e) {
            throw new DockerException(e);
        } catch (IOException e) {
            throw new DockerException(e);
       }
    }

    // this call is BLOCKING
    public DockerImage build(@NonNull File buildContext, @NullAllowed File dockerfile,
            String repository, String tag, boolean pull, boolean noCache, final BuildEvent.Listener listener) throws DockerException {

        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        if (!buildContext.isDirectory()) {
            throw new IllegalArgumentException("Build context has to be a directory");
        }
        if (dockerfile != null && !dockerfile.isFile()) {
            throw new IllegalArgumentException("Dockerfile has to be a file");
        }
        if (repository == null && tag != null) {
            throw new IllegalArgumentException("Repository can't be empty when using tag");
        }

        String dockerfileName = null;
        if (dockerfile != null) {
            dockerfileName = dockerfile.getName();
        }

        Socket s = null;
        try {
            URL url = createURL(instance.getUrl(), null);
            s = createSocket(url);

            StringBuilder request = new StringBuilder();
            request.append("POST /build?");
            request.append("pull=").append(pull ? 1 : 0);
            request.append("&nocache=").append(noCache ? 1 : 0);
            if (dockerfileName != null) {
                request.append("&dockerfile=").append(HttpUtils.encodeParameter(dockerfileName));
            }
            if (repository != null) {
                request.append("&t=").append(HttpUtils.encodeParameter(repository));
                if (tag != null) {
                    request.append(":").append(tag);
                }
            }
            request.append(" HTTP/1.1\r\n");

            JSONObject registryConfig = new JSONObject();
            JSONObject configs = new JSONObject();
            registryConfig.put("configs", configs);
            for (Credentials c : DockerConfig.getDefault().getAllCredentials()) {
                configs.put(c.getRegistry(), createAuthObject(c));
            }

            if (!configs.isEmpty()) {
                request.append("X-Registry-Config: ")
                        .append(HttpUtils.encodeBase64(registryConfig.toJSONString()))
                        .append("\r\n");
            }
            request.append("Transfer-Encoding: chunked\r\n");
            request.append("Content-Type: application/tar\r\n");
            request.append("Content-Encoding: gzip\r\n");
            request.append("\r\n");

            OutputStream os = s.getOutputStream();
            os.write(request.toString().getBytes("ISO-8859-1"));
            os.flush();

            // FIXME should we allow \ as separator as that would be formally
            // separator on windows without possibility to escape anything
            // If we would allow that we have to use File comparison
            Future<Void> task = new FolderUploader(instance, os).upload(buildContext,
                    new IgnoreFileFilter(buildContext, dockerfile, '/'), new FolderUploader.Listener() {
                @Override
                public void onUpload(String path) {
                    listener.onEvent(new BuildEvent(instance, path, false, null, true));
                }
            });

            InputStream is = s.getInputStream();
            HttpUtils.Response response = HttpUtils.readResponse(is);
            int responseCode = response.getCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                task.cancel(true);
                String error = HttpUtils.readContent(is, response);
                throw new DockerRemoteException(responseCode,
                        error != null ? error : response.getMessage());
            }

            try {
                if (task.isDone()) {
                    task.get();
                } else {
                    LOGGER.log(Level.INFO, "Server responded OK yet upload has not finished");
                    task.cancel(true);
                }
            } catch (InterruptedException ex) {
                LOGGER.log(Level.INFO, null, ex);
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                throw new DockerException(ex.getCause());
            }

            is = HttpUtils.getResponseStream(is, response);

            JSONParser parser = new JSONParser();
            try (InputStreamReader r = new InputStreamReader(is, HttpUtils.getCharset(response))) {
                String line;
                String stream = null;
                while ((line = readEventObject(r)) != null) {
                    JSONObject o = (JSONObject) parser.parse(line);
                    stream = (String) o.get("stream");
                    if (stream != null) {
                        listener.onEvent(new BuildEvent(instance, stream.trim(), false, null, false));
                    } else {
                        String error = (String) o.get("error");
                        if (error != null) {
                            BuildEvent.Error detail = null;
                            JSONObject detailObj = (JSONObject) o.get("errorDetail");
                            if (detailObj != null) {
                                long code = ((Number) getOrDefault(detailObj, "code", 0)).longValue();
                                String mesage = (String) detailObj.get("message");
                                detail = new BuildEvent.Error(code, mesage);
                            }
                            listener.onEvent(new BuildEvent(instance, error, true, detail, false));
                        } else {
                            LOGGER.log(Level.INFO, "Unknown event {0}", o);
                        }
                    }
                    parser.reset();
                }

                if (stream != null) {
                    Matcher m = ID_PATTERN.matcher(stream.trim());
                    if (m.matches()) {
                        // the docker itself does not emit any event for built image
                        // we assume the last stream contains the built image id
                        // FIXME as there is no BUILD event we use PULL event
                        long time = System.currentTimeMillis() / 1000;
                        if (emitEvents) {
                            instance.getEventBus().sendEvent(
                                    new DockerEvent(instance, DockerEvent.Status.PULL,
                                            m.group(1), null, time));
                        }
                        // FIXME image size and time parameters
                        return new DockerImage(instance, Collections.singletonList(DockerUtils.getTag(repository, tag)),
                                m.group(1), time, 0, 0);
                    }
                }
            } catch (ParseException ex) {
                throw new DockerException(ex);
            }
            return null;
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

    public ActionChunkedResult logs(DockerContainer container) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        DockerContainerDetail info = getDetail(container);
        Socket s = null;
        try {
            URL url = createURL(instance.getUrl(), null);
            s = createSocket(url);

            OutputStream os = s.getOutputStream();
            os.write(("GET /containers/" + container.getId()
                    + "/logs?stderr=1&stdout=1&timestamps=1&follow=1 HTTP/1.1\r\n\r\n").getBytes("ISO-8859-1"));
            os.flush();

            InputStream is = s.getInputStream();
            HttpUtils.Response response = HttpUtils.readResponse(is);
            int responseCode = response.getCode();
            if (responseCode != 101 && responseCode != HttpURLConnection.HTTP_OK) {
                String error = HttpUtils.readContent(is, response);
                throw new DockerRemoteException(responseCode,
                        error != null ? error : response.getMessage());
            }

            is = HttpUtils.getResponseStream(is, response);

            StreamItem.Fetcher fetcher;
            Integer length = HttpUtils.getLength(response.getHeaders());
            // if there was no log it may return just standard reply with content length 0
            if (length != null && length == 0) {
                assert !(is instanceof ChunkedInputStream);
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
            return new ActionChunkedResult(s, fetcher, HttpUtils.getCharset(response));
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

    public Pair<DockerContainer, ActionStreamResult> run(String name, JSONObject configuration) throws DockerException {
        Socket s = null;
        try {
            URL url = createURL(instance.getUrl(), null);
            s = createSocket(url);

            byte[] data = configuration.toJSONString().getBytes("UTF-8");

            OutputStream os = s.getOutputStream();
            os.write(("POST " + (name != null ? "/containers/create?name=" + HttpUtils.encodeParameter(name) : "/containers/create") + " HTTP/1.1\r\n"
                    + "Content-Type: application/json\r\n"
                    + "Content-Length: " + data.length + "\r\n\r\n").getBytes("ISO-8859-1"));
            os.write(data);
            os.flush();

            InputStream is = s.getInputStream();
            HttpUtils.Response response = HttpUtils.readResponse(is);
            if (response.getCode() != HttpURLConnection.HTTP_CREATED) {
                String error = HttpUtils.readContent(is, response);
                throw new DockerRemoteException(response.getCode(),
                        error != null ? error : response.getMessage());
            }

            is = HttpUtils.getResponseStream(is, response);

            JSONObject value;
            try {
                JSONParser parser = new JSONParser();
                value = (JSONObject) parser.parse(HttpUtils.readContent(is, response));
            } catch (ParseException ex) {
                throw new DockerException(ex);
            }

            String id = (String) value.get("Id");
            DockerContainer container = new DockerContainer(
                    instance,
                    id,
                    (String) configuration.get("Image"),
                    "/" + name,
                    DockerContainer.Status.STOPPED);
            ActionStreamResult r = attach(container, true, true);

            os.write(("POST /containers/" + id + "/start HTTP/1.1\r\n\r\n").getBytes("ISO-8859-1"));
            os.flush();

            response = HttpUtils.readResponse(is);
            if (response.getCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                String error = HttpUtils.readContent(is, response);
                throw codeToException(response.getCode(),
                        error != null ? error : response.getMessage());
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

    // this call is BLOCKING
    private void events(Long since, DockerEvent.Listener listener, ConnectionListener connectionListener) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            URL url = createURL(instance.getUrl(), null);
            Socket s = createSocket(url);
            try {
                OutputStream os = s.getOutputStream();
                os.write(("GET " + (since != null ? "/events?since=" + since : "/events") + " HTTP/1.1\r\n"
                        + "Accept: application/json\r\n\r\n").getBytes("ISO-8859-1"));
                os.flush();

                InputStream is = s.getInputStream();
                HttpUtils.Response response = HttpUtils.readResponse(is);
                int responseCode = response.getCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    String error = HttpUtils.readContent(is, response);
                    throw new DockerRemoteException(responseCode,
                            error != null ? error : response.getMessage());
                }

                is = HttpUtils.getResponseStream(is, response);

                if (connectionListener != null) {
                    connectionListener.onConnect(s);
                }

                JSONParser parser = new JSONParser();
                try (InputStreamReader r = new InputStreamReader(is, HttpUtils.getCharset(response))) {
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
                closeSocket(s);
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

    private Object doGetRequest(@NonNull String url, @NonNull String action, Set<Integer> okCodes) throws DockerException {
        //assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            URL httpUrl = createURL(url, action);
            HttpURLConnection conn = createConnection(httpUrl);
            try {
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (!okCodes.contains(conn.getResponseCode())) {
                    String error = HttpUtils.readError(conn);
                    throw codeToException(conn.getResponseCode(),
                            error != null ? error : conn.getResponseMessage());
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

    private Object doPostRequest(@NonNull String url, @NonNull String action,
            @NullAllowed InputStream data, boolean output, Set<Integer> okCodes) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            URL httpUrl = createURL(url, action);
            HttpURLConnection conn = createConnection(httpUrl);
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
                    String error = HttpUtils.readError(conn);
                    throw codeToException(conn.getResponseCode(),
                            error != null ? error : conn.getResponseMessage());
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

    private Object doDeleteRequest(@NonNull String url, @NonNull String action, boolean output, Set<Integer> okCodes) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            URL httpUrl = createURL(url, action);
            HttpURLConnection conn = createConnection(httpUrl);
            try {
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded" );

                if (!okCodes.contains(conn.getResponseCode())) {
                    String error = HttpUtils.readError(conn);
                    throw codeToException(conn.getResponseCode(),
                            error != null ? error : conn.getResponseMessage());
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

    private Socket createSocket(URL url) throws IOException {
        try {
            if ("https".equals(url.getProtocol())) { // NOI18N
                SSLContext context = ContextProvider.getInstance().getSSLContext(instance);
                return context.getSocketFactory().createSocket(url.getHost(), url.getPort());
            } else {
                Socket s = new Socket(ProxySelector.getDefault().select(url.toURI()).get(0));
                s.connect(new InetSocketAddress(url.getHost(), url.getPort()));
                return s;
            }
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    private HttpURLConnection createConnection(URL url) throws IOException {
        assert "http".equals(url.getProtocol()) || "https".equals(url.getProtocol()); // NOI18N
        try {
            HttpURLConnection ret = (HttpURLConnection) url.openConnection(ProxySelector.getDefault().select(url.toURI()).get(0));
            if (ret instanceof HttpsURLConnection) {
                ((HttpsURLConnection) ret).setSSLSocketFactory(ContextProvider.getInstance().getSSLContext(instance).getSocketFactory());
            }
            return ret;
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
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

    private static JSONObject createAuthObject(Credentials credentials) {
        if (credentials == null) {
            return null;
        }
        JSONObject value = new JSONObject();
        value.put("username", credentials.getUsername());
        value.put("password", new String(credentials.getPassword()));
        value.put("email", credentials.getEmail());
        value.put("serveraddress", credentials.getRegistry());
        value.put("auth", "");
        return value;
    }

    private static DockerException codeToException(int code, String message) {
        if (code == HttpURLConnection.HTTP_CONFLICT) {
            return new DockerConflictException(message);
        }
        return new DockerRemoteException(code, message);
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

    private static Object getOrDefault(Map map, Object key, Object def) {
        Object ret = map.get(key);
        if (ret == null) {
            ret = def;
        }
        return ret;
    }

    private static String getImage(DockerTag tag) {
        String id = tag.getTag();
        if (id.equals("<none>:<none>")) { // NOI18N
            id = tag.getImage().getId();
        }
        return id;
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

    private static class EmptyStreamResult implements StreamResult {

        private final OutputStream os = new NullOutputStream();

        private final InputStream is = new NullInputStream();

        private final boolean tty;

        public EmptyStreamResult(boolean tty) {
            this.tty = tty;
        }

        @Override
        public OutputStream getStdIn() {
            return os;
        }

        @Override
        public InputStream getStdOut() {
            return is;
        }

        @Override
        public InputStream getStdErr() {
            return null;
        }

        @Override
        public boolean hasTty() {
            return tty;
        }

        @Override
        public void close() throws IOException {
            // noop
        }
    }

}
