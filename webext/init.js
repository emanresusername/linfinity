const DEFAULT_GAME_SETTINGS = {
    width: 40,
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

const DEFAULT_DISPLAY_SETTINGS = {
    height: 20,
    manualScroll: false,
    showAdvanced: false,
    showHelp: true
};
let displaySettings = Object.assign({}, DEFAULT_DISPLAY_SETTINGS);

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

const LINFINITY = new Linfinity(DEFAULT_GAME_SETTINGS, linfinityStopped);

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
    if (!displaySettings.manualScroll) {
        SCROLL_PANE.scrollTop = SCROLL_PANE.scrollHeight;
    }
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
for (let elem of document.querySelectorAll(".settings-help")) {
    // Outer elem will end up as the last element in selection and always get used in the event listener
    let innerElem = elem;
    elem.parentElement.addEventListener('mouseenter', function(e) {
        SETTINGS_HELP.innerHTML = innerElem.innerHTML;
    });
};

const SETTINGS_PANEL = document.getElementById('settings');
const HEIGHT_INPUT = document.getElementById("height");
const WIDTH_INPUT = document.getElementById("width");

function styleScrollPane(key, value) {
    SCROLL_PANE.style[key] = value;
}

function scrollPaneComputedStyle() {
    return window.getComputedStyle(SCROLL_PANE, null);
}

function syncSettingsDimensionsToDisplay(event) {
    let height = +HEIGHT_INPUT.value;
    styleScrollPane('height', `${height}em`);
    let width = +WIDTH_INPUT.value;
    linfinitySet('width', width);
}

WIDTH_INPUT.value = LINFINITY.settings[WIDTH_INPUT.id];
HEIGHT_INPUT.value = displaySettings[HEIGHT_INPUT.id];

[HEIGHT_INPUT, WIDTH_INPUT].forEach(input => {
    input.addEventListener('change', syncSettingsDimensionsToDisplay);
});

function toggleManualScroll(isScrollable) {
    styleScrollPane('overflow-y', isScrollable ? 'scroll' : 'hidden');
}

for(let elem of document.querySelectorAll('.setting-toggle')) {
    elem.checked = displaySettings[elem.id];
}

const SCROLL_TOGGLE = document.getElementById('manualScroll');
SCROLL_TOGGLE.addEventListener('change', function(e) {
    let toggled = displaySettings.manualScroll = SCROLL_TOGGLE.checked;
    toggleManualScroll(toggled);
});

function toggleAdvancedSettings(show) {
    for (let elem of document.querySelectorAll('.advanced-setting')) {
        elem.style.display = show ? null : 'none';
    }
}

const ADVANCED_TOGGLE = document.getElementById('showAdvanced');
ADVANCED_TOGGLE.addEventListener('change', function(e) {
    let toggled = displaySettings.showAdvanced = ADVANCED_TOGGLE.checked;
    toggleAdvancedSettings(toggled);
});

const HELP_TOGGLE = document.getElementById('showHelp');
function toggleHelp(show) {
    SETTINGS_HELP.style.display = show ? null : 'none';
}

HELP_TOGGLE.addEventListener('change', function(e) {
    let toggled = displaySettings.showHelp = HELP_TOGGLE.checked;
    toggleHelp(toggled);
});

syncSettingsDimensionsToDisplay();
toggleManualScroll(displaySettings.manualScroll);
toggleAdvancedSettings(displaySettings.showAdvanced);
