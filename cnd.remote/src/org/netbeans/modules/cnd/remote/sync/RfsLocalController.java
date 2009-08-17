package org.netbeans.modules.cnd.remote.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.Exceptions;

class RfsLocalController implements Runnable {

    private final BufferedReader requestReader;
    private final PrintStream responseStream;
    private final String remoteDir;
    private final File localDir;
    private final ExecutionEnvironment execEnv;
    private final PrintWriter err;
    private final File privProjectStorageDir;
    private final TimestampAndSharabilityFilter filter;
    private static final Logger logger = Logger.getLogger("cnd.remote.logger"); //NOI18N
    private final Set<String> processedFiles = new HashSet<String>();

    public RfsLocalController(ExecutionEnvironment executionEnvironment, File localDir, String remoteDir, InputStream requestStream, OutputStream responseStream, PrintWriter err, File privProjectStorageDir) {
        super();
        this.execEnv = executionEnvironment;
        this.localDir = localDir;
        this.remoteDir = remoteDir;
        this.requestReader = new BufferedReader(new InputStreamReader(requestStream));
        this.responseStream = new PrintStream(responseStream);
        this.err = err;
        this.privProjectStorageDir = privProjectStorageDir;
        this.filter = new TimestampAndSharabilityFilter(privProjectStorageDir, execEnv);
    }

    private void respond_ok() {
        responseStream.printf("1\n");
        // NOI18N
        responseStream.flush();
    }

    private void respond_err(String tail) {
        responseStream.printf("0 %s\n", tail);
        // NOI18N
        responseStream.flush();
    }

    public void run() {
        long totalCopyingTime = 0;
        while (true) {
            try {
                String request = requestReader.readLine();
                String remoteFile = request;
                logger.finest("LC: REQ " + request);
                if (request == null) {
                    break;
                }
                if (processedFiles.contains(remoteFile)) {
                    logger.info("RC asks for file " + remoteFile + " again?!");
                    respond_ok();
                    continue;
                } else {
                    processedFiles.add(remoteFile);
                }
                if (remoteFile.startsWith(remoteDir)) {
                    File localFile = new File(localDir, remoteFile.substring(remoteDir.length()));
                    if (localFile.exists() && !localFile.isDirectory()) {
                        if (filter.accept(localFile)) {
                            logger.finest("LC: uploading " + localFile + " to " + remoteFile + " started");
                            long fileTime = System.currentTimeMillis();
                            Future<Integer> task = CommonTasksSupport.uploadFile(localFile.getAbsolutePath(), execEnv, remoteFile, 511, err);
                            try {
                                int rc = task.get();
                                fileTime = System.currentTimeMillis() - fileTime;
                                totalCopyingTime += fileTime;
                                System.err.printf("LC: uploading %s to %s finished; rc=%d time =%d total time = %d ms \n", localFile, remoteFile, rc, fileTime, totalCopyingTime);
                                if (rc == 0) {
                                    respond_ok();
                                } else {
                                    respond_err("1");
                                }
                            } catch (InterruptedException ex) {
                                filter.dropTimestamp(localFile);
                                Exceptions.printStackTrace(ex);
                                break;
                            } catch (ExecutionException ex) {
                                filter.dropTimestamp(localFile);
                                Exceptions.printStackTrace(ex);
                                respond_err("2 execution exception\n");
                            } finally {
                                responseStream.flush();
                            }
                        } else {
                            logger.finest("LC: file " + localFile + " not accepted");
                            respond_ok();
                        }
                    } else {
                        respond_ok();
                    }
                } else {
                    respond_ok();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        filter.flush();
    }
}
