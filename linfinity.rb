class Lin
  attr_reader :position, :direction, :speed, :split, :merge, :char

  def initialize
    @position = 0
    @direction = 1
    @speed = 1
    @split = 0.05
    @merge = 0.25
    @char = '1'
  end

  def move
    @position = position + direction * speed
  end

  def bounce
    @direction = -direction
  end
end

class Row
  attr_reader :lin_map, :char, :size, :last_index
  def initialize lins, size
    @char = '0'
    @size = size
    @last_index = size - 1
    @lin_map = lins.each_with_object({}) do |lin, obj|
      obj[move(lin)] = lin
    end
  end

  def move lin
    lin.move
    pos = lin.position
    if pos >= last_index
      lin.bounce
      last_index
    elsif pos < 1
      lin.bounce
      0
    else
      pos
    end
  end

  def char_at position
    if lin = lin_map[position]
      lin.char
    else
      char
    end
  end

  def display
    puts size.times.inject('') { |row, i|
      row + char_at(i)
    }
  end
end

class Linfinity
  attr_reader :delay, :size

  def initialize opts = {delay: 0.1, size: 40}
    @delay = opts[:delay]
    @size = opts[:size]
  end

  def run
    loop.inject([Lin.new]) do |lins, _|
      Row.new(lins, size).display
      sleep delay
      lins
    end
  end
end
