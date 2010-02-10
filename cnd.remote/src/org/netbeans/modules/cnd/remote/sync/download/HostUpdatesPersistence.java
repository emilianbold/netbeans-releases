package org.netbeans.modules.cnd.remote.sync.download;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Exceptions;

class HostUpdatesPersistence {

    private final Properties data;
    private final File dataFile;
    private static final String VERSION = "1.0"; // NOI18N
    // NOI18N
    private static final String VERSION_KEY = "____VERSION"; // NOI18N

    public HostUpdatesPersistence(File privProjectStorageDir, ExecutionEnvironment executionEnvironment) {
        super();
        data = new Properties();
        String dataFileName = "downloads-" + executionEnvironment.getHost() + '-' + executionEnvironment.getUser() + '-' + executionEnvironment.getSSHPort(); // NOI18N
        //NOI18N
        dataFile = new File(privProjectStorageDir, dataFileName);
        try {
            load();
            if (!VERSION.equals(data.get(VERSION_KEY))) {
                data.clear();
            }
        } catch (IOException ex) {
            data.clear();
            Exceptions.printStackTrace(ex);
        }
    }

    private void load() throws IOException {
        if (dataFile.exists()) {
            final FileInputStream is = new FileInputStream(dataFile);
            BufferedInputStream bs = new BufferedInputStream(is);
            try {
                data.load(bs);
            } finally {
                bs.close();
            }
        }
    }

    @SuppressWarnings(value = "RV")
    public void store() {
        File dir = dataFile.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.printf("Error creating directory %s\n", dir.getAbsolutePath());
            }
        }
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(dataFile));
            data.setProperty(VERSION_KEY, VERSION);
            data.store(os, null);
            os.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            if (!dataFile.delete()) {
                System.err.printf("Error deleting file %s\n", dataFile.getAbsolutePath());
            }
        }
    }

    public boolean getFileSelected(File file, boolean defaultValue) {
        return getBoolean(file.getAbsolutePath(), defaultValue);
    }

    public void setFileSelected(File file, boolean selected) {
        setBoolean(file.getAbsolutePath(), selected);
    }

    public boolean getRememberChoice() {
        return getBoolean("", false);
    }

    public void setRememberChoice(boolean value) {
        setBoolean("", value);
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        Object value = data.get(key);
        if ("1".equals(value)) { // NOI18N
            return true;
        } else if ("0".equals(value)) { // NOI18N
            return false;
        } else {
            return defaultValue;
        }
    }

    private void setBoolean(String key, boolean value) {
        data.put(key, value ? "1" : "0"); // NOI18N
    }
}
