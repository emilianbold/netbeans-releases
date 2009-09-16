/**
This file is part of javaee-patterns.

javaee-patterns is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

javaee-patterns is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.opensource.org/licenses/gpl-2.0.php>.

* Copyright (c) 12. July 2009 Adam Bien, blog.adam-bien.com
* http://press.adam-bien.com
*/
package com.abien.business.orderprocessor.control;

import com.abien.business.orderprocessor.entity.Order;
import java.util.Collections;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Adam Bien (blog.adam-bien.com)
 */
@Stateless
public class Warehouse {
    
    @PersistenceContext
    EntityManager em;

    public Order checkout(Order order){
        this.em.persist(order);
        System.out.println("Ordered: Product Id:"+order.getProductId() + " Customer:"+order.getCustomer() + " Amount:"+order.getAmount());
        return order;
    }

    public List<Order> allOrders() {
        List<Order> result = em.createNamedQuery(Order.findAll).getResultList();
        return result == null ? Collections.<Order>emptyList() : result;
    }

    public Order findOrder(long id){
        return this.em.find(Order.class, id);
    }

    public void removeOrder(long orderId) {
        Order order = findOrder(orderId);
        if (order != null) {
            em.remove(order);
        }

    }

}
