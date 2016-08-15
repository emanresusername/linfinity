// https://gist.github.com/littlefolk/788723
// https://developer.mozilla.org/ja/New_in_JavaScript_1.7#Array_comprehensions
function range(begin, end) {
    let arr = [];
    for (let i = begin; i < end; ++i) {
        arr.push(i);
    }
    return arr;
};

// http://code.activestate.com/recipes/347689-ruby-arrayeach_cons-each_slice-overlapping-and-non/
function each_cons(l, n) {
    return [
        for (i of(range(0, l.length - n + 1))) l.slice(i, i + n)
    ];
};

function randomIndex(length) {
    return Math.floor(Math.random() * length);
}

function randomHexColor() {
    return '#' + ['red', 'blue', 'green'].map(primaryColor => {
        return randomIndex(256).toString(16);
    }).join('');
}

class Lin {
    static chars() {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    }

    constructor(properties) {
        Object.assign(this, properties);
    }

    mutate() {
        this.char = Lin.chars().charAt(randomIndex(Lin.chars().length));
        this.color = randomHexColor();
        this.speed = 1;
        this.splitChance = 0.01;
        this.mergeChance = 0.25;
        this.mutateChance = 0.25;
    }

    move() {
        this.position = this.position + this.direction * this.speed;
    }

    bounce() {
        this.direction = -this.direction;
    }

    shouldSplit() {
        return Math.random() < this.splitChance;
    }

    shouldMerge() {
        return Math.random() < this.mergeChance;
    }

    shouldMutate() {
        return Math.random() < this.mutateChance;
    }

    bouncedOrMerged() {
        if (this.shouldMerge()) {
            return null;
        } else {
            this.bounce();
            return this;
        }
    }

    // next generation of lin(s)
    nextGin() {
        if (this.shouldSplit()) {
            let bounced = new Lin(this);
            if (bounced.shouldMutate()) {
                bounced.mutate();
            }
            bounced.bounce();
            return [this, bounced];
        } else {
            return [this];
        }
    }
}

class Row {
    constructor(lins, size, blankChar = ' ') {
        this.size = size;
        this.last_index = size - 1;
        this.lins = lins;
        this.blankChar = blankChar;
        let lin_positions = this.lin_positions = new Map();
        lins.forEach((lin) => {
            let pos = this.move(lin);
            if (lin_positions.has(pos)) {
                lin_positions.get(pos).push(lin);
            } else {
                lin_positions.set(pos, [lin]);
            }
        });
    }

    move(lin) {
        lin.move();
        let pos = lin.position;
        if (pos >= this.last_index) {
            lin.bounce();
            return this.last_index;
        } else if (pos < 1) {
            lin.bounce();
            return 0;
        } else {
            return pos;
        }
    }

    display() {
        let rowHtml = '';
        for (let i = 0; i < this.size; ++i) {
            if (this.lin_positions.has(i)) {
                let lins = this.lin_positions.get(i);
                if (lins.length > 1) {
                    rowHtml += '#';
                } else if (lins.length == 1) {
                    let lin = lins[0];
                    rowHtml += `<font style="color: ${lin.color};">${lin.char}</font>`;
                }
            } else {
                rowHtml += this.blankChar;
            }
        }
        return rowHtml;
    }

    collideLins() {
        let collidedLins = [];
        for (let [pos, lins] of this.lin_positions) {
            if (lins.length < 2) {
                collidedLins = collidedLins.concat(lins);
            } else {
                for (let pair of each_cons(lins, 2)) {
                    let collidedPair = lins.map(lin => lin.bouncedOrMerged())
                        .filter(lin => lin != null);
                    collidedLins = collidedLins.concat(collidedPair);
                }
            }
        }
        return collidedLins;
    }
}

class Linfinity {
    constructor({
        delay= 1000,
        rowWidth= 15,
        initialLins= 2,
        blankChar = ' '
    }) {
        this.delay = delay;
        this.rowWidth = rowWidth;
        this.initialLins = initialLins;
        this.blankChar = blankChar;
    }

    run(displayCallback) {
        if (this.interval) {
            this.stop();
        }

        let rowWidth = this.rowWidth;
        let lins = [];
        for (let i = 0; i < this.initialLins; ++i) {
            let lin = new Lin({
                position: randomIndex(rowWidth),
                direction: (Math.random() < 0.5 ? 1 : -1)
            });
            lin.mutate();
            lins.push(lin);
        }
        this.interval = setInterval(() => {
            if (lins.length < 1) {
                this.stop();
            } else {
                let row = new Row(lins, rowWidth, this.blankChar);
                displayCallback(row.display());
                lins = [].concat.apply([],
                    row.collideLins().map(lin => lin.nextGin()));
            }
        }, this.delay);
    }

    stop() {
        clearInterval(this.interval);
    }

    domDisplayCallback(container) {
        return (innerHTML) => {
            let displayElement = document.createElement('p');
            displayElement.innerHTML = `<pre>${innerHTML}</pre>`;
            container.appendChild(displayElement);
        };
    }
}
