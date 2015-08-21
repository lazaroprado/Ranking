var menu = {
    init: function() {
        var url = window.location.pathname.replace('/','');
        switch(url) {
            case 'index':
                $('.menu-index').addClass('active');
                break;
            case 'rules':
                $('.menu-rules').addClass('active');
                break;
            case 'player':
                $('.menu-player').addClass('active');
                break;
            case 'game':
                $('.menu-game').addClass('active');
                break;
            case 'ranking':
                $('.menu-ranking').addClass('active');
                break;

        }
    }
};

var games = {
    saveGame: function() {
        var principal = $('.principal_players'),
            principal_score = $('.principal_score'),
            visitor = $('.visitor_players'),
            visitor_score = $('.visitor_score'),
            isOficial = $('#oficial_game').is(':checked');

        if(principal.val() == 'default' || visitor.val() == 'default'){
            messages.error("Você precisa escolher 2 jogadores para cadastrar um jogo.");
            return;
        } else if(principal.val() == visitor.val()){
            messages.error("Você não pode jogar contra você mesmo.");
            return;
        }


        var json = JSON.stringify({
            'principal': {
                'player_id': principal.val(),
                'score': parseInt(principal_score.val()),
                'victory': false
            },
            'visitor': {
                'player_id': visitor.val(),
                'score': parseInt(visitor_score.val()),
                'victory': false
            },
            'oficial': isOficial,
            'processed': false,
            'victoryThree': false
        });

        $.ajax({
            type: 'POST',
            url: '/api/game',
            data: json,
            contentType:"application/json; charset=utf-8",
            dataType:"json",
            success: function(response) {
                if(response) {
                    $('.principal_players').val('default');
                    $('.principal_score').val(0);
                    $('.visitor_players').val('default');
                    $('.visitor_score').val(0);

                    messages.success('Jogo cadastrado com sucesso!');
                }
                else messages.error('Ocorreu algum problema com esse cadastro');
            },
            error: function() {
                messages.error('Ocorreu algum problema com esse cadastro');
            }
        });

    },

    initPlayerData: function() {
        $('.player_id').each(function (i, item) {
            $(item).unbind('click').click(function() { games.showPlayerGames($(item).attr('id'), $(item).text()); });
        });

    },

    showPlayerGames: function(player_id, name) {
        var games_table = $('#games_table'),
            games_table_div = $('#games_table_div');
        games_table.children().remove();
        games_table_div.show();
        $('.games_owner').text(name);
        $.ajax({
            type: 'GET',
            url: '/api/games/' + player_id,
            contentType:"application/json; charset=utf-8",
            success: function(games) {
                //TODO criar overlay ajax
                var old_date = "";
                $(games).each(function(i, game) {
                    var player = [name, game.principal.player_id == player_id ? game.principal.score : game.visitor.score],
                        other_player_id = game.principal.player_id == player_id ? game.visitor.player_id : game.principal.player_id,
                        other_player = [$('#'+other_player_id).text(), game.principal.player_id == other_player_id ? game.principal.score : game.visitor.score],
                        date = game.registration.toString();
                    if(date != old_date) {
                        old_date = date;
                        games_table.append(
                            '<tr><td colspan="5" style="width: 100%;">'+old_date+'</td></tr>'
                        );
                    }
                    games_table.append(
                        '<tr>' +
                            '<td style="width: 30%;">'+player[0]+'</td>' +
                            '<td style="width: 10%;">'+player[1]+'</td>' +
                            '<td style="width: 5%;"> x </td>' +
                            '<td style="width: 10%;">'+other_player[1]+'</td>' +
                            '<td style="width: 30%;">'+other_player[0]+'</td>' +
                        '</tr>'
                    );
                });
                window.location = "#games";
            },
            error: function() {
                messages.error('Ocorreu algum problema com essa busca');
            }
        });
    }

};

var players = {
    savePlayer: function() {
        var player = $('.player_name'),
            team = $('.player_team');

        if(player.val() == ''){
            messages.error("Escreva um nome para o jogador.");
            return;
        } else if(team.val() == 'default'){
            messages.error("Você precisa escolher um time.");
            return;
        }

        var json = JSON.stringify({
            'name': player.val(),
            'team': team.val(),
            'total_score': 0,
            'goals_balance': 0,
            'victories': 0,
            'draws': 0,
            'defeats': 0,
            'historical_percentage': 0
        });

        $.ajax({
            type: 'POST',
            url: '/api/player',
            data: json,
            contentType:"application/json; charset=utf-8",
            dataType:"json",
            success: function(response) {
                if(response) {
                    $('.player_team').val('default');
                    $('.player_name').val("");

                    messages.success('Jogador cadastrado com sucesso!');
                }
                else messages.error('Ocorreu algum problema com esse cadastro');
            },
            error: function() {
                messages.error('Ocorreu algum problema com esse cadastro');
            }
        });
    }
};

var messages = {
    modal: function(msg) {
        bootbox.alert(msg);
    },
    error: function(msg) {
        var msg_error = $('.msg-error');
        msg_error.text(msg);
        msg_error.show();
        setTimeout(function() {
            msg_error.hide(500);
        }, 3000);
    },
    success: function(msg) {
        var msg_success = $('.msg-success');
        msg_success.text(msg);
        msg_success.show();
        setTimeout(function() {
            msg_success.hide(500);
        }, 3000);
    }
};

$(document).ready(function() {
    menu.init();
});