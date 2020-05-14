package com.akivaGrobman.Game.Client.Backend.GameRules;

import com.akivaGrobman.Game.Client.Backend.Exceptions.IllegalMoveException;
import com.akivaGrobman.Game.Client.Backend.Exceptions.NoPieceFoundException;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Board.Board;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.King;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.Piece;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.PieceColor;
import com.akivaGrobman.Game.Client.ChessGame;
import java.awt.*;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class CheckChecker {

    private static final int MAX_DEPTH = 3;
    private final int DEPTH;
    private final PieceColor PLAYERS_COLOR;
    private final Board BOARD;

    public CheckChecker(int depth, PieceColor playersColor, Board board) {
        this.DEPTH = depth;
        this.PLAYERS_COLOR = playersColor;
        this.BOARD = Board.getClone(board);
    }

    public boolean isInCheck() {
        List<Point> enemyPiecePositions = getEnemyPiecePositions();
        // in this method we ignore the NoSuchElementException because when depth == 2.  because one of the players doesn't have a king
        Point kingPosition = getKingPosition(PLAYERS_COLOR);
        for (Point enemyPiecePosition: enemyPiecePositions) {
            try {
                Piece piece = BOARD.getPiece(enemyPiecePosition);
                assert piece != null;
                if(piece instanceof King) continue;
                if(DEPTH < MAX_DEPTH) {
                    if(BOARD.isLegalMove(enemyPiecePosition, kingPosition, DEPTH + 1)) {
                        return true;
                    }
                }
            } catch (NoPieceFoundException | IllegalMoveException ignored) {}
        }
        return false;
    }

    private Point getKingPosition(PieceColor kingColor) {
        for (int y = 0; y < ChessGame.SUM_OF_ROWS; y++) {
            for (int x = 0; x < ChessGame.SUM_OF_COLUMNS; x++) {
                try {
                    Piece piece = BOARD.getPiece(new Point(x, y));
                    if(piece instanceof King && piece.getPieceColor() == kingColor) {
                        return new Point(x, y);
                    }
                } catch (NoPieceFoundException ignored) {}
            }
        }
        throw new NoSuchElementException(PLAYERS_COLOR.toString());
    }

    private List<Point> getEnemyPiecePositions() {
        List<Point> positions = new ArrayList<>();
        for (int y = 0; y < ChessGame.SUM_OF_ROWS; y++) {
            for (int x = 0; x < ChessGame.SUM_OF_COLUMNS; x++) {
                try {
                    Piece piece = BOARD.getPiece(new Point(x, y));
                    if(piece.getPieceColor() != PLAYERS_COLOR) {
                        positions.add(new Point(x, y));
                    }
                } catch (NoPieceFoundException ignored) {}
            }
        }
        return positions;
    }

}
