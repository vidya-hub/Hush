package org.schabi.newpipe.hush.games;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameModelsTest {
    @Test public void twenty48MergesOnceAndUndoRestoresBoard() {
        final GameModels.Twenty48 game = new GameModels.Twenty48(new Random(4));
        Arrays.fill(game.board, 0);
        game.board[0] = 2;
        game.board[1] = 2;
        game.board[2] = 2;
        game.board[3] = 2;
        final int[] before = game.board.clone();
        assertTrue(game.move(0));
        assertEquals(4, game.board[0]);
        assertEquals(4, game.board[1]);
        assertEquals(8, game.score);
        assertTrue(game.undo());
        assertArrayEquals(before, game.board);
        assertFalse(game.undo());
    }

    @Test public void snakeRejectsReverseTurnAndPausesOnWall() {
        final GameModels.Snake game = new GameModels.Snake(new Random(2));
        game.turn(3);
        assertEquals(1, game.nextDirection);
        game.body.clear();
        game.body.add(8);
        game.body.add(9);
        game.body.add(10);
        game.direction = 1;
        game.nextDirection = 1;
        game.food = 200;
        game.paused = false;
        for (int i = 0; i < 8; i++) game.tick();
        assertFalse(game.alive);
        assertTrue(game.paused);
    }

    @Test public void sudokuUsesAUniqueSolutionAndHintFillsIt() {
        final GameModels.Sudoku game = new GameModels.Sudoku(new Random(1));
        assertEquals(1, countSolutions(game.clues.clone(), 2));
        game.selected = 2;
        game.hint();
        assertEquals(game.solution[2], game.cells[2]);
        assertFalse(game.complete());
        game.newPuzzle(1);
        assertEquals(1, countSolutions(game.clues.clone(), 2));
    }

    @Test public void generatedMake24RoundsAreAlwaysSolvable() {
        final GameModels.Make24 game = new GameModels.Make24(new Random(8));
        for (int i = 0; i < 40; i++) {
            assertTrue(GameModels.Make24.solvable(game.terms));
            assertFalse(game.hint().isEmpty());
            game.next();
        }
    }

    private int countSolutions(final int[] board, final int limit) {
        int selected = -1;
        int bestMask = 0;
        int bestCount = 10;
        for (int i = 0; i < 81; i++) {
            if (board[i] != 0) continue;
            final int row = i / 9;
            final int col = i % 9;
            int used = 0;
            for (int n = 0; n < 9; n++) {
                used |= 1 << board[row * 9 + n];
                used |= 1 << board[n * 9 + col];
                used |= 1 << board[(row / 3 * 3 + n / 3) * 9 + col / 3 * 3 + n % 3];
            }
            final int options = (~used) & 0x3FE;
            final int count = Integer.bitCount(options);
            if (count < bestCount) {
                bestCount = count;
                selected = i;
                bestMask = options;
            }
        }
        if (selected < 0) return 1;
        if (bestMask == 0) return 0;
        int found = 0;
        for (int digit = 1; digit <= 9; digit++) {
            if ((bestMask & 1 << digit) == 0) continue;
            board[selected] = digit;
            found += countSolutions(board, limit - found);
            board[selected] = 0;
            if (found >= limit) break;
        }
        return found;
    }
}
