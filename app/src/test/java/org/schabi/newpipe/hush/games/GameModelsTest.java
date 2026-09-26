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

    @Test public void moveEventsMatchCommittedBoardAndCannotMutateIt() {
        final GameModels.Twenty48 game=new GameModels.Twenty48(new Random(3));
        Arrays.fill(game.board,0);game.board[0]=2;game.board[1]=2;
        GameModels.Twenty48.MoveResult move=game.moveDetailed(0);
        assertTrue(move.changed);assertEquals(4,move.scoreDelta);assertEquals(2,move.motions.size());
        assertTrue(move.motions.get(0).merged);assertEquals(0,move.motions.get(1).to);
        assertTrue(move.spawnIndex>=0);assertArrayEquals(game.board,move.after());
        int[] copy=move.after();copy[0]=4096;assertEquals(4,game.board[0]);
    }
    @Test public void invalid2048MoveDoesNotSpawnAndSaveRestoresUndo() {
        GameModels.Twenty48 game=new GameModels.Twenty48(new Random(3));
        Arrays.fill(game.board,0);game.board[0]=2;int[] before=game.board.clone();
        assertFalse(game.move(0));assertArrayEquals(before,game.board);
        assertTrue(game.move(1));GameModels.Twenty48 restored=new GameModels.Twenty48(new Random(5));
        restored.load(game.save());assertArrayEquals(game.board,restored.board);assertTrue(restored.undo());assertArrayEquals(before,restored.board);
    }
    @Test public void snakeQueuesOnlyOneTurnPerTickAndRestoresPaused() {
        GameModels.Snake game=new GameModels.Snake(new Random(2));game.turn(0);game.turn(2);
        assertEquals(0,game.nextDirection);game.paused=false;game.tick();assertEquals(0,game.direction);
        game.turn(3);assertEquals(3,game.nextDirection);
        GameModels.Snake restored=new GameModels.Snake(new Random(3));restored.load(game.save());
        assertTrue(restored.paused);assertEquals(game.body.toString(),restored.body.toString());
    }
    @Test public void sudokuPermutationsRemainUniqueAndCluesCannotChange() {
        GameModels.Sudoku game=new GameModels.Sudoku(new Random(9));
        for(int level=0;level<2;level++)for(int round=0;round<8;round++){
            game.newPuzzle(level);assertEquals(1,countSolutions(game.clues.clone(),2));
            for(int i=0;i<81;i++)if(game.clues[i]!=0){int clue=game.cells[i];game.set(i,clue%9+1,false);game.erase(i);assertEquals(clue,game.cells[i]);}
        }
    }
    @Test public void make24UsesExactFractionsAndUndoSurvivesSave() {
        GameModels.Make24 game=new GameModels.Make24(new Random(2));game.terms.clear();
        for(int n:new int[]{3,3,8,8})game.terms.add(new GameModels.Make24.Term(n,1,Integer.toString(n)));
        assertTrue(game.combine(2,1,3));assertTrue(game.combine(0,2,1));assertTrue(game.combine(0,1,3));assertTrue(game.solved);
        GameModels.Make24 restored=new GameModels.Make24(new Random(2));restored.load(game.save());assertTrue(restored.solved);
        assertTrue(restored.undo());assertFalse(restored.solved);assertEquals(2,restored.terms.size());
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
