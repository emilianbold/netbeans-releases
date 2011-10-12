/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package customerdb.service;
import com.sun.jersey.api.spring.Autowire;
import com.sun.jersey.spi.resource.Singleton;
import javax.persistence.EntityManager;
import customerdb.DiscountCode;
import java.net.URI;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import org.springframework.transaction.annotation.Transactional;


/**
 *
 * @author __USER__
 */
@Path("customerdb.discountcode")
@Singleton
@Autowire
public class DiscountCodeRESTFacade {
    @PersistenceContext(unitName = "CustomerDBSpringPU")
    @Error("Please fix your project manually, for instructions see http://wiki.netbeans.org/SpringWithAopalliance")
    protected EntityManager entityManager;

    public DiscountCodeRESTFacade() {
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Transactional
    public Response create(DiscountCode entity) {
        entityManager.persist(entity);
        return Response.created(URI.create(entity.getDiscountCode().toString())).build();
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    @Transactional
    public void edit(DiscountCode entity) {
        entityManager.merge(entity);
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public void remove(@PathParam("id") String id) {
        DiscountCode entity = entityManager.getReference(DiscountCode.class, id.charAt(0));
        entityManager.remove(entity);
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    @Transactional
    public DiscountCode find(@PathParam("id") String id) {
        return entityManager.find(DiscountCode.class, id.charAt(0));
    }

    @GET
    @Produces({"application/xml", "application/json"})
    @Transactional
    public List<DiscountCode> findAll() {
        return find(true, -1, -1);
    }

    @GET
    @Path("{max}/{first}")
    @Produces({"application/xml", "application/json"})
    @Transactional
    public List<DiscountCode> findRange(@PathParam("max") Integer max, @PathParam("first") Integer first) {
        return find(false, max, first);
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    @Transactional
    public String count() {
        try {
            Query query = entityManager.createQuery("SELECT count(o) FROM DiscountCode AS o");
            return query.getSingleResult().toString();
        } finally {
            entityManager.close();
        }
    }

    private List<DiscountCode> find(boolean all, int maxResults, int firstResult) {
        try {
            Query query = entityManager.createQuery("SELECT object(o) FROM DiscountCode AS o");
            if (!all) {
                query.setMaxResults(maxResults);
                query.setFirstResult(firstResult);
            }
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }
    
}
