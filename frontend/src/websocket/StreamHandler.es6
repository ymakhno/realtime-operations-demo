import HandlerSupport from "./HandlerSupport.es6"

class StreamHandler extends HandlerSupport {
    
    constructor(url, openHandler) {
        super();
        this._openHandler = openHandler;
        this._currentId = Math.floor(Date.now());
        this._enabled = false;

        this._offset = 0;
        this._limit = 20;
        this._requestedFields = {"DEPOSIT" : [], "TRANSFER" : [], "WITHDRAWAL" : []};

        this._socket = new WebSocket(url);
        this._socket.onopen = this._onOpen.bind(this);
        this._socket.onmessage = this._onMessage.bind(this);
    }

    restartFromOffset(offset) {
        this.startStream(offset, this._limit, this._requestedFields);
    }

    restartWithNewFields(requestedFields) {
        this.startStream(this._offset, this._limit, requestedFields);
    }

    startStream(offset, limit, requestedFields) {
        if (this._enabled) {
            throw 'Stream is already enabled';
        }
        this._offset = offset;
        this._limit = limit;
        this._requestedFields = requestedFields;
        let command = {
            streamId : this._currentId,
            type : 'startStream',
            offset : offset,
            limit : limit,
            requestedFields : requestedFields
        };
        this._data = [];
        this._socket.send(JSON.stringify(command))
    }

    stopStream() {
        if (this._enabled) {
            let command = {
                streamId : this._currentId,
                type : 'stopStream'
            };
            this._socket.send(JSON.stringify(command))
        }
    }

    get offset() {
        return this._offset;
    }

    get limit() {
        return this._limit;
    }

    _onOpen(event) {
        this._openHandler(this);
    }

    _onMessage(event) {
        let message = JSON.parse(event.data);
        let newRows = message["newRows"];
        let total = message["queueState"]["total"];
        let lastValidRowId = parseInt(message["queueState"]["lastRowInQueue"]);

        let limit = this._limit;
        var slice = this._data;
        if (this._data.length + newRows.length > limit) {
            slice = this._data.slice(0, limit - newRows.length);
        }

        this._data = newRows.concat(slice);

        let dataLength = this._data.length;
        if (dataLength > 0 &&
            parseInt(this._data[dataLength - 1]["rowId"]) < lastValidRowId) {
            this._data = this._data
                .filter((i) => parseInt(i["rowId"]) >= lastValidRowId)
        }
        this.fire({data: this._data, total: total});
    }
}

export default StreamHandler;