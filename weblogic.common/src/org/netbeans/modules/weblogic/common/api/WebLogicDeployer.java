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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    private final WebLogicConfiguration config;

    private final File javaBinary;

    private WebLogicDeployer(WebLogicConfiguration config, File javaBinary) {
        this.config = config;
        this.javaBinary = javaBinary;
    }

    @NonNull
    public static WebLogicDeployer getInstance(@NonNull WebLogicConfiguration config, @NullAllowed File javaBinary) {
        return new WebLogicDeployer(config, javaBinary);
    }

    @NonNull
    public Future<Boolean> deploy(@NonNull final File file,
            @NullAllowed final SingleDeployListener listener, final String... parameters) {

        if (listener != null) {
            listener.onStart();
        }

        return DEPLOYMENT_RP.submit(new Callable<Boolean>() {

            @Override
            public Boolean call() {
                int length = config.isRemote() ? parameters.length + 2 : parameters.length + 1;
                String[] execParams = new String[length];
                execParams[execParams.length - 1] = file.getAbsolutePath();
                if (config.isRemote()) {
                    execParams[execParams.length - 2] = "-upload"; // NOI18N
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
                        return false;
                    } else {
                        if (listener != null) {
                            listener.onFinish();
                        }
                        return true;
                    }
                } catch (InterruptedException ex) {
                    if (listener != null) {
                        listener.onInterrupted();
                    }
                    result.cancel(true);
                    Thread.currentThread().interrupt();
                } catch (TimeoutException ex) {
                    if (listener != null) {
                        listener.onTimeout();
                    }
                    result.cancel(true);
                } catch (ExecutionException ex) {
                    if (listener != null) {
                        Throwable cause = ex.getCause();
                        if (cause instanceof Exception) {
                            listener.onException((Exception) cause);
                        } else {
                            listener.onException(ex);
                        }
                    }
                }
                return false;
            }
        });
    }

    @NonNull
    public Future<Boolean> redeploy(@NonNull final Collection<String> names,
            @NullAllowed final BatchDeployListener listener, final String... parameters) {

        if (listener != null) {
            listener.onStart();
        }

        return DEPLOYMENT_RP.submit(new Callable<Boolean>() {

            @Override
            public Boolean call() {
                boolean failed = false;
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
                            failed = true;
                            if (listener != null) {
                                listener.onFail(lineProcessor.getLastLine());
                            }
                            break;
                        }
                    } catch (InterruptedException ex) {
                        failed = true;
                        if (listener != null) {
                            listener.onInterrupted();
                        }
                        result.cancel(true);
                        Thread.currentThread().interrupt();
                        break;
                    } catch (TimeoutException ex) {
                        failed = true;
                        if (listener != null) {
                            listener.onTimeout();
                        }
                        result.cancel(true);
                        break;
                    } catch (ExecutionException ex) {
                        failed = true;
                        if (listener != null) {
                            Throwable cause = ex.getCause();
                            if (cause instanceof Exception) {
                                listener.onException((Exception) cause);
                            } else {
                                listener.onException(ex);
                            }
                        }
                        break;
                    }
                }
                if (!failed) {
                    if (listener != null) {
                        listener.onFinish();
                    }
                }
                return !failed;
            }
        });
    }

    public Future<Boolean> undeploy(@NonNull final Collection<String> names,
            @NullAllowed final BatchDeployListener listener) {

        if (listener != null) {
            listener.onStart();
        }

        return DEPLOYMENT_RP.submit(new Callable<Boolean>() {

            @Override
            public Boolean call() {
                boolean failed = false;
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
                            failed = true;
                            if (listener != null) {
                                listener.onFail(lineProcessor.getLastLine());
                            }
                            break;
                        }
                    } catch (InterruptedException ex) {
                        failed = true;
                        if (listener != null) {
                            listener.onInterrupted();
                        }
                        result.cancel(true);
                        Thread.currentThread().interrupt();
                        break;
                    } catch (TimeoutException ex) {
                        failed = true;
                        if (listener != null) {
                            listener.onTimeout();
                        }
                        result.cancel(true);
                        break;
                    } catch (ExecutionException ex) {
                        failed = true;
                        if (listener != null) {
                            Throwable cause = ex.getCause();
                            if (cause instanceof Exception) {
                                listener.onException((Exception) cause);
                            } else {
                                listener.onException(ex);
                            }
                        }
                        break;
                    }
                }
                if (!failed) {
                    if (listener != null) {
                        listener.onFinish();
                    }
                }
                return !failed;
            }
        });
    }

    private BaseExecutionService createService(final String command,
            final LineProcessor processor, String... parameters) {

        org.netbeans.api.extexecution.base.ProcessBuilder builder = org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
        builder.setExecutable(getJavaBinary());
        builder.setRedirectErrorStream(true);
        List<String> arguments = new ArrayList<String>();
        // NB supports only JDK6+ while WL 9, only JDK 5
        Version version = config.getLayout().getDomainVersion();
        if (version == null
                || !version.isAboveOrEqual(WebLogicConfiguration.VERSION_10)) {
            arguments.add("-Dsun.lang.ClassLoader.allowArraySyntax=true"); // NOI18N
        }
        arguments.add("-cp"); // NOI18N
        arguments.add(getClassPath());
        arguments.add("weblogic.Deployer"); // NOI18N
        arguments.add("-adminurl"); // NOI18N
        arguments.add(config.getAdminURL());
        arguments.add("-username"); // NOI18N
        arguments.add(config.getUsername());
        arguments.add("-password"); // NOI18N
        arguments.add(config.getPassword());
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
