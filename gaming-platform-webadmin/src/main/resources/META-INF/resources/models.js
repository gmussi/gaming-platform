const INITIALIZING = "Initializing";
const CHECKING_ACCOUNT = "Checking account";
const REGISTERING = "Registering";
const AUTHENTICATING = "Authenticating";
const GETTING_TICKET= "Getting ticket";
const CONNECTING = "Connecting";
const MATCHMAKING = "Matchmaking";
const PLAYING = "Playing";
const VICTORY = "Victorious";
const DEFEAT = "Crushed";
const DRAW = "Draw";
const FAILURE = "Failure";

const Player = Backbone.Model.extend({
    "defaults": {
        "name": null,
        "status": INITIALIZING,
        "opponent": null,
        "isMyTurn": false,
        "isPlayer1": false,
        "matchId": null
    },

    automate() {
        this.set("status", CHECKING_ACCOUNT);

        // check if account exists
        let name = this.get("name");
        let that = this;
        jQuery.get(`http://localhost:8080/auth/available/${name}`)
            .done((exists) => {
                console.log(exists, exists == "true")
                if (exists == "true") {
                    that.login();
                } else {
                    that.register();
                }
            })
            .fail((event) => {
                that.set("status", FAILURE);
                console.log(that, event);
            });
    },
    login() {
        let that = this;
        this.set("status", AUTHENTICATING);
        jQuery.post("http://localhost:8080/auth/login", {
            "username": this.get("name"),
            "password": this.get("name")
        }).done((token) => {
            that.set("token", token);
            that.getTicket();
        }).fail((event) => {
            that.set("status", FAILURE);
            console.log(that, event);
        });
    },
    register() {
        let that = this;
        this.set("status", REGISTERING);
        jQuery.post(`http://localhost:8080/auth/register`, {
            "username": this.get("name"),
            "password": this.get("name")
        }).done((token) => {
            that.set("token", token);
            that.getTicket();
        }).fail((event) => {
            that.set("status", FAILURE);
            console.log(that, event);
        });
    },
    getTicket() {
        let that = this;
        this.set("status", GETTING_TICKET);
        jQuery.ajax({
            url: "http://localhost:8080/ticket",
            headers: {
                "Authorization": "Bearer " + this.get("token")
            }
        }).done((ticket) => that.initConnection(ticket));
    },
    initConnection(ticket) {
        this.set("status", CONNECTING);
        let name = this.get("name");

        let ws = new WebSocket(`ws://localhost:8080/player/${name}/${ticket}`);
        ws.onopen = (event) => this.onConnected(event);
        ws.onclose = (event) => this.onDisconnected(event);
        ws.onerror = (event) => this.onDisconnected(event);
        ws.onmessage = (event) => this.onPlayerEvent(event);
        this.set("ws", ws);
    },
    onConnected(event) {

    },
    onDisconnected(event) {

    },
    onPlayerEvent(event) {

    }
});

const TicTacToePlayer = Player.extend({

});

class Players extends Backbone.Collection {
    preinitialize() {
        this.on("add", this.addPlayer);
    }

    addPlayer(player, view) {
        $("#players").append(view.$el);
        view.render();
        player.automate();
    }
};


const players = new Players();


