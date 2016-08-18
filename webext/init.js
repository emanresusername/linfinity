const DEFAULT_SETTINGS = {
    width: 40,
    height: 20,
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
    if (linfinity.isBeyond) {
        resetLins(linfinity);
    }
}

const LINFINITY = new Linfinity(DEFAULT_SETTINGS, linfinityStopped);

function linfinitySet(key, value) {
    LINFINITY.settings[key] = value;
}

for (let elem of document.querySelectorAll(".setting-input")) {
    let key = elem.id;
    elem.value = LINFINITY.settings[key];
    elem.addEventListener('change', function(e) {
        linfinitySet(key, this.value);
    });
}

document.getElementById('resetLins').addEventListener('click', function(e) {
    resetLins(LINFINITY);
});

const SCROLL_PANE = document.getElementById('displayScrollPane');

function displayCallback(innerHTML, settings) {
    let container = document.getElementById('display');
    let displayElement = document.createElement('pre');
    displayElement.style.margin = 0;
    displayElement.style['font-size'] = 'inherit';
    displayElement.innerHTML = `${innerHTML}`;
    container.appendChild(displayElement);
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

const SETTINGS_HELP = document.getElementById('settingsHelp');
for(let elem of document.querySelectorAll(".settings-help")) {
    // Outer elem will end up as the last element in selection and always get used in the event listener
    let innerElem = elem;
    elem.parentElement.addEventListener('mouseenter', function(e) {
        SETTINGS_HELP.innerHTML = innerElem.innerHTML;
    });
};

const SETTINGS_PANEL = document.getElementById('settings');
const HEIGHT_INPUT = document.getElementById("height");
const WIDTH_INPUT = document.getElementById("width");

function linfinitySetWidth(width) {
    linfinitySet('width', width);
}
function linfinitySetHeight(height) {
    linfinitySet('height', height);
}

function styleScrollPane(key, value) {
    SCROLL_PANE.style[key] = value;
}
function scrollPaneComputedStyle() {
    return window.getComputedStyle(SCROLL_PANE, null);
}

function syncSettingsDimensionsToDisplay(event) {
    let height = +HEIGHT_INPUT.value;
    styleScrollPane('height', `${height}em`);
    linfinitySetHeight(height);
    let width = +WIDTH_INPUT.value;
    linfinitySetWidth(width);
}

[HEIGHT_INPUT, WIDTH_INPUT].forEach(input => {
    let key = input.id;
    input.value = LINFINITY.settings[key];
    input.addEventListener('change', syncSettingsDimensionsToDisplay);
});

function toggleScrollable(isScrollable) {
    styleScrollPane('overflow-y', isScrollable ? 'scroll' : 'hidden');
}

syncSettingsDimensionsToDisplay();
toggleScrollable(false);
