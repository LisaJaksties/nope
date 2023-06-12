package ai_player

import java.util.*
import kotlin.collections.ArrayList

/*
 * AI Player code
 */

class AILogic {

    fun makeMove(turnData: GameState, user: String): Move {
        val playerMove = Move(MoveType.PUT, null, null, null, "Because I can!")

        println()
        println("TopCard: ${turnData.topCard}")

        // indicate the possible hand cards to put on top card
        val matchingHandCards = findMatchingCards(turnData.hand!!, turnData.topCard!!, turnData.lastTopCard)

        //1. Nope, 2. Take, 3. Put
        if (matchingHandCards.isEmpty() && turnData.secondTurn == true) {
            playerMove.type = MoveType.NOPE
        } else if (matchingHandCards.isEmpty()) {
            playerMove.type = MoveType.TAKE
        } else {
            playerMove.type = MoveType.PUT
        }

        if (playerMove.type == MoveType.PUT) {

            // select cards to send
            val cardsForPut = aiDecision(matchingHandCards, turnData, user)
            if (cardsForPut.isEmpty()) {
                println("Something Went Wrong Choosing a Card to Send: cards is empty")
            } else if (cardsForPut.size > 3) {
                println("Something Went Wrong Choosing a Card to Send: more than maximum cards")
            } else if (cardsForPut.size == 1) {
                playerMove.card1 =
                    Card(cardsForPut[0].type, cardsForPut[0].color, cardsForPut[0].value, null, null, null)
            } else if (cardsForPut.size == 2) {
                playerMove.card1 =
                    Card(cardsForPut[0].type, cardsForPut[0].color, cardsForPut[0].value, null, null, null)
                playerMove.card2 =
                    Card(cardsForPut[1].type, cardsForPut[1].color, cardsForPut[1].value, null, null, null)
            } else {
                playerMove.card1 =
                    Card(cardsForPut[0].type, cardsForPut[0].color, cardsForPut[0].value, null, null, null)
                playerMove.card2 =
                    Card(cardsForPut[1].type, cardsForPut[1].color, cardsForPut[1].value, null, null, null)
                playerMove.card3 =
                    Card(cardsForPut[2].type, cardsForPut[2].color, cardsForPut[2].value, null, null, null)
            }
        }
        return playerMove
    }

    private fun findMatchingCards(
        handCards: ArrayList<Card>,
        lastCard: Card,
        preLastCard: Card?
    ): ArrayList<ArrayList<Card>> {
        var matchingCardCollocations = ArrayList<ArrayList<Card>>()


        if (lastCard.type == Type.NUMBER) {

            val colorMatchingCard = ArrayList<Card>()
            val inputColor = lastCard.color.color
            // check Colors and save cards that match
            for (card in handCards) {
                val currentCardColor = card.color.color

                // case 1 same color card              case 2  hand card consisting of two colors  case 3 top card consisting of two colors   case 4 hand card is multi card    case 5: both two colored values -> check for similarity and compare it with last card
                if (inputColor == currentCardColor || inputColor in currentCardColor || currentCardColor in inputColor || currentCardColor == Color.MULTI.color || checkDoubleValuesForeEquality(
                        card,
                        lastCard
                    ).color in lastCard.color.color
                ) {
                    colorMatchingCard.add(card)

                }
            }

            //special cards are added to matchingCardCollocations
            for (card in colorMatchingCard) {
                if (card.type == Type.SEE_THROUGH || card.type == Type.REBOOT || card.type == Type.SELECTION) {
                    val tempCards = ArrayList<Card>()
                    tempCards.add(card)
                    matchingCardCollocations.add(tempCards)
                }

            }
            // put two cards with same color

            // put one card or special card
            when (lastCard.value) {
                1 -> {
                    for (card in colorMatchingCard) {
                        if (card.type == Type.NUMBER || card.type == Type.JOKER) {
                            val tempCards = ArrayList<Card>()
                            tempCards.add(card)
                            matchingCardCollocations.add(tempCards)
                        }

                    }
                    // put two cards with same color
                }

                2 -> {
                    for (i in 0 until colorMatchingCard.size) {
                        val card = colorMatchingCard[i]
                        for (j in i + 1 until colorMatchingCard.size) {
                            val card2 = colorMatchingCard[j]
                            if (checkSameColor(card, card2, lastCard)) {
                                val tempCards = ArrayList<Card>()
                                tempCards.add(card)
                                tempCards.add(card2)
                                matchingCardCollocations.add(tempCards)
                            }
                        }
                    }
                    // put three cards together
                }

                else -> {
                    for (i in 0 until colorMatchingCard.size) {
                        val card = colorMatchingCard[i]
                        for (j in i + 1 until colorMatchingCard.size) {
                            val card2 = colorMatchingCard[j]
                            for (k in j + 1 until colorMatchingCard.size) {
                                val card3 = colorMatchingCard[k]
                                // check for same color
                                if (checkSameColor(card, card2, card3, lastCard)) {
                                    val tempCards = ArrayList<Card>()
                                    tempCards.add(card)
                                    tempCards.add(card2)
                                    tempCards.add(card3)
                                    matchingCardCollocations.add(tempCards)
                                }
                            }
                        }
                    }
                }
            }


            // print possible list of combinations that can be sent
            for (i in 0 until matchingCardCollocations.size) {
                println("$i : ${matchingCardCollocations[i].size} : ${matchingCardCollocations[i]}")
            }

        } else {
            // Joker as top card or  Reboot as top card
            if (lastCard.type == Type.JOKER || lastCard.type == Type.REBOOT) {
                for (card in handCards) {
                    val tempCards = ArrayList<Card>()
                    tempCards.add(card)
                    matchingCardCollocations.add(tempCards)
                }
            }
            // SeeThrough as top card
            if (lastCard.type == Type.SEE_THROUGH) {
                matchingCardCollocations = if (preLastCard != null) {
                    findMatchingCards(handCards, preLastCard, null)
                } else {
                    val cardST = Card(Type.NUMBER, lastCard.color, 1, null, null, null)
                    findMatchingCards(handCards, cardST, null)

                }
            }
            // Selection as top card
            if (lastCard.type == Type.SELECTION) {

                //one color
                matchingCardCollocations = if (lastCard.color != Color.MULTI) {
                    val cardS = Card(Type.NUMBER, lastCard.color, lastCard.selectValue!!, null, null, null)
                    findMatchingCards(handCards, cardS, preLastCard)
                    //multicolor
                } else {
                    val cardS = Card(Type.NUMBER, lastCard.selectedColor!!, lastCard.selectValue!!, null, null, null)
                    findMatchingCards(handCards, cardS, preLastCard)
                }
            }
        }

        return matchingCardCollocations

    }

    private fun checkSameColor(card: Card, card2: Card, lastCard: Card): Boolean {
        var isSameColor = false
        if (card.color.color == card2.color.color || card.color.color in card2.color.color || card2.color.color in card.color.color || card2.color.color == Color.MULTI.color || card.color.color == Color.MULTI.color || checkDoubleValuesForeEquality(
                card,
                card2
            ).color in lastCard.color.color
        ) {
            isSameColor = true
        }

        return isSameColor
    }

    private fun checkSameColor(card: Card, card2: Card, card3: Card, lastCard: Card): Boolean {
        var isSameColor = false
        // check equality of card 1 and 2
        if (card.color.color == card2.color.color || card.color.color in card2.color.color || card2.color.color in card.color.color || card2.color.color == Color.MULTI.color || card.color.color == Color.MULTI.color || checkDoubleValuesForeEquality(
                card,
                card2
            ).color in lastCard.color.color
        ) {
            // check equality of card 2 and 3
            if (card2.color.color == card3.color.color || card2.color.color in card3.color.color || card3.color.color in card2.color.color || card3.color.color == Color.MULTI.color || card2.color.color == Color.MULTI.color || checkDoubleValuesForeEquality(
                    card2,
                    card3
                ).color in lastCard.color.color
            ) {
                // check equality of card 1 and 3
                if (card.color.color == card3.color.color || card.color.color in card3.color.color || card3.color.color in card.color.color || card.color.color == Color.MULTI.color || card3.color.color == Color.MULTI.color || checkDoubleValuesForeEquality(
                        card,
                        card3
                    ).color in lastCard.color.color
                ) {
                    isSameColor = true
                }
            }
        }
        return isSameColor
    }

    private fun checkDoubleValuesForeEquality(card: Card, card2: Card): Color {
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
            matchingColor =
                card1Colors.find { it in card2Colors }?.let { Color.valueOf(it.uppercase(Locale.getDefault())) }
                    ?: Color.NULL
        }

        return matchingColor
    }


    private fun aiDecision(
        fittingCardInDeck: ArrayList<ArrayList<Card>>,
        turnData: GameState,
        user: String
    ): ArrayList<Card> {
        var bestMoveCards = ArrayList<Card>()

        //top priority: choose a special card if possible
        for (cards in fittingCardInDeck) {
            for (singleCard in cards) {
                if (singleCard.type == Type.REBOOT) {
                    bestMoveCards.add(singleCard)
                    return bestMoveCards
                }
            }
        }
        for (cards in fittingCardInDeck) {
            for (singleCard in cards) {
                if (singleCard.type == Type.SEE_THROUGH) {
                    bestMoveCards.add(singleCard)
                    return bestMoveCards
                }
            }
        }
        // 2. priority: check my own hand


        // put sth with Joker! if possible
        var containsJoker = false
        for (cards in fittingCardInDeck) {
            for (singleCard in cards) {
                if (singleCard.type == Type.JOKER) {
                    containsJoker = true
                    bestMoveCards.addAll(cards)
                    break
                }
            }

            if (containsJoker) {
                val sortedCards = ArrayList<Card>()
                var jokerC = Card(Type.NUMBER, Color.BLUE, null, null, null, null)
                for (singleCard in bestMoveCards) {
                    if (singleCard.type != Type.JOKER) {
                        sortedCards.add(singleCard)
                    } else {
                        jokerC = singleCard
                    }
                }
                sortedCards.add(jokerC)
                return sortedCards
            }
        }
        // search for double color cards and put them if possible
        var containsDoubleColor = false
        val doubleColored = setOf(
            Color.BLUE_GREEN,
            Color.RED_BLUE,
            Color.YELLOW_BLUE,
            Color.RED_GREEN,
            Color.YELLOW_GREEN,
            Color.RED_YELLOW
        )
        for (cards in fittingCardInDeck) {
            for (singleCard in cards) {
                if (singleCard.color in doubleColored) {
                    containsDoubleColor = true
                    bestMoveCards.addAll(cards)
                    break
                }
            }

            if (containsDoubleColor) {
                val opponent = Player(null, null, null, null)
                //sort in order 1,2,3
                bestMoveCards.sortBy { it.value }

                //look for opponents hand size
                for (p in turnData.players!!) {
                    if (p.username != (user)) {
                        opponent.handcards = p.handcards
                    }
                }
                // higher value if more cards
                // lower value if less than 4
                if (opponent.handcards!! > 6) {
                    //leave the order this way
                } else if (opponent.handcards!! >= 4 && bestMoveCards.size == 3) {
                    bestMoveCards = middleValueOnTop(bestMoveCards)
                } else {
                    bestMoveCards = lowValueOnTop(bestMoveCards)
                }
                return bestMoveCards
            }
        }
        // return one color that I can't put next time
        var countRed = 0
        var countYellow = 0
        var countBlue = 0
        var countGreen = 0
        for (card in turnData.hand!!) {
            when (card.color) {
                Color.RED -> {
                    countRed++
                }

                Color.YELLOW -> {
                    countYellow++
                }

                Color.BLUE -> {
                    countBlue++
                }

                Color.GREEN -> {
                    countGreen++
                }

                else -> {}
            }
        }
        //identify color that occurs the most in my hands
        val searchedColor: Color = if (countRed <= countBlue && countRed <= countYellow && countRed <= countGreen) {
            Color.RED
        } else if (countYellow <= countBlue && countYellow <= countRed && countYellow <= countGreen) {
            Color.YELLOW
        } else if (countBlue <= countYellow && countBlue <= countRed && countBlue <= countGreen) {
            Color.BLUE
        } else {
            Color.GREEN
        }

        for (cards in fittingCardInDeck) {
            for (singleCard in cards) {
                if (singleCard.color == searchedColor) {
                    bestMoveCards.addAll(cards)
                    bestMoveCards = sortByColor(bestMoveCards, searchedColor)
                    return bestMoveCards
                }
            }
        }
        // plan z : choose the first option
        bestMoveCards.addAll(fittingCardInDeck[0])
        // in order (last one top priority)
        return bestMoveCards

    }

    /**
     * sorting the cards : lowest value on top
     */
    private fun lowValueOnTop(cardsToSort: ArrayList<Card>): ArrayList<Card> {
        val sortedCards = ArrayList<Card>()
        for (i in cardsToSort.size - 1 downTo 0) {
            sortedCards.add(cardsToSort[i])
        }
        return sortedCards
    }

    /**
     * sorting the cards: middle value on top if 3 cards
     */
    private fun middleValueOnTop(cardsToSort: ArrayList<Card>): ArrayList<Card> {
        val sortedCards = ArrayList<Card>()

        sortedCards.add(cardsToSort[2])
        sortedCards.add(cardsToSort[0])
        sortedCards.add(cardsToSort[1])

        return sortedCards
    }

    /**
     * sorting the cards after color
     */
    private fun sortByColor(cardsToSort: ArrayList<Card>, color: Color): ArrayList<Card> {
        var sortedCards = ArrayList<Card>()
        for (i in cardsToSort.size - 1 downTo 0) {
            if (cardsToSort[i].color == color) {
                if (cardsToSort[i] != cardsToSort.last()) {
                    sortedCards = swap2Cards(cardsToSort, cardsToSort.last(), cardsToSort[i])
                } else {
                    sortedCards.addAll(cardsToSort)
                }
                return sortedCards
            }
        }

        return sortedCards
    }

    /**
     * swap cards
     */
    private fun swap2Cards(cards: ArrayList<Card>, lastCard: Card, card: Card): ArrayList<Card> {
        val lastIndex = cards.indexOf(lastCard)
        val cardIndex = cards.indexOf(card)

        // Check if both cards are present in the list
        if (lastIndex != -1 && cardIndex != -1) {
            // swap cards
            val temp = cards[lastIndex]
            cards[lastIndex] = cards[cardIndex]
            cards[cardIndex] = temp
        }

        return cards

    }


}

