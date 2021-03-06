package com.akivaGrobman.Game.Client.Backend.GameRules;

import com.akivaGrobman.Game.Client.Backend.GameObjects.Board.Board;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Move;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.King;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.Pawn;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.PieceColor;
import com.akivaGrobman.Game.Client.Backend.Players.Positions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.akivaGrobman.Game.Client.Backend.GameRules.SpecialMoves.*;
import static org.junit.jupiter.api.Assertions.*;

class SpecialMovesTest {

    @Test
    void willDetectAnEnpassantMove() {
        boolean wasEnpassant;
        Board board;
        List<Move> moves = new ArrayList<>();
        Pawn pawn1 = new Pawn(PieceColor.WHITE);
        Pawn pawn2 = new Pawn(PieceColor.BLACK);
        board = Board.getConsumeBoard(List.of(pawn1, pawn2), List.of(new Point(1, 2), new Point(1, 3)), new ArrayList<>());
        Positions positions = new Positions(new Point(2, 3), PieceColor.WHITE);
        positions.setDestination(new Point(1, 2));
        moves.add(new Move(positions, null));

        wasEnpassant = wasEnpassant(board, moves.get(moves.size() - 1).getPositions().getOrigin(), moves.get(moves.size() - 1).getPositions().getDestination(), moves.get(moves.size() - 1).getPieceAtDestination());

        assertTrue(wasEnpassant);
    }

    @Test
    void willDetectACastlingMove() {
        King king = new King(1, PieceColor.BLACK);
        Board board = Board.getConsumeBoard(Collections.singletonList(king), Collections.singletonList(new Point(1, 0)), List.of(new Point(4, 0), new Point(3, 0), new Point(2, 0)));
        List<Move> moves = new ArrayList<>();
        Positions positions = new Positions(new Point(4, 0), PieceColor.BLACK);
        positions.setDestination(new Point(1, 0));
        moves.add(new Move(positions, king));
        boolean wasCastlingMove;

        wasCastlingMove = wasCastling(board, moves.get(moves.size() - 1).getPositions());

        assertTrue(wasCastlingMove);
    }

    @Test
    void willDetectAPawnPromotion() {
        boolean wasPromotionForPawnOne;
        boolean wasPromotionForPawnTwo;
        Pawn promotedPawn = new Pawn(PieceColor.WHITE);
        Pawn misplacedPawn = new Pawn(PieceColor.BLACK);
        Board board = Board.getConsumeBoard(List.of(promotedPawn, misplacedPawn), List.of(new Point(2, 0), new Point(1, 0)), List.of(new Point(2,1), new Point(2, 6)));

        wasPromotionForPawnOne = wasPromotion(board, new Point(2, 0));
        wasPromotionForPawnTwo = wasPromotion(board, new Point(1, 0));

        assertTrue(wasPromotionForPawnOne);
        assertFalse(wasPromotionForPawnTwo);
    }

}