const events = new EventSource(`${SERVER.STATISTICS}/statistics/stream`);

events.onmessage = (event) => {
    let input = JSON.parse(event.data);
    console.log(input);
    switch (input.type) {
        case "PLAYER_COUNT":
            $("#player-count").text(input.count);
            break;
    }
}