package org.riskfirst.tweetprint.image.kite9;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JEditorPane;
import javax.swing.JFrame;


public class HtmlImageGenerator extends AbstractPNGWriter {
	

    public HtmlImageGenerator() {
    }

    public BufferedImage getBufferedImage(String html, Dimension size) {
        JEditorPane editorPane = createJEditorPane(size);
        
        editorPane.setEditable(false);
        editorPane.setText(html);
        editorPane.setContentType("text/html");

        JFrame frame = new JFrame();
        frame.setPreferredSize(editorPane.getPreferredSize());
        frame.setUndecorated(true);
        frame.add(editorPane);
        frame.pack();

        BufferedImage img = new BufferedImage((int) size.getWidth(), (int) size.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = img.getGraphics();
        graphics.drawRect(10, 10, 10, 10);

        frame.setVisible(true);
        frame.paint(graphics);
        frame.setVisible(false);
        frame.dispose();

        return img;
    }

    protected JEditorPane createJEditorPane(Dimension size) {
        final JEditorPane editorPane = new JEditorPane();
        editorPane.setSize(size);
        editorPane.setEditable(false);
        final SynchronousHTMLEditorKit kit = new SynchronousHTMLEditorKit();
        editorPane.setEditorKitForContentType("text/html", kit);
        editorPane.setContentType("text/html");
//        editorPane.addPropertyChangeListener(new PropertyChangeListener() {
//            public void propertyChange(PropertyChangeEvent evt) {
//                if (evt.getPropertyName().equals("page")) {
//                    onDocumentLoad();
//                }
//            }
//        });
        return editorPane;
    }

//    public void show() {
//        JFrame.setDefaultLookAndFeelDecorated(true);
//        JFrame frame = new JFrame();
//        frame.setTitle("My First Swing Application");
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        JLabel label = new JLabel("Welcome");
//        frame.add(label);
//        frame.add(editorPane);
//        frame.pack();
//        frame.setVisible(true);
//    }
}