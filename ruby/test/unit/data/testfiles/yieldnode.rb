        class Foo
           def exit_test
              if component.size == 1
                yield component.first
              else
                raise Cyclic.new("topological sort failed: #{component.inspect}")
              end
           end
        end
