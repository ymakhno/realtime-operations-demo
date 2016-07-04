class HandlerSupport {
    constructor() {
        this._handlers = []
    }

    addHandler(handler) {
        this._handlers.push(handler);
    }

    fire(e) {
        this._handlers.forEach(h => h(e));
    }
}

export default HandlerSupport;