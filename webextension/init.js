const DEFAULT_SETTINGS = {
    width: 40,
    height: 13,
    delay: 100,
    blankChar: '_',
    collideChar: '#',
    splitChance: 0.01,
    mergeChance: 0.35,
    mutateChance: 0.25,
    dieChance: 0.001,
    linChars: "0123456789",
    beyondMessage: "...AND BEYOND!!!",
    initialNumLins: 4
};

const LINFINITY = new Linfinity(DEFAULT_SETTINGS);

for (elem of document.querySelectorAll(".setting-input")) {
    let key = elem.id;
    elem.value = LINFINITY.settings[key];
    elem.addEventListener('change', function(e) {
        LINFINITY.settings[key] = this.value;
    });
}

document.getElementById('resetLins').addEventListener('click', function(e) {
    LINFINITY.resetLins(LINFINITY.settings.initialNumLins);
});

function displayCallback(innerHTML, settings) {
    let container = document.getElementById('display');
    let displayElement = document.createElement('p');
    displayElement.innerHTML = `<pre>${innerHTML}</pre>`;
    container.appendChild(displayElement);
    if (container.children.length > settings.height) {
        container.firstElementChild.remove();
    }
}

document.getElementById('startStop').addEventListener('click', function(e) {
    this.started = !this.started;
    if (this.started) {
        this.value = "Stop";
        LINFINITY.run(displayCallback);
    } else {
        this.value = "Start";
        LINFINITY.stop();
    }
});
