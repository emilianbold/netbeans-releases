/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.db.mysql.sakila;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.api.sql.execute.SQLExecutionInfo;
import org.netbeans.modules.db.api.sql.execute.SQLExecutor;
import org.netbeans.modules.db.api.sql.execute.StatementExecutionInfo;
import org.netbeans.modules.db.mysql.spi.sample.SampleProvider;
import org.openide.util.NbBundle;

/**
 * Provider implementation for Sakila
 *
 * @author David Van Couvering
 */
public class SakilaSampleProvider implements SampleProvider {
    private static final String SAMPLE_NAME = "sakila";
    private static List<String> sampleNames;
    
    private static final Logger LOGGER = Logger.getLogger(SakilaSampleProvider.class.getName());

    private static final SakilaSampleProvider DEFAULT = new SakilaSampleProvider();
    
    public static SakilaSampleProvider getDefault() {
        return DEFAULT;
    }

    public void create(String sampleName, DatabaseConnection dbconn) throws DatabaseException {
        if (! SAMPLE_NAME.equals(sampleName)) {
            throw new DatabaseException(NbBundle.getMessage(this.getClass(),
                    "ERR_SampleNotSupported", sampleName));
        }
        createTables(dbconn);
        processDataFile(dbconn);
        processMrHillyer(dbconn);;
    }

    public boolean supportsSample(String name) {
        return name.equals(SAMPLE_NAME);
    }

    public List<String> getSampleNames() {
        if (sampleNames == null) {
            sampleNames = new ArrayList<String>();
            sampleNames.add(SAMPLE_NAME);
        }
        return sampleNames;
    }


    private static InputStream getFileStreamFromResourceDir(String filename)
            throws DatabaseException {
        InputStream stream = null;

        try {
            stream = SakilaSampleProvider.class.getClassLoader().getResourceAsStream("org/netbeans/modules/db/mysql/sakila/resources/" + filename);

        } catch (Exception e) {
            DatabaseException dbe = new DatabaseException(
                    NbBundle.getMessage(SakilaSampleProvider.class, "MSG_ErrorLoadingFile", filename, e.getMessage()));
            dbe.initCause(e);
            throw dbe;
        }

        if (stream == null) {
            throw new DatabaseException(NbBundle.getMessage(SakilaSampleProvider.class,
                    "MSG_FileNotFound", filename));
        }

        return stream;
    }

    private void createTables(DatabaseConnection dbconn) throws DatabaseException {
        // The schema file is small enough, we can read it in all at once'
        BufferedReader reader = new BufferedReader(new InputStreamReader(getFileStreamFromResourceDir("sakila-schema.sql"))); // NOI18N
        StringBuilder builder = new StringBuilder();
        
        try {
            String line = reader.readLine();
            while (line != null) {
                // Re-add the newline or our SQL parser gets confused - it's line-based
                builder.append(line + "\n");
                line = reader.readLine();
            }
        } catch (IOException ioe) {
            throw new DatabaseException(ioe);
        }

        executeSQL(dbconn, builder.toString());
    }
    
    private void executeSQL(DatabaseConnection dbconn, String sql) throws DatabaseException {
        SQLExecutionInfo info = SQLExecutor.execute(dbconn, sql);
        if (info.hasExceptions()) {
            for (StatementExecutionInfo stmt : info.getStatementInfos()) {
                if (stmt.hasExceptions()) {
                    LOGGER.log(Level.INFO, NbBundle.getMessage(this.getClass(), 
                        "ERR_SQLHadErrors", stmt.getSQL() + "\n")); // NOI18N
                    for (Throwable t : stmt.getExceptions()) {
                        LOGGER.log(Level.INFO, null, t);
                    }
                }
            }

            throw new DatabaseException(NbBundle.getMessage(this.getClass(), "ERR_ExecutionFailed"));
        }        
    }


    private void processDataFile(DatabaseConnection dbconn) throws DatabaseException {
        SQLStreamChunker chunker = new SQLStreamChunker(getFileStreamFromResourceDir("sakila-data.sql")); // NOI18N
        
        for (String chunk = chunker.nextChunk() ; chunk != null ; chunk = chunker.nextChunk()) {
            executeSQL(dbconn, chunk);
        }
    }

    static final int HILLYER_LENGTH = 36365;
    static final String HILLYER_NAME = "hillyer.png";
    private void processMrHillyer(DatabaseConnection dbconn)  throws DatabaseException {
        // Read the photo of Mike Hillyer and load it directly
        // into the database using JDBC.  Doing this directly through our
        // SQL processor code wasn't working...
        String sql = "UPDATE staff SET picture = ? WHERE last_name = 'Hillyer'";

        
        InputStream stream = null;
        PreparedStatement ps = null;
        Connection conn = null;
        try {
          stream = getFileStreamFromResourceDir(HILLYER_NAME);
          assert(stream != null);
          
          conn = dbconn.getJDBCConnection();
          assert(conn != null);
          conn.setAutoCommit(false);
          ps = conn.prepareStatement(sql);
          ps.setBinaryStream(1, stream, (int) HILLYER_LENGTH);
          ps.executeUpdate();
          conn.commit();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            try {
              ps.close();
              stream.close();
            } catch (Exception e) {
                LOGGER.log(Level.FINE, null, e);
            }
        }
  }

    /**
     * Works with an InputStream to deliver SQL in chunks, rather than all at once,
     * so we don't read a massive SQL file into memory.
     */
    private class SQLStreamChunker {
        private static final int DEFAULT_CHUNK_SIZE = 100000;

        private final BufferedReader reader;
        private final int chunkSize;

        public SQLStreamChunker(InputStream sqlStream, int chunkSize) {
            reader = new BufferedReader(new InputStreamReader(sqlStream));
            this.chunkSize = chunkSize;
        }

        public SQLStreamChunker(InputStream sqlStream) {
            this(sqlStream, DEFAULT_CHUNK_SIZE);
        }

        /**
         * Reads in a chunk of SQL and then reads up to
         * the next commit statement or to the end of the file.
         *
         * @return the next chunk of SQL or null if at end of file
         *
         * @throws org.netbeans.api.db.explorer.DatabaseException if there was an error reading the stream
         */
        public String nextChunk() throws DatabaseException {
            StringBuilder builder = new StringBuilder();

            try {
                String line = reader.readLine();
                int bytesRead = 0;

                while (line != null && bytesRead < chunkSize )  { 
                    // Re-insert the newline so our SQL parser doesn't get confused.
                    builder.append(line + "\n");

                    line = reader.readLine();                    
                    if (line != null) {
                        bytesRead += line.length();
                    }
                }

                // Now read up to the next commit statement (or EOF whichever comes first
                while ( line != null && (! line.toLowerCase().contains("commit;"))) {
                    builder.append(line + "\n");
                    line = reader.readLine();
                }
            } catch (IOException ioe) {
                throw new DatabaseException(ioe);
            }

            if (builder.length() == 0) {
                return null;
            } else {
                return builder.toString();
            }
        }

    }

}
