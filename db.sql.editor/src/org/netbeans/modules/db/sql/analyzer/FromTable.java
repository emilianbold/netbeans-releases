/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.db.sql.analyzer;

import java.util.List;

/**
 *
 * @author Andrei Badea
 */
public class FromTable {

    private final List<String> parts;
    private final String alias;

    public FromTable(List<String> parts, String alias) {
        this.parts = parts;
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public List<String> getParts() {
        return parts;
    }
}
