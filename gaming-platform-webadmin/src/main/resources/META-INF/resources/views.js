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
        'click .pos': 'posClick'
    },
    render: function() {
        let name = this.model.get("name");
        let status = this.model.get("status");
        let opponent = this.model.get("opponent");
        let me = this.model.get("isPlayer1") ? 1 : 2;
        let isMyTurn = this.model.get("isMyTurn");

        let button = this.model.get("ws") != null ?
            `<button type="button" class="btn btn-secondary disconnect">Disconnect</button>`
            :
            `<button type="button" class="btn btn-secondary connect">Connect</button>`;
        this.$el.html(`
            <div class="card h-100">
                <div class="card-body" id="${name}-bg">
                    <h5 class="card-title">${name} ${button}</h5>
                    <p class="card-text">Status: ${status}</p>
                    <p class="card-text">Opponent: ${opponent != null ? opponent : ""}</p>
                    <div class="container" style="background-color: ${status == PLAYING && isMyTurn ? '#ffffff' : '#bcbcbc'}">
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
        this.model.initConnection();
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
    }
});

const TicTacToeView = PlayerView.extend({

});