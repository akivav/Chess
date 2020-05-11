package com.akivaGrobman.Game.Client;

import com.akivaGrobman.Game.Client.Backend.Exceptions.IllegalMoveException;
import com.akivaGrobman.Game.Client.Backend.Exceptions.NoPieceFoundException;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Board.Board;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Move;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.*;
import com.akivaGrobman.Game.Client.Backend.Players.Enemy;
import com.akivaGrobman.Game.Client.Backend.Players.Positions;
import com.akivaGrobman.Game.Client.Backend.Players.Player;
import com.akivaGrobman.Game.Client.Frontend.GraphicBoard;
import com.akivaGrobman.Game.Client.Frontend.PawnPromotionWindow;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.akivaGrobman.Game.Client.Backend.GameRules.SpecialMoves.*;

public class ChessGame {

    public static final int SUM_OF_ROWS = 8;
    public static final int SUM_OF_COLUMNS = 8;
    private final Board backendBoard;
    private final GraphicBoard onScreenBoard;
    private final List<Move> moves;
    private Player player;
    private Enemy enemy;
    private Player currentPlayer;
    private PieceType promotedPiece;

    public ChessGame(PieceColor playersColor, Enemy enemy) {
        backendBoard = new Board();
        moves = new ArrayList<>();
        setPlayers(playersColor, enemy);
        onScreenBoard = new GraphicBoard(backendBoard, this);
        if(player.getPlayersColor() == PieceColor.BLACK) {
            makeEnemyMove();
        }
    }

    private void setPlayers(PieceColor playersColor, Enemy enemy) {
        enemy.setContext(this);
        this.enemy = enemy;
        player = new Player(playersColor);
        player.setContext(this);
        if(player.getPlayersColor() == PieceColor.WHITE) {
            currentPlayer = player;
        } else {
            currentPlayer = this.enemy;
        }
    }

    public void tileClicked(Point tilePosition) {
        if(isLocalPlayer(currentPlayer)) {
            player.addPositionToMove(tilePosition);
            try {
                if(backendBoard.getPiece(tilePosition).getPieceColor() == currentPlayer.getPlayersColor()) {
                    onScreenBoard.resetTilesColor();
                    onScreenBoard.drawLegalTiles(backendBoard.getPiece(tilePosition).getLegalMoves(backendBoard, tilePosition));
                } else {
                    onScreenBoard.resetTilesColor();
                }
            } catch (NoPieceFoundException e) {
                onScreenBoard.resetTilesColor();
            }
        }
    }

    public void move(Positions positions, Player player) {
        if(!isLocalPlayer(player) || isLegalMove(positions)) {
            addMoveToMoveList(positions);
            updateBoards(positions);
            handleSpecialMoves(positions);
            if(isLocalPlayer(player)) {
                enemy.sendMove(positions);
            }
            changeCurrentPlayer();
            if(enemyKingIsInCheck(positions.getDestination())) {
                putKingInCheck(currentPlayer);
            }
            if(isLocalPlayer(player)) {
                Thread sendMessage = new Thread(this::makeEnemyMove);
                sendMessage.start();
            }
        }
    }

    private boolean enemyKingIsInCheck(Point movedPiecePosition) {
        try {
            return backendBoard.isLegalMove(movedPiecePosition, backendBoard.getKingPosition(backendBoard.getPiece(movedPiecePosition).getPieceColor()), 1);
        } catch (IllegalMoveException | NoPieceFoundException e) {
            return false;
        }
    }

    private void putKingInCheck(Player currentPlayer) {
        King playersKing = backendBoard.getKing(currentPlayer.getPlayersColor());
        playersKing.setToIsInCheck();
    }

    private void addMoveToMoveList(Positions positions) {
        Piece piece;
        try {
            piece = backendBoard.getPiece(positions.getDestination());
        } catch (NoPieceFoundException e) {
            piece = null;
        }
        moves.add(new Move(positions, piece));
    }

    private void updateBoards(Positions positions) {
        Piece piece = getPiece(positions.getOrigin());
        assert piece != null; // if piece is null then the move shouldn't be legal
        // for castling
        if(piece instanceof King) {
            ((King) piece).moved();
        } else if(piece instanceof Rook) {
            ((Rook) piece).moved();
        }
        backendBoard.updateTile(positions.getOrigin(), null);
        backendBoard.updateTile(positions.getDestination(), piece);
        onScreenBoard.updateTile(positions.getDestination(), piece.getPieceType(), piece.getPieceColor());
        onScreenBoard.updateTile(positions.getOrigin(), null, null);
    }

    private void handleSpecialMoves(Positions positions) {
        updateEnpassantData(positions);
        if(wasEnpassant(backendBoard, moves)) {
            backendBoard.updateTile(new Point(positions.getDestination().x, positions.getOrigin().y), null);
            onScreenBoard.updateTile(new Point(positions.getDestination().x, positions.getOrigin().y), null, null);
        } else if(wasCastling(backendBoard, positions)) {
            int y = positions.getDestination().y;
            Piece rook;
            int originalX;
            int newX;
            if(positions.getDestination().x == 1) {
                originalX = 0;
                newX = 2;
            } else {
                originalX = 7;
                newX = 5;
            }
            rook = getPiece(new Point(originalX, positions.getDestination().y));
            assert rook != null; // will not be a castling move if piece is null
            backendBoard.updateTile(new Point(newX, y), rook);
            backendBoard.updateTile(new Point(originalX, y), null);
            onScreenBoard.updateTile(new Point(newX, y), PieceType.ROOK, positions.getPlayersColor());
            onScreenBoard.updateTile(new Point(originalX, y), null, null);
        } else if(wasPromotion(backendBoard, positions.getDestination())) {
            promotedPiece = null;
            System.out.println("promotion");
            Piece piece = null;
            new PawnPromotionWindow(this, positions.getPlayersColor(), onScreenBoard.getFrame());
            assert promotedPiece != null;
            try {
                switch (promotedPiece) {
                    case ROOK:
                        piece = new Rook(backendBoard.getPiece(positions.getDestination()).getPieceColor());
                        break;
                    case QUEEN:
                        piece = new Queen(backendBoard.getPiece(positions.getDestination()).getPieceColor());
                        break;
                    case BISHOP:
                        piece = new Bishop(backendBoard.getPiece(positions.getDestination()).getPieceColor());
                        break;
                    case KNIGHT:
                        piece = new Knight(backendBoard.getPiece(positions.getDestination()).getPieceColor());
                        break;
                    default:
                        throw new Error("wrong type " + promotedPiece);
                }
            } catch (NoPieceFoundException ignored) {}
            assert piece != null;
            backendBoard.updateTile(positions.getDestination(), piece);
            onScreenBoard.updateTile(positions.getDestination(), promotedPiece, piece.getPieceColor());
        }
    }

    private void updateEnpassantData(Positions positions) {
        try {
            Piece piece = backendBoard.getPiece(positions.getDestination());
            if(piece instanceof Pawn) {
                if(Math.abs(positions.getOrigin().y - positions.getDestination().y) == 2) {
                    backendBoard.setEnpassant(piece.getPieceColor(), positions.getDestination().x);
                }
            }
            backendBoard.resetOpponentsEnpassant(piece.getPieceColor());
        } catch (NoPieceFoundException e) {
            e.printStackTrace();
        }
    }

    private void makeEnemyMove() {
        move(enemy.getMove(), enemy);
    }

    private boolean isLegalMove(Positions positions) {
        try {
            if(player.getPlayersColor() != backendBoard.getPiece(positions.getOrigin()).getPieceColor()) return false;
            int STARTING_DEPTH = 1;
            return backendBoard.isLegalMove(positions.getOrigin(), positions.getDestination(), STARTING_DEPTH);
        } catch (IllegalMoveException | NoPieceFoundException e) {
            String msg = e.getMessage();
            System.out.println(msg);
            return false;
        }
    }

    public Piece getPiece(Point position) {
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

    private boolean isLocalPlayer(Player player) {
        return this.player.equals(player);
    }

    public PieceColor getPlayersColor() {
        return player.getPlayersColor();
    }

    public void promotionClick(PieceType pieceType) {
        promotedPiece = pieceType;
    }
}

