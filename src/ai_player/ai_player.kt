package ai_player/*
 * AI Player code
 */

// fun analyseMove(topCard: String, lastTopCard: String, handCards: Array, prevTurnCards: Array, cardsetsToReturn: Array): Array{
//     // 1. Strategy: do I have a colorful card -> return this one

//     // 2. Strategy: do I have a selection card? does it match colors? -> return this one + number of cards the next player has to return + ggf. color when its colorful card

//     // 3. Strategy:

//     // 4. Strategy: compare the cardsetsToReturn with handCards: which color could I return myself -> return the other one

//     // 5. Strategy:
// }



class AILogic{

    fun makeMove(turnData: GameState):Move{
        var playerMove = Move(MoveType.PUT,null,null,null,"Because I can!")

        println()
        println("TopCard: ${turnData.topCard}")

        // indicate the possible handcards to put on topcard
        val matchingHandCards = findMatchingCards(turnData.hand!!, turnData.topCard!!, turnData.lastTopCard)

        //1. Nope, 2. Take, 3. Put
        if(matchingHandCards.isEmpty() && turnData.secondTurn == true) {
            playerMove.type = MoveType.NOPE
        } else if(matchingHandCards.isEmpty()){
            playerMove.type = MoveType.TAKE
        } else{
            playerMove.type = MoveType.PUT
        }

        if(playerMove.type == MoveType.PUT){

            // select cards to send
            val cardsForPut = aiDecision(matchingHandCards, turnData)
            if(cardsForPut.isEmpty() || cardsForPut.size > 3){
                println("Something Went Wrong Choosing a Card to Send")
            } else if(cardsForPut.size == 1){
                playerMove.card1 = Card(cardsForPut[0].type, cardsForPut[0].color, cardsForPut[0].value,null,null,null)
            }else if(cardsForPut.size == 2){
                playerMove.card1 = Card(cardsForPut[0].type, cardsForPut[0].color,  cardsForPut[0].value,null,null,null)
                playerMove.card2 = Card(cardsForPut[1].type, cardsForPut[1].color,  cardsForPut[1].value,null,null,null)
            }else{
                playerMove.card1 = Card(cardsForPut[0].type, cardsForPut[0].color, cardsForPut[0].value,null,null,null)
                playerMove.card2 = Card(cardsForPut[1].type, cardsForPut[1].color, cardsForPut[1].value,null,null,null)
                playerMove.card3 = Card(cardsForPut[2].type, cardsForPut[2].color, cardsForPut[2].value,null,null,null)
            }
        }
        return playerMove
    }

    private fun findMatchingCards(handCards: ArrayList<Card>, lastCard: Card, preLastCard: Card?): ArrayList<ArrayList<Card>> {
        var cardMatches = false
        var matchingCardCollocations =  ArrayList<ArrayList<Card>>()

        if(lastCard.type == Type.NUMBER){

            var colorMatchingCard = ArrayList<Card>()
            val inputColor = lastCard.color.color
            // check Colors and save cards that match
            for (card in handCards){
                val currentCardColor = card.color.color
                // case 1 same color card              case 2  hand card consisting of two colors  case 3 top card consisting of two colors   case 4 hand card is multi card    case 5: both two colored values -> check for similarity and compare it with last card
                if(inputColor == currentCardColor || inputColor in currentCardColor || currentCardColor in inputColor || currentCardColor == Color.MULTI.color|| checkDoubleValuesForeEquality(card,lastCard,lastCard).color in lastCard.color.color){
                    colorMatchingCard.add(card)

                }
            }

            // put one card or special card
            if(lastCard.value == 1){
                for(card in colorMatchingCard){
                    if( card.type == Type.NUMBER ){
                        var tempCards = ArrayList<Card>()
                        tempCards.add(card)
                        matchingCardCollocations.add(tempCards)
                    }

                }
            // put two cards with same color
            }else if(lastCard.value == 2){
                for (i in 0 until colorMatchingCard.size) {
                    val card = colorMatchingCard[i]
                    for (j in i+1 until colorMatchingCard.size) {
                        val card2 = colorMatchingCard[j]
                        if(checkSameColor(card,card2,lastCard)){
                            var tempCards = ArrayList<Card>()
                            tempCards.add(card)
                            tempCards.add(card2)
                            matchingCardCollocations.add(tempCards)
                        }
                    }
                }
            // put three cards together
            }else{
                for (i in 0 until colorMatchingCard.size) {
                    val card = colorMatchingCard[i]
                    for (j in i+1 until colorMatchingCard.size) {
                        val card2 = colorMatchingCard[j]
                        for (k in j+1 until colorMatchingCard.size) {
                            val card3 = colorMatchingCard[k]
                            // check for same color
                            if(checkSameColor(card,card2,card3,lastCard)){
                                var tempCards = ArrayList<Card>()
                                tempCards.add(card)
                                tempCards.add(card2)
                                tempCards.add(card3)
                                matchingCardCollocations.add(tempCards)
                            }
                        }
                    }
                }
            }


            // print possible list of combinations that can be send
            for(i in 0 until matchingCardCollocations.size){
                println("$i : ${matchingCardCollocations[i].size} : ${matchingCardCollocations[i]}")
            }

        }else{
            // Joker as topcard or  Reboot as topcard
            if(lastCard.type == Type.JOKER || lastCard.type == Type.REBOOT){
                for (card in handCards){
                    var tempCards = ArrayList<Card>()
                    tempCards.add(card)
                    matchingCardCollocations.add(tempCards)
                }
            }
            // SeeThrough as topcard
            if(lastCard.type == Type.SEE_THROUGH){
                matchingCardCollocations = findMatchingCards(handCards, preLastCard!!, null)


            }
            // Selection as topcard
            if(lastCard.type == Type.SELECTION){

                //one color
                if(lastCard.color != Color.MULTI){
                    val cardS = Card(Type.NUMBER, lastCard.color,lastCard.selectValue!!, null, null, null )
                    matchingCardCollocations = findMatchingCards(handCards, preLastCard!!, null)
                //multicolor
                }else{
                    val cardS = Card(Type.NUMBER, lastCard.selectedColor!!,lastCard.selectValue!!, null, null, null )
                    matchingCardCollocations = findMatchingCards(handCards, preLastCard!!, null)
                }
            }
        }

        return matchingCardCollocations

    }

    private fun checkSameColor(card:Card, card2:Card, lastCard: Card):Boolean{
        var isSameColor = false
        if(card.color.color == card2.color.color || card.color.color in card2.color.color || card2.color.color in card.color.color || card2.color.color == Color.MULTI.color || card.color.color == Color.MULTI.color|| checkDoubleValuesForeEquality(card,card2, lastCard).color in lastCard.color.color){
            isSameColor = true
        }

        return isSameColor
    }
    private fun checkSameColor(card:Card, card2:Card, card3:Card,lastCard: Card):Boolean{
        var isSameColor = false
        // check equality of card 1 and 2
        if(card.color.color == card2.color.color || card.color.color in card2.color.color || card2.color.color in card.color.color || card2.color.color == Color.MULTI.color || card.color.color == Color.MULTI.color|| checkDoubleValuesForeEquality(card,card2,lastCard).color in lastCard.color.color){
            // check equality of card 2 and 3
            if(card2.color.color == card3.color.color || card2.color.color in card3.color.color || card3.color.color in card2.color.color || card3.color.color == Color.MULTI.color || card2.color.color == Color.MULTI.color|| checkDoubleValuesForeEquality(card2,card3,lastCard).color in lastCard.color.color){
                // check equality of card 1 and 3
                if(card.color.color == card3.color.color || card.color.color in card3.color.color || card3.color.color in card.color.color || card.color.color == Color.MULTI.color|| card3.color.color == Color.MULTI.color|| checkDoubleValuesForeEquality(card,card3,lastCard).color in lastCard.color.color){
                    isSameColor = true
                }
            }
        }
        return isSameColor
    }
    private fun checkDoubleValuesForeEquality(card:Card, card2:Card, lastCard: Card): Color {
        var matchingColor = Color.NULL
        // check for two-type colors
        val twoTypeColors = setOf(
            Color.RED_YELLOW, Color.BLUE_GREEN, Color.YELLOW_BLUE,
            Color.RED_BLUE, Color.RED_GREEN, Color.YELLOW_GREEN
        )

        if (card.color in twoTypeColors && card2.color in twoTypeColors) {
            val card1Colors = card.color.color.split("-")
            val card2Colors = card2.color.color.split("-")
            // Find the matching color between card1Colors and card2Colors and set matchingColor to the color or else to null
            matchingColor = card1Colors.find { it in card2Colors }?.let { Color.valueOf(it.toUpperCase()) } ?: Color.NULL
        }

        return matchingColor
    }


    private fun aiDecision(fittingCardInDeck: ArrayList< ArrayList<Card>>, turndata: GameState): ArrayList<Card> {
        var bestMoveCards = ArrayList<Card>()

        // TODO add smart decision
        bestMoveCards.addAll(fittingCardInDeck[0])

        // in order (last one top priority)
        return bestMoveCards
    }




}

