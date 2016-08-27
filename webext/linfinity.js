function randomIndex(length) {
    return Math.floor(Math.random() * length);
}

function randomHexColor() {
    return '#' + ['red', 'blue', 'green'].map(primaryColor => {
        return randomIndex(256).toString(16);
    }).join('');
}

class Lin {
    constructor(properties) {
        Object.assign(this, properties);
    }

    mutate() {
        let {
            linChars
        } = this.settings;
        Object.assign(this, {
            char: linChars.charAt(randomIndex(linChars.length)),
            color: randomHexColor(),
            speed: 1
        });
    }

    move() {
        this.position = this.position + this.direction * this.speed;
    }

    bounce() {
        this.direction = -this.direction;
    }

    shouldSplit() {
        return Math.random() < this.settings.splitChance;
    }

    shouldMerge() {
        return Math.random() < this.settings.mergeChance;
    }

    shouldMutate() {
        return Math.random() < this.settings.mutateChance;
    }

    shouldDie() {
        return Math.random() < this.settings.dieChance;
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
        } else if (this.shouldDie()) {
            return [];
        } else {
            return [this];
        }
    }
}

class Row {
    constructor(lins, settings) {
        this.lins = lins;
        this.settings = settings;
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
        let last_index = this.settings.width - 1;
        if (pos >= last_index) {
            lin.bounce();
            return lin.position = last_index;
        } else if (pos < 1) {
            lin.bounce();
            return lin.position = 0;
        } else {
            return pos;
        }
    }

    display() {
        let colorData = [];
        let chars = '';
        for (let i = 0; i < this.settings.width; ++i) {
            let color, char;
            if (this.lin_positions.has(i)) {
                let lins = this.lin_positions.get(i);
                if (lins.length > 1) {
                    char = this.settings.collideChar.charAt(0);
                    color = 'black';
                } else if (lins.length == 1) {
                    let lin = lins[0];
                    char = lin.char;
                    color = lin.color;
                }
            } else {
                char = this.settings.blankChar;
                color = 'black';
            }
            chars += char;
            colorData.push({
                start: i,
                length: 1,
                color: color
            });
        }
        return {chars, colorData};
    }

    collideLins() {
        let collidedLins = [];
        for (let [pos, lins] of this.lin_positions) {
            if (lins.length < 2) {
                collidedLins = collidedLins.concat(lins);
            } else {
                let collidedAtPos = lins.map(lin => lin.bouncedOrMerged())
                    .filter(lin => lin != null);
                collidedLins = collidedLins.concat(collidedAtPos);
            }
        }
        return collidedLins;
    }
}

class Linfinity {
    constructor(settings, stoppedCallback) {
        this.settings = Object.assign({}, settings);
        this.resetLins(this.settings.initialNumLins);
        this.stoppedCallback = stoppedCallback;
    }

    get isBeyond() {
        return this.lins.length < 1;
    }

    resetLins(numLins) {
        let lins = this.lins = [];
        for (let i = 0; i < numLins; ++i) {
            let lin = new Lin({
                position: randomIndex(this.settings.width),
                direction: (Math.random() < 0.5 ? 1 : -1),
                settings: this.settings
            });
            lin.mutate();
            lins.push(lin);
        }
    }

    run(displayCallback) {
        if (this.isBeyond) {
            this.displayBeyond(displayCallback);
        } else {
            let row = new Row(this.lins, this.settings);
            displayCallback(row.display(), this.settings);
            this.lins = [].concat.apply([],
                row.collideLins().map(lin => lin.nextGin()));
            this.timeout = setTimeout(
                () => this.run(displayCallback),
                this.settings.delay
            );
        }
    }

    displayBeyond(displayCallback) {
        let c = 0;
        let displayRowAndQueueNext = () => {
            if (c >= this.settings.beyondMessage.length) {
                this.stop();
            } else {
                let char = this.settings.beyondMessage.charAt(c);
                let string = '';
                for (let i = 0; i < this.settings.width; ++i) {
                    string += char;
                }
                displayCallback({
                    chars: string,
                    colorData: [{
                        start: 0,
                        length: string.length,
                        color: 'black'
                    }]
                }, this.settings);                ++c;
                this.timeout = setTimeout(
                    displayRowAndQueueNext,
                    this.settings.delay * 3
                );
            }
        };
        displayRowAndQueueNext();
    }

    stop() {
        clearTimeout(this.timeout);
        this.timeout = null;
        this.stoppedCallback(this);
    }
}
