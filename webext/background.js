function linfinityWindow() {
    let url = chrome.runtime.getURL("linfinity.html");
    chrome.windows.create({
        url,
        width: 530,
        height: 300,
        type: 'panel'
    });
}

chrome.browserAction.onClicked.addListener(linfinityWindow);
