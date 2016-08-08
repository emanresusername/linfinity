class Lin
  attr_reader :position, :direction, :speed, :split_chance,
              :merge_chance, :random, :char, :mutate_chance

  def initialize
    @position = 0
    @direction = 1
    mutate
  end

  def mutate
    @random = Random.new
    @char = random.rand(36).to_s(36)
    @speed = 1
    @split_chance = 0.01
    @merge_chance = 0.25
    @mutate_chance = 0.25
  end

  def move
    @position = position + direction * speed
  end

  def bounce
    @direction = -direction
  end

  def split?
    random.rand < split_chance
  end

  def merge?
    random.rand < merge_chance
  end

  def mutate?
    random.rand < mutate_chance
  end

  def bounced_or_merged
    if merge?
      nil
    else
      bounce
      self
    end
  end

  # next generation of lin(s)
  def next_gin
    if split?
      bounced = dup
      bounced.mutate if mutate?
      bounced.bounce
      [self, bounced]
    else
      [self]
    end
  end
end

class Row
  attr_reader :lins, :lin_positions, :size, :last_index
  def initialize(lins, size)
    @size = size
    @last_index = size - 1
    @lins = lins
    @lin_positions = lins.each_with_object(Hash.new { |h, k| h[k] = [] }) do |lin, hash|
      hash[move(lin)] << lin
    end
  end

  def move(lin)
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
    size.times.inject('') do |row, i|
      row + case (lins = lin_positions[i]).size
            when 0
              ' '
            when 1
              lins.first.char
            else
              '#'
      end
    end
  end

  def collide_lins
    lin_positions.flat_map do |_pos, lins|
      if lins.size < 2
        lins
      else
        lins.each_cons(2).flat_map do |pair|
          pair.map(&:bounced_or_merged).compact
        end
      end
    end
  end
end

class Linfinity
  attr_reader :delay, :size, :io

  def initialize(delay: 0.1, size: 157, output: nil)
    @io = output ? open(output, 'w') : STDOUT
    @delay = delay
    @size = size
  end

  def run
    loop.inject([Lin.new]) do |lins, _|
      break unless lins.any?
      row = Row.new(lins, size)
      io.puts row.display
      sleep delay if io == STDOUT
      row.collide_lins.map(&:next_gin).flatten
    end
    io.flush
  end
end
