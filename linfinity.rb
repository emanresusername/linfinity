class Lin
  attr_reader :position, :direction, :speed, :split, :merge, :random

  def initialize
    @position = 0
    @direction = 1
    @speed = 1
    @split = 0.01
    @merge = 0.25
    @random = Random.new
  end

  def move
    @position = position + direction * speed
  end

  def bounce
    @direction = -direction
  end

  def split?
    random.rand < split
  end

  def merge?
    random.rand < merge
  end

  # next generation of lin(s)
  def next_gin
    if split?
      bounced = dup
      bounced.bounce
      [self, bounced]
    else
      [self]
    end
  end
end

class Row
  attr_reader :lins, :lin_positions, :size, :last_index
  def initialize lins, size
    @size = size
    @last_index = size - 1
    @lins = lins
    @lin_positions = lins.inject({}) do |hash, lin|
      hash.merge(move(lin) => true)
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

  def display
    puts size.times.inject('') { |row, i|
      row + (lin_positions[i] ? '1' : '0')
    }
  end
end

class Linfinity
  attr_reader :delay, :size

  def initialize opts = {delay: 0.1, size: 157}
    @delay = opts[:delay]
    @size = opts[:size]
  end

  def run
    loop.inject([Lin.new]) do |lins, _|
      Row.new(lins, size).display
      sleep delay
      lins.map(&:next_gin).flatten
    end
  end
end
