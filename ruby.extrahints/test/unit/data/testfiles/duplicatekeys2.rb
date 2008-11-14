some_function1("a" => 1, "b"=> 2, "a" => 3)
some_function2(:a => 1, :b=> 2, :a => 3)
some_function3(1 => :x, 2=> :y, 1 => :z)
mixed_function1("a" =>1, :a => 1)
mixed_function2("1" =>1, 1 => 1)

  SIGNALS = {
    'HUP'     => :reload,
    'INT'     => :exit_now,
    'TERM'    => :exit_now,
    'USR1'    => :exit,
    'USR1'    => :exit,
    'USR2'    => :restart
  }


