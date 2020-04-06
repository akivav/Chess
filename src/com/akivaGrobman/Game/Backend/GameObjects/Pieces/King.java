package com.akivaGrobman.Game.Backend.GameObjects.Pieces;

import com.akivaGrobman.Game.Backend.Exceptions.IllegalMoveException;
import com.akivaGrobman.Game.Backend.Exceptions.MoveWillPutKingInCheck;
import com.akivaGrobman.Game.Backend.Exceptions.NoPieceFoundException;
import com.akivaGrobman.Game.Backend.GameObjects.Board;
import com.akivaGrobman.Game.Backend.GameRules.CheckChecker;

import java.awt.*;
import java.util.List;
import static com.akivaGrobman.Game.Backend.GameRules.BoardConditionsChecker.*;

public class King extends Piece implements PieceMoves {

    private boolean wasInCheck;

    public King(Point position, PieceColor color) {
        super(position, PieceType.KING, color);
        wasInCheck = false;
    }

    @Override
    public void move(Point destinationsPosition, Board board) throws IllegalMoveException {
        this.board = board;
        if (isLegalMove(destinationsPosition)) {
            super.move(destinationsPosition, board);
        }  else {
            throw new IllegalMoveException(getClass().getSimpleName(), getPiecePosition(), destinationsPosition);
        }
    }

    @Override
    public Piece getClone() {
        King king = new King((Point) getPiecePosition().clone(), getPieceColor());
        king.wasInCheck = this.wasInCheck;
        return king;
    }

    @Override
    public String getPieceInString() {
        return " " + getPieceType() + " ";
    }

    public boolean isInCheck(Board board, int depth) {
        return CheckChecker.kingIsInCheck(getPieceColor(), board, depth);
    }

    private boolean isLegalMove(Point destination) throws IllegalMoveException {
        Point tempDestination = new Point(getPiecePosition());
        Point direction = getDirection(destination);
        tempDestination.x += direction.x;
        tempDestination.y += direction.y;
        if(!isInBounds(tempDestination)) {
            return false;
        }
        return destination.equals(tempDestination) && canMoveThere(tempDestination, getPieceColor());
    }

    private Point getDirection(Point destination) {
        Point direction = new Point();
        if(getPiecePosition().x == destination.x) {
            direction.x = 0;
        } else {
            direction.x = (Math.min(getPiecePosition().x, destination.x) == getPiecePosition().x)? 1 : -1;
        }
        if (getPiecePosition().y == destination.y) {
            direction.y = 0;
        } else {
            direction.y = (Math.min(getPiecePosition().y, destination.y) == getPiecePosition().y)? 1 : -1;
        }
        return direction;
    }

    @Override
    public List<Point> getLegalMoves() {
        return null;
    }

}
