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
package com.abien.business.orderprocessor.boundary;

import com.abien.business.orderprocessor.control.BillingService;
import com.abien.business.orderprocessor.control.DeliveryService;
import com.abien.business.orderprocessor.control.Warehouse;
import com.abien.business.orderprocessor.entity.Order;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
//import static org.mockito.Mockito.*;

/** This Unit test uses the Mockito library
 * See: http://mockito.org/
 *
 * @author Adam Bien (blog.adam-bien.com)
 */
public class OrderServiceTest {

    private OrderService orderService;

    @Before
    public void initOrderService() {
        this.orderService = new OrderService();
        this.orderService.billing = new BillingService();
        this.orderService.delivery = new DeliveryService();
        // the following can be uncommented when Mocito library is added to test classpath
        /*
        this.orderService.warehouse = mock(Warehouse.class);
         */
    }

    @Test
    public void testOrder() {
        Order order = new Order(200, 20, "Adidas");
        // the following can be uncommented when Mocito library is added to test classpath
        /*
        when(this.orderService.warehouse.checkout(order)).thenReturn(order);
        Order ordered = this.orderService.order(order);
        assertNotNull(ordered);
        this.orderService.delivery.deliver(ordered);
        this.orderService.billing.payForOrder(ordered);
         */
    }
}
