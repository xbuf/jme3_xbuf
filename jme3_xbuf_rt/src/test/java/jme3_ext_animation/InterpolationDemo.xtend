package jme3_ext_animation;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author John B. Matthews (see https://groups.google.com/forum/#!msg/comp.lang.java.programmer/mHAWRkI_Qg0/EWDZqyPBpIQJ)
 * @author David Bernard
 */
class InterpolationDemo extends JPanel {

	static def void main(String[] args) {
		EventQueue.invokeLater[
			val f = new JFrame("InterpolationDemo")
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
			f.add(new InterpolationDemo())
			f.pack()
			f.setLocationRelativeTo(null)
			f.setVisible(true)
		]
	}

	AffineTransform at;


	new() {
		this.setPreferredSize(new Dimension(640, 480))
	}

	override paintComponent(Graphics g) {
		super.paintComponent(g);
		val g2d = g as Graphics2D
		g2d.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON
		)
		val w = getWidth()
		val h = getHeight()
		g2d.setColor(Color.gray);
		//g2d.drawLine(0, 0, 0, h)
		//g2d.drawLine(w-1, 0, w-1, h)
		g2d.drawLine(0, h / 2, w, h / 2)

		at = g2d.getTransform()
		g2d.translate(0, h / 2)
		g2d.scale(1, -1)
		g2d.setColor(Color.blue)
		var x1 = 0
		var y1 = 0
		var x2 = 0
		var y2 = 0
		//val fct = Interpolations.sin
		//val fct = Interpolations.linear
		val fct = Interpolations.cubicBezier(0.390411f, 0.000000f, 0.609589f, 1.000000f)
		for (var x = 0f; x <= 1f; x += 0.001f) {
			val y = fct.apply(x, 0, 1)
			x2 = Math.round(x * w) as int
			y2 = Math.round(y * h/2) as int
			if (x > 0) g2d.drawLine(x1, y1, x2, y2)
			x1 = x2
			y1 = y2
		}

		g2d.setTransform(at)
		g2d.setColor(Color.blue)
		g2d.drawString("y = " + fct.class.simpleName + ".apply(x)", 100, 100)
	}
}