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
package org.netbeans.modules.docker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.modules.docker.api.Credentials;

/**
 *
 * @author Petr Hejl
 */
public class DockerConfig {

    private static final String DOCKER_HUB_DOMAIN = "index.docker.io";

    private static final String DOCKER_HUB_REGISTRY = "https://" + DOCKER_HUB_DOMAIN + "/v1/";

    private final File dockerConfig;

    public DockerConfig(File dockerConfig) {
        this.dockerConfig = dockerConfig;
    }

    public Credentials load(String registry) throws IOException {
        if (!dockerConfig.isFile()) {
            return null;
        }

        Set<String> names = generateRegistryNames(registry);

        JSONObject current = parse();
        JSONObject value = null;
        Iterator<String> it = names.iterator();
        while (value == null && it.hasNext()) {
            value = (JSONObject) current.get(it.next());
        }
        if (value == null) {
            return null;
        }

        byte[] auth = Base64.getDecoder().decode((String) value.get("auth")); // NOI18N
        CharBuffer chars = Charset.forName("UTF-8").newDecoder().decode(ByteBuffer.wrap(auth)); // NOI18N
        int index = -1;
        for (int i = 0; i < chars.length(); i++) {
            if (chars.get(i) == ':') {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new IOException("Malformed registry authentication record");
        }
        String username = new String(chars.array(), 0, index);
        char[] password = new char[chars.length() - index - 1];
        if (password.length > 0) {
            System.arraycopy(chars.array(), index + 1, password, 0, password.length);
        }
        return new Credentials(registry, username, password, (String) value.get("email")); // NOI18N
    }

    public void save(Credentials credentials) throws IOException {
        StringBuilder sb = new StringBuilder(credentials.getUsername());
        sb.append(':');
        sb.append(credentials.getPassword());
        String auth = Base64.getEncoder().encodeToString(sb.toString().getBytes("UTF-8")); // NOI18N

        JSONObject value = new JSONObject();
        value.put("auth", auth); // NOI18N
        value.put("email", credentials.getEmail()); // NOI18N

        JSONObject current = parse();
        current.put(credentials.getRegistry(), value);
        try (Writer w = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(dockerConfig)), "UTF-8")) { // NOI18N
            current.writeJSONString(w);
        }
    }

    private JSONObject parse() throws IOException {
        try (Reader r = new InputStreamReader(new BufferedInputStream(new FileInputStream(dockerConfig)), "UTF-8")) { // NOI18N
            JSONParser parser = new JSONParser();
            try {
                return (JSONObject) parser.parse(r);
            } catch (ParseException ex) {
                throw new IOException(ex);
            }
        }
    }

    private static Set<String> generateRegistryNames(String registry) {
        Set<String> result = new LinkedHashSet<>();

        if (registry == null) {
            result.add(DOCKER_HUB_REGISTRY);
            result.addAll(generateRegistryNames(DOCKER_HUB_DOMAIN));
            return result;
        }

        result.add(registry);
        generatePaths(registry, result);

        if (!registry.contains("://")) {
            String https = "https://" + registry;
            result.add(https);
            generatePaths(https, result);
            String http = "http://" + registry;
            result.add(http);
            generatePaths(http, result);
        }
        return result;
    }

    private static void generatePaths(String registry, Set<String> result) {
        StringBuilder extended = new StringBuilder(registry);
        if (registry.endsWith("/")) {
            extended.append("v1");
        } else {
            extended.append("/v1");
        }
        result.add(extended.toString());
        extended.append("/");
        result.add(extended.toString());
    }
}
