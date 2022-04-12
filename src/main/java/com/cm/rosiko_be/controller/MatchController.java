package com.cm.rosiko_be.controller;

import com.cm.rosiko_be.data.*;
import com.cm.rosiko_be.data.missions.*;
import com.cm.rosiko_be.enums.CardType;
import com.cm.rosiko_be.enums.Color;
import com.cm.rosiko_be.enums.MatchState;
import com.cm.rosiko_be.services.TimerService;
import com.cm.rosiko_be.services.WSServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.util.*;
import static com.cm.rosiko_be.enums.CardType.*;
import static com.cm.rosiko_be.enums.Stage.*;

@Controller
public class MatchController {

    public static final int MAX_PLAYERS = 6;                //Massimo numero di giocatori
    public static final int MIN_PLAYERS = 1;                //Minimo numero di giocatori
    public static final int INITIAL_ARMIES_2_PLAYERS = 25;  // !!! DELETE !!!
    public static final int INITIAL_ARMIES_3_PLAYERS = 35;  //Armate che ha a disposizione il giocatore a inizio partita nel caso in cui ci fossero 3 giocatori
    public static final int INITIAL_ARMIES_4_PLAYERS = 30;  //Armate che ha a disposizione il giocatore a inizio partita nel caso in cui ci fossero 4 giocatori
    public static final int INITIAL_ARMIES_5_PLAYERS = 25;  //Armate che ha a disposizione il giocatore a inizio partita nel caso in cui ci fossero 5 giocatori
    public static final int INITIAL_ARMIES_6_PLAYERS = 20;  //Armate che ha a disposizione il giocatore a inizio partita nel caso in cui ci fossero 6 giocatori
    public static final int INITIAL_ARMIES_TO_PLACE = 3;    //Numero di armate che si possono piazzare nella fase INITIAL_PLACEMENT
    public static final int TERRITORIES_FOR_AN_ARMY = 3;    //Numero di territori in possesso per avere una armata.
    public static final int MINIMUM_ATTACKING_ARMIES = 2;   //Numero minimo di armate che un territorio deve avere per potere attaccare.
    public static final int MAX_ATTACKING_DICES = 3;        //Numero massimo di dadi concessi all'attaccante.
    public static final int MIN_ATTACKING_DICES = 1;        //Numero minimo di dadi concessi all'attaccante.
    public static final int MAX_DICE_VALUE = 6;             //Massimo valore che può assumere un dado.
    public static final int MIN_DICE_VALUE = 1;             //Minimo valore che può assumere un dado.
    public static final int MIN_ARMIES_FOR_TERRITORY = 1;   //Numero minimo di armate possibili per un territorio.
    public static final int TRACTOR_SET_BONUS = 4;          //Bonus di armate nel caso di un tris di trattori
    public static final int FARMER_SET_BONUS = 6;           //Bonus di armate nel caso di un tris di contadini
    public static final int COW_SET_BONUS = 8;              //Bonus di armate nel caso di un tris di mucche
    public static final int DIFFERENT_CARDS_SET_BONUS = 10; //Bonus di armate nel caso di un tris di mucca, contadino e trattore
    public static final int JOLLY_SET_BONUS = 12;           //Bonus di armate nel caso di un tris di un jolly + 2 carte uguali
    public static final int SET_CARDS_NUMBER = 3;           //Numero di carte per fare un tris
    public static final int CARD_TERRITORY_BONUS = 2;       //Numero armate bonus se si possiede il territorio della carta giocata
    public static final int MINIMUM_AVAIABLE_ARMIES = 1;    //Numero armate bonus se si possiede il territorio della carta giocata
    public static final int MAX_INACTIVITY_PERIOD = 10;     //Minuti di inattività consentiti per ogni giocatore

    private Match match;

    @Autowired
    public WSServices wsServices;

    @Autowired
    public TimerService timerService;


    public MatchController(){}


    //Inizio della partita
    public void startMatch(){
        try {
            //Controlla che il numero di giocatori sia compreso tra MIN_PLAYERS e MAX_PLAYERS
            if(match.getPlayers().size() < MIN_PLAYERS || match.getPlayers().size() > MAX_PLAYERS){
                match.setState(MatchState.ERROR);
                throw new Exception("Number of players is " + match.getPlayers().size() + "\nMax number of players: " + MAX_PLAYERS + "\nMin number of players: " + MIN_PLAYERS);
            }

            //Set mach state
            match.setState(MatchState.STARTED);

            //Preparazione iniziale
            setup();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Passa al prossimo turno
    public void nextStage(){
        List<Player> players = match.getPlayers();
        Player turnPlayer = match.getTurnPlayer();

        //Ferma il timer di inattività
        timerService.stopTimer();

        //Controlla se c'è un vincitore
        checkWinner();

        //Primo turno
        if(turnPlayer == null) match.setTurnPlayer(players.get(0));
        else{
            //Turni successivi
            handleStage();
        }

        //Setta i territori cliccabili in base alla fase di gioco
        setSelectable();

        //Caso di vittoria
        if(match.getStage().equals(GAME_OVER)){
            System.out.println("GAME OVER");
            wsServices.notifyMatch(match.getId());
        }
        else{
            //Fai partire il timer di inattività
            timerService.startTimer(this);
        }
    }

    //Piazza una armata su un territorio, poi se le condizioni sono soddisfatte passa il turno
    public void placeArmy(String territoryId){
        Territory territory = match.getMap().getTerritory(territoryId);
        Player owner = territory.getOwner();

        //Se è il turno del giocatore e ha abbastanza armate disponibili allora piazza l'armata
        if(match.getTurnPlayer().equals(owner) && owner.getAvailableArmies()>=1){
            placeArmies(territory, 1);
        }

        nextStage();
    }

    //Seleziona un proprio territorio dal quale sia possibile attaccare
    public void selectAttacker(String territoryId){
        //Conferma i movimenti se ce ne sono
        confirmMovement();

        Territory territory = match.getMap().getTerritory(territoryId);
        Player owner = territory.getOwner();

        //Se è il turno del giocatore e il territorio selezionato ha almeno due armate
        //Allora setta il territorio come attaccante
        if(match.getTurnPlayer().equals(owner) && territory.getArmies() >= MINIMUM_ATTACKING_ARMIES){
            match.setAttacker(territory);
            match.setDefender(null);
            match.setTerritoryTo(null);
            match.setTerritoryFrom(null);
        }

        nextStage();
    }

    //Seleziona un proprio territorio dal quale sia possibile attaccare
    public void selectDefender(String territoryId){
        //Conferma i movimenti se ce ne sono
        confirmMovement();

        Territory territory = match.getMap().getTerritory(territoryId);

        //Se il territorio è confinante con l'attaccante setta il territorio come difensore
        if(territory.isBordering(match.getAttacker())){
            match.setDefender(territory);
            match.setTerritoryTo(null);
            match.setTerritoryFrom(null);
        }
        nextStage();
    }

    //Deseleziona un territorio che sia quello attaccante o quello difensivo
    public void deselectTerritory(String territoryId){

        //Conferma i movimenti se ce ne sono
        confirmMovement();

        //Se il territorio è quello attaccante lo deseleziona e deseleziona pure quello difensivo
        if(match.getAttacker()!= null && match.getAttacker().getId().equals(territoryId)){
            match.setAttacker(null);
            match.setDefender(null);
        }

        //Se il territorio è quello difensivo lo deseleziona
        if(match.getDefender()!= null && match.getDefender().getId().equals(territoryId)){
            match.setDefender(null);
        }

        //Se il territorio è quello dal quale spostare le armate deseleziona pure quello di destinazione
        if(match.getTerritoryFrom()!= null && match.getTerritoryFrom().getId().equals(territoryId)){
            match.setTerritoryFrom(null);
            match.setTerritoryTo(null);
        }

        //Se il territorio è quello al quale destinare le armate lo deseleziona
        if(match.getTerritoryTo()!= null && match.getTerritoryTo().getId().equals(territoryId)){
            match.setTerritoryTo(null);
        }

        nextStage();
    }

    //Deseleziona tutti i territori
    private void deselectTerritories(){

        //Conferma i movimenti se ce ne sono
        confirmMovement();

        match.setAttacker(null);
        match.setDefender(null);
        match.setTerritoryFrom(null);
        match.setTerritoryTo(null);

        nextStage();
    }

    //L'attaccker attacca il defender con i dadi specificati nell'attributo difende.
    public void attack(int numberOfAttackerDice){
        List<Integer> dicesAttackerList = getDicesAttacker(numberOfAttackerDice);
        List<Integer> dicesDefenderList = getDicesDefender();

        int armiesLostByAttacker = 0;
        int armiesLostByDefender = 0;

        //Lancio dei dadi
        diceRoll(dicesAttackerList);
        diceRoll(dicesDefenderList);

        //Calcolo delle armate perse dall'attaccante e dal difensore;
        for(int i=0; i<Integer.min(dicesAttackerList.size(), dicesDefenderList.size()); i++){
            if( dicesAttackerList.get(i) > dicesDefenderList.get(i) ) {armiesLostByDefender++;}
            else {armiesLostByAttacker++;}
        }

        //Salvataggio esito dei dadi nel match
        String[] diceAttackerResult = new String[MAX_ATTACKING_DICES];
        String[] diceDefenderResult = new String[MAX_ATTACKING_DICES];
        for(int i=0; i<MAX_ATTACKING_DICES; i++){
            diceAttackerResult[i] = "none";
            diceDefenderResult[i] = "none";
        }
        for(int i=0; i< dicesAttackerList.size(); i++){
            diceAttackerResult[i] = dicesAttackerList.get(i).toString();
        }
        for(int i=0; i< dicesDefenderList.size(); i++){
            diceDefenderResult[i] = dicesDefenderList.get(i).toString();
        }
        match.setDiceAttacker(diceAttackerResult);
        match.setDiceDefender(diceDefenderResult);

        //Decremento delle armate sconfitte
        match.getAttacker().removeArmies(armiesLostByAttacker);
        match.getDefender().removeArmies(armiesLostByDefender);

        //Caso di conquista
        conquest(dicesAttackerList.size());
    }

    //Sposta le armate da un territorio a un altro
    public void moveArmies(int armies){
        Territory territoryFrom = match.getTerritoryFrom();
        Territory territoryTo = match.getTerritoryTo();

        //Sposta solo se i territori sono confinanti e se il numero di armate è coerente
        if(territoryFrom.isBordering(territoryTo)
            && territoryFrom.getArmies() - armies >= MIN_ARMIES_FOR_TERRITORY
            && territoryTo.getArmies() + armies >= MIN_ARMIES_FOR_TERRITORY) {
            match.setMoveArmies(armies);
        }
    }

    //Inizia la dase di spostamento
    public void displacementStage(){
        deselectTerritories();
        match.setStage(DISPLACEMENT);
    }

    //Seleziona il territorio dal quale spostare le armate
    public void selectTerritoryFrom(String territoryId){
        Territory territory = match.getMap().getTerritory(territoryId);
        if(territory.getOwner().getId().equals(match.getTurnPlayer().getId()) && territory.getArmies() > MIN_ARMIES_FOR_TERRITORY){
            match.setTerritoryFrom(territory);
            match.setDefender(null);
        }

        nextStage();
    }

    //Seleziona il territorio sul quale spostare le armate
    public void selectTerritoryTo(String territoryId){
        Territory territory = match.getMap().getTerritory(territoryId);

        if(     territory.getOwner().getId().equals(match.getTurnPlayer().getId())
                && match.getTerritoryFrom() != null
                && territory.getOwner().getId().equals(match.getTerritoryFrom().getOwner().getId())){
            if(territory.isBordering(match.getTerritoryFrom())) {
                match.setTerritoryTo(territory);
                match.setDefender(null);
            }
            else match.setTerritoryFrom(territory);
        }

        nextStage();
    }

    //Termina il turno
    public void endsTurn(){
        //Passa il turno
        nextTurn();
    }

    //Gioca il tris nel caso sia il proprio turno ed è la fase del piazzamento delle armate
    public void playCards(String playerId, Integer[] cardsId){
        Player player = match.getTurnPlayer();
        int bonusArmies = 0;
        int availableArmies = player.getAvailableArmies();
        List<Card> playerCards = new ArrayList<>();
        boolean bonus = false;

        if(player.getId().equals(playerId) && match.getStage().equals(PLACEMENT) && cardsId.length == SET_CARDS_NUMBER){

            //Get cards
            for(int cardId : cardsId){
                playerCards.add(player.getCard(cardId));
            }

            //Cow set
            if(!bonus){
                bonus = true;
                for(Card card : playerCards){
                    if(!card.getCardType().equals(COW)){
                        bonus = false; break;
                    }
                }
                if(bonus) bonusArmies = COW_SET_BONUS;
            }

            //Farmer set
            if(!bonus) {
                bonus = true;
                for (Card card : playerCards) {
                    if (!card.getCardType().equals(FARMER)) {
                        bonus = false;
                        break;
                    }
                }
                if (bonus) bonusArmies = FARMER_SET_BONUS;
            }

            //Tractor set
            if(!bonus) {
                bonus = true;
                for (Card card : playerCards) {
                    if (!card.getCardType().equals(TRACTOR)) {
                        bonus = false;
                        break;
                    }
                }
                if (bonus) bonusArmies = TRACTOR_SET_BONUS;
            }

            //All different set
            if(!bonus) {
                bonus = true;
                for (int i = 0; i < playerCards.size(); i++) {
                    if (playerCards.get(i).getCardType().equals(JOLLY)) {
                        bonus = false;
                        break;
                    }
                    for (int j = i + 1; j < playerCards.size(); j++) {
                        if (playerCards.get(i).getCardType().equals(playerCards.get(j).getCardType())) {
                            bonus = false;
                            break;
                        }
                    }
                }
                if (bonus) bonusArmies = DIFFERENT_CARDS_SET_BONUS;
            }

            //Jolly set
            if(!bonus) {
                bonus = true;
                boolean jolly = false;
                CardType cardType = null;

                for (int i = 0; i < playerCards.size(); i++) {
                    if (playerCards.get(i).getCardType().equals(JOLLY)) {
                        if(jolly) bonus = false; //Caso di più jolly nel tris
                        jolly = true;
                    }
                }
                for (int i = 0; i < playerCards.size(); i++) {
                    if (!playerCards.get(i).getCardType().equals(JOLLY)) {
                        if(cardType == null) cardType = playerCards.get(i).getCardType();
                        else{
                            if(!cardType.equals(playerCards.get(i).getCardType())){
                                bonus = false;
                            }
                        }
                    }
                }
                if (bonus && jolly) bonusArmies = JOLLY_SET_BONUS;
            }

            //Caso in cui il tris è valido
            if(bonus){

                //Bonus owner
                for(Card card : playerCards){
                    if(!card.getCardType().equals(JOLLY)){
                        Player owner = match.getMap().getTerritory(card.getTerritoryId()).getOwner();
                        if(owner.getId().equals(player.getId())) bonusArmies += CARD_TERRITORY_BONUS;
                    }
                }

                //Assegnazione delle armate bonus
                player.setAvailableArmies(availableArmies + bonusArmies);

                //Le carte giocate vengono tolte dal giocatore e rimesse nel mazzo
                for(Card card : playerCards){
                    returnACard(player, card);
                }
            }
        }
    }

    //Preparazione iniziale della partita
    private void setup() throws Exception {

        //Imposta l'ordine dei giocatori
        setPlayersOrder();

        //Assegnazione delle armate
        assignArmies();

        //Distribuzione degli obiettivi
        assignMissions();

        //Distribuzione dei territori
        territoriesDistribution();

        //Creazione del mazzo di carte
        setCards();

        //Primo turno
        nextStage();

        //Posizionamento delle armate
        initialArmiesPlacement();
    }

    //Imposta l'ordine dei giocatori in modo randomico
    private void setPlayersOrder(){
        List<Player> orderedPlayers = new ArrayList<>();
        List<Player> players = match.getPlayers();

        while (players.size() > 0){
            int index = (int) (Math.random() * players.size());
            orderedPlayers.add(players.get(index));
            players.remove(index);
        }

        match.setPlayers(orderedPlayers);
    }

    //Assegnamento iniziale delle armate a tutti i giocatori in base a quanti sono
    private void assignArmies() throws Exception {
        int armies = 0;
        switch (match.getPlayers().size()){
            case 2: armies = INITIAL_ARMIES_2_PLAYERS; break;
            case 3: armies = INITIAL_ARMIES_3_PLAYERS; break;
            case 4: armies = INITIAL_ARMIES_4_PLAYERS; break;
            case 5: armies = INITIAL_ARMIES_5_PLAYERS; break;
            case 6: armies = INITIAL_ARMIES_6_PLAYERS; break;
            //default: throw new Exception("Number of players is " + match.getPlayers().size() + "\nMax number of players: " + MAX_PLAYERS + "\nMin number of players: " + MIN_PLAYERS);
        }
        for (Player player : match.getPlayers()) {
            player.setAvailableArmies(armies);
        }
    }

    //Vengono distribuiti tutti i territori della mappa ai giocatori in modo casuale
    private void territoriesDistribution(){
        List<Territory> territories = match.getMap().getTerritories();
        List<Player> players = match.getPlayers();
        int playerIndex = 0;

        //Shuffle the territories
        Collections.shuffle(territories);

        for(Territory territory : territories){
            territory.setOwner(players.get(playerIndex));
            playerIndex++;
            if(playerIndex>=players.size()) playerIndex = 0;
        }
    }

    //Vengono istanziate le missioni e assegnate casualmente ai giocatori
    private void assignMissions(){
        List<Mission> missions = new ArrayList<>();
        missions.add(new Mission01());
        missions.add(new Mission02());
        missions.add(new Mission03());
        missions.add(new Mission04());
        missions.add(new Mission05());
        missions.add(new Mission06());
        missions.add(new Mission07());
        for (Color color : Color.values()){
            missions.add(new MissionColor(color));
        }

        Collections.shuffle(missions);
        int index = 0;

        for(Player player : match.getPlayers()){
            player.setMission(missions.get(index));
            index++;
        }
    }

    //Piazzamento iniziale delle armate
    private void initialArmiesPlacement(){

        //Ogni giocatore piazza un'armata sui propri territori
        for(Territory territory : match.getMap().getTerritories()){
            placeArmies(territory, 1);
            territory.getOwner().setArmiesPlacedThisTurn(0); //non conteggia l'armata come piazzata questo turno
        }

        //Viene settata la fase di gioco in cui i giocatori devono piazzare 3 armate per turno fino all'esaurimento
        match.setStage(INITIAL_PLACEMENT);
    }

    //Piazzamento di un numero di armate da parte di un giocatore
    private void placeArmies(Territory territory, int armies){
        Player player = territory.getOwner();
        int availableArmies = player.getAvailableArmies();

        //Se il giocatore ha abbastanza armate le posiziona sul territorio
        if( availableArmies >= armies ){
            player.setAvailableArmies(availableArmies - armies);
            player.increaseArmiesPlacedThisTurn(armies);
            territory.addArmies(armies);
        }
    }

    //Gestisce l'avanzamento delle fasi nel turno di gioco in base allo stato del match
    private void handleStage(){
        Player turnPlayer = match.getTurnPlayer();

        //Fase di piazzamento iniziale delle armate.
        //Passa il turno solo se il giocatore ha piazzato le 3 armate
        //Oppure non ne ha più disponibili
        //Oppure è inattivo
        if(     match.getStage().equals(INITIAL_PLACEMENT)
                && (turnPlayer.getArmiesPlacedThisTurn()>=INITIAL_ARMIES_TO_PLACE
                || turnPlayer.getAvailableArmies()==0
                || !turnPlayer.isActive()
                )
        ){
            //resetta le armate piazzate nel turno
            turnPlayer.setArmiesPlacedThisTurn(0);

            //Setta il giocatore successivo che deve piazzare le armate
            Player nextPlayer = nextPlayerWithAvailableArmies();
            if(nextPlayer != null)  match.setTurnPlayer(nextPlayer);
            else { //Nel caso non ci siano più giocatori con armate disponibili si passa alla prossima fase del gioco

                //Imposta il primo giocatore
                match.setTurnPlayer(match.getPlayers().get(0));

                //Setta il primo turno
                match.setTurn(1);
                match.setStage(PLACEMENT);
            }
        }

        //Fase di piazzamento delle armate.
        //Se è il primo turno che svolge il giocatore allora salta questa fase.
        if(match.getStage().equals(PLACEMENT)){

            //Assegna le armate dovute al giocatore di turno
            if(!match.isArmiesWereAssigned()) setAvailableArmies();

            //Se le armate disponibili del giocatore sono esaurite allora si passa alla fase di attacco
            if( turnPlayer.getAvailableArmies() <= 0 ) match.setStage(ATTACK);
        }

        //Fase di spostamento delle armate. Il giocatore può effettuare un solo spostamento
        if(match.getStage().equals(DISPLACEMENT)){
            if(match.isMovementConfirmed()){
                nextTurn();
            }
        }
    }

    //Calcola e assegna le armate disponibili per il piazzamento al giocatore di turno.
    private void setAvailableArmies(){
        Player player = match.getTurnPlayer();
        int territoriesCounter = match.getTerritoriesOwned(player).size();
        List<Continent> continentsOwned = match.getContinentsOwned(player);

        //Se è il primo turno del giocatore allora non vengono assegnate armate
        if( match.getTurn() <= match.getPlayers().size()) return;

        //Armate disponibili in base al numero di territori posseduti dal giocatore
        int armiesAvailable = territoriesCounter / TERRITORIES_FOR_AN_ARMY;
        if(armiesAvailable <= 0) armiesAvailable = MINIMUM_AVAIABLE_ARMIES;

        //Aggiunta del bonus armate dovuto ai continenti in possesso del giocatore
        for(Continent continent : continentsOwned){
            armiesAvailable += continent.getBonusArmies();
        }

        //Assegnazione delle armate disponibili per il giocatore
        player.setAvailableArmies(armiesAvailable);

        //Segna che le armate sono state assegnate
        match.setArmiesWereAssigned(true);
    }

    //Individua in base alla fase di gioco i territori cliccabili dal giocatore
    private void setSelectable(){

        //Azzera i selezionabili
        for (Territory territory: match.getMap().getTerritories()) {
            territory.setClickable(false);
        }

        //Fase di piazzamento
        if(match.getStage().equals(INITIAL_PLACEMENT) || match.getStage().equals(PLACEMENT)){
            for(Territory territory : match.getMap().getTerritories()){
                if(territory.getOwner().getId().equals(match.getTurnPlayer().getId())){
                    territory.setClickable(true);
                }
            }
        }

        //Fase di attacco
        if(match.getStage().equals(ATTACK)){
            //Va selezionato un attaccante
            if(match.getAttacker() == null){
                for(Territory territory : match.getMap().getTerritories()){
                    if(     territory.getOwner().getId().equals(match.getTurnPlayer().getId())
                            && territory.getArmies() >= MINIMUM_ATTACKING_ARMIES){
                        territory.setClickable(true);
                    }
                }
            }
            //Va selezionato un difensore o un altro attaccante
            if(match.getAttacker() != null){
                for(Territory territory : match.getMap().getTerritories()){
                    if(
                        (   //Se il territorio è mio e ha abbastanza armate per un attacco e non è il territorio attaccante
                            territory.getOwner().getId().equals(match.getTurnPlayer().getId())
                            && territory.getArmies() >= MINIMUM_ATTACKING_ARMIES
                            && !territory.getId().equals(match.getAttacker().getId())
                        )
                            ||
                        (   //Se il territorio è confinante all'attaccante e non è mio
                            territory.isBordering(match.getAttacker())
                            && !territory.getOwner().getId().equals(match.getTurnPlayer().getId())
                        )
                    ){
                        territory.setClickable(true);
                    }
                }
            }
        }

        //Fase di spostamento
        if(match.getStage().equals(DISPLACEMENT)){
            for(Territory territory : match.getMap().getTerritories()){
                //Va selezionato un territorio con abbastanza armate spostabili da cui trasferire le armate
                if(match.getTerritoryFrom() == null){
                    if(     territory.getOwner().getId().equals(match.getTurnPlayer().getId())
                            && territory.getArmies() > MIN_ARMIES_FOR_TERRITORY){
                        territory.setClickable(true);
                    }
                }
                //Va selezionato un proprio territorio sul quale spostare le armate
                //Oppure un territorio non confinante dal quale spostare le armate
                if(match.getTerritoryFrom() != null && match.getTerritoryTo() == null){
                    if(     !territory.getId().equals(match.getTerritoryFrom().getId()) &&
                            (territory.getOwner().getId().equals(match.getTurnPlayer().getId())
                            && territory.isBordering(match.getTerritoryFrom()))
                            ||
                            (territory.getOwner().getId().equals(match.getTurnPlayer().getId())
                            && territory.getArmies() > MIN_ARMIES_FOR_TERRITORY)
                    ){
                        territory.setClickable(true);
                    }
                }
            }
        }
    }

    //Ritorna un array con i dadi a disposizione del difensore
    private List<Integer> getDicesDefender(){
        List<Integer> listDices = new ArrayList<>();
        Territory defender = match.getDefender();
        int armies = 0;
        int numDices = 0;

        if(defender != null) armies = defender.getArmies();
        if(armies > MAX_ATTACKING_DICES) numDices = MAX_ATTACKING_DICES;
        else numDices = armies;

        for(int i=0; i<numDices; i++) listDices.add(5);

        return listDices;
    }

    //Ritorna la lista di dadi disponibili per l'attacco
    private List<Integer> getDicesAttacker(int numberOfAttackerDice) {
        List<Integer> listDices = new ArrayList<>();
        int attackerArmies = match.getAttacker().getArmies();
        int maxDiceAvailable = Integer.min(attackerArmies -1, MAX_ATTACKING_DICES);

        for(int i=0; i<Integer.min(maxDiceAvailable, numberOfAttackerDice); i++){
            listDices.add(5);
        }

        return listDices;
    }

    //Lancia i dadi passati come parametro e li ordina dal valore maggiore al minore
    private void diceRoll(List<Integer> diceList){
        Random random = new Random();

        for (int i=0; i<diceList.size(); i++){
            diceList.set(i, random.nextInt(MAX_DICE_VALUE) + MIN_DICE_VALUE);
        }

        Collections.sort(diceList);
        Collections.reverse(diceList);
    }

    //Nel caso in cui l'attaccante sconfiggesse tutte le armate del difensore allora conquista il territorio attaccato
    private void conquest(int attackArmies){
        Territory attacker = match.getAttacker();
        Territory defender = match.getDefender();
        Player player = match.getTurnPlayer();
        Player enemy = defender.getOwner();

        if(defender.getArmies() <= 0){
            defender.setOwner(attacker.getOwner());
            match.setTerritoryFrom(match.getAttacker());
            match.setTerritoryTo(match.getDefender());
            moveArmies(attackArmies);
            player.setMustDrawACard(true);     //A fine turno il giocatore potrà pescare una carta
        }

        //Controlla se il giocatore sconfitto ha perso tutti i territori
        List<Territory> territories = match.getTerritoriesOwned(enemy);
        if(territories.size() == 0){
            player.addDefeatedPlayer(enemy);
            player.addCards(enemy.takeCards());
            enemy.setActive(false);
        }

        //Controlla se c'è un vincitore
        checkWinner();
    }

    //Conferma il movimento
    private void confirmMovement(){

        Territory territoryFrom = match.getTerritoryFrom();
        Territory territoryTo = match.getTerritoryTo();

        if(match.getMoveArmies() > 0 && territoryFrom != null && territoryTo != null){

            int armies = match.getMoveArmies();

            territoryFrom.removeArmies(armies);
            territoryTo.addArmies(armies);
            match.setMoveArmies(0);

            if(match.getStage().equals(DISPLACEMENT)) {
                match.setMovementConfirmed(true);
                nextTurn();
            }
        }
    }

    //Inizia il turno successivo
    public void nextTurn(){
        Player player = match.getTurnPlayer();
        int turn = match.getTurn();

        //Se il giocatore ha conquistato un territorio in questo turno allora pesca la carta
        if(player.isMustDrawACard()) drawACard(player);

        //Imposta il prossimo giocatore come giocatore di turno
        match.setTurnPlayer(nextPlayer());

        //Incrementa il turno
        turn++;
        match.setTurn(turn);

        //Imposta il primo stage
        match.setStage(PLACEMENT);

        //Resetta i parametri
        match.setMovementConfirmed(false);
        match.setArmiesWereAssigned(false);
        deselectTerritories();

        //Gestisci le fasi di gioco
        nextStage();
    }

    //Ritorna il giocatore attivo successivo
    public Player nextPlayer(){
        List<Player> players = match.getPlayers();
        Player turnPlayer = match.getTurnPlayer();
        Player nextPlayer = null;
        int index = players.indexOf(turnPlayer);
        boolean playerFound = false;

        while(!playerFound){
            //Seleziona giocatore successivo
            index++;
            if(index>=players.size()) index = 0;
            nextPlayer = players.get(index);
            playerFound = nextPlayer.isActive();
            if(nextPlayer.equals(turnPlayer)) {
                playerFound = true;
                nextPlayer = null;
            }
        }
        return nextPlayer;
    }

    //Ritorna il giocatore attivo successivo
    private Player nextPlayer(Player player){
        List<Player> players = match.getPlayers();
        Player nextPlayer = null;
        int index = players.indexOf(player);
        boolean playerFound = false;

        while(!playerFound){
            //Seleziona giocatore successivo
            index++;
            if(index>=players.size()) index = 0;
            nextPlayer = players.get(index);
            playerFound = nextPlayer.isActive();
            if(nextPlayer.equals(player)) {
                playerFound = true;
                nextPlayer = null;
            }
        }
        return nextPlayer;
    }

    //Controlla se è stato raggiunto un obiettivo
    public void checkWinner(){

        List<Player> activePlayers = match.getActivePlayers();

        if(match.getWinner() == null && !match.getStage().equals(GAME_OVER)){
            //Caso in cui un giocatore ha raggiunto l'obiettivo
            for (Player player : activePlayers) {
                if (player.getMission().isMissionCompleted(player, match)) {
                    match.setWinner(player);
                }
            }

            //Caso in cui ci sia un unico giocatore attivo
            if(activePlayers.size() == 1){
                match.setWinner(activePlayers.get(0));
            }
        }

        //Caso in cui non ci siano più giocatori attivi
        if(match.getWinner() != null || activePlayers.size() <= 0) match.setStage(GAME_OVER);
    }

    //Crea il mazzo di carte per la partita
    private void setCards(){
        List<Card> cards = new ArrayList<>();
        int id = 0;

        for (Territory territory: match.getMap().getTerritories()) {
            cards.add(new Card(id, territory, territory.getCardType()));
            id++;
        }

        //Aggiunta di 2 jolly
        cards.add(new Card(id, CardType.JOLLY));
        id++;
        cards.add(new Card(id,CardType.JOLLY));

        //mischia le carte
        Collections.shuffle(cards);

        match.setCards(cards);
    }

    //Pesca una carta
    private void drawACard(Player player){
        List<Card> cards = match.getCards();

        if(player.isMustDrawACard() && cards.size()>0){
            Card card = cards.get(0);
            player.addCard(card);
            cards.remove(0);
        }

        player.setMustDrawACard(false);
    }

    //Riponi la carta nel mazzo
    private void returnACard(Player player, Card card){
        if(player.hasCard(card)){
            player.removeCard(card);
            match.getCards().add(card);
        }
    }

    //Ritorna il primo giocatore seguendo il giro che abbia ancora armate disponibili
    private Player nextPlayerWithAvailableArmies(){
        Player nextPlayer = null;
        Player turnPlayer = match.getTurnPlayer();
        Player currentPlayer = nextPlayer();

        //Controlla se ci sono giocatori attivi
        List<Player> activePlayers = match.getActivePlayers();

        if(activePlayers.size() == 1){
            Player player = activePlayers.get(0);
            match.setWinner(player);
            nextPlayer = player;
        }
        if(activePlayers.size() > 1){
            while (nextPlayer == null && !currentPlayer.getId().equals(turnPlayer.getId())){
                if(currentPlayer.getAvailableArmies() > 0) nextPlayer = currentPlayer;
                else currentPlayer = nextPlayer(currentPlayer);
            }
        }

        return nextPlayer;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Match getMatch(){return this.match;}
}
