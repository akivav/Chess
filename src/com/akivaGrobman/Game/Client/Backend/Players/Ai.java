package com.akivaGrobman.Game.Client.Backend.Players;

import com.akivaGrobman.Game.Client.Backend.Exceptions.NoPieceFoundException;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Board.Board;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.King;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.Piece;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.PieceColor;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.PieceType;
import com.akivaGrobman.Game.Client.GameManagers.ChessGame;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import static com.akivaGrobman.Game.Client.Backend.GameRules.SpecialMoves.wasEnpassant;
import static com.akivaGrobman.Game.Client.GameManagers.ChessGame.SUM_OF_COLUMNS;
import static com.akivaGrobman.Game.Client.GameManagers.ChessGame.SUM_OF_ROWS;

public class Ai extends Player{

    private final int MAX_DEPTH;

    public Ai(PieceColor aiColor, ChessGame game) {
         super(aiColor);
         MAX_DEPTH = 2;
         setContext(game);
     }

    public void makeAMove(Board board) {
         game.move(getMove(board), this);
     }

    private Positions getMove(Board board) {
         return getBestMoveForAi(Board.getClone(board));
     }

    private Positions getBestMoveForAi(Board board) {
        List<Point> piecePositions = getPiecePositions(board, getPlayersColor());
        int highestScore = Integer.MIN_VALUE;
        Point origin = null;
        Point destination = null;
        for (Point tempOrigin: piecePositions) {
            Piece piece = getPiece(board, tempOrigin);
            assert piece != null; //check the getPiecePositions method to see why
            for (Point tempDestination: piece.getLegalMoves(board, tempOrigin)) {
                Piece pieceAtDestination = getPiece(board, tempDestination);
                Piece pieceInEnpassantPosition = getPiece(board, new Point(tempDestination.x, tempOrigin.y)); // will only actualy be enpassant position sometimes but the wasEnpassant method will do the actualy enpassnt check
                if (hasKingAtDestination(board, tempDestination)) {
                    Positions bestMove = new Positions(tempOrigin, getPlayersColor());
                    bestMove.setDestination(tempDestination);
                    return bestMove;
                }
                board.updateTile(tempOrigin, null);
                board.updateTile(tempDestination, piece);
                if (wasEnpassant(board, tempOrigin, tempDestination, pieceAtDestination)) {
                    board.updateTile(new Point(tempDestination.x, tempOrigin.y), null);
                }
                int score = getMinMax(board, getPlayersColor(), 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
                board.updateTile(tempOrigin, piece);
                board.updateTile(tempDestination, pieceAtDestination);
                board.updateTile(new Point(tempDestination.x, tempOrigin.y), pieceInEnpassantPosition);
                if(highestScore < score) {
                    highestScore = score;
                    origin = new Point(tempOrigin);
                    destination = new Point(tempDestination);
                }
            }
        }
        Positions bestMove = new Positions(origin, getPlayersColor());
        bestMove.setDestination(destination);
        return bestMove;
    }

    private int getMinMax(Board board, PieceColor playersColor, int depth, int alpha, int beta) {
        if(depth == MAX_DEPTH) {
            return getBoardScore(board, playersColor);
        }
        try {
            if(playersColor == getPlayersColor()) {
                return getMax(board, getOtherPlayersColor(playersColor), depth, alpha, beta);
            } else {
                return getMin(board, getOtherPlayersColor(playersColor), depth, alpha, beta);
            }
        } catch (NoSuchElementException e) { // todo might need to pass getOtherPlayersColor
            return getBoardScore(board, playersColor);
        }
    }

    private int getMin(Board board, PieceColor playersColor, int depth, int alpha, int beta) {
        int min = Integer.MAX_VALUE;
        List<Point> piecesPositions = getPiecePositions(board, playersColor);
        for (Point piecePosition: piecesPositions) {
            Piece piece = getPiece(board, piecePosition);
            assert piece != null;
            for (Point destination : piece.getLegalMoves(board, piecePosition)) {
                Piece pieceAtDestination = getPiece(board, destination);
                Piece pieceForEnpassant = getPiece(board, new Point(destination.x, piecePosition.y));
                if (hasKingAtDestination(board, destination)) {
                    return getBoardScore(board, playersColor);
                }
                board.updateTile(piecePosition, null);
                board.updateTile(destination, piece);
                if (wasEnpassant(board, piecePosition, destination, pieceAtDestination)) {
                    board.updateTile(new Point(destination.x, piecePosition.y), null);
                }
                int score = getMinMax(board, playersColor, depth + 1, alpha, beta);
                min = Integer.min(min, score);
                beta = Integer.min(beta, min);
                if (alpha >= beta) return min;
                board.updateTile(piecePosition, piece);
                board.updateTile(destination, pieceAtDestination);
                board.updateTile(new Point(destination.x, piecePosition.y), pieceForEnpassant);
            }
        }
        return min;
    }

    private int getMax(Board board, PieceColor playersColor, int depth, int alpha, int beta) {
        int max = Integer.MIN_VALUE;
        List<Point> piecesPositions = getPiecePositions(board, playersColor);
        for (Point piecePosition: piecesPositions) {
            Piece piece = getPiece(board, piecePosition);
            assert piece != null;
            for (Point destination : piece.getLegalMoves(board, piecePosition)) {
                Piece pieceAtDestination = getPiece(board, destination);
                Piece pieceForEnpassant = getPiece(board, new Point(destination.x, piecePosition.y));
                if (hasKingAtDestination(board, destination)) {
                    return getBoardScore(board, playersColor);
                }
                board.updateTile(piecePosition, null);
                board.updateTile(destination, piece);
                if (wasEnpassant(board, piecePosition, destination, pieceAtDestination)) {
                    board.updateTile(new Point(destination.x, piecePosition.y), null);
                }
                int score = getMinMax(board, playersColor, depth + 1, alpha, beta);
                max = Integer.max(max, score);
                beta = Integer.max(beta, max);
                if (alpha >= beta) return max;
                board.updateTile(piecePosition, piece);
                board.updateTile(destination, pieceAtDestination);
                board.updateTile(new Point(destination.x, piecePosition.y), pieceForEnpassant);
            }
        }
        return max;
    }

    private int getBoardScore(Board board, PieceColor currentPlayer) {
        List<Point> playersPositions = new ArrayList<>();
        List<Point> aiPositions = new ArrayList<>();
        int score = getScoreByPieces(board, playersPositions, aiPositions);
        int sumOfAiPossibleMoves = getPlayersPossibleMoves(board, aiPositions);
        int sumOfPlayersPossibleMoves = getPlayersPossibleMoves(board, playersPositions);
        score += ((sumOfAiPossibleMoves - sumOfPlayersPossibleMoves) / 50) * getMultiplier(currentPlayer);
        if(sumOfAiPossibleMoves == 0) {
            if(board.getKing(currentPlayer).isInCheck(board, 1)) {
                return Integer.MAX_VALUE * -1 * getMultiplier(currentPlayer);
            } else {
                return 0;
            }
        }
        return score;
    }

    private int getPlayersPossibleMoves(Board board, List<Point> piecePositions) {
        int sumOfMoves = 0;
        for (Point piecePosition: piecePositions) {
            Piece piece = getPiece(board, piecePosition);
            assert piece != null;
            sumOfMoves += piece.getLegalMoves(board, piecePosition).size();
        }
        return sumOfMoves;
    }

    private int getScoreByPieces(Board board, List<Point> playersPiecePositions, List<Point> aiPiecePositions) {
        int score = 0;
        for (Point piecePosition : playersPiecePositions) {
            Piece piece = getPiece(board, piecePosition);
            assert piece != null;
            score -= getPieceScore(piece.getPieceType());
        }
        for (Point piecePosition : aiPiecePositions) {
            Piece piece = getPiece(board, piecePosition);
            assert piece != null;
            score += getPieceScore(piece.getPieceType());
        }
        return score;
    }

    private int getPieceScore(PieceType piece) {
        switch (piece) {
            case PAWN:
                return  1;
            case ROOK:
                return  5;
            case KNIGHT:
            case BISHOP:
                return  3;
            case KING:
                return 999;
            case QUEEN:
                return  9;
            default:
                throw new Error("piece type not found " + piece);
        }
    }

    private int getMultiplier(PieceColor currentPlayer) {
        return (currentPlayer == getPlayersColor())? 1: -1;
    }

    private boolean hasKingAtDestination(Board board, Point destination) {
        Piece piece = getPiece(board, destination);
        return piece instanceof King;
    }

    private List<Point> getPiecePositions(Board board, PieceColor playersColor) {
        final int MAX_PIECES = 16;
        int pieceCount = 0;
        List<Point> positions = new ArrayList<>();
        for (int y = 0; y < SUM_OF_ROWS; y++) {
            for (int x = 0; x < SUM_OF_COLUMNS; x++) {
                if(pieceCount == MAX_PIECES) return positions;
                try {
                    Piece piece = board.getPiece(new Point(x, y));
                    if(playersColor == piece.getPieceColor()) {
                        pieceCount++;
                        positions.add(new Point(x, y));
                    }
                } catch (NoPieceFoundException ignored) {}
            }
        }
        return positions;
    }

    private Piece getPiece(Board board, Point origin) {
        try {
            return board.getPiece(origin);
        } catch (NoPieceFoundException e) {
            return null;
        }
    }

    private PieceColor getOtherPlayersColor(PieceColor playersColor) {
        return (playersColor == PieceColor.BLACK)? PieceColor.WHITE: PieceColor.BLACK;
    }

    @Override
    public void addPositionToMove(Point position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Positions getMove() {
        throw new UnsupportedOperationException();
    }

}