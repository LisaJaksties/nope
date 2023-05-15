package view

import ai_player.Tournament
import socket.*
import java.awt.*
import javax.swing.*
import javax.swing.table.DefaultTableModel


class GUIMain : JFrame("Nope Card Game") {

    private val scoreTable = JTable()
    private var tournamentTable = JTable()
    private var currentTournamentTable = JTable()

    private var inTournament = false
    private var tournamentCreator = false

    private val menuPanel = JPanel()
    private val scorePanel = JPanel()
    private val crTournamentPanel = JPanel()
    private val tournamentListPanel = JPanel()
    private val tournamentLobbyPanel = JPanel()

    private val inGamePanel = JPanel()
    private val cardLayout = CardLayout()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(1080, 720)

        val img = ImageIcon("bin/cover.png")
        setIconImage(img.getImage())

        // Show the window
        isVisible = true

        // Create a JPanel to hold the menu items
        menuPanel.layout = BoxLayout(menuPanel, BoxLayout.Y_AXIS)
        menuPanel.setBackground(Color.WHITE)

        // Create a JLabel for the title
        val titleLabel = JLabel("Welcome to Nope!™")
        titleLabel.alignmentX = CENTER_ALIGNMENT
        titleLabel.font = titleLabel.font.deriveFont(24f)
        menuPanel.add(titleLabel)
        val subtitleLabel = JLabel("THE KNOCKOUT CARD GAME")
        subtitleLabel.alignmentX = CENTER_ALIGNMENT
        subtitleLabel.font = titleLabel.font.deriveFont(16f)
        menuPanel.add(subtitleLabel)
        menuPanel.add(Box.createRigidArea(Dimension(0,40)))
        // Create a JButton for each menu item
        val items = listOf("Create Tournament", "Join Tournament", "High Score List","game room")
        for (item in items) {
            val button = JButton(item)
            button.alignmentX = CENTER_ALIGNMENT
            button.preferredSize = Dimension(400,70)
            button.maximumSize = Dimension(800, 70)
            button.setForeground(Color.BLACK)                    // Vordergrundfarbe auf "rot" setzen
            button.background = Color(233,196,183) // Hintergrundfarbe auf "weiß" setzen
            button.isBorderPainted = false

            if(item == "Create Tournament"){
                button.addActionListener{
                    cardLayout.show(contentPane, "Create a new Tournament")
                }
            }
            if(item == "Join Tournament"){
                button.addActionListener{
                    cardLayout.show(contentPane, "tournamentLobby")
                }
            }
            if (item == "High Score List") {
                button.addActionListener {
                    cardLayout.show(contentPane, "scores")
                    //updateScoreTable()
                }
            }
            if(item == "game room")   {
                button.addActionListener {
                    cardLayout.show(contentPane, "game")

                }

            } else {
                button.addActionListener {
                    cardLayout.show(contentPane, "menu")
                }
            }
            menuPanel.add(button)
            menuPanel.add(Box.createRigidArea(Dimension(0,40)))

        }

        // Create a JButton to exit the application
        val exitButton = JButton("Exit")
        exitButton.alignmentX = CENTER_ALIGNMENT
        exitButton.background = Color(233,196,183)
        exitButton.maximumSize = Dimension(400, 45)
        exitButton.addActionListener {
            dispose()
        }
        menuPanel.add(exitButton)



        // Create a JPanel to hold the score table
        scorePanel.layout = BorderLayout()

        // Create a JTable for the score list
        scoreTable.model = DefaultTableModel(arrayOf("Name", "Score"), 0)
        scoreTable.fillsViewportHeight = true

        // Create a JScrollPane for the score table
        val scoreScrollPane = JScrollPane(scoreTable)
        scorePanel.add(scoreScrollPane, BorderLayout.CENTER)

        // Create a JButton to return to the menu
        val returnButton = JButton("Return")
        returnButton.addActionListener {
            cardLayout.show(contentPane, "menu")
        }
        scorePanel.add(returnButton, BorderLayout.SOUTH)


        initTournamentPanel()

        initTournamentListPanel()

        initTournamentLobbyPanel()

        initGamePanel()


        // Add the menu and score panels to the frame
        contentPane.layout = cardLayout
        contentPane.add(menuPanel, "menu")
        contentPane.add(scorePanel, "scores")
        contentPane.add(crTournamentPanel, "Create a new Tournament")
        contentPane.add(tournamentListPanel, "tournamentLobby")
        contentPane.add(tournamentLobbyPanel, "game lobby")
        contentPane.add(inGamePanel, "game")
    }

    private fun initGamePanel() {
        inGamePanel.layout = BorderLayout()
        inGamePanel.background = Color(0,0,0)

        // divide main Panel into two by adding a new panel
        val tempPanel = JPanel()
        tempPanel.layout = BoxLayout(tempPanel, BoxLayout.Y_AXIS)
        tempPanel.isVisible = true
        tempPanel.background = Color(255, 255, 160)



        val tempPanel3 = JPanel()
        tempPanel3.layout = BorderLayout()
        tempPanel3.border = object : javax.swing.border.LineBorder(Color(240,222,132), 2) {
            override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
                super.paintBorder(c, g, x, y, width, height)
                // additional border painting code here
            }
        }
        tempPanel3.background = Color(255, 255, 255)

        var hand = JTextArea(7, 10)
        hand.font = Font("Arial", Font.PLAIN, 22)
        hand.append("Your handcards:\n")
        hand.append("Green 2 \n")
        hand.append("Green Yellow 3 \n")
        hand.append("Green Yellow 3 \n")
        hand.append("Red Yellow 2 \n")
        hand.append("Green Yellow 3 \n")
        hand.append("Green Yellow 3 \n")
        hand.append("Green Yellow 3 \n")
        hand.append("Green Yellow 3 \n")
        hand.append("LuckyCard From Jinx: multi \n")
        hand.append("Card of Destruction: 9000 \n")


        var currTime = 222
        var time = "Rest Time    -    " + currTime + "        "
        val timeCount = JLabel(time )
        timeCount.font = Font("Arial", Font.BOLD, 24)
        tempPanel3.add(timeCount, BorderLayout.EAST)
        tempPanel3.add(hand, BorderLayout.WEST)





        val tempPanelDiv = JPanel(GridBagLayout())
        tempPanelDiv.isVisible = true
        tempPanelDiv.background = Color(0, 0, 0)

        val tempPanel4 = JPanel()
        tempPanel4.layout = BorderLayout()
        tempPanel4.background = Color(23, 100, 255)

        val taName = JTextArea(7, 10)
        taName.font = Font("Arial", Font.PLAIN, 22)
        val tid = 22
        taName.append("\n\n\n\n\n")
        taName.append("Tournament id :   $tid\n")
        taName.append("Match id :   $tid\n")
        taName.append("Round x out of y :   $tid\n")
        taName.append("Playerlist :  \n")
        taName.append("summer :  points $tid\n")
        taName.append("evilBot3000 : points  $tid\n")
        taName.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n")
        taName.append("current Player : Muchacho \n")
        taName.append("summer : cardsize  $tid\n")
        taName.append("evilBot3000 : cardsize  $tid\n")
        taName.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n")
        taName.append("~ Shin chan hat eine Kate gezogen   ~\n")
        taName.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n")
        tempPanel4.add(taName,BorderLayout.CENTER)




        val tempPanel2 = JPanel()
        tempPanel2.layout = GridBagLayout()
        tempPanel2.background = Color(233,196,183)

        val gbc1 = GridBagConstraints()
        gbc1.fill = GridBagConstraints.BOTH
        gbc1.ipady= 20
        gbc1.ipadx = 20


        val cardPile = JLabel("FANCY DANCY TOP CARDS : ")
        cardPile.font = Font("Arial", Font.ITALIC, 33)
        gbc1.gridx = 0
        gbc1.gridy = 0
        tempPanel2.add(cardPile,gbc1)



        gbc1.weightx = 0.5
        gbc1.gridx = 0
        gbc1.gridy = 1

        val but = JButton("Top Card:")
        but.preferredSize = Dimension(20, 240)
        but.background = Color(255,255,255)
        tempPanel2.add(but, gbc1)

        gbc1.gridx = 1

        val but2 = JButton("Top Card2:")
        but2.preferredSize = Dimension(20, 240)
        but2.background = Color(255,255,255)
        tempPanel2.add(but2, gbc1)

        var pileSize = 22
        var drawPileSize = "DRAW PILE SIZE :  " + pileSize
        val drawPile = JLabel(drawPileSize)
        drawPile.font = Font("Arial", Font.ITALIC, 33)
        gbc1.gridx = 0
        gbc1.gridy = 2
        tempPanel2.add(drawPile,gbc1)

        gbc1.gridx = 1
        gbc1.gridy = 2
        tempPanel2.add(tempPanel3,gbc1)






        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.4
        gbc.weighty = 0.4

        tempPanelDiv.add(tempPanel2, gbc)

        gbc.gridx = 1
        gbc.gridy = 0
        gbc.weightx = 0.5
        gbc.weighty = 0.5
        tempPanelDiv.add(tempPanel4, gbc)


        tempPanel.add(tempPanelDiv,gbc1)
        tempPanel.add(tempPanel3)




        val returnButton4 = JButton("leave Tournament")
        returnButton4.font = Font("Arial", Font.BOLD, 14)
        returnButton4.preferredSize = Dimension(150, 40)
        returnButton4.background = Color(255,255,255)
        returnButton4.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
        returnButton4.addActionListener {

            cardLayout.show(contentPane, "tournamentLobby")
        }



        val buttt = JButton("I am here for some space")
        buttt.font = Font("Arial", Font.BOLD, 14)
        buttt.preferredSize = Dimension(150, 40)
        buttt.background = Color(255,255,255)
        buttt.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
        buttt.addActionListener {

            cardLayout.show(contentPane, "tournamentLobby")
        }
        inGamePanel.add(buttt,BorderLayout.NORTH)
        inGamePanel.add(tempPanel, BorderLayout.CENTER)
        inGamePanel.add(returnButton4, BorderLayout.SOUTH)
    }

    private fun initTournamentLobbyPanel(){
        tournamentLobbyPanel.layout = BorderLayout()


        // divide main Panel into two by adding a new panel
        val tempPanel = JPanel()
        tempPanel.layout = BorderLayout()
        tempPanel.background = Color(255, 255, 255)


        val leaveTournament = JButton("Leave Tournament")
        leaveTournament.font = Font("Arial", Font.BOLD, 14)
        leaveTournament.preferredSize = Dimension(150, 40)
        leaveTournament.background = Color(233,196,183)
        leaveTournament.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        leaveTournament.isBorderPainted = true
        leaveTournament.addActionListener {
            if (inTournament) {
                val tournamentInfoStatus = leaveTournament()
                if(tournamentInfoStatus.success){
                    showMessage(this, "You left the tournament", 2000)
                    inTournament = false
                    tournamentCreator = false
                    // reset current tournament to nothing
                    currentTournament.bestOf = null
                    currentTournament.id = null
                    currentTournament.currentSize = null
                    currentTournament.status = null
                    currentTournament.players = null
                    currentTournament.createdAt = null

                    cardLayout.show(contentPane, "tournamentLobby")
                }else{
                    showMessage(this, tournamentInfoStatus.error.toString(),2000)
                }

            } else {
                showMessage(this, "You cannot leave a tournament as you ar not in one", 2000)
            }

        }

        val startTournament = JButton("Start Tournament")
        startTournament.font = Font("Arial", Font.BOLD, 14)
        startTournament.preferredSize = Dimension(150, 40)
        startTournament.background = Color(233,196,183)
        startTournament.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        startTournament.isBorderPainted = true
        startTournament.addActionListener {
            if (tournamentCreator) {
                val tournamentInfoStatus = startTournament()
                if(tournamentInfoStatus.success){
                    showMessage(this, "The Tournament starts now -> go to game room", 2000)
                    cardLayout.show(contentPane, "game")
                }else{
                    showMessage(this,tournamentInfoStatus.error.toString(),2000)
                }
            } else {
                showMessage(this, "Sorry but only the admin can start a game", 2000)
            }
        }

        tempPanel.add(leaveTournament, BorderLayout.WEST)
        tempPanel.add(startTournament, BorderLayout.EAST)





        currentTournamentTable.model =
            DefaultTableModel(arrayOf("ID", "Current Size", "Date", "status", "Players","BestOf"), 0)
        currentTournamentTable.fillsViewportHeight = true
        currentTournamentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        currentTournamentTable.rowSelectionAllowed = true
        currentTournamentTable.columnSelectionAllowed = false
        currentTournamentTable.font = Font("Arial", Font.ITALIC, 16)
        currentTournamentTable.gridColor = Color(233,196,183)



        val scrollPane = JScrollPane(currentTournamentTable)
        scrollPane.background = Color(255, 255, 255)
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        tournamentLobbyPanel.add(scrollPane, BorderLayout.CENTER)





        //tournamentLobbyPanel.add(gameInfo, BorderLayout.CENTER)
        tournamentLobbyPanel.add(tempPanel, BorderLayout.SOUTH)


    }
    private fun initTournamentListPanel() {
        tournamentListPanel.layout = BorderLayout()
        // Create a JButton to return to the menu
        val returnButton3 = JButton("leave Room")
        returnButton3.font = Font("Arial", Font.BOLD, 14)
        returnButton3.preferredSize = Dimension(150, 40)
        returnButton3.background = Color(255,255,255)
        returnButton3.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)

        returnButton3.addActionListener {
            cardLayout.show(contentPane, "menu")
        }


        tournamentTable.model =
            DefaultTableModel(arrayOf("Number", "ID", "Current Size", "Date", "status", "Players"), 0)
        tournamentTable.fillsViewportHeight = true
        tournamentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        tournamentTable.rowSelectionAllowed = true
        tournamentTable.columnSelectionAllowed = false
        tournamentTable.font = Font("Arial", Font.ITALIC, 16)
        tournamentTable.gridColor = Color(233,196,183)



        val scrollPane = JScrollPane(tournamentTable)
        scrollPane.background = Color(255, 255, 255)
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        tournamentListPanel.add(scrollPane, BorderLayout.CENTER)

        val joinTournament = JButton("Join Tournament")
        joinTournament.font = Font("Arial", Font.BOLD, 14)
        joinTournament.preferredSize = Dimension(150, 40)
        joinTournament.background = Color(233,196,183)
        joinTournament.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        joinTournament.isBorderPainted = true
        joinTournament.addActionListener {

            val selectedRowIndex = tournamentTable.selectedRow
            if (selectedRowIndex != -1) {

                val tournamentId = tournamentList[selectedRowIndex].id
                // join tournament
                val tournamentInfoStatus = joinTournament(tournamentId!!)
                if(tournamentInfoStatus.success){
                    inTournament = true
                    showMessage(this, "You joined game number: $tournamentId ", 4000)
                    //updateTournamentList()

                    var newTournament = Tournament(tournamentList[selectedRowIndex].id,tournamentList[selectedRowIndex].createdAt, tournamentList[selectedRowIndex].currentSize,tournamentList[selectedRowIndex].players, tournamentList[selectedRowIndex].status, tournamentList[selectedRowIndex].bestOf, null )

                    currentTournament = newTournament
                    updateCurrentTournamentList()
                    Thread.sleep(1000)
                    cardLayout.show(contentPane, "game lobby")
                }
                else{
                    showMessage(this, tournamentInfoStatus.error.toString(), 2000)
                }

            } else {
                showMessage(this, "Please choose the tournament you want to join", 2000)
            }
        }


        tournamentListPanel.add(returnButton3, BorderLayout.NORTH)
        tournamentListPanel.add(joinTournament, BorderLayout.SOUTH)



    }
    private fun initTournamentPanel() {
        crTournamentPanel.layout = BorderLayout()
        crTournamentPanel.background = Color(255, 255, 255)


        // Create a JButton to create a Tournament
        val createButton = JButton("Create Tournament")
        createButton.isEnabled = false
        createButton.font = Font("Arial", Font.BOLD, 14)
        createButton.preferredSize = Dimension(150, 40)
        createButton.background = Color(233,196,183)
        createButton.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        createButton.isBorderPainted = true

        // Create a JButton to return to the menu
        val returnButton2 = JButton("Back to Main Menu")
        returnButton2.font = Font("Arial", Font.BOLD, 14)
        returnButton2.preferredSize = Dimension(150, 40)
        returnButton2.background = Color(255,255,255)
        returnButton2.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)

        returnButton2.addActionListener {
            cardLayout.show(contentPane, "menu")
        }

        // divide main Panel into two by adding a new panel
        val centerPanel = JPanel()
        centerPanel.layout = BoxLayout(centerPanel, BoxLayout.Y_AXIS)
        centerPanel.background = Color(255, 255, 255)

        // create Tournament Field
        val label = JLabel("Select ~Best of Matches~ number :")
        label.font = Font("Arial", Font.BOLD, 30)
        label.preferredSize = Dimension(300, 30)
        label.horizontalAlignment = SwingConstants.CENTER
        label.verticalAlignment = SwingConstants.CENTER
        //label.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)

        val tfName = JTextField(5)
        tfName.font = Font("Arial", Font.PLAIN, 20)
        tfName.preferredSize = Dimension(100, 30)
        tfName.horizontalAlignment = SwingConstants.CENTER
        tfName.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
        tfName.addActionListener {
            // check if there is an input of integer
            if (tfName.text.matches(Regex("-?\\d+"))) {
                createButton.isEnabled = true
            } else {
                createButton.isEnabled = false
            }
        }
        createButton.addActionListener {
            val number = tfName.text.toIntOrNull()
            if (number != null) {

                val tournamentInfoStatus = createTournament(number)
                if(tournamentInfoStatus.success && tournamentInfoStatus.tournamentId != null){

                    showMessage(this,"Tournament created with ID: ${tournamentInfoStatus.tournamentId}, current size: ${tournamentInfoStatus.currentSize}, best of: ${tournamentInfoStatus.bestOf}",4000)
                    inTournament = true
                    tournamentCreator = true
                    //updateTournamentList()
                    currentTournament.id = tournamentInfoStatus.tournamentId
                    currentTournament.currentSize = tournamentInfoStatus.currentSize
                    currentTournament.bestOf = tournamentInfoStatus.bestOf

                    updateCurrentTournamentList()
                    cardLayout.show(contentPane, "game lobby")
                    tfName.text = "" // clear textfield
                } else{
                    showMessage(this,tournamentInfoStatus.error.toString(),4000)
                }

            } else {
                showMessage(this, "Please enter a valid integer value between 3 and 7.",2000)


            }
        }
        centerPanel.add(label)
        centerPanel.add(tfName)
//        crTournamentPanel.add(label, BorderLayout.LINE_START)
//        crTournamentPanel.add(tfName, BorderLayout.CENTER)

        crTournamentPanel.add(createButton, BorderLayout.SOUTH)
        crTournamentPanel.add(returnButton2, BorderLayout.NORTH)
        crTournamentPanel.add(centerPanel, BorderLayout.CENTER)
    }
    /*    private fun updateScoreTable() {
            val jsonString = """
                [
                    {"name": "John", "score": 100},
                    {"name": "Mary", "score": 90},
                    {"name": "Bob", "score": 80},
                    {"name": "Alice", "score": 70},
                    {"name": "David", "score": 60}
                ]
            """.trimIndent()

            val gson = Gson()
            val typeToken = object : TypeToken<List<Score>>() {}.type
            val scores = gson.fromJson<List<Score>>(jsonString, typeToken)

            scores.sortedByDescending { it.score }
                .forEach { score ->
                    val model = scoreTable.model as DefaultTableModel
                    model.addRow(arrayOf(score.name, score.score))
                }
        }*/
    private fun updateTournament(){
        val model = tournamentTable.model as DefaultTableModel
        val tempTournamentList = mutableListOf<Tournament>()
        tempTournamentList.addAll(tournamentList.map { it.copy() })


        for(i in 0 until tempTournamentList.size ){
            val currTour = tempTournamentList[i]
            //println(currTour)


            var players = ""
            for(j in 0 until currTour.players!!.size){
                val currP = currTour.players!![j]
                players  += " ${currP.username} "
            }
            model.addRow(arrayOf(i, currTour.id, currTour.currentSize, currTour.createdAt, currTour.status, players))
        }
        model.fireTableDataChanged()
        model.fireTableRowsInserted(0, tournamentList.size)

    }
    fun updateTournamentList(){

        tournamentTable.model= DefaultTableModel(arrayOf("Number", "ID", "Current Size", "Date", "status", "Players"), 0 )
        updateTournament()
        if(inTournament == true){
            tournamentTable.setEnabled(false)
        }else{
            tournamentTable.isEnabled = true
        }
    }
    private fun updateCurrentTournament(){
        if (currentTournament.id != null){

            var players = ""
            if(currentTournament.players != null){
                for(j in 0 until currentTournament.players!!.size){
                    val currP = currentTournament.players!![j]
                    players  += " ${currP.username} "
                }
            }
            val model = currentTournamentTable.model as DefaultTableModel
            model.addRow(arrayOf( currentTournament.id, currentTournament.currentSize, currentTournament.createdAt, currentTournament.status, players, currentTournament.bestOf))
            println("CURRENT T $currentTournament")
        }
    }
    private fun updateCurrentTournamentList(){

        currentTournamentTable.model= DefaultTableModel(arrayOf("ID", "Current Size", "Date", "status", "Players", "Best Of"), 0 )
        updateCurrentTournament()


    }

}