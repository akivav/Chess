package com.akivaGrobman.Game.Client.Backend.GameRules;

import com.akivaGrobman.Game.Client.Backend.GameObjects.Board.Board;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.PieceColor;
import com.akivaGrobman.Game.Client.Backend.GameObjects.Pieces.Rook;
import org.junit.jupiter.api.Test;
import java.awt.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckCheckerTest {

    @Test
    void willReturnTrueIfMovePutsKingInCheck() {
        Rook enemyRook = new Rook(new Point(4, 1), PieceColor.WHITE);
        Board board = Board.getConsumeBoard(List.of(enemyRook), List.of(new Point(4, 1)));
        boolean isInCheck;

        isInCheck = CheckChecker.kingIsInCheck(PieceColor.BLACK, board, 1);

        assertTrue(isInCheck);
    }

}