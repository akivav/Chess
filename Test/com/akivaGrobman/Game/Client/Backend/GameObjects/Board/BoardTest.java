package com.akivaGrobman.Game.Client.Backend.GameObjects.Board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.*;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void cloneIsToFullDepth() throws Exception {
        Board clone = Board.getClone(board);

        assertEquals(board.toString(), clone.toString());
        assertEquals(board.getPiece(new Point(1,1)), clone.getPiece(new Point(1,1)));
        assertNotEquals(board.hashCode(), clone.hashCode());
        assertNotEquals(Arrays.deepHashCode(board.getBoard()), Arrays.deepHashCode(clone.getBoard()));
    }

}