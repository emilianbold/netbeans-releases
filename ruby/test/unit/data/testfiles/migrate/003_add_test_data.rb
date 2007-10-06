# Copyright (c) 2006 The Pragmatic Programmers, LLC.
# Reproduced from the book "Agile Web Development with Rails, 2nd Ed.",
# published by The Pragmatic Bookshelf.
# Available from www.pragmaticprogrammer.com/titles/rails2
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this source code (the "Software"), to deal in the Software without
# restriction, including without limitation the rights to use, copy, modify,
# merge, publish, distribute, sublicense, and/or sell copies of the Software,
# and to permit persons to whom the Software is furnished to do so, subject to
# the following conditions:
# 
# 1) This Software cannot be used in any training course or seminar, whether
# presented live, via video, audio, screencast, or any other media, without
# explicit prior permission from the publisher.
# 
# 2) The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE. 

#START:vcc
class AddTestData < ActiveRecord::Migration
  def self.up
#END:vcc    
    Product.create(:title => 'Pragmatic Project Automation',
    :description => 
    %{<p>
       <em>Pragmatic Project Automation</em> shows you how to improve the 
       consistency and repeatability of your project's procedures using 
       automation to reduce risk and errors.
      </p>
      <p>
        Simply put, we're going to put this thing called a computer to work 
        for you doing the mundane (but important) project stuff. That means 
        you'll have more time and energy to do the really 
        exciting---and difficult---stuff, like writing quality code.
      </p>},
    :image_url =>   '/images/auto.jpg',    
    :price => 29.95)

#START:vcc
    Product.create(:title => 'Pragmatic Version Control',
      :description =>
      %{<p>
         This book is a recipe-based approach to using Subversion that will 
         get you up and 
         running quickly---and correctly. All projects need version control: 
         it's a foundational piece of any project's infrastructure. Yet half 
         of all project teams in the U.S. don't use any version control at all. 
         Many others don't use it well, and end up experiencing time-consuming problems.
      </p>},
    :image_url => '/images/svn.jpg',
    :price => 28.50)
    
    # . . .
#END:vcc

    Product.create(:title => 'Pragmatic Unit Testing (C#)',
    :description => 
    %{<p>
        Pragmatic programmers use feedback to drive their development and 
        personal processes. The most valuable feedback you can get while 
        coding comes from unit testing.
      </p>
      <p>
        Without good tests in place, coding can become a frustrating game of 
        "whack-a-mole." That's the carnival game where the player strikes at a 
        mechanical mole; it retreats and another mole pops up on the opposite side 
        of the field. The moles pop up and down so fast that you end up flailing 
        your mallet helplessly as the moles continue to pop up where you least 
        expect them.
      </p>},
    :image_url => '/images/utc.jpg',
    :price => 27.75)

  end

#START:vcc
  def self.down
    Product.delete_all
  end
end
#END:vcc
