function drawES6Chart({size = 'big', cords = { x: 0, y: 0 }, radius = 25} = test) {
  console.log(size, cords, radius);
  // do some chart drawing
}

drawES6Chart({   
  cords: { x: 18, y: 30 },
  radius: 30
});

drawES6Chart({size : "small", cords: {x: -5, y: -5}, radius : 5});