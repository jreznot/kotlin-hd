const {app, BrowserWindow} = require('electron');
const path = require('path');
const url = require('url');
const net = require('net');

let mainWindow;
let serverProcess;

let pipeStream;

let pipeRequestIdSeq = 0;
let pipeRequests = new Map();

global.pipe = {};
global.pipe.send = function(method, payload) {
    let requestId = pipeRequestIdSeq++;

    let message = {
        id: requestId.toString(),
        method: method,
        payload: payload
    };
    console.log("> Request sent: " + message.id);
    pipeStream.write(JSON.stringify(message) + '\n');

    let _received;

    var promise = new Promise(function(resolve, reject) {
        _received = resolve;
    });
    promise.received = _received;
    pipeRequests.set(message.id, promise);

    return promise
};

function createWindow() {
    let PIPE_PATH = '\\\\.\\pipe\\demo';

    const pipeServer = net.createServer(function(stream) {
        stream.on('data', function(c) {
            console.log('Pipe: ', c.toString().trim());

            let data = JSON.parse(c);
            if (data.id && data.payload) {
                let promise = pipeRequests.get(data.id);
                if (promise) {
                    console.log("< Response received: " + data.id);

                    promise.received(data.payload);
                }
            }
        });
        stream.on('end', function() {
            pipeServer.close();
        });

        pipeStream = stream;

        console.log('Pipe is connected');

        global.pipe.send('hello', {});
    }).listen(PIPE_PATH, function () {
        // Windows only
        serverProcess = require('child_process')
            .spawn('cmd.exe', ['/c', 'backend.bat'],
                {
                    cwd: app.getAppPath() + '/backend/bin'
                });

        if (!serverProcess) {
            console.error('Unable to start server from ' + app.getAppPath());
            app.quit();
            return;
        }

        serverProcess.stdout.on('data', function (data) {
            console.log('Server: ' + data.toString().trim());
        });

        console.log("Server PID: " + serverProcess.pid);

        startUp();
    });
}

function openWindow() {
    mainWindow = new BrowserWindow({
        title: 'Demo',
        width: 1280,
        height: 800
    });

    mainWindow.loadURL(url.format({
        pathname: path.join(__dirname, 'index.html'),
        protocol: 'file:',
        slashes: true
    }));

    mainWindow.on('closed', function () {
        mainWindow = null;
    });

    mainWindow.on('close', function (e) {
        if (serverProcess) {
            e.preventDefault();

            // kill Java executable
            const kill = require('tree-kill');
            kill(serverProcess.pid, 'SIGTERM', function () {
                console.log('Server process killed');

                serverProcess = null;

                mainWindow.close();
            });
        }
    });
}

function startUp() {
    openWindow();
}

app.on('ready', createWindow);

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit()
    }
});

app.on('activate', () => {
    if (mainWindow === null) {
        createWindow()
    }
});