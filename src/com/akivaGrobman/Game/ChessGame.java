package com.akivaGrobman.Game;

import com.akivaGrobman.Game.Backend.Exceptions.IllegalMoveException;
import com.akivaGrobman.Game.Backend.Exceptions.NoPieceFoundException;
import com.akivaGrobman.Game.Backend.GameObjects.Board;
import com.akivaGrobman.Game.Backend.GameObjects.Pieces.Piece;
import com.akivaGrobman.Game.Backend.GameObjects.Pieces.PieceColor;
import com.akivaGrobman.Game.Backend.GameObjects.Pieces.PieceType;
import com.akivaGrobman.Game.Backend.Players.Move;
import com.akivaGrobman.Game.Backend.Players.Player;
import com.akivaGrobman.Game.Frontend.GraphicBoard;

import java.awt.*;

public class ChessGame {

    private final int STARTING_DEPTH = 1;
    public static final int SUM_OF_ROWS = 8;
    public static final int SUM_OF_COLUMNS = 8;

    private Player player;
    private Player enemy;
    private Player currentPlayer;
    private Board backendBoard;
    private GraphicBoard onScreenBoard;

    public ChessGame() {
        backendBoard = new Board();
        setPlayers();
        onScreenBoard = new GraphicBoard(backendBoard, player);
    }

    private void setPlayers() {
        // todo get this from server
        player = new Player(PieceColor.WHITE, this);
        enemy = new Player(PieceColor.BLACK, this);
        if(player.getPlayersColor() == PieceColor.WHITE) {
            currentPlayer = player;
        } else {
            currentPlayer = enemy;
        }
    }

    public synchronized void move(Move move, Player player) {
        if(currentPlayer.equals(player)) {
            Piece oldPiece = getPiece(move.getDestination());
            move(move);
            if(wasLegalMove(move, oldPiece)) {
                Point destination = move.getDestination();
                Piece piece = getPiece(destination);
                onScreenBoard.updateTile(destination, piece.getPieceType(), currentPlayer.getPlayersColor());
                onScreenBoard.updateTile(move.getOrigin(), null, null);
                changeCurrentPlayer();
            }
        }
    }

    private boolean wasLegalMove(Move move, Piece oldPiece) {
        return getPiece(move.getDestination()) != null && oldPiece != getPiece(move.getDestination()) && !backendBoard.hasPieceInThisPosition(move.getOrigin());
    }

    private void move(Move currentMove) {
        try {
            backendBoard.move(currentMove.getOrigin(), currentMove.getDestination(), STARTING_DEPTH);
        } catch (IllegalMoveException | NoPieceFoundException e) {
            String msg = e.getMessage();
            if (msg.contains("can not move piece to original position")) {
                System.out.println("can not move piece to original position");
            } else if (msg.contains("no piece found in position x = " + currentMove.getOrigin().x + " y = " + currentMove.getOrigin().y)) {
                System.out.println("no piece found in position x = " + currentMove.getOrigin().x + " y = " + currentMove.getOrigin().y);
            }
        }
    }

    private Piece getPiece(Point position) {
        try {
            return backendBoard.getPiece(position);
        } catch (NoPieceFoundException e) {
            return null;
        }
    }

    private void changeCurrentPlayer() {
        if(currentPlayer == player) {
            currentPlayer = enemy;
        } else {
            currentPlayer = player;
        }
    }

}
