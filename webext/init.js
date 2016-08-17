const DEFAULT_SETTINGS = {
    width: 40,
    height: 9999999999999,
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

const START_STOP_BUTTON = document.getElementById('startStop');

function resetLins(linfinity) {
    linfinity.resetLins(linfinity.settings.initialNumLins);
}

function linfinityStopped(linfinity) {
    START_STOP_BUTTON.started = false;
    START_STOP_BUTTON.value = "Start";
    if(linfinity.isBeyond) {
        resetLins(linfinity);
    }
}

const LINFINITY = new Linfinity(DEFAULT_SETTINGS, linfinityStopped);

for (elem of document.querySelectorAll(".setting-input")) {
    let key = elem.id;
    elem.value = LINFINITY.settings[key];
    elem.addEventListener('change', function(e) {
        LINFINITY.settings[key] = this.value;
    });
}

document.getElementById('resetLins').addEventListener('click', function(e) {
    resetLins(LINFINITY);
});

const SCROLL_PANE = document.getElementById('displayScrollPane');
function displayCallback(innerHTML, settings) {
    let container = document.getElementById('display');
    let displayElement = document.createElement('p');
    displayElement.innerHTML = `<pre>${innerHTML}</pre>`;
    container.appendChild(displayElement);
    while (container.children.length > settings.height) {
        container.firstElementChild.remove();
    }
    SCROLL_PANE.scrollTop = SCROLL_PANE.scrollHeight;
}

START_STOP_BUTTON.addEventListener('click', function(e) {
    this.started = !this.started;
    if (this.started) {
        LINFINITY.run(displayCallback);
        this.value = "Stop";
    } else {
        LINFINITY.stop();
        linfinityStopped(LINFINITY);
    }
});

const SETTINGS_PANEL = document.getElementById('settings');
function resizeDisplayScrollPane() {
    let settingsHeight = SETTINGS_PANEL.clientHeight;
    SCROLL_PANE.style.height = settingsHeight;
}
