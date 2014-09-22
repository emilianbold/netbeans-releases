/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.weblogic.common.api;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.base.BaseExecutionDescriptor;
import org.netbeans.api.extexecution.base.BaseExecutionService;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.LineProcessor;
import org.netbeans.api.extexecution.base.input.LineProcessors;
import org.netbeans.modules.weblogic.common.ProxyUtils;
import org.openide.util.BaseUtilities;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public final class WebLogicDeployer {

    private static final Logger LOGGER = Logger.getLogger(WebLogicDeployer.class.getName());

    private static final RequestProcessor DEPLOYMENT_RP = new RequestProcessor(WebLogicDeployer.class);

    private static final int TIMEOUT = 300000;

    private static final Version VERSION_10 = Version.fromJsr277NotationWithFallback("10"); // NOI18N

    private final WebLogicConfiguration config;

    private final File javaBinary;

    private final Callable<String> nonProxy;

    private WebLogicDeployer(WebLogicConfiguration config, File javaBinary, @NullAllowed Callable<String> nonProxy) {
        this.config = config;
        this.javaBinary = javaBinary;
        this.nonProxy = nonProxy;
    }

    @NonNull
    public static WebLogicDeployer getInstance(@NonNull WebLogicConfiguration config,
            @NullAllowed File javaBinary, @NullAllowed Callable<String> nonProxy) {
        return new WebLogicDeployer(config, javaBinary, nonProxy);
    }

    @NonNull
    public Future<Collection<String>> list() {
        return DEPLOYMENT_RP.submit(new Callable<Collection<String>>() {

            @Override
            public Collection<String> call() throws Exception {
                ApplicationLineProcessor processor = new ApplicationLineProcessor();
                BaseExecutionService service = createService("-listapps", processor); // NOI18N
                Future<Integer> result = service.run();
                try {
                    Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                    if (value != 0) {
                        throw new IOException("Command failed");
                    } else {
                        return processor.getApplications();
                    }
                } catch (InterruptedException | TimeoutException ex) {
                    result.cancel(true);
                    throw ex;
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof Exception) {
                        throw (Exception) cause;
                    } else {
                        throw ex;
                    }
                }
            }
        });
    }

    @NonNull
    public Future<Void> deploy(@NonNull File file, @NullAllowed DeployListener listener,
            @NullAllowed String name) {

        List<String> params = new ArrayList<>();
        if (name != null) {
            params.add("-name"); // NOI18N
            params.add(name);
        }
        if (file.isDirectory()) {
            params.add("-nostage"); // NOI18N
            params.add("-source"); // NOI18N
        }
        return deploy(file, listener, params.toArray(new String[params.size()]));
    }

    @NonNull
    public Future<Void> redeploy(@NonNull String name, @NonNull File file,
            @NullAllowed BatchDeployListener listener) {
        List<String> params = new ArrayList<>();
        params.add("-source"); // NOI18N
        params.add(file.getAbsolutePath());

        if (config.isRemote()) {
            params.add("-upload");
            // we must use remote otherwise it will fail
            params.add("-remote");
        }
        return redeploy(Collections.singletonList(name), listener, params.toArray(new String[params.size()]));
    }

    @NonNull
    public Future<Void> redeploy(@NonNull Collection<String> names, @NullAllowed BatchDeployListener listener) {
        return redeploy(names, listener, new String[]{});
    }

    @NonNull
    public Future<Void> undeploy(@NonNull final Collection<String> names,
            @NullAllowed final BatchDeployListener listener) {

        if (listener != null) {
            listener.onStart();
        }

        return DEPLOYMENT_RP.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                LastLineProcessor lineProcessor = new LastLineProcessor();
                for (String name : names) {
                    BaseExecutionService service = createService("-undeploy", lineProcessor, "-name", name);
                    if (listener != null) {
                        listener.onStepStart(name);
                    }

                    Future<Integer> result = service.run();
                    try {
                        Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                        if (value != 0) {
                            if (listener != null) {
                                listener.onFail(lineProcessor.getLastLine());
                            }
                            throw new IOException("Command failed");
                        } else {
                            if (listener != null) {
                                listener.onStepFinish(name);
                            }
                        }
                    } catch (InterruptedException ex) {
                        if (listener != null) {
                            listener.onInterrupted();
                        }
                        result.cancel(true);
                        throw ex;
                    } catch (TimeoutException ex) {
                        if (listener != null) {
                            listener.onTimeout();
                        }
                        result.cancel(true);
                        throw ex;
                    } catch (ExecutionException ex) {
                        if (listener != null) {
                            Throwable cause = ex.getCause();
                            if (cause instanceof Exception) {
                                listener.onException((Exception) cause);
                                throw (Exception) cause;
                            } else {
                                listener.onException(ex);
                                throw ex;
                            }
                        }
                    }
                }
                if (listener != null) {
                    listener.onFinish();
                }
                return null;
            }
        });
    }

    @NonNull
    public Future<Void> start(@NonNull final Collection<String> names,
            @NullAllowed final BatchDeployListener listener) {

        if (listener != null) {
            listener.onStart();
        }

        return DEPLOYMENT_RP.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                LastLineProcessor lineProcessor = new LastLineProcessor();
                for (String name : names) {
                    BaseExecutionService service = createService("-start", lineProcessor, "-name", name);
                    if (listener != null) {
                        listener.onStepStart(name);
                    }

                    Future<Integer> result = service.run();
                    try {
                        Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                        if (value != 0) {
                            if (listener != null) {
                                listener.onFail(lineProcessor.getLastLine());
                            }
                            throw new IOException("Command failed");
                        } else {
                            if (listener != null) {
                                listener.onStepFinish(name);
                            }
                        }
                    } catch (InterruptedException ex) {
                        if (listener != null) {
                            listener.onInterrupted();
                        }
                        result.cancel(true);
                        throw ex;
                    } catch (TimeoutException ex) {
                        if (listener != null) {
                            listener.onTimeout();
                        }
                        result.cancel(true);
                        throw ex;
                    } catch (ExecutionException ex) {
                        if (listener != null) {
                            Throwable cause = ex.getCause();
                            if (cause instanceof Exception) {
                                listener.onException((Exception) cause);
                                throw (Exception) cause;
                            } else {
                                listener.onException(ex);
                                throw ex;
                            }
                        }
                    }
                }
                if (listener != null) {
                    listener.onFinish();
                }
                return null;
            }
        });
    }

    @NonNull
    public Future<Void> stop(@NonNull final Collection<String> names,
            @NullAllowed final BatchDeployListener listener) {

        if (listener != null) {
            listener.onStart();
        }

        return DEPLOYMENT_RP.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                LastLineProcessor lineProcessor = new LastLineProcessor();
                for (String name : names) {
                    BaseExecutionService service = createService("-stop", lineProcessor, "-name", name);
                    if (listener != null) {
                        listener.onStepStart(name);
                    }

                    Future<Integer> result = service.run();
                    try {
                        Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                        if (value != 0) {
                            if (listener != null) {
                                listener.onFail(lineProcessor.getLastLine());
                            }
                            throw new IOException("Command failed");
                        } else {
                            if (listener != null) {
                                listener.onStepFinish(name);
                            }
                        }
                    } catch (InterruptedException ex) {
                        if (listener != null) {
                            listener.onInterrupted();
                        }
                        result.cancel(true);
                        throw ex;
                    } catch (TimeoutException ex) {
                        if (listener != null) {
                            listener.onTimeout();
                        }
                        result.cancel(true);
                        throw ex;
                    } catch (ExecutionException ex) {
                        if (listener != null) {
                            Throwable cause = ex.getCause();
                            if (cause instanceof Exception) {
                                listener.onException((Exception) cause);
                                throw (Exception) cause;
                            } else {
                                listener.onException(ex);
                                throw ex;
                            }
                        }
                    }
                }
                if (listener != null) {
                    listener.onFinish();
                }
                return null;
            }
        });
    }

    private Future<Void> deploy(@NonNull final File file,
            @NullAllowed final DeployListener listener, final String... parameters) {

        if (listener != null) {
            listener.onStart();
        }

        return DEPLOYMENT_RP.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                int length = config.isRemote() ? parameters.length + 3 : parameters.length + 1;
                String[] execParams = new String[length];
                execParams[execParams.length - 1] = file.getAbsolutePath();
                if (config.isRemote()) {
                    execParams[execParams.length - 2] = "-upload"; // NOI18N
                    execParams[execParams.length - 3] = "-remote"; // NOI18N
                }
                if (parameters.length > 0) {
                    System.arraycopy(parameters, 0, execParams, 0, parameters.length);
                }

                LastLineProcessor lineProcessor = new LastLineProcessor();
                BaseExecutionService service = createService("-deploy", lineProcessor, execParams); // NOI18N
                Future<Integer> result = service.run();
                try {
                    Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                    if (value != 0) {
                        if (listener != null) {
                            listener.onFail(lineProcessor.getLastLine());
                        }
                        throw new IOException("Command failed");
                    } else {
                        if (listener != null) {
                            listener.onFinish();
                        }
                        return null;
                    }
                } catch (InterruptedException ex) {
                    if (listener != null) {
                        listener.onInterrupted();
                    }
                    result.cancel(true);
                    throw ex;
                } catch (TimeoutException ex) {
                    if (listener != null) {
                        listener.onTimeout();
                    }
                    result.cancel(true);
                    throw ex;
                } catch (ExecutionException ex) {
                    if (listener != null) {
                        Throwable cause = ex.getCause();
                        if (cause instanceof Exception) {
                            listener.onException((Exception) cause);
                            throw (Exception) cause;
                        } else {
                            listener.onException(ex);
                            throw ex;
                        }
                    }
                }
                return null;
            }
        });
    }

    private Future<Void> redeploy(@NonNull final Collection<String> names,
            @NullAllowed final BatchDeployListener listener, final String... parameters) {

        if (listener != null) {
            listener.onStart();
        }

        return DEPLOYMENT_RP.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                LastLineProcessor lineProcessor = new LastLineProcessor();
                for (String name : names) {
                    String[] execParams = new String[parameters.length + 2];
                    execParams[0] = "-name"; // NOI18N
                    execParams[1] = name;
                    if (parameters.length > 0) {
                        System.arraycopy(parameters, 0, execParams, 2, parameters.length);
                    }
                    BaseExecutionService service = createService("-redeploy", lineProcessor, execParams); // NOI18N
                    if (listener != null) {
                        listener.onStepStart(name);
                    }

                    Future<Integer> result = service.run();
                    try {
                        Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                        if (value != 0) {
                            if (listener != null) {
                                listener.onFail(lineProcessor.getLastLine());
                            }
                            throw new IOException("Command failed");
                        } else {
                            if (listener != null) {
                                listener.onStepFinish(name);
                            }
                        }
                    } catch (InterruptedException ex) {
                        if (listener != null) {
                            listener.onInterrupted();
                        }
                        result.cancel(true);
                        throw ex;
                    } catch (TimeoutException ex) {
                        if (listener != null) {
                            listener.onTimeout();
                        }
                        result.cancel(true);
                        throw ex;
                    } catch (ExecutionException ex) {
                        if (listener != null) {
                            Throwable cause = ex.getCause();
                            if (cause instanceof Exception) {
                                listener.onException((Exception) cause);
                                throw (Exception) cause;
                            } else {
                                listener.onException(ex);
                                throw ex;
                            }
                        }
                        break;
                    }
                }
                if (listener != null) {
                    listener.onFinish();
                }
                return null;
            }
        });
    }

    private BaseExecutionService createService(final String command,
            final LineProcessor processor, String... parameters) {

        org.netbeans.api.extexecution.base.ProcessBuilder builder =
                org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
        builder.setExecutable(getJavaBinary());
        builder.setRedirectErrorStream(true);
        List<String> arguments = new ArrayList<String>();
        // NB supports only JDK6+ while WL 9, only JDK 5
        Version version = config.getDomainVersion();
        if (version == null
                || !version.isAboveOrEqual(VERSION_10)) {
            arguments.add("-Dsun.lang.ClassLoader.allowArraySyntax=true"); // NOI18N
        }

        if (config.isRemote()) {
            try {
                // XXX authentication
                // t3 and t3s is afaik sits on top of http and https (source ?)
                List<Proxy> proxies = ProxySelector.getDefault().select(
                        new URI("http://" + config.getHost() + ":" + config.getPort())); // NOI18N
                if (!proxies.isEmpty()) {
                    Proxy first = proxies.get(0);
                    if (first.type() != Proxy.Type.DIRECT) {
                        SocketAddress addr = first.address();
                        if (addr instanceof InetSocketAddress) {
                            InetSocketAddress inet = (InetSocketAddress) addr;
                            if (first.type() == Proxy.Type.HTTP) {
                                arguments.add("-Dhttp.proxyHost=" + inet.getHostString()); // NOI18N
                                arguments.add("-Dhttp.proxyPort=" + inet.getPort()); // NOI18N
                                arguments.add("-Dhttps.proxyHost=" + inet.getHostString()); // NOI18N
                                arguments.add("-Dhttps.proxyPort=" + inet.getPort()); // NOI18N
                            } else if (first.type() == Proxy.Type.SOCKS) {
                                arguments.add("-DsocksProxyHost=" + inet.getHostString()); // NOI18N
                                arguments.add("-DsocksProxyPort=" + inet.getPort()); // NOI18N
                            }
                        }
                    }
                }
                String nonProxyHosts = ProxyUtils.getNonProxyHosts(nonProxy);
                if (nonProxyHosts != null) {
                    arguments.add("-Dhttp.nonProxyHosts=\"" + nonProxyHosts + "\""); // NOI18N
                }
            } catch (URISyntaxException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        arguments.add("-cp"); // NOI18N
        arguments.add(getClassPath());
        arguments.add("weblogic.Deployer"); // NOI18N
        arguments.add("-adminurl"); // NOI18N
        arguments.add(config.getAdminURL());
        arguments.add("-username"); // NOI18N
        arguments.add(config.getUsername());
        //arguments.add("-password"); // NOI18N
        //arguments.add(config.getPassword());
        arguments.add(command);

        arguments.addAll(Arrays.asList(parameters));
        builder.setArguments(arguments);

        final LineProcessor realProcessor;
        if (processor != null || LOGGER.isLoggable(Level.FINEST)) {
            if (processor == null) {
                realProcessor = new LoggingLineProcessor(Level.FINEST);
            } else if (!LOGGER.isLoggable(Level.FINEST)) {
                realProcessor = processor;
            } else {
                realProcessor = LineProcessors.proxy(processor, new LoggingLineProcessor(Level.FINEST));
            }
        } else {
            realProcessor = null;
        }
        BaseExecutionDescriptor descriptor = new BaseExecutionDescriptor().outProcessorFactory(new BaseExecutionDescriptor.InputProcessorFactory() {

            @Override
            public InputProcessor newInputProcessor() {
                return InputProcessors.bridge(realProcessor);
            }
        }).inReaderFactory(new BaseExecutionDescriptor.ReaderFactory() {

            @Override
            public Reader newReader() {
                return new StringReader(config.getPassword() + "\n"); // NOI18N
            }
        });
        return BaseExecutionService.newService(builder, descriptor);
    }

    private String getClassPath() {
        File[] files = config.getLayout().getClassPath();
        StringBuilder sb = new StringBuilder();
        for (File file : files) {
            sb.append(file.getAbsolutePath()).append(File.pathSeparatorChar);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private String getJavaBinary() {
        if (javaBinary != null) {
            return javaBinary.getAbsolutePath();
        }
        return BaseUtilities.isWindows() ? "java.exe" : "java"; // NOI18N
    }

    private static class ApplicationLineProcessor implements LineProcessor {

        private final List<String> applications = new ArrayList<>();

        @Override
        public synchronized void processLine(String line) {
            if (line.startsWith(" ")) {
                applications.add(line.trim());
            }
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {
        }

        public synchronized List<String> getApplications() {
            return applications;
        }
    }

    private static class LastLineProcessor implements LineProcessor {

        private static final Pattern STACK_TRACE_PATTERN = Pattern.compile("^\\s+((at)|(\\.\\.\\.)).*$"); // NOI18N

        private String last = "";

        @Override
        public synchronized void processLine(String line) {
            if (line.length() != 0 && !STACK_TRACE_PATTERN.matcher(line).matches()) {
                last = line;
            }
        }

        public synchronized String getLastLine() {
            return last;
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {
        }
    }

    private static class LoggingLineProcessor implements LineProcessor {

        private final Level level;

        public LoggingLineProcessor(Level level) {
            this.level = level;
        }

        @Override
        public void processLine(String line) {
            LOGGER.log(level, line);
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {
        }
    }
}
