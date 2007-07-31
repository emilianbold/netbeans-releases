/*
 * Utils.java
 */

package org.netbeans.test.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;

public class Utils {

    public static final long MAX_EXECUTION_TIME = 60000000;
    public static final long MAX_INSTALATION_WAIT = 60000000;
    public static final int DELAY = 50;

    public static int getInstaller(TestData data) {
        //File sourceBandle = new File(data.getInstallerFileName());
        File destBundle = new File(data.getTestWorkDir() + File.separator + "installer" + "." + data.getPlatformExt());

        InputStream in = null;
        FileOutputStream out = null;

        try {
            URL sourceBandeleURL = new URL(data.getInstallerURL(data.getInstallerType()));

            in = sourceBandeleURL.openStream();
            out = new FileOutputStream(destBundle);

            byte[] buffer = new byte[10240];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException ex) {
            data.getLogger().log(Level.SEVERE, "Can not get bundle", ex);
            return -2;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    data.getLogger().log(Level.SEVERE, "Can not get bundle", ex);
                    return -3;
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    data.getLogger().log(Level.SEVERE, "Can not get bundle", ex);
                    return -4;
                }
            }
        }
        data.setInstallerFile(destBundle);
        return 0;
    }

    public static int extractBundle(TestData data) {
        int errorLevel = 0;

        try {
            java.lang.String command = null;
            command = data.getInstallerFile().getCanonicalPath();

            java.lang.ProcessBuilder builder;
            java.lang.Process process;

            if (0 != data.getPaltformName().compareTo("windows")) {
                if (data.getPaltformName().contains("linux") || data.getPaltformName().contains("solaris")) {
                    builder = new java.lang.ProcessBuilder("chmod", "+x", command);
                    process = builder.start();

                    long runningTime;
                    for (runningTime = 0; runningTime < MAX_EXECUTION_TIME; runningTime += DELAY) {
                        try {
                            errorLevel = process.exitValue();
                            break;
                        } catch (IllegalThreadStateException e) {
                            ; // do nothing - the process is still running
                        }
                    }

                    if (runningTime >= MAX_EXECUTION_TIME) {
                        process.destroy();
                        data.getLogger().log(Level.SEVERE, "Timeout. Chmod process destroyed");
                        return -3;
                    } else if (errorLevel != 0) {
                        return errorLevel;
                    }
                }
            }

            String pathToExtract = data.getTestWorkDir() + java.io.File.separator + "bundle";

            builder = new java.lang.ProcessBuilder(command, "--silent", "--userdir", data.getTestWorkDir().getCanonicalPath(), "--extract", pathToExtract, "--output", "inst.log");


/*            for (Iterator it = builder.command().iterator(); it.hasNext();) {
            String str = (String)it.next();
            data.getLogger().log(Level.SEVERE, "Command " + str);
            }*/


            process = builder.start();

            long runningTime;
            for (runningTime = 0; runningTime < MAX_EXECUTION_TIME; runningTime += DELAY) {
                try {
                    errorLevel = process.exitValue();
                    break;
                } catch (IllegalThreadStateException e) {
                    ; // do nothing - the process is still running
                }
            }

            if (runningTime >= MAX_EXECUTION_TIME) {
                process.destroy();
                data.getLogger().log(Level.SEVERE, "Timeout. Process destroyed");
                return -1;
            } else if (errorLevel == 0) {
                data.setBundleFile(new File(pathToExtract + java.io.File.separator + "bundle.jar"));
                data.getInstallerFile().deleteOnExit();
                return 0;
            } else {
                return errorLevel;
            }
        } catch (IOException ex) {
            data.getLogger().log(Level.SEVERE, null, ex);
            return -2;
        }
    }

    public static int loadClasses(TestData data) {
        try {

            data.setClassLoader(new URLClassLoader(new URL[]{data.getBundleFile().toURI().toURL()}, data.getClass().getClassLoader()));
            data.setInstallerMainClass(Class.forName(data.getInstallerMainClassName(), true, data.getClassLoader()));
        } catch (IllegalArgumentException ex) {
            data.getLogger().log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            data.getLogger().log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            data.getLogger().log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            data.getLogger().log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    public static int runInstaller(final TestData data) {
        (new Runnable() {

            public void run() {
                try {
                    data.getInstallerMainClass().getMethod("main", java.lang.String[].class).invoke(null, (java.lang.Object) (new String[] {}));
                } catch (NoSuchMethodException ex) {
                    data.getLogger().log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    data.getLogger().log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    data.getLogger().log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    data.getLogger().log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    data.getLogger().log(Level.SEVERE, null, ex);
                }
            }
        }).run();
        return 0;
    }

    public static int runUninstaller(final TestData data) {
        (new Runnable() {

            public void run() {
                try {
                    //dirty hack --ignore-lock
                    data.getUninstallerMainClass().getMethod("main", java.lang.String[].class).invoke(null, (java.lang.Object) (new String[] {"--force-uninstall", "--ignore-lock"}));
                } catch (NoSuchMethodException ex) {
                    data.getLogger().log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    data.getLogger().log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    data.getLogger().log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    data.getLogger().log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    data.getLogger().log(Level.SEVERE, null, ex);
                }
            }
        }).run();
        return 0;
    }
    
    public static int loadEngineClasses(TestData data) {
        try {
            String jarFileName = data.getWorkDirCanonicalPath() + File.separator + ".nbi" + File.separator + "nbi-engine.jar";

            data.setEngineClassLoader(new URLClassLoader(new URL[]{new File(jarFileName).toURI().toURL()}, data.getClass().getClassLoader()));
            data.setUninstallerMainClass(Class.forName(data.getUninstallerMainClassName(), true, data.getEngineClassLoader()));
        } catch (IllegalArgumentException ex) {
            data.getLogger().log(Level.SEVERE, "-1", ex);
            return -1;
        } catch (SecurityException ex) {
            data.getLogger().log(Level.SEVERE, "-2", ex);
            return -2;
        } catch (ClassNotFoundException ex) {
            data.getLogger().log(Level.SEVERE, "-3", ex);
            return -3;
        } catch (MalformedURLException ex) {
            data.getLogger().log(Level.SEVERE, "-4", ex);
            return -4;
        }

        return 0;
    }
    
}
