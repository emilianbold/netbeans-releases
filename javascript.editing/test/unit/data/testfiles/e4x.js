// From http://www.w3schools.com/e4x/e4x_example.asp
var order = new XML()

order=<order id="555">
<date>2005-08-01</date>
<customer>
  <firstname>John</firstname>
  <lastname>Johnson</lastname>
</customer>
<item>
  <name>Maxilaku</name>
  <qty>5</qty>
  <price>155.00</price>
</item>
</order>

// Calculate the price:
var total=order.item.qty * order.item.price

//Display the customers full name:
document.write(order.customer.lastname)
document.write(",")
document.write(order.customer.firstname)

//Add a new item:
order.item+=
<item>
  <name>Pavlova</name>
  <qty>10</qty>
  <price>128.00</price>
</item>

//Display the order id:
document.write(order.@id)


