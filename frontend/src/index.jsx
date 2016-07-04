import React from 'react'
import ReactDOM from 'react-dom'
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import StreamHandler from './websocket/StreamHandler.es6';
import HandlerSupport from './websocket/HandlerSupport.es6';
import OperationTable from './components/OperationTable.jsx';
import ConfigurationDialog from './components/ConfigurationDialog.jsx';
import $ from 'jQuery';

import getMuiTheme from "material-ui/styles/getMuiTheme.js";

let l = window.location;
const appPath = l.pathname.substr(0, l.pathname.lastIndexOf('/'));
const wsPath = ((l.protocol === "https:") ? "wss://" : "ws://") + l.hostname + (((l.port != 80) && (l.port != 443)) ? ":" + l.port : "");

$.getJSON(appPath + "/fields", (fieldsPayload) => {
    new StreamHandler(wsPath + appPath + "/websocket",
        (handler) => {
            let dialogOpenHandler = new HandlerSupport();
            let tableStructureHandler = new HandlerSupport();

            tableStructureHandler.addHandler(e => handler.restartWithNewFields(e.wsFields));
            ReactDOM.render(
                <MuiThemeProvider muiTheme={getMuiTheme()}>
                    <div>
                        <OperationTable streamHandler={handler}
                                        onConfigureClick={dialogOpenHandler.fire.bind(dialogOpenHandler)}
                                        tableStructureHandler={tableStructureHandler} />
                        <ConfigurationDialog fieldsByOperations={fieldsPayload.fields}
                                             defaultFields={fieldsPayload.defaultFields}
                                             dialogOpenHandler={dialogOpenHandler}
                                             tableStructureHandler={tableStructureHandler} />
                    </div>
                </MuiThemeProvider>,
                document.getElementById('app')
            );
        }
    )
});
