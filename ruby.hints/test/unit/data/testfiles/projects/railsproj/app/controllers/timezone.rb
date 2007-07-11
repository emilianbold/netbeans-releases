require 'date'
require 'tzinfo/country'
require 'tzinfo/time_or_datetime'
require 'tzinfo/timezone_period'

module TZInfo
  def period_for_local(local, dst = nil)            
    results = periods_for_local(local)
      
    if results.empty?
      raise PeriodNotFound
    elsif results.size < 2
      results.first
    else
      # ambiguous result try to resolve
        
      if !dst.nil?
        matches = results.find_all {|period| period.dst? == dst}
        results = matches if !matches.empty?            
      end
        
      if results.size < 2
        results.first
      else
        # still ambiguous, try the block
                    
        if block_given?
          results = yield results
        end
          
        if results.is_a?(TimezonePeriod)
          results
        elsif results && results.size == 1
          results.first
        else          
          raise AmbiguousTime, "#{local} is an ambiguous local time."
        end
      end
    end      
  end
end
