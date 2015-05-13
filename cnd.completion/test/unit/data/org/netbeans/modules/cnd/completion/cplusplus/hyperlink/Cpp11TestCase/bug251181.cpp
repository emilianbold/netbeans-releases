namespace bug251181 {
    namespace parsing_test251181 {
        struct AAA251181 {
            int boo();
        };

        int zoo251181() { 
            if (true) {
                auto x = []()->AAA251181{ 
                    return AAA251181(); 
                };
                return x().boo();
            } else {
                return []() mutable noexcept(1+1) ->AAA251181{ 
                    return AAA251181(); 
                }().boo();        
            }
        }
    }
    
    namespace overloading_test251181 {
        struct true_type251181
        {
          static constexpr bool value = true;
        };

        struct false_type251181
        {
          static constexpr bool value = false;
        };

        struct Runnable251181 {};

        template <typename T>
        class my_function251181 {};

        template<typename _Res, typename... _ArgTypes>
        class my_function251181<_Res(_ArgTypes...)> {
        public:
            template <typename T>
            my_function251181(T functor) {};
        };

        namespace Variant1_251181 {
          typedef int a_time251181;

          struct Scheduler251181 { 
              void schedule(Runnable251181 *r, bool deleteOnComplete = false);
              void schedule(my_function251181<void ()> func);
              void schedule(a_time251181 period, a_time251181 duration, Runnable251181 *r);
              void schedule(a_time251181 period, a_time251181 duration, my_function251181<void ()> f);
          };

          void foo251181() {
            Scheduler251181 scheduler;
            Runnable251181 runnable;
            scheduler.schedule(&runnable);
            scheduler.schedule([](){ return; });
            scheduler.schedule(1000, 1000, &runnable);
            scheduler.schedule(1000, 1000, [](){ return; });
          }
        }

        namespace Variant2_251181 {
          struct a_time251181 {
            a_time251181(int time) {}
          };

          struct Scheduler251181 {
              void schedule(Runnable251181 *r, bool deleteOnComplete = false);
              void schedule(my_function251181<void ()> func);
              void schedule(a_time251181 period, a_time251181 duration, Runnable251181 *r);
              void schedule(a_time251181 period, a_time251181 duration, my_function251181<void ()> f);
          };

          void foo251181() {
            Scheduler251181 scheduler;
            Runnable251181 runnable;
            scheduler.schedule(&runnable);
            scheduler.schedule([](){ return; });
            scheduler.schedule(1000, 1000, &runnable);
            scheduler.schedule(1000, 1000, [](){ return; });
          }
        }
    }
}