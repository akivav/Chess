package com.akivaGrobman.Game.Client.Frontend;

import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.PieceColor;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.PieceType;
import com.akivaGrobman.Game.Client.ChessGame;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GraphicTile extends JPanel {

    private final ChessGame game;
    private final Point tilePosition;
    private final Color tileColor;
    private BufferedImage image;
    private PieceType pieceType;
    private PieceColor pieceColor;

    public GraphicTile(Point position, Color color, ChessGame game) {
        tilePosition = position;
        tileColor = color;
        this.game = game;
        image = null;
        panelSetUp();
    }

    public void update(PieceType pieceType, PieceColor pieceColor) {
        this.pieceType = pieceType;
        this.pieceColor = pieceColor;
        image = getPieceImage();
    }

    public void resetColor() {
        setBackground(tileColor);
    }

    public void drawAsLegalTile() {
        setBackground(Color.cyan);
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if(hasPiece())
            g.drawImage(image, 10, 10, GraphicBoard.TILE_SIZE - 20, GraphicBoard.TILE_SIZE - 20, this);
    }

    private void panelSetUp() {
        this.setPreferredSize(new Dimension(GraphicBoard.TILE_SIZE, GraphicBoard.TILE_SIZE));
        this.setBorder(BorderFactory.createLineBorder(Color.darkGray));
        this.addMouseListener(new MouseListener(game, tilePosition));
        this.setBackground(tileColor);
    }

    private boolean hasPiece() {
        return image != null;
    }

    private BufferedImage getPieceImage() {
        try {
            return  getImageFromFileName("Images/" + pieceColor.toString().toLowerCase() + "_" + pieceType.toString().toLowerCase() + ".png");
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    private BufferedImage getImageFromFileName(String fileName) throws IOException {
        return ImageIO.read(getClass().getResource(fileName));
    }

    @Override
    public String toString() {
        return (pieceType != null)? " " + pieceType.toString() + " ": "      ";
    }

}
