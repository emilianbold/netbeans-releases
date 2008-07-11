/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.db.mysql.sakila;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.mysql.spi.sample.SampleProvider;

/**
 * Provider implementation for Sakila
 *
 * @author David Van Couvering
 */
public class SakilaSampleProvider implements SampleProvider {
    private static final String SAMPLE_NAME = "sakila";
    private static List<String> sampleNames;

    private static final SakilaSampleProvider DEFAULT = new SakilaSampleProvider();

    public static SakilaSampleProvider getDefault() {
        return DEFAULT;
    }

    public void create(String sampleName, DatabaseConnection dbconn) throws DatabaseException {
        // Do nothing for now
        // TODO - add code to execute SQL for Sakila
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

}
