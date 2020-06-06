const PlayerView = Backbone.View.extend({
    tagName: 'div',
    className: 'col-md-3 col-xl-3',
    initialize: function(args) {
        this.listenTo(this.model, "change", this.render);
        this.listenTo(this.model, "change:status", this.flash);
    },
    events: {
        'click .connect': 'connect',
        'click .disconnect': 'disconnect',
        'click .pos': 'posClick',
        'click .remove': 'remove'
    },
    render: function() {
        let name = this.model.get("name");
        let status = this.model.get("status");
        let opponent = this.model.get("opponent");
        let me = this.model.get("started") ? 1 : 2;
        let pos = this.model.get("pos").map(num => num == 0 ? "_" : num == me ? 'X' : 'O');
        let isMyTurn = this.model.get("isMyTurn");

        let button = "";
        if (this.model.get("automated")) {
            button = `<button type="button" class="btn btn-secondary remove">Remove</button>`;
        } else if (this.model.get("ws") != null) {
            button = `<button type="button" class="btn btn-secondary disconnect">Disconnect</button>`;
        } else {
            button = `<button type="button" class="btn btn-secondary connect">Connect</button>`;
        }
        this.$el.html(`
            <div class="card h-100">
                <div class="card-body" id="${name}-bg">
                    <h5 class="card-title">${name} ${button}</h5>
                    <p class="card-text">Status: ${status}</p>
                    <p class="card-text">Opponent: ${opponent != null ? opponent : ""}</p>
                    <div class="container" style="background-color: ${status == PLAYING && isMyTurn ? '#ffffff' : '#bcbcbc'}">
                        <div class="row">
                            <div data-pos="0" class="col border-bottom border-right border-dark text-center pos">${pos[0]}</div>
                            <div data-pos="1" class="col border-left border-right border-bottom border-dark text-center pos">${pos[1]}</div>
                            <div data-pos="2" class="col border-left border-bottom border-dark text-center pos">${pos[2]}</div>
                            <div class="w-100"></div>
                            <div data-pos="3" class="col border-top border-right border-bottom border-dark text-center pos">${pos[3]}</div>
                            <div data-pos="4" class="col border border-dark text-center pos">${pos[4]}</div>
                            <div data-pos="5" class="col border-top border-bottom border-left border-dark text-center pos">${pos[5]}</div>
                            <div class="w-100"></div>
                            <div data-pos="6" class="col border-top border-right border-dark text-center pos">${pos[6]}</div>
                            <div data-pos="7" class="col border-right border-top border-left border-dark text-center pos">${pos[7]}</div>
                            <div data-pos="8" class="col border-top border-left border-dark text-center pos">${pos[8]}</div>
                        </div>
                    </div>
                </div>
            </div>
        `);
        return this;
    },
    disconnect() {
        this.model.disconnect();
    },
    connect() {
        this.model.getTicket();
    },
    remove() {
        players.removePlayer(this.model, this);
    },
    flash() {
        let name = this.model.get("name");
        let status = this.model.get("status");
        if (status == VICTORY) {
            setTimeout(() => {
                $(`#${name}-bg`).addClass("flash-green");
            }, 1);
        } else if (status == DEFEAT) {
            setTimeout(() => {
                $(`#${name}-bg`).addClass("flash-red");
            }, 1);
        } else if (status == DRAW) {
            setTimeout(() => {
                $(`#${name}-bg`).addClass("flash-yellow");
            }, 1);
        }
    },
    posClick(event) {
        // get the array
        let pos = this.model.get("pos");

        // get the value from the data-pos attribute
        let newpos = event.target.dataset.pos;

        // check if the spot can be clicked
        if (pos[newpos] == 0 && this.model.get("isMyTurn")) {
            this.model.play(newpos);
        }
    }
});

const TicTacToeView = PlayerView.extend({

});