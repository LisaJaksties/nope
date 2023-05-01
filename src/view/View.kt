package view/*
 * GUI code
 */

import javax.swing.*
import java.awt.*
import java.awt.geom.*

class MainWindow : JFrame("Nope") {
    init {
        // Create the button
        val button = JButton("Click Me!")

        // Add the button to the bottom of the window
        add(button, BorderLayout.SOUTH)

        // Add the panel to the center of the window
        val backg = Background()
        add(backg, BorderLayout.CENTER)

        // Create a game card button and add it to the center of the window
        val cardbutton = GameCardButton("3")
        add(cardbutton, BorderLayout.NORTH)

        // Set the window size and center it on the screen
        setSize(800, 700)
        setLocationRelativeTo(null)

        // Show the window
        isVisible = true

    }
}

class GameCardButton(text: String) : JButton(text) {
    init {
        // Set the size and preferred size of the button
        val size = Dimension(200, 300)
        setSize(size)
        preferredSize = size

        // Make the button transparent
        isOpaque = false

        // Set the font and foreground color of the button
        font = Font("Arial", Font.BOLD, 36)
        foreground = Color.WHITE

        // Set the button's border to null
        border = null
    }

    override fun paintComponent(g: Graphics) {
        val g2d = g.create() as Graphics2D

        // Create a round rectangle shape that defines the shape of the button
        val buttonShape = RoundRectangle2D.Float(0f, 0f, width.toFloat(), height.toFloat(), 10f, 10f)

        // Set the clip of the graphics context to the button shape
        g2d.clip = buttonShape

        // Draw the background gradient
        val gradient = GradientPaint(0f, 0f, Color.BLUE, 0f, height.toFloat(), Color.WHITE)
        g2d.paint = gradient
        g2d.fillRect(0, 0, width, height)

        // Draw the text
        val fm = g2d.fontMetrics
        val textWidth = fm.stringWidth(text)
        val textHeight = fm.height
        g2d.color = foreground
        g2d.drawString(text, (width - textWidth) / 2, (height - textHeight) / 2 + fm.ascent)

        // Dispose the graphics context
        g2d.dispose()
    }
}

class Background : JPanel(){
    init{
        // Set the preferred size of the panel
        preferredSize = Dimension(800, 700)
    }
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        // Define the pastel blue and white colors
        val pastelBlue = Color(179, 229, 252)
        val white = Color.WHITE

        // Create the gradient paint object
        val gradient = GradientPaint(0f, 0f, pastelBlue, 0f, height.toFloat(), white)

        // Set the paint to the graphics object
        val g2d = g as Graphics2D
        g2d.paint = gradient

        // Fill the panel with the gradient
        g2d.fillRect(0, 0, width, height)
    }
}

class GameCardPanel : JPanel() {
    init {
        // Set the preferred size of the panel
        preferredSize = Dimension(50, 75)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        // Define the card background color and font
        val cardBackgroundColor = Color.WHITE
        val cardFont = Font("Arial", Font.BOLD, 48)

        // Set the paint to the graphics object
        g.color = cardBackgroundColor
        g.fillRect(0, 0, width, height)

        // Draw the number 3 in the center of the card
        g.color = Color.BLACK
        g.font = cardFont
        val fm = g.fontMetrics
        val number = "3"
        val x = (width - fm.stringWidth(number)) / 2
        val y = (fm.ascent + (height - (fm.ascent + fm.descent)) / 2)
        g.drawString(number, x, y)

        // Draw the card border
        g.color = Color.BLACK
        g.drawRect(0, 0, width - 1, height - 1)
    }
}