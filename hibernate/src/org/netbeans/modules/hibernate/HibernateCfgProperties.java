/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hibernate;

import org.hibernate.cfg.Environment;

/**
 * This class contains all the properties in the Hibernate configuration file
 * 
 * @author Dongmei Cao
 */
public class HibernateCfgProperties {
    
    public final static String[] jdbcProps = new String[] {
        Environment.DRIVER,
        Environment.URL,
        Environment.USER,
        Environment.PASS,
        Environment.POOL_SIZE
    };
    
    public final static String[] datasourceProps = new String[] {
        Environment.DATASOURCE,
        Environment.JNDI_URL,
        Environment.JNDI_CLASS,
        Environment.USER,
        Environment.PASS
    };
    
    public final static String[] optionalConfigProps = new String[] {
        Environment.DIALECT,
        Environment.SHOW_SQL,
        Environment.FORMAT_SQL,
        Environment.DEFAULT_SCHEMA,
        Environment.DEFAULT_CATALOG,
        Environment.SESSION_FACTORY_NAME,
        Environment.MAX_FETCH_DEPTH,
        Environment.DEFAULT_BATCH_FETCH_SIZE,
        Environment.DEFAULT_ENTITY_MODE,
        Environment.ORDER_UPDATES,
        Environment.GENERATE_STATISTICS,
        Environment.USE_IDENTIFIER_ROLLBACK,
        Environment.USE_SQL_COMMENTS
    };
    
    public final static String[] optionalJdbcConnProps = new String[] {
        Environment.STATEMENT_FETCH_SIZE,
        Environment.STATEMENT_BATCH_SIZE,
        Environment.BATCH_VERSIONED_DATA,
        Environment.BATCH_STRATEGY,
        Environment.USE_SCROLLABLE_RESULTSET,
        Environment.USE_STREAMS_FOR_BINARY,
        Environment.USE_GET_GENERATED_KEYS,
        Environment.CONNECTION_PROVIDER,
        Environment.ISOLATION,
        Environment.AUTOCOMMIT,
        Environment.RELEASE_CONNECTIONS
    };
    
    public final static String[] optionalCacheProps = new String[] {
        Environment.CACHE_PROVIDER,
        Environment.USE_MINIMAL_PUTS,
        Environment.USE_QUERY_CACHE,
        Environment.USE_SECOND_LEVEL_CACHE,
        Environment.QUERY_CACHE_FACTORY,
        Environment.CACHE_REGION_PREFIX,
        Environment.USE_STRUCTURED_CACHE
    };
    
    public final static String[] optionalTransactionProps = new String[] {
        Environment.TRANSACTION_STRATEGY,
        Environment.USER_TRANSACTION,
        Environment.TRANSACTION_MANAGER_STRATEGY,
        Environment.FLUSH_BEFORE_COMPLETION,
        Environment.AUTO_CLOSE_SESSION
    };
    
    public final static String[] optionalMiscProps = new String[] {
        Environment.CURRENT_SESSION_CONTEXT_CLASS,
        Environment.QUERY_TRANSLATOR,
        Environment.QUERY_SUBSTITUTIONS,
        Environment.HBM2DDL_AUTO,
        Environment.USE_REFLECTION_OPTIMIZER
    };

}
